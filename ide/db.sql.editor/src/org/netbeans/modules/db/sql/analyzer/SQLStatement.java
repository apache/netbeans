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

package org.netbeans.modules.db.sql.analyzer;

import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;

/**
 *
 * @author Jiri Rechtacek, Jiri Skrivanek
 */
public class SQLStatement {

    SQLStatementKind kind;
    int startOffset, endOffset;
    SortedMap<Integer, Context> offset2Context;
    TablesClause tablesClause;
    private List<SelectStatement> subqueries;

    SQLStatement(int startOffset, int endOffset, SortedMap<Integer, Context> offset2Context) {
        this(startOffset, endOffset, offset2Context, null, null);
    }

    SQLStatement(int startOffset, int endOffset, SortedMap<Integer, Context> offset2Context, TablesClause tablesClause) {
        this(startOffset, endOffset, offset2Context, tablesClause, null);
    }

    SQLStatement(int startOffset, int endOffset, SortedMap<Integer, Context> offset2Context, TablesClause tablesClause, List<SelectStatement> subqueries) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.offset2Context = offset2Context;
        this.tablesClause = tablesClause;
        this.subqueries = subqueries;
    }

    public SQLStatementKind getKind() {
        return kind;
    }

    /** Returns context at given offset. If offset falls into subquery, it
     * tries to find context in this subquery and returns it if not null.
     * @param offset offset within statement
     * @return context at given offset
     */
    public Context getContextAtOffset(int offset) {
        if (offset < startOffset || offset > endOffset) {
            return null;
        }
        Context result = null;
        // scan subqueries
        if (subqueries != null) {
            for (SQLStatement subquery : subqueries) {
                result = subquery.getContextAtOffset(offset);
                if (result != null) {
                    return result;
                }
            }
        }
        // scan this statement
        for (Entry<Integer, Context> entry : offset2Context.entrySet()) {
            if (offset >= entry.getKey()) {
                result = entry.getValue();
            } else {
                return result;
            }
        }
        return result;
    }

    public List<SelectStatement> getSubqueries() {
        return subqueries;
    }

    TablesClause getTablesClause() {
        return tablesClause;
    }

    public enum Context {

        START(0),
        // DELETE
        DELETE(200),
        // DROP TABLE
        DROP(300),
        DROP_TABLE(310),
        // INSERT
        INSERT(400),
        INSERT_INTO(410),
        COLUMNS(420),
        VALUES(430),
        // SELECT
        SELECT(500),
        FROM(510),
        JOIN_CONDITION(520),
        WHERE(530),
        GROUP(540),
        GROUP_BY(550),
        HAVING(560),
        ORDER(570),
        ORDER_BY(580),
        // UPDATE
        UPDATE(600),
        SET(610),
        // CREATE
        CREATE(700),
        CREATE_PROCEDURE(710),
        CREATE_FUNCTION(720),
        BEGIN(730),
        END(740),
        CREATE_TABLE(750),
        CREATE_TEMPORARY_TABLE(760),
        CREATE_DATABASE(770),
        CREATE_SCHEMA(780),
        CREATE_VIEW(790),
        CREATE_VIEW_AS(800);

        private final int order;

        private Context(int order) {
            this.order = order;
        }

        public boolean isAfter(Context context) {
            return this.order >= context.order;
        }
    }
}
