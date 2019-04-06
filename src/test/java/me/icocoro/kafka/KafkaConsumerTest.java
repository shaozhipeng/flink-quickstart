package me.icocoro.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;

/**
 * KafkaConsumerTest
 * By shaozhipeng
 */
public class KafkaConsumerTest {
    public static void main(String[] args) {
        String topicName = "my-topic";
        String groupID = "test-group";

        Properties props = new Properties();
        // 必须指定，多个时如kafka1:9092,kafka2:9092,kafka3:9092
        props.put("bootstrap.servers", "localhost:9092");
        // 默认是空字符串 要显示指定和业务相关的消费者组名称，空字符串会报错
        props.put("group.id", groupID);
        // 必须指定序列化方式，可自定义
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        // 从最早的消息开始读取
        props.put("auto.offset.reset", "earliest");
        props.put("zookeeper.session.timeout.ms", "400");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
        consumer.subscribe(Arrays.asList(topicName));

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(1000);
                for (ConsumerRecord record : records) {
                    System.out.println(record.topic() + "-" + record.partition() + "-" + record.value() + "-" + record.timestamp());
                }
            }
        } finally {
            consumer.close();
        }
    }
}
