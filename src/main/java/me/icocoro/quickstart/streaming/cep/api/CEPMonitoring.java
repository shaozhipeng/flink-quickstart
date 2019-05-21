/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.icocoro.quickstart.streaming.cep.api;

import me.icocoro.quickstart.streaming.cep.api.events.MonitoringEvent;
import me.icocoro.quickstart.streaming.cep.api.events.TemperatureAlert;
import me.icocoro.quickstart.streaming.cep.api.events.TemperatureEvent;
import me.icocoro.quickstart.streaming.cep.api.events.TemperatureWarning;
import me.icocoro.quickstart.streaming.cep.api.sources.MonitoringEventSource;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.IngestionTimeExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import java.util.List;
import java.util.Map;

/**
 * CEP监控示例
 * <p>
 * 输入事件流由来自一组机架的温度和功率事件组成。
 * 目标是检测机架何时即将过热。
 * 构建一个CEP模式，每当模式在给定时间间隔内匹配到两个连续事件的温度高于给定的阈值时，即产生温度警告。
 * 警告本身并不重要，但如果我们看到同一机架有两个温度上升的警告，我们希望产生一个报警。
 * 这是通过定义另一个CEP模式来分析生成的温度警告流来实现的。
 */
public class CEPMonitoring {
    // 温度-给定的阈值
    private static final double TEMPERATURE_THRESHOLD = 100;

    private static final int MAX_RACK_ID = 10;
    // 每100毫秒产生一个事件
    private static final long PAUSE = 100;
    private static final double TEMPERATURE_RATIO = 0.5;
    private static final double POWER_STD = 10;
    private static final double POWER_MEAN = 100;
    private static final double TEMP_STD = 20;
    private static final double TEMP_MEAN = 80;

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Use ingestion time => TimeCharacteristic == EventTime + IngestionTimeExtractor
        // 使用摄取时间作为事件时间
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        // Input stream of monitoring events
        // 监控事件的输入流
        DataStream<MonitoringEvent> inputEventStream = env
                .addSource(new MonitoringEventSource(
                        MAX_RACK_ID,
                        PAUSE,
                        TEMPERATURE_RATIO,
                        POWER_STD,
                        POWER_MEAN,
                        TEMP_STD,
                        TEMP_MEAN))
                .assignTimestampsAndWatermarks(new IngestionTimeExtractor<>());

        // Warning pattern: Two consecutive temperature events whose temperature is higher than the given threshold
        // appearing within a time interval of 10 seconds
        // 警告模式：10秒时间间隔内连续两次出现温度高于阈值的温度事件。
        Pattern<MonitoringEvent, ?> warningPattern = Pattern.<MonitoringEvent>begin("first")
                .subtype(TemperatureEvent.class)
                .where(new IterativeCondition<TemperatureEvent>() {
                    private static final long serialVersionUID = -6301755149429716724L;

                    @Override
                    public boolean filter(TemperatureEvent value, Context<TemperatureEvent> ctx) throws Exception {
                        return value.getTemperature() >= TEMPERATURE_THRESHOLD;
                    }
                })
                // 严格临近
                .next("second")
                .subtype(TemperatureEvent.class)
                .where(new IterativeCondition<TemperatureEvent>() {
                    private static final long serialVersionUID = 2392863109523984059L;

                    @Override
                    public boolean filter(TemperatureEvent value, Context<TemperatureEvent> ctx) throws Exception {
                        return value.getTemperature() >= TEMPERATURE_THRESHOLD;
                    }
                })
                .within(Time.seconds(10));

        // Create a pattern stream from our warning pattern
        // 温度事件输入流和模式的组合
        // PatternStream(DataStream<T> inputStream, Pattern<T, ?> pattern, EventComparator<T> comparator){} comparator默认为null
        PatternStream<MonitoringEvent> tempPatternStream = CEP.pattern(
                inputEventStream.keyBy("rackID"),
                warningPattern);

        // Generate temperature warnings for each matched warning pattern
        // 检索 选择匹配到的事件生成温度警告 连续两次温度取个平均值
        // select
        DataStream<TemperatureWarning> warnings = tempPatternStream.select(
                (Map<String, List<MonitoringEvent>> pattern) -> {
                    TemperatureEvent first = (TemperatureEvent) pattern.get("first").get(0);
                    TemperatureEvent second = (TemperatureEvent) pattern.get("second").get(0);

                    return new TemperatureWarning(first.getRackID(), (first.getTemperature() + second.getTemperature()) / 2);
                }
        );

        // Alert pattern: Two consecutive temperature warnings appearing within a time interval of 20 seconds
        // 报警模式：对上面匹配出来的温度警告流事件进行匹配 20秒内连续两次温度警告
        Pattern<TemperatureWarning, ?> alertPattern = Pattern.<TemperatureWarning>begin("first")
                .next("second")
                .within(Time.seconds(20));

        // Create a pattern stream from our alert pattern
        // 温度警告流和报警模式的组合
        PatternStream<TemperatureWarning> alertPatternStream = CEP.pattern(
                warnings.keyBy("rackID"),
                alertPattern);

        // Generate a temperature alert only if the second temperature warning's average temperature is higher than
        // first warning's temperature
        // 选择匹配到的温度警告，只有第二个温度警告的平均温度高于第一个温度警告的平均温度时，才生成一个温度报警
        // flatSelect
        DataStream<TemperatureAlert> alerts = alertPatternStream.flatSelect(
                (Map<String, List<TemperatureWarning>> pattern, Collector<TemperatureAlert> out) -> {
                    TemperatureWarning first = pattern.get("first").get(0);
                    TemperatureWarning second = pattern.get("second").get(0);

                    if (first.getAverageTemperature() < second.getAverageTemperature()) {
                        out.collect(new TemperatureAlert(first.getRackID()));
                    }
                },
                TypeInformation.of(TemperatureAlert.class));

        // Print the warning and alert events to stdout
        warnings.print();
        alerts.print();

        env.execute("CEP monitoring job");
    }
}
