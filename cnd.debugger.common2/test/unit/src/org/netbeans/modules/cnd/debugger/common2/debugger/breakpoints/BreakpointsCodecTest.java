/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
