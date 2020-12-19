/*
 * Copyright (C) 2020.  Dennis Hoehl
 * This file is part of DG report generator <https://github.com/dhoehl/dg-report-generator>.
 *
 * DG report generator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DG report generator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DG report generator.  If not, see <http://www.gnu.org/licenses/>.
 */

package dhoehl.model;

import java.util.ArrayList;
import java.util.List;

public class Table {
    private final List<Column> mTable;

    public Table() {
        mTable = new ArrayList<>();
    }

    public void addRow(String[] row) {
        /* Check if there a enough columns already */
        if (mTable.size() < row.length) {
            for (int i = mTable.size(); i < row.length; i++) {
                mTable.add(new Column());
            }
        }
        for (int i = 0; i < row.length; i++) {
            mTable.get(i).add(row[i]);
        }
    }

    public String getCell(int row, int column) {
        //Sanity check
        if (!(mTable.size() > column) || !(mTable.get(column).size() > row)) {
            throw new IndexOutOfBoundsException("Row or column not within bounds");
        }
        return mTable.get(column).get(row);
    }

    public void deleteRow(int rowNumber) {
        //Sanity check
        for (Column row : mTable) {
            if (!(row.size() > rowNumber)) {
                throw new IndexOutOfBoundsException("Row not within bounds");
            }
        }
        for (Column column : mTable) {
            column.remove(rowNumber);
        }
    }

    public int getColumns() {
        return mTable.size();
    }

    public int getRows() {
        return mTable.get(0).size();
    }

    public String getRow(int i) {
        StringBuilder sb = new StringBuilder();
        for (Column c : mTable)
            sb.append(c.get(i)).append(",");
        return sb.toString();
    }

    //Wrapper to look nice
    public static class Column extends ArrayList<String> {

    }
}
