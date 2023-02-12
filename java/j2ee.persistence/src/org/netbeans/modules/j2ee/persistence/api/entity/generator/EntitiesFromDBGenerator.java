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

package org.netbeans.modules.j2ee.persistence.api.entity.generator;


import org.netbeans.modules.dbschema.DBException;
import org.netbeans.modules.j2ee.persistence.api.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.dbschema.DBIdentifier;
import org.netbeans.modules.dbschema.SchemaElement;
import org.netbeans.modules.dbschema.jdbcimpl.ConnectionProvider;
import org.netbeans.modules.dbschema.jdbcimpl.SchemaElementImpl;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation.CollectionType;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation.FetchType;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.JavaPersistenceGenerator;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.DBSchemaTableProvider;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.PersistenceGenerator;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.RelatedCMPHelper;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.SelectedTables;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableClosure;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * A class for generating entity classes from database tables.
 *
 * @author Erno Mononen
 */
public final class EntitiesFromDBGenerator {
    
    private final List<String> tableNames;
    private final boolean generateNamedQueries;
    private final String packageName;
    private final SourceGroup location;
    private final DatabaseConnection connection;
    private final Project project;
    private final PersistenceUnit persistenceUnit;
    private final PersistenceGenerator generator;
    
    // Global mapping options
    private boolean fullyQualifiedTableNames;
    private CollectionType collectionType;
    private boolean regenTableAttrs;
    private FetchType fetchType;
    
    private SchemaElement schemaElement;
    
    /**
     * Creates a new instance of EntitiesFromDBGenerator.
     *
     * @param tableNames the names of the tables for which entities are generated. Must not be null.
     * @param generateNamedQueries specifies whether named queries should be generated.
     * @param fullyQualifiedTableNames specifies whether fully qualified database table names should be used.
     *        Attribute catalog and schema are added to the Table annotation if true
     * @param regenTablesAttrs specified whether attributes used for regenerating tables from entity classes
     *        should be included. If true, unique containtraints are generated on @Table annotation and attributes
     *        nullable (if false), length (for String type), precision and scale(for decimal type) are added to
     *        the Column annotation
     * @param fetchType specifies the fetch type for the associations. Can be <code>FetchType.DEFAULT</code>,
     *        <code>FetchType.EAGER</code> or <code>FetchType.LAZY</code>. Default to <code>FetchType.DEFAULT</code>,
     *        meaning no fetch attribute is added to the relationship annotation
     * @param collectionType specifies the collection type for the OneToMany and ManyToMany fields.
     *        Can be <code>CollectionType.COLLECTION</code>, <code>CollectionType.LIST</code> or
     *        <code>CollectionType.SET</code>. Default to <code>CollectionType.COLLECTION</code>.
     * @param packageName the name of the package for the generated entities. Must not be null.
     * @param location the location. Must not be null.
     * @param connection the database connection for the specified tables. Must not be null.
     * @param project the project to which entities are generated.
     * @param persistenceUnit the persistenceUnit to which generated entities should be added
     * as managed classes. May be null, in which case it is up to the client to add
     * the generated entities to an appropriate persistence unit (if any).
     *
     */
    public EntitiesFromDBGenerator(List<String> tableNames, boolean generateNamedQueries,
            boolean fullyQualifiedTableNames, boolean regenTablesAttrs,
            FetchType fetchType, CollectionType collectionType,
            String packageName, SourceGroup location, DatabaseConnection connection,
            Project project, PersistenceUnit persistenceUnit) {
        this(tableNames, generateNamedQueries, fullyQualifiedTableNames, regenTablesAttrs, fetchType, collectionType, packageName, location, connection, project, persistenceUnit, new JavaPersistenceGenerator(persistenceUnit));
    }

    /**
     * Creates a new instance of EntitiesFromDBGenerator.
     *
     * @param tableNames the names of the tables for which entities are generated. Must not be null.
     * @param generateNamedQueries specifies whether named queries should be generated.
     * @param fullyQualifiedTableNames specifies whether fully qualified database table names should be used.
     *        Attribute catalog and schema are added to the Table annotation if true
     * @param regenTablesAttrs specified whether attributes used for regenerating tables from entity classes
     *        should be included. If true, unique containtraints are generated on @Table annotation and attributes
     *        nullable (if false), length (for String type), precision and scale(for decimal type) are added to
     *        the Column annotation
     * @param fetchType specifies the fetch type for the associations. Can be <code>FetchType.DEFAULT</code>,
     *        <code>FetchType.EAGER</code> or <code>FetchType.LAZY</code>. Default to <code>FetchType.DEFAULT</code>,
     *        meaning no fetch attribute is added to the relationship annotation
     * @param collectionType specifies the collection type for the OneToMany and ManyToMany fields.
     *        Can be <code>CollectionType.COLLECTION</code>, <code>CollectionType.LIST</code> or
     *        <code>CollectionType.SET</code>. Default to <code>CollectionType.COLLECTION</code>.
     * @param packageName the name of the package for the generated entities. Must not be null.
     * @param location the location. Must not be null.
     * @param connection the database connection for the specified tables. Must not be null.
     * @param project the project to which entities are generated.
     * @param persistenceUnit the persistenceUnit to which generated entities should be added
     * as managed classes. May be null, in which case it is up to the client to add
     * the generated entities to an appropriate persistence unit (if any).
     * @param persistenceGenerator persistence generator
     *
     */
    public EntitiesFromDBGenerator(List<String> tableNames, boolean generateNamedQueries,
            boolean fullyQualifiedTableNames, boolean regenTablesAttrs,
            FetchType fetchType, CollectionType collectionType,
            String packageName, SourceGroup location, DatabaseConnection connection,
            Project project, PersistenceUnit persistenceUnit, PersistenceGenerator persistenceGenerator) {
        Parameters.notNull("project", project); //NOI18N
        Parameters.notNull("tableNames", tableNames); //NOI18N
        Parameters.notNull("packageName", packageName); //NOI18N
        Parameters.notNull("location", location); //NOI18N
        Parameters.notNull("connection", connection); //NOI18N
        
        this.tableNames = tableNames;
        this.generateNamedQueries = generateNamedQueries;
        this.fullyQualifiedTableNames = fullyQualifiedTableNames;
        this.regenTableAttrs = regenTablesAttrs;
        this.fetchType = fetchType;
        this.collectionType = collectionType;
        this.packageName = packageName;
        this.location = location;
        this.connection = connection;
        this.project = project;
        this.persistenceUnit = persistenceUnit;
        this.generator = persistenceGenerator;
    }
    
    /**
     * Creates a new instance of EntitiesFromDBGenerator.
     *
     * @param tableNames the names of the tables for which entities are generated. Must not be null.
     * @param generateNamedQueries specifies whether named queries should be generated.
     * @param packageName the name of the package for the generated entities. Must not be null.
     * @param location the location. Must not be null.
     * @param connection the database connection for the specified tables. Must not be null.
     * @param project the project to which entities are generated.
     * @param persistenceUnit the persistenceUnit to which generated entities should be added
     * as managed classes. May be null, in which case it is up to the client to add
     * the generated entities to an appropriate persistence unit (if any).
     *
     */
    public EntitiesFromDBGenerator(List<String> tableNames, boolean generateNamedQueries,
            String packageName, SourceGroup location, DatabaseConnection connection,
            Project project, PersistenceUnit persistenceUnit) {
        
        this(tableNames, generateNamedQueries, false, false, FetchType.DEFAULT, CollectionType.COLLECTION,
                packageName, location, connection, project, persistenceUnit);
    }
    
    /**
     * Performs the generation of entity classes.
     *
     * @param progressContributor the progress contributor for the generation process.
     *
     * @return a set of <code>FileObject</code>s representing the generated entity
     * classes.
     * @throws SQLException in case an error was encountered when connecting to the db.
     * @throws IOException in case the writing of the generated entities fails.
     */
    public Set<FileObject> generate(ProgressContributor progressContributor) throws SQLException, IOException{
        
        RelatedCMPHelper helper = new RelatedCMPHelper(project, PersistenceLocation.getLocation(project, location.getRootFolder()), generator);
        helper.setLocation(location);
        helper.setPackageName(packageName);
        
        try{
            
            TableClosure tableClosure = getTableClosure();
            SelectedTables selectedTables = new SelectedTables(generator, tableClosure, location, packageName);
            
            helper.setTableClosure(tableClosure);
            helper.setTableSource(getSchemaElement(), null);
            helper.setSelectedTables(selectedTables);
            helper.setGenerateFinderMethods(generateNamedQueries);
            helper.setFullyQualifiedTableNames(fullyQualifiedTableNames);
            helper.setRegenTablesAttrs(regenTableAttrs);
            helper.setFetchType(fetchType);
            helper.setCollectionType(collectionType);
            
            helper.buildBeans();
            
        } catch (DBException ex){
            throw new IOException(ex);
        }
        
        
        generator.generateBeans(null, helper, null, progressContributor);
        
        Set<FileObject> result = generator.createdObjects();
        return result;
    }
    
    
    private TableClosure getTableClosure() throws SQLException, DBException{
        TableProvider tableProvider = new DBSchemaTableProvider(getSchemaElement(), generator, project);
        
        Set<Table> selectedTables = new HashSet<>();
        for (Table each : tableProvider.getTables()){
            if (tableNames.contains(each.getName())){
                selectedTables.add(each);
            }
        }
        
        TableClosure tableClosure = new TableClosure(tableProvider);
        tableClosure.addTables(selectedTables);
        return tableClosure;
    }
    
    /**
     * Get the schema element representing the selected tables.
     */
    private SchemaElement getSchemaElement() throws SQLException, DBException{
        
        if (this.schemaElement != null){
            return this.schemaElement;
        }
        
        ConnectionProvider connectionProvider = getConnectionProvider();
        SchemaElementImpl impl = new SchemaElementImpl(connectionProvider);
        schemaElement = new SchemaElement(impl);
        schemaElement.setName(DBIdentifier.create("schema")); // NOI18N
        impl.initTables(connectionProvider, new LinkedList<>(tableNames), new LinkedList<>(), true);
        
        return schemaElement;
    }
    
    /**
     * Gets the connection provider for our <code>connection</code>.
     */
    private ConnectionProvider getConnectionProvider() throws SQLException{
        ConnectionProvider connectionProvider = new ConnectionProvider(connection.getJDBCConnection(), connection.getDriverClass());
        connectionProvider.setSchema(connection.getSchema());
        return connectionProvider;
    }
    
}
