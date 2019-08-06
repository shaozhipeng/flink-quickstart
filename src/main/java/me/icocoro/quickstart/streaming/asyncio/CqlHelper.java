package me.icocoro.quickstart.streaming.asyncio;

import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.gson.internal.LinkedTreeMap;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * CqlHelper
 */
public class CqlHelper {

    private static final String KEY_SPACE = "M";
    private static final String CASS_TABLE_NAME = "cassTableName";

    public static Insert createInsertQuery(POJOWrapper wrapper, boolean notExists) {
        LinkedTreeMap<String, Object> fieldMap = getFieldMap(wrapper.getTableName());
        if (null == fieldMap) {
            return null;
        }
        Insert insert = QueryBuilder.insertInto(KEY_SPACE, (String) fieldMap.get(CASS_TABLE_NAME));
        //  这里使用LinkedTreeMap是有问题的，数据类型的问题，需要类似TypeInformation一样的东西才行
        LinkedTreeMap<String, Object> map = (LinkedTreeMap) wrapper.getT();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (fieldMap.containsKey(entry.getKey())) {
                if (fieldMap.get(entry.getKey()) instanceof List) {
                    List<String> list = (List) fieldMap.get(entry.getKey());
                    insert.value(list.get(0), trans(list.get(1), entry.getValue()));
                } else {
                    insert.value((String) fieldMap.get(entry.getKey()), entry.getValue());
                }
            }
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        ZoneId shanghaiZoneId = ZoneId.of("Asia/Shanghai");
        if (map.containsKey("createTime")) {
            insert.value("ts_create_time", LocalDateTime.parse(map.get("createTime").toString(), formatter).atZone(shanghaiZoneId).toInstant().toEpochMilli());
        }
        if (map.containsKey("updateTime")) {
            insert.value("ts_update_time", LocalDateTime.parse(map.get("updateTime").toString(), formatter).atZone(shanghaiZoneId).toInstant().toEpochMilli());
        }
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        insert.value("sys_update_time", timestamp);
        // UUID should be 16 or 0 bytes (36)
        insert.value("sys_create_time", timestamp);
//        insert.value("sys_create_time", UUIDs.timeBased());
        if (notExists) {
            insert.ifNotExists();
        }
        return insert;
    }

    /**
     * 偷个懒
     */
    private static Object trans(String type, Object value) {
        if ("String".equals(type)) {
            return String.valueOf(value.toString());
        }
        if ("Long".equals(type)) {
            return Long.parseLong(new BigDecimal(value.toString()).toPlainString());
        }
        if ("BigDecimal".equals(type)) {
            return new BigDecimal(value.toString());
        }
        if ("Integer".equals(type)) {
            return (int) (new BigDecimal(value.toString()).doubleValue());
        }
        return value;
    }

    public static void main(String[] args) {

        System.out.println(new BigDecimal("1.56505702338E+12").toPlainString());
    }

    public static LinkedTreeMap<String, Object> getFieldMap(String tableName) {
        if ("pojo".equals(tableName)) {
            return POJOFieldMap;
        }
        return null;
    }

    public static LinkedTreeMap<String, Object> POJOFieldMap = new LinkedTreeMap<>();

    static {
        // 大小写都一样 日期不用管
        POJOFieldMap.put("aid", Arrays.asList("aid", "String"));
        POJOFieldMap.put("astyle", Arrays.asList("astyle", "String"));
        POJOFieldMap.put("aname", Arrays.asList("aname", "String"));
        POJOFieldMap.put("logTime", Arrays.asList("log_time", "Long"));
        POJOFieldMap.put("energy", Arrays.asList("energy", "BigDecimal"));
        POJOFieldMap.put("age", Arrays.asList("age", "Integer"));
        POJOFieldMap.put("astatus", Arrays.asList("astatus", "String"));
        POJOFieldMap.put("createTime", "create_time");
        POJOFieldMap.put("updateTime", "update_time");

        POJOFieldMap.put("primaryKey", "aid");
        POJOFieldMap.put("timeCharacteristic", "updateTime");
        POJOFieldMap.put("cassTableName", "M_POJO");
    }
}
