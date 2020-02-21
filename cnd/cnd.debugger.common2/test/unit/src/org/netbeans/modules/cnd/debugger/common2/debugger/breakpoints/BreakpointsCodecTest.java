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
package org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.cnd.debugger.common2.debugger.test.CndBaseTestCase;
import org.netbeans.modules.cnd.debugger.common2.debugger.test.CndCoreTestUtils;
import org.netbeans.modules.cnd.debugger.common2.utils.UserdirFile;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 */
public class BreakpointsCodecTest extends CndBaseTestCase {
    public BreakpointsCodecTest() {
        super("BreakpointsCodecTest");
    }
    
    @org.junit.Test
    public void testBreakpointsOld() throws IOException {
        System.out.println("Breakpoints old");
        doTest("Breakpoints_old");
    }
    
    @org.junit.Test
    public void testBreakpointsNew() throws IOException {
        System.out.println("Breakpoints new");
        doTest("Breakpoints_new");
    }
    
    private void doTest(String filename) throws IOException {
        UserdirFile userdirFile = new UserdirFile(
                BreakpointBag.moduleFolderName,
                BreakpointBag.folderName,
                filename);
        prepareUserdirFile(userdirFile);
        BreakpointBag instance = new BreakpointBag();
        BreakpointBag.doRestore(userdirFile, instance);
        UserdirFile outUserdirFile = new UserdirFile(
                userdirFile.moduleFolderName(),
                userdirFile.folderName(),
                filename + "_saved");
        BreakpointBag.doSave(outUserdirFile, instance);
        compareReferenceFiles(outUserdirFile, "Breakpoints_saved.xml");
    }

    private void compareReferenceFiles(UserdirFile testFile, String goldenFile) throws IOException {
        FileUtil.refreshAll();
        FileObject configFile = FileUtil.getConfigFile(testFile.fullPath());
        File outFile = FileUtil.toFile(configFile);
        assertNotNull("out file is not generated", outFile);
        File gf = getGoldenFile(goldenFile);
        if (CndCoreTestUtils.diff(outFile, gf, null)) {
            // copy golden
            File goldenDataFileCopy = new File(outFile.getParentFile(), goldenFile + ".golden"); // NOI18N
            CndCoreTestUtils.copyToWorkDir(gf, goldenDataFileCopy);
            fail("Files differ; diff " + outFile.getAbsolutePath() + " " + goldenDataFileCopy); // NOI18N
        }
    }

    private void prepareUserdirFile(UserdirFile userdirFile) throws IOException {
        FileObject configFile = FileUtil.getConfigFile(userdirFile.fullPath());
        if (configFile != null) {
            configFile.delete();
        }
        FileObject testDataDir = FileUtil.createFolder(FileUtil.getConfigRoot(),
                userdirFile.moduleFolderName() + File.separator + userdirFile.folderName());
        File gf = getGoldenFile(userdirFile.filename() + ".xml");
        FileObject gFO = FileUtil.toFileObject(gf);
        FileUtil.copyFile(gFO, testDataDir, userdirFile.filename());
    }
}
