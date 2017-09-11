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
        
        List<EntityMember> fields = new ArrayList<EntityMember>();
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
        
        Set<String> tables = new HashSet<String>();
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
        
        List<EntityMember> fields = new ArrayList<EntityMember>();
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
        
        public boolean isLobType() {
            return lob;
        }
        
        public boolean isPrimaryKey() {
            return primaryKey;
        }
        
        public void setPrimaryKey(boolean isPk, boolean isPkField) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public boolean supportsFinder() {
            return supportsFinder;
        }
        
        public Integer getLength() {
            return length;
        }

        public Integer getPrecision(){
            return precision;
        }

        public Integer getScale(){
            return scale;
        }
        
        public boolean isNullable() {
            return nullable;
        }
        
        public String getColumnName() {
            return columnName;
        }
        
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
        
        public Set<String> getTableNames() {
            return tableNames;
        }
        
        public FileObject getRootFolder(String tableName) {
            return rootFolder;
        }
        
        public String getPackageName(String tableName) {
            return packageName;
        }
        
        public String getClassName(String tableName) {
            return EntityMember.makeClassName(tableName);
        }

        public String getCatalog() {
            return catalogName;
        }
        
        public String getSchema() {
            return schemaName;
        }

        public Set<List<String>> getUniqueConstraints(String tableName) {
            return null;
        }

        @Override
        public UpdateType getUpdateType(String tableName) {
            return UpdateType.NEW;
        }
    }
    
}
