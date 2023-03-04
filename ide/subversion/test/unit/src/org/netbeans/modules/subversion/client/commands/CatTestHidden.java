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
import java.io.FileInputStream;
import java.io.InputStream;
import org.netbeans.modules.versioning.util.FileUtils;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 *
 * @author tomas
 */
public class CatTestHidden extends AbstractCommandTestCase {
    
    public CatTestHidden(String testName) throws Exception {
        super(testName);
    }
             
    public void testCatFile() throws Exception {                                                
        testCatFile("file");
    }                   

    public void testCatFileWithAtSign() throws Exception {
        testCatFile("@file");
        testCatFile("fi@le");
        testCatFile("file@");
    }

    public void testCatFileInDir() throws Exception {
        testCatFile("folder/file");
    }

    public void testCatFileInDirWithAtSign() throws Exception {
        testCatFile("folder/@file");
        testCatFile("folder/fi@le");
        testCatFile("folder/file@");
    }

    private void testCatFile(String path) throws Exception {
        createAndCommitParentFolders(path);
        File file = createFile(path);
        write(file, 1);
        add(file);
        commit(file);

        InputStream is1 = getNbClient().getContent(file, SVNRevision.HEAD);
        InputStream is2 = new FileInputStream(file);

        assertInputStreams(is2, is1);
    }
    
    public void testCatFilePrevRev() throws Exception {
        testCatFilePrevRev("file");
    }

    public void testCatFileWithAtSignPrevRev() throws Exception {
        testCatFilePrevRev("@file");
        testCatFilePrevRev("fi@le");
        testCatFilePrevRev("file@");
    }

    private void testCatFilePrevRev(String path) throws Exception {
        createAndCommitParentFolders(path);
        File file = createFile(path);
        write(file, 1);
        add(file);
        commit(file);

        File prevRevisionCopy = new File(file.getParentFile(), "prevRevisionCopy");
        FileUtils.copyFile(file, prevRevisionCopy);

        ISVNClientAdapter c = getNbClient();        
        SVNRevision prevrev = getRevision(file);
        write(file, 2);        
        commit(file);
        
        InputStream is1 = c.getContent(file, prevrev);
        InputStream is2 = new FileInputStream(prevRevisionCopy);
        
        assertInputStreams(is2, is1);
    }               

    public void testCatURL() throws Exception {
        testCatURL("file");
    }

    public void testCatURLWithAtSign() throws Exception {
        testCatURL("@file");
        testCatURL("fi@le");
        testCatURL("file@");
    }

    public void testCatURLInDir() throws Exception {
        testCatURL("folder/file");
    }
    
    public void testCatURLInDirWithAtSign() throws Exception {
        testCatURL("folder/@file");
        testCatURL("folder/fi@le");
        testCatURL("folder/file@");
    }
    
    private void testCatURL(String path) throws Exception {
        createAndCommitParentFolders(path);
        File file = createFile(path);
        write(file, 1);
        add(file);
        commit(file);
        
        ISVNClientAdapter c = getNbClient();        
        InputStream is1 = c.getContent(getFileUrl(file), SVNRevision.HEAD);
        InputStream is2 = new FileInputStream(file);
        
        assertInputStreams(is2, is1);
    }               

    public void testCatURLPrevRev() throws Exception {
        testCatURLPrevRev("file");
    }

    public void testCatURLWithAtSignPrevRev() throws Exception {
        testCatURLPrevRev("@file");
        testCatURLPrevRev("fi@le");
        testCatURLPrevRev("file@");
    }

    private void testCatURLPrevRev(String path) throws Exception {
        File file = createFile(path);
        write(file, 1);
        add(file);
        commit(file);

        File prevRevisionCopy = new File(file.getParentFile(), "prevRevisionCopy");
        FileUtils.copyFile(file, prevRevisionCopy);

        ISVNClientAdapter c = getNbClient();        
        SVNRevision prevrev = getRevision(file);
        write(file, 2);        
        commit(file);
        
        InputStream is1 = c.getContent(getFileUrl(file), prevrev);
        InputStream is2 = new FileInputStream(prevRevisionCopy);
        
        assertInputStreams(is2, is1);
    }               
    
}
