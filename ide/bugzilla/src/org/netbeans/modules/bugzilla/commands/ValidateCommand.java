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
