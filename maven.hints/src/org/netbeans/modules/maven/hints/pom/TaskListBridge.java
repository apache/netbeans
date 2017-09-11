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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
                List<ErrorDescription> errs = StatusProvider.StatusProviderImpl.findHints(model, prj, -1, -1, -1);
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
