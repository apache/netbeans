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


package org.netbeans.modules.j2ee.persistence.provider;

import java.io.File;
import java.net.URL;
import junit.framework.*;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.dd.common.Property;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests for ProviderUtil.
 * @author Erno Mononen
 */
public class ProviderUtilTest extends NbTestCase {
    
    private PersistenceUnit persistenceUnit1;
    private PersistenceUnit persistenceUnit2;
    private PersistenceUnit persistenceUnit3;
    private PersistenceUnit persistenceUnit4;
    private PersistenceUnit persistenceUnit5;
    private PersistenceUnit persistenceUnit6;
    private PersistenceUnit persistenceUnit7;
    
    public ProviderUtilTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        this.persistenceUnit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit();
        this.persistenceUnit2 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit();
        this.persistenceUnit3 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_1.PersistenceUnit();
        this.persistenceUnit4 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_2.PersistenceUnit();
        this.persistenceUnit5 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_0.PersistenceUnit();
        this.persistenceUnit6 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_1.PersistenceUnit();
        this.persistenceUnit7 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_2.PersistenceUnit();
    }
    
    @Override
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ProviderUtilTest.class);
        return suite;
    }
    
    public void testGetProvider1() {
        persistenceUnit1.setProvider(ProviderUtil.HIBERNATE_PROVIDER1_0.getProviderClass());
        assertEquals(ProviderUtil.HIBERNATE_PROVIDER1_0, ProviderUtil.getProvider(persistenceUnit1));
    }
    

    public void testSetTableGeneration1(){
        Provider provider = ProviderUtil.ECLIPSELINK_PROVIDER2_0;
        persistenceUnit1.setProvider(provider.getProviderClass());
        
        ProviderUtil.setTableGeneration(persistenceUnit1, Provider.TABLE_GENERATION_CREATE, provider);
        assertPropertyExists(persistenceUnit1, provider.getTableGenerationPropertyName());
        assertValueExists(persistenceUnit1, provider.getTableGenerationCreateValue());
        assertNoSuchValue(persistenceUnit1, provider.getTableGenerationDropCreateValue());
        
        ProviderUtil.setTableGeneration(persistenceUnit1, Provider.TABLE_GENERATION_DROPCREATE, provider);
        assertPropertyExists(persistenceUnit1, provider.getTableGenerationPropertyName());
        assertValueExists(persistenceUnit1, provider.getTableGenerationDropCreateValue());
        assertNoSuchValue(persistenceUnit1, provider.getTableGenerationCreateValue());
        
    }
    
    public void testSetProvider1(){
        Provider provider = ProviderUtil.KODO_PROVIDER;
        ProviderUtil.setProvider(persistenceUnit1, provider, getConnection(), Provider.TABLE_GENERATTION_UNKOWN);
        assertEquals(provider.getProviderClass(), persistenceUnit1.getProvider());
        assertPropertyExists(persistenceUnit1, provider.getJdbcDriver());
        assertPropertyExists(persistenceUnit1, provider.getJdbcUrl());
        assertPropertyExists(persistenceUnit1, provider.getJdbcUsername());
    }
    
    public void testChangeProvider1(){
        Provider originalProvider = ProviderUtil.TOPLINK_PROVIDER1_0;
        ProviderUtil.setProvider(persistenceUnit1, originalProvider, getConnection(), Provider.TABLE_GENERATION_CREATE);
        assertEquals(originalProvider.getProviderClass(), persistenceUnit1.getProvider());
        
        Provider newProvider = ProviderUtil.ECLIPSELINK_PROVIDER2_0;
        ProviderUtil.setProvider(persistenceUnit1, newProvider, getConnection(), Provider.TABLE_GENERATION_DROPCREATE);
        // assert that old providers properties were removed
        assertNoSuchProperty(persistenceUnit1, originalProvider.getTableGenerationPropertyName());
        assertNoSuchProperty(persistenceUnit1, originalProvider.getJdbcDriver());
        assertNoSuchProperty(persistenceUnit1, originalProvider.getJdbcUrl());
        assertNoSuchProperty(persistenceUnit1, originalProvider.getJdbcUsername());
        // assert that new providers properties are set
        assertEquals(newProvider.getProviderClass(), persistenceUnit1.getProvider());
        assertPropertyExists(persistenceUnit1, newProvider.getJdbcDriver());
        assertPropertyExists(persistenceUnit1, newProvider.getJdbcUrl());
        assertPropertyExists(persistenceUnit1, newProvider.getJdbcUsername());
        assertPropertyExists(persistenceUnit1, newProvider.getTableGenerationPropertyName());
    }
    
    /**
     * Tests that changing of provider preserves existing
     * table generation value.
     */
    public void testTableGenerationPropertyIsPreserved1(){
        Provider originalProvider = ProviderUtil.KODO_PROVIDER;
        ProviderUtil.setProvider(persistenceUnit1, originalProvider, getConnection(), Provider.TABLE_GENERATION_CREATE);
        
        Provider newProvider = ProviderUtil.ECLIPSELINK_PROVIDER2_0;
        ProviderUtil.setProvider(persistenceUnit1, newProvider, getConnection(), Provider.TABLE_GENERATION_CREATE);
        assertEquals(newProvider.getTableGenerationPropertyName(),
                ProviderUtil.getProperty(persistenceUnit1, newProvider.getTableGenerationPropertyName()).getName());
        assertEquals(newProvider.getTableGenerationCreateValue(),
                ProviderUtil.getProperty(persistenceUnit1, newProvider.getTableGenerationPropertyName()).getValue());
        
        
        
    }
    
    public void testRemoveProviderProperties1(){
        Provider provider = ProviderUtil.KODO_PROVIDER;
        PersistenceUnit persistenceUnit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit();
        ProviderUtil.setProvider(persistenceUnit, provider, getConnection(), Provider.TABLE_GENERATION_CREATE);
        //        ProviderUtil.setTableGeneration(persistenceUnit, Provider.TABLE_GENERATION_CREATE, provider);
        
        ProviderUtil.removeProviderProperties(persistenceUnit);
        assertNoSuchProperty(persistenceUnit1, provider.getTableGenerationPropertyName());
        assertNoSuchProperty(persistenceUnit1, provider.getJdbcDriver());
        assertNoSuchProperty(persistenceUnit1, provider.getJdbcUrl());
        assertNoSuchProperty(persistenceUnit1, provider.getJdbcUsername());
        
    }
    
    
    public void testGetPUDataObject1() throws Exception{
        String invalidPersistenceXml = getDataDir().getAbsolutePath() + File.separator + "invalid_persistence.xml";
        FileObject invalidPersistenceFO = FileUtil.toFileObject(new File(invalidPersistenceXml));
        try{
            ProviderUtil.getPUDataObject(invalidPersistenceFO);
            fail("InvalidPersistenceXmlException should have been thrown");
        } catch (InvalidPersistenceXmlException ipx){
            assertEquals(invalidPersistenceXml, ipx.getPath());
        }
        
    }

    public void testGetProvider2() {
        persistenceUnit2.setProvider(ProviderUtil.HIBERNATE_PROVIDER2_0.getProviderClass());
        assertEquals(ProviderUtil.HIBERNATE_PROVIDER2_0, ProviderUtil.getProvider(persistenceUnit2));
    }

    public void testSetTableGeneration2(){
        Provider provider = ProviderUtil.ECLIPSELINK_PROVIDER2_0;
        persistenceUnit2.setProvider(provider.getProviderClass());

        ProviderUtil.setTableGeneration(persistenceUnit2, Provider.TABLE_GENERATION_CREATE, provider);
        assertPropertyExists(persistenceUnit2, provider.getTableGenerationPropertyName());
        assertValueExists(persistenceUnit2, provider.getTableGenerationCreateValue());
        assertNoSuchValue(persistenceUnit2, provider.getTableGenerationDropCreateValue());

        ProviderUtil.setTableGeneration(persistenceUnit2, Provider.TABLE_GENERATION_DROPCREATE, provider);
        assertPropertyExists(persistenceUnit2, provider.getTableGenerationPropertyName());
        assertValueExists(persistenceUnit2, provider.getTableGenerationDropCreateValue());
        assertNoSuchValue(persistenceUnit2, provider.getTableGenerationCreateValue());

    }

    public void testSetProvider2(){
        Provider provider = ProviderUtil.KODO_PROVIDER;
        ProviderUtil.setProvider(persistenceUnit2, provider, getConnection(), Provider.TABLE_GENERATTION_UNKOWN);
        assertEquals(provider.getProviderClass(), persistenceUnit2.getProvider());
        assertPropertyExists(persistenceUnit2, provider.getJdbcDriver());
        assertPropertyExists(persistenceUnit2, provider.getJdbcUrl());
        assertPropertyExists(persistenceUnit2, provider.getJdbcUsername());
    }

    public void testChangeProvider2(){
        Provider originalProvider = ProviderUtil.TOPLINK_PROVIDER1_0;
        ProviderUtil.setProvider(persistenceUnit2, originalProvider, getConnection(), Provider.TABLE_GENERATION_CREATE);
        assertEquals(originalProvider.getProviderClass(), persistenceUnit2.getProvider());

        Provider newProvider = ProviderUtil.ECLIPSELINK_PROVIDER2_0;
        ProviderUtil.setProvider(persistenceUnit2, newProvider, getConnection(), Provider.TABLE_GENERATION_DROPCREATE);
        // assert that old providers properties were removed
        assertNoSuchProperty(persistenceUnit2, originalProvider.getTableGenerationPropertyName());
        assertNoSuchProperty(persistenceUnit2, originalProvider.getJdbcDriver());
        assertNoSuchProperty(persistenceUnit2, originalProvider.getJdbcUrl());
        assertNoSuchProperty(persistenceUnit2, originalProvider.getJdbcUsername());
        // assert that new providers properties are set
        assertEquals(newProvider.getProviderClass(), persistenceUnit2.getProvider());
        assertPropertyExists(persistenceUnit2, newProvider.getJdbcDriver());
        assertPropertyExists(persistenceUnit2, newProvider.getJdbcUrl());
        assertPropertyExists(persistenceUnit2, newProvider.getJdbcUsername());
        assertPropertyExists(persistenceUnit2, newProvider.getTableGenerationPropertyName());
    }
    
    public void testGetProvider3() {
        persistenceUnit3.setProvider(ProviderUtil.OPENJPA_PROVIDER2_1.getProviderClass());
        assertEquals(ProviderUtil.OPENJPA_PROVIDER2_1, ProviderUtil.getProvider(persistenceUnit3));
    }
    
    public void testSetProvider3(){
        Provider provider = ProviderUtil.OPENJPA_PROVIDER2_1;
        ProviderUtil.setProvider(persistenceUnit3, provider, getConnection(), Provider.TABLE_GENERATTION_UNKOWN);
        assertEquals(provider.getProviderClass(), persistenceUnit3.getProvider());
        assertPropertyExists(persistenceUnit3, provider.getJdbcDriver());
        assertPropertyExists(persistenceUnit3, provider.getJdbcUrl());
        assertPropertyExists(persistenceUnit3, provider.getJdbcUsername());
    }
    
    public void testGetProvider4() {
        persistenceUnit4.setProvider(ProviderUtil.ECLIPSELINK_PROVIDER2_2.getProviderClass());
        assertEquals(ProviderUtil.ECLIPSELINK_PROVIDER2_2, ProviderUtil.getProvider(persistenceUnit4));
    }
    
    public void testSetProvider4(){
        Provider provider = ProviderUtil.ECLIPSELINK_PROVIDER2_2;
        ProviderUtil.setProvider(persistenceUnit4, provider, getConnection(), Provider.TABLE_GENERATTION_UNKOWN);
        assertEquals(provider.getProviderClass(), persistenceUnit4.getProvider());
        assertPropertyExists(persistenceUnit4, provider.getJdbcDriver());
        assertPropertyExists(persistenceUnit4, provider.getJdbcUrl());
        assertPropertyExists(persistenceUnit4, provider.getJdbcUsername());
    }
    
    public void testGetProvider5() {
        persistenceUnit5.setProvider(ProviderUtil.DATANUCLEUS_PROVIDER3_0.getProviderClass());
        assertEquals(ProviderUtil.ECLIPSELINK_PROVIDER2_2, ProviderUtil.getProvider(persistenceUnit5));
    }
    
    public void testSetProvider5(){
        Provider provider = ProviderUtil.DATANUCLEUS_PROVIDER3_0;
        ProviderUtil.setProvider(persistenceUnit5, provider, getConnection(), Provider.TABLE_GENERATTION_UNKOWN);
        assertEquals(provider.getProviderClass(), persistenceUnit5.getProvider());
        assertPropertyExists(persistenceUnit5, provider.getJdbcDriver());
        assertPropertyExists(persistenceUnit5, provider.getJdbcUrl());
        assertPropertyExists(persistenceUnit5, provider.getJdbcUsername());
    }
    
    public void testGetProvider6() {
        persistenceUnit6.setProvider(ProviderUtil.ECLIPSELINK_PROVIDER3_1.getProviderClass());
        assertEquals(ProviderUtil.ECLIPSELINK_PROVIDER3_1, ProviderUtil.getProvider(persistenceUnit4));
    }
    
    public void testSetProvider6(){
        Provider provider = ProviderUtil.ECLIPSELINK_PROVIDER3_1;
        ProviderUtil.setProvider(persistenceUnit6, provider, getConnection(), Provider.TABLE_GENERATTION_UNKOWN);
        assertEquals(provider.getProviderClass(), persistenceUnit6.getProvider());
        assertPropertyExists(persistenceUnit6, provider.getJdbcDriver());
        assertPropertyExists(persistenceUnit6, provider.getJdbcUrl());
        assertPropertyExists(persistenceUnit6, provider.getJdbcUsername());
    }
    
    public void testGetProvider7() {
        persistenceUnit7.setProvider(ProviderUtil.ECLIPSELINK_PROVIDER3_2.getProviderClass());
        assertEquals(ProviderUtil.ECLIPSELINK_PROVIDER3_2, ProviderUtil.getProvider(persistenceUnit7));
    }
    
    public void testSetProvider7(){
        Provider provider = ProviderUtil.ECLIPSELINK_PROVIDER3_2;
        ProviderUtil.setProvider(persistenceUnit7, provider, getConnection(), Provider.TABLE_GENERATTION_UNKOWN);
        assertEquals(provider.getProviderClass(), persistenceUnit7.getProvider());
        assertPropertyExists(persistenceUnit7, provider.getJdbcDriver());
        assertPropertyExists(persistenceUnit7, provider.getJdbcUrl());
        assertPropertyExists(persistenceUnit7, provider.getJdbcUsername());
    }

    /**
     * Tests that changing of provider preserves existing
     * table generation value.
     */
    public void testTableGenerationPropertyIsPreserved2(){
        Provider originalProvider = ProviderUtil.KODO_PROVIDER;
        ProviderUtil.setProvider(persistenceUnit2, originalProvider, getConnection(), Provider.TABLE_GENERATION_CREATE);

        Provider newProvider = ProviderUtil.ECLIPSELINK_PROVIDER2_0;
        ProviderUtil.setProvider(persistenceUnit2, newProvider, getConnection(), Provider.TABLE_GENERATION_CREATE);
        assertEquals(newProvider.getTableGenerationPropertyName(),
                ProviderUtil.getProperty(persistenceUnit2, newProvider.getTableGenerationPropertyName()).getName());
        assertEquals(newProvider.getTableGenerationCreateValue(),
                ProviderUtil.getProperty(persistenceUnit2, newProvider.getTableGenerationPropertyName()).getValue());

    }

    public void testRemoveProviderProperties2(){
        Provider provider = ProviderUtil.KODO_PROVIDER;
        PersistenceUnit persistenceUnit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit();
        ProviderUtil.setProvider(persistenceUnit, provider, getConnection(), Provider.TABLE_GENERATION_CREATE);
        //        ProviderUtil.setTableGeneration(persistenceUnit, Provider.TABLE_GENERATION_CREATE, provider);

        ProviderUtil.removeProviderProperties(persistenceUnit);
        assertNoSuchProperty(persistenceUnit1, provider.getTableGenerationPropertyName());
        assertNoSuchProperty(persistenceUnit1, provider.getJdbcDriver());
        assertNoSuchProperty(persistenceUnit1, provider.getJdbcUrl());
        assertNoSuchProperty(persistenceUnit1, provider.getJdbcUsername());

    }
    

    /**
     * Asserts that property with given name exists in persistence unit.
     */
    protected void assertPropertyExists(PersistenceUnit pu, String propertyName){
        if (!propertyExists(pu, propertyName)){
            fail("Property " + propertyName + " was not found.");
        }
        assertTrue(true);
    }
    
    /**
     * Asserts that no property with given name exists in persistence unit.
     */
    protected void assertNoSuchProperty(PersistenceUnit pu, String propertyName){
        if (propertyExists(pu, propertyName)){
            fail("Property " + propertyName + " was found.");
        }
        assertTrue(true);
    }
    
    protected void assertNoSuchValue(PersistenceUnit pu, String value){
        if (valueExists(pu, value)){
            fail("Property with value " + value + " was found");
        }
        assertTrue(true);
    }
    
    protected void assertValueExists(PersistenceUnit pu, String value){
        if (!valueExists(pu, value)){
            fail("Property with value " + value + " was not found");
        }
        assertTrue(true);
    }
    
    
    /**
     * @return true if property with given name exists in persistence unit,
     * false otherwise.
     */
    protected boolean propertyExists(PersistenceUnit pu, String propertyName){
        Property[] properties = ProviderUtil.getProperties(pu);
        for (int i = 0; i < properties.length; i++) {
            if (properties[i].getName().equals(propertyName)){
                return true;
            }
        }
        return false;
    }
    
    /**
     * @return true if property with given value exists in persistence unit,
     * false otherwise.
     */
    protected boolean valueExists(PersistenceUnit pu, String propertyValue){
        Property[] properties = ProviderUtil.getProperties(pu);
        for (int i = 0; i < properties.length; i++) {
            if (properties[i].getValue().equals(propertyValue)){
                return true;
            }
        }
        return false;
    }
    
    private DatabaseConnection getConnection(){
        JDBCDriver driver = JDBCDriver.create("driver", "driver", "foo.bar", new URL[]{});
        return DatabaseConnection.create(driver, "foo", "bar", "schema", "password", false);
    }
    
}
