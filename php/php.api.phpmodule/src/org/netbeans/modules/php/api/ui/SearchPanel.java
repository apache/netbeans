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

package org.netbeans.modules.php.api.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.php.api.util.UiUtils.SearchWindow.SearchWindowSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * Heavily inspired by Andrei's {@link org.netbeans.modules.spring.beans.ui.customizer.SelectConfigFilesPanel}.
 * @author Tomas Mysik
 */
public final class SearchPanel extends JPanel {
    private static final long serialVersionUID = 26389784456741L;

    private final RequestProcessor rp;
    private final SearchWindowSupport support;

    private List<String> foundItems;
    private DialogDescriptor descriptor;
    private Task detectTask;

    private SearchPanel(SearchWindowSupport support) {
        assert support != null;

        this.support = support;

        initComponents();

        rp = new RequestProcessor("PHP Search Panel detection thread (" + support.getPleaseWaitPart() + ")", 1, true); // NOI18N

        Mnemonics.setLocalizedText(detectedFilesLabel, support.getListTitle());
        messageLabel.setText(NbBundle.getMessage(SearchPanel.class, "LBL_PleaseWait", support.getPleaseWaitPart()));
    }

    public static SearchPanel create(SearchWindowSupport support) {
        return new SearchPanel(support);
    }

    public boolean open() {
        descriptor = new DialogDescriptor(this, support.getWindowTitle(), true, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelDetection();
            }
        });
        if (foundItems == null) {
            // no available items, will run the detection task
            descriptor.setValid(false);
            foundItemsList.setEnabled(true);
            progressBar.setIndeterminate(true);
            detectTask = rp.create(new Runnable() {
                @Override
                public void run() {
                    // just to be sure that the progress bar is displayed at least for a while
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        return;
                    }
                    final List<String> allItems = support.detect();
                    assert allItems != null;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            updateFoundItems(allItems);
                        }
                    });
                }
            });
            detectTask.schedule(0);
        } else {
            updateFoundItems(foundItems);
        }
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        try {
            dialog.setVisible(true);
        } finally {
            dialog.dispose();
        }
        return descriptor.getValue() == DialogDescriptor.OK_OPTION;
    }

    public List<String> getFoundItems() {
        return foundItems;
    }

    public String getSelectedItem() {
        return foundItemsList.getSelectedValue();
    }

    void cancelDetection() {
        if (detectTask != null) {
            detectTask.cancel();
        }
    }

    void updateFoundItems(List<String> foundItems) {
        this.foundItems = foundItems;
        foundItemsList.setEnabled(true);
        foundItemsList.setListData(foundItems.toArray(new String[0]));
        // In an attempt to hide the progress bar and label, but force the occupy the same space.
        String message = null;
        if (foundItems.isEmpty()) {
            message = support.getNoItemsFound();
        } else {
            message = " "; // NOI18N
            // preselect the 1st item
            foundItemsList.setSelectedIndex(0);
        }
        messageLabel.setText(message);
        progressBar.setIndeterminate(false);
        progressBar.setBorderPainted(false);
        progressBar.setBackground(getBackground());
        descriptor.setValid(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        detectedFilesLabel = new JLabel();
        foundItemsScrollPane = new JScrollPane();
        foundItemsList = new JList<String>();
        messageLabel = new JLabel();
        progressBar = new JProgressBar();

        detectedFilesLabel.setLabelFor(foundItemsList);
        Mnemonics.setLocalizedText(detectedFilesLabel, "title"); // NOI18N

        foundItemsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        foundItemsList.setEnabled(false);
        foundItemsScrollPane.setViewportView(foundItemsList);
        foundItemsList.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SearchPanel.class, "SelectPhpInterpreterPanel.phpInterpretersList.AccessibleContext.accessibleName")); // NOI18N
        foundItemsList.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SearchPanel.class, "SelectPhpInterpreterPanel.phpInterpretersList.AccessibleContext.accessibleDescription")); // NOI18N

        messageLabel.setLabelFor(progressBar);
        Mnemonics.setLocalizedText(messageLabel, "please wait..."); // NOI18N

        progressBar.setString(" "); // NOI18N
        progressBar.setStringPainted(true);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(foundItemsScrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                    .addComponent(progressBar, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                    .addComponent(detectedFilesLabel, Alignment.LEADING)
                    .addComponent(messageLabel, Alignment.LEADING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(detectedFilesLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(foundItemsScrollPane, GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(messageLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        detectedFilesLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SearchPanel.class, "SelectPhpInterpreterPanel.detectedFilesLabel.AccessibleContext.accessibleName")); // NOI18N
        detectedFilesLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SearchPanel.class, "SelectPhpInterpreterPanel.detectedFilesLabel.AccessibleContext.accessibleDescription")); // NOI18N
        foundItemsScrollPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SearchPanel.class, "SelectPhpInterpreterPanel.phpInterpretersScrollPane.AccessibleContext.accessibleName")); // NOI18N
        foundItemsScrollPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SearchPanel.class, "SelectPhpInterpreterPanel.phpInterpretersScrollPane.AccessibleContext.accessibleDescription")); // NOI18N
        messageLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SearchPanel.class, "SelectPhpInterpreterPanel.messageLabel.AccessibleContext.accessibleName")); // NOI18N
        messageLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SearchPanel.class, "SelectPhpInterpreterPanel.messageLabel.AccessibleContext.accessibleDescription")); // NOI18N
        progressBar.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SearchPanel.class, "SelectPhpInterpreterPanel.progressBar.AccessibleContext.accessibleName")); // NOI18N
        progressBar.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SearchPanel.class, "SelectPhpInterpreterPanel.progressBar.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(SearchPanel.class, "SelectPhpInterpreterPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SearchPanel.class, "SelectPhpInterpreterPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel detectedFilesLabel;
    private JList<String> foundItemsList;
    private JScrollPane foundItemsScrollPane;
    private JLabel messageLabel;
    private JProgressBar progressBar;
    // End of variables declaration//GEN-END:variables

    public static final class Strings {
        final String windowTitle;
        final String listTitle;
        final String pleaseWaitPart;
        final String noItemsFound;

        public Strings(String windowTitle, String listTitle, String pleaseWaitPart, String noItemsFound) {
            assert windowTitle != null;
            assert listTitle != null;
            assert pleaseWaitPart != null;
            assert noItemsFound != null;

            this.windowTitle = windowTitle;
            this.listTitle = listTitle;
            this.pleaseWaitPart = pleaseWaitPart;
            this.noItemsFound = noItemsFound;
        }
    }

    public static interface Detector {
        List<String> detect();
    }
}
