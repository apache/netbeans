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
package org.netbeans.modules.subversion.ui.copy;

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
public class SwitchTo extends CopyDialog implements PropertyChangeListener {

    private RepositoryPaths repositoryPaths;
    private final File root;
    private final RepositoryFile repositoryFile;
        
    public SwitchTo(RepositoryFile repositoryFile, File root, boolean localChanges) {
        super(new SwitchToPanel(), NbBundle.getMessage(SwitchTo.class, "CTL_SwitchTo_Title", root.getName()), NbBundle.getMessage(SwitchTo.class, "CTL_SwitchTo_Action")); // NOI18N
        
        this.root = root;        
        this.repositoryFile = repositoryFile;       
        
        SwitchToPanel panel = getSwitchToPanel();
        panel.warningLabel.setVisible(localChanges);

        setupUrlComboBox(repositoryFile, panel.urlComboBox, false);
        
        repositoryPaths = 
            new RepositoryPaths(
                repositoryFile, 
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
    
    private SwitchToPanel getSwitchToPanel() {
        return (SwitchToPanel) getPanel();
    }    
        
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if( evt.getPropertyName().equals(RepositoryPaths.PROP_VALID) ) {            
            boolean valid = ((Boolean)evt.getNewValue()).booleanValue();
            getOKButton().setEnabled(valid);
        }        
    }    
}
