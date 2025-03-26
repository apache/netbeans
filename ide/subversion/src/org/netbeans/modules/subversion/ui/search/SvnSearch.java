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
    
    public static final String SEACRH_HELP_ID_CHECKOUT = "org.netbeans.modules.subversion.ui.search.checkout"; 
    public static final String SEACRH_HELP_ID_SWITCH = "org.netbeans.modules.subversion.ui.search.switch"; 
    public static final String SEACRH_HELP_ID_COPY = "org.netbeans.modules.subversion.ui.search.copy"; 
    public static final String SEACRH_HELP_ID_URL_PATTERN = "org.netbeans.modules.subversion.ui.search.urlpattern"; 
    public static final String SEACRH_HELP_ID_MERGE = "org.netbeans.modules.subversion.ui.search.merge"; 
    public static final String SEACRH_HELP_ID_REVERT = "org.netbeans.modules.subversion.ui.search.revert";     
    public static final String SEARCH_HELP_ID_UPDATE = "org.netbeans.modules.subversion.ui.search.update"; 
    public static final String SEARCH_HELP_ID_SELECT_DIFF_TREE = "org.netbeans.modules.subversion.ui.search.selectdifftree"; //NOI18N
    
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
                        searchView.setResults(messages.toArray(new ISVNLogMessage[0]));      
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
