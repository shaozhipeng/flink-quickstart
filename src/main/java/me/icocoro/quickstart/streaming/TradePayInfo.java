package me.icocoro.quickstart.streaming;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 业务实体类
 */
public class TradePayInfo implements Serializable {

    private static final long serialVersionUID = 5954824327295213329L;
    private String tradePayId;

    private String tradeNo;

    private String orderNo;

    private String tradeType;

    private BigDecimal totalAmount;

    // 事件时间
    private BigInteger timestamp;

    public String getTradePayId() {
        return tradePayId;
    }

    public void setTradePayId(String tradePayId) {
        this.tradePayId = tradePayId;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigInteger getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(BigInteger timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "TradePayInfo{" +
                "tradePayId='" + tradePayId + '\'' +
                ", tradeNo='" + tradeNo + '\'' +
                ", orderNo='" + orderNo + '\'' +
                ", tradeType='" + tradeType + '\'' +
                ", totalAmount=" + totalAmount +
                ", timestamp=" + timestamp +
                '}';
    }
}
