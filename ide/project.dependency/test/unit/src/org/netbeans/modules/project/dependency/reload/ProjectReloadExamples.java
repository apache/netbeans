/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.project.dependency.reload;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.dependency.DependencyChange;
import org.netbeans.modules.project.dependency.DependencyChangeException;
import org.netbeans.modules.project.dependency.ProjectDependencies;
import org.netbeans.modules.project.dependency.ProjectOperationException;
import org.netbeans.modules.project.dependency.ProjectReload;
import org.netbeans.modules.project.dependency.ProjectReload.ProjectState;
import org.netbeans.modules.project.dependency.ProjectReload.Quality;
import org.netbeans.modules.project.dependency.ProjectReload.StateRequest;

/**
 *
 * @author sdedic
 */
public class ProjectReloadExamples {
    private Project project;
    
    public void exampleGetProjectState() {
        // @start region="getProjectStateExample"
        ProjectState st = ProjectReload.getProjectState(project);
        if (st.isConsistent()) {
            // the project is still consistent with on-disk content, no files were edited etc
        }
        if (st.getQuality().isAtLeast(ProjectReload.Quality.LOADED)) {
            // the project data is not broken, but maybe some libraries are not yet downloaded - see Quality javadoc
        }
        if (!st.getEditedFiles().isEmpty()) {
            // need to check or save files
        }
        if (!st.getChangedFiles().isEmpty()) {
            // some files are changed
        }
        st.addChangeListener((e) -> {
            if (!st.isValid()) {
                // time to reload :)
            }
        });
        // @end region="getProjectStateExample"
    }
    
    public void exampelRunOperationWithProject() {
        DependencyChange change = null;
        // @start region="withProjectStateExample"
        CompletableFuture<?> f = ProjectReload.withProjectState(project, 
            // load new state, if files have been changed
            StateRequest.refresh().
                // save files being edited
                saveModifications().
                // require that externals are resolved / downloaded
                toQuality(Quality.RESOLVED)
            ).thenAccept(st -> {
                try {
                    ProjectDependencies.modifyDependencies(project, change);
                } catch (DependencyChangeException ex) {
                    // handle issues
                } catch (ProjectOperationException ex) {
                    // handle generic issues
                }
        }).exceptionally(t -> {
            if (t instanceof CancellationException) {
                // the operation was cancelled
            }
            if (t instanceof ProjectOperationException) {
                ProjectOperationException ex = (ProjectOperationException)t;
                switch (ex.getState()) {
                    case UNSUPPORTED:
                        // the project reload is not supported
                        break;
                    case BROKEN:
                        // project did not reach the desired quality
                        break;
                    case OUT_OF_SYNC:
                        // project files are out of sync, e.g. the user did not confirm
                        // save of edited files
                        break;
                    case OFFLINE:
                        // online operation was necessary, but offline mode was requested
                }
            }
            return null;
        });
        
        // we may request to cancel our loading task and subsequent operations. It may stop loading the project, or simply
        // ignore the request.
        f.cancel(true);
        // @end region="withProjectStateExample"
    }
}
