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

package org.netbeans.modules.java.j2seproject.queries;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.TestUtil;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.BaseUtilities;
import org.openide.util.Mutex;
import org.openide.util.test.MockLookup;

/**
 * Tests for BinaryForSourceQueryImpl
 *
 * @author Tomas Zezula
 */
public class BinaryForSourceQueryImplTest extends NbTestCase {
    
    public BinaryForSourceQueryImplTest(String testName) {
        super(testName);
    }
    
    private FileObject scratch;
    private FileObject projdir;
    private FileObject sources;
    private FileObject tests;
    private FileObject buildClasses;
    private FileObject buildTestClasses;
    private URL distJarURL;
    private ProjectManager pm;
    private J2SEProject pp;
    AntProjectHelper helper;
    
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        pm = null;
        super.tearDown();
    }
    
    
    private void prepareProject () throws IOException {
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");        
        MockLookup.setLayersAndInstances();
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.6"));   //NOI18N
        helper = J2SEProjectGenerator.createProject(FileUtil.toFile(projdir),"proj",null,null,null, false);
        J2SEProjectGenerator.setDefaultSourceLevel(null);   //NOI18N
        pm = ProjectManager.getDefault();
        pp = Optional.ofNullable(pm.findProject(projdir))
                .map((p) -> p.getLookup().lookup(J2SEProject.class))
                .orElseThrow(()-> new AssertionError("No J2SEProject in " + FileUtil.getFileDisplayName(projdir)));
        sources = projdir.getFileObject("src");
        tests = FileUtil.createFolder(projdir, "test");
        FileObject fo = projdir.createFolder("build");
        buildClasses = fo.createFolder("classes");
        buildTestClasses = FileUtil.createFolder(fo, "test/classes");
        distJarURL = FileUtil.urlForArchiveOrDir(FileUtil.normalizeFile(
                helper.resolveFile(pp.evaluator().getProperty(ProjectProperties.DIST_JAR))));
    }
    
    public void testBinaryForSourceQuery() throws Exception {
        this.prepareProject();
        FileObject folder = scratch.createFolder("SomeFolder");
        BinaryForSourceQuery.Result result = BinaryForSourceQuery.findBinaryRoots(folder.toURL());
        assertEquals("Non-project folder does not have any source folder", 0, result.getRoots().length);
        folder = projdir.createFolder("SomeFolderInProject");
        result = BinaryForSourceQuery.findBinaryRoots(folder.toURL());
        assertEquals("Project non build folder does not have any source folder", 0, result.getRoots().length);
        result = BinaryForSourceQuery.findBinaryRoots(sources.toURL());
        assertEquals("Project build folder must have source folder", 2, result.getRoots().length);
        assertEquals("Project build folder must have source folder",buildClasses.toURL(),result.getRoots()[0]);
        assertEquals("Project jar artifact must have source folder", distJarURL, result.getRoots()[1]);
        BinaryForSourceQuery.Result result2 = BinaryForSourceQuery.findBinaryRoots(sources.toURL());
        assertTrue (result == result2);
    }

    public void testChanges() throws Exception {
        prepareProject();
        final SourceRoots src = ((J2SEProject)pp).getSourceRoots();
        final SourceRoots tsts = ((J2SEProject)pp).getTestSourceRoots();
        assertTrue(Arrays.equals(new URL[] {sources.toURL()}, src.getRootURLs()));
        assertTrue(Arrays.equals(new URL[] {tests.toURL()}, tsts.getRootURLs()));
        BinaryForSourceQuery.Result r = BinaryForSourceQuery.findBinaryRoots(sources.toURL());
        assertNotNull(r);
        URL[] roots = r.getRoots();
        assertTrue(Arrays.equals(
                new URL[] {
                    buildClasses.toURL(),
                    distJarURL
                },
                roots));
        r = BinaryForSourceQuery.findBinaryRoots(tests.toURL());
        assertNotNull(r);
        roots = r.getRoots();
        assertTrue(Arrays.equals(new URL[] {buildTestClasses.toURL()}, roots));
        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
            @Override
            public Void run() throws Exception {                
                final URL[] srcRoots = tsts.getRootURLs();
                final String[] srcLabels = tsts.getRootNames();
                tsts.putRoots(new URL[0], new String[0]);
                src.putRoots(srcRoots, srcLabels);
                ProjectManager.getDefault().saveProject(pp);
                return null;
            }
        });
        assertTrue(Arrays.equals(new URL[]{tests.toURL()}, src.getRootURLs()));
        assertTrue(Arrays.equals(new URL[0], tsts.getRootURLs()));
        r = BinaryForSourceQuery.findBinaryRoots(sources.toURL());
        assertNotNull(r);
        roots = r.getRoots();
        assertTrue(Arrays.equals(new URL[0], roots));
        r = BinaryForSourceQuery.findBinaryRoots(tests.toURL());
        assertNotNull(r);
        roots = r.getRoots();
        assertTrue(Arrays.equals(
                new URL[] {
                    buildClasses.toURL(),
                    distJarURL
                },
                roots));
        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
            @Override
            public Void run() throws Exception {
                final URL[] srcRoots = src.getRootURLs();
                final String[] srcLabels = src.getRootNames();
                src.putRoots(new URL[]{sources.toURL()}, new String[]{"Sources"}); //NOI18N
                tsts.putRoots(srcRoots, srcLabels);
                ProjectManager.getDefault().saveProject(pp);
                return null;
            }
        });
        assertTrue(Arrays.equals(new URL[]{sources.toURL()}, src.getRootURLs()));
        assertTrue(Arrays.equals(new URL[]{tests.toURL()}, tsts.getRootURLs()));
        r = BinaryForSourceQuery.findBinaryRoots(sources.toURL());
        assertNotNull(r);
        roots = r.getRoots();
        assertTrue(Arrays.equals(
                new URL[]{
                    buildClasses.toURL(),
                    distJarURL
                },
                roots));
        r = BinaryForSourceQuery.findBinaryRoots(tests.toURL());
        assertNotNull(r);
        roots = r.getRoots();
        assertTrue(Arrays.equals(new URL[] {buildTestClasses.toURL()}, roots));
    }
                    
}
