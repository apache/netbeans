/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
