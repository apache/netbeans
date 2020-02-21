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
package org.netbeans.modules.git.remote.cli;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo.GitRevCommit;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 * Result of a blame command, wraps line annotations for a given file.
 * 
 */
public final class GitBlameResult {

    private final VCSFileProxy blamedFile;
    private final int lineCount;
    private final GitLineDetails[] lineDetails;

    GitBlameResult(VCSFileProxy file, Map<String, GitBlameContent> result, JGitRepository repository) {
        this.blamedFile = file;
        TreeMap<Integer, GitLineDetails> lines = new TreeMap<>();
        for (Map.Entry<String, GitBlameContent> entry : result.entrySet()) {
            GitBlameContent v = entry.getValue();
            GitRevCommit rev = new GitRevCommit();
            rev.autorAndMail = v.author+" "+v.author_mail;
            rev.autorTime = v.author_time + " " + v.author_tz;
            rev.commiterAndMail = v.committer + " " + v.committer_mail;
            rev.commiterTime = v.committer_time + " " + v.committer_tz;
            rev.message = v.summary;
            rev.revisionCode = v.revision;
            GitRevisionInfo revInfo = new GitRevisionInfo(rev, repository);
            VCSFileProxy sourceFile = VCSFileProxy.createFileProxy(repository.getLocation(), v.filename);
            GitUser author = revInfo.getAuthor();
            GitUser committer = revInfo.getCommitter();
            for(Map.Entry<Integer,LineInfo> e : v.lines.entrySet()) {
                lines.put(e.getKey(), new GitLineDetails(e.getValue().lineContent, revInfo, author, committer, sourceFile, e.getValue().line - 1));
            }
        }
        lineCount = lines.size();
        this.lineDetails = new GitLineDetails[lineCount];
        for (Map.Entry<Integer, GitLineDetails> entry : lines.entrySet()) {
            if ("0000000000000000000000000000000000000000".equals(entry.getValue().getRevisionInfo().getRevision())) {
                // local change
            } else {
                lineDetails[entry.getKey()-1] = entry.getValue();
            }
        }
    }
    

    /**
     * @return annotated file
     */
    public VCSFileProxy getBlamedFile () {
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

    public static final class LineInfo {
        public int line;
        public String lineContent;
    }
    public static final class GitBlameContent {
        public String revision;
        public String author;
        public String author_mail;
        public String author_time;
        public String author_tz;
        public String committer;
        public String committer_mail;
        public String committer_time;
        public String committer_tz;
        public String summary;
        public String previous;
        public String filename;
        public HashMap<Integer, LineInfo> lines = new HashMap<Integer, LineInfo>();
    }
}
