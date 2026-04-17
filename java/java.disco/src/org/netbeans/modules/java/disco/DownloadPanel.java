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
package org.netbeans.modules.java.disco;

import io.foojay.api.discoclient.event.DownloadEvt;
import io.foojay.api.discoclient.event.Evt;
import io.foojay.api.discoclient.pkg.Pkg;

import static org.netbeans.modules.java.disco.SwingWorker2.submit;

import org.netbeans.modules.java.disco.archive.JDKCommonsUnzip;
import org.netbeans.modules.java.disco.archive.UnarchiveUtils;
import org.netbeans.modules.java.disco.ioprovider.IOContainerPanel;
import java.awt.CardLayout;
import java.io.File;
import java.io.IOException;
import javax.swing.Action;

import static javax.swing.SwingUtilities.invokeLater;

import javax.swing.UIManager;
import org.checkerframework.checker.guieffect.qual.UIEffect;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;
import org.openide.windows.IOContainer;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

@SuppressWarnings("initialization")
public class DownloadPanel extends javax.swing.JPanel {

    public static final String PROP_DOWNLOAD_FINISHED = "downloadFinished";

    private boolean downloadFinished;
    private File download;
    private final Client discoClient;
    private final WizardState state;
    private IOContainerPanel executionPanel;
    private String downloadFolder;

    @UIEffect
    public static DownloadPanel create(WizardState state) {
        DownloadPanel d = new DownloadPanel(state);
        d.init();
        return d;
    }

    @UIEffect
    @SuppressWarnings("initialization")
    private DownloadPanel(WizardState state) {
        this.state = state;
        discoClient = Client.getInstance();
    }

    @UIEffect
    private void init() {
        setName("Download");

        initComponents();

        this.executionPanel = new IOContainerPanel();
    }

    private boolean initialLoad = false; //track the async load in addNotify

    @Override
    @UIEffect
    public void addNotify() {
        super.addNotify();
        //we do the bellow only once
        if (initialLoad)
            return;
        initialLoad = true;

        //this potentially does network calls, do it after component shown
        discoClient.removeAllObservers();
        discoClient.setOnEvt(DownloadEvt.DOWNLOAD_STARTED, this::handleDownloadStarted);
        discoClient.setOnEvt(DownloadEvt.DOWNLOAD_FINISHED, this::handleDownloadFinished);
        discoClient.setOnEvt(DownloadEvt.DOWNLOAD_FAILED, this::handleDownloadFailed);
        discoClient.setOnEvt(DownloadEvt.DOWNLOAD_PROGRESS, this::handleDownloadProgress);

        //immediatelly start download
        downloadButtonActionPerformed();
    }

    @NonNull
    public String getDefaultDownloadFolder() {
        File f = OS.getDefaultDownloadFolder();
        if (f == null)
            return "";
        return f.getAbsolutePath();
    }

    @UIEffect
    private void downloadBundle(File destinationFolder) {
        setStatus("Preparing...");
        submit(() -> {
            Pkg bundle = state.selection.get(discoClient);
            return discoClient.getPkgInfoByPkgId(bundle.getId(), bundle.getJavaVersion());
        }).then(pkgInfo -> {
            download = new File(destinationFolder, pkgInfo.getFileName());
            String path = download.getAbsolutePath();
            submit(() -> discoClient.downloadPkg(pkgInfo, path))
                    .handle(this::handleDownloadFailed)
                    .execute();
        }).handle(this::handleDownloadFailed)
        .execute();
    }

    @UIEffect
    private void setStatus(String text) {
        setStatus(text, null);
    }

    @UIEffect
    private void setStatus(String text, @Nullable String uiKey) {
        statusLabel.setText(text);
        if (uiKey != null) {
            statusLabel.setIcon(UIManager.getIcon(uiKey));
        } else {
            statusLabel.setIcon(null);
        }
        putClientProperty(WizardDescriptor.PROP_INFO_MESSAGE, text);
    }

    private void handleDownloadStarted(Evt e) {
                invokeLater(() -> {
                    setStatus("Downloading...");
                });
    }

    private void handleDownloadFinished(Evt e) {
        downloadFinished = true;
        if (UnarchiveUtils.isArchiveFile(download)) {
            invokeLater(() -> {
                setStatus("Unarchiving...", "Menu.arrowIcon");

                bottomPanel.add(executionPanel);
                ((CardLayout) bottomPanel.getLayout()).last(bottomPanel);
                InputOutput io = IOProvider.getDefault().getIO("Unarchive output", new Action[0], IOContainer.create(executionPanel));

                submit(() -> {
                    return unarchive(io);
                }).then(file -> {
                    download = file;

                    notifyDownloadFinished();
                }).handle(Exceptions::printStackTrace) //this exception is after the file is downloaded, so we still have the package.
                .execute();
            });
        } else {
            invokeLater(this::notifyDownloadFinished);
        }
    }

    @UIEffect
    private void notifyDownloadFinished() {
        setStatus("Finished.");
        firePropertyChange(PROP_DOWNLOAD_FINISHED, false, true);
    }

    private void handleDownloadProgress(Evt e) {
        DownloadEvt event = (DownloadEvt) e;
        int percentage = (int) ((double) event.getFraction() / (double) event.getFileSize() * 100);
        invokeLater(() -> progressBar.setValue(percentage));
    }

    private void handleDownloadFailed(@Nullable Exception e) {
        if (e != null)
            Exceptions.printStackTrace(e);

        invokeLater(() -> {
            setStatus("Download failed", "OptionPane.warningIcon");
            //TODO: Allow back button somehow?
        });
    }

    private void handleDownloadFailed(Evt e) {
        handleDownloadFailed((Exception) null);
    }

    private File unarchive(InputOutput io) throws IOException, InterruptedException {
        File outputFile = UnarchiveUtils.unarchive(download, io);
        //find bin folder and return the parent of that as the download path
        File binFolder = JDKCommonsUnzip.findBin(outputFile);
        if (binFolder != null) {
            File parent = binFolder.getParentFile();
            if (parent != null) //but, really, could the parent ever be null?
                return parent;
        }
        return outputFile;
    }

    public boolean isDownloadFinished() {
        return downloadFinished;
    }

    //@Nullable
    public File getDownload() {
        return download;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @UIEffect
    @SuppressWarnings({"unchecked", "nullness"})
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        statusLabel = new javax.swing.JLabel();
        bottomPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        progressBar = new javax.swing.JProgressBar();

        statusLabel.setFont(statusLabel.getFont().deriveFont((statusLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        org.openide.awt.Mnemonics.setLocalizedText(statusLabel, org.openide.util.NbBundle.getMessage(DownloadPanel.class, "DownloadPanel.statusLabel.text")); // NOI18N

        bottomPanel.setLayout(new java.awt.CardLayout());

        jPanel1.setLayout(new java.awt.BorderLayout());

        progressBar.setStringPainted(true);
        jPanel1.add(progressBar, java.awt.BorderLayout.NORTH);

        bottomPanel.add(jPanel1, "card2");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(119, 119, 119))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(bottomPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bottomPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void downloadButtonActionPerformed() {
        //start from 0, maybe we restarted.
        progressBar.setValue(0);
        downloadBundle(new File(downloadFolder));
    }

    public void setDownloadFolder(String folder) {
        this.downloadFolder = folder;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusLabel;
    // End of variables declaration//GEN-END:variables

}
