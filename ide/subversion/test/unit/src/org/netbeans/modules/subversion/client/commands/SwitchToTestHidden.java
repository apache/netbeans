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

/**
 *
 * @author tomas
 */
public class SwitchToTestHidden extends AbstractCommandTestCase {
    
    public SwitchToTestHidden(String testName) throws Exception {
        super(testName);
    }
    
    public void testSwitchToFile() throws Exception {                                        
        File file = createFile("file");
        add(file);
        commit(file);
                
        File filecopy = createFile("filecopy");
        
        ISVNClientAdapter c = getNbClient();
        c.copy(getFileUrl(file), getFileUrl(filecopy), "copy", SVNRevision.HEAD);

        assertCopy(getFileUrl(filecopy));
        assertInfo(file, getFileUrl(file));
        
        c.switchToUrl(file, getFileUrl(filecopy), SVNRevision.HEAD, false);
        
        assertInfo(file, getFileUrl(filecopy));         
        assertNotifiedFiles();// XXX empty also in svnCA - why?! - no output from cli
    }                 
    
    public void testSwitchToFilePrevRev() throws Exception {                                        
        File file = createFile("file");
        add(file);
        write(file, 1);
        commit(file);
        
        File filecopy = createFile("filecopy");
        
        ISVNClientAdapter c = getNbClient();
        c.copy(getFileUrl(file), getFileUrl(filecopy), "copy", SVNRevision.HEAD);

        assertCopy(getFileUrl(filecopy));
        assertInfo(file, getFileUrl(file));
        
        // switch to copy
        c.switchToUrl(file, getFileUrl(filecopy), SVNRevision.HEAD, false);        
        assertInfo(file, getFileUrl(filecopy));        
        
        SVNRevision prevrev = getRevision(file);
        
        // change copy
        write(file, 2);
        commit(file);
        
        // switch to trunk
        c.switchToUrl(file, getFileUrl(file), SVNRevision.HEAD, false);        
        assertInfo(file, getFileUrl(file));        
        
        // switch to copies prev revision
        c.switchToUrl(file, getFileUrl(filecopy), prevrev, false);    
        
        // test
        assertInfo(file, getFileUrl(filecopy));        
        assertContents(file, 1);
        SVNRevision rev = getRevision(file);
        assertEquals(((SVNRevision.Number)prevrev).getNumber(), ((SVNRevision.Number)rev).getNumber());
        assertNotifiedFiles(file);        
    }                 

    public void testSwitchToFolderNonRec() throws Exception {                                        
        File folder = createFolder("folder");
        File file = createFile(folder, "file");
        File folder1 = createFolder(folder, "folder1");
        File file1 = createFile(folder1, "file1");
        add(folder);
        add(file);
        add(folder1);
        add(file1);
        commit(folder);
                
        File foldercopy = createFolder("foldercopy");
        
        ISVNClientAdapter c = getNbClient();
        c.copy(getFileUrl(folder), getFileUrl(foldercopy), "copy", SVNRevision.HEAD);

        assertCopy(getFileUrl(foldercopy));
        assertInfo(folder, getFileUrl(folder));
        assertInfo(file, getFileUrl(folder).appendPath(file.getName()));
        assertInfo(folder1, getFileUrl(folder).appendPath(folder1.getName()));
        assertInfo(file1, getFileUrl(folder).appendPath(folder1.getName()).appendPath(file1.getName()));
        
        c.switchToUrl(folder, getFileUrl(foldercopy), SVNRevision.HEAD, false);
        
        assertInfo(folder, getFileUrl(foldercopy));
        assertInfo(file, getFileUrl(foldercopy).appendPath(file.getName()));        
        assertInfo(folder1, getFileUrl(folder).appendPath(folder1.getName()));
        assertInfo(file1, getFileUrl(folder).appendPath(folder1.getName()).appendPath(file1.getName()));
        assertNotifiedFiles(new File[] {});  // XXX empty also in svnCA - why?! - no output from cli      
    }

    public void testSwitchToFolderRec() throws Exception {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");
        File folder1 = createFolder(folder, "folder1");
        File file1 = createFile(folder1, "file1");
        add(folder);
        add(file);
        add(folder1);
        add(file1);
        commit(folder);

        File foldercopy = createFolder("foldercopy");

        ISVNClientAdapter c = getNbClient();
        c.copy(getFileUrl(folder), getFileUrl(foldercopy), "copy", SVNRevision.HEAD);

        assertCopy(getFileUrl(foldercopy));
        assertInfo(folder, getFileUrl(folder));
        assertInfo(file, getFileUrl(folder).appendPath(file.getName()));
        assertInfo(folder1, getFileUrl(folder).appendPath(folder1.getName()));
        assertInfo(file1, getFileUrl(folder).appendPath(folder1.getName()).appendPath(file1.getName()));

        c.switchToUrl(folder, getFileUrl(foldercopy), SVNRevision.HEAD, true);

        assertInfo(folder, getFileUrl(foldercopy));
        assertInfo(file, getFileUrl(foldercopy).appendPath(file.getName()));
        assertInfo(folder1, getFileUrl(foldercopy).appendPath(folder1.getName()));
        assertInfo(file1, getFileUrl(foldercopy).appendPath(folder1.getName()).appendPath(file1.getName()));
        assertNotifiedFiles(new File[] {});       // XXX empty also in svnCA - why?! - no output from cli
    }

    public void testSwitchToFolderWithAtSignRec() throws Exception {
        if(!shouldBeTestedWithCurrentClient(true, false)) {
            return;
        }
        File folder = createFolder("fol@der");
        File file = createFile(folder, "file");
        File folder1 = createFolder(folder, "folder1");
        File file1 = createFile(folder1, "file1");
        add(folder);
        add(file);
        add(folder1);
        add(file1);
        commit(folder);

        File foldercopy = createFolder("folder@copy");

        ISVNClientAdapter c = getNbClient();
        c.copy(getFileUrl(folder), getFileUrl(foldercopy), "copy", SVNRevision.HEAD);

        assertCopy(getFileUrl(foldercopy));
        assertInfo(folder, getFileUrl(folder));
        assertInfo(file, getFileUrl(folder).appendPath(file.getName()));
        assertInfo(folder1, getFileUrl(folder).appendPath(folder1.getName()));
        assertInfo(file1, getFileUrl(folder).appendPath(folder1.getName()).appendPath(file1.getName()));

        c.switchToUrl(folder, getFileUrl(foldercopy), SVNRevision.HEAD, true);

        assertInfo(folder, getFileUrl(foldercopy));
        assertInfo(file, getFileUrl(foldercopy).appendPath(file.getName()));
        assertInfo(folder1, getFileUrl(foldercopy).appendPath(folder1.getName()));
        assertInfo(file1, getFileUrl(foldercopy).appendPath(folder1.getName()).appendPath(file1.getName()));
        assertNotifiedFiles(new File[] {});       // XXX empty also in svnCA - why?! - no output from cli
    }
        
}
