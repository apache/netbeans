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
import org.netbeans.modules.autoupdate.services.UpdateManagerImpl;
import org.openide.modules.SpecificationVersion;
import org.openide.modules.SpecificationVersion;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Radek Matous
 */
public class UpdateUnitTest extends DefaultTestCase {
    
    public UpdateUnitTest(String testName) {
        super(testName);
    }
    
    public UpdateUnit getUpdateUnit(String codeNameBase) {
        UpdateUnit uu = UpdateManagerImpl.getInstance().getUpdateUnit(codeNameBase);
        assertNotNull(uu);
        return uu;
    }
    public UpdateElement getAvailableUpdate(UpdateUnit updateUnit, int idx) {
        List<UpdateElement> available = updateUnit.getAvailableUpdates();
        assertTrue(available.size() > idx);
        return available.get(idx);
    }
    
    public void testAvailableUpdateSort() {        
        UpdateUnit engineUnit = getUpdateUnit("org.yourorghere.engine");
        assertNull("cannot be installed",engineUnit.getInstalled());
        UpdateElement engineElement = getAvailableUpdate(engineUnit,0);
        assertNotNull(engineElement);
        UpdateElement engineElement1 = getAvailableUpdate(engineUnit,1);
        assertNotNull(engineElement1);
        SpecificationVersion specVer = new SpecificationVersion(engineElement.getSpecificationVersion());        
        SpecificationVersion specVer1 = new SpecificationVersion(engineElement1.getSpecificationVersion());
        assertEquals(1, specVer.compareTo(specVer1));
    }
}
