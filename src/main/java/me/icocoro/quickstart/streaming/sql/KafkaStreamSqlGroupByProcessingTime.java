package me.icocoro.quickstart.streaming.sql;

import me.icocoro.quickstart.streaming.POJO;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

import java.util.Properties;

/**
 * 根据处理时间窗口进行分组统计-注意处理时间是没有+8小时的
 */
public class KafkaStreamSqlGroupByProcessingTime {
    private static final String LOCAL_KAFKA_BROKER = "localhost:9092";
    private static final String GROUP_ID = "GroupID4PT";

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties kafkaProps = new Properties();
        kafkaProps.setProperty("bootstrap.servers", LOCAL_KAFKA_BROKER);
        kafkaProps.setProperty("group.id", GROUP_ID);
        kafkaProps.setProperty("auto.offset.reset", "earliest");

        FlinkKafkaConsumer011<POJO> consumer = new FlinkKafkaConsumer011<>("testPOJO", new POJOSchema(), kafkaProps);
        DataStream<POJO> tradeOrderStream = env.addSource(consumer);

        tradeOrderStream.print();

        final StreamTableEnvironment tEnv = TableEnvironment.getTableEnvironment(env);
        tEnv.registerDataStream("t_pojo", tradeOrderStream, "aid, astyle, energy, age, proctime.proctime");

        Table results =
//                tEnv.sqlQuery("SELECT * FROM t_pojo");
//                tEnv.sqlQuery("SELECT TUMBLE_END(eTime, INTERVAL '5' SECOND) AS endTime, TUMBLE_START(eTime, INTERVAL '5' SECOND) AS startTime, astyle, SUM(energy) AS total_energy, COUNT(aid) AS cnt FROM t_pojo GROUP BY TUMBLE(eTime, INTERVAL '5' SECOND), astyle");
                tEnv.sqlQuery("SELECT HOP_START(proctime, INTERVAL '30' SECOND, INTERVAL '1' MINUTE) AS wstart, HOP_END(proctime, INTERVAL '30' SECOND, INTERVAL '1' MINUTE) AS wend, astyle, SUM(energy) AS total_energy, COUNT(aid) AS cnt FROM t_pojo GROUP BY astyle, HOP(proctime, INTERVAL '30' SECOND, INTERVAL '1' MINUTE)");

        // 表转为流
        tEnv.toRetractStream(results, Row.class).print();
//        tEnv.toAppendStream(results, Row.class).print();

        env.execute();
    }
}
