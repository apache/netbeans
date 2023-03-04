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

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.openide.util.Mutex.ExceptionAction;
import org.openide.util.MutexException;

/**
 *
 */
public final class UpgradableProject {

    private Project project;
    private EclipseProjectReference reference;
    private boolean initialized;
    private boolean createdFromEclipse;
    
    public UpgradableProject(Project project) {
        this.project = project;
    }
    
    public boolean isEclipseProjectReachable() {
        if (!isUpgradable()) {
            return false;
        }
        return getEclipseProjectReference().isEclipseProjectReachable();
    }
    
    public boolean isUpgradable() {
        return getEclipseProjectReference() != null;
    }
    
    public boolean isUpToDate(boolean deepTest) {
        assert isUpgradable() && isEclipseProjectReachable();
        return getEclipseProjectReference().isUpToDate(deepTest);
    }
    
    public void update(final List<String> importProblems) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new ExceptionAction<Void>() {

                public Void run() throws Exception {
                    getEclipseProjectReference().update(importProblems);
                    return null;
                }
            });
        } catch (MutexException ex) {
            IOException ioe = new IOException();
            ioe.initCause(ex.getCause());
            throw ioe;
        }
    }
    
    private @CheckForNull EclipseProjectReference getEclipseProjectReference() {
        if (!initialized) {
            reference = EclipseProjectReference.read(project);
            // cache data only if project is reachable
            initialized = reference == null ? true : reference.isEclipseProjectReachable();
            createdFromEclipse = reference != null;
        }
        return reference;
    }
    
    boolean isCreatedFromEclipse() {
        // cause init:
        getEclipseProjectReference();
        return createdFromEclipse;
    }
    
    /**
     * Shows UI to resolve location of eclipse project and workspace and returns 
     * true if reference was succesfully resolved.
     */
    public boolean updateBrokenEclipseReference(@NonNull Map<String,String> resolvedEntries) {
        EclipseProjectReference ref = getEclipseProjectReference();
        if (ref == null) {
            return true;
        }
        return UpdateEclipseReferencePanel.showEclipseReferenceResolver(ref, resolvedEntries);
    }

    File getStoredEclipseProjectFolder() {
        return getEclipseProjectReference().getFallbackEclipseProjectLocation();
    }
    
    File getStoredEclipseWorkspaceFolder() {
        return getEclipseProjectReference().getFallbackWorkspaceProjectLocation();
    }
    
    Workspace getWorkspace() {
        EclipseProject prj = getEclipseProject();
        return prj != null ? prj.getWorkspace() : null;
    }
    
    EclipseProject getEclipseProject() {
        return getEclipseProjectReference().getEclipseProject(false);
    }

    public Project getProject() {
        return project;
    }
    
}
