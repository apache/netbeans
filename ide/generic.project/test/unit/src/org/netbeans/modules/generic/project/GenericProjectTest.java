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
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Properties;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.generic.project.ui.actions.GpProjectOperations;
import org.netbeans.modules.generic.project.ui.wizard.GpWizardIterator;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.ProjectContainerProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.DirectoryProjectProvider;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

@SuppressWarnings("null")
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
        assertFalse(new GpFactory().isProject(root));
    }

    public void testMarkerFileIsProject() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        assertTrue(new GpFactory().isProject(root));
    }

    public void testAttributeOnlyIsProject() throws Exception {
        FileObject root = newProjectDir();
        root.setAttribute(GenericProject.MARKER_ATTRIBUTE, Boolean.TRUE);
        assertTrue(new GpFactory().isProject(root));
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

    public void testSubprojectsAreContainedProjects() throws Exception {
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
        assertEquals("projects nested deeper than the directory are not exposed",
        Set.of(child), projectDirectories(spp.getSubprojects()));
    }

    public void testContainerProviderCompleteContainedSet() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        FileObject child = FileUtil.createFolder(root, "child"); // NOI18N
        FileUtil.createData(child, GenericProject.MARKER_FILE);
        FileObject deep = FileUtil.createFolder(FileUtil.createFolder(child, "inner"), "deep"); // NOI18N
        FileUtil.createData(deep, GenericProject.MARKER_FILE);

        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);
        ProjectContainerProvider pcp = prj.getLookup().lookup(ProjectContainerProvider.class);
        assertNotNull(pcp);
        ProjectContainerProvider.Result res = pcp.getContainedProjects();
        assertEquals("projects nested deeper than the directory are not exposed",
                Set.of(child), projectDirectories(res.getProjects()));
        for (Project p : res.getProjects()) {
            assertNotSame("the container must not list itself", prj, p);
        }
        SubprojectProvider spp = prj.getLookup().lookup(SubprojectProvider.class);
        assertNotNull(spp);
        assertEquals("legacy and container views must agree",
                projectDirectories(res.getProjects()), projectDirectories(spp.getSubprojects()));
    }

    public void testContainerProviderSkipsNotSharableSubtrees() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        FileObject hidden = FileUtil.createFolder(root, "hidden"); // NOI18N
        FileObject inside = FileUtil.createFolder(hidden, "inside"); // NOI18N
        FileUtil.createData(inside, GenericProject.MARKER_FILE);
        FileObject visible = FileUtil.createFolder(root, "visible"); // NOI18N
        FileUtil.createData(visible, GenericProject.MARKER_FILE);

        MockLookup.setInstances(sharabilityFor(hidden, SharabilityQuery.Sharability.NOT_SHARABLE));
        try {
            Project prj = ProjectManager.getDefault().findProject(root);
            assertNotNull(prj);
            ProjectContainerProvider pcp = prj.getLookup().lookup(ProjectContainerProvider.class);
            assertNotNull(pcp);
            assertEquals(new HashSet<>(List.of(visible)),
                    projectDirectories(pcp.getContainedProjects().getProjects()));
        } finally {
            MockLookup.setInstances();
        }
    }

    public void testPromotePlainDirectory() throws Exception {
        FileObject dir = newProjectDir();
        Project prj = GenericProject.promoteToProject(dir);
        assertNotNull(prj);
        assertTrue(prj instanceof GenericProject);
        assertEquals(Boolean.TRUE, dir.getAttribute(GenericProject.MARKER_ATTRIBUTE));
        assertNull("promotion must not touch the working tree",
                dir.getFileObject(GenericProject.MARKER_FILE));
        assertEquals(prj, ProjectManager.getDefault().findProject(dir));
    }

    public void testPromoteIsIdempotent() throws Exception {
        FileObject dir = newProjectDir();
        Project first = GenericProject.promoteToProject(dir);
        Project second = GenericProject.promoteToProject(dir);
        assertSame(first, second);
    }

    public void testPromoteExistingProjectIsNoOp() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        Project existing = ProjectManager.getDefault().findProject(root);
        assertNotNull(existing);
        assertSame(existing, GenericProject.promoteToProject(root));
        assertFalse("the marker project must not gain a local attribute",
                GenericProject.isAttributeProject(root));
    }

    public void testPromoteNotSharableDirectory() throws Exception {
        FileObject dir = newProjectDir();
        MockLookup.setInstances(sharabilityFor(dir, SharabilityQuery.Sharability.NOT_SHARABLE));
        try {
            assertNull(GenericProject.promoteToProject(dir));
            assertNull(dir.getAttribute(GenericProject.MARKER_ATTRIBUTE));
        } finally {
            MockLookup.setInstances();
        }
    }

    public void testDirectoryProjectProviderService() throws Exception {
        DirectoryProjectProvider provider = Lookup.getDefault().lookup(DirectoryProjectProvider.class);
        assertNotNull("the promotion service must be registered in the default lookup", provider);
        FileObject dir = newProjectDir();
        Project prj = provider.createProject(dir);
        assertNotNull(prj);
        assertTrue(prj instanceof GenericProject);
        assertEquals(Boolean.TRUE, dir.getAttribute(GenericProject.MARKER_ATTRIBUTE));
        assertNull(dir.getFileObject(GenericProject.MARKER_FILE));
    }

    private Set<FileObject> projectDirectories(Set<? extends Project> projects) {
        Set<FileObject> dirs = new HashSet<>();
        for (Project p : projects) {
            dirs.add(p.getProjectDirectory());
        }
        return dirs;
    }

    private static SharabilityQueryImplementation2 sharabilityFor(FileObject dir, SharabilityQuery.Sharability answer) {
        return (URI uri) -> dir.toURI().equals(uri)
                ? answer
                : SharabilityQuery.Sharability.UNKNOWN;
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
        assertTrue(new GpFactory().isProject(root));

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
        new GpFactory().saveProject(prj);
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

        new GpFactory().saveProject(prj);
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
        new GpFactory().saveProject(prj);
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

    public void testProjectLookupProvidesAuxiliaryProperties() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);
        assertNotNull("the project must provide its own AuxiliaryProperties",
                prj.getLookup().lookup(AuxiliaryProperties.class));
    }

    public void testMarkerProjectStoresSharedAuxDataInProjectDir() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);

        GenericProject.setFilesFilterPattern((GenericProject) prj, ".*-ignored");
        ProjectUtils.getPreferences(prj, GenericProject.class, true).flush();

        // the shared data must live in the project directory (the
        // marker file is the data file)...
        FileObject dataFile = root.getFileObject(GpAuxiliaryProperties.dataFileName());
        assertNotNull("the auxiliary data file must exist in the project directory", dataFile);
        Properties data = readDataFile(dataFile);
        assertTrue("the shared setting must be stored in the data file",
                data.containsKey(auxKey("shared", GenericProject.FILES_FILTER_PATTERN_KEY)));
        // ...and not in the user configuration
        assertNull("no shared auxiliary data may be created in the user configuration for marker projects",
                FileUtil.getConfigRoot().getFileObject(
                        GpAuxiliaryProperties.dataFolder(root) + "/" + GpAuxiliaryProperties.dataFileName()));
    }

    public void testRenameOverrideIsStoredInTheMarkerFile() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);

        // a pure rename (the folder is not renamed) stores the new
        // name as a display name override...
        GpProjectOperations op = prj.getLookup().lookup(GpProjectOperations.class);
        assertNotNull(op);
        op.notifyRenamed("Renamed Project");
        ProjectUtils.getPreferences(prj, GenericProject.class, true).flush();

        // ...in the shared area, i.e. the marker file, so the name is
        // versioned along with the project...
        Properties data = readDataFile(root.getFileObject(GenericProject.MARKER_FILE));
        assertTrue("the display name override must be stored in the data file",
                data.containsKey(auxKey("shared", GenericProject.DISPLAY_NAME_KEY)));
        // ...and not in the user configuration
        assertNull("no private auxiliary data may be created in the user configuration",
                FileUtil.getConfigRoot().getFileObject(
                        GpAuxiliaryProperties.dataFolder(root) + "/" + GpAuxiliaryProperties.privateDataFileName()));
    }

    public void testMarkerProjectKeepsPrivateDataOutOfWorkingTree() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);

        GenericProject.setGlobalFileFilterEnabled((GenericProject) prj, false);
        ProjectUtils.getPreferences(prj, GenericProject.class, false).flush();

        // the private data must live in the user configuration...
        FileObject privateFile = FileUtil.getConfigRoot().getFileObject(
                GpAuxiliaryProperties.dataFolder(root) + "/" + GpAuxiliaryProperties.privateDataFileName());
        assertNotNull("the private auxiliary data file must be created in the user configuration", privateFile);
        Properties privateData = readDataFile(privateFile);
        assertTrue("the private setting must be stored in the private data file",
                privateData.containsKey(auxKey("private", GenericProject.GLOBAL_FILE_FILTER_KEY)));
        // ...and no private setting may be written into the working tree
        Properties markerData = readDataFile(root.getFileObject(GenericProject.MARKER_FILE));
        for (String key : markerData.stringPropertyNames()) {
            assertFalse("no private setting may be written into the working tree: " + key,
                    key.startsWith("private.")); // NOI18N
        }
    }

    public void testRepeatedWritesReuseTheDataFile() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);

        // several consecutive writes to the existing data file must
        // not fail (createData throws if the file already exists)
        GenericProject.setDisplayNameOverride((GenericProject) prj, "First Name");
        ProjectUtils.getPreferences(prj, GenericProject.class, true).flush();
        GenericProject.setDisplayNameOverride((GenericProject) prj, "Second Name");
        ProjectUtils.getPreferences(prj, GenericProject.class, true).flush();

        // the display name is shared: the data file is the marker file
        // in the project directory
        assertNotNull("the auxiliary data file must exist in the project directory",
                root.getFileObject(GpAuxiliaryProperties.dataFileName()));
        // a fresh instance (fresh cache) must see the second value,
        // i.e. the second write has really reached the disk
        GenericProject nue = new GenericProject(root);
        assertEquals("Second Name", GenericProject.getDisplayNameOverride(nue));
    }

    public void testLocalProjectStoresAuxDataInUserConfig() throws Exception {
        FileObject root = newProjectDir();
        root.setAttribute(GenericProject.MARKER_ATTRIBUTE, Boolean.TRUE);
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);

        GenericProject.setDisplayNameOverride((GenericProject) prj, "My Fancy Project");
        ProjectUtils.getPreferences(prj, GenericProject.class, true).flush();

        // the data must live in the user configuration...
        assertNotNull("the shared auxiliary data file must be created in the user configuration",
                FileUtil.getConfigRoot().getFileObject(
                        GpAuxiliaryProperties.dataFolder(root) + "/" + GpAuxiliaryProperties.dataFileName()));
        // ...and nowhere in the project's working tree
        assertNull("no auxiliary data file may appear in the project directory",
                root.getFileObject(GpAuxiliaryProperties.dataFileName()));
        // no private setting was stored: no private data file either
        assertNull("no private auxiliary data file may be created for local projects",
                FileUtil.getConfigRoot().getFileObject(
                        GpAuxiliaryProperties.dataFolder(root) + "/" + GpAuxiliaryProperties.privateDataFileName()));
        assertNull("no auxiliary configuration attribute may be set on the project directory",
                root.getAttribute("org.netbeans.spi.project.AuxiliaryConfiguration"
                        + ".http://www.netbeans.org/ns/auxiliary-configuration-preferences/1#preferences"));
    }

    public void testMarkerToggledMovesSharedAuxData() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);

        // the shared data lives in the marker file (the marker file
        // is the shared data file)
        GenericProject.setFilesFilterPattern((GenericProject) prj, ".*-ignored"); // NOI18N
        ProjectUtils.getPreferences(prj, GenericProject.class, true).flush();
        assertNotNull(root.getFileObject(GpAuxiliaryProperties.dataFileName()));

        // turning the marker off moves the shared data to the user
        // configuration
        GenericProject.setMarkerState(root, false);
        assertEquals(".*-ignored", GenericProject.getFilesFilterPattern((GenericProject) prj));
        assertNull("the project-local data file must be removed",
                root.getFileObject(GpAuxiliaryProperties.dataFileName()));
        assertNotNull("the data must now live in the user configuration",
                FileUtil.getConfigRoot().getFileObject(
                        GpAuxiliaryProperties.dataFolder(root) + "/" + GpAuxiliaryProperties.dataFileName()));

        // and back on moves it back into the project directory
        GenericProject.setMarkerState(root, true);
        assertEquals(".*-ignored", GenericProject.getFilesFilterPattern((GenericProject) prj));
        assertNotNull("the data must be back in the project directory",
                root.getFileObject(GpAuxiliaryProperties.dataFileName()));
        assertNull("no shared auxiliary data may remain in the user configuration",
                FileUtil.getConfigRoot().getFileObject(
                        GpAuxiliaryProperties.dataFolder(root) + "/" + GpAuxiliaryProperties.dataFileName()));
    }

    public void testMarkerDeletedExternallyDropsSharedDataOnly() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);

        GenericProject.setDisplayNameOverride((GenericProject) prj, "My Fancy Project");
        GenericProject.setFilesFilterPattern((GenericProject) prj, ".*-ignored"); // NOI18N
        GenericProject.setGlobalFileFilterEnabled((GenericProject) prj, false);
        ProjectUtils.getPreferences(prj, GenericProject.class, true).flush();
        ProjectUtils.getPreferences(prj, GenericProject.class, false).flush();

        // the marker file is the shared data file: deleting it
        // outside of the IDE (while no instance is open to migrate
        // the data) drops the shared data with it...
        root.getFileObject(GenericProject.MARKER_FILE).delete();
        root.setAttribute(GenericProject.MARKER_ATTRIBUTE, Boolean.TRUE);

        GenericProject nue = new GenericProject(root);
        assertNull("the shared filter pattern must be gone with the marker file",
                GenericProject.getFilesFilterPattern(nue));
        assertNull("the shared display name override must be gone with the marker file",
                GenericProject.getDisplayNameOverride(nue));
        // ...while the private data, which lives in the user
        // configuration, survives
        assertFalse("the private filter toggle must survive the marker deletion",
                GenericProject.isGlobalFileFilterEnabled(nue));
        assertNull(root.getFileObject(GpAuxiliaryProperties.dataFileName()));
        assertNull("no shared auxiliary data may appear in the user configuration",
                FileUtil.getConfigRoot().getFileObject(
                        GpAuxiliaryProperties.dataFolder(root) + "/" + GpAuxiliaryProperties.dataFileName()));
    }

    public void testPreferencesSharedAndPrivateAreIsolated() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);

        Preferences shared = ProjectUtils.getPreferences(prj, GenericProject.class, true);
        Preferences priv = ProjectUtils.getPreferences(prj, GenericProject.class, false);
        shared.put("key", "shared-value"); // NOI18N
        priv.put("key", "private-value");  // NOI18N
        shared.flush();

        assertEquals("shared-value", shared.get("key", null));  // NOI18N
        assertEquals("private-value", priv.get("key", null));   // NOI18N
        assertTrue("the shared key must be listed for the shared area",
                List.of(shared.keys()).contains("key"));
        assertTrue("the private key must be listed for the private area",
                List.of(priv.keys()).contains("key"));
    }

    public void testPreferencesSurviveNewProjectInstance() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        Project prj = ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);

        GenericProject.setDisplayNameOverride((GenericProject) prj, "My Fancy Project");
        ProjectUtils.getPreferences(prj, GenericProject.class, true).flush();

        // a fresh instance (bypassing the ProjectManager's project
        // cache) must see the value, i.e. it has really been
        // persisted to disk
        GenericProject nue = new GenericProject(root);
        assertEquals("My Fancy Project", GenericProject.getDisplayNameOverride(nue));
    }

    public void testGlobalFileFilterEnabledByDefault() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        GenericProject prj = (GenericProject) ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);
        assertTrue("the global file filter must be enabled by default",
                GenericProject.isGlobalFileFilterEnabled(prj));
    }

    public void testGlobalFileFilterToggleRoundTrip() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        GenericProject prj = (GenericProject) ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);

        GenericProject.setGlobalFileFilterEnabled(prj, false);
        assertFalse(GenericProject.isGlobalFileFilterEnabled(prj));
        // the disabled state is the only one that must be stored
        assertEquals("false", ProjectUtils.getPreferences(prj, GenericProject.class, false)
                .get(GenericProject.GLOBAL_FILE_FILTER_KEY, null)); // NOI18N

        GenericProject.setGlobalFileFilterEnabled(prj, true);
        assertTrue(GenericProject.isGlobalFileFilterEnabled(prj));
        // the enabled state is the default and keeps the data file clean
        assertNull(ProjectUtils.getPreferences(prj, GenericProject.class, false)
                .get(GenericProject.GLOBAL_FILE_FILTER_KEY, null)); // NOI18N
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

    public void testFilesFilterPatternRoundTrip() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        GenericProject prj = (GenericProject) ProjectManager.getDefault().findProject(root);
        assertNotNull(prj);
        assertNull("no additional pattern by default",
                GenericProject.getFilesFilterPattern(prj));

        GenericProject.setFilesFilterPattern(prj, ".*-old.*"); // NOI18N
        assertEquals(".*-old.*", GenericProject.getFilesFilterPattern(prj));
        // the pattern is project content and lives in the shared area
        assertEquals(".*-old.*", ProjectUtils.getPreferences(prj, GenericProject.class, true)
                .get(GenericProject.FILES_FILTER_PATTERN_KEY, null)); // NOI18N

        // a blank pattern is the same as none
        GenericProject.setFilesFilterPattern(prj, "   "); // NOI18N
        assertNull(GenericProject.getFilesFilterPattern(prj));
        assertNull(ProjectUtils.getPreferences(prj, GenericProject.class, true)
                .get(GenericProject.FILES_FILTER_PATTERN_KEY, null)); // NOI18N
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

    /**
     * The key under which the given setting of this module is stored
     * in an auxiliary data file: the area prefix ({@code shared} or
     * {@code private}, plus a dot) and the code-name-base of the
     * module owning {@link GenericProject} (dots replaced by dashes,
     * as the auxiliary properties preferences provider builds the
     * key).
     */
    private static String auxKey(String area, String key) {
        return area + ".org-netbeans-modules-generic-project." + key; // NOI18N
    }

    private static Properties readDataFile(FileObject file) throws Exception {
        Properties props = new Properties();
        try (InputStream is = file.getInputStream()) {
            props.load(is);
        }
        return props;
    }

    public interface TestMarker {
    }

    @ProjectServiceProvider(service=TestMarker.class, projectType=GenericProject.PROJECT_KEY)
    public static class TestMarkerProvider implements TestMarker {
    }

}
