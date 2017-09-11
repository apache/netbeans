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

package org.netbeans.modules.subversion.client.commands;

import org.netbeans.modules.subversion.client.AbstractCommandTestCase;
import java.io.File;
import org.netbeans.modules.subversion.utils.TestUtilities;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

/**
 *
 * @author tomas
 */
public class DifferentWorkingDirsTestHidden extends AbstractCommandTestCase {
    
    public DifferentWorkingDirsTestHidden(String testName) throws Exception {
        super(testName);
    }
    
    public void testDifferentWorkingDirs1() throws Exception {
        File folder1 = createFolder("folder1");
        File folder11 = createFolder(folder1, "folder11");
        File folder12 = createFolder(folder1, "folder12");

        File folder2 = createFolder("folder2");
        File folder21 = createFolder(folder2,"folder2");
        File folder211 = createFolder(folder21,"folder2");
        File folder2111 = createFolder(folder211,"folder2");

        ISVNClientAdapter c = getNbClient();
        for(File f : new File[] {folder1, folder11, folder12, folder2, folder21, folder211, folder2111}) {
            c.addDirectory(f, false);
        }

        c.commit(new File[] {folder1, folder11, folder12, folder2, folder21, folder211, folder2111}, "msg", false);

        assertNotifiedFiles(new File[] {folder1, folder11, folder12, folder2, folder21, folder211, folder2111});
    }

//    // fails with 1.6
//    public void testDifferentWorkingDirs2() throws Exception {
//
//        // 1. wc
//        File folder1 = createFolder("folder1");
//        File folder11 = createFolder(folder1, "folder11");
//        File folder12 = createFolder(folder1, "folder12");
//        File file1_12 = createFile(folder12, "file1_12");
//
//        // 2. wc
//        File file2_12 = createFile(folder12, "file2_12");
//        File tmpFolder = File.createTempFile("testDifferentWorkingDirs", null);
//        tmpFolder = new File(tmpFolder.getParentFile(), "tmpFolder");
//        TestUtilities.deleteRecursively(tmpFolder);
//        tmpFolder.mkdirs();
//
//        cleanUpRepo(new String[] {tmpFolder.getName()});
//        try {
//            importFile(tmpFolder);
//        } catch (SVNClientException sVNClientException) {
//            // ignore
//        }
//
//        File tmpFile = new File(tmpFolder, "file");
//        tmpFile.createNewFile();
//
//        // client
//        ISVNClientAdapter c = getNbClient();
//
//        for(File f : new File[] {folder1, folder11, folder12, file1_12, file2_12, tmpFile}) {
//            c.addFile(f);
//        }
//
//        // commit
//        c.commit(new File[] {folder1, folder11, folder12, file1_12, file2_12, tmpFile}, "msg", false);
//
//        assertNotifiedFiles(new File[] {folder1, folder11, folder12, file1_12, file2_12, tmpFile});
//    }

    // fails with 1.6
    public void testDifferentWorkingDirs3() throws Exception {        
        File folder = createFolder("folder");        
        File folder1 = createFolder(folder, "folder1");        
        File folder2 = createFolder(folder, "folder2");        
        
        File wc1 = createFolder(folder1, "wc1");        
        File wc2 = createFolder(folder2, "wc2");        

        cleanUpRepo(new String[] {wc1.getName()});
        cleanUpRepo(new String[] {wc2.getName()});
        importFile(wc1);
        importFile(wc2);
        
        File file1 = createFile(wc1, "file1");
        File file2 = createFile(wc2, "file2");
        
        ISVNClientAdapter c = getNbClient();
        
        for(File f : new File[] {file1, file2}) {
            c.addFile(f);
        }
        
        c.commit(new File[] {file1, file2}, "msg", false);        
        
        assertStatus(SVNStatusKind.NORMAL, file1);
        assertStatus(SVNStatusKind.NORMAL, file2);
        
        assertNotifiedFiles(new File[] {file1, file2});
    }
    
}
