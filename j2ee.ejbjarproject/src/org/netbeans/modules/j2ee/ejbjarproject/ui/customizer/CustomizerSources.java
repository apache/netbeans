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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.ejbjarproject.ui.customizer;

import org.netbeans.modules.java.api.common.project.ui.customizer.SourceRootsUi;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.nio.charset.Charset;
import javax.swing.CellEditor;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.javaee.project.api.ui.utils.UIUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.spi.java.project.support.ui.IncludeExcludeVisualizer;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author  Tomas Zezula
 */
public class CustomizerSources extends javax.swing.JPanel implements HelpCtx.Provider {
    private static final long serialVersionUID = 26668383530631180L;
    
    private String originalEncoding;
    private EjbJarProjectProperties uiProperties;
    private File projectFld;
    private boolean notified;
    
    public CustomizerSources( EjbJarProjectProperties uiProperties ) {
        initComponents();
        jScrollPane1.getViewport().setBackground( sourceRoots.getBackground() );
        jScrollPane2.getViewport().setBackground( testRoots.getBackground() );
        
        sourceRoots.setModel( uiProperties.SOURCE_ROOTS_MODEL );
        testRoots.setModel( uiProperties.TEST_ROOTS_MODEL );
        sourceRoots.getTableHeader().setReorderingAllowed(false);
        testRoots.getTableHeader().setReorderingAllowed(false);
        
        FileObject projectFolder = uiProperties.getProject().getProjectDirectory();
        File pf = FileUtil.toFile( projectFolder );
        this.projectLocation.setText( pf == null ? "" : pf.getPath() ); // NOI18N
        this.projectFld = pf;
        
        jTextFieldConfigFilesFolder.setDocument(uiProperties.META_INF_MODEL);
        
        SourceRootsUi.EditMediator emSR = SourceRootsUi.registerEditMediator(
                (EjbJarProject)uiProperties.getProject(),
                ((EjbJarProject)uiProperties.getProject()).getSourceRoots(),
                sourceRoots,
                addSourceRoot,
                removeSourceRoot,
                upSourceRoot,
                downSourceRoot,null,false);
        
        SourceRootsUi.EditMediator emTSR = SourceRootsUi.registerEditMediator(
                (EjbJarProject)uiProperties.getProject(),
                ((EjbJarProject)uiProperties.getProject()).getTestSourceRoots(),
                testRoots,
                addTestRoot,
                removeTestRoot,
                upTestRoot,
                downTestRoot,null,true);
        
        emSR.setRelatedEditMediator( emTSR );
        emTSR.setRelatedEditMediator( emSR );
        this.sourceLevel.setModel(uiProperties.JAVAC_SOURCE_MODEL);
        this.sourceLevel.setRenderer(uiProperties.JAVAC_SOURCE_RENDERER);
        uiProperties.JAVAC_SOURCE_MODEL.addListDataListener(new ListDataListener() {
            public void intervalAdded(ListDataEvent e) {
                enableSourceLevel();
            }
            
            public void intervalRemoved(ListDataEvent e) {
                enableSourceLevel();
            }
            
            public void contentsChanged(ListDataEvent e) {
                enableSourceLevel();
            }
        });
        enableSourceLevel();
        this.originalEncoding = ((EjbJarProject)uiProperties.getProject()).evaluator().getProperty(EjbJarProjectProperties.SOURCE_ENCODING);
        if (this.originalEncoding == null) {
            this.originalEncoding = Charset.defaultCharset().name();
        }
        
        this.encoding.setModel(ProjectCustomizer.encodingModel(originalEncoding));
        this.encoding.setRenderer(ProjectCustomizer.encodingRenderer());
        final String lafid = UIManager.getLookAndFeel().getID();
        if (!"Aqua".equals(lafid)) { // NOI18N
             encoding.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE); // NOI18N
             encoding.addItemListener(new ItemListener() {
                 public void itemStateChanged(ItemEvent e) {
                     JComboBox combo = (JComboBox) e.getSource();
                     combo.setPopupVisible(false);
                 }
             });
        }
        
        
        this.encoding.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                handleEncodingChange();
            }
        });
        
        UIUtil.initTwoColumnTableVisualProperties(this, sourceRoots);
        UIUtil.initTwoColumnTableVisualProperties(this, testRoots);
        this.uiProperties = uiProperties;
        uiProperties.addOptionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CellEditor cellEditor = sourceRoots.getCellEditor();
                if (cellEditor != null) {
                    cellEditor.stopCellEditing();
                }
                cellEditor = testRoots.getCellEditor();
                if (cellEditor != null) {
                    cellEditor.stopCellEditing();
                }
            }
        });
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerSources.class);
    }
    
    private void enableSourceLevel() {
        this.sourceLevel.setEnabled(sourceLevel.getItemCount()>0);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        projectLocation = new javax.swing.JTextField();
        jLabelConfigFilesFolder = new javax.swing.JLabel();
        jTextFieldConfigFilesFolder = new javax.swing.JTextField();
        jButtonBrowse = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        sourceRoots = new javax.swing.JTable();
        addSourceRoot = new javax.swing.JButton();
        removeSourceRoot = new javax.swing.JButton();
        upSourceRoot = new javax.swing.JButton();
        downSourceRoot = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        testRoots = new javax.swing.JTable();
        addTestRoot = new javax.swing.JButton();
        removeTestRoot = new javax.swing.JButton();
        upTestRoot = new javax.swing.JButton();
        downTestRoot = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        sourceLevel = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        encoding = new javax.swing.JComboBox();
        includeExcludeButton = new javax.swing.JButton();

        jLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("MNE_ProjectFolder").charAt(0));
        jLabel1.setLabelFor(projectLocation);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("CTL_ProjectFolder")); // NOI18N

        projectLocation.setEditable(false);

        jLabelConfigFilesFolder.setDisplayedMnemonic(NbBundle.getMessage(CustomizerSources.class, "MNE_ConfigFilesFolder").charAt(0));
        jLabelConfigFilesFolder.setLabelFor(jTextFieldConfigFilesFolder);
        jLabelConfigFilesFolder.setText(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CTL_ConfigFilesFolder")); // NOI18N

        jButtonBrowse.setMnemonic(NbBundle.getMessage(CustomizerSources.class, "MNE_ConfigFilesFolderBrowse").charAt(0));
        jButtonBrowse.setText(org.openide.util.NbBundle.getMessage(CustomizerSources.class, "LBL_Browse_JButton")); // NOI18N
        jButtonBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowseActionPerformed(evt);
            }
        });

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel2.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("MNE_SourceRoots").charAt(0));
        jLabel2.setLabelFor(sourceRoots);
        jLabel2.setText(bundle.getString("CTL_SourceRoots")); // NOI18N

        jScrollPane1.setPreferredSize(new java.awt.Dimension(450, 150));

        sourceRoots.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Package Folder", "Label"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(sourceRoots);

        addSourceRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("MNE_AddSourceRoot").charAt(0));
        addSourceRoot.setText(bundle.getString("CTL_AddSourceRoot")); // NOI18N

        removeSourceRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("MNE_RemoveSourceRoot").charAt(0));
        removeSourceRoot.setText(bundle.getString("CTL_RemoveSourceRoot")); // NOI18N

        upSourceRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("MNE_UpSourceRoot").charAt(0));
        upSourceRoot.setText(bundle.getString("CTL_UpSourceRoot")); // NOI18N

        downSourceRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("MNE_DownSourceRoot").charAt(0));
        downSourceRoot.setText(bundle.getString("CTL_DownSourceRoot")); // NOI18N

        jLabel3.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("MNE_TestRoots").charAt(0));
        jLabel3.setLabelFor(testRoots);
        jLabel3.setText(bundle.getString("CTL_TestRoots")); // NOI18N

        jScrollPane2.setPreferredSize(new java.awt.Dimension(450, 150));

        testRoots.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Package Folder", "Label"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(testRoots);

        addTestRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("MNE_AddTestRoot").charAt(0));
        addTestRoot.setText(bundle.getString("CTL_AddTestRoot")); // NOI18N

        removeTestRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("MNE_RemoveTestRoot").charAt(0));
        removeTestRoot.setText(bundle.getString("CTL_RemoveTestRoot")); // NOI18N

        upTestRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("MNE_UpTestRoot").charAt(0));
        upTestRoot.setText(bundle.getString("CTL_UpTestRoot")); // NOI18N

        downTestRoot.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/customizer/Bundle").getString("MNE_DownTestRoot").charAt(0));
        downTestRoot.setText(bundle.getString("CTL_DownTestRoot")); // NOI18N

        jLabel4.setLabelFor(sourceLevel);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "TXT_SourceLevel")); // NOI18N

        jLabel5.setLabelFor(encoding);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "TXT_Encoding")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(includeExcludeButton, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.includeExcludeButton")); // NOI18N
        includeExcludeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                includeExcludeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 458, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(jLabel2)
            .addComponent(jLabel3)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sourceLevel, 0, 218, Short.MAX_VALUE)
                    .addComponent(encoding, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(includeExcludeButton))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabelConfigFilesFolder))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextFieldConfigFilesFolder)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonBrowse))
                    .addComponent(projectLocation)))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addSourceRoot, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(removeSourceRoot, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(upSourceRoot, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(downSourceRoot, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(addTestRoot, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(removeTestRoot, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(upTestRoot, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(downTestRoot, javax.swing.GroupLayout.Alignment.TRAILING)))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addSourceRoot, downSourceRoot, removeSourceRoot, upSourceRoot});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addTestRoot, downTestRoot, removeTestRoot, upTestRoot});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(projectLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelConfigFilesFolder)
                    .addComponent(jTextFieldConfigFilesFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonBrowse))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel3))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addSourceRoot, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeSourceRoot)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(upSourceRoot)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(downSourceRoot)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(sourceLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(includeExcludeButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(encoding, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, 0)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addTestRoot, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeTestRoot)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(upTestRoot)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(downTestRoot)
                        .addGap(0, 75, Short.MAX_VALUE))))
        );

        jTextFieldConfigFilesFolder.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "ACSD_CustomizerSources_ConfigFilesFolder")); // NOI18N
        jButtonBrowse.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerSources.class, "ACSD_CustomizerSources_ConfigFilesFolderBrowse")); // NOI18N
        sourceLevel.getAccessibleContext().setAccessibleName(bundle.getString("AN_SourceLevel")); // NOI18N
        sourceLevel.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_SourceLevel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void jButtonBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowseActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        File fileName = new File(jTextFieldConfigFilesFolder.getText());
        File configFiles = fileName.isAbsolute() ? fileName : new File(projectFld, fileName.getPath());
        if (configFiles.isAbsolute()) {
            chooser.setSelectedFile(configFiles);
        } else {
            chooser.setSelectedFile(projectFld);
        }
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File selected = FileUtil.normalizeFile(chooser.getSelectedFile());
            String newConfigFiles;
            if (CollocationQuery.areCollocated(projectFld, selected)) {
                newConfigFiles = PropertyUtils.relativizeFile(projectFld, selected);
            } else {
                newConfigFiles = selected.getPath();
            }
            jTextFieldConfigFilesFolder.setText(newConfigFiles);
        }
    }//GEN-LAST:event_jButtonBrowseActionPerformed

private void includeExcludeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_includeExcludeButtonActionPerformed
IncludeExcludeVisualizer v = new IncludeExcludeVisualizer();
    uiProperties.loadIncludesExcludes(v);
    DialogDescriptor dd = new DialogDescriptor(v.getVisualizerPanel(),
            NbBundle.getMessage(CustomizerSources.class, "CustomizerSources.title.includeExclude"));
    dd.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
    if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dd))) {
        uiProperties.storeIncludesExcludes(v);
    }
}//GEN-LAST:event_includeExcludeButtonActionPerformed
    
    private void handleEncodingChange() {
        Charset enc = (Charset) encoding.getSelectedItem();
        String encName;
        if (enc != null) {
            encName = enc.name();
        } else {
            encName = originalEncoding;
        }
        if (!notified && encName != null && !encName.equals(originalEncoding)) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(CustomizerSources.class, "MSG_EncodingWarning"), NotifyDescriptor.WARNING_MESSAGE));
            notified = true;
        }
        this.uiProperties.putAdditionalProperty(EjbJarProjectProperties.SOURCE_ENCODING, encName);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addSourceRoot;
    private javax.swing.JButton addTestRoot;
    private javax.swing.JButton downSourceRoot;
    private javax.swing.JButton downTestRoot;
    private javax.swing.JComboBox encoding;
    private javax.swing.JButton includeExcludeButton;
    private javax.swing.JButton jButtonBrowse;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabelConfigFilesFolder;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTextFieldConfigFilesFolder;
    private javax.swing.JTextField projectLocation;
    private javax.swing.JButton removeSourceRoot;
    private javax.swing.JButton removeTestRoot;
    private javax.swing.JComboBox sourceLevel;
    private javax.swing.JTable sourceRoots;
    private javax.swing.JTable testRoots;
    private javax.swing.JButton upSourceRoot;
    private javax.swing.JButton upTestRoot;
    // End of variables declaration//GEN-END:variables
}
