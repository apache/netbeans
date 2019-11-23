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

import java.io.IOException;
import java.util.Arrays;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.errors.CheckoutConflictException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitMergeResult;
import org.netbeans.libs.git.GitRepository.FastForwardOption;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class MergeCommand extends GitCommand {
    private final String revision;
    private GitMergeResult result;
    private String commitMessage;
    private final FastForwardOption ffOption;

    public MergeCommand (Repository repository, GitClassFactory gitFactory, String revision,
            FastForwardOption ffOption, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.ffOption = ffOption;
        this.revision = revision;
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        org.eclipse.jgit.api.MergeCommand command = new Git(repository).merge();
        setFastForward(command);
        Ref ref = null;
        try {
            ref = repository.findRef(revision);
        } catch (IOException ex) {
            throw new GitException(ex);
        }

        if (ref == null) {
            command.include(Utils.findCommit(repository, revision));
        } else {
            String msg = commitMessage;
            if (msg == null) {
                msg = Utils.getRefName(ref);
            }
            command.include(msg, ref.getTarget().getObjectId());
        }
        try {
            result = getClassFactory().createMergeResult(command.call(), repository.getWorkTree());
        } catch (org.eclipse.jgit.api.errors.NoMessageException | org.eclipse.jgit.api.errors.WrongRepositoryStateException ex) {
            throw new GitException(Utils.getBundle(MergeCommand.class).getString("MSG_MergeCommand.commitErr.wrongRepoState"), ex); //NOI18N
        } catch (org.eclipse.jgit.api.errors.CheckoutConflictException ex) {
            parseConflicts(ex);
        } catch (JGitInternalException ex) {
            if (ex.getCause() instanceof CheckoutConflictException) {
                parseConflicts(ex.getCause());
            }
            throw new GitException(ex);
        } catch (GitAPIException ex) {
            throw new GitException(ex);
        }
    }

    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder("git merge "); //NOI18N
        if (ffOption != null) {
            sb.append(ffOption).append(" "); //NOI18N
        }
        return sb.append(revision).toString();
    }

    public GitMergeResult getResult () {
        return result;
    }

    void setCommitMessage (String message) {
        if (message != null) {
            message = message.replace("\n", "").replace("\r", ""); //NOI18N
        }
        this.commitMessage = message;
    }

    private void parseConflicts (Throwable original) throws GitException.CheckoutConflictException, GitException {
        String[] lines = original.getMessage().split("\n"); //NOI18N
        if (lines.length > 1) {
            throw new GitException.CheckoutConflictException(Arrays.copyOfRange(lines, 1, lines.length), original);
        }
        throw new GitException(original);
    }

    private void setFastForward (org.eclipse.jgit.api.MergeCommand cmd) {
        if (ffOption == null) {
            // will fall back on the config default
            return;
        }
        switch (ffOption) {
            case FAST_FORWARD:
                cmd.setFastForward(org.eclipse.jgit.api.MergeCommand.FastForwardMode.FF);
                break;
            case FAST_FORWARD_ONLY:
                cmd.setFastForward(org.eclipse.jgit.api.MergeCommand.FastForwardMode.FF_ONLY);
                break;
            case NO_FAST_FORWARD:
                cmd.setFastForward(org.eclipse.jgit.api.MergeCommand.FastForwardMode.NO_FF);
                break;
        }
    }
}
