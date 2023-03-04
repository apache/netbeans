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
/*
 * ValidatorSchemaFactoryRegistryTest.java
 * JUnit based test
 *
 * Created on February 6, 2007, 11:13 PM
 */

package org.netbeans.modules.xml.wsdl.validator;

import java.util.ArrayList;
import junit.framework.*;
import java.util.Collection;
import java.util.Hashtable;
import org.netbeans.modules.xml.wsdl.validator.spi.ValidatorSchemaFactory;
import org.openide.util.Lookup;

/**
 *
 * @author radval
 */
public class ValidatorSchemaFactoryRegistryTest extends TestCase {

    public ValidatorSchemaFactoryRegistryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of getDefault method, of class org.netbeans.modules.xml.wsdl.validator.ValidatorSchemaFactoryRegistry.
     */
    public void testGetDefault() {
        System.out.println("getDefault");

        ValidatorSchemaFactoryRegistry result = ValidatorSchemaFactoryRegistry.getDefault();
        assertNotNull(result);
        
        
        
    }

    /**
     * Test of getValidatorSchemaFactory method, of class org.netbeans.modules.xml.wsdl.validator.ValidatorSchemaFactoryRegistry.
     */
    public void testGetValidatorSchemaFactory() {
        System.out.println("getValidatorSchemaFactory");
        
        String namespace = "";
        ValidatorSchemaFactoryRegistry instance = ValidatorSchemaFactoryRegistry.getDefault();
        
        ValidatorSchemaFactory expResult = null;
        ValidatorSchemaFactory result = instance.getValidatorSchemaFactory(namespace);
        assertEquals(expResult, result);
        
        
        
    }

    /**
     * Test of getAllValidatorSchemaFactories method, of class org.netbeans.modules.xml.wsdl.validator.ValidatorSchemaFactoryRegistry.
     */
    public void testGetAllValidatorSchemaFactories() {
        System.out.println("getAllValidatorSchemaFactories");
        
        ValidatorSchemaFactoryRegistry instance = ValidatorSchemaFactoryRegistry.getDefault();
        
        Collection<ValidatorSchemaFactory> expResult = new ArrayList<ValidatorSchemaFactory>();
        Collection<ValidatorSchemaFactory> result = instance.getAllValidatorSchemaFactories();
        assertEquals(expResult.size(), result.size());
        
       
        
    }
    
}
