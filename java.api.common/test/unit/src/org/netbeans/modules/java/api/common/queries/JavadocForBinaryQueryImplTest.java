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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.api.common.queries;

import java.io.File;
import java.net.URL;
import org.netbeans.api.java.queries.JavadocForBinaryQuery.Result;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.netbeans.spi.project.support.ant.AntBasedTestUtil;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.test.MockLookup;

/**
 * Tests for {@link JavadocForBinaryQueryImpl}.
 *
 * @author Tomas Mysik
 */
public class JavadocForBinaryQueryImplTest extends NbTestCase {

    private FileObject projdir;
    private AntProjectHelper helper;
    private PropertyEvaluator eval;
    private Project prj;
    private FileObject builddir;
    private static final String BUILD_CLASSES_DIR  = "build.classes.dir";
    private static final String JAVADOC_DIR = "dist.javadoc.dir";

    private static final String JAVADOC_1 = "javadoc1";
    private static final String JAVADOC_2 = "javadoc2";

    public JavadocForBinaryQueryImplTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        MockLookup.setInstances(AntBasedTestUtil.testAntBasedProjectType());
        super.setUp();
        this.clearWorkDir();
        File wd = getWorkDir();
        FileObject scratch = FileUtil.toFileObject(wd);
        assertNotNull(wd);
        projdir = scratch.createFolder("proj");
        helper = ProjectGenerator.createProject(projdir, "test");
        assertNotNull(helper);
        eval = helper.getStandardPropertyEvaluator();
        assertNotNull(eval);
        prj = ProjectManager.getDefault().findProject(projdir);
        assertNotNull(prj);
        builddir = projdir.createFolder("build");
        assertNotNull(builddir);
    }

    public void testJavadocForBinaryQuery() throws Exception {
        setProjectDirectory(BUILD_CLASSES_DIR, builddir);
        JavadocForBinaryQueryImplementation javadocForBinaryQuery =
                QuerySupport.createJavadocForBinaryQuery(helper, eval);

        setProjectDirectory(JAVADOC_DIR, projdir.createFolder(JAVADOC_1));
        Result javadoc = javadocForBinaryQuery.findJavadoc(builddir.toURL());
        assertNotNull(javadoc);

        URL[] roots = javadoc.getRoots();
        assertEquals(1, roots.length);
        assertEquals(getJavadocUrl(JAVADOC_1), roots[0]);

        // change javadoc directory
        setProjectDirectory(JAVADOC_DIR, projdir.createFolder(JAVADOC_2));
        roots = javadoc.getRoots();
        assertEquals(1, roots.length);
        assertEquals(getJavadocUrl(JAVADOC_2), roots[0]);
    }

    private URL getJavadocUrl(String javadoc) {
        return projdir.getFileObject(javadoc).toURL();
    }

    private void setProjectDirectory(final String property, final FileObject directory) throws Exception {
        assertTrue(directory.isFolder());
        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
            public Void run() throws Exception {
                EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                ep.setProperty(property, directory.getName());
                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                ProjectManager.getDefault().saveProject(prj);
                return null;
            }
        });
    }
}
