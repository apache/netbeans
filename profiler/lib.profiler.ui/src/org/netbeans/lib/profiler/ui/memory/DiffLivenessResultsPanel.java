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

package org.netbeans.lib.profiler.ui.memory;

import org.netbeans.lib.profiler.results.memory.LivenessMemoryResultsDiff;
import org.netbeans.lib.profiler.results.memory.LivenessMemoryResultsSnapshot;
import org.netbeans.lib.profiler.ui.components.table.ClassNameTableCellRenderer;
import org.netbeans.lib.profiler.ui.components.table.CustomBarCellRenderer;
import org.netbeans.lib.profiler.ui.components.table.DiffBarCellRenderer;
import org.netbeans.lib.profiler.ui.components.table.LabelTableCellRenderer;
import org.netbeans.lib.profiler.utils.StringUtils;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.profiler.api.GoToSource;


/**
 * This panel displays memory liveness diff.
 *
 * @author Jiri Sedlacek
 */
public class DiffLivenessResultsPanel extends SnapshotLivenessResultsPanel {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.lib.profiler.ui.memory.Bundle"); // NOI18N
    private static final String GO_SOURCE_POPUP_ITEM = messages.getString("SnapshotLivenessResultsPanel_GoSourcePopupItem"); // NOI18N
                                                                                                                             // -----

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private JMenuItem popupShowSource;
    private JPopupMenu popup;
    private LivenessMemoryResultsDiff diff;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public DiffLivenessResultsPanel(LivenessMemoryResultsSnapshot snapshot, MemoryResUserActionsHandler actionsHandler,
                                    int allocTrackEvery) {
        super(snapshot, actionsHandler, allocTrackEvery);
        diff = (LivenessMemoryResultsDiff) snapshot;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == popupShowSource && popupShowSource != null) {
            performDefaultAction(-1);
        }
    }

    protected CustomBarCellRenderer getBarCellRenderer() {
        return new DiffBarCellRenderer(diff.getMinTrackedLiveObjectsSizeDiff(), diff.getMaxTrackedLiveObjectsSizeDiff());
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
        }

        return popup;
    }

    protected Object computeValueAt(int row, int col) {
        int index = (Integer) filteredToFullIndexes.get(row);

        switch (col) {
            case 0:
                return sortedClassNames[index];
            case 1:
                return trackedLiveObjectsSize[index];
            case 2:
                return ((trackedLiveObjectsSize[index] > 0) ? "+" : "") + intFormat.format(trackedLiveObjectsSize[index]) + " B"; // NOI18N
            case 3:
                return ((nTrackedLiveObjects[index] > 0) ? "+" : "") + intFormat.format(nTrackedLiveObjects[index]); // NOI18N
            case 4:
                return ((nTrackedAllocObjects[index] > 0) ? "+" : "") + intFormat.format(nTrackedAllocObjects[index]); // NOI18N
            case 5:
                if (avgObjectAge[index] == 0) return "0.0"; // NOI18N
                // NOTE: StringUtils.floatPerCentToString() doesn't handle correctly negative values!
                else return ((avgObjectAge[index] > 0) ? "+" : "-") + StringUtils.floatPerCentToString(Math.abs(avgObjectAge[index])); // NOI18N
            case 6:
                return ((maxSurvGen[index] > 0) ? "+" : "") + intFormat.format(maxSurvGen[index]); // NOI18N
            case 7:
                return ((nTotalAllocObjects[index] > 0) ? "+" : "") + intFormat.format(nTotalAllocObjects[index]); // NOI18N
            default:
                return null;
        }
    }

    protected void initColumnsData() {
        super.initColumnsData();

        ClassNameTableCellRenderer classNameTableCellRenderer = new ClassNameTableCellRenderer();
        LabelTableCellRenderer labelTableCellRenderer = new LabelTableCellRenderer(JLabel.TRAILING);

        columnRenderers = new TableCellRenderer[] {
                              classNameTableCellRenderer, null, labelTableCellRenderer, labelTableCellRenderer,
                              labelTableCellRenderer, labelTableCellRenderer, labelTableCellRenderer, labelTableCellRenderer
                          };
    }

    protected void initDataUponResultsFetch() {
        super.initDataUponResultsFetch();

        if (barRenderer != null) {
            barRenderer.setMinimum(diff.getMinTrackedLiveObjectsSizeDiff());
            barRenderer.setMaximum(diff.getMaxTrackedLiveObjectsSizeDiff());
        }
    }

    protected boolean passesValueFilter(int i) {
        return true;
    }

    protected void performDefaultAction(int classId) {
        String className = null;
        int selectedRow = resTable.getSelectedRow();

        if (selectedRow != -1) {
            className = (String) resTable.getValueAt(selectedRow, 0).toString().replace("[]", ""); // NOI18N;
        }

        if (className != null) {
            actionsHandler.showSourceForMethod(className, null, null);
        }
    }

    protected boolean truncateZeroItems() {
        return false;
    }
}
