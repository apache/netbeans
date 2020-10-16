/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
        FileObject srcRoot = nbRoot().getFileObject("apisupport/apisupport.project");
        testRoots = UnitTestForSourceQuery.findUnitTests(srcRoot);
        assertEquals("Test root for project should be null", Collections.EMPTY_LIST, Arrays.asList(testRoots));
        srcRoot = nbRoot().getFileObject("apisupport/apisupport.project/test/unit/src");
        testRoots = UnitTestForSourceQuery.findUnitTests(srcRoot);
        assertEquals("Test root for tests should be null", Collections.EMPTY_LIST, Arrays.asList(testRoots));
        srcRoot = nbRoot().getFileObject("apisupport/apisupport.project/src");
        testRoots = UnitTestForSourceQuery.findUnitTests(srcRoot);
        assertEquals("Test root defined", 1, testRoots.length);
        assertTrue("Test root exists", Utilities.toFile(URI.create(testRoots[0].toExternalForm())).exists());
        assertEquals("Test root", URLMapper.findFileObject(testRoots[0]), nbRoot().getFileObject("apisupport/apisupport.project/test/unit/src"));
        assertEquals("One test for this project", 1, UnitTestForSourceQuery.findUnitTests(nbRoot().getFileObject("platform/openide.windows/src")).length);
    }

    public void testFindSource() {
        URL[] srcRoots = UnitTestForSourceQuery.findSources(nbRoot());
        assertEquals("Source root for non project folder should be null", Collections.EMPTY_LIST, Arrays.asList(srcRoots));
        FileObject testRoot = nbRoot().getFileObject("apisupport/apisupport.project");
        srcRoots = UnitTestForSourceQuery.findSources(testRoot);
        assertEquals("Source root for project should be null", Collections.EMPTY_LIST, Arrays.asList(srcRoots));
        testRoot = nbRoot().getFileObject("apisupport/apisupport.project/src");
        srcRoots = UnitTestForSourceQuery.findSources(testRoot);
        assertEquals("Source root for sources should be null", Collections.EMPTY_LIST, Arrays.asList(srcRoots));
        assertEquals("No sources for this project's sources", Collections.EMPTY_LIST, Arrays.asList(UnitTestForSourceQuery.findSources(nbRoot().getFileObject("platform/openide.windows/src"))));
        testRoot = nbRoot().getFileObject("apisupport/apisupport.project/test/unit/src");
        srcRoots = UnitTestForSourceQuery.findSources(testRoot);
        assertEquals("Source root defined", 1, srcRoots.length);
        assertTrue("Source root exists", Utilities.toFile(URI.create(srcRoots[0].toExternalForm())).exists());
        assertEquals("Source root", URLMapper.findFileObject(srcRoots[0]), nbRoot().getFileObject("apisupport/apisupport.project/src"));
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
