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

package org.netbeans.modules.bugtracking.vcs;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import javax.swing.JPanel;
import org.netbeans.modules.versioning.hooks.GitHook;
import org.netbeans.modules.versioning.hooks.GitHookContext;
import org.netbeans.modules.versioning.hooks.GitHookContext.LogEntry;
import org.openide.util.NbBundle;

/**
 * Git hook implementation
 * @author Tomas Stupka
 */
public class GitHookImpl extends GitHook {

    private static final String[] SUPPORTED_ISSUE_INFO_VARIABLES = new String[] {"id", "summary"};                        // NOI18N
    private static final String[] SUPPORTED_REVISION_VARIABLES = new String[] {"changeset", "author", "date", "message"}; // NOI18N

    private final String name;

    private HookImpl delegate;
    
    public GitHookImpl() {
        this.name = NbBundle.getMessage(GitHookImpl.class, "LBL_VCSHook");       // NOI18N
        VCSHooksConfig config = VCSHooksConfig.getInstance(VCSHooksConfig.HookType.GIT);
        delegate = new HookImpl(config, SUPPORTED_ISSUE_INFO_VARIABLES, SUPPORTED_REVISION_VARIABLES);
    }

    @Override
    public GitHookContext beforeCommit(GitHookContext context) throws IOException {
        String msg = delegate.beforeCommit(context.getFiles(), context.getMessage());
        return msg != null ? new GitHookContext(context.getFiles(), msg, context.getLogEntries()) : null;
    }

    @Override
    public void afterCommit(GitHookContext context) {
        String author = context.getLogEntries()[0].getAuthor();
        String changeset = context.getLogEntries()[0].getChangeset();
        Date date = context.getLogEntries()[0].getDate();
        String message = context.getMessage();
        delegate.afterCommit(context.getFiles(), author, changeset, date, message, "GIT", true);        
    }

    @Override
    public GitHookContext beforePush(GitHookContext context) throws IOException {
        return super.beforePush(context);
    }

    @Override
    public void afterPush(GitHookContext context) {
        LogEntry[] logEntries = context.getLogEntries();
        String[] changesets = new String[logEntries.length];
        for (int i = 0; i < logEntries.length; i++) {
            LogEntry logEntry = logEntries[i];
            changesets[i] = logEntry.getChangeset();
        }        
        delegate.afterPush(context.getFiles(), changesets, "GIT");
    }

    @Override
    public JPanel createComponent(GitHookContext context) {
        return delegate.createComponent(context.getFiles());
    }

    @Override
    public String getDisplayName() {
        return name;
    }
    
    @Override
    public void afterCommitReplace (GitHookContext originalContext, GitHookContext newContext, Map<String, String> mapping) {
        delegate.afterChangesetReplace(newContext.getFiles(), mapping, "GIT");
    }

}
