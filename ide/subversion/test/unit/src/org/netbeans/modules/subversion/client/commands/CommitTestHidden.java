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
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

/**
 *
 * @author tomas
 */
public class CommitTestHidden extends AbstractCommandTestCase {
    
    public CommitTestHidden(String testName) throws Exception {
        super(testName);
    }

    public void testCommitFile() throws Exception {                
        testCommitFile("file");
    }

    public void testCommitFileWithAtSign() throws Exception {
        testCommitFile("@file");
        testCommitFile("fi@le");
        testCommitFile("file@");
    }

    public void testCommitFileInDir() throws Exception {
        testCommitFile("folder/file");
    }

    public void testCommitFileWithAtSignInDir() throws Exception {
        testCommitFile("folder/@file");
        testCommitFile("folder/fi@le");
        testCommitFile("folder/file@");
    }

    private void testCommitFile(String path) throws Exception {
        createAndCommitParentFolders(path);
        File file = createFile(path);

        add(file);
        assertStatus(SVNStatusKind.ADDED, file);

        SVNRevision revisionBefore = (SVNRevision.Number) getRevision(getRepoUrl());

        ISVNClientAdapter client = getNbClient();

        long r = client.commit(new File[] {file}, "commit", true);

        SVNRevision revisionAfter = (SVNRevision.Number) getRevision(getRepoUrl());

        assertTrue(file.exists());
        assertStatus(SVNStatusKind.NORMAL, file);
        assertNotSame(revisionBefore, revisionAfter);
        assertEquals(((SVNRevision.Number)revisionAfter).getNumber(), r);
        assertNotifiedFiles(file);
    }
    
    public void testCommitFolder() throws Exception {                
        testCommitFolder("folder", "file");
    }

    public void testCommitFolderWithAtSign() throws Exception {
        testCommitFolder("@folder", "file1");
        testCommitFolder("fol@der", "file2");
        testCommitFolder("folder@", "file3");
    }

    private void testCommitFolder(String folderName, String fileName) throws Exception {
        File folder = createFolder(folderName);
        File file = createFile(folder, fileName);
                
        add(folder);               
        assertStatus(SVNStatusKind.ADDED, file);
        assertStatus(SVNStatusKind.ADDED, folder);

        SVNRevision revisionBefore = (SVNRevision.Number) getRevision(getRepoUrl());
        
        ISVNClientAdapter client = getNbClient();        
        long r = client.commit(new File[] {folder}, "commit", true);
        
        SVNRevision revisionAfter = (SVNRevision.Number) getRevision(getRepoUrl());
        
        assertTrue(file.exists());        
        assertStatus(SVNStatusKind.NORMAL, file);                
        assertStatus(SVNStatusKind.NORMAL, folder);                
        assertNotSame(revisionBefore, revisionAfter);
        assertEquals(((SVNRevision.Number)revisionAfter).getNumber(), r);
        assertNotifiedFiles(new File[] {file, folder});
        
    }
    
    public void testCommitFolderNonRecursively() throws Exception {                
        File folder = createFolder("folder");
        File file = createFile(folder, "file");
                
        add(folder);               
        assertStatus(SVNStatusKind.ADDED, file);
        assertStatus(SVNStatusKind.ADDED, folder);

        SVNRevision revisionBefore = (SVNRevision.Number) getRevision(getRepoUrl());
        
        ISVNClientAdapter client = getNbClient();        
        long r = client.commit(new File[] {folder}, "commit", false);
        
        SVNRevision revisionAfter = (SVNRevision.Number) getRevision(getRepoUrl());
        
        assertTrue(file.exists());        
        assertStatus(SVNStatusKind.ADDED, file);                
        assertStatus(SVNStatusKind.NORMAL, folder);                
        assertNotSame(revisionBefore, revisionAfter);
        assertEquals(((SVNRevision.Number)revisionAfter).getNumber(), r);
        assertNotifiedFiles(new File[] {folder});
        
    }    

}
