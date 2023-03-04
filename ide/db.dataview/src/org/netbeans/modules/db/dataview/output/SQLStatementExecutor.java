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

import java.sql.Connection;
import java.sql.SQLException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.db.dataview.meta.DBConnectionFactory;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * @author Ahimanikya Satapathy
 */
abstract class SQLStatementExecutor implements Runnable, Cancellable {

    protected final DataView dataView;
    protected Connection conn = null;
    protected boolean error = false;
    protected volatile Throwable ex;
    protected String errorMsg = ""; // NOI18N
    protected boolean lastCommitState;
    private final String title;
    private final String titleMsg;
    private final boolean runInTransaction;
    private volatile RequestProcessor.Task task;
    private long startTime;


    /**
     * @param parent
     * @param title
     * @param msg
     * @param runInTransaction 
     */
    public SQLStatementExecutor(DataView parent, String title, String msg, boolean runInTransaction) {
        this.title = title;
        this.titleMsg = msg;
        this.dataView = parent;
        this.runInTransaction = runInTransaction;
    }

    public void setTask(RequestProcessor.Task task) {
        this.task = task;
    }

    @Override
    public void run() {
        assert task != null;
        try {
            startTime = System.currentTimeMillis();
            ProgressHandle handle = ProgressHandleFactory.createHandle(title, this);
            handle.setDisplayName(titleMsg);
            handle.start();
            try {
                handle.switchToIndeterminate();
                dataView.setInfoStatusText(""); // NOI18N
                errorMsg = "";  // NOI18N
                dataView.disableButtons();

                conn = DBConnectionFactory.getInstance().getConnection(dataView.getDatabaseConnection());
                if (conn == null) {
                    String msg;
                    Throwable connEx = DBConnectionFactory.getInstance().getLastException();
                    if (connEx != null) {
                        msg = connEx.getMessage();
                    } else {
                        msg = NbBundle.getMessage(SQLStatementExecutor.class, "MSG_connection_failure", dataView.getDatabaseConnection());
                    }
                    NotifyDescriptor nd = new NotifyDescriptor.Message(msg);
                    DialogDisplayer.getDefault().notify(nd);
                    return;
                }
                if(runInTransaction) {
                lastCommitState = setAutocommit(conn, false);
                } else {
                    lastCommitState = setAutocommit(conn, true);
                }
                execute(); // delegate 
            } finally {
                handle.finish();
            }
        } catch (Exception e) {
            this.ex = e;
        } finally {
            if (ex != null) {
                errorMsg += ex.getMessage();
                error = true;
            }
            finished(); // delegate 
            setAutocommit(conn, lastCommitState);
        }
    }

    @Override
    public boolean cancel() {
        return task.cancel();
    }

    protected abstract void finished();

    protected abstract void execute() throws SQLException, DBException;

    protected void executeOnSuccess() {}

    protected void reinstateToolbar() {
        // reinstate the toolbar
        synchronized (dataView) {
            dataView.resetToolbar(false);
        }
    }

    protected void commitOrRollback(String cmdName) {
        if (!error && commit(conn)) {
            long executionTime = System.currentTimeMillis() - startTime;
            String execTimeStr = SQLExecutionHelper.millisecondsToSeconds(executionTime);
            String infoMsg = cmdName + " " + NbBundle.getMessage(SQLStatementExecutor.class, "MSG_execution_success", execTimeStr);
            dataView.setInfoStatusText(infoMsg);
            executeOnSuccess(); // delegate 
        } else {
            rollback(conn);
            reinstateToolbar();

            String msg = cmdName + " " + NbBundle.getMessage(SQLStatementExecutor.class, "MSG_failed");
            if (ex == null) {
                errorMsg = msg + " " + errorMsg;
            } else {
                errorMsg = msg;
            }

            ex = new DBException(errorMsg, ex);
            dataView.setErrorStatusText(conn, null, ex);

            NotifyDescriptor nd = new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }

    private boolean setAutocommit(Connection conn, boolean newState) {
        try {
            if (conn != null) {
                boolean lastState = conn.getAutoCommit();
                conn.setAutoCommit(newState);
                return lastState;
            }
        } catch (SQLException e) {
        }
        return newState;
    }

    private boolean commit(Connection conn) {
        if(! runInTransaction) {
            return true;
        }
        try {
            if (conn != null && !conn.getAutoCommit()) {
                conn.commit();
            }
        } catch (SQLException sqlEx) {
            String msg = NbBundle.getMessage(SQLStatementExecutor.class, "MSG_failure_to_commit");
            dataView.setErrorStatusText(conn, null, msg, sqlEx);
            ex = sqlEx;
            return false;
        }
        return true;
    }

    private void rollback(Connection conn) {
        if(! runInTransaction) {
            String msg = NbBundle.getMessage(SQLStatementExecutor.class, "MSG_failure_rollback");
            dataView.setErrorStatusText(conn, null, msg, null);
        }
        try {
            if (conn != null && !conn.getAutoCommit()) {
                conn.rollback();
            }
        } catch (SQLException e) {
            String msg = NbBundle.getMessage(SQLStatementExecutor.class, "MSG_failure_rollback");
            dataView.setErrorStatusText(conn, null, msg, e);
        }
    }
}
