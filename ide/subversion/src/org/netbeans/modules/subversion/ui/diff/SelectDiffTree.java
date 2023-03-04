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
package org.netbeans.modules.subversion.ui.diff;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Level;
import javax.swing.JTextField;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.ui.browser.Browser;
import org.netbeans.modules.subversion.ui.browser.RepositoryPaths;
import org.netbeans.modules.subversion.ui.copy.CopyDialog;
import org.netbeans.modules.subversion.ui.search.SvnSearch;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class SelectDiffTree extends CopyDialog implements PropertyChangeListener {

    private final RepositoryPaths repositoryPaths;
    private final File root;
    private final RepositoryFile repositoryFile;
        
    @NbBundle.Messages({
        "# {0} - repository file name", "CTL_SelectDiffTree_Title=Select Tree To Diff - {0}",
        "CTL_SelectDiffTree_okButton=Select",
        "CTL_SelectDiffTree_RepositoryFile=Repository File",
        "LBL_BrowserMessageSelectDiffTreeFile=&Choose the File you want to diff",
        "LBL_BrowserMessageSelectDiffTreeFolder=&Choose the Folder you want to diff"
    })
    public SelectDiffTree (RepositoryFile repositoryFile, File root) {
        super(new SelectDiffTreePanel(), Bundle.CTL_SelectDiffTree_Title(root.getName()),
                Bundle.CTL_SelectDiffTree_okButton());
        
        this.root = root;        
        this.repositoryFile = repositoryFile;       
        
        SelectDiffTreePanel panel = getSelectDiffTreePanel ();
        setupUrlComboBox(repositoryFile, panel.urlComboBox, true);
        
        repositoryPaths = 
            new RepositoryPaths(
                repositoryFile, 
                (JTextField) panel.urlComboBox.getEditor().getEditorComponent(),
                panel.browseRepositoryButton,
                panel.revisionTextField,
                panel.searchRevisionButton
            );
        repositoryPaths.addPropertyChangeListener(this);
        getSelectDiffTreePanel ().getAccessibleContext().setAccessibleDescription(Bundle.CTL_SelectDiffTree_RepositoryFile());
        
        String browserPurposeMessage;
        int browserMode;
        if(root.isFile()) {
            getSelectDiffTreePanel ().urlLabel.setText(Bundle.CTL_SelectDiffTree_RepositoryFile());
            browserPurposeMessage = Bundle.LBL_BrowserMessageSelectDiffTreeFile();
            browserMode = Browser.BROWSER_SINGLE_SELECTION_ONLY | Browser.BROWSER_SHOW_FILES | Browser.BROWSER_FILES_SELECTION_ONLY;
        } else {
            browserPurposeMessage = Bundle.LBL_BrowserMessageSelectDiffTreeFolder();
            browserMode = Browser.BROWSER_SINGLE_SELECTION_ONLY;                                    
        }
        repositoryPaths.setupBehavior(browserPurposeMessage, browserMode, Browser.BROWSER_HELP_ID_SELECT_DIFF_TREE, SvnSearch.SEARCH_HELP_ID_SELECT_DIFF_TREE);                
    }            
    
    RepositoryFile getRepositoryFile() {        
        try {
            RepositoryFile[] repositoryFiles = repositoryPaths.getRepositoryFiles();
            if(repositoryFiles.length > 0) {
                return repositoryFiles[0];
            } else {
                SVNRevision revision = repositoryPaths.getRevision();
                if(revision == null) {
                    return null;
                }
                SVNUrl url = SvnUtils.getRepositoryUrl(root);
                RepositoryFile rf = new RepositoryFile(repositoryFile.getRepositoryUrl(), url, revision);
                return rf;
            }
        } catch (SVNClientException ex) {            
            SvnClientExceptionHandler.notifyException(ex, true, true);
        } catch (MalformedURLException ex) {
            // should be already checked and 
            // not happen at this place anymore
            Subversion.LOG.log(Level.INFO, null, ex);            
        }
        return null;
    }    
    
    private SelectDiffTreePanel getSelectDiffTreePanel () {
        return (SelectDiffTreePanel) getPanel();
    }    
        
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if( evt.getPropertyName().equals(RepositoryPaths.PROP_VALID) ) {            
            boolean valid = ((Boolean)evt.getNewValue()).booleanValue();
            getOKButton().setEnabled(valid);
        }        
    }    
}
