/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
    private final Map<String, Table> name2Table = new HashMap<String, Table>();

    public TableProviderImpl(String schema, String catalog, Map<String, Set<String>> tablesAndRefs) {
        this(schema, catalog, tablesAndRefs, emptyDisabledReasonMap());
    }

    public TableProviderImpl(String catalog, String schema, Map<String, Set<String>> tablesAndRefs, Map<String, DisabledReason> disabledReasons) {
        Map<String, TableImpl> name2Table = new HashMap<String, TableImpl>();
        Map<String, Set<Table>> name2Referenced = new HashMap<String, Set<Table>>();
        Map<String, Set<Table>> name2ReferencedBy = new HashMap<String, Set<Table>>();
        Map<String, Set<Table>> name2Join = new HashMap<String, Set<Table>>();

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

        Set<Table> tmpTables = new HashSet<Table>();
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

        public Set<Table> getReferencedTables() {
            return referencedTables;
        }

        private void setReferencedByTables(Set<Table> referencedByTables) {
            this.referencedByTables = referencedByTables;
        }

        public Set<Table> getReferencedByTables() {
            return referencedByTables;
        }

        private void setJoinTables(Set<Table> joinTables) {
            this.joinTables = joinTables;
        }

        public Set<Table> getJoinTables() {
            return joinTables;
        }

        @Override
        public Set<List<String>> getUniqueConstraints() {
            return null;
        }
    }
}
