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
import java.net.MalformedURLException;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.internal.bugzilla.core.RepositoryConfiguration;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.mylyn.util.BugtrackingCommand;

/**
 *
 * @author Tomas Stupka
 */
public class GetConfigurationCommand extends BugtrackingCommand {

    private final boolean forceRefresh;
    private BugzillaRepository repository;
    private RepositoryConfiguration conf;

    public GetConfigurationCommand(boolean forceRefresh, BugzillaRepository repository) {
        this.forceRefresh = forceRefresh;
        this.repository = repository;
    }

    @Override
    public void execute() throws CoreException, IOException, MalformedURLException {
        boolean refresh = forceRefresh;
        String b = System.getProperty("org.netbeans.modules.bugzilla.persistentRepositoryConfiguration", "false"); // NOI18N
        if("true".equals(b)) {                                                  // NOI18N
            refresh = true;
        }
        Bugzilla.LOG.log(Level.FINE, " Refresh bugzilla configuration [{0}, forceRefresh={1}]", new Object[]{repository.getUrl(), refresh}); // NOI18N
        conf = Bugzilla.getInstance().getRepositoryConfiguration(repository, refresh);
    }

    public RepositoryConfiguration getConf() {
        return hasFailed() ? null : conf;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GetConfigurationCommand [repository=");                      // NOI18N
        sb.append(repository.getUrl());
        sb.append("]");                                                         // NOI18N
        return sb.toString();
    }

}
