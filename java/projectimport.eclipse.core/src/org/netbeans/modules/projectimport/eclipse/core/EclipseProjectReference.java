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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectImportModel;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeFactory;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeUpdater;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 * Represents reference to Eclipse project which consist of eclipse project location,
 * eclipse workspace location, eclipse files timestamp and key identifying relevant 
 * import data. File references are stored relative if collocated. If differnt user
 * opens NetBeans project and Eclipse reference cannot be resolved then UI asking 
 * for eclipse project location and eclipse workspace location is shown. These
 * are stored in NbPreferences in userdir for now.
 */
public class EclipseProjectReference {

    private Project project;
    private final File eclipseProjectLocation;
    private final File eclipseWorkspaceLocation;
    private long timestamp;
    private String key;
    
    private boolean initialized;
    private EclipseProject eclipseProject;
    private ProjectImportModel importModel;
    
    private static final String PROJECT_PREFIX = "project-"; //NOI18N
    private static final String WORKSPACE_PREFIX = "workspace-"; //NOI18N
    
    public EclipseProjectReference(Project project, String eclipseProjectLocation, String eclipseWorkspaceLocation, long timestamp, String key) {
        this.eclipseProjectLocation = PropertyUtils.resolveFile(FileUtil.toFile(project.getProjectDirectory()), eclipseProjectLocation);
        if (eclipseWorkspaceLocation != null) {
            this.eclipseWorkspaceLocation = PropertyUtils.resolveFile(FileUtil.toFile(project.getProjectDirectory()), eclipseWorkspaceLocation);
        } else {
            this.eclipseWorkspaceLocation = null;
        }
        this.timestamp = timestamp;
        this.key = key;
        this.project = project;
    }

    Project getProject() {
        return project;
    }
    
    public @NonNull File getEclipseProjectLocation() {
        return eclipseProjectLocation;
    }

    public @CheckForNull File getEclipseWorkspaceLocation() {
        return eclipseWorkspaceLocation;
    }
    
    File getFallbackEclipseProjectLocation() {
        String path = getPreferences().get(PROJECT_PREFIX+getEclipseProjectLocation().getPath(), null);
        if (path != null) {
            return new File(path);
        }
        return getEclipseProjectLocation();
    }

    File getFallbackWorkspaceProjectLocation() {
        if (eclipseWorkspaceLocation == null) {
            return null;
        }
        String path = getPreferences().get(WORKSPACE_PREFIX+getEclipseWorkspaceLocation().getPath(), null);
        if (path != null) {
            return FileUtil.normalizeFile(new File(path));
        }
        return getEclipseWorkspaceLocation();
    }

    void updateReference(String eclipseLocation, String eclipseWorkspace) {
        if (eclipseLocation != null) {
            getPreferences().put(PROJECT_PREFIX+getEclipseProjectLocation().getPath(), eclipseLocation); //NOI18N
        }
        if (eclipseWorkspace != null) {
            getPreferences().put(WORKSPACE_PREFIX+getEclipseWorkspaceLocation().getPath(), eclipseWorkspace); //NOI18N
        }
    }
    
    private static Preferences getPreferences() {
        return NbPreferences.forModule(EclipseProjectReference.class);
    }

    public static @CheckForNull EclipseProjectReference read(Project project) {
        // XXX using shared prefs is incorrect if an absolute path was stored!
        Preferences prefs = ProjectUtils.getPreferences(project, EclipseProjectReference.class, true);
        String projectLoc = prefs.get("project", null); //NOI18N
        if (projectLoc == null) {
            return null;
        }
        return new EclipseProjectReference(project, projectLoc, prefs.get("workspace", null), Long.parseLong(prefs.get("timestamp", null)), prefs.get("key", null)); //NOI18N
    }
    
    public static void write(Project project, EclipseProjectReference ref) {
        Preferences prefs = ProjectUtils.getPreferences(project, EclipseProjectReference.class, true);
        File baseDir = FileUtil.toFile(project.getProjectDirectory());
        if (CollocationQuery.areCollocated(baseDir, ref.eclipseProjectLocation)) {
            prefs.put("project", PropertyUtils.relativizeFile(baseDir, ref.eclipseProjectLocation)); //NOI18N
        } else {
            prefs.put("project", ref.eclipseProjectLocation.getPath()); //NOI18N
        }
        if (ref.eclipseWorkspaceLocation != null) {
            if (CollocationQuery.areCollocated(baseDir, ref.eclipseWorkspaceLocation)) {
                prefs.put("workspace", PropertyUtils.relativizeFile(baseDir, ref.eclipseWorkspaceLocation)); //NOI18N
            } else {
                prefs.put("workspace", ref.eclipseWorkspaceLocation.getPath()); //NOI18N
            }
        }
        prefs.put("timestamp", Long.toString(ref.getCurrentTimestamp())); //NOI18N
        prefs.put("key", ref.key); //NOI18N
    }

    /**
     * @param deepTest if false only file timestamps are compared; if false 
     *  project classpath is compared
     */
    public boolean isUpToDate(boolean deepTest) {
        if (getCurrentTimestamp() > timestamp) {
            return false;
        } else if (!deepTest) {
            return true;
        }
        EclipseProject ep = getEclipseProject(true);
        if (ep == null) {
            // an exception was thrown; pretend proj is uptodate
            return true;
        }
        if (!(ep.getProjectTypeFactory() instanceof ProjectTypeUpdater)) {
            assert false : "project with <eclipse> data in project.xml is upgradable: "+ //NOI18N
                    project.getProjectDirectory()+" " +ep.getProjectTypeFactory().getClass().getName(); //NOI18N
        }
        ProjectTypeUpdater updater = (ProjectTypeUpdater)ep.getProjectTypeFactory();
        return key.equals(updater.calculateKey(importModel));
    }

    void update(List<String> importProblems) throws IOException {
        EclipseProject ep = getEclipseProject(true);
        if (ep == null) {
            // an exception was thrown; pretend proj is uptodate
            return;
        }
        ProjectTypeFactory factory = ep.getProjectTypeFactory();
        if (!(factory instanceof ProjectTypeUpdater)) {
            assert false : "project with <eclipse> data in project.xml is upgradable"; //NOI18N
            return;
        }
        
        // resolve classpath containers
        ep.resolveContainers(importProblems, true);
        
        // create ENV variables in build.properties
        ep.setupEnvironmentVariables(importProblems);
        
        // perform update
        key = ((ProjectTypeUpdater) factory).update(project, importModel, key, importProblems);
        write(project, this);
    }

    private long getCurrentTimestamp() {
        // use directly Files:
        File dotClasspath = new File(getFallbackEclipseProjectLocation(), ".classpath"); //NOI18N
        File dotProject = new File(getFallbackEclipseProjectLocation(), ".project"); //NOI18N
        return Math.max(dotClasspath.lastModified(), dotProject.lastModified());
    }
    
    boolean isEclipseProjectReachable() {
        boolean b = EclipseUtils.isRegularProject(eclipseProjectLocation) &&
                (eclipseWorkspaceLocation == null || 
                 (eclipseWorkspaceLocation != null && EclipseUtils.isRegularWorkSpace(eclipseWorkspaceLocation)));
        if (b) {
            // if project/workspace are reachable remove fallback properties
            getPreferences().remove(PROJECT_PREFIX+eclipseProjectLocation.getPath());
            if (eclipseWorkspaceLocation != null) {
                getPreferences().remove(WORKSPACE_PREFIX+eclipseWorkspaceLocation.getPath());
            }
            return true;
        }
        return EclipseUtils.isRegularProject(getFallbackEclipseProjectLocation()) &&
                (eclipseWorkspaceLocation == null ||
                 (eclipseWorkspaceLocation != null && EclipseUtils.isRegularWorkSpace(getFallbackWorkspaceProjectLocation())));
    }

    public EclipseProject getEclipseProject(boolean forceReload) {
        if (forceReload || !initialized) {
            try {
                EclipseProject ep = null;
                if (getFallbackWorkspaceProjectLocation() != null) {
                    Workspace w = WorkspaceFactory.getInstance().load(getFallbackWorkspaceProjectLocation());
                    ep = w.getProjectByProjectDir(getFallbackEclipseProjectLocation());
                }
                if (ep == null) {
                    ep = ProjectFactory.getInstance().load(getFallbackEclipseProjectLocation(), getFallbackWorkspaceProjectLocation());
                }
                eclipseProject = ep;
            } catch (ProjectImporterException ex) {
                Exceptions.printStackTrace(ex);
                eclipseProject = null;
                initialized = true;
                return null;
            }
            File f = FileUtil.toFile(project.getProjectDirectory());
            importModel = new ProjectImportModel(eclipseProject, f, 
                    JavaPlatformSupport.getJavaPlatformSupport().getJavaPlatform(eclipseProject, new ArrayList<String>()), Collections.<Project>emptyList());
            initialized = true;
        }
        return eclipseProject;
    }
}
