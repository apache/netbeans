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

package org.netbeans.modules.project.ui.test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Iterator;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Mutex;

/**
 * Helper class for working with projects.
 */
public class ProjectSupport {

    private ProjectSupport() {}

    /** Opens project in specified directory.
     * @param projectDir a directory with project to open
     * @return Project instance of opened project
     */
    public static Object openProject(File projectDir) {
        final ProjectListListener listener = new ProjectListListener();
        try {
            // open project
            final Project project = OpenProjectList.fileToProject(projectDir);
            if(project == null) {
                ErrorManager.getDefault().log(ErrorManager.USER, "Project not found: "+projectDir);
                return null;
            }
            // posting the to AWT event thread
            Mutex.EVENT.writeAccess(new Runnable() {
                @Override
                public void run() {
                    OpenProjectList.getDefault().addPropertyChangeListener(listener);
                    OpenProjectList.getDefault().open(project);
                }
            });
            // WAIT PROJECT OPEN - start
            // We need to wait until project is open and then we can start to
            // wait when scanning finishes. If we don't wait, scanning is started
            // too early and finishes immediatelly.
            Thread waitThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!listener.projectListChanged) {
                        try {
                            Thread.sleep(50);
                        } catch (Exception e) {
                            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
                        }
                    }
                }
            });
            waitThread.start();
            try {
                waitThread.join(60000L);  // wait 1 minute at the most
            } catch (InterruptedException iex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, iex);
            }
            if (waitThread.isAlive()) {
                // time-out expired, project not opened -> interrupt the wait thread
                ErrorManager.getDefault().log(ErrorManager.USER, "Project not opened in 60 second.");
                waitThread.interrupt();
                return null;
            }
            // WAIT PROJECT OPEN - end
            // wait until metadata scanning is finished
            try {
                Class.forName("org.netbeans.api.java.source.SourceUtils", true, Thread.currentThread().getContextClassLoader()).
                        getMethod("waitScanFinished").invoke(null);
            } catch (ClassNotFoundException x) {
                System.err.println("Warning: org.netbeans.api.java.source.SourceUtils could not be found, will not wait for scan to finish");
            }
            return project;
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            return null;
        } finally {
            OpenProjectList.getDefault().removePropertyChangeListener(listener);
        }
    }
    
    /** Closes project with system or display name equals to given name.
     * It also closes all files open in editor. If a file is modified, all
     * changes are discarded.
     * @param name system or display name of project to be closed.
     * @return true if project is closed, false otherwise (i.e. project was
     * not found).
     */
    public static boolean closeProject(String name) {
        Project[] projects = OpenProjectList.getDefault().getOpenProjects();
        for(int i=0;i<projects.length;i++) {
            final Project project = projects[i];
            if(ProjectUtils.getInformation(project).getDisplayName().equals(name) ||
                    ProjectUtils.getInformation(project).getName().equals(name)) {
                final ProjectListListener listener = new ProjectListListener();
                // posting the to AWT event thread
                Mutex.EVENT.writeAccess(new Runnable() {
                    @Override
                    public void run() {
                        discardChanges(project);
                        OpenProjectList.getDefault().addPropertyChangeListener(listener);
                        OpenProjectList.getDefault().close(new Project[] {project}, true);
                    }
                });
                // WAIT PROJECT CLOSED - start
                Thread waitThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (!listener.projectListChanged) {
                            try {
                                Thread.sleep(50);
                            }
                            catch (Exception e) {
                                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
                            }
                        }
                    }
                });
                waitThread.start();
                try {
                    waitThread.join(60000L);  // wait 1 minute at the most
                } catch (InterruptedException iex) {
                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, iex);
                }
                if (waitThread.isAlive()) {
                    // time-out expired, project not opened -> interrupt the wait thread
                    ErrorManager.getDefault().log(ErrorManager.USER, "Project not closed in 60 second.");
                    waitThread.interrupt();
                    return false;
                }
                // WAIT PROJECT CLOSED - end
                return true;
            }
        }
        // project not found
        return false;
    }
    
    /** Discards all changes in modified files of given project. */
    private static void discardChanges(Project project) {
        // discard all changes in modified files
        Iterator iter = DataObject.getRegistry().getModifiedSet().iterator();
        while (iter.hasNext()) {
            DataObject dobj = (DataObject) iter.next();
            if (dobj != null) {
                FileObject fobj = dobj.getPrimaryFile();
                Project owner = FileOwnerQuery.getOwner(fobj);
                if(owner == project) {
                    dobj.setModified(false);
                }
            }
        }
    }
    
    /** Opens project on specified path.
     * @param projectPath path to a directory with project to open
     * @return Project instance of opened project
     */
    public static Object openProject(String projectPath) {
        return openProject(new File(projectPath));
    }
    
    /** Listener for project open. */
    static class ProjectListListener implements PropertyChangeListener {
        public volatile boolean projectListChanged = false;
        
        /** Listen for property which changes when project is hopefully opened. */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(OpenProjectList.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
                projectListChanged = true;
            }
        }
    }
}
