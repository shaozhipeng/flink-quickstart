### Streaming

#### Streaming SQL

[FlinkStreamingSQL DEMO](https://github.com/shaozhipeng/flink-quickstart/tree/master/src/main/java/me/icocoro/quickstart/streaming/sql)

#### FlinkCEP

[FlinkCEP DEMO](https://github.com/shaozhipeng/flink-quickstart/tree/master/src/main/java/me/icocoro/quickstart/streaming/cep)

#### Asynchronous I/O

[Asynchronous I/O DEMO](https://github.com/shaozhipeng/flink-quickstart/tree/master/src/main/java/me/icocoro/quickstart/streaming/asyncio)

#### Open platform

[Open platform](https://github.com/shaozhipeng/flink-quickstart/tree/master/src/main/java/me/icocoro/quickstart/openplatform)

### Resources、examples and training exercises

[flink-examples](https://github.com/shaozhipeng/flink/tree/master/flink-examples)  
[flink-training-exercises](https://github.com/shaozhipeng/flink-training-exercises)  
[zh-community](https://zh.ververica.com/)  
[flink-training-course](https://github.com/flink-china/flink-training-course)  
关注公众号回复 Flink 有Flink视频链接，😊 欢迎Star、Fork、New Issue、公众号留言提建议...

#### 交流学习  

![image](http://images.icocoro.me/images/new/qrcode_for_gh_15fee3a03797_258.jpg)

##### GitHub

git init

git add src  
git add pom.xml  
git add README.md  

git commit -m 'flink-quickstart first commit'  
git remote add origin https://github.com/shaozhipeng/flink-quickstart.git  
git push -u origin master 

### 代码结构

```bash
$ tree
.
├── README.md
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── me
│   │   │       └── icocoro
│   │   │           └── quickstart
│   │   │               ├── WordCount.java
│   │   │               ├── WordCountData.java
│   │   │               ├── batch
│   │   │               │   ├── README.md
│   │   │               │   ├── jdbc
│   │   │               │   └── table
│   │   │               ├── configuration
│   │   │               ├── deploy
│   │   │               ├── issures
│   │   │               ├── metrics
│   │   │               ├── openplatform
│   │   │               ├── performance
│   │   │               ├── sourcecode
│   │   │               ├── streaming
│   │   │               │   ├── BusinessEntity.java
│   │   │               │   ├── KafkaToHBaseJob.java
│   │   │               │   ├── ObjectSchema.java
│   │   │               │   ├── POJO.java
│   │   │               │   ├── StreamKafkaProducer.java
│   │   │               │   ├── asyncio
│   │   │               │   │   ├── AsyncCassandraRequest.java
│   │   │               │   │   ├── AsyncHBaseRequest.java
│   │   │               │   │   ├── AsyncIOJob.java
│   │   │               │   │   └── AsyncRedisRequest.java
│   │   │               │   ├── cep
│   │   │               │   │   ├── README.md
│   │   │               │   │   ├── api
│   │   │               │   │   │   ├── CEPMonitoring.java
│   │   │               │   │   │   ├── FlinkCEPTest.java
│   │   │               │   │   │   ├── HopCEPTest.java
│   │   │               │   │   │   ├── README.md
│   │   │               │   │   │   ├── events
│   │   │               │   │   │   │   ├── MonitoringEvent.java
│   │   │               │   │   │   │   ├── PowerEvent.java
│   │   │               │   │   │   │   ├── TemperatureAlert.java
│   │   │               │   │   │   │   ├── TemperatureEvent.java
│   │   │               │   │   │   │   └── TemperatureWarning.java
│   │   │               │   │   │   └── sources
│   │   │               │   │   │       └── MonitoringEventSource.java
│   │   │               │   │   └── sql
│   │   │               │   ├── graphs
│   │   │               │   ├── ml
│   │   │               │   ├── rocketmq
│   │   │               │   │   └── README.md
│   │   │               │   ├── sink
│   │   │               │   │   ├── HDFSSinkDemo.java
│   │   │               │   │   ├── KafkaSinkDemo.java
│   │   │               │   │   └── README.md
│   │   │               │   ├── source
│   │   │               │   │   └── README.md
│   │   │               │   └── sql
│   │   │               │       ├── KafkaStreamSqlGroupByEventTime.java
│   │   │               │       ├── KafkaStreamSqlGroupByProcessingTime.java
│   │   │               │       ├── KafkaStreamToJDBCTable.java
│   │   │               │       ├── MessageProducer.java
│   │   │               │       ├── POJOSchema.java
│   │   │               │       ├── README.md
│   │   │               │       ├── SQLTester.java
│   │   │               │       ├── StreamingDimensionJoin.java
│   │   │               │       ├── StreamingJoin.java
│   │   │               │       └── t_pojo.sql
│   │   │               └── test
│   │   └── resources
│   │       └── logback.xml
│   └── test
│       └── java
│           └── me
│               └── icocoro
│                   ├── hbase
│                   │   └── HBaseClientTest.java
│                   ├── kafka
│                   │   ├── KafkaConsumerTest.java
│                   │   └── KafkaProducerTest.java
│                   └── rocketmq
```