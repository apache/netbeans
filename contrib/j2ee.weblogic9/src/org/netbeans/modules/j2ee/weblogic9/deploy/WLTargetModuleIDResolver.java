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
package org.netbeans.modules.j2ee.weblogic9.deploy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import org.netbeans.modules.j2ee.deployment.plugins.spi.TargetModuleIDResolver;

/**
 *
 * @author Petr Hejl
 */
public class WLTargetModuleIDResolver extends TargetModuleIDResolver {

    private final WLDeploymentManager dm;

    public WLTargetModuleIDResolver(WLDeploymentManager dm) {
        this.dm = dm;
    }

    @Override
    public TargetModuleID[] lookupTargetModuleID(Map targetModuleInfo, Target[] targetList) {
        String contextRoot = (String) targetModuleInfo.get(KEY_CONTEXT_ROOT);
        if (contextRoot == null) {
            return EMPTY_TMID_ARRAY;
        }
        // WAR modules in EAR contains slash in name
        String noSlashContextRoot = contextRoot;
        if (contextRoot.startsWith("/")) { // NOI18N
            noSlashContextRoot = contextRoot.substring(1);
        }

        ArrayList result = new ArrayList();
        try {
            addCollisions(contextRoot, noSlashContextRoot, result, dm.getAvailableModules(ModuleType.WAR, targetList));
        } catch (Exception ex) {
            Logger.getLogger(WLTargetModuleIDResolver.class.getName()).log(Level.INFO, null, ex);
        }

        return (TargetModuleID[]) result.toArray(new TargetModuleID[0]);
    }

    private void addCollisions(String contextRoot, String noSlashContextRoot, List<TargetModuleID> result, TargetModuleID[] candidates) {
        if (candidates == null) {
            return;
        }
        for (int i = 0; i < candidates.length; i++) {
            TargetModuleID tm = candidates[i];
            if (contextRoot.equals(tm.getModuleID()) || noSlashContextRoot.equals(tm.getModuleID())) {
                TargetModuleID parent = tm.getParentTargetModuleID();
                if (parent != null) {
                    result.add(parent);
                } else {
                    result.add(tm);
                }
            }
        }
    }
}
