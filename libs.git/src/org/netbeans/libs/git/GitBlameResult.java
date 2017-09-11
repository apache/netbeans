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
