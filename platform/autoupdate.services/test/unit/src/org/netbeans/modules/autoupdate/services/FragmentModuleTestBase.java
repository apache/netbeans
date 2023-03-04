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

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.fail;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.modules.autoupdate.updateprovider.InstalledModuleProvider;
import org.openide.modules.ModuleInfo;

/**
 *
 * @author sdedic
 */
public abstract class FragmentModuleTestBase extends OperationsTestImpl {

    public FragmentModuleTestBase(String testName) {
        super(testName);
    }

    protected String moduleCodeNameBaseForTest() {
        return "org.yourorghere.fragment";//NOI18N
    } 
    
    protected Map<String, ModuleInfo> getModuleInfos () {
        return InstalledModuleProvider.getInstalledModules ();
    }
    
    protected UpdateElement installModuleWithRestart(UpdateUnit toInstall, UpdateElement installElement, AtomicReference<OperationSupport.Restarter> restarterOut) throws Exception {
        installElement = (installElement != null) ? installElement : toInstall.getAvailableUpdates ().get (0);
        assertNull (getModuleInfos ().get (toInstall.getCodeName ()));
        assertNotNull (toInstall);

        OperationSupport.Restarter r = null;

        OperationContainer<InstallSupport> container = OperationContainer.createForInstall ();
        OperationContainer.OperationInfo<InstallSupport> info = container.add (installElement);
        assertNotNull (info);
        container.add (info.getRequiredElements ());

        InstallSupport support = container.getSupport ();
        assertNotNull (support);

        InstallSupport.Validator v = support.doDownload (null, false);
        assertNotNull (v);
        InstallSupport.Installer i = support.doValidate (v, null);
        assertNotNull (i);
        assertNull (support.getCertificate (i, installElement)); // Test NBM is not signed nor certificate
        assertFalse (support.isTrusted (i, installElement));
        assertFalse (support.isSigned (i, installElement));
        try {
            r = support.doInstall (i, null);
        } catch (OperationException ex) {
            if (OperationException.ERROR_TYPE.INSTALL == ex.getErrorType ()) {
                // can ingore
                // module system cannot load the module either
            } else {
                fail (ex.toString ());
            }
        }
        if (restarterOut != null) {
            restarterOut.set(r);
        }
        return installElement;
    }
    
    
}
