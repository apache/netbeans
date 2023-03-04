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

package org.netbeans.modules.db.sql.execute;

import java.sql.SQLWarning;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.db.dataview.api.DataView;

/**
 * Encapsulates the result of the execution of a single SQL statement.
 *
 * @author Andrei Badea
 */
public class SQLExecutionResult {

    /**
     * The info about the executed statement.
     */
    private final StatementInfo statementInfo;
        
    /**
     * The ResultSet returned by the statement execution.
     */
    private final DataView dataView;
    
    
    public SQLExecutionResult(StatementInfo info, DataView dataView) {
        this.statementInfo = info;
        this.dataView = dataView;
    }
    
    public StatementInfo getStatementInfo() {
        return statementInfo;
    }
    
    public DataView getDataView() {
        return dataView;
    }

    public boolean hasResults() {
        return dataView.hasResultSet();
    }

    public boolean hasExceptions() {
        return dataView.hasExceptions();
    }
    
    public int getUpdateCount() {
        return dataView.getUpdateCount();
    }

    public List<Integer> getUpdateCounts() {
        return dataView.getUpdateCounts();
    }

    public List<Long> getFetchTimes() {
        return dataView.getFetchTimes();
    }
    
    public Collection<Throwable> getExceptions() {
        return dataView.getExceptions();
    }
    
    public long getExecutionTime() {
        return dataView.getExecutionTime();
    }
    
    /**
     * Retrieve logical error position.
     *
     * @param logicalOffset
     * @return -1 if no error position is availble, else it is an offset into the SQL
     */
    public int getErrorPosition() {
        return dataView.getErrorPosition();
    }
    
    /**
     * Translate a logicalOffset (an offset in the sql) into a line/column
     * pair in the complete script environment.
     * 
     * <p>Both values are zero-based</p>
     * 
     * @param logicalOffset
     * @return int array with two components, first denotes line, second column
     */
    public int[] getRawErrorLocation() {
        int errorOffset = getErrorPosition();

        if (errorOffset >= 0) {
            return getStatementInfo().translateToRawPosLineColumn(errorOffset);
        } else {
            return new int[] { 
                getStatementInfo().getStartLine(), 
                getStatementInfo().getStartColumn()};
        }
    }

    public boolean hasWarnings() {
        return dataView.hasWarnings();
    }

    public Collection<SQLWarning> getWarnings() {
        return dataView.getWarnings();
    }
    
    public String toString() {
        return "SQLExecutionResult[dataView=" + dataView + ",rowCount=" + getUpdateCount() + ",exception=" + getExceptions() + ",executionTime=" + getExecutionTime() + "]";
    }
}
