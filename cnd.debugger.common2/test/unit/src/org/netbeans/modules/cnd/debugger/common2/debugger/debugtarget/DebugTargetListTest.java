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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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