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

package org.netbeans.modules.profiler.oql.repository.api;

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jaroslav Bachorik
 */
public class OQLQueryRepositoryTest {
    private OQLQueryRepository instance;

    public OQLQueryRepositoryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        instance = OQLQueryRepository.getInstance();
    }

    @After
    public void tearDown() {
        instance = null;
    }

    /**
     * Test of listCategories() method, of class OQLQueryBrowser.
     * Matching for any category
     */
    @Test
    public void testListAllCategories() {
        System.out.println("listAllCategories");
        
        List result = instance.listCategories();
        assertTrue(result.size() > 0);
    }

    /**
     * Test of listCategories(pattern) method, of class OQLQueryBrowser.
     * Matching for an existing pattern
     */
    @Test
    public void testListMatchingCategories() {
        System.out.println("listMatchingCategories");
        String pattern = "Sam.*";
        List result = instance.listCategories(pattern);
        assertEquals(1, result.size());
    }

    /**
     * Test of listCategories(pattern) method, of class OQLQueryBrowser.
     * Matching for a nonexisting pattern
     */
    @Test
    public void testListNonMatchingCategories() {
        System.out.println("listNonMatchingCategories");
        String pattern = "[0-9]+";
        List result = instance.listCategories(pattern);
        assertEquals(0, result.size());
    }

    /**
     * Test of listQueries() method, of class OQLQueryBrowser
     * Listing all queries available
     */
    @Test
    public void testListAllQueries() {
        System.out.println("listAllQueries");
        List result = instance.listQueries();
        assertEquals(11, result.size());
    }

    /**
     * Test of listQueries(OQLQueryCategory) method, of class OQLQueryBrowser
     * Listing all queries available for certain category
     */
    @Test
    public void testListAllCategoryQueries() {
        System.out.println("listAllCategoryQueries");
        OQLQueryCategory category = instance.listCategories().get(0);
        List result = instance.listQueries(category);
        assertEquals(4, result.size());
    }

    /**
     * Test of listQueries(String) method, of class OQLQueryBrowser
     * Listing all queries available matching the given pattern
     */
    @Test
    public void testListAllMatchingQueries() {
        System.out.println("listAllMatchingQueries");
        List result = instance.listQueries(".+?allocated.*");
        assertEquals(2, result.size());
    }

    /**
     * Test of listQueries(OQLQueryCategory, String) method, of class OQLQueryBrowser
     * Listing all queries available for certain category matching the given pattern
     */
    @Test
    public void testListMatchingCategoryQueries() {
        System.out.println("listMatchingCategoryQueries");
        OQLQueryCategory category = instance.listCategories().get(0);
        List result = instance.listQueries(category, ".+?allocated.*");
        assertEquals(2, result.size());
    }
}
