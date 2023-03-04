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

package org.netbeans.modules.profiler.actions;

import org.openide.util.NbBundle;
import java.awt.event.ActionEvent;
import javax.swing.*;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;
import org.netbeans.modules.profiler.v2.SnapshotsWindow;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;


/**
 * Action to display the Snapshots window.
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "LBL_SnapshotsWindowAction=&Snapshots",
    "HINT_SnapshotsWindowAction=Show Profiler Snapshots Window"
})
@ActionID(category="Profile", id="org.netbeans.modules.profiles.actions.SnapshotsWindowAction")
@ActionRegistration(displayName="#LBL_SnapshotsWindowAction", iconBase="org/netbeans/modules/profiler/impl/icons/takeSnapshot.png")
@ActionReference(path="Menu/Window/Profile", position=99)
public final class SnapshotsWindowAction extends AbstractAction {
    
    public SnapshotsWindowAction() {
        putValue(Action.NAME, Bundle.LBL_SnapshotsWindowAction());
        putValue(Action.SHORT_DESCRIPTION, Bundle.HINT_SnapshotsWindowAction());
        putValue(Action.SMALL_ICON, Icons.getIcon(ProfilerIcons.SNAPSHOT_TAKE));
        putValue("iconBase", Icons.getResource(ProfilerIcons.SNAPSHOT_TAKE));
    }

    
    public void actionPerformed(final ActionEvent e) {
        SnapshotsWindow.instance().showStandalone();
    }
}
