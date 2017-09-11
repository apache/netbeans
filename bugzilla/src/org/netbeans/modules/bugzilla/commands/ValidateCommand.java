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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugzilla.commands;

import java.io.IOException;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaClient;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.netbeans.modules.team.commons.LogUtils;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.mylyn.util.BugtrackingCommand;

/**
 *
 * @author Tomas Stupka
 */
public class ValidateCommand extends BugtrackingCommand {

    private final TaskRepository taskRepository;

    public ValidateCommand(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public void execute() throws CoreException {
        log();
        try {
            BugzillaClient client = Bugzilla.getInstance().getRepositoryConnector().getClientManager().getClient(taskRepository, new NullProgressMonitor());
            client.validate(new NullProgressMonitor());
        } catch (IOException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex); // XXX handle errors
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ValidateCommand [repository=");                              // NOI18N
        sb.append(taskRepository.getUrl());
        sb.append("]");                                                         // NOI18N
        return sb.toString();
    }

    String getUrl() {
        return taskRepository.getUrl();
    }
    
    String getUser() {
        return taskRepository.getCredentials(AuthenticationType.REPOSITORY).getUserName();
    }
    
    private void log() {
        Bugzilla.LOG.log(
            Level.INFO,
            "validating [{0},{1},{2},{3}]",                                     // NOI18N
            new Object[]{
                taskRepository.getUrl(),
                getCredentialsString(taskRepository.getCredentials(AuthenticationType.REPOSITORY)),
                getCredentialsString(taskRepository.getCredentials(AuthenticationType.HTTP)),
                getCredentialsString(taskRepository.getCredentials(AuthenticationType.PROXY))});
    }

    private String getCredentialsString(AuthenticationCredentials c) {
        if(c == null) {
            return "null, null";                                                            // NOI18N
        }
        return c.getUserName() + "," + LogUtils.getPasswordLog(c.getPassword().toCharArray());     // NOI18N
    }


}
