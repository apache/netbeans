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
package org.netbeans.modules.cnd.completion.options;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.AbstractListModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.cnd.completion.cplusplus.CsmCompletionUtils;
import org.netbeans.modules.cnd.completion.cplusplus.CsmCompletionUtils.DocProvider;
import org.netbeans.modules.cnd.completion.cplusplus.CsmCompletionUtils.DocProviderList;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 *
 *
 */
public class CodeCompletionPanel extends javax.swing.JPanel implements DocumentListener {

    private final Preferences preferences;
    private final Map<String, Object> id2Saved = new HashMap<String, Object>();
    private final DocProviderListModel docProviderListModel;

    /** Creates new form CodeCompletionPanel */
    public CodeCompletionPanel(Preferences preferences) {
        this.preferences = preferences;
        initComponents();
        autoInsertIncludeDirectives.setSelected(preferences.getBoolean(CsmCompletionUtils.CPP_AUTO_INSERT_INCLUDE_DIRECTIVES, true));
        autoCompletionTriggersField.setText(preferences.get(CsmCompletionUtils.CPP_AUTO_COMPLETION_TRIGGERS, CsmCompletionUtils.CPP_AUTO_COMPLETION_TRIGGERS_DEFAULT));
        autoCompletionTriggersPreprocField.setText(preferences.get(CsmCompletionUtils.PREPRPOC_AUTO_COMPLETION_TRIGGERS, CsmCompletionUtils.PREPRPOC_AUTO_COMPLETION_TRIGGERS_DEFAULT));
        autoCompletionTriggersField.getDocument().addDocumentListener(this);
        autoCompletionTriggersPreprocField.getDocument().addDocumentListener(this);
        
        documentationPanel.setVisible(false);
        documentationCheckBox.setSelected(false);
        documentationCheckBox.setIcon(getCollapsedIcon());
        
        docProviderListModel = new DocProviderListModel(CsmCompletionUtils.getDocProviderList());
        docProviderList.setModel(docProviderListModel);
        docProviderList.setCellRenderer(new DocProviderCheckboxListRenderer());
        docProviderList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                JList<DocProviderCheckboxListItem> list = (JList<DocProviderCheckboxListItem>) event.getSource();
                int index = list.locationToIndex(event.getPoint());
                if (index != -1) {
                    Rectangle checkBounds = ((DocProviderCheckboxListRenderer)list.getCellRenderer()).check.getBounds();
                    if (event.getPoint().x <= checkBounds.width) {
                        DocProviderCheckboxListItem item = (DocProviderCheckboxListItem) list.getModel().getElementAt(index);
                        item.setSelected(!item.isSelected());
                        list.repaint(list.getCellBounds(index, index));
                        preferences.put(CsmCompletionUtils.DOC_PROVIDER_LIST, docProviderListModel.toMethodList().toStorageString());
                    }
                }
            }
        });
        docProviderList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_SPACE) {
                    DocProviderCheckboxListItem item = docProviderList.getSelectedValue();
                    if (item != null) {
                        int index = docProviderList.getSelectedIndex();
                        item.setSelected(!item.isSelected());
                        docProviderList.repaint(docProviderList.getCellBounds(index, index));
                        preferences.put(CsmCompletionUtils.DOC_PROVIDER_LIST, docProviderListModel.toMethodList().toStorageString());
                    }
                }
            }
        });
        docProviderList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                int index = docProviderList.getSelectedIndex();
                if (index == -1) {
                    upButton.setEnabled(false);
                    downButton.setEnabled(false);
                    return;
                }
                if (index >= 0 && index < docProviderListModel.getSize() - 1) {
                    downButton.setEnabled(true);
                } else {
                    downButton.setEnabled(false);
                }
                if (index >= 1 && index < docProviderListModel.getSize()) {
                    upButton.setEnabled(true);
                } else {
                    upButton.setEnabled(false);
                }
            }
        });
        
        id2Saved.put(CsmCompletionUtils.CPP_AUTO_INSERT_INCLUDE_DIRECTIVES, autoInsertIncludeDirectives.isSelected());
        id2Saved.put(CsmCompletionUtils.CPP_AUTO_COMPLETION_TRIGGERS, autoCompletionTriggersField.getText());
        id2Saved.put(CsmCompletionUtils.PREPRPOC_AUTO_COMPLETION_TRIGGERS, autoCompletionTriggersPreprocField.getText());
        id2Saved.put(CsmCompletionUtils.DOC_PROVIDER_LIST, CsmCompletionUtils.getDocProviderList().toStorageString());
    }

    public static PreferencesCustomizer.Factory getCustomizerFactory() {
        return new PreferencesCustomizer.Factory() {

            @Override
            public PreferencesCustomizer create(Preferences preferences) {
                return new CodeCompletionPreferencesCusromizer(preferences);
            }
        };
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        autoCompletionTriggersLabel = new javax.swing.JLabel();
        autoCompletionTriggersField = new javax.swing.JTextField();
        autoInsertIncludeDirectives = new javax.swing.JCheckBox();
        autoCompletionTriggersPreprocLabel = new javax.swing.JLabel();
        autoCompletionTriggersPreprocField = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        documentationCheckBox = new javax.swing.JCheckBox();
        documentationPanel = new javax.swing.JPanel();
        docimentationListLabel = new javax.swing.JLabel();
        documentationScrollPane = new javax.swing.JScrollPane();
        docProviderList = new javax.swing.JList<>();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();

        autoCompletionTriggersLabel.setLabelFor(autoCompletionTriggersField);
        org.openide.awt.Mnemonics.setLocalizedText(autoCompletionTriggersLabel, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionTriggersLabel.text")); // NOI18N

        autoCompletionTriggersField.setAlignmentX(1.0F);

        autoInsertIncludeDirectives.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(autoInsertIncludeDirectives, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoInclude.text")); // NOI18N
        autoInsertIncludeDirectives.setBorder(null);
        autoInsertIncludeDirectives.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoInsertIncludeDirectivesActionPerformed(evt);
            }
        });

        autoCompletionTriggersPreprocLabel.setLabelFor(autoCompletionTriggersPreprocField);
        org.openide.awt.Mnemonics.setLocalizedText(autoCompletionTriggersPreprocLabel, org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionTriggersPreprocLabel.text")); // NOI18N

        autoCompletionTriggersPreprocField.setAlignmentX(1.0F);

        documentationCheckBox.setText(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.documentationCheckBox.text")); // NOI18N
        documentationCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                documentationCheckBoxActionPerformed(evt);
            }
        });

        docimentationListLabel.setText(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.docimentationListLabel.text")); // NOI18N

        documentationScrollPane.setViewportView(docProviderList);

        upButton.setText(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.upButton.text")); // NOI18N
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });

        downButton.setText(org.openide.util.NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.downButton.text")); // NOI18N
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout documentationPanelLayout = new javax.swing.GroupLayout(documentationPanel);
        documentationPanel.setLayout(documentationPanelLayout);
        documentationPanelLayout.setHorizontalGroup(
            documentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(documentationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(documentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(documentationPanelLayout.createSequentialGroup()
                        .addComponent(documentationScrollPane)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(documentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(downButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(upButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(docimentationListLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        documentationPanelLayout.setVerticalGroup(
            documentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(documentationPanelLayout.createSequentialGroup()
                .addComponent(docimentationListLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(documentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(documentationPanelLayout.createSequentialGroup()
                        .addComponent(upButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(downButton))
                    .addComponent(documentationScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(autoCompletionTriggersLabel)
                            .addComponent(autoCompletionTriggersPreprocLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(autoCompletionTriggersField, javax.swing.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
                            .addComponent(autoCompletionTriggersPreprocField)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(autoInsertIncludeDirectives)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(documentationCheckBox)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(documentationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(autoCompletionTriggersLabel)
                    .addComponent(autoCompletionTriggersField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(autoInsertIncludeDirectives)
                .addGap(24, 24, 24)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(autoCompletionTriggersPreprocLabel)
                    .addComponent(autoCompletionTriggersPreprocField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(documentationCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(documentationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void autoInsertIncludeDirectivesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoInsertIncludeDirectivesActionPerformed
        preferences.putBoolean(CsmCompletionUtils.CPP_AUTO_INSERT_INCLUDE_DIRECTIVES, autoInsertIncludeDirectives.isSelected());
}//GEN-LAST:event_autoInsertIncludeDirectivesActionPerformed

    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        int selectedIndex = docProviderList.getSelectedIndex();
        if (selectedIndex <= 0) {
            return;
        }
        docProviderListModel.swapElement(selectedIndex, selectedIndex-1);
        docProviderList.repaint(docProviderList.getCellBounds(selectedIndex-1, selectedIndex));
        docProviderList.setSelectedIndex(selectedIndex-1);
        preferences.put(CsmCompletionUtils.DOC_PROVIDER_LIST, docProviderListModel.toMethodList().toStorageString());
    }//GEN-LAST:event_upButtonActionPerformed

    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        int selectedIndex = docProviderList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= docProviderListModel.getSize() -1 ) {
            return;
        }
        docProviderListModel.swapElement(selectedIndex, selectedIndex+1);
        docProviderList.repaint(docProviderList.getCellBounds(selectedIndex, selectedIndex+1));
        docProviderList.setSelectedIndex(selectedIndex+1);
        preferences.put(CsmCompletionUtils.DOC_PROVIDER_LIST, docProviderListModel.toMethodList().toStorageString());
    }//GEN-LAST:event_downButtonActionPerformed

    private void documentationCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_documentationCheckBoxActionPerformed
        documentationPanel.setVisible(documentationCheckBox.isSelected());
        documentationCheckBox.setIcon(documentationCheckBox.isSelected() ? getExpandedIcon() : getCollapsedIcon());
    }//GEN-LAST:event_documentationCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField autoCompletionTriggersField;
    private javax.swing.JLabel autoCompletionTriggersLabel;
    private javax.swing.JTextField autoCompletionTriggersPreprocField;
    private javax.swing.JLabel autoCompletionTriggersPreprocLabel;
    private javax.swing.JCheckBox autoInsertIncludeDirectives;
    private javax.swing.JList<DocProviderCheckboxListItem> docProviderList;
    private javax.swing.JLabel docimentationListLabel;
    private javax.swing.JCheckBox documentationCheckBox;
    private javax.swing.JPanel documentationPanel;
    private javax.swing.JScrollPane documentationScrollPane;
    private javax.swing.JButton downButton;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton upButton;
    // End of variables declaration//GEN-END:variables

    @Override
    public void insertUpdate(DocumentEvent e) {
        update(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        update(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        update(e);
    }

    private void update(DocumentEvent e) {
        if (e.getDocument() == autoCompletionTriggersField.getDocument()) {
            preferences.put(CsmCompletionUtils.CPP_AUTO_COMPLETION_TRIGGERS, autoCompletionTriggersField.getText());
        } else if (e.getDocument() == autoCompletionTriggersPreprocField.getDocument()) {
            preferences.put(CsmCompletionUtils.PREPRPOC_AUTO_COMPLETION_TRIGGERS, autoCompletionTriggersPreprocField.getText());
        }
    }
    
//<editor-fold defaultstate="collapsed" desc="Expanded Icon">
    private static final boolean isGtk = "GTK".equals(UIManager.getLookAndFeel().getID()); //NOI18N
    
    private static Icon getExpandedIcon() {
        return UIManager.getIcon(isGtk ? "Tree.gtk_expandedIcon" : "Tree.expandedIcon"); //NOI18N
    }
    
    private static Icon getCollapsedIcon() {
        return UIManager.getIcon(isGtk ? "Tree.gtk_collapsedIcon" : "Tree.collapsedIcon"); //NOI18N
    }
    
    String getSavedValue(String key) {
        return id2Saved.get(key).toString();
    }
//</editor-fold>
    
    private static class CodeCompletionPreferencesCusromizer implements PreferencesCustomizer {

        private final Preferences preferences;
        private CodeCompletionPanel component;

        private CodeCompletionPreferencesCusromizer(Preferences p) {
            preferences = p;
        }

        @Override
        public String getId() {
            return "org.netbeans.modules.cnd.completion.options"; //NOI18N
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanelName"); // NOI18N
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("netbeans.optionsDialog.editor.codeCompletion.cpp"); //NOI18N
        }

        @Override
        public JComponent getComponent() {
            if (component == null) {
                component = new CodeCompletionPanel(preferences);
            }
            return component;
        }
    }

    public static final class CustomCustomizerImpl extends PreferencesCustomizer.CustomCustomizer {

        @Override
        public String getSavedValue(PreferencesCustomizer customCustomizer, String key) {
            if (customCustomizer instanceof CodeCompletionPreferencesCusromizer) {
                return ((CodeCompletionPanel) customCustomizer.getComponent()).getSavedValue(key);
            }
            return null;
        }
    }    
    
    private class DocProviderCheckboxListItem {

        private final DocProvider provider;
        private boolean isSelected;

        private DocProviderCheckboxListItem(DocProvider provider, boolean selected) {
            this.provider = provider;
            this.isSelected = selected;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }

        public String getDisplayName() {
            return provider.getDisplayName();
        }

        @Override
        public String toString() {
            return provider.toString() + ' ' + isSelected; //NOI18N
        }

        public Pair<DocProvider, Boolean> toPair() {
            return Pair.of(provider, isSelected);
        }
    }

    private static class DocProviderCheckboxListRenderer extends JPanel implements ListCellRenderer<DocProviderCheckboxListItem> {

        private final JCheckBox check;
        private final JLabel label;
        private final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        private final Border focusBorder = new EtchedBorder();

        private DocProviderCheckboxListRenderer() {
            check = new JCheckBox();
            label = new JLabel();
            this.setLayout(new BorderLayout());
            this.add(check, BorderLayout.WEST);
            this.add(label, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends DocProviderCheckboxListItem> list, DocProviderCheckboxListItem value,
                int index, boolean isSelected, boolean cellHasFocus) {
            final Color bc;
            final Color fc;
            if (isSelected) {
                bc = UIManager.getColor("List.selectionBackground"); //NOI18N
                fc = UIManager.getColor("List.selectionForeground"); //NOI18N
            } else {
                bc = list.getBackground();
                fc = list.getForeground();
            }
            setBackground(bc); // NOI18N
            setForeground(fc); // NOI18N

            label.setBackground(bc);
            label.setForeground(fc);
            label.setText(value.getDisplayName());
            label.setFont(list.getFont());

            check.setSelected(value.isSelected());
            check.setBackground(bc);
            check.setForeground(fc);
            check.setEnabled(list.isEnabled());

            Border border;
            if (cellHasFocus) {
                border = focusBorder;
            } else {
                border = noFocusBorder;
            }
            setBorder(border);

            return this;
        }
    }

    private final class DocProviderListModel extends AbstractListModel<DocProviderCheckboxListItem> {

        private final ArrayList<DocProviderCheckboxListItem> list = new ArrayList<>(2);

        private DocProviderListModel(DocProviderList methodsList) {
            if (methodsList == null || methodsList.isEmpty()) {
                resetModel(DocProviderList.DEFAULT_LIST);
            } else {
                resetModel(methodsList);
            }
        }

        @Override
        public int getSize() {
            return list.size();
        }

        @Override
        public DocProviderCheckboxListItem getElementAt(int index) {
            return list.get(index);
        }

        private void swapElement(int from, int to) {
            DocProviderCheckboxListItem fromItem = list.get(from);
            list.set(from, list.get(to));
            list.set(to, fromItem);
        }

        private void resetModel(DocProviderList methodsList) {
            DocProvider[] methods = methodsList.getProviders();
            for (int i = 0; i < methods.length; i++) {
                DocProvider method = methods[i];
                boolean enabled = methodsList.isEnabled(method);
                DocProviderCheckboxListItem authenticationCheckboxListItem = new DocProviderCheckboxListItem(method, enabled);
                if (i == list.size()) {
                    list.add(authenticationCheckboxListItem);
                } else {
                    list.set(i, authenticationCheckboxListItem);
                }
            }
        }

        private DocProviderList toMethodList() {
            List<Pair<DocProvider, Boolean>> pairs = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                DocProviderCheckboxListItem m = list.get(i);
                pairs.add(Pair.of(m.provider, m.isSelected));
            }
            return new DocProviderList(pairs);
        }
    }
}
