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
package org.netbeans.modules.subversion.remote.ui.copy;

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
import org.netbeans.modules.subversion.remote.ui.search.SvnSearch;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
public class SwitchTo extends CopyDialog implements PropertyChangeListener {

    private final RepositoryPaths repositoryPaths;
    private final VCSFileProxy root;
    private final RepositoryFile repositoryFile;
        
    public SwitchTo(RepositoryFile repositoryFile, VCSFileProxy root, boolean localChanges) {
        super(root, new SwitchToPanel(), NbBundle.getMessage(SwitchTo.class, "CTL_SwitchTo_Title", root.getName()), NbBundle.getMessage(SwitchTo.class, "CTL_SwitchTo_Action")); // NOI18N
        
        this.root = root;        
        this.repositoryFile = repositoryFile;       
        
        SwitchToPanel panel = getSwitchToPanel();
        panel.warningLabel.setVisible(localChanges);

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
        getSwitchToPanel().getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SwitchTo.class, "CTL_SwitchTo_RepositoryFile"));
        
        String browserPurposeMessage;
        int browserMode;
        if(root.isFile()) {
            getSwitchToPanel().urlLabel.setText(NbBundle.getMessage(SwitchTo.class, "CTL_SwitchTo_RepositoryFile")); // NOI18N
            browserPurposeMessage = org.openide.util.NbBundle.getMessage(CreateCopy.class, "LBL_BrowserMessageSwitchFile");
            browserMode = Browser.BROWSER_SINGLE_SELECTION_ONLY | Browser.BROWSER_SHOW_FILES | Browser.BROWSER_FILES_SELECTION_ONLY;                        
        } else {
            browserPurposeMessage = org.openide.util.NbBundle.getMessage(CreateCopy.class, "LBL_BrowserMessageSwitchFolder");
            browserMode = Browser.BROWSER_SINGLE_SELECTION_ONLY;                                    
        }
        repositoryPaths.setupBehavior(browserPurposeMessage, browserMode, Browser.BROWSER_HELP_ID_SWITCH_TO, SvnSearch.SEACRH_HELP_ID_SWITCH);                
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
    
    private SwitchToPanel getSwitchToPanel() {
        return (SwitchToPanel) getPanel();
    }    
        
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if( evt.getPropertyName().equals(RepositoryPaths.PROP_VALID) ) {            
            boolean valid = (Boolean)evt.getNewValue();
            getOKButton().setEnabled(valid);
        }        
    }    
}
