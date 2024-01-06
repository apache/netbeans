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
package org.netbeans.modules.j2ee.persistence.unit;

import junit.framework.*;
import junit.textui.TestRunner;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;

/**
 * Tests for the persistence unit data object.
 * @author Erno Mononen
 */
public class PersistenceUnitDataObjectTest extends PersistenceEditorTestBase{
    
    public PersistenceUnitDataObjectTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(PersistenceUnitDataObjectTest.class);
        return suite;
    }
    
    public void testAddPersistenceUnit() throws Exception{
        String version=dataObject.getPersistence().getVersion();
        PersistenceUnit persistenceUnit = null;
        
        if(Persistence.VERSION_3_2.equals(version)) {
            persistenceUnit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_2.PersistenceUnit();
        } else if(Persistence.VERSION_3_1.equals(version)) {
            persistenceUnit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_1.PersistenceUnit();
        } else if(Persistence.VERSION_3_0.equals(version)) {
            persistenceUnit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_0.PersistenceUnit();
        } else if(Persistence.VERSION_2_2.equals(version)) {
            persistenceUnit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_2.PersistenceUnit();
        } else if(Persistence.VERSION_2_1.equals(version)) {
            persistenceUnit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_1.PersistenceUnit();
        } else if(Persistence.VERSION_2_0.equals(version)) {
            persistenceUnit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit();
        } else if(Persistence.VERSION_1_0.equals(version)) {
            persistenceUnit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit();
        } else {
            persistenceUnit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_1.PersistenceUnit();
        }
        
        persistenceUnit.setName("em3");
        persistenceUnit.setJtaDataSource("jdbc/__default");
        dataObject.addPersistenceUnit(persistenceUnit);
        
        assertTrue(containsUnit(persistenceUnit));
        assertTrue(dataCacheContains("\"em3\""));
        assertTrue(dataCacheContains("<jta-data-source>jdbc/__default"));
    }
    
    public void testRemovePersistenceUnit()throws Exception{
        int originalSize = dataObject.getPersistence().getPersistenceUnit().length;
        PersistenceUnit toBeRemoved = dataObject.getPersistence().getPersistenceUnit(0);
        String name = toBeRemoved.getName();
        dataObject.removePersistenceUnit(toBeRemoved);
        assertFalse(containsUnit(toBeRemoved));
        assertTrue(dataObject.getPersistence().getPersistenceUnit().length == originalSize -1);
        assertFalse(dataCacheContains("name=\"" + name + "\""));
    }
    
    public void testChangeName() throws Exception{
        PersistenceUnit persistenceUnit = dataObject.getPersistence().getPersistenceUnit(0);
        String oldName = persistenceUnit.getName();
        String newName = "new name";
        persistenceUnit.setName(newName);
        dataObject.modelUpdatedFromUI();
        assertTrue(dataCacheContains("\"" + newName + "\""));
        assertFalse(dataCacheContains("\"" + oldName + "\""));
    }

    public void testChangeDatasource() throws Exception{
        PersistenceUnit persistenceUnit = dataObject.getPersistence().getPersistenceUnit(0);
        String newDatasource = "jdbc/new_datasource";
        persistenceUnit.setJtaDataSource(newDatasource);
        dataObject.modelUpdatedFromUI();
        assertEquals(newDatasource, persistenceUnit.getJtaDataSource());
        assertTrue(dataCacheContains(newDatasource));
    }

    public void testAddClass() throws Exception{
        PersistenceUnit persistenceUnit = dataObject.getPersistence().getPersistenceUnit(0);
        String clazz = "com.foo.bar.FooClass";
        dataObject.addClass(persistenceUnit, clazz, false);
        assertTrue(dataCacheContains(clazz));
    }
    
    public void testRemoveClass() throws Exception {
        PersistenceUnit persistenceUnit = dataObject.getPersistence().getPersistenceUnit(0);
        String clazz = "com.foo.bar.FooClass";
        dataObject.addClass(persistenceUnit, clazz, false);
        assertTrue(dataCacheContains(clazz));
        dataObject.removeClass(persistenceUnit, clazz, false);
        assertFalse(dataCacheContains(clazz));
    }

    public void testAddMultipleClasses() throws Exception {
        PersistenceUnit persistenceUnit = dataObject.getPersistence().getPersistenceUnit(0);
        String clazz = "com.foo.bar.FooClass";
        String clazz2 = "com.foo.bar.FooClass2";
        String clazz3 = "com.foo.bar.FooClass3";
        dataObject.addClass(persistenceUnit, clazz, false);
        dataObject.addClass(persistenceUnit, clazz2, false);
        dataObject.addClass(persistenceUnit, clazz3, false);
        assertTrue(dataCacheContains(clazz));
        assertTrue(dataCacheContains(clazz2));
        assertTrue(dataCacheContains(clazz3));
    }
    
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
}
