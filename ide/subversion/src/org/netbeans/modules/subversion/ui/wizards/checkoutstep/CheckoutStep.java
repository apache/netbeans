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

package org.netbeans.modules.subversion.ui.wizards.checkoutstep;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Level;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.ui.browser.Browser;
import org.netbeans.modules.subversion.ui.wizards.AbstractStep;
import org.netbeans.modules.subversion.ui.browser.RepositoryPaths;
import org.netbeans.modules.subversion.ui.search.SvnSearch;
import org.netbeans.modules.versioning.util.AccessibleJFileChooser;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 * @author Tomas Stupka
 */
public class CheckoutStep extends AbstractStep implements ActionListener, DocumentListener, FocusListener, ItemListener {

    public static final String CHECKOUT_DIRECTORY = "checkoutStep.checkoutDirectory";
    
    private CheckoutPanel workdirPanel;
    private RepositoryPaths repositoryPaths;
    private boolean invalidTarget;

    @Override
    public HelpCtx getHelp() {    
        return new HelpCtx(CheckoutStep.class);
    }    

    @Override
    protected JComponent createComponent() {
        if (workdirPanel == null) {
            workdirPanel = new CheckoutPanel();
            workdirPanel.browseWorkdirButton.addActionListener(this);
            workdirPanel.scanForProjectsCheckBox.addItemListener(this);
            workdirPanel.atWorkingDirLevelCheckBox.addItemListener(this);
                    
            workdirPanel.workdirTextField.setText(defaultWorkingDirectory().getPath().trim());            
            workdirPanel.workdirTextField.getDocument().addDocumentListener(this);                
            workdirPanel.workdirTextField.addFocusListener(this);
            workdirPanel.repositoryPathTextField.getDocument().addDocumentListener(this);        
            workdirPanel.repositoryPathTextField.addFocusListener(this);
            workdirPanel.revisionTextField.getDocument().addDocumentListener(this);
            workdirPanel.revisionTextField.addFocusListener(this);                        
        }          
        validateUserInput(true);                                
        return workdirPanel;              
    }

    public void setup(RepositoryFile repositoryFile) {
        if(repositoryPaths == null) {                    
            repositoryPaths = 
                new RepositoryPaths(
                        repositoryFile, 
                        workdirPanel.repositoryPathTextField, 
                        workdirPanel.browseRepositoryButton, 
                        workdirPanel.revisionTextField, 
                        workdirPanel.searchRevisionButton,
                        workdirPanel.browseRevisionButton
                );        
            String browserPurposeMessage = org.openide.util.NbBundle.getMessage(CheckoutStep.class, "LBL_BrowserMessage");
            int browserMode = Browser.BROWSER_SHOW_FILES | Browser.BROWSER_FOLDERS_SELECTION_ONLY;
            repositoryPaths.setupBehavior(browserPurposeMessage, browserMode, Browser.BROWSER_HELP_ID_CHECKOUT, SvnSearch.SEACRH_HELP_ID_CHECKOUT);
        } else {
            repositoryPaths.setRepositoryFile(repositoryFile);
        }                
        workdirPanel.repositoryPathTextField.setText(repositoryFile.getPath());
        refreshWorkingCopy(new RepositoryFile[] {repositoryFile});
        if(!repositoryFile.getRevision().equals(SVNRevision.HEAD)) {
            workdirPanel.revisionTextField.setText(repositoryFile.getRevision().toString());
        } else {
            workdirPanel.revisionTextField.setText(SVNRevision.HEAD.toString());
        }
        workdirPanel.revisionTextField.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify (JComponent input) {
                if (workdirPanel.revisionTextField.getText().trim().isEmpty()) {
                    workdirPanel.revisionTextField.setText(SVNRevision.HEAD.toString());
                }
                return true;
            }
        });
        workdirPanel.scanForProjectsCheckBox.setSelected(SvnModuleConfig.getDefault().getShowCheckoutCompleted());
    }    
     
    @Override
    protected void validateBeforeNext() {
        if (validateUserInput(true)) {
            String text = getWorkdirText();
            File file = new File(text);
            if (file.exists() == false) {
                boolean done = file.mkdirs();
                if (done == false) {
                    invalid(new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(CheckoutStep.class, "BK2013") + file.getPath(), false));// NOI18N
                    invalidTarget = true;
                }
            }
        }
    }

    private String getWorkdirText () {
        return workdirPanel.workdirTextField.getText().trim();
    }

    private boolean validateUserInput(boolean full) {                
        invalidTarget = false;
        if(repositoryPaths != null) {
            try {           
                repositoryPaths.getRepositoryFiles();
                if (repositoryPaths.getRevision() instanceof SVNRevision.Number && ((SVNRevision.Number) repositoryPaths.getRevision()).getNumber() < 0) {
                    invalid(new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(CheckoutStep.class, "BK2018"), false)); //NOI18N
                    return false;
                }
            } catch (NumberFormatException ex) {
                invalid(new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(CheckoutStep.class, "BK2018"), false));// NOI18N
                return false;
            } catch (MalformedURLException ex) {
                invalid(new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(CheckoutStep.class, "BK2015"), true));// NOI18N
                return false;
            }
        }
        
        String text = getWorkdirText();
        if (text == null || text.length() == 0) {
            invalid(new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(CheckoutStep.class, "BK2014"), true));// NOI18N
            return false;
        }                
        
        AbstractStep.WizardMessage errorMessage = null;
        if (full) {
            File file = new File(text);
            if (file.exists() == false) {
                // it's automaticaly create later on, check for permisions here
                File parent = file.getParentFile();
                while (parent != null) {
                    if (parent.exists()) {
                        if (parent.canWrite() == false) {
                            errorMessage = new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(CheckoutStep.class, "BK2016") + parent.getPath(), false);// NOI18N
                        }
                        break;
                    }
                    parent = parent.getParentFile();
                }
            } else {
                if (file.isFile()) {
                    errorMessage = new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(CheckoutStep.class, "BK2017"), false);// NOI18N
                }
            }
        }

        if (errorMessage == null) {
            valid();
        } else {
            invalid(errorMessage);
        }

        return errorMessage == null;
    }
    
    private void onBrowseWorkdir() {
        File defaultDir = defaultWorkingDirectory();
        JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(CheckoutStep.class, "ACSD_BrowseFolder"), defaultDir);// NOI18N
        fileChooser.setDialogTitle(NbBundle.getMessage(CheckoutStep.class, "BK0010"));// NOI18N
        fileChooser.setMultiSelectionEnabled(false);
        FileFilter[] old = fileChooser.getChoosableFileFilters();
        for (int i = 0; i < old.length; i++) {
            FileFilter fileFilter = old[i];
            fileChooser.removeChoosableFileFilter(fileFilter);

        }
        fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }
            @Override
            public String getDescription() {
                return NbBundle.getMessage(CheckoutStep.class, "BK0008");// NOI18N
            }
        });
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showDialog(workdirPanel, NbBundle.getMessage(CheckoutStep.class, "BK0009"));// NOI18N
        File f = fileChooser.getSelectedFile();
        if (f != null) {
            workdirPanel.workdirTextField.setText(f.getAbsolutePath().trim());
        }                
    }    
    
    /**
     * Returns file to be initaly used.
     * <ul>
     * <li>first is takes text in workTextField
     * <li>then recent project folder
     * <li>then recent checkout folder
     * <li>finally <tt>user.home</tt>
     * <ul>
     */
    private File defaultWorkingDirectory() {
        File defaultDir = null;
        String current = getWorkdirText();
        if (current != null && !(current.trim().equals(""))) {  // NOI18N
            File currentFile = new File(current);
            while (currentFile != null && currentFile.exists() == false) {
                currentFile = currentFile.getParentFile();
            }
            if (currentFile != null) {
                if (currentFile.isFile()) {
                    defaultDir = currentFile.getParentFile();
                } else {
                    defaultDir = currentFile;
                }
            }
        }

        if (defaultDir == null) {
            String coDir = SvnModuleConfig.getDefault().getPreferences().get(CHECKOUT_DIRECTORY, null);
            if(coDir != null) {
                defaultDir = new File(coDir);               
            }            
        }

        if (defaultDir == null) {
            File projectFolder = ProjectChooser.getProjectsFolder();
            if (projectFolder.exists() && projectFolder.isDirectory()) {
                defaultDir = projectFolder;
            }
        }

        if (defaultDir == null) {
            defaultDir = new File(System.getProperty("user.home"));  // NOI18N
        }

        return defaultDir;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {        
        validateUserInput(false);
        repositoryFoldersChanged();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        validateUserInput(false);
        repositoryFoldersChanged();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {        
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (!invalidTarget) {
            // click on Finish triggers in a series of focus events which results in deletion of the invalid target message
            // so do not validate when Finish is clicked and the message is shown
            validateUserInput(true);
        }
        repositoryFoldersChanged();
    }
        
    @Override
    public void actionPerformed(ActionEvent e) {
        assert e.getSource() == workdirPanel.browseWorkdirButton;
        onBrowseWorkdir();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Object source = e.getSource();
        if (source == workdirPanel.scanForProjectsCheckBox) {
            SvnModuleConfig.getDefault().setShowCheckoutCompleted(workdirPanel.scanForProjectsCheckBox.isSelected());
        } else if (source == workdirPanel.atWorkingDirLevelCheckBox) {
            RepositoryFile[] repositoryFiles = null;
            if (getRepositoryPath().length() != 0) {
                try {
                    repositoryFiles = repositoryPaths.getRepositoryFiles();
                } catch (NumberFormatException ex) {
                    // ignore
                } catch (MalformedURLException ex) {
                    // ignore
                }
            }
            refreshWorkingCopy(repositoryFiles);
        }
    }
    
    public File getWorkdir() {
        return new File(getWorkdirText());
    }        

    public RepositoryFile[] getRepositoryFiles() {
        try {            
            return repositoryPaths.getRepositoryFiles(".");
        } catch (MalformedURLException ex) {
            Subversion.LOG.log(Level.INFO, null, ex); // should not happen
        }
        return null;
    }

    public boolean isAtWorkingDirLevel() {
        return workdirPanel.atWorkingDirLevelCheckBox.isSelected();
    }

    public boolean isExport() {
        return workdirPanel.exportCheckBox.isSelected();
    }

    public boolean isOldFormatPreferred () {
        return workdirPanel.preferOldFormatCheckBox.isSelected();
    }

    private void repositoryFoldersChanged() {
        if (getRepositoryPath().equals("")) {
            resetWorkingDirLevelCheckBox();
            refreshWorkingCopy(null);
            return;
        }        
        
        RepositoryFile[] repositoryFiles = null;
        try {
            repositoryFiles = repositoryPaths.getRepositoryFiles();
        } catch (NumberFormatException ex) {
            // ignore
        } catch (MalformedURLException ex) {
            // ignore
        }

        if ((repositoryFiles == null) || (repositoryFiles.length == 0) ||
           repositoryFiles.length >  1) 
        { 
            resetWorkingDirLevelCheckBox();
            refreshWorkingCopy(repositoryFiles);
            return;
        }        
        
        String repositoryFolder = repositoryFiles[0].getFileUrl().getLastPathSegment().trim();                           
        if(repositoryFolder.equals("")  ||      // the skip option doesn't make sense if there is no one, 
           repositoryFolder.equals("."))        // or more as one folder to be checked out  
        {
            resetWorkingDirLevelCheckBox();
            refreshWorkingCopy(repositoryFiles);
            return;
        } else {                        
            workdirPanel.atWorkingDirLevelCheckBox.setText (
                    NbBundle.getMessage(CheckoutStep.class, 
                                        "CTL_Checkout_CheckoutContentFolder", 
                                         new Object[] {repositoryFolder})
            );
            workdirPanel.atWorkingDirLevelCheckBox.setEnabled(true);                
            refreshWorkingCopy(repositoryFiles);
        }
    }

    private void resetWorkingDirLevelCheckBox() {
        workdirPanel.atWorkingDirLevelCheckBox.setText(NbBundle.getMessage(CheckoutStep.class, "CTL_Checkout_CheckoutContentEmpty"));
        workdirPanel.atWorkingDirLevelCheckBox.setEnabled(false);
    }

    private String getRepositoryPath() {
        return workdirPanel.repositoryPathTextField.getText().trim();
    }

    private void refreshWorkingCopy(RepositoryFile[] repositoryFiles) {
        String localFolderPath = trimTrailingSlashes(getWorkdirText());
        int filesCount = (repositoryFiles != null) ? repositoryFiles.length : 0;

        String workingCopyPath;
        if (filesCount == 0) {
            workingCopyPath = localFolderPath;
        } else {
            String repositoryFilePath = trimSlashes(repositoryFiles[0].getPath());
            if (repositoryFilePath.equals(".")) {                       //NOI18N
                repositoryFilePath = "";                                //NOI18N
            }
            if ((filesCount == 1)
                && (workdirPanel.atWorkingDirLevelCheckBox.isSelected()
                    || (repositoryFilePath.length() == 0))) {
                workingCopyPath = localFolderPath;
            } else {
                String repositoryFolderName = repositoryFiles[0].getName();
                StringBuilder buf = new StringBuilder(localFolderPath.length()
                                                      + repositoryFolderName.length()
                                                      + 10);
                buf.append(localFolderPath).append(File.separatorChar).append(repositoryFolderName);
                if (filesCount > 1) {
                    buf.append(", ...");                                //NOI18N
                }
                workingCopyPath = buf.toString();
            }
        }
        workdirPanel.workingCopy.setText(workingCopyPath);
    }

    private static String trimTrailingSlashes(String path) {
        return trimSlashes(path, true);
    }

    private static String trimSlashes(String path) {
        return trimSlashes(path, false);
    }

    private static String trimSlashes(String path, boolean trailingOnly) {
        final int length = path.length();
        if (length == 0) {
            return path;
        }

        int startIndex = 0;
        if (!trailingOnly) {
            while ((startIndex < length) && (path.charAt(startIndex) == '/')) {
                startIndex++;
            }
            if (startIndex == length) {
                return "";                                              //NOI18N
            }
        }

        int endIndex = length;
        while ((endIndex != 0) && (path.charAt(endIndex - 1) == '/')) {
            endIndex--;
        }
        if (endIndex == 0) {
            return "";                                                  //NOI18N
        }

        return (startIndex == 0) && (endIndex == length)
               ? path
               : path.substring(startIndex, endIndex);
    }

}

