package me.icocoro.quickstart.streaming.asyncio;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import org.apache.flink.api.common.functions.MapFunction;
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
 * AsyncCassandraJob
 * 如果是做ETL，Cassandra用来做数据存储，Redis用来做数据预合并
 * 合并的目的是保证日志乱序的情况下最新的数据不会被旧的数据覆盖掉；而Cassandra可以做插入式更新
 * 预合并时需要能够区分新数据和旧数据的时间戳，基本逻辑就是来一条消息都去看一下是不是最新的，如果是最新的直接输出到下游；如果是旧的，把旧的数据中在新数据里没有的合并到新数据然后返回，输出到下游。
 * 设置一定的延迟时间作为过期时间，比如5分钟内没有新的消息验证新旧，Redis的键就过期掉，即每一次有消息验证新旧的同时把过期时间重置为5分钟。
 */
public class AsyncCassandraJob {
    private static String KAFKA_BROKER = "localhost:9092";
    private static String SIMPLE_NAME = AsyncCassandraJob.class.getSimpleName();

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
        DataStream dataStream = env.addSource(consumer).map(new MapFunction<String, POJOWrapper>() {
            @Override
            public POJOWrapper map(String s) throws Exception {
                // eventType和tableName来自消息，此处直接写上 注意使用LinkedTreeMap
                return new POJOWrapper("INSERT", "pojo", s, new Gson().fromJson(s, LinkedTreeMap.class));
            }

            private static final long serialVersionUID = -3960328094650787179L;
        });

        dataStream.print();

        // 只落库Cassandra 未与Redis预聚合整合在一块
        DataStream<String> resultStream = AsyncDataStream.unorderedWait(dataStream, new AsyncCassandraRequest(), 2, TimeUnit.MINUTES, 100);
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
