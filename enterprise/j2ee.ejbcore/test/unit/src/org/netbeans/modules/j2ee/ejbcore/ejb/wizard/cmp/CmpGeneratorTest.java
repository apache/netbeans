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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.cmp;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.dbschema.DBException;
import org.netbeans.modules.dbschema.SchemaElement;
import org.netbeans.modules.dbschema.SchemaElementUtil;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.DBSchemaTableProvider;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.PersistenceGenerator;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.ProgressPanel;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.RelatedCMPHelper;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.SelectedTables;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.Table;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableClosure;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableProvider;

/**
 *
 * @author Martin Adamek
 */
public class CmpGeneratorTest extends TestBase {
    
    private FileObject dbFO;
    
    public CmpGeneratorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws IOException {
        super.setUp();
        File dbF = new File(getDataDir().getAbsolutePath(), "derby-sample.dbschema");
        dbFO = FileUtil.toFileObject(dbF);
        assertNotNull(dbFO);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(CmpGeneratorTest.class);
        return suite;
    }
    
    public void testGenerationMultipleCMPs() throws IOException, SQLException, DBException {
        TestModule testModule = createEjb21Module();

        String pkgName = "testGenerationMultipleCMPs";
        String[] tableNames = new String[] {
            "CUSTOMER",
            "DISCOUNT_CODE",
            "MANUFACTURER",
            "MICRO_MARKET",
            "PRODUCT",
            "PRODUCT_CODE",
            "PURCHASE_ORDER"
        };
        
        generate(testModule, pkgName, tableNames);
        
        // check XML
        EjbJar ejbJar = DDProvider.getDefault().getDDRoot(testModule.getDeploymentDescriptor());
        Entity entity = (Entity) ejbJar.getEnterpriseBeans().findBeanByName(EnterpriseBeans.ENTITY, Entity.EJB_CLASS, pkgName + ".Manufacturer");
        assertNotNull(entity);
        assertEquals("ManufacturerEB", entity.getDefaultDisplayName());
        assertEquals("Manufacturer", entity.getEjbName());
        assertNull(entity.getHome());
        assertNull(entity.getRemote());
        assertEquals(pkgName + ".ManufacturerLocalHome", entity.getLocalHome());
        assertEquals(pkgName + ".ManufacturerLocal", entity.getLocal());
        assertEquals(pkgName + ".Manufacturer", entity.getEjbClass());
        assertEquals("Container", entity.getPersistenceType());
        assertEquals("java.lang.Integer", entity.getPrimKeyClass());
        assertFalse(entity.isReentrant());
        assertEquals("Manufacturer", entity.getAbstractSchemaName());
        assertEquals(11, entity.getCmpField().length);
        assertEquals("manufacturerId", entity.getPrimkeyField());
    }
    
    private FileObject generate(TestModule testModule, String pkgName, String[] tables) throws IOException, SQLException, DBException {
        CmpGenerator generator = new CmpGenerator(testModule.getProject());
        
        FileObject sourceRoot = testModule.getSources()[0];
        FileObject packageFileObject = sourceRoot.getFileObject(pkgName);
        if (packageFileObject != null) {
            packageFileObject.delete();
        }
        packageFileObject = sourceRoot.createFolder(pkgName);
        
        SchemaElement schemaElement = SchemaElementUtil.forName(dbFO);
        
        RelatedCMPHelper helper = new RelatedCMPHelper(testModule.getProject(), testModule.getConfigFilesFolder(), generator);
        SourceGroup location = ProjectUtils.getSources(testModule.getProject()).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)[0];
        TableClosure tableClosure = getTableClosure(Arrays.asList(tables), schemaElement, generator);
        SelectedTables selectedTables = new SelectedTables(generator, tableClosure, location, pkgName);

        helper.setPackageName(pkgName);
        helper.setTableSource(schemaElement, dbFO);
        helper.setLocation(location);
        helper.setTableClosure(tableClosure);
        helper.setSelectedTables(selectedTables);
        helper.buildBeans();
        
        ProgressContributor progressContributor = AggregateProgressFactory.createProgressContributor("CMP Generator");
        generator.generateBeans(new ProgressPanel(), helper, dbFO, progressContributor);
        progressContributor.finish();
        
        return packageFileObject;
    }
    
    private static TableClosure getTableClosure(List<String> tableNames, SchemaElement schemaElement, PersistenceGenerator generator) throws SQLException, DBException {
        TableProvider tableProvider = new DBSchemaTableProvider(schemaElement, generator);
        
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
    
}
