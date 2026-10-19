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

package org.netbeans.modules.generic.project;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.test.MockLookup;

public class GenericProjectTest extends NbTestCase {

    static {
        // ensures the default lookup includes the meta-inf services,
        // e.g. MasterURLMapper and the ProjectManagerImplementation
        MockLookup.init();
    }

    public GenericProjectTest(String name) {
        super(name);
    }

    public void testNoMarkerNotProject() throws Exception {
        FileObject root = newProjectDir();
        assertFalse(new GenericProject.GpFactory().isProject(root));
    }

    public void testMarkerFileIsProject() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        assertTrue(new GenericProject.GpFactory().isProject(root));
    }

    public void testAttributeOnlyIsProject() throws Exception {
        FileObject root = newProjectDir();
        root.setAttribute(GenericProject.MARKER_ATTRIBUTE, Boolean.TRUE);
        assertTrue(new GenericProject.GpFactory().isProject(root));
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

    public void testSubprojectsAreFirstLevelOnly() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        FileObject child = FileUtil.createFolder(root, "child"); // NOI18N
        FileUtil.createData(child, GenericProject.MARKER_FILE);
        FileUtil.createFolder(root, "plain"); // NOI18N
        FileUtil.createData(root, "readme.txt"); // NOI18N
        FileObject deep = FileUtil.createFolder(FileUtil.createFolder(child, "inner"), "deep"); // NOI18N
        FileUtil.createData(deep, GenericProject.MARKER_FILE);

        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);
        SubprojectProvider spp = prj.getLookup().lookup(SubprojectProvider.class);
        assertNotNull(spp);
        Set<? extends Project> subs = spp.getSubprojects();
        assertEquals(1, subs.size());
        assertEquals(child, subs.iterator().next().getProjectDirectory());
    }

    public void testCreateProject() throws Exception {
        clearWorkDir();
        File dir = new File(getWorkDir(), "proj-" + getName()); // NOI18N
        Set<FileObject> created = GpWizardIterator.createProject(dir, false);
        assertEquals(1, created.size());
        FileObject dirFo = created.iterator().next();
        FileObject marker = dirFo.getFileObject(GenericProject.MARKER_FILE);
        assertNotNull(marker);
        assertEquals(dir.getName(), marker.getAttribute("displayName")); // NOI18N
        Project prj = ProjectManager.getDefault().findProject(dirFo);
        assertNotNull(prj);
        assertTrue(prj instanceof GenericProject);
    }

    public void testCreateProjectLocal() throws Exception {
        clearWorkDir();
        File dir = new File(getWorkDir(), "proj-" + getName()); // NOI18N
        Set<FileObject> created = GpWizardIterator.createProject(dir, true);
        assertEquals(1, created.size());
        FileObject dirFo = created.iterator().next();
        assertNull(dirFo.getFileObject(GenericProject.MARKER_FILE));
        assertEquals(Boolean.TRUE, dirFo.getAttribute(GenericProject.MARKER_ATTRIBUTE));
        Project prj = ProjectManager.getDefault().findProject(dirFo);
        assertNotNull(prj);
        assertTrue(prj instanceof GenericProject);
    }

    public void testSetMarkerStateLocalAndBack() throws Exception {
        FileObject root = newProjectDir();
        GenericProject.createMarker(root);
        assertTrue(GenericProject.hasMarker(root));

        GenericProject.setMarkerState(root, false);
        assertFalse(GenericProject.hasMarker(root));
        assertEquals(Boolean.TRUE, root.getAttribute(GenericProject.MARKER_ATTRIBUTE));
        assertTrue(new GenericProject.GpFactory().isProject(root));

        GenericProject.setMarkerState(root, true);
        assertTrue(GenericProject.hasMarker(root));
        assertNull(root.getAttribute(GenericProject.MARKER_ATTRIBUTE));
    }

    public void testSaveProjectLocalKeepsNoMarker() throws Exception {
        clearWorkDir();
        File dir = new File(getWorkDir(), "proj-" + getName()); // NOI18N
        GpWizardIterator.createProject(dir, true);
        FileObject root = FileUtil.toFileObject(dir);
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);
        new GenericProject.GpFactory().saveProject(prj);
        assertNull(root.getFileObject(GenericProject.MARKER_FILE));
        assertEquals(Boolean.TRUE, root.getAttribute(GenericProject.MARKER_ATTRIBUTE));
    }

    public void testDisplayNameOverrideRoundTrip() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);
        ProjectInformation info = ProjectUtils.getInformation(prj);
        assertEquals(root.getNameExt(), info.getName());
        assertEquals(root.getNameExt(), info.getDisplayName());

        GenericProject.setDisplayNameOverride((GenericProject) prj, "My Fancy Project");
        assertEquals(root.getNameExt(), info.getName());
        assertEquals("My Fancy Project", info.getDisplayName());

        GenericProject.setDisplayNameOverride((GenericProject) prj, null);
        assertEquals(root.getNameExt(), info.getDisplayName());
    }

    public void testDisplayNameOverrideFiresPropertyChange() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);
        ProjectInformation info = ProjectUtils.getInformation(prj);

        AtomicReference<String> fired = new AtomicReference<>();
        info.addPropertyChangeListener(evt -> fired.compareAndSet(null, evt.getPropertyName()));
        GenericProject.setDisplayNameOverride((GenericProject) prj, "New Name");
        assertEquals(ProjectInformation.PROP_DISPLAY_NAME, fired.get());
    }

    public void testRenameOperationStoresOverride() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);
        GpProjectOperations op = prj.getLookup().lookup(GpProjectOperations.class);
        assertNotNull(op);

        op.notifyRenamed("Renamed Project");
        assertEquals("Renamed Project", ProjectUtils.getInformation(prj).getDisplayName());
        assertEquals(root.getNameExt(), ProjectUtils.getInformation(prj).getName());
    }

    public void testMovedClearsStaleOverride() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);
        GpProjectOperations op = prj.getLookup().lookup(GpProjectOperations.class);
        assertNotNull(op);

        GenericProject.setDisplayNameOverride((GenericProject) prj, "Old Override");
        assertEquals("Old Override", ProjectUtils.getInformation(prj).getDisplayName());

        // notification on the new project instance (original != null):
        // the new folder name is authoritative again
        op.notifyMoved(prj, new File("/tmp/does-not-matter"), root.getNameExt()); // NOI18N
        assertEquals(root.getNameExt(), ProjectUtils.getInformation(prj).getDisplayName());
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

    public void testDeleteOperationFiles() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        FileUtil.createData(root, "readme.txt");
        FileObject sub = FileUtil.createFolder(root, "src");
        FileUtil.createData(sub, "Main.java");
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);
        GpProjectOperations op = prj.getLookup().lookup(GpProjectOperations.class);
        assertNotNull(op);

        assertEquals(List.of(root.getFileObject(GenericProject.MARKER_FILE)), op.getMetadataFiles());

        // all of the directory's content is data, the marker file
        // excluded (it is metadata), deepest-first so that the
        // stock delete flow can delete every entry and the
        // (now empty) project folder
        FileObject readme = root.getFileObject("readme.txt"); // NOI18N
        FileObject main = sub.getFileObject("Main.java"); // NOI18N
        List<FileObject> data = op.getDataFiles();
        assertEquals(new HashSet<>(List.of(readme, main, sub)), new HashSet<>(data));
        assertTrue("children must be listed before their parent", data.indexOf(main) < data.indexOf(sub));
    }

    public void testDeleteOperationPreventsMarkerResurrection() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);
        GpProjectOperations op = prj.getLookup().lookup(GpProjectOperations.class);
        assertNotNull(op);

        op.notifyDeleting();
        root.getFileObject(GenericProject.MARKER_FILE).delete(); // simulate the stock flow

        new GenericProject.GpFactory().saveProject(prj);
        assertNull("a saved deleted project must not be resurrected", root.getFileObject(GenericProject.MARKER_FILE));
    }

    public void testDeleteOperationClearsAttribute() throws Exception {
        FileObject root = newProjectDir(); // local (attribute-based) project
        root.setAttribute(GenericProject.MARKER_ATTRIBUTE, Boolean.TRUE);
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);
        GpProjectOperations op = prj.getLookup().lookup(GpProjectOperations.class);
        assertNotNull(op);

        op.notifyDeleting();

        assertNull("the recognition attribute must be dropped", root.getAttribute(GenericProject.MARKER_ATTRIBUTE));
        new GenericProject.GpFactory().saveProject(prj);
        assertNull("a saved deleted local project must not gain a marker file", root.getFileObject(GenericProject.MARKER_FILE));
    }

    public void testCopyOperationKeepsFolderNameAuthoritative() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);
        GenericProject.setDisplayNameOverride((GenericProject) prj, "My Fancy Project");

        // simulate the stock copy flow: duplicate the directory
        // content, find the new project, and notify the operations
        // of both projects
        FileObject copy = FileUtil.createFolder(root.getParent(), "copy-" + getName()); // NOI18N
        FileUtil.copyFile(root.getFileObject(GenericProject.MARKER_FILE), copy, GenericProject.MARKER_FILE, null);
        Project nue = ProjectManager.getDefault().findProject(copy);
        assertNotNull(nue);
        GpProjectOperations opOrig = prj.getLookup().lookup(GpProjectOperations.class);
        GpProjectOperations opNue = nue.getLookup().lookup(GpProjectOperations.class);
        assertNotNull(opOrig);
        assertNotNull(opNue);
        opOrig.notifyCopied(null, FileUtil.toFile(root), copy.getNameExt());
        opNue.notifyCopied(prj, FileUtil.toFile(root), copy.getNameExt());

        assertEquals("the folder name is authoritative on the copy",
                copy.getNameExt(), ProjectUtils.getInformation(nue).getDisplayName());
        assertEquals("the original keeps its display name",
                "My Fancy Project", ProjectUtils.getInformation(prj).getDisplayName());
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

    public void testProjectLookupContainsLayerServices() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);
        // TestMarkerProvider is registered for this project type via
        // @ProjectServiceProvider, i.e. as a layer contribution under
        // "Projects/<key>/Lookup": the composite project lookup must
        // mix it in
        assertNotNull(prj.getLookup().lookup(TestMarker.class));
        assertNotNull("the project's own services must still be present",
                prj.getLookup().lookup(ProjectInformation.class));
    }

    private FileObject newProjectDir() throws Exception {
        clearWorkDir();
        File dir = new File(getWorkDir(), "proj-" + getName()); // NOI18N
        assertTrue(dir.mkdirs() || dir.isDirectory());
        return FileUtil.toFileObject(dir);
    }

    public interface TestMarker {
    }

    @ProjectServiceProvider(service=TestMarker.class, projectType=GenericProject.PROJECT_KEY)
    public static class TestMarkerProvider implements TestMarker {
    }

}
