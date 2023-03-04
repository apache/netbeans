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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.mylyn.util.BugtrackingCommand;

/**
 *
 * @author Tomas Stupka
 */
public class AddAttachmentCommand extends BugtrackingCommand {

    private final String id;
    private final BugzillaRepository repository;
    private final String comment;
    private final FileTaskAttachmentSource attachmentSource;
    private final TaskAttribute attAttribute;
    private final File file;
    private String stringValue;

    public AddAttachmentCommand(String id, BugzillaRepository repository, String comment, FileTaskAttachmentSource attachmentSource, File file, TaskAttribute attAttribute) {
        this.id = id;
        this.repository = repository;
        this.comment = comment;
        this.attachmentSource = attachmentSource;
        this.file = file;
        this.attAttribute = attAttribute;
    }

    @Override
    public void execute() throws CoreException, IOException, MalformedURLException {
        Bugzilla.getInstance().getClient(repository)
            .postAttachment(
                id,
                comment,
                attachmentSource,
                attAttribute,
                new NullProgressMonitor());
    }

    @Override
    public String toString() {
        if(stringValue == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("AddAttachmentCommand [repository=");                     // NOI18N
            sb.append(repository.getUrl());
            sb.append(",id=");                                                  // NOI18N
            sb.append(id);
            sb.append(",comment=");                                             // NOI18N
            sb.append(comment);
            sb.append(",file=");                                                // NOI18N
            sb.append(file.getAbsolutePath());
            sb.append(",desc=");                                                // NOI18N
            TaskAttribute ta = attAttribute.getMappedAttribute(TaskAttribute.ATTACHMENT_DESCRIPTION);
            sb.append(ta != null ? ta.getValue() : "");                         // NOI18N
            sb.append(",patch=");                                               // NOI18N
            ta = attAttribute.getMappedAttribute(TaskAttribute.ATTACHMENT_IS_PATCH);
            sb.append(ta != null ? ta.getValue() : "");                         // NOI18N
            sb.append(",contentType=");                                         // NOI18N
            ta = attAttribute.getMappedAttribute(TaskAttribute.ATTACHMENT_CONTENT_TYPE);
            sb.append(ta != null ? ta.getValue() : "");                         // NOI18N
            sb.append("]");                                                     // NOI18N
            stringValue = sb.toString();
        }
        return stringValue;
    }

}
