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
package org.netbeans.modules.cnd.remote.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.remote.ServerUpdateCache;
import org.netbeans.modules.cnd.api.toolchain.ui.Save;
import org.netbeans.modules.cnd.remote.server.RemoteServerRecord;
import org.netbeans.modules.cnd.remote.ui.setup.CreateHostWizardIterator;
import org.netbeans.modules.cnd.api.toolchain.ToolsCacheManager;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Mange the removeServer development hosts list.
 * 
 */
@SuppressWarnings("rawtypes") // UI editor produces code with tons of rawtypes warnings
public class EditServerListDialog extends JPanel implements ActionListener, PropertyChangeListener, ListSelectionListener, Save {

    private DefaultListModel<ServerRecord> model;
    private DialogDescriptor desc;
    private ServerRecord defaultRecord;
    private ProgressHandle phandle;
    private PropertyChangeSupport pcs;
    private boolean buttonsEnabled;
    private final ToolsCacheManager cacheManager;
    private final AtomicReference<ExecutionEnvironment> selectedEnv;
    private final boolean isRemoveAvaliable;

    private static final String CMD_ADD = "Add"; // NOI18N
    private static final String CMD_REMOVE = "Remove"; // NOI18N
    private static final String CMD_DEFAULT = "SetAsDefault"; // NOI18N
    private static final String CMD_PATHMAPPER = "PathMapper"; // NOI18N
    private static final String CMD_PROPERTIES = "Properties"; // NOI18N
    private static final String CMD_RETRY = "Retry"; // NOI18N
    
    private static final RequestProcessor RP = new RequestProcessor("EditServerListDialog", 1); // NOI18N

    public EditServerListDialog(ToolsCacheManager cacheManager, AtomicReference<ExecutionEnvironment> selectedEnv) {
        this(cacheManager,selectedEnv, true);
    }

    public EditServerListDialog(ToolsCacheManager cacheManager, AtomicReference<ExecutionEnvironment> selectedEnv, boolean removeAvaliable) {
        this.cacheManager = cacheManager;
        initComponents();
        initServerList(cacheManager.getServerUpdateCache());
        desc = null;
        //lbReason.setText(" "); // NOI18N - this keeps the dialog from resizing
        tfReason.setEnabled(false); // setVisible(false);
        pbarStatusPanel.setVisible(false);
        this.selectedEnv = selectedEnv;
        isRemoveAvaliable = removeAvaliable;
        initListeners();
    }


    private void initListeners() {
        lstDevHosts.addListSelectionListener(this);
        btAddServer.addActionListener(this);
        if (isRemoveAvaliable) {
            btRemoveServer.addActionListener(this);
            btRemoveServer.setActionCommand(CMD_REMOVE);
        } else {
            btRemoveServer.setEnabled(false);
            btRemoveServer.setVisible(false);
        }
        btSetAsDefault.addActionListener(this);
        btPathMapper.addActionListener(this);
        btProperties.addActionListener(this);
        btRetry.addActionListener(this);

        btAddServer.setActionCommand(CMD_ADD);
        btSetAsDefault.setActionCommand(CMD_DEFAULT);
        btPathMapper.setActionCommand(CMD_PATHMAPPER);
        btProperties.setActionCommand(CMD_PROPERTIES);
        btRetry.setActionCommand(CMD_RETRY);

        pcs = new PropertyChangeSupport(this);
        pcs.addPropertyChangeListener(this);
        setButtons(true);
        valueChanged(null);
    }

    @SuppressWarnings("unchecked")
    private void initServerList(ServerUpdateCache cache) {
        model = new DefaultListModel<>();

        if (cache == null) {
            for (ServerRecord rec : ServerList.getRecords()) {
                model.addElement(rec);
            }
            defaultRecord = ServerList.getDefaultRecord();
        } else {
            for (ServerRecord rec : cache.getHosts()) {
                model.addElement(rec);
            }
            defaultRecord = cache.getDefaultRecord();
            if (defaultRecord == null) {
                defaultRecord = ServerList.getDefaultRecord();
            }
        }
        lstDevHosts.setModel(model);
        lstDevHosts.setSelectedValue(defaultRecord, false);
        lstDevHosts.setCellRenderer(new MyCellRenderer());
    }

    private void checkDefaultButton(RemoteServerRecord record) {
        final ExecutionEnvironment env = record.getExecutionEnvironment();
        // make fast checks just in the UI thread, in which we are called
        if (record.equals(defaultRecord)) {
            btSetAsDefault.setEnabled(false);
        } else if (!buttonsEnabled) {
            btSetAsDefault.setEnabled(false);
        } else if (env.isLocal()) {
            btSetAsDefault.setEnabled(true);
        } else {
            // need to check remote tool chains in a separate thread
            btSetAsDefault.setEnabled(false);
            final Runnable enabler = new Runnable() {
                @Override
                public void run() {
                    // check if it has already changed
                    ServerRecord curr = (ServerRecord) lstDevHosts.getSelectedValue();
                    if (curr != null && curr.getExecutionEnvironment().equals(env)) {
                        btSetAsDefault.setEnabled(true);
                    }
                }
            };
            Runnable checker = new Runnable() {
                @Override
                public void run() {
                    CompilerSetManager compilerSetManagerCopy = cacheManager.getCompilerSetManagerCopy(env, false);
                    if (!compilerSetManagerCopy.isEmpty()) {
                        SwingUtilities.invokeLater(enabler);
                    }
                }
            };
            RP.post(checker);
        }
    }

    private void revalidateRecord(final RemoteServerRecord record, String password, boolean rememberPassword) {
        if (!record.isOnline()) {
            record.resetOfflineState(); // this is a do-over
            setButtons(false);
            hideReason();
            phandle = ProgressHandle.createHandle("");
            pbarStatusPanel.removeAll();
            pbarStatusPanel.add(ProgressHandleFactory.createProgressComponent(phandle), BorderLayout.CENTER);
            pbarStatusPanel.setVisible(true);
//            revalidate();
            phandle.start();
            // TODO: not good to use object's toString as resource key
            tfStatus.setText(NbBundle.getMessage(RemoteServerRecord.class, RemoteServerRecord.State.INITIALIZING.toString()));
            // move expensive operation out of EDT
            RP.post(new Runnable() {

                @Override
                public void run() {
                    record.init(pcs);
                    if (record.isOnline()) {
                        CompilerSetManager csm = cacheManager.getCompilerSetManagerCopy(record.getExecutionEnvironment(), false);
                        csm.initialize(false, true, null);
                    }
                    phandle.finish();
                    // back to EDT to work with Swing
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            pbarStatusPanel.setVisible(false);
                            setButtons(true);
                            valueChanged(null);
                        }
                    });
                }
            });
        }
    }

    private final class MyCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel out = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                ServerRecord rec = (ServerRecord) value;
                out.setText(rec.getDisplayName());
                if (rec.equals(getDefaultRecord())) {
                    out.setFont(out.getFont().deriveFont(Font.BOLD));
                }
            }
            return out;
        }
    }

    public void setDialogDescriptor(DialogDescriptor desc) {
        this.desc = desc;
    }

    public List<ServerRecord> getHosts() {
        List<ServerRecord> result = new ArrayList<>(model.getSize());
        for (int i = 0; i < model.getSize(); i++) {
            result.add(model.get(i));
        }
        return result;
    }

    public ServerRecord getDefaultRecord() {
        return defaultRecord;
    }

    private void setButtons(boolean enable) {
        buttonsEnabled = enable;
        if (desc != null) {
            desc.setValid(enable);
        }
        btAddServer.setEnabled(enable);
        btAddServer.setEnabled(enable);
        if (isRemoveAvaliable) {
            btRemoveServer.setEnabled(enable);
        }
        btPathMapper.setEnabled(enable);
        btSetAsDefault.setEnabled(enable);
        btRetry.setEnabled(enable);
        btProperties.setEnabled(enable);
    }

    /** Helps the AddServerDialog know when to enable/disable the OK button */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Object source = evt.getSource();
        String prop = evt.getPropertyName();
        if (source instanceof DialogDescriptor && prop.equals(DialogDescriptor.PROP_VALID)) {
            ((DialogDescriptor) source).setValid(false);
        }
    }

    /** Enable/disable the Remove and Set As Default buttons */
    @Override
    public void valueChanged(ListSelectionEvent evt) {
        RemoteServerRecord record = (RemoteServerRecord) lstDevHosts.getSelectedValue();
        if (record != null) {
            tfStatus.setText(record.getStateAsText());
            btRemoveServer.setEnabled(record.isRemote() && buttonsEnabled && isRemoveAvaliable);
            checkDefaultButton(record);
            btProperties.setEnabled(record.isRemote());
            btPathMapper.setEnabled(buttonsEnabled && record.isRemote() && record.getSyncFactory().isPathMappingCustomizable());
            if (!record.isOnline()) {
                showReason(record.getReason());
                btRetry.setEnabled(true);
            } else {
                hideReason();
                btRetry.setEnabled(false);
            }
            if (selectedEnv != null) {
                selectedEnv.set(record.getExecutionEnvironment());
            }
        } else {
            RemoteUtil.LOGGER.warning("ESLD.valueChanged: No selection in Dev Hosts list");
            if (selectedEnv != null) {
                selectedEnv.set(null);
            }
        }
    }

    private void showReason(String reason) {
        tfReason.setText(reason);
        tfReason.setEnabled(true); // setVisible(true);
    }

    private void hideReason() {
        //lbReason.setText(" "); // NOI18N
        tfReason.setEnabled(false); // setVisible(false);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        Object o = evt.getSource();

        if (o instanceof JButton) {
            JButton b = (JButton) o;
            if (b.getActionCommand().equals(CMD_ADD)) {
                cacheManager.setHosts(getHosts());
                ServerRecord result = CreateHostWizardIterator.invokeMe(cacheManager);
                if (result != null) {
                    if (!model.contains(result)) {
                        model.addElement(result);
                        lstDevHosts.setSelectedValue(result, true);
                    }
                }
            } else if (b.getActionCommand().equals(CMD_REMOVE)) {
                ServerRecord rec2delete = (ServerRecord) lstDevHosts.getSelectedValue();
                int idx = lstDevHosts.getSelectedIndex();
                if (rec2delete != null) {
                    model.removeElement(rec2delete);
                    lstDevHosts.setSelectedIndex(model.size() > idx ? idx : idx - 1);
                    if (defaultRecord.equals(rec2delete)) {
                        defaultRecord = (ServerRecord) lstDevHosts.getSelectedValue();
                    }
                }
                lstDevHosts.repaint();

            } else if (b.getActionCommand().equals(CMD_DEFAULT)) {
                defaultRecord = (ServerRecord) lstDevHosts.getSelectedValue();
                b.setEnabled(false);
                lstDevHosts.repaint();
            } else if (b.getActionCommand().equals(CMD_PATHMAPPER)) {
                EditPathMapDialog.showMe((ServerRecord) lstDevHosts.getSelectedValue());
            } else if (b.getActionCommand().equals(CMD_PROPERTIES)) {
                RemoteServerRecord record = (RemoteServerRecord) lstDevHosts.getSelectedValue();
                if (record.isRemote()) {
                    if (HostPropertiesDialog.invokeMe(record)) {
                        lstDevHosts.repaint();
                    }
                    btPathMapper.setEnabled(buttonsEnabled && record.isRemote() && record.getSyncFactory().isPathMappingCustomizable());
                }
            } else if (b.getActionCommand().equals(CMD_RETRY)) {
                this.revalidateRecord(getSelectedRecord(), null, false);
            }
        }
    }

    @Override
    public void save(ToolsCacheManager cacheManager) {
        cacheManager.setHosts(getHosts());
        cacheManager.setDefaultRecord(getDefaultRecord());
    }

    private RemoteServerRecord getSelectedRecord() {
        // we know for sure it's a RemoteServerRecord, not just a ServerRecord
        return (RemoteServerRecord) lstDevHosts.getSelectedValue();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lbDevHosts = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstDevHosts = new javax.swing.JList();
        btAddServer = new javax.swing.JButton();
        btRemoveServer = new javax.swing.JButton();
        btSetAsDefault = new javax.swing.JButton();
        btPathMapper = new javax.swing.JButton();
        btProperties = new javax.swing.JButton();
        lbStatus = new javax.swing.JLabel();
        tfStatus = new javax.swing.JTextField();
        btRetry = new javax.swing.JButton();
        lbReason = new javax.swing.JLabel();
        tfReason = new javax.swing.JTextField();
        pbarStatusPanel = new javax.swing.JPanel();

        setMinimumSize(new java.awt.Dimension(419, 255));
        setLayout(new java.awt.GridBagLayout());

        lbDevHosts.setLabelFor(lstDevHosts);
        org.openide.awt.Mnemonics.setLocalizedText(lbDevHosts, org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_ServerList")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(lbDevHosts, gridBagConstraints);

        jScrollPane1.setMinimumSize(new java.awt.Dimension(200, 200));

        lstDevHosts.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstDevHosts.setMinimumSize(new java.awt.Dimension(200, 200));
        lstDevHosts.setSelectedIndex(0);
        jScrollPane1.setViewportView(lstDevHosts);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1000.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(btAddServer, org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_AddServer")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btAddServer, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(btRemoveServer, org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_RemoveServer")); // NOI18N
        btRemoveServer.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btRemoveServer, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(btSetAsDefault, org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_SetAsDefault")); // NOI18N
        btSetAsDefault.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btSetAsDefault, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(btPathMapper, org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_PathMapper")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 6);
        add(btPathMapper, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(btProperties, org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "EditServerListDialog.btProperties.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(btProperties, gridBagConstraints);

        lbStatus.setLabelFor(tfStatus);
        org.openide.awt.Mnemonics.setLocalizedText(lbStatus, org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_Status")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(lbStatus, gridBagConstraints);

        tfStatus.setColumns(20);
        tfStatus.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1000.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 6);
        add(tfStatus, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(btRetry, org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_Retry")); // NOI18N
        btRetry.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 0, 6);
        add(btRetry, gridBagConstraints);

        lbReason.setLabelFor(tfReason);
        org.openide.awt.Mnemonics.setLocalizedText(lbReason, org.openide.util.NbBundle.getMessage(EditServerListDialog.class, "LBL_Reason")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(lbReason, gridBagConstraints);

        tfReason.setEditable(false);
        tfReason.setPreferredSize(new java.awt.Dimension(40, 21));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 6);
        add(tfReason, gridBagConstraints);

        pbarStatusPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        add(pbarStatusPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAddServer;
    private javax.swing.JButton btPathMapper;
    private javax.swing.JButton btProperties;
    private javax.swing.JButton btRemoveServer;
    private javax.swing.JButton btRetry;
    private javax.swing.JButton btSetAsDefault;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbDevHosts;
    private javax.swing.JLabel lbReason;
    private javax.swing.JLabel lbStatus;
    private javax.swing.JList lstDevHosts;
    private javax.swing.JPanel pbarStatusPanel;
    private javax.swing.JTextField tfReason;
    private javax.swing.JTextField tfStatus;
    // End of variables declaration//GEN-END:variables
}
