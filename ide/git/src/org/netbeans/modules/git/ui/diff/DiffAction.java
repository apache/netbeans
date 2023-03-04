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

package org.netbeans.modules.git.ui.diff;

import java.io.File;
import org.netbeans.modules.git.ui.actions.GitAction;
import org.netbeans.modules.git.ui.repository.Revision;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.diff.DiffAction", category = "Git")
@ActionRegistration(displayName = "#LBL_DiffAction_Name")
@NbBundle.Messages({
    "LBL_DiffAction_Name=&Diff To HEAD",
    "LBL_DiffAction_PopupName=Diff To HEAD"
})
public class DiffAction extends GitAction {
    private static final String ICON_RESOURCE = "org/netbeans/modules/git/resources/icons/diff.png"; //NOI18N
    
    public DiffAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected void performContextAction (Node[] nodes) {
        VCSContext context = getCurrentContext(nodes);
        diff(context);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }

    public void diff (VCSContext context) {
        diff(context, Revision.HEAD, Revision.LOCAL);
    }

    public void diff (VCSContext context, Revision left, Revision right) {
        String contextName = Utils.getContextDisplayName(context);
        MultiDiffPanelController controller = new MultiDiffPanelController(context, left, right);
        DiffTopComponent tc = new DiffTopComponent(controller);
        controller.setActions(tc);
        tc.setName(NbBundle.getMessage(DiffAction.class, "CTL_DiffPanel_Title", contextName)); //NOI18N
        tc.open();
        tc.requestActive();
    }

    public void diff (File file, Revision rev1, Revision rev2, int requestedRightLine) {
        MultiDiffPanelController controller = new MultiDiffPanelController(file, rev1, rev2, requestedRightLine);
        DiffTopComponent tc = new DiffTopComponent(controller);
        controller.setActions(tc);
        tc.setName(NbBundle.getMessage(DiffAction.class, "CTL_DiffPanel_Title", file.getName())); // NOI18N
        tc.open();
        tc.requestActive();
    }
}
