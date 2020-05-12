package me.icocoro.quickstart.streaming.asyncio;

import com.google.gson.Gson;
import me.icocoro.quickstart.streaming.test.POJO;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.redisson.Redisson;
import org.redisson.api.RFuture;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * AsyncRedisRequest
 * Redis在数据访问方面是单线程的，通过并发获得的唯一好处是将协议和I/O工作分散到不同的线程中去做。
 */
public class AsyncRedisRequest extends RichAsyncFunction<String, String> {
    private static Logger logger = LoggerFactory.getLogger(AsyncRedisRequest.class);

    private static final long serialVersionUID = -8022470346098502168L;
    private transient RedissonClient redissonClient = null;
    private transient RScript script = null;
    private transient String sha = null;
    private static final String lua =
            "local k = KEYS[1]\n" +
                    "local e = ARGV[1]\n" +
                    "local isExists = redis.call('exists', k)\n" +
                    "if not isExists or isExists == 0 then\n" +
                    "  redis.call('hmset', k, 'totalNum', 1)\n" +
                    "  redis.call('hmset', k, 'totalEnergy', e)\n" +
                    "else\n" +
                    "  redis.call('hmset', k, 'totalNum', tonumber(redis.call('hget', k, 'totalNum')) + 1)\n" +
                    "  redis.call('hmset', k, 'totalEnergy', string.format(\"%.2f\", tonumber(redis.call('hget', k, 'totalEnergy')) + tonumber(e)))\n" +
                    "end\n" +
                    "return redis.call('hgetall', k)";

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        // ((SingleServerConfig)config.useSingleServer().setTimeout(1000000)).setAddress("redis://127.0.0.1:6379");
        Config config = new Config();
        // 默认的Config在evalShaAsync时有bug，默认Codec是oldConf.setCodec(new FstCodec()); 不会使用getScript(StringCodec.INSTANCE)。
        config.setCodec(StringCodec.INSTANCE);
        config.useSingleServer()
                .setTimeout(1000000)
                .setAddress("redis://127.0.0.1:6379");
        redissonClient = Redisson.create(config);
        script = redissonClient.getScript(StringCodec.INSTANCE);
        sha = script.scriptLoad(lua);
    }

    @Override
    public void close() throws Exception {
        super.close();
        if (redissonClient != null && !redissonClient.isShutdown()) {
            redissonClient.shutdown();
        }
    }

    @Override
    public void asyncInvoke(String input, ResultFuture<String> resultFuture) throws Exception {
        POJO pojo = new Gson().fromJson(input, POJO.class);
        pojo.getAid();
        pojo.getLogTime();

        /**
         RBucket<String> bucket = redissonClient.getBucket("", StringCodec.INSTANCE);
         RFuture<String> future = bucket.getAsync();
         */
//        RBucket<String> bucket = redissonClient.getBucket("asyncio_" + pojo.getAid(), StringCodec.INSTANCE);
//        RFuture future = bucket.getAndSetAsync(input, 24, TimeUnit.HOURS);

        // 注意时区
        String dateStr = DateTime.now().toString(DateTimeFormat.forPattern("yyyy-MM-dd"));

        String key = "asyncio_atype_" + pojo.getAstyle() + "_" + dateStr;

//        RTransaction transaction = redissonClient.createTransaction(TransactionOptions.defaults());
//        RMap<String, Object> transactionMap = transaction.getMap(key, StringCodec.INSTANCE);
//        // 按天计算累计计数和Energy(卡路里-能量变化)
//        // 需要注意transaction.getMap,transactionMap.isExists()和后续的计数、能量的计算在partition>1即多个线程执行的时候，是非线程安全的。
//        System.out.println("pojo.getAstyle(): " + pojo.getAstyle() + " transactionMap.isExists(): " + transactionMap.isExists() + " threadName: " + Thread.currentThread().getName());
//        if (transactionMap.isExists()) {
//            transactionMap.put("totalNum", Long.valueOf((String) transactionMap.get("totalNum")) + 1);
//            transactionMap.put("totalEnergy", new BigDecimal((String) transactionMap.get("totalEnergy")).add(pojo.getEnergy()));
//        } else {
//            transactionMap.put("totalNum", 1L);
//            transactionMap.put("totalEnergy", new BigDecimal(0.00));
//        }

//        RFuture transactionFuture = transaction.commitAsync();
        System.out.println("sha: " + sha + " threadName: " + Thread.currentThread().getName());

        // Mode mode, String shaDigest, ReturnType returnType, List<Object> keys, Object... values
        RFuture<List<Object>> scriptFuture = script.evalShaAsync(RScript.Mode.READ_ONLY, sha, RScript.ReturnType.MAPVALUELIST, Arrays.asList(key), String.valueOf(pojo.getEnergy().doubleValue()));

        scriptFuture.whenComplete((res, exception) -> {
            resultFuture.complete(Arrays.asList(res.toString()));
        });

//        List<Object> list = script.evalSha(RScript.Mode.READ_ONLY, sha, RScript.ReturnType.MAPVALUELIST, Arrays.asList(key), String.valueOf(pojo.getEnergy().doubleValue()));
//        resultFuture.complete(Arrays.asList(list.toString()));
    }

    @Override
    public void timeout(String input, ResultFuture<String> resultFuture) throws Exception {
        // 可以使用一个侧面输出处理一下
        logger.info("timeout: ");
    }

}
