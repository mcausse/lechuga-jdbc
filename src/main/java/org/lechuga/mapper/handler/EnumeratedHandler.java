package org.lechuga.mapper.handler;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.lechuga.jdbc.exception.LechugaException;

public class EnumeratedHandler implements Handler {

    final Handler strHandler = Handlers.STRING;

    final Class<?> enumClass;

    public EnumeratedHandler(Class<?> enumClass) {
        super();
        this.enumClass = enumClass;

        if (!enumClass.isEnum()) {
            throw new LechugaException("required an Enum type: " + enumClass.getName());
        }
    }

    @Override
    public Object getJdbcValue(Object value) {
        return strHandler.getJdbcValue(((Enum<?>) value).name());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object readValue(ResultSet rs, String columnName) throws SQLException {
        String v = (String) strHandler.readValue(rs, columnName);
        return Enum.valueOf((Class<Enum>) enumClass, v);
    }

}
