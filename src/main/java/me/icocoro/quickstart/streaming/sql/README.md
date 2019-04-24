#### 启动本地Zookeeper和Kafka

#### MessageProducer#main

```bash
{"aid":"ID000-8","astyle":"STYLE000-8","aname":"NAME-8","logTime":1554861531221,"energy":3.92,"age":72}
{"aid":"ID000-4","astyle":"STYLE000-4","aname":"NAME-4","logTime":1554861532524,"energy":569.05,"age":36}
......
```

#### KafkaStreamSqlGroupByProcessingTime#main

```bash
3> POJO{aid='ID000-8', astyle='STYLE000-8', aname='NAME-8', logTime=1554861531221, energy=3.92, age=72}
3> POJO{aid='ID000-4', astyle='STYLE000-4', aname='NAME-4', logTime=1554861532524, energy=569.05, age=36}
......
3> (true,2019-04-10 02:01:00.0,2019-04-10 02:02:00.0,STYLE000-9,2056.38,4)
2> (true,2019-04-10 02:01:00.0,2019-04-10 02:02:00.0,STYLE000-8,1009.95,4)
......
```

#### KafkaStreamSqlGroupByEventTime#main

```bash
(true,STYLE000-8,2019-04-10 09:58:50.0,2019-04-10 09:59:00.0,228.98,3.92)
STYLE000-8,2019-04-10 09:58:50.0,2019-04-10 09:59:00.0,292.56,3,72
STYLE000-4,2019-04-10 09:58:50.0,2019-04-10 09:59:00.0,569.05,1,36
(true,STYLE000-4,2019-04-10 09:58:50.0,2019-04-10 09:59:00.0,569.05,569.05)
STYLE000-6,2019-04-10 09:58:50.0,2019-04-10 09:59:00.0,969.93,2,54
(true,STYLE000-6,2019-04-10 09:58:50.0,2019-04-10 09:59:00.0,513.46,456.47)
(true,STYLE000-9,2019-04-10 09:58:50.0,2019-04-10 09:59:00.0,460.99,232.41)
STYLE000-9,2019-04-10 09:58:50.0,2019-04-10 09:59:00.0,1093.26,3,81
(true,STYLE000-2,2019-04-10 09:59:00.0,2019-04-10 09:59:10.0,945.19,288.31)
(true,STYLE000-1,2019-04-10 09:59:00.0,2019-04-10 09:59:10.0,283.97,203.27)
(true,STYLE000-8,2019-04-10 09:59:00.0,2019-04-10 09:59:10.0,717.39,717.39)
(true,STYLE000-9,2019-04-10 09:59:00.0,2019-04-10 09:59:10.0,963.12,963.12)
(true,STYLE000-6,2019-04-10 09:59:00.0,2019-04-10 09:59:10.0,792.53,792.53)
(true,STYLE000-3,2019-04-10 09:59:00.0,2019-04-10 09:59:10.0,708.63,64.07)
(true,STYLE000-5,2019-04-10 09:59:00.0,2019-04-10 09:59:10.0,757.12,757.12)
STYLE000-2,2019-04-10 09:59:00.0,2019-04-10 09:59:10.0,1233.50,2,18
STYLE000-1,2019-04-10 09:59:00.0,2019-04-10 09:59:10.0,487.24,2,9
STYLE000-8,2019-04-10 09:59:00.0,2019-04-10 09:59:10.0,717.39,1,72
STYLE000-9,2019-04-10 09:59:00.0,2019-04-10 09:59:10.0,963.12,1,81
STYLE000-6,2019-04-10 09:59:00.0,2019-04-10 09:59:10.0,792.53,1,54
STYLE000-3,2019-04-10 09:59:00.0,2019-04-10 09:59:10.0,772.70,2,27
STYLE000-5,2019-04-10 09:59:00.0,2019-04-10 09:59:10.0,757.12,1,45
```

### [dynamic_tables](https://ci.apache.org/projects/flink/flink-docs-stable/dev/table/streaming/dynamic_tables.html)

#### registerDataStream

Registers the given [[DataStream]] as table with specified field names in the [[TableEnvironment]]'s catalog.
Registered tables can be referenced in SQL queries.
Example:  
DataStream<Tuple2<String, Long>> set = ...
tableEnv.registerDataStream("myTable", set, "a, b")

#### toAppendStream 附加流

Append Mode: 追加模式，这种模式只适用于当动态表仅由INSERT更改修改时，即仅附加，之前发射的结果不会被更新

Converts the given [[Table]] into an append [[DataStream]] of a specified type.
The [[Table]] must only have insert (append) changes. If the [[Table]] is also modified by update or delete changes, the conversion will fail.

The fields of the [[Table]] are mapped to [[DataStream]] fields as follows:
[[org.apache.flink.types.Row]] and [[org.apache.flink.api.java.tuple.Tuple]]
types: Fields are mapped by position, field types must match.
POJO [[DataStream]] types: Fields are mapped by field name, field types must match.

#### toRetractStream 撤销流

Retract Mode: 缩进或撤销模式，始终都可以使用此模式，它使用一个boolean标识来编码INSERT和DELETE更改

Converts the given [[Table]] into a [[DataStream]] of add and retract messages.
The message will be encoded as [[JTuple2]]. The first field is a [[JBool]] flag, the second field holds the record of the specified type [[T]].
A true [[JBool]] flag indicates an add message, a false flag indicates a retract message.

The fields of the [[Table]] are mapped to [[DataStream]] fields as follows:
[[org.apache.flink.types.Row]] and [[org.apache.flink.api.java.tuple.Tuple]]
types: Fields are mapped by position, field types must match.
POJO [[DataStream]] types: Fields are mapped by field name, field types must match.

A retract stream of type X is a DataStream<Tuple2<Boolean, X>>. 
The boolean field indicates the type of the change. 
True is INSERT, false is DELETE.

### [time_attributes](https://ci.apache.org/projects/flink/flink-docs-stable/dev/table/streaming/time_attributes.html)

#### time attributes: proctime
#### time attributes: rowtime

### [kafka-connector source](https://ci.apache.org/projects/flink/flink-docs-stable/dev/table/connect.html#kafka-connector)

Specify the start reading position: By default, the Kafka source will start reading data from the committed group offsets in Zookeeper or Kafka brokers. 

### [jdbcappendtablesink](https://ci.apache.org/projects/flink/flink-docs-stable/dev/table/connect.html#jdbcappendtablesink)

#### JDBCAppendTableSink

#### JDBCOutputFormat 无法保存数据，且会影响Stream本身数据的print输出

调用dataStream.writeUsingOutputFormat时，数据不会保存到数据库，而且影响到了dataStream.print();？？？

```java
DataStream<Row> dataStream = tableEnv.toAppendStream(table, Row.class, tableEnv.queryConfig());

dataStream.print();

final JDBCOutputFormat jdbcOutputFormat = createJDBCOutputFormat();
dataStream.writeUsingOutputFormat(jdbcOutputFormat);

env.execute();
```

消息数据源

#### DDL

astyle,time_start,time_end,sum_energy,cnt,avg_age,day_date,topic,group_id

```sql
CREATE TABLE `t_pojo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `astyle` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `time_start` timestamp NULL DEFAULT NULL,
  `time_end` timestamp NULL DEFAULT NULL,
  `sum_energy` decimal(15,2) DEFAULT NULL,
  `cnt` int(16) DEFAULT NULL,
  `avg_age` int(16) DEFAULT NULL,
  `day_date` date DEFAULT NULL,
  `topic` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `group_id` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

![image](http://images.icocoro.me/images/new/20190411.png)

### 如果只是纯粹输出流数据，是即时的，全部消息都会消费到（打印输出），说明FlinkSQL的window触发和销毁有点问题【需要调整Watermark？】
### Flink消费kafka不及时？

比如发送40条数据，窗口消费33条，另外7条，需要继续发送新的数据，才会被消费掉，即便重启程序-也要发送新的数据，才会消费上次"未及时"消费的数据。  
除非修改新的group_id后，会从头消费全部数据。

![image](http://images.icocoro.me/images/new/20190421000.png)

#### 不用显示创建Watermark 即默认-9223372036854775808 Long.MIN_VALUE

```java
new AscendingTimestampExtractor<POJO>() {
    @Override
    public long extractAscendingTimestamp(POJO> element) {
        return element.getLogTime() + CommConstant.TIME_OFFSET;
    }
}
```
##### 使用新的消费者组group.id在Flink程序启动时，可以从头消费数据，但会有预留数据消费不到；当有新的数据发送到Kafka时，上次预留的数据会被消费掉，而新的数据又会有预留
##### 消息流中新的数据，事件时间不是单调递增，这些比之前事件时间小的事件是不会被消费的 会提示：Timestamp monotony violated:
##### Flink程序重启后，仍需向Kafka发送新的数据，才会消费之前预留的数据，且会消费所有原来未消费的数据，包括上面（Timestamp monotony violated:）的数据

#### 使用currentMaxTimestamp - maxOutOfOrderness作为Watermark的时间戳

```java
// use currentMaxTimestamp - maxOutOfOrderness as timestamp of Watermark
private static class CustomWatermarkExtractor implements AssignerWithPeriodicWatermarks<POJO> {
    Long currentMaxTimestamp = 0L;
    final Long maxOutOfOrderness = 3500L;

    @Override
    public long extractTimestamp(POJO element, long l) {
        long timestamp = element.getLogTime() + CommConstant.TIME_OFFSET;
        currentMaxTimestamp = Math.max(timestamp, currentMaxTimestamp);
        return timestamp;
    }

    @Nullable
    @Override
    public Watermark getCurrentWatermark() {
        return new Watermark(currentMaxTimestamp - maxOutOfOrderness);
    }
}
```

##### 使用新的消费者组group.id在Flink程序启动时，可以从头消费数据，但会有预留数据消费不到；当有新的数据发送到Kafka时，上次预留的数据会被消费掉，而新的数据又会有预留
##### 消息流中新的数据，事件时间不是单调递增，这些比之前事件时间小的事件是不会被消费的
##### Flink程序重启后，有时仍需向Kafka发送新的数据，才会消费之前预留的数据，有时要发两批，且有数据丢失未被消费到
##### 总之是不确定的，不准确的；准确性失效！

#### 使用System.currentTimeMillis()作为Watermark的时间戳，时间使用默认的utc0，对外提供数据时注意转换即可；或者+8个小时

```java
// use System.currentTimeMillis() as timestamp of Watermark
private static class CustomWatermarkExtractor2 implements AssignerWithPeriodicWatermarks<POJO> {

    private static final long serialVersionUID = -742759155861320823L;

    @Override
    public long extractTimestamp(POJO element, long previousElementTimestamp) {
        return element.getLogTime() + TIME_OFFSET;
    }

    @Nullable
    @Override
    public Watermark getCurrentWatermark() {
        return new Watermark(System.currentTimeMillis() + TIME_OFFSET);
    }
}
```

##### 使用新的消费者组group.id在Flink程序启动时，不会消费Kafka任何已经存在的消息
##### 消息流中新的数据，事件时间不是单调递增，这些比之前事件时间小的事件是不会被消费的
##### 当有新的数据发送到Kafka时，之前从未被消费过的消息仍然不会被消费，而只是消费最新发送或最新接收到的消息【相当于only latest】
##### Flink程序重启后，同上不会消费任何未启动期间Kafka接收的新消息，而只会处理Flink程序启动后Kafka接收到的消息
##### 当然消息不会有预留，因为要么全部消费，要么直接消费不到了；容错失效！

[Stackoverflow](https://stackoverflow.com/questions/55499764/how-to-let-flink-flush-last-line-to-sink-when-producerkafka-does-not-produce-n/55525476#55525476)

### SQLTester

从Socket服务器接收数据，消费是即时的，数据可以消费完。

```java
private final static AscendingTimestampExtractor extractor = new AscendingTimestampExtractor<Tuple3<String, Double, Time>>() {
    private static final long serialVersionUID = -6815003214365056610L;

    @Override
    public long extractAscendingTimestamp(Tuple3<String, Double, Time> element) {
        // 当前时间+28800000L
        return element.f2.getTime() + 28800000L;
    }
}
```

#### 说明Kafka那里消费的问题并不是assignTimestampsAndWatermarks...

### rowtime和proctime时间晚8小时的问题

http://mail-archives.apache.org/mod_mbox/flink-user/201711.mbox/%3C351FD9AB-7A28-4CE0-BD9C-C2A15E5372D6@163.com%3E

https://github.com/apache/calcite  
org.apache.calcite.runtime.SqlFunctions

```java
/** Converts the internal representation of a SQL TIMESTAMP (long) to the Java
   * type used for UDF parameters ({@link java.sql.Timestamp}). */
  public static java.sql.Timestamp internalToTimestamp(long v) {
    return new java.sql.Timestamp(v - LOCAL_TZ.getOffset(v));
  }
```
