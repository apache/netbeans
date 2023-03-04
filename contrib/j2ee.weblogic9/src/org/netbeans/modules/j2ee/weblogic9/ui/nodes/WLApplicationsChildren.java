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

package org.netbeans.modules.j2ee.weblogic9.ui.nodes;

import javax.enterprise.deploy.shared.ModuleType;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;

import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * It describes children nodes of the Applications node
 *
 * @author Michal Mocnak
 * @author Petr Hejl
 */
public class WLApplicationsChildren extends WLNodeChildren {
    
    WLApplicationsChildren(Lookup lookup) {
        WLDeploymentManager manager = lookup.lookup(WLDeploymentManager.class);
        assert manager != null;

        setKeys(new Object[] {
                createEarApplicationsNode(lookup),
                createEjbModulesNode(lookup),
                createWebApplicationsNode(lookup)});
    }
    
    /*
     * Creates an EAR Applications parent node
     */
    public static WLItemNode createEarApplicationsNode(Lookup lookup) {
        return new  WLItemNode(
                new WLModuleChildFactory(lookup, ModuleType.EAR),
                NbBundle.getMessage(WLApplicationsChildren.class, "LBL_EarApps"), ModuleType.EAR);
    }
    
    /*
     * Creates an Web Applications parent node
     */
    public static WLItemNode createWebApplicationsNode(Lookup lookup) {
        return new WLItemNode(
                new WLModuleChildFactory(lookup, ModuleType.WAR),
                NbBundle.getMessage(WLApplicationsChildren.class, "LBL_WebApps"), ModuleType.WAR);
    }
    
    /*
     * Creates an EJB Modules parent node
     */
    public static WLItemNode createEjbModulesNode(Lookup lookup) {
        return new WLItemNode(
                new WLModuleChildFactory(lookup, ModuleType.EJB),
                NbBundle.getMessage(WLApplicationsChildren.class, "LBL_EjbModules"), ModuleType.EJB);
    }


}
