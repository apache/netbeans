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

package org.netbeans.modules.db.sql.editor.api.completion;

import org.netbeans.modules.db.sql.analyzer.SQLStatementKind;
import org.netbeans.modules.db.sql.editor.completion.SQLCompletionEnv;
import org.netbeans.modules.db.sql.editor.completion.SQLCompletionQuery;
import org.netbeans.modules.db.sql.analyzer.SQLStatementAnalyzer;

/**
 *
 * @author Andrei Badea
 */
public class SQLCompletion {
    public static final String UNKNOWN_TAG = "__UNKNOWN__"; // NOI8N

    private final SQLCompletionContext initContext;
    private final SQLCompletionQuery delegate;

    public static SQLCompletion create(SQLCompletionContext initContext) {
        return new SQLCompletion(initContext);
    }

    public static boolean canComplete(SQLCompletionContext context) {
        String statement = context.getStatement().toString();
        if (statement == null) {
            throw new NullPointerException("The context's charSequence property should not be null.");
        }
        SQLCompletionEnv env = SQLCompletionEnv.forStatement(statement, 0, null);
        SQLStatementKind kind = SQLStatementAnalyzer.analyzeKind(env.getTokenSequence());
        return kind == SQLStatementKind.SELECT
                || kind == SQLStatementKind.INSERT
                || kind == SQLStatementKind.DROP
                || kind == SQLStatementKind.UPDATE
                || kind == SQLStatementKind.DELETE;
    }

    private SQLCompletion(SQLCompletionContext initContext) {
        this.initContext = initContext;
        delegate = new SQLCompletionQuery(initContext.getDatabaseConnection());
    }

    public void query(SQLCompletionResultSet resultSet, SubstitutionHandler substitutionHandler) {
        SQLCompletionEnv env = SQLCompletionEnv.forStatement(initContext.getStatement().toString(), initContext.getOffset(), substitutionHandler);
        delegate.query(resultSet, env);
    }

    // If needed, and when supported by SQLCompletionQuery,
    // can add:
    // public boolean canFilter(SQLCompletionContext context);
    // public void filter(SQLCompletionResultSet resultSet, SubstitutionHandler handler)
}
