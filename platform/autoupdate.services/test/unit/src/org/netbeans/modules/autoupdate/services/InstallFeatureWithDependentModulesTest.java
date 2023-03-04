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

import java.util.Set;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstallFeatureWithDependentModulesTest extends OperationsTestImpl {
    public InstallFeatureWithDependentModulesTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp () throws Exception {
        modulesOnly = false;
        super.setUp ();
    }
    
    protected String moduleCodeNameBaseForTest() {
        return "feature-depending-on-engine";//NOI18N
    }     

    public void testSelf() throws Exception {
        UpdateUnit toInstall = UpdateManagerImpl.getInstance ().getUpdateUnit (moduleCodeNameBaseForTest ());
        // XXX installModule (toInstall);
    }
    
    public UpdateElement installModule(UpdateUnit toInstall) throws Exception {
        assertNotNull ("I have to have something toInstall.", toInstall);
        assertNull ("... and no installed.", toInstall.getInstalled ());
        assertNotNull ("... and some available updates.", toInstall.getAvailableUpdates ());
        assertFalse ("... and some available updates are not empty.", toInstall.getAvailableUpdates ().isEmpty ());
        UpdateElement toInstallElement = toInstall.getAvailableUpdates ().get (0);
        OperationContainer<InstallSupport> container = OperationContainer.createForInstall ();
        OperationInfo<InstallSupport> info = container.add (toInstallElement);
        assertNotNull ("OperationInfo for element " + toInstallElement, info);
        Set<UpdateElement> reqs = info.getRequiredElements ();
        assertNotNull ("getRequiredElements() cannot returns null.", reqs);
        assertFalse ("Something missing", reqs.isEmpty ());
        return super.installModule(toInstall, toInstallElement);
    }

    
}

