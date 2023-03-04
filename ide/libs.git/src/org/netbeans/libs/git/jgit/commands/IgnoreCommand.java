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

package org.netbeans.libs.git.jgit.commands;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import org.eclipse.jgit.ignore.IgnoreNode.MatchResult;
import org.eclipse.jgit.lib.Repository;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.IgnoreRule;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class IgnoreCommand extends IgnoreUnignoreCommand {

    public IgnoreCommand(Repository repository, GitClassFactory gitFactory, File[] files, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, files, monitor, listener);
    }

    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder("ignoring "); //NOI18N
        for (File file : files) {
            sb.append(file).append(' ');
        }
        return sb.toString();
    }

    @Override
    protected MatchResult addStatement (List<IgnoreRule> ignoreRules, File gitIgnore, String path, boolean isDirectory, boolean rootIgnore, boolean writableIgnoreFile) throws IOException {
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
