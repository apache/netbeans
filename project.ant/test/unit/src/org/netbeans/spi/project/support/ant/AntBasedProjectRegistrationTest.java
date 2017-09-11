/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
