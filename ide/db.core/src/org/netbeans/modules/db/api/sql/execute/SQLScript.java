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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.db.sql.execute.SQLExecuteHelper;
import org.netbeans.modules.db.sql.execute.StatementInfo;

/**
 * A class for working with SQL scripts. Currently allows parsing the script
 * into statements.
 *
 * @author Andrei Badea
 */
public final class SQLScript {

    // XXX binary search in getStatementAtOffset().

    private final List<SQLScriptStatement> statements;

    public static SQLScript create(String sql) {
        List<SQLScriptStatement> statements = new ArrayList<SQLScriptStatement>();
        for (StatementInfo statement : SQLExecuteHelper.split(sql)) {
            // For the code completion we want the whole text, including leading and trailing offset.
            String text = sql.substring(statement.getRawStartOffset(), statement.getRawEndOffset());
            statements.add(new SQLScriptStatement(text, statement.getRawStartOffset(), statement.getRawEndOffset()));
        }
        return new SQLScript(statements);
    }

    private SQLScript(List<SQLScriptStatement> statements) {
        this.statements = statements;
    }

    public SQLScriptStatement getStatementAtOffset(int offset) {
        for (SQLScriptStatement statement : statements) {
            if (offset >= statement.getStartOffset() && offset <= statement.getEndOffset()) {
                return statement;
            }
        }
        return null;
    }
}
