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

import java.text.MessageFormat;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitConfig;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.jgit.Utils;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class InitRepositoryCommand extends GitCommand {
    private final VCSFileProxy workDir;
    private final ProgressMonitor monitor;
    private final boolean isBare;

    public InitRepositoryCommand (JGitRepository repository, GitClassFactory gitFactory, boolean isBare, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.monitor = monitor;
        this.isBare = isBare;
        this.workDir = getRepository().getLocation();
    }

    @Override
    protected boolean prepareCommand () throws GitException {
        boolean repositoryExists = getRepository().getMetadataLocation().exists();
        if (repositoryExists) {
            String message = MessageFormat.format(Utils.getBundle(InitRepositoryCommand.class).getString("MSG_Error_RepositoryExists"), //NOI18N
                    getRepository().getLocation(), getRepository().getMetadataLocation());
            monitor.preparationsFailed(message);
            throw new GitException(message);
        } else {
            prepare();
        }
        return true;
    }

    @Override
    protected void run() throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                }
                
            }.runCLI();
            
            JGitConfig cfg = getRepository().getConfig();
            cfg.setBoolean(JGitConfig.CONFIG_CORE_SECTION, null, JGitConfig.CONFIG_KEY_BARE, false);
            cfg.unset(JGitConfig.CONFIG_CORE_SECTION, null, JGitConfig.CONFIG_KEY_AUTOCRLF);
            cfg.save();
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        }
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "init"); //NOI18N
        if (isBare) {
            addArgument(0, "--bare"); //NOI18N
        }
        addArgument(0, workDir.getPath());
    }
}
