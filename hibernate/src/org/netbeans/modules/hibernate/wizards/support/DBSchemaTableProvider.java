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

package org.netbeans.modules.hibernate.wizards.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.dbschema.ColumnElement;
import org.netbeans.modules.dbschema.ForeignKeyElement;
import org.netbeans.modules.dbschema.SchemaElement;
import org.netbeans.modules.dbschema.TableElement;
import org.netbeans.modules.dbschema.UniqueKeyElement;
import org.netbeans.modules.hibernate.wizards.support.Table.DisabledReason;
import org.netbeans.modules.hibernate.wizards.support.Table.NoPrimaryKeyDisabledReason;

/**
 * An implementation of a table provider backed by a dbschema.
 *
 * @author Andrei Badea, gowri
 */
public class DBSchemaTableProvider implements TableProvider {

    private final SchemaElement schemaElement;    
    private final Set<Table> tables;

    public DBSchemaTableProvider(SchemaElement schemaElement) {
        this.schemaElement = schemaElement;    

        tables = buildTables();
    }

    public Set<Table> getTables() {
        return tables;
    }
    
    /**
     * Returns true if the table is a join table. A table is considered
     * a join table regardless of whether the tables it joins are
     * included in the tables to generate.
     */
    public static boolean isJoinTable(TableElement e) {
        ForeignKeyElement[] foreignKeys = e.getForeignKeys();
        if (foreignKeys == null ||
                foreignKeys.length != 2) {
            return false;
        }
        
        int foreignKeySize = foreignKeys[0].getColumns().length +
                foreignKeys[1].getColumns().length;
        
        if (foreignKeySize < e.getColumns().length) {
            return false;
        }
        
        // issue 89576: a table which references itself is not a join table
        String tableName = e.getName().getName();
        for (int i = 0; i < 2; i++) {
            if (tableName.equals(foreignKeys[i].getReferencedTable().getName().getName())) {
                return false;
            }
        }
        
        // issue 90962: a table whose foreign keys are unique is not a join table
        if (isFkUnique(foreignKeys[0]) || isFkUnique(foreignKeys[1])) {
            return false;
        }
        
        //issue 237629 table with overlapping of fk columns isn't join table
        for(ColumnElement c:foreignKeys[0].getColumns()) {
            for(ColumnElement c2:foreignKeys[1].getColumns()) {
                if(c2!=null && c!=null && c2.equals(c)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private static boolean isFkUnique(ForeignKeyElement key) {
        UniqueKeyElement[] uk = key.getDeclaringTable().getUniqueKeys();
        if (uk == null) {
            return false;
        }
        
        ColumnElement[] columns = key.getColumns();
        for (int uin=0; uin < uk.length; uin++) {
            if (containsSameColumns(columns, uk[uin])) {
                return true;
            }
        }
        
        return false;
    }
    
    private static boolean containsSameColumns(ColumnElement[] fkColumns,
            UniqueKeyElement uk) {
        if (fkColumns.length == uk.getColumns().length) {
            for (int i = 0; i < fkColumns.length; i++) {
                if (uk.getColumn(fkColumns[i].getName())==null) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    


    private Set<Table> buildTables() {
        Map<String, DBSchemaTable> name2Table = new HashMap<String, DBSchemaTable>();
        Map<String, Set<Table>> name2Referenced = new HashMap<String, Set<Table>>();
        Map<String, Set<Table>> name2ReferencedBy = new HashMap<String, Set<Table>>();
        Map<String, Set<Table>> name2Join = new HashMap<String, Set<Table>>();

        // need to create all the tables first
        TableElement[] tableElements = schemaElement.getTables();           
        
        for (TableElement tableElement : tableElements) {        
            if (tableElement.isTable()) {                
                boolean join = isJoinTable(tableElement);                
                List<DisabledReason> disabledReasons = getDisabledReasons(tableElement);
                DisabledReason disabledReason = null;
                // only take the first disabled reason
                for (DisabledReason reason : disabledReasons) {
                    // issue 76202: join tables are not required to have a primary key
                    // so try the other disabled reasons, if any
                    if (!(join && reason instanceof NoPrimaryKeyDisabledReason)) {
                        disabledReason = reason;                        
                        break;
                    }
                }

                String tableName = tableElement.getName().getName();                
                DBSchemaTable table = new DBSchemaTable(tableName, disabledReason);                
                name2Table.put(tableName, table);
                name2Referenced.put(tableName, new HashSet<Table>());
                name2ReferencedBy.put(tableName, new HashSet<Table>());
                name2Join.put(tableName, new HashSet<Table>());
            }
        }

        // referenced, referenced by and join tables
        for (TableElement tableElement : tableElements) {
            if (!tableElement.isTable()) {
                continue;
            }

            String tableName = tableElement.getName().getName();
            Table table = name2Table.get(tableName);            
            ForeignKeyElement[] foreignKeyElements = tableElement.getForeignKeys();
            if (foreignKeyElements != null) {
                for (ForeignKeyElement foreignKeyElement : foreignKeyElements) {
                    TableElement referencedTableElement = foreignKeyElement.getReferencedTable();
                    String referencedTableName = referencedTableElement.getName().getName();
                    Table referencedTable = name2Table.get(referencedTableName);

                    name2Referenced.get(tableName).add(referencedTable);
                    name2ReferencedBy.get(referencedTableName).add(table);

                    if (table.isJoin()) {
                        name2Join.get(referencedTableName).add(table);
                    }
                }
            }
        }

        Set<Table> result = new HashSet<Table>();        
        for (DBSchemaTable table : name2Table.values()) {
            String tableName = table.getName();

            table.setReferencedTables(Collections.unmodifiableSet(name2Referenced.get(tableName)));
            table.setReferencedByTables(Collections.unmodifiableSet(name2ReferencedBy.get(tableName)));
            table.setJoinTables(Collections.unmodifiableSet(name2Join.get(tableName)));

            result.add(table);            
        }
        
        return Collections.unmodifiableSet(result);
    }

    private static List<DisabledReason> getDisabledReasons(TableElement tableElement) {
        List<DisabledReason> result = new ArrayList<DisabledReason>();

        if (hasNoPrimaryKey(tableElement)) {
            result.add(new NoPrimaryKeyDisabledReason());
        }        
        return result;
    }

    private static boolean hasNoPrimaryKey(TableElement tableElement) {
        return tableElement.getPrimaryKey() == null;
    }

    private static final class DBSchemaTable extends Table {        

        private Set<Table> referencedTables;
        private Set<Table> referencedByTables;
        private Set<Table> joinTables;        

        public DBSchemaTable(String name, DisabledReason disabledReason) {
            super(name, false, disabledReason);            
        }

        public Set<Table> getReferencedTables() {
            return referencedTables;
        }

        private void setReferencedTables(Set<Table> referencedTables) {
            this.referencedTables = referencedTables;
        }

        public Set<Table> getReferencedByTables() {
            return referencedByTables;
        }

        private void setReferencedByTables(Set<Table> referencedByTables) {
            this.referencedByTables = referencedByTables;
        }

        public Set<Table> getJoinTables() {
            return joinTables;
        }

        private void setJoinTables(Set<Table> joinTables) {
            this.joinTables = joinTables;
        }
    }
}
