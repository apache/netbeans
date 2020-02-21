/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.remote.cli.jgit.commands;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.IgnoreRule;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.FileListener;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class IgnoreCommand extends IgnoreUnignoreCommand {

    public IgnoreCommand(JGitRepository repository, GitClassFactory gitFactory, VCSFileProxy[] files, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, files, monitor, listener);
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "ignoring"); //NOI18N
        addFiles(0, files);
    }

    @Override
    protected MatchResult addStatement (List<IgnoreRule> ignoreRules, VCSFileProxy gitIgnore, String path, boolean isDirectory, boolean rootIgnore, boolean writableIgnoreFile) throws IOException {
        MatchResult result = MatchResult.CHECK_PARENT;
        boolean changed = false;
        String escapedPath = escapeChars(path);
        for (ListIterator<IgnoreRule> it = ignoreRules.listIterator(ignoreRules.size()); it.hasPrevious(); ) {
            IgnoreRule rule = it.previous();
            if (rule.isMatch(path, isDirectory)) {
                if (rule.getResult()) {
                    if (result == MatchResult.NOT_IGNORED && escapedPath.equals(rule.getPattern(true))) {
                        // this statement is redundant, since it is negated in one of following statements
                        if (writableIgnoreFile) {
                            it.remove();
                            changed = true;
                        }
                    } else if (result != MatchResult.NOT_IGNORED) {
                        result = MatchResult.IGNORED;
                    }
                } else {
                    if (escapedPath.equals(rule.getPattern(true))) {
                        if (writableIgnoreFile) {
                            it.remove();
                            changed = true;
                        }
                    } else if (result != MatchResult.IGNORED) {
                        result = MatchResult.NOT_IGNORED;
                    }
                }
            }
        }
        if (writableIgnoreFile && !result.equals(MatchResult.IGNORED) && (result.equals(MatchResult.NOT_IGNORED) || rootIgnore)) {
            if (escapedPath.startsWith("!")) { //NOI18N
                escapedPath = "\\" + escapedPath; //NOI18N
            }
            ignoreRules.add(new IgnoreRule(escapedPath));
            changed = true;
            result = MatchResult.IGNORED;
        }
        if (changed) {
            save(gitIgnore, ignoreRules);
        }
        return result;
    }

    @Override
    protected boolean handleAdditionalIgnores (String path, boolean directory) throws IOException {
        return checkExcludeFile(path, directory) != MatchResult.IGNORED && checkGlobalExcludeFile(path, directory) != MatchResult.IGNORED;
    }    
}
