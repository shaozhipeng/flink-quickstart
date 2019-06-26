package xuwei.tech.streaming.sink;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;

/**
 * 接收socket数据，把数据保存到redis中
 * <p>
 * list
 * <p>
 * lpush list_key value
 * <p>
 * Created by xuwei.tech on 2018/10/23.
 */
public class StreamingDemoToRedis {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<String> text = env.socketTextStream("localhost", 9000, "\n");

        //lpsuh l_words word

        //对数据进行组装,把string转化为tuple2<String,String>
        DataStream<Tuple2<String, String>> l_wordsData = text.map(new MapFunction<String, Tuple2<String, String>>() {
            private static final long serialVersionUID = -7085913438214006726L;

            @Override
            public Tuple2<String, String> map(String value) throws Exception {
                return new Tuple2<>("l_words", value);
            }
        });

        //创建redis的配置
        FlinkJedisPoolConfig conf = new FlinkJedisPoolConfig.Builder().setHost("localhost").setPort(6379).build();

        //创建redissink
        RedisSink<Tuple2<String, String>> redisSink = new RedisSink<>(conf, new MyRedisMapper());

//        l_wordsData.print();
        l_wordsData.addSink(redisSink);

        env.execute("StreamingDemoToRedis");
    }

    public static class MyRedisMapper implements RedisMapper<Tuple2<String, String>> {
        private static final long serialVersionUID = -4141424656353539776L;

        //表示从接收的数据中获取需要操作的redis key
        @Override
        public String getKeyFromData(Tuple2<String, String> data) {
            return data.f0;
        }

        //表示从接收的数据中获取需要操作的redis value
        @Override
        public String getValueFromData(Tuple2<String, String> data) {
            return data.f1;
        }

        @Override
        public RedisCommandDescription getCommandDescription() {
            return new RedisCommandDescription(RedisCommand.LPUSH);
        }
    }

}
