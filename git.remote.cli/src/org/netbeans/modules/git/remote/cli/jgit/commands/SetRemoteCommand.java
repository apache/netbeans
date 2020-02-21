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

import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitRemoteConfig;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitConfig;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;

/**
 *
 */
public class SetRemoteCommand extends GitCommand {
    private final GitRemoteConfig remote;
    
    public SetRemoteCommand (JGitRepository repository, GitClassFactory gitFactory, GitRemoteConfig remoteConfig, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.remote = remoteConfig;
    }

    @Override
    protected void run () throws GitException {
        JGitConfig config = getRepository().getConfig();
        config.load();
        config.unset(JGitConfig.CONFIG_REMOTE_SECTION, remote.getRemoteName(), JGitConfig.CONFIG_KEY_URL);
        config.unset(JGitConfig.CONFIG_REMOTE_SECTION, remote.getRemoteName(), JGitConfig.CONFIG_KEY_PUSHURL);
        config.unset(JGitConfig.CONFIG_REMOTE_SECTION, remote.getRemoteName(), JGitConfig.CONFIG_KEY_FETCH);
        config.unset(JGitConfig.CONFIG_REMOTE_SECTION, remote.getRemoteName(), JGitConfig.CONFIG_KEY_PUSH);
        for (String uri : remote.getUris()) {
            config.setString(JGitConfig.CONFIG_REMOTE_SECTION, remote.getRemoteName(), JGitConfig.CONFIG_KEY_URL, uri);
        }
        for (String uri : remote.getPushUris()) {
            config.setString(JGitConfig.CONFIG_REMOTE_SECTION, remote.getRemoteName(), JGitConfig.CONFIG_KEY_PUSHURL, uri);
        }
        for (String spec : remote.getFetchRefSpecs()) {
            config.setString(JGitConfig.CONFIG_REMOTE_SECTION, remote.getRemoteName(), JGitConfig.CONFIG_KEY_FETCH, spec);
        }
        for (String spec : remote.getPushRefSpecs()) {
            config.setString(JGitConfig.CONFIG_REMOTE_SECTION, remote.getRemoteName(), JGitConfig.CONFIG_KEY_PUSH, spec);
        }
        config.save();
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "setting up remote"); //NOI18N
        addArgument(0, remote.getRemoteName());
    }
}
