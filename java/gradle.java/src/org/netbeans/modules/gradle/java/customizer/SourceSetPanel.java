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
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.gradle.java.spi.support.JavaToolchainSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Laszlo Kishalmi
 */
public class SourceSetPanel extends javax.swing.JPanel {

    @Messages({
        "sourceSetDetail.SOURCES=Sources",
        "sourceSetDetail.COMPILE_CP=Compile Classpath",
        "sourceSetDetail.RUNTIME_CP=Runtime Classpath",
        "sourceSetDetail.ANNOTATION_PROC=Annotation Processors",
        "sourceSetDetail.COMPILER_ARGS=Compiler Args",
    })
    enum Details {
        SOURCES, COMPILE_CP, RUNTIME_CP, ANNOTATION_PROC, COMPILER_ARGS;

        @Override
        public String toString() {
            return NbBundle.getMessage(SourceSetPanel.class, "sourceSetDetail." + name()); //NOI18N
        }
    }

    private static final String PROJECT_ICON = "org.netbeans.modules/gradle/resources/gradle.png"; //NOI18N
    private static final String ARTIFACT_ICON = "org.netbeans.modules/gradle/resources/module-artifact.png"; //NOI18N

    final Icon folderIcon = new ImageIcon(NodeUtils.getTreeFolderIcon(false));
    final Icon projectIcon = ImageUtilities.loadImageIcon(PROJECT_ICON, false);
    final Icon artifactIcon = ImageUtilities.loadImageIcon(ARTIFACT_ICON, false);

    final Path relativeRoot;
    final GradleJavaSourceSet sourceSet;
    private final DefaultMutableTreeNode sourcesRoot = new DefaultMutableTreeNode(new Object());
    private final DefaultTreeModel sourcesModel = new DefaultTreeModel(sourcesRoot, true);
    private final DefaultMutableTreeNode argumentsRoot = new DefaultMutableTreeNode(new Object());
    private final DefaultTreeModel argumentsModel = new DefaultTreeModel(argumentsRoot, true);

    /**
     * Creates new form SourceSetPanel
     */
    public SourceSetPanel(GradleJavaSourceSet sourceSet, File relativeTo) {
        this.sourceSet = sourceSet;
        relativeRoot = relativeTo.toPath();
        initComponents();
        
        File javaHome = sourceSet.getCompilerJavaHome(GradleJavaSourceSet.SourceType.JAVA);
        JavaPlatform platform =JavaPlatform.getDefault();
        if (javaHome != null) {
            platform = JavaToolchainSupport.getDefault().platformByHome(javaHome);
        }
        jtPlatform.setText(platform.getDisplayName());
        
        if (platform.isValid()) {
            FileObject home = platform.getInstallFolders().iterator().next();
            jtPlatform.setToolTipText(home.getPath());
        }
        
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
                sourcesModel.insertNodeInto(typeNode, sourcesRoot, sourcesRoot.getChildCount());
                for (File dir : sourceSet.getSourceDirs(type)) {
                    sourcesModel.insertNodeInto(new DefaultMutableTreeNode(dir, false), typeNode, typeNode.getChildCount());
                }
                trSources.expandPath(new TreePath(typeNode.getPath()));
            }
            if (!sourceSet.getCompilerArgs(type).isEmpty()) {
                DefaultMutableTreeNode typeNode = new DefaultMutableTreeNode(type, true);
                argumentsModel.insertNodeInto(typeNode, argumentsRoot, argumentsRoot.getChildCount());
                for (String compilerArg : sourceSet.getCompilerArgs(type)) {
                    argumentsModel.insertNodeInto(new DefaultMutableTreeNode(compilerArg, false), typeNode, typeNode.getChildCount());
                }
                trCompilerArgs.expandPath(new TreePath(typeNode.getPath()));
            }
        }
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

        DefaultListModel<File> apModel = new DefaultListModel<>();
        for (File file : sourceSet.getAnnotationProcessorPath()) {
            apModel.addElement(file);
        }
        lsAnnotationProcessors.setModel(apModel);
        lsAnnotationProcessors.setCellRenderer(new MyListCellRenderer());
    }

    void addDetailsChangeListener(ChangeListener l) {
        tpDetails.addChangeListener(l);
    }

    void setActiveDetails(Details detail) {
        tpDetails.setSelectedIndex(detail.ordinal());
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
        tpDetails = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        trSources = new javax.swing.JTree();
        jScrollPane2 = new javax.swing.JScrollPane();
        lsCompile = new javax.swing.JList<>();
        jScrollPane3 = new javax.swing.JScrollPane();
        lsRuntime = new javax.swing.JList<>();
        jScrollPane4 = new javax.swing.JScrollPane();
        lsAnnotationProcessors = new javax.swing.JList<>();
        jScrollPane6 = new javax.swing.JScrollPane();
        trCompilerArgs = new javax.swing.JTree();
        tfSourceLevel = new javax.swing.JTextField();
        tfOutputResources = new javax.swing.JTextField();
        tfOutputClasses = new javax.swing.JTextField();
        lbPlatform = new javax.swing.JLabel();
        jtPlatform = new javax.swing.JTextField();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SourceSetPanel.class, "SourceSetPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SourceSetPanel.class, "SourceSetPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SourceSetPanel.class, "SourceSetPanel.jLabel3.text")); // NOI18N

        tpDetails.setTabPlacement(javax.swing.JTabbedPane.RIGHT);
        tpDetails.setMinimumSize(new java.awt.Dimension(150, 32));

        trSources.setModel(sourcesModel);
        trSources.setRootVisible(false);
        jScrollPane1.setViewportView(trSources);

        tpDetails.addTab(Details.SOURCES.toString(), jScrollPane1);

        jScrollPane2.setViewportView(lsCompile);

        tpDetails.addTab(Details.COMPILE_CP.toString(), jScrollPane2);

        jScrollPane3.setViewportView(lsRuntime);

        tpDetails.addTab(Details.RUNTIME_CP.toString(), jScrollPane3);

        jScrollPane4.setViewportView(lsAnnotationProcessors);

        tpDetails.addTab(Details.ANNOTATION_PROC.toString(), jScrollPane4);

        trCompilerArgs.setModel(argumentsModel);
        trCompilerArgs.setRootVisible(false);
        jScrollPane6.setViewportView(trCompilerArgs);

        tpDetails.addTab(Details.COMPILER_ARGS.toString(), jScrollPane6);

        tfSourceLevel.setEditable(false);

        tfOutputResources.setEditable(false);

        tfOutputClasses.setEditable(false);

        lbPlatform.setLabelFor(jtPlatform);
        org.openide.awt.Mnemonics.setLocalizedText(lbPlatform, org.openide.util.NbBundle.getMessage(SourceSetPanel.class, "SourceSetPanel.lbPlatform.text")); // NOI18N

        jtPlatform.setEditable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tpDetails, javax.swing.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfOutputResources)
                            .addComponent(tfOutputClasses)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                            .addComponent(lbPlatform, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(tfSourceLevel, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jtPlatform))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbPlatform)
                    .addComponent(jtPlatform, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(tfSourceLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tpDetails, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
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
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTextField jtPlatform;
    private javax.swing.JLabel lbPlatform;
    private javax.swing.JList<File> lsAnnotationProcessors;
    private javax.swing.JList<File> lsCompile;
    private javax.swing.JList<File> lsRuntime;
    private javax.swing.JTextField tfOutputClasses;
    private javax.swing.JTextField tfOutputResources;
    private javax.swing.JTextField tfSourceLevel;
    private javax.swing.JTabbedPane tpDetails;
    private javax.swing.JTree trCompilerArgs;
    private javax.swing.JTree trSources;
    // End of variables declaration//GEN-END:variables
}
