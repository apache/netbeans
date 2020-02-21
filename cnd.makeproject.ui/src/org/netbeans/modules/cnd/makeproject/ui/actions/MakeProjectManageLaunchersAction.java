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
package org.netbeans.modules.cnd.makeproject.ui.actions;

import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.ui.launchers.actions.ManageLaunchers;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 */
public class MakeProjectManageLaunchersAction  extends NodeAction {

    @Override
    public String getName() {
        return getString("LBL_ManageLaunchersAction_Name"); // NOI18N
    }

    @Override
    public void performAction(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return;
        }
        Node n = activatedNodes[0];
        Project project = (Project) n.getValue("Project"); // NOI18N
        if (project == null) {
            project = activatedNodes[0].getLookup().lookup(Project.class);
        }
        if (project == null) {
            return;
        }

        ManageLaunchers.invoke(project);
    }

    @Override
    public boolean enable(Node[] activatedNodes) {
        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    private String getString(String s) {
        return NbBundle.getMessage(getClass(), s);
    }
}
