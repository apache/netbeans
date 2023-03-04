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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.util.FileUtils;
import org.netbeans.modules.mylyn.util.BugtrackingCommand;

/**
 *
 * @author Tomas Stupka
 */
public class GetAttachmentCommand extends BugtrackingCommand {
    private BugzillaRepository repository;
    private final String id;
    private final OutputStream os;
    private String stringValue;

    public GetAttachmentCommand(BugzillaRepository repository, String id, OutputStream os) {
        this.repository = repository;
        this.id = id;
        this.os = os;
    }

    @Override
    public void execute() throws CoreException, IOException, MalformedURLException {
        InputStream is = Bugzilla.getInstance().getClient(repository).getAttachmentData(id, new NullProgressMonitor());
        FileUtils.copyStream(is, os);
    }

    @Override
    public String toString() {
        if(stringValue == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("GetAttachmentCommand [repository=");                     // NOI18N
            sb.append(repository.getUrl());
            sb.append(",attachmentID=");                                        // NOI18N
            sb.append(id);
            sb.append("]");                                                     // NOI18N
            stringValue = sb.toString();
        }
        return stringValue;
    }

}
