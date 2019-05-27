package me.icocoro.quickstart.streaming.sink;

import com.google.gson.Gson;
import me.icocoro.quickstart.WordCount;
import me.icocoro.quickstart.WordCountData;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;
import org.apache.flink.util.Collector;

import java.io.Serializable;
import java.util.Properties;

public class KafkaSinkDemo {
    public static void main(String[] args) throws Exception {

        // 命令行参数
        final ParameterTool params = ParameterTool.fromArgs(args);

        // 执行环境-上下文
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.getConfig().setGlobalJobParameters(params);

        // 输入数据流
        DataStream<String> text;
        if (params.has("input")) {
            // 从指定路径下读取文件中的数据
            text = env.readTextFile(params.get("input"));
        } else {
            // 模拟数据
            text = env.fromElements(WordCountData.WORDS);
        }

        DataStream<Tuple2<String, Integer>> counts =
                // 将数据转换为(word,1)的形式
                text.flatMap(new WordCount.Tokenizer())
                        // 根据word分组 对Integer求和
                        .keyBy(0).sum(1);

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");

        // 将结果输出到Kafka
        counts.map(new MapFunction<Tuple2<String, Integer>, String>() {
            @Override
            public String map(Tuple2<String, Integer> e) throws Exception {
                return new Gson().toJson(new Word(e.f0, e.f1));
            }

            private static final long serialVersionUID = -7123696196337628777L;

        }).addSink(new FlinkKafkaProducer011<>("kafka-sink-demo", new SimpleStringSchema(), properties)).name("FlinkKafkaProducer011-0");

        // 开始执行程序-设置一个Job名称
        env.execute("Streaming-" + KafkaSinkDemo.class.getSimpleName());
    }

    /**
     * String => Tuple2<String, Integer>
     */
    public static final class Tokenizer implements FlatMapFunction<String, Tuple2<String, Integer>> {

        private static final long serialVersionUID = 1052635571744713050L;

        @Override
        public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
            // 变小写，切分正则匹配出的单词
            String[] tokens = value.toLowerCase().split("\\W+");

            // 输出<String, Integer> Integer默认1 后面直接sum
            for (String token : tokens) {
                if (token.length() > 0) {
                    out.collect(new Tuple2<>(token, 1));
                }
            }
        }
    }
}

class Word implements Serializable {
    private static final long serialVersionUID = -7946599086375253493L;
    private String word;
    private Integer cnt;

    public Word(String word, Integer cnt) {
        this.word = word;
        this.cnt = cnt;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Integer getCnt() {
        return cnt;
    }

    public void setCnt(Integer cnt) {
        this.cnt = cnt;
    }
}
