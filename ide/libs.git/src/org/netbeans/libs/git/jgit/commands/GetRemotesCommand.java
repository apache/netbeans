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

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.RemoteConfig;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class GetRemotesCommand extends GitCommand {

    private Map<String, GitRemoteConfig> remotes;
    
    public GetRemotesCommand (Repository repository, GitClassFactory gitFactory, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        try {
            List<RemoteConfig> configs = RemoteConfig.getAllRemoteConfigs(repository.getConfig());
            remotes = new HashMap<String, GitRemoteConfig>(configs.size());
            for (RemoteConfig remote : configs) {
                remotes.put(remote.getName(), getClassFactory().createRemoteConfig(remote));
            }
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage().contains("Invalid wildcards")) {
                throw new GitException("Unsupported remote definition in " 
                        + new File(repository.getDirectory(), "config")
                        + ". Please fix the definition before using remotes.", ex);
            }
            throw ex;
        } catch (URISyntaxException ex) {
            throw new GitException(ex);
        }
    }

    @Override
    protected String getCommandDescription () {
        return "git remote -v"; //NOI18N
    }

    public Map<String, GitRemoteConfig> getRemotes () {
        return remotes;
    }
}
