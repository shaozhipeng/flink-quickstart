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

