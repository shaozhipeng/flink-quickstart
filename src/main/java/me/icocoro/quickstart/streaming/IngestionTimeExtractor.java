package me.icocoro.quickstart.streaming;

import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;

/**
 * 使用数据摄入时间作为事件时间
 *
 * @param <T>
 */
public class IngestionTimeExtractor<T> implements AssignerWithPeriodicWatermarks<T> {

    private static final long serialVersionUID = -1845587671209584794L;

    private static final long TIME_OFFSET = 28800000;

    private long maxTimestamp;

    @Override
    public long extractTimestamp(T element, long previousElementTimestamp) {
        // 时间戳单调递增 UTC+8
        final long now = Math.max(System.currentTimeMillis(), maxTimestamp);
        maxTimestamp = now;
        return now + TIME_OFFSET;
    }

    @Override
    public Watermark getCurrentWatermark() {
        final long now = Math.max(System.currentTimeMillis(), maxTimestamp);
        maxTimestamp = now;
        return new Watermark(now + TIME_OFFSET);
    }
}
