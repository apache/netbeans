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
