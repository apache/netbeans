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

package org.netbeans.modules.git.client;

import java.util.logging.Level;
import javax.swing.JButton;
import org.netbeans.libs.git.GitException;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.ui.repository.remote.RemoteRepository;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public class GitClientExceptionHandler {
    private final boolean handleAuthenticationIssues;
    private final GitClient client;

    public GitClientExceptionHandler (GitClient client, boolean handleAuthenticationIssues) {
        this.client = client;
        this.handleAuthenticationIssues = handleAuthenticationIssues;
    }

    boolean handleException (Exception ex) {
        boolean handled = false;
        if (handleAuthenticationIssues && ex instanceof GitException.AuthorizationException) {
            return handleException((GitException.AuthorizationException) ex);
        }
        return handled;
    }
    
    private boolean handleException (GitException.AuthorizationException ex) {
        boolean confirmed = false;
        String repositoryUrl = ex.getRepositoryUrl();
        if (repositoryUrl == null || repositoryUrl.trim().isEmpty()) {
            Git.LOG.log(Level.INFO, "empty repository URL", ex); //NOI18N
        }
        if (RemoteRepository.updateFor(repositoryUrl)) {
            client.setCallback(new CredentialsCallback());
            confirmed = true;
        }
        return confirmed;
    }

    public static void notifyException (Exception ex, boolean annotate) {
        if(isCancelledAction(ex)) {
            return;
        }
        Git.LOG.log(Level.INFO, ex.getMessage(), ex);
        if( annotate ) {
            String msg = getMessage(ex);
            annotate(msg);
        }
    }

    public static void annotate (String msg) {
        CommandReport report = new CommandReport(NbBundle.getMessage(GitClientExceptionHandler.class, "MSG_SubversionCommandError"), msg); //NOI18N
        JButton ok = new JButton(NbBundle.getMessage(GitClientExceptionHandler.class, "CTL_CommandReport_OK")); //NOI18N
        NotifyDescriptor descriptor = new NotifyDescriptor(
                report,
                NbBundle.getMessage(GitClientExceptionHandler.class, "MSG_CommandFailed_Title"), //NOI18N
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.ERROR_MESSAGE,
                new Object [] { ok },
                ok);
        DialogDisplayer.getDefault().notify(descriptor);
    }
    
    public static boolean isCancelledAction (final Exception ex) {
        Throwable sourceException = ex;
        while (sourceException != null && !(sourceException instanceof GitCanceledException)) {
            sourceException = sourceException.getCause();
        }
        return sourceException instanceof GitCanceledException;
    }

    private static String getMessage (final Exception ex) {
        Throwable cause = ex;
        String message = cause.getLocalizedMessage();
        while (message == null && cause != null) {
            message = cause.getLocalizedMessage();
            cause = cause.getCause();
        }
        return message == null ? "" : message; //NOI18N
    }
}
