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

package org.netbeans.modules.db.sql.loader;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.sql.execute.SQLExecutionLogger;
import org.netbeans.modules.db.sql.execute.SQLExecutionResult;
import org.openide.cookies.LineCookie;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;
import static org.netbeans.modules.db.sql.loader.Bundle.*;

public class SQLExecutionLoggerImpl implements SQLExecutionLogger {

    private final LineCookie lineCookie;
    private final InputOutput inputOutput;

    private boolean inputOutputSelected = false;
    private int errorCount;

    @NbBundle.Messages({
        "# {0} - the name of the executed SQL file",
        "LBL_SQLFileExecution={0} execution"})
    public SQLExecutionLoggerImpl(String displayName, LineCookie lineCookie) {
        this.lineCookie = lineCookie;

        String ioName = LBL_SQLFileExecution(displayName);
        inputOutput = IOProvider.getDefault().getIO(ioName, true);
    }

    public SQLExecutionLoggerImpl(String displayName) {
        this(displayName, null);
    }

    @Override
    public void log(SQLExecutionResult result) {
        logWarnings(result);
        if (result.hasExceptions()) {
            logException(result);
        } else {
            logSuccess(result);
        }
    }

    @Override
    @NbBundle.Messages({
        "# {0} - execution time", 
        "# {1} - total number of errors", 
        "LBL_ExecutionFinished=Execution finished after {0,number,0.###} s, {1,choice,0#no errors|1#1 error|1.0<{1,number,integer} errors} occurred."})
    public void finish(long executionTime) {
        try (OutputWriter writer = inputOutput.getOut()) {
            writer.println(""); // NOI18N
            writer.println(LBL_ExecutionFinished(
                    executionTime / 1000d,
                    errorCount));
            writer.println("");
        }
        inputOutput.select();
    }

    @Override
    @NbBundle.Messages("LBL_ExecutionCancelled=Execution canceled.")
    public void cancel() {
        try (OutputWriter writer = inputOutput.getErr()) {
            writer.println(LBL_ExecutionCancelled());
            writer.println(""); // NOI18N
        }
    }

    public void close() {
        inputOutput.closeInputOutput();
    }

    private void logWarnings(SQLExecutionResult result) {
        if (!result.getWarnings().isEmpty()) {
            try (OutputWriter writer = inputOutput.getOut()) {
                for (SQLWarning s : result.getWarnings()) {
                    writeSQLWarning(s, writer);
                }

                writer.println(""); // NOI18N
            }
        }
    }
    
    @NbBundle.Messages({
        "# {0} - execution time",
        "LBL_ExecutionFailed=Failed in {0,number,0.###} s."})
    private void logException(SQLExecutionResult result) {
        errorCount++;

        if (!inputOutputSelected) {
            inputOutputSelected = true;
            inputOutput.select();
        }

        try (OutputWriter writer = inputOutput.getErr()) {
            startLineColumn(writer, result, Bundle.LBL_ExecutionFailed(result.getExecutionTime() / 1000d));

            for(Throwable e: result.getExceptions()) {
                if (e instanceof SQLException) {
                    writeSQLException((SQLException)e, writer);
                } else {
                    writeGenericException(e, writer);
                }
            }
            
            printLineColumn(writer, result, true);
            writer.println(""); // NOI18N
        }
    }

    @NbBundle.Messages({
        "# {0} - error code", 
        "# {1} - error sql state", 
        "# {2} - error message", 
        "LBL_WarningCodeStateMessage=[Warning, Error code {0}, SQLState {1}] {2}"})
    private void writeSQLWarning(SQLWarning e, OutputWriter writer) {
        writer.println(LBL_WarningCodeStateMessage(
                e.getErrorCode(),
                e.getSQLState(),
                e.getMessage()));
    }
            
    @NbBundle.Messages({
        "# {0} - error code", 
        "# {1} - error sql state", 
        "# {2} - error message", 
        "LBL_ErrorCodeStateMessage=[Exception, Error code {0}, SQLState {1}] {2}"})
    private void writeSQLException(SQLException e, OutputWriter writer) {
        while (e != null) {
            writer.println(LBL_ErrorCodeStateMessage(
                    e.getErrorCode(),
                    e.getSQLState(),
                    e.getMessage()));

            e = e.getNextException();
        }
    }
    
    @NbBundle.Messages({
        "# {0} - error message",
        "# {1} - exception class",
        "LBL_ExceptionMessage=[Exception] {1}: {0}"})
    private void writeGenericException(Throwable e, OutputWriter writer) {
        LOG.log(Level.INFO, "Exception in SQL Execution", e);
        writer.println(LBL_ExceptionMessage(
                e.getMessage(),
                e.getClass().getName()));
    }
    private static final Logger LOG = Logger.getLogger(SQLExecutionLoggerImpl.class.getName());

    @NbBundle.Messages({
        "# {0} - fetchtime in seconds", 
        "LBL_ExecutedFetchTime=Fetching resultset took {0,number,0.###} s.",
        "# {0} - number of affected rows", 
        "LBL_ExecutedRowsAffected={0,choice,0#no rows|1#1 row|1.0<{0,number,integer} rows} affected.",
        "# {0} - execution time",
        "LBL_ExecutedSuccessfullyTime=Executed successfully in {0,number,0.###} s."})
    private void logSuccess(SQLExecutionResult result) {
        try (OutputWriter writer = inputOutput.getOut()) {
            startLineColumn(writer, result, LBL_ExecutedSuccessfullyTime(result.getExecutionTime() / 1000d));
            
            List<Integer> updateCounts = result.getUpdateCounts();
            List<Long> fetchTimes = result.getFetchTimes();
            
            for (int i = 0; i < Math.max(updateCounts.size(), fetchTimes.size()); i++) {
                Integer updateCount = updateCounts.size() > i ? updateCounts.get(i) : null;
                Long fetchTime = fetchTimes.size() > i ? fetchTimes.get(i) : null;
                if (updateCount != null && updateCount >= 0) {
                    writer.println(LBL_ExecutedRowsAffected(updateCount));
                }
                if (fetchTime != null) {
                    writer.println(LBL_ExecutedFetchTime(fetchTime / 1000d));
                }
            }
            writer.println(""); // NOI18N
        }
    }

    private void startLineColumn(OutputWriter writer, SQLExecutionResult result, String message) {
        int line = result.getStatementInfo().getStartLine();
        int col = result.getStatementInfo().getStartColumn();
        String text = String.format("[%d:%d] %s", line + 1, col + 1, message);
        Hyperlink link = new Hyperlink(line, col);
        
        try {
            writer.println(text, link);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }
    
    @NbBundle.Messages({
        "# {0} - line number", 
        "# {1} - column number", 
        "LBL_LineColumn=Line {0}, column {1}"})
    private void printLineColumn(OutputWriter writer, SQLExecutionResult result, boolean hyperlink) {
        int[] errorCoords = result.getRawErrorLocation();
        int errLine = errorCoords[0];
        int errCol = errorCoords[1];
        
        String lineColumn = "  " + LBL_LineColumn(errLine + 1, errCol + 1);

        try {
            if (hyperlink) {
                Hyperlink errLink = new Hyperlink(errLine, errCol);
                writer.println(lineColumn, errLink);
            } else {
                writer.println(lineColumn);
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }

    /**
     * Represents a hyperlinked line in an InputOutput.
     */
    private final class Hyperlink implements OutputListener {

        private final Line line;
        private final int column;

        public Hyperlink(int line, int column) {
            this.line = lineCookie.getLineSet().getCurrent(line);
            this.column = column;
        }

        @Override
        public void outputLineSelected(OutputEvent ev) {
            goToLine(false);
        }

        @Override
        public void outputLineAction(OutputEvent ev) {
            goToLine(true);
        }

        @SuppressWarnings("deprecation")
        private void goToLine(boolean focus) {
            if (!line.isDeleted()) {
                line.show(focus ? Line.SHOW_GOTO : Line.SHOW_TRY_SHOW, column);
            }
        }
    }
}
