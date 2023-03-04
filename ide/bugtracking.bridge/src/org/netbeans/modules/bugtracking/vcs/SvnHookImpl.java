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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JPanel;
import org.netbeans.modules.versioning.hooks.SvnHook;
import org.netbeans.modules.versioning.hooks.SvnHookContext;
import org.netbeans.modules.versioning.hooks.SvnHookContext.LogEntry;
import org.openide.util.NbBundle;

/**
 * Subversion commit hook implementation
 * @author Tomas Stupka
 */
public class SvnHookImpl extends SvnHook {

    private static final String[] SUPPORTED_ISSUE_INFO_VARIABLES = new String[] {"id", "summary"};                        // NOI18N
    private static final String[] SUPPORTED_REVISION_VARIABLES = new String[] {"revision", "author", "date", "message"};  // NOI18N

    private final String name;
    private final HookImpl delegate;

     public SvnHookImpl() {
        this.name = NbBundle.getMessage(SvnHookImpl.class, "LBL_VCSHook");       // NOI18N
        VCSHooksConfig config = VCSHooksConfig.getInstance(VCSHooksConfig.HookType.SVN);
        delegate = new HookImpl(config, SUPPORTED_ISSUE_INFO_VARIABLES, SUPPORTED_REVISION_VARIABLES);
    }

    @Override
    public SvnHookContext beforeCommit(SvnHookContext context) throws IOException {
        String msg = delegate.beforeCommit(context.getFiles(), context.getMessage());
        return msg != null ? new SvnHookContext(context.getFiles(), msg, new ArrayList<LogEntry>()) : null;
    }

    @Override
    public void afterCommit(SvnHookContext context) {
        final List<LogEntry> logEntries = context.getLogEntries();
        if(logEntries == null || logEntries.isEmpty()) {
            return; 
        }    
        LogEntry logEntry = logEntries.get(0);
        String author = logEntry.getAuthor();
        long revision = logEntry.getRevision();
        Date date = logEntry.getDate();
        String message = context.getMessage();
        delegate.afterCommit(context.getFiles(), author, Long.toString(revision), date, message, "SVN", false);        
    }    

    @Override
    public JPanel createComponent(SvnHookContext context) {
        HookPanel panel = delegate.createComponent(context.getFiles(), true);
        panel.commitRadioButton.setVisible(false);
        panel.pushRadioButton.setVisible(false);
        return panel;
    }

    @Override
    public String getDisplayName() {
        return name;
    }
}
