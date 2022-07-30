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

package org.netbeans.modules.tomcat5.optional;

import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.shared.ModuleType;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;
import org.netbeans.modules.tomcat5.deploy.TomcatModule;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import org.netbeans.modules.j2ee.deployment.plugins.spi.TargetModuleIDResolver;

/*
 * TMIDResolver.java
 *
 * @author  nn136682
 */
public class TMIDResolver extends TargetModuleIDResolver {

    TomcatManager tm;

    /** Creates a new instance of UndeploySupport */
    public TMIDResolver(DeploymentManager dm) {
        this.tm = (TomcatManager) dm;
    }
    
    @Override
    public TargetModuleID[] lookupTargetModuleID(java.util.Map queryInfo, Target[] targetList) {
        String contextRoot = (String) queryInfo.get(KEY_CONTEXT_ROOT);
        if (contextRoot == null) {
            return EMPTY_TMID_ARRAY;
        }
        // Tomcat ROOT context path bug hack
        if ("".equals(contextRoot)) { // NOI18N
            contextRoot = "/"; // NOI18N
        }
        ArrayList result = new ArrayList();
        try {
            TargetModuleID[] tmidList = tm.getAvailableModules(ModuleType.WAR, targetList);
            for (int i=0; i<tmidList.length; i++) {
                TomcatModule tm = (TomcatModule) tmidList[i];
                if (contextRoot.equals(tm.getPath())) {
                    result.add(tm);
                }
            }
        } catch(IllegalStateException | TargetException ex) {
            Logger.getLogger(TMIDResolver.class.getName()).log(Level.INFO, null, ex);
        }
        
        return (TargetModuleID[]) result.toArray(new TargetModuleID[0]);
    }
}
