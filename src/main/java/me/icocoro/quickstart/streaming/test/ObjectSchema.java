package me.icocoro.quickstart.streaming.test;

import com.google.gson.Gson;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;

public class ObjectSchema<T> implements DeserializationSchema<T>, SerializationSchema<T> {

    private static final long serialVersionUID = -8937568534985583665L;
    private final Class<T> clazz;

    public ObjectSchema(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T deserialize(byte[] bytes) {
        try {
            return new Gson().fromJson(new String(bytes), this.clazz);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public boolean isEndOfStream(T t) {
        return false;
    }

    @Override
    public byte[] serialize(T t) {
        return new Gson().toJson(t).getBytes();
    }

    @Override
    public TypeInformation<T> getProducedType() {
        return TypeExtractor.getForClass(this.clazz);
    }
}
