package org.moncheta.mapper.handler.custom;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.moncheta.jdbc.exception.BaseException;
import org.moncheta.mapper.handler.Handler;
import org.moncheta.mapper.handler.Handlers;

public class DateStringHandler implements Handler {

    final Handler strHandler = Handlers.STRING;

    final String dateFormat;

    public DateStringHandler(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Override
    public Object getJdbcValue(Object value) {
        if (value == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return strHandler.getJdbcValue(sdf.format(value));
    }

    @Override
    public Object readValue(ResultSet rs, String columnName) throws SQLException {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        String v = (String) strHandler.readValue(rs, columnName);
        if (v == null) {
            return null;
        }
        try {
            return sdf.parseObject(v);
        } catch (ParseException e) {
            throw new BaseException("formatting " + v + " => " + dateFormat, e);
        }
    }

}
