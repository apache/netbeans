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
package org.netbeans.modules.gradle.nodes;

import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.gradle.AbstractGradleProjectTestCase;
import org.netbeans.modules.gradle.NbGradleProjectImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;
import org.openide.nodes.Node;

/**
 *
 * @author lkishalmi
 */
public class BuildScriptsNodeTest extends AbstractGradleProjectTestCase {

    private FileObject root;

    public BuildScriptsNodeTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        LocalFileSystem fs = new LocalFileSystem();
        fs.setRootDirectory(getWorkDir());
        root = fs.getRoot();
    }

    @Test
    public void testGradleOnRoot() throws Exception {
        FileObject a = createGradleProject("projectA",
                "apply plugin: 'java'\n", "");
        Project prjA = ProjectManager.getDefault().findProject(a);
        BuildScriptsNode node = new BuildScriptsNode((NbGradleProjectImpl)prjA);
        Node build = childNodeFor(node, a.getFileObject("build.gradle"));
        assertNotNull(build);
        assertEquals("build.gradle", build.getDisplayName());
        Node settings = childNodeFor(node, a.getFileObject("settings.gradle"));
        assertNotNull(settings);
        assertEquals("settings.gradle", settings.getDisplayName());
        assertNull(node.getChildren().findChild("buildSrc"));
    }

    @Test
    public void testGradleWithBuildSrc() throws Exception {
        FileObject a = createGradleProject("projectA",
                "apply plugin: 'java'\n", "");
        FileObject b = a.createFolder("buildSrc");
        Project prjA = ProjectManager.getDefault().findProject(a);
        BuildScriptsNode node = new BuildScriptsNode((NbGradleProjectImpl)prjA);
        Node build = childNodeFor(node, a.getFileObject("build.gradle"));
        assertNotNull(build);
        assertEquals("build.gradle", build.getDisplayName());
        Node settings = childNodeFor(node, a.getFileObject("settings.gradle"));
        assertNotNull(settings);
        assertEquals("settings.gradle", settings.getDisplayName());
        Node buildSrc = childNodeFor(node, b);
        assertNotNull(buildSrc);
    }

    @Test
    public void testGradleOnBuildSrc() throws Exception {
        FileObject a = createGradleProject("projectA",
                "apply plugin: 'java'\n", "");
        FileObject b = a.createFolder("buildSrc");
        Project prjA = ProjectManager.getDefault().findProject(a);
        Project prjB = ProjectManager.getDefault().findProject(b);
        BuildScriptsNode node = new BuildScriptsNode((NbGradleProjectImpl)prjB);
        assertNull(childNodeFor(node, b));
        assertNull(childNodeFor(node, a.getFileObject("build.gradle")));
        assertNull(childNodeFor(node, a.getFileObject("settings.gradle")));
    }

    @Test
    public void testGradleWithSubProject() throws Exception {
        FileObject a = createGradleProject("projectA",
                "", "include 'projectB'");
        FileObject gprops = a.createData("gradle.properties");
        FileObject b = createGradleProject("projectA/projectB",
                "apply plugin: 'java'\n", null);
        Project prjA = ProjectManager.getDefault().findProject(a);
        Project prjB = ProjectManager.getDefault().findProject(b);
        BuildScriptsNode node = new BuildScriptsNode((NbGradleProjectImpl)prjB);
        Node build = node.getChildren().findChild("build");
        assertNotNull(build);
        assertEquals("build.gradle [root]", build.getDisplayName());
        Node settings = node.getChildren().findChild("settings");
        assertNotNull(settings);
        assertEquals("settings.gradle", settings.getDisplayName());
        Node gradle = childNodeFor(node, gprops);
        assertNotNull(gradle);
        assertEquals("gradle.properties [root]", gradle.getDisplayName());

    }

    private static Node childNodeFor(Node node, FileObject fo) {
        for (Node child : node.getChildren().getNodes(true)) {
            FileObject cfo = child.getLookup().lookup(FileObject.class);
            if (fo.equals(cfo)) {
                return child;
            }
        }
        return null;
    }
}
