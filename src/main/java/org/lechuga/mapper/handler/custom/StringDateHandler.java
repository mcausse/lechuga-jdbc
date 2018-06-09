package org.lechuga.mapper.handler.custom;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.lechuga.jdbc.exception.LechugaException;
import org.lechuga.mapper.handler.Handler;
import org.lechuga.mapper.handler.Handlers;

public class StringDateHandler implements Handler {

    final Handler strHandler = Handlers.DATE;

    final String dateFormat;

    public StringDateHandler(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Override
    public Object getJdbcValue(Object value) {
        if (value == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        try {
            return strHandler.getJdbcValue(sdf.parse((String) value));
        } catch (ParseException e) {
            throw new LechugaException("parsing " + value + " => " + dateFormat, e);
        }
    }

    @Override
    public Object readValue(ResultSet rs, String columnName) throws SQLException {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        Date v = (Date) strHandler.readValue(rs, columnName);
        if (v == null) {
            return null;
        }
        return sdf.format(v);
    }

}
