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

package org.netbeans.libs.git.jgit.commands;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.MessageFormat;
import org.eclipse.jgit.lib.Repository;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public abstract class GitCommand {
    private final Repository repository;
    private final ProgressMonitor monitor;
    protected static final String EMPTY_ROOTS = Utils.getBundle(GitCommand.class).getString("MSG_Error_NoFiles"); //NOI18N
    private final GitClassFactory gitFactory;

    protected GitCommand (Repository repository, GitClassFactory gitFactory, ProgressMonitor monitor) {
        this.repository = repository;
        this.gitFactory = gitFactory;
        this.monitor = monitor;
    }

    public final void execute () throws GitException {
        if (prepareCommand()) {
            try {
                monitor.started(getCommandDescription());
                try {
                    AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                        @Override
                        public Void run () throws GitException {
                            GitCommand.this.run();
                            return null;
                        }
                    });
                } catch (PrivilegedActionException e) {
                    throw (GitException) e.getException();
                }
            } catch (RuntimeException ex) {
                if (ex.getMessage() != null && ex.getMessage().contains("Unknown repository format")) { //NOI18N
                    throw new GitException("It seems the config file for repository at [" + repository.getWorkTree() + "] is corrupted.\nEnsure it's valid.", ex); //NOI18N
                } else {
                    throw ex;
                }
            } finally {
                monitor.finished();
            }
        }
    }

    protected abstract void run () throws GitException;

    protected boolean prepareCommand () throws GitException {
        boolean repositoryExists = repository.getDirectory().exists();
        if (!repositoryExists) {
            String message = MessageFormat.format(Utils.getBundle(GitCommand.class).getString("MSG_Error_RepositoryDoesNotExist"), repository.getWorkTree()); //NOI18N
            monitor.preparationsFailed(message);
            throw new GitException(message);
        }
        return repositoryExists;
    }

    protected Repository getRepository () {
        return repository;
    }

    protected abstract String getCommandDescription ();

    protected final GitClassFactory getClassFactory () {
        return gitFactory;
    }

    protected final void processMessages (String messages) {
        for (String msg : messages.split("\n")) { //NOI18N
            if (msg.startsWith(MSG_ERROR)) { //NOI18N
                monitor.notifyError(msg.substring(MSG_ERROR.length()).trim());
            } else if (msg.startsWith(MSG_WARNING)) { //NOI18N
                monitor.notifyWarning(msg.substring(MSG_WARNING.length()).trim());
            } else if (!msg.isEmpty()) {
                // these are not warnings, i guess, just plain informational messages
                monitor.notifyMessage(msg);
            }
        }
    }
    private static final String MSG_WARNING = "warning:"; //NOI18N
    private static final String MSG_ERROR = "error:"; //NOI18N
}
