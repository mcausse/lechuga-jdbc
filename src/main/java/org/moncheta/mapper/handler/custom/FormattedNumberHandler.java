package org.moncheta.mapper.handler.custom;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

import org.moncheta.jdbc.ResultSetUtils;
import org.moncheta.mapper.handler.Handler;

public class FormattedNumberHandler implements Handler {

    final DecimalFormat formatter;
    final String numericFormat;

    public FormattedNumberHandler(final String numericFormat, final String decimalSeparator,
            final String groupingSeparator) {
        super();

        this.numericFormat = numericFormat;

        final DecimalFormatSymbols a = new DecimalFormatSymbols();
        a.setDecimalSeparator(decimalSeparator.charAt(0));
        a.setGroupingSeparator(groupingSeparator.charAt(0));

        this.formatter = new DecimalFormat(numericFormat);
        formatter.setDecimalFormatSymbols(a);
    }

    public FormattedNumberHandler() {
        this("#,###,###,##0.00", ",", ".");
    }

    @Override
    public Object getJdbcValue(Object v) {
        if (v == null) {
            return null;
        }
        try {
            return formatter.parse((String) v);
        } catch (final ParseException e) {
            throw new RuntimeException(numericFormat + ": " + v, e);
        }
    }

    @Override
    public Object readValue(ResultSet rs, String columnName) throws SQLException {
        Double v = ResultSetUtils.getDouble(rs, columnName);
        if (v == null) {
            return null;
        }
        try {
            return formatter.format(((Number) v).doubleValue());
        } catch (final ArithmeticException e) {
            throw new RuntimeException(numericFormat + ": " + v, e);
        }
    }

}
