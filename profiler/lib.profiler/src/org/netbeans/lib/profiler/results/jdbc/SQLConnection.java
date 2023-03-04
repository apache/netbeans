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

import java.util.List;

/**
 *
 * @author Tomas Hurka
 */
class SQLConnection {

    SQLStatement currentStatement;
    
    void invoke(String methodName, String methodSignature, List parameters) {
        switch (methodName) {
            case "createStatement":
                createStatement();
                break;
            case "prepareStatement":
                prepareStatement((String) parameters.get(1));
                break;
            case "prepareCall":
                prepareCall((String) parameters.get(1));
                break;
        }
    }

    void createStatement() {
        assert currentStatement == null;
        currentStatement = new SQLStatement(JdbcCCTProvider.SQL_STATEMENT);
    }

    void prepareStatement(String sql) {
         assert currentStatement == null;
       currentStatement = new SQLStatement(JdbcCCTProvider.SQL_PREPARED_STATEMENT, sql);
    }

    void prepareCall(String sql) {
        assert currentStatement == null;
        currentStatement = new SQLStatement(JdbcCCTProvider.SQL_CALLABLE_STATEMENT, sql);
    }

    SQLStatement useCurrentStatement() {
        SQLStatement st = currentStatement;
        currentStatement = null;
        return st;
    }

}
