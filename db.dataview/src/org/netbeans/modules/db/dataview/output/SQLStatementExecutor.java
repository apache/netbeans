/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

    protected void executeOnSucess() {}

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
            executeOnSucess(); // delegate 
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
