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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.cli.GitConstants;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitObjectType;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.SearchCriteria;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.git.remote.cli.progress.RevisionInfoListener;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;

/**
 *
 */
public class LogCommand extends GitCommand {
    public static final boolean KIT = false;
    private final ProgressMonitor monitor;
    private final RevisionInfoListener listener;
    private final List<GitRevisionInfo> revisions;
    private final String revision;
    private final SearchCriteria criteria;
    private final boolean fetchBranchInfo;
    private final Revision revisionPlaseHolder;
    private static final Logger LOG = Logger.getLogger(LogCommand.class.getName());

    public LogCommand (JGitRepository repository, GitClassFactory gitFactory, SearchCriteria criteria,
            boolean fetchBranchInfo, ProgressMonitor monitor, RevisionInfoListener listener) {
        super(repository, gitFactory, monitor);
        this.monitor = monitor;
        this.listener = listener;
        this.criteria = criteria;
        this.fetchBranchInfo = fetchBranchInfo;
        this.revision = null;
        this.revisions = new LinkedList<>();
        if (fetchBranchInfo) {
            this.revisionPlaseHolder = new Revision();
        } else {
            this.revisionPlaseHolder = null;
        }
    }
    
    public LogCommand (JGitRepository repository, GitClassFactory gitFactory, String revision, ProgressMonitor monitor, RevisionInfoListener listener) {
        super(repository, gitFactory, monitor);
        this.monitor = monitor;
        this.listener = listener;
        this.criteria = null;
        this.fetchBranchInfo = false;
        this.revision = revision;
        this.revisions = new LinkedList<>();
        this.revisionPlaseHolder = null;
    }
    
    public GitRevisionInfo[] getRevisions () {
        return revisions.toArray(new GitRevisionInfo[revisions.size()]);
    }
    
    @Override
    protected void prepare() throws GitException {
        if (fetchBranchInfo) {
            setCommandsNumber(2);
        }
        super.prepare();
        addArgument(0, "log"); //NOI18N
        addArgument(0, "--raw"); //NOI18N
        addArgument(0, "--pretty=raw"); //NOI18N
        if (criteria != null && criteria.isFollow() && criteria.getFiles() != null && criteria.getFiles().length == 1) {
            // Options --follow and  --full-diff are not compatible.
            // Lets give preference to --full-diff. As result log will not tack renames.
            //addArgument(0, "--follow"); //NOI18N
        }
        if (criteria != null && !criteria.isIncludeMerges()) {
            addArgument(0, "--no-merges"); //NOI18N
        } else {
            addArgument(0, "-m"); //NOI18N
        }
        
        if (revision != null) {
            addArgument(0, "--no-walk"); //NOI18N
            addArgument(0, revision);
        } else if (criteria != null && criteria.getRevisionTo() != null && criteria.getRevisionFrom() != null) {
            if (criteria.getRevisionFrom().equals(criteria.getRevisionTo())) {
                addArgument(0, criteria.getRevisionFrom());
            } else {
                if (criteria.isAddSelfFrom()) {
                    addArgument(0, criteria.getRevisionFrom()+"^.."+criteria.getRevisionTo());
                } else {
                    addArgument(0, criteria.getRevisionFrom()+".."+criteria.getRevisionTo());
                }
            }
        } else if (criteria != null && criteria.getRevisionTo() != null) {
            addArgument(0, criteria.getRevisionTo());
        } else if (criteria != null && criteria.getRevisionFrom() != null) {
            if (criteria.isAddSelfFrom()) {
                addArgument(0, criteria.getRevisionFrom()+"^..");
            } else {
                addArgument(0, criteria.getRevisionFrom()+"..");
            }
        } else {
            addArgument(0, "--all");
        }
        if (criteria != null && criteria.getUsername() != null) {
            addArgument(0, "--author="+criteria.getUsername());
        }
        if (criteria != null && criteria.getMessage() != null) {
            String pattern = criteria.getMessage();
            if (pattern.indexOf('\n')>=0) {
                pattern = pattern.substring(0,pattern.indexOf('\n'));
            }
            if (!pattern.startsWith("^") && !pattern.startsWith(".*")) {
                pattern = ".*" + pattern;
            }
            if (!pattern.endsWith("$") && !pattern.endsWith(".*")) {
                pattern = pattern + ".*";
            }
            addArgument(0, "--grep="+pattern);
        }
        if (criteria != null && criteria.getFrom() != null && criteria.getTo() != null) {
            addArgument(0, "--since="+(criteria.getFrom().getTime()/1000));
            addArgument(0, "--until="+(criteria.getTo().getTime()/1000));
        } else if (criteria != null && criteria.getFrom() != null) {
            addArgument(0, "--since="+(criteria.getFrom().getTime()/1000));
        } else if (criteria != null && criteria.getTo() != null) {
            addArgument(0, "--until="+(criteria.getTo().getTime()/1000));
        }
        if (criteria != null && criteria.getLimit() > 0) {
            addArgument(0, "-"+criteria.getLimit());
        }
        
        if (criteria != null && criteria.getFiles().length > 0) {
            addArgument(0, "--full-diff");
            addArgument(0, "--");
            addFiles(0, criteria.getFiles());
        }
        if (fetchBranchInfo) {
            addArgument(1, "branch");
            addArgument(1, "-v"); //NOI18N
            addArgument(1, "-v"); //NOI18N
            addArgument(1, "-a"); //NOI18N
            addArgument(1, "--contains");
            addArgument(1, revisionPlaseHolder);
        }
        
    }
    
    @Override
    protected void run () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            final LinkedHashMap<String, GitRevisionInfo.GitRevCommit> statuses = new LinkedHashMap<String, GitRevisionInfo.GitRevCommit>();
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    parseLog(output, statuses);
                }

                @Override
                protected void errorParser(String error) throws GitException {
                    for (String msg : error.split("\n")) { //NOI18N
                        if (msg.startsWith("fatal: Invalid object")) {
                            throw new GitException.MissingObjectException(GitConstants.HEAD ,GitObjectType.COMMIT);
                        } else if (msg.startsWith("fatal: ambiguous argument")) {
                            throw new GitException.MissingObjectException(revision ,GitObjectType.COMMIT);
                        }
                    }
                    super.errorParser(error);
                }
                
            }.runCLI();
            for(Map.Entry<String, GitRevisionInfo.GitRevCommit> entry : statuses.entrySet()) {
                if (fetchBranchInfo) {
                    revisionPlaseHolder.setContent(entry.getKey());
                    final Map<String, GitBranch> branches = new LinkedHashMap<String, GitBranch>();
                    new Runner(canceled, 1){

                        @Override
                        public void outputParser(String output) throws GitException {
                            ListBranchCommand.parseBranches(output, getClassFactory(), branches);
                        }

                    }.runCLI();
                    revisions.add(getClassFactory().createRevisionInfo(entry.getValue(), branches, getRepository()));
                } else {
                    revisions.add(getClassFactory().createRevisionInfo(entry.getValue(), getRepository()));
                }
            }
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        }
    }
    
    static void parseLog(String output, LinkedHashMap<String, GitRevisionInfo.GitRevCommit> statuses) {
        //#git --no-pager log --name-status --no-walk 0254bffe448b1951af6edef531d80f8e629c575a"
        //commit 9c0e341a6a9197e2408862d2e6ff4b7635a01f9b (from 19f759b14972f669dc3eb203c06944e03365f6bc)
        //Reflog: refs/stash@{0} 
        //Reflog message: On master: stash
        //Merge: 1126f32 846626a
        //Author: 
        //Date:   Tue Feb 17 16:12:39 2015 +0300
        //
        //    Merge b
        GitRevisionInfo.GitRevCommit status = new GitRevisionInfo.GitRevCommit();
        StringBuilder buf = new StringBuilder();
        for (String line : output.split("\n")) { //NOI18N
            if (line.startsWith("Reflog:")) {
                continue;
            }
            if (line.startsWith("Reflog message:")) {
                continue;
            }
            if (line.startsWith("committer")) {
                String s = line.substring(9).trim();
                int i = s.indexOf('>');
                if (i > 0) {
                    status.commiterAndMail = s.substring(0, i + 1);
                    status.commiterTime = s.substring(i + 1).trim();
                }
                continue;
            }
            if (line.startsWith("commit")) {
                String revCode = line.substring(6).trim();
                int i = revCode.indexOf('(');
                if (i > 0) {
                    revCode = revCode.substring(0, i-1).trim();
                }
                if (status.revisionCode != null) {
                    status.message = buf.toString();
                    buf.setLength(0);
                    statuses.put(status.revisionCode, status);
                    if (statuses.containsKey(revCode)) {
                        status = statuses.get(revCode);
                    } else {
                        status = new GitRevisionInfo.GitRevCommit();
                    }
                }
                status.revisionCode = revCode;
                continue;
            }
            if (line.startsWith("tree")) {
                status.treeCode = line.substring(4).trim();
                continue;
            }
            if (line.startsWith("parent")) {
                status.parents.add(line.substring(6).trim());
                continue;
            }
            if (line.startsWith("author")) {
                String s = line.substring(6).trim();
                int i = s.indexOf('>');
                if (i > 0) {
                    status.autorAndMail = s.substring(0, i + 1);
                    status.autorTime = s.substring(i + 1).trim();
                }
                continue;
            }
            if (line.startsWith(" ")) {
                //if (buf.length() > 0) {
                //    buf.append('\n');
                //}
                buf.append(line.trim());
                buf.append('\n');
                continue;
            }
            if (line.startsWith(":")) {
                String[] s = line.split("\\s");
                if (s.length > 2) {
                    String file = s[s.length - 1];
                    String st = s[s.length - 2];
                    GitRevisionInfo.GitFileInfo.Status gitSt = GitRevisionInfo.GitFileInfo.Status.UNKNOWN;
                    if ("A".equals(st)) {
                        gitSt = GitRevisionInfo.GitFileInfo.Status.ADDED;
                    } else if ("M".equals(st)) {
                        gitSt = GitRevisionInfo.GitFileInfo.Status.MODIFIED;
                    } else if ("R".equals(st)) {
                        gitSt = GitRevisionInfo.GitFileInfo.Status.RENAMED;
                    } else if ("C".equals(st)) {
                        gitSt = GitRevisionInfo.GitFileInfo.Status.COPIED;
                    } else if ("D".equals(st)) {
                        gitSt = GitRevisionInfo.GitFileInfo.Status.REMOVED;
                    }
                    status.commitedFiles.put(file, gitSt);
                }
                continue;
            }
        }
        if (status.revisionCode != null) {
            status.message = buf.toString();
            statuses.put(status.revisionCode, status);
        }
    }
}
