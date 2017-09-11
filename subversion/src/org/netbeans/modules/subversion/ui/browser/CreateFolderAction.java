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
package org.netbeans.modules.subversion.ui.browser;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.sql.Date;
import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.JButton;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.ui.browser.RepositoryPathNode.RepositoryPathEntry;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 * Creates a new folder in the browser
 *
 * @author Tomas Stupka
 */
public class CreateFolderAction extends BrowserAction implements PropertyChangeListener {
    private final String defaultFolderName;    
    
    public CreateFolderAction(String defaultFolderName) {        
        this.defaultFolderName = defaultFolderName;
        putValue(Action.NAME, org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "CTL_Action_MakeDir")); // NOI18N
        setEnabled(false);
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {            
            setEnabled(isEnabled());   
        }
    }
    
    @Override
    public boolean isEnabled() {
        Browser browser = getBrowser();
        if(browser == null) {
            return false;
        }        
        if(browser.getExplorerManager().getRootContext() == Node.EMPTY) {
            return false;
        }
        Node[] nodes = getBrowser().getSelectedNodes();
        if(nodes.length != 1) {
            return false;
        }
        return nodes[0] instanceof RepositoryPathNode && 
               ((RepositoryPathNode) nodes[0]).getEntry().getSvnNodeKind() == SVNNodeKind.DIR;
    }

    /**
     * Configures this action with the actual browser instance
     */
    @Override
    public void setBrowser(Browser browser) {        
        Browser oldBrowser = getBrowser();
        if(oldBrowser!=null) {
            oldBrowser.removePropertyChangeListener(this);
        }
        browser.addPropertyChangeListener(this);
        super.setBrowser(browser);                
    }    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Subversion.getInstance().getParallelRequestProcessor().post(new Runnable() {
            @Override
            public void run() {                           
                Node[] nodes = getSelectedNodes();
                if(nodes.length > 1) {                        
                    return; 
                }      

                RepositoryPathNode repositoryPathNode = (RepositoryPathNode) nodes[0];                                     
                Children children = repositoryPathNode.getChildren();
                Node[] childNodes = children.getNodes();
                if(childNodes.length > 0) {
                  try {
                        // force listing of all child nodes ...
                        getExplorerManager().setSelectedNodes(new Node[] {childNodes[0]}); 
                    } catch (PropertyVetoException ex) {
                        Subversion.LOG.log(Level.INFO, null, ex); // should not happen
                    }                         
                }

                DialogDescriptor.InputLine input = 
                    new DialogDescriptor.InputLine(java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/browser/Bundle").getString("CTL_Browser_NewFolder_Prompt"), java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/browser/Bundle").getString("CTL_Browser_NewFolder_Title"));
                input.setInputText(defaultFolderName);
                DialogDisplayer.getDefault().notify(input);                    
                String newDir = input.getInputText().trim();                    
                if(input.getValue() == DialogDescriptor.CANCEL_OPTION || 
                   input.getValue() == DialogDescriptor.CLOSED_OPTION || 
                   newDir.equals(""))  // NOI18N
                {
                    return;
                }                    

                RepositoryFile parentFile = repositoryPathNode.getEntry().getRepositoryFile();                    
                Node segmentNode = repositoryPathNode;
                String[] segments = newDir.split("/"); // NOI18N
                boolean allNodesExists = true;
                for (int i = 0; i < segments.length; i++) {                                                
                    
                    RepositoryFile newFile = parentFile.appendPath(segments[i]);
                        
                    Node nextChildNode = segmentNode.getChildren().findChild(segments[i]);
                    if(nextChildNode != null) {
                        segmentNode = nextChildNode;
                    } else {
                        allNodesExists = false;
                        RepositoryPathEntry entry = new RepositoryPathEntry(newFile, SVNNodeKind.DIR, new SVNRevision(0), new Date(System.currentTimeMillis()), ""); // XXX get author
                        Node node = RepositoryPathNode.createRepositoryPathNode(getBrowser(), entry);
                        segmentNode.getChildren().add(new Node[] {node});
                        segmentNode = node;
                    }
                    parentFile = newFile;

                    if( i == segments.length - 1 ) {
                        
                        if(allNodesExists) {
                            JButton ok = new JButton(java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/browser/Bundle").getString("CTL_Browser_OK"));
                            NotifyDescriptor descriptor = new NotifyDescriptor(
                                    org.openide.util.NbBundle.getMessage(CreateFolderAction.class, "MSG_Browser_FolderExists", newDir), // NOI18N
                                    org.openide.util.NbBundle.getMessage(CreateFolderAction.class, "MSG_Browser_WrongFolerName"), // NOI18N
                                    NotifyDescriptor.DEFAULT_OPTION,
                                    NotifyDescriptor.ERROR_MESSAGE,
                                    new Object [] { ok },
                                    ok);
                            DialogDisplayer.getDefault().notify(descriptor);        
                        }
                        
                        // we are done, select the node ...
                        try {
                            setSelectedNodes(new Node[] {segmentNode});
                        } catch (PropertyVetoException ex) {
                            Subversion.LOG.log(Level.INFO, null, ex); // should not happen
                        }
                    }                            
                }                                                            
            }
        });
    }
}    
