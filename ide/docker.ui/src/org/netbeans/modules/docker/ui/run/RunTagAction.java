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
package org.netbeans.modules.docker.ui.run;

import org.netbeans.modules.docker.api.DockerTag;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Petr Hejl
 */
public class RunTagAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        for (Node node : activatedNodes) {
            DockerTag tag = node.getLookup().lookup(DockerTag.class);
            if (tag != null) {
                perform(tag);
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length > 1) {
            return false;
        }
        for (Node node : activatedNodes) {
            if (node.getLookup().lookup(DockerTag.class) == null) {
                return false;
            }
        }
        return true;
    }

    @NbBundle.Messages("LBL_RunTagAction=Run...")
    @Override
    public String getName() {
        return Bundle.LBL_RunTagAction();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    private void perform(DockerTag tag) {
        RunTagWizard wizard = new RunTagWizard(tag);
        wizard.show();
    }
}
