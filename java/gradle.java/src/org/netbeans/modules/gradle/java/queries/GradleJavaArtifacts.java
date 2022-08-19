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
package org.netbeans.modules.gradle.java.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectActionContext;
import org.netbeans.modules.gradle.api.BuildPropertiesSupport;
import org.netbeans.modules.gradle.api.BuildPropertiesSupport.Property;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.GradleTask;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.modules.gradle.api.execute.RunConfig;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.ProjectArtifactsQuery;
import org.netbeans.modules.project.dependency.spi.ProjectArtifactsImplementation;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
public class GradleJavaArtifacts implements ProjectArtifactsImplementation<GradleJavaArtifacts.Result> {

    @Override
    public Result evaluate(ProjectArtifactsQuery.Filter query) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Project findProject(Result r) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public List<ArtifactSpec> findArtifacts(Result r) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Collection<ArtifactSpec> findExcludedArtifacts(Result r) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void handleChangeListener(Result r, ChangeListener l, boolean add) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean computeSupportsChanges(Result r) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    static class Result implements PropertyChangeListener {
        private final Project project;
        private final ProjectArtifactsQuery.Filter filter;
        private final NbGradleProject gradleProject;
        
        private final List<String> buildTasks;

        public Result(Project project, ProjectArtifactsQuery.Filter filter, NbGradleProject gradleProject) {
            this.project = project;
            this.filter = filter;
            this.gradleProject = gradleProject;
            
            String action = ActionProvider.COMMAND_BUILD;
            GradleExecConfiguration cfg = null;
            if (filter.getBuildContext() != null) {
                ProjectActionContext pac = filter.getBuildContext();
                if (pac.getProjectAction() != null) {
                    action = pac.getProjectAction();
                }
                if (pac.getConfiguration() != null) {
                    cfg = (GradleExecConfiguration)pac.getConfiguration();
                }
            }
            RunConfig rc = RunUtils.createRunConfig(project, action, "Searching for artifacts", Lookup.EMPTY, cfg, Collections.EMPTY_SET);
            buildTasks = new ArrayList<>(rc.getCommandLine().getTasks());
        }
        
        public List<ArtifactSpec> update() {
            GradleBaseProject gbp = GradleBaseProject.get(project);
            Set<String> allTaskNames = new HashSet<>();
            for (String taskName : buildTasks) {
                GradleTask gt = gbp.getTaskByName(taskName);
                for (GradleTask dep : gbp.getTaskPredecessors(gt)) {
                    allTaskNames.add(dep.getName());
                }
            }
            
            List<ArtifactSpec> result = new ArrayList<>();
            // if there's a JAR task, let's inspect the details; otherwise we don't have an artifact (?)
            if (allTaskNames.contains("jar")) {
                addJarArtifacts(gbp, result);
            }
            
            return result;
        }
        
        private void addJarArtifacts(GradleBaseProject gbp, List<ArtifactSpec> results) {
            String name = gbp.getName();
            String group = gbp.getGroup();
            String version = gbp.getVersion();
            String baseName = name;
            String filename;
            
            String dir;
            
            BuildPropertiesSupport props = BuildPropertiesSupport.get(project);
            Property p = props.findTaskProperty("jar", "archiveFileName");
            if (p != null && p.getStringValue() != null) {
                filename = p.getStringValue();
            } else {
                if (gbp.isVersionSpecified()) {
                    filename = String.format("%s-%s.jar", baseName, version);
                } else {
                    filename = baseName + ".jar";
                }
            }
            
            p = props.findTaskProperty("jar", "destinationDirectory");
            if (p != null && p.getStringValue() != null) {
                dir = p.getStringValue();
            }
            
        }
        

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
        }
    }
}
