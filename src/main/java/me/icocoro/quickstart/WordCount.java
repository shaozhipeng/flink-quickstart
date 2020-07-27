package me.icocoro.quickstart;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.elasticsearch.ActionRequestFailureHandler;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer;
import org.apache.flink.streaming.connectors.elasticsearch6.ElasticsearchSink;
import org.apache.flink.util.Collector;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Requests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WordCount
 */
public class WordCount {

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
                text.flatMap(new Tokenizer())
                        // 根据word分组 对Integer求和
                        .keyBy(0).sum(1);

        List<HttpHost> httpHosts = new ArrayList<>();
        httpHosts.add(new HttpHost("192.168.13.72", 9200, "http"));
        httpHosts.add(new HttpHost("192.168.13.73", 9200, "http"));
        httpHosts.add(new HttpHost("192.168.13.74", 9200, "http"));

        ElasticsearchSink.Builder builder = new ElasticsearchSink.Builder<>(
                httpHosts,
                new ElasticsearchSinkFunction<Tuple2<String, Integer>>() {

                    private IndexRequest createIndexRequest(Tuple2<String, Integer> tuple2) {
                        Map<String, Object> json = new HashMap<>();
                        json.put("word", tuple2.f0+"中文");
                        json.put("cnt", tuple2.f1);

                        System.out.println("json: " + json);
                        return Requests.indexRequest()
                                .index("index-flink")
                                .type("wordcount")
                                .id((String) json.get("word")) // 主键拼接后做个md5
                                .source(json);
                    }

                    @Override
                    public void process(Tuple2<String, Integer> tuple2, RuntimeContext runtimeContext, RequestIndexer requestIndexer) {
                        requestIndexer.add(createIndexRequest(tuple2));
                    }

                    private static final long serialVersionUID = -5887591444785181096L;
                });

        builder.setFailureHandler(new ActionRequestFailureHandler() {

            private static final long serialVersionUID = 8549335619099881608L;

            @Override
            public void onFailure(ActionRequest actionRequest, Throwable throwable, int i, RequestIndexer requestIndexer) throws Throwable {

            }
        });

        builder.setBulkFlushMaxActions(1);

        counts.addSink(builder.build());

        // 输出结果
        if (params.has("output")) {
            // 输出到指定目录下的文件中
            counts.writeAsText(params.get("output"));
        } else {
            // 打印到控制台
            counts.print();
        }

        // 开始执行程序-设置一个Job名称
        env.execute("Streaming WordCount");
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
