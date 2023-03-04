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

import org.netbeans.lib.profiler.TargetAppRunner;
import org.netbeans.lib.profiler.ui.LiveResultsPanel;
import org.netbeans.lib.profiler.ui.cpu.statistics.StatisticalModule;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.lib.profiler.results.ExportDataDumper;
import org.netbeans.lib.profiler.ui.AppearanceController;


/**
 * A display containing a flat profile (always appears together with CCT)
 *
 * @author Ian Formanek
 */

//public class LiveFlatProfilePanel extends FlatProfilePanel implements LiveResultsPanel {
public class LiveFlatProfilePanel extends JPanel implements LiveResultsPanel {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.lib.profiler.ui.cpu.Bundle"); // NOI18N
    private static final String NO_RESULTS_STRING = messages.getString("LiveFlatProfilePanel_NoResultsString"); // NOI18N
                                                                                                                // -----

    //~ Instance fields ----------------------------------------------------------------------------------------------------------
    
    private CPUResUserActionsHandler actionsHandler = null;
    private Collection statModules = null;
    private CPUSelectionHandler handler = new CPUSelectionHandler() {
        public void methodSelected(int threadId, int methodId, int view) {
            for (Iterator it = statModules.iterator(); it.hasNext();) {
                ((StatisticalModule) it.next()).setSelectedMethodId(methodId);
            }
        }
    };

    private JPanel noResultsPanel = null;
    private JPanel resultsTable = null;
    private LiveFlatProfileCollectorPanel fpCollectorPanel = null;
    private TargetAppRunner runner;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    //  public LiveFlatProfilePanel(TargetAppRunner runner, CPUResUserActionsHandler actionsHandler, DrillDownContext context, List additionalStats) {
    public LiveFlatProfilePanel(TargetAppRunner runner, CPUResUserActionsHandler actionsHandler, Collection additionalStats, boolean sampling) {
        this.actionsHandler = actionsHandler;
        this.runner = runner;

        statModules = additionalStats;

        //    drilldownContext = context;
        //    drilldownContext.update(); // get the latest context state
        initComponents();

        setupFlatCollector(sampling);
    }
    
    public LiveFlatProfilePanel(TargetAppRunner runner, CPUResUserActionsHandler actionsHandler, boolean sampling) {
        this(runner, actionsHandler, Collections.EMPTY_LIST, sampling);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setAdditionalStats(Collection<StatisticalModule> stats) {
        statModules = stats;
    }
    
    public int getSortingColumn() {
        return fpCollectorPanel.getSortingColumn();
    }

    public boolean getSortingOrder() {
        return fpCollectorPanel.getSortingOrder();
    }

    public BufferedImage getViewImage(boolean onlyVisibleArea) {
        return fpCollectorPanel.getViewImage(onlyVisibleArea);
    }

    public String getViewName() {
        return fpCollectorPanel.getViewName();
    }

    public boolean fitsVisibleArea() {
        return fpCollectorPanel.fitsVisibleArea();
    }

    public void handleRemove() {
        fpCollectorPanel.handleRemove();
    }

    /**
     * Called when auto refresh is on and profiling session will finish
     * to give the panel chance to do some cleanup before asynchrounous
     * call to updateLiveResults() will happen.
     *
     * Currently it closes the context menu if open, which would otherwise
     * block updating the results.
     */
    public void handleShutdown() {
        handleRemove();
        fpCollectorPanel.handleShutdown();
    }

    public boolean hasValidDrillDown() {
        return false;
    }

    // --- Save current View action support --------------------------------------
    public boolean hasView() {
        return fpCollectorPanel.hasView();
    }

    public void reset() {
        fpCollectorPanel.reset();
    }

    public boolean supports(int instrumentationType) {
        return fpCollectorPanel.supports(instrumentationType);
    }

    public void updateLiveResults() {
        fpCollectorPanel.updateLiveResults();
    }

    public void exportData(int exportedFileType, ExportDataDumper eDD, String viewName) {
        fpCollectorPanel.exportData(exportedFileType, eDD, viewName);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        AppearanceController.getDefault().customizeLiveFlatProfilePanel(this);

        noResultsPanel = new JPanel();
        noResultsPanel.setLayout(new BorderLayout());
        noResultsPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        final JLabel noResultsLabel = new JLabel(NO_RESULTS_STRING);

        noResultsLabel.setFont(noResultsLabel.getFont().deriveFont(14));
        noResultsLabel.setEnabled(false);
        noResultsPanel.add(noResultsLabel, BorderLayout.NORTH);

        resultsTable = new JPanel(new CardLayout());
        resultsTable.add(noResultsLabel, "NORESULTS"); // NOI18N

        add(resultsTable, BorderLayout.CENTER);
    }

    private void setupFlatCollector(boolean sampling) {
        fpCollectorPanel = new LiveFlatProfileCollectorPanel(runner, actionsHandler, handler, sampling);
        resultsTable.add(fpCollectorPanel, "RESULTS"); // NOI18N
                                                       //    ((CardLayout)resultsTable.getLayout()).show(resultsTable, "NORESULTS");

        ((CardLayout) resultsTable.getLayout()).show(resultsTable, "RESULTS"); // NOI18N
    }
}
