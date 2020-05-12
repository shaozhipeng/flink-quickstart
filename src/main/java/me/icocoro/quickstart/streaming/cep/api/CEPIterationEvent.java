package me.icocoro.quickstart.streaming.cep.api;

import me.icocoro.quickstart.streaming.IngestionTimeExtractor;
import me.icocoro.quickstart.streaming.sql.POJOSchema;
import me.icocoro.quickstart.streaming.test.POJO;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;

import java.util.Properties;

public class CEPIterationEvent {
    private static final String LOCAL_KAFKA_BROKER = "localhost:9092";
    private static final String GROUP_ID = CEPIterationEvent.class.getSimpleName();
    private static final String GROUP_TOPIC = GROUP_ID;

    public static void main(String[] args) throws Exception {
        // 参数
        ParameterTool params = ParameterTool.fromArgs(args);

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 使用事件时间
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.enableCheckpointing(5000);

        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        env.getConfig().disableSysoutLogging();
        env.getConfig().setRestartStrategy(RestartStrategies.fixedDelayRestart(5, 10000));

        // 不使用POJO的时间
        final AssignerWithPeriodicWatermarks extractor = new IngestionTimeExtractor<POJO>();

        // 与Kafka Topic的Partition保持一致
        env.setParallelism(3);

        Properties kafkaProps = new Properties();
        kafkaProps.setProperty("bootstrap.servers", LOCAL_KAFKA_BROKER);
        kafkaProps.setProperty("group.id", GROUP_ID);

        // 接入Kafka的消息
        FlinkKafkaConsumer011<POJO> consumer = new FlinkKafkaConsumer011<>(GROUP_TOPIC, new POJOSchema(), kafkaProps);
        DataStream<POJO> pojoDataStream = env.addSource(consumer)
                .assignTimestampsAndWatermarks(extractor);

        pojoDataStream.print();

        // 根据astyle进行分组
        DataStream<POJO> keyedPojos = pojoDataStream
                .keyBy("astyle");

        // 计算energy在过去1分钟内的平均值，1分钟后的energy比平均值小50%，或者比平均值大30% 就匹配成功
        Pattern<POJO, POJO> completedPojo =
                Pattern.<POJO>begin("start")
                        .where(new SimpleCondition<POJO>() {
                            private static final long serialVersionUID = -6847788055093903603L;

                            @Override
                            public boolean filter(POJO pojo) throws Exception {
                                return "00".equals(pojo.getAstatus());
                            }
                        })
                        .times(1);


        env.execute(CEPIterationEvent.class.getSimpleName());
    }
}
