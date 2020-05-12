package me.icocoro.quickstart.streaming.cep.api;

import me.icocoro.quickstart.streaming.sql.POJOSchema;
import me.icocoro.quickstart.streaming.test.POJO;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 先滑动聚合再CEP(不使用within)
 */
public class HopCEPTest {
    private static final String LOCAL_KAFKA_BROKER = "localhost:9092";
    private static final String GROUP_ID = HopCEPTest.class.getSimpleName();
    private static final long TIME_OFFSET = 28800000;

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 使用IngestionTime作为EventTime
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.enableCheckpointing(5000);
        // Flink处理程序被cancel后，会保留Checkpoint数据，以便根据实际需要恢复到指定的Checkpoint处理
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        env.getConfig().disableSysoutLogging();
        env.getConfig().setRestartStrategy(RestartStrategies.fixedDelayRestart(5, 10000));

        final AscendingTimestampExtractor extractor = new AscendingTimestampExtractor<POJO>() {
            private static final long serialVersionUID = -904965568992964982L;

            @Override
            public long extractAscendingTimestamp(POJO element) {
                return System.currentTimeMillis() + TIME_OFFSET;
            }
        };

        Properties kafkaProps = new Properties();
        kafkaProps.setProperty("bootstrap.servers", LOCAL_KAFKA_BROKER);
        kafkaProps.setProperty("group.id", GROUP_ID);

        FlinkKafkaConsumer011<POJO> consumer = new FlinkKafkaConsumer011<>("testPOJO", new POJOSchema(), kafkaProps);
        DataStream<POJO> pojoDataStream = env.addSource(consumer)
                .assignTimestampsAndWatermarks(extractor);

//        pojoDataStream.print();

        final StreamTableEnvironment tEnv = StreamTableEnvironment.create(env);
        tEnv.registerDataStream("t_pojo", pojoDataStream, "aid, astyle, energy, age, rowtime.rowtime");

        // 30秒滑动一次
        Table results =
                tEnv.sqlQuery("SELECT HOP_START(rowtime, INTERVAL '30' SECOND, INTERVAL '1' MINUTE) AS wstart, HOP_END(rowtime, INTERVAL '30' SECOND, INTERVAL '1' MINUTE) AS wend, astyle, SUM(energy) AS total_energy, COUNT(aid) AS cnt FROM t_pojo GROUP BY astyle, HOP(rowtime, INTERVAL '30' SECOND, INTERVAL '1' MINUTE)");

        // 表转为流
        DataStream<Row> dataStream = tEnv.toAppendStream(results, Row.class);

        // 定义模式
        Pattern<Row, ?> pattern = Pattern.<Row>begin("begin")
                .where(new SimpleCondition<Row>() {

                    private static final long serialVersionUID = -532341937496271392L;

                    @Override
                    public boolean filter(Row value) throws Exception {
                        return Integer.valueOf(value.getField(4).toString()) > 2;
                    }
                });


        // 匹配
        PatternStream<Row> patternStream = CEP.pattern(
                dataStream.keyBy(new KeySelector<Row, String>() {
                    private static final long serialVersionUID = -3111948121768273301L;

                    @Override
                    public String getKey(Row row) throws Exception {
                        return row.getField(2).toString();
                    }
                }),
                pattern);

        // 检索
        DataStream<String> rowDataStream = patternStream.select((Map<String, List<Row>> listMap) -> {
            Row first = (Row) listMap.get("begin").get(0);
            return first.getField(0) + " " + first.getField(1) + " " + first.getField(2) + " " + " " + first.getField(3) + " " + first.getField(4);
        });

        rowDataStream.print();

        env.execute("Streaming-" + HopCEPTest.class.getSimpleName());
    }
}
