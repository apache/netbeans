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
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 *
 * @author tomas
 */
public class MergeTestHidden extends AbstractCommandTestCase {
    
    public MergeTestHidden(String testName) throws Exception {
        super(testName);
    }
    
    public void testMergeBack() throws Exception {                                        
        File file = createFile("file");
        add(file);
        write(file, "1");
        commit(file);
        assertInfo(file, getFileUrl(file));

        SVNRevision r1 = getRevision(file);
        write(file, "1\n2");
        commit(file);        
        assertEquals("1\n2", read(file));
        SVNRevision r2 = getRevision(file);
        
        ISVNClientAdapter c = getNbClient();
        // used to fail with svnkit: http://issues.tmatesoft.com/issue/SVNKIT-229
        c.merge(getFileUrl(file), r2, getFileUrl(file), r1, file, false, false);
        assertTrue(file.exists());
        assertEquals("1", read(file));
        assertNotifiedFiles(new File[] {file});        
    }
    
    public void testMergeFolderNonRec() throws Exception {                                        
        // init wc
        File folder = createFolder("folder");
        add(folder);
        commit(folder);
        assertInfo(folder, getFileUrl(folder));
        
        // init copy from wc
        File foldercopy = new File(getWC(), "foldercopy");
                
        ISVNClientAdapter c = getNbClient();
        c.copy(getFileUrl(folder), getFileUrl(foldercopy), "copy", SVNRevision.HEAD);
        // switch to copy
        c.switchToUrl(folder, getFileUrl(foldercopy), SVNRevision.HEAD, true);

        assertCopy(getFileUrl(foldercopy));
        assertInfo(folder, getFileUrl(foldercopy));
        
        // add new files to copy
        File folder1 = createFolder(folder, "folder1");
        File file = createFile(folder, "file");
        File file1 = createFile(folder1, "file");
        add(file);
        add(folder1);
        add(file1);
        commit(folder);
        assertTrue(file.exists());
        
        // switch back to wc
        c.switchToUrl(folder, getFileUrl(folder), SVNRevision.HEAD, true);
        assertCopy(getFileUrl(folder));
        assertInfo(folder, getFileUrl(folder));        
        assertFalse(file.exists());
        assertFalse(file1.exists());
        assertFalse(folder1.exists());
        
        // merge wc with copy
        ISVNLogMessage[] log = getCompleteLog(getFileUrl(folder));
        c.merge(getFileUrl(foldercopy), log[0].getRevision(), getFileUrl(foldercopy), SVNRevision.HEAD, folder, false, false);
        assertTrue(file.exists());
        assertFalse(file1.exists());
        assertFalse(folder1.exists());
        
        assertNotifiedFiles(new File[] {file, folder1});
    }
    
    public void testMergeFolderFrom2Urls() throws Exception {                                        
        File folder = createFolder("folder");
        add(folder);
        commit(folder);
        assertInfo(folder, getFileUrl(folder));
        
        File foldercopy1 = new File(getWC(), "foldercopy1");
        
        ISVNClientAdapter c = getNbClient();
        c.copy(getFileUrl(folder), getFileUrl(foldercopy1), "copy", SVNRevision.HEAD);
        c.switchToUrl(folder, getFileUrl(foldercopy1), SVNRevision.HEAD, true);

        assertCopy(getFileUrl(foldercopy1));
        assertInfo(folder, getFileUrl(foldercopy1));
        
        File file1 = createFile(folder, "file1");
        add(file1);
        commit(folder);
        assertTrue(file1.exists());

        File foldercopy2 = new File(getWC(), "foldercopy2");
        
        c = getNbClient();
        c.copy(getFileUrl(foldercopy1), getFileUrl(foldercopy2), "copy", SVNRevision.HEAD);
        c.switchToUrl(folder, getFileUrl(foldercopy2), SVNRevision.HEAD, true);

        assertCopy(getFileUrl(foldercopy2));
        assertInfo(folder, getFileUrl(foldercopy2));
        
        File file2 = createFile(folder, "file2");
        add(file2);
        commit(folder);
        assertTrue(file2.exists());
        
        c.switchToUrl(folder, getFileUrl(folder), SVNRevision.HEAD, true);
        assertCopy(getFileUrl(folder));
        assertInfo(folder, getFileUrl(folder));                
        assertFalse(file1.exists());
        assertFalse(file2.exists());
        
        ISVNLogMessage[] log = getCompleteLog(getFileUrl(foldercopy1));
        c.merge(getFileUrl(foldercopy1), log[0].getRevision(), getFileUrl(foldercopy2), SVNRevision.HEAD, folder, true, true);
        assertTrue(file1.exists());
        assertTrue(file2.exists());
        assertNotifiedFiles(new File[] {file1, file2});        
    }
    
    public void testFolderMergeRec() throws Exception {                                        
        File folder = createFolder("folder");
        add(folder);
        commit(folder);
        assertInfo(folder, getFileUrl(folder));
        
        File foldercopy = new File(getWC(), "foldercopy");
        
        ISVNClientAdapter c = getNbClient();
        c.copy(getFileUrl(folder), getFileUrl(foldercopy), "copy", SVNRevision.HEAD);
        c.switchToUrl(folder, getFileUrl(foldercopy), SVNRevision.HEAD, true);

        assertCopy(getFileUrl(foldercopy));
        assertInfo(folder, getFileUrl(foldercopy));
        
        File folder1 = createFolder(folder, "folder1");
        File file = createFile(folder, "file");
        File file1 = createFile(folder1, "file");
        add(file);
        add(folder1);
        add(file1);
        commit(folder);
        assertTrue(file.exists());
        
        c.switchToUrl(folder, getFileUrl(folder), SVNRevision.HEAD, true);
        assertCopy(getFileUrl(folder));
        assertInfo(folder, getFileUrl(folder));        
        assertFalse(file.exists());
        assertFalse(file1.exists());
        assertFalse(folder1.exists());
        
        ISVNLogMessage[] log = getCompleteLog(getFileUrl(folder));
        c.merge(getFileUrl(foldercopy), log[0].getRevision(), getFileUrl(foldercopy), SVNRevision.HEAD, folder, false, true);
        assertTrue(file.exists());
        assertTrue(file1.exists());
        assertTrue(folder1.exists());
        assertNotifiedFiles(new File[] {file, file1, folder1});        
    }
    
}
