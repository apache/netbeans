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

import java.util.*;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.entitygenerator.DbSchemaEjbGenerator;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityClass;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation;
import org.netbeans.modules.j2ee.persistence.entitygenerator.GeneratedTables;
import org.openide.filesystems.*;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.dbschema.SchemaElement;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation.CollectionType;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation.FetchType;

/**
 * This class provides a simple collector for information necessary to support
 * the CMP set wizard. 
 *
 * @author Chris Webster, Andrei Badea
 */
public class RelatedCMPHelper {

    private final Project project;
    private final FileObject configFilesFolder;
    private final PersistenceGenerator persistenceGen;
    private final DBSchemaFileList dbschemaFileList;
    
    private SchemaElement schemaElement;
    private DatabaseConnection dbconn;
    private FileObject dbschemaFile;
    private String datasourceName;

    private TableClosure tableClosure;
    private SelectedTables selectedTables;
    
    private SourceGroup location;
    private String packageName;
    
    private boolean generateMappedSuperclasses;
    private boolean generateFinderMethods;
    private boolean generateJAXBAnnotations;
    private boolean generateValidationConstraints;

    private boolean useColumnNamesInRelationships = true;
    private boolean generateUnresolvedRelationships = false;
    private boolean useDefaults = false;


    private DbSchemaEjbGenerator generator;
    
    private TableSource tableSource;
    
    private PersistenceUnit persistenceUnit;

    private boolean createPU = false;

    public boolean isCreatePU() {
        return createPU;
    }

    public void setCreatePU(boolean createPU) {
        this.createPU = createPU;
    }
    
    // Global mapping options added in NB 6.5
    private boolean fullyQualifiedTableNames = false;
    private FetchType fetchType = FetchType.DEFAULT;
    private boolean regenTablesAttrs = false;
    private CollectionType collectionType = CollectionType.COLLECTION;
    
    public RelatedCMPHelper(Project project, FileObject configFilesFolder, PersistenceGenerator persistenceGen) {
        this.project = project;
        this.configFilesFolder = configFilesFolder;
        this.persistenceGen = persistenceGen;
        
        tableSource = TableSource.get(project);
        dbschemaFileList = new DBSchemaFileList(project, configFilesFolder);
    }
    
    public Project getProject() {
        return project;
    }
    
    FileObject getConfigFilesFolder() {
        return configFilesFolder;
    }
    
    PersistenceGenerator getPersistenceGenerator() {
        return persistenceGen;
    }
    
    public DBSchemaFileList getDBSchemaFileList() {
        return dbschemaFileList;
    }
    
    public void setTableClosure(TableClosure tableClosure) {
        assert tableClosure != null;
        this.tableClosure = tableClosure;
    }
    
    public TableClosure getTableClosure() {
        return tableClosure;
    }
    
    public void setSelectedTables(SelectedTables selectedTables) {
        assert selectedTables != null;
        this.selectedTables = selectedTables;
    }

//    public PersistenceUnit getPersistenceUnit() {
//        return persistenceUnit;
//    }
//
//    public void setPersistenceUnit(PersistenceUnit persistenceUnit) {
//        this.persistenceUnit = persistenceUnit;
//    }
//
    /**
     * Sets the source of the tables when the source is a database connection
     * (possibly retrieved from a data source).
     *
     * @param  schemaElement the SchemaElement instance containing the database tables.
     * @param  dbconn the database connection which was used to retrieve <code>schemaElement</code>.
     * @param  dataSourceName the JNDI name of the {@link org.netbeans.modules.j2ee.deployment.common.api.Datasource data source}
     *         which was used to retrieve <code>dbconn</code> or null if the connection 
     *         was not retrieved from a data source.
     */
    public void setTableSource(SchemaElement schemaElement, DatabaseConnection dbconn, String datasourceName) {
        this.schemaElement = schemaElement;
        this.dbconn = dbconn;
        this.dbschemaFile = null;
        this.datasourceName = datasourceName;
        
        updateTableSource();
    }
    
    /**
     * Sets the source of the tables when the source is a dbschema file.
     *
     * @param  schemaElement the SchemaElement instance containing the database tables.
     * @param  dbschemaFile the dbschema file which was used to retrieve <code>schemaElement</code>.
     */
    public void setTableSource(SchemaElement schemaElement, FileObject dbschemaFile) {
        this.schemaElement = schemaElement;
        this.dbconn = null;
        this.dbschemaFile = dbschemaFile;
        this.datasourceName = null;
        
        updateTableSource();
    }
    
    public TableSource getTableSource() {
        return tableSource;
    }
    
    private void updateTableSource() {
        if (dbconn != null) {
            if (datasourceName != null) {
                tableSource = new TableSource(datasourceName, TableSource.Type.DATA_SOURCE);
            } else {
                tableSource = new TableSource(dbconn.getName(), TableSource.Type.CONNECTION);
            }
        } else if (dbschemaFile != null) {
            tableSource = new TableSource(FileUtil.toFile(dbschemaFile).getAbsolutePath(), TableSource.Type.SCHEMA_FILE);
        } else {
            tableSource = null;
        }
    }
    
    public SchemaElement getSchemaElement() {
        return schemaElement;
    }
    
    public DatabaseConnection getDatabaseConnection(){
        return dbconn;
    }
    
    public FileObject getDBSchemaFile() {
        return dbschemaFile;
    }
    
    /**
     * Returns the package for bean and module generation.
     */
    public String getPackageName() {
        return packageName;
    }
    
    /**
     * Sets the package for bean and module generation.
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public SourceGroup getLocation() {
        return location;
    }
    
    public void setLocation(SourceGroup location) {
        this.location = location;
    }
    
    public boolean isGenerateMappedSuperclasses() {
        return generateMappedSuperclasses;
    }
    
    public void setGenerateMappedSuperclasses(boolean generateMappedSuperclasses) {
        this.generateMappedSuperclasses = generateMappedSuperclasses;
    }
    
    public boolean isUseColumnNamesInRelationships() {
        return useColumnNamesInRelationships;
    }

    public void setUseColumnNamesInRelationships(boolean useColumnNamesInRelationships) {
        this.useColumnNamesInRelationships = useColumnNamesInRelationships;
    }

    public boolean isGenerateFinderMethods() {
        return this.generateFinderMethods;
    }
    
    public void setGenerateFinderMethods(boolean generateFinderMethods) {
        this.generateFinderMethods = generateFinderMethods;
    }

    public boolean isGenerateJAXBAnnotations() {
        return this.generateJAXBAnnotations;
    }

    public void setGenerateJAXBAnnotations(boolean generateJAXBAnnotations) {
        this.generateJAXBAnnotations = generateJAXBAnnotations;
    }

    public boolean isGenerateValidationConstraints() {
        return generateValidationConstraints;
    }

    public void setGenerateValidationConstraints(boolean generateValidationConstraints) {
        this.generateValidationConstraints = generateValidationConstraints;
    }

    public boolean isFullyQualifiedTableNames() {
        return fullyQualifiedTableNames;
    }

    public void setFullyQualifiedTableNames(boolean fullyQualifiedNames) {
        this.fullyQualifiedTableNames = fullyQualifiedNames;
    }

    public FetchType getFetchType() {
        return fetchType;
    }

    public void setFetchType(FetchType fetchType) {
        this.fetchType = fetchType;
    }

    public boolean isRegenTablesAttrs() {
        return regenTablesAttrs;
    }

    public void setRegenTablesAttrs(boolean regenSchemaAttrs) {
        this.regenTablesAttrs = regenSchemaAttrs;
    }
    
    public CollectionType getCollectionType() {
        return collectionType;
    }
    
    public void setCollectionType(CollectionType type) {
        collectionType = type;
    }
    
    /**
     * Public because used in J2EE functional tests.
     */
    public void buildBeans() {
        TableSource.put(project, tableSource);
        
        GenerateTablesImpl genTables = new GenerateTablesImpl();
        FileObject rootFolder = getLocation().getRootFolder();
        String pkgName = getPackageName();

        for (Table table : selectedTables.getTables()) {
            String pkg = pkgName;
            UpdateType ut = selectedTables.getUpdateType(table);
            if( ut == UpdateType.UPDATE){
                String fqn = persistenceGen.getFQClassName(table.getName());
                if(fqn != null){
                    int ind = fqn.lastIndexOf(".");
                    if(ind>-1){
                        pkg = fqn.substring(0, ind);
                    } else {
                        pkg = "";
                    }
                } else {
                    assert false:"Entity for " + table.getName() + " isn't resolved";
                }
            }
            genTables.addTable(table.getCatalog(), table.getSchema(), table.getName(), rootFolder, pkg, 
                    selectedTables.getClassName(table), ut, table.getUniqueConstraints());
        }

        // add the (possibly related) disabled tables, so that the relationships are created correctly
        // XXX what if this adds related tables that the user didn't want, such as join tables?
/*
        for (Table table : tableClosure.getAvailableTables()) {
            if (table.getDisabledReason() instanceof Table.ExistingDisabledReason) {
                Table.ExistingDisabledReason exDisReason = (Table.ExistingDisabledReason)table.getDisabledReason();
                String fqClassName = exDisReason.getFQClassName();
                SourceGroup sourceGroup = Util.getClassSourceGroup(getProject(), fqClassName); // NOI18N
                if (sourceGroup != null) {
                    genTables.addTable(table.getCatalog(), table.getSchema(), table.getName(), sourceGroup.getRootFolder(), 
                            JavaIdentifiers.getPackageName(fqClassName), JavaIdentifiers.unqualify(fqClassName), selectedTables.getUpdateType(table).toString(),
                            table.getUniqueConstraints());
                }
            }
        }
*/
        generator = new DbSchemaEjbGenerator(genTables, schemaElement, collectionType, useColumnNamesInRelationships, useDefaults, generateUnresolvedRelationships);
    }
    
    public EntityClass[] getBeans() {
        return generator.getBeans();
    }
    
    public EntityRelation[] getRelations() {
        return generator.getRelations();
    }

    /**
     * @return the generateUnresolvedRelationships
     */
    public boolean isGenerateUnresolvedRelationships() {
        return generateUnresolvedRelationships;
    }

    /**
     * @param generateUnresolvedRelationships the generateUnresolvedRelationships to set
     */
    public void setGenerateUnresolvedRelationships(boolean generateUnresolvedRelationships) {
        this.generateUnresolvedRelationships = generateUnresolvedRelationships;
    }

    /**
     * @return the useDefaults
     */
    public boolean isUseDefaults() {
        return useDefaults;
    }

    /**
     * @param useDefaults the useDefaults to set
     */
    public void setUseDefaults(boolean useDefaults) {
        this.useDefaults = useDefaults;
    }
    
    private static final class GenerateTablesImpl implements GeneratedTables {
        
        private String catalog; // for all the tables
        private String schema; // for all the tables
        private final Set<String> tableNames = new HashSet<String>();
        private final Map<String, FileObject> rootFolders = new HashMap<String, FileObject>();
        private final Map<String, String> packageNames = new HashMap<String, String>();
        private final Map<String, String> classNames = new HashMap<String, String>();
        private final Map<String, UpdateType> updateTypes = new HashMap<String, UpdateType>();
        private final Map<String, Set<List<String>>> allUniqueConstraints = new HashMap<String, Set<List<String>>>();
        
        @Override
        public Set<String> getTableNames() {
            return Collections.unmodifiableSet(tableNames);
        }
        
        private void addTable(String catalogName, String schemaName, String tableName, 
                FileObject rootFolder, String packageName, String className, UpdateType updateType,
                Set<List<String>> uniqueConstraints) {
            tableNames.add(tableName);
            catalog = catalogName;
            schema = schemaName;
            rootFolders.put(tableName, rootFolder);
            packageNames.put(tableName, packageName);
            classNames.put(tableName, className);
            updateTypes.put(tableName, updateType);
            allUniqueConstraints.put(tableName, uniqueConstraints);
        }
        
        @Override
        public String getCatalog() {
            return catalog;
        }
         
        @Override
        public String getSchema() {
            return schema;
        }
        
        @Override
        public FileObject getRootFolder(String tableName) {
            return rootFolders.get(tableName);
        }

        @Override
        public String getPackageName(String tableName) {
            return packageNames.get(tableName);
        }
        
        @Override
        public String getClassName(String tableName) {
            return classNames.get(tableName);
        }

        @Override
        public UpdateType getUpdateType(String tableName){
            return updateTypes.get(tableName);
        }

        @Override
        public Set<List<String>> getUniqueConstraints(String tableName) {
            return this.allUniqueConstraints.get(tableName);
        }
    }
}
