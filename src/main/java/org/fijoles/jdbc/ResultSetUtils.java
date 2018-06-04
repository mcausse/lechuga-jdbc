package org.fijoles.jdbc;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ResultSetUtils {

    public static <T extends Enum<T>> T getEnum(final Class<T> enumClass, final ResultSet rs) throws SQLException {

        final String v = rs.getString(1);
        if (v == null) {
            return null;
        }
        return Enum.valueOf(enumClass, v);
    }

    public static <T extends Enum<T>> T getEnum(final Class<T> enumClass, final ResultSet rs, String columnLabel)
            throws SQLException {

        final String v = rs.getString(columnLabel);
        if (v == null) {
            return null;
        }
        return Enum.valueOf(enumClass, v);
    }

    public static Byte getByte(final ResultSet rs) throws SQLException {
        final byte v = rs.getByte(1);
        if (rs.wasNull()) {
            return null;
        }
        return v;
    }

    public static Short getShort(final ResultSet rs) throws SQLException {
        final short v = rs.getShort(1);
        if (rs.wasNull()) {
            return null;
        }
        return v;
    }

    public static Integer getInteger(final ResultSet rs) throws SQLException {
        final int v = rs.getInt(1);
        if (rs.wasNull()) {
            return null;
        }
        return v;
    }

    public static Long getLong(final ResultSet rs) throws SQLException {
        final long v = rs.getLong(1);
        if (rs.wasNull()) {
            return null;
        }
        return v;
    }

    public static Float getFloat(final ResultSet rs) throws SQLException {
        final float v = rs.getFloat(1);
        if (rs.wasNull()) {
            return null;
        }
        return v;
    }

    public static Double getDouble(final ResultSet rs) throws SQLException {
        final double v = rs.getDouble(1);
        if (rs.wasNull()) {
            return null;
        }
        return v;
    }

    public static Byte getByte(final ResultSet rs, final String columnLabel) throws SQLException {
        final byte v = rs.getByte(columnLabel);
        if (rs.wasNull()) {
            return null;
        }
        return v;
    }

    public static Short getShort(final ResultSet rs, final String columnLabel) throws SQLException {
        final short v = rs.getShort(columnLabel);
        if (rs.wasNull()) {
            return null;
        }
        return v;
    }

    public static Integer getInteger(final ResultSet rs, final String columnLabel) throws SQLException {
        final int v = rs.getInt(columnLabel);
        if (rs.wasNull()) {
            return null;
        }
        return v;
    }

    public static Long getLong(final ResultSet rs, final String columnLabel) throws SQLException {
        final long v = rs.getLong(columnLabel);
        if (rs.wasNull()) {
            return null;
        }
        return v;
    }

    public static Float getFloat(final ResultSet rs, final String columnLabel) throws SQLException {
        final float v = rs.getFloat(columnLabel);
        if (rs.wasNull()) {
            return null;
        }
        return v;
    }

    public static Double getDouble(final ResultSet rs, final String columnLabel) throws SQLException {
        final double v = rs.getDouble(columnLabel);
        if (rs.wasNull()) {
            return null;
        }
        return v;
    }

    public static String getString(final ResultSet rs) throws SQLException {
        return rs.getString(1);
    }

    public static boolean getBoolean(final ResultSet rs) throws SQLException {
        return rs.getBoolean(1);
    }

    public static byte[] getBytes(final ResultSet rs) throws SQLException {
        return rs.getBytes(1);
    }

    public static Timestamp getTimestamp(final ResultSet rs) throws SQLException {
        return rs.getTimestamp(1);
    }

    public static String getString(final ResultSet rs, final String columnLabel) throws SQLException {
        return rs.getString(columnLabel);
    }

    public static boolean getBoolean(final ResultSet rs, final String columnLabel) throws SQLException {
        return rs.getBoolean(columnLabel);
    }

    public static byte[] getBytes(final ResultSet rs, final String columnLabel) throws SQLException {
        return rs.getBytes(columnLabel);
    }

    public static Timestamp getTimestamp(final ResultSet rs, final String columnLabel) throws SQLException {
        return rs.getTimestamp(columnLabel);
    }

    public static BigDecimal getBigDecimal(final ResultSet rs) throws SQLException {
        return rs.getBigDecimal(1);
    }

    public static BigDecimal getBigDecimal(final ResultSet rs, final String columnLabel) throws SQLException {
        return rs.getBigDecimal(columnLabel);
    }

    public static Character getChar(final ResultSet rs) throws SQLException {
        String v = rs.getString(1);
        if (v == null || v.isEmpty()) {
            return null;
        }
        return v.charAt(0);
    }

    public static Character getChar(final ResultSet rs, final String columnLabel) throws SQLException {
        String v = rs.getString(columnLabel);
        if (v == null || v.isEmpty()) {
            return null;
        }
        return v.charAt(0);
    }

}
