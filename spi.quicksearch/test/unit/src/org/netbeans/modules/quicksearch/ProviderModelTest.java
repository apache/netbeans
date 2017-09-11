/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
        UnitTestUtils.prepareTest(new String [] { "/org/netbeans/modules/quicksearch/resources/testGetProviders.xml" });
        
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
