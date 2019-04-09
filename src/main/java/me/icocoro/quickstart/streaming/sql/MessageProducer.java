package me.icocoro.quickstart.streaming.sql;

import com.google.gson.Gson;
import me.icocoro.quickstart.streaming.POJO;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Properties;

public class MessageProducer {
    private static final String LOCAL_KAFKA_BROKER = "localhost:9092";

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put("bootstrap.servers", LOCAL_KAFKA_BROKER);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        props.put("acks", "-1");
        props.put("retries", 3);
        props.put("batch.size", 323840);
        props.put("linger.ms", 10);
        props.put("buffer.memory", 33554432);
        props.put("max.block.ms", 3000);
        props.put("request.timeout.ms", "60000");

        Producer<String, String> producer = new KafkaProducer<>(props);
        Gson gson = new Gson();

        for (int i = 0; i < 20; i++) {
            POJO pojo = new POJO();
            int j = (int) (Math.random() * 10);
            pojo.setAid("ID000-" + j);
            pojo.setAname("NAME-" + j);
            pojo.setAstyle("STYLE000-" + j);
            pojo.setEnergy(new BigDecimal(1000 * Math.random()).setScale(2, RoundingMode.HALF_UP));
            pojo.setAge(j * 9);
            long time = System.currentTimeMillis();
            pojo.setLogTime(time);

            String value = gson.toJson(pojo);

            producer.send(new ProducerRecord<String, String>("testPOJO", Integer.toString(i), value));

            System.out.println(value);

            Thread.sleep(1000);
        }

        producer.close();
    }
}

