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

package org.netbeans.api.autoupdate;

import java.util.List;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.autoupdate.services.UpdateUnitFactoryTest;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogProvider;

/**
 *
 * @author Jiri Rechtacek
 */
public class UpdateProviderFactoryTest extends NbTestCase {
    
    public UpdateProviderFactoryTest (String testName) {
        super (testName);
    }

    @Override
    protected void setUp () throws Exception {
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDirPath());
        MockServices.setServices (MyProvider.class, MyProvider2.class);
    }
    
    @Override
    protected void tearDown () throws  Exception {
    }

    public void testGetUpdatesProviders () {
        List<UpdateUnitProvider> result = UpdateUnitProviderFactory.getDefault ().getUpdateUnitProviders (false);
        
        assertFalse ("Providers found in lookup.", result.isEmpty ());
        assertEquals ("Two providers found.", 2, result.size ());
    }

    public void testSetEnable () {
        List<UpdateUnitProvider> result = UpdateUnitProviderFactory.getDefault ().getUpdateUnitProviders (false);

        UpdateUnitProvider provider = result.get (1);
        boolean state = false;
        provider.setEnable (state);
        
        assertEquals ("New state stored.", state, provider.isEnabled ());

        List<UpdateUnitProvider> resultOnlyEnabled = UpdateUnitProviderFactory.getDefault ().getUpdateUnitProviders (true);
        
        assertFalse ("Providers still found in lookup.", resultOnlyEnabled.isEmpty ());
        assertEquals ("Only one enable provider found.", 1, resultOnlyEnabled.size ());
        assertTrue ("Provider in only enabled must be enabled.", resultOnlyEnabled.get (0).isEnabled ());
    }

    public static class MyProvider extends AutoupdateCatalogProvider {
        public MyProvider () {
            super ("test-updates-provider", "test-updates-provider", UpdateUnitFactoryTest.class.getResource ("data/catalog.xml"));
        }
    }
    
    public static class MyProvider2 extends AutoupdateCatalogProvider {
        public MyProvider2 () {
            super ("test-updates-provider-2", "test-updates-provider-2", UpdateUnitFactoryTest.class.getResource ("data/catalog.xml"));
        }
    }
}
