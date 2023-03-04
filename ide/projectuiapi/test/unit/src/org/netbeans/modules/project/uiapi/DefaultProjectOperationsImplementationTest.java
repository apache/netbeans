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
package org.netbeans.modules.project.uiapi;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.uiapi.DefaultProjectOperationsImplementation.Executor;
import org.netbeans.modules.project.uiapi.DefaultProjectOperationsImplementation.UserInputHandler;
import org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/**
 * @author Jan Lahoda
 */
public class DefaultProjectOperationsImplementationTest extends NbTestCase {
    
    public DefaultProjectOperationsImplementationTest(String testName) {
        super(testName);
    }
    
    private FileObject scratch;
    private FileObject projdir;
    private Project prj;
    private File projectDirectory;
    
    private void createProject(FileObject projdir) throws Exception {
        TestUtil.createFileFromContent(DefaultProjectOperationsImplementationTest.class.getResource("data/test.txt"), projdir, "nbproject/test.txt");
        TestUtil.createFileFromContent(DefaultProjectOperationsImplementationTest.class.getResource("data/test.txt"), projdir, "src/test/test.txt");
    }
    
    protected void setUp() throws Exception {
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        
        createProject(projdir);

        MockLookup.setInstances(new TestProjectFactory(),
            new SimpleFileOwnerQueryImplementation());
        
        prj = ProjectManager.getDefault().findProject(projdir);
        
        assertNotNull(prj);
        
        projectDirectory = FileUtil.toFile(projdir);
        
        assertNotNull(projectDirectory);
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    //<editor-fold defaultstate="collapsed" desc="Delete Operation">
    public void testDeleteProjectDeleteAll() throws Exception {
        TestUserInputHandler handler = new TestUserInputHandler(TestUserInputHandler.USER_OK_ALL);
        
        DefaultProjectOperationsImplementation.deleteProject(prj, handler);
        
        assertTrue(handler.confirmationDialogCalled);
        
        assertFalse(projectDirectory.exists());
    }
    
    public void testDeleteProjectDeleteMetadata() throws Exception {
        TestUserInputHandler handler = new TestUserInputHandler(TestUserInputHandler.USER_OK_METADATA);
        
        DefaultProjectOperationsImplementation.deleteProject(prj, handler);
        
        assertTrue(handler.confirmationDialogCalled);
        
        assertTrue(projectDirectory.exists());
        assertTrue(Arrays.equals(new String[] {"src"}, projectDirectory.list()));
    }
    
    public void testDeleteProjectDoNotDelete() throws Exception {
        TestUserInputHandler handler = new TestUserInputHandler(TestUserInputHandler.USER_CANCEL);
        
        DefaultProjectOperationsImplementation.deleteProject(prj, handler);
        
        assertTrue(handler.confirmationDialogCalled);
        
        assertTrue(projectDirectory.exists());
        List<String> items = Arrays.asList(projectDirectory.list());
        Collections.sort(items);
        assertEquals(Arrays.asList("nbproject", "src"), items);
    }
    
    public void testDeleteProjectNestedProject() throws Exception {
        FileObject projdir2 = projdir.createFolder("proj2");
        
        createProject(projdir2);
        
        TestUserInputHandler handler = new TestUserInputHandler(TestUserInputHandler.USER_OK_ALL);
        
        DefaultProjectOperationsImplementation.deleteProject(prj, handler);
        
        assertTrue(handler.confirmationDialogCalled);
        
        assertTrue(projectDirectory.exists());
        assertTrue(Arrays.equals(new String[] {"proj2"}, projectDirectory.list()));
    }
    
    public void testDeleteProjectNestedLibrary() throws Exception {
        FileObject library = projdir.createFolder("lib");
        
        TestUserInputHandler handler = new TestUserInputHandler(TestUserInputHandler.USER_OK_ALL);
        
        DefaultProjectOperationsImplementation.deleteProject(prj, handler);
        
        assertTrue(handler.confirmationDialogCalled);
        
        assertTrue(projectDirectory.exists());
        assertTrue(Arrays.equals(new String[] {"lib"}, projectDirectory.list()));
    }
    
    public void testDeleteProjectExternalSources() throws Exception {
        FileObject extDir = scratch.createFolder("external");
        File extDirFile = FileUtil.toFile(extDir);
        
        assertNotNull(extDirFile);
        
        DeleteProjectOperationImpl dpoi = prj.getLookup().lookup(DeleteProjectOperationImpl.class);
        
        assertNotNull(dpoi);
        
        dpoi.setExternalFile(extDir);
        
        TestUserInputHandler handler = new TestUserInputHandler(TestUserInputHandler.USER_OK_ALL);
        
        DefaultProjectOperationsImplementation.deleteProject(prj, handler);
        
        assertTrue(handler.confirmationDialogCalled);
        
        assertFalse(projectDirectory.exists());
        
        assertTrue(extDirFile.exists());
    }
    
    private static final class TestUserInputHandler implements UserInputHandler {
        
        public static final int USER_CANCEL = 1;
        public static final int USER_OK_METADATA = 2;
        public static final int USER_OK_ALL = 3;

        private int answer;
        private Exception exception;
        
        private boolean confirmationDialogCalled;
        
        public TestUserInputHandler(int answer) {
            this.answer = answer;
            this.confirmationDialogCalled = false;
            this.exception = null;
        }

        public void showConfirmationDialog(final JComponent panel, Project project, String caption, String confirmButton, String cancelButton, boolean doSetMessageType, final Executor executor) {
            confirmationDialogCalled = true;
            
            if (answer == USER_CANCEL) {
                return ;
            }
            
            if (answer == USER_OK_ALL) {
                ((DefaultProjectDeletePanel) panel).setDeleteSources(true);
            } else {
                ((DefaultProjectDeletePanel) panel).setDeleteSources(false);
            }
            
            try {
                executor.execute();
            } catch (Exception e) {
                exception = e;
            }
        }
        
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Copy Operation">
    public void testCopyWithLib() throws Exception {
        TestUtil.createFileFromContent(DefaultProjectOperationsImplementationTest.class.getResource("data/test.txt"), projdir, "lib/test.txt");
        ProgressHandle handle = ProgressHandleFactory.createHandle("test-handle");
        handle.start(DefaultProjectOperationsImplementation.MAX_WORK);
        FileObject newTarget = prj.getProjectDirectory().getParent();
        
        DefaultProjectOperationsImplementation.doCopyProject(handle, prj, "projCopy", newTarget);
        
        File newProject = new File(FileUtil.toFile(newTarget), "projCopy");
        
        assertTrue(newProject.isDirectory());
        assertTrue(new File(newProject, "nbproject").isDirectory());
        assertTrue(new File(newProject, "src").isDirectory());
        assertTrue(new File(newProject, "lib").isDirectory());
    }
    
    public void testCopyWithInnerProjectSimple() throws Exception {
        TestUtil.createFileFromContent(DefaultProjectOperationsImplementationTest.class.getResource("data/test.txt"), projdir, "lib/test.txt");
        FileObject projdir2 = projdir.createFolder("proj2");
        
        createProject(projdir2);
        
        ProgressHandle handle = ProgressHandleFactory.createHandle("test-handle");
        handle.start(DefaultProjectOperationsImplementation.MAX_WORK);
        FileObject newTarget = prj.getProjectDirectory().getParent();
        
        DefaultProjectOperationsImplementation.doCopyProject(handle, prj, "projCopy", newTarget);
        
        File newProject = new File(FileUtil.toFile(newTarget), "projCopy");
        
        assertTrue(newProject.isDirectory());
        assertTrue(new File(newProject, "nbproject").isDirectory());
        assertTrue(new File(newProject, "src").isDirectory());
        assertTrue(new File(newProject, "lib").isDirectory());
        assertFalse(new File(newProject, "proj2").exists());
    }
    
    public void testCopyWithInnerProjectComplex() throws Exception {
        TestUtil.createFileFromContent(DefaultProjectOperationsImplementationTest.class.getResource("data/test.txt"), projdir, "lib/test.txt");
        FileObject projdir2 = projdir.getFileObject("lib").createFolder("proj2");
        
        createProject(projdir2);
        
        ProgressHandle handle = ProgressHandleFactory.createHandle("test-handle");
        handle.start(DefaultProjectOperationsImplementation.MAX_WORK);
        
        FileObject newTarget = prj.getProjectDirectory().getParent();
        
        DefaultProjectOperationsImplementation.doCopyProject(handle, prj, "projCopy", newTarget);
        
        File newProject = new File(FileUtil.toFile(newTarget), "projCopy");
        
        assertTrue(newProject.isDirectory());
        assertTrue(new File(newProject, "nbproject").isDirectory());
        assertTrue(new File(newProject, "src").isDirectory());
        assertTrue(new File(newProject, "lib").isDirectory());
        assertFalse(new File(new File(newProject, "lib"), "proj2").exists());
    }

    public void testMainProjectFlagNotMovedWhenCopying() throws Exception {
        OpenProjects.getDefault().close(OpenProjects.getDefault().getOpenProjects());
        OpenProjects.getDefault().open(new Project[] {prj}, false);
        
        //set the project to be copied as main.
        OpenProjects.getDefault().setMainProject(prj);       
        assertTrue(prj.getProjectDirectory().equals(OpenProjects.getDefault().getMainProject().getProjectDirectory()));
        
        ProgressHandle handle = ProgressHandleFactory.createHandle("test-handle");
        handle.start(DefaultProjectOperationsImplementation.MAX_WORK);
        FileObject oldProject = prj.getProjectDirectory();
        File       oldProjectFile = FileUtil.toFile(oldProject);
        FileObject newTarget = oldProject.getParent();
        
        DefaultProjectOperationsImplementation.doCopyProject(handle, prj, "projCopy", newTarget);
        
        //test that after copying the main project is still the original one.
        Project main = OpenProjects.getDefault().getMainProject();
        assertTrue(main != null && main.getProjectDirectory().equals(prj.getProjectDirectory()));
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Move Operation">
    public void testMoveWithLib() throws Exception {
        TestUtil.createFileFromContent(DefaultProjectOperationsImplementationTest.class.getResource("data/test.txt"), projdir, "lib/test.txt");
        ProgressHandle handle = ProgressHandleFactory.createHandle("test-handle");
        handle.start(DefaultProjectOperationsImplementation.MAX_WORK);
        FileObject oldProject = prj.getProjectDirectory();
        File       oldProjectFile = FileUtil.toFile(oldProject);
        FileObject newTarget = oldProject.getParent();
        
        DefaultProjectOperationsImplementation.doMoveProject(handle, prj, "projMove","projMove", newTarget, "ERR_Cannot_Move");
        
        File newProject = new File(FileUtil.toFile(newTarget), "projMove");
        
        assertTrue(newProject.isDirectory());
        assertTrue(new File(newProject, "nbproject").isDirectory());
        assertTrue(new File(newProject, "src").isDirectory());
        assertTrue(new File(newProject, "lib").isDirectory());
        
        assertFalse(oldProjectFile.exists());
    }
    
    public void testMoveWithInnerProjectSimple() throws Exception {
        TestUtil.createFileFromContent(DefaultProjectOperationsImplementationTest.class.getResource("data/test.txt"), projdir, "lib/test.txt");
        FileObject projdir2 = projdir.createFolder("proj2");
        
        createProject(projdir2);
        
        ProgressHandle handle = ProgressHandleFactory.createHandle("test-handle");
        handle.start(DefaultProjectOperationsImplementation.MAX_WORK);
        FileObject oldProject = prj.getProjectDirectory();
        File       oldProjectFile = FileUtil.toFile(oldProject);
        FileObject newTarget = oldProject.getParent();
        
        DefaultProjectOperationsImplementation.doMoveProject(handle, prj, "projMove", "projMove", newTarget, "ERR_Cannot_Move");
        
        File newProject = new File(FileUtil.toFile(newTarget), "projMove");
        
        assertTrue(newProject.isDirectory());
        assertTrue(new File(newProject, "nbproject").isDirectory());
        assertTrue(new File(newProject, "src").isDirectory());
        assertTrue(new File(newProject, "lib").isDirectory());
        // We now try to just move the project directory as is:
        assertTrue(new File(newProject, "proj2").isDirectory());
        assertFalse(new File(oldProjectFile, "proj2").exists());
    }
    
    public void testMoveWithInnerProjectComplex() throws Exception {
        TestUtil.createFileFromContent(DefaultProjectOperationsImplementationTest.class.getResource("data/test.txt"), projdir, "lib/test.txt");
        FileObject projdir2 = projdir.getFileObject("lib").createFolder("proj2");
        
        createProject(projdir2);
        
        ProgressHandle handle = ProgressHandleFactory.createHandle("test-handle");
        handle.start(DefaultProjectOperationsImplementation.MAX_WORK);
        
        FileObject oldProject     = prj.getProjectDirectory();
        File       oldProjectFile = FileUtil.toFile(oldProject);
        FileObject newTarget      = oldProject.getParent();
        
        DefaultProjectOperationsImplementation.doMoveProject(handle, prj, "projMove", "projMove", newTarget, "ERR_Cannot_Move");
        
        File newProject = new File(FileUtil.toFile(newTarget), "projMove");
        
        assertTrue(newProject.isDirectory());
        assertTrue(new File(newProject, "nbproject").isDirectory());
        assertTrue(new File(newProject, "src").isDirectory());
        assertTrue(new File(newProject, "lib").isDirectory());
        assertTrue(new File(new File(newProject, "lib"), "proj2").isDirectory());
        assertFalse(new File(new File(oldProjectFile, "lib"), "proj2").exists());
    }
    
    public void testMainProjectFlagMovedForMainProject() throws Exception {
        OpenProjects.getDefault().open(new Project[] {prj}, false);
        OpenProjects.getDefault().setMainProject(prj);
        assertEquals(prj, OpenProjects.getDefault().getMainProject());
        ProgressHandle handle = ProgressHandleFactory.createHandle("test-handle");
        handle.start(DefaultProjectOperationsImplementation.MAX_WORK);
        FileObject oldProject = prj.getProjectDirectory();
        File       oldProjectFile = FileUtil.toFile(oldProject);
        FileObject newTarget = oldProject.getParent();
        
        DefaultProjectOperationsImplementation.doMoveProject(handle, prj, "projMove", "projMove", newTarget, "ERR_Cannot_Move");
        
        Project newProject = ProjectManager.getDefault().findProject(newTarget.getFileObject("projMove"));
        
        assertEquals(OpenProjects.getDefault().getMainProject(), newProject);
    }
    
    public void testMainProjectFlagNotMovedForNonMainProject() throws Exception {
        OpenProjects.getDefault().open(new Project[] {prj}, false);
        
        Project main = OpenProjects.getDefault().getMainProject();
        
        assertTrue(main == null || !prj.getProjectDirectory().equals(main.getProjectDirectory()));
        
        ProgressHandle handle = ProgressHandleFactory.createHandle("test-handle");
        handle.start(DefaultProjectOperationsImplementation.MAX_WORK);
        FileObject oldProject = prj.getProjectDirectory();
        File       oldProjectFile = FileUtil.toFile(oldProject);
        FileObject newTarget = oldProject.getParent();
        
        DefaultProjectOperationsImplementation.doMoveProject(handle, prj, "projMove", "projMove", newTarget, "ERR_Cannot_Move");
        
        Project newProject = ProjectManager.getDefault().findProject(newTarget.getFileObject("projMove"));
        
        main = OpenProjects.getDefault().getMainProject();
        
        assertTrue(    main == null
                    || (    !prj.getProjectDirectory().equals(main.getProjectDirectory())
                         && !newProject.getProjectDirectory().equals(main.getProjectDirectory())));
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Utilities">
    private static final class TestProject implements Project {
        
        private final Lookup l;
        private final FileObject projectDirectory;
        
        TestProject(FileObject projectDirectory) throws IOException {
            l = Lookups.fixed(new DeleteProjectOperationImpl(this));
            this.projectDirectory = projectDirectory;
        }
        
        public FileObject getProjectDirectory() {
            return projectDirectory;
        }
        
        public Lookup getLookup() {
            return l;
        }
        
        public String toString() {
            return "TestAntBasedProject[" + getProjectDirectory() + "]";
        }
        
    }
    
    public static class TestProjectFactory implements ProjectFactory {
        
        public boolean isProject(FileObject projectDirectory) {
            return projectDirectory.getFileObject("nbproject") != null;
        }
        
        public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
            if (isProject(projectDirectory))
                return new TestProject(projectDirectory);
            
            return null;
        }
        
        public void saveProject(Project project) throws IOException, ClassCastException {
        }
        
    }
    
    public static final class DeleteProjectOperationImpl implements DeleteOperationImplementation {
        
        private boolean wasCleaned = false;
        private boolean wasNotified = false;
        
        private FileObject externalFile = null;
        
        private TestProject project;
        
        public DeleteProjectOperationImpl(TestProject project) {
            this.project = project;
        }
        
        public List<FileObject> getMetadataFiles() {
            return Collections.singletonList(project.getProjectDirectory().getFileObject("nbproject"));
        }
        
        public List<FileObject> getDataFiles() {
            if (externalFile == null) {
                return Collections.singletonList(project.getProjectDirectory().getFileObject("src"));
            } else {
                return Arrays.asList(project.getProjectDirectory().getFileObject("src"), externalFile);
            }
        }
        
        public void setExternalFile(FileObject externalFile) {
            this.externalFile = externalFile;
        }
        
        public synchronized boolean getWasCleaned() {
            return wasCleaned;
        }
        
        public synchronized void notifyDeleting() throws IOException {
            wasCleaned = true;
        }
        
        public synchronized boolean getWasNotified() {
            return wasNotified;
        }
        
        public synchronized void notifyDeleted() throws IOException {
            wasNotified = true;
        }
        
    }
    //</editor-fold>
    
}
