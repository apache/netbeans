/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.Query;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author pfiala
 */
public abstract class QueryMethodsTableModel extends InnerTableModel {

    protected final EntityHelper.Queries queries;

    public QueryMethodsTableModel(String[] columnNames, int[] columnWidths, final EntityHelper.Queries queries) {
        super(null, columnNames, columnWidths);
        this.queries = queries;
        queries.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                Object source = evt.getSource();
                if (source instanceof Entity) {
                    tableChanged();
                } else if (source instanceof Query) {
                    for (int i = 0, n = getRowCount(); i < n; i++) {
                        QueryMethodHelper queryMethodHelper = getQueryMethodHelper(i);
                        if (queryMethodHelper.query == source) {
                            fireTableRowsUpdated(i, i);
                            return;
                        }
                    }
                } else {
                    fireTableDataChanged();
                }
            }
        });
    }

    public void removeRow(int row) {
        getQueryMethodHelper(row).removeQuery();
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public abstract QueryMethodHelper getQueryMethodHelper(int row);

}
