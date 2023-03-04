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
package org.netbeans.lib.profiler.ui.cpu;

import javax.swing.JLabel;
import org.netbeans.lib.profiler.results.cpu.DiffFlatProfileContainer;
import org.netbeans.lib.profiler.ui.components.table.DiffBarCellRenderer;
import org.netbeans.lib.profiler.ui.components.table.LabelTableCellRenderer;
import org.netbeans.lib.profiler.utils.StringUtils;

/**
 *
 * @author Jiri Sedlacek
 */
public class DiffFlatProfilePanel extends SnapshotFlatProfilePanel {
    
    public DiffFlatProfilePanel(CPUResUserActionsHandler actionsHandler, Boolean sampling) {
        super(actionsHandler, sampling);
    }
    
    
    protected boolean supportsReverseCallGraph() {
        return false;
    }

    protected boolean supportsSubtreeCallGraph() {
        return false;
    }
    
    
    protected Object computeValueAt(int row, int col) {
        long value;
        switch (col) {
            case 0:
                return flatProfileContainer.getMethodNameAtRow(row);
            case 1:
                return (float)flatProfileContainer.getTimeInMcs0AtRow(row);
            case 2:
                value = flatProfileContainer.getTimeInMcs0AtRow(row);
                return (value > 0 ? "+" : "") + StringUtils.mcsTimeToString(value) + " ms"; // NOI18N
            case 3:
                if (collectingTwoTimeStamps) {
                    value = flatProfileContainer.getTimeInMcs1AtRow(row);
                    return (value > 0 ? "+" : "") + StringUtils.mcsTimeToString(value) + " ms"; // NOI18N
                } else {
                    value = flatProfileContainer.getTotalTimeInMcs0AtRow(row);
                    return (value > 0 ? "+" : "") + StringUtils.mcsTimeToString(value) + " ms"; // NOI18N
                }
            case 4:
                if (collectingTwoTimeStamps) {
                    value = flatProfileContainer.getTotalTimeInMcs0AtRow(row);
                    return (value > 0 ? "+" : "") + StringUtils.mcsTimeToString(value) + " ms"; // NOI18N
                } else {
                    value = flatProfileContainer.getNInvocationsAtRow(row);
                    return (value > 0 ? "+" : "") + intFormat.format(value); // NOI18N
                }
            case 5:
                value = flatProfileContainer.getTotalTimeInMcs1AtRow(row);
                return (value > 0 ? "+" : "") + StringUtils.mcsTimeToString(value) + " ms"; // NOI18N
            case 6:
                value = flatProfileContainer.getNInvocationsAtRow(row);
                return (value > 0 ? "+" : "") + intFormat.format(value); // NOI18N
            default:
                return null;
        }
    }
    
    protected void initColumnsData() {
        super.initColumnsData();
        columnRenderers[1] = new DiffBarCellRenderer(0, 0);
        columnRenderers[2] = new LabelTableCellRenderer(JLabel.TRAILING);
    }
    
    protected void obtainResults() {
        super.obtainResults();
        DiffFlatProfileContainer container = (DiffFlatProfileContainer)flatProfileContainer;
        DiffBarCellRenderer renderer = (DiffBarCellRenderer)columnRenderers[1];
        renderer.setMinimum(container.getMinTime());
        renderer.setMaximum(container.getMaxTime());
    }
    
}
