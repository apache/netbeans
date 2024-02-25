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
package org.netbeans.modules.java.lsp.server.debugging.launch;

import java.io.IOException;
import java.util.Collection;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.SingleMethod;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

public class NbLaunchDelegateTest {

    public NbLaunchDelegateTest() {
    }

    @Test
    public void testFileObjectsLookup() throws Exception {
        FileObject fo = FileUtil.createMemoryFileSystem().getRoot().createData("test.txt");
        Lookup lkp = NbLaunchDelegate.createTargetLookup(null, null, fo);
        assertEquals(fo, lkp.lookup(FileObject.class));

        DataObject obj = lkp.lookup(DataObject.class);
        assertNotNull("DataObject also found", obj);

        assertEquals("It's FileObject's data object", obj, DataObject.find(fo));
        assertNull("No single method", lkp.lookup(SingleMethod.class));
    }

    @Test
    public void testFileObjectsLookupWithSingleMethod() throws Exception {
        FileObject fo = FileUtil.createMemoryFileSystem().getRoot().createData("test-with-method.txt");
        SingleMethod m = new SingleMethod(fo, "main");
        Lookup lkp = NbLaunchDelegate.createTargetLookup(null, m, fo);
        assertEquals(fo, lkp.lookup(FileObject.class));

        DataObject obj = lkp.lookup(DataObject.class);
        assertNotNull("DataObject also found", obj);

        assertEquals("It's FileObject's data object", obj, DataObject.find(fo));

        assertEquals("Found single method", m, lkp.lookup(SingleMethod.class));
    }

    @Test
    public void testFindsMavenProject() throws Exception {
        FileObject dir = FileUtil.createMemoryFileSystem().getRoot().createFolder("testprj");
        FileObject xml = dir.createData("build.xml");
        Project prj = ProjectManager.getDefault().findProject(dir);
        assertNotNull("Project dir recognized", prj);

        SingleMethod m = new SingleMethod(xml, "main");
        Lookup lkp = NbLaunchDelegate.createTargetLookup(prj, null, null);
        assertNull("No file object", lkp.lookup(FileObject.class));
        DataObject obj = lkp.lookup(DataObject.class);
        assertNull("No DataObject ", obj);
        assertNull("No single method", lkp.lookup(SingleMethod.class));
        assertEquals(prj, lkp.lookup(Project.class));
    }

    @Test
    public void testFindsWithFileObjectAndDataObject() throws Exception {
        FileObject dir = FileUtil.createMemoryFileSystem().getRoot().createFolder("testprj");
        FileObject xml = dir.createData("build.xml");
        Project prj = ProjectManager.getDefault().findProject(dir);
        assertNotNull("Project dir recognized", prj);

        SingleMethod m = new SingleMethod(xml, "main");
        Lookup lkp = NbLaunchDelegate.createTargetLookup(prj, m, xml);
        assertEquals("File object is available", xml, lkp.lookup(FileObject.class));
        DataObject obj = lkp.lookup(DataObject.class);
        assertNotNull("DataObject is available", obj);
        assertEquals("DataObject's FileObject is correct", xml, obj.getPrimaryFile());
        assertEquals("Single method is also present", m, lkp.lookup(SingleMethod.class));
        assertEquals(prj, lkp.lookup(Project.class));
    }

    @Test
    public void testFindsWithFileObjectAndDataObjectNoMethod() throws Exception {
        FileObject dir = FileUtil.createMemoryFileSystem().getRoot().createFolder("testprj");
        FileObject xml = dir.createData("build.xml");
        Project prj = ProjectManager.getDefault().findProject(dir);
        assertNotNull("Project dir recognized", prj);

        Lookup lkp = NbLaunchDelegate.createTargetLookup(prj, null, xml);
        assertEquals("File object is available", xml, lkp.lookup(FileObject.class));
        DataObject obj = lkp.lookup(DataObject.class);
        assertNotNull("DataObject is available", obj);
        assertEquals("DataObject's FileObject is correct", xml, obj.getPrimaryFile());
        assertNull("No Single method is present", lkp.lookup(SingleMethod.class));
        assertEquals(prj, lkp.lookup(Project.class));
    }

    @Test
    public void testAvoidNPEWithoutActionsProviderInLookup() throws Exception {
        MockProjectFactory.MockProject prj = new MockProjectFactory.MockProject(FileUtil.getConfigRoot());
        Collection<ActionProvider> all = NbLaunchDelegate.findActionProviders(prj);
        assertFalse("There shall be no null in " + all, all.contains(null));
    }

    @ServiceProvider(service = ProjectFactory.class)
    public static final class MockProjectFactory implements ProjectFactory {

        @Override
        public boolean isProject(FileObject projectDirectory) {
            return projectDirectory.getNameExt().equals("testprj");
        }

        @Override
        public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
            if (isProject(projectDirectory)) {
                return new MockProject(projectDirectory);
            } else {
                return null;
            }
        }

        @Override
        public void saveProject(Project project) throws IOException, ClassCastException {
        }

        private static final class MockProject implements Project {
            private final FileObject dir;

            MockProject(FileObject dir) {
                this.dir = dir;
            }

            @Override
            public FileObject getProjectDirectory() {
                return dir;
            }

            @Override
            public Lookup getLookup() {
                return Lookups.fixed(this);
            }

        }
    }
}
