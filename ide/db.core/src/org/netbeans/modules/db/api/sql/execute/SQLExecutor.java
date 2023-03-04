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

package org.netbeans.modules.db.api.sql.execute;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.sql.execute.SQLExecuteHelper;
import org.netbeans.modules.db.sql.execute.SQLExecutionLogger;
import org.netbeans.modules.db.sql.execute.SQLExecutionResult;
import org.netbeans.modules.db.sql.execute.SQLExecutionResults;

/**
 * Support execution of SQL scripts without bringing up the editor
 *
 * @author David Van Couvering
 */
public class SQLExecutor {
    private static Logger LOGGER = Logger.getLogger(SQLExecutor.class.getName());

    /**
     * Execute SQL and log summary information about the results and any errors
     * are logged to the error log file. 
     *
     * This method should not be called on the AWT event thread; to do so will
     * cause an exception to be thrown.
     *
     * @param dbconn the database connection to use when executing the SQL
     * @param sql the SQL which contains one or more valid SQL statements
     *
     * @throws IllegalStateException if this is executed on the AWT event thread
     * @throws DatabaseException if the database connection is not connected
     */
    public static SQLExecutionInfo execute(DatabaseConnection dbconn, String sql)
            throws DatabaseException {
        return execute(dbconn, sql, new LogFileLogger());
    }

    /**
     * Execute SQL.
     *
     * Note this is a very basic implementation.  Subsequent extensions could
     * include getting results back, passing in a logger, etc., but trying to
     * keep things simple for now.
     *
     * This method should not be called on the AWT event thread; to do so will
     * cause an exception to be thrown.
     *
     * @param dbconn the database connection to use when executing the SQL
     * @param sql the SQL which contains one or more valid SQL statements
     * @param logger this object is notified with execution information when execution
     *    of each statement completes and when the entire execution completes or is cancelled.
     *
     * @throws IllegalStateException if this is executed on the AWT event thread
     * @throws DatabaseException if the database connection is not connected
     */
    public static SQLExecutionInfo execute(DatabaseConnection dbconn, String sql, SQLExecuteLogger logger)
            throws DatabaseException {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("You can not run this method on the event dispatching thread."); // NOI18N
        }

        if (logger == null) {
            throw new NullPointerException();
        }

        if (dbconn == null) {
            throw new IllegalArgumentException("The connection parameter cannot be null");
        }

        Connection conn = dbconn.getJDBCConnection(true);
        if (conn == null) {
            throw new DatabaseException("The connection is not open"); // NOI18N
        }


        SQLExecutionResults results = SQLExecuteHelper.execute(sql, 0, sql.length(),
                dbconn, new LoggerProxy(logger));

        return new SQLExecutionInfoImpl(results);
    }

    private static class SQLExecutionInfoImpl implements SQLExecutionInfo {
        private final boolean hasExceptions;
        private final List<Throwable> exceptions;
        private final List<StatementExecutionInfo> infos;

        SQLExecutionInfoImpl(SQLExecutionResults results) {
            hasExceptions = results.hasExceptions();

            exceptions = new ArrayList<Throwable>();
            infos = new ArrayList<StatementExecutionInfo>();

            for (SQLExecutionResult result : results.getResults()) {
                infos.add(new StatementExecutionInfoImpl(result));
                if (result.hasExceptions()) {
                    exceptions.addAll(result.getExceptions());
                }
            }

        }

        public boolean hasExceptions() {
            return hasExceptions;
        }

        public List<? extends Throwable> getExceptions() {
            return exceptions;
        }

        public List<StatementExecutionInfo> getStatementInfos() {
            return infos;
        }

    }

    private static class StatementExecutionInfoImpl extends StatementExecutionInfo {
        private SQLExecutionResult result;
        public StatementExecutionInfoImpl(SQLExecutionResult result) {
            this.result = result;
        }

        public String getSQL() {
            return result.getStatementInfo().getSQL();
        }

        public boolean hasExceptions() {
            return result.hasExceptions();
        }

        public Collection<Throwable> getExceptions() {
            return result.getExceptions();
        }

        public long getExecutionTime() {
            return result.getExecutionTime();
        }

        @Override
        public int getErrorPosition() {
            return result.getErrorPosition();
        }
    }


    private static class LoggerProxy implements SQLExecutionLogger {
        private final SQLExecuteLogger delegate;

        public LoggerProxy(SQLExecuteLogger delegate) {
            this.delegate = delegate;
        }

        public void log(SQLExecutionResult result) {
            delegate.log(new StatementExecutionInfoImpl(result));
        }

        public void finish(long executionTime) {
            delegate.finish(executionTime);
        }

        public void cancel() {
            delegate.cancel();
        }

    }

}
