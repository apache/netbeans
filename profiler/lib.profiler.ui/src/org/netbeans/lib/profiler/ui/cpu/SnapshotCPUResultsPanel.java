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

import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.results.CCTNode;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot;


/**
 * Superclass providing common support for results displays displaying CPU results backed by CPUResultsSnapshot.
 *
 * @author Ian Formanek
 */
public abstract class SnapshotCPUResultsPanel extends CPUResultsPanel implements CommonConstants, ScreenshotProvider {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    protected CPUResultsSnapshot snapshot;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public SnapshotCPUResultsPanel(CPUResUserActionsHandler actionsHandler, Boolean sampling) {
        super(actionsHandler, sampling);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setDataToDisplay(CPUResultsSnapshot snapshot, int view) {
        this.snapshot = snapshot;
        this.currentView = view;
    }

    public CPUResultsSnapshot getSnapshot() {
        return snapshot;
    }

    /**
     * Gives a hint whether a tab panel is explicitly closeable
     * Subclasses should override the default implementation
     */
    protected boolean isCloseable() {
        return false;
    }

    protected String[] getMethodClassNameAndSig(int methodId, int currentView) {
        return snapshot.getMethodClassNameAndSig(methodId, currentView);
    }

    protected void showReverseCallGraph(int threadId, int methodId, int currentView, int sortingColumn, boolean sortingOrder) {
        actionsHandler.showReverseCallGraph(snapshot, threadId, methodId, currentView, sortingColumn, sortingOrder);
    }

    protected void showSubtreeCallGraph(CCTNode node, int currentView, int sortingColumn, boolean sortingOrder) {
        actionsHandler.showSubtreeCallGraph(snapshot, node, currentView, sortingColumn, sortingOrder);
    }

    protected boolean supportsReverseCallGraph() {
        return true;
    }

    protected boolean supportsSubtreeCallGraph() {
        return true;
    }
}
