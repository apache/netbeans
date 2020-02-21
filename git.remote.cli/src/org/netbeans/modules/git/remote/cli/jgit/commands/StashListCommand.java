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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.git.remote.cli.progress.RevisionInfoListener;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;

/**
 *
 */
public class StashListCommand extends GitCommand {

    private final List<GitRevisionInfo> revisions;
    private final RevisionInfoListener listener;
    private final ProgressMonitor monitor;
    
    public StashListCommand (JGitRepository repository, GitClassFactory accessor,
            ProgressMonitor monitor, RevisionInfoListener listener) {
        super(repository, accessor, monitor);
        revisions = new ArrayList<>();
        this.monitor = monitor;
        this.listener = listener;
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "stash"); //NOI18N
        addArgument(0, "list"); //NOI18N
        addArgument(0, "--raw"); //NOI18N
        addArgument(0, "--pretty=raw"); //NOI18N
    }

    public GitRevisionInfo[] getRevisions () {
        return revisions.toArray(new GitRevisionInfo[revisions.size()]);
    }

    private void addRevision (GitRevisionInfo info) {
        revisions.add(info);
        listener.notifyRevisionInfo(info);
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
                    LogCommand.parseLog(output, statuses);
                }
            }.runCLI();
            for(Map.Entry<String, GitRevisionInfo.GitRevCommit> entry : statuses.entrySet()) {
                addRevision(getClassFactory().createRevisionInfo(entry.getValue(), getRepository()));
            }
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if(canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        }        
    }
    
}
