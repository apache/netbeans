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

package org.netbeans.modules.j2ee.persistence.editor.completion.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.BadLocationException;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.dbschema.ColumnElement;
import org.netbeans.modules.dbschema.TableElement;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappings;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.ManyToMany;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.ManyToOne;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.OneToMany;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.OneToOne;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Table;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.editor.completion.AnnotationUtils;
import org.netbeans.modules.j2ee.persistence.editor.completion.CompletionContextResolver;
import org.netbeans.modules.j2ee.persistence.editor.completion.CCParser;
import org.netbeans.modules.j2ee.persistence.editor.completion.JPACompletionItem;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.editor.completion.JPACodeCompletionProvider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSource;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourceProvider;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Marek Fukala, Sergey Petrov
 */
public class DBCompletionContextResolver implements CompletionContextResolver {
    
    private DatabaseConnection dbconn;
    private DBMetaDataProvider provider;
    
    //annotations names handled somehow by this completion context resolver
    private static final String[] ANNOTATION_QUERY_TYPES = {
        "Table", //0
        "SecondaryTable", //1
        "Column", //2
        "PrimaryKeyJoinColumn", //3
        "JoinColumn", //4
        "JoinTable", //5
        "PersistenceUnit", //6
        "PersistenceContext", //7
        "ManyToMany", //8
        "Index"
    };
    
    private static final String PERSISTENCE_PKG = "javax.persistence";
    
    @Override
    public List resolve(JPACodeCompletionProvider.Context ctx) {
        
        List<JPACompletionItem> result = new ResultItemsFilterList(ctx);
        
        //parse the annotation
        CCParser.CC parsedNN = ctx.getParsedAnnotation();
        if (parsedNN == null) {
            return result;
        }
        
        CCParser.NNAttr nnattr = parsedNN.getAttributeForOffset(ctx.getCompletionOffset());
        if(nnattr == null) {
            return result;
        }
        
        String annotationName = parsedNN.getName();
        if(annotationName == null) {
            return result;
        }
        
        try {
            //get nn index from the nn list
            int index = getAnnotationIndex(annotationName);
            if(index == 6 || index == 7) {
                //we do not need database connection for PU completion
                completePersistenceUnitContext(ctx, parsedNN, nnattr, result);
            } else if(index != -1) {
                //the completion has been invoked in supported annotation and there is no db connection initialized yet
                //try to init the database connection
                dbconn = findDatabaseConnection(ctx);
                if(dbconn != null) {
                    // DatabaseConnection.getJDBCConnection() unfortunately acquires Children.MUTEX read access;
                    // it should not be called in a MDR transaction, as this is deadlock-prone
                    //assert Thread.currentThread() != JMManager.getTransactionMutex().getThread();
                    
                    Connection conn = dbconn.getJDBCConnection();
                    if(conn != null) {
                        this.provider = getDBMetadataProvider(dbconn, conn);
                    } else {
                        //Database connection not established ->
                        //put 'connect' CC item
                        result = new ArrayList<>();
                        result.add(new JPACompletionItem.NoConnectionElementItem(dbconn));
                        return result;
                    }
                } else {
                    //no database connection -> give up
                    ErrorManager.getDefault().log("No Database Connection.");
                    return result;
                }
            }
            
            //test if the initialization of DB and DBMetadataProvider has succeeded
            if(this.provider != null) {
                //and retrieve the CC items under MDR transs
                //JMIUtils utils = JMIUtils.get(ctx.getBaseDocument());
                //utils.beginTrans(false);
                //TODO, should it be done in source modification task?
                try {
                    //((JMManager) JMManager.getManager()).setSafeTrans(true);
                    switch(index) {
                        case 0:
                            completeTable(parsedNN, nnattr, result, false);//Table
                            break;
                        case 5: //JoinTable
                        case 1:
                            completeTable(parsedNN, nnattr, result, true);//SecondaryTable
                            break;
                        case 2:
                            completeColumn(ctx, parsedNN, nnattr, result);//Column
                            break;
                        case 3:
                            completePrimaryKeyJoinColumn(ctx, parsedNN, nnattr, result);
                            break;
                        case 4:
                            completeJoinColumn(ctx, parsedNN, nnattr, result); //JoinColumn
                            break;
                        case 8:
                            completeManyToMany(ctx, parsedNN, nnattr, result);
                        case 9:
                            completeIndex(ctx, parsedNN, nnattr, result);
                    }
                } finally {
                    //utils.endTrans(false);
                }
            }
            
        } catch(SQLException ex){
            
        }
        
        return result;
    }
    
    /** @return index of the annotation type qhich is going to be queried or -1 if no such annotation found. */
    private int getAnnotationIndex(String annotationName) {
        if(annotationName.startsWith(PERSISTENCE_PKG)) {
            //cut off the package
            annotationName = annotationName.substring(annotationName.lastIndexOf('.') + 1);
        }
        for(int i = 0; i < ANNOTATION_QUERY_TYPES.length; i++) {
            if(ANNOTATION_QUERY_TYPES[i].equals(annotationName)) {
                return i;
            }
        }
        return -1;
    }
    
    private DBMetaDataProvider getDBMetadataProvider(DatabaseConnection dbconn, Connection con) {
        return DBMetaDataProvider.get(con, dbconn.getDriverClass());
    }
    
    private DatabaseConnection findDatabaseConnection(JPACodeCompletionProvider.Context ctx) {
        PersistenceUnit[] pus = ctx.getPersistenceUnits();
        if(pus == null || pus.length == 0) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "There isn't any defined persistence unit for this class in the project!");
            return null;
        }
        PersistenceUnit pu = pus[0]; // XXX only using the first persistence unit
        
        // try to find a connection specified using the PU properties
        DatabaseConnection dbcon = ProviderUtil.getConnection(pu);
        if (dbcon != null) {
            return dbcon;
        }
        
        // try to find a datasource-based connection, but only for a FileObject-based context,
        // otherwise we don't have a J2eeModuleProvider to retrieve the DS's from
        String datasourceName = ProviderUtil.getDatasourceName(pu);
        if (datasourceName == null) {
            return null;
        }
        FileObject fo = ctx.getFileObject();
        if (fo == null) {
            return null;
        }
        Project project = FileOwnerQuery.getOwner(fo);
        if (project == null) {
            return null;
        }
        JPADataSource datasource = null;
        JPADataSourceProvider dsProvider = project.getLookup().lookup(JPADataSourceProvider.class);
        if (dsProvider == null){
            return null;
        }
        for (JPADataSource each : dsProvider.getDataSources()){
            if (datasourceName.equals(each.getJndiName())){
                datasource = each;
            }
        }
        if (datasource == null) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "The " + datasourceName + " was not found."); // NOI18N
            return null;
        }
        List<DatabaseConnection> dbconns = findDatabaseConnections(datasource);
        if (!dbconns.isEmpty()) {
            return dbconns.get(0);
        }
        return null;
    }
    /**
     * Finds the database connections whose database URL and user name equal
     * the database URL and the user name of the passed data source.
     *
     * @param  datasource the data source.
     *
     * @return the list of database connections; never null.
     *
     * @throws NullPointerException if the datasource parameter was null.
     */
    private static List<DatabaseConnection> findDatabaseConnections(JPADataSource datasource) {
        // copied from j2ee.common.DatasourceHelper (can't depend on that)
        if (datasource == null) {
            throw new NullPointerException("The datasource parameter cannot be null."); // NOI18N
        }
        String databaseUrl = datasource.getUrl();
        String user = datasource.getUsername();
        if (databaseUrl == null || user == null) {
            return Collections.emptyList();
        }
        List<DatabaseConnection> result = new ArrayList<>();
        for (DatabaseConnection dbconn : ConnectionManager.getDefault().getConnections()) {
            if (databaseUrl.equals(dbconn.getDatabaseURL()) && user.equals(dbconn.getUser())) {
                result.add(dbconn);
            }
        }
        if (!result.isEmpty()) {
            return Collections.unmodifiableList(result);
        } else {
            return Collections.emptyList();
        }
    }
    
    private List completeTable(CCParser.CC nn, CCParser.NNAttr nnattr, List<JPACompletionItem> results, boolean secondaryTable) throws SQLException {
        String completedMember = nnattr.getName();
        Map<String,Object> members = nn.getAttributes();
        
        if ("catalog".equals(completedMember)) { // NOI18N
            Catalog[] catalogs = provider.getCatalogs();
            for (int i = 0; i < catalogs.length; i++) {
                String catalogName = catalogs[i].getName();
                if (catalogName != null) {
                    results.add(new JPACompletionItem.CatalogElementItem(catalogName, nnattr.isValueQuoted(), nnattr.getValueOffset()));
                }
            }
        } else if ("schema".equals(completedMember)) { // NOI18N
            String catalogName = getThisOrDefaultCatalog((String)members.get("catalog")); // NOI18N
            Catalog catalog = provider.getCatalog(catalogName);
            if (catalog != null) {
                Schema[] schemas = catalog.getSchemas();
                for (int i = 0; i < schemas.length; i++) {
                    results.add(new JPACompletionItem.SchemaElementItem(schemas[i].getName(), nnattr.isValueQuoted(), nnattr.getValueOffset()));
                }
            }
        } else if ("name".equals(completedMember)) { // NOI18N
            String catalogName = getThisOrDefaultCatalog((String)members.get("catalog")); // NOI18N
            String schemaName = getThisOrDefaultSchema((String)members.get("schema")); // NOI18N
            Schema schema = DBMetaDataUtils.getSchema(provider, catalogName, schemaName);
            if (schema != null) {
                String[] tableNames = schema.getTableNames();
                for (int i = 0; i < tableNames.length; i++) {
                    results.add(new JPACompletionItem.TableElementItem(tableNames[i], nnattr.isValueQuoted(), nnattr.getValueOffset()));
                }
            }
        }
        return results;
    }
    
    private List completeIndex(JPACodeCompletionProvider.Context ctx, CCParser.CC nn, CCParser.NNAttr nnattr, List<JPACompletionItem> results) throws SQLException {
        String completedMember = nnattr.getName();
        Map<String,Object> members = nn.getAttributes();
        
        if ("columnList".equals(completedMember)) { // NOI18N
            String catalogName = getThisOrDefaultCatalog((String)members.get("catalog")); // NOI18N
            String schemaName = getThisOrDefaultSchema((String)members.get("schema")); // NOI18N
            Schema schema = DBMetaDataUtils.getSchema(provider, catalogName, schemaName);
            if (schema != null) {
                String[] tableNames = schema.getTableNames();
                //additiona;l parsing inside of columnList literal is reequred
                //TODO: made more general parsing??
                //need new value offset and values
                int cmplOffset = ctx.getCompletionOffset();
                int testLen = cmplOffset - nnattr.getValueOffset();
                String toParse = nnattr.getValue().toString();
                //column list is simple structure with space and ',' separator
                int lastSpace = toParse.lastIndexOf(' ');
                int lastComma = toParse.lastIndexOf(',');
                int shift  =  Math.max(lastComma, lastSpace) + 1 + (nnattr.isValueQuoted() ? 1 : 0);
                //
                boolean compleTables = false;
                if(lastSpace == -1 && lastSpace ==-1) {
                    compleTables = true;//we are at the beginning
                } else if (lastComma > -1 && (lastComma == (testLen-1) || toParse.substring(lastComma+1).trim().length()==0)) {
                    compleTables = true;
                }
                if(compleTables) {
                    for (int i = 0; i < tableNames.length; i++) {
                        results.add(new JPACompletionItem.IndexElementItem(tableNames[i], false, nnattr.getValueOffset(), shift));
                    }
                } else {
                    for (int i = 0; i < JPACompletionItem.IndexElementItem.PARAMS.length; i++) {
                        results.add(new JPACompletionItem.IndexElementItem(JPACompletionItem.IndexElementItem.PARAMS[i], false, nnattr.getValueOffset(), shift));
                    }                    
                }
            }
        }
        return results;
    }
   
    private List completePrimaryKeyJoinColumn(JPACodeCompletionProvider.Context ctx, CCParser.CC nn, CCParser.NNAttr nnattr, List<JPACompletionItem> results) throws SQLException {
        String completedMember = nnattr.getName();
        
        if ("name".equals(completedMember)) { // NOI18N
            //XXX should I take into account the @SecondaryTable here???
            Entity entity = PersistenceUtils.getEntity(((TypeElement) ctx.getJavaClass()).getQualifiedName().toString(), ctx.getEntityMappings());
            if(entity != null) {
                org.netbeans.modules.j2ee.persistence.api.metadata.orm.Table table = entity.getTable();
                if(table != null) {
                    String tableName = table.getName();
                    if(tableName != null) {
                        String catalogName = getThisOrDefaultCatalog(table.getCatalog());
                        String schemaName = getThisOrDefaultSchema(table.getSchema());
                        //if(DEBUG) System.out.println("Columns for " + catalogName + "." + schemaName + "." + tableName);
                        TableElement tableElement = DBMetaDataUtils.getTable(provider, catalogName, schemaName, tableName);
                        if(tableElement != null) {
                            ColumnElement[] columnElements = tableElement.getColumns();
                            for (int i = 0; i < columnElements.length; i++) {
                                results.add(new JPACompletionItem.ColumnElementItem(columnElements[i].getName().getName(), tableName, true, -1));
                            }
                        }
                    }
                }
            }
        }
        
        return results;
    }
    
    
    private List completeColumn(JPACodeCompletionProvider.Context ctx, CCParser.CC nn, CCParser.NNAttr nnattr, List<JPACompletionItem> results) throws SQLException {
        String completedMember = nnattr.getName();
        Map<String,Object> members = nn.getAttributes();
        
        if ("table".equals(completedMember)) { // NOI18N
            Set<String> mappingTables = getMappingEntityTableNames(((TypeElement) ctx.getJavaClass()).getQualifiedName().toString());
            for (Iterator i = mappingTables.iterator(); i.hasNext();) {
                String tableName = (String)i.next();
                results.add(new JPACompletionItem.TableElementItem(tableName, nnattr.isValueQuoted(), nnattr.getValueOffset()));
            }
        }
        if ("name".equals(completedMember)) { // NOI18N
            String catalogName = null;
            String schemaName = null;
            String tableName = (String)members.get("table"); // NOI18N
            
            if (tableName == null) {
                //no table attribute provided
                //get the columns from @Table and @SecondaryTable(s) annotations
                if(ctx == null){
                    System.out.println("CTX");
                }
                if(ctx.getJavaClass()==null){
                    System.out.println("JC");
                }
                Entity entity = PersistenceUtils.getEntity(((TypeElement) ctx.getJavaClass()).getQualifiedName().toString(), ctx.getEntityMappings());
                if(entity != null) {
                    Table table = entity.getTable();
                    if(table != null) {
                        //the entity has table defined
                        tableName = table.getName();
                        if(tableName != null) {
                            catalogName = getThisOrDefaultCatalog(table.getCatalog());
                            schemaName = getThisOrDefaultSchema(table.getSchema());
                            TableElement tableElement = DBMetaDataUtils.getTable(provider, catalogName, schemaName, tableName);
                            if(tableElement != null) {
                                ColumnElement[] columnElements = tableElement.getColumns();
                                for (int i = 0; i < columnElements.length; i++) {
                                    results.add(new JPACompletionItem.ColumnElementItem(columnElements[i].getName().getName(), tableName, nnattr.isValueQuoted(), nnattr.getValueOffset()));
                                }
                            }
                        }
                    }
//TODO: isn't implemented in model yet, need investigation
//                    SecondaryTable[] stables = entity.getSecondaryTable();
//                    if(stables != null) {
//                        for(int idx = 0; idx < stables.length; idx++) {
//                            String secTableName = stables[idx].getName();
//                            TableElement tableElement = DBMetaDataUtils.getTable(provider, catalogName, schemaName, secTableName);
//                            if(tableElement != null) {
//                                ColumnElement[] columnElements = tableElement.getColumns();
//                                for (int i = 0; i < columnElements.length; i++) {
//                                    results.add(new JPACompletionItem.ColumnElementItem(columnElements[i].getName().getName(), tableName, nnattr.isValueQuoted(), nnattr.getValueOffset()));
//                                }
//                            }
//                        }
//                    }
                }
            } else {
                //table attribute of @Column annotation provided
                catalogName = getThisOrDefaultCatalog(catalogName);
                schemaName = getThisOrDefaultSchema(schemaName);
                TableElement tableElement = DBMetaDataUtils.getTable(provider, catalogName, schemaName, tableName);
                if(tableElement != null) {
                    ColumnElement[] columnElements = tableElement.getColumns();
                    for (int i = 0; i < columnElements.length; i++) {
                        results.add(new JPACompletionItem.ColumnElementItem(columnElements[i].getName().getName(), tableName, nnattr.isValueQuoted(), nnattr.getValueOffset()));
                    }
                }
            }
        }
        
        return results;
    }
    
    private List completeJoinColumn(JPACodeCompletionProvider.Context ctx, CCParser.CC nn, CCParser.NNAttr nnattr, List<JPACompletionItem> results) throws SQLException {
        String completedMember = nnattr.getName();
        Map<String,Object> members = nn.getAttributes();
        
        if ("name".equals(completedMember)) { // NOI18N
            //I need to get @Table annotation to get know which table is primary for this class
            //XXX should I take into account the @SecondaryTable here???
            Entity entity = PersistenceUtils.getEntity(((TypeElement) ctx.getJavaClass()).getQualifiedName().toString(), ctx.getEntityMappings());
            //TODO it is bad - since we should allow the CC to complete even the class is not "Entity"
            if(entity != null && entity.getAttributes() != null) {
                String propertyName = ctx.getCompletedMemberName();
                String resolvedClassName = ctx.getCompletedMemberClassName();
                TypeElement type = null;
                
                if(type == null) {
                    //show an error message
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(JPACodeCompletionProvider.class, "MSG_CannotFindClass", new Object[]{resolvedClassName, propertyName}));
                    return Collections.EMPTY_LIST;
                }
                
                String resolvedType = type.getQualifiedName().toString();
                
                if(DEBUG) {
                    System.out.println("completion called on property " + propertyName + " of " + resolvedType + " type.");
                }
                
                EntityMappings em = ctx.getEntityMappings();
                
                //set is in the declared class
                ManyToOne[] m2o = entity.getAttributes().getManyToOne();
                OneToOne[] o2o = entity.getAttributes().getOneToOne();
                
                //set is in the declaring class
                OneToMany[] o2m = entity.getAttributes().getOneToMany();
                //set is in both refered and declaring class
                ManyToMany[] m2m = entity.getAttributes().getManyToMany();
                
                ManyToOne m2onn = null;
                if(m2o != null) {
                    for(int i = 0; i < m2o.length; i++) {
                        if(m2o[i].getName().equals(propertyName)) {
                            m2onn = m2o[i];
                            break;
                        }
                    }
                }
                OneToOne o2onn = null;
                if(o2o != null) {
                    for(int i = 0; i < o2o.length; i++) {
                        if(o2o[i].getName().equals(propertyName)) {
                            o2onn = o2o[i];
                            break;
                        }
                    }
                }
                
                OneToMany o2mnn = null;
                if(o2m != null) {
                    for(int i = 0; i < o2m.length; i++) {
                        if(o2m[i].getName().equals(propertyName)) {
                            o2mnn = o2m[i];
                            break;
                        }
                    }
                }
                
                ManyToMany m2mnn = null;
                if(m2m != null) {
                    for(int i = 0; i < m2m.length; i++) {
                        if(m2m[i].getName().equals(propertyName)) {
                            m2mnn = m2m[i];
                            break;
                        }
                    }
                }
                
                
                if(m2onn != null || o2onn != null) {
                    if(DEBUG) {
                        System.out.println("found OneToOne or ManyToOne annotation on the completed field.");
                    }
                    //OneToOne or ManyToOne
                    //find the entity according to the type of the referred object
                    Entity ent = PersistenceUtils.getEntity(resolvedType, ctx.getEntityMappings());
                    
                    //also check whether the entity is explicitly determined by "targetEntity"
                    //attribute of the OneToOne or ManyToOne annotations
                    if(m2onn != null) {
                        String targetEntity = m2onn.getTargetEntity();
                        if(targetEntity != null) {
                            ent = PersistenceUtils.getEntity(targetEntity, em);
                            if(DEBUG) {
                                System.out.println("entity " + ent.getName() +  " is specified in ManyToOne element.");
                            }
                        }
                    }
                    if(o2onn != null) {
                        String targetEntity = o2onn.getTargetEntity();
                        if(targetEntity != null) {
                            ent = PersistenceUtils.getEntity(targetEntity, em);
                            if(DEBUG) {
                                System.out.println("entity " + ent.getName() +  " is specified in OneToOne element.");
                            }
                        }
                    }
                    
                    if(ent != null) {
                        Table table = ent.getTable();
                        if(table != null) {
                            String catalogName = getThisOrDefaultCatalog(null); //XXX need to provide correct data
                            String schemaName = getThisOrDefaultSchema(null);//XXX need to provide correct data
                            String tableName = table.getName();
                            if(tableName != null) {
                                TableElement tableElement = DBMetaDataUtils.getTable(provider, catalogName, schemaName, tableName);
                                if(tableElement != null) {
                                    ColumnElement[] columnElements = tableElement.getColumns();
                                    for (int i = 0; i < columnElements.length; i++) {
                                        results.add(new JPACompletionItem.ColumnElementItem(columnElements[i].getName().getName(), tableName, nnattr.isValueQuoted(), nnattr.getValueOffset()));
                                    }
                                    if(DEBUG) {
                                        System.out.println("added " +columnElements.length + " CC items.");
                                    }
                                }
                            }
                        } else {
                            if(DEBUG) {
                                System.out.println("the found entity has not defined table!?! (probably a  bug in values defaultter).");
                            }
                        }
                    }
                    
                }
                
                //the @JoinTable doesn't make sense for @OneToMany
                
                if(m2mnn != null) {
                    if(DEBUG) {
                        System.out.println("found ManyToMany annotation on the completed field.");
                    }
                    //the column names in this case needs to be gotten from the surrounding @JoinTable annotation
                    //using of the model doesn't make much sense here because once we complete @JoinColumn inside
                    //a @JoinTable the @JoinTable must be present in the source
                    
                    //gettting the annotations structure from own simple parser
                    
                    if(DEBUG) {
                        System.out.println(nn);
                    }
                    
                    CCParser.CC tblNN = null;
                    if(nn != null && nn.getName().equals("JoinTable")) { //NOI18N
                        Map<String, Object> attrs = nn.getAttributes();
                        Object val = attrs.get("table"); //NOI18N
                        if(val instanceof CCParser.CC) {
                            CCParser.CC tableNN = (CCParser.CC)val;
                            if(tableNN.getName().equals("Table")) {//NOI18N
                                tblNN = tableNN;
                            }
                        }
                    }
                    
                    if(tblNN != null) {
                        String catalogName = getThisOrDefaultCatalog((String)tblNN.getAttributes().get("catalog")); //XXX need to provide correct data
                        String schemaName = getThisOrDefaultSchema((String)tblNN.getAttributes().get("schema"));//XXX need to provide correct data
                        String tableName = (String)tblNN.getAttributes().get("name");
                        if(tableName != null) {
                            TableElement tableElement = DBMetaDataUtils.getTable(provider, catalogName, schemaName, tableName);
                            if(tableElement != null) {
                                ColumnElement[] columnElements = tableElement.getColumns();
                                for (int i = 0; i < columnElements.length; i++) {
                                    results.add(new JPACompletionItem.ColumnElementItem(columnElements[i].getName().getName(), tableName, nnattr.isValueQuoted(), nnattr.getValueOffset()));
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return results;
    }
    
    private List completePersistenceUnitContext(JPACodeCompletionProvider.Context ctx, CCParser.CC nn, CCParser.NNAttr nnattr, List<JPACompletionItem> results) throws SQLException {
        String completedMember = nnattr.getName();
        Map<String,Object> members = nn.getAttributes();
        
        if ("unitName".equals(completedMember)) { // NOI18N
            PersistenceUnit[] pus = ctx.getPersistenceUnits();
            for (PersistenceUnit pu : pus) {
                results.add(new JPACompletionItem.PersistenceUnitElementItem(pu.getName(), nnattr.isValueQuoted(), nnattr.getValueOffset()));
            }
        }
        
        return results;
    }
    
    private List completeManyToMany(JPACodeCompletionProvider.Context ctx, CCParser.CC nn, CCParser.NNAttr nnattr, List<JPACompletionItem> results) throws SQLException {
        String completedMember = nnattr.getName();
        Map<String,Object> members = nn.getAttributes();
        
        if ("mappedBy".equals(completedMember)) { // NOI18N
            Element type = null;//ctx.getSyntaxSupport().getTypeFromName(resolvedClassName, false, null, false);
            if(type instanceof TypeElement) {
                TypeElement cdef = (TypeElement)type;
                Entity entity = PersistenceUtils.getEntity(cdef.getQualifiedName().toString(), ctx.getEntityMappings());
                if(entity != null) {
                    //the class is entity => get all its properties
                    List<ExecutableElement> resultMethods = new LinkedList<>();
                    List<VariableElement> resultFields = new LinkedList<>();
                    TypeElement typeElement = cdef;
                    while (typeElement != null) {
                        if (org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil.isAnnotatedWith(cdef, "javax.persistence.Entity") || org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil.isAnnotatedWith(cdef, "javax.persistence.MappedSuperclass")) { // NOI18N
                            resultMethods.addAll(ElementFilter.methodsIn(typeElement.getEnclosedElements()));
                            resultFields.addAll(ElementFilter.fieldsIn(typeElement.getEnclosedElements()));
                        }
                        typeElement = org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil.getSuperclassTypeElement(typeElement);
                    }                    
                    element:
                    for(VariableElement f : resultFields) {
                        for(javax.lang.model.element.Modifier mod:f.getModifiers()){
                            if(javax.lang.model.element.Modifier.TRANSIENT == mod) {
                                continue element;
                            }
                        }
                        if(JpaControllerUtil.isAnnotatedWith(f,"javax.persistence.Transient")) { //NOI18N
                            continue;
                        }
                        String name = f.getSimpleName().toString();
                        String capName = Character.toUpperCase(name.charAt(0))+name.substring(1);
                        for(ExecutableElement meth:resultMethods){
                            if(("get"+capName).equals(meth.getSimpleName().toString()) || ("is"+capName).equals(meth.getSimpleName().toString())){
                                results.add(new JPACompletionItem.EntityPropertyElementItem(name, nnattr.isValueQuoted(), nnattr.getValueOffset()));
                                break;
                            }
                           
                        }
                    }
                    for(ExecutableElement f : resultMethods) {
                            if(JpaControllerUtil.guessField(f)!=null){
                                results.add(new JPACompletionItem.EntityPropertyElementItem(JpaControllerUtil.getPropNameFromMethod(f.getSimpleName().toString()), nnattr.isValueQuoted(), nnattr.getValueOffset()));
                            }
                    }            
                }
            }
            
            
        }
        
        return results;
    }
    
    
    /**
     * Returns the tables to which this class is mapped.
     */
    private Set<String> getMappingEntityTableNames(TypeElement clazz) {
        Set result = new TreeSet();
        List<? extends AnnotationMirror> annotations = clazz.getAnnotationMirrors();
        
        for (Iterator<? extends AnnotationMirror> i = annotations.iterator(); i.hasNext();) {
            AnnotationMirror annotation = i.next();
            TypeElement annTypeElement = (TypeElement) annotation.getAnnotationType().asElement();
            String annotationTypeName = annTypeElement.getQualifiedName().toString();
            
            if ("javax.persistence.Table".equals(annotationTypeName)) { // NOI18N
                String tableName = AnnotationUtils.getStringMemberValue(annotation, "name"); // NOI18N
                if (tableName != null) {
                    result.add(tableName);
                }
            } else if ("javax.persistence.SecondaryTable".equals(annotationTypeName)) { // NOI18N
                String tableName = AnnotationUtils.getStringMemberValue(annotation, "name"); // NOI18N
                if (tableName != null) {
                    result.add(tableName);
                }
            } else if ("javax.persistence.SecondaryTables".equals(annotationTypeName)) { // NOI18N
                List<AnnotationMirror> secondaryTableNNs = AnnotationUtils.getAnnotationsMemberValue(annotation, "value"); // NOI18N
                for (Iterator<AnnotationMirror> j = secondaryTableNNs.iterator(); j.hasNext();) {
                    AnnotationMirror secondaryTableNN = (AnnotationMirror)j.next();
                    String tableName = AnnotationUtils.getStringMemberValue(secondaryTableNN, "name"); // NOI18N
                }
            }
        }
        
        return result;
    }
    
    private String getThisOrDefaultCatalog(String catalogName) throws SQLException {
        assert provider != null;
        if (catalogName != null && !catalogName.isEmpty()) {
            return catalogName;
        } else {
            return provider.getDefaultCatalog();
        }
    }
    
    private String getThisOrDefaultSchema(String schemaName) {
        assert dbconn != null;
        if (schemaName != null && !schemaName.isEmpty()) {
            return schemaName;
        } else {
            // XXX this may be wrong, the persistence provider would use
            // the default connection's schema as gived by the database server
            return dbconn.getSchema();
        }
    }

    private Set getMappingEntityTableNames(String javaClass) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    private static final class ResultItemsFilterList extends ArrayList {
        private JPACodeCompletionProvider.Context ctx;
        public ResultItemsFilterList(JPACodeCompletionProvider.Context ctx) {
            super();
            this.ctx = ctx;
        }
        
        @Override
        public boolean add(Object o) {
            if(!(o instanceof JPACompletionItem)) {
                return false;
            }
            
            JPACompletionItem ri = (JPACompletionItem)o;
            //check if the pretext corresponds to the result item text
            try {
                String preText = ctx.getBaseDocument().getText(ri.getSubstituteOffset(), ctx.getCompletionOffset() - ri.getSubstituteOffset());
                if(!ri.canFilter() || ri.getItemText().startsWith(preText)) {
                    return super.add(ri);
                }
            }catch(BadLocationException ble) {
                //ignore
            }
            return false;
        }
    }
    
    private static final boolean DEBUG = Boolean.getBoolean("debug." + DBCompletionContextResolver.class.getName());
}
