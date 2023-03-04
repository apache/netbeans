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
package org.netbeans.modules.j2ee.sun.ddloaders;

import junit.framework.TestCase;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.Type;

/**
 *
 * @author vkraemer
 */
public class DDTypeTest extends TestCase {
    
    public DDTypeTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of equals method, of class DDType.
     */
    public void testEquals() {
        System.out.println("equals");
        Object obj = null;
        DDType instance = DDType.DD_GF_APPLICATION;
        boolean expResult = false;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
        obj = "String";
        result = instance.equals(obj);
        assertEquals(expResult, result);
        obj = DDType.DD_GF_APP_CLIENT;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        obj = DDType.DD_GF_EJB_JAR;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        obj = DDType.DD_GF_RESOURCE;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        obj = DDType.DD_GF_WEB_APP;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        obj = DDType.DD_SUN_APPLICATION;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        expResult = true;
        obj = DDType.DD_GF_APPLICATION;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of hashCode method, of class DDType.
     */
    public void testHashCode() {
        System.out.println("hashCode");
        DDType instance = DDType.DD_GF_APPLICATION;
        int expResult = -942715005;
        int result = instance.hashCode();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getDescriptorMimeTypeSuffix method, of class DDType.
     */
    public void testGetDescriptorMimeTypeSuffix() {
        System.out.println("getDescriptorMimeTypeSuffix");
        DDType instance = DDType.DD_GF_APPLICATION;
        String expResult = "-application+xml";
        String result = instance.getDescriptorMimeTypeSuffix();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
}
