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

import java.util.List;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.TestUtils;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.autoupdate.updateprovider.NativeComponentItem;
import org.netbeans.modules.autoupdate.updateprovider.UpdateItemImpl;
import org.netbeans.spi.autoupdate.CustomInstaller;
import org.netbeans.spi.autoupdate.UpdateItem;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstallCustomInstalledTest extends OperationsTestImpl {
    public InstallCustomInstalledTest(String testName) {
        super(testName);
    }
    
    protected String moduleCodeNameBaseForTest () {
        return "hello-installer";
    }
    
    @Override
    public void setUp () throws Exception {
        super.setUp ();
        TestUtils.setCustomInstaller (installer);
    }
    
    public void testSelf () throws Exception {
        List<UpdateUnit> units = UpdateManager.getDefault ().getUpdateUnits (UpdateManager.TYPE.CUSTOM_HANDLED_COMPONENT);
        assertNotNull (units);
        assertFalse (units.isEmpty ());
        UpdateUnit toInstall = UpdateManagerImpl.getInstance ().getUpdateUnit (moduleCodeNameBaseForTest ());
        assertFalse (toInstall + " has available elements.", toInstall.getAvailableUpdates ().isEmpty ());
        UpdateElement toInstallElement = toInstall.getAvailableUpdates ().get (0);
        installNativeComponent (toInstall, toInstallElement);
        assertTrue ("Custom installer was called.", installerCalled);
    }
    
    private boolean installerCalled = false;
    
    private CustomInstaller installer = new CustomInstaller () {
        public boolean install (String codeName, String specificationVersion, ProgressHandle handle) throws OperationException {
            UpdateItem exp = TestUtils.getUpdateItemWithCustomInstaller ();
            UpdateItemImpl impl = Trampoline.SPI.impl (exp);
            assertTrue ("Get instanceOf NativeComponentItem", impl instanceof NativeComponentItem);
            NativeComponentItem nativeImpl = (NativeComponentItem) impl;
            assertNotNull ("Code name is not null.", codeName);
            assertNotNull ("SpecificationVersion is not null.", specificationVersion);
            assertEquals ("Was called with as same codeName as excepted.", impl.getCodeName (), codeName);
            assertEquals ("Was called with as same specificationVersion as excepted.", nativeImpl.getSpecificationVersion (), specificationVersion);
            installerCalled = true;
            return true;
        }
    };
    
}
