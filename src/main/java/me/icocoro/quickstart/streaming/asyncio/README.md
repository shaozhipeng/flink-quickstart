### 使用异步IO访问外部数据

#### 异步IO操作的需求

&emsp;Flink在做流数据计算时，很多时候需要与外部系统进行交互（比如数据库、Redis、Hive、HBase等等存储系统）。  
&emsp;往往需要注意系统间通信延迟是否会拖慢整个Flink作业，影响整体吞吐量和实时性。

&emsp;访问外部系统时，很容易就能想到在MapFunction里面进行交互操作，将请求发送给外部系统，然后一直等到外部系统返回响应，这是一个同步交互的过程。   
&emsp;这种同步等待在许多情况下占据了Map算子的绝大部分时间。

&emsp;与外部系统的异步交互意味着单个并行Map实例可以同时处理多个请求并同时接收响应。  
&emsp;这样，等待时间可以覆盖发送其他请求和接收响应的时间。   
&emsp;至少，等待时间是在多个请求上摊销的。  
&emsp;这使得大多数情况下流处理的吞吐量更高。

&emsp;通过将MapFunction扩展到非常高的并行度来提高吞吐量在某些情况下也是可能的，但通常需要非常高的资源成本：  
&emsp;拥有更多并行MapFunction实例意味着更多的任务、线程、Flink内部网络连接、与外部系统的网络连接、缓冲区、还有一般的内部「簿记」开销。

![image](http://images.icocoro.me//images/new/20190530000.png)

#### 使用异步IO的先决条件

1. 对外部系统进行异步IO访问的客户端API。  
2. 或者在没有这样的客户端的情况下，可以通过创建多个客户端并使用线程池处理同步调用来尝试将同步客户端转变为有限的并发客户端。但是，这种方法通常比适当的异步客户端效率低。

#### 异步IO API

&emsp;Flink的异步IO API允许用户将异步请求客户端与数据流一起使用。API处理与数据流的集成，包括处理顺序、事件时间和容错等。   
&emsp;假设有一个目标数据库的异步客户端，我们需要以下三个部分来实现对数据库的异步IO流转换：
1. 自定义一个AsyncFunction（继承RichAsyncFunction），用来调度请求。
2. 一个回调函数，它接受操作的结果并将其交给ResultFuture。
3. 在DataStream上应用异步IO操作作为转换。

```java
// This example implements the asynchronous request and callback with Futures that have the
// interface of Java 8's futures (which is the same one followed by Flink's Future)

/**
 * An implementation of the 'AsyncFunction' that sends requests and sets the callback.
 */
class AsyncDatabaseRequest extends RichAsyncFunction<String, Tuple2<String, String>> {

    /** The database specific client that can issue concurrent requests with callbacks */
    private transient DatabaseClient client;

    @Override
    public void open(Configuration parameters) throws Exception {
        client = new DatabaseClient(host, post, credentials);
    }

    @Override
    public void close() throws Exception {
        client.close();
    }

    @Override
    public void asyncInvoke(String key, final ResultFuture<Tuple2<String, String>> resultFuture) throws Exception {

        // issue the asynchronous request, receive a future for result
        final Future<String> result = client.query(key);

        // set the callback to be executed once the request by the client is complete
        // the callback simply forwards the result to the result future
        CompletableFuture.supplyAsync(new Supplier<String>() {

            @Override
            public String get() {
                try {
                    return result.get();
                } catch (InterruptedException | ExecutionException e) {
                    // Normally handled explicitly.
                    return null;
                }
            }
        }).thenAccept( (String dbResult) -> {
            resultFuture.complete(Collections.singleton(new Tuple2<>(key, dbResult)));
        });
    }
}

// create the original stream
DataStream<String> stream = ...;

// apply the async I/O transformation
DataStream<Tuple2<String, String>> resultStream =
    AsyncDataStream.unorderedWait(stream, new AsyncDatabaseRequest(), 1000, TimeUnit.MILLISECONDS, 100);
```

&emsp;重要说明：ResultFuture在第一次调用ResultFuture.complete时就执行完成了。随后的所有complete调用都将被忽略。（需要消化一下这句话）

&emsp;异步操作的两个参数控制：  
1. 超时：超时是指异步请求在被视为失败之前可能需要多长时间。此参数可防止请求未发出或者失败的请求。
2. 容量：此参数定义可以同时进行的异步请求数。尽管异步IO方法通常会带来更好的吞吐量，但异步IO算子仍然可能成为流应用程序的瓶颈。限制并发请求的数量可确保算子不会累积不断增长的待处理请求积压，只是一旦容量耗尽，它将触发反压。

#### 超时处理

1. 程序抛出异常，重启应用。
2. 覆盖AsyncFunction的timeout方法。

#### 数据的流转顺序

&emsp;AsyncFunction发出的并发请求经常以某种未定义的顺序完成，具体取决于首先完成的请求。   
&emsp;为了控制发出结果记录的顺序，Flink提供了两种模式：
1. 无序：异步请求完成后立即发出结果记录。  
在异步IO操作完之后，流中记录的顺序与以前不同。   
当使用处理时间作为基本时间特性时，此模式具有最低延迟和最低开销。  
对此模式调用AsyncDataStream.unorderedWait（...）即可。
2. 有序：在这种情况下，保留流顺序。 
结果记录的发出顺序与触发异步请求的顺序相同（异步IO操作时输入记录的顺序）。   
为此，算子缓冲结果记录，直到其所有先前记录被发出（或超时）。   
这通常会在检查点中引入一些额外的延迟和一些开销，因为与无序模式相比，记录或结果在检查点状态下会维持更长的时间。   
对此模式调用AsyncDataStream.orderedWait（...）即可。

&emsp;当在流作业中使用事件时间时，异步IO操作符能够正确得处理水印。 
&emsp;这意味着两种顺序模式具体如下：
1. 无序：水印不会超过记录，反之亦然，这意味着水印建立了一个顺序边界。 
记录仅在水印之间无序发出。只有在发出水印后才会发出「某个水印后发生的记录」。   
反过来，只有在发出「水印之前输入的所有结果记录」之后，才会发出水印。（需要消化一下）  
这意味着在存在水印的情况下，无序模式会引入一些与有序模式相同的延迟和管理开销。  
开销量取决于水印的增长频率。
2. 有序：保留记录的水印顺序，就像保留记录之间的顺序一样。与处理时间相比，开销没有明显变化。

&emsp;摄取时间是事件时间的特殊情况，其中自动生成的水印基于源处理时间。

#### 容错保证

&emsp;异步IO算子提供恰好一次的容错保证。   
&emsp;它在检查点中存储正在进行的异步请求的记录，并在从故障中恢复时恢复或重新触发请求。

#### 实现建议

&emsp;对于使用Executor（或Scala中的ExecutionContext）进行回调的Futures实现，我们建议使用DirectExecutor，因为回调通常只做最小的工作，而DirectExecutor避免了额外的线程到线程的切换开销。  
&emsp;回调通常只将结果传递给ResultFuture，后者将其添加到输出缓冲区。   
&emsp;从那里开始，包括记录发出和与检查点「簿记」交互的重要逻辑无论如何都发生在专用线程池中。

```java
org.apache.flink.runtime.concurrent.Executors.directExecutor() or com.google.common.util.concurrent.MoreExecutors.directExecutor().
```

#### 「警告」提醒

The AsyncFunction is not called Multi-Threaded.  
AsyncFunction不是多线程调用的。

&emsp;我们想在这里明确指出的常见混淆是AsyncFunction不是以多线程方式调用的。  
&emsp;只存在一个AsyncFunction实例，并且为流的相应分区中的每个记录顺序调用它。  
&emsp;除非asyncInvoke（...）方法返回快速并依赖于回调（客户端提供），否则它将不会进行正确的异步IO操作.

&emsp;例如，下面两种方式会阻塞asyncInvoke（...）方法，从而使异步行为失效：
1. 调用阻塞的数据库客户端进行查找或查询，直到收到结果为止。
2. 阻塞或等待异步客户端在asyncInvoke（...）方法中返回的future-type对象。