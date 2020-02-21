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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.cli.GitConstants;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;

/**
 *
 */
public class ListBranchCommand extends GitCommand {
    public static final boolean KIT = false;
    private final boolean all;
    private final ProgressMonitor monitor;
    private Map<String, GitBranch> branches;

    public ListBranchCommand (JGitRepository repository, GitClassFactory gitFactory, boolean all, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.all = all;
        this.monitor = monitor;
    }

    public Map<String, GitBranch> getBranches () {
        return branches;
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "branch"); //NOI18N
        addArgument(0, "-v"); //NOI18N
        addArgument(0, "-v"); //NOI18N
        if (all) {
            addArgument(0, "-a"); //NOI18N
        }
    }
    
    @Override
    protected void run () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            branches = new LinkedHashMap<String, GitBranch>();
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    parseBranches(output, getClassFactory(), branches);
                }

                @Override
                protected void errorParser(String error) throws GitException {
                    throw new GitException("It seems the config file for repository at [" + ListBranchCommand.this.getRepository().getLocation().getPath() + "] is corrupted.\nEnsure it's valid.");
                }
                
            }.runCLI();
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        }
    }

    static void parseBranches(String output, GitClassFactory factory, Map<String, GitBranch> branches) {
        //#git branch -a -v
        //* master                18d0fec Post 2.3 cycle (batch #1)
        //  remotes/origin/HEAD   -> origin/master
        //  remotes/origin/maint  9874fca Git 2.3
        //  remotes/origin/master 18d0fec Post 2.3 cycle (batch #1)
        //  remotes/origin/next   021ec32 Merge branch 'mg/push-repo-option-doc' into next
        //  remotes/origin/pu     f5d0ad1 Merge branch 'nd/slim-index-pack-memory-usage' into pu
        //  remotes/origin/todo   5914a77 Meta/Announce: adjust to possible change to the top-level RelNotes
        //#git branch -vv --all
        //* master                8408c76 [origin/master] init commit
        //  remotes/origin/master 8408c76 init commit        
        //#git branch -vv --all
        //* master                79c5362 [origin/master] init commit
        //  nova1                 79c5362 [master] init commit
        //  remotes/origin/master 79c5362 init commit
        //#git branch -vv --all
        //* master    295d064 commit
        //  newbranch fa663c1 [master: behind 2] initial commit        
        //#git branch -vv --all
        //  master    699a8aa commit
        //* newbranch 1e2f87c [master: ahead 1, behind 1] commit
        //#git branch -vv --all
        //* (no branch) 024ac8c change
        //  master      024ac8c change
        //  nova        024ac8c change
        HashMap<String, String> links = new HashMap<String,String>();
        for (String line : output.split("\n")) { //NOI18N
            if (line.startsWith("* ") || line.startsWith("  ")) {
                boolean def = '*' == line.charAt(0);
                line = line.substring(2);
                String branchName = null;
                if (line.startsWith("(no branch)")) {
                    branchName = "(no branch)";
                    line = "(no_branch)"+line.substring(11);
                }
                String[] s = line.split("\\s+");
                if (s.length > 1 && "->".equals(s[1])) {
                    continue;
                }
                if (branchName == null) {
                    branchName = s[0];
                }
                boolean remote = branchName.startsWith("remotes/");
                if (remote) {
                    branchName = branchName.substring(8);
                }
                GitBranch createBranch;
                if (s.length > 1) {
                    int i = line.indexOf('[');
                    int j = line.indexOf(']');
                    if (i > 0 && j > 0 && i < j) {
                        String link = line.substring(i+1, j);
                        int k = link.indexOf(':');
                        if (k > 0) {
                            links.put(branchName, link.substring(0, k));
                        } else {
                            links.put(branchName, link);
                        }
                    }
                    createBranch = factory.createBranch(branchName, remote, def, s[1]);
                } else {
                    createBranch = factory.createBranch(branchName, remote, def, GitConstants.HEAD);
                }
                branches.put(branchName, createBranch);
                //public abstract GitBranch createBranch (String name, boolean remote, boolean active, ObjectId id);
                continue;
            }
        }
        for (Map.Entry<String, String> entry : links.entrySet()) {
            GitBranch orig = branches.get(entry.getKey());
            GitBranch link = branches.get(entry.getValue());
            if (orig != null && link != null && link.isRemote()) {
                factory.setBranchTracking(orig, link);
            }
        }
    }
}
