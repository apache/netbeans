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
package org.netbeans.modules.docker.ui.build2;

import org.netbeans.modules.docker.api.DockerInstance;
import org.netbeans.modules.docker.ui.node.StatefulDockerInstance;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;


public final class BuildImageAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        for (Node node : activatedNodes) {
            DockerInstance instance = node.getLookup().lookup(DockerInstance.class);
            if (instance != null) {
                perform(instance);
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        StatefulDockerInstance checked = activatedNodes[0].getLookup().lookup(StatefulDockerInstance.class);
        if (checked == null || !checked.isAvailable()) {
            return false;
        }

        return activatedNodes[0].getLookup().lookup(DockerInstance.class) != null;
    }

    @NbBundle.Messages("LBL_BuildImageAction=Build...")
    @Override
    public String getName() {
        return Bundle.LBL_BuildImageAction();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    private void perform(DockerInstance instance) {
        BuildImageWizard wizard = new BuildImageWizard();
        wizard.setInstance(instance);
        wizard.show();
    }

}
