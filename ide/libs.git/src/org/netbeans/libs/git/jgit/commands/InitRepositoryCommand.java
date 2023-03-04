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
import java.io.IOException;
import java.text.MessageFormat;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class InitRepositoryCommand extends GitCommand {
    private final File workDir;
    private final ProgressMonitor monitor;

    public InitRepositoryCommand (Repository repository, GitClassFactory gitFactory, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.monitor = monitor;
        this.workDir = getRepository().getWorkTree();
    }

    @Override
    protected boolean prepareCommand () throws GitException {
        boolean repositoryExists = getRepository().getDirectory().exists();
        if (repositoryExists) {
            String message = MessageFormat.format(Utils.getBundle(InitRepositoryCommand.class).getString("MSG_Error_RepositoryExists"), //NOI18N
                    getRepository().getWorkTree(), getRepository().getDirectory());
            monitor.preparationsFailed(message);
            throw new GitException(message);
        }
        return true;
    }

    @Override
    protected void run() throws GitException {
        Repository repository = getRepository();
        try {
            if (!(workDir.exists() || workDir.mkdirs())) {
                throw new GitException(MessageFormat.format(Utils.getBundle(InitRepositoryCommand.class).getString("MSG_Exception_CannotCreateFolder"), workDir.getAbsolutePath())); //NOI18N
            }
            repository.create();
            StoredConfig cfg = repository.getConfig();
            cfg.setBoolean(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_BARE, false);
            cfg.unset(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_AUTOCRLF);
            cfg.save();
        } catch (IllegalStateException ex) {
            throw new GitException(ex);
        } catch (IOException ex) {
            throw new GitException(ex);
        }
    }

    @Override
    protected String getCommandDescription () {
        return new StringBuilder("git init ").append(workDir.getAbsolutePath()).toString(); //NOI18N
    }
}
