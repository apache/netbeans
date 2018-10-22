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

package org.netbeans.modules.gradle.java.customizer;

import org.netbeans.modules.gradle.java.api.GradleJavaSourceSet;
import org.netbeans.modules.gradle.spi.nodes.NodeUtils;
import java.awt.Component;
import java.io.File;
import java.nio.file.Path;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Laszlo Kishalmi
 */
public class SourceSetPanel extends javax.swing.JPanel {
    private static final String PROJECT_ICON = "org.netbeans.modules/gradle/resources/gradle.png"; //NOI18N
    private static final String ARTIFACT_ICON = "org.netbeans.modules/gradle/resources/module-artifact.png"; //NOI18N

    final Icon folderIcon = new ImageIcon(NodeUtils.getTreeFolderIcon(false));
    final Icon projectIcon = ImageUtilities.loadImageIcon(PROJECT_ICON, false);
    final Icon artifactIcon = ImageUtilities.loadImageIcon(ARTIFACT_ICON, false);

    final Path relativeRoot;
    final GradleJavaSourceSet sourceSet;
    DefaultMutableTreeNode sourcesRoot = new DefaultMutableTreeNode(new Object());

    /**
     * Creates new form SourceSetPanel
     */
    public SourceSetPanel(GradleJavaSourceSet sourceSet, File relativeTo) {
        this.sourceSet = sourceSet;
        relativeRoot = relativeTo.toPath();
        initComponents();
        if (sourceSet.getSourcesCompatibility().equals(sourceSet.getTargetCompatibility())) {
            tfSourceLevel.setText(sourceSet.getSourcesCompatibility());
        } else {
            tfSourceLevel.setText(sourceSet.getSourcesCompatibility() + " / " + sourceSet.getTargetCompatibility());
        }
        if (!sourceSet.getOutputClassDirs().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            String separator = "";
            for (File dir : sourceSet.getOutputClassDirs()) {
                sb.append(separator);
                sb.append(relativeRoot.relativize(dir.toPath()));
                separator = ", ";
            }
            tfOutputClasses.setText(sb.toString());
        }
        if (sourceSet.getOutputResources() != null) {
            tfOutputResources.setText(relativeRoot.relativize(sourceSet.getOutputResources().toPath()).toString());
        }

        for (GradleJavaSourceSet.SourceType type : GradleJavaSourceSet.SourceType.values()) {
            if (!sourceSet.getSourceDirs(type).isEmpty()) {
                DefaultMutableTreeNode typeNode = new DefaultMutableTreeNode(type);
                sourcesRoot.add(typeNode);
                for (File dir : sourceSet.getSourceDirs(type)) {
                    typeNode.add(new DefaultMutableTreeNode(dir, false));
                }
            }
        }
        trSources.setModel(new DefaultTreeModel(sourcesRoot, true));
        DefaultMutableTreeNode currentNode = sourcesRoot;
        do {
            if (currentNode.getLevel() <= 1) {
                trSources.expandPath(new TreePath(currentNode.getPath()));
            }
            currentNode = currentNode.getNextNode();
        } while (currentNode != null);
        trSources.setCellRenderer(new MyTreeCellRenderer());

        DefaultListModel<File> compileModel = new DefaultListModel<>();
        for (File file : sourceSet.getCompileClassPath()) {
            compileModel.addElement(file);
        }
        lsCompile.setModel(compileModel);
        lsCompile.setCellRenderer(new MyListCellRenderer());

        DefaultListModel<File> runtimeModel = new DefaultListModel<>();
        for (File file : sourceSet.getRuntimeClassPath()) {
            runtimeModel.addElement(file);
        }
        lsRuntime.setModel(runtimeModel);
        lsRuntime.setCellRenderer(new MyListCellRenderer());

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        trSources = new javax.swing.JTree();
        jScrollPane2 = new javax.swing.JScrollPane();
        lsCompile = new javax.swing.JList<>();
        jScrollPane3 = new javax.swing.JScrollPane();
        lsRuntime = new javax.swing.JList<>();
        tfSourceLevel = new javax.swing.JTextField();
        tfOutputResources = new javax.swing.JTextField();
        tfOutputClasses = new javax.swing.JTextField();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SourceSetPanel.class, "SourceSetPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SourceSetPanel.class, "SourceSetPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SourceSetPanel.class, "SourceSetPanel.jLabel3.text")); // NOI18N

        jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        trSources.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        trSources.setRootVisible(false);
        jScrollPane1.setViewportView(trSources);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(SourceSetPanel.class, "SourceSetPanel.jScrollPane1.TabConstraints.tabTitle"), jScrollPane1); // NOI18N

        jScrollPane2.setViewportView(lsCompile);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(SourceSetPanel.class, "SourceSetPanel.jScrollPane2.TabConstraints.tabTitle"), jScrollPane2); // NOI18N

        jScrollPane3.setViewportView(lsRuntime);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(SourceSetPanel.class, "SourceSetPanel.jScrollPane3.TabConstraints.tabTitle"), jScrollPane3); // NOI18N

        tfSourceLevel.setEditable(false);
        tfSourceLevel.setText(org.openide.util.NbBundle.getMessage(SourceSetPanel.class, "SourceSetPanel.tfSourceLevel.text")); // NOI18N

        tfOutputResources.setEditable(false);

        tfOutputClasses.setEditable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfSourceLevel))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfOutputResources)
                            .addComponent(tfOutputClasses))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(tfSourceLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(tfOutputClasses, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(tfOutputResources, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );
    }// </editor-fold>//GEN-END:initComponents


    private class MyTreeCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component comp = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (comp instanceof JLabel && value instanceof DefaultMutableTreeNode) {
                JLabel label = (JLabel) comp;
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                if (node.getUserObject() instanceof File) {
                    File dir = (File) node.getUserObject();
                    String relative = relativeRoot.relativize(dir.toPath()).toString();
                    label.setText(relative);
                    label.setToolTipText(dir.getAbsolutePath());
                    label.setIcon(folderIcon);
                    label.setEnabled(dir.exists());
                }
            }
            return comp;
        }

    }

    private class MyListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (comp instanceof JLabel && value instanceof File) {
                JLabel label = (JLabel) comp;
                File file = (File) value;
                Path path = file.toPath();
                if (path.startsWith(relativeRoot)) {
                    String relative = relativeRoot.relativize(path).toString();
                    label.setText("<html><b>" + relative + "</b>"); //NOI18N
                    label.setIcon(relative.endsWith(".jar") ? projectIcon : folderIcon); //NOI18N
                } else {
                    label.setText(path.getFileName().toString());
                    label.setIcon(artifactIcon);
                }
                label.setToolTipText(file.getAbsolutePath());
                label.setEnabled(file.exists());
            }
            return comp;
        }

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JList<File> lsCompile;
    private javax.swing.JList<File> lsRuntime;
    private javax.swing.JTextField tfOutputClasses;
    private javax.swing.JTextField tfOutputResources;
    private javax.swing.JTextField tfSourceLevel;
    private javax.swing.JTree trSources;
    // End of variables declaration//GEN-END:variables
}
