package me.icocoro.quickstart.streaming.hotmovies;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.io.PojoCsvInputFormat;
import org.apache.flink.api.java.typeutils.PojoTypeInfo;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.TimerService;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class HotMoviesJob {

    public static void main(String[] args) throws Exception {

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        URL fileUrl = HotMoviesJob.class.getClassLoader().getResource("movies.csv");
        Path filePath = Path.fromLocalFile(new File(fileUrl.toURI()));
        PojoTypeInfo<MoviesRating> pojoType = (PojoTypeInfo<MoviesRating>) TypeExtractor.createTypeInfo(MoviesRating.class);
        String[] fieldOrder = new String[]{"userId", "itemId", "rating", "timestamp"};
        PojoCsvInputFormat<MoviesRating> csvInput = new PojoCsvInputFormat<>(filePath, pojoType, fieldOrder);

        DataStream<MoviesRating> dataSource = env.createInput(csvInput, pojoType)
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<MoviesRating>() {
                    private static final long serialVersionUID = -99653831668982036L;

                    @Override
                    public long extractAscendingTimestamp(MoviesRating moviesRating) {
                        // 原始数据单位秒，将其转成毫秒
                        return moviesRating.getTimestamp() * 1000;
                    }
                })
                .filter(new FilterFunction<MoviesRating>() {
                    private static final long serialVersionUID = -3855770093657376551L;

                    @Override
                    public boolean filter(MoviesRating moviesRating) throws Exception {
                        return moviesRating.getRating() > 0;
                    }
                });

        DataStream<ItemRating> itemRatingDataStream = dataSource
                .keyBy(new KeySelector<MoviesRating, Long>() {
                    private static final long serialVersionUID = -8781577834192777430L;

                    @Override
                    public Long getKey(MoviesRating moviesRating) throws Exception {
                        return moviesRating.getItemId();
                    }
                })
                .window(SlidingEventTimeWindows.of(Time.minutes(180), Time.minutes(5)))
                .aggregate(new SumAgg(), new WindowResultFunction());

        DataStream<String> topItems = itemRatingDataStream
                .keyBy(new KeySelector<ItemRating, Long>() {

                    private static final long serialVersionUID = -5334211228423564421L;

                    @Override
                    public Long getKey(ItemRating itemRating) throws Exception {
                        return itemRating.getWindowEnd();
                    }

                })
                .process(new TopNHotItems(3));

        topItems.print();

        env.execute(HotMoviesJob.class.getSimpleName());

    }

    /**
     * IN, ACC, OUT
     */
    public static class SumAgg implements AggregateFunction<MoviesRating, Long, Long> {

        private static final long serialVersionUID = 8869024622623024057L;

        @Override
        public Long createAccumulator() {
            return 0L;
        }

        @Override
        public Long add(MoviesRating moviesRating, Long r) {
            return r + moviesRating.getRating();
        }

        @Override
        public Long getResult(Long result) {
            return result;
        }

        @Override
        public Long merge(Long r1, Long r2) {
            return r1 + r2;
        }
    }


    /**
     * IN, OUT, KEY, W extends Window
     */
    public static class WindowResultFunction implements WindowFunction<Long, ItemRating, Long, TimeWindow> {

        private static final long serialVersionUID = -2146619446151678578L;

        @Override
        public void apply(
                Long key,  // 窗口的主键，即 itemId
                TimeWindow window,  // 窗口
                Iterable<Long> aggregateResult, // 聚合函数的结果，sum 值
                Collector<ItemRating> collector  // 输出类型为 ItemRating
        ) throws Exception {
            Long itemId = key;
            Long sum = aggregateResult.iterator().next();
            collector.collect(ItemRating.of(itemId, window.getEnd(), sum));
        }
    }

    public static class ItemRating implements Serializable {
        private static final long serialVersionUID = -2426281004966450993L;

        private long itemId;     // 电影ID
        private long windowEnd;  // 窗口结束时间戳
        private long rating;  // 总的评分

        public ItemRating() {
        }

        private ItemRating(long itemId, long windowEnd, long rating) {
            this.itemId = itemId;
            this.windowEnd = windowEnd;
            this.rating = rating;
        }

        public static ItemRating of(long itemId, long windowEnd, long rating) {
            return new ItemRating(itemId, windowEnd, rating);
        }

        public long getItemId() {
            return itemId;
        }

        public void setItemId(long itemId) {
            this.itemId = itemId;
        }

        public long getWindowEnd() {
            return windowEnd;
        }

        public void setWindowEnd(long windowEnd) {
            this.windowEnd = windowEnd;
        }

        public long getRating() {
            return rating;
        }

        public void setRating(long rating) {
            this.rating = rating;
        }
    }

    /**
     * K, I, O
     */
    public static class TopNHotItems extends KeyedProcessFunction<Long, ItemRating, String> {

        private static final long serialVersionUID = 4908962722282897334L;

        private int topSize;

        public TopNHotItems(int topSize) {
            this.topSize = topSize;
        }

        /**
         * 电影评分数据
         * 状态
         */
        private ListState<ItemRating> itemState;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            // 状态的注册
            ListStateDescriptor<ItemRating> itemsStateDesc = new ListStateDescriptor<>(
                    "itemState-state",
                    ItemRating.class);
            itemState = getRuntimeContext().getListState(itemsStateDesc);
        }

        @Override
        public void processElement(ItemRating input, Context ctx, Collector<String> out) throws Exception {
            // 把每一部电影的汇总评分数据放入itemState
            itemState.add(input);
            // 注册 windowEnd+1 的 EventTime Timer, 当触发时，说明收齐了属于windowEnd窗口的所有数据
            ctx.timerService().registerEventTimeTimer(input.windowEnd + 1);
        }

        /**
         * Called when a timer set using {@link TimerService} fires.
         *
         * @param timestamp
         * @param ctx
         * @param out
         * @throws Exception
         */
        @Override
        public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
            super.onTimer(timestamp, ctx, out);
            // 窗口内收到的所有数据
            List<ItemRating> allItems = new ArrayList<>();
            for (ItemRating item : itemState.get()) {
                allItems.add(item);
            }

            // 提前清除状态中的数据，释放空间
            itemState.clear();

            // 从大到小排列
            allItems.sort((o1, o2) -> (int) (o2.getRating() - o1.getRating()));

            StringBuilder result = new StringBuilder();
            result.append("====================================\n");
            result.append("时间: ").append(new Timestamp(timestamp - 1)).append("\n");
            if (allItems.size() < topSize) {
                topSize = allItems.size();
            }
            for (int i = 0; i < topSize; i++) {
                ItemRating currentItem = allItems.get(i);
                result.append("No").append(i).append(":")
                        .append("  电影ID=").append(currentItem.getItemId())
                        .append("  评分=").append(currentItem.getRating())
                        .append("\n");
            }
            result.append("====================================\n\n");

            out.collect(result.toString());

        }
    }

}