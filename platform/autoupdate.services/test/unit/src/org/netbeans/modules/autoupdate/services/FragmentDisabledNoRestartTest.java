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
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.UpdateUnit;

/**
 * Checks that a fragment installed on top of already enabled module will
 * cause a restart.
 * 
 * @author sdedic
 */
public class FragmentDisabledNoRestartTest extends FragmentModuleTestBase {
    
    public FragmentDisabledNoRestartTest(String testName) {
        super(testName);
    }

    @Override
    public void testSelf() throws Exception {
        UpdateUnit hostUnit = UpdateManagerImpl.getInstance().getUpdateUnit("org.yourorghere.engine");
        installModule(hostUnit, null);
        
        OperationContainer<OperationSupport> container = OperationContainer.createForDirectDisable ();
        container.add(hostUnit.getInstalled());
        OperationSupport support = container.getSupport ();
        support.doOperation (null);

        UpdateUnit toInstall = UpdateManagerImpl.getInstance().getUpdateUnit(moduleCodeNameBaseForTest());
        
        AtomicReference<OperationSupport.Restarter> restarter = new AtomicReference<>();
        installModuleWithRestart(toInstall, null, restarter);
        
        assertNull ("Module must not cause restart, host is disabled", restarter.get());
    }
    
}
