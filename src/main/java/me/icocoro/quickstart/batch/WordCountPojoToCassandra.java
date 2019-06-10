/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.icocoro.quickstart.batch;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Table;
import me.icocoro.quickstart.WordCountData;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.batch.connectors.cassandra.CassandraPojoOutputFormat;
import org.apache.flink.core.fs.FileSystem.WriteMode;
import org.apache.flink.streaming.connectors.cassandra.ClusterBuilder;
import org.apache.flink.util.Collector;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

/**
 * WordCountPojoToCassandra
 */
public class WordCountPojoToCassandra {

    /**
     * Word
     */
    @Table(keyspace = "test", name = "wd2",
            writeConsistency = "QUORUM",
            caseSensitiveKeyspace = false,
            caseSensitiveTable = false)
    public static class Word implements Serializable {

        private static final long serialVersionUID = -5419790159153135625L;

        @Column(name = "word")
        private String word;

        @Column(name = "cnt")
        private int cnt;

        @Column(name = "update_time")
        private UUID updateTime = UUIDs.timeBased();

        @Column(name = "atime")
        // 此处使用Date类型，不使用Timestamp
        // com.datastax.driver.core.exceptions.CodecNotFoundException: Codec not found for requested operation: [timestamp <-> java.sql.Timestamp]
        private Date atime = new Timestamp(System.currentTimeMillis());

        // constructors
        public Word() {
        }

        public Word(String word, int cnt) {
            this.setWord(word);
            this.setCnt(cnt);
        }

        // getters setters
        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public int getCnt() {
            return cnt;
        }

        public void setCnt(int cnt) {
            this.cnt = cnt;
        }

        public UUID getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(UUID updateTime) {
            this.updateTime = updateTime;
        }

        public Date getAtime() {
            return atime;
        }

        public void setAtime(Date atime) {
            this.atime = atime;
        }

        @Override
        public String toString() {
            return "Word{" +
                    "word='" + word + '\'' +
                    ", cnt=" + cnt +
                    ", updateTime=" + updateTime +
                    ", atime=" + atime +
                    '}';
        }
    }

    public static void main(String[] args) throws Exception {

        final ParameterTool params = ParameterTool.fromArgs(args);

        // 设置执行环境
        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        env.getConfig().setGlobalJobParameters(params);

        // 输入数据流
        DataSet<String> text;
        if (params.has("input")) {
            // 从指定路径下读取文件中的数据
            text = env.readTextFile(params.get("input"));
        } else {
            // 模拟数据
            text = WordCountData.getDefaultTextLineDataSet(env);
        }

        DataSet<Word> counts =
                // 将数据转换为(word,1)的形式
                text.flatMap(new Tokenizer())
                        // 根据word分组 对value求和
                        .groupBy("word")
                        .reduce(new ReduceFunction<Word>() {
                            private static final long serialVersionUID = 7865438764925929956L;

                            @Override
                            public Word reduce(Word value1, Word value2) throws Exception {
                                return new Word(value1.word, value1.cnt + value2.cnt);
                            }
                        });

        // 输出结果
        if (params.has("output")) {
            counts.writeAsText(params.get("output"), WriteMode.OVERWRITE);
            // 执行程序
            env.execute("WordCount-Pojo Example");
        } else {
            System.out.println("Printing result to stdout. Use --output to specify output path.");
            counts.output(new CassandraPojoOutputFormat<Word>(new ClusterBuilder() {
                private static final long serialVersionUID = -8443267085808156800L;

                @Override
                protected Cluster buildCluster(Cluster.Builder builder) {
                    return builder.addContactPoint("127.0.0.1").build();
                }
            }, Word.class));
            env.execute("WordCount-Pojo Example");
        }

    }

    /**
     * Tokenizer String => Word
     */
    public static final class Tokenizer implements FlatMapFunction<String, Word> {

        private static final long serialVersionUID = -67995104110786015L;

        @Override
        public void flatMap(String value, Collector<Word> out) {
            // 变小写，切分正则匹配出的单词
            String[] tokens = value.toLowerCase().split("\\W+");

            // 输出<String, Integer> Integer默认1 后面直接sum
            for (String token : tokens) {
                if (token.length() > 0) {
                    out.collect(new Word(token, 1));
                }
            }
        }
    }

}
