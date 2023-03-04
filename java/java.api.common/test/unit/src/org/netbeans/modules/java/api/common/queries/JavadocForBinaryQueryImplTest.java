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
