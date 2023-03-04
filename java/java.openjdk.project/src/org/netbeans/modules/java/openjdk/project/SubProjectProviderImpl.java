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
package org.netbeans.modules.java.openjdk.project;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.ChangeListener;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.openjdk.project.ModuleDescription.Dependency;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lahvac
 */
public class SubProjectProviderImpl implements SubprojectProvider{

    private final JDKProject project;

    public SubProjectProviderImpl(JDKProject project) {
        this.project = project;
    }

    @Override
    public Set<? extends Project> getSubprojects() {
        if (project.currentModule == null) return Collections.emptySet();
        Set<Project> subprojects = new HashSet<>();
        for (Dependency depend : project.currentModule.depend) {
            FileObject dependRoot = project.moduleRepository.findModuleRoot(depend.moduleName);

            if (dependRoot != null) {
                try {
                    subprojects.add(ProjectManager.getDefault().findProject(dependRoot));
                } catch (IOException | IllegalArgumentException ex) {
                    Logger.getLogger(SubProjectProviderImpl.class.getName()).log(Level.FINE, null, ex);
                }
            }
        }

        return subprojects;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
    }

}
