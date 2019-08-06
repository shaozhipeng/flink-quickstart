package me.icocoro.quickstart.streaming.asyncio;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DowngradingConsistencyRetryPolicy;
import com.datastax.driver.core.policies.LoggingRetryPolicy;
import com.datastax.driver.core.policies.RetryPolicy;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import org.apache.flink.cassandra.shaded.com.google.common.util.concurrent.AsyncFunction;
import org.apache.flink.cassandra.shaded.com.google.common.util.concurrent.FutureCallback;
import org.apache.flink.cassandra.shaded.com.google.common.util.concurrent.Futures;
import org.apache.flink.cassandra.shaded.com.google.common.util.concurrent.ListenableFuture;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * AsyncCassandraRequest
 */
public class AsyncCassandraRequest extends RichAsyncFunction<POJOWrapper, String> {

    private static final long serialVersionUID = -3713249881014861537L;

    Logger logger = LoggerFactory.getLogger(AsyncCassandraRequest.class);

    private transient Cluster cluster;
    private transient ListenableFuture<Session> session;

    @Override
    public void open(Configuration parameters) throws Exception {
        // 配置与Cassandra集群之间的Socket连接参数
        SocketOptions so = new SocketOptions()
                // 读取数据超时时间 默认12000
                .setReadTimeoutMillis(30000)
                // 连接超时时间 默认5000
                .setConnectTimeoutMillis(30000);

        // 连接池相关的参数
        PoolingOptions poolingOptions = new PoolingOptions()
                // 每个连接的最大并发请求数 默认-2147483648
                .setMaxRequestsPerConnection(HostDistance.LOCAL, 32768)
                // 每个Cassandra节点多少个连接 核心连接数和最大连接数
                .setConnectionsPerHost(HostDistance.LOCAL, 4, 10)
                // 心跳间隔 默认30秒
                .setHeartbeatIntervalSeconds(60)
                // 连接池超时时间 默认5000
                .setPoolTimeoutMillis(30000);

        // 写数据时的数据一致性级别 QUORUM=至少成功写入Q个复制节点（Q=N/2+1）
        QueryOptions queryOptions = new QueryOptions().setConsistencyLevel(ConsistencyLevel.QUORUM);

        // 重试策略 如果做了预合并 加上Cassandra的插入式更新，可以多尝试几次直到成功
        // DowngradingConsistencyRetryPolicy：会降级的一致性，意思是重试之后的一致性要比初次的一致性级别低，也就是会保证最终一致性
        // 使用LoggingRetryPolicy包装一下
        RetryPolicy retryPolicy = new LoggingRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE);

        cluster = Cluster.builder().addContactPoint("127.0.0.1").withSocketOptions(so)
                .withClusterName("Test Cluster")
//                .withCredentials("username", "password")
                .withPoolingOptions(poolingOptions)
                .withQueryOptions(queryOptions)
                .withRetryPolicy(retryPolicy)
                // 负载均衡策略 RoundRobinPolicy Hash取模
                .withLoadBalancingPolicy(new RoundRobinPolicy())
                .withPort(9042).build();

        session = cluster.connectAsync();
    }

    @Override
    public void close() throws Exception {
        if (cluster != null) {
            cluster.close();
        }
    }

    @Override
    public void timeout(POJOWrapper input, ResultFuture<String> resultFuture) {
        logger.info("timeout: " + input);
    }

    @Override
    public void asyncInvoke(POJOWrapper input, ResultFuture<String> resultFuture) throws Exception {
        System.out.println("asyncInvoke: " + Thread.currentThread().getName());

        ListenableFuture<ResultSet> resultSet = Futures.transform(session, new AsyncFunction<Session, ResultSet>() {
            @Override
            public ListenableFuture<ResultSet> apply(Session session) throws Exception {
                System.out.println(CqlHelper.createInsertQuery(input, false));
                return session.executeAsync(CqlHelper.createInsertQuery(input, false));
            }
        });

        Futures.addCallback(resultSet, new FutureCallback<ResultSet>() {
            @Override
            public void onSuccess(@Nullable ResultSet rows) {
                System.out.println("rows: " + rows);
                resultFuture.complete(Arrays.asList("success"));
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }
}


