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
