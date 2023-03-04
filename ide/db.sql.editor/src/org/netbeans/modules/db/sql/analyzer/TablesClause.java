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

import java.util.Map;
import java.util.Set;

/**
 * Holds table or view names with possible aliases which can be defined in FROM or UPDATE
 * clause.
 *
 * @author Andrei Badea
 */
public class TablesClause {

    private final Set<QualIdent> unaliasedTableNames;
    private final Map<String, QualIdent> aliasedTableNames;

    public TablesClause(Set<QualIdent> unaliasedTableNames, Map<String, QualIdent> aliasedTableNames) {
        this.unaliasedTableNames = unaliasedTableNames;
        this.aliasedTableNames = aliasedTableNames;
    }

    /**
     * @return set of unaliased table or view names
     */
    public Set<QualIdent> getUnaliasedTableNames() {
        return unaliasedTableNames;
    }

    /**
     * @return map of aliased table or view names
     */
    public Map<String, QualIdent> getAliasedTableNames() {
        return aliasedTableNames;
    }

    /**
     * @return unaliased table or view name
     */
    public QualIdent getTableNameByAlias(String alias) {
        return aliasedTableNames.get(alias);
    }
}
