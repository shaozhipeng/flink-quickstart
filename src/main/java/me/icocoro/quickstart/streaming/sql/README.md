#### 启动本地Zookeeper和Kafka

#### MessageProducer#main

```bash
{"aid":"ID000-8","astyle":"STYLE000-8","aname":"NAME-8","logTime":1554861531221,"energy":3.92,"age":72}
{"aid":"ID000-4","astyle":"STYLE000-4","aname":"NAME-4","logTime":1554861532524,"energy":569.05,"age":36}
{"aid":"ID000-9","astyle":"STYLE000-9","aname":"NAME-9","logTime":1554861533526,"energy":399.86,"age":81}
{"aid":"ID000-9","astyle":"STYLE000-9","aname":"NAME-9","logTime":1554861534528,"energy":460.99,"age":81}
{"aid":"ID000-8","astyle":"STYLE000-8","aname":"NAME-8","logTime":1554861535532,"energy":228.98,"age":72}
{"aid":"ID000-6","astyle":"STYLE000-6","aname":"NAME-6","logTime":1554861536535,"energy":513.46,"age":54}
{"aid":"ID000-8","astyle":"STYLE000-8","aname":"NAME-8","logTime":1554861537539,"energy":59.66,"age":72}
{"aid":"ID000-6","astyle":"STYLE000-6","aname":"NAME-6","logTime":1554861538544,"energy":456.47,"age":54}
{"aid":"ID000-9","astyle":"STYLE000-9","aname":"NAME-9","logTime":1554861539549,"energy":232.41,"age":81}
{"aid":"ID000-2","astyle":"STYLE000-2","aname":"NAME-2","logTime":1554861540555,"energy":945.19,"age":18}
{"aid":"ID000-1","astyle":"STYLE000-1","aname":"NAME-1","logTime":1554861541559,"energy":283.97,"age":9}
{"aid":"ID000-5","astyle":"STYLE000-5","aname":"NAME-5","logTime":1554861542561,"energy":757.12,"age":45}
{"aid":"ID000-3","astyle":"STYLE000-3","aname":"NAME-3","logTime":1554861543566,"energy":64.07,"age":27}
{"aid":"ID000-1","astyle":"STYLE000-1","aname":"NAME-1","logTime":1554861544570,"energy":203.27,"age":9}
{"aid":"ID000-2","astyle":"STYLE000-2","aname":"NAME-2","logTime":1554861545573,"energy":288.31,"age":18}
{"aid":"ID000-6","astyle":"STYLE000-6","aname":"NAME-6","logTime":1554861546575,"energy":792.53,"age":54}
{"aid":"ID000-9","astyle":"STYLE000-9","aname":"NAME-9","logTime":1554861547581,"energy":963.12,"age":81}
{"aid":"ID000-3","astyle":"STYLE000-3","aname":"NAME-3","logTime":1554861548583,"energy":708.63,"age":27}
{"aid":"ID000-8","astyle":"STYLE000-8","aname":"NAME-8","logTime":1554861549589,"energy":717.39,"age":72}
{"aid":"ID000-2","astyle":"STYLE000-2","aname":"NAME-2","logTime":1554861550594,"energy":585.91,"age":18}
```

#### KafkaStreamSqlGroupByProcessingTime#main

```bash
3> POJO{aid='ID000-8', astyle='STYLE000-8', aname='NAME-8', logTime=1554861531221, energy=3.92, age=72}
3> POJO{aid='ID000-4', astyle='STYLE000-4', aname='NAME-4', logTime=1554861532524, energy=569.05, age=36}
3> POJO{aid='ID000-9', astyle='STYLE000-9', aname='NAME-9', logTime=1554861533526, energy=399.86, age=81}
3> POJO{aid='ID000-9', astyle='STYLE000-9', aname='NAME-9', logTime=1554861534528, energy=460.99, age=81}
3> POJO{aid='ID000-8', astyle='STYLE000-8', aname='NAME-8', logTime=1554861535532, energy=228.98, age=72}
3> POJO{aid='ID000-6', astyle='STYLE000-6', aname='NAME-6', logTime=1554861536535, energy=513.46, age=54}
3> POJO{aid='ID000-8', astyle='STYLE000-8', aname='NAME-8', logTime=1554861537539, energy=59.66, age=72}
3> POJO{aid='ID000-6', astyle='STYLE000-6', aname='NAME-6', logTime=1554861538544, energy=456.47, age=54}
3> POJO{aid='ID000-9', astyle='STYLE000-9', aname='NAME-9', logTime=1554861539549, energy=232.41, age=81}
3> POJO{aid='ID000-2', astyle='STYLE000-2', aname='NAME-2', logTime=1554861540555, energy=945.19, age=18}
3> POJO{aid='ID000-1', astyle='STYLE000-1', aname='NAME-1', logTime=1554861541559, energy=283.97, age=9}
3> POJO{aid='ID000-5', astyle='STYLE000-5', aname='NAME-5', logTime=1554861542561, energy=757.12, age=45}
3> POJO{aid='ID000-3', astyle='STYLE000-3', aname='NAME-3', logTime=1554861543566, energy=64.07, age=27}
3> POJO{aid='ID000-1', astyle='STYLE000-1', aname='NAME-1', logTime=1554861544570, energy=203.27, age=9}
3> POJO{aid='ID000-2', astyle='STYLE000-2', aname='NAME-2', logTime=1554861545573, energy=288.31, age=18}
3> POJO{aid='ID000-6', astyle='STYLE000-6', aname='NAME-6', logTime=1554861546575, energy=792.53, age=54}
3> POJO{aid='ID000-9', astyle='STYLE000-9', aname='NAME-9', logTime=1554861547581, energy=963.12, age=81}
3> POJO{aid='ID000-3', astyle='STYLE000-3', aname='NAME-3', logTime=1554861548583, energy=708.63, age=27}
3> POJO{aid='ID000-8', astyle='STYLE000-8', aname='NAME-8', logTime=1554861549589, energy=717.39, age=72}
3> POJO{aid='ID000-2', astyle='STYLE000-2', aname='NAME-2', logTime=1554861550594, energy=585.91, age=18}
3> (true,2019-04-10 02:01:00.0,2019-04-10 02:02:00.0,STYLE000-9,2056.38,4)
2> (true,2019-04-10 02:01:00.0,2019-04-10 02:02:00.0,STYLE000-8,1009.95,4)
1> (true,2019-04-10 02:01:00.0,2019-04-10 02:02:00.0,STYLE000-1,487.24,2)
3> (true,2019-04-10 02:01:00.0,2019-04-10 02:02:00.0,STYLE000-6,1762.46,3)
2> (true,2019-04-10 02:01:00.0,2019-04-10 02:02:00.0,STYLE000-4,569.05,1)
1> (true,2019-04-10 02:01:00.0,2019-04-10 02:02:00.0,STYLE000-3,772.70,2)
3> (true,2019-04-10 02:01:00.0,2019-04-10 02:02:00.0,STYLE000-5,757.12,1)
2> (true,2019-04-10 02:01:00.0,2019-04-10 02:02:00.0,STYLE000-2,1819.41,3)
2> (true,2019-04-10 02:01:30.0,2019-04-10 02:02:30.0,STYLE000-8,1009.95,4)
3> (true,2019-04-10 02:01:30.0,2019-04-10 02:02:30.0,STYLE000-9,2056.38,4)
2> (true,2019-04-10 02:01:30.0,2019-04-10 02:02:30.0,STYLE000-2,1819.41,3)
1> (true,2019-04-10 02:01:30.0,2019-04-10 02:02:30.0,STYLE000-3,772.70,2)
3> (true,2019-04-10 02:01:30.0,2019-04-10 02:02:30.0,STYLE000-5,757.12,1)
2> (true,2019-04-10 02:01:30.0,2019-04-10 02:02:30.0,STYLE000-4,569.05,1)
1> (true,2019-04-10 02:01:30.0,2019-04-10 02:02:30.0,STYLE000-1,487.24,2)
3> (true,2019-04-10 02:01:30.0,2019-04-10 02:02:30.0,STYLE000-6,1762.46,3)
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

```bash

```

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

### 如果只是纯粹输出流数据，是即时的，全部消息都会消费到（打印输出），说明FlinkSQL的window触发和销毁有点问题
### Flink消费kafka不及时，出错重试导致重复计算和结果入库重复，切换别的输出Sink后数据丢失，总之不可能保证绝对的百分之百正确...

比如发送40条数据，窗口消费33条，另外7条，需要继续发送新的数据，才会被消费掉，即便重启程序-也要发送新的数据，才会消费上次"未及时"消费的数据。  
除非修改新的group_id后，会从头消费全部数据。

![image](http://images.icocoro.me/images/new/20190421000.png)

### SQLTester

从Socket服务器接收数据，消费是即时的，数据可以消费完。

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
