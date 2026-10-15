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

package org.netbeans.modules.team.ide;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.team.ide.spi.IDEProject;
import org.netbeans.modules.team.ide.spi.ProjectServices;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.team.ide.spi.ProjectServices.class)
public class ProjectServicesImpl implements ProjectServices {

    /** Listener on OpenProjects, bridging to the registered IDEProject.OpenListener listeners. */
    private static ProjectOpenListener projectOpenListener;

    /** Registered listeners from the UI of team project sources. Notified when
     * new projects get opened. */
    private static List<Reference<IDEProject.OpenListener>> ideProjectOpenListeners;

    @Override
    public FileObject[] getOpenProjectsDirectories() {
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        if (openProjects.length == 0) {
            return null;
        }
        FileObject[] directories = new FileObject[openProjects.length];
        for (int i = 0; i < openProjects.length; i++) {
            Project project = openProjects[i];
            directories[i] = project.getProjectDirectory();
        }
        return directories;
    }

    @Override
    public FileObject getMainProjectDirectory() {
        Project p = OpenProjects.getDefault().getMainProject();
        return p != null ? p.getProjectDirectory() : null;
    }

    @Override
    public FileObject getFileOwnerDirectory(FileObject fileObject) {
        Project project = FileOwnerQuery.getOwner(fileObject);
        return project != null ? project.getProjectDirectory() : null;
    }

    @Override
    public FileObject[] getCurrentSelection() {
        Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
        if(nodes == null) {
            return null;
        }        
        List<FileObject> ret = new ArrayList<FileObject>();
        for(Node node : nodes) {
            Lookup nodeLookup = node.getLookup();
            Collection<? extends Project> projects = nodeLookup.lookupAll(Project.class);
            if(projects != null && !projects.isEmpty()) {
                for (Project project : projects) {
                    ret.add(project.getProjectDirectory());
                }
            } else {
                DataObject dataObj = nodeLookup.lookup(DataObject.class);
                if (dataObj != null) {
                    FileObject fileObj = dataObj.getPrimaryFile();
                    if (fileObj != null) {
                        ret.add(fileObj);
                    }
                }
            }
        }
        return ret.toArray(new FileObject[0]);
    }

    public FileObject[] getProjectDirectories(Lookup lookup) {
        Collection<? extends Project> projects = lookup.lookupAll(Project.class);
        if(projects == null) {
            return null;
        }
        List<FileObject> ret = new ArrayList<FileObject>();
        for (Project project : projects) {
            ret.add(project.getProjectDirectory());
        }
        return ret.toArray(new FileObject[0]);
    }

    @Override
    public <T> T runAfterProjectOpenFinished(Callable<T> operation) throws Exception {
        // wait until projects are opened
        OpenProjects.getDefault().openProjects().get();
        return operation.call();
    }

    @Override
    public boolean openProject(URL url) {
        Project p = getProject(url);
        if (p == null) {
            return false;
        }
        OpenProjects.getDefault().open(new Project[] { p }, false);
        TopComponent projectsTC = WindowManager.getDefault().findTopComponent("projectTabLogical_tc"); // NOI18N
        projectsTC.requestActive();
        ExplorerManager em = ((ExplorerManager.Provider) projectsTC).getExplorerManager();

        Node root = em.getRootContext();
        Node projNode = null;
        for (Node n : root.getChildren().getNodes()) {
            Project prj = n.getLookup().lookup(Project.class);
            if (prj != null && prj.getProjectDirectory().equals(p.getProjectDirectory())) {
                projNode = n;
                break;
            }
        }
        if (projNode == null) { // fallback
            projNode = root.getChildren().findChild(ProjectUtils.getInformation(p).getName());
        }
        if (projNode != null) {
            try {
                em.setSelectedNodes(new Node[] { projNode });
            } catch (Exception ignore) { // may ignore it
            }
        }
        return true;
    }

    @Override
    public void openOtherProject(File workingDir) {
        chooseAndOpenProjects(workingDir, true);
    }

    @Override
    public File[] chooseProjects(File workingDir) {
        return chooseAndOpenProjects(workingDir, false);
    }

    private File[] chooseAndOpenProjects(File workingDir, boolean open) {
        if (workingDir != null) {
            ProjectChooser.setProjectsFolder(workingDir);
        }
        JFileChooser chooser = ProjectChooser.projectChooser();
        if (workingDir != null) {
            chooser.setCurrentDirectory(workingDir);
        }
        chooser.setMultiSelectionEnabled(true);

        File[] projectDirs;
        int option = chooser.showOpenDialog(WindowManager.getDefault().getMainWindow());
        if (option == JFileChooser.APPROVE_OPTION) {
            if (chooser.isMultiSelectionEnabled()) {
                projectDirs = chooser.getSelectedFiles();
            } else {
                projectDirs = new File[] { chooser.getSelectedFile() };
            }

            if (open) {
                ArrayList<Project> projects = new ArrayList<Project>(projectDirs.length);
                for (File d : projectDirs) {
                    try {
                        Project p = ProjectManager.getDefault().findProject(FileUtil.toFileObject(d));
                        if (p != null) {
                            projects.add(p);
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IllegalArgumentException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

                if (!projects.isEmpty()) {
                    OpenProjects.getDefault().open(projects.toArray(new Project[0]), false);
                }
                WindowManager.getDefault().findTopComponent("projectTabLogical_tc").requestActive(); // NOI18N
            }
        } else {
            projectDirs = new File[0];
        }
        return projectDirs;
    }

    @Override
    public void reopenProjectsFromNewLocation(File[] oldLocations, File[] newLocations) {
        List<Project> projectsToClose = new ArrayList<Project>();
        List<Project> projectsToOpen = new ArrayList<Project>();
        ProjectManager.getDefault().clearNonProjectCache();
        for (int i=0; i < oldLocations.length; i++) {
            Project prj = FileOwnerQuery.getOwner(FileUtil.toFileObject(oldLocations[i]));
            if (prj != null) {
                projectsToClose.add(prj);
            }
        }
        for (int i=0; i < newLocations.length; i++) {
            Project prj = FileOwnerQuery.getOwner(FileUtil.toFileObject(newLocations[i]));
            if (prj != null) {
                projectsToOpen.add(prj);
            }
        }
        projectsToClose.remove(null);
        projectsToOpen.remove(null);
        OpenProjects.getDefault().close(projectsToClose.toArray(new Project[0]));
        OpenProjects.getDefault().open(projectsToOpen.toArray(new Project[0]), false);
    }

    @Override
    public void createNewProject(File workingDir) {
        Action newProjectAction = CommonProjectActions.newProjectAction();
        if (newProjectAction != null) {
            ProjectChooser.setProjectsFolder(workingDir);
            newProjectAction.actionPerformed(new ActionEvent(this,
                    ActionEvent.ACTION_PERFORMED, "command")); // NOI18N
        }
    }

    @Override
    public IDEProject getIDEProject(URL url) {
        Project p = getProject(url);
        return p != null ? createIDEProject(p) : null;
    }

    @Override
    public IDEProject[] getOpenProjects() {
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        IDEProject[] ideProjects = new IDEProject[openProjects.length];
        for (int i=0; i < openProjects.length; i++) {
            ideProjects[i] = createIDEProject(openProjects[i]);
        }
        return ideProjects;
    }

    private static Project getProject(URL url) {
        try {
            return FileOwnerQuery.getOwner(url.toURI());
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    private static IDEProject createIDEProject(Project p) {
        ProjectInformation pi = ProjectUtils.getInformation(p);
        return new NbProject(pi.getDisplayName(), pi.getIcon(), p.getProjectDirectory().toURL());
    }

    @Override
    public synchronized void addProjectOpenListener(IDEProject.OpenListener listener) {
        if (ideProjectOpenListeners == null) {
            ideProjectOpenListeners = new LinkedList<Reference<IDEProject.OpenListener>>();
        } else {
            Iterator<Reference<IDEProject.OpenListener>> it = ideProjectOpenListeners.iterator();
            while (it.hasNext()) {
                Reference<IDEProject.OpenListener> r = it.next();
                IDEProject.OpenListener l = r.get();
                if (l == null || l == listener) {
                    it.remove(); // also doing cleanup of GC'ed references
                }
            }
        }
        ideProjectOpenListeners.add(new WeakReference<IDEProject.OpenListener>(listener));
        if (projectOpenListener == null) {
            projectOpenListener = new ProjectOpenListener();
            OpenProjects.getDefault().addPropertyChangeListener(projectOpenListener);
        }
    }

    @Override
    public synchronized void removeProjectOpenListener(IDEProject.OpenListener listener) {
        if (ideProjectOpenListeners != null) {
            Iterator<Reference<IDEProject.OpenListener>> it = ideProjectOpenListeners.iterator();
            while (it.hasNext()) {
                Reference<IDEProject.OpenListener> r = it.next();
                IDEProject.OpenListener l = r.get();
                if (l == null || l == listener) {
                    it.remove(); // also doing cleanup of GC'ed references
                }
            }
            if (ideProjectOpenListeners.isEmpty()) {
                ideProjectOpenListeners = null;
                if (projectOpenListener != null) {
                    OpenProjects.getDefault().removePropertyChangeListener(projectOpenListener);
                    projectOpenListener = null;
                }
            }
        }
    }

    private static synchronized IDEProject.OpenListener[] getIDEProjectOpenListeners() {
        if (ideProjectOpenListeners == null) {
            return null;
        }
        List<IDEProject.OpenListener> listenerList = new ArrayList<IDEProject.OpenListener>(ideProjectOpenListeners.size());
        Iterator<Reference<IDEProject.OpenListener>> it = ideProjectOpenListeners.iterator();
        while (it.hasNext()) {
            Reference<IDEProject.OpenListener> r = it.next();
            IDEProject.OpenListener l = r.get();
            if (l == null) {
                it.remove(); // also doing cleanup of GC'ed references
            } else {
                listenerList.add(l);
            }
        }
        if (ideProjectOpenListeners.isEmpty()) {
            ideProjectOpenListeners = null;
            if (projectOpenListener != null) {
                OpenProjects.getDefault().removePropertyChangeListener(projectOpenListener);
                projectOpenListener = null;
            }
            return null;
        }
        return listenerList.toArray(new IDEProject.OpenListener[0]);
    }

    private static class NbProject extends IDEProject {
        private ProjectDeleteListener projectDeleteListener;

        NbProject(String displayName, Icon icon, URL url) {
            super(displayName, icon, url);
        }

        @Override
        public synchronized boolean addDeleteListener(DeleteListener l) {
            boolean added = super.addDeleteListener(l);
            if (added && projectDeleteListener == null) {
                Project p = getProject(getURL());
                if (p != null) {
                    FileObject projDir = p.getProjectDirectory();
                    projectDeleteListener = new ProjectDeleteListener(projDir.toURL(), this);
                    projDir.addFileChangeListener(projectDeleteListener);
                } else {
                    super.removeDeleteListener(l);
                    added = false;
                }
            }
            return added;
        }

        @Override
        public synchronized boolean removeDeleteListener(DeleteListener l) {
            boolean removed = super.removeDeleteListener(l);
            if (removed && getDeleteListeners().isEmpty() && projectDeleteListener != null) {
                Project p = getProject(getURL());
                if (p != null) {
                    FileObject projDir = p.getProjectDirectory();
                    projDir.removeFileChangeListener(projectDeleteListener);
                    projectDeleteListener = null;
                }
            }
            return removed;
        }
    }

    private static class ProjectOpenListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())
                    || evt.getNewValue() == null) {
                return;
            }
            IDEProject.OpenListener[] listeners = getIDEProjectOpenListeners();
            if (listeners == null) {
                return;
            }
            Project[] newProjects = (Project[])evt.getNewValue();
            Project[] oldProjects = (Project[])evt.getOldValue();
            List<Project> openedList;
            if (oldProjects == null) {
                openedList = Arrays.asList(newProjects);
            } else {
                openedList = new ArrayList<Project>();
                openedList.addAll(Arrays.asList(newProjects));
                openedList.removeAll(Arrays.asList(oldProjects));
            }
            if (!openedList.isEmpty()) {
                IDEProject[] newlyOpened = new IDEProject[openedList.size()];
                for (int i=0; i < newlyOpened.length; i++) {
                    newlyOpened[i] = createIDEProject(openedList.get(i));
                }
                for (IDEProject.OpenListener l : listeners) {
                    l.projectsOpened(newlyOpened);
                }
            }
        }
    }

    private static class ProjectDeleteListener extends FileChangeAdapter {
        private URL url;
        private Reference<NbProject> projectRef;

        ProjectDeleteListener(URL url, NbProject ideProject) {
            this.url = url;
            this.projectRef = new WeakReference<NbProject>(ideProject);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            if (fe.getFile().toURL().equals(url)) {
                NbProject project = projectRef.get();
                if (project != null) {
                    project.notifyDeleted();
                } else {
                    fe.getFile().removeFileChangeListener(this);
                }
            }
        }
    }
}
