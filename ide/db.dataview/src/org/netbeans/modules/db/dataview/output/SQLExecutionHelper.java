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

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.meta.DBConnectionFactory;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.netbeans.modules.db.dataview.meta.DBMetaDataFactory;
import org.netbeans.modules.db.dataview.meta.DBTable;
import org.netbeans.modules.db.dataview.util.DBReadWriteHelper;
import org.netbeans.modules.db.dataview.util.DataViewUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Cancellable;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * This class assumes there will be only one connection which can't be closed.
 *
 * @author Ahimanikya Satapathy
 */
class SQLExecutionHelper {

    private static final Logger LOGGER = Logger.getLogger(SQLExecutionHelper.class.getName());
    private final DataView dataView;
    // the RequestProcessor used for executing statements.
    private final RequestProcessor rp = new RequestProcessor("SQLStatementExecution", 20, true); // NOI18N
    private static final String LIMIT_CLAUSE = "LIMIT ";               // NOI18N
    public static final String OFFSET_CLAUSE = "OFFSET ";              // NOI18N
    private static Pattern GROUP_BY_IN_SELECT = null;
    private boolean limitSupported = false;
    private boolean useScrollableCursors = false;
    private int resultSetScrollType = ResultSet.TYPE_FORWARD_ONLY;
    private boolean supportesMultipleResultSets = false;

    SQLExecutionHelper(DataView dataView) {
        this.dataView = dataView;
    }

    void initialDataLoad() {
        assert (! SwingUtilities.isEventDispatchThread()) : "Must be called off the EDT!";

        /**
         * Wrap initializing the SQL result into a runnable. This makes it
         * possible to wait for the result in the main thread and cancel the
         * running statement from the main thread.
         *
         * If no statement is run - Thread.interrupted is checked at critical
         * points and allows us to do an early exit.
         *
         * See #159929.
         */
        class Loader implements Runnable, Cancellable {
            // Indicate whether the execution is finished
            private boolean finished = false;
            private Connection conn = null;
            private Statement stmt = null;
            private Thread loaderThread = null;

            @Override
            public void run() {
                loaderThread = Thread.currentThread();
                try {
                    DatabaseConnection dc = dataView.getDatabaseConnection();
                    conn = DBConnectionFactory.getInstance().getConnection(dc);
                    checkNonNullConnection(conn);
                    checkSupportForMultipleResultSets(conn);
                    DBMetaDataFactory dbMeta = new DBMetaDataFactory(conn);
                    limitSupported = dbMeta.supportsLimit();
                    String sql = dataView.getSQLString();
                    boolean isSelect = isSelectStatement(sql);

                    updateScrollableSupport(conn, dc, sql);

                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    
                    stmt = prepareSQLStatement(conn, sql);

                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    
                    boolean isResultSet = executeSQLStatementForExtraction(stmt, sql);

                    int updateCount = -1;
                    if(! isResultSet) {
                        updateCount = stmt.getUpdateCount();
                    }
                    
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }

                    ResultSet rs;

                    while (true) {
                        if (isResultSet) {
                            rs = stmt.getResultSet();

                            Collection<DBTable> tables = dbMeta.generateDBTables(rs, sql, isSelect);
                            DataViewDBTable dvTable = new DataViewDBTable(tables);
                            DataViewPageContext pageContext = dataView.addPageContext(dvTable);

                            loadDataFrom(pageContext, rs);

                            DataViewUtils.closeResources(rs);

                            dbMeta.postprocessTables(tables);
                        } else {
                            synchronized (dataView) {
                                dataView.addUpdateCount(updateCount);
                            }
                        }
                        if (supportesMultipleResultSets) {
                            isResultSet = stmt.getMoreResults();
                            updateCount = stmt.getUpdateCount();
                            if (isResultSet == false && updateCount == -1) {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                } catch (final SQLException | RuntimeException sqlEx) {
                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {
                            @Override
                            public void run() {
                                dataView.setErrorStatusText(conn, stmt, sqlEx);
                            }
                        });
                    } catch (InterruptedException | InvocationTargetException ex) {
                        assert false;
                        // Ok - we were denied access to Swing EDT
                    }
                } catch (InterruptedException ex) {
                    // Expected when interrupted while waiting to get enter to
                    // the swing EDT
                } finally {
                    loaderThread = null;
                    DataViewUtils.closeResources(stmt);
                    synchronized (Loader.this) {
                        finished = true;
                        this.notifyAll();
                    }
                }
            }

            @Override
            public boolean cancel() {
                if (stmt != null) {
                    try {
                        stmt.cancel();
                    } catch (SQLException sqlEx) {
                        LOGGER.log(Level.FINE, null, sqlEx);
                        // Ok! The DBMS might not support Statement-Canceling
                    }
                }
                if(loaderThread != null) {
                    try {
                        loaderThread.interrupt();
                    } catch (NullPointerException ex) {
                        // Ignore - the call was finished between checking 
                        // loaderThread an calling interrupt on it.
                    }
                }
                return true;
            }

            /**
             * Check that the connection is not null. If it is null, try to find
             * cause of the failure and throw an exception.
             */
            private void checkNonNullConnection(Connection conn) throws
                    SQLException {
                if (conn == null) {
                    String msg;
                    Throwable t = DBConnectionFactory.getInstance()
                            .getLastException();
                    if (t != null) {
                        msg = t.getMessage();
                    } else {
                        msg = NbBundle.getMessage(SQLExecutionHelper.class,
                                "MSG_connection_failure", //NOI18N
                                dataView.getDatabaseConnection());
                    }
                    NotifyDescriptor nd = new NotifyDescriptor.Message(msg,
                            NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notifyLater(nd);
                    LOGGER.log(Level.INFO, msg, t);
                    throw new SQLException(msg, t);
                }
            }

            private void checkSupportForMultipleResultSets(Connection conn) {
                try {
                    supportesMultipleResultSets = conn.getMetaData().supportsMultipleResultSets();
                } catch (SQLException | RuntimeException e) {
                    LOGGER.log(Level.INFO, "Database driver throws exception "  //NOI18N
                            + "when checking for multiple resultset support."); //NOI18N
                    LOGGER.log(Level.FINE, null, e);
                }
            }
        }
        Loader loader = new Loader();
        Future<?> f = rp.submit(loader);
        try {
            f.get();
        } catch (InterruptedException ex) {
            f.cancel(true);
        } catch (ExecutionException ex) {
            throw new RuntimeException(ex.getCause());
        }
        synchronized (loader) {
            while (true) {
                if (!loader.finished) {
                    try {
                        loader.wait();
                    } catch (InterruptedException ex) {
                    }
                } else {
                    break;
                }
            }
        }
    }

    /**
     * @param pageContext
     * @param table
     * @param insertSQLs
     * @param insertedRows
     * @return count of successfully inserted rows
     */
    int executeInsertRow(final DataViewPageContext pageContext,
            final DBTable table,
            final String[] insertSQLs,
            final Object[][] insertedRows) {

        assert (!SwingUtilities.isEventDispatchThread());

        dataView.setEditable(false);

        int done = 0;
        Exception caughtException = null;

        try {
            Connection conn = dataView.getDatabaseConnection().getJDBCConnection();
            List<DBColumn> columns = table.getColumnList();

            for (int j = 0; j < insertSQLs.length; j++) {
                try (PreparedStatement pstmt = conn.prepareStatement(insertSQLs[j])) {
                    int pos = 1;
                    for (int i = 0; i < insertedRows[j].length; i++) {
                        Object val = insertedRows[j][i];

                        // Check for Constant e.g <NULL>, <DEFAULT>, <CURRENT_TIMESTAMP> etc
                        if (DataViewUtils.isSQLConstantString(val, columns.get(i))) {
                            continue;
                        }

                        // literals
                        int colType = columns.get(i).getJdbcType();
                        DBReadWriteHelper.setAttributeValue(pstmt, pos++, colType, val);
                    }

                    int rows = pstmt.executeUpdate();

                    if (rows != 1) {
                        throw new SQLException("MSG_failure_insert_rows");
                    }
                    done++;
                }
            }
        } catch (DBException | SQLException ex) {
            LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
            caughtException = ex;
        } finally {
            dataView.resetEditable();
        }

        final Exception finalCaught = caughtException;

        // refresh when required
        Boolean needRequery;
        try {
            needRequery = Mutex.EVENT.writeAccess(new Mutex.ExceptionAction<Boolean>() {
                @Override
                public Boolean run() {
                    if (finalCaught != null) {
                        DialogDisplayer.getDefault().notifyLater(
                                new NotifyDescriptor.Message(
                                        finalCaught.getLocalizedMessage()));
                    }
                    return pageContext.refreshRequiredOnInsert();
                }
            });
        } catch (MutexException ex) {
            needRequery = true;
        }
        
        if (needRequery) {
            SQLExecutionHelper.this.executeQuery();
        } else {
            Mutex.EVENT.writeAccess(new Runnable() {
                @Override
                public void run() {
                    synchronized (dataView) {
                        dataView.resetToolbar(false);
                    }
                }
            });
        }

        return done;
    }

    void executeDeleteRow(final DataViewPageContext pageContext, final DBTable table, final DataViewTableUI rsTable) {
        dataView.setEditable(false);

        SQLStatementGenerator generator = dataView.getSQLStatementGenerator();
        String title = NbBundle.getMessage(SQLExecutionHelper.class, "LBL_sql_delete");

        class DeleteElement {
            public List<Object> values = new ArrayList<>();
            public List<Integer> types = new ArrayList<>();
            public String sql;
        }

        final List<DeleteElement> rows = new ArrayList<>();
        for(int viewRow: rsTable.getSelectedRows()) {
            int modelRow = rsTable.convertRowIndexToModel(viewRow);
            DeleteElement de = new DeleteElement();
            de.sql = generator.generateDeleteStatement(table, de.types, de.values, modelRow, rsTable.getModel());
            rows.add(de);
        }

        SQLStatementExecutor executor = new SQLStatementExecutor(dataView, title, "", true) {
            @Override
            public void execute() throws SQLException, DBException {
                dataView.setEditable(false);
                for (DeleteElement de: rows) {
                    if (Thread.currentThread().isInterrupted() || error) {
                        break;
                    }
                    deleteARow(de);
                }
            }

            private void deleteARow(DeleteElement deleteRow) throws SQLException, DBException {
                PreparedStatement pstmt = conn.prepareStatement(deleteRow.sql);
                try {
                    int pos = 1;
                    for (Object val : deleteRow.values) {
                        DBReadWriteHelper.setAttributeValue(pstmt, pos, deleteRow.types.get(pos - 1), val);
                        pos++;
                    }

                    int rows = executePreparedStatement(pstmt);
                    if (rows == 0) {
                        error = true;
                        errorMsg += NbBundle.getMessage(SQLExecutionHelper.class, "MSG_no_match_to_delete");
                    } else if (rows > 1) {
                        error = true;
                        errorMsg += NbBundle.getMessage(SQLExecutionHelper.class, "MSG_no_unique_row_for_match");
                    }
                } finally {
                    DataViewUtils.closeResources(pstmt);
                }
            }

            @Override
            public void finished() {
                dataView.resetEditable();
                commitOrRollback(NbBundle.getMessage(SQLExecutionHelper.class, "LBL_delete_command"));
            }

            @Override
            protected void executeOnSuccess() {
                SQLExecutionHelper.this.executeQuery();
            }
        };

        RequestProcessor.Task task = rp.create(executor);
        executor.setTask(task);
        task.schedule(0);
    }

    void executeUpdateRow(final DBTable table, final DataViewTableUI rsTable, final boolean selectedOnly) {
        dataView.setEditable(false);

        SQLStatementGenerator generator = dataView.getSQLStatementGenerator();
        final DataViewTableUIModel dataViewTableUIModel = rsTable.getModel();
        String title = NbBundle.getMessage(SQLExecutionHelper.class, "LBL_sql_update");

        class UpdateElement {
            public List<Object> values = new ArrayList<>();
            public List<Integer> types = new ArrayList<>();
            public String sql;
            public Integer key;
        }

        final List<UpdateElement> updateSet = new ArrayList<>();

        int[] viewRows = rsTable.getSelectedRows();
        List<Integer> modelRows = new ArrayList<>();
        for(Integer viewRow: viewRows) {
            modelRows.add(rsTable.convertRowIndexToModel(viewRow));
        }

        for (Integer key : dataViewTableUIModel.getUpdateKeys()) {
            if (modelRows.contains(key) || (!selectedOnly)) {
                UpdateElement ue = new UpdateElement();
                try {
                    ue.key = key;
                    ue.sql = generator.generateUpdateStatement(table, key,
                            dataViewTableUIModel.getChangedData(key), ue.values, ue.types,
                            rsTable.getModel());
                    updateSet.add(ue);
                } catch (DBException ex) {
                    // The model protects against illegal values, so rethrow
                    throw new RuntimeException(ex);
                }
            }
        }

        SQLStatementExecutor executor = new SQLStatementExecutor(dataView, title, "", true) {

            private PreparedStatement pstmt;
            private final Set<Integer> keysToRemove = new HashSet<>();

            @Override
            public void execute() throws SQLException, DBException {
                for (UpdateElement ue : updateSet) {
                    if(Thread.interrupted()) {
                        break;
                    }
                    updateARow(ue);
                    keysToRemove.add(ue.key);
                }
            }

            private void updateARow(UpdateElement ue) throws SQLException, DBException {
                pstmt = conn.prepareStatement(ue.sql);
                int pos = 1;
                for (Object val : ue.values) {
                    DBReadWriteHelper.setAttributeValue(pstmt, pos, ue.types.get(pos - 1), val);
                    pos++;
                }

                try {
                    int rows = executePreparedStatement(pstmt);
                    if (rows == 0) {
                        error = true;
                        errorMsg += NbBundle.getMessage(SQLExecutionHelper.class, "MSG_no_match_to_update");
                    } else if (rows > 1) {
                        error = true;
                        errorMsg += NbBundle.getMessage(SQLExecutionHelper.class, "MSG_no_unique_row_for_match");
                    }
                } finally {
                    DataViewUtils.closeResources(pstmt);
                }
            }

            @Override
            public void finished() {
                dataView.resetEditable();
                commitOrRollback(NbBundle.getMessage(SQLExecutionHelper.class, "LBL_update_command"));
            }

            @Override
            protected void executeOnSuccess() {
                dataView.getSQLExecutionHelper().executeQuery();
                Mutex.EVENT.writeAccess(new Runnable() {
                    @Override
                    public void run() {
                        DataViewTableUIModel tblContext = rsTable.getModel();
                        for (Integer key : keysToRemove) {
                            tblContext.removeUpdateForSelectedRow(key, false);
                        }
                        reinstateToolbar();
                    }
                });
            }
        };
        RequestProcessor.Task task = rp.create(executor);
        executor.setTask(task);
        task.schedule(0);
    }

    // Truncate is allowed only when there is single table used in the query.
    void executeTruncate(final DataViewPageContext pageContext, final DBTable dbTable) {
        String msg = NbBundle.getMessage(SQLExecutionHelper.class, "MSG_truncate_table_progress");
        String title = NbBundle.getMessage(SQLExecutionHelper.class, "LBL_sql_truncate");
        SQLStatementExecutor executor = new SQLStatementExecutor(dataView, title, msg, true) {

            private PreparedStatement stmt = null;

            @Override
            public void execute() throws SQLException, DBException {
                String truncateSql = "TRUNCATE TABLE " + dbTable.getFullyQualifiedName(true); // NOI18N

                try {
                    stmt = conn.prepareStatement(truncateSql);
                    executePreparedStatement(stmt);
                } catch (SQLException sqe) {
                    LOGGER.log(Level.FINE, "TRUNCATE Not supported...will try DELETE * \n"); // NOI18N
                    truncateSql = "DELETE FROM " + dbTable.getFullyQualifiedName(true); // NOI18N
                    stmt = conn.prepareStatement(truncateSql);
                    executePreparedStatement(stmt);
                } finally {
                    DataViewUtils.closeResources(stmt);
                }
            }

            @Override
            public void finished() {
                commitOrRollback(NbBundle.getMessage(SQLExecutionHelper.class, "LBL_truncate_command"));
            }

            @Override
            protected void executeOnSuccess() {
                pageContext.first();
                SQLExecutionHelper.this.executeQuery();
            }
        };

        RequestProcessor.Task task = rp.create(executor);
        executor.setTask(task);
        task.schedule(0);
    }

    void executeQueryOffEDT() {
        rp.post(new Runnable() {
            @Override
            public void run() {
                executeQuery();
            }
        });
    }

    // Once Data View is created the it assumes the query never changes.
    void executeQuery() {
        assert (! SwingUtilities.isEventDispatchThread());

        String title = NbBundle.getMessage(SQLExecutionHelper.class, "LBL_sql_executequery");
        SQLStatementExecutor executor = new SQLStatementExecutor(dataView, title, dataView.getSQLString(), false) {

            private Statement stmt = null;

            // Execute the Select statement
            @Override
            public void execute() throws SQLException, DBException {
                dataView.setEditable(false);
                String sql = dataView.getSQLString();
                if (Thread.interrupted()) {
                    return;
                }

                stmt = prepareSQLStatement(conn, sql);

                // Execute the query and retrieve all resultsets
                try {
                    if (Thread.interrupted()) {
                        return;
                    }
                    boolean isResultSet = executeSQLStatementForExtraction(stmt, sql);

                    int updateCount = -1;
                    if(! isResultSet) {
                        updateCount = stmt.getUpdateCount();
                    }
                    
                    ResultSet rs = null;
                    int res = -1;

                    while (true) {
                        if (isResultSet) {
                            res++;
                            DataViewPageContext pageContext = dataView.getPageContext(res);
                            rs = stmt.getResultSet();
                            loadDataFrom(pageContext, rs);
                            DataViewUtils.closeResources(rs);
                        } else {
                            synchronized (dataView) {
                                dataView.addUpdateCount(updateCount);
                            }
                        }
                        if (supportesMultipleResultSets) {
                            isResultSet = stmt.getMoreResults();
                            updateCount = stmt.getUpdateCount();
                            if (isResultSet == false && updateCount == -1) {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                } catch (SQLException | InterruptedException sqlEx) {
                    LOGGER.log(Level.INFO, "Failed to retrieve resultset", sqlEx);
                    String title = NbBundle.getMessage(SQLExecutionHelper.class, "MSG_error");
                    String msg = NbBundle.getMessage(SQLExecutionHelper.class, "Confirm_Close");
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(sqlEx.getMessage() + "\n" + msg, title,
                            NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                    if (nd.getValue().equals(NotifyDescriptor.YES_OPTION)) {
                        dataView.removeComponents();
                    }
                }
            }

            @Override
            public void finished() {
                synchronized (dataView) {
                    dataView.resetEditable();
                    if (error) {
                        dataView.setErrorStatusText(conn, stmt, ex);
                    }
                    dataView.resetToolbar(error);
                }
                DataViewUtils.closeResources(stmt);
            }

            @Override
            public boolean cancel() {
                boolean superResult = super.cancel();
                if (stmt != null) {
                    try {
                        stmt.cancel();
                    } catch (SQLException sqlEx) {
                        LOGGER.log(Level.FINEST, null, sqlEx);
                        // Ok! The DBMS might not support Statement-Canceling
                    }
                }
                return superResult;
            }
        };
        RequestProcessor.Task task = rp.create(executor);
        executor.setTask(task);
        task.schedule(0);
    }

    private void loadDataFrom(final DataViewPageContext pageContext, ResultSet rs) throws SQLException, InterruptedException {
        if (rs == null) {
            return;
        }

        int pageSize = pageContext.getPageSize();
        int startFrom;
        if (useScrollableCursors ) {
            startFrom = pageContext.getCurrentPos(); // will use rs.absolute
        } else if (!limitSupported || isLimitUsedInSelect(dataView.getSQLString())) {
            startFrom = pageContext.getCurrentPos(); // need to use slow skip
        } else {
            startFrom = 0; // limit added to select, can start from first item
        }

        final List<Object[]> rows = new ArrayList<>();
        int colCnt = pageContext.getTableMetaData().getColumnCount();
        int curRowPos = 0;
        try {
            long start = System.currentTimeMillis();
            boolean hasNext = false;
            boolean needSlowSkip = true;

            if (useScrollableCursors
                    && (rs.getType() == ResultSet.TYPE_SCROLL_INSENSITIVE
                    || rs.getType() == ResultSet.TYPE_SCROLL_SENSITIVE)) {
                try {
                    hasNext = rs.absolute(startFrom);
                    curRowPos = rs.getRow();
                    needSlowSkip = false;
                } catch (SQLException ex) {
                    LOGGER.log(Level.FINE, "Absolute positioning failed", ex); // NOI18N
                }
            }

            if (needSlowSkip) {
                // Skip till current position
                hasNext = rs.next();
                curRowPos++;
                while (hasNext && curRowPos < startFrom) {
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    hasNext = rs.next();
                    curRowPos++;
                }
            }
            
            // Get next page
            int rowCnt = 0;
            while (((pageSize <= 0) || (pageSize > rowCnt)) && (hasNext)) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                Object[] row = new Object[colCnt];
                for (int i = 0; i < colCnt; i++) {
                    row[i] = DBReadWriteHelper.readResultSet(rs,
                            pageContext.getTableMetaData().getColumn(i), i + 1);
                }
                rows.add(row);
                rowCnt++;
                try {
                    hasNext = rs.next();
                    curRowPos++;
                } catch (SQLException x) {
                    LOGGER.log(Level.INFO, "Failed to forward to next record, cause: " + x.getLocalizedMessage(), x);
                    hasNext = false;
                }
            }
            
            long end = System.currentTimeMillis();
            
            dataView.addFetchTime(end - start);
        } catch (SQLException e) {
            LOGGER.log(Level.INFO, "Failed to set up table model.", e); // NOI18N
            throw e;
        } finally {
            Mutex.EVENT.writeAccess(new Runnable() {
                @Override
                public void run() {
                    pageContext.getModel().setData(rows);
                    pageContext.getModel().setRowOffset(pageContext.getCurrentPos() - 1);
                }
            });
        }
    }

    private Statement prepareSQLStatement(Connection conn, String sql) throws SQLException {
        Statement stmt;
        if (sql.startsWith("{")) { // NOI18N
            stmt = useScrollableCursors
                    ? conn.prepareCall(sql, resultSetScrollType, ResultSet.CONCUR_READ_ONLY)
                    : conn.prepareCall(sql);
        } else if (isSelectStatement(sql)) {
            stmt = useScrollableCursors
                    ? conn.createStatement(resultSetScrollType, ResultSet.CONCUR_READ_ONLY)
                    : conn.createStatement();

            // set a reasonable fetchsize
            setFetchSize(stmt, 50);

            // hint to only query a certain number of rows -> potentially
            // improve performance for low page numbers
            // only usable for "non-total" resultsets
            try {
                Integer maxRows = 0;
                for (DataViewPageContext pageContext : dataView.getPageContexts()) {
                    int currentRows = pageContext.getCurrentPos();
                    int pageSize = pageContext.getPageSize();
                    if (pageSize <= 0) {
                        maxRows = 0;
                        break;
                    } else {
                        maxRows = Math.max(maxRows, currentRows + pageSize);
                    }
                }
                stmt.setMaxRows(maxRows);
            } catch (SQLException exc) {
                LOGGER.log(Level.WARNING, "Unable to set Max row count", exc); // NOI18N
                try {
                    stmt.setMaxRows(0);
                } catch (SQLException ex) {}
            }
        } else {
            stmt = useScrollableCursors
                    ? conn.createStatement(resultSetScrollType, ResultSet.CONCUR_READ_ONLY)
                    : conn.createStatement();
        }
        return stmt;
    }

    private boolean executeSQLStatementForExtraction(Statement stmt, String sql) throws SQLException {
        LOGGER.log(Level.FINE, "Statement: {0}", sql); // NOI18N
        dataView.setInfoStatusText(NbBundle.getMessage(SQLExecutionHelper.class, "LBL_sql_executestmt") + sql);

        long startTime = System.currentTimeMillis();
        boolean isResultSet = false;

        try {
            if (stmt instanceof PreparedStatement) {
                isResultSet = ((PreparedStatement) stmt).execute();
            } else {
                DataViewPageContext pc = dataView.getPageContexts().size() > 0
                        ? dataView.getPageContext(0) : null;
                isResultSet = stmt.execute(sql);
            }
        } catch (NullPointerException ex) {
            LOGGER.log(Level.INFO, "Failed to execute SQL Statement [{0}], cause: {1}", new Object[]{sql, ex});
            throw new SQLException(ex);
        } catch (SQLException sqlExc) {
            LOGGER.log(Level.INFO, "Failed to execute SQL Statement [{0}], cause: {1}", new Object[]{sql, sqlExc});
            throw sqlExc;
        } finally {
            extractWarnings(stmt);
        }

        long executionTime = System.currentTimeMillis() - startTime;
        synchronized (dataView) {
            dataView.setExecutionTime(executionTime);
        }
        return isResultSet;
    }

    private int executePreparedStatement(PreparedStatement stmt) throws SQLException {
        long startTime = System.currentTimeMillis();
        
        stmt.execute();

        long executionTime = System.currentTimeMillis() - startTime;
        String execTimeStr = SQLExecutionHelper.millisecondsToSeconds(executionTime);
        dataView.setInfoStatusText(NbBundle.getMessage(SQLExecutionHelper.class, "MSG_execution_success", execTimeStr));
        
        return stmt.getUpdateCount();
    }

    private boolean isSelectStatement(String queryString) {
        String sqlUpperTrimmed = queryString.trim().toUpperCase();
        return sqlUpperTrimmed.startsWith("SELECT")  // NOI18N
                && (! sqlUpperTrimmed.contains("INTO")); // NOI18N
    }

    private boolean isLimitUsedInSelect(String sql) {
        return sql.toUpperCase().contains(LIMIT_CLAUSE);
    }

    static String millisecondsToSeconds(long ms) {
        NumberFormat fmt = NumberFormat.getInstance();
        fmt.setMaximumFractionDigits(3);
        return fmt.format(ms / 1000.0);
    }

    /**
     * Guarded version of setFetchSize. See #227756.
     */
    private static void setFetchSize(Statement stmt, int fetchSize) {
        try {
            stmt.setFetchSize(fetchSize);
        } catch (SQLException e) {
            // ignore -  used only as a hint to the driver to optimize
            LOGGER.log(Level.INFO, "Unable to set Fetch size", e); // NOI18N
            // But try to reset to default behaviour
            try {
                stmt.setFetchSize(0);
            } catch (SQLException ex) {
            }
        }
    }

    /**
     * Determine if DBMS/Driver supports scrollable resultsets to be able to
     * determine complete row count for a given SQL.
     *
     * @param conn established JDBC connection
     * @param dc connection information
     * @param sql the sql to be executed
     */
    private void updateScrollableSupport(Connection conn, DatabaseConnection dc,
            String sql) {
        useScrollableCursors = dc.isUseScrollableCursors();
        if (!useScrollableCursors) {
            return;
        }
        String driverName = dc.getDriverClass();
        /* Derby fails to support scrollable cursors when invoking 'stored procedures'
         which return resultsets - it fails hard: not throwing a SQLException,
         but terminating the connection - so don't try to use scrollable cursor
         on derby, for "non"-selects */
        if (driverName != null && driverName.startsWith("org.apache.derby")) { //NOI18N
            if (!isSelectStatement(sql)) {
                resultSetScrollType = ResultSet.TYPE_FORWARD_ONLY;
                return;
            }
        }
        /* Try to get a "good" scrollable ResultSet and follow the DBs support */
        try {
            if (conn.getMetaData().supportsResultSetType(
                    ResultSet.TYPE_SCROLL_INSENSITIVE)) {
                resultSetScrollType = ResultSet.TYPE_SCROLL_INSENSITIVE;
            } else if (conn.getMetaData().supportsResultSetType(
                    ResultSet.TYPE_SCROLL_SENSITIVE)) {
                resultSetScrollType = ResultSet.TYPE_SCROLL_SENSITIVE;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Exception while querying" //NOI18N
                    + " database for scrollable resultset support"); //NOI18N
        }
    }

    private void extractWarnings(Statement stmt) {
        try {
            for (SQLWarning warning = stmt.getConnection().getWarnings(); warning != null; warning = warning.getNextWarning()) {
                dataView.addWarning(warning);
            }
            stmt.getConnection().clearWarnings();
        } catch (Throwable ex) {
            LOGGER.log(Level.FINE, "Failed to retrieve warnings", ex);
            // Exceptions will be ignored at this point
        }
        try {
            for(SQLWarning warning = stmt.getWarnings(); warning != null; warning = warning.getNextWarning()) {
                dataView.addWarning(warning);
            }
            stmt.clearWarnings();
        } catch (Throwable ex) {
            LOGGER.log(Level.FINE, "Failed to retrieve warnings", ex);
            // Exceptions will be ignored at this point
        }
    }
}
