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
import java.io.FileOutputStream;
import java.io.OutputStream;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

// XXX testChangesFromAntPropertyChanges
import org.openide.util.test.MockChangeListener;
import org.openide.util.test.MockLookup;

/**
 * Test functionality of GlobFileBuiltQuery.
 * @author Jesse Glick
 */
public class GlobFileBuiltQueryTest extends NbTestCase {
    
    static {
        MockLookup.setInstances(AntBasedTestUtil.testAntBasedProjectType());
    }
    
    public GlobFileBuiltQueryTest(String name) {
        super(name);
    }
    
    private FileObject scratch;
    private FileObject prj;
    private FileObject extsrc;
    private FileObject extbuild;
    private AntProjectHelper h;
    private FileBuiltQueryImplementation fbqi;
    private FileObject foo, bar, fooTest, baz, nonsense;
    private FileBuiltQuery.Status fooStatus, barStatus, fooTestStatus, bazStatus;
    
    protected void setUp() throws Exception {
        super.setUp();
        scratch = TestUtil.makeScratchDir(this);
        prj = scratch.createFolder("prj");
        h = ProjectGenerator.createProject(prj, "test");
        extsrc = scratch.createFolder("extsrc");
        extbuild = scratch.createFolder("extbuild");
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty("src.dir", "src");
        ep.setProperty("test.src.dir", "test/src");
        ep.setProperty("ext.src.dir", "../extsrc");
        ep.setProperty("build.classes.dir", "build/classes");
        ep.setProperty("test.build.classes.dir", "build/test/classes");
        ep.setProperty("ext.build.classes.dir", "../extbuild/classes");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ProjectManager.getDefault().saveProject(ProjectManager.getDefault().findProject(prj));
        foo = TestUtil.createFileFromContent(null, prj, "src/pkg/Foo.java");
        bar = TestUtil.createFileFromContent(null, prj, "src/pkg/Bar.java");
        fooTest = TestUtil.createFileFromContent(null, prj, "test/src/pkg/FooTest.java");
        baz = TestUtil.createFileFromContent(null, extsrc, "pkg2/Baz.java");
        nonsense = TestUtil.createFileFromContent(null, prj, "misc-src/whatever/Nonsense.java");
        fbqi = h.createGlobFileBuiltQuery(h.getStandardPropertyEvaluator(), new String[] {
            "${src.dir}/*.java",
            "${test.src.dir}/*.java",
            "${ext.src.dir}/*.java",
        }, new String[] {
            "${build.classes.dir}/*.class",
            "${test.build.classes.dir}/*.class",
            "${ext.build.classes.dir}/*.class",
        });
        fooStatus = fbqi.getStatus(foo);
        barStatus = fbqi.getStatus(bar);
        fooTestStatus = fbqi.getStatus(fooTest);
        bazStatus = fbqi.getStatus(baz);
    }
    
    /** Enough time (millisec) for file timestamps to be different. */
    private static final long PAUSE = 1500;
    
    public void testBasicFunctionality() throws Exception {
        assertNotNull("have status for Foo.java", fooStatus);
        assertNotNull("have status for Bar.java", barStatus);
        assertNotNull("have status for FooTest.java", fooTestStatus);
        assertNull("non-matching file ignored", fbqi.getStatus(nonsense));
        assertFalse("Foo.java not built", fooStatus.isBuilt());
        assertFalse("Bar.java not built", barStatus.isBuilt());
        assertFalse("FooTest.java not built", fooTestStatus.isBuilt());
        FileObject fooClass = TestUtil.createFileFromContent(null, prj, "build/classes/pkg/Foo.class");
        assertTrue("Foo.java now built", fooStatus.isBuilt());
        Thread.sleep(PAUSE);
        TestUtil.createFileFromContent(null, prj, "src/pkg/Foo.java");
        assertFalse("Foo.class out of date", fooStatus.isBuilt());
        TestUtil.createFileFromContent(null, prj, "build/classes/pkg/Foo.class");
        assertTrue("Foo.class rebuilt", fooStatus.isBuilt());
        fooClass.delete();
        assertFalse("Foo.class deleted", fooStatus.isBuilt());
        TestUtil.createFileFromContent(null, prj, "build/test/classes/pkg/FooTest.class");
        assertTrue("FooTest.java now built", fooTestStatus.isBuilt());
        assertFalse("Bar.java still not built", barStatus.isBuilt());
        TestUtil.createFileFromContent(null, prj, "build/classes/pkg/Foo.class");
        assertTrue("Foo.java built again", fooStatus.isBuilt());
        DataObject.find(foo).setModified(true);
        assertFalse("Foo.java modified", fooStatus.isBuilt());
        DataObject.find(foo).setModified(false);
        assertTrue("Foo.java unmodified again", fooStatus.isBuilt());
        FileObject buildDir = prj.getFileObject("build");
        assertNotNull("build dir exists", buildDir);
        buildDir.delete();
        assertFalse("Foo.java not built (build dir gone)", fooStatus.isBuilt());
        assertFalse("Bar.java still not built", barStatus.isBuilt());
        assertFalse("FooTest.java not built (build dir gone)", fooTestStatus.isBuilt());
        // Just to check that you can delete a source file safely:
        bar.delete();
        barStatus.isBuilt();
    }
    
    /** Maximum amount of time (in milliseconds) to wait for expected changes. */
    private static final long WAIT = 10000;
    /** Maximum amount of time (in milliseconds) to wait for unexpected changes. */
    private static final long QUICK_WAIT = 500;

    @RandomlyFails // NB-Core-Build #1755
    public void testChangeFiring() throws Exception {
        MockChangeListener fooL = new MockChangeListener();
        fooStatus.addChangeListener(fooL);
        MockChangeListener barL = new MockChangeListener();
        barStatus.addChangeListener(barL);
        MockChangeListener fooTestL = new MockChangeListener();
        fooTestStatus.addChangeListener(fooTestL);
        assertFalse("Foo.java not built", fooStatus.isBuilt());
        FileObject fooClass = TestUtil.createFileFromContent(null, prj, "build/classes/pkg/Foo.class");
        fooL.msg("change in Foo.java").expectEvent(WAIT);
        assertTrue("Foo.java now built", fooStatus.isBuilt());
        fooL.msg("no more changes in Foo.java").expectNoEvents(QUICK_WAIT);
        fooClass.delete();
        fooL.msg("change in Foo.java").expectEvent(WAIT);
        assertFalse("Foo.java no longer built", fooStatus.isBuilt());
        fooTestL.msg("no changes yet in FooTest.java").expectNoEvents(QUICK_WAIT);
        assertFalse("FooTest.java not yet built", fooTestStatus.isBuilt());
        FileObject fooTestClass = TestUtil.createFileFromContent(null, prj, "build/test/classes/pkg/FooTest.class");
        fooTestL.msg("change in FooTest.java").expectEvent(WAIT);
        assertTrue("FooTest.java now built", fooTestStatus.isBuilt());
        FileObject buildDir = prj.getFileObject("build");
        assertNotNull("build dir exists", buildDir);
        buildDir.delete();
        fooL.msg("no change in Foo.java (still not built)").expectNoEvents(QUICK_WAIT);
        assertFalse("Foo.java not built (build dir gone)", fooStatus.isBuilt());
        fooTestL.msg("got change in FooTest.java (build dir gone)").expectEvent(WAIT);
        assertFalse("FooTest.java not built (build dir gone)", fooTestStatus.isBuilt());
        barL.msg("never got changes in Bar.java (never built)").expectNoEvents(QUICK_WAIT);
        TestUtil.createFileFromContent(null, prj, "build/classes/pkg/Foo.class");
        fooL.msg("change in Foo.class").expectEvent(WAIT);
        assertTrue("Foo.class created", fooStatus.isBuilt());
        Thread.sleep(PAUSE);
        TestUtil.createFileFromContent(null, prj, "src/pkg/Foo.java");
        fooL.msg("change in Foo.java").expectEvent(WAIT);
        assertFalse("Foo.class out of date", fooStatus.isBuilt());
        TestUtil.createFileFromContent(null, prj, "build/classes/pkg/Foo.class");
        fooL.msg("touched Foo.class").expectEvent(WAIT);
        assertTrue("Foo.class touched", fooStatus.isBuilt());
        DataObject.find(foo).setModified(true);
        fooL.msg("Foo.java modified in memory").expectEvent(WAIT);
        assertFalse("Foo.java modified in memory", fooStatus.isBuilt());
        DataObject.find(foo).setModified(false);
        fooL.msg("Foo.java unmodified in memory").expectEvent(WAIT);
        assertTrue("Foo.java unmodified again", fooStatus.isBuilt());
        File buildF = new File(FileUtil.toFile(prj), "build");
        assertTrue("build dir exists", buildF.isDirectory());
        TestUtil.deleteRec(buildF);
        assertFalse(buildF.getAbsolutePath() + " is gone", buildF.exists());
        prj.getFileSystem().refresh(false);
        fooL.msg("build dir deleted").expectEvent(WAIT);
        assertFalse("Foo.class gone (no build dir)", fooStatus.isBuilt());
        File pkg = new File(buildF, "classes/pkg".replace('/', File.separatorChar));
        File fooClassF = new File(pkg, "Foo.class");
        //System.err.println("--> going to make " + fooClassF);
        assertTrue("created " + pkg, pkg.mkdirs());
        assertFalse("no such file yet: " + fooClassF, fooClassF.exists());
        OutputStream os = new FileOutputStream(fooClassF);
        os.close();
        prj.getFileSystem().refresh(false);
        fooL.msg(fooClassF.getAbsolutePath() + " created on disk").expectEvent(WAIT);
        assertTrue("Foo.class back", fooStatus.isBuilt());
        Thread.sleep(PAUSE);
        TestUtil.createFileFromContent(null, prj, "src/pkg/Foo.java");
        fooL.msg("change in Foo.java").expectEvent(WAIT);
        assertFalse("Foo.class out of date", fooStatus.isBuilt());
        os = new FileOutputStream(fooClassF);
        os.write(69); // force Mac OS X to update timestamp
        os.close();
        prj.getFileSystem().refresh(false);
        fooL.msg("Foo.class recreated on disk").expectEvent(WAIT);
        assertTrue("Foo.class touched", fooStatus.isBuilt());
    }

    public void testExternalSourceRoots() throws Exception {
        // Cf. #43609.
        assertNotNull("have status for Baz.java", bazStatus);
        MockChangeListener bazL = new MockChangeListener();
        bazStatus.addChangeListener(bazL);
        assertFalse("Baz.java not built", bazStatus.isBuilt());
        FileObject bazClass = TestUtil.createFileFromContent(null, extbuild, "classes/pkg2/Baz.class");
        bazL.msg("got change").expectEvent(WAIT);
        assertTrue("Baz.java now built", bazStatus.isBuilt());
        Thread.sleep(PAUSE);
        TestUtil.createFileFromContent(null, extsrc, "pkg2/Baz.java");
        bazL.msg("got change").expectEvent(WAIT);
        assertFalse("Baz.class out of date", bazStatus.isBuilt());
        TestUtil.createFileFromContent(null, extbuild, "classes/pkg2/Baz.class");
        bazL.msg("got change").expectEvent(WAIT);
        assertTrue("Baz.class rebuilt", bazStatus.isBuilt());
        bazClass.delete();
        bazL.msg("got change").expectEvent(WAIT);
        assertFalse("Baz.class deleted", bazStatus.isBuilt());
        TestUtil.createFileFromContent(null, extbuild, "classes/pkg2/Baz.class");
        bazL.msg("got change").expectEvent(WAIT);
        assertTrue("Baz.java built again", bazStatus.isBuilt());
        DataObject.find(baz).setModified(true);
        bazL.msg("got change").expectEvent(WAIT);
        assertFalse("Baz.java modified", bazStatus.isBuilt());
        DataObject.find(baz).setModified(false);
        bazL.msg("got change").expectEvent(WAIT);
        assertTrue("Baz.java unmodified again", bazStatus.isBuilt());
        extbuild.delete();
        bazL.msg("got change").expectEvent(WAIT);
        assertFalse("Baz.java not built (build dir gone)", bazStatus.isBuilt());
    }
    
    public void testFileRenames() throws Exception {
        // Cf. #45694.
        assertNotNull("have status for Foo.java", fooStatus);
        MockChangeListener fooL = new MockChangeListener();
        fooStatus.addChangeListener(fooL);
        assertFalse("Foo.java not built", fooStatus.isBuilt());
        FileObject fooClass = TestUtil.createFileFromContent(null, prj, "build/classes/pkg/Foo.class");
        fooL.msg("got change").expectEvent(WAIT);
        assertTrue("Foo.java now built", fooStatus.isBuilt());
        FileLock lock = foo.lock();
        try {
            foo.rename(lock, "Foo2", "java");
        } finally {
            lock.releaseLock();
        }
        fooL.msg("got change").expectEvent(WAIT);
        assertFalse("Foo2.java no longer built", fooStatus.isBuilt());
        fooClass = TestUtil.createFileFromContent(null, prj, "build/classes/pkg/Foo2.class");
        fooL.msg("got change").expectEvent(WAIT);
        assertTrue("Now Foo2.java is built", fooStatus.isBuilt());
    }
    
    /**See issue #66713.
     */
    public void testInvalidFile() throws Exception {
        FileObject baz = TestUtil.createFileFromContent(null, prj, "src/pkg/Baz.java");
        
        baz.delete();
        assertNull(fbqi.getStatus(baz));
    }
    
}
