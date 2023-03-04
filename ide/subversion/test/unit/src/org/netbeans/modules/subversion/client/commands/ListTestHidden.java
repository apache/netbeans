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
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 *
 * @author tomas
 */
public class ListTestHidden extends AbstractCommandTestCase {
    
    public ListTestHidden(String testName) throws Exception {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        try {
            super.setUp();
        } catch (Exception e) {
            stopSvnServer();
        }
        if(getName().equals("testListNullAuthor")) {
            setAnnonWriteAccess();
            runSvnServer();
        }
    }
    
    @Override
    protected void tearDown() throws Exception {
        if(getName().equals("testListNullAuthor")) {
            restoreAuthSettings();
        }
        super.tearDown();
    }    
    
    @Override
    protected String getRepoURLProtocol() {
        if(getName().equals("testListNullAuthor")) {        
            return "svn://localhost/";
        }
        return super.getRepoURLProtocol();
    }                

    public void testListWrongFile() throws Exception {                                
        // XXX add refclient
        SVNClientException e1 = null;
        try {
            getNbClient().getList(getRepoUrl().appendPath("arancha"), SVNRevision.HEAD, false);
        } catch (SVNClientException e) {
            e1 = e;
        }
        SVNClientException e2 = null;
        try {
            list(getRepoUrl().appendPath("arancha"));        
        } catch (SVNClientException e) {
            e2 = e;
        }
        assertNotNull(e1);
        assertNotNull(e2);
        assertTrue(SvnClientExceptionHandler.isWrongUrl(e1.getMessage()));
        assertTrue(SvnClientExceptionHandler.isWrongUrl(e2.getMessage()));
    }
    
    public void testListNoFile() throws Exception {                                
        ISVNClientAdapter c = getNbClient();
        ISVNDirEntry[] entries1 = c.getList(getTestUrl().appendPath(getWC().getName()), SVNRevision.HEAD, false);
                        
        assertEquals(0, entries1.length);
    }
    
    public void testListFiles() throws Exception {                        
        File file1 = createFile("file1");
        File file2 = createFile("file2");
        File file3 = createFile("file3");
                
        add(file1);                       
        add(file2);                       
        add(file3);                       
        commit(getWC());
                                
        ISVNDirEntry[] entries1 = getNbClient().getList(getTestUrl().appendPath(getWC().getName()), SVNRevision.HEAD, false);        
        assertEquals(3, entries1.length);
        ISVNDirEntry[] entries2 = getFullWorkingClient().getList(getTestUrl().appendPath(getWC().getName()), SVNRevision.HEAD, false);
        
        assertEntryArrays(entries1, entries2);
    }
    
    public void testListFilesRecursively() throws Exception {                        
        File folder = createFolder("file1");
        File file1 = createFile(folder, "file1");
        File file2 = createFile(folder, "file2");
        File file3 = createFile(folder, "file3");
                
        add(folder);                       
        add(file1);                       
        add(file2);                       
        add(file3);                       
        commit(getWC());
                        
        ISVNDirEntry[] entries1 = getNbClient().getList(getTestUrl().appendPath(getWC().getName()), SVNRevision.HEAD, true);        
        assertEquals(4, entries1.length);
        ISVNDirEntry[] entries2 = getFullWorkingClient().getList(getTestUrl().appendPath(getWC().getName()), SVNRevision.HEAD, true);
        
        assertEntryArrays(entries1, entries2);
    }

//    XXX not idea how to push a null username through svnclientadapter
//    public void testListNullAuthor() throws Exception {
//        File file = createFile("file");
//
//        add(file);
//        commit(getWC());
//
//        ISVNClientAdapter c = getNbClient();
//        ISVNDirEntry[] entries = c.getList(getTestUrl().appendPath(getWC().getName()), SVNRevision.HEAD, false);
//
//        assertNull(entries[0].getLastCommitAuthor());
//
//    }
    
}
