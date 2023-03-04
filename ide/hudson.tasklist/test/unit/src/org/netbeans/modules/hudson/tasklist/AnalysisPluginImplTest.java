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

package org.netbeans.modules.hudson.tasklist;

import java.util.Arrays;
import java.util.Collection;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class AnalysisPluginImplTest extends NbTestCase {
    
    public AnalysisPluginImplTest(String n) {
        super(n);
    }

    @Override protected void setUp() throws Exception {
        clearWorkDir();
    }

    public void testLocate() throws Exception {
        FileObject d = FileUtil.toFileObject(getWorkDir());
        FileUtil.createData(d, "build.xml");
        FileUtil.createData(d, "src/p/C.java");
        FileUtil.createData(d, "test/build.xml");
        FileUtil.createData(d, "test/p/CTest.java");
        Collection<FileObject> roots = Arrays.asList(d, d.getFileObject("src"), d.getFileObject("test"));
        assertEquals(d.getFileObject("build.xml"), AnalysisPluginImpl.locate("/h/workspace/myjob/build.xml", roots));
        assertEquals(d.getFileObject("src/p/C.java"), AnalysisPluginImpl.locate("/h/workspace/myjob/src/p/C.java", roots));
        assertEquals(d.getFileObject("test/p/CTest.java"), AnalysisPluginImpl.locate("/h/workspace/myjob/test/p/CTest.java", roots));
        assertEquals(d.getFileObject("src/p/C.java"), AnalysisPluginImpl.locate("/tmp/clover123.tmp/p/C.java", roots));
        assertEquals(d.getFileObject("test/p/CTest.java"), AnalysisPluginImpl.locate("/tmp/clover456.tmp/p/CTest.java", roots));
        assertEquals(null, AnalysisPluginImpl.locate("/h/workspace/myjob/src/p/X.java", roots));
        assertEquals(null, AnalysisPluginImpl.locate("/junk", roots));
        assertEquals(null, AnalysisPluginImpl.locate("huh?!", roots));
    }

    public void testWorkspacePath() throws Exception {
        assertEquals("trunk/src/main/org/apache/tools/ant/taskdefs/optional/net/FTPTask.java", AnalysisPluginImpl.workspacePath("/x1/jenkins/jenkins-slave/workspace/Ant_Nightly/trunk/src/main/org/apache/tools/ant/taskdefs/optional/net/FTPTask.java", "Ant_Nightly"));
        assertEquals("src/p/C.java", AnalysisPluginImpl.workspacePath("C:\\hudson\\workspace\\some job\\src\\p\\C.java", "some job"));
        assertEquals(null, AnalysisPluginImpl.workspacePath("/tmp/whatever", "j"));
        assertEquals("src/p/C.java", AnalysisPluginImpl.workspacePath("/hudson/workdir/jobs/j/workspace/src/p/C.java", "j"));
    }

}
