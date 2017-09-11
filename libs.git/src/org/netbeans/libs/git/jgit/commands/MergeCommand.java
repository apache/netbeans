/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
            ref = repository.getRef(revision);
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
