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

package org.netbeans.modules.java.j2seproject.ui.wizards;

import java.awt.Cursor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.java.api.common.project.ui.wizards.FolderList;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.*;

//XXX There should be a way how to add nonexistent test dir

/**
 * Sets up name and location for new Java project from existing sources.
 * @author Tomas Zezula et al.
 */
public class PanelSourceFolders extends SettingsPanel implements PropertyChangeListener {

    private Panel firer;
    private WizardDescriptor wizardDescriptor;

    // key to action value that influence folder list current directory
    public static final String INITIAL_SOURCE_ROOT = "EXISTING_SOURCES_CURRENT_DIRECTORY"; // NOI18N

    /** Creates new form PanelSourceFolders */
    public PanelSourceFolders (Panel panel) {
        this.firer = panel;
        initComponents();
        this.setName(NbBundle.getMessage(PanelConfigureProjectVisual.class,"LAB_ConfigureSourceRoots"));
        this.putClientProperty ("NewProjectWizard_Title", NbBundle.getMessage(PanelSourceFolders.class,"TXT_JavaExtSourcesProjectLocation")); // NOI18N
        this.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PanelSourceFolders.class,"AN_PanelSourceFolders"));
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PanelSourceFolders.class,"AD_PanelSourceFolders"));
        this.sourcePanel.addPropertyChangeListener (this);
        this.testsPanel.addPropertyChangeListener(this);
        ((FolderList)this.sourcePanel).setRelatedFolderList((FolderList)this.testsPanel, FolderList.testRootsFilter());
        ((FolderList)this.testsPanel).setRelatedFolderList((FolderList)this.sourcePanel);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (FolderList.PROP_FILES.equals(evt.getPropertyName())) {
            this.dataChanged();
        }
        else if (FolderList.PROP_LAST_USED_DIR.equals (evt.getPropertyName())) {
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


    @Override
    void read (WizardDescriptor settings) {
        this.wizardDescriptor = settings;
        File projectLocation = (File) settings.getProperty ("projdir");         //NOI18N
        ((FolderList)this.sourcePanel).setProjectFolder(projectLocation);
        ((FolderList)this.testsPanel).setProjectFolder(projectLocation);
        File[] srcRoot = (File[]) settings.getProperty ("sourceRoot");          //NOI18N
        assert srcRoot != null : "sourceRoot property must be initialized!" ;   //NOI18N
        ((FolderList)this.sourcePanel).setFiles(srcRoot);
        File[] testRoot = (File[]) settings.getProperty ("testRoot");           //NOI18N
        assert testRoot != null : "testRoot property must be initialized!";     //NOI18N
        ((FolderList)this.testsPanel).setFiles (testRoot);

        // #58489 honor existing source folder
        File currentDirectory = null;
        FileObject folder = Templates.getExistingSourcesFolder(wizardDescriptor);
        if (folder != null) {
            currentDirectory = FileUtil.toFile(folder);
        }        
        if (currentDirectory != null && currentDirectory.isDirectory()) {       
            ((FolderList)sourcePanel).setLastUsedDir(currentDirectory);
            ((FolderList)testsPanel).setLastUsedDir(currentDirectory);
        }
    }

    @Override
    void store (WizardDescriptor settings) {
        File[] sourceRoots = ((FolderList)this.sourcePanel).getFiles();
        File[] testRoots = ((FolderList)this.testsPanel).getFiles();
        settings.putProperty ("sourceRoot",sourceRoots);    //NOI18N
        settings.putProperty("testRoot",testRoots);      //NOI18N
    }
    
    @Override
    boolean valid (WizardDescriptor settings) {
        File projectLocation = (File) settings.getProperty ("projdir");  //NOI18N
        File[] sourceRoots = ((FolderList)this.sourcePanel).getFiles();
        File[] testRoots = ((FolderList)this.testsPanel).getFiles();
        String result = checkValidity (projectLocation, sourceRoots, testRoots);
        if (result == null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, "");   //NOI18N
            return true;
        }
        else {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, result);
            return false;
        }
    }

    static String checkValidity (final File projectLocation, final File[] sources, final File[] tests ) {
        String ploc = projectLocation.getAbsolutePath ();        
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
    
    @Override
    void validate (WizardDescriptor d) throws WizardValidationException {
        // sources root
        searchClassFiles (((FolderList)this.sourcePanel).getFiles());
        // test root, not asked in issue 48198
        //searchClassFiles (FileUtil.toFileObject (FileUtil.normalizeFile(new File (tests.getText ()))));
    }

    void prepareValidation() {
        enablePanels(false);
    }

    private void enablePanels(final boolean enable) {
        setCursor(enable ?
            Cursor.getDefaultCursor() :
            Utilities.createProgressCursor(this));
        sourcePanel.setEnabled(enable);
        testsPanel.setEnabled(enable);
        jLabel2.setText(enable ?
                NbBundle.getMessage(PanelSourceFolders.class, "TXT_MoveRootDescription") :
                NbBundle.getMessage(PanelSourceFolders.class, "TXT_SearchingClassFiles"));
    }

    private static void findClassFiles(File folder, List<File> files) {
        File[] kids = folder.listFiles();
        if (kids == null) {
            return;
        }
        for (File kid : kids) {
            if (kid.isFile() && kid.getName().endsWith(".class")) {
                files.add(kid);
            } else if (kid.isDirectory()) {
                findClassFiles(kid, files);
            }
        }
    }
    private void searchClassFiles (File[] folders) throws WizardValidationException {
        try {
            final List<File> classFiles = new ArrayList<File>();
            for (File folder : folders) {
                findClassFiles(folder, classFiles);
            }
            if (!classFiles.isEmpty()) {
                final AtomicInteger action = new AtomicInteger(-1);   //0 - cancel, 1 - delete, 2 - keep
                SwingUtilities.invokeAndWait(new Runnable(){
                    @Override
                    public void run() {
                        JButton DELETE_OPTION = new JButton (NbBundle.getMessage (PanelSourceFolders.class, "TXT_DeleteOption")); // NOI18N
                        JButton KEEP_OPTION = new JButton (NbBundle.getMessage (PanelSourceFolders.class, "TXT_KeepOption")); // NOI18N
                        JButton CANCEL_OPTION = new JButton (NbBundle.getMessage (PanelSourceFolders.class, "TXT_CancelOption")); // NOI18N
                        KEEP_OPTION.setMnemonic(NbBundle.getMessage (PanelSourceFolders.class, "MNE_KeepOption").charAt(0));
                        DELETE_OPTION.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage (PanelSourceFolders.class, "AD_DeleteOption"));
                        KEEP_OPTION.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage (PanelSourceFolders.class, "AD_KeepOption"));
                        CANCEL_OPTION.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage (PanelSourceFolders.class, "AD_CancelOption"));
                        NotifyDescriptor desc = new NotifyDescriptor (
                            NbBundle.getMessage (PanelSourceFolders.class, "MSG_FoundClassFiles"), // NOI18N
                            NbBundle.getMessage (PanelSourceFolders.class, "MSG_FoundClassFiles_Title"), // NOI18N
                            NotifyDescriptor.YES_NO_CANCEL_OPTION,
                            NotifyDescriptor.QUESTION_MESSAGE,
                            new Object[] {DELETE_OPTION, KEEP_OPTION, CANCEL_OPTION},
                            DELETE_OPTION
                            );
                        Object result = DialogDisplayer.getDefault().notify(desc);
                        if (KEEP_OPTION.equals (result)) {
                            action.set(2);
                        } else if (DELETE_OPTION.equals (result)) {
                            action.set(1);
                        } else {
                            action.set(0);
                        }
                    }
                });
                switch (action.get()) {
                    case 0: // Canceled
                        throw new WizardValidationException (this.sourcePanel, "", ""); // NOI18N
                    case 1: //Delete class files
                        for (File f : classFiles) {
                            f.delete(); // ignore if fails
                        }
                        break;
                    case 2: //Keep -> NOP
                        break;
                    default:
                        throw new IllegalStateException();
                }
            }
        } catch (InterruptedException e) {
            Exceptions.printStackTrace(e);
        } catch (InvocationTargetException e) {
            Exceptions.printStackTrace(e);
        } finally {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    enablePanels(true);
                }
            });
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel3 = new javax.swing.JLabel();
        sourcePanel = new FolderList (NbBundle.getMessage(PanelSourceFolders.class,"CTL_SourceRoots"), NbBundle.getMessage(PanelSourceFolders.class,"MNE_SourceRoots").charAt(0),NbBundle.getMessage(PanelSourceFolders.class,"AD_SourceRoots"), NbBundle.getMessage(PanelSourceFolders.class,"CTL_AddSourceRoot"),
            NbBundle.getMessage(PanelSourceFolders.class,"MNE_AddSourceFolder").charAt(0), NbBundle.getMessage(PanelSourceFolders.class,"AD_AddSourceFolder"),NbBundle.getMessage(PanelSourceFolders.class,"MNE_RemoveSourceFolder").charAt(0), NbBundle.getMessage(PanelSourceFolders.class,"AD_RemoveSourceFolder"));
        testsPanel = new FolderList (NbBundle.getMessage(PanelSourceFolders.class,"CTL_TestRoots"), NbBundle.getMessage(PanelSourceFolders.class,"MNE_TestRoots").charAt(0),NbBundle.getMessage(PanelSourceFolders.class,"AD_TestRoots"), NbBundle.getMessage(PanelSourceFolders.class,"CTL_AddTestRoot"),
            NbBundle.getMessage(PanelSourceFolders.class,"MNE_AddTestFolder").charAt(0), NbBundle.getMessage(PanelSourceFolders.class,"AD_AddTestFolder"),NbBundle.getMessage(PanelSourceFolders.class,"MNE_RemoveTestFolder").charAt(0), NbBundle.getMessage(PanelSourceFolders.class,"AD_RemoveTestFolder"));
        jLabel2 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "LBL_SourceDirectoriesLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelSourceFolders.class).getString("ACSN_jLabel3")); // NOI18N
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelSourceFolders.class).getString("ACSD_jLabel3")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.45;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(sourcePanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.45;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(testsPanel, gridBagConstraints);

        jLabel2.setText(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "TXT_MoveRootDescription")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jLabel2, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSN_PanelSourceFolders")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelSourceFolders.class, "ACSD_PanelSourceFolders")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel sourcePanel;
    private javax.swing.JPanel testsPanel;
    // End of variables declaration//GEN-END:variables

    
    static class Panel implements WizardDescriptor.AsynchronousValidatingPanel, WizardDescriptor.FinishablePanel {

        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private PanelSourceFolders component;
        private WizardDescriptor settings;

        @Override
        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        @Override
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

        @Override
        public void storeSettings(Object settings) {
            this.component.store (this.settings);
        }

        @Override
        public void validate() throws WizardValidationException {
            this.component.validate(this.settings);
        }

        @Override
        public void prepareValidation() {
            this.component.prepareValidation();
        }

        @Override
        public boolean isValid() {
            return this.component.valid (this.settings);
        }

        @Override
        public synchronized java.awt.Component getComponent() {
            if (this.component == null) {
                this.component = new PanelSourceFolders (this);
            }
            return this.component;
        }

        @Override
        public HelpCtx getHelp() {
            return new HelpCtx ("org.netbeans.modules.java.j2seproject.ui.wizards.PanelSourceFolders");
        }

        private void fireChangeEvent () {
            changeSupport.fireChange();
        }

        @Override
        public boolean isFinishPanel() {
            return true;
        }

    }

}
