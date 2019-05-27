### Flink已经实现的Sink

#### Kafka

![image](http://images.icocoro.me/images/new/20190522000.png)

#### HDFS

```bash
$ hadoop fs -ls hdfs://localhost/test/output3
2019-05-22 19:59:24,044 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
Found 1 items
drwxr-xr-x   - shaozhipeng supergroup          0 2019-05-22 19:58 hdfs://localhost/test/output3/2019-05-22--19

$ hadoop fs -ls hdfs://localhost/test/output3/2019-05-22--19
2019-05-22 20:00:04,268 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
Found 4 items
-rw-r--r--   1 shaozhipeng supergroup        212 2019-05-22 19:58 hdfs://localhost/test/output3/2019-05-22--19/_part-0-0.pending
-rw-r--r--   1 shaozhipeng supergroup        446 2019-05-22 19:58 hdfs://localhost/test/output3/2019-05-22--19/_part-1-0.pending
-rw-r--r--   1 shaozhipeng supergroup        696 2019-05-22 19:58 hdfs://localhost/test/output3/2019-05-22--19/_part-2-0.pending
-rw-r--r--   1 shaozhipeng supergroup        699 2019-05-22 19:58 hdfs://localhost/test/output3/2019-05-22--19/_part-3-0.pending

$ hadoop fs -cat  hdfs://localhost/test/output3/2019-05-22--19/_part-0-0.pending
2019-05-22 21:03:06,707 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
them1
devoutly1
pause1
law1
delay1
would1
resolution1
slings1
dreams1
would2
patient1
others1
enterprises1
shuffled1
this1
he1
conscience1
this2
rub1
long1
office1
weary1
o1
er1
orisons1
```

#### JDBCAppendTableSink

如果不设置env.enableCheckpointing(checkpoint);  
JDBCAppendTableSink会在Job被Cancel的时候将数据保存到数据库。  

设置env.enableCheckpointing(checkpoint);之后才可以及时保存到数据库。

另外1.7.2的flink + 2.8.5的Hadoop做checkpoint是有问题的，并发写入HDFS报异常。

#### Cassandra

```bash
$ ./bin/cqlsh
Connected to Test Cluster at 127.0.0.1:9042.
[cqlsh 5.0.1 | Cassandra 3.0.18 | CQL spec 3.4.0 | Native protocol v4]
Use HELP for help.
cqlsh> DESCRIBE CLUSTER;

Cluster: Test Cluster
Partitioner: Murmur3Partitioner

cqlsh> DESCRIBE KEYSPACES;

system_traces  system_schema  system_auth  system  system_distributed
```  

时间UTC+0

```bash
CREATE KEYSPACE test WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};

CREATE TABLE test.wd (
	word TEXT,
	cnt INT,
	atime TIMESTAMP,
    update_time TIMEUUID,
    PRIMARY KEY(word, update_time)
) WITH CLUSTERING ORDER BY (update_time DESC);


word       | update_time                          | atime                    | cnt
------------+--------------------------------------+--------------------------+-----
  oppressor | 87486842-8071-11e9-97de-83f0dd209c94 | 2019-05-27 11:21:02+0000 |   1
       does | 8748dd71-8071-11e9-97de-83f0dd209c94 | 2019-05-27 11:21:02+0000 |   1
       time | 8759a650-8071-11e9-97de-83f0dd209c94 | 2019-05-27 11:21:02+0000 |   1
  something | 87553982-8071-11e9-97de-83f0dd209c94 | 2019-05-27 11:21:02+0000 |   1
   opposing | 8751b713-8071-11e9-97de-83f0dd209c94 | 2019-05-27 11:21:02+0000 |   1
          a | 87695dc9-8071-11e9-97de-83f0dd209c94 | 2019-05-27 11:21:02+0000 |   5
```
