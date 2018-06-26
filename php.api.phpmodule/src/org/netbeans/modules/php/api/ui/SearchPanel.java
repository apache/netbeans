/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
        foundItemsList.setListData(foundItems.toArray(new String[foundItems.size()]));
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
