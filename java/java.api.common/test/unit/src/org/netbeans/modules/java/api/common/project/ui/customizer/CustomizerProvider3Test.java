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
package org.netbeans.modules.java.api.common.project.ui.customizer;

import java.util.HashMap;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.junit.MockServices;
import org.openide.util.Lookup;

/**
 * Test of SPI class org.netbeans.modules.java.api.common.project.ui.customizer.CustomizerProvider3
 *
 * @author Petr Somol
 */
public class CustomizerProvider3Test {

    public CustomizerProvider3Test() {
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        MockServices.setServices(CustomizerProvider3Test.MockCustomizerProvider.class);
    }

    @Test
    public void testShowCustomizer() {
         CustomizerProvider3 provider = Lookup.getDefault().lookup(CustomizerProvider3.class);
         assertNotNull(provider);
         MockCustomizerProvider mockProvider = (MockCustomizerProvider)provider;
         assertFalse(mockProvider.customizerOpen());
         
         provider.showCustomizer();
         assertTrue(mockProvider.customizerOpen());
    }
    
    @Test
    public void testCloseCancelCustomizer() {
         CustomizerProvider3 provider = Lookup.getDefault().lookup(CustomizerProvider3.class);
         assertNotNull(provider);
         MockCustomizerProvider mockProvider = (MockCustomizerProvider)provider;
         
         provider.showCustomizer();
         assertTrue(mockProvider.customizerOpen());

         MockCustomizer.invokeProjectModifyingAction();
         assertFalse(mockProvider.customizerOpen());
    }

    public static final class MockCustomizerProvider implements CustomizerProvider3 {

        private MockCustomizer customizerDialog = null;
        private Map<String,String> props = new HashMap<>();
        
        public MockCustomizerProvider() {
        }
        
        @Override
        public void cancelCustomizer() {
            customizerDialog = null;
        }

        @Override
        public void showCustomizer(String preselectedCategory, String preselectedSubCategory) {
            showCustomizer();
        }

        @Override
        public void showCustomizer() {
            customizerDialog = new MockCustomizer();
        }
        
        public void loadProperties(Map<String,String> properties) {
            this.props.putAll(properties);
        }
        
        public void saveProperties(Map<String,String> properties) {
            properties.putAll(this.props);
        }
        
        public boolean customizerOpen() {
            return customizerDialog != null;
        }
        
    }
    
    /**
     *
     */
    public static final class MockCustomizer{

        // user invokes an action that changes
        // project metafiles to such extent that project
        // properties dialog needs to be closed
        // before making changes to project metafiles
        public static void invokeProjectModifyingAction() {
            CustomizerProvider3 provider = Lookup.getDefault().lookup(CustomizerProvider3.class);
            provider.cancelCustomizer();
            // ..do whatever is needed
        }
        
    }
    
}
