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
import javax.swing.*;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.ui.NBSwingWorker;

@NbBundle.Messages({
    "SaveSnapshotAction_ActionName=Save Snapshot",
    "SaveSnapshotAction_ActionDescr=Save Snapshot to Project"
})
class SaveSnapshotAction extends AbstractAction {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final LoadedSnapshot snapshot;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public SaveSnapshotAction(LoadedSnapshot snapshot) {
        putValue(Action.NAME, Bundle.SaveSnapshotAction_ActionName());
        putValue(Action.SHORT_DESCRIPTION, Bundle.SaveSnapshotAction_ActionDescr());
        putValue(Action.SMALL_ICON, Icons.getIcon(GeneralIcons.SAVE));
        putValue("iconBase", Icons.getResource(GeneralIcons.SAVE)); // NOI18N
        this.snapshot = snapshot;
        updateState();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void actionPerformed(ActionEvent e) {
        new NBSwingWorker() {
            private final ProgressHandle ph = ProgressHandle.createHandle(Bundle.MSG_SavingSnapshot());
            @Override
            protected void doInBackground() {
                ph.setInitialDelay(500);
                ph.start();
                ResultsManager.getDefault().saveSnapshot(snapshot);
            }

            @Override
            protected void done() {
                ph.finish();
                updateState();
            }
        }.execute();
    }

    public void updateState() {
        setEnabled(!snapshot.isSaved());
    }
}
