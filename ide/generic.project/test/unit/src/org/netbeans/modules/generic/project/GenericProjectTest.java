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
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
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
        assertFalse(new GenericProject.GenericProjectFactory().isProject(root));
    }

    public void testMarkerFileIsProject() throws Exception {
        FileObject root = newProjectDir();
        FileUtil.createData(root, GenericProject.MARKER_FILE);
        assertTrue(new GenericProject.GenericProjectFactory().isProject(root));
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

    private FileObject newProjectDir() throws Exception {
        clearWorkDir();
        File dir = new File(getWorkDir(), "proj-" + getName()); // NOI18N
        assertTrue(dir.mkdirs() || dir.isDirectory());
        return FileUtil.toFileObject(dir);
    }

}
