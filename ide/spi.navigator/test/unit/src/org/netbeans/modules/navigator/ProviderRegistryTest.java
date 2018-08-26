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

package org.netbeans.modules.navigator;

import java.util.Collection;
import javax.swing.JComponent;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.util.Lookup;


/**
 *
 * @author Dafe Simonek
 */
public class ProviderRegistryTest extends NbTestCase {

    /** test data type contants */
    private static final String MARVELOUS_DATA_TYPE_NAME = "MarvelousDataType";
    private static final String MARVELOUS_DATA_TYPE = "text/marvelous/data_type";
    
    public ProviderRegistryTest(String testName) {
        super(testName);
    }
    
    public void testGetProviders () throws Exception {
        UnitTestUtils.prepareTest(new String [] { "/org/netbeans/modules/navigator/resources/testGetProvidersLayer.xml" });
        
        ProviderRegistry providerReg = ProviderRegistry.getInstance();
        
        System.out.println("Asking for non-existent type...");
        assertEquals(0, providerReg.getProviders("image/non_existent_type").size());
        
        System.out.println("Asking for non-existent class...");
        assertEquals(0, providerReg.getProviders("text/plain").size());
        
        System.out.println("Asking for valid type and provider...");
        Collection<? extends NavigatorPanel> result = providerReg.getProviders(MARVELOUS_DATA_TYPE);
        assertEquals(1, result.size());
        NavigatorPanel np = result.iterator().next();
        assertTrue(np instanceof MarvelousDataTypeProvider);
        MarvelousDataTypeProvider provider = (MarvelousDataTypeProvider)np;
        assertEquals(MARVELOUS_DATA_TYPE_NAME, provider.getDisplayName());
    }
    

    /** Dummy navigator panel provider, just to test right loading and instantiating
     * for certain data type
     */ 
    public static final class MarvelousDataTypeProvider implements NavigatorPanel {
        
        public String getDisplayName () {
            return MARVELOUS_DATA_TYPE_NAME;
        }
    
        public String getDisplayHint () {
            return null;
        }

        public JComponent getComponent () {
            return null;
        }

        public void panelActivated (Lookup context) {
        }

        public void panelDeactivated () {
        }
        
        public Lookup getLookup () {
            return null;
        }
        
    }
    
    
}
