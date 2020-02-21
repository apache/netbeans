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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.jgit.GitRef;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.jgit.Utils;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;

/**
 */
public class ListRemoteTagsCommand extends TransportCommand {
    private Map<String, String> remoteTags;
    private final String remoteUrl;
    private Collection<GitRef> refs;
    private final ProgressMonitor monitor;

    public ListRemoteTagsCommand (JGitRepository repository, GitClassFactory gitFactory, String remoteRepositoryUrl, ProgressMonitor monitor) {
        super(repository, gitFactory, remoteRepositoryUrl, monitor);
        this.remoteUrl = remoteRepositoryUrl;
        this.monitor = monitor;
    }

    private void processRefs () {
        remoteTags = new LinkedHashMap<String, String>();
        remoteTags.putAll(Utils.refsToTags(refs));
    }
    
    public Map<String, String> getTags () {
        return remoteTags;
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "ls-remote"); //NOI18N
        addArgument(0, "--tags"); //NOI18N
        addArgument(0, remoteUrl);
    }

    @Override
    protected final void runTransportCommand () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            refs = new ArrayList<>();
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    parseListRemoreTagsOutput(output);
                }
            }.runCLI();
            processRefs();
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        }
    }
    
    private void parseListRemoreTagsOutput(String output) {
        //d280c8ec1a4f776a3ac00767029b444143586410	refs/tags/my-tag
        //88c25ec82dfe40c92f6ea2dbad77e5ddd1cb77e0	refs/tags/my-tag^{}
        for (String line : output.split("\n")) { //NOI18N
            if (line.indexOf('^') < 0) {
                String[] s = line.split("\\s");
                if (s.length >= 2) {
                    String revision = s[0];
                    String name = s[1];
                    refs.add(new GitRef(name, revision));
                }
            }
        }
    }
    
}
