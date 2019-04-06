package me.icocoro.quickstart.streaming;

import com.alibaba.fastjson.JSON;
import org.apache.commons.net.ntp.TimeStamp;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

/**
 * KafkaToHBaseJob
 */
public class KafkaToHBaseJob {

    private static String zkServer = "localhost";
    private static String port = "2181";

    private static TableName tableName = TableName.valueOf("trade_pay_info");
    private static final String topic = "flink_topic3";

    public static void main(String[] args) {

        // 设置运行环境
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 设置检查点时间
        env.enableCheckpointing(1000);

        // 注意Kafka版本 extends FlinkKafkaConsumer010，反序列化为TradePayInfo
        DataStream<TradePayInfo> transaction = env.addSource(new FlinkKafkaConsumer011<TradePayInfo>(topic, new TradePayInfoSchema(), configByKafka()));
        // 纯粹做数据存储
        transaction.rebalance().map(new MapFunction<TradePayInfo, Object>() {
            private static final long serialVersionUID = 1L;

            public TradePayInfo map(TradePayInfo tradePayInfo) throws IOException {
                // tablename rowkey cf:field dateformat如20190403 这里的操作显然还有优化空间
                write2HBase("dateformat" + tradePayInfo.getTradePayId(), "baseinfo", "trade_pay_id", tradePayInfo.getTradePayId());
                write2HBase("dateformat" + tradePayInfo.getTradePayId(), "baseinfo", "trade_no", tradePayInfo.getTradeNo());
                write2HBase("dateformat" + tradePayInfo.getTradePayId(), "baseinfo", "trade_type", tradePayInfo.getTradeType());
                write2HBase("dateformat" + tradePayInfo.getTradePayId(), "baseinfo", "total_amount", tradePayInfo.getTotalAmount() + "");
                write2HBase("dateformat" + tradePayInfo.getTradePayId(), "baseinfo", "timestamp", tradePayInfo.getTimestamp() + "");
                return tradePayInfo;
            }
        }).print();

        try {
            env.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // 自定义反序列化Schema 消息msg为json串
    static class TradePayInfoSchema implements DeserializationSchema<TradePayInfo>, SerializationSchema<TradePayInfo> {

        private static final long serialVersionUID = -6141464537937744275L;

        @Override
        public TradePayInfo deserialize(byte[] bytes) throws IOException {
            System.out.printf("msg---- " + new String(bytes));
            // 这里最好做个判断，消息是不是符合预期，可返回null，而不是报错抛出
            return JSON.parseObject(new String(bytes), TradePayInfo.class);
        }

        @Override
        public boolean isEndOfStream(TradePayInfo tradePayInfo) {
            return false;
        }

        @Override
        public byte[] serialize(TradePayInfo tradePayInfo) {
            return JSON.toJSONString(tradePayInfo).getBytes();
        }

        @Override
        public TypeInformation<TradePayInfo> getProducedType() {
            return TypeExtractor.getForClass(TradePayInfo.class);
        }
    }

    // Kafka Consumer配置
    public static Properties configByKafka() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "groupid_flink");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        return props;
    }

    // 数据写入HBase中
    public static void write2HBase(String row, String cf, String column, String value) throws IOException {
        Configuration config = HBaseConfiguration.create();

        config.set("hbase.zookeeper.quorum", zkServer);
        config.set("hbase.zookeeper.property.clientPort", port);
        config.setInt("hbase.rpc.timeout", 30000);
        config.setInt("hbase.client.operation.timeout", 30000);
        config.setInt("hbase.client.scanner.timeout.period", 30000);

        Connection connect = ConnectionFactory.createConnection(config);
        Admin admin = connect.getAdmin();
        // 此处不要在代码里面创建HBase表，并发判断是有问题的，除非并发数为1 应提前使用hbase shell创建好
//        if (!admin.tableExists(tableName)) {
//            admin.createTable(new HTableDescriptor(tableName).addFamily(new HColumnDescriptor(cf)));
//        }
        Table table = connect.getTable(tableName);
        TimeStamp ts = new TimeStamp(new Date());
        Put put = new Put(Bytes.toBytes(row));
        put.addColumn(Bytes.toBytes(cf), Bytes.toBytes(column), Bytes.toBytes(value));
        table.put(put);
        table.close();
        connect.close();
    }
}
