package me.icocoro.quickstart.streaming.asyncio;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.java.StreamTableEnvironment;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Time;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AsyncIOJob {

    /**
     * 起一个服务线程，不断的产生数据向外输出
     */
    private static void listenAndGenerateNumbers(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Socket clientSocket = serverSocket.accept();
            System.out.println("Hahaha");

            Random random = new Random();
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            String types[] = new String[]{"T0001", "T0002", "T0003", "C0001", "C0002"};

            for (int i = 0; i < 10000; i++) {
                String type = types[random.nextInt(types.length)];
                double price = new BigDecimal(random.nextDouble() * 30 + 22).setScale(BigDecimal.ROUND_HALF_DOWN, 2).doubleValue();
                // 逗号分隔的字符串
                out.println(UUID.randomUUID().toString().replace("-", "") + "," + type + "," + price);
                Thread.sleep(random.nextInt(10) + 50);
            }

            System.out.println("Closing server");
            clientSocket.close();
            serverSocket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 转换数据，增加一列时间作为事件时间
     */
    private static final MapFunction<String, Tuple4<String, String, Double, Time>> mapFunction = new MapFunction<String, Tuple4<String, String, Double, Time>>() {

        private static final long serialVersionUID = 7989711013674682226L;

        @Override
        public Tuple4<String, String, Double, Time> map(String s) throws Exception {
            String p[] = s.split(",");
            String id = p[0];
            String type = p[1];
            Double price = Double.parseDouble(p[2]);
            // 当前时间
            Time creationDate = new Time(System.currentTimeMillis());
            return new Tuple4<>(id, type, price, creationDate);
        }
    };

    /**
     * 事件时间+28800000L
     */
    private final static AscendingTimestampExtractor extractor = new AscendingTimestampExtractor<Tuple4<String, String, Double, Time>>() {
        private static final long serialVersionUID = -6815003214365056610L;

        @Override
        public long extractAscendingTimestamp(Tuple4<String, String, Double, Time> element) {
            // 当前时间+28800000L
            return element.f3.getTime() + 28800000L;
        }
    };

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        final StreamTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        final int port = 3901;
        // 起一个Socket服务
        new Thread(() -> listenAndGenerateNumbers(port)).start();
        Thread.sleep(1000); // wait the socket for a little;

        // 从上面的Socket服务接收数据
        DataStream<String> text = env.socketTextStream("localhost", port, "\n");
        DataStream<Tuple4<String, String, Double, Time>> dataStream = text
                .map(mapFunction)
                .assignTimestampsAndWatermarks(extractor);

//        dataStream.print();
        DataStream<String> resultStream = AsyncDataStream.orderedWait(dataStream, new AsyncCassandraETLRequest(), 1000, TimeUnit.MILLISECONDS, 100);

        resultStream.print();

        env.execute();
    }

}