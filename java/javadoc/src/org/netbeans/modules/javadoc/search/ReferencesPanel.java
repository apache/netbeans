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

package org.netbeans.modules.javadoc.search;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.net.URL;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * UI to display javadoc references in details.
 * <p>Usage: {@code ReferencesPanel.showInWindow()}
 *
 * @see IndexBuilder
 * @see org.netbeans.modules.javadoc.search.IndexOverviewAction
 * 
 * @author Jan Pokorsky
 */
public class ReferencesPanel extends javax.swing.JPanel implements Runnable, ListSelectionListener {

    private static final String PLEASE_WAIT = NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.wait.text");
    private static final Object LOCK = new Object();
    private static final String EMPTY_LOCATION = ""; // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(ReferencesPanel.class.getName(), 1, false, false);
    private int state = 0;
    private ListModel model;
    /** Descriptions of indices that should be accessed under {@link #LOCK lock}. */
    private ItemDesc[] items;
    private final AbstractButton openBtn;

    /** Creates new form ReferencesPanel */
    public ReferencesPanel(AbstractButton openBtn) {
        initComponents();
        Mnemonics.setLocalizedText(listLabel, NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.listLabel.text"));
        Mnemonics.setLocalizedText(locationLabel, NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.locationLabel.text"));
        this.openBtn = openBtn;
    }

    public static URL showInWindow() {
        JButton openBtn = new JButton();
        Mnemonics.setLocalizedText(openBtn, NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.ok.text"));
        openBtn.getAccessibleContext().setAccessibleDescription(openBtn.getText());
        openBtn.setEnabled(false);

        final Object[] buttons = new Object[] { openBtn, DialogDescriptor.CANCEL_OPTION };

        ReferencesPanel panel = new ReferencesPanel(openBtn);
        DialogDescriptor desc = new DialogDescriptor(
                panel,
                NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.title"),
                true,
                buttons,
                openBtn,
                DialogDescriptor.DEFAULT_ALIGN,
                HelpCtx.DEFAULT_HELP,
                null);
        desc.setClosingOptions(buttons);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(desc);
        dialog.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ReferencesPanel.class, "AN_ReferencesDialog"));
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ReferencesPanel.class, "AD_ReferencesDialog"));

        // schedule computing indices
        RP.post(panel);
        dialog.setVisible(true);
        dialog.dispose();

        return desc.getValue() == openBtn
                ? panel.getSelectedItem()
                : null;
    }

    public @Override void run() {
        switch (state) {
            case 0:
                runGetIndiciesTask();
                break;
            case 1:
                runListUpdateTask();
                break;
        }
    }

    private void runGetIndiciesTask() {
        final List<IndexBuilder.Index> data = IndexBuilder.getDefault().getIndices(true);

        synchronized (LOCK) {

            ItemDesc[] modelItems;
            if (data == null || data.isEmpty()) {
                modelItems = new ItemDesc[] { ItemDesc.noItem() };
            } else {
                modelItems = new ItemDesc[data.size()];
                this.items = modelItems;
                int i = 0;
                for (IndexBuilder.Index index : data) {
                    modelItems[i] = new ItemDesc(index.display, index.fo);
                    i++;
                }
            }
            
            model = new FixListModel(modelItems);
        }

        state = 1;
        EventQueue.invokeLater(this);
    }

    private void runListUpdateTask() {
        refList.setModel(model);
        refList.addListSelectionListener(this);
        refList.setSelectedIndex(0);
    }

    public @Override void valueChanged(ListSelectionEvent e) {
        URL item = getSelectedItem();
        String s = item == null
                ? EMPTY_LOCATION
                : URLUtils.getDisplayName(item);
        locationField.setText(s);
        openBtn.setEnabled(item != null);
    }

    URL getSelectedItem() {
        int index = refList.getSelectedIndex();
        synchronized (LOCK) {
            return index < 0 || items == null || items.length == 0
                    ? null
                    : items[index].location;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        listLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        refList = new javax.swing.JList(new String[] {PLEASE_WAIT});
        locationField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();

        listLabel.setLabelFor(refList);
        listLabel.setText(org.openide.util.NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.listLabel.text")); // NOI18N

        refList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(refList);
        refList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.refList.AccessibleContext.accessibleName")); // NOI18N
        refList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.refList.AccessibleContext.accessibleDescription")); // NOI18N

        locationField.setEditable(false);

        locationLabel.setLabelFor(locationField);
        locationLabel.setText(org.openide.util.NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.locationLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .addComponent(listLabel, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(locationField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .addComponent(locationLabel, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(listLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(locationLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(locationField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        listLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.listLabel.AccessibleContext.accessibleName")); // NOI18N
        listLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.listLabel.AccessibleContext.accessibleDescription")); // NOI18N
        locationLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.locationLabel.AccessibleContext.accessibleName")); // NOI18N
        locationLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.locationLabel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel listLabel;
    private javax.swing.JTextField locationField;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JList refList;
    // End of variables declaration//GEN-END:variables

    private static final class ItemDesc {
        private static ItemDesc NO_ITEM;
        String name;
        String locationName;
        URL location;

        public ItemDesc(String name, URL location) {
            this.name = name;
            this.location = location;
        }

        String getLocationName() {
            if (locationName == null) {
                locationName = URLUtils.getDisplayName(location);
            }
            return locationName;
        }

        static ItemDesc noItem() {
            if (NO_ITEM == null) {
                NO_ITEM = new ItemDesc(NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.noJavadoc"), null);
            }
            return NO_ITEM;
        }

    }

    private static final class FixListModel implements ListModel {

        private ItemDesc[] items;

        public FixListModel(ItemDesc[] items) {
            this.items = items;
        }

        public @Override int getSize() {
            return items.length;
        }

        public @Override Object getElementAt(int index) {
            return items[index].name;
        }

        public @Override void addListDataListener(ListDataListener l) {
            // no op
        }

        public @Override void removeListDataListener(ListDataListener l) {
            // no op
        }

    }
}
