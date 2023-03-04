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

package org.netbeans.modules.xml.schema.model;


import org.junit.Test;
import org.junit.After;
import static org.junit.Assert.*;

import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author Nikita Krjukov
 */
public class SchemaModelPerformanceTest {
    
    @After
    public void tearDown() {
        TestCatalogModel.getDefault().clearDocumentPool();
    }

    /**
     * Checks the optimization related to issue #168232
     */
    @Test
    public void testPerformance1() throws Exception {
        SchemaModel sm;
        sm = Util.loadSchemaModel2("resources/performance1.zip", "C.xsd"); // NOI18N
        sm = Util.loadSchemaModel2("resources/performance1.zip", "unresolvedIncludes.xsd"); // NOI18N
        //
        sm = Util.loadSchemaModel2("resources/performance1.zip", "A.xsd"); // NOI18N
        //
        // Wait 2 second till all models are loaded and validated
        Thread.sleep(1000);
        //
        assert(sm.getState() == State.VALID);
        GlobalElement e1 = (GlobalElement)sm.getSchema().getChildren().get(1);
        assertNotNull(e1);
        assertEquals(e1.getName(), "A1"); // NOI18N
        NamedComponentReference ncr = e1.getType();
        String name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assertEquals(name, "hl7_performance_test:C1"); // NOI18N
        //this is when it'll try to resolve
        //
        long before = System.currentTimeMillis();
        GlobalComplexType gct = (GlobalComplexType)ncr.get();
        long after = System.currentTimeMillis();
        long delay = after - before;
        assertTrue("Delay=" + delay, delay < 100L);
        System.out.println("Delay1=" + delay);
        //
        assertNotNull(gct);
        assertEquals(gct.getName(), "C1");
        //
        // Try again. It should be much faster now.
        e1 = (GlobalElement)sm.getSchema().getChildren().get(2);
        assertNotNull(e1);
        assertEquals(e1.getName(), "A2"); // NOI18N
        ncr = e1.getType();
        name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assertEquals(name, "hl7_performance_test:C2"); // NOI18N
        //this is when it'll try to resolve
        //
        before = System.currentTimeMillis();
        gct = (GlobalComplexType)ncr.get();
        after = System.currentTimeMillis();
        delay = after - before;
        assertTrue("Delay=" + delay, delay < 5L);
        System.out.println("Delay2=" + delay);
        //
        assertNotNull(gct);
        assertEquals(gct.getName(), "C2");
        //
        // Wait 5.5 seconds and try third time
        Thread.sleep(5500);
        //
        e1 = (GlobalElement)sm.getSchema().getChildren().get(3);
        assertNotNull(e1);
        assertEquals(e1.getName(), "A3"); // NOI18N
        ncr = e1.getType();
        name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assertEquals(name, "hl7_performance_test:C3"); // NOI18N
        //this is when it'll try to resolve
        //
        before = System.currentTimeMillis();
        gct = (GlobalComplexType)ncr.get();
        after = System.currentTimeMillis();
        delay = after - before;
        assertTrue("Delay=" + delay, delay < 30L);
        System.out.println("Delay3=" + delay);
        //
        assertNotNull(gct);
        assertEquals(gct.getName(), "C3");
    }
    
}
