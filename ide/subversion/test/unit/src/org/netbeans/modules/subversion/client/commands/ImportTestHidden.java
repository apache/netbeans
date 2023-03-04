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
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author tomas
 */
public class ImportTestHidden extends AbstractCommandTestCase {
    
    public ImportTestHidden(String testName) throws Exception {
        super(testName);
    }
    
    public void testImportFile() throws Exception {
        testImportFile("file", "targetdir");
    }

    public void testImportFileWithAtSign() throws Exception {
//        testImportFile("@file", "targedir"); // fails until fixed in svn - http://subversion.tigris.org/issues/show_bug.cgi?id=3416
        testImportFile("fi@le", "targedir");
        testImportFile("file@", "targedir2");
    }

    public void testImportFileToUrlWithAtSign() throws Exception {
//        testImportFile("file", "@targetdir"); // fails until fixed in svn - http://subversion.tigris.org/issues/show_bug.cgi?id=3416
        testImportFile("file", "target@dir");
        testImportFile("file", "targetdir@");
    }

    public void testImportFileWithAtSignToUrlWithAtSign() throws Exception {
        testImportFile("fi@le", "target@dir"); // fails until fixed in svn - http://subversion.tigris.org/issues/show_bug.cgi?id=3416
//        testImportFile("@file", "@targetdir"); // fails until fixed in svn - http://subversion.tigris.org/issues/show_bug.cgi?id=3416
    }

    public void testImportFile(String fileToImport, String lastUrlPart) throws Exception {
        File file = createFile(fileToImport);
                
        assertTrue(file.exists());
        
        ISVNClientAdapter c = getNbClient();
        SVNUrl url = getRepoUrl().appendPath(getName()).appendPath(lastUrlPart);
        c.doImport(file, url, "imprd", false);

        assertTrue(file.exists());
        assertStatus(SVNStatusKind.UNVERSIONED, file);

        ISVNInfo info = getInfo(url);
        assertNotNull(info);        
        assertEquals(url.toString(), TestUtilities.decode(info.getUrl()).toString());
        assertNotifiedFiles(new File[] {file});    // XXX empty also in svnCA - why?! - no output from cli    
    }            
    
    public void testImportFolder() throws Exception {                                        
        File folder = createFolder("folder");
                
        assertTrue(folder.exists());
        
        ISVNClientAdapter c = getNbClient();
        SVNUrl url = getTestUrl().appendPath(getName()); 
        c.mkdir(url, "mrkvadir");        
        url = url.appendPath(folder.getName());
        c.mkdir(url, "mrkvadir");        
        c.doImport(folder, url, "imprd", false);

        assertTrue(folder.exists());
        assertStatus(SVNStatusKind.UNVERSIONED, folder);
        
        ISVNInfo info = getInfo(url);
        assertNotNull(info);        
        assertEquals(url.toString(), TestUtilities.decode(info.getUrl()).toString());
        assertNotifiedFiles(new File[] {});        // XXX empty also in svnCA - why?! - no output from cli
    }
    
    public void testImportFolderNonRecursivelly() throws Exception {                                        
        File folder = createFolder("folder");
        File folder1 = createFolder(folder, "folder1");
        File file = createFile(folder1, "file");
                
        assertTrue(folder.exists());
        assertTrue(folder1.exists());
        assertTrue(file.exists());
        
        ISVNClientAdapter c = getNbClient();
        SVNUrl url = getTestUrl().appendPath(getName());
        c.mkdir(url, "mrkvadir");        
        url = url.appendPath(folder.getName());
        c.mkdir(url, "mrkvadir");        
        c.doImport(folder, url, "imprd", false);

        assertTrue(folder.exists());
        assertTrue(folder1.exists());
        assertTrue(file.exists());
        assertStatus(SVNStatusKind.UNVERSIONED, folder);
        assertStatus(SVNStatusKind.UNVERSIONED, folder1);
        assertStatus(SVNStatusKind.UNVERSIONED, file);
        
        ISVNInfo info = getInfo(url);
        assertNotNull(info);        
        assertEquals(url.toString(), TestUtilities.decode(info.getUrl()).toString());
        
        SVNClientException ex = null;
        try {
            info = getInfo(url.appendPath(folder1.getName()));
        } catch (SVNClientException e) {
            ex = e;
        }
        assertNotNull(ex);
        assertNotifiedFiles(new File[] {});        // XXX empty also in svnCA - why?! - no output from cli
    }
    
    public void testImportFolderRecursivelly() throws Exception {                                        
        File folder = createFolder("folder");
        File folder1 = createFolder(folder, "folder1");
        File file = createFile(folder1, "file");
                
        assertTrue(folder.exists());
        assertTrue(folder1.exists());
        assertTrue(file.exists());
        
        ISVNClientAdapter c = getNbClient();
        SVNUrl url = getTestUrl().appendPath(getName());
        c.mkdir(url, "mrkvadir");        
        url = url.appendPath(folder.getName());
        c.doImport(folder, url, "imprd", true);

        assertTrue(folder.exists());
        assertTrue(folder1.exists());
        assertTrue(file.exists());
        assertStatus(SVNStatusKind.UNVERSIONED, folder);
        assertStatus(SVNStatusKind.UNVERSIONED, folder1);
        assertStatus(SVNStatusKind.UNVERSIONED, file);
        
        ISVNInfo info = getInfo(url);
        assertNotNull(info);        
        assertEquals(url.toString(), TestUtilities.decode(info.getUrl()).toString());
        
        url = url.appendPath(folder1.getName());
        info = getInfo(url);
        assertNotNull(info);        
        assertEquals(url.toString(), TestUtilities.decode(info.getUrl()).toString());
        
        url = url.appendPath(file.getName());
        info = getInfo(url);        
        assertNotNull(info);        
        assertEquals(url.toString(), TestUtilities.decode(info.getUrl()).toString());
        assertNotifiedFiles(new File[] {folder1, file}); 
    }
    
}
