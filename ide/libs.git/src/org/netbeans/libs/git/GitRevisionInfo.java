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

package org.netbeans.libs.git;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.netbeans.libs.git.jgit.Utils;

/**
 * Provides information about a certain commit, usually is returned by 
 * git commit or log command.
 * 
 * @author Jan Becicka
 */
public final class GitRevisionInfo {

    private final RevCommit revCommit;
    private final Repository repository;
    private final Map<String, GitBranch> branches;
    private GitFileInfo[] modifiedFiles;
    private static final Logger LOG = Logger.getLogger(GitRevisionInfo.class.getName());
    private String shortMessage;

    GitRevisionInfo (RevCommit commit, Repository repository) {
        this(commit, Collections.<String, GitBranch>emptyMap(), repository);
    }

    GitRevisionInfo (RevCommit commit, Map<String, GitBranch> affectedBranches, Repository repository) {
        this.revCommit = commit;
        this.repository = repository;
        this.branches = Collections.unmodifiableMap(affectedBranches);
    }

    /**
     * @return id of the commit
     */
    public String getRevision () {
        return ObjectId.toString(revCommit.getId());
    }

    /**
     * @return the first line of the commit message.
     */
    public String getShortMessage () {
        if (shortMessage == null) {
            String msg = revCommit.getFullMessage();
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
        return revCommit.getFullMessage();
    }

    /**
     * @return time this commit was created in milliseconds.
     */
    public long getCommitTime () {
        // must be indeed author, that complies with CLI
        // committer time is different after rebase
        PersonIdent author = revCommit.getAuthorIdent();
        if (author == null) {
            return (long) revCommit.getCommitTime() * 1000;
        } else {
            return author.getWhen().getTime();
        }
    }

    /**
     * @return author of the commit
     */
    public GitUser getAuthor () {
        return GitClassFactoryImpl.getInstance().createUser(revCommit.getAuthorIdent());
    }

    /**
     * @return person who actually committed the changes, may or may not be the same as a return value of the <code>getAuthor</code> method.
     */
    public GitUser getCommitter () {
        return GitClassFactoryImpl.getInstance().createUser(revCommit.getCommitterIdent());
    }
    
    /**
     * Returns the information about the files affected (modified, deleted or added) by this commit.
     * <strong>First time call should not be done from the EDT.</strong> When called for the first time the method execution can take a big amount of time
     * because it compares the commit tree with its parents and identifies the modified files. 
     * Any subsequent call to the first <strong>successful</strong> call will return the cached value and will be fast.
     * @return files affected by this change set
     * @throws GitException when an error occurs
     */
    public java.util.Map<java.io.File, GitFileInfo> getModifiedFiles () throws GitException {
        if (modifiedFiles == null) {
            synchronized (this) {
                listFiles();
            }
        }
        Map<File, GitFileInfo> files = new HashMap<>(modifiedFiles.length);
        for (GitFileInfo info : modifiedFiles) {
            files.put(info.getFile(), info);
        }
        return files;
    }
    
    /**
     * @return commit ids of this commit's parents
     */
    public String[] getParents () {
        String[] parents = new String[revCommit.getParentCount()];
        for (int i = 0; i < revCommit.getParentCount(); ++i) {
            parents[i] = ObjectId.toString(revCommit.getParent(i).getId());
        }
        return parents;
    }
    
    /**
     * @return all branches known to contain this commit.
     * @since 1.14
     */
    public Map<String, GitBranch> getBranches () {
        return branches;
    }
    
    private void listFiles() throws GitException {
        try (RevWalk revWalk = new RevWalk(repository);
            TreeWalk walk = new TreeWalk(repository)) {
            List<GitFileInfo> result;
            walk.reset();
            walk.setRecursive(true);
            RevCommit parentCommit = null;
            if (revCommit.getParentCount() > 0) {
                for (RevCommit commit : revCommit.getParents()) {
                    revWalk.markStart(revWalk.lookupCommit(commit));
                }
                revWalk.setRevFilter(RevFilter.MERGE_BASE);
                Iterator<RevCommit> it = revWalk.iterator();
                if (it.hasNext()) {
                    parentCommit = it.next();
                }
                if (parentCommit != null) {
                    walk.addTree(parentCommit.getTree().getId());
                }
            }
            walk.addTree(revCommit.getTree().getId());
            walk.setFilter(AndTreeFilter.create(TreeFilter.ANY_DIFF, PathFilter.ANY_DIFF));
            if (parentCommit != null) {
                result = Utils.getDiffEntries(repository, walk, GitClassFactoryImpl.getInstance());
            } else {
                result = new ArrayList<>();
                while (walk.next()) {
                    result.add(new GitFileInfo(new File(repository.getWorkTree(), walk.getPathString()), walk.getPathString(), GitFileInfo.Status.ADDED, null, null));
                }
            }
            this.modifiedFiles = result.toArray(new GitFileInfo[0]);
        } catch (IOException ex) {
            throw new GitException(ex);
        }
    }

    private static Map<String, GitBranch> buildBranches (RevCommit commit, Map<String, GitBranch> branches) {
        Map<String, GitBranch> retval = new LinkedHashMap<>(branches.size());
        
        return retval;
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
        private final File file;
        private final File originalFile;

        GitFileInfo (File file, String relativePath, Status status, File originalFile, String originalPath) {
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
        public File getFile () {
            return file;
        }

        /**
         * @return the original file this file was copied or renamed from.
         *         For other statuses than <code>COPIED</code> or <code>RENAMED</code> it may be <code>null</code> 
         *         or the same as the return value of <code>getFile</code> method
         */
        public File getOriginalFile () {
            return originalFile;
        }
    }
    
}
