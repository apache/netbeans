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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
    private final JavaPersistenceGenerator generator; 
    
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
        this.generator = new JavaPersistenceGenerator(persistenceUnit);
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
            IOException wrapper = new IOException(ex.getMessage());
            wrapper.initCause(ex);
            throw wrapper;
        }
        
        
        generator.generateBeans(null, helper, null, progressContributor);
        
        Set<FileObject> result = generator.createdObjects();
        return result;
    }
    
    
    private TableClosure getTableClosure() throws SQLException, DBException{
        TableProvider tableProvider = new DBSchemaTableProvider(getSchemaElement(), generator, project);
        
        Set<Table> selectedTables = new HashSet<Table>();
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
        impl.initTables(connectionProvider, new LinkedList(tableNames), new LinkedList(), true);
        
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
