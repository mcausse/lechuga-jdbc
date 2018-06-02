package org.moncheta.jdbc.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapResultSetExtractor implements ResultSetExtractor<List<Map<String, Object>>> {

    List<String> columnNames;

    @Override
    public List<Map<String, Object>> extract(ResultSet rs) throws SQLException {

        if (columnNames == null) {
            columnNames = new ArrayList<>();
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                columnNames.add(rs.getMetaData().getColumnName(i));
            }
        }

        List<Map<String, Object>> r = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (String col : columnNames) {
                row.put(col, rs.getObject(col));
            }
            r.add(row);
        }

        return r;
    }

}
