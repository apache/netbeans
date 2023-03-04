/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.libs.git.jgit.commands;

import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class SetRemoteCommand extends GitCommand {
    private final GitRemoteConfig remote;
    
    private static final String KEY_URL = "url"; //NOI18N
    private static final String KEY_PUSHURL = "pushurl"; //NOI18N
    private static final String KEY_FETCH = "fetch"; //NOI18N
    private static final String KEY_PUSH = "push"; //NOI18N
    
    public SetRemoteCommand (Repository repository, GitClassFactory gitFactory, GitRemoteConfig remoteConfig, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.remote = remoteConfig;
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        StoredConfig config = repository.getConfig();
        boolean finished = false;
        try {
            config.unset(ConfigConstants.CONFIG_REMOTE_SECTION, remote.getRemoteName(), KEY_URL);
            config.unset(ConfigConstants.CONFIG_REMOTE_SECTION, remote.getRemoteName(), KEY_PUSHURL);
            config.unset(ConfigConstants.CONFIG_REMOTE_SECTION, remote.getRemoteName(), KEY_FETCH);
            config.unset(ConfigConstants.CONFIG_REMOTE_SECTION, remote.getRemoteName(), KEY_PUSH);
            RemoteConfig cfg = new RemoteConfig(config, remote.getRemoteName());
            for (String uri : remote.getUris()) {
                cfg.addURI(new URIish(uri));
            }
            for (String uri : remote.getPushUris()) {
                cfg.addPushURI(new URIish(uri));
            }
            for (String spec : remote.getFetchRefSpecs()) {
                cfg.addFetchRefSpec(new RefSpec(spec));
            }
            for (String spec : remote.getPushRefSpecs()) {
                cfg.addPushRefSpec(new RefSpec(spec));
            }
            cfg.update(config);
            config.save();
            finished = true;
        } catch (Exception ex) {
            throw new GitException(ex);
        } finally {
            if (!finished) {
                try {
                    if (config instanceof FileBasedConfig) {
                        FileBasedConfig fileConfig = (FileBasedConfig) config;
                        fileConfig.clear();
                    }
                    config.load();
                } catch (Exception e) {

                }
            }
        }
    }

    @Override
    protected String getCommandDescription () {
        return new StringBuilder("setting up remote: ").append(remote.getRemoteName()).toString(); //NOI18N
    }
}
