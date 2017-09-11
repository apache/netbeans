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
