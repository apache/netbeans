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
