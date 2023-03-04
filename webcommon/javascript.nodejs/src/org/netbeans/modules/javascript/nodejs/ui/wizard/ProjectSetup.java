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
package org.netbeans.modules.javascript.nodejs.ui.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.javascript.nodejs.platform.NodeJsPlatformProvider;
import org.netbeans.modules.javascript.nodejs.platform.NodeJsSupport;
import org.netbeans.modules.javascript.nodejs.ui.customizer.NodeJsRunPanel;

final class ProjectSetup implements PropertyChangeListener {

    final OpenProjects openProjects = OpenProjects.getDefault();
    private final Project project;


    private ProjectSetup(Project project) {
        assert project != null;
        this.project = project;
    }

    public static void setupRun(Project project) {
        ProjectSetup setupProject = new ProjectSetup(project);
        setupProject.openProjects.addPropertyChangeListener(setupProject);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
            if (Arrays.asList(openProjects.getOpenProjects()).contains(project)) {
                openProjects.removePropertyChangeListener(this);
                NodeJsSupport nodeJsSupport = NodeJsSupport.forProject(project);
                nodeJsSupport.getPreferences().setRunEnabled(true);
                nodeJsSupport.firePropertyChanged(NodeJsPlatformProvider.PROP_RUN_CONFIGURATION, null, NodeJsRunPanel.IDENTIFIER);
            }
        }
    }

}
