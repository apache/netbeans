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

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle;

/**
 * This logger writes everything to the log file.
 *
 * @author David Van Couvering
 */
public class LogFileLogger implements SQLExecuteLogger {
    private static final Logger LOGGER = Logger.getLogger(LogFileLogger.class.getName());
    
    private int errorCount;

    @Override
    public void log(StatementExecutionInfo info) {
        if (info.hasExceptions()) {
            logException(info);
        }
    }

    @Override
    public void finish(long executionTime) {
        LOGGER.log(Level.INFO, (NbBundle.getMessage(LogFileLogger.class, "LBL_ExecutionFinished",
                executionTime / 1000d,
                errorCount)));
    }

    @Override
    public void cancel() {
        LOGGER.log(Level.INFO, NbBundle.getMessage(LogFileLogger.class, "LBL_ExecutionCancelled"));
    }

    private void logException(StatementExecutionInfo info) {
        errorCount++;

        for(Throwable e: info.getExceptions()) {
            if (e instanceof SQLException) {
                logSQLException((SQLException)e, info);
            } else {
                LOGGER.log(Level.INFO, NbBundle.getMessage(LogFileLogger.class, "MSG_SQLExecutionException", info.getSQL()), e);
            }
        }
    }

    private void logSQLException(SQLException e, StatementExecutionInfo info) {
        while (e != null) {
            LOGGER.log(Level.INFO, NbBundle.getMessage(LogFileLogger.class, "MSG_SQLExecutionException", info.getSQL()), e);
            e = e.getNextException();
        }
    }
}
