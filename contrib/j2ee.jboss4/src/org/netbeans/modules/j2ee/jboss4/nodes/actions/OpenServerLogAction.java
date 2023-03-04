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

package org.netbeans.modules.j2ee.jboss4.nodes.actions;

import java.io.File;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.ide.JBOutputSupport;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import org.netbeans.modules.j2ee.jboss4.nodes.JBManagerNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.windows.InputOutput;

/**
 *
 * @author Libor Kotouc
 */
public class OpenServerLogAction extends NodeAction {
    
    public OpenServerLogAction() {
    }

    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    protected void performAction(Node[] activatedNodes) {
        for (Node activatedNode : activatedNodes) {
            Object node = activatedNode.getLookup().lookup(JBManagerNode.class);
            
            if (!(node instanceof JBManagerNode)) {
                continue;
            }
            
            JBDeploymentManager dm = ((JBManagerNode)node).getDeploymentManager();
            InputOutput io = UISupport.getServerIO(dm.getUrl());
            if (io != null) {
                io.select();
            }
            
            InstanceProperties ip = dm.getInstanceProperties();
            JBOutputSupport outputSupport = JBOutputSupport.getInstance(ip, false);
            if (outputSupport == null) {
                outputSupport = JBOutputSupport.getInstance(ip, true);
                String serverDir = ip.getProperty(JBPluginProperties.PROPERTY_SERVER_DIR);
                String logFileName = serverDir + File.separator + "log" + File.separator + "server.log" ; // NOI18N
                File logFile = new File(logFileName);
                if (logFile.exists()) {
                    outputSupport.start(io, logFile);
                }                
            }
        }        
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public String getName() {
        return NbBundle.getMessage(OpenServerLogAction.class, "LBL_OpenServerLogAction");
    }

    public boolean asynchronous() {
        return false;
    }
    
}
