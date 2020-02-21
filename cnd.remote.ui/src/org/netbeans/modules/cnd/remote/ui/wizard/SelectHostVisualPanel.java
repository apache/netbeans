/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

/*
 * SelectHostVisualPanel.java
 *
 * Created on Sep 14, 2010, 8:28:17 PM
 */

package org.netbeans.modules.cnd.remote.ui.wizard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 *
 */
@SuppressWarnings("rawtypes") // UI editor produces code with tons of rawtypes warnings
final class SelectHostVisualPanel extends javax.swing.JPanel {

    private final SelectHostWizardPanel controller;
    private final boolean allowLocal;
    private final DefaultListModel<ServerRecord> model;
    private final CreateHostVisualPanel1 createHostPanel;
    private final AtomicBoolean setupNewHost;
    private RequestProcessor.Task focusTask;

    private static final String LAST_SELECTED_HOST_KEY = "last-selected-remote-host"; //NOI18N
    private final boolean allowToCreateNewHostDirectly;

    public SelectHostVisualPanel(SelectHostWizardPanel controller, boolean allowLocal,
            CreateHostVisualPanel1 createHostPanel, AtomicBoolean setupNewHost, boolean allowToCreateNewHostDirectly) {
        
        setName(NbBundle.getMessage(SelectHostVisualPanel.class, "SelectHostVisualPanel.title"));
        this.allowToCreateNewHostDirectly = allowToCreateNewHostDirectly;
        this.controller = controller;
        this.setupNewHost = setupNewHost;
        this.allowLocal = allowLocal;
        this.createHostPanel = createHostPanel;
        initComponents();
        newHostPane.add(createHostPanel, BorderLayout.CENTER);
        rbNew.setSelected(setupNewHost.get());
        rbExistent.setSelected(!setupNewHost.get());
        model = new DefaultListModel<>();
        rbNew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                requestFocusInEDT(SelectHostVisualPanel.this.createHostPanel);
            }
        });
        rbExistent.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                requestFocusInEDT(lstDevHosts);
            }
        });
        if (setupNewHost.get()) {
            requestFocusInEDT(SelectHostVisualPanel.this.createHostPanel);
        } else {
            requestFocusInEDT(lstDevHosts);
        }
    }

    private void requestFocusInEDT(final Component c) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                c.requestFocus();
            }
        });
    }

    private boolean setDefaultHostSelection() {
        if (model.size() > 0) {
            ServerRecord defRec = ServerList.getDefaultRecord();
            if (defRec.isRemote()) {
                lstDevHosts.setSelectedValue(defRec, true);
                return true;
            } else {
                String hostKey = NbPreferences.forModule(getClass()).get(LAST_SELECTED_HOST_KEY, null);
                if (hostKey == null) {
                    lstDevHosts.setSelectedIndex(0);
                    return true;
                } else {
                    for (int i = 0; i < model.getSize(); i++) {
                        ServerRecord record = model.get(i);
                        if (hostKey.equals(ExecutionEnvironmentFactory.toUniqueID(record.getExecutionEnvironment()))) {
                            lstDevHosts.setSelectedIndex(i);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void rememberHostSelection() {
        ServerRecord record = (ServerRecord) lstDevHosts.getSelectedValue();
        if (record != null) {
            String hostKey = ExecutionEnvironmentFactory.toUniqueID(record.getExecutionEnvironment());
            NbPreferences.forModule(getClass()).put(LAST_SELECTED_HOST_KEY, hostKey);
        }
    }

    @SuppressWarnings("unchecked")
    private void initServerList() {
        model.clear();
        for (ServerRecord rec : ServerList.getRecords()) {
            if (rec.isRemote() || allowLocal) {
                model.addElement(rec);
            }
        }
        boolean fire = false;
        if (model.isEmpty()) {
            if (! rbNew.isSelected()) {
                rbNew.setSelected(true);
                rbExistent.setSelected(false);
                fire = true;
            }
            rbExistent.setEnabled(false);
        }
        lstDevHosts.setModel(model);
        lstDevHosts.setCellRenderer(new HostListCellRenderer());
        fire = setDefaultHostSelection(); // NB: before adding selection listener
        lstDevHosts.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    fireChange();
                }
            }
        });
        if (fire) {
            fireChange();
        }
    }

    public boolean getSetupNewHost() {
        return rbNew.isSelected();
    }

    private void fireChange() {
        controller.stateChanged(new ChangeEvent(this));
    }


    /** called each time the component is to be displayed (i.e. from readSettings)*/
    /*package*/ void onReadSettings() {
        initServerList();
        // modeChanged() called right from here doesn't work on Mac if called
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                modeChanged();
            }
        });
    }

    /*package*/ void onStoreSettings() {
        rememberHostSelection();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("rawtypes")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup = new javax.swing.ButtonGroup();
        rbExistent = new javax.swing.JRadioButton();
        rbNew = new javax.swing.JRadioButton();
        newHostPane = new javax.swing.JPanel();
        existentHostScroller = new javax.swing.JScrollPane();
        lstDevHosts = new javax.swing.JList();

        setMaximumSize(new java.awt.Dimension(534, 409));
        setPreferredSize(new java.awt.Dimension(534, 409));
        setLayout(new java.awt.GridBagLayout());

        buttonGroup.add(rbExistent);
        org.openide.awt.Mnemonics.setLocalizedText(rbExistent, org.openide.util.NbBundle.getMessage(SelectHostVisualPanel.class, "SelectHostVisualPanel.rbExistent.text")); // NOI18N
        rbExistent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectExistentHostActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(rbExistent, gridBagConstraints);

        buttonGroup.add(rbNew);
        org.openide.awt.Mnemonics.setLocalizedText(rbNew, org.openide.util.NbBundle.getMessage(SelectHostVisualPanel.class, "SelectHostVisualPanel.rbNew.text")); // NOI18N
        rbNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setupNewHostActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(rbNew, gridBagConstraints);

        newHostPane.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.7;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(newHostPane, gridBagConstraints);

        lstDevHosts.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        existentHostScroller.setViewportView(lstDevHosts);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(existentHostScroller, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void selectExistentHostActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectExistentHostActionPerformed
        setupNewHost.set(false);
        modeChanged();
        fireChange();
    }//GEN-LAST:event_selectExistentHostActionPerformed

    private void setupNewHostActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setupNewHostActionPerformed
        setupNewHost.set(true);
        modeChanged();
        fireChange();
    }//GEN-LAST:event_setupNewHostActionPerformed

    private void modeChanged() {
        if (setupNewHost.get()) {
            lstDevHosts.setEnabled(false);
            createHostPanel.setEnabled(true);
        } else {
            lstDevHosts.setEnabled(true);
            createHostPanel.setEnabled(false);
        }
    }

    public boolean isExistent() {
        return rbExistent.isSelected();
    }

    public ExecutionEnvironment getSelectedHost() {
        ExecutionEnvironment execEnv = null;
        if (rbExistent.isSelected()) {
            ServerRecord record = (ServerRecord) lstDevHosts.getSelectedValue();
            execEnv =  (record == null) ? null : record.getExecutionEnvironment();
        } else if (allowToCreateNewHostDirectly && controller.isValid()) {
            execEnv = ExecutionEnvironmentFactory.createNew(createHostPanel.getUser(), createHostPanel.getHostname(), createHostPanel.getPort());
        }
        return execEnv;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JScrollPane existentHostScroller;
    private javax.swing.JList lstDevHosts;
    private javax.swing.JPanel newHostPane;
    private javax.swing.JRadioButton rbExistent;
    private javax.swing.JRadioButton rbNew;
    // End of variables declaration//GEN-END:variables

    void enableControls(boolean enable) {
        rbExistent.setEnabled(enable);
        rbNew.setEnabled(enable);
        lstDevHosts.setEnabled(enable);
        createHostPanel.setEnabled(rbNew.isSelected() ? enable : false);
    }
    // End of variables declaration

    private static final class HostListCellRenderer extends DefaultListCellRenderer {

        @Override
        @SuppressWarnings("rawtypes")
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel out = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                ServerRecord rec = (ServerRecord) value;
                out.setText(rec.getDisplayName());
            }
            return out;
        }
    }
}
