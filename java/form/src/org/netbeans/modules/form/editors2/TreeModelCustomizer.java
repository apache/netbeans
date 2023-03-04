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
package org.netbeans.modules.form.editors2;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.TreeModel;

/**
 * Property customizer for <code>TreeModel</code>.
 *
 * @author Jan Stola
 */
public class TreeModelCustomizer extends javax.swing.JPanel {
    /** Property editor that invoked the customizer. */
    private TreeModelEditor propEditor;

    /**
     * Creates new form <code>TreeModelCustomizer</code>.
     * 
     * @param propEditor property editor that invoked the customizer.
     */
    public TreeModelCustomizer(TreeModelEditor propEditor) {
        this.propEditor = propEditor;
        initComponents();
        String code = propEditor.getCodeValue();
        if (code == null) {
            String defaultModel = "JTree\n colors\n  blue\n  violet\n  red\n  yellow\n" // NOI18N
                + " sports\n  basketball\n  soccer\n  football\n  hockey\n" // NOI18N
                + " food\n  hot dogs\n  pizza\n  ravioli\n  bananas"; // NOI18N
            textArea.setText(defaultModel);
            expandTree();
        }
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateTree();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateTree();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateTree();
            }
        });
        if (code != null) {
            textArea.setText(code);
            updateTree();
        }
    }

    /**
     * Updates the tree according to the textual representation of the tree
     * model in the text area.
     */
    private void updateTree() {
        String txt = textArea.getText();
        int tabSize = textArea.getTabSize();
        StringBuilder sb = new StringBuilder(tabSize);
        for (int i=0; i<tabSize; i++) {
            sb.append(" "); // NOI18N
        }
        txt = txt.replace("\t", sb.toString()); // NOI18N
        TreeModel model = propEditor.createTreeModel(txt);
        tree.setModel(model);
        expandTree();
        propEditor.setValue(model);
    }
    
    /**
     * Expands the whole tree.
     */
    private void expandTree() {
        for (int i=0; i<tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        splitPane = new javax.swing.JSplitPane();
        scrollPane1 = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();
        scrollPane2 = new javax.swing.JScrollPane();
        tree = new javax.swing.JTree();
        label = new javax.swing.JLabel();
        warningLabel = new javax.swing.JLabel();

        splitPane.setDividerLocation(150);

        scrollPane1.setViewportView(textArea);
        textArea.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TreeModelCustomizer.class, "ACSD_TreeModelEditor_TextArea")); // NOI18N

        splitPane.setLeftComponent(scrollPane1);

        scrollPane2.setViewportView(tree);
        tree.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TreeModelCustomizer.class, "ACSD_TreeModelEditor_Tree")); // NOI18N

        splitPane.setRightComponent(scrollPane2);

        label.setLabelFor(textArea);
        org.openide.awt.Mnemonics.setLocalizedText(label, org.openide.util.NbBundle.getMessage(TreeModelCustomizer.class, "TreeModelCustomizer.label.text")); // NOI18N

        warningLabel.setLabelFor(tree);
        org.openide.awt.Mnemonics.setLocalizedText(warningLabel, org.openide.util.NbBundle.getMessage(TreeModelCustomizer.class, "TreeModelCustomizer.warningLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(label, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addComponent(warningLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(warningLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel label;
    private javax.swing.JScrollPane scrollPane1;
    private javax.swing.JScrollPane scrollPane2;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JTextArea textArea;
    private javax.swing.JTree tree;
    private javax.swing.JLabel warningLabel;
    // End of variables declaration//GEN-END:variables

}
