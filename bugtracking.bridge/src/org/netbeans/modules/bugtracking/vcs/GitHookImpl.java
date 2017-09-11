/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
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
