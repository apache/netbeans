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

package org.netbeans.lib.profiler.ui.memory;

import org.netbeans.lib.profiler.results.memory.LivenessMemoryResultsSnapshot;
import org.netbeans.lib.profiler.ui.UIUtils;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.*;
import org.netbeans.modules.profiler.api.GoToSource;


/**
 * This class implements presentation frames for Object Liveness Profiling.
 *
 * @author Misha Dmitriev
 * @author Ian Formanek
 * @author Jiri Sedlacek
 */
public class SnapshotLivenessResultsPanel extends LivenessResultsPanel implements ActionListener {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.lib.profiler.ui.memory.Bundle"); // NOI18N
    private static final String GO_SOURCE_POPUP_ITEM = messages.getString("SnapshotLivenessResultsPanel_GoSourcePopupItem"); // NOI18N
    private static final String STACK_TRACES_POPUP_ITEM = messages.getString("SnapshotLivenessResultsPanel_StackTracesPopupItem"); // NOI18N
                                                                                                                                   // -----

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private JMenuItem popupShowSource;
    private JMenuItem popupShowStacks;
    private JPopupMenu popup;
    private LivenessMemoryResultsSnapshot snapshot;
    private int allocTrackEvery;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public SnapshotLivenessResultsPanel(LivenessMemoryResultsSnapshot snapshot, MemoryResUserActionsHandler actionsHandler,
                                        int allocTrackEvery) {
        super(actionsHandler);
        this.snapshot = snapshot;
        this.allocTrackEvery = allocTrackEvery;

        fetchResultsFromSnapshot();
        //prepareResults();
        initColumnsData();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == popupShowStacks) {
            actionsHandler.showStacksForClass(selectedClassId, getSortingColumn(), getSortingOrder());
        } else if (source == popupShowSource && popupShowSource != null) {
            showSourceForClass(selectedClassId);
        }
    }

    protected String getClassName(int classId) {
        return snapshot.getClassName(classId);
    }

    protected String[] getClassNames() {
        return snapshot.getClassNames();
    }

    protected int getPercentsTracked() {
        return 100 / allocTrackEvery;
    }

    protected JPopupMenu getPopupMenu() {
        if (popup == null) {
            popup = new JPopupMenu();

            if (GoToSource.isAvailable()) {
                Font boldfont = popup.getFont().deriveFont(Font.BOLD);

                popupShowSource = new JMenuItem();
                popupShowSource.setText(GO_SOURCE_POPUP_ITEM);
                popupShowSource.setFont(boldfont);
                popup.add(popupShowSource);
                popupShowSource.addActionListener(this);
            }

            if (snapshot.containsStacks()) {
                if (GoToSource.isAvailable()) popup.addSeparator();
                popupShowStacks = new JMenuItem();
                popupShowStacks.setText(STACK_TRACES_POPUP_ITEM);
                popup.add(popupShowStacks);
                popupShowStacks.addActionListener(this);
            }
        }

        return popup;
    }

    protected void performDefaultAction(int classId) {
        showSourceForClass(classId);
    }

    private void fetchResultsFromSnapshot() {
        nTrackedAllocObjects = UIUtils.copyArray(snapshot.getNTrackedAllocObjects());
        nTrackedLiveObjects = UIUtils.copyArray(snapshot.getNTrackedLiveObjects());
        trackedLiveObjectsSize = UIUtils.copyArray(snapshot.getTrackedLiveObjectsSize());
        nTotalAllocObjects = UIUtils.copyArray(snapshot.getnTotalAllocObjects());
        avgObjectAge = UIUtils.copyArray(snapshot.getAvgObjectAge());
        maxSurvGen = UIUtils.copyArray(snapshot.getMaxSurvGen());
        nInstrClasses = snapshot.getNInstrClasses();

        nTrackedItems = snapshot.getNTrackedItems();
        // Operations necessary for correct bar representation of results
        maxValue = snapshot.getMaxValue();
        nTotalTrackedBytes = snapshot.getNTotalTrackedBytes();
        nTotalTracked = snapshot.getNTotalTracked();

        initDataUponResultsFetch();
    }
}
