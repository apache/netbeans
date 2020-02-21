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

/*
 * HostPropertiesDialog.java
 *
 * Created on Apr 28, 2009, 1:51:26 AM
 */
package org.netbeans.modules.cnd.remote.ui;

import java.awt.Dialog;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.netbeans.modules.cnd.remote.server.RemoteServerList;
import org.netbeans.modules.cnd.remote.server.RemoteServerRecord;
import org.netbeans.modules.cnd.remote.ui.impl.RemoteSyncNotifierImpl;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ui.util.NativeExecutionUIUtils;
import org.netbeans.modules.nativeexecution.api.ui.util.ValidateablePanel;
import org.netbeans.modules.nativeexecution.api.ui.util.ValidatablePanelListener;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 */
@SuppressWarnings("rawtypes") // UI editor produces code with tons of rawtypes warnings
public class HostPropertiesDialog extends JPanel {

    private final ValidatablePanelListener validationListener;
    private final JButton ok;
    private final ValidateablePanel vpanel;

    public static boolean invokeMe(RemoteServerRecord record) {
        HostPropertiesDialog pane = new HostPropertiesDialog(record);

        Object[] buttons = new Object[]{
            pane.ok,
            DialogDescriptor.CANCEL_OPTION
        };

        DialogDescriptor dd = new DialogDescriptor(
                pane, NbBundle.getMessage(HostPropertiesDialog.class, "TITLE_HostProperties"),
                true, buttons, pane.ok, DialogDescriptor.DEFAULT_ALIGN, null, null);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setResizable(false);
        
        try {
            dialog.setVisible(true);
        } catch (Throwable th) {
            if (!(th.getCause() instanceof InterruptedException)) {
                throw new RuntimeException(th);
            }
            dd.setValue(DialogDescriptor.CANCEL_OPTION);
        } finally {
            dialog.dispose();
        }

        if (dd.getValue() == pane.ok) {
            pane.vpanel.applyChanges(null);
            String displayName = pane.tfName.getText();
            boolean changed = false;
            if (!displayName.equals(record.getDisplayName())) {
                record.setDisplayName(displayName);
                changed = true;
            }
            RemoteSyncFactory syncFactory = (RemoteSyncFactory) pane.cbSync.getSelectedItem();
            if (!syncFactory.equals(record.getSyncFactory())) {
                record.setSyncFactory(syncFactory);
                changed = true;
            }
            if (pane.cbACL.isEnabled()) {
                FileSystemProvider.AccessCheckType access = pane.cbACL.isSelected() ? 
                        FileSystemProvider.AccessCheckType.FULL : FileSystemProvider.AccessCheckType.FAST;
                ExecutionEnvironment execEnv = record.getExecutionEnvironment();
                if (FileSystemProvider.getAccessCheckType(execEnv) != access) {
                    FileSystemProvider.setAccessCheckType(execEnv, access);
                    changed = true;
                }
            }
//            if (record.isX11forwardingPossible()) {
            boolean x11forwarding = pane.cbX11.isSelected();
            if (x11forwarding != record.getX11Forwarding()) {
                record.setX11Forwarding(x11forwarding);
                changed = true;
            }
//            }
            if (changed) {
                RemoteServerList.storePreferences();
                return true;
            }
        }
        return false;
    }

    /** Creates new form HostPropertiesDialog */
    @org.netbeans.api.annotations.common.SuppressWarnings("Se") // it's never serialized!
    private HostPropertiesDialog(RemoteServerRecord serverRecord) {
        validationListener = new ValidationListenerImpl();

        initComponents();

        ok = new JButton("OK"); // NOI18N
        final ExecutionEnvironment execEnv = serverRecord.getExecutionEnvironment();

        cbACL.setEnabled(FileSystemProvider.canSetAccessCheckType(execEnv));
        cbACL.setSelected(FileSystemProvider.getAccessCheckType(execEnv) == FileSystemProvider.AccessCheckType.FULL);

        vpanel = NativeExecutionUIUtils.getConfigurationPanel(execEnv);
        vpanel.addValidationListener(validationListener);

        connectionPanel.add(vpanel);
        tfName.setText(serverRecord.getDisplayName());
        RemoteSyncNotifierImpl.arrangeComboBox(cbSync, serverRecord.getExecutionEnvironment());
        cbSync.setSelectedItem(serverRecord.getSyncFactory());
        cbX11.setSelected(serverRecord.getX11Forwarding());
//        // if x11forwarding is set, but we consider it is unavailable,
//        // we should at least allow switching it off => || serverRecord.getX11Forwarding()
//        cbX11.setEnabled(serverRecord.isX11forwardingPossible() || serverRecord.getX11Forwarding());
        addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorAdded(AncestorEvent event) {
                tfName.requestFocus();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });

        setError(null);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        connectionPanel = new javax.swing.JPanel();
        serverRecordPanel = new javax.swing.JPanel();
        cbSync = new javax.swing.JComboBox();
        tfName = new javax.swing.JTextField();
        lblName = new javax.swing.JLabel();
        cbX11 = new javax.swing.JCheckBox();
        lblSync = new javax.swing.JLabel();
        cbACL = new javax.swing.JCheckBox();
        errorLabel = new javax.swing.JLabel();

        connectionPanel.setLayout(new java.awt.BorderLayout());

        serverRecordPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.serverRecordPanel.border.title"))); // NOI18N

        tfName.setText(org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.tfName.text")); // NOI18N

        lblName.setLabelFor(tfName);
        org.openide.awt.Mnemonics.setLocalizedText(lblName, org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.lblName.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbX11, org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.cbX11.text")); // NOI18N

        lblSync.setLabelFor(cbSync);
        org.openide.awt.Mnemonics.setLocalizedText(lblSync, org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.lblSync.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbACL, org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.cbACL.text")); // NOI18N

        javax.swing.GroupLayout serverRecordPanelLayout = new javax.swing.GroupLayout(serverRecordPanel);
        serverRecordPanel.setLayout(serverRecordPanelLayout);
        serverRecordPanelLayout.setHorizontalGroup(
            serverRecordPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(serverRecordPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(serverRecordPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(serverRecordPanelLayout.createSequentialGroup()
                        .addGroup(serverRecordPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblSync, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(serverRecordPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbSync, 0, 323, Short.MAX_VALUE)
                            .addComponent(tfName, javax.swing.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE))
                        .addGap(12, 12, 12))
                    .addGroup(serverRecordPanelLayout.createSequentialGroup()
                        .addComponent(cbX11)
                        .addContainerGap(288, Short.MAX_VALUE))
                    .addGroup(serverRecordPanelLayout.createSequentialGroup()
                        .addComponent(cbACL)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        serverRecordPanelLayout.setVerticalGroup(
            serverRecordPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(serverRecordPanelLayout.createSequentialGroup()
                .addGroup(serverRecordPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(serverRecordPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSync)
                    .addComponent(cbSync, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addComponent(cbACL)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbX11)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        errorLabel.setForeground(java.awt.Color.red);
        errorLabel.setText(org.openide.util.NbBundle.getMessage(HostPropertiesDialog.class, "HostPropertiesDialog.errorLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(connectionPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
                    .addComponent(serverRecordPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(errorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(connectionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(serverRecordPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(errorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbACL;
    private javax.swing.JComboBox cbSync;
    private javax.swing.JCheckBox cbX11;
    private javax.swing.JPanel connectionPanel;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblSync;
    private javax.swing.JPanel serverRecordPanel;
    private javax.swing.JTextField tfName;
    // End of variables declaration//GEN-END:variables

    private void setError(final String error) {
        Mutex.EVENT.readAccess(new Runnable() {

            @Override
            public void run() {
                ok.setEnabled(error == null);
                errorLabel.setText(error == null ? " " : error); // NOI18N
            }
        });
    }

    private class ValidationListenerImpl implements ValidatablePanelListener {

        @Override
        public void stateChanged(ValidateablePanel src) {
            setError(src.hasProblem() ? src.getProblem() : null);
        }
    }
}
