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
