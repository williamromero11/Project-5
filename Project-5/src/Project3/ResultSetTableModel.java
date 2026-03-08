package Project3;

import javax.swing.table.AbstractTableModel;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultSetTableModel extends AbstractTableModel {
    private final List<String> columns = new ArrayList<>();
    private final List<List<Object>> rows = new ArrayList<>();

    public ResultSetTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int colCount = md.getColumnCount();
        for (int i = 1; i <= colCount; i++) {
            columns.add(md.getColumnLabel(i));
        }
        while (rs.next()) {
            List<Object> row = new ArrayList<>(colCount);
            for (int i = 1; i <= colCount; i++) {
                row.add(rs.getObject(i));
            }
            rows.add(row);
        }
    }

    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return columns.size(); }
    @Override public String getColumnName(int col) { return columns.get(col); }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return rows.get(rowIndex).get(columnIndex);
    }
}