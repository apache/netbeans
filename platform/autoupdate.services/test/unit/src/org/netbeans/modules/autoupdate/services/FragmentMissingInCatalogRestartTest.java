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

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.TestUtils;
import org.netbeans.api.autoupdate.UpdateUnit;

/**
 * Checks that a fragment on top of enabled module causes restart although
 * the catalog XML does not list its Fragment-Host: attribute. 
 * 
 * @author sdedic
 */
public class FragmentMissingInCatalogRestartTest extends FragmentModuleTestBase {
    
    public FragmentMissingInCatalogRestartTest(String testName) {
        super(testName);
    }

    protected InputStream updateCatalogContents() {
        return TestUtils.class.getResourceAsStream("data/updates-bad-fragment.xml");
    }
    
    @Override
    public void testSelf() throws Exception {
        installModule(UpdateManagerImpl.getInstance().getUpdateUnit("org.yourorghere.engine"), null);

        UpdateUnit toInstall = UpdateManagerImpl.getInstance().getUpdateUnit(moduleCodeNameBaseForTest());
        
        AtomicReference<OperationSupport.Restarter> restarter = new AtomicReference<>();
        installModuleWithRestart(toInstall, null, restarter);
        assertNotNull(restarter.get());
    }
    
}
