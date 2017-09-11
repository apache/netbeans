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
package org.netbeans.modules.subversion.ui.search;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Handles the UI for revision search.
 *
 * @author Tomas Stupka
 */
public class SvnSearch implements ActionListener, DocumentListener {
    
    public final static String SEACRH_HELP_ID_CHECKOUT = "org.netbeans.modules.subversion.ui.search.checkout"; 
    public final static String SEACRH_HELP_ID_SWITCH = "org.netbeans.modules.subversion.ui.search.switch"; 
    public final static String SEACRH_HELP_ID_COPY = "org.netbeans.modules.subversion.ui.search.copy"; 
    public final static String SEACRH_HELP_ID_URL_PATTERN = "org.netbeans.modules.subversion.ui.search.urlpattern"; 
    public final static String SEACRH_HELP_ID_MERGE = "org.netbeans.modules.subversion.ui.search.merge"; 
    public final static String SEACRH_HELP_ID_REVERT = "org.netbeans.modules.subversion.ui.search.revert";     
    public final static String SEARCH_HELP_ID_UPDATE = "org.netbeans.modules.subversion.ui.search.update"; 
    public final static String SEARCH_HELP_ID_SELECT_DIFF_TREE = "org.netbeans.modules.subversion.ui.search.selectdifftree"; //NOI18N
    
    private static final String DATE_FROM = "svnSearch.dateFrom";    // NOI18N
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd"); // NOI18N
    private final SvnSearchPanel panel;    
    
    private final RepositoryFile[] repositoryFiles;
    private final SvnSearchView searchView;
    private SvnProgressSupport support;
    private final NoContentPanel noContentPanel;
    
    public SvnSearch(RepositoryFile... repositoryFile) {
        this.repositoryFiles = repositoryFile;                
        panel = new SvnSearchPanel();
        panel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SvnSearch.class, "ACSN_SummaryView_Name"));  // NOI18N
        panel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SvnSearch.class, "ACSD_SummaryView_Desc"));   // NOI18N
        
        panel.listButton.addActionListener(this);
        panel.dateFromTextField.getDocument().addDocumentListener(this); 
        
        String date = DATE_FORMAT.format(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 7));
        panel.dateFromTextField.setText(SvnModuleConfig.getDefault().getPreferences().get(DATE_FROM, date));
        
        searchView = new SvnSearchView();
        
        panel.listPanel.setLayout(new BorderLayout());  
        panel.listPanel.add(searchView.getComponent());

        noContentPanel = new NoContentPanel();
        panel.noContentPanel.setLayout(new BorderLayout());  
        panel.noContentPanel.add(noContentPanel);
        noContentPanel.setLabel(org.openide.util.NbBundle.getMessage(SvnSearch.class, "LBL_NoResults_SearchNotPerformed")); // NOI18N

        panel.listPanel.setVisible(false);
        panel.noContentPanel.setVisible(true);
    }       

    /**
     * Cancels all running tasks
     */
    public void cancel() {
        if(support != null) {
            support.cancel();
        }
    }
    
    @NbBundle.Messages({
        "# {0} - resource URL",
        "MSG_SvnSearch.error.pathNotFound=Resource does not exist: {0}"
    })
    private void listLogEntries() {        
                
        noContentPanel.setLabel(org.openide.util.NbBundle.getMessage(SvnSearch.class, "LBL_NoResults_SearchInProgress")); // NOI18N
        panel.listPanel.setVisible(false);
        panel.noContentPanel.setVisible(true);       
        
        final SVNRevision revisionFrom = getRevisionFrom();
        final SVNUrl repositoryUrl = this.repositoryFiles[0].getRepositoryUrl();
        if(revisionFrom instanceof SVNRevision.DateSpec) {
            SvnModuleConfig.getDefault().getPreferences().put(DATE_FROM, panel.dateFromTextField.getText().trim());
        }
                
        final String[] paths = new String[repositoryFiles.length];
        for(int i = 0; i < repositoryFiles.length; i++) {
            String[] segments = repositoryFiles[i].getPathSegments();
            StringBuilder sb = new StringBuilder();
            for(String segment : segments) {
                sb.append(segment);
                sb.append('/');
            }
            paths[i] = sb.toString();
        }

        RequestProcessor rp = Subversion.getInstance().getRequestProcessor();
        support = new SvnProgressSupport() {
            @Override
            protected void perform() {                                                                                                                            
                ISVNLogMessage[] messageArray= null;
                try {                        
                    SvnClient client = Subversion.getInstance().getClient(repositoryUrl, this);                         
                    messageArray = SvnUtils.getLogMessages(client, repositoryUrl, paths, null, SVNRevision.HEAD, revisionFrom, false, false, 0);
                } catch (SVNClientException ex) {
                    if (SvnClientExceptionHandler.isFileNotFoundInRevision(ex.getMessage())) {
                        for (int i=0; i < paths.length; ++i) {
                            String path = paths[i];
                            while (path.endsWith("/")) {
                                path = path.substring(0, path.length() - 1);
                            }
                            if (ex.getMessage().contains(path)) {
                                noContentPanel.setLabel(Bundle.MSG_SvnSearch_error_pathNotFound(paths[i]));
                                SvnClientExceptionHandler.notifyException(ex, false, false);
                                return;
                            }
                        }
                    }
                    SvnClientExceptionHandler.notifyException(ex, true, true);
                }

                if(isCanceled()) {
                    return;
                }    

                if(messageArray == null) {                
                    return;
                }

                final List<ISVNLogMessage> messages = new ArrayList<ISVNLogMessage>();
                if(revisionFrom instanceof SVNRevision.DateSpec) {
                    long timeFrom = ((SVNRevision.DateSpec) revisionFrom).getDate().getTime();                        
                    for(ISVNLogMessage lm : messageArray) {                        
                        if(lm.getDate().getTime() >= timeFrom) {
                            messages.add(lm);
                        }                                                                        
                    }   
                } else {
                    long revision = ((SVNRevision.Number) revisionFrom).getNumber();
                    for(ISVNLogMessage lm : messageArray) {                                                    
                        if(lm.getRevision().getNumber() >= revision) {
                            messages.add(lm);
                        }                                                                                                                                
                    }
                }

                if(isCanceled()) {
                    return;
                }

                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        panel.listPanel.setVisible(true);
                        panel.noContentPanel.setVisible(false);                     
                        searchView.setResults(messages.toArray(new ISVNLogMessage[messages.size()]));      
                    }
                });
            }                        
        };
        support.start(rp, repositoryUrl, org.openide.util.NbBundle.getMessage(SvnSearch.class, "LBL_Search_Progress")).addTaskListener(             // NOI18N
            new TaskListener(){
                @Override
                public void taskFinished(Task task) {
                    support = null;
                }            
            }
        ); 
    }
    
    public JPanel getSearchPanel() {
        return panel;
    }
    
    public SVNRevision getSelectedRevision() {
        return searchView.getSelectedValue();
    }

    public void addListSelectionListener(ListSelectionListener listener) {
        searchView.addListSelectionListener(listener);
    }
    
    public void removeListSelectionListener(ListSelectionListener listener) {
        searchView.removeListSelectionListener(listener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==panel.listButton) {
            listLogEntries();            
        }        
    }
    
    private SVNRevision getRevisionFrom() {
        String value = panel.dateFromTextField.getText().trim();
        if(value.equals("")) {
            return new SVNRevision.Number(1);
        }
        try {
            return new SVNRevision.DateSpec(DATE_FORMAT.parse(value));
        } catch (ParseException ex) {
            return null; // should not happen
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
        boolean isValid = false;        
        String dateString = panel.dateFromTextField.getText();
        if(dateString.equals("")) { // NOI18N
            isValid = true;
        } else {       
            try {
                DATE_FORMAT.parse(panel.dateFromTextField.getText());
                isValid = true;
            } catch (ParseException ex) {
                // ignore
            }
        }
        panel.listButton.setEnabled(isValid);
    }
    
}
