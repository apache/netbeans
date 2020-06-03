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

package org.netbeans.modules.cnd.debugger.common2.debugger.debugtarget;

import org.netbeans.modules.cnd.debugger.common2.debugger.test.CndBaseTestCase;
import org.netbeans.modules.cnd.debugger.common2.debugger.test.CndCoreTestUtils;
import java.io.File;
import org.netbeans.modules.cnd.debugger.common2.utils.UserdirFile;
import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 */
public class DebugTargetListTest extends CndBaseTestCase {

    public DebugTargetListTest() {
        super("DebugTargetListTest");
    }

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
    }

    @org.junit.AfterClass
    public static void tearDownClass() throws Exception {
    }

    @org.junit.Before
    @Override
    public void setUp() throws Exception {
        System.setProperty("org.netbeans.modules.cnd.makeproject.api.runprofiles", "true");
        System.setProperty("debug.engine", "on");
    }

    @org.junit.After
    @Override
    public void tearDown() throws Exception {
    }

//    /**
//     * Test of getInstance method, of class DebugTargetList.
//     */
//    @org.junit.Test
//    public void testGetInstance() {
//        System.out.println("getInstance");
//        DebugTargetList expResult = null;
//        DebugTargetList result = DebugTargetList.getInstance();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of saveList method, of class DebugTargetList.
//     */
//    @org.junit.Test
//    public void testSaveList() {
//        System.out.println("saveList");
//        DebugTargetList.saveList();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of save method, of class DebugTargetList.
//     */
//    @org.junit.Test
//    public void testSave() {
//        System.out.println("save");
//        UserdirFile userdirFile = null;
//        DebugTargetList instance = null;
//        instance.save(userdirFile);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of restore method, of class DebugTargetList.
     */
    @org.junit.Test
    public void testRestoreV1() throws IOException {
        if (true) return; // Disabled
        System.out.println("restore V1");
        doTestRestore("debugtargets_v1");
    }

    @org.junit.Test
    public void testRestoreV2() throws IOException {
        if (true) return; // Disabled
        System.out.println("restore V2");
        doTestRestore("debugtargets_v2");
    }

    private void doTestRestore(String filename) throws IOException {
        UserdirFile userdirFile = new UserdirFile(
                DebugTargetList.moduleFolderName,
                DebugTargetList.folderName,
                filename);
        prepareUserdirFile(userdirFile);
        DebugTargetList instance = new DebugTargetList(DebugTargetList.debuglistMaxSize);
        instance.restore(userdirFile);
        UserdirFile outUserdirFile = new UserdirFile(
                userdirFile.moduleFolderName(),
                userdirFile.folderName(),
                filename + "_saved");
        instance.save(outUserdirFile);
        compareReferenceFiles(outUserdirFile, "debugtargets_saved.xml");
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
//    /**
//     * Test of cloneList method, of class DebugTargetList.
//     */
//    @org.junit.Test
//    public void testCloneList() {
//        System.out.println("cloneList");
//        DebugTargetList instance = null;
//        DebugTargetList expResult = null;
//        DebugTargetList result = instance.cloneList();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}