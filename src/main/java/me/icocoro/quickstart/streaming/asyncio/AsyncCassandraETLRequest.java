package me.icocoro.quickstart.streaming.asyncio;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DowngradingConsistencyRetryPolicy;
import com.datastax.driver.core.policies.RetryPolicy;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import com.google.gson.Gson;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.cassandra.shaded.com.google.common.base.Function;
import org.apache.flink.cassandra.shaded.com.google.common.util.concurrent.AsyncFunction;
import org.apache.flink.cassandra.shaded.com.google.common.util.concurrent.FutureCallback;
import org.apache.flink.cassandra.shaded.com.google.common.util.concurrent.Futures;
import org.apache.flink.cassandra.shaded.com.google.common.util.concurrent.ListenableFuture;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Time;
import java.util.*;

/**
 * AsyncCassandraETLRequest
 */
public class AsyncCassandraETLRequest
        extends RichAsyncFunction<Tuple4<String, String, Double, Time>, String> {

    Logger logger = LoggerFactory.getLogger(AsyncCassandraETLRequest.class);

    private static final long serialVersionUID = 7527316098904762297L;

    private transient Cluster cluster;
    private transient ListenableFuture<Session> session;

    @Override
    public void open(Configuration parameters) throws Exception {
        SocketOptions so = new SocketOptions().setReadTimeoutMillis(30000).setConnectTimeoutMillis(30000);
        PoolingOptions poolingOptions = new PoolingOptions()
                .setMaxRequestsPerConnection(HostDistance.LOCAL, 32768)
                .setConnectionsPerHost(HostDistance.LOCAL, 4, 10)
                .setHeartbeatIntervalSeconds(60)
                .setPoolTimeoutMillis(30000);
        QueryOptions queryOptions = new QueryOptions().setConsistencyLevel(ConsistencyLevel.QUORUM);
        RetryPolicy retryPolicy = DowngradingConsistencyRetryPolicy.INSTANCE;

        cluster = Cluster.builder().addContactPoint("127.0.0.1").withSocketOptions(so)
                .withClusterName("Test Cluster")
                .withPoolingOptions(poolingOptions)
                .withQueryOptions(queryOptions)
                .withRetryPolicy(retryPolicy)
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
    public void timeout(Tuple4<String, String, Double, Time> input, ResultFuture<String> resultFuture) {
        // 异步查询超时，10000条数据还是全部入库了的
        logger.info("timeout: " + input);
    }

    @Override
    public void asyncInvoke(Tuple4<String, String, Double, Time> input, ResultFuture<String> resultFuture) throws Exception {
        ListenableFuture<ResultSet> resultSet = Futures.transform(session,
                (AsyncFunction<Session, ResultSet>) session -> {
                    logger.info("session.getState().getConnectedHosts(): " + session.getState().getConnectedHosts());
                    // insert or update 简单测试
                    String cql = "SELECT * FROM test.xxx WHERE id='" + input.f0 + "'";
                    logger.info(cql);
                    // update_time
                    String insertCql = "INSERT INTO test.xxx (id, type, price, create_time, update_time, uid) " +
                            "VALUES ('" + input.f0 + "','" + input.f1 + "'," + input.f2 + ",toTimestamp(NOW()),toTimestamp(NOW()),NOW())";
                    logger.info(insertCql);
                    String updateCqlCql = "";
                    ResultSet rs = session.execute(cql);
                    if (rs != null && rs.all() != null && rs.all().size() > 0) {
//                            session.execute(updateCqlCql);
                    } else {
                        session.execute(insertCql);
                    }
                    return session.executeAsync(cql);
                });

        ListenableFuture<String> rr = Futures.transform(resultSet,
                new Function<ResultSet, String>() {
                    List<String> strings = new ArrayList<>();
                    // 只取一条
                    Map<String, Object> map = new HashMap();

                    public String apply(ResultSet rs) {
                        Iterator<Row> rowIterator = rs.iterator();
                        while (rowIterator.hasNext()) {
                            Row row = rowIterator.next();
                            map.put("id", row.getString("id"));
                            map.put("type", row.getString("type"));
                            map.put("price", row.getDouble("price"));
                            map.put("create_time", row.getTimestamp("create_time"));
                            map.put("update_time", row.getTimestamp("update_time"));
                            return new Gson().toJson(map);
                        }
                        return "";
                    }
                });

        Futures.addCallback(rr, new FutureCallback<String>() {
            public void onSuccess(String r) {
                // ETL here nothing to do.
                // 如果需要返回完整的数据 则调用complete
                resultFuture.complete(Arrays.asList(r));
            }

            public void onFailure(Throwable t) {
                t.printStackTrace();
                logger.info("Failed to retrieve the r: %s%n",
                        t.getMessage());
            }
        });
    }
}
