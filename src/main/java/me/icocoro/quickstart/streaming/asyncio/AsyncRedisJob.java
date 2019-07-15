package me.icocoro.quickstart.streaming.asyncio;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * AsyncRedisJob
 */
public class AsyncRedisJob {
    private static String KAFKA_BROKER = "localhost:9092";
    private static String SIMPLE_NAME = AsyncRedisJob.class.getSimpleName();

    public static void main(String[] args) throws Exception {
        // 使用处理时间、使用Checkpoint、使用默认的从上一次提交的offset之后开始消费、需要注意程序变更时cancel & savepoint再run with savepoint
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 和kafka对应topic的分区数保持一致
        env.setParallelism(3);
        env.enableCheckpointing(5000);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(500);
        env.getCheckpointConfig().setCheckpointTimeout(60000);
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
        env.getCheckpointConfig().setFailOnCheckpointingErrors(true);
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        env.getConfig().disableSysoutLogging();
        env.getConfig().setRestartStrategy(RestartStrategies.fixedDelayRestart(5, 10000));

        Properties props = new Properties();
        props.setProperty("bootstrap.servers", KAFKA_BROKER);
        props.setProperty("group.id", SIMPLE_NAME);
        props.setProperty("auto.offset.reset", "latest");

        FlinkKafkaConsumer011<String> consumer = new FlinkKafkaConsumer011<>(SIMPLE_NAME, new SimpleStringSchema(), props);
        DataStream dataStream = env.addSource(consumer);

        DataStream<String> resultStream = AsyncDataStream.unorderedWait(dataStream, new AsyncRedisRequest(), 2, TimeUnit.MINUTES, 100);
        resultStream.addSink(new SinkFunction<String>() {
            private static final long serialVersionUID = -6092219651499836275L;

            @Override
            public void invoke(String value, Context context) throws Exception {
                // 这里如果没有实际输出，就什么都不做
                System.out.println("value: " + value);
            }
        });

        env.execute(SIMPLE_NAME);
    }
}
