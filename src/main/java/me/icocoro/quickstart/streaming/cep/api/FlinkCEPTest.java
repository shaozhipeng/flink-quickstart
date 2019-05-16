package me.icocoro.quickstart.streaming.cep.api;

import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * CEP测试
 */
public class FlinkCEPTest {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 用于观察测试数据处理顺序
        env.setParallelism(1);

        // 模拟数据源
        DataStream<LoginEvent> loginEventStream = env.fromCollection(Arrays.asList(
                new LoginEvent("1", "192.168.0.1", "fail", 8),// 1-2 1-2-3 1-2-3-4
                new LoginEvent("1", "192.168.0.2", "fail", 9),// 2-3 2-3-4
                new LoginEvent("1", "192.168.0.3", "fail", 10),// 3-4
                new LoginEvent("1", "192.168.0.4", "fail", 10),
                new LoginEvent("2", "192.168.10.10", "success", -1),
                new LoginEvent("3", "192.168.10.10", "fail", 5),// 10-11
                new LoginEvent("3", "192.168.10.11", "fail", 6),
                new LoginEvent("4", "192.168.10.10", "fail", 6),// 10-11 10-11-12
                new LoginEvent("4", "192.168.10.11", "fail", 7),// 11-12
                new LoginEvent("4", "192.168.10.12", "fail", 8),
                new LoginEvent("5", "192.168.10.13", "success", 8),
                new LoginEvent("5", "192.168.10.14", "success", 9),
                new LoginEvent("5", "192.168.10.15", "success", 10),
                new LoginEvent("6", "192.168.10.16", "fail", 6),
                new LoginEvent("6", "192.168.10.17", "fail", 8),
                new LoginEvent("6", "192.168.10.18", "fail", 8),
                new LoginEvent("7", "192.168.10.18", "fail", 5),
                new LoginEvent("6", "192.168.10.19", "fail", 10),
                new LoginEvent("6", "192.168.10.19", "fail", 9)
        ));

        // 定义模式
        Pattern<LoginEvent, ?> loginFailPattern = Pattern.<LoginEvent>begin("begin")
                // IterativeCondition迭代条件
                // 该条件基于先前接受的事件的属性或其子集的统计信息来接受后续事件。
                // SimpleCondition public abstract class SimpleCondition<T> extends IterativeCondition<T> implements FilterFunction<T>
                .where(new SimpleCondition<LoginEvent>() {

                    private static final long serialVersionUID = -8776863985544289508L;

                    @Override
                    public boolean filter(LoginEvent value) throws Exception {
                        return value.getType().equals("fail");
                    }
                })
//                .times(4);
//                .times(4).optional(); // 0的效果看不出来
//                .times(2, 4);
//                .times(2, 4).greedy(); // 尽可能重复也看不出来
//                .times(2, 4).optional().greedy();
//                .oneOrMore();
//                .oneOrMore().greedy(); // 看不出来尽可能重复是怎么个意思
//                .oneOrMore().optional(); // 0也看不出来
//                .oneOrMore().optional().greedy();
//                .timesOrMore(2);
//                .timesOrMore(2).greedy();
//                .timesOrMore(2).optional().greedy();
//                .where(new IterativeCondition<LoginEvent>() {
//                    @Override
//                    public boolean filter(LoginEvent value, Context<LoginEvent> ctx) throws Exception {
//                        if (!value.getType().equals("fail")) {
//                            return false;
//                        }
//
//                        int sum = value.getWeight();
//                        int i = 0;
//                        System.out.println("begin value: " + value + " value i: " + i);
////                        System.out.println("xx: " + ctx.getEventsForPattern("begin").iterator().hasNext());
//                        for (LoginEvent event : ctx.getEventsForPattern("begin")) {
//                            sum += event.getWeight();
//                            i = i + 1;
//                            System.out.println("event: " + event);
//                        }
//                        System.out.println("after value: " + value + " value i: " + i);
//                        if (i == 0) {
//                            return true;
//                        }
//                        return Double.compare(sum / i, 5.0) > 0;
//                    }
//                })
//                .times(4);
                .or(new SimpleCondition<LoginEvent>() {
                    private static final long serialVersionUID = -5368940865714513531L;

                    @Override
                    public boolean filter(LoginEvent value) {
                        return value.getWeight() >= 6;
                    }
                })
                .oneOrMore()
                .until(new SimpleCondition<LoginEvent>() {
                    private static final long serialVersionUID = -4449768287572631142L;

                    @Override
                    public boolean filter(LoginEvent value) throws Exception {
                        return value.getWeight() == 8 || value.getWeight() == 9;
                    }
                });

        // 进行分组匹配
        PatternStream<LoginEvent> patternStream = CEP.pattern(
                loginEventStream.keyBy(LoginEvent::getUserId),
                loginFailPattern);

        DataStream<LoginWarning> loginFailDataStream = patternStream.select((Map<String, List<LoginEvent>> pattern) -> {
            List<LoginEvent> first = pattern.get("begin");
            return new LoginWarning(first.get(0).getUserId(), first.get(0).getIp(), first.get(0).getType());
        });

//        loginFailDataStream.print();

        // 定义模式序列 fail fail weightHigh>8 weightHigh!=11
        // 模式匹配后跳过策略
        Pattern<LoginEvent, ?> start = Pattern.<LoginEvent>begin("start").where(new SimpleCondition<LoginEvent>() {
            private static final long serialVersionUID = 7128114484076301206L;

            @Override
            public boolean filter(LoginEvent value) throws Exception {
                return value.getType().equals("fail");
            }
        });
        Pattern<LoginEvent, ?> fail = start.next("fail").where(new SimpleCondition<LoginEvent>() {
            private static final long serialVersionUID = 3524346872991995189L;

            @Override
            public boolean filter(LoginEvent value) throws Exception {
                return value.getType().equals("fail");
            }
        });
        Pattern<LoginEvent, ?> weightHigh = fail.followedByAny("weightHigh").where(new SimpleCondition<LoginEvent>() {
            private static final long serialVersionUID = -1591807807709294286L;

            @Override
            public boolean filter(LoginEvent value) throws Exception {
                return value.getWeight() > 6;
            }
        });
        Pattern<LoginEvent, ?> newPattern = weightHigh.notNext("end").where(new SimpleCondition<LoginEvent>() {
            private static final long serialVersionUID = -4029347898258058599L;

            @Override
            public boolean filter(LoginEvent value) throws Exception {
                return value.getWeight() == 11;
            }
        });

        PatternStream<LoginEvent> patternStream2 = CEP.pattern(
                loginEventStream.keyBy(LoginEvent::getUserId),
                newPattern);

        // DataStream<里面不能是List<>>
        DataStream<String> newPatternStream = patternStream2.select((Map<String, List<LoginEvent>> pattern) -> {
            List<LoginWarning> loginWarnings = new ArrayList<LoginWarning>();
            if (null != pattern.get("start")) {
//                System.out.println("list: " + pattern.get("weightHigh"));
                LoginEvent first = pattern.get("start").get(0);
                if (null != first) {
                    loginWarnings.add(new LoginWarning(first.getUserId(), first.getIp(), first.getType(), first.getWeight()));
                }
            }
            if (null != pattern.get("fail")) {
//                System.out.println("list: " + pattern.get("weightHigh"));
                LoginEvent first = pattern.get("fail").get(0);
                if (null != first) {
                    loginWarnings.add(new LoginWarning(first.getUserId(), first.getIp(), first.getType(), first.getWeight()));
                }
            }
            if (null != pattern.get("weightHigh")) {
//                System.out.println("list: " + pattern.get("weightHigh"));
                LoginEvent first = pattern.get("weightHigh").get(0);
                if (null != first) {
                    loginWarnings.add(new LoginWarning(first.getUserId(), first.getIp(), first.getType(), first.getWeight()));
                }
            }
            if (null != pattern.get("end")) {
//                System.out.println("list: " + pattern.get("end"));
                LoginEvent first = pattern.get("end").get(0);
                if (null != first) {
                    loginWarnings.add(new LoginWarning(first.getUserId(), first.getIp(), first.getType(), first.getWeight()));
                }
            }
            return loginWarnings.toString();
        });

        newPatternStream.print();

        env.execute();

    }
}

class LoginWarning {
    private String userId;
    private String type;
    private String ip;
    private Integer weight;

    public LoginWarning() {
    }

    public LoginWarning(String userId, String type, String ip) {
        this.userId = userId;
        this.type = type;
        this.ip = ip;
    }

    public LoginWarning(String userId, String type, String ip, Integer weight) {
        this.userId = userId;
        this.type = type;
        this.ip = ip;
        this.weight = weight;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "LoginWarning{" +
                "userId='" + userId + '\'' +
                ", type='" + type + '\'' +
                ", ip='" + ip + '\'' +
                ", weight='" + weight + '\'' +
                '}';
    }

}

/**
 * 登录事件
 */
class LoginEvent {

    private String userId;
    private String ip;
    private String type;
    private Integer weight;

    public LoginEvent() {
    }

    public LoginEvent(String userId, String ip, String type, Integer weight) {
        this.userId = userId;
        this.ip = ip;
        this.type = type;
        this.weight = weight;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "LoginEvent{" +
                "userId='" + userId + '\'' +
                ", ip='" + ip + '\'' +
                ", type='" + type + '\'' +
                ", weight=" + weight +
                '}';
    }
}