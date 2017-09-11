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

package org.netbeans.modules.apisupport.project.queries;

import java.io.IOException;
import java.net.URI;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.modules.apisupport.project.TestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Utilities;

/**
 * Test for UnitTestForSourceQuery
 * @author Tomas Zezula
 */
public class UnitTestForSourceQueryImplTest extends TestBase {

    public UnitTestForSourceQueryImplTest(String testName) {
        super(testName);
    }   

    public void testFindUnitTest() throws Exception {
        URL[] testRoots = UnitTestForSourceQuery.findUnitTests(nbRoot());
        assertEquals("Test root for non project folder should be null", Collections.EMPTY_LIST, Arrays.asList(testRoots));
        FileObject srcRoot = nbRoot().getFileObject("apisupport.project");
        testRoots = UnitTestForSourceQuery.findUnitTests(srcRoot);
        assertEquals("Test root for project should be null", Collections.EMPTY_LIST, Arrays.asList(testRoots));
        srcRoot = nbRoot().getFileObject("apisupport.project/test/unit/src");
        testRoots = UnitTestForSourceQuery.findUnitTests(srcRoot);
        assertEquals("Test root for tests should be null", Collections.EMPTY_LIST, Arrays.asList(testRoots));
        srcRoot = nbRoot().getFileObject("apisupport.project/src");
        testRoots = UnitTestForSourceQuery.findUnitTests(srcRoot);
        assertEquals("Test root defined", 1, testRoots.length);
        assertTrue("Test root exists", Utilities.toFile(URI.create(testRoots[0].toExternalForm())).exists());
        assertEquals("Test root", URLMapper.findFileObject(testRoots[0]), nbRoot().getFileObject("apisupport.project/test/unit/src"));
        assertEquals("One test for this project", 1, UnitTestForSourceQuery.findUnitTests(nbRoot().getFileObject("openide.windows/src")).length);
    }

    public void testFindSource() {
        URL[] srcRoots = UnitTestForSourceQuery.findSources(nbRoot());
        assertEquals("Source root for non project folder should be null", Collections.EMPTY_LIST, Arrays.asList(srcRoots));
        FileObject testRoot = nbRoot().getFileObject("apisupport.project");
        srcRoots = UnitTestForSourceQuery.findSources(testRoot);
        assertEquals("Source root for project should be null", Collections.EMPTY_LIST, Arrays.asList(srcRoots));
        testRoot = nbRoot().getFileObject("apisupport.project/src");
        srcRoots = UnitTestForSourceQuery.findSources(testRoot);
        assertEquals("Source root for sources should be null", Collections.EMPTY_LIST, Arrays.asList(srcRoots));
        assertEquals("No sources for this project's sources", Collections.EMPTY_LIST, Arrays.asList(UnitTestForSourceQuery.findSources(nbRoot().getFileObject("openide.windows/src"))));
        testRoot = nbRoot().getFileObject("apisupport.project/test/unit/src");
        srcRoots = UnitTestForSourceQuery.findSources(testRoot);
        assertEquals("Source root defined", 1, srcRoots.length);
        assertTrue("Source root exists", Utilities.toFile(URI.create(srcRoots[0].toExternalForm())).exists());
        assertEquals("Source root", URLMapper.findFileObject(srcRoots[0]), nbRoot().getFileObject("apisupport.project/src"));
    }        

    public void testCorrectURLForNonexistentFolder143633() throws IOException {
        FileObject prjFO = TestBase.generateStandaloneModuleDirectory(getWorkDir(), "noTestDir");
        FileObject testFO = prjFO.getFileObject("test");
        testFO.delete();
        assertFalse("test dir successfully deleted in project " + prjFO, (new File(FileUtil.toFile(prjFO), "test").exists()));
        FileObject srcFO = prjFO.getFileObject("src");
        URL[] testRoots = UnitTestForSourceQuery.findUnitTests(srcFO);
        assertEquals(testRoots.length, 1);
        URL url = testRoots[0];
        assertTrue("Nonexistent test root URL " + url + " must end with a slash", url.toString().endsWith("/"));
    }
}
