package me.icocoro.quickstart.streaming.sql;

import com.google.gson.Gson;
import me.icocoro.quickstart.streaming.test.POJO;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.Properties;

/**
 * 模拟往Kafka实时发送消息数据
 */
public class MessageProducer {
    private static final String LOCAL_KAFKA_BROKER = "192.168.13.219:9092,192.168.13.220:9092,192.168.13.221:9092";

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

        for (int i = 0; i < 15; i++) {
            String msg = "15:05:27.099 fastpay [DubboServerHandler-192.168.13.44:20886-thread-199] INFO  c.s.e.c.s.p.impl.PayTradeManagerImpl - EXTRACT——INSERT_PAY_PAYMENT_INFO——{\"amount\":1.25,\"authId\":\"test12320190220163148\",\"bankId\":\"123\",\"bankType\":\"CCB\",\"cardNo\":\"oLNRpclgt0INPlRQeqOTTm22yAQjiCuDTcB0eNC+3n4=\",\"cardType\":\"0\",\"channelType\":\"03\",\"createTime\":\"20190620150527\",\"payCode\":\"90101\",\"payMethod\":\"fastpay\",\"paySerialNo\":\"p2204910112" + (i) + "\",\"payStatus\":\"01\",\"payType\":\"1\",\"proCode\":\"20601\",\"systemEnvFlag\":\"test\",\"tradePayId\":\"t22026908\"}\n";

            producer.send(new ProducerRecord<String, String>("extract-logs", Integer.toString(0000), msg));
        }
//        for (int i = 0; i < 50; i++) {
//            POJO pojo = new POJO();
//            int j = (int) (Math.random() * 10);
//            pojo.setAid("ID000-" + j);
//            pojo.setAname("NAME-" + j);
//            pojo.setAstyle("STYLE000-" + j);
//            pojo.setEnergy(new BigDecimal(1000 * Math.random()).setScale(2, RoundingMode.HALF_UP));
//            pojo.setAge(j * 9);
//            long time = System.currentTimeMillis();
//            pojo.setTt(new Date(time));
//            pojo.setLogTime(time);
//
//            String value = gson.toJson(pojo);
//
//            producer.send(new ProducerRecord<String, String>("testPOJO", Integer.toString(i), value));
//
//            System.out.println(value);
//
//            Thread.sleep(500);
//        }

        producer.close();
    }
}

