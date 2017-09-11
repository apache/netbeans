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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
