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

package org.netbeans.modules.php.project.ui.wizards;

import java.awt.Font;
import java.util.Collections;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;
import org.netbeans.modules.php.project.connections.ui.transfer.TransferFilesChooser;
import org.netbeans.modules.php.project.connections.ui.transfer.TransferFilesChooserPanel;
import org.netbeans.modules.php.project.connections.ui.transfer.TransferFilesChooserPanel.TransferFilesChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * All the methods must be invoked in AWT thread.
 */
@org.netbeans.api.annotations.common.SuppressWarnings("SE_BAD_FIELD_STORE")
public final class RemoteConfirmationPanelVisual extends JPanel {
    static enum State { FETCHING, NO_FILES, FILES };

    private static final long serialVersionUID = 3753241413078222434L;
    private static final int STEP_INDEX = 2;

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final TransferFilesChangeListener transferFilesChangeListener;

    private volatile TransferFilesChooserPanel transferPanel;
    private State state = null;

    public RemoteConfirmationPanelVisual(RemoteConfirmationPanel wizardPanel, WizardDescriptor descriptor) {
        assert SwingUtilities.isEventDispatchThread() : "Must be run in EDT";
        assert wizardPanel != null;
        assert descriptor != null;

        // Provide a name in the title bar.
        setName(wizardPanel.getSteps()[STEP_INDEX]);
        putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, STEP_INDEX);
        // Step name (actually the whole list for reference).
        putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, wizardPanel.getSteps());

        initComponents();

        setFetchingFiles();
        uploadInfoLabel.setText(NbBundle.getMessage(RemoteConfirmationPanelVisual.class, "TXT_UploadInfo"));

        transferFilesChangeListener = new TransferFilesChangeListener() {
            @Override
            public void selectedFilesChanged() {
                changeSupport.fireChange();
            }
            @Override
            public void filterChanged() {
            }
        };
    }

    public void addRemoteConfirmationListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeRemoteConfirmationListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public void setRemoteFiles(Set<TransferFile> remoteFiles) {
        assert SwingUtilities.isEventDispatchThread() : "Must be run in EDT";
        assert remoteFiles != null;

        state = State.FILES;
        transferPanel = TransferFilesChooser.forDownload(remoteFiles).getEmbeddablePanel();
        transferPanel.addChangeListener(transferFilesChangeListener);

        filesPanel.removeAll();
        filesPanel.add(transferPanel);

        statusLabel.setText(NbBundle.getMessage(RemoteConfirmationPanelVisual.class, "LBL_Confirmation")); // NOI18N
        setState(true);
    }

    public void setNoFiles(String reason) {
        assert SwingUtilities.isEventDispatchThread() : "Must be run in EDT";

        state = State.NO_FILES;
        resetTransferFilter();

        statusLabel.setText(NbBundle.getMessage(RemoteConfirmationPanelVisual.class, "LBL_NoFiles", reason)); // NOI18N
        setState(false);
    }

    public void setFetchingFiles() {
        assert SwingUtilities.isEventDispatchThread() : "Must be run in EDT";

        state = State.FETCHING;
        resetTransferFilter();

        statusLabel.setText(NbBundle.getMessage(RemoteConfirmationPanelVisual.class, "LBL_FetchingRemoteFiles")); // NOI18N
        setState(false);
    }

    public Set<TransferFile> getRemoteFiles() {
        if (transferPanel == null) {
            return Collections.emptySet();
        }
        return transferPanel.getSelectedFiles();
    }

    State getState() {
        assert SwingUtilities.isEventDispatchThread() : "Must be run in EDT";
        return state;
    }

    private void setState(boolean enabled) {
        downloadInfoLabel.setVisible(enabled);
        uploadInfoLabel.setVisible(enabled);
        filesPanel.setVisible(enabled);
    }

    private void resetTransferFilter() {
        if (transferPanel != null) {
            transferPanel.removeChangeListener(transferFilesChangeListener);
        }
        transferPanel = null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        statusLabel = new JLabel();
        downloadInfoLabel = new JLabel();
        uploadInfoLabel = new JLabel();
        filesPanel = new JPanel();

        setFocusTraversalPolicy(null);

        statusLabel.setFont(statusLabel.getFont().deriveFont(statusLabel.getFont().getStyle() | Font.BOLD));
        statusLabel.setText("DUMMY"); // NOI18N

        downloadInfoLabel.setText(NbBundle.getMessage(RemoteConfirmationPanelVisual.class, "RemoteConfirmationPanelVisual.downloadInfoLabel.text")); // NOI18N

        uploadInfoLabel.setText("DUMMY"); // NOI18N

        filesPanel.setLayout(new BoxLayout(filesPanel, BoxLayout.LINE_AXIS));

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(statusLabel)
                    .addComponent(downloadInfoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(uploadInfoLabel))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(filesPanel, GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(statusLabel)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(downloadInfoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(filesPanel, GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(uploadInfoLabel))
        );

        downloadInfoLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RemoteConfirmationPanelVisual.class, "RemoteConfirmationPanelVisual.downloadInfoLabel.AccessibleContext.accessibleName")); // NOI18N
        downloadInfoLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RemoteConfirmationPanelVisual.class, "RemoteConfirmationPanelVisual.downloadInfoLabel.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(RemoteConfirmationPanelVisual.class, "RemoteConfirmationPanelVisual.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RemoteConfirmationPanelVisual.class, "RemoteConfirmationPanelVisual.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel downloadInfoLabel;
    private JPanel filesPanel;
    private JLabel statusLabel;
    private JLabel uploadInfoLabel;
    // End of variables declaration//GEN-END:variables

}
