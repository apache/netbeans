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

package org.netbeans.modules.j2ee.ejbjarproject.ui.wizards;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Enumeration;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.j2ee.common.FileSearchUtility;
import org.netbeans.modules.java.api.common.project.ui.wizards.FolderList;
import org.netbeans.modules.javaee.project.api.ant.ui.wizard.ProjectLocationWizardPanel;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

//XXX There should be a way how to add nonexistent test dir

/**
 * Sets up name and location for new Java project from existing sources.
 * @author Tomas Zezula et al.
 */
public class PanelSourceFolders extends SettingsPanel implements PropertyChangeListener {
    private static final long serialVersionUID = 17605310116880120L;

    private final Panel firer;
    private WizardDescriptor wizardDescriptor;
    private File oldProjectLocation;
    
    /** Creates new form PanelSourceFolders */
    public PanelSourceFolders (Panel panel) {
        this.firer = panel;
        initComponents();
        this.setName(NbBundle.getMessage(PanelSourceFolders.class,"LAB_ConfigureSourceRoots"));
        this.putClientProperty ("NewProjectWizard_Title", NbBundle.getMessage(PanelSourceFolders.class,"TXT_ImportEJBModule")); // NOI18N
        this.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PanelSourceFolders.class,"AN_PanelSourceFolders"));
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PanelSourceFolders.class,"AD_PanelSourceFolders"));
        this.sourcePanel.addPropertyChangeListener (this);
        this.testsPanel.addPropertyChangeListener(this);
        ((FolderList)this.sourcePanel).setRelatedFolderList((FolderList)this.testsPanel, FolderList.testRootsFilter());
        ((FolderList)this.testsPanel).setRelatedFolderList((FolderList)this.sourcePanel);

        DocumentListener configAndLibrariesListener = new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                dataChanged();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                dataChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                dataChanged();
            }
        };
        this.jTextFieldConfigFiles.getDocument().addDocumentListener(configAndLibrariesListener);
        this.jTextFieldLibraries.getDocument().addDocumentListener(configAndLibrariesListener);
    }

    public void initValues(FileObject fo) {
        ((FolderList) this.sourcePanel).setLastUsedDir(FileUtil.toFile(fo));
        ((FolderList) this.testsPanel).setLastUsedDir(FileUtil.toFile(fo));
        
        FileObject confFO = FileSearchUtility.guessConfigFilesPath(fo, EjbJarProvider.FILE_DD);
        if (confFO == null) { // without deployment descriptor
            // XXX guess appropriate conf. folder
        } else {
            String configFiles = FileUtil.toFile(confFO).getAbsolutePath();
            jTextFieldConfigFiles.setText(configFiles);
        }
        FileObject librariesFO = FileSearchUtility.guessLibrariesFolder(fo);
        if (librariesFO != null) {
            String libraries = FileUtil.toFile(librariesFO).getAbsolutePath();
            jTextFieldLibraries.setText(libraries);
        }
    }

    
    public void propertyChange(PropertyChangeEvent evt) {
        if (FolderList.PROP_FILES.equals(evt.getPropertyName())) {
            this.dataChanged();
        } else if (FolderList.PROP_LAST_USED_DIR.equals (evt.getPropertyName())) {
            if (evt.getSource() == this.sourcePanel) {                
                ((FolderList)this.testsPanel).setLastUsedDir 
                        ((File)evt.getNewValue());
            }
            else if (evt.getSource() == this.testsPanel) {
                ((FolderList)this.sourcePanel).setLastUsedDir 
                        ((File)evt.getNewValue());
            }
        }
    }

    private void dataChanged () {
        this.firer.fireChangeEvent();
    }


    void read (WizardDescriptor settings) {
        this.wizardDescriptor = settings;
        
        // #56706: only reinitialize the locations on the panel if the user changed the project location
        File projectLocation = (File) settings.getProperty(WizardProperties.SOURCE_ROOT);
        ((FolderList)this.sourcePanel).setProjectFolder(projectLocation);
        ((FolderList)this.testsPanel).setProjectFolder(projectLocation);
        if (!projectLocation.equals(oldProjectLocation)) {
            File[] srcRoot = (File[]) settings.getProperty (WizardProperties.JAVA_ROOT);      //NOI18N
            if (srcRoot!=null) {
                ((FolderList)this.sourcePanel).setFiles(srcRoot);
            }
            File[] testRoot = (File[]) settings.getProperty (WizardProperties.TEST_ROOT);       //NOI18N
            if (testRoot != null) {
                ((FolderList)this.testsPanel).setFiles (testRoot);
            }
            initValues(FileUtil.toFileObject(FileUtil.normalizeFile(projectLocation)));
            oldProjectLocation = projectLocation;
        }
    }

    void store (WizardDescriptor settings) {
        File[] sourceRoots = ((FolderList)this.sourcePanel).getFiles();
        File[] testRoots = ((FolderList)this.testsPanel).getFiles();
        settings.putProperty (WizardProperties.JAVA_ROOT,sourceRoots);    //NOI18N
        settings.putProperty(WizardProperties.TEST_ROOT,testRoots);      //NOI18N
        settings.putProperty(WizardProperties.CONFIG_FILES_FOLDER, getConfigFiles());
        settings.putProperty(WizardProperties.LIB_FOLDER, getLibraries());
    }
    
    boolean valid (WizardDescriptor settings) {
        File projectLocation = (File) settings.getProperty (ProjectLocationWizardPanel.PROJECT_DIR);  //NOI18N
        File confFolder = getConfigFiles();
        if (confFolder == null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                    NbBundle.getMessage(PanelSourceFolders.class, "MSG_BlankConfigurationFilesFolder"));
            return false;
        }
        File[] sourceRoots = ((FolderList)this.sourcePanel).getFiles();
        if (sourceRoots.length == 0) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                    NbBundle.getMessage(PanelSourceFolders.class, "MSG_BlankSourceFilesFolder"));
            return false;
        }
        File[] testRoots = ((FolderList)this.testsPanel).getFiles();
        String result = checkValidity (projectLocation, confFolder, getLibraries(), sourceRoots, testRoots);
        if (result == null) {
            wizardDescriptor.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE," ");   //NOI18N
            return true;
        }
        else {
            wizardDescriptor.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE,result);       //NOI18N
            return false;
        }
    }

    static String checkValidity (final File projectLocation, final File configFilesLocation, final File librariesLocation, final File[] sources, final File[] tests ) {
        String ploc = projectLocation.getAbsolutePath ();
        if (configFilesLocation != null) {
            FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(configFilesLocation));
            if (fo == null || !fo.isFolder()) {
                return NbBundle.getMessage(PanelSourceFolders.class, "MSG_IllegalConfigurationFilesFolder");
            }
        }
        if (librariesLocation != null) {
            FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(librariesLocation));
            if (fo == null || !fo.isFolder()) {
                return NbBundle.getMessage(PanelSourceFolders.class, "MSG_IllegalLibrariesFolder");
            }
        }
        if (sources.length ==0) {
            return " ";  //NOI18N
        }
        for (int i=0; i<sources.length;i++) {
            if (!sources[i].isDirectory() || !sources[i].canRead()) {
                return MessageFormat.format(NbBundle.getMessage(PanelSourceFolders.class,"MSG_IllegalSources"),
                        new Object[] {sources[i].getAbsolutePath()});
            }
            String sloc = sources[i].getAbsolutePath ();
            if (ploc.equals (sloc) || ploc.startsWith (sloc + File.separatorChar)) {
                return NbBundle.getMessage(PanelSourceFolders.class,"MSG_IllegalProjectFolder");
            }
        }
        for (int i=0; i<tests.length; i++) {
            if (!tests[i].isDirectory() || !tests[i].canRead()) {
                return MessageFormat.format(NbBundle.getMessage(PanelSourceFolders.class,"MSG_IllegalTests"),
                        new Object[] {sources[i].getAbsolutePath()});
            }            
            String tloc = tests[i].getAbsolutePath();
            if (ploc.equals(tloc) || ploc.startsWith(tloc + File.separatorChar)) {
                return NbBundle.getMessage(PanelSourceFolders.class,"MSG_IllegalProjectFolder");
            }
        }
        return null;
    }
    
    void validate (WizardDescriptor d) throws WizardValidationException {
        // sources root
        searchClassFiles (((FolderList)this.sourcePanel).getFiles());
        // test root, not asked in issue 48198
        //searchClassFiles (FileUtil.toFileObject (FileUtil.normalizeFile(new File (tests.getText ()))));
    }
    
    private void searchClassFiles (File[] folders) throws WizardValidationException {
        boolean found = false;
        for (int i=0; i<folders.length; i++) {
            FileObject folder = FileUtil.toFileObject(folders[i]);
            if (folder != null) {
                Enumeration en = folder.getData (true);
                while (!found && en.hasMoreElements ()) {
                    Object obj = en.nextElement ();
                    assert obj instanceof FileObject : "Instance of FileObject: " + obj; // NOI18N
                    FileObject fo = (FileObject) obj;
                    found = "class".equals (fo.getExt ()); // NOI18N
                }
            }
        }
        if (found) {
            Object DELETE_OPTION = NbBundle.getMessage (PanelSourceFolders.class, "TXT_DeleteOption"); // NOI18N
            Object KEEP_OPTION = NbBundle.getMessage (PanelSourceFolders.class, "TXT_KeepOption"); // NOI18N
            Object CANCEL_OPTION = NbBundle.getMessage (PanelSourceFolders.class, "TXT_CancelOption"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor (
                    NbBundle.getMessage (PanelSourceFolders.class, "MSG_FoundClassFiles"), // NOI18N
                    NbBundle.getMessage (PanelSourceFolders.class, "MSG_FoundClassFiles_Title"), // NOI18N
                    NotifyDescriptor.YES_NO_CANCEL_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE,
                    new Object[] {DELETE_OPTION, KEEP_OPTION, CANCEL_OPTION},
                    null
                    );
            Object result = DialogDisplayer.getDefault().notify(desc);
            if (DELETE_OPTION.equals (result)) {
                deleteClassFiles (folders);
            } else if (!KEEP_OPTION.equals (result)) {
                // cancel, back to wizard
                throw new WizardValidationException (this.sourcePanel, "", ""); // NOI18N
            }
        }
    }
    
    private void deleteClassFiles (File[] folders) {
        for (int i=0; i<folders.length; i++) {
            FileObject folder = FileUtil.toFileObject(folders[i]);
            Enumeration en = folder.getData (true);
            while (en.hasMoreElements ()) {
                Object obj = en.nextElement ();
                assert obj instanceof FileObject : "Instance of FileObject: " + obj;
                FileObject fo = (FileObject) obj;
                try {
                    if ("class".equals (fo.getExt ())) { // NOI18N
                        fo.delete ();
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        sourcePanel = new FolderList (NbBundle.getMessage(PanelSourceFolders.class,"CTL_SourceRoots"), NbBundle.getMessage(PanelSourceFolders.class,"MNE_SourceRoots").charAt(0),NbBundle.getMessage(PanelSourceFolders.class,"AD_SourceRoots"), NbBundle.getMessage(PanelSourceFolders.class,"CTL_AddSourceRoot"),
            NbBundle.getMessage(PanelSourceFolders.class,"MNE_AddSourceFolder").charAt(0), NbBundle.getMessage(PanelSourceFolders.class,"AD_AddSourceFolder"),NbBundle.getMessage(PanelSourceFolders.class,"MNE_RemoveSourceFolder").charAt(0), NbBundle.getMessage(PanelSourceFolders.class,"AD_RemoveSourceFolder"));
        testsPanel = new FolderList (NbBundle.getMessage(PanelSourceFolders.class,"CTL_TestRoots"), NbBundle.getMessage(PanelSourceFolders.class,"MNE_TestRoots").charAt(0),NbBundle.getMessage(PanelSourceFolders.class,"AD_TestRoots"), NbBundle.getMessage(PanelSourceFolders.class,"CTL_AddTestRoot"),
            NbBundle.getMessage(PanelSourceFolders.class,"MNE_AddTestFolder").charAt(0), NbBundle.getMessage(PanelSourceFolders.class,"AD_AddTestFolder"),NbBundle.getMessage(PanelSourceFolders.class,"MNE_RemoveTestFolder").charAt(0), NbBundle.getMessage(PanelSourceFolders.class,"AD_RemoveTestFolder"));
        jLabel1 = new javax.swing.JLabel();
        jTextFieldConfigFiles = new javax.swing.JTextField();
        jButtonConfigFilesLocation = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldLibraries = new javax.swing.JTextField();
        jButtonLibraries = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "LBL_SourceDirectoriesLabel")); // NOI18N

        jLabel1.setLabelFor(jTextFieldConfigFiles);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbjarproject/ui/wizards/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, bundle.getString("LBL_IW_ConfigFilesFolder_Label")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonConfigFilesLocation, bundle.getString("LBL_NWP1_BrowseLocation_Button_w")); // NOI18N
        jButtonConfigFilesLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonConfigFilesLocationActionPerformed(evt);
            }
        });

        jLabel2.setLabelFor(jTextFieldLibraries);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, bundle.getString("LBL_IW_LibrariesLocation_Label")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonLibraries, bundle.getString("LBL_NWP1_BrowseLocation_Button")); // NOI18N
        jButtonLibraries.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLibrariesActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, bundle.getString("LBL_IW_DragAndDrop")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldLibraries, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                    .addComponent(jTextFieldConfigFiles, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonConfigFilesLocation, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButtonLibraries, javax.swing.GroupLayout.Alignment.TRAILING)))
            .addComponent(sourcePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE)
            .addComponent(testsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE)
            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextFieldConfigFiles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonConfigFilesLocation))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextFieldLibraries, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonLibraries))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sourcePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(testsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelSourceFolders.class).getString("ACSN_jLabel3")); // NOI18N
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelSourceFolders.class).getString("ACSD_jLabel3")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSN_PanelSourceFolders")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_PanelSourceFolders")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonLibrariesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLibrariesActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        if (jTextFieldLibraries.getText().length() > 0 && getLibraries().exists()) {
            chooser.setSelectedFile(getLibraries());
        } else {
            chooser.setCurrentDirectory((File) wizardDescriptor.getProperty(ProjectLocationWizardPanel.PROJECT_DIR));
        }
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File configFilesDir = FileUtil.normalizeFile(chooser.getSelectedFile());
            jTextFieldLibraries.setText(configFilesDir.getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonLibrariesActionPerformed

    private void jButtonConfigFilesLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonConfigFilesLocationActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        if (jTextFieldConfigFiles.getText().length() > 0 && getConfigFiles().exists()) {
            chooser.setSelectedFile(getConfigFiles());
        } else {
            chooser.setCurrentDirectory((File) wizardDescriptor.getProperty(ProjectLocationWizardPanel.PROJECT_DIR));
        }
        if ( JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File configFilesDir = FileUtil.normalizeFile(chooser.getSelectedFile());
            jTextFieldConfigFiles.setText(configFilesDir.getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonConfigFilesLocationActionPerformed

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonConfigFilesLocation;
    private javax.swing.JButton jButtonLibraries;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField jTextFieldConfigFiles;
    private javax.swing.JTextField jTextFieldLibraries;
    private javax.swing.JPanel sourcePanel;
    private javax.swing.JPanel testsPanel;
    // End of variables declaration//GEN-END:variables

    
    static class Panel implements WizardDescriptor.ValidatingPanel {
        
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private PanelSourceFolders component;
        private WizardDescriptor settings;
        
        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        public void readSettings(Object settings) {
            this.settings = (WizardDescriptor) settings;
            this.component.read (this.settings);
            // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
            // this name is used in NewProjectWizard to modify the title
            Object substitute = component.getClientProperty ("NewProjectWizard_Title"); // NOI18N
            if (substitute != null) {
                this.settings.putProperty ("NewProjectWizard_Title", substitute); // NOI18N
            }
        }

        public void storeSettings(Object settings) {
            this.component.store (this.settings);
        }
        
        public void validate() throws WizardValidationException {
            this.component.validate(this.settings);
        }
                
        public boolean isValid() {
            return this.component.valid (this.settings);
        }

        public synchronized java.awt.Component getComponent() {
            if (this.component == null) {
                this.component = new PanelSourceFolders (this);
            }
            return this.component;
        }

        public HelpCtx getHelp() {
            return new HelpCtx (PanelSourceFolders.class);
        }        
        
        private void fireChangeEvent () {
            changeSupport.fireChange();
        }
                
    }

    private File getAsFile(String filename) {
        if (filename != null && filename.trim().length() > 0) {
            return FileUtil.normalizeFile(new File(filename));
        }
        return null;
    }

    public File getConfigFiles() {
        return getAsFile(jTextFieldConfigFiles.getText());
    }

    public File getLibraries() {
        return getAsFile(jTextFieldLibraries.getText());
    }
}
