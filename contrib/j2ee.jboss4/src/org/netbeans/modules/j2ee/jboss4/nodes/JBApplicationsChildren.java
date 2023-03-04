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

package org.netbeans.modules.j2ee.jboss4.nodes;

import javax.enterprise.deploy.shared.ModuleType;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * It describes children nodes of the Applications node
 *
 * @author Michal Mocnak
 */
public class JBApplicationsChildren extends Children.Keys {
    
    JBApplicationsChildren(Lookup lookup) {
        setKeys(new Object[] {createEarApplicationsNode(lookup),
                                createEjbModulesNode(lookup),
                                createWebApplicationsNode(lookup)});
    }
    
    protected void addNotify() {
    }
    
    protected void removeNotify() {
    }
    
    protected org.openide.nodes.Node[] createNodes(Object key) {
        if (key instanceof AbstractNode){
            return new Node[]{(AbstractNode)key};
        }
        
        return null;
    }
    
    /*
     * Creates an EAR Applications parent node
     */
    public static JBItemNode createEarApplicationsNode(Lookup lookup) {
        return new  JBItemNode(new JBEarApplicationsChildren(lookup), NbBundle.getMessage(JBTargetNode.class, "LBL_EarApps"), ModuleType.EAR);
    }
    
    /*
     * Creates an Web Applications parent node
     */
    public static JBItemNode createWebApplicationsNode(Lookup lookup) {
        return new JBItemNode(new JBWebApplicationsChildren(lookup), NbBundle.getMessage(JBTargetNode.class, "LBL_WebApps"), ModuleType.WAR);
    }
    
    /*
     * Creates an EJB Modules parent node
     */
    public static JBItemNode createEjbModulesNode(Lookup lookup) {
        return new JBItemNode(new JBEjbModulesChildren(lookup), NbBundle.getMessage(JBTargetNode.class, "LBL_EjbModules"), ModuleType.EJB);
    }
}
