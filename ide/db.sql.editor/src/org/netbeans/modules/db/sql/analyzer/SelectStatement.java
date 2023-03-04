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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;

/**
 *
 * @author Andrei Badea
 */
public class SelectStatement extends SQLStatement {

    private final List<List<String>> selectValues;

    SelectStatement(int startOffset, int endOffset, List<List<String>> selectValues, TablesClause fromClause, List<SelectStatement> subqueries, SortedMap<Integer, Context> offset2Context) {
        super(startOffset, endOffset, offset2Context, fromClause, subqueries);
        this.kind = SQLStatementKind.SELECT;
        this.selectValues = selectValues;
    }

    public TablesClause getTablesInEffect(int offset) {
        List<SelectStatement> statementPath = new ArrayList<SelectStatement>();
        fillStatementPath(offset, statementPath);
        if (statementPath.size() == 0) {
            return null;
        }
        if (statementPath.size() == 1) {
            return statementPath.get(0).getTablesClause();
        }
        Collections.reverse(statementPath);
        Set<QualIdent> unaliasedTableNames = new TreeSet<QualIdent>();
        Map<String, QualIdent> aliasedTableNames = new HashMap<String, QualIdent>();
        for (SelectStatement statement : statementPath) {
            TablesClause statementFromClause = statement.getTablesClause();
            if (statementFromClause != null) {
                unaliasedTableNames.addAll(statementFromClause.getUnaliasedTableNames());
                for (Entry<String, QualIdent> entry : statementFromClause.getAliasedTableNames().entrySet()) {
                    String alias = entry.getKey();
                    QualIdent tableName = entry.getValue();
                    if (!aliasedTableNames.containsKey(alias)) {
                        aliasedTableNames.put(alias, tableName);
                    }
                }
            }
        }
        return new TablesClause(Collections.unmodifiableSet(unaliasedTableNames), Collections.unmodifiableMap(aliasedTableNames));
    }

    public List<List<String>> getSelectValues() {
        return selectValues;
    }

    private void fillStatementPath(int offset, List<SelectStatement> path) {
        if (offset >= startOffset && offset <= endOffset) {
            path.add(this);
            for (SelectStatement subquery : getSubqueries()) {
                subquery.fillStatementPath(offset, path);
            }
        }
    }
}
