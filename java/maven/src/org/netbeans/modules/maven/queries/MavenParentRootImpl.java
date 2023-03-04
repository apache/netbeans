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
package org.netbeans.modules.maven.queries;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.project.ParentProjectProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.RootProjectProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author sdedic
 */
@ProjectServiceProvider(service = {ParentProjectProvider.class, RootProjectProvider.class}, projectType="org-netbeans-modules-maven")
public class MavenParentRootImpl implements ParentProjectProvider, RootProjectProvider {
    private final Project project;
    
    /**
     * Cached parent project, or {@link #NO_PROJECT} if none.
     */
    // @GuardedBy(this)
    private Reference<Project>  parent = new WeakReference<>(null);

    /**
     * Cached root project, in worst case this project itself.
     */
    // @GuardedBy(this)
    private Reference<Project>  root = new WeakReference<>(null);
    
    public MavenParentRootImpl(Project proj) {
        this.project = proj;
    }
    
    @Override
    public Project getPartentProject() {
        synchronized (this) {
            if (parent == NO_PROJECT) {
                return null;
            }
            Project fromCache = parent.get();
            if (fromCache != null) {
                return fromCache;
            }
        }
        Project found = findParentProject(project, project);
        synchronized (this) {
            parent = found == null ? NO_PROJECT : new WeakReference<>(found);
        }
        return found;
    }
    
    private static Project findParentProject(Project from, Project toFind) {
        FileObject dir = from.getProjectDirectory();
        FileObject findDir = toFind.getProjectDirectory();
        if (dir == null) {
            // ??
            return null;
        }
        FileObject parentDir = dir.getParent();
        if (parentDir == null) {
            return null;
        }
        Project parentProj = FileOwnerQuery.getOwner(parentDir);
        if (parentProj == null) {
            return null;
        }
        FileObject parentProjDir = parentProj.getProjectDirectory();
        NbMavenProject parentMaven = parentProj.getLookup().lookup(NbMavenProject.class);
        if (parentMaven == null) {
            return null;
        }
        // verify that the parent project lists the child as a module
        for (String s : parentMaven.getMavenProject().getModules()) {
            FileObject child = parentProjDir.getFileObject(s);
            if (findDir.equals(child)) {
                return parentProj;
            }
        }
        
        return findParentProject(parentProj, toFind);
    }

    @Override
    public Project getRootProject() {
        synchronized (this) {
            if (root == NO_PROJECT) {
                return null;
            }
            Project r = root.get();
            if (r != null) {
                return r;
            }
        }
        Project that = project;
        while (true) {
            Project parent = findParentProject(that, that);
            if (parent == null || parent == that) {
                break;
            }
            that = parent;
        }
        
        synchronized (this) {
            root = new WeakReference<>(that);
        }
        return that;
    }
    
    private static final WeakReference<Project> NO_PROJECT = new WeakReference<>(null);
}
