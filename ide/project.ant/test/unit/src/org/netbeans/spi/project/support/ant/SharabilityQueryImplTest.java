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
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.openide.util.test.MockLookup;

/**
 * Test functionality of SharabilityQueryImpl.
 * @author Jesse Glick
 */
public class SharabilityQueryImplTest extends NbTestCase {

    public SharabilityQueryImplTest(String name) {
        super(name);
    }
    
    /** Location of top of testing dir (contains projdir and external). */
    private File scratchF;
    /** Tested impl. */
    private SharabilityQueryImplementation2 sqi;
    
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockLookup.setInstances(AntBasedTestUtil.testAntBasedProjectType());
        FileObject scratch = TestUtil.makeScratchDir(this);
        scratchF = FileUtil.toFile(scratch);
        FileObject projdir = scratch.createFolder("projdir");
        AntProjectHelper h = ProjectGenerator.createProject(projdir, "test");
        assertEquals("right project directory", projdir, h.getProjectDirectory());
        EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("build.dir", "build");
        props.setProperty("build2.dir", "build/2");
        props.setProperty("dist.dir", "dist");
        props.setProperty("src.dir", "src");
        File externalF = new File(scratchF, "external");
        props.setProperty("src2.dir", new File(externalF, "src").getAbsolutePath());
        props.setProperty("build3.dir", new File(externalF, "build").getAbsolutePath());
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        ProjectManager.getDefault().saveProject(ProjectManager.getDefault().findProject(projdir));
        sqi = h.createSharabilityQuery2(h.getStandardPropertyEvaluator(), new String[] {"${src.dir}", "${src2.dir}"},
                                       new String[] {"${build.dir}", "${build2.dir}", "${build3.dir}", "${dist.dir}"});
    }
    
    private URI file(String path) {
        return Utilities.toURI(new File(scratchF, path.replace('/', File.separatorChar)));
    }
    
    public void testBasicIncludesExcludes() throws Exception {
        assertEquals("project directory is mixed", SharabilityQuery.Sharability.MIXED, sqi.getSharability(file("projdir")));
        assertEquals("build.xml is sharable", SharabilityQuery.Sharability.SHARABLE, sqi.getSharability(file("projdir/build.xml")));
        assertEquals("src/ is sharable", SharabilityQuery.Sharability.SHARABLE, sqi.getSharability(file("projdir/src")));
        assertEquals("src/org/foo/ is sharable", SharabilityQuery.Sharability.SHARABLE, sqi.getSharability(file("projdir/src/org/foo")));
        assertEquals("src/org/foo/Foo.java is sharable", SharabilityQuery.Sharability.SHARABLE, sqi.getSharability(file("projdir/src/org/foo/Foo.java")));
        assertEquals("nbproject/ is mixed", SharabilityQuery.Sharability.MIXED, sqi.getSharability(file("projdir/nbproject")));
        assertEquals("nbproject/project.xml is sharable", SharabilityQuery.Sharability.SHARABLE, sqi.getSharability(file("projdir/nbproject/project.xml")));
        assertEquals("nbproject/private/ is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("projdir/nbproject/private")));
        assertEquals("nbproject/private/private.properties is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("projdir/nbproject/private/private.properties")));
        assertEquals("build/ is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("projdir/build")));
        assertEquals("build/classes/org/foo/Foo.class is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("projdir/build/classes/org/foo/Foo.class")));
        assertEquals("dist/ is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("projdir/dist")));
    }
    
    public void testOverlaps() throws Exception {
        assertEquals("build/2/ is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("projdir/build/2")));
        assertEquals("build/2/whatever is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("projdir/build/2/whatever")));
        // overlaps in includePaths tested in basicIncludesExcludes: src is inside projdir
    }
    
    public void testExternalFiles() throws Exception {
        assertEquals("external/src is sharable", SharabilityQuery.Sharability.SHARABLE, sqi.getSharability(file("external/src")));
        assertEquals("external/src/org/foo/Foo.java is sharable", SharabilityQuery.Sharability.SHARABLE, sqi.getSharability(file("external/src/org/foo/Foo.java")));
        assertEquals("external/build is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("external/build")));
        assertEquals("external/build/classes/org/foo/Foo.class is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("external/build/classes/org/foo/Foo.class")));
    }
    
    public void testUnknownFiles() throws Exception {
        assertEquals("some other dir is unknown", SharabilityQuery.Sharability.UNKNOWN, sqi.getSharability(file("something")));
        assertEquals("some other file is unknown", SharabilityQuery.Sharability.UNKNOWN, sqi.getSharability(file("something/else")));
        assertEquals("external itself is unknown", SharabilityQuery.Sharability.UNKNOWN, sqi.getSharability(file("external")));
    }
    
    public void testDirNamesEndingInSlash() throws Exception {
        assertEquals("project directory is mixed", SharabilityQuery.Sharability.MIXED, sqi.getSharability(file("projdir/")));
        assertEquals("src/ is sharable", SharabilityQuery.Sharability.SHARABLE, sqi.getSharability(file("projdir/src/")));
        assertEquals("src/org/foo/ is sharable", SharabilityQuery.Sharability.SHARABLE, sqi.getSharability(file("projdir/src/org/foo/")));
        assertEquals("nbproject/ is mixed", SharabilityQuery.Sharability.MIXED, sqi.getSharability(file("projdir/nbproject/")));
        assertEquals("nbproject/private/ is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("projdir/nbproject/private/")));
        assertEquals("build/ is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("projdir/build/")));
        assertEquals("dist/ is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("projdir/dist/")));
        assertEquals("build/2/ is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("projdir/build/2/")));
        assertEquals("some other dir is unknown", SharabilityQuery.Sharability.UNKNOWN, sqi.getSharability(file("something/")));
        assertEquals("external itself is unknown", SharabilityQuery.Sharability.UNKNOWN, sqi.getSharability(file("external/")));
        assertEquals("external/src is sharable", SharabilityQuery.Sharability.SHARABLE, sqi.getSharability(file("external/src/")));
        assertEquals("external/build is not sharable", SharabilityQuery.Sharability.NOT_SHARABLE, sqi.getSharability(file("external/build/")));
    }
    
    public void testSubprojectFiles() throws Exception {
        assertEquals("nbproject/private from a subproject is sharable as far as this impl is concerned", SharabilityQuery.Sharability.SHARABLE, sqi.getSharability(file("projdir/subproj/nbproject/private")));
    }
    
    // XXX testChangedProperties
    // XXX testExternalSourceDirs
    
}
