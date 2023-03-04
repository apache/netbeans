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

package org.netbeans.modules.php.project.connections.ui.transfer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JButton;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;
import org.netbeans.modules.php.project.connections.ui.transfer.tree.TransferSelector;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

public final class TransferFilesChooser {

    public enum TransferType { UPLOAD, DOWNLOAD };

    private final Set<TransferFile> transferFiles;
    private final TransferType transferType;
    private final long timestamp;

    // @GuardedBy(this)
    private TransferFilesChooserVisual transferVisual;
    // @GuardedBy(this)
    private TransferFilesChooserPanel transferPanel;

    private TransferFilesChooser(Set<TransferFile> transferFiles, TransferType transferType, long timestamp) {
        assert transferFiles != null;
        assert transferType != null;

        // make a synchronized copy of transfer files
        this.transferFiles = Collections.synchronizedSet(new HashSet<>(transferFiles));
        this.transferType = transferType;
        this.timestamp = timestamp;
    }

    public static TransferFilesChooser forDownload(Set<TransferFile> transferFiles) {
        return forDownload(transferFiles, -1);
    }

    public static TransferFilesChooser forDownload(Set<TransferFile> transferFiles, long timestamp) {
        return new TransferFilesChooser(transferFiles, TransferType.DOWNLOAD, timestamp);
    }

    public static TransferFilesChooser forUpload(Set<TransferFile> transferFiles) {
        return forUpload(transferFiles, -1);
    }

    public static TransferFilesChooser forUpload(Set<TransferFile> transferFiles, long timestamp) {
        return new TransferFilesChooser(transferFiles, TransferType.UPLOAD, timestamp);
    }

    public Set<TransferFile> showDialog() {
        return Mutex.EVENT.readAccess(new Mutex.Action<Set<TransferFile>>() {
            @Override
            public Set<TransferFile> run() {
                return showDialogInernal();
            }
        });
    }

    @NbBundle.Messages({
        "TransferFilesChooser.title.download=File Download",
        "TransferFilesChooser.title.upload=File Upload",
        "TransferFilesChooser.button.downloadWithMnemonics=&Download",
        "TransferFilesChooser.button.uploadWithMnemonics=&Upload"
    })
    Set<TransferFile> showDialogInernal() {
        String title;
        String buttonLabel;
        switch (transferType) {
            case DOWNLOAD:
                title = Bundle.TransferFilesChooser_title_download();
                buttonLabel = Bundle.TransferFilesChooser_button_downloadWithMnemonics();
                break;
            case UPLOAD:
                title = Bundle.TransferFilesChooser_title_upload();
                buttonLabel = Bundle.TransferFilesChooser_button_uploadWithMnemonics();
                break;
            default:
                throw new IllegalStateException("Unknown transfer type: " + transferType);
        }
        JButton okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, buttonLabel);
        DialogDescriptor descriptor = new DialogDescriptor(
                getTransferVisual(),
                title,
                true,
                new Object[] {okButton, DialogDescriptor.CANCEL_OPTION},
                okButton,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);
        if (DialogDisplayer.getDefault().notify(descriptor) == okButton) {
            return getTransferPanel().getSelectedFiles();
        }
        return Collections.<TransferFile>emptySet();
    }

    public TransferFilesChooserPanel getEmbeddablePanel() {
        return getTransferVisual().getEmbeddablePanel();
    }

    public boolean hasAnyTransferableFiles() {
        return getTransferPanel().hasAnyTransferableFiles();
    }

    private synchronized TransferFilesChooserVisual getTransferVisual() {
        if (transferVisual == null) {
            transferVisual = new TransferFilesChooserVisual(getTransferPanel(), transferType);
        }
        return transferVisual;
    }

    private synchronized TransferFilesChooserPanel getTransferPanel() {
        if (transferPanel == null) {
            transferPanel = new TransferSelector(transferFiles, transferType, timestamp);
        }
        return transferPanel;
    }
}
