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

package org.netbeans.modules.bugzilla.util;

import java.awt.Color;
import java.util.Collections;
import java.util.MissingResourceException;
import org.netbeans.modules.bugtracking.commons.ListValuePicker;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.eclipse.core.runtime.CoreException;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.api.RepositoryManager;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;
import org.netbeans.modules.bugtracking.commons.NBBugzillaUtils;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.BugzillaConnector;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;
import org.netbeans.modules.bugzilla.repository.BugzillaConfiguration;
import org.netbeans.modules.mylyn.util.commands.GetRepositoryTasksCommand;
import org.netbeans.modules.mylyn.util.MylynSupport;
import org.netbeans.modules.mylyn.util.NbTask;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka, Jan Stola
 */
public class BugzillaUtil {
    
    private static final Color ERROR_COLOR;
    static {
        Color c = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (c == null) {
            c = new Color(153, 0, 0);
        }
        ERROR_COLOR = c;
    }

    public static boolean show(JPanel panel, String title, String okName) {
        return show(panel, title, okName, new HelpCtx(panel.getClass()));
    }

    public static boolean show(JPanel panel, String title, String okName, HelpCtx helpCtx) {
        JButton ok = new JButton(okName);
        ok.getAccessibleContext().setAccessibleDescription(ok.getText());
        JButton cancel = new JButton(NbBundle.getMessage(BugzillaUtil.class, "LBL_Cancel")); // NOI18N
        cancel.getAccessibleContext().setAccessibleDescription(cancel.getText());
        DialogDescriptor descriptor = new DialogDescriptor (
                panel,
                title,
                true,
                new Object[] {ok, cancel},
                ok,
                DialogDescriptor.DEFAULT_ALIGN,
                helpCtx,
                null);
        return DialogDisplayer.getDefault().notify(descriptor) == ok;
    }

    /**
     * Returns Task for the given issue id or null if an error occurred
     * @param repository
     * @param id
     * @return
     */
    public static NbTask getTask (final BugzillaRepository repository, final String id, boolean handleExceptions) {
        MylynSupport supp = MylynSupport.getInstance();
        try {
            GetRepositoryTasksCommand cmd = supp.getCommandFactory()
                    .createGetRepositoryTasksCommand(repository.getTaskRepository(), Collections.<String>singleton(id));
            repository.getExecutor().execute(cmd, handleExceptions);
            if(cmd.hasFailed() && Bugzilla.LOG.isLoggable(Level.FINE)) {
                Bugzilla.LOG.log(Level.FINE, cmd.getErrorMessage());
            }
            if (cmd.getTasks().isEmpty()) {
                // fallback on local
                NbTask task = supp.getTask(repository.getTaskRepository().getRepositoryUrl(), id);
                if (cmd.hasFailed() && task != null) {
                    return task;
                }
            } else {
                return cmd.getTasks().iterator().next();
            }
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.INFO, null, ex);
        }
        return null;
    }

    public static String getKeywords(String message, String keywordsString, BugzillaRepository repository) {

        try {
            BugzillaConfiguration bc = repository.getConfiguration();
            if(bc == null || !bc.isValid()) {
                // XXX is there something else we could do at this point?
                return keywordsString;
            }
            return ListValuePicker.getValues(
                    NbBundle.getMessage(BugzillaUtil.class, "CTL_KeywordsTitle"), 
                    NbBundle.getMessage(BugzillaUtil.class, "LBL_Keywords"),
                    message, 
                    keywordsString, 
                    bc.getKeywords());
        } catch (MissingResourceException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
            return keywordsString;
        }       
    }

    public static boolean isAssertEnabled() {
        boolean retval = false;
        assert retval = true;
        return retval;
    }

    /**
     * Determines wheter the given {@link RepositoryProvider} is the
     * repository hosting netbeans or not
     *
     * @param repo
     * @return true if the given repository is the netbenas bugzilla, otherwise false
     */
    public static boolean isNbRepository(BugzillaRepository repo) {
        return isNbRepository(repo.getUrl());
    }
    
    public static boolean isNbRepository(String url) {
        return NBBugzillaUtils.isNbRepository(url);        
    }

    public static boolean showQAContact(BugzillaRepository repo) {
        return true;
    }

    public static boolean showStatusWhiteboard(BugzillaRepository repo) {
        return true;
    }

    public static boolean showIssueType(BugzillaRepository repo) {
        return NBBugzillaUtils.isNbRepository(repo.getUrl());
    }

    public static Repository getRepository(BugzillaRepository bugzillaRepository) {
        Repository repository = RepositoryManager.getInstance().getRepository(BugzillaConnector.ID, bugzillaRepository.getID());
        if(repository == null) {
            repository = createRepository(bugzillaRepository);
        }
        return repository;
    }
    
    public static Repository createRepository(BugzillaRepository bugzillaRepository) {
        return Bugzilla.getInstance().getBugtrackingFactory().createRepository(
                bugzillaRepository, 
                Bugzilla.getInstance().getStatusProvider(),
                Bugzilla.getInstance().getSchedulingProvider(),
                Bugzilla.getInstance().createPriorityProvider(bugzillaRepository),
                Bugzilla.getInstance().getBugzillaIssueFinder());
    }

    public static void openIssue(BugzillaIssue bugzillaIssue) {
        Bugzilla.getInstance().getBugtrackingFactory().openIssue(bugzillaIssue.getRepository(), bugzillaIssue);
    }
    
    public static void openQuery(BugzillaQuery bugzillaQuery) {
        Bugzilla.getInstance().getBugtrackingFactory().editQuery(bugzillaQuery.getRepository(), bugzillaQuery);
    }

    public static Color getErrorForegroundColor () {
        return ERROR_COLOR;
    }
}
