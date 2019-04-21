package me.icocoro.quickstart.streaming.sql;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Time;
import java.util.Random;

public class SQLTester {

    /**
     * 起一个服务线程，不断的产生数据向外输出
     */
    private static void listenAndGenerateNumbers(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Socket clientSocket = serverSocket.accept();
            System.out.println("Accepted connection");

            Random random = new Random();
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            String rooms[] = new String[]{"living room", "kitchen", "outside", "bedroom", "attic"};

            for (int i = 0; i < 30; i++) {
                String room = rooms[random.nextInt(rooms.length)];
                double temp = random.nextDouble() * 30 + 20;
                // 逗号分隔的字符串
                out.println(room + "," + temp);
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
    private static final MapFunction<String, Tuple3<String, Double, Time>> mapFunction = new MapFunction<String, Tuple3<String, Double, Time>>() {

        private static final long serialVersionUID = 7989711013674682226L;

        @Override
        public Tuple3<String, Double, Time> map(String s) throws Exception {
            // data is: <roomname>,<temperature>
            String p[] = s.split(",");
            String room = p[0];
            Double temperature = Double.parseDouble(p[1]);
            // 当前时间
            Time creationDate = new Time(System.currentTimeMillis());
            return new Tuple3<>(room, temperature, creationDate);
        }
    };

    /**
     * 事件时间+28800000L
     */
    private final static AscendingTimestampExtractor extractor = new AscendingTimestampExtractor<Tuple3<String, Double, Time>>() {
        private static final long serialVersionUID = -6815003214365056610L;

        @Override
        public long extractAscendingTimestamp(Tuple3<String, Double, Time> element) {
            // 当前时间+28800000L
            return element.f2.getTime() + 28800000L;
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
        DataStream<Tuple3<String, Double, Time>> dataset = text
                .map(mapFunction)
                .assignTimestampsAndWatermarks(extractor);

        dataset.print();

        // Register it so we can use it in SQL
        // 注册为table
        tableEnv.registerDataStream("sensors", dataset, "room, temperature, creationDate, rowtime.rowtime");

        String query = "SELECT room, TUMBLE_END(rowtime, INTERVAL '10' SECOND), AVG(temperature) AS avgTemp, COUNT(1) AS cnt FROM sensors GROUP BY TUMBLE(rowtime, INTERVAL '10' SECOND), room";
        Table table = tableEnv.sqlQuery(query);

        // Just for printing purposes, in reality you would need something other than Row
        tableEnv.toAppendStream(table, Row.class).print();

        env.execute();
    }
}
