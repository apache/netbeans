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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.dbschema.ColumnElement;
import org.netbeans.modules.dbschema.ForeignKeyElement;
import org.netbeans.modules.dbschema.SchemaElement;
import org.netbeans.modules.dbschema.TableElement;
import org.netbeans.modules.dbschema.UniqueKeyElement;
import org.netbeans.modules.j2ee.persistence.entitygenerator.DbSchemaEjbGenerator;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table.DisabledReason;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table.ExistingDisabledReason;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table.NoPrimaryKeyDisabledReason;
import org.openide.filesystems.FileObject;

/**
 * An implementation of a table provider backed by a dbschema.
 *
 * @author Andrei Badea
 */
public class DBSchemaTableProvider implements TableProvider {

    private final SchemaElement schemaElement;
    private final PersistenceGenerator persistenceGen;
    private final Set<Table> tables;
    private Set<String> tablesReferecedByOtherTables;
    private Project project;

    public DBSchemaTableProvider(SchemaElement schemaElement, PersistenceGenerator persistenceGen) {
        this(schemaElement, persistenceGen, null);

    }

    public DBSchemaTableProvider(SchemaElement schemaElement, PersistenceGenerator persistenceGen, Project project) {
        this.schemaElement = schemaElement;
        this.project = project;
        this.persistenceGen = persistenceGen;

        tablesReferecedByOtherTables = DbSchemaEjbGenerator.getTablesReferecedByOtherTables(schemaElement);
        tables = buildTables();
        
    }

    @Override
    public Set<Table> getTables() {
        return tables;
    }

    private Set<Table> buildTables() {
        Map<String, DBSchemaTable> name2Table = new HashMap<String, DBSchemaTable>();
        Map<String, Set<Table>> name2Referenced = new HashMap<String, Set<Table>>();
        Map<String, Set<Table>> name2ReferencedBy = new HashMap<String, Set<Table>>();
        Map<String, Set<Table>> name2Join = new HashMap<String, Set<Table>>();

        // need to create all the tables first
        TableElement[] tableElements = schemaElement.getTables();
        //classpath is used for verification
        ClassPath source = null;
        if(project != null){
            Sources sources=ProjectUtils.getSources(project);
            SourceGroup groups[]=sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            SourceGroup firstGroup=groups[0];
            FileObject fo=firstGroup.getRootFolder();
            source=ClassPath.getClassPath(fo, ClassPath.SOURCE);
        }
        for (TableElement tableElement : tableElements) {
            boolean join = DbSchemaEjbGenerator.isJoinTable(tableElement, tablesReferecedByOtherTables);

            List<DisabledReason> disabledReasons = getDisabledReasons(tableElement, persistenceGen, source);
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

            String catalogName = tableElement.getDeclaringSchema().getCatalog().getName();
            String schemaName = tableElement.getDeclaringSchema().getSchema().getName();
            String tableName = tableElement.getName().getName();
            DBSchemaTable table = new DBSchemaTable(catalogName, schemaName, tableName, join, disabledReason, persistenceGen, tableElement.isTable());
            
            // Set the unique constraints columns
            table.setUniqueConstraints(getUniqueConstraints(tableElement));

            name2Table.put(tableName, table);
            name2Referenced.put(tableName, new HashSet<Table>());
            name2ReferencedBy.put(tableName, new HashSet<Table>());
            name2Join.put(tableName, new HashSet<Table>());
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
    
    private Set<List<String>> getUniqueConstraints(TableElement tableElement) {
        Set<List<String>> uniqueConstraintsCols = new HashSet<List<String>>();
        UniqueKeyElement[] uks = tableElement.getUniqueKeys();
        for (int ukIx = 0; ukIx < uks.length; ukIx++) {
            if (!uks[ukIx].isPrimaryKey()) {
                ColumnElement[] colElms = uks[ukIx].getColumns();
                if (colElms == null || colElms.length == 0) {
                    // bad one
                    continue;
                }
                List<String> cols = new ArrayList<String>();
                for (int cIx = 0; cIx < colElms.length; cIx++) {
                    cols.add( colElms[cIx].getName().getName());
                }
                uniqueConstraintsCols.add(cols);
            }
        }
        return uniqueConstraintsCols;
    }

    private static List<DisabledReason> getDisabledReasons(TableElement tableElement, PersistenceGenerator persistenceGen, ClassPath source) {
        List<DisabledReason> result = new ArrayList<DisabledReason>();

        if (tableElement.isTable() && hasNoPrimaryKey(tableElement)) {
            result.add(new NoPrimaryKeyDisabledReason());
        }

        String fqClassName = persistenceGen.getFQClassName(tableElement.getName().getName());
        if (fqClassName != null) {
            if(source == null || source.findResource(fqClassName.replace('.', '/')+".java")!=null) {
                result.add(new ExistingDisabledReason(fqClassName));
            }
            else {
                result.add(new Table.ExistingNotInSourceDisabledReason(fqClassName));
            }
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
        
        // A set of unique constraints columns
        private Set<List<String>> uniqueConstraints;
        
        public DBSchemaTable(String catalog, String schema, String name, boolean join, DisabledReason disabledReason, PersistenceGenerator persistenceGen) {
            super(catalog, schema, name, join, disabledReason);
        }
        
        public DBSchemaTable(String catalog, String schema, String name, boolean join, DisabledReason disabledReason, PersistenceGenerator persistenceGen, boolean isTable) {
            super(catalog, schema, name, join, disabledReason, isTable);
        }

        @Override
        public Set<Table> getReferencedTables() {
            return referencedTables;
        }

        private void setReferencedTables(Set<Table> referencedTables) {
            this.referencedTables = referencedTables;
        }

        @Override
        public Set<Table> getReferencedByTables() {
            return referencedByTables;
        }

        private void setReferencedByTables(Set<Table> referencedByTables) {
            this.referencedByTables = referencedByTables;
        }

        @Override
        public Set<Table> getJoinTables() {
            return joinTables;
        }

        private void setJoinTables(Set<Table> joinTables) {
            this.joinTables = joinTables;
        }
        
        @Override
        public Set<List<String>> getUniqueConstraints() {
            return this.uniqueConstraints;
        }
        
        public void setUniqueConstraints(Set<List<String>> constraints) {
            this.uniqueConstraints = constraints;
        }
    }
}
