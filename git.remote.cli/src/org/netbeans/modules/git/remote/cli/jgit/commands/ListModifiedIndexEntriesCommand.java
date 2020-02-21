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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.FileListener;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class ListModifiedIndexEntriesCommand extends GitCommand {

    private final VCSFileProxy[] roots;
    private final ProgressMonitor monitor;
    private final FileListener listener;
    private final Set<VCSFileProxy> files;

    public ListModifiedIndexEntriesCommand (JGitRepository repository, GitClassFactory gitFactory, VCSFileProxy[] roots, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, monitor);
        this.roots = roots;
        this.monitor = monitor;
        this.listener = listener;
        this.files = new HashSet<VCSFileProxy>();
    }

    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "status"); //NOI18N
        addArgument(0, "--short"); //NOI18N
        addArgument(0, "--ignored"); //NOI18N
        addArgument(0, "--untracked-files=normal"); //NOI18N
        addArgument(0, "--"); //NOI18N
        addFiles(0, roots);
    }

    public VCSFileProxy[] getFiles () {
        return files.toArray(new VCSFileProxy[files.size()]);
    }

    @Override
    protected void run () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            final LinkedHashMap<String, StatusCommand.StatusLine> list = new LinkedHashMap<>();
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    StatusCommand.parseStatusOutput(output, list, false);
                }
            }.runCLI();
            for (Map.Entry<String, StatusCommand.StatusLine> e : list.entrySet()) {
                if (e.getValue().first == 'M') {
                    files.add(VCSFileProxy.createFileProxy(getRepository().getLocation(), e.getKey()));
                }
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
