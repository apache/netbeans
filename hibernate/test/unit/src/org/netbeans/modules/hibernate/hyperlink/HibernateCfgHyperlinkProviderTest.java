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
public class HibernateCfgHyperlinkProviderTest extends HibernateCompletionTestBase {

    public HibernateCfgHyperlinkProviderTest(String name) {
        super(name);
    }

    /**
     * Test of isHyperlinkPoint method, of class HibernateCfgHyperlinkProvider.
     */
    @Test
    public void testIsHyperlinkPoint1() throws Exception{
        System.out.println("isHyperlinkPoint");
        setupCompletion("resources/hibernate.cfg.xml", null);
        HibernateCfgHyperlinkProvider instance = new HibernateCfgHyperlinkProvider();
        
        boolean hyperpointNot = instance.isHyperlinkPoint(instanceDocument, 847);
        assertTrue(!hyperpointNot);
        
        boolean hyperpointYes = instance.isHyperlinkPoint(instanceDocument, 855);
        assertTrue(hyperpointYes);
        
    }

    /**
     * Test of getHyperlinkSpan method, of class HibernateCfgHyperlinkProvider.
     */
    @Test
    public void testGetHyperlinkSpan() throws Exception {
        System.out.println("getHyperlinkSpan");
        setupCompletion("resources/hibernate.cfg.xml", null);
        HibernateCfgHyperlinkProvider instance = new HibernateCfgHyperlinkProvider();
        instance.isHyperlinkPoint(instanceDocument, 855);
        int[] hyperpointSpan = instance.getHyperlinkSpan(instanceDocument, 855);
        int[] expected = new int[]{853, 874};
        assertEquals(hyperpointSpan[0], expected[0]);
        assertEquals(hyperpointSpan[1], expected[1]);
    }
}
