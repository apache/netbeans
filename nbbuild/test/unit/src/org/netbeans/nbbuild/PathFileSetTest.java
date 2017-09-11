/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.StringTokenizer;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.selectors.FilenameSelector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import static org.junit.Assert.*;

/**
 *
 * @author Richard Michalsky
 */
public class PathFileSetTest extends NbTestCase {

    private PathFileSet pfs;
    private Project fakeproj;
    private ClusterRecord cl1;
    private ClusterRecord cl2;

    public PathFileSetTest(String testName) {
        super(testName);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

//    @Before
    @Override
    protected void setUp() throws IOException {
        clearWorkDir();
        pfs = new PathFileSet();
        fakeproj = new Project();
        fakeproj.setBaseDir(getWorkDir());
        fakeproj.addBuildListener(new BuildListener() {

            public void messageLogged(BuildEvent buildEvent) {
                if (buildEvent.getPriority() <= Project.MSG_VERBOSE) {
                    System.err.println(buildEvent.getMessage());
                }
            }

            public void taskStarted(BuildEvent buildEvent) {
            }

            public void taskFinished(BuildEvent buildEvent) {
            }

            public void targetStarted(BuildEvent buildEvent) {
            }

            public void targetFinished(BuildEvent buildEvent) {
            }

            public void buildStarted(BuildEvent buildEvent) {
            }

            public void buildFinished(BuildEvent buildEvent) {
            }
        });
        pfs.setProject(fakeproj);
        cl1 = new ClusterRecord("cl1").create().addModule("org-m1", true, true, true);
        cl2 = new ClusterRecord("cl2").create().addModule("org-m2", true, false, false);
        pfs.addPath(new Path(fakeproj, "cl1" + File.pathSeparator + "cl2"));
    }

//    @After
    @Override
    protected void tearDown() {
    }

    private void executeAndCheckResults(String[] expected) throws BuildException, IOException {
        String[] output = new String[pfs.size()];
        int j = 0;
        for (Iterator it = pfs.iterator(); it.hasNext(); j++) {
            FileResource fileResource = (FileResource) it.next();
            String path = fileResource.getFile().getAbsolutePath().replace('\\', '/');
            output[j] = path;
        }
        Arrays.sort(output);
        String wd = getWorkDir().getPath().replace('\\', '/').concat("/");
        for (int i = 0; i < expected.length; i++) {
            expected[i] = wd + expected[i];
        }
        assertArrayEquals(expected, output);
    }

    private class ClusterRecord {

        File ut;
        File cm;
        File m;
        private String clusterName;

        public ClusterRecord(String clusterName) {
            this.clusterName = clusterName;
        }

        public ClusterRecord create() throws IOException {
            ut = new File(getWorkDir(), clusterName + "/update_tracking");
            ut.mkdirs();
            assertTrue(ut.isDirectory());

            cm = new File(getWorkDir(), clusterName + "/config/Modules");
            cm.mkdirs();
            assertTrue(cm.isDirectory());

            m = new File(getWorkDir(), clusterName + "/modules");
            m.mkdirs();
            assertTrue(m.isDirectory());
            return this;
        }

        public ClusterRecord addModule(String dashedCNB, boolean jar, boolean config, boolean updateTracking) throws IOException {
            if (updateTracking) {
                assertTrue(new File(ut, dashedCNB + ".xml").createNewFile());
            }
            if (jar) {
                assertTrue(new File(m, dashedCNB + ".jar").createNewFile());
            }
            if (config) {
                assertTrue(new File(cm, dashedCNB + ".xml").createNewFile());
            }
            return this;
        }
    }

    @Test
    public void testFindAllFiles() throws IOException {
        executeAndCheckResults(new String[]{"cl1/config/Modules/org-m1.xml",
                    "cl1/modules/org-m1.jar",
                    "cl1/update_tracking/org-m1.xml",
                    "cl2/modules/org-m2.jar"});
    }

    @Test
    public void testWildcardIncludes() throws IOException {
        pfs.setInclude("**/*.jar");
        executeAndCheckResults(new String[]{"cl1/modules/org-m1.jar", "cl2/modules/org-m2.jar"});
    }

    @Test
    public void testSimpleIncludes() throws IOException {
        pfs.setInclude("modules/org-m1.jar");
        executeAndCheckResults(new String[]{"cl1/modules/org-m1.jar"});
    }

    @Test
    /** Cluster name is part of the basedir and thus WON'T match. */
    public void testWrongRelativeIncludes() throws IOException {
        pfs.setInclude("cl1/modules/org-m1.jar");
        assertEquals(pfs.size(), 0);
    }

    @Test
    public void testEmptySet() throws IOException {
        pfs.setInclude("pattern not present");
        assertEquals(pfs.size(), 0);
    }

    @Test
    public void testFileNameSelector() throws IOException {
        FilenameSelector sel = new FilenameSelector();
        sel.setCasesensitive(false);
        sel.setName("**/config/**/*.xml");
        pfs.add(sel);
        executeAndCheckResults(new String[]{"cl1/config/Modules/org-m1.xml"});
    }

    @Test
    public void testPathAttribute() throws IOException {
        new ClusterRecord("cl3").create().addModule("org-m3", true, false, false);
        new ClusterRecord("cl4").create().addModule("org-m4", true, false, false);
        Path path = new Path(fakeproj, getWorkDir().getPath() + File.separator + "cl3"
                + File.pathSeparator
                + getWorkDir().getPath() + File.separator + "cl4");
        pfs.setPath(path);
        pfs.setInclude("**/*.jar");
        executeAndCheckResults(new String[] {
            "cl1/modules/org-m1.jar", "cl2/modules/org-m2.jar",
            "cl3/modules/org-m3.jar", "cl4/modules/org-m4.jar"
        });
    }
}
