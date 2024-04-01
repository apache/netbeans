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

package org.netbeans.modules.subversion.ui.wizards.repositorystep;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.WizardStepProgressSupport;
import org.netbeans.modules.subversion.ui.repository.Repository;
import org.netbeans.modules.subversion.ui.repository.RepositoryConnection;
import org.netbeans.modules.subversion.ui.wizards.AbstractStep;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;
        
/**
 *
 *
 *
 * @author Tomas Stupka
 */
public class RepositoryStep extends AbstractStep implements WizardDescriptor.AsynchronousValidatingPanel, PropertyChangeListener {

    public static final String IMPORT_HELP_ID = "org.netbeans.modules.subversion.ui.wizards.repositorystep.RepositoryStep.import";
    public static final String CHECKOUT_HELP_ID = "org.netbeans.modules.subversion.ui.wizards.repositorystep.RepositoryStep.checkout";
    public static final String URL_PATTERN_HELP_ID = "org.netbeans.modules.subversion.ui.wizards.repositorystep.RepositoryStep.urlPattern";
    
    private Repository repository;        
    private RepositoryStepPanel panel;    
    private RepositoryFile repositoryFile;    
    private int repositoryModeMask;
    private WizardStepProgressSupport support;

    private final String helpID;
    
    public RepositoryStep(String helpID) {
        this.repositoryModeMask = 0;
        this.helpID = helpID;
    }
    
    public RepositoryStep(int repositoryModeMask, String helpID) {
        this.repositoryModeMask = repositoryModeMask;
        this.helpID = helpID;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(helpID);
    }        

    @Override
    protected JComponent createComponent() {
        if (repository == null) {         
            repositoryModeMask = repositoryModeMask | Repository.FLAG_URL_EDITABLE | Repository.FLAG_URL_ENABLED | Repository.FLAG_SHOW_HINTS | Repository.FLAG_SHOW_PROXY;
            String title = org.openide.util.NbBundle.getMessage(RepositoryStep.class, "CTL_Repository_Location");       // NOI18N
            repository = new Repository(repositoryModeMask, title); 
            repository.addPropertyChangeListener(this);
            panel = new RepositoryStepPanel();            
            panel.repositoryPanel.add(repository.getPanel());
            Dimension size = panel.getPreferredSize();
            panel.setPreferredSize(new Dimension(size.width, size.height + new JLabel("A").getPreferredSize().height + new JButton("A").getPreferredSize().height + 20)); //NOI18N
            valid();
        }                        
        return panel;
    }

    @Override
    protected void validateBeforeNext() {            
        try {
            support = new RepositoryStepProgressSupport(panel.progressPanel);        
            SVNUrl url = getUrl();
            if (url != null) {
                support.setRepositoryRoot(url);
                RequestProcessor rp = Subversion.getInstance().getRequestProcessor(url);
                RequestProcessor.Task task = support.start(rp, url, NbBundle.getMessage(RepositoryStep.class, "BK2012"));
                task.waitFinished();
            }
        } finally {
            support = null;
        }
    }
    
    @Override
    public void prepareValidation() {                
    }

    private SVNUrl getUrl() {        
        try {
            return getSelectedRepositoryConnection().getSvnUrl();                
        } catch (MalformedURLException mue) {
            // probably a synchronization issue
            invalid(new WizardMessage(mue.getLocalizedMessage(), false));
        }                                
        return null;
    }
    
    private void storeHistory() {        
        RepositoryConnection rc = getSelectedRepositoryConnection();
        if(rc != null) {  
            SvnModuleConfig.getDefault().insertRecentUrl(rc);           
        }        
    }
    
    public RepositoryFile getRepositoryFile() {
        return repositoryFile;
    }               
    
    private RepositoryConnection getSelectedRepositoryConnection() {      
        try {
            return repository.getSelectedRC();
        } catch (Exception ex) {
            invalid(new AbstractStep.WizardMessage(ex.getLocalizedMessage(), false));
            return null;
        }
    }                       

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(Repository.PROP_VALID)) {
            if(repository.isValid()) {
                valid(new AbstractStep.WizardMessage(repository.getMessage(), false));
            } else {
                invalid(new AbstractStep.WizardMessage(repository.getMessage(), false));
            }
        }
    }

    public void stop() {
        if(support != null) {
            support.cancel();
        }
    }
    
    private class RepositoryStepProgressSupport extends WizardStepProgressSupport {

        public RepositoryStepProgressSupport(JPanel panel) {
            super(panel);
        }

        @Override
        public void perform() {
            final RepositoryConnection rc = getSelectedRepositoryConnection();
            if (rc == null) {
                return;
            }
            storeHistory();
            AbstractStep.WizardMessage invalidMsg = null;
            try {
                invalid(null);

                SvnClient client;
                SVNUrl url = rc.getSvnUrl();
                try {
                    int handledExceptions = (SvnClientExceptionHandler.EX_DEFAULT_HANDLED_EXCEPTIONS) ^ // the default without
                                            (SvnClientExceptionHandler.EX_NO_HOST_CONNECTION |          // host connection errors (misspeled host or proxy urls, ...)
                                             SvnClientExceptionHandler.EX_AUTHENTICATION |              // authentication errors
                                             SvnClientExceptionHandler.EX_SSL_NEGOTIATION_FAILED);      // client cert errors
                    client = Subversion.getInstance().getClient(url, rc.getUsername(), rc.getPassword(), handledExceptions);
                } catch (SVNClientException ex) {
                    SvnClientExceptionHandler.notifyException(ex, true, true);
                    invalidMsg = new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(RepositoryStep.class, "CTL_Repository_Invalid", rc.getUrl()), false);
                    return;
                }
                    
                repositoryFile = null; // reset
                ISVNInfo info = null;    
                try {
                    repository.storeConfigValues();
                    setCancellableDelegate(client);
                    info = client.getInfo(url);
                } catch (SVNClientException ex) {
                    annotate(ex);
                    invalidMsg = new AbstractStep.WizardMessage(SvnClientExceptionHandler.parseExceptionMessage(ex), false);
                } 
                if(isCanceled()) {
                    return;
                }

                if(info != null) {
                    SVNUrl repositoryUrl = SvnUtils.decode(info.getRepository());
                    if(repositoryUrl==null) {
                        // XXX see issue #72810 and #72921. workaround!
                        repositoryUrl = rc.getSvnUrl();
                    }
                    SVNRevision revision = rc.getSvnRevision();
                    String[] repositorySegments = repositoryUrl.getPathSegments();
                    String[] selectedSegments = rc.getSvnUrl().getPathSegments();
                    if (selectedSegments.length < repositorySegments.length && SvnUtils.decodeToString(rc.getSvnUrl()).contains("\\")) { //NOI18N
                        // WA for bug #196830 with svnkit: the entered url contains backslashes. While javahl does not like backslashes and a warning is reported earlier, svnkit internally 
                        // translates them into normal slashes and does not complain. However rc.getUrl still returns the url with backslashes
                        invalidMsg = new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(RepositoryStep.class, "CTL_Repository_Invalid", rc.getUrl()), false); // NOI18N
                        return;
                    }
                    String[] repositoryFolder = new String[selectedSegments.length - repositorySegments.length];
                    System.arraycopy(selectedSegments, repositorySegments.length,
                                     repositoryFolder, 0,
                                     repositoryFolder.length);

                    repositoryFile = new RepositoryFile(repositoryUrl, repositoryFolder, revision);
                } else {
                    invalidMsg = new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(RepositoryStep.class, "CTL_Repository_Invalid", rc.getUrl()), false); // NOI18N
                    return;
                }
            } catch (MalformedURLException ex) {
                // probably a synchronization issue
                invalidMsg = new WizardMessage(ex.getLocalizedMessage(), false);
            } finally {
                if(isCanceled()) {
                    valid(new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(RepositoryStep.class, "CTL_Repository_Canceled"), false)); // NOI18N
                } else if(invalidMsg == null) {
                  valid();
                } else {
                  valid(invalidMsg);
                }                
            }
        }

        @Override
        public void setEditable(boolean editable) {
            repository.setEditable(editable);        
        }        
    };

}

