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
package org.netbeans.modules.java.mx.project;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class SdkSuiteTest extends SuiteCheck {
    public SdkSuiteTest(String n) {
        super(n);
    }

    public static junit.framework.Test suite() {
        return suite(SdkSuiteTest.class);
    }

    public void testParseSdkSourcesWithoutError() throws Exception {
        verifyNoErrorsInSuite("sdk");
    }

    public void testRootsForAnSdkJar() throws Exception {
        File sdkSibling = findSuite("sdk");

        FileObject fo = FileUtil.toFileObject(sdkSibling);
        assertNotNull("project directory found", fo);

        FileObject graalSdkJar = FileUtil.createData(fo, "mxbuild/dists/jdk1.8/graal-sdk.jar");
        assertNotNull(graalSdkJar);

        Project p = ProjectManager.getDefault().findProject(fo);
        assertNotNull("project found", p);
        assertEquals("It is suite project: " + p, "SuiteProject", p.getClass().getSimpleName());

        final URL archiveURL = new URL("jar:" + graalSdkJar.toURL() + "!/");

        SourceForBinaryQuery.Result2 result2 = SourceForBinaryQuery.findSourceRoots2(archiveURL);
        final FileObject[] resultRoots = result2.getRoots();
        assertTrue("There should be some roots", resultRoots.length > 0);

        Set<FileObject> expected = new HashSet<>();
        for (FileObject ch : fo.getFileObject("src").getChildren()) {
            if (ch.getNameExt().endsWith(".test")) {
                // tests are not in graal-sdk.jar
                continue;
            }
            if (ch.getNameExt().endsWith(".tck")) {
                // TCK is not in graal-sdk.jar
                continue;
            }
            if (ch.getNameExt().contains("org.graalvm.launcher")) {
                // launcher is not in graal-sdk.jar
                continue;
            }
            expected.add(ch);
        }

        for (FileObject r : resultRoots) {
            if ("src_gen".equals(r.getName())) {
                continue;
            }

            FileObject parent = r.getParent();
            boolean removed = expected.remove(parent);
            assertTrue("Object " + parent + " was removed: " + expected, removed);
        }

        assertTrue("All roots found: " + expected, expected.isEmpty());
    }

}
