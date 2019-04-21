package me.icocoro.quickstart.streaming.sql;

import me.icocoro.quickstart.streaming.ObjectSchema;
import me.icocoro.quickstart.streaming.POJO;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.io.jdbc.JDBCAppendTableSink;
import org.apache.flink.api.java.io.jdbc.JDBCOutputFormat;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

import javax.annotation.Nullable;
import java.util.Properties;

/**
 * 消费Kafka流数据，转为Table使用SQL进行分组统计，再转为Append流并将Row数据写入JDBC数据库表中
 */
public class KafkaStreamToJDBCTable {
    private static final String LOCAL_KAFKA_BROKER = "localhost:9092";
    private static final String GROUP_ID = KafkaStreamToJDBCTable.class.getSimpleName();
    private static final String topic = "testPOJO";

    private final static AscendingTimestampExtractor extractor = new AscendingTimestampExtractor<POJO>() {
        private static final long serialVersionUID = -904965568992964982L;

        @Override
        public long extractAscendingTimestamp(POJO element) {
            return element.getLogTime() + 8 * 60 * 60 * 1000;
        }
    };

    private static class CustomWatermarkExtractor implements AssignerWithPeriodicWatermarks<POJO> {

        private static final long serialVersionUID = -742759155861320823L;

        private long currentTimestamp = Long.MIN_VALUE;

        @Override
        public long extractTimestamp(POJO element, long previousElementTimestamp) {
            this.currentTimestamp = element.getLogTime();
            return element.getLogTime();
        }

        @Nullable
        @Override
        public Watermark getCurrentWatermark() {
            return new Watermark(currentTimestamp == Long.MIN_VALUE ? Long.MIN_VALUE : currentTimestamp - 1);
        }
    }

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);
        // 要设置Checkpoint才能将数据保存到外部系统？
        env.enableCheckpointing(5000);
        env.getConfig().disableSysoutLogging();
        env.getConfig().setRestartStrategy(RestartStrategies.fixedDelayRestart(4, 10000));

        final StreamTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        Properties kafkaProps = new Properties();
        kafkaProps.setProperty("bootstrap.servers", LOCAL_KAFKA_BROKER);
        kafkaProps.setProperty("group.id", GROUP_ID);
        kafkaProps.setProperty("auto.offset.reset", "earliest");

//        FlinkKafkaConsumer011<POJO> consumer = new FlinkKafkaConsumer011<>(topic, new POJOSchema(), kafkaProps);
        FlinkKafkaConsumer011<POJO> consumer = new FlinkKafkaConsumer011<>(topic, new ObjectSchema<>(POJO.class), kafkaProps);

        DataStream<POJO> pojoDataStream = env
                .addSource(consumer)
                // public SingleOutputStreamOperator<T> assignTimestampsAndWatermarks(AssignerWithPeriodicWatermarks<T> timestampAndWatermarkAssigner)
                // 要把SingleOutputStreamOperator返回给pojoDataStream
                .assignTimestampsAndWatermarks(new CustomWatermarkExtractor());

//        pojoDataStream.print();

        tableEnv.registerDataStream("t_pojo", pojoDataStream, "aid, astyle, energy, age, rowtime.rowtime");

        String query =
//                "SELECT astyle, HOP_START(rowtime, INTERVAL '10' SECOND, INTERVAL '10' SECOND) time_start, HOP_END(rowtime, INTERVAL '10' SECOND, INTERVAL '10' SECOND) time_end, SUM(energy) AS sum_energy, CAST(COUNT(aid) AS INT) AS cnt, CAST(AVG(age) AS INT) AS avg_age FROM t_pojo GROUP BY HOP(rowtime, INTERVAL '10' SECOND, INTERVAL '10' SECOND), astyle";
                "SELECT astyle, TUMBLE_START(rowtime, INTERVAL '10' SECOND) time_start, TUMBLE_END(rowtime, INTERVAL '10' SECOND) time_end, SUM(energy) AS sum_energy, CAST(COUNT(aid) AS INT) AS cnt, CAST(AVG(age) AS INT) AS avg_age FROM t_pojo GROUP BY TUMBLE(rowtime, INTERVAL '10' SECOND), astyle";

        Table table = tableEnv.sqlQuery(query);

        TypeInformation[] FIELD_TYPES = new TypeInformation[]{
                Types.STRING,
                Types.SQL_TIMESTAMP,
                Types.SQL_TIMESTAMP,
                Types.BIG_DEC,
                Types.INT,
                Types.INT
        };

        JDBCAppendTableSink sink = JDBCAppendTableSink.builder()
                .setDrivername("com.mysql.jdbc.Driver")
                .setDBUrl("jdbc:mysql://127.0.0.1:3306/flink_demo?characterEncoding=utf8&useSSL=false")
                .setUsername("root")
                .setPassword("123456")
                .setQuery("INSERT INTO t_pojo (astyle,time_start,time_end,sum_energy,cnt,avg_age,day_date,topic,group_id) VALUES (?,?,?,?,?,?,CURRENT_DATE(),'" + topic + "','" + GROUP_ID + "')")
                .setParameterTypes(FIELD_TYPES)
                .build();

        DataStream<Row> dataStream = tableEnv.toAppendStream(table, Row.class, tableEnv.queryConfig());
        // 可以正常入库
        sink.emitDataStream(dataStream);

//        dataStream.print();

//        final JDBCOutputFormat jdbcOutputFormat = createJDBCOutputFormat();
        // 并不会写到数据库表中
//        dataStream.writeUsingOutputFormat(jdbcOutputFormat);

        // Oracle需要注意字段名称加上双引号
//        JDBCAppendTableSink sink2 = JDBCAppendTableSink.builder()
//                .setDrivername("oracle.jdbc.driver.OracleDriver")
//                .setDBUrl("jdbc:oracle:thin:@127.0.0.1:1521:schemaname")
//                .setUsername("username")
//                .setPassword("password")
//                .setQuery("INSERT INTO t_pojo (\"astyle\",\"time_start\",\"time_end\",\"sum_energy\",\"cnt\",\"avg_age\",\"day_date\",\"topic\",\"group_id\") VALUES (?,?,?,?,?,?,CURRENT_DATE(),'" + topic + "','" + GROUP_ID + "')")
//                .setParameterTypes(FIELD_TYPES)
//                .build();

        env.execute();
    }

    // JDBCOutputFormat
    private static JDBCOutputFormat createJDBCOutputFormat() {
        return JDBCOutputFormat.buildJDBCOutputFormat()
                .setDBUrl(String.format("jdbc:mysql://127.0.0.1:3306/flink_demo?characterEncoding=utf8&useSSL=false"))
                .setDrivername("com.mysql.jdbc.Driver")
                .setUsername("root")
                .setPassword("123456")
                .setQuery(String.format("INSERT INTO t_pojo (astyle,time_start,time_end,sum_energy,cnt,avg_age,day_date,topic,group_id) VALUES (?,?,?,?,?,?,CURRENT_DATE(),'" + topic + "','" + GROUP_ID + "')"))
                .setSqlTypes(new int[]{
                        java.sql.Types.VARCHAR,
                        java.sql.Types.TIMESTAMP,
                        java.sql.Types.TIMESTAMP,
                        java.sql.Types.DECIMAL,
                        java.sql.Types.INTEGER,
                        java.sql.Types.INTEGER})
                .finish();
    }
}
