package me.icocoro.quickstart.streaming.test;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.UUID;

/**
 * StreamKafkaProducer
 */
public class StreamKafkaProducer {
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
        // 请求超时时间 默认30s
        props.put("request.timeout.ms", "60000");

        String topic = "flink_topic3";

        Producer<String, String> producer = new KafkaProducer<>(props);
        for (int i = 0; i < 100; i++) {
            // 同步发送
            // {"tradePayId":"0b661d90b-c3e5-4cdd-a47b-430dc7bb81d9","tradeNo":"t0","orderNo":"o0","tradeType":"T0","totalAmount":"0.0","timestamp":"1554370655637"}
            producer.send(new ProducerRecord<String, String>(topic, Integer.toString(i),
                    "{\"tradePayId\":\"" + (i + UUID.randomUUID().toString())
                            + "\",\"tradeNo\":\"" + ("t" + i)
                            + "\",\"orderNo\":\"" + ("o" + i)
                            + "\",\"tradeType\":\"" + ("T" + i)
                            + "\",\"totalAmount\":\"" + i * 100 * Math.random()
                            + "\",\"timestamp\":\"" + System.currentTimeMillis()
                            + "\"}"));
            // 异步发送可以调用 Future<RecordMetadata> send(ProducerRecord<K, V> var1, Callback var2);
        }

        producer.close();
    }
}
