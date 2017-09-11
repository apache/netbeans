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

import java.nio.file.Path;
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
