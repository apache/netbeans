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
package org.netbeans.modules.subversion.ui.browser;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Level;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.ui.search.SvnSearch;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class RepositoryPaths implements ActionListener, DocumentListener {
    
    private static final RepositoryFile[] EMPTY_REPOSITORY_FILES = new RepositoryFile[0];

    private RepositoryFile repositoryFile;

    // controled components    
    private JTextComponent repositoryPathTextField;
    private JTextComponent revisionTextField;
    
    private JButton browseButton;
    private JButton searchRevisionButton;
    private JButton browseRevisionButton;

    // browser
    private int browserMode;
    private BrowserAction[] browserActions;
    private String browserPurpose;
    private String browserHelpID;
    private String searchHelpID; 
    
    private boolean valid = false;
    public static final String PROP_VALID = "valid"; // NOI18N
    private List<PropertyChangeListener> listeners;
    
    private PropertyChangeSupport propertyChangeSupport;
    
    public RepositoryPaths(RepositoryFile repositoryFile,
                           JTextComponent repositoryPathTextField,
                           JButton browseButton,
                           JTextField revisionTextField,
                           JButton searchRevisionButton) {
        this(repositoryFile,
            repositoryPathTextField,
            browseButton,
            revisionTextField,
            searchRevisionButton,
            null);
    }

    public RepositoryPaths(RepositoryFile repositoryFile, 
                           JTextComponent repositoryPathTextField,  
                           JButton browseButton, 
                           JTextField revisionTextField, 
                           JButton searchRevisionButton,
                           JButton browseRevisionButton) {

        assert repositoryFile != null;
        assert (repositoryPathTextField !=null && browseButton != null) ||
               (revisionTextField != null && searchRevisionButton != null);

        this.repositoryFile = repositoryFile;

        if(repositoryPathTextField!=null) {
            this.repositoryPathTextField = repositoryPathTextField;
            repositoryPathTextField.getDocument().addDocumentListener(this);
            this.browseButton = browseButton;
            if(browseButton != null) {
                browseButton.addActionListener(this);   
            }            
        }        

        if(revisionTextField!=null) {
            this.revisionTextField = revisionTextField;
            revisionTextField.setInputVerifier(new RevisionInputVerifier());
            revisionTextField.getDocument().addDocumentListener(this);
            this.searchRevisionButton = searchRevisionButton;
            this.browseRevisionButton = browseRevisionButton;
            if(searchRevisionButton != null) {
                searchRevisionButton.addActionListener(this);   
            }            
            if(browseRevisionButton != null) {
                browseRevisionButton.addActionListener(this);
            }
        }                
    }
    
    private static class RevisionInputVerifier extends InputVerifier {
        @Override
        public boolean verify (JComponent input) {
            if (input instanceof JTextComponent) {
                JTextComponent comp = (JTextComponent) input;
                if (comp.getText().trim().isEmpty()) {
                    comp.setText(SVNRevision.HEAD.toString());
                }
            }
            return true;
        }
    }

    public void setupBehavior(String browserPurpose, int browserMode, String browserHelpID, String searchHelpID) {
        this.browserMode    = browserMode;                
        this.browserPurpose = browserPurpose;
        this.browserHelpID  = browserHelpID;
        this.searchHelpID   = searchHelpID;
    }            
    
    public void setupBehavior(String browserPurpose, int browserMode, BrowserAction[] browserActions, String browserHelpID, String searchHelpID) {
        setupBehavior(browserPurpose, browserMode, browserHelpID, searchHelpID);
        this.browserActions = browserActions;
        this.browserPurpose = browserPurpose;
    }            
    
    public RepositoryFile[] getRepositoryFiles() throws MalformedURLException, NumberFormatException {
        return getRepositoryFiles(null);
    }
    
    public RepositoryFile[] getRepositoryFiles(String defaultPath) throws MalformedURLException, NumberFormatException {

        SVNRevision revision = getRevision();
        
        if(repositoryPathTextField==null) {
            RepositoryFile rf = new RepositoryFile(repositoryFile.getRepositoryUrl(), repositoryFile.getFileUrl(), revision);
            return new RepositoryFile[] {rf};
        }
     
        if(getRepositoryString().equals("")) { // NOI18N
            if(defaultPath != null && !defaultPath.trim().equals("") ) {                
                return new RepositoryFile[] { new RepositoryFile(getRepositoryUrl(), defaultPath, revision) } ;
            } else {
                return EMPTY_REPOSITORY_FILES;   
            }            
        }
        if(revision == null) {
            // should not be possible to get here!
            return EMPTY_REPOSITORY_FILES;
        }        
        String[] paths = getRepositoryString().split(","); // NOI18N
        RepositoryFile[] ret = new RepositoryFile[paths.length];
        SVNUrl repositoryUrl = getRepositoryUrl();
       
        for (int i = 0; i < paths.length; i++) {
            String path = paths[i].trim();
            String repositoryUrlString = SvnUtils.decodeToString(getRepositoryUrl());
            if(path.startsWith("file://")  ||   // NOI18N
               path.startsWith("http://")  ||   // NOI18N
               path.startsWith("https://") ||   // NOI18N
               path.startsWith("svn://")   ||   // NOI18N
               path.startsWith("svn+ssh://")) { // NOI18N   // XXX check only for svn+ and concat the remaining part from the protocol
                // must be a complete URL 
                // so check if it matches with the given repository URL
                if(path.startsWith(repositoryUrlString)) {
                    // lets take only the part without the repository base URL
                    ret[i] = new RepositoryFile(repositoryUrl, path.substring(repositoryUrlString.length()), revision);
                } else {
                    throw new MalformedURLException(NbBundle.getMessage(RepositoryPaths.class, "MSG_RepositoryPath_WrongStart", path, repositoryUrlString)); // NOI18N
                }
            } else {                
                ret[i] = new RepositoryFile(repositoryUrl, path, revision);    
            }                
        }                                    
        return ret;
    }

    private void browseRepository() {
        SVNRevision revision = getRevision();
        RepositoryFile[] repositoryFilesToSelect;
        try {
            repositoryFilesToSelect = getRepositoryFiles();
        } catch (MalformedURLException ex) {
            Subversion.LOG.log(Level.SEVERE, null, ex);
            return;
        }
        
        RepositoryFile repositoryFile = new RepositoryFile(getRepositoryUrl(), revision);
        Browser browser = new Browser(browserPurpose, browserMode, repositoryFile, repositoryFilesToSelect, browserActions, browserHelpID);                               

        // handle results    
        RepositoryFile[] selectedFiles = browser.getRepositoryFiles();

        if(selectedFiles.length > 0) {                
            StringBuilder paths = new StringBuilder();
            for (int i = 0; i < selectedFiles.length; i++) {
                paths.append(selectedFiles[i].getPath());
                if(i < selectedFiles.length - 1) {
                    paths.append(", "); // NOI18N
                }
            }  
            setRepositoryTextField(paths.toString());
        }             
    }          
    
    private void browseRepositoryForRevision() {
        final SVNUrl repositoryUrl = getRepositoryUrl();

        String title = NbBundle.getMessage(RepositoryPaths.class, "CTL_BrowseTag"); // NOI18N
        RepositoryFile repoFile = new RepositoryFile(repositoryUrl, SVNRevision.HEAD);
        int mode = Browser.BROWSER_SINGLE_SELECTION_ONLY | Browser.BROWSER_SHOW_FILES | Browser.BROWSER_FOLDERS_SELECTION_ONLY;
        Browser browser = new Browser(title, mode, repoFile, null, null, Browser.BROWSER_HELP_ID_MERGE_TAG);
        final RepositoryFile[] repositoryFiles = browser.getRepositoryFiles();
        if(repositoryFiles == null || repositoryFiles.length == 0) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                revisionTextField.setText(repositoryFiles[0].getRevision().toString());
            }
        });
    }

    private void searchRepository() {
        SVNRevision revision = getRevision();     
        
        final SvnSearch svnSearch;
        try {
            RepositoryFile[] repositoryFiles = getRepositoryFiles();
            if(repositoryFiles.length > 0) {
                svnSearch = new SvnSearch(repositoryFiles); 
            } else {
                svnSearch = new SvnSearch(new RepositoryFile[] { repositoryFile }); 
            }                            
        } catch (MalformedURLException ex) {
            Subversion.LOG.log(Level.SEVERE, null, ex);
            return;
        }
                               
        final DialogDescriptor dialogDescriptor = 
                new DialogDescriptor(svnSearch.getSearchPanel(), java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/browser/Bundle").getString("CTL_RepositoryPath_SearchRevisions")); 
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx(searchHelpID));
        dialogDescriptor.setValid(false);
        
        svnSearch.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                //if( ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName()) ) {
                    dialogDescriptor.setValid(svnSearch.getSelectedRevision() != null);
               // }
            }
        });
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);        
        dialog.setVisible(true);

        // handle results
        if (DialogDescriptor.OK_OPTION.equals(dialogDescriptor.getValue())) {       
            revision = svnSearch.getSelectedRevision();
            if(revision != null) {
                if(revision.equals(SVNRevision.HEAD) ) {
                    setRevisionTextField(""); // NOI18N
                } else {
                    setRevisionTextField(revision.toString());                       
                }
            }
        } else {
            svnSearch.cancel(); 
        }
    }
    
    public SVNRevision getRevision() {
        
        if (revisionTextField == null) {
            return SVNRevision.HEAD;
        }
        String revisionString = getRevisionString();

        if (revisionString.equals("")) {
            return SVNRevision.HEAD;
        }            
        return SvnUtils.getSVNRevision(revisionString);        
    }           
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == browseButton) {
            browseRepository();
        } else if (e.getSource() == searchRevisionButton) {
            searchRepository();
        } else if (e.getSource() == browseRevisionButton) {
            browseRepositoryForRevision();
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        validateUserInput();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        validateUserInput();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        validateUserInput();
    }

    private void validateUserInput() {
        boolean oldValue = this.valid;
        boolean valid = true;

        RepositoryFile[] files = null;
        try {
            files = getRepositoryFiles();
        } catch (NumberFormatException ex) {
            valid = false;
        } catch (MalformedURLException ex) {            
            valid = false;
        }
        
        if(browseButton != null) {
            browseButton.setEnabled(valid);
        }        
        if(searchRevisionButton != null) {
            searchRevisionButton.setEnabled(valid);
        }
        
        if(valid && !acceptEmptyUrl() && repositoryPathTextField != null && getRepositoryString().equals("")) { // NOI18N
            valid = false;
        }

        if(valid && !acceptEmptyRevision() && revisionTextField != null && getRevisionString().equals("")) { // NOI18N
            valid = false;
        }        
        
        this.valid = valid;
        fireValidPropertyChanged(oldValue, valid);

    }

    private void fireValidPropertyChanged(boolean oldValue, boolean valid) {
        getChangeSupport().firePropertyChange(new PropertyChangeEvent(this, PROP_VALID, oldValue, valid));        
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        getChangeSupport().addPropertyChangeListener(l);        
        validateUserInput();
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        getChangeSupport().removePropertyChangeListener(l);
    }
    
    private PropertyChangeSupport getChangeSupport() {
        if(propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        return propertyChangeSupport;
    }

    protected boolean acceptEmptyUrl() {
        return false;
    }
        
    protected boolean acceptEmptyRevision() {
        return true;
    }

    public SVNUrl getRepositoryUrl() {
        return repositoryFile.getRepositoryUrl();
    }

    public void setRepositoryFile(RepositoryFile repositoryFile) {
        this.repositoryFile = repositoryFile;
    }        

    public void setRepositoryTextField(String url) {
        repositoryPathTextField.setText(url);
    }
    
    public void setRevisionTextField(String revision) {
        revisionTextField.setText(revision); 
    }
    
    protected String getRepositoryString() {
        String text = repositoryPathTextField.getText();
        int length = text.length();

        if (length == 0) {
            return text;
        }

        char c;

        int startIndex;
        for (startIndex = 0; startIndex < length; startIndex++) {
            c = text.charAt(startIndex);
            if ((c != ',') && (c != ' ')) {
                break;
            }
        }
        if (startIndex == length) {         //just spaces and commas
            return "";                                                  //NOI18N
        }

        int endIndex = length;
        c = text.charAt(endIndex - 1);
        while ((c == ',') || (c == ' ')) {
            c = text.charAt(--endIndex - 1);
        }

        return ((startIndex == 0) && (endIndex == length))
               ? text
               : text.substring(startIndex, endIndex);
    }
    
    protected String getRevisionString() {
        return revisionTextField.getText().trim();
    }    
}
