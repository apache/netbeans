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

package org.netbeans.modules.profiler.heapwalk;

import org.netbeans.modules.profiler.heapwalk.ui.SummaryControllerUI;
import javax.swing.AbstractButton;
import javax.swing.JPanel;


/**
 *
 * @author Jiri Sedlacek
 * @author Tomas Hurka
 */
public class SummaryController extends AbstractTopLevelController implements NavigationHistoryManager.NavigationHistoryCapable {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private HeapFragmentWalker heapFragmentWalker;
    private HintsController hintsController;
    private OverviewController overviewController;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    // --- Constructors ----------------------------------------------------------
    public SummaryController(HeapFragmentWalker heapFragmentWalker) {
        this.heapFragmentWalker = heapFragmentWalker;
        hintsController = new HintsController(this);
        overviewController = new OverviewController(this);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    // --- NavigationHistoryManager.NavigationHistoryCapable implementation ------
    public NavigationHistoryManager.Configuration getCurrentConfiguration() {
        return new NavigationHistoryManager.Configuration();
    }

    // --- Public interface ------------------------------------------------------
    public HeapFragmentWalker getHeapFragmentWalker() {
        return heapFragmentWalker;
    }

    public void configure(NavigationHistoryManager.Configuration configuration) {
        heapFragmentWalker.switchToHistorySummaryView();
    }

    protected AbstractButton[] createClientPresenters() {
        return new AbstractButton[] {overviewController.getPresenter(),hintsController.getPresenter()};
    }

    protected AbstractButton createControllerPresenter() {
        return ((SummaryControllerUI) getPanel()).getPresenter();
    }

    // --- Protected implementation ----------------------------------------------
    protected JPanel createControllerUI() {
        return new SummaryControllerUI(this);
    }
    
    public HintsController getHintsController() {
        return hintsController;
    }
    
    public OverviewController getOverViewController() {
        return overviewController;
    }
}
