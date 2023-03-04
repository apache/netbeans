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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.modules.dbschema.SchemaElement;
import org.netbeans.modules.dbschema.SchemaElementUtil;
import org.netbeans.modules.j2ee.persistence.entitygenerator.DbSchemaEjbGenerator;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityClass;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityMember;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation.CollectionType;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation.FetchType;
import org.netbeans.modules.j2ee.persistence.entitygenerator.GeneratedTables;
import org.netbeans.modules.j2ee.persistence.sourcetestsupport.SourceTestSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 * Tests for <code>JavaPersistenceGenerator</code>, name 
 * and paths shortened due to #122544.
 * 
 * @author Erno Mononen
 */
public class JPAGenTest extends SourceTestSupport{
    
    private JavaPersistenceGenerator generator;
    /**
     * The package name to be used.
     */
    private String packageName = "generated";
    
    public JPAGenTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.generator = new JavaPersistenceGenerator();
        getWorkDirFO().createFolder(packageName);
    }
    
    
    public void testGenerateOneEntity() throws IOException{
        EntityClass user = getUserEntity();
        
        generator.generateBeans(new EntityClass[]{user}, true, false, false, false, false,
                FetchType.DEFAULT, CollectionType.COLLECTION, getProgressContributor(), null, null);
        assertEquals(1,generator.createdObjects().size());
        
        FileObject result = generator.createdObjects().iterator().next();
        assertFile(FileUtil.toFile(result), getGoldenFile("testGenEntity/" + result.getName() + ".pass"));
    }
    
    public void testGenerateTwoUnrelated() throws IOException{
        
        EntityClass user = getUserEntity();
        
        EntityClass product = new EntityClass( null, null ,"PRODUCT", 
                getWorkDirFO(), packageName, "Product", UpdateType.NEW, false, null);
        product.usePkField(true);
        
        EntityMemberImpl description = new EntityMemberImpl();
        description.setMemberName("description");
        description.setColumnName("DESCRIPTION");
        description.setSupportsFinder(true);
        description.setNullable(false);
        description.setPrimaryKey(false);
        description.setMemberType("java.lang.String");
        description.setTableName("PRODUCT");
        
        EntityMember id = getId("PRODUCT");
        
        List<EntityMember> fields = new ArrayList<>();
        fields.add(id);
        fields.add(description);
        
        product.setFields(fields);
        
        
        generator.generateBeans(new EntityClass[]{user, product}, true, false, false,
                false, false, FetchType.DEFAULT, CollectionType.COLLECTION,
                getProgressContributor(), null, null);
        Set<FileObject> result = generator.createdObjects();
        assertEquals(2, result.size());
  
        for(FileObject each : result){
            assertFile(FileUtil.toFile(each), getGoldenFile("testGenUnrelated/" + each.getName() + ".pass"));
        }
    }
    
    public void testGenerateEntityFromSampleSchema() throws Exception{
        SchemaElement schema = getSampleSchema();
        TableProvider tableProvider = new DBSchemaTableProvider(schema, generator);
        
        Set<String> tables = new HashSet<>();
        for (Table each : tableProvider.getTables()){
            if(each.getName().equals("CUSTOMER")){
                tables.add(each.getName());
                break;
            }
        }
        GenerateTablesImpl genTables = new GenerateTablesImpl(schema.getCatalog().getName(), schema.getSchema().getName(), tables, packageName, getWorkDirFO());
        
        EntityClass[] beans = new DbSchemaEjbGenerator(genTables, schema).getBeans();
        
        generator.generateBeans(beans, true, false, false, false, false, FetchType.DEFAULT, CollectionType.COLLECTION, getProgressContributor(), null, null);
        Set<FileObject> result = generator.createdObjects();
        assertEquals(1, result.size());
        
        for(FileObject each : result){
            assertFile(FileUtil.toFile(each), getGoldenFile("testGenFromSample/" + each.getName() + ".pass"));
        }
    }

    /**
     * got null in getElements().getTypeElement(..)
     * TODO: need additional investigation
     * @throws IOException
     */
//    public void testGenerateRelatedEntitiesFromSampleSchema() throws IOException{
//        SchemaElement schema = getSampleSchema();
//
//        Set<String> relatedTables = new HashSet<String>();
//        relatedTables.add("PRODUCT");
//        relatedTables.add("PRODUCT_CODE");
//        relatedTables.add("MANUFACTURER");
//
//        GenerateTablesImpl genTables = new GenerateTablesImpl(schema.getCatalog().getName(), schema.getSchema().getName(), relatedTables, packageName, getWorkDirFO());
//
//        EntityClass[] beans = new DbSchemaEjbGenerator(genTables, schema).getBeans();
//
//        generator.generateBeans(beans, true, false, false, FetchType.DEFAULT, CollectionType.COLLECTION, getProgressContributor(), null, null);
//        Set<FileObject> result = generator.createdObjects();
//        assertEquals(3, result.size());
//
//        for(FileObject each : result){
//            assertFile(FileUtil.toFile(each), getGoldenFile("testGenRelFromSample/" + each.getName() + ".pass"));
//        }
//
//    }
    
    /**
     * Return the schema for the bundled sample database.
     */
    private SchemaElement getSampleSchema(){
        return SchemaElementUtil.forName(URLMapper.findFileObject(getClass().getResource("sampledb.dbschema")));
    }
    
    private ProgressContributor getProgressContributor(){
        return AggregateProgressFactory.createProgressContributor("myContributor");
    }
    
    private FileObject getWorkDirFO() throws IOException{
        return FileUtil.toFileObject(getWorkDir());
    }
    
    
    private EntityClass getUserEntity() throws IOException{
        EntityClass user = new EntityClass( null, null, 
                "USER", getWorkDirFO(), packageName, "User", UpdateType.NEW, false, null);
        user.usePkField(true);
        
        EntityMemberImpl name = new EntityMemberImpl();
        name.setMemberName("name");
        name.setColumnName("NAME");
        name.setSupportsFinder(true);
        name.setNullable(false);
        name.setPrimaryKey(false);
        name.setMemberType("java.lang.String");
        name.setTableName("USER");
        
        EntityMember id = getId("USER");
        
        List<EntityMember> fields = new ArrayList<>();
        fields.add(id);
        fields.add(name);
        
        user.setFields(fields);
        return user;
    }
    
    private EntityMember getId(String tableName){
        EntityMemberImpl id = new EntityMemberImpl();
        id.setMemberName("id");
        id.setColumnName(tableName + "_ID");
        id.setSupportsFinder(true);
        id.setNullable(false);
        id.setPrimaryKey(true);
        id.setMemberType("java.lang.Long");
        id.setTableName(tableName);
        return id;
    }
    
    private static class EntityMemberImpl extends EntityMember{
        
        private boolean lob;
        private boolean primaryKey;
        private boolean supportsFinder;
        private boolean nullable;
        private String columnName;
        private String tableName;
        private Integer length;
        private Integer precision;
        private Integer scale;
        
        public void setLob(boolean lob) {
            this.lob = lob;
        }
        
        public void setPrimaryKey(boolean primaryKey) {
            this.primaryKey = primaryKey;
        }
        
        public void setSupportsFinder(boolean supportsFinder) {
            this.supportsFinder = supportsFinder;
        }
        
        public void setNullable(boolean nullable) {
            this.nullable = nullable;
        }
        
        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }
        
        public void setTableName(String tableName) {
            this.tableName = tableName;
        }
        
        @Override
        public boolean isLobType() {
            return lob;
        }
        
        @Override
        public boolean isPrimaryKey() {
            return primaryKey;
        }
        
        @Override
        public void setPrimaryKey(boolean isPk, boolean isPkField) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        @Override
        public boolean supportsFinder() {
            return supportsFinder;
        }
        
        @Override
        public Integer getLength() {
            return length;
        }

        @Override
        public Integer getPrecision(){
            return precision;
        }

        @Override
        public Integer getScale(){
            return scale;
        }
        
        @Override
        public boolean isNullable() {
            return nullable;
        }
        
        @Override
        public String getColumnName() {
            return columnName;
        }
        
        @Override
        public String getTableName() {
            return tableName;
        }

        @Override
        public boolean isAutoIncrement() {
            return false;
        }
    }
    
    private static final class GenerateTablesImpl implements GeneratedTables {
        
        private String schemaName;
        private String catalogName;
        private Set<String> tableNames;
        private String packageName;
        private FileObject rootFolder;
        
        public GenerateTablesImpl(String catalog, String schema, Set<String> tableNames, String packageName,
                FileObject rootFolder) {
            this.schemaName = schema;
            this.catalogName = catalog;
            this.tableNames = tableNames;
            this.packageName = packageName;
            this.rootFolder = rootFolder;
        }
        
        @Override
        public Set<String> getTableNames() {
            return tableNames;
        }
        
        @Override
        public FileObject getRootFolder(String tableName) {
            return rootFolder;
        }
        
        @Override
        public String getPackageName(String tableName) {
            return packageName;
        }
        
        @Override
        public String getClassName(String tableName) {
            return EntityMember.makeClassName(tableName);
        }

        @Override
        public String getCatalog() {
            return catalogName;
        }
        
        @Override
        public String getSchema() {
            return schemaName;
        }

        @Override
        public Set<List<String>> getUniqueConstraints(String tableName) {
            return null;
        }

        @Override
        public UpdateType getUpdateType(String tableName) {
            return UpdateType.NEW;
        }
    }
    
}
