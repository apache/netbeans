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

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitConfig;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;

/**
 *
 */
public class RemoveRemoteCommand extends GitCommand {
    private final String remote;

    public RemoveRemoteCommand (JGitRepository repository, GitClassFactory gitFactory, String remote, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.remote = remote;
    }

    @Override
    protected void run () throws GitException {
        JGitConfig config = getRepository().getConfig();
        config.load();
        config.unsetSection(JGitConfig.CONFIG_REMOTE_SECTION, remote);
        Collection<String> subSections = config.getSubsections(JGitConfig.CONFIG_BRANCH_SECTION);
        for (String subSection : subSections) {
            if (remote.equals(config.getString(JGitConfig.CONFIG_BRANCH_SECTION, subSection, JGitConfig.CONFIG_KEY_REMOTE))) {
                config.unset(JGitConfig.CONFIG_BRANCH_SECTION, subSection, JGitConfig.CONFIG_KEY_REMOTE);
                config.unset(JGitConfig.CONFIG_BRANCH_SECTION, subSection, JGitConfig.CONFIG_KEY_MERGE);
            }
        }
        config.save();
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "remote"); //NOI18N
        addArgument(0, "rm"); //NOI18N
        addArgument(0, remote); //NOI18N
    }
}
