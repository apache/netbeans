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

package org.netbeans.modules.hibernate.hyperlink;

import org.junit.Test;
import org.netbeans.modules.hibernate.completion.HibernateCompletionTestBase;
import static org.junit.Assert.*;

/**
 *
 * @author Dongmei Cao
 */
public class HibernateMappingHyperlinkProviderTest extends HibernateCompletionTestBase {

    public HibernateMappingHyperlinkProviderTest(String name) {
        super(name);
    }

    
    /**
     * Test of isHyperlinkPoint method, of class HibernateMappingHyperlinkProvider.
     */
    @Test
    public void testIsHyperlinkPointOnClass() throws Exception {
        System.out.println("testIsHyperlinkPointOnClass");
        setupCompletion("resources/Person.hbm.xml", null);
        HibernateMappingHyperlinkProvider instance = new HibernateMappingHyperlinkProvider();
        
        boolean hyperpointNot = instance.isHyperlinkPoint(instanceDocument, 212);
        assertTrue(!hyperpointNot);
        
        boolean hyperpointYes = instance.isHyperlinkPoint(instanceDocument, 219);
        assertTrue(hyperpointYes);
    }
    
    /**
     * Test of getHyperlinkSpan method, of class HibernateMappingHyperlinkProvider.
     */
    @Test
    public void testGetHyperlinkSpanOnClass() throws Exception {
        System.out.println("testGetHyperlinkSpanClass");
        setupCompletion("resources/Person.hbm.xml", null);
        HibernateMappingHyperlinkProvider instance = new HibernateMappingHyperlinkProvider();
        instance.isHyperlinkPoint(instanceDocument, 219);
        int[] hyperpointSpan = instance.getHyperlinkSpan(instanceDocument, 219);
        int[] expected = new int[]{216, 229};
        assertEquals(hyperpointSpan[0], expected[0]);
        assertEquals(hyperpointSpan[1], expected[1]);
    }
    
    /**
     * Test of isHyperlinkPoint method, of class HibernateMappingHyperlinkProvider.
     */
    @Test
    public void testIsHyperlinkPointOnProperty() throws Exception {
        System.out.println("testIsHyperlinkPointProperty");
        setupCompletion("resources/Person.hbm.xml", null);
        HibernateMappingHyperlinkProvider instance = new HibernateMappingHyperlinkProvider();
        
        boolean hyperpointNot = instance.isHyperlinkPoint(instanceDocument, 351);
        assertTrue(!hyperpointNot);
        
        boolean hyperpointYes = instance.isHyperlinkPoint(instanceDocument, 363);
        assertTrue(hyperpointYes);
    }
    
    /**
     * Test of getHyperlinkSpan method, of class HibernateMappingHyperlinkProvider.
     */
    @Test
    public void testGetHyperlinkSpanOnProperty() throws Exception {
        System.out.println("testGetHyperlinkSpanOnProperty");
        setupCompletion("resources/Person.hbm.xml", null);
        HibernateMappingHyperlinkProvider instance = new HibernateMappingHyperlinkProvider();
        instance.isHyperlinkPoint(instanceDocument, 363);
        int[] hyperpointSpan = instance.getHyperlinkSpan(instanceDocument, 363);
        int[] expected = new int[]{362, 366};
        assertEquals(hyperpointSpan[0], expected[0]);
        assertEquals(hyperpointSpan[1], expected[1]);
    }
}
