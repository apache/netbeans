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

/**
 * @author Radek Matous
 */
public class RefreshProvidersTest extends DefaultTestCase {
    
    public RefreshProvidersTest (String testName) {
        super (testName);
    }
    
    public void testRefreshProviders () throws IOException {
        List<UpdateUnitProvider> result = UpdateUnitProviderFactory.getDefault ().getUpdateUnitProviders (false);
        assertEquals(result.toString(), 2, result.size());
       
        int updateUnitsCount = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE).size();
        populateCatalog(TestUtils.class.getResourceAsStream("data/updates-subset.xml"));
        UpdateUnitProviderFactory.getDefault ().refreshProviders(null, true);
        assertEquals(UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE).toString(), 
                updateUnitsCount - 3, UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE).size());
    }

}
