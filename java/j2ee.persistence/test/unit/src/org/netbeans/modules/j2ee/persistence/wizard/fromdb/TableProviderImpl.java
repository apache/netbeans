/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table.DisabledReason;

/**
 *
 * @author Andrei Badea
 */
public class TableProviderImpl implements TableProvider {

    private final Set<Table> tables;
    private final Map<String, Table> name2Table = new HashMap<>();

    public TableProviderImpl(String schema, String catalog, Map<String, Set<String>> tablesAndRefs) {
        this(schema, catalog, tablesAndRefs, emptyDisabledReasonMap());
    }

    public TableProviderImpl(String catalog, String schema, Map<String, Set<String>> tablesAndRefs, Map<String, DisabledReason> disabledReasons) {
        Map<String, TableImpl> name2Table = new HashMap<>();
        Map<String, Set<Table>> name2Referenced = new HashMap<>();
        Map<String, Set<Table>> name2ReferencedBy = new HashMap<>();
        Map<String, Set<Table>> name2Join = new HashMap<>();

        // need to create all the tables first
        for (String tableName : tablesAndRefs.keySet()) {
            DisabledReason disabledReason = disabledReasons.get(tableName);
            boolean join = tableName.contains("_");
            TableImpl table = new TableImpl(catalog, schema, tableName, join, disabledReason);

            name2Table.put(tableName, table);
            name2Referenced.put(tableName, new HashSet<Table>());
            name2ReferencedBy.put(tableName, new HashSet<Table>());
            name2Join.put(tableName, new HashSet<Table>());
        }

        // referenced, referenced by and join tables
        for (String tableName : tablesAndRefs.keySet()) {
            Table table = name2Table.get(tableName);

            for (String referencedTableName : tablesAndRefs.get(tableName)) {
                Table referencedTable = name2Table.get(referencedTableName);

                name2Referenced.get(tableName).add(referencedTable);
                name2ReferencedBy.get(referencedTableName).add(table);

                if (table.isJoin()) {
                    name2Join.get(referencedTableName).add(table);
                }
            }
        }

        Set<Table> tmpTables = new HashSet<>();
        for (TableImpl table : name2Table.values()) {
            String tableName = table.getName();

            table.setReferencedTables(Collections.unmodifiableSet(name2Referenced.get(tableName)));
            table.setReferencedByTables(Collections.unmodifiableSet(name2ReferencedBy.get(tableName)));
            table.setJoinTables(Collections.unmodifiableSet(name2Join.get(tableName)));

            tmpTables.add(table);
            this.name2Table.put(table.getName(), table);
        }
        tables = Collections.unmodifiableSet(tmpTables);
    }

    @Override
    public Set<Table> getTables() {
        return tables;
    }

    Table getTableByName(String name) {
        return name2Table.get(name);
    }

    private static Map<String, DisabledReason> emptyDisabledReasonMap() {
        return Collections.emptyMap();
    }

    private static final class TableImpl extends Table {

        private Set<Table> referencedTables;
        private Set<Table> referencedByTables;
        private Set<Table> joinTables;

        public TableImpl(String catalog, String schema, String name, boolean join, DisabledReason disabledReason) {
            super(catalog, schema, name, join, disabledReason);
        }

        private void setReferencedTables(Set<Table> referencedTables) {
            this.referencedTables = referencedTables;
        }

        @Override
        public Set<Table> getReferencedTables() {
            return referencedTables;
        }

        private void setReferencedByTables(Set<Table> referencedByTables) {
            this.referencedByTables = referencedByTables;
        }

        @Override
        public Set<Table> getReferencedByTables() {
            return referencedByTables;
        }

        private void setJoinTables(Set<Table> joinTables) {
            this.joinTables = joinTables;
        }

        @Override
        public Set<Table> getJoinTables() {
            return joinTables;
        }

        @Override
        public Set<List<String>> getUniqueConstraints() {
            return null;
        }
    }
}
