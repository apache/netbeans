/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.client.rest.wizard;

import java.io.File;
import java.io.IOException;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author ads
 */
public class HtmlPanelVisual extends javax.swing.JPanel {
    
    static final String HTML = ".html";             // NOI18N
    private static final String DEFAULT_HTML = "newHtml";   // NOI18N

    /**
     * Creates new form HtmlPanelVisual
     */
    public HtmlPanelVisual(HtmlPanel panel) {
        myPanel = panel;
        initComponents();
        
        DocumentListener listener = new DocumentListener() {
            
            @Override
            public void removeUpdate( DocumentEvent arg0 ) {
                updateFilePath();
            }
            
            @Override
            public void insertUpdate( DocumentEvent arg0 ) {
                updateFilePath();                
            }
            
            @Override
            public void changedUpdate( DocumentEvent arg0 ) {
                updateFilePath();                
            }
        };
        initDefaults();
        
        myName.getDocument().addDocumentListener( listener);
        myFolder.getDocument().addDocumentListener(listener);
        
        myCreatedFile.setText(getCreatedFilePath().getPath());
    }
    
    void store( WizardDescriptor descriptor ) {
        descriptor.putProperty(HtmlPanel.HTML_FILE, getCreatedFilePath());
    }
    
    void read( WizardDescriptor descriptor ) {
        Project project = Templates.getProject(descriptor);
        FileObject projectDirectory = project.getProjectDirectory();
        FileObject documentBase = (FileObject)myPanel.getDescriptor().getProperty(HtmlPanel.PROP_DOCUMENT_BASE);
        if (documentBase != null) {
            myFolder.setText(FileUtil.getRelativePath(projectDirectory, documentBase));
        } else {
            myFolder.setText(HtmlPanel.PUBLIC_HTML);
        }
    }
        
    boolean valid() {
        myPanel.getDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, 
                null);
        File file = getCreatedFilePath();
        try {
            file.getCanonicalFile();
        }
        catch(IOException e ){
            myPanel.getDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, 
                    NbBundle.getMessage(HtmlPanelVisual.class, "ERR_InvalidPath")); // NOI18N
            return false;
        }
        if ( file.exists() ){
            myPanel.getDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, 
                    NbBundle.getMessage(HtmlPanelVisual.class, "ERR_FileExists", file.getPath())); // NOI18N
            return false;
        }
        return true;
    }
    
    private void updateFilePath(){
        myCreatedFile.setText(getCreatedFilePath().getPath());
        myPanel.fireChangeEvent();
    }
    
    private File getCreatedFilePath(){
        Project project = Templates.getProject(myPanel.getDescriptor());
        FileObject projectDirectory = project.getProjectDirectory();
        String folder = myFolder.getText();
        String file = myName.getText() + HTML;
        File path = FileUtil.toFile(projectDirectory);
        if ( folder!= null && folder.length() >0 ){
            path = new File(path, folder);
        }
        return new File(path, file);
    }
    
    private void initDefaults() {
        Project project = Templates.getProject(myPanel.getDescriptor());
        FileObject projectDirectory = project.getProjectDirectory();
        String name = DEFAULT_HTML;
        int i = 0;
        while( true ){
            String nameExt = name +HTML;
            if ( projectDirectory.getFileObject(nameExt) != null){
                name = DEFAULT_HTML+i;
                i++;
            }
            else {
                break;
            }
        }
        myName.setText(name);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        myNameLbl = new javax.swing.JLabel();
        myName = new javax.swing.JTextField();
        myFolderLbl = new javax.swing.JLabel();
        myFolder = new javax.swing.JTextField();
        myCreatedFile = new javax.swing.JTextField();
        myCreatedFileLbl = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(489, 97));

        myNameLbl.setLabelFor(myName);
        org.openide.awt.Mnemonics.setLocalizedText(myNameLbl, org.openide.util.NbBundle.getMessage(HtmlPanelVisual.class, "LBL_HtmlFileName")); // NOI18N

        myFolderLbl.setLabelFor(myFolder);
        org.openide.awt.Mnemonics.setLocalizedText(myFolderLbl, org.openide.util.NbBundle.getMessage(HtmlPanelVisual.class, "LBL_Folder")); // NOI18N

        myCreatedFile.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(myCreatedFileLbl, org.openide.util.NbBundle.getMessage(HtmlPanelVisual.class, "LBL_CreatedFile")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(myNameLbl)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(myCreatedFileLbl, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(myFolderLbl, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(myCreatedFile, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                    .addComponent(myName)
                    .addComponent(myFolder))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(myNameLbl)
                    .addComponent(myName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(myFolderLbl)
                    .addComponent(myFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(myCreatedFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(myCreatedFileLbl))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        myNameLbl.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HtmlPanelVisual.class, "ACSN_FileName")); // NOI18N
        myNameLbl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HtmlPanelVisual.class, "ACSD_FileName")); // NOI18N
        myName.getAccessibleContext().setAccessibleName(myNameLbl.getAccessibleContext().getAccessibleName());
        myName.getAccessibleContext().setAccessibleDescription(myNameLbl.getAccessibleContext().getAccessibleDescription());
        myFolderLbl.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HtmlPanelVisual.class, "ACSN_Folder")); // NOI18N
        myFolderLbl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HtmlPanelVisual.class, "ACSD_Folder")); // NOI18N
        myCreatedFileLbl.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HtmlPanelVisual.class, "ACSN_CreatedFile")); // NOI18N
        myCreatedFileLbl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HtmlPanelVisual.class, "ACSD_CreatedFile")); // NOI18N

        getAccessibleContext().setAccessibleName(myFolderLbl.getAccessibleContext().getAccessibleName());
        getAccessibleContext().setAccessibleDescription(myFolderLbl.getAccessibleContext().getAccessibleDescription());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField myCreatedFile;
    private javax.swing.JLabel myCreatedFileLbl;
    private javax.swing.JTextField myFolder;
    private javax.swing.JLabel myFolderLbl;
    private javax.swing.JTextField myName;
    private javax.swing.JLabel myNameLbl;
    // End of variables declaration//GEN-END:variables
    
    private HtmlPanel myPanel;

}
