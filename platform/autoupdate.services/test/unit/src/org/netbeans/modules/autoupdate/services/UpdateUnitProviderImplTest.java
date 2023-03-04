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

package org.netbeans.modules.autoupdate.services;

import java.io.IOException;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.modules.autoupdate.updateprovider.*;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jiri Rechtacek
 */
public class UpdateUnitProviderImplTest extends NbTestCase {
    
    private static URL URL_TO_TEST_CATALOG = null;
    private static Preferences p = null;
    
    public UpdateUnitProviderImplTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(TestUpdateProvider.class);
        URL_TO_TEST_CATALOG = AutoupdateCatalogParserTest.class.getResource("data/catalog.xml");
        System.out.println("getWorkDirPath (): " + getWorkDirPath());
        System.setProperty("netbeans.user", getWorkDirPath());
        System.out.println("NbPreferences.root (): " + NbPreferences.root());
        p = NbPreferences.root().node("/org/netbeans/modules/autoupdate");
        p.clear();
        String[] childNodes = p.childrenNames();
        for (String childName : childNodes) {
            p.node(childName).removeNode();    
        }        
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        clearWorkDir();
    }
    
    public void testRemoveUpdateUnitProvider() throws Exception {
        testCreateUpdateProvider();
        UpdateUnitProviderFactory factory = UpdateUnitProviderFactory.getDefault();
        List<UpdateUnitProvider> providers =  factory.getUpdateUnitProviders(false);
        int originalSize = providers.size();
        assertTrue(originalSize > 0);
        for (UpdateUnitProvider updateUnitProvider : providers) {
            factory.remove(updateUnitProvider);
            assertEquals(--originalSize, factory.getUpdateUnitProviders(false).size());
        }        
    }
    
    public void testCreateUpdateProviderWithIllegalName () throws Exception {                
        String codeName = "http://wrong.myorg.org/files/updates.xml";
        String displayName = "Update Provider With Illegal Name";
        
        UpdateUnitProvider newProvider = UpdateUnitProviderImpl.createUpdateUnitProvider (codeName, displayName, URL_TO_TEST_CATALOG);
        
        assertNotNull(codeName + " provider found.", newProvider);
        assertEquals("Display name equals.", displayName, newProvider.getDisplayName());
    }
    
    public void testCreateUpdateProvider() throws Exception {                
        String codeName1 = "test-update-provider-1";
        String displayName1 = "1st Update Provider";
        
        String codeName2 = "test-update-provider-2";
        String displayName2 = "2nd Update Provider";
        URL url = URL_TO_TEST_CATALOG;
        
        UpdateUnitProvider result1 = UpdateUnitProviderFactory.getDefault().create(codeName1, displayName1, url);
        assertNotNull(codeName1 + " provider found.", result1);
        assertEquals("Code name equals.", codeName1, result1.getName());
        assertEquals("Display name equals.", displayName1, result1.getDisplayName());
        
        UpdateUnitProvider result2 = UpdateUnitProviderFactory.getDefault().create(codeName2, displayName2, url);
        assertNotNull(codeName2 + " provider found.", result2);
        assertEquals("Code name equals.", codeName2, result2.getName());
        assertEquals("Display name equals.", displayName2, result2.getDisplayName());
        
        assertFalse ("Different objects.", result2.equals (result1));
        
        String[] children = p.childrenNames();
        assertNotNull("Some instances stored.", children);
        assertEquals("Two instances stored.", 2, children.length);
        
        assertEquals("First storage " + codeName1, codeName1, children [0]);
        assertEquals("Second storage " + codeName1, codeName2, children [1]);
        
        UpdateProvider load1 = UpdateUnitProviderImpl.loadProvider(children [0]);
        assertNotNull(children [0] + " loaded.", load1);
        
        UpdateProvider load2 = UpdateUnitProviderImpl.loadProvider(children [1]);
        assertNotNull(children [1] + " loaded.", load2);
        
        assertEquals("Original and loaded are identical " + codeName1, result1.getName(), load1.getName());
        assertEquals("Display name equals.", result1.getDisplayName(), load1.getDisplayName());
        
        assertEquals("Original and loaded are identical " + codeName1, result2.getName(), load2.getName());
        assertEquals("Display name equals.", result2.getDisplayName(), load2.getDisplayName());
    }
    
    public void testGetUpdatesProviders() throws Exception {
        p.clear();
        
        //        List<UpdateProvider> expResult = Collections.emptyList ();
        //        List<UpdateProvider> result = UpdateUnitProviderFactory.getUpdatesProviders ();
        //        assertEquals ("Update Providers are empty at init.", expResult, result);
        List<UpdateUnitProvider> result;
        
        // create new one
        String codeName = "test-update-provider";
        String displayName = "2nd Update Provider";
        URL url = URL_TO_TEST_CATALOG;
        
        UpdateUnitProvider newProvider = UpdateUnitProviderImpl.createUpdateUnitProvider(
            codeName, displayName, url, 
            ProviderCategory.forValue(CATEGORY.COMMUNITY)
        );
        assertNotNull(codeName + " provider found.", newProvider);
        
        result = UpdateUnitProviderImpl.getUpdateUnitProviders(false);
        assertFalse("Update Providers are non empty.", result.isEmpty());
        boolean found = false;
        for (UpdateUnitProvider provider : result) {
            found = found || provider.getName().equals(newProvider.getName());
        }
        assertTrue("New Provider " + newProvider.getName() + " found among Update Providers.", found);
    }
    
    public static class TestUpdateProvider implements UpdateProvider {
        private Map<String, UpdateItem> providers =  new HashMap<String, UpdateItem>();
        public String getName() {
            return "lookup-based-updateProvider";
        }
        
        public String getDisplayName() {
            return "lookupBasedUpdateProvider";
        }
        
        public String getDescription () {
            return null;
        }

        public Map<String, UpdateItem> getUpdateItems() throws IOException {
            return providers;
        }
        
        public boolean refresh(boolean force) throws IOException {
            return true;
        }

        public CATEGORY getCategory() {
            return CATEGORY.COMMUNITY;
        }
    }        
}
