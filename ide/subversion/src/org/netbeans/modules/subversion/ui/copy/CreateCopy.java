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
package org.netbeans.modules.subversion.ui.copy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Level;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.ui.browser.Browser;
import org.netbeans.modules.subversion.ui.browser.BrowserAction;
import org.netbeans.modules.subversion.ui.browser.CreateFolderAction;
import org.netbeans.modules.subversion.ui.browser.RepositoryPaths;
import org.netbeans.modules.subversion.ui.search.SvnSearch;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class CreateCopy extends CopyDialog implements DocumentListener, FocusListener, ActionListener, PropertyChangeListener {

    private final RepositoryPaths copyToRepositoryPaths;
    private final RepositoryPaths copyFromRepositoryPaths;
    
    private final File localeFile;
    private final RepositoryFile repositoryFile;
    private final boolean localChanges;
    
    /** Creates a new instance of CreateCopy */
    public CreateCopy(RepositoryFile repositoryFile, File localeFile, boolean localChanges) {        
        super(new CreateCopyPanel(), NbBundle.getMessage(CreateCopy.class, "CTL_CopyDialog_Prompt", localeFile.getName()), NbBundle.getMessage(CreateCopy.class, "CTL_CopyDialog_Title")); // NOI18N
        
        this.localeFile = localeFile;        
        this.repositoryFile = repositoryFile;
        this.localChanges = localChanges;
        
        CreateCopyPanel panel = getCreateCopyPanel();                
                       
        panel.localRadioButton.addActionListener(this);        
        panel.remoteRadioButton.addActionListener(this);        
        panel.skipCheckBox.addActionListener(this);        

        panel.copyFromLocalTextField.setText(localeFile.getAbsolutePath());
        panel.copyFromRemoteTextField.setText(SvnUtils.decodeToString(repositoryFile.getFileUrl()));        
                        
        copyFromRepositoryPaths = 
            new RepositoryPaths(
                repositoryFile, 
                panel.copyFromRemoteTextField,
                null,
                panel.copyFromRevisionTextField,
                panel.searchButton
            );
        copyFromRepositoryPaths.setupBehavior(null, 0, null, SvnSearch.SEARCH_HELP_ID_UPDATE);
        
        if(localeFile.isFile()) {
            org.openide.awt.Mnemonics.setLocalizedText(panel.localRadioButton, org.openide.util.NbBundle.getMessage(CreateCopy.class, "CTL_CopyForm_fromLocalFile"));               // NOI18N                 
            org.openide.awt.Mnemonics.setLocalizedText(panel.remoteRadioButton, org.openide.util.NbBundle.getMessage(CreateCopy.class, "CTL_CopyForm_fromRemoteFile"));             // NOI18N            
            panel.skipCheckBox.setEnabled(false);
        } else {
            org.openide.awt.Mnemonics.setLocalizedText(panel.localRadioButton, org.openide.util.NbBundle.getMessage(CreateCopy.class, "CTL_CopyForm_fromLocalFolder"));             // NOI18N            
            org.openide.awt.Mnemonics.setLocalizedText(panel.remoteRadioButton, org.openide.util.NbBundle.getMessage(CreateCopy.class, "CTL_CopyForm_fromRemoteFolder"));           // NOI18N
        }        
        
        panel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CreateCopy.class, "CTL_CopyDialog_Title"));                   // NOI18N
        
        copyToRepositoryPaths = 
            new RepositoryPaths(
                repositoryFile, 
                (JTextComponent) panel.urlComboBox.getEditor().getEditorComponent(),
                panel.browseRepositoryButton,
                null,
                null
            );
                
        String browserPurposeMessage = "";
        if(localeFile.isFile()) {
            browserPurposeMessage = org.openide.util.NbBundle.getMessage(CreateCopy.class, "LBL_BrowserMessageCopyFile");                       // NOI18N
        } else {
            browserPurposeMessage = org.openide.util.NbBundle.getMessage(CreateCopy.class, "LBL_BrowserMessageCopyFolder");                     // NOI18N
        }

        String defaultFolderName = localeFile.isFile() ? "" : localeFile.getName();
        int browserMode = Browser.BROWSER_SINGLE_SELECTION_ONLY;
        copyToRepositoryPaths.setupBehavior(browserPurposeMessage, browserMode, new BrowserAction[] { new CreateFolderAction(defaultFolderName)} , Browser.BROWSER_HELP_ID_COPY, null);                
        copyToRepositoryPaths.addPropertyChangeListener(this);

        setupUrlComboBox(repositoryFile, panel.urlComboBox, false);
        panel.messageTextArea.getDocument().addDocumentListener(this);
        ((JTextComponent) panel.urlComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
        
        setFromLocal();
        validateUserInput();
    }       
    
    protected void validateUserInput() {                        
        String text = getCreateCopyPanel().messageTextArea.getText();    
        try {
            RepositoryFile rf[] = copyToRepositoryPaths.getRepositoryFiles();
            if(rf == null || rf.length == 0) {
                
                setErrorText(org.openide.util.NbBundle.getMessage(CreateCopy.class, "LBL_MISSING_REPOSITORY_FOLDER"));
                
                getOKButton().setEnabled(false);        
                return;
            }
        } catch (NumberFormatException ex) {            
            setErrorText(ex.getLocalizedMessage()); // should not happen            
            getOKButton().setEnabled(false);        
            return;
        } catch (MalformedURLException ex) {            
            setErrorText(ex.getLocalizedMessage()); // should not happen           
            getOKButton().setEnabled(false);        
            return;
        }        
        if (text == null || text.length() == 0) {   
                        
            setErrorText(org.openide.util.NbBundle.getMessage(CreateCopy.class, "LBL_MISSING_COPY_MESSAGE"));
            
            getOKButton().setEnabled(false);        
            return;
        }    
        resetErrorText();
        getOKButton().setEnabled(true);              
    }    

    void setErrorText(String txt) {
        CreateCopyPanel panel = getCreateCopyPanel();
        panel.invalidValuesLabel.setVisible(true);
        panel.invalidValuesLabel.setText(txt);
    }
    
    private void resetErrorText() {
        CreateCopyPanel panel = getCreateCopyPanel();        
        panel.invalidValuesLabel.setVisible(false);
        panel.invalidValuesLabel.setText("");        
    }
    
    CreateCopyPanel getCreateCopyPanel() {
        return (CreateCopyPanel) getPanel();
    }
    
    RepositoryFile getToRepositoryFile() {
        try {
            return getToRepositoryFileIntern();
        } catch (MalformedURLException ex) {
            Subversion.LOG.log(Level.INFO, null, ex); // should not happen
        } catch (NumberFormatException ex) {
            Subversion.LOG.log(Level.INFO, null, ex); // should not happen
        }
        return null;
    }

    private RepositoryFile getToRepositoryFileIntern() throws NumberFormatException, MalformedURLException {
        RepositoryFile[] toRepositoryFiles = copyToRepositoryPaths.getRepositoryFiles();
        if(toRepositoryFiles.length > 0) {
            RepositoryFile toRepositoryFile = toRepositoryFiles[0];
            if(skipContents()) {
                return toRepositoryFile;                
            } else {
                if(isLocal()) {
                    return toRepositoryFile.appendPath(localeFile.getName());   
                } else {
                    return toRepositoryFile.appendPath(repositoryFile.getFileUrl().getLastPathSegment());   
                }             
            }
        } else {
            return null;
        }
    }

    String getMessage() {
        return SvnUtils.fixLineEndings(getCreateCopyPanel().messageTextArea.getText());
    }

    boolean isLocal() {
        return getCreateCopyPanel().localRadioButton.isSelected();        
    }
    
    File getLocalFile() {
        return localeFile;
    }
    
    RepositoryFile getFromRepositoryFile() {
        try {
            return copyFromRepositoryPaths.getRepositoryFiles()[0];
        }
        catch (MalformedURLException ex) {
            Subversion.LOG.log(Level.INFO, null, ex); // should not happen
        } catch (NumberFormatException ex) {
            Subversion.LOG.log(Level.INFO, null, ex); // should not happen
        }
        return null;
    }
    
    boolean switchTo() {
        return getCreateCopyPanel().switchToCheckBox.isSelected();
    }

    boolean skipContents() {
        return getCreateCopyPanel().skipCheckBox.isSelected();
    }
    
    public void insertUpdate(DocumentEvent e) {
        validateUserInput();
        setPreview();
    }

    public void removeUpdate(DocumentEvent e) {
        validateUserInput();        
        setPreview();        
    }

    public void changedUpdate(DocumentEvent e) {
        validateUserInput();        
        setPreview();        
    }

    public void focusGained(FocusEvent e) {
    }

    public void focusLost(FocusEvent e) {
        validateUserInput();        
        setPreview();        
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if( evt.getPropertyName().equals(RepositoryPaths.PROP_VALID) ) {
            boolean valid = ((Boolean)evt.getNewValue()).booleanValue();
            if(valid) {
                // it's a bit more we have to validate
                validateUserInput();
            } else {
                getOKButton().setEnabled(valid);
            }            
        }        
    }

    public void actionPerformed(ActionEvent evt) {
        if(evt.getSource() == getCreateCopyPanel().localRadioButton) {
            setFromLocal();        
            setPreview();        
        } else if(evt.getSource() == getCreateCopyPanel().remoteRadioButton) {
            selectFromRemote();
            setPreview();        
        } else if(evt.getSource() == getCreateCopyPanel().skipCheckBox) {
            setPreview();
        }        
    }
    
    private void setFromLocal() {
        CreateCopyPanel panel = getCreateCopyPanel();
        panel.copyFromLocalTextField.setEnabled(true);        
        panel.copyFromRemoteTextField.setEnabled(false);
        panel.copyFromRevisionTextField.setEnabled(false);
        panel.searchButton.setEnabled(false);
        panel.warningLabel.setVisible(localChanges);                                              
    }

    private void selectFromRemote() {
        CreateCopyPanel panel = getCreateCopyPanel();
        panel.copyFromLocalTextField.setEnabled(false);        
        panel.copyFromRemoteTextField.setEnabled(true);
        panel.copyFromRevisionTextField.setEnabled(true);
        panel.searchButton.setEnabled(true);
        panel.warningLabel.setVisible(false);                                              
    }

    private void setPreview() {
        try {
            RepositoryFile repositoryFile = getToRepositoryFileIntern();
            if(repositoryFile!=null) {
                getCreateCopyPanel().previewTextField.setText(SvnUtils.decodeToString(repositoryFile.getFileUrl()));    
            } else {
                getCreateCopyPanel().previewTextField.setText("");              // NOI18N
            }
        }
        catch (NumberFormatException ex) {
            // wrong value -> we can't copy anything
            getCreateCopyPanel().previewTextField.setText("");                  // NOI18N
        } catch (MalformedURLException ex) {
            // wrong value -> we can't copy anything
            getCreateCopyPanel().previewTextField.setText("");                  // NOI18N
        };                
    }
    
}
