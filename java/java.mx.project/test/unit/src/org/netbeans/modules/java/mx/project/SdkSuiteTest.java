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
package org.netbeans.modules.java.mx.project;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
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

        Project p = ProjectManager.getDefault().findProject(fo);
        assertNotNull("project found", p);
        assertEquals("It is suite project: " + p, "SuiteProject", p.getClass().getSimpleName());
        Sources src = ProjectUtils.getSources(p);
        List<FileObject> resultRoots = new ArrayList<>();
        for (SourceGroup sourceGroup : src.getSourceGroups("java")) {
            BinaryForSourceQuery.Result binaryResult = BinaryForSourceQuery.findBinaryRoots(sourceGroup.getRootFolder().toURL());
            for (URL r : binaryResult.getRoots()) {
                SourceForBinaryQuery.Result2 result2 = SourceForBinaryQuery.findSourceRoots2(r);
                final FileObject[] rr = result2.getRoots();
                resultRoots.addAll(Arrays.asList(rr));
            }
        }

        assertTrue("There should be some roots", !resultRoots.isEmpty());

        Set<FileObject> expected = new HashSet<>();
        for (FileObject ch : fo.getFileObject("src").getChildren()) {
            String name = ch.getNameExt();
            if (name.equals("org.graalvm.launcher.native") || name.equals("org.graalvm.toolchain.test")) {
                // Not a Java code
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
