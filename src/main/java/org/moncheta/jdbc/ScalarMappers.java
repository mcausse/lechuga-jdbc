package org.moncheta.jdbc;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.moncheta.jdbc.exception.BaseException;

public class ScalarMappers {

    public static final RowMapper<String> STRING = (rs) -> ResultSetUtils.getString(rs);
    public static final RowMapper<Boolean> BOOLEAN = (rs) -> ResultSetUtils.getBoolean(rs);
    public static final RowMapper<Date> TIMESTAMP = (rs) -> ResultSetUtils.getTimestamp(rs);
    public static final RowMapper<byte[]> BYTE_ARRAY = (rs) -> ResultSetUtils.getBytes(rs);

    public static final RowMapper<Byte> BYTE = (rs) -> ResultSetUtils.getByte(rs);
    public static final RowMapper<Short> SHORT = (rs) -> ResultSetUtils.getShort(rs);
    public static final RowMapper<Integer> INTEGER = (rs) -> ResultSetUtils.getInteger(rs);
    public static final RowMapper<Long> LONG = (rs) -> ResultSetUtils.getLong(rs);
    public static final RowMapper<Float> FLOAT = (rs) -> ResultSetUtils.getFloat(rs);
    public static final RowMapper<Double> DOUBLE = (rs) -> ResultSetUtils.getDouble(rs);
    public static final RowMapper<BigDecimal> BIG_DECIMAL = (rs) -> ResultSetUtils.getBigDecimal(rs);

    static final Map<Class<?>, RowMapper<?>> scalarMappers = new LinkedHashMap<>();

    static {
        scalarMappers.put(String.class, STRING);
        scalarMappers.put(Date.class, TIMESTAMP);
        scalarMappers.put(byte[].class, BYTE_ARRAY);
        scalarMappers.put(BigDecimal.class, BIG_DECIMAL);

        scalarMappers.put(Boolean.class, BOOLEAN);
        scalarMappers.put(Byte.class, BYTE);
        scalarMappers.put(Short.class, SHORT);
        scalarMappers.put(Integer.class, INTEGER);
        scalarMappers.put(Long.class, LONG);
        scalarMappers.put(Float.class, FLOAT);
        scalarMappers.put(Double.class, DOUBLE);

        scalarMappers.put(boolean.class, BOOLEAN);
        scalarMappers.put(byte.class, BYTE);
        scalarMappers.put(short.class, SHORT);
        scalarMappers.put(int.class, INTEGER);
        scalarMappers.put(long.class, LONG);
        scalarMappers.put(float.class, FLOAT);
        scalarMappers.put(double.class, DOUBLE);
    }

    @SuppressWarnings("unchecked")
    public static <T> RowMapper<T> getScalarMapperFor(Class<?> columnClass) {
        if (!scalarMappers.containsKey(columnClass)) {
            throw new BaseException("no scalar mapper defined for: " + columnClass.getName());
        }
        return (RowMapper<T>) scalarMappers.get(columnClass);
    }

}