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

import java.io.IOException;
import java.util.Set;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class RemoveRemoteCommand extends GitCommand {
    private final String remote;

    public RemoveRemoteCommand (Repository repository, GitClassFactory gitFactory, String remote, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.remote = remote;
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        StoredConfig config = repository.getConfig();
        config.unsetSection(ConfigConstants.CONFIG_REMOTE_SECTION, remote);
        Set<String> subSections = config.getSubsections(ConfigConstants.CONFIG_BRANCH_SECTION);
        for (String subSection : subSections) {
            if (remote.equals(config.getString(ConfigConstants.CONFIG_BRANCH_SECTION, subSection, ConfigConstants.CONFIG_KEY_REMOTE))) {
                config.unset(ConfigConstants.CONFIG_BRANCH_SECTION, subSection, ConfigConstants.CONFIG_KEY_REMOTE);
                config.unset(ConfigConstants.CONFIG_BRANCH_SECTION, subSection, ConfigConstants.CONFIG_KEY_MERGE);
            }
        }
        try {
            config.save();
        } catch (IOException ex) {
            throw new GitException(ex);
        }
    }

    @Override
    protected String getCommandDescription () {
        return new StringBuilder("git remote rm ").append(remote).toString(); //NOI18N
    }
}
