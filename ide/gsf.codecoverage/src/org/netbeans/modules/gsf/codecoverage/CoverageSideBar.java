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
package org.netbeans.modules.gsf.codecoverage;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.gsf.codecoverage.api.CoverageProvider;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageDetails;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageSummary;
import org.netbeans.spi.editor.SideBarFactory;
import org.netbeans.spi.project.ActionProvider;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Editor footer for files while in code coverage mode: Show file coverage rate, warnings about
 * files being out of date, and quick buttons for enabling/disabling highlights and clearing
 * results.
 * <p>
 * <b>NOTE</b>: You must compile this module before attempting to open this form in the GUI builder!
 * The design depends on the CoverageBar class and Matisse can only load the form if the .class, not
 * just the .java file, is available!
 *
 * @author Tor Norbye
 */
public class CoverageSideBar extends javax.swing.JPanel {

    private static final String COVERAGE_SIDEBAR_PROP = "coverageSideBar"; // NOI18N
    private static final String COVERAGE_SIDEBAR_FOCUS = "coverageSideBarFocus"; // NOI18N
    private static final String FOCUS_KEY_BINDING = "control shift F11";
    private final FileObject fileForDocument;
    private boolean enabled;

    /**
     * Creates new form CoverageSideBar
     */
    public CoverageSideBar(final JTextComponent target) {
        Document document = target.getDocument();
        fileForDocument = GsfUtilities.findFileObject(document);

        String mimeType = (String) document.getProperty("mimeType"); // NOI18N
        boolean on = false;
        if (mimeType != null) {
            CoverageManagerImpl manager = CoverageManagerImpl.getInstance();
            on = manager.isEnabled(mimeType);
            if (on) {
                CoverageProvider provider = getProvider();
                if (provider != null) {
                    on = provider.isEnabled() && manager.getShowEditorBar();
                } else {
                    on = false;
                }
            }
        }

        if (on) {
            showCoveragePanel(true);
        } else {
            updatePreferredSize();
        }

        Action focus = new AbstractAction(COVERAGE_SIDEBAR_FOCUS) {

            public void actionPerformed(ActionEvent e) {
                CoverageSideBar.this.requestFocusInWindow();
            }
        };
        target.getInputMap().put(KeyStroke.getKeyStroke(FOCUS_KEY_BINDING), COVERAGE_SIDEBAR_FOCUS);
        target.getActionMap().put(COVERAGE_SIDEBAR_FOCUS, focus);
        // Since CoverageSideBar is a component storing its instance in target rather than document
        // to btw allow GC of cloned editor components.
        target.putClientProperty(COVERAGE_SIDEBAR_PROP, this);
    }

    public static CoverageSideBar getSideBar(JTextComponent target) {
        return (CoverageSideBar) target.getClientProperty(COVERAGE_SIDEBAR_PROP);
    }

    public void setCoverage(FileCoverageDetails details) {
        if (details != null) {
            FileCoverageSummary summary = details.getSummary();
            float coverage = summary.getCoveragePercentage();

            if (coverage >= 0.0) {
                coverageBar.setCoveragePercentage(coverage);
            }
            coverageBar.setStats(summary.getLineCount(), summary.getExecutedLineCount(),
                summary.getInferredCount(), summary.getPartialCount());

            long dataModified = details.lastUpdated();
            FileObject fo = details.getFile();
            boolean tooOld = false;
            if (fo != null && dataModified > 0 && dataModified < fo.lastModified().getTime()) {
                tooOld = true;
            } else if (fo != null && fo.isValid()) {
                try {
                    DataObject dobj = DataObject.find(fo);
                    tooOld = dobj.isModified();
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (tooOld) {
                warningsLabel.setText(NbBundle.getMessage(CoverageSideBar.class, "DataTooOld"));
            } else {
                warningsLabel.setText("");
            }
        } else {
            coverageBar.setCoveragePercentage(0.0f);
            coverageBar.setStats(0, 0, 0, 0);
            warningsLabel.setText("");
        }
    }

    public boolean isShowingCoverage() {
        return enabled;
    }

    public void showCoveragePanel(boolean on) {
        if (on == enabled) {
            return;
        }
        this.enabled = on;
        if (on) {
            initComponents();
            setCoverage(null); // hide until we know
        } else {
            removeAll();
        }

        updatePreferredSize();
        revalidate();
        repaint();
    }

    private void updatePreferredSize() {
        if (enabled) {
            // Recompute
            setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            setPreferredSize(null);
            Dimension preferred = getPreferredSize();
            setPreferredSize(preferred);
        } else {
            setPreferredSize(new Dimension(0, 0));
            setMaximumSize(new Dimension(0, 0));
        }
        revalidate();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        label = new JLabel();
        coverageBar = new CoverageBar();
        warningsLabel = new JLabel();
        testButton = new JButton();
        allTestsButton = new JButton();
        clearButton = new JButton();
        reportButton = new JButton();
        jButton1 = new JButton();

        Mnemonics.setLocalizedText(label, NbBundle.getMessage(CoverageSideBar.class, "CoverageSideBar.label.text")); // NOI18N
        label.setToolTipText(NbBundle.getMessage(CoverageSideBar.class, "CoverageSideBar.label.toolTipText")); // NOI18N
        label.setFocusable(false);

        coverageBar.setMinimumSize(new Dimension(40, 10));

        warningsLabel.setForeground(UIManager.getDefaults().getColor("nb.errorForeground"));

        Mnemonics.setLocalizedText(testButton, NbBundle.getMessage(CoverageSideBar.class, "CoverageSideBar.testButton.text")); // NOI18N
        testButton.setEnabled(isActionSupported(ActionProvider.COMMAND_TEST_SINGLE)
        );
        testButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                testOne(evt);
            }
        });

        Mnemonics.setLocalizedText(allTestsButton, NbBundle.getMessage(CoverageSideBar.class, "CoverageSideBar.allTestsButton.text")); // NOI18N
        allTestsButton.setEnabled(isActionSupported(getAllTestAction())
        );
        allTestsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                allTests(evt);
            }
        });

        Mnemonics.setLocalizedText(clearButton, NbBundle.getMessage(CoverageSideBar.class, "CoverageSideBar.clearButton.text")); // NOI18N
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                clearResults(evt);
            }
        });

        Mnemonics.setLocalizedText(reportButton, NbBundle.getMessage(CoverageSideBar.class, "CoverageSideBar.reportButton.text")); // NOI18N
        reportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                report(evt);
            }
        });

        Mnemonics.setLocalizedText(jButton1, NbBundle.getMessage(CoverageSideBar.class, "CoverageSideBar.jButton1.text")); // NOI18N
        jButton1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                done(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(label)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(coverageBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED, 86, Short.MAX_VALUE)
                .addComponent(warningsLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(testButton)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(allTestsButton)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(clearButton)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(reportButton)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jButton1))
        );
        layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                .addComponent(label)
                .addComponent(coverageBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(jButton1)
                .addComponent(reportButton)
                .addComponent(clearButton)
                .addComponent(allTestsButton)
                .addComponent(testButton)
                .addComponent(warningsLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void clearResults(ActionEvent evt) {//GEN-FIRST:event_clearResults
        Project project = getProject();
        if (project != null) {
            CoverageManagerImpl.getInstance().clear(project);
        }
}//GEN-LAST:event_clearResults

    private void allTests(ActionEvent evt) {//GEN-FIRST:event_allTests
        runAction(getAllTestAction());
    }//GEN-LAST:event_allTests

    private void testOne(ActionEvent evt) {//GEN-FIRST:event_testOne
        runAction(ActionProvider.COMMAND_TEST_SINGLE);
    }//GEN-LAST:event_testOne

    private void report(ActionEvent evt) {//GEN-FIRST:event_report
        Project project = getProject();
        if (project != null) {
            CoverageManagerImpl.getInstance().showReport(project);
        }
    }//GEN-LAST:event_report

    private void done(ActionEvent evt) {//GEN-FIRST:event_done
        Project project = getProject();
        if (project != null) {
            CoverageManagerImpl.getInstance().setEnabled(project, false);
        }
        showCoveragePanel(false);
    }//GEN-LAST:event_done

    private void runAction(String action) {
        Project project = getProject();
        if (project != null) {
            Lookup lookup = project.getLookup();
            if (fileForDocument != null) {
                try {
                    DataObject dobj = DataObject.find(fileForDocument);
                    lookup = dobj.getLookup();
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            ActionProvider provider = project.getLookup().lookup(ActionProvider.class);
            if (provider != null) {
                if (provider.isActionEnabled(action, lookup)) {
                    provider.invokeAction(action, lookup);
                }
            }
        }
    }

    private String getAllTestAction() {
        String action = ActionProvider.COMMAND_TEST;
        CoverageProvider provider = getProvider();
        if (provider != null && provider.getTestAllAction() != null) {
            action = provider.getTestAllAction();
        }
        return action;
    }

    private boolean isActionSupported(String action) {
        Project project = getProject();
        if (project != null) {
            ActionProvider provider = project.getLookup().lookup(ActionProvider.class);
            if (provider != null) {
                return Arrays.asList(provider.getSupportedActions()).contains(action);
            }
        }
        return false;
    }

    private Project getProject() {
        if (fileForDocument != null) {
            Project project = FileOwnerQuery.getOwner(fileForDocument);
            return project;
        }

        return null;
    }

    private CoverageProvider getProvider() {
        Project project = getProject();
        if (project != null) {
            return CoverageManagerImpl.getProvider(project);
        }

        return null;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton allTestsButton;
    private JButton clearButton;
    private CoverageBar coverageBar;
    private JButton jButton1;
    private JLabel label;
    private JButton reportButton;
    private JButton testButton;
    private JLabel warningsLabel;
    // End of variables declaration//GEN-END:variables

    public static final class Factory implements SideBarFactory {

        @Override
        public JComponent createSideBar(JTextComponent target) {
            return new CoverageSideBar(target);
        }
    }
}
