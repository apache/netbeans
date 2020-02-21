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
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitSubmoduleStatus;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class SubmoduleStatusCommand extends GitCommand {
    
    private final VCSFileProxy[] roots;
    private final LinkedHashMap<VCSFileProxy, GitSubmoduleStatus> statuses;
    private final ProgressMonitor monitor;

    public SubmoduleStatusCommand (JGitRepository repository, GitClassFactory classFactory,
            VCSFileProxy[] roots, ProgressMonitor monitor) {
        super(repository, classFactory, monitor);
        this.roots = roots;
        this.monitor = monitor;
        statuses = new LinkedHashMap<>();
    }

    public Map<VCSFileProxy, GitSubmoduleStatus> getStatuses () {
        return statuses;
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "submodule"); //NOI18N
        addArgument(0, "status"); //NOI18N
        addFiles(0, roots);
    }

    @Override
    protected void run () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    parseStatusOutput(output);
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
    
    private void parseStatusOutput(String output) {
        //"-636aae5b92efef302398bd5e84219dba80e8fe06 submodules/submodule1"
        for (String line : output.split("\n")) { //NOI18N
            if(line.indexOf(' ') >=0) {
                char c = line.charAt(0);
                line= line.substring(1);
                String[] s = line.split(" ");
                if (s.length == 2) {
                    VCSFileProxy file = VCSFileProxy.createFileProxy(getRepository().getLocation(), s[1]);
                    GitSubmoduleStatus.StatusType st = GitSubmoduleStatus.StatusType.INITIALIZED;
                    switch(c) {
                        case '-'://submodule is not initialized
                            st = GitSubmoduleStatus.StatusType.UNINITIALIZED;
                            break;
                        case '+'://the currently checked out submodule commit does not match the SHA-1 found in the index of the containing repository
                            st = GitSubmoduleStatus.StatusType.REV_CHECKED_OUT;
                            break;
                        case 'U'://submodule has merge conflicts
                            st = GitSubmoduleStatus.StatusType.MISSING;
                            break;
                    }
                    statuses.put(file, getClassFactory().createSubmoduleStatus(st, file));
                }
            }
        }
    }
    
}
