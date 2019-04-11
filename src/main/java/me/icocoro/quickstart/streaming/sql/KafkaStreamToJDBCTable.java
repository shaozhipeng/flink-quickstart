package me.icocoro.quickstart.streaming.sql;

import me.icocoro.quickstart.streaming.POJO;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.io.jdbc.JDBCAppendTableSink;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

import java.util.Properties;

/**
 * 消费Kafka流数据，转为Table使用SQL进行分组统计，再转为Append流并将Row数据写入JDBC数据库表中
 */
public class KafkaStreamToJDBCTable {
    private static final String LOCAL_KAFKA_BROKER = "localhost:9092";
    private static final String GROUP_ID = KafkaStreamToJDBCTable.class.getSimpleName();

    private final static AscendingTimestampExtractor extractor = new AscendingTimestampExtractor<POJO>() {
        private static final long serialVersionUID = -904965568992964982L;

        @Override
        public long extractAscendingTimestamp(POJO element) {
            return element.getLogTime() + 8 * 60 * 60 * 1000;
        }
    };

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);
        // 要设置Checkpoint才能将数据保存到外部系统
        env.enableCheckpointing(1000);

        final StreamTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        Properties kafkaProps = new Properties();
        kafkaProps.setProperty("bootstrap.servers", LOCAL_KAFKA_BROKER);
        kafkaProps.setProperty("group.id", GROUP_ID);
        kafkaProps.setProperty("auto.offset.reset", "earliest");

        String topic = "testPOJO";

        FlinkKafkaConsumer011<POJO> consumer = new FlinkKafkaConsumer011<>(topic, new POJOSchema(), kafkaProps);
        DataStream<POJO> pojoDataStream = env
                .addSource(consumer)
                // public SingleOutputStreamOperator<T> assignTimestampsAndWatermarks(AssignerWithPeriodicWatermarks<T> timestampAndWatermarkAssigner)
                // 要把SingleOutputStreamOperator返回给pojoDataStream
                .assignTimestampsAndWatermarks(extractor);

//        pojoDataStream.print();

        tableEnv.registerDataStream("t_pojo", pojoDataStream, "aid, astyle, energy, age, rowtime.rowtime");

        String query =
                "SELECT astyle, HOP_START(rowtime, INTERVAL '10' SECOND, INTERVAL '10' SECOND) time_start, HOP_END(rowtime, INTERVAL '10' SECOND, INTERVAL '10' SECOND) time_end, SUM(energy) AS sum_energy, COUNT(aid) AS cnt, CAST(AVG(age) AS INT) AS avg_age FROM t_pojo GROUP BY HOP(rowtime, INTERVAL '10' SECOND, INTERVAL '10' SECOND), astyle";

        Table table = tableEnv.sqlQuery(query);

        TypeInformation[] FIELD_TYPES = new TypeInformation[]{
                Types.STRING,
                Types.SQL_TIMESTAMP,
                Types.SQL_TIMESTAMP,
                Types.BIG_DEC,
                Types.LONG,
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
        sink.emitDataStream(dataStream);

        env.execute();
    }
}
