package me.icocoro.quickstart.streaming.hotmovies;

import java.io.Serializable;

/**
 * user_id,item_id,rating,timestamp
 * 196,242,3,881250949
 * 电影评分
 */
public class MoviesRating implements Serializable {

    private static final long serialVersionUID = 2373384309719484883L;
    // 用户ID
    private long userId;

    // 电影ID
    private long itemId;

    // 评分
    private long rating;

    // 时间戳-秒
    private long timestamp;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public long getRating() {
        return rating;
    }

    public void setRating(long rating) {
        this.rating = rating;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
