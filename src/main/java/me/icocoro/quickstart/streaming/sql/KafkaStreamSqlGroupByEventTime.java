package me.icocoro.quickstart.streaming.sql;

import me.icocoro.quickstart.streaming.POJO;
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
 * 根据事件时间窗口进行分组统计
 */
public class KafkaStreamSqlGroupByEventTime {
    private static final String LOCAL_KAFKA_BROKER = "localhost:9092";
    private static final String GROUP_ID = "GroupID4ET";

    private final static AscendingTimestampExtractor extractor = new AscendingTimestampExtractor<POJO>() {
        private static final long serialVersionUID = -904965568992964982L;

        @Override
        public long extractAscendingTimestamp(POJO element) {
            return element.getLogTime() + 8 * 60 * 60 * 1000;
        }
    };

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        final StreamTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        Properties kafkaProps = new Properties();
        kafkaProps.setProperty("bootstrap.servers", LOCAL_KAFKA_BROKER);
        kafkaProps.setProperty("group.id", GROUP_ID);
        kafkaProps.setProperty("auto.offset.reset", "earliest");

        FlinkKafkaConsumer011<POJO> consumer = new FlinkKafkaConsumer011<>("testPOJO", new POJOSchema(), kafkaProps);
        DataStream<POJO> pojoDataStream = env
                .addSource(consumer)
                // public SingleOutputStreamOperator<T> assignTimestampsAndWatermarks(AssignerWithPeriodicWatermarks<T> timestampAndWatermarkAssigner)
                // 要把SingleOutputStreamOperator返回给pojoDataStream
                .assignTimestampsAndWatermarks(extractor);

//        pojoDataStream.print();

        tableEnv.registerDataStream("t_pojo", pojoDataStream, "aid, astyle, energy, age, tt, rowtime.rowtime");

//        String query = "SELECT CURRENT_DATE, tt, rowtime from t_pojo";
        String query =
                "SELECT astyle, HOP_START(rowtime, INTERVAL '1' MINUTE, INTERVAL '1' DAY) time_start, HOP_END(rowtime, INTERVAL '1' MINUTE, INTERVAL '1' DAY) time_end, SUM(energy) AS sum_energy, COUNT(aid) AS cnt, AVG(age) AS avg_age FROM t_pojo WHERE tt>=CURRENT_DATE GROUP BY HOP(rowtime, INTERVAL '1' MINUTE, INTERVAL '1' DAY), astyle";
//                "SELECT astyle, TUMBLE_START(rowtime, INTERVAL '10' SECOND) time_start, TUMBLE_END(rowtime, INTERVAL '10' SECOND) time_end, SUM(energy) AS sum_energy, COUNT(aid) AS cnt, AVG(age) AS avg_age FROM t_pojo GROUP BY HOP(rowtime, INTERVAL '10' SECOND), astyle";

        Table table = tableEnv.sqlQuery(query);

        tableEnv.toAppendStream(table, Row.class).print();
//        tableEnv.toRetractStream(table, Row.class).print();

//        String query2 =
//                "SELECT astyle, TUMBLE_START(rowtime, INTERVAL '10' SECOND), TUMBLE_END(rowtime, INTERVAL '10' SECOND), MAX(energy) AS max_energy, MIN(energy) AS min_energy FROM t_pojo GROUP BY TUMBLE(rowtime, INTERVAL '10' SECOND), astyle";
//
//        Table table2 = tableEnv.sqlQuery(query2);
//        tableEnv.toRetractStream(table2, Row.class).print();

        env.execute();
    }
}
