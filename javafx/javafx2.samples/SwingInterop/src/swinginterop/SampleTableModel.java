/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package swinginterop;

import java.util.Arrays;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.BarChart;

/**
 * SampleTableModel
 */
public class SampleTableModel extends AbstractTableModel {
    private static ObservableList<BarChart.Series> bcData;
    
    private final String[] names = {"2007", "2008", "2009"};

    // TOOD - helpful if these constants had a descriptive constant name 
    private Object[][] data = {
            {567D,  956D,  1154D},
            {1292D, 1665D, 1927D},
            {1292D, 2559D, 2774D}
    };

    public double getTickUnit() {
        return 1000;
    }

    public List<String> getColumnNames() {
        return Arrays.asList(names);
    }

    @Override
    public int getRowCount() {
        return data.length;
    }

    @Override
    public int getColumnCount() {
        return names.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        return data[row][column];
    }

    @Override
    public String getColumnName(int column) {
        return names[column];
    }

    @Override
    public Class getColumnClass(int column) {
        return getValueAt(0, column).getClass();
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        if (value instanceof Double) {
            data[row][column] = (Double) value;
        }

        fireTableCellUpdated(row, column);
    }

    public ObservableList<BarChart.Series> getBarChartData() {
        if (bcData == null) {
            bcData = FXCollections.<BarChart.Series>observableArrayList();
            for (int row = 0; row < getRowCount(); row++) {
                ObservableList<BarChart.Data> series = FXCollections.<BarChart.Data>observableArrayList();
                for (int column = 0; column < getColumnCount(); column++) {
                    series.add(new BarChart.Data(getColumnName(column), getValueAt(row, column)));
                }
                bcData.add(new BarChart.Series(series));
            }
        }
        return bcData;
    }

}
