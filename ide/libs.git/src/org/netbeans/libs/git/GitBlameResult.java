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
package org.netbeans.libs.git;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * Result of a blame command, wraps line annotations for a given file.
 * 
 * @author Ondra Vrabec
 */
public final class GitBlameResult {

    private final File blamedFile;
    private final int lineCount;
    private GitLineDetails[] lineDetails;

    GitBlameResult (BlameResult result, Repository repository) {
        this.lineCount = result.getResultContents().size();
        this.lineDetails = new GitLineDetails[lineCount];

        Map<String, File> cachedFiles = new HashMap<String, File>(lineCount);
        this.blamedFile = getFile(cachedFiles, result.getResultPath(), repository.getWorkTree());

        Map<RevCommit, GitRevisionInfo> cachedRevisions = new HashMap<RevCommit, GitRevisionInfo>(lineCount);
        Map<PersonIdent, GitUser> cachedUsers = new HashMap<PersonIdent, GitUser>(lineCount * 2);
        for (int i = 0; i < lineCount; ++i) {
            RevCommit commit = result.getSourceCommit(i);
            if (commit == null) {
                lineDetails[i] = null;
            } else {
                GitRevisionInfo revInfo = cachedRevisions.get(commit);
                if (revInfo == null) {
                    revInfo = new GitRevisionInfo(commit, repository);
                    cachedRevisions.put(commit, revInfo);
                }
                GitUser author = getUser(cachedUsers, result.getSourceAuthor(i));
                GitUser committer = getUser(cachedUsers, result.getSourceCommitter(i));
                File sourceFile = getFile(cachedFiles, result.getSourcePath(i), repository.getWorkTree());
                String content = result.getResultContents().getString(i);
                lineDetails[i] = new GitLineDetails(content, revInfo, author, committer, sourceFile, result.getSourceLine(i));
            }
        }
    }
    
    /**
     * @return annotated file
     */
    public File getBlamedFile () {
        return blamedFile;
    }

    /**
     * @return number of collected line annotations
     */
    public int getLineCount () {
        return lineCount;
    }

    /**
     * Returns a line annotation for a line specified by the given line number
     * @param lineNumber line number
     * @return line annotation or <code>null</code> if no line annotation is available for the given line number
     */
    public GitLineDetails getLineDetails (int lineNumber) {
        return lineNumber < lineCount ? lineDetails[lineNumber] : null;
    }

    private GitUser getUser (Map<PersonIdent, GitUser> cached, PersonIdent ident) {
        GitUser user = cached.get(ident);
        if (user == null) {
            user = GitClassFactoryImpl.getInstance().createUser(ident);
            cached.put(ident, user);
        }
        return user;
    }

    private File getFile (Map<String, File> cached, String relativePath, File workTree) {
        File file = cached.get(relativePath);
        if (file == null) {
            file = new File(workTree, relativePath.replace("/", File.separator)); //NOI18N
            cached.put(relativePath, file);
        }
        return file;
    }
}
