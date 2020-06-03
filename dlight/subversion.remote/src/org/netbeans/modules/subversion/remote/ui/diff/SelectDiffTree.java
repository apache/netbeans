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
package org.netbeans.modules.subversion.remote.ui.diff;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.util.logging.Level;
import javax.swing.JTextField;
import org.netbeans.modules.subversion.remote.RepositoryFile;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.remote.ui.browser.Browser;
import org.netbeans.modules.subversion.remote.ui.browser.RepositoryPaths;
import org.netbeans.modules.subversion.remote.ui.copy.CopyDialog;
import org.netbeans.modules.subversion.remote.ui.search.SvnSearch;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.NbBundle.Messages;

/**
 *
 * 
 */
public class SelectDiffTree extends CopyDialog implements PropertyChangeListener {

    private final RepositoryPaths repositoryPaths;
    private final VCSFileProxy root;
    private final RepositoryFile repositoryFile;
        
    @Messages({
        "# {0} - repository file name", "CTL_SelectDiffTree_Title=Select Tree To Diff - {0}",
        "CTL_SelectDiffTree_okButton=Select",
        "CTL_SelectDiffTree_RepositoryFile=Repository File",
        "LBL_BrowserMessageSelectDiffTreeFile=&Choose the File you want to diff",
        "LBL_BrowserMessageSelectDiffTreeFolder=&Choose the Folder you want to diff"
    })
    public SelectDiffTree (RepositoryFile repositoryFile, VCSFileProxy root) {
        super(root, new SelectDiffTreePanel(), Bundle.CTL_SelectDiffTree_Title(root.getName()),
                Bundle.CTL_SelectDiffTree_okButton());
        
        this.root = root;        
        this.repositoryFile = repositoryFile;       
        
        SelectDiffTreePanel panel = getSelectDiffTreePanel ();
        setupUrlComboBox(repositoryFile, panel.urlComboBox);
        
        repositoryPaths = 
            new RepositoryPaths(
                VCSFileProxySupport.getFileSystem(root), repositoryFile, 
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
                RepositoryFile rf = new RepositoryFile(VCSFileProxySupport.getFileSystem(root), repositoryFile.getRepositoryUrl(), url, revision);
                return rf;
            }
        } catch (SVNClientException ex) {            
            SvnClientExceptionHandler.notifyException(new Context(root), ex, true, true);
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
