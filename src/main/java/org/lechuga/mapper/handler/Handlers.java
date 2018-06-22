package org.lechuga.mapper.handler;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.lechuga.annotated.anno.CustomHandler;
import org.lechuga.jdbc.ResultSetUtils;
import org.lechuga.jdbc.exception.LechugaException;

public class Handlers {

    public static final Handler STRING = (rs, c) -> ResultSetUtils.getString(rs, c);
    public static final Handler DATE = (rs, c) -> ResultSetUtils.getTimestamp(rs, c);
    public static final Handler BYTE_ARRAY = (rs, c) -> ResultSetUtils.getBytes(rs, c);
    public static final Handler BIG_DECIMAL = (rs, c) -> ResultSetUtils.getBigDecimal(rs, c);

    public static final Handler BOOLEAN = (rs, c) -> ResultSetUtils.getBoolean(rs, c);
    public static final Handler BYTE = (rs, c) -> ResultSetUtils.getByte(rs, c);
    public static final Handler SHORT = (rs, c) -> ResultSetUtils.getShort(rs, c);
    public static final Handler INTEGER = (rs, c) -> ResultSetUtils.getInteger(rs, c);
    public static final Handler LONG = (rs, c) -> ResultSetUtils.getLong(rs, c);
    public static final Handler FLOAT = (rs, c) -> ResultSetUtils.getFloat(rs, c);
    public static final Handler DOUBLE = (rs, c) -> ResultSetUtils.getDouble(rs, c);

    public static final Handler PBOOLEAN = (rs, c) -> rs.getBoolean(c);
    public static final Handler PBYTE = (rs, c) -> rs.getByte(c);
    public static final Handler PSHORT = (rs, c) -> rs.getShort(c);
    public static final Handler PINTEGER = (rs, c) -> rs.getInt(c);
    public static final Handler PLONG = (rs, c) -> rs.getLong(c);
    public static final Handler PFLOAT = (rs, c) -> rs.getFloat(c);
    public static final Handler PDOUBLE = (rs, c) -> rs.getDouble(c);

    static final Map<Class<?>, Handler> HANDLERS = new LinkedHashMap<>();
    static {
        HANDLERS.put(String.class, STRING);
        HANDLERS.put(Date.class, DATE);
        HANDLERS.put(byte[].class, BYTE_ARRAY);
        HANDLERS.put(BigDecimal.class, BIG_DECIMAL);

        HANDLERS.put(Boolean.class, BOOLEAN);
        HANDLERS.put(Byte.class, BYTE);
        HANDLERS.put(Short.class, SHORT);
        HANDLERS.put(Integer.class, INTEGER);
        HANDLERS.put(Long.class, LONG);
        HANDLERS.put(Float.class, FLOAT);
        HANDLERS.put(Double.class, DOUBLE);

        HANDLERS.put(boolean.class, PBOOLEAN);
        HANDLERS.put(byte.class, PBYTE);
        HANDLERS.put(short.class, PSHORT);
        HANDLERS.put(int.class, PINTEGER);
        HANDLERS.put(long.class, PLONG);
        HANDLERS.put(float.class, PFLOAT);
        HANDLERS.put(double.class, PDOUBLE);
    }

    /**
     * Els tipus aquí contemplats especifiquen els que poden tenir les propietats de
     * les entitats a tractar. Per a algun tipus diferent, anotar amb
     * {@link CustomHandler}.
     */
    public static Handler getHandlerFor(Class<?> type) {
        if (!HANDLERS.containsKey(type)) {
            throw new LechugaException(
                    "unsupported column type: " + type.getName() + ": please specify a concrete " + Handler.class.getName());
        }
        return HANDLERS.get(type);
    }

}