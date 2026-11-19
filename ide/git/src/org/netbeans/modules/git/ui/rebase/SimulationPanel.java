/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.git.ui.rebase;

import java.io.CharConversionException;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;
import org.netbeans.libs.git.GitRebaseTodoLine.RebaseAction;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.ui.output.OutputLogger;
import org.openide.awt.Mnemonics;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;

/**
 * The panel simulating the rebase plan settled in the todo table: it
 * replays the plan in a temporary repository (see {@link RebaseSimulator})
 * and reports where the rebase would stop with a conflict, without touching
 * the repository. The simulation stops at the first conflicting commit,
 * like a real rebase, the commits after it are not predicted.
 *
 * <p>The outcome is reported in the panel itself and in the output of the
 * Git console, no dialogs are shown.
 */
@SuppressWarnings("serial")
class SimulationPanel extends JPanel {

    private final File repository;
    private final JTable table;
    private final InteractiveRebasePanel.TodoTableModel model;
    private final DefaultListModel<String> logModel;
    private final RequestProcessor processor = new RequestProcessor("RebaseSimulation", 1); //NOI18N

    private String baseRevision;
    private String tipRevision;
    private int generation;
    private boolean running;
    private boolean hasResult;
    private ProgressMonitor.DefaultProgressMonitor monitor;
    private int simulatedKept;

    SimulationPanel(File repository, JTable table, InteractiveRebasePanel.TodoTableModel model) {
        this.repository = repository;
        this.table = table;
        this.model = model;
        initComponents();
        progressBar.setVisible(false);
        logModel = new DefaultListModel<>();
        logList.setModel(logModel);
        simulateButton.addActionListener(e -> {
            if (running) {
                cancelSimulation();
            } else {
                startSimulation();
            }
        });
        model.addTableModelListener(e -> planChanged());
        addHierarchyListener(e -> {
            if (!isShowing() && running) {
                // the dialog was closed, the result is no longer needed
                cancelSimulation();
            }
        });
    }

    /**
     * The range the plan is settled for: the simulation rebases the commits
     * of the range onto the base, like the real rebase does.
     */
    void setRange(String baseRevision, String tipRevision) {
        this.baseRevision = baseRevision;
        this.tipRevision = tipRevision;
        updateSimulateButton();
    }

    /**
     * The plan was changed: a finished simulation is stale, a running
     * simulation simulates a plan that is no longer the plan of the table.
     */
    private void planChanged() {
        if (running) {
            cancelSimulation();
        } else if (hasResult) {
            resetToIdle();
        }
        updateSimulateButton();
    }

    /**
     * The simulate button is only enabled while a simulation is possible:
     * the range is settled and the plan keeps at least one commit, a plan
     * dropping every commit is refused by the driver.
     */
    private void updateSimulateButton() {
        boolean enabled = !running && baseRevision != null && planHasKeptStep();
        simulateButton.setEnabled(enabled);
    }

    /**
     * Whether the plan keeps at least one commit.
     */
    private boolean planHasKeptStep() {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getAction(i) != RebaseAction.DROP) {
                return true;
            }
        }
        return false;
    }

    private void startSimulation() {
        if (baseRevision == null || tipRevision == null || running || !planHasKeptStep()) {
            return;
        }
        // the number of the commits the plan keeps, for the report of a
        // clean outcome
        simulatedKept = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getAction(i) != RebaseAction.DROP) {
                simulatedKept++;
            }
        }
        int generation = ++this.generation;
        monitor = new ProgressMonitor.DefaultProgressMonitor() {
            @Override
            public void started(String command) {
                addLogLine(generation, command);
            }
            @Override
            public void beginTask(String taskName, int totalWorkUnits) {
                addLogLine(generation, taskName);
            }
        };
        running = true;
        hasResult = false;
        statusLabel.setText(Bundle.LBL_SimulationPanel_running());
        logModel.clear();
        progressBar.setVisible(true);
        simulateButton.setText(Bundle.CTL_SimulationPanel_cancel());
        simulateButton.setEnabled(true);
        RebasePlan plan = model.createPlan();
        processor.submit(() -> {
            RebaseSimulator.Result result = runSimulation(plan, monitor);
            SwingUtilities.invokeLater(() -> finishSimulation(generation, result));
        });
    }

    private void addLogLine(int generation, String line) {
        SwingUtilities.invokeLater(() -> {
            if (running && generation == this.generation) {
                logModel.addElement(line);
            }
        });
    }

    /**
     * The simulation on a background thread, never throws.
     */
    private RebaseSimulator.Result runSimulation(RebasePlan plan, ProgressMonitor.DefaultProgressMonitor monitor) {
        try {
            return RebaseSimulator.simulate(
                    repository,
                    baseRevision,
                    tipRevision,
                    scratch -> new InteractiveRebaseDialog.InteractiveRebaseHandler(scratch, plan),
                    monitor
            );
        } catch (RuntimeException ex) {
            Exceptions.printStackTrace(ex);
            return RebaseSimulator.Result.failed(String.valueOf(ex));
        }
    }

    private void cancelSimulation() {
        generation++;
        if (monitor != null) {
            monitor.cancel();
        }
        resetToIdle();
        statusLabel.setText(Bundle.LBL_SimulationPanel_canceled());
    }

    private void finishSimulation(int generation, RebaseSimulator.Result result) {
        if (generation != this.generation) {
            // a newer simulation or a change of the plan superseded it
            return;
        }
        running = false;
        progressBar.setVisible(false);
        simulateButton.setText(Bundle.CTL_SimulationPanel_simulate());
        switch (result.kind()) {
            case CANCELED -> {
                resetToIdle();
                statusLabel.setText(Bundle.LBL_SimulationPanel_canceled());
            }
            case STOPPED -> {
                int row = rowOf(result.stoppedCommit());
                String shortId = shortenedId(result.stoppedCommit());
                if (row >= 0) {
                    // flag the row the rebase would stop at
                    table.setRowSelectionInterval(row, row);
                }
                hasResult = true;
                statusLabel.setText(Bundle.MSG_SimulationPanel_stopped(shortId, result.conflicts().size()));
                logModel.clear();
                logModel.addAll(result.conflicts());
                OutputLogger.getLogger(repository).outputLine(Bundle.MSG_SimulationPanel_log_stopped(shortId));
                for (String file : result.conflicts()) {
                    OutputLogger.getLogger(repository).outputLine("    " + file);
                }
            }
            case CLEAN -> {
                hasResult = true;
                statusLabel.setText(Bundle.MSG_SimulationPanel_clean(simulatedKept));
                OutputLogger.getLogger(repository).outputLine(Bundle.MSG_SimulationPanel_log_clean());
            }
            case FAILED -> {
                hasResult = true;
                String message = escape(result.message());
                statusLabel.setText(Bundle.MSG_SimulationPanel_error(message));
                OutputLogger.getLogger(repository).outputLine(Bundle.MSG_SimulationPanel_log_error(message));
            }
        }
        updateSimulateButton();
    }

    private void resetToIdle() {
        running = false;
        hasResult = false;
        progressBar.setVisible(false);
        simulateButton.setText(Bundle.CTL_SimulationPanel_simulate());
        statusLabel.setText(Bundle.LBL_SimulationPanel_idle());
        logModel.clear();
    }

    /**
     * The row of the commit with the given id in the todo table, -1 if the
     * table does not contain the commit.
     */
    private int rowOf(String commit) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getRevision(i).getRevision().equals(commit)) {
                return i;
            }
        }
        return -1;
    }

    private static String shortenedId(String id) {
        return id.substring(0, Math.min(7, id.length()));
    }

    /**
     * Escapes the text for inclusion in an <code>&lt;html&gt;</code> label,
     * the errors of the simulation are not under our control.
     */
    private static String escape(String text) {
        try {
            return XMLUtil.toElementContent(text);
        } catch (CharConversionException ex) {
            Logger.getLogger(SimulationPanel.class.getName()).log(Level.INFO, "Can not HTML escape: {0}", text); //NOI18N
            return ""; //NOI18N
        }
    }

    @NbBundle.Messages({
        "CTL_SimulationPanel_simulate=Simulate",
        "CTL_SimulationPanel_cancel=Cancel",
        "TTT_SimulationPanel_simulate=Replay the plan in a temporary repository and report where the rebase would stop with a conflict, without touching the branch",

        "LBL_SimulationPanel_idle=<html>Simulate the rebase before it runs: dry-run conflicts will be displayed below.</html>",
        "LBL_SimulationPanel_running=<html>Simulating the rebase…</html>",
        "LBL_SimulationPanel_canceled=<html>The simulation was canceled.</html>",

        "# {0} - number of the commits kept in the plan",
        "MSG_SimulationPanel_clean=<html>No conflicts: all {0,choice,1#commit|1<commits} of the plan apply cleanly.</html>",
        "# {0} - short id of the stopping commit",
        "# {1} - number of the files in conflict",
        "MSG_SimulationPanel_stopped=<html>The rebase would stop applying {0} with {1,choice,1#a conflict|1<conflicts} in the files listed below.</html>",
        "# {0} - the error of the simulation",
        "MSG_SimulationPanel_error=<html>Simulation failed: {0}</html>",
        "MSG_SimulationPanel_log_clean=Simulated rebase: the plan applies without conflicts.",
        "# {0} - short id of the stopping commit",
        "MSG_SimulationPanel_log_stopped=Simulated rebase: would stop applying {0} with conflicts in:",
        "# {0} - the error of the simulation",
        "MSG_SimulationPanel_log_error=Simulated rebase failed: {0}"
    })
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        simulateButton = new JButton();
        statusLabel = new JLabel();
        progressBar = new JProgressBar();
        logScroll = new JScrollPane();
        logList = new JList<>();

        setBorder(BorderFactory.createEtchedBorder());

        Mnemonics.setLocalizedText(simulateButton, NbBundle.getMessage(SimulationPanel.class, "CTL_SimulationPanel_simulate")); // NOI18N
        simulateButton.setToolTipText(NbBundle.getMessage(SimulationPanel.class, "TTT_SimulationPanel_simulate")); // NOI18N
        simulateButton.setEnabled(false);

        Mnemonics.setLocalizedText(statusLabel, NbBundle.getMessage(SimulationPanel.class, "LBL_SimulationPanel_idle")); // NOI18N

        progressBar.setIndeterminate(true);

        logScroll.setViewportView(logList);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(progressBar, GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(simulateButton))
                    .addComponent(statusLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(logScroll))
                .addContainerGap())
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(logScroll, GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(simulateButton, GroupLayout.Alignment.TRAILING)
                    .addComponent(progressBar, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    JList<String> logList;
    JScrollPane logScroll;
    JProgressBar progressBar;
    JButton simulateButton;
    JLabel statusLabel;
    // End of variables declaration//GEN-END:variables

}
