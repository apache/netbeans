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

package org.netbeans.modules.autoupdate.services;

import java.lang.ref.Reference;
import java.util.List;
import org.netbeans.api.autoupdate.DefaultTestCase;
import org.netbeans.api.autoupdate.UpdateUnit;

/**
 *
 * @author Radek Matous
 */
public class UpdateManagerImplTest extends DefaultTestCase {
    
    public UpdateManagerImplTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        keepItNotToGC = null;
    }
            
    public void testNoMemoryLeak() throws Exception {
        List<UpdateUnit> units = UpdateManagerImpl.getInstance().getUpdateUnits();
        assertTrue(units.size() != 0);
        Reference<?> ref = UpdateManagerImpl.getInstance().getCacheReference();
        assertNotNull(ref);
        assertNotNull(ref.get());
        
        units = null;
        assertGC("", ref);        
        assertNotNull(ref);
        assertNull(ref.get());        
        
        units = UpdateManagerImpl.getInstance().getUpdateUnits();
        ref = UpdateManagerImpl.getInstance().getCacheReference();
        assertNotNull(ref);
        assertNotNull(ref.get());
        
        UpdateManagerImpl.getInstance().clearCache();
        ref = UpdateManagerImpl.getInstance().getCacheReference();
        assertNull(ref);        
    }    
}

