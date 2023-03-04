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
package org.netbeans.modules.javaee.wildfly.nodes.actions;

import java.io.File;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.javaee.wildfly.WildflyDeploymentManager;
import org.netbeans.modules.javaee.wildfly.ide.WildflyOutputSupport;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginProperties;
import org.netbeans.modules.javaee.wildfly.nodes.WildflyManagerNode;
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

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        for (Node activatedNode : activatedNodes) {
            Object node = activatedNode.getLookup().lookup(WildflyManagerNode.class);

            if (!(node instanceof WildflyManagerNode)) {
                continue;
            }

            WildflyDeploymentManager dm = ((WildflyManagerNode) node).getDeploymentManager();
            InputOutput io = UISupport.getServerIO(dm.getUrl());
            if (io != null) {
                io.select();
            }

            InstanceProperties ip = dm.getInstanceProperties();
            WildflyOutputSupport outputSupport = WildflyOutputSupport.getInstance(ip, false);
            if (outputSupport == null) {
                outputSupport = WildflyOutputSupport.getInstance(ip, true);
                String serverDir = ip.getProperty(WildflyPluginProperties.PROPERTY_SERVER_DIR);
                String logFileName = serverDir + File.separator + "log" + File.separator + "server.log"; // NOI18N
                File logFile = new File(logFileName);
                if (logFile.exists()) {
                    outputSupport.start(io, logFile);
                }
            }
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(OpenServerLogAction.class, "LBL_OpenServerLogAction");
    }

    @Override
    public boolean asynchronous() {
        return false;
    }

}
