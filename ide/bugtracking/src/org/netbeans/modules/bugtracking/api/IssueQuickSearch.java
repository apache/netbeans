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
package org.netbeans.modules.bugtracking.api;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.bugtracking.ui.search.QuickSearchComboBar;
import org.netbeans.modules.bugtracking.ui.search.QuickSearchPanel;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;

/**
 * Provides a UI Component to pick issues. 
 * 
 * <p>
 * The component given by {@link #getComponent()} contains:
 * <ul>
 *   <li>a combo box containing all known repositories. 
 *   <li>a button to create a new repository
 *   <li>a field to type some text into - to find Issues either by id or summary.
 * </ul>
 * 
 * @author Tomas Stupka
 * @since 1.85
 */
public final class IssueQuickSearch {
    private final QuickSearchPanel panel;
    
    private IssueQuickSearch(FileObject context, RepositoryFilter filter) {
        panel = new QuickSearchPanel(context, filter);
    }
    
    /**
     * Determines what kind of repositories should be shown in the repositories combo box.
     * @since 1.85
     */
    public enum RepositoryFilter {
        /**
         * Show only repositories which provide the attach file functionality.
         * @since 1.85
         */
        ATTACH_FILE,
        /**
         * Show all Repositories.
         * @since 1.85
         */
        ALL
    }
    
    /**
     * Creates an IssueQuickSearch providing all repositories and none of them preselected.
     * 
     * @return a new IssueQuickSearch instance
     * @since 1.85
     */
    public static IssueQuickSearch create() {
       return new IssueQuickSearch(null, RepositoryFilter.ALL);
    }
    
    /**
     * Creates an IssueQuickSearch providing all repositories, where one might 
     * be preselected determined by the given file - e.g. a file from the same VCS
     * repository was used to pick an Issue in some previous session.
     * 
     * @param context a file to give a hint about a repository to preselect
     * @return a new IssueQuickSearch instance
     * @since 1.85
     */
    public static IssueQuickSearch create(FileObject context) {
       return new IssueQuickSearch(context, RepositoryFilter.ALL);
    }
    
    /**
     * Creates an IssueQuickSearch providing a filtered list of repositories, where one might 
     * be preselected determined by the given file - e.g. a file from the same VCS
     * repository was used to pick an Issue in some previous session.
     * 
     * @param context a file to give a hint about a repository to preselect
     * @param filter what kind of repositories should be provided
     * @return a new IssueQuickSearch instance
     * @since 1.85
     */
    public static IssueQuickSearch create(FileObject context, RepositoryFilter filter) {
       return new IssueQuickSearch(context, filter);
    }
    
    /**
     * Opens a modal dialog to search after Issues from the given repository. 
     * The dialog presents a field to type some text into - to find Issues either by id or summary.
     * 
     * @param message a message to displayed together with the combo box - e.g. Select task that this task depends on.
     * @param repository the repository from which is the Issue to be found
     * @param caller caller component
     * @param helpCtx a help context or null if none
     * @return an Issue instance or null if none was selected.
     * @since 1.85
     */
    public static Issue selectIssue(String message, Repository repository, JPanel caller, HelpCtx helpCtx) {
        return QuickSearchComboBar.selectIssue(message, repository, caller, helpCtx);
    }
    
    /**
     * Sets the repository for which issues should be made available in 
     * the issue combo bar.
     * 
     * @param repository 
     * @since 1.85
     */
    public void setRepository(Repository repository) {
        panel.setRepository(repository);
    }
    
    /**
     * Returns the IssueQuickSearch component.
     * 
     * @return the IssueQuickSearch component
     * @since 1.85
     */
    public JComponent getComponent() {
        return panel;
    }
    
    /**
     * Returns the issue selected in the issue combo bar or null if none selected.
     * 
     * @return an Issue instance or null if none was selected.
     * @since 1.85
     */
    public Issue getIssue() {
        return panel.getIssue();
    }

    /**
     * Register for notifications about changes in the issue combo bar. 
     * Fires each time an Issue is either selected or deselected.
     * 
     * @param listener 
     * @since 1.85
     */
    public void setChangeListener(ChangeListener listener) {
        panel.setChangeListener(listener);
    }

    /**
     * Select the given issue in the combo bar.
     * 
     * @param issue 
     * @since 1.85
     */
    public void setIssue(Issue issue) {
        panel.setIssue(issue.getImpl());
    }

    /**
     * Returns the selected repository.
     * 
     * @return a Repository
     * @since 1.85
     */
    public Repository getSelectedRepository() {
        return panel.getSelectedRepository();
    }

    /**
     * Sets whether or not this component is enabled.
     * 
     * @param enabled 
     * @since 1.85
     */
    public void setEnabled(boolean enabled) {
        panel.setEnabled(enabled);
    }
}
