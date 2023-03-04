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

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.CharConversionException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;

/**
 * DataView to show data of a given sql query string, provides static method to create 
 * the DataView Pannel from a given sql query string and a connection. 
 *
 * TODO: Show execution plan for executed query
 * TODO: Navigate foreign key relationships in results
 * 
 * @author Ahimanikya Satapathy
 */
public class DataView {
    private static final Logger LOG = Logger.getLogger(DataView.class.getName());
    private static final int MAX_TAB_LENGTH = 25;
    private final List<Throwable> errMessages = new ArrayList<>();
    private final List<SQLWarning> warningMessages = new ArrayList<>();
    private final List<Integer> updateCount = new ArrayList<>();
    private final List<Long> fetchTimes = new ArrayList<>();
    private DatabaseConnection dbConn;
    private String sqlString; // Once Set, Data View assumes it will never change
    private SQLStatementGenerator stmtGenerator;
    private SQLExecutionHelper execHelper;
    private final List<DataViewPageContext> dataPage = new ArrayList<>();
    private final List<DataViewUI> dataViewUI = new ArrayList<>();
    private JComponent container;
    private int initialPageSize = org.netbeans.modules.db.dataview.api.DataViewPageContext.getStoredPageSize();
    private boolean nbOutputComponent = false;
    private long executionTime;
    private int errorPosition = -1;

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
        assert dbConn != null;

        DataView dv = new DataView();
        dv.dbConn = dbConn;
        dv.sqlString = sqlString.trim();
        dv.nbOutputComponent = false;
        if(pageSize >= 0) {
            dv.initialPageSize = pageSize;
        }
        try {
            dv.execHelper = new SQLExecutionHelper(dv);
            dv.execHelper.initialDataLoad();
            dv.stmtGenerator = new SQLStatementGenerator();
        } catch (Exception ex) {
            dv.setErrorStatusText(null, null, ex);
        }
        return dv;
    }

    public static DataView create(DatabaseConnection dbConn, String sqlString, int pageSize, boolean nbOutputComponent) {
        DataView dataView = create(dbConn, sqlString, pageSize);
        dataView.nbOutputComponent = nbOutputComponent;
        return dataView;
    }

    /**
     * Create the UI component and renders the data fetched from database on create()
     * 
     * @param dataView DataView Object created using create()
     * @return a JComponent that after rending the given dataview
     */
    public synchronized List<Component> createComponents() {
        List<Component> results;
        if (! hasResultSet()) {
            return Collections.emptyList();
        }

        if(dataPage.size() > 1) {
            container = new JTabbedPane();
        } else {
            container = new JPanel(new BorderLayout());
        }

        for (int i = 0; i < dataPage.size(); i++) {
            DataViewUI ui = new DataViewUI(this, dataPage.get(i), nbOutputComponent);
            ui.setName("Result Set " + i);
            dataViewUI.add(ui);
            container.add(ui);
            resetToolbar(hasExceptions());
        }

        String sql = getSQLString();
        String sqlSpaceNormalized = sql.replaceAll("\\s+", " ");
        if (sqlSpaceNormalized.length() > MAX_TAB_LENGTH) {
            String trimmed = sqlSpaceNormalized.substring(0, MAX_TAB_LENGTH) + "\u2026";
            container.setName(trimmed);
        } else {
            container.setName(sqlSpaceNormalized);
        }

        try {
            // Limit SQL length to 512 chars to create excessive tooltip
            int length = Math.min(sql.length(), 512);
            String displaySQL = sql.substring(0, length);
            if(sql.length() > 512) {
                displaySQL += "\u2026";
            }
            container.setToolTipText(
                    NbBundle.getMessage(DataView.class, "DataViewUI_ToolTip",
                            XMLUtil.toAttributeValue(dbConn.getDisplayName()),
                            XMLUtil.toAttributeValue(displaySQL)));
        } catch (CharConversionException ex) {
            LOG.log(Level.WARNING, "", ex);
        }

        results = new ArrayList<>();
        results.add(container);
        return results;
    }

    /**
     * Returns true if there were any expection in the last database call.
     * 
     * @return true if error occurred in last database call, false otherwise.
     */
    public boolean hasExceptions() {
        return !errMessages.isEmpty();
    }

    /**
     * Returns true if there were any warnings in the last database call.
     * 
     * @return true if a warning resulted from the last database call, false otherwise.
     */
    public boolean hasWarnings() {
        return !warningMessages.isEmpty();
    }
    
    /**
     * Returns true if the statement executed has ResultSet.
     * 
     * @return true if the statement executed has ResultSet, false otherwise.
     */
    public boolean hasResultSet() {
        return dataPage.size() > 0;
    }

    /**
     * Returns Collection of a error messages of Throwable type, if there were any 
     * expection in the last database call, empty otherwise
     * 
     * @return Collection<Throwable>
     */
    public Collection<Throwable> getExceptions() {
        return Collections.unmodifiableCollection(errMessages);
    }

    /**
     * Returns Collection of SQLWarnings, if there were any 
     * warnings in the last database call, empty otherwise
     * 
     * @return Collection<Throwable>
     */
    public Collection<SQLWarning> getWarnings() {
        return Collections.unmodifiableCollection(warningMessages);
    }
    
    /**
     * Get updated row count for the last executed sql statement.
     * 
     * @return number of rows updated in last execution, -1 if no rows updated
     */
    public int getUpdateCount() {
        int result = 0;
        for(Integer uc: updateCount) {
            if(uc != null && uc > 0) {
                result += uc;
            }
        }
        if(result > 0) {
            return result;
        } else {
            return -1;
        }
    }

    public List<Integer> getUpdateCounts() {
        return new ArrayList<>(this.updateCount);
    }
    
    public List<Long> getFetchTimes() {
        return new ArrayList<>(this.fetchTimes);
    }
    
    /**
     * Get execution time for the last executed sql statement
     * 
     * @return execution time for last executed sql statement in milliseconds
     */
    public long getExecutionTime() {
        return executionTime;
    }

    /**
     * Returns editing tool bar items.
     * 
     * @return an array of JButton
     */
    public JButton[] getEditButtons() {
        assert nbOutputComponent != false;
        return dataViewUI.get(0).getEditButtons();
    }

    public synchronized void setEditable(final boolean editable) {
        Mutex.EVENT.writeAccess(new Runnable() {
            @Override
            public void run() {
                for (DataViewPageContext pageContext : dataPage) {
                    pageContext.getModel().setEditable(editable);
                }
            }
        });
    }

    // Used by org.netbeans.modules.db.dataview.api.DataViewPageContext#getPageSize
    public int getPageSize() {
        if (dataViewUI.isEmpty()) {
            return initialPageSize;
        }
        return dataViewUI.get(0).getPageSize();
    }

    // Non API modules follow

    List<DataViewPageContext> getPageContexts() {
        return this.dataPage;
    }

    DataViewPageContext getPageContext(int i) {
        return this.dataPage.get(i);
    }

    DataViewPageContext addPageContext(final DataViewDBTable table) throws InterruptedException {
        try {
            final DataViewPageContext pageContext = new DataViewPageContext(initialPageSize);
            this.dataPage.add(pageContext);
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    pageContext.setTableMetaData(table);
                    pageContext.getModel().setColumns(table.getColumns().toArray(new DBColumn[0]));
                }
            });
            return pageContext;
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    DatabaseConnection getDatabaseConnection() {
        return dbConn;
    }

    String getSQLString() {
        return sqlString;
    }

    SQLExecutionHelper getSQLExecutionHelper() {
        if (execHelper == null) {
            execHelper = new SQLExecutionHelper(this);
        }
        return execHelper;
    }

    SQLStatementGenerator getSQLStatementGenerator() {
        if (stmtGenerator == null) {
            stmtGenerator = new SQLStatementGenerator();
        }
        return stmtGenerator;
    }

    public void resetEditable() {
        Mutex.EVENT.writeAccess(new Runnable() {
            @Override
            public void run() {
                for (DataViewPageContext pageContext : dataPage) {
                    pageContext.resetEditableState();
                }
            }
        });
    }

    synchronized void disableButtons() {
        assert dataViewUI != null;
        Mutex.EVENT.writeAccess(new Runnable() {

            @Override
            public void run() {
                for(DataViewUI ui: dataViewUI) {
                    ui.disableButtons();
                }
            }
        });
        errMessages.clear();
        warningMessages.clear();
        updateCount.clear();
        fetchTimes.clear();
    }

    synchronized void removeComponents() {
        Mutex.EVENT.writeAccess(new Runnable() {

            @Override
            public void run() {
                if (container != null) {
                    try {
                        container.getParent().remove(container);
                    } catch (NullPointerException ex) {
                    }
                    container.removeAll();
                    container.repaint();
                    container.revalidate();
                }
            }
        });
    }

    void setInfoStatusText(String statusText) {
        if (statusText != null) {
            StatusDisplayer.getDefault().setStatusText(statusText);
        }
    }

    synchronized void setErrorStatusText(Connection con, Statement stmt, Throwable ex) {
        if (ex != null) {
            if (ex instanceof DBException) {
                if (ex.getCause() instanceof SQLException) {
                    errMessages.add(ex.getCause());
                }
            }
            errMessages.add(ex);
            errorPosition = ErrorPositionExtractor.extractErrorPosition(con, stmt, ex, sqlString);
            
            String title = NbBundle.getMessage(DataView.class, "MSG_error");
            StatusDisplayer.getDefault().setStatusText(title + ": " + ex.getMessage());
        }
    }

    synchronized void setErrorStatusText(Connection con, Statement stmt, String message, Throwable ex) {
        if (ex != null) {
            errMessages.add(ex);
        }

        errorPosition = ErrorPositionExtractor.extractErrorPosition(con, stmt, ex, sqlString);
        
        String title = NbBundle.getMessage(DataView.class, "MSG_error");
        StatusDisplayer.getDefault().setStatusText(title + ": " + message);
    }

    void resetToolbar(final boolean wasError) {
        assert dataViewUI != null;
        Mutex.EVENT.writeAccess(new Runnable() {

            @Override
            public void run() {
                for(DataViewUI ui: dataViewUI) {
                    ui.resetToolbar(wasError);
                }
            }
        });
    }

    void addUpdateCount(int updateCount) {
        synchronized (this.updateCount) {
            this.updateCount.add(updateCount);
            this.fetchTimes.add(null);
        }
    }

    void addFetchTime(long fetchTime) {
        synchronized (this.updateCount) {
            this.updateCount.add(null);
            this.fetchTimes.add(fetchTime);
        }
    }
    
    void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    /**
     * If exception is reportet this indicates the position of the error
     * 
     * @return position of reported error, -1 if not available
     */
    public int getErrorPosition() {
        if(errMessages.isEmpty()) {
            return -1;
        } else {
            return errorPosition;
        }
    }
    
    public void addWarning(SQLWarning warning) {
        warningMessages.add(warning);
    }
    
    private DataView() {
    }
}
