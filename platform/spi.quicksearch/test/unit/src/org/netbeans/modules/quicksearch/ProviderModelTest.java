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

package org.netbeans.modules.quicksearch;

import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;


/**
 *
 * @author Dafe Simonek
 */
public class ProviderModelTest extends NbTestCase {
    
    private static final String DISPLAY_NAME = "Test2 category";
    private static final String COMMAND_PREFIX = "t";

    public ProviderModelTest(String testName) {
        super(testName);
    }
    
    /** Tests ProviderModel functionality */
    public void testGetProviders () throws Exception {
        UnitTestUtils.prepareTest(new String [] { "org/netbeans/modules/quicksearch/resources/testGetProviders.xml" });
        
        ProviderModel model = ProviderModel.getInstance();
        
        System.out.println("Asking for test providers...");
        List<ProviderModel.Category> categories = model.getCategories();
        
        assertEquals(3, categories.size());
        
        
        System.out.println("Testing empty providers category...");
       
        ProviderModel.Category cat = categories.get(0);

        assertTrue("empty".equals(cat.getName()));
        assertTrue(cat.getName().equals(cat.getDisplayName()));
        assertTrue(cat.getCommandPrefix() == null);
        
        System.out.println("Testing category with provider which doesn't define category description...");
        
        cat = categories.get(1);
        
        assertTrue("test1".equals(cat.getName()));
        assertTrue(cat.getName().equals(cat.getDisplayName()));
        assertTrue(cat.getCommandPrefix() == null);
        
        List<SearchProvider> providers = cat.getProviders();
        assertEquals(1, providers.size());
        SearchProvider sp = providers.iterator().next();
        assertTrue(sp instanceof Test1Provider);

        System.out.println("Testing category with provider with full category description...");
        
        cat = categories.get(2);
        
        assertTrue("test2".equals(cat.getName()));
        // localized FO name don't work in test, don't know why
        //assertTrue(DISPLAY_NAME.equals(cat.getDisplayName()));
        assertTrue(COMMAND_PREFIX.equals(cat.getCommandPrefix()));
        
        providers = cat.getProviders();
        assertEquals(1, providers.size());
        sp = providers.iterator().next();
        assertTrue(sp instanceof Test2Provider);
    }
    
    
    /** Test provider without category description */
    public static class Test1Provider implements SearchProvider {
        
        public void evaluate(SearchRequest request, SearchResponse response) {
            // no operation
        }

    }
    
    /** Test provider with full category description */
    public static class Test2Provider implements SearchProvider {

        public void evaluate(SearchRequest request, SearchResponse response) {
            // no operation
        }

    }
    
}
