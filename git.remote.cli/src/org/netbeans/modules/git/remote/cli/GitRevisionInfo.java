/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.remote.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 * Provides information about a certain commit, usually is returned by 
 * git commit or log command.
 * 
 */
public final class GitRevisionInfo {
    private static final Logger LOG = Logger.getLogger(GitRevisionInfo.class.getName());
    private JGitRepository repository;
    private final Map<String, GitBranch> branches;
    private GitFileInfo[] modifiedFiles;
    private String shortMessage;
    //CLI:
    private String branch;
    private String revisionCode;
    private String message;
    private String autorAndMail;
    private String commiterAndMail;
    private String autorTime;
    private String commiterTime;
    private String[] parents;
    private String treeCode;

    GitRevisionInfo(GitRevCommit status, JGitRepository repository) {
        this(status, Collections.<String, GitBranch>emptyMap(), repository);
    }

    GitRevisionInfo(GitRevCommit status, Map<String, GitBranch> affectedBranches, JGitRepository repository) {
        this.branch = status.branch;
        this.branches = affectedBranches;
        this.revisionCode = status.revisionCode;
        this.treeCode = status.treeCode;
        this.message = status.message;
        this.autorAndMail = status.autorAndMail;
        autorTime = status.autorTime;
        modifiedFiles = new GitFileInfo[status.commitedFiles.size()];
        int i = 0;
        for (Map.Entry<String, GitRevisionInfo.GitFileInfo.Status> entry : status.commitedFiles.entrySet()) {
            VCSFileProxy file = VCSFileProxy.createFileProxy(repository.getLocation(), entry.getKey());
            GitFileInfo info = new GitFileInfo(file, entry.getKey(), entry.getValue(), null, null);
            modifiedFiles[i++] = info;
        }
        parents = status.parents.toArray(new String[status.parents.size()]);
        commiterAndMail = status.commiterAndMail;
        commiterTime = status.commiterTime;
        this.repository = repository;
    }

    /**
     * @return id of the commit
     */
    public String getRevision () {
        return revisionCode;
    }

    /**
     * @return id of the commit
     */
    public String getTree () {
        return treeCode;
    }
    
    /**
     * @return the first line of the commit message.
     */
    public String getShortMessage () {
        if (shortMessage == null) {
            String msg = message;
            StringBuilder sb = new StringBuilder();
            boolean empty = true;
            for (int pos = 0; pos < msg.length(); ++pos) {
                char c = msg.charAt(pos);
                if (c == '\r' || c == '\n') {
                    if (!empty) {
                        break;
                    }
                } else {
                    sb.append(c);
                    empty = false;
                }
            }
            shortMessage = sb.toString();
        }
        return shortMessage;
    }

    /**
     * @return full commit message
     */
    public String getFullMessage () {
        return message;
    }

    /**
     * @return time this commit was created in milliseconds.
     */
    public long getCommitTime () {
        if (commiterTime != null) {
            String[] s = commiterTime.split(" ");
            long res = Long.parseLong(s[0])*1000;
            //int zone = Integer.parseInt(s[1]);
            //res += (zone/100)*3600*1000;
            return res;
        }
        return -1;
    }

    /**
     * @return time this commit was created in milliseconds.
     */
    public long getAuthorTime () {
        if (autorTime != null) {
            //1423691643 -0800
            String[] s = autorTime.split(" ");
            long res = Long.parseLong(s[0])*1000;
            //int zone = Integer.parseInt(s[1]);
            //res += (zone/100)*3600*1000;
            return res;
        }
        return -1;
    }

    /**
     * @return author of the commit
     */
    public GitUser getAuthor () {
        if (autorAndMail != null) {
            int i = autorAndMail.indexOf('<');
            return new GitUser(autorAndMail.substring(0,i).trim(), autorAndMail.substring(i+1,autorAndMail.length()-1));
        }
        return null;
    }

    /**
     * @return person who actually committed the changes, may or may not be the same as a return value of the <code>getAuthor</code> method.
     */
    public GitUser getCommitter () {
        if (commiterAndMail != null) {
            int i = commiterAndMail.indexOf('<');
            return new GitUser(commiterAndMail.substring(0,i).trim(), commiterAndMail.substring(i+1,commiterAndMail.length()-1));
        }
        return null;
    }
    
    /**
     * Returns the information about the files affected (modified, deleted or added) by this commit.
     * <strong>First time call should not be done from the EDT.</strong> When called for the first time the method execution can take a big amount of time
     * because it compares the commit tree with its parents and identifies the modified files. 
     * Any subsequent call to the first <strong>successful</strong> call will return the cached value and will be fast.
     * @return files affected by this change set
     * @throws GitException when an error occurs
     */
    public java.util.Map<VCSFileProxy, GitFileInfo> getModifiedFiles () throws GitException {
        Map<VCSFileProxy, GitFileInfo> files = new HashMap<VCSFileProxy, GitFileInfo>(modifiedFiles.length);
        for (GitFileInfo info : modifiedFiles) {
            files.put(info.getFile(), info);
        }
        return files;
    }
    
    /**
     * @return commit ids of this commit's parents
     */
    public String[] getParents () {
        return parents;
    }
    
    /**
     * @return all branches known to contain this commit.
     * @since 1.14
     */
    public Map<String, GitBranch> getBranches () {
        return branches;
    }

    /**
     * Provides information about what happened to a file between two different commits.
     * If the file is copied or renamed between the two commits, you can get the path
     * of the original file.
     */
    public static final class GitFileInfo {

        /**
         * State of the file in the second commit in relevance to the first commit.
         */
        public static enum Status {
            ADDED,
            MODIFIED,
            RENAMED,
            COPIED,
            REMOVED,
            UNKNOWN
        }

        private final String relativePath;
        private final String originalPath;
        private final Status status;
        private final VCSFileProxy file;
        private final VCSFileProxy originalFile;

        GitFileInfo (VCSFileProxy file, String relativePath, Status status, VCSFileProxy originalFile, String originalPath) {
            this.relativePath = relativePath;
            this.status = status;
            this.file = file;
            this.originalFile = originalFile;
            this.originalPath = originalPath;
        }

        /**
         * @return relative path of the file to the root of the repository
         */
        public String getRelativePath() {
            return relativePath;
        }

        /**
         * @return the relative path of the original file this file was copied or renamed from.
         *         For other statuses than <code>COPIED</code> or <code>RENAMED</code> it may be <code>null</code> 
         *         or the same as the return value of <code>getPath</code> method
         */
        public String getOriginalPath() {
            return originalPath;
        }

        /**
         * @return state of the file between the two commits
         */
        public Status getStatus() {
            return status;
        }

        /**
         * @return the file this refers to
         */
        public VCSFileProxy getFile () {
            return file;
        }

        /**
         * @return the original file this file was copied or renamed from.
         *         For other statuses than <code>COPIED</code> or <code>RENAMED</code> it may be <code>null</code> 
         *         or the same as the return value of <code>getFile</code> method
         */
        public VCSFileProxy getOriginalFile () {
            return originalFile;
        }
    }
    
    public static final class GitRevCommit {
        public String branch;
        public String revisionCode;
        public String treeCode;
        public String message;
        public String autorAndMail;
        public String autorTime;
        public String commiterAndMail;
        public String commiterTime;
        public LinkedHashMap<String, GitRevisionInfo.GitFileInfo.Status> commitedFiles = new LinkedHashMap<String, GitRevisionInfo.GitFileInfo.Status>();
        public ArrayList<String> parents = new ArrayList<String>();

        public GitRevCommit() {
        }
    }

    
}
