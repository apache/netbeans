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

/*
 * ConstructorPanel.java
 *
 * Created on Jul 20, 2008, 10:34:25 PM
 */

package org.netbeans.modules.php.editor.codegen.ui;

import java.awt.Dimension;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.php.editor.codegen.CGSGenerator;
import org.netbeans.modules.php.editor.codegen.CGSInfo;
import org.netbeans.modules.php.editor.codegen.ComboBoxModelElement;
import org.netbeans.modules.php.editor.codegen.Property;

/**
 *
 * @author Petr Pisl
 */
public class ConstructorPanel extends JPanel {

    protected final String className;
    protected final List<? extends Property> properties;
    protected final CGSInfo cgsInfo;

    public ConstructorPanel(CGSGenerator.GenType genType, CGSInfo cgsInfo) {
        initComponents();
        this.className = cgsInfo.getClassName();
        switch (genType) {
            case CONSTRUCTOR: properties = cgsInfo.getInstanceProperties(); break;
            case GETTER: properties = cgsInfo.getPossibleGetters(); break;
            case SETTER: properties = cgsInfo.getPossibleSetters(); break;
            case METHODS: properties = cgsInfo.getPossibleMethods(); break;
            default: properties = cgsInfo.getPossibleGettersSetters(); break;
        }
        this.cgsInfo = cgsInfo;
        initPanel(genType);
        initTree();
    }

    private void initPanel(CGSGenerator.GenType genType) {
        boolean customizeMethodGeneration = true;
        String name = "";
        if (properties.size() > 0) {
            name = properties.get(0).getName();
        }
        ComboBoxModel model = genType.getModel(name);
        if (genType.equals(CGSGenerator.GenType.METHODS)) {
            customizeMethodGeneration = false;
            Dimension preferredSize = getPreferredSize();
            setPreferredSize(new Dimension((int) (preferredSize.getWidth() * 1.3), (int) (preferredSize.getHeight() * 1.3)));
        }
        this.label.setText(genType.getPanelTitle());
        this.pGSCustomize.setVisible(customizeMethodGeneration);
        if (customizeMethodGeneration) {
            cbMethodGeneration.setModel(model);
            int index = 0;
            if (cgsInfo.getHowToGenerate() != null) {
                for (int i = 0; i < model.getSize(); i++) {
                    Object modelElement = model.getElementAt(index);
                    assert modelElement instanceof ComboBoxModelElement;
                    if (cgsInfo.getHowToGenerate().equals(((ComboBoxModelElement) modelElement).getGenWay())) {
                        break;
                    }
                    index = i;
                }
            }
            cbMethodGeneration.setSelectedIndex(index);
        }
        cbGenerateDoc.setSelected(cgsInfo.isGenerateDoc());
        cbGenerateDoc.setVisible(false);
        fluentSetterCheckBox.setVisible(genType.isFluentSetterVisible());
        fluentSetterCheckBox.setSelected(cgsInfo.isFluentSetter());
        publicModifierCheckBox.setVisible(genType.isPublicModifierVisible());
        publicModifierCheckBox.setSelected(cgsInfo.isPublicModifier());
    }

    private void initTree() {
        JTree tree = new JTree(getRootNode());
        tree.setCellRenderer(new CheckBoxTreeRenderer());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.putClientProperty("JTree.lineStyle", "Angled");  //NOI18N
        NodeSelectionListener listener = new NodeSelectionListener(tree);
        tree.addMouseListener(listener);
        tree.addKeyListener(listener);
        tree.expandRow(0);
        tree.setShowsRootHandles(true);
        tree.setSelectionRow(0);

        initTree(tree);

        scrollPane.add(tree);
        scrollPane.setViewportView(tree);
    }

    protected MutableTreeNode getRootNode() {
        CheckNode root = new CheckNode.CGSClassNode(className);
        for (Property property : properties) {
            root.add(new CheckNode.CGSPropertyNode(property));
        }
        return root;
    }

    protected void initTree(JTree tree) {
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")  //NOI18N
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        label = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        pGSCustomize = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cbMethodGeneration = new javax.swing.JComboBox();
        cbGenerateDoc = new javax.swing.JCheckBox();
        fluentSetterCheckBox = new javax.swing.JCheckBox();
        publicModifierCheckBox = new javax.swing.JCheckBox();

        label.setDisplayedMnemonic('G');
        label.setLabelFor(scrollPane);
        label.setText(org.openide.util.NbBundle.getMessage(ConstructorPanel.class, "ConstructorPanel.label.text")); // NOI18N

        jLabel1.setDisplayedMnemonic('M');
        jLabel1.setLabelFor(cbMethodGeneration);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(ConstructorPanel.class, "ConstructorPanel.jLabel1.text")); // NOI18N

        cbMethodGeneration.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbMethodGeneration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbMethodGenerationActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pGSCustomizeLayout = new javax.swing.GroupLayout(pGSCustomize);
        pGSCustomize.setLayout(pGSCustomizeLayout);
        pGSCustomizeLayout.setHorizontalGroup(
            pGSCustomizeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pGSCustomizeLayout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbMethodGeneration, 0, 259, Short.MAX_VALUE))
        );
        pGSCustomizeLayout.setVerticalGroup(
            pGSCustomizeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pGSCustomizeLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pGSCustomizeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cbMethodGeneration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(65, 65, 65))
        );

        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConstructorPanel.class, "ConstructorPanel.jLabel1.AccessibleContext.accessibleName")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConstructorPanel.class, "ConstructorPanel.jLabel1.AccessibleContext.accessibleDescription")); // NOI18N
        cbMethodGeneration.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConstructorPanel.class, "ConstructorPanel.cbMethodGeneration.AccessibleContext.accessibleName")); // NOI18N
        cbMethodGeneration.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConstructorPanel.class, "ConstructorPanel.cbMethodGeneration.AccessibleContext.accessibleDescription")); // NOI18N

        cbGenerateDoc.setMnemonic('e');
        cbGenerateDoc.setText(org.openide.util.NbBundle.getMessage(ConstructorPanel.class, "LBL_Generate_Documentation")); // NOI18N
        cbGenerateDoc.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        cbGenerateDoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbGenerateDocActionPerformed(evt);
            }
        });

        fluentSetterCheckBox.setMnemonic('F');
        fluentSetterCheckBox.setText(org.openide.util.NbBundle.getMessage(ConstructorPanel.class, "ConstructorPanel.fluentSetterCheckBox.text")); // NOI18N
        fluentSetterCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fluentSetterCheckBoxActionPerformed(evt);
            }
        });

        publicModifierCheckBox.setMnemonic('p');
        publicModifierCheckBox.setText(org.openide.util.NbBundle.getMessage(ConstructorPanel.class, "ConstructorPanel.publicModifierCheckBox.text")); // NOI18N
        publicModifierCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                publicModifierCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
                    .addComponent(cbGenerateDoc, javax.swing.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
                    .addComponent(pGSCustomize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(publicModifierCheckBox)
                            .addComponent(label)
                            .addComponent(fluentSetterCheckBox))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(label)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pGSCustomize, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbGenerateDoc)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fluentSetterCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(publicModifierCheckBox)
                .addContainerGap())
        );

        label.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConstructorPanel.class, "ConstructorPanel.label.AccessibleContext.accessibleName")); // NOI18N
        label.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConstructorPanel.class, "ConstructorPanel.label.AccessibleContext.accessibleDescription")); // NOI18N
        scrollPane.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConstructorPanel.class, "ConstructorPanel.scrollPane.AccessibleContext.accessibleName")); // NOI18N
        scrollPane.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConstructorPanel.class, "ConstructorPanel.scrollPane.AccessibleContext.accessibleDescription")); // NOI18N
        pGSCustomize.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConstructorPanel.class, "ConstructorPanel.pGSCustomize.AccessibleContext.accessibleName")); // NOI18N
        pGSCustomize.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConstructorPanel.class, "ConstructorPanel.pGSCustomize.AccessibleContext.accessibleDescription")); // NOI18N
        cbGenerateDoc.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConstructorPanel.class, "ConstructorPanel.cbGenerateDoc.AccessibleContext.accessibleName")); // NOI18N
        cbGenerateDoc.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConstructorPanel.class, "ConstructorPanel.cbGenerateDoc.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConstructorPanel.class, "ConstructorPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConstructorPanel.class, "ConstructorPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void cbMethodGenerationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbMethodGenerationActionPerformed
        Object selectedItem = cbMethodGeneration.getSelectedItem();
        assert selectedItem instanceof ComboBoxModelElement;
        cgsInfo.setHowToGenerate(((ComboBoxModelElement) selectedItem).getGenWay());
    }//GEN-LAST:event_cbMethodGenerationActionPerformed

    private void cbGenerateDocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbGenerateDocActionPerformed
        cgsInfo.setGenerateDoc(cbGenerateDoc.isSelected());
    }//GEN-LAST:event_cbGenerateDocActionPerformed

    private void fluentSetterCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fluentSetterCheckBoxActionPerformed
        cgsInfo.setFluentSetter(fluentSetterCheckBox.isSelected());
    }//GEN-LAST:event_fluentSetterCheckBoxActionPerformed

    private void publicModifierCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publicModifierCheckBoxActionPerformed
        cgsInfo.setPublicModifier(publicModifierCheckBox.isSelected());
    }//GEN-LAST:event_publicModifierCheckBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbGenerateDoc;
    private javax.swing.JComboBox cbMethodGeneration;
    private javax.swing.JCheckBox fluentSetterCheckBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel label;
    private javax.swing.JPanel pGSCustomize;
    private javax.swing.JCheckBox publicModifierCheckBox;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables

}
