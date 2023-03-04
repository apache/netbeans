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
package org.netbeans.modules.php.project.connections.sync;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 * Synchronization progress panel.
 */
public class ProgressPanel extends JPanel {

    private static final long serialVersionUID = -46789965146754L;

    private static final int NO_SYNC_UNITS = 1;

    final SummaryPanel summaryPanel;
    final ProgressHandle progressHandle;

    volatile boolean syncRunning = false;
    // @GuardedBy(AWT)
    JButton actionButton = null;
    // @GuardedBy(AWT)
    NotificationLineSupport notificationLineSupport = null;
    // @GuardedBy(AWT)
    Dialog dialog = null;
    // @GuardedBy(AWT)
    JLabel progressMessageLabel = null;

    volatile boolean error = false;

    // @GuardedBy(read in one thread only)
    private int workUnits = 0;


    @NbBundle.Messages("ProgressPanel.progress.title=Synchronizing...")
    public ProgressPanel(SyncPanel.SyncInfo syncInfo) {
        assert SwingUtilities.isEventDispatchThread();
        assert syncInfo != null;

        summaryPanel = new SummaryPanel(syncInfo.upload, syncInfo.download, syncInfo.delete, syncInfo.noop);
        progressHandle = ProgressHandle.createHandle(Bundle.ProgressPanel_progress_title());
        // #211494
        progressMessageLabel = ProgressHandleFactory.createDetailLabelComponent(progressHandle);
        // set correct height of the component
        progressMessageLabel.setText(" "); // NOI18N

        initComponents();
        summaryPanelHolder.add(summaryPanel, BorderLayout.CENTER);
        progressPanelHolder.add(ProgressHandleFactory.createProgressComponent(progressHandle), BorderLayout.CENTER);
        progressMessagePanelHolder.add(progressMessageLabel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    @NbBundle.Messages({
        "ProgressPanel.title=Synchronization",
        "ProgressPanel.button.cancel=&Cancel"
    })
    public void createPanel(AtomicBoolean cancel) {
        assert SwingUtilities.isEventDispatchThread();
        actionButton = new JButton();
        actionButton.addActionListener(new ActionButtionListener(cancel));
        Mnemonics.setLocalizedText(actionButton, Bundle.ProgressPanel_button_cancel());
        DialogDescriptor descriptor = new DialogDescriptor(
                this,
                Bundle.ProgressPanel_title(),
                true,
                new Object[] {actionButton},
                actionButton,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);
        descriptor.setValid(false);
        descriptor.setClosingOptions(new Object[]{});
        descriptor.setAdditionalOptions(new Object[]{autoCloseCheckBox});
        notificationLineSupport = descriptor.createNotificationLineSupport();
        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
    }

    public void start(List<SyncItem> items) {
        syncRunning = true;
        int units = 0;
        for (SyncItem syncItem : items) {
            if (syncItem.getOperation().hasProgress()) {
                units += syncItem.getSize() / 1000;
            }
        }
        progressHandle.start(units == 0 ? NO_SYNC_UNITS : units);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                dialog.setVisible(true);
            }
        });
    }

    @NbBundle.Messages({
        "ProgressPanel.cancel=<html><b>Synchronization cancelled.</b>"
    })
    public void cancel() {
        finishInternal(Bundle.ProgressPanel_cancel(), true);
    }

    @NbBundle.Messages({
        "ProgressPanel.success=<html><b>Synchronization successfully finished.</b>"
    })
    public void finish() {
        finishInternal(Bundle.ProgressPanel_success(), false);
    }

    @NbBundle.Messages({
        "ProgressPanel.button.ok=&OK",
        "ProgressPanel.details.output=Details can be reviewed in Output window."
    })
    private void finishInternal(final String message, final boolean cancel) {
        syncRunning = false;
        finishProgress(cancel);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressMessageLabel.setText(Bundle.ProgressPanel_details_output());
                Mnemonics.setLocalizedText(actionButton, Bundle.ProgressPanel_button_ok());
                if (!error) {
                    if (autoCloseCheckBox.isSelected()) {
                        dialog.dispose();
                    } else {
                        if (cancel) {
                            notificationLineSupport.setWarningMessage(message);
                        } else {
                            notificationLineSupport.setInformationMessage(message);
                        }
                    }
                }
            }
        });
    }

    public void downloadErrorOccured() {
        summaryPanel.downloadError();
        errorOccurred();
    }

    public void uploadErrorOccured() {
        summaryPanel.uploadError();
        errorOccurred();
    }

    public void deleteErrorOccured() {
        summaryPanel.deleteError();
        errorOccurred();
    }

    @NbBundle.Messages({
        "ProgressPanel.error=<html><b>Error occurred during synchronization.</b>"
    })
    private void errorOccurred() {
        if (error) {
            // error already set
            return;
        }
        error = true;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                notificationLineSupport.setErrorMessage(Bundle.ProgressPanel_error());
            }
        });
    }

    @NbBundle.Messages({
        "# {0} - file name",
        "ProgressPanel.uploading=Uploading {0}..."
    })
    public void decreaseUploadNumber(SyncItem syncItem) {
        progress(syncItem, Bundle.ProgressPanel_uploading(syncItem.getName()));
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                summaryPanel.decreaseUploadNumber();
            }
        });
    }

    @NbBundle.Messages({
        "# {0} - file name",
        "ProgressPanel.downloading=Downloading {0}..."
    })
    public void decreaseDownloadNumber(SyncItem syncItem) {
        progress(syncItem, Bundle.ProgressPanel_downloading(syncItem.getName()));
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                summaryPanel.decreaseDownloadNumber();
            }
        });
    }

    public void setDeleteNumber(final int number) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                summaryPanel.setDeleteNumber(number);
            }
        });
    }

    public void decreaseNoopNumber() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                summaryPanel.decreaseNoopNumber();
            }
        });
    }

    private void progress(SyncItem syncItem, String message) {
        if (syncItem.getOperation().hasProgress()) {
            workUnits += syncItem.getSize() / 1000;
            progressHandle.progress(message, workUnits);
        }
    }

    private void finishProgress(boolean cancel) {
        if (workUnits == 0) {
            // no sync at all
            progressHandle.progress(" ", NO_SYNC_UNITS); // NOI18N
        } else {
            progressHandle.progress(" "); // NOI18N
        }
        if (!cancel) {
            progressHandle.finish();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        autoCloseCheckBox = new JCheckBox();
        summaryPanelHolder = new JPanel();
        progressPanelHolder = new JPanel();
        progressMessagePanelHolder = new JPanel();

        Mnemonics.setLocalizedText(autoCloseCheckBox, NbBundle.getMessage(ProgressPanel.class, "ProgressPanel.autoCloseCheckBox.text")); // NOI18N

        summaryPanelHolder.setLayout(new BorderLayout());

        progressPanelHolder.setLayout(new BorderLayout());

        progressMessagePanelHolder.setLayout(new BorderLayout());

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addComponent(summaryPanelHolder, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(progressPanelHolder, GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                    .addComponent(progressMessagePanelHolder, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(summaryPanelHolder, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(progressPanelHolder, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(progressMessagePanelHolder, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox autoCloseCheckBox;
    private JPanel progressMessagePanelHolder;
    private JPanel progressPanelHolder;
    private JPanel summaryPanelHolder;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class ActionButtionListener implements ActionListener {

        private final AtomicBoolean cancel;


        public ActionButtionListener(AtomicBoolean cancel) {
            this.cancel = cancel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (syncRunning) {
                // cancel
                cancel.set(true);
            } else {
                // ok -> close the dialog & free all resources
                progressHandle.finish();
                dialog.dispose();
            }
        }

    }

}
