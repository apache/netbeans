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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.AnnotationProcessorTestUtils;
import org.openide.util.test.MockLookup;

/**
 * That that a project registered by annotation is really found.
 * @author Jaroslav Tulach
 */
public class AntBasedProjectRegistrationTest extends NbTestCase {
    
    public AntBasedProjectRegistrationTest(String name) {
        super(name);
    }
    
    private FileObject scratch;
    private FileObject projdir;
    private ProjectManager pm;
    
    protected @Override void setUp() throws Exception {
        super.setUp();
        MockLookup.init();
        assertEquals("No factory has been used yet", 0, AnnotatedProject.factoryCalls);
        Collection<? extends AntBasedProjectType> all = Lookups.forPath("Services/AntBasedProjectTypes").lookupAll(AntBasedProjectType.class);
        assertEquals("Two found", 2, all.size());
        Iterator<? extends AntBasedProjectType> it = all.iterator();
        if ("testFactory".equals(getName())) {
            it.next();
        }
        AntBasedProjectType t = it.next();
        MockLookup.setInstances(t);
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");
        ProjectGenerator.createProject(projdir, t.getType());
        pm = ProjectManager.getDefault();
    }

    protected @Override void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        pm = null;
        super.tearDown();
    }
    
    public void testFindProject() throws Exception {
        Project p = pm.findProject(projdir);
        assertNotNull("Annotation project found", p.getLookup().lookup(AnnotatedProject.class));
    }

    public void testFactory() throws Exception {
        Project p = pm.findProject(projdir);
        assertNotNull("Annotation project found", p.getLookup().lookup(AnnotatedProject.class));
        assertEquals(1, AnnotatedProject.factoryCalls);
        AnnotatedProject.factoryCalls = 0;
    }

    public void testCanThrowIOException() throws Exception {
        File f = FileUtil.toFile(projdir);
        LocalFileSystem fs = new LocalFileSystem();
        fs.setRootDirectory(f.getParentFile());
        FileObject prj = fs.findResource(f.getName());
        IOException e = new IOException("Mine");
        AnnotatedProject.throwEx = e;
        try {
            Project p = pm.findProject(prj);
            fail("Exception shall be thrown");
        } catch (IOException ex) {
            assertSame("It is our exception", e, ex);
        }
    }

    public void testTypeNeedsToBeProject() throws Exception {
        String prj =
                "import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;" +
                "import org.netbeans.spi.project.support.ant.AntProjectHelper;" +
                "@AntBasedProjectRegistration(" +
                "  iconResource=\"noicon\"," +
                "  type=\"test\"," +
                "  privateNamespace=\"urn:test:private\"," +
                "  sharedNamespace=\"urn:test:shared\"" +
                ")" +
                "public final class Prj extends Object {" +
                "  public Prj(AntProjectHelper h) {" +
                "  }" +
                "}" +
                "";
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "x.Prj", prj);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean res = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, out);
        assertFalse("Compilation failed", res);
        if (!out.toString().contains("extend Project")) {
            fail(out.toString());
        }
    }

    public void testReturnTypeNeedsToBeProject() throws Exception {
        String prj =
                "import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;" +
                "import org.netbeans.spi.project.support.ant.AntProjectHelper;" +
                "" +
                "public final class Prj extends Object {" +
                "  @AntBasedProjectRegistration(" +
                "    iconResource=\"noicon\"," +
                "    type=\"test\"," +
                "    privateNamespace=\"urn:test:private\"," +
                "    sharedNamespace=\"urn:test:shared\"" +
                "  )" +
                "  public static String factory(AntProjectHelper h) {" +
                "    return null;" +
                "  }" +
                "}" +
                "";
        AnnotationProcessorTestUtils.makeSource(getWorkDir(), "x.Prj", prj);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean res = AnnotationProcessorTestUtils.runJavac(getWorkDir(), null, getWorkDir(), null, out);
        assertFalse("Compilation failed", res);
        if (!out.toString().contains("return Project")) {
            fail(out.toString());
        }
    }

    @AntBasedProjectRegistration(
        iconResource="noicon",
        type="test",
        privateNamespace="urn:test:private",
        sharedNamespace="urn:test:shared"
    )
    public static final class AnnotatedProject extends AntBasedTestUtil.TestAntBasedProject {
        static int factoryCalls;
        private static IOException throwEx;
        
        public AnnotatedProject(AntProjectHelper h) throws IOException {
            super(h, null);
            IOException e = throwEx;
            if (e != null) {
                throwEx = null;
                throw e;
            }
        }

        @Override
        public Lookup getLookup() {
            return Lookups.singleton(this);
        }

        @AntBasedProjectRegistration(
            iconResource = "noicon",
            type = "test.factory",
            privateNamespace = "urn:test:private",
            sharedNamespace = "urn:test:shared"
        )
        public static Project factoryMethod(AntProjectHelper h) throws IOException {
            factoryCalls++;
            return new AnnotatedProject(h);
        }
    }
    
}
