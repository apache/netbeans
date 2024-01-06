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

import java.util.List;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.xml.multiview.Error;

/**
 * Tests for the <code>PersistenceValidator</code>.
 * @author Erno Mononen
 */
public class PersistenceValidatorTest extends PersistenceEditorTestBase {
    
    public PersistenceValidatorTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    /**
     * Tests that validator reports duplicate names as errors.
     */
    public void testValidateNameIsUnique() {
        String version=dataObject.getPersistence().getVersion();
        PersistenceUnit unit1 = null;
        if(Persistence.VERSION_3_2.equals(version)) {
            unit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_2.PersistenceUnit();
        } else if(Persistence.VERSION_3_1.equals(version)) {
            unit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_1.PersistenceUnit();
        } else if(Persistence.VERSION_3_0.equals(version)) {
            unit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_0.PersistenceUnit();
        } else if(Persistence.VERSION_2_2.equals(version)) {
            unit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_2.PersistenceUnit();
        } else if(Persistence.VERSION_2_1.equals(version)) {
            unit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_1.PersistenceUnit();
        } else if(Persistence.VERSION_2_0.equals(version)) {
            unit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit();
        } else if(Persistence.VERSION_1_0.equals(version)) {
            unit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit();
        } else {
            unit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_1.PersistenceUnit();
        }
        unit1.setName("name1");
        dataObject.addPersistenceUnit(unit1);
        PersistenceUnit unit2 = null;
        if(Persistence.VERSION_3_2.equals(version)) {
            unit2 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_2.PersistenceUnit();
        } else if(Persistence.VERSION_3_1.equals(version)) {
            unit2 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_1.PersistenceUnit();
        } else if(Persistence.VERSION_3_0.equals(version)) {
            unit2 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_0.PersistenceUnit();
        } else if(Persistence.VERSION_2_2.equals(version)) {
            unit2 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_2.PersistenceUnit();
        } else if(Persistence.VERSION_2_1.equals(version)) {
            unit2 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_1.PersistenceUnit();
        } else if(Persistence.VERSION_2_0.equals(version)) {
            unit2 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit();
        } else if(Persistence.VERSION_1_0.equals(version)) {
            unit2 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit();
        } else {
            unit2 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_1.PersistenceUnit();
        }
        unit2.setName("name1");
        dataObject.addPersistenceUnit(unit2);
        PersistenceValidator validator = new PersistenceValidatorImpl(dataObject, false);
        List<Error> errors = validator.validate();
        assertEquals(2, errors.size());
        assertEquals(Error.DUPLICATE_VALUE_MESSAGE, errors.get(0).getErrorType());
        assertEquals(Error.DUPLICATE_VALUE_MESSAGE, errors.get(1).getErrorType());
    }

    
    /**
     * Tests that validator reports usage of exclude-unlisted-classes in 
     * Java SE environments as errors.
     */
    public void testValidateExcludeUnlistedClasses(){
        // Java SE
        PersistenceValidator javaSEvalidator = new PersistenceValidatorImpl(dataObject, true);
        String version=dataObject.getPersistence().getVersion();
        PersistenceUnit unit1 = null;
        if(Persistence.VERSION_3_2.equals(version)) {
            unit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_2.PersistenceUnit();
        } else if(Persistence.VERSION_3_1.equals(version)) {
            unit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_1.PersistenceUnit();
        } else if(Persistence.VERSION_3_0.equals(version)) {
            unit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_0.PersistenceUnit();
        } else if(Persistence.VERSION_2_2.equals(version)) {
            unit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_2.PersistenceUnit();
        } else if(Persistence.VERSION_2_1.equals(version)) {
            unit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_1.PersistenceUnit();
        } else if(Persistence.VERSION_2_0.equals(version)) {
            unit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit();
        } else if(Persistence.VERSION_1_0.equals(version)) {
            unit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit();
        } else {
            unit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_1.PersistenceUnit();
        }
        unit1.setName("unit1");
        unit1.setExcludeUnlistedClasses(true);
        dataObject.addPersistenceUnit(unit1);
        List<Error> errors = javaSEvalidator.validate();
        assertEquals(1, errors.size());
        assertEquals(Error.TYPE_WARNING, errors.get(0).getErrorType());
        // Java EE
        PersistenceValidator javaEEvalidator = new PersistenceValidatorImpl(dataObject, false);
        errors = javaEEvalidator.validate();
        assertTrue(errors.isEmpty());
    }
    
    /**
     * Tests that validator reports usage of jar-files in 
     * Java SE environments as errors.
     */
    public void testValidateJarFiles(){
        // Java SE
        PersistenceValidator javaSEvalidator = new PersistenceValidatorImpl(dataObject, true);
        String version=dataObject.getPersistence().getVersion();
        PersistenceUnit unit1 = null;
        if(Persistence.VERSION_3_2.equals(version)) {
            unit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_2.PersistenceUnit();
        } else if(Persistence.VERSION_3_1.equals(version)) {
            unit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_1.PersistenceUnit();
        } else if(Persistence.VERSION_3_0.equals(version)) {
            unit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_0.PersistenceUnit();
        } else if(Persistence.VERSION_2_2.equals(version)) {
            unit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_2.PersistenceUnit();
        } else if(Persistence.VERSION_2_1.equals(version)) {
            unit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_1.PersistenceUnit();
        } else if(Persistence.VERSION_2_0.equals(version)) {
            unit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit();
        } else if(Persistence.VERSION_1_0.equals(version)) {
            unit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit();
        } else {
            unit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_3_1.PersistenceUnit();
        }
        unit1.setName("unit1");
        unit1.addJarFile("my-jar.jar");
        dataObject.addPersistenceUnit(unit1);
        List<Error> errors = javaSEvalidator.validate();
        assertEquals(1, errors.size());
        assertEquals(Error.TYPE_WARNING, errors.get(0).getErrorType());
        // Java EE
        PersistenceValidator javaEEvalidator = new PersistenceValidatorImpl(dataObject, false);
        errors = javaEEvalidator.validate();
        assertTrue(errors.isEmpty());
    }
    
    /**
     * Implementation of PersistenceValidator that allows to be specified 
     * whether we're dealing with Java SE environment. 
     */ 
    private static class PersistenceValidatorImpl extends PersistenceValidator {
        
        private boolean javaSE;
        
        public PersistenceValidatorImpl(PUDataObject puDataObject, boolean javaSE){
            super(puDataObject);
            this.javaSE = javaSE;
        }

        @Override
        protected boolean isJavaSE() {
            return javaSE;
        }
        
        
    }
}
