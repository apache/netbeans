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

package org.netbeans.modules.generic.project.ui;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.generic.project.GenericProject;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.test.MockLookup;

/**
 * Tests for the UI services of the generic project (logical view,
 * action provider, file filter): unlike the headless core services,
 * they are contributed to the project lookup from this module via
 * {@code @ProjectServiceProvider}, so they can only be exercised
 * in a test environment where this module is loaded.
 */
public class GpProjectUiTest extends NbTestCase {

    static {
        // ensures the default lookup includes the meta-inf services,
        // e.g. MasterURLMapper and the ProjectManagerImplementation
        MockLookup.init();
    }

    public GpProjectUiTest(String name) {
        super(name);
    }

    public void testLogicalViewShowsAllFiles() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        FileUtil.createData(root, "readme.txt");
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);
        assertTrue(prj instanceof GenericProject);
        LogicalViewProvider provider = prj.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull(provider);
        Node node = provider.createLogicalView();
        assertNotNull(node);
        Node[] children = node.getChildren().getNodes(true);
        assertEquals(2, children.length);
    }

    public void testLogicalViewNodeHonorsDisplayNameOverride() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);
        Node node = prj.getLookup().lookup(LogicalViewProvider.class).createLogicalView();
        assertEquals(root.getNameExt(), node.getDisplayName());

        GenericProject.setDisplayNameOverride((GenericProject) prj, "Fancy Name");
        assertEquals("Fancy Name", node.getDisplayName());

        GenericProject.setDisplayNameOverride((GenericProject) prj, null);
        assertEquals(root.getNameExt(), node.getDisplayName());
    }

    public void testLogicalViewNodeLookupExposesProjectAndDirectory() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);
        Node node = prj.getLookup().lookup(LogicalViewProvider.class).createLogicalView();
        assertEquals(prj, node.getLookup().lookup(GenericProject.class));
        assertEquals(root, node.getLookup().lookup(FileObject.class));
        DataObject dobj = node.getLookup().lookup(DataObject.class);
        assertNotNull("the project directory DataObject must be in the node lookup", dobj);
        assertEquals(root, dobj.getPrimaryFile());
    }

    public void testFindPathReturnsNodeForProjectFile() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        FileObject file = FileUtil.createData(root, "readme.txt"); // NOI18N
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);
        LogicalViewProvider provider = prj.getLookup().lookup(LogicalViewProvider.class);
        Node node = provider.createLogicalView();
        Node found = provider.findPath(node, file);
        assertNotNull("the node of a project file must be found", found);
        assertEquals(file, found.getLookup().lookup(DataObject.class).getPrimaryFile());
    }

    public void testFindPathReturnsNodeForNestedFile() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        FileObject file = FileUtil.createData(FileUtil.createFolder(root, "src"), "Main.java"); // NOI18N
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);
        LogicalViewProvider provider = prj.getLookup().lookup(LogicalViewProvider.class);
        Node node = provider.createLogicalView();
        Node found = provider.findPath(node, file);
        assertNotNull("the node of a nested file must be found", found);
        assertEquals(file, found.getLookup().lookup(DataObject.class).getPrimaryFile());
    }

    public void testFindPathReturnsRootForProjectDirectory() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);
        LogicalViewProvider provider = prj.getLookup().lookup(LogicalViewProvider.class);
        Node node = provider.createLogicalView();
        assertSame(node, provider.findPath(node, root));
    }

    public void testFindPathReturnsNullForForeignOrOtherTarget() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        FileObject foreign = FileUtil.createData(FileUtil.toFileObject(getWorkDir()), "outside.txt"); // NOI18N
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);
        LogicalViewProvider provider = prj.getLookup().lookup(LogicalViewProvider.class);
        Node node = provider.createLogicalView();
        assertNull(provider.findPath(node, foreign));
        assertNull(provider.findPath(node, "not a file object")); // NOI18N
    }

    public void testFindPathReturnsNullForFilteredFile() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        FileObject file = FileUtil.createData(root, "generated.tmp"); // NOI18N
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);
        GenericProject.setFilesFilterPattern((GenericProject) prj, ".tmp"); // NOI18N
        LogicalViewProvider provider = prj.getLookup().lookup(LogicalViewProvider.class);
        Node node = provider.createLogicalView();
        assertNull("a file hidden by the project filter must not be findable",
                provider.findPath(node, file));
    }

    public void testActionProviderSupportsRename() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);
        ActionProvider ap = prj.getLookup().lookup(ActionProvider.class);
        assertNotNull(ap);
        assertTrue(List.of(ap.getSupportedActions()).contains(ActionProvider.COMMAND_RENAME));
        assertTrue(ap.isActionEnabled(ActionProvider.COMMAND_RENAME, null));
        assertTrue(List.of(ap.getSupportedActions()).contains(ActionProvider.COMMAND_DELETE));
        assertTrue(ap.isActionEnabled(ActionProvider.COMMAND_DELETE, null));
        assertTrue(List.of(ap.getSupportedActions()).contains(ActionProvider.COMMAND_MOVE));
        assertTrue(ap.isActionEnabled(ActionProvider.COMMAND_MOVE, null));
        assertTrue(List.of(ap.getSupportedActions()).contains(ActionProvider.COMMAND_COPY));
        assertTrue(ap.isActionEnabled(ActionProvider.COMMAND_COPY, null));
    }

    public void testFilterHidesFilesTheIdeIgnores() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        FileUtil.createData(root, "readme.txt");
        FileObject git = FileUtil.createData(root, ".git");
        GenericProject prj = (GenericProject) ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);

        // sanity: in the test environment the IDE's global filter
        // (masterfs' implementation) hides ".git"
        assertFalse(VisibilityQuery.getDefault().isVisible(git));

        GpFileFilter filter = new GpFileFilter(prj);
        assertFalse("the filter must hide files the IDE ignores",
                filter.acceptFileObject(git));
        assertTrue("regular files must stay visible",
                filter.acceptFileObject(root.getFileObject("readme.txt")));

        CountDownLatch latch = new CountDownLatch(1);
        filter.addChangeListener(evt -> latch.countDown());
        GenericProject.setGlobalFileFilterEnabled(prj, false);
        assertTrue("the filter must be notified when the toggle changes",
                latch.await(10, TimeUnit.SECONDS));
        assertTrue("disabling the filter must show everything",
                filter.acceptFileObject(git));
    }

    public void testAdditionalFilterPattern() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        FileUtil.createData(root, "readme.txt");
        FileUtil.createData(root, "readme-old.txt");
        FileObject git = FileUtil.createData(root, ".git");
        GenericProject prj = (GenericProject) ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);

        GpFileFilter filter = new GpFileFilter(prj);
        assertTrue(filter.acceptFileObject(root.getFileObject("readme.txt")));

        // an additional pattern hides its matches on top of the global
        // filter (the two are AND-combined)
        CountDownLatch latch = new CountDownLatch(1);
        filter.addChangeListener(evt -> latch.countDown());
        GenericProject.setFilesFilterPattern(prj, ".*-old.*"); // NOI18N
        assertTrue("the filter must be notified when the pattern changes",
                latch.await(10, TimeUnit.SECONDS));
        assertFalse("files matching the additional pattern must be hidden",
                filter.acceptFileObject(root.getFileObject("readme-old.txt")));
        assertTrue("other files must stay visible",
                filter.acceptFileObject(root.getFileObject("readme.txt")));
        assertFalse("the global filter must still apply",
                filter.acceptFileObject(git));

        // clearing the pattern shows the files again
        final CountDownLatch latch2 = new CountDownLatch(1);
        filter.addChangeListener(evt -> latch2.countDown());
        GenericProject.setFilesFilterPattern(prj, null);
        assertTrue(latch2.await(10, TimeUnit.SECONDS));
        assertTrue(filter.acceptFileObject(root.getFileObject("readme-old.txt")));
    }

    public void testInvalidAdditionalPatternIsIgnored() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        FileUtil.createData(root, "readme-old.txt");
        GenericProject prj = (GenericProject) ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);

        // simulate a hand-edited data file with a broken pattern:
        // it must act as no additional filter (a warning is logged)
        GenericProject.setFilesFilterPattern(prj, ".*-old("); // NOI18N
        GpFileFilter filter = new GpFileFilter(prj);
        assertTrue("an invalid pattern must not hide files",
                filter.acceptFileObject(root.getFileObject("readme-old.txt")));
    }

    private FileObject newProjectDir() throws Exception {
        clearWorkDir();
        File dir = new File(getWorkDir(), "proj-" + getName()); // NOI18N
        assertTrue(dir.mkdirs() || dir.isDirectory());
        return FileUtil.toFileObject(dir);
    }

}