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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
