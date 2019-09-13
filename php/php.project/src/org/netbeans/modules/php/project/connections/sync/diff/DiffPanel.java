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
package org.netbeans.modules.php.project.connections.sync.diff;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.connections.RemoteClient;
import org.netbeans.modules.php.project.connections.RemoteException;
import org.netbeans.modules.php.project.connections.TmpLocalFile;
import org.netbeans.modules.php.project.connections.sync.SyncItem;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Panel for viewing diffs between remote and local files.
 */
public final class DiffPanel extends JPanel {

    private static final long serialVersionUID = 54678645646768L;

    static final Logger LOGGER = Logger.getLogger(DiffPanel.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(DiffPanel.class);
    // @GuardedBy(AWT)
    private static Dimension PREFERRED_SIZE = new Dimension(1024, 768);

    final SyncItem syncItem;
    final RemoteClient remoteClient;
    final String charsetName;

    // tmp files
    volatile TmpLocalFile remoteTmpFile = null;
    volatile TmpLocalFile localTmpFile = null;
    volatile EditableTmpLocalFileStreamSource editableTmpLocalFileStreamSource = null;

    // @GuardedBy(AWT)
    DialogDescriptor descriptor;
    // @GuardedBy(AWT)
    private NotificationLineSupport notificationLineSupport = null;


    public DiffPanel(RemoteClient remoteClient, SyncItem syncItem, String charsetName) {
        this.remoteClient = remoteClient;
        this.syncItem = syncItem;
        this.charsetName = charsetName;

        initComponents();
        setPreferredSize(PREFERRED_SIZE);
    }

    @NbBundle.Messages({
        "# {0} - file path",
        "DiffPanel.title=Remote Diff for {0}",
        "DiffPanel.button.titleWithMnemonics=&Upload Local Changes"
    })
    public boolean open() throws IOException {
        assert SwingUtilities.isEventDispatchThread();
        JButton okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, Bundle.DiffPanel_button_titleWithMnemonics());
        descriptor = new DialogDescriptor(
                this,
                Bundle.DiffPanel_title(syncItem.getPath()),
                true,
                new Object[] {okButton, DialogDescriptor.CANCEL_OPTION},
                okButton,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);
        notificationLineSupport = descriptor.createNotificationLineSupport();
        descriptor.setValid(false);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        setDiffView();
        try {
            dialog.setVisible(true);
        } finally {
            PREFERRED_SIZE = getSize();
            dialog.dispose();
            DiffFileEncodingQueryImpl.clear();
        }
        boolean ok = descriptor.getValue() == okButton;
        boolean fileModified = false;
        try {
            if (editableTmpLocalFileStreamSource != null) {
                fileModified = editableTmpLocalFileStreamSource.save();
            }
        } finally {
            if (ok) {
                // set new tmp file?
                if (fileModified) {
                    // clean any old tmp file
                    syncItem.cleanupTmpLocalFile();
                    // set new tmp file
                    syncItem.setTmpLocalFile(localTmpFile);
                } else if (localTmpFile != null) {
                    localTmpFile.cleanup();
                }
            } else {
                // cancel -> cleanup local tmp file
                if (localTmpFile != null) {
                    localTmpFile.cleanup();
                }
            }
            // always cleanup remote tmp file
            if (remoteTmpFile != null) {
                remoteTmpFile.cleanup();
            }
        }
        return ok;
    }

    void showError(final String msg) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                if (msg != null) {
                    notificationLineSupport.setErrorMessage(msg);
                    descriptor.setValid(false);
                } else {
                    notificationLineSupport.clearMessages();
                    descriptor.setValid(true);
                }
            }
        });
    }

    @NbBundle.Messages("DiffPanel.error.cannotReadFiles=Cannot read files for comparison.")
    private void setDiffView() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                String name = syncItem.getName();
                String mimeType = getMimeType();
                // remote stream
                final StreamSource remoteStream = getRemoteStreamSource(name, mimeType);
                if (remoteStream == null) {
                    // some error, already processed
                    return;
                }
                // local stream
                editableTmpLocalFileStreamSource = getLocalStreamSource(name, mimeType);
                if (editableTmpLocalFileStreamSource == null) {
                    // some error, already processed
                    return;
                }
                // update ui
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DiffController diffController = DiffController.createEnhanced(remoteStream, editableTmpLocalFileStreamSource);
                            remove(loadingLabel);
                            add(diffController.getJComponent(), BorderLayout.CENTER);
                            revalidate();
                            repaint();
                            descriptor.setValid(true);
                        } catch (IOException ex) {
                            LOGGER.log(Level.INFO, null, ex);
                            showError(Bundle.DiffPanel_error_cannotReadFiles());
                        }
                    }
                });
            }

        });
    }

    String getMimeType() {
        TransferFile localTransferFile = syncItem.getLocalTransferFile();
        if (localTransferFile != null) {
            FileObject fileObject = FileUtil.toFileObject(localTransferFile.resolveLocalFile());
            if (fileObject != null) {
                return fileObject.getMIMEType();
            }
        }
        return getMimeType(syncItem.getName());
    }

    private String getMimeType(String filename) {
        try {
            return FileUtil.createMemoryFileSystem().getRoot().createData(filename).getMIMEType();
        } catch (IOException ex) {
            // ignored, should not happen
            LOGGER.log(Level.WARNING, null, ex);
        }
        return "content/unknown"; // NOI18N
    }

    @NbBundle.Messages({
        "# {0} - file name",
        "DiffPanel.error.cannotDownload=File {0} cannot be downloaded."
    })
    StreamSource getRemoteStreamSource(String name, String mimeType) {
        TransferFile transferFile = syncItem.getRemoteTransferFile();
        if (transferFile == null) {
            return new NullStreamSource(name, mimeType, true);
        }
        remoteTmpFile = TmpLocalFile.onDisk(getExtension(name));
        try {
            if (remoteClient.downloadTemporary(remoteTmpFile, transferFile)) {
                rememberEncoding(remoteTmpFile);
                return new TmpLocalFileStreamSource(name, remoteTmpFile, mimeType, charsetName, true);
            } else {
                showError(Bundle.DiffPanel_error_cannotDownload(name));
            }
        } catch (RemoteException ex) {
            LOGGER.log(Level.INFO, null, ex);
            showError(ex.getLocalizedMessage());
        }
        return null;
    }

    @NbBundle.Messages({
        "DiffPanel.error.copyContent=Content of file cannot be copied to temporary file.",
        "DiffPanel.error.opening=Local file cannot be opened."
    })
    EditableTmpLocalFileStreamSource getLocalStreamSource(String name, String mimeType) {
        localTmpFile = TmpLocalFile.onDisk(getExtension(name));
        try {
            TmpLocalFile currentTmpLocalFile = syncItem.getTmpLocalFile();
            if (currentTmpLocalFile != null) {
                // already has tmp file
                copyContent(new File(currentTmpLocalFile.getAbsolutePath()), localTmpFile);
            } else {
                // no tmp file yet
                TransferFile localTransferFile = syncItem.getLocalTransferFile();
                if (localTransferFile != null) {
                    copyContent(localTransferFile.resolveLocalFile(), localTmpFile);
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            showError(Bundle.DiffPanel_error_copyContent());
            return null;
        }
        try {
            rememberEncoding(localTmpFile);
            return new EditableTmpLocalFileStreamSource(name, localTmpFile, mimeType, charsetName, false);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            showError(Bundle.DiffPanel_error_opening());
        }
        return null;
    }

    private String getExtension(String filename) {
        List<String> parts = StringUtils.explode(filename, "."); // NOI18N
        if (parts.isEmpty()) {
            return null;
        }
        return parts.get(parts.size() - 1);
    }

    private void copyContent(File file, TmpLocalFile localTmpFile) throws IOException {
        FileObject fileObject = FileUtil.toFileObject(file);
        if (fileObject == null || !fileObject.isValid()) {
            return;
        }
        try (InputStream inputStream = fileObject.getInputStream(); OutputStream outputStream = localTmpFile.getOutputStream()) {
            FileUtil.copy(inputStream, outputStream);
        }
    }

    void rememberEncoding(TmpLocalFile tmpLocalFile) {
        if (tmpLocalFile != null) {
            String path = tmpLocalFile.getAbsolutePath();
            assert path != null : "Path for local tmp file should be present";
            FileObject fo = FileUtil.toFileObject(new File(path));
            assert fo != null : "Fileobject for " + path + " should exist";
            DiffFileEncodingQueryImpl.addCharset(fo, Charset.forName(charsetName));
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        loadingLabel = new JLabel();

        setLayout(new BorderLayout());

        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        Mnemonics.setLocalizedText(loadingLabel, NbBundle.getMessage(DiffPanel.class, "DiffPanel.loadingLabel.text")); // NOI18N
        add(loadingLabel, java.awt.BorderLayout.CENTER);
        add(loadingLabel, BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel loadingLabel;
    // End of variables declaration//GEN-END:variables

}
