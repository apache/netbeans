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
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

/**
 *
 * @author tomas
 */
public class RemoveTestHidden extends AbstractCommandTestCase {
    
    public RemoveTestHidden(String testName) throws Exception {
        super(testName);
    }
    
    public void testRemoveFile() throws Exception {
        testRemoveFile("file");
    }

    public void testRemoveFileWithAtSign() throws Exception {
        testRemoveFile("@file");
        testRemoveFile("fi@le");
        testRemoveFile("file@");
    }

    public void testRemoveFileInDir() throws Exception {
        testRemoveFile("folder/file");
    }

    public void testRemoveFileWithAtSignInDir() throws Exception {
        testRemoveFile("folder/@file");
        testRemoveFile("folder/fi@le");
        testRemoveFile("folder/file@");
    }

    private void testRemoveFile(String filePath) throws Exception {
        createAndCommitParentFolders(filePath);
        File file = createFile(filePath);
        add(file);
        commit(file);
                
        assertTrue(file.exists());
        
        ISVNClientAdapter c = getNbClient();
        c.remove(new File[] {file}, true);

        assertFalse(file.exists());
        assertStatus(SVNStatusKind.DELETED, file);    
        assertNotifiedFiles(new File[] {file});        
    }            
    
    public void testRemoveFolder() throws Exception {
        testRemoveFolder("folder");
    }

    public void testRemoveFolderWithAtSign() throws Exception {
        testRemoveFolder("@folder");
        testRemoveFolder("fol@der");
        testRemoveFolder("folder@");
    }

    private void testRemoveFolder(String folderName) throws Exception {
        File folder = createFolder(folderName);
        add(folder);
        commit(folder);
                
        assertTrue(folder.exists());
        
        ISVNClientAdapter c = getNbClient();
        c.remove(new File[] {folder}, true);

        assertFalse(folder.exists());
        assertStatus(SVNStatusKind.DELETED, folder);   
        assertNotifiedFiles(new File[] {folder});        
    }            
    
    public void testRemoveFileTree() throws Exception {
        testRemoveFileTree("folder", "file1", "file2");
    }

    public void testRemoveFileTreeWithAtSigns() throws Exception {
        testRemoveFileTree("@folder1", "@file1", "@file2");
        testRemoveFileTree("@folder2", "fi@le1", "fi@le2");
        testRemoveFileTree("@folder3", "file1@", "file2@");
        testRemoveFileTree("fol@der1", "@file1", "@file2");
        testRemoveFileTree("fol@der2", "fi@le1", "fi@le2");
        testRemoveFileTree("fol@der3", "file1@", "file2@");
        testRemoveFileTree("folder1@", "@file1", "@file2");
        testRemoveFileTree("folder2@", "fi@le1", "fi@le2");
        testRemoveFileTree("folder3@", "file1@", "file2@");
    }

    public void testRemoveFileTree(String folderName,
                                   String fileName1,
                                   String fileName2) throws Exception {
        File folder = createFolder(folderName);
        File file1 = createFile(folder, fileName1);
        File file2 = createFile(folder, fileName2);
        add(folder);
        commit(folder);
                
        assertTrue(folder.exists());
        
        ISVNClientAdapter c = getNbClient();
        c.remove(new File[] {folder}, true);

        assertFalse(folder.exists());
        assertFalse(file1.exists());
        assertFalse(file2.exists());
        assertStatus(SVNStatusKind.DELETED, folder);        
        assertStatus(SVNStatusKind.DELETED, file1);        
        assertStatus(SVNStatusKind.DELETED, file2);        
        assertNotifiedFiles(new File[] {folder, file1, file2});        
    }            
    
}
