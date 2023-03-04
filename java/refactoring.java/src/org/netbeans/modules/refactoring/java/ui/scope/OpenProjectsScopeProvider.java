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
package org.netbeans.modules.refactoring.java.ui.scope;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.modules.refactoring.spi.ui.ScopeProvider;
import org.netbeans.modules.refactoring.spi.ui.ScopeReference;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 */
@NbBundle.Messages("LBL_AllProjects=Open Projects")
@ScopeProvider.Registration(id = "all-projects", displayName = "#LBL_AllProjects", position = 100, iconBase = "org/netbeans/modules/refactoring/java/resources/all_projects.png")
@ScopeReference(path = "org-netbeans-modules-refactoring-java-ui-WhereUsedPanel")
public class OpenProjectsScopeProvider extends ScopeProvider {
    private Scope scope;

    @Override
    public boolean initialize(Lookup context, AtomicBoolean cancel) {
        Future<Project[]> openProjects = OpenProjects.getDefault().openProjects();
        
        Project[] projects;
        try {
            projects = openProjects.get();
        } catch (InterruptedException | ExecutionException ex) {
            return false;
        }
        
        if(projects == null || projects.length == 0) {
            return false;
        }

        Set<FileObject> srcRoots = new HashSet<>();
        
        for (Project project : projects) {
            ProjectInformation pi = ProjectUtils.getInformation(project);
            final SourceGroup[] sourceGroups = ProjectUtils.getSources(pi.getProject()).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            for (int i = 0; i < sourceGroups.length; i++) {
                srcRoots.add(sourceGroups[i].getRootFolder());
            }
        }
        if(srcRoots.isEmpty()) {
            return false;
        }
        scope = Scope.create(srcRoots, null, null);

        return true;
    }

    @Override
    public Scope getScope() {
        return scope;
    }
}
