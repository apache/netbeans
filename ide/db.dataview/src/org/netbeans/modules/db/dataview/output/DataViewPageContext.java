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

package org.netbeans.modules.db.dataview.output;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.openide.util.Mutex;

/**
 * Holds data view page pointers and the current page data set
 *
 * @author Ahimanikya Satapathy
 */
class DataViewPageContext {
    public static final String PROP_pageSize = "pageSize";
    public static final String PROP_currentPos = "currentPos";
    public static final String PROP_tableMetaData = "tableMetaData";
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private int pageSize = 10;
    private int currentPos = 1;
    private DataViewDBTable tableMetaData = null;
    private final DataViewTableUIModel model = new DataViewTableUIModel(new DBColumn[0]);

    DataViewPageContext(int pageSize) {
        this.pageSize = pageSize;
    }

    public DataViewDBTable getTableMetaData() {
        return tableMetaData;
    }

    public void setTableMetaData(DataViewDBTable tableMetaData) {
        DataViewDBTable old = this.tableMetaData;
        this.tableMetaData = tableMetaData;
        firePropertyChange(PROP_tableMetaData, old, tableMetaData);
        resetEditableState();
    }

    void resetEditableState() {
        model.setEditable(tableMetaData == null ? false : tableMetaData.hasOneTable());
    }

    int getPageSize() {
        return pageSize;
    }

    synchronized void setPageSize(int pageSize) {
        int oldPageSize = this.pageSize;
        this.pageSize = pageSize;
        firePropertyChange(PROP_pageSize, oldPageSize, pageSize);
    }

    int getCurrentPos() {
        return currentPos;
    }

    private synchronized void setCurrentPos(int currentPos) {
        int oldPos = this.currentPos;
        this.currentPos = currentPos;
        firePropertyChange(PROP_currentPos, oldPos, currentPos);
    }

    synchronized void first() {
        setCurrentPos(1);
    }

    synchronized void previous() {
        setCurrentPos(getCurrentPos() - pageSize);
    }

    synchronized void next() {
        setCurrentPos(getCurrentPos() + pageSize);
    }

    DataViewTableUIModel getModel() {
        return model;
    }

    boolean hasRows() {
        return model.getRowCount() > 0;
    }

    boolean hasNext() {
        if (pageSize == 0) {
            return false;
        }
        return getModel().getRowCount() >= pageSize;
    }

    boolean hasOnePageOnly() {
        return pageSize == 0 || getModel().getRowCount() < pageSize;
    }

    boolean hasPrevious() {
        return pageSize != 0 && ((currentPos - pageSize) > 0) && hasRows();
    }

    boolean isLastPage() {
        return pageSize == 0 || getModel().getRowCount() < pageSize;
    }

    boolean refreshRequiredOnInsert() {
        return pageSize == 0 || (isLastPage() && model.getRowCount() <= pageSize);
    }

    /**
     * Ensure the property change event is dispatched into the EDT
     *
     * @param propertyName
     * @param oldValue
     * @param newValue
     */
    protected void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
        Mutex.EVENT.writeAccess(new Runnable() {
            @Override
            public void run() {
                pcs.firePropertyChange(propertyName, oldValue, newValue);
            }
        });

    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }
}
