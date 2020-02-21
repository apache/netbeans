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
package org.netbeans.modules.cnd.debugger.common2.utils;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;

/**
 *
 */
public class ProjectComboBoxSupport {

    public static void fillProjectsCombo(javax.swing.JComboBox comboBox, Project selectedProject) {
        if (selectedProject == null) {
            selectedProject = OpenProjects.getDefault().getMainProject();
        }
        for (Project proj : OpenProjects.getDefault().getOpenProjects()) {
            // include only cnd projects (see IZ 164690)
            if (proj.getLookup().lookup(ConfigurationDescriptorProvider.class) != null) {
                ProjectInformation pinfo = ProjectUtils.getInformation(proj);
                ProjectCBItem pi = new ProjectCBItem(pinfo);
                comboBox.addItem(pi);
                if (selectedProject != null && proj == selectedProject) {
                    comboBox.setSelectedItem(pi);
                }
            }
        }
    }

    public static class ProjectCBItem {

        private ProjectInformation pinfo;

        public ProjectCBItem(ProjectInformation pinfo) {
            this.pinfo = pinfo;
        }

        @Override
        public String toString() {
            return pinfo.getDisplayName();
        }

        public Project getProject() {
            return pinfo.getProject();
        }

        public ProjectInformation getProjectInformation() {
            return pinfo;
        }
    }
}
