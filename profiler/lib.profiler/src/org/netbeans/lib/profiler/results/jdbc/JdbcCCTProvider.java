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
package org.netbeans.lib.profiler.results.jdbc;

import org.netbeans.lib.profiler.results.CCTProvider;
import org.netbeans.lib.profiler.results.cpu.FlatProfileProvider;
import org.netbeans.lib.profiler.results.memory.RuntimeMemoryCCTNode;

/**
 *
 * @author Tomas Hurka
 */
public interface JdbcCCTProvider extends CCTProvider, FlatProfileProvider {
    public static final int SQL_STATEMENT_UNKNOWN = -1;
    public static final int SQL_STATEMENT = 0;
    public static final int SQL_PREPARED_STATEMENT = 1;
    public static final int SQL_CALLABLE_STATEMENT = 2;

    public static final int SQL_COMMAND_BATCH = -2;
    public static final int SQL_COMMAND_OTHER = -1;
    public static final int SQL_COMMAND_ALTER = 0;
    public static final int SQL_COMMAND_CREATE = 1;
    public static final int SQL_COMMAND_DELETE = 2;
    public static final int SQL_COMMAND_DESCRIBE = 3;
    public static final int SQL_COMMAND_INSERT = 4;
    public static final int SQL_COMMAND_SELECT = 5;
    public static final int SQL_COMMAND_SET = 6;
    public static final int SQL_COMMAND_UPDATE = 7;
        
    public static final String STATEMENT_INTERFACE = java.sql.Statement.class.getName();
    public static final String PREPARED_STATEMENT_INTERFACE = java.sql.PreparedStatement.class.getName();
    public static final String CALLABLE_STATEMENT_INTERFACE = java.sql.CallableStatement.class.getName();
    public static final String CONNECTION_INTERFACE = java.sql.Connection.class.getName();
    public static final String DRIVER_INTERFACE = java.sql.Driver.class.getName();

    public static interface Listener extends CCTProvider.Listener {
    }
    
    RuntimeMemoryCCTNode[] getStacksForSelects();
    int getCommandType(int selectId);
    int getSQLCommand(int selectId);
    String[] getTables(int selectId);
    void updateInternals();
    void beginTrans(boolean mutable);
    void endTrans();
}
