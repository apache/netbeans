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
public class UnignoreCommand extends IgnoreUnignoreCommand {
    
    public UnignoreCommand(JGitRepository repository, GitClassFactory gitFactory, VCSFileProxy[] files, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, files, monitor, listener);
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "unignoring"); //NOI18N
        addFiles(0, files);
    }

    @Override
    protected MatchResult addStatement (List<IgnoreRule> ignoreRules, VCSFileProxy gitIgnore, String path, boolean isDirectory, boolean forceWrite, boolean writableIgnoreFile) throws IOException {
        MatchResult result = MatchResult.CHECK_PARENT;
        boolean changed = false;
        String escapedPath = escapeChars(path);
        for (ListIterator<IgnoreRule> it = ignoreRules.listIterator(ignoreRules.size()); it.hasPrevious(); ) {
            IgnoreRule rule = it.previous();
            if (rule.isMatch(path, isDirectory)) {
                if (rule.getResult()) {
                    if (escapedPath.equals(rule.getPattern(true))) {
                        if (writableIgnoreFile) {
                            it.remove();
                            changed = true;
                        }
                    } else if (result != MatchResult.NOT_IGNORED) {
                        result = MatchResult.IGNORED;
                    }
                } else {
                    if (result == MatchResult.IGNORED && escapedPath.equals(rule.getPattern(true))) {
                        // this statement is redundant, since it is negated in one of following statements
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
        if (writableIgnoreFile && result.equals(MatchResult.IGNORED) || result == MatchResult.CHECK_PARENT && forceWrite) {
            escapedPath = "!" + escapedPath;
            ignoreRules.add(new IgnoreRule(escapedPath));
            changed = true;
            result = MatchResult.NOT_IGNORED;
        }
        if (changed) {
            save(gitIgnore, ignoreRules);
        }
        return result;
    }

    @Override
    protected boolean handleAdditionalIgnores (String path, boolean directory) throws IOException {
        return checkExcludeFile(path, directory) != MatchResult.NOT_IGNORED && checkGlobalExcludeFile(path, directory) == MatchResult.IGNORED;
    }
}
