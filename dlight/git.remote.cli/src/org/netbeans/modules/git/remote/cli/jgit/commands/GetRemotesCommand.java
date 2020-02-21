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
import org.netbeans.modules.git.remote.cli.GitRemoteConfig;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;

/**
 *
 */
public class GetRemotesCommand extends GitCommand {
    public static final boolean KIT = false;
    private final ProgressMonitor monitor;
    private Map<String, GitRemoteConfig> remotes;
    
    public GetRemotesCommand (JGitRepository repository, GitClassFactory gitFactory, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.monitor = monitor;
    }
    
    public Map<String, GitRemoteConfig> getRemotes () {
        return remotes;
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "remote"); //NOI18N
        addArgument(0, "-v"); //NOI18N
    }

    @Override
    protected void run () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            remotes = new LinkedHashMap<>();
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    parseRemoteOutput(output);
                }

                @Override
                protected void errorParser(String error) throws GitException {
                    parseAddError(error);
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
    
    private void parseRemoteOutput(String output) {
        //$ git remote -v
        //origin	https://github.com/git/git (fetch)
        //origin	https://github.com/git/git (push)
        Map<String, RemoteContainer> list = new LinkedHashMap<>();
        for (String line : output.split("\n")) { //NOI18N
            if (!line.isEmpty()) {
                line = line.replace('\t', ' ').trim();
                String[] s = line.split(" ");
                String remoteName = s[0];
                RemoteContainer conf = list.get(remoteName);
                if (conf == null) {
                    conf = new RemoteContainer();
                    list.put(remoteName, conf);
                }
                if (s.length == 3) {
                    if ("(fetch)".equals(s[2])) {
                        conf.uris.add(s[1]);
                    } else if ("(push)".equals(s[2])) {
                        conf.pushUris.add(s[1]);
                    }  
                }
            }
        }
        for(Map.Entry<String, RemoteContainer> e : list.entrySet()) {
            GitRemoteConfig fromConfig = new GitRemoteConfig(getRepository().getConfig(), e.getKey());
            final RemoteContainer value = e.getValue();
            value.fetchSpecs =  fromConfig.getFetchRefSpecs();
            value.pushSpecs = fromConfig.getPushRefSpecs();
            if (value.uris.isEmpty()) {
                value.uris = fromConfig.getUris();
            }
            if (value.pushUris.isEmpty()) {
                value.uris = fromConfig.getPushUris();
            }
            GitRemoteConfig conf = new GitRemoteConfig(e.getKey(), value.uris, value.pushUris, value.fetchSpecs, value.pushSpecs);
            remotes.put(e.getKey(), conf);
        }
    }
    
    private void parseAddError(String error) {
        processMessages(error);
    }
    
    private static final class RemoteContainer {
        List<String> uris = new ArrayList<>();
        List<String> pushUris = new ArrayList<>();
        List<String> fetchSpecs = new ArrayList<>();
        List<String> pushSpecs = new ArrayList<>();
    }

}
