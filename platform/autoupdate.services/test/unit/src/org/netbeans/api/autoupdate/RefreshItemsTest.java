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

import java.io.IOException;
import java.util.List;
import org.netbeans.modules.autoupdate.services.UpdateManagerImpl;

/**
 * @author Jirka Rechtacek
 */
public class RefreshItemsTest extends DefaultTestCase {
    
    public RefreshItemsTest (String testName) {
        super (testName);
    }
    
    public void testRefreshItems () throws IOException {
        List<UpdateUnitProvider> result = UpdateUnitProviderFactory.getDefault ().getUpdateUnitProviders (false);
        assertEquals(result.toString(), 2, result.size());
        int updateUnitsCount = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE).size();
        
        UpdateUnit toTest = UpdateManagerImpl.getInstance ().getUpdateUnit ("org.yourorghere.refresh_providers_test");
        assertNotNull ("UpdateUnit for org.yourorghere.refresh_providers_test found.", toTest);
        UpdateElement toTestElement = toTest.getAvailableUpdates().get (0);
        assertNotNull ("UpdateElement for org.yourorghere.refresh_providers_test found.", toTestElement);
        assertTrue (toTestElement + " needs restart.", toTestElement.impl.getInstallInfo().needsRestart ());
        
        populateCatalog(TestUtils.class.getResourceAsStream("data/updates-subset.xml"));
        UpdateUnitProviderFactory.getDefault ().refreshProviders(null, true);
        assertEquals(UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE).toString(), 
                updateUnitsCount - 3, UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE).size());
        
        UpdateUnit toTestAgain = UpdateManagerImpl.getInstance ().getUpdateUnit ("org.yourorghere.refresh_providers_test");
        assertNotNull ("Unit for org.yourorghere.refresh_providers_test found.", toTestAgain);
        
        UpdateElement toTestAgainElement = toTestAgain.getAvailableUpdates().get (0);
        assertNotNull ("UpdateElement for org.yourorghere.refresh_providers_test found.", toTestAgainElement);
        
        assertFalse ("First unit is not as same as second unit.", 
                System.identityHashCode(toTest) == System.identityHashCode(toTestAgain));
        assertFalse ("First element is not as same as second element.",
                System.identityHashCode(toTestElement) == System.identityHashCode(toTestAgainElement));
        assertFalse ("IMPLS: First unit is not as same as second unit.", 
                System.identityHashCode(toTest.impl) == System.identityHashCode(toTestAgain.impl));
        assertFalse ("IMPLS: First element is not as same as second element.",
                System.identityHashCode(toTestElement.impl) == System.identityHashCode(toTestAgainElement.impl));
        
        //assertFalse ("First unit is not as same as second unit.", toTest.equals (toTestAgain));
        //assertFalse ("First element is not as same as second element.", toTestElement.equals (toTestAgainElement));
        
        assertFalse (toTestAgainElement + " doesn't need restart now.", toTestAgainElement.impl.getInstallInfo ().needsRestart ());
    }
}
