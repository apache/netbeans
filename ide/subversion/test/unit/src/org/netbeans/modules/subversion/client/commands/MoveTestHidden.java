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
import java.io.InputStream;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.subversion.utils.TestUtilities;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

/**
 *
 * @author tomas
 */
public class MoveTestHidden extends AbstractCommandTestCase {
    
    public MoveTestHidden(String testName) throws Exception {
        super(testName);
    }
    
    public void testMoveURL2URL() throws Exception {
        testMoveURL2URL("file", "filemove");
    }

    public void testMoveURL2URLWithAtSign() throws Exception {
//        testMoveURL2URL("file1", "@filemove"); // fails until fixed in svn - http://subversion.tigris.org/issues/show_bug.cgi?id=3416
        testMoveURL2URL("file2", "file@move");
        testMoveURL2URL("file3", "filemove@");
    }

    public void testMoveURLWithAtSign2URL() throws Exception {
        testMoveURL2URL("@file", "filemove1");
        testMoveURL2URL("fi@le", "filemove2");
        testMoveURL2URL("file@", "filemove3");
    }

    public void testMoveURL2URLInDir() throws Exception {
        testMoveURL2URL("folder/file", "filemove");
    }

    public void testMoveURL2URLWithAtSignInDir() throws Exception {
//        testMoveURL2URL("folder/file1", "@filemove"); // fails until fixed in svn - http://subversion.tigris.org/issues/show_bug.cgi?id=3416
        testMoveURL2URL("folder/file2", "file@move");
        testMoveURL2URL("folder/file3", "filemove@");
    }

    public void testMoveURLWithAtSign2URLInDir() throws Exception {
        testMoveURL2URL("folder/@file", "filemove1");
        testMoveURL2URL("folder/fi@le", "filemove2");
        testMoveURL2URL("folder/file@", "filemove3");
    }

    private void testMoveURL2URL(String srcPath, String targetFileName) throws Exception {
        createAndCommitParentFolders(srcPath);
        File file = createFile(srcPath);
        add(file);
        commit(file);
                
        File filemove = createFile(renameFile(srcPath, targetFileName));
        filemove.delete(); // we're operating with repository directly, cannot leave unversioned files lying on disk (they would be committed in the next round)
        
        ISVNClientAdapter c = getNbClient();
        c.move(getFileUrl(file), getFileUrl(filemove), "move", SVNRevision.HEAD);

        ISVNInfo info = null;
        SVNClientException ex = null;
        try {
            getInfo(getFileUrl(file));
        } catch (SVNClientException e) {
            ex = e;
        }
        assertNull(info);
        assertNotNull(ex);
        
        info = getInfo(getFileUrl(filemove));
        assertNotNull(info);
        assertEquals(getFileUrl(filemove), TestUtilities.decode(info.getUrl()));
        
        assertNotifiedFiles(new File[] {});        
    }        
    
    public void testMoveURL2URLPrevRevision() throws Exception {
        testMoveURL2URLPrevRevision("file", "filemove");
    }

    @RandomlyFails
    public void testMoveURL2URLWithAtSignPrevRevision() throws Exception {
        testMoveURL2URLPrevRevision("file1", "@filemove"); // fails until fixed in svn - http://subversion.tigris.org/issues/show_bug.cgi?id=3416
        testMoveURL2URLPrevRevision("file2", "file@move");
        testMoveURL2URLPrevRevision("file3", "filemove@");
    }

    public void testMoveURLWithAtSign2URLPrevRevision() throws Exception {
        testMoveURL2URLPrevRevision("@file", "filemove1");
        testMoveURL2URLPrevRevision("fi@le", "filemove2");
        testMoveURL2URLPrevRevision("file@", "filemove3");
    }

    private void testMoveURL2URLPrevRevision(String srcPath,
                                             String targetFileName) throws Exception {
        createAndCommitParentFolders(srcPath);
        File file = createFile(srcPath);
        write(file, 1);
        add(file);
        commit(file);
        SVNRevision prevRev = getRevision(file);
        write(file, 2);
        commit(getWC());        
        
        File filemove = createFile(renameFile(srcPath, targetFileName));
        filemove.delete(); // we're operating with repository directly, cannot leave unversioned files lying on disk (they would be committed in the next round)
        
        ISVNClientAdapter c = getNbClient();
        c.copy(getFileUrl(file), getFileUrl(filemove), "move", prevRev);

        ISVNLogMessage[] logs = getLog(getFileUrl(filemove));
        assertEquals(((SVNRevision.Number)prevRev).getNumber() ,logs[0].getChangedPaths()[0].getCopySrcRevision().getNumber());

        InputStream is = getContent(getFileUrl(filemove));
        assertContents(is, 1);
        assertNotifiedFiles(new File[] {});        
    }           
    
    public void testMoveFile2File() throws Exception {
        testMoveFile2File("file", "filemove");
    }

    public void testMoveFileWithAtSign2File() throws Exception {
        testMoveFile2File("@file", "filemove1");
        testMoveFile2File("fi@le", "filemove2");
        testMoveFile2File("file@", "filemove3");
    }

    public void testMoveFile2FileWithAtSign() throws Exception {
//        testMoveFile2File("file1", "@filemove"); // fails until fixed in svn - http://subversion.tigris.org/issues/show_bug.cgi?id=3416
        testMoveFile2File("file2", "file@move");
        testMoveFile2File("file3", "filemove@");
    }

    public void testMoveFile2FileInDir() throws Exception {
        testMoveFile2File("folder/file", "filemove");
    }

    public void testMoveFileWithAtSign2FileInDir() throws Exception {
        testMoveFile2File("folder/@file", "filemove1");
        testMoveFile2File("folder/fi@le", "filemove2");
        testMoveFile2File("folder/file@", "filemove3");
    }

    public void testMoveFile2FileWithAtSignInDir() throws Exception {
//        testMoveFile2File("folder/file1", "@filemove"); // fails until fixed in svn - http://subversion.tigris.org/issues/show_bug.cgi?id=3416
        testMoveFile2File("folder/file2", "file@move");
        testMoveFile2File("folder/file3", "filemove@");
    }

    private void testMoveFile2File(String srcPath, String targetFileName) throws Exception {
        createAndCommitParentFolders(srcPath);
        File file = createFile(srcPath);
        add(file);
        commit(file);
                
        File filemove = new File(getWC(), renameFile(srcPath, targetFileName));
        
        ISVNClientAdapter c = getNbClient();
        c.move(file, filemove, true);

        assertTrue(filemove.exists());
        assertStatus(SVNStatusKind.ADDED, filemove);
        if (isSvnkit()) {
            // no notification about target, instead "Copying    target_path" comes into logMessage()
            assertNotifiedFiles(new File[] {file});
        } else {
            assertNotifiedFiles(new File[] {file, filemove});
        }
    }        
    
}
