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

package org.netbeans.spi.project.support.ant;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.openide.util.test.MockLookup;

/**
 * Test functionality of SimpleAntArtifact.
 * @author Jesse Glick
 */
public class SimpleAntArtifactTest extends NbTestCase {
    
    public SimpleAntArtifactTest(String name) {
        super(name);
    }
    
    private FileObject scratch;
    private FileObject sisterprojdir;
    private ProjectManager pm;
    private AntProjectHelper sisterh;
    
    protected void setUp() throws Exception {
        super.setUp();
        scratch = TestUtil.makeScratchDir(this);
        MockLookup.setInstances(AntBasedTestUtil.testAntBasedProjectType());
        pm = ProjectManager.getDefault();
        sisterprojdir = FileUtil.createFolder(scratch, "proj2");
        sisterh = ProjectGenerator.createProject(sisterprojdir, "test");
        EditableProperties props = sisterh.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("build.jar", "build/proj2.jar");
        props.setProperty("build.jar.absolute", getWorkDir().getAbsolutePath()+"/build/proj3.jar");
        sisterh.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
    }
    
    protected void tearDown() throws Exception {
        scratch = null;
        sisterprojdir = null;
        sisterh = null;
        pm = null;
        super.tearDown();
    }
    
    /**
     * Check that {@link SimpleAntArtifact} works as documented.
     */
    public void testSimpleAntArtifact() throws Exception {
        AntArtifact art = sisterh.createSimpleAntArtifact("jar", "build.jar", sisterh.getStandardPropertyEvaluator(), "dojar", "clean");
        assertEquals("correct type", "jar", art.getType());
        assertEquals("correct target name", "dojar", art.getTargetName());
        assertEquals("correct clean target name", "clean", art.getCleanTargetName());
        assertEquals("correct artifact location", URI.create("build/proj2.jar"), art.getArtifactLocations()[0]);
        assertEquals("no artifact file yet", 0, art.getArtifactFiles().length);
        FileObject artfile = FileUtil.createData(sisterprojdir, "build/proj2.jar");
        assertEquals("now have an artifact file", Collections.singletonList(artfile), Arrays.asList(art.getArtifactFiles()));
        assertEquals("correct script location", new File(FileUtil.toFile(sisterprojdir), "build.xml"), art.getScriptLocation());
        assertEquals("no script file yet", null, art.getScriptFile());
        FileObject scriptfile = FileUtil.createData(sisterprojdir, "build.xml");
        assertEquals("now have a script file", scriptfile, art.getScriptFile());
        assertEquals("correct project", pm.findProject(sisterprojdir), art.getProject());
        
        art = sisterh.createSimpleAntArtifact("jar", "build.jar.absolute", sisterh.getStandardPropertyEvaluator(), "dojar", "clean");
        assertEquals("correct artifact location", Utilities.toURI((new File(getWorkDir().getAbsolutePath()+"/build/proj3.jar"))), art.getArtifactLocations()[0]);
    }
    
}
