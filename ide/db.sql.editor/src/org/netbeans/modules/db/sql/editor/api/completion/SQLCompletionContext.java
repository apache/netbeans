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

import org.netbeans.api.db.explorer.DatabaseConnection;

/**
 *
 * @author Andrei Badea
 */
public class SQLCompletionContext {

    private final DatabaseConnection dbconn;
    private final CharSequence statement;
    private final int offset;

    public static SQLCompletionContext empty() {
        return new SQLCompletionContext(null, null, 0);
    }

    private SQLCompletionContext(DatabaseConnection dbconn, CharSequence statement, int offset) {
        this.dbconn = dbconn;
        this.statement = statement;
        this.offset = offset;
    }

    public SQLCompletionContext setDatabaseConnection(DatabaseConnection dbconn) {
        return new SQLCompletionContext(dbconn, this.statement, this.offset);
    }

    public SQLCompletionContext setStatement(CharSequence statement) {
        return new SQLCompletionContext(this.dbconn, statement, this.offset);
    }

    public SQLCompletionContext setOffset(int offset) {
        return new SQLCompletionContext(this.dbconn, this.statement, offset);
    }

    public DatabaseConnection getDatabaseConnection() {
        return dbconn;
    }

    public CharSequence getStatement() {
        return statement;
    }

    public int getOffset() {
        return offset;
    }
}
