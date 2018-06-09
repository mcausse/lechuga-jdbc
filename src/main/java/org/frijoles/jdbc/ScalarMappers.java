package org.frijoles.jdbc;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.frijoles.jdbc.exception.FrijolesException;

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

    public static final RowMapper<Byte> PBYTE = (rs) -> rs.getByte(1);
    public static final RowMapper<Short> PSHORT = (rs) -> rs.getShort(1);
    public static final RowMapper<Integer> PINTEGER = (rs) -> rs.getInt(1);
    public static final RowMapper<Long> PLONG = (rs) -> rs.getLong(1);
    public static final RowMapper<Float> PFLOAT = (rs) -> rs.getFloat(1);
    public static final RowMapper<Double> PDOUBLE = (rs) -> rs.getDouble(1);

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

        // scalarMappers.put(boolean.class, PBOOLEAN);
        scalarMappers.put(byte.class, PBYTE);
        scalarMappers.put(short.class, PSHORT);
        scalarMappers.put(int.class, PINTEGER);
        scalarMappers.put(long.class, PLONG);
        scalarMappers.put(float.class, PFLOAT);
        scalarMappers.put(double.class, PDOUBLE);
    }

    @SuppressWarnings("unchecked")
    public static <T> RowMapper<T> getScalarMapperFor(Class<?> columnClass) {
        if (!scalarMappers.containsKey(columnClass)) {
            throw new FrijolesException("no scalar mapper defined for: " + columnClass.getName());
        }
        return (RowMapper<T>) scalarMappers.get(columnClass);
    }

}