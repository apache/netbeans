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
package org.netbeans.modules.maven.hints.pom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class TaskListBridge extends FileTaskScanner {
    private static final String TASKLIST_ERROR = "nb-tasklist-error"; //NOI18N
    private static final String TASKLIST_WARNING = "nb-tasklist-warning"; //NOI18N

    public TaskListBridge() {
        super(NbBundle.getMessage(TaskListBridge.class, "LBL_TaskList_DisplayName"),
                NbBundle.getMessage(TaskListBridge.class, "LBL_TaskList_Desc"),
                null);
    }

    @Override
    public List<? extends Task> scan(FileObject resource) {
        if (Constants.POM_MIME_TYPE.equals(resource.getMIMEType()) //NOI18N
                && "pom.xml".equals(resource.getNameExt())) { //NOI18N
            Project prj = FileOwnerQuery.getOwner(resource);
            if (prj != null && prj.getLookup().lookup(NbMavenProject.class) != null) {
                ModelSource ms = Utilities.createModelSource(resource);
                POMModel model = POMModelFactory.getDefault().getModel(ms);
                model.setAutoSyncActive(false);
                List<ErrorDescription> errs = PomModelUtils.findHints(model, prj);
                List<Task> tasks = new ArrayList<Task>();

                for (ErrorDescription error : errs) {
                    try {
                        Task task = Task.create(resource,
                                severityToTaskListString(error.getSeverity()),
                                error.getDescription(),
                                error.getRange().getBegin().getLine() + 1);

                        tasks.add(task);
                    } catch (IOException e) {
                        Logger.getLogger(TaskListBridge.class.getName()).
                                log(Level.INFO, "Error while converting errors to tasklist", e);
                    }
                }
                return tasks;
            }
        }
        return Collections.<Task>emptyList();
    }

    @Override
    public void attach(Callback callback) {
        //noop
    }

    private static String severityToTaskListString(Severity severity) {
        if (severity == Severity.ERROR){
            return TASKLIST_ERROR;
        }
        return TASKLIST_WARNING;
    }

}
