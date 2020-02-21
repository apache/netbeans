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

package org.netbeans.modules.cnd.debugger.dbx.arraybrowser;

import javax.swing.table.AbstractTableModel;

class DataModel extends AbstractTableModel {
    private final String[] columnNames;
    private final Object[][] data;
    private int numberOfRows = 0;

    public DataModel(int row, int col) {
	super();
	numberOfRows = row;
	columnNames = new String[col];
	data = new Object[row][col];

	for (int i = 0; i < col; i++)
	    columnNames[i] = new Integer(i).toString();

        for (int i = 0; i < row; i++) {
	    for (int j = 0; j < col; j++) {
                data[i][j] = new String(""); 
	    }
	}
    }

    public int getColumnCount () {
	return columnNames.length;
    }

    public int getRowCount () {
	return numberOfRows;
    }

    @Override
    public String getColumnName(int col) {
	return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
	return data[row][col];
    }

    @Override
    public Class<?> getColumnClass(int c) {
	return getValueAt(0, c).getClass();
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
	data[row][col] = value;
    }
}
