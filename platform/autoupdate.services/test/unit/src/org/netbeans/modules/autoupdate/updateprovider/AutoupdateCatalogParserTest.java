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

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.Map;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.autoupdate.services.Trampoline;
import org.netbeans.modules.autoupdate.services.UpdateUnitProviderImpl;
import org.netbeans.modules.autoupdate.services.Utilities;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.openide.util.Lookup;

/**
 *
 * @author Jiri Rechtacek
 */
public class AutoupdateCatalogParserTest extends NbTestCase {
    static {
        URL.setURLStreamHandlerFactory(Lookup.getDefault().lookup(URLStreamHandlerFactory.class));
    }
    
    public AutoupdateCatalogParserTest (String testName) {
        super (testName);
    }
    
    private URL URL_TO_TEST_CATALOG = null;
    private Map<String, UpdateItem> updateItems;
    private URL BASE = AutoupdateCatalogParserTest.class.getResource ("data");

    @Override
    protected void setUp () throws Exception {
        System.setProperty("netbeans.user", getWorkDirPath());
        URL_TO_TEST_CATALOG = AutoupdateCatalogParserTest.class.getResource ("data/catalog.xml");
        updateItems = AutoupdateCatalogParser.getUpdateItems (URL_TO_TEST_CATALOG, null);
    }
    
    public void testGetItems () {        
        assertNotNull ("UpdateItems found " + URL_TO_TEST_CATALOG, updateItems);
        assertEquals ("4 items found.", 4, updateItems.keySet ().size ());
    }
    
    public void testLicenses () {
        for (UpdateItem item : updateItems.values ()) {
            UpdateItemImpl impl = Trampoline.SPI.impl (item);
            assertTrue ("UpdateItemImpl " + impl + " instanceof ModuleItem.", impl instanceof ModuleItem);
            ModuleItem mi = (ModuleItem) impl;
            assertNotNull (mi + " has license.", mi.getAgreement ());
            assertFalse (mi + " has non-empty license.", mi.getAgreement ().length () == 0);
        }
    }

    public void testLicenseUrlCDDL() {
        UpdateItem item = updateItems.get("org.netbeans.test.license.cddl.url_1.0");
        UpdateItemImpl impl = Trampoline.SPI.impl(item);
        assertTrue("UpdateItemImpl " + impl + " instanceof ModuleItem.", impl instanceof ModuleItem);
        ModuleItem mi = (ModuleItem) impl;
        assertNotNull(mi + " has license.", mi.getAgreement());        
        assertTrue(mi + " has non-cddl license.", mi.getAgreement().startsWith("COMMON DEVELOPMENT AND DISTRIBUTION LICENSE"));
    }
    
    public void testVisiblePlugin () {
        UpdateItem item = updateItems.get ("org.netbeans.test.visible_1.0");
        assertNotNull ("org.netbeans.test.visible_1.0 found", item);
        UpdateItemImpl impl = Trampoline.SPI.impl (item);
        assertNotNull ("Impl org.netbeans.test.visible_1.0 found", item);
        assertTrue ("Impl org.netbeans.test.visible_1.0 instanceof ModuleItem", impl instanceof ModuleItem);
        ModuleItem mi = (ModuleItem) impl;
        assertTrue ("org.netbeans.test.visible_1.0 is visible.", Utilities.isKitModule (mi.getModuleInfo ()));
        assertFalse ("org.netbeans.test.visible is not eager", mi.isEager ());
        assertEquals ("Both has 1.0 spec. version", mi.getSpecificationVersion (), mi.getModuleInfo ().getSpecificationVersion ().toString ());
        assertEquals ("org.netbeans.test.eager is in Debugging category", "Debugging", mi.getCategory ());
    }
    
    public void testHiddenPlugin () {
        UpdateItem item = updateItems.get ("org.netbeans.test.hidden_1.0");
        assertNotNull ("org.netbeans.test.hidden_1.0 found", item);
        UpdateItemImpl impl = Trampoline.SPI.impl (item);
        assertNotNull ("Impl org.netbeans.test.hidden_1.0 found", item);
        assertTrue ("Impl org.netbeans.test.hidden_1.0 instanceof ModuleItem", impl instanceof ModuleItem);
        ModuleItem mi = (ModuleItem) impl;
        assertFalse ("org.netbeans.test.hidden is not eager", mi.isEager ());
        assertFalse ("org.netbeans.test.hidden is hidden.", Utilities.isKitModule (mi.getModuleInfo ()));
        assertEquals ("Both has 1.0 spec. version", mi.getSpecificationVersion (), mi.getModuleInfo ().getSpecificationVersion ().toString ());
        assertEquals ("org.netbeans.test.eager is in Debugging category", "Debugging", mi.getCategory ());
    }
    
    public void testEagerPlugin () {
        UpdateItem item = updateItems.get ("org.netbeans.test.eager_1.0");
        assertNotNull ("org.netbeans.test.eager_1.0 found", item);
        UpdateItemImpl impl = Trampoline.SPI.impl (item);
        assertNotNull ("Impl org.netbeans.test.eager_1.0 found", item);
        assertTrue ("Impl org.netbeans.test.eager_1.0 instanceof ModuleItem", impl instanceof ModuleItem);
        ModuleItem mi = (ModuleItem) impl;
        assertFalse ("org.netbeans.test.eager is hidden.", Utilities.isKitModule (mi.getModuleInfo ()));
        assertTrue ("org.netbeans.test.eager is eager", mi.isEager ());
        assertEquals ("Both has 1.0 spec. version", mi.getSpecificationVersion (), mi.getModuleInfo ().getSpecificationVersion ().toString ());
        assertEquals ("org.netbeans.test.eager is in Base IDE category", "Base IDE", mi.getCategory ());
    }
    
    public void testCatalogNotification () throws IOException {
        UpdateUnitProvider p = UpdateUnitProviderFactory.getDefault ().create ("test-provider", "test-provider", URL_TO_TEST_CATALOG);
        UpdateUnitProviderImpl i = Trampoline.API.impl (p);
        AutoupdateCatalogParser.getUpdateItems (URL_TO_TEST_CATALOG, (AutoupdateCatalogProvider) i.getUpdateProvider ());
        assertTrue (p + " has notification Important thing!", p.getDescription ().indexOf ("Important thing!") != -1);
        assertTrue (p + " has notification with url http://plugins.netbeans.org/tests", p.getDescription ().indexOf ("http://plugins.netbeans.org/tests") != -1);
    }
    
    public void testRelativeUrl () throws URISyntaxException {
        UpdateItem item = updateItems.get ("org.netbeans.test.visible_1.0");
        assertNotNull ("org.netbeans.test.visible_1.0 found", item);
        UpdateItemImpl impl = Trampoline.SPI.impl (item);
        assertNotNull ("Impl org.netbeans.test.visible_1.0 found", item);
        assertTrue ("Impl org.netbeans.test.visible_1.0 instanceof ModuleItem", impl instanceof ModuleItem);
        ModuleItem mi = (ModuleItem) impl;
        assertTrue ("mi.getDistribution () isAbsolute.", mi.getDistribution ().toURI ().isAbsolute ());
        assertTrue (mi.getDistribution () + " starts with " + BASE.toExternalForm (),
                mi.getDistribution ().toExternalForm ().startsWith (BASE.toExternalForm ()));
    }
    
    public void testAbsoluteUrl () throws URISyntaxException {
        UpdateItem item = updateItems.get ("org.netbeans.test.eager_1.0");
        assertNotNull ("org.netbeans.test.eager_1.0 found", item);
        UpdateItemImpl impl = Trampoline.SPI.impl (item);
        assertNotNull ("Impl org.netbeans.test.eager_1.0 found", item);
        assertTrue ("Impl org.netbeans.test.eager_1.0 instanceof ModuleItem", impl instanceof ModuleItem);
        ModuleItem mi = (ModuleItem) impl;
        assertTrue ("mi.getDistribution () isAbsolute.", mi.getDistribution ().toURI ().isAbsolute ());
        assertFalse (mi.getDistribution () + " doesn't start with " + BASE.toExternalForm (),
                mi.getDistribution ().toExternalForm ().startsWith (BASE.toExternalForm ()));
        assertEquals (mi.getDistribution () + " is http://www.netbeans.org/updates/org-netbeans-test-eager.nbm",
                "http://www.netbeans.org/updates/org-netbeans-test-eager.nbm",
                mi.getDistribution ().toExternalForm ());
    }
    
    public void testCatalogDate () {
        UpdateItem item = updateItems.get ("org.netbeans.test.eager_1.0");
        UpdateItemImpl impl = Trampoline.SPI.impl (item);
        ModuleItem mi = (ModuleItem) impl;
        assertEquals ("Eager has own date 2008/01/01", "2008/01/01", mi.getDate ());
        item = updateItems.get ("org.netbeans.test.visible_1.0");
        impl = Trampoline.SPI.impl (item);
        mi = (ModuleItem) impl;
        assertEquals ("Eager has not own date. Give date from catalog. It's 2008/08/08", "2008/08/08", mi.getDate ());
    }
    
    public void testModuleNotification () {
        UpdateItem item = updateItems.get ("org.netbeans.test.hidden_1.0");
        assertNotNull ("org.netbeans.test.hidden_1.0 found", item);
        UpdateItemImpl impl = Trampoline.SPI.impl (item);
        assertNotNull ("Impl org.netbeans.test.hidden_1.0 found", item);
        assertTrue ("Impl org.netbeans.test.hidden_1.0 instanceof ModuleItem", impl instanceof ModuleItem);
        ModuleItem mi = (ModuleItem) impl;
        assertNotNull ("Hidden has own non-null module notification.", mi.getModuleNotification ());
        assertEquals ("Hidden has own module notification.", "Don't play with hidden modules!", mi.getModuleNotification ());
        item = updateItems.get ("org.netbeans.test.visible_1.0");
        impl = Trampoline.SPI.impl (item);
        mi = (ModuleItem) impl;
        assertTrue ("Visible has no module notification.", mi.getModuleNotification () == null);
    }
    
}
