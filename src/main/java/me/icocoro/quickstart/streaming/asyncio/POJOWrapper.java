package me.icocoro.quickstart.streaming.asyncio;

import java.io.Serializable;

public class POJOWrapper<T> implements Serializable {

    private static final long serialVersionUID = -2269329990737878437L;

    private String eventType;
    private String tableName;
    private String originalJson;
    private T t;

    public POJOWrapper(String eventType, String tableName, String originalJson, T t) {
        this.eventType = eventType;
        this.tableName = tableName;
        this.originalJson = originalJson;
        this.t = t;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getOriginalJson() {
        return originalJson;
    }

    public void setOriginalJson(String originalJson) {
        this.originalJson = originalJson;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }

    @Override
    public String toString() {
        return "POJOWrapper{" +
                "eventType='" + eventType + '\'' +
                ", tableName='" + tableName + '\'' +
                ", originalJson='" + originalJson + '\'' +
                ", t=" + t +
                '}';
    }
}
