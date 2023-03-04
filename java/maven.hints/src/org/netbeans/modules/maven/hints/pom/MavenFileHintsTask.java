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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.hints.pom.spi.POMErrorFixProvider;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.filesystems.FileObject;

/**
 * A ParserResult task that should be run on POM file changes. It validates the
 * POM, reports possible errors. If no errors, it runs {@link POMErrorFixProvider}s
 * to provide additional hints for Maven's POM model.
 *
 * @author sdedic
 */
public class MavenFileHintsTask extends ParserResultTask<MavenResult> {

    @Override
    public void run(MavenResult result, SchedulerEvent event) {
        FileObject fo = result.getPomFile();
        Project p = FileOwnerQuery.getOwner(fo);
        if (fo == null || p == null || p.getProjectDirectory() != fo.getParent()) {
            // ?? pom file ought to form a project!
            return;
        }
        List<ErrorDescription> errors = PomModelUtils.findHints(result.getProjectModel(), p);
        HintsController.setErrors(fo, PomModelUtils.LAYER_POM, errors);
    }
    
    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
        // 
    }

    @MimeRegistration(mimeType = "text/x-maven-pom+xml", service = TaskFactory.class)
    public static class F extends TaskFactory {
        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            return Arrays.asList(
                    new MavenFileHintsTask(),
                    new MavenSelectionHintsTask()
            );
        }
    }
    
}
