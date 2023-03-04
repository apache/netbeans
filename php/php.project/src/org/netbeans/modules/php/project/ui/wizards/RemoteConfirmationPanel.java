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

import java.awt.Component;
import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.php.project.connections.RemoteClient;
import org.netbeans.modules.php.project.connections.RemoteException;
import org.netbeans.modules.php.project.connections.common.RemoteUtils;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;
import org.netbeans.modules.php.project.connections.ui.transfer.TransferFilesChooser;
import org.netbeans.modules.php.project.ui.actions.RemoteCommand;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;

/**
 * @author Tomas Mysik
 */
public class RemoteConfirmationPanel implements WizardDescriptor.Panel<WizardDescriptor>,
        WizardDescriptor.FinishablePanel<WizardDescriptor>, CancelablePanel, ChangeListener {

    static final String REMOTE_FILES = "remoteFiles"; // NOI18N
    static final String REMOTE_CLIENT = "remoteClient"; // NOI18N

    private static final RequestProcessor RP = new RequestProcessor("Fetching remote files", 2); // NOI18N

    final ChangeSupport changeSupport = new ChangeSupport(this);
    private final String[] steps;

    private RemoteConfirmationPanelVisual confirmationPanel = null;
    private WizardDescriptor descriptor = null;
    private volatile boolean canceled;
    private volatile RemoteClient remoteClient;

    public RemoteConfirmationPanel(String[] steps) {
        this.steps = steps.clone();
    }

    String[] getSteps() {
        return steps;
    }

    @Override
    public Component getComponent() {
        if (confirmationPanel == null) {
            confirmationPanel = new RemoteConfirmationPanelVisual(this, descriptor);
            confirmationPanel.addRemoteConfirmationListener(this);
        }
        return confirmationPanel;
    }

    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        descriptor = settings;
        getComponent();

        canceled = false;

        fetchRemoteFiles();
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        descriptor = settings;
        getComponent();

        if (remoteClient != null) {
            // this method is called more than once so stor the remove client just once
            settings.putProperty(REMOTE_CLIENT, remoteClient);
        }
        cancel();

        settings.putProperty(REMOTE_FILES, confirmationPanel.getRemoteFiles());
    }

    @Override
    public boolean isValid() {
        switch (confirmationPanel.getState()) {
            case FETCHING:
                descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, " "); // NOI18N
                descriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, NbBundle.getMessage(RemoteConfirmationPanel.class, "LBL_FetchingRemoteFiles"));
                return false;

            case NO_FILES:
                descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(RemoteConfirmationPanel.class, "MSG_NoFilesAvailable"));
                return false;

            case FILES:
                if (confirmationPanel.getRemoteFiles().isEmpty()) {
                    descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(RemoteConfirmationPanel.class, "MSG_NoFilesSelected"));
                    return false;
                }
                break;

            default:
                throw new IllegalStateException("Unknown state: " + confirmationPanel.getState());
        }

        descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, " "); // NOI18N
        return true;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

    @Override
    public void cancel() {
        canceled = true;
        if (remoteClient != null) {
            remoteClient.cancel();
            disconnectRemoteClient();
        }
    }

    void fetchRemoteFiles() {
        getComponent();
        confirmationPanel.setFetchingFiles();

        RP.post(new Runnable() {
            @Override
            public void run() {
                ProgressHandle handle = ProgressHandle.createHandle(NbBundle.getMessage(RemoteConfirmationPanel.class, "LBL_FetchingRemoteFilesProgress"));
                try {
                    handle.start();

                    Set<TransferFile> remoteFiles = Collections.emptySet();
                    String reason = ""; // NOI18N
                    try {
                        remoteFiles = getRemoteFiles();
                    } catch (RemoteException ex) {
                        Logger.getLogger(RemoteConfirmationPanel.class.getName()).log(Level.INFO, "Cannot fetch files", ex);
                        reason = ex.getMessage();
                    }

                    final Set<TransferFile> rmt = Collections.synchronizedSet(remoteFiles);
                    final String rsn = reason;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            boolean hasAnyTransferableFiles = TransferFilesChooser.forDownload(rmt).hasAnyTransferableFiles();
                            if (canceled) {
                                return;
                            }
                            if (hasAnyTransferableFiles) {
                                confirmationPanel.setRemoteFiles(rmt);
                            } else {
                                confirmationPanel.setNoFiles(rsn);
                            }
                            changeSupport.fireChange();
                        }
                    });
                } finally {
                    handle.finish();
                }
            }
        });
    }

    private Set<TransferFile> getRemoteFiles() throws RemoteException {
        assert descriptor != null;

        File sources = NewPhpProjectWizardIterator.getSources(descriptor);
        RemoteConfiguration remoteConfiguration = (RemoteConfiguration) descriptor.getProperty(RunConfigurationPanel.REMOTE_CONNECTION);
        InputOutput remoteLog = RemoteCommand.getRemoteLog(remoteConfiguration.getDisplayName());
        String remoteDirectory = (String) descriptor.getProperty(RunConfigurationPanel.REMOTE_DIRECTORY);
        disconnectRemoteClient();
        remoteClient = new RemoteClient(remoteConfiguration, new RemoteClient.AdvancedProperties()
                    .setInputOutput(remoteLog)
                    .setAdditionalInitialSubdirectory(remoteDirectory)
                    .setPreservePermissions(false));
        return remoteClient.prepareDownload(sources, sources);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }

    // #205087
    private void disconnectRemoteClient() {
        if (remoteClient != null) {
            // #211563
            final RemoteClient remoteClientCopy = remoteClient;
            remoteClient = null;
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        remoteClientCopy.disconnect(true);
                    } catch (RemoteException ex) {
                        RemoteUtils.processRemoteException(ex);
                    }
                }
            });
        }
    }

}
