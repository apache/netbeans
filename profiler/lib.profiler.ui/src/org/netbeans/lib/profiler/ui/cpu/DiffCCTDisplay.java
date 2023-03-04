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
import org.netbeans.lib.profiler.results.cpu.CPUResultsDiff;
import org.netbeans.lib.profiler.ui.components.table.DiffBarCellRenderer;
import org.netbeans.lib.profiler.ui.components.table.LabelTableCellRenderer;
import org.netbeans.lib.profiler.utils.StringUtils;

/**
 *
 * @author Jiri Sedlacek
 */
public class DiffCCTDisplay extends CCTDisplay {
    
    public DiffCCTDisplay(CPUResUserActionsHandler actionsHandler, Boolean sampling) {
        super(actionsHandler, sampling);
    }
    
    
    protected boolean supportsReverseCallGraph() {
        return false;
    }

    protected boolean supportsSubtreeCallGraph() {
        return false;
    }
    
    
    protected Float getNodeTimeRel(long time, float percent) {
        return (float)time;
    }

    protected String getNodeTime(long time, float percent) {
        return getNodeSecondaryTime(time);
    }

    protected String getNodeSecondaryTime(long time) {
        return (time > 0 ? "+" : "") + StringUtils.mcsTimeToString(time) + " ms"; // NOI18N
    }

    protected String getNodeInvocations(int nCalls) {
        return (nCalls > 0 ? "+" : "") + Integer.valueOf(nCalls).toString(); // NOI18N
    }
    
    protected void initColumnsData() {
        super.initColumnsData();
        columnRenderers[1] = new DiffBarCellRenderer(0, 0);
        columnRenderers[2] = new LabelTableCellRenderer(JLabel.TRAILING);
    }
    
    public void prepareResults() {
        super.prepareResults();
        DiffBarCellRenderer renderer = (DiffBarCellRenderer)columnRenderers[1];
        long bound = ((CPUResultsDiff)snapshot).getBound(currentView);
        renderer.setMinimum(-bound);
        renderer.setMaximum(bound);
    }
    
}
