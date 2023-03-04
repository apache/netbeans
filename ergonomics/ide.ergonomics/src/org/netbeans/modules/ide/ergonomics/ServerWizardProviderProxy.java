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

package org.netbeans.modules.ide.ergonomics;

import org.netbeans.modules.ide.ergonomics.newproject.FeatureOnDemandWizardIterator;
import org.netbeans.spi.server.ServerWizardProvider;
import org.openide.WizardDescriptor.InstantiatingIterator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Pavel Flaska
 */
public class ServerWizardProviderProxy implements ServerWizardProvider {

    private FileObject fob;
    private String displayName;

    public static ServerWizardProvider create(FileObject fob) {
        return new ServerWizardProviderProxy(fob);
    }

    private ServerWizardProviderProxy(FileObject fob) {
        this.fob = fob;
    }
    
    @Override
    public String getDisplayName() {
        if (originalActive()) {
            return null;
        }
        if (displayName == null) {
            displayName = (String) fob.getAttribute("displayName"); // NOI18N
            if (displayName == null) { // attribute not available
                displayName = fob.getName();
            }
        }
        return displayName;
    }

    public static boolean isReal(ServerWizardProvider swp) {
        return swp != null && !(swp instanceof ServerWizardProviderProxy);
    }

    @Override
    public InstantiatingIterator getInstantiatingIterator() {
        if (originalActive()) {
            return null;
        }
        return new FeatureOnDemandWizardIterator(fob);
    }

    private boolean originalActive() {
        Object orig = fob.getAttribute("originalDefinition"); // NOI18N
        if (orig instanceof String) {
            if (FileUtil.getConfigFile((String) orig) != null) {
                return true;
            }
        }
        return false;
    }

}
