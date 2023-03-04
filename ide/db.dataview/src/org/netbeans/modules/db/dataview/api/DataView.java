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
package org.netbeans.modules.db.dataview.api;

import java.awt.Component;
import java.sql.SQLWarning;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import org.netbeans.api.db.explorer.DatabaseConnection;

/**
 * DataView to show data of a given sql query string, provides static method to create 
 * the DataView Pannel from a given sql query string and a connection. 
 *
 * @author Ahimanikya Satapathy
 */
public class DataView {

    org.netbeans.modules.db.dataview.output.DataView delegate;

    /**
     * Create and populate a DataView Object. Populates 1st data page of default size.
     * The caller can run this in background thread and then create the GUI components 
     * to render to render the DataView by calling DataView.createComponent()
     * 
     * @param dbConn instance of DBExplorer DatabaseConnection 
     * @param queryString SQL query string
     * @param pageSize default page size for this data view
     * @return a new DataView instance
     */
    public static DataView create(DatabaseConnection dbConn, String sqlString, int pageSize) {
        DataView dataView = new DataView();
        dataView.delegate = org.netbeans.modules.db.dataview.output.DataView.create(dbConn, sqlString, pageSize);
        return dataView;
    }

    public static DataView create(DatabaseConnection dbConn, String sqlString, int pageSize, boolean nbOutputComponent) {
        DataView dataView = new DataView();
        dataView.delegate = org.netbeans.modules.db.dataview.output.DataView.create(dbConn, sqlString, pageSize, nbOutputComponent);
        return dataView;
    }

    /**
     * Create the UI component and renders the data fetched from database on create()
     * 
     * @param dataView DataView Object created using create()
     * @return a JComponent that after rending the given dataview
     */
    public List<Component> createComponents() {
        return delegate.createComponents();
    }

    /**
     * Returns true if there were any expection in the last database call.
     * 
     * @return true if error occurred in last database call, false otherwise.
     */
    public boolean hasExceptions() {
        return delegate.hasExceptions();
    }

    /**
     * Returns true if there were any warnings in the last database call.
     * 
     * @return true if a warning resulted from the last database call, false otherwise.
     */
    public boolean hasWarnings() {
        return delegate.hasWarnings();
    }
    
    /**
     * Returns true if the statement executed has ResultSet.
     * 
     * @return true if the statement executed has ResultSet, false otherwise.
     */
    public boolean hasResultSet() {
        return delegate.hasResultSet();
    }

    /**
     * Returns Collection of a error messages of Throwable type, if there were any 
     * expection in the last database call, empty otherwise
     * 
     * @return Collection<Throwable>
     */
    public Collection<Throwable> getExceptions() {
        return delegate.getExceptions();
    }

    /**
     * Returns Collection of SQLWarnings, if there were any 
     * warnings in the last database call, empty otherwise
     * 
     * @return Collection<Throwable>
     */
    public Collection<SQLWarning> getWarnings() {
        return delegate.getWarnings();
    }
    
    /**
     * If exception is reportet this indicates the position of the error.
     * 
     * <p>The reported position is the zero-based index into the supplied SQL</p>
     * 
     * @return position of reported error, -1 if not available
     */
    public int getErrorPosition() {
        return delegate.getErrorPosition();
    }
    
    /**
     * Get updated row count for the last executed sql statement.
     * 
     * @return number of rows updated in last execution, -1 if no rows updated
     */
    public int getUpdateCount() {
        return delegate.getUpdateCount();
    }

    /**
     * Get modified row counts for the last executed sql statement.
     * 
     * @return number of rows updated in last execution, index is the xth result for this statement
     */
    public List<Integer> getUpdateCounts() {
        return delegate.getUpdateCounts();
    }
    
    /**
     * Get fetchtimes for the resultsets of the last executed sql statement.
     * 
     * @return fetchtime in ms, index is the xth result for this statement
     */
    public List<Long> getFetchTimes() {
        return delegate.getFetchTimes();
    }
    
    /**
     * Get execution time for the last executed sql statement
     * 
     * @return execution time for last executed sql statement in milliseconds
     */
    public long getExecutionTime() {
        return delegate.getExecutionTime();
    }

    /**
     * Returns editing tool bar items.
     * 
     * @return an array of JButton
     */
    public JButton[] getEditButtons() {
        return delegate.getEditButtons();
    }

    /**
     * Sets editable mode to true/false for this component
     *
     * @param editable if set to false, all the editing functionality will be disabled
     */
    public void setEditable(boolean editable) {
        delegate.setEditable(editable);
    }

    private DataView() {
    }
}
