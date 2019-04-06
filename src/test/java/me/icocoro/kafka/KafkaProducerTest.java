package me.icocoro.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Cluster;

import java.util.Map;
import java.util.Properties;

/**
 * KafkaProducerTest 程序实例
 * By shaozhipeng
 */
public class KafkaProducerTest {
    public static void main(String[] args) {
        Properties props = new Properties();
        // 必须指定bootstrap.servers，必须写明端口号，使用了域名，多个如kafka1:9092,kafka2:9092,kafka3:9092
        props.put("bootstrap.servers", "localhost:9092");
        // 必须指定序列化方式
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        // 控制producer生产消息的持久性 0 all/-1 1
        props.put("acks", "-1");
        // producer重试次数
        props.put("retries", 3);
        // 默认16384 多个消息放入batch中批量发送的大小，对于吞吐量和延时调优至关重要
        props.put("batch.size", 323840);
        // 控制消息延时发送行为，batch未满也可以发送
        props.put("linger.ms", 10);
        // 缓存消息的缓冲区大小
        props.put("buffer.memory", 33554432);
        props.put("max.block.ms", 3000);
        // 消息压缩设置
        props.put("compression.type", "lz4");
        // producer端能够发送的最大消息大小 默认1048576
        props.put("max.request.size", "10485760");
        // 请求超时时间 默认30s
        props.put("request.timeout.ms", "60000");
        // 自定义partition分区策略
//        props.put("partitioner.class", "KafkaProducerTestPartitioner");

        Producer<String, String> producer = new KafkaProducer<>(props);
        for (int i = 0; i < 100; i++) {
            // 同步发送
            producer.send(new ProducerRecord<String, String>("my-topic", Integer.toString(i), Integer.toString(i)));
            // 异步发送可以调用 Future<RecordMetadata> send(ProducerRecord<K, V> var1, Callback var2);
        }

        producer.close();
    }
}


/**
 * 可以自定义分区规则
 */
class KafkaProducerTestPartitioner implements Partitioner {

    @Override
    public int partition(String s, Object o, byte[] bytes, Object o1, byte[] bytes1, Cluster cluster) {
        return 0;
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> map) {

    }
}