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

package org.netbeans.modules.profiler;

import org.openide.util.NbBundle;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;

@NbBundle.Messages({
    "SnapshotInfoAction_ActionName=Snapshot information",
    "SnapshotInfoAction_ActionDescr=Display snapshot information",
    "SnapshotInfoAction_WindowCaption=Snapshot Information"
})
class SnapshotInfoAction extends AbstractAction {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final LoadedSnapshot snapshot;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public SnapshotInfoAction(LoadedSnapshot snapshot) {
        putValue(Action.NAME, Bundle.SnapshotInfoAction_ActionName());
        putValue(Action.SHORT_DESCRIPTION, Bundle.SnapshotInfoAction_ActionDescr());
        putValue(Action.SMALL_ICON, Icons.getIcon(GeneralIcons.INFO));
        putValue("iconBase", Icons.getResource(GeneralIcons.INFO)); // NOI18N
        this.snapshot = snapshot;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void actionPerformed(ActionEvent e) {
        HelpCtx helpCtx = new HelpCtx("SnapshotInfo.HelpCtx"); // NOI18N
        DialogDescriptor dd = new DialogDescriptor(new SnapshotInfoPanel(snapshot),
                              Bundle.SnapshotInfoAction_WindowCaption(), true,
                              new Object[] { DialogDescriptor.OK_OPTION }, 
                              DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN,
                              helpCtx, null);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
    }
}
