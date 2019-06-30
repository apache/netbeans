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
package org.netbeans.modules.payara.common.nodes.actions;


import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Ludovic Champenois
 * @author Peter Williams
 */
public class DeployDirectoryAction extends NodeAction {
    
    public DeployDirectoryAction() {
    }
    
    protected boolean enable(Node[] nodes) {
        for(Node node: nodes) {
            DeployDirectoryCookie cookie = node.getCookie(DeployDirectoryCookie.class);
            if (cookie == null) {
                return false;
            }
        }        
        return true;
    }
    
    public String getName() {
        return NbBundle.getMessage(DeployDirectoryAction.class, "LBL_DeployDirAction"); // NOI18N
    }
    
    protected void performAction(Node[] nodes) {
        if(nodes != null && nodes.length > 0) {
            DeployDirectoryCookie deployCookie = 
                    nodes[0].getCookie(DeployDirectoryCookie.class);
            if(deployCookie != null) {
                deployCookie.deployDirectory();
            }
            
            RefreshModulesCookie refreshCookie = 
                    nodes[0].getCookie(RefreshModulesCookie.class);
            if(refreshCookie != null) {
                refreshCookie.refresh();
            }
        }
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
