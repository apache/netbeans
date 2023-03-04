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
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author tomas
 */
public class ResolvedTestHidden extends AbstractCommandTestCase {
    
    public ResolvedTestHidden(String testName) throws Exception {
        super(testName);
    }
           
    protected void setUp() throws Exception {
        importWC = true;
        if(getName().startsWith("testResolved")) {
            importWC = false;            
        } 
        super.setUp();      
    }
    
    public void testResolved() throws Exception {
        testResolved("wc1", "wc2", "file");
    }

    public void testResolvedFileWithAtSign1() throws Exception {
        testResolved("wc1", "wc2", "@file");
    }

    public void testResolvedFileWithAtSign2() throws Exception {
        testResolved("wc1", "wc2", "fi@le");
    }

    public void testResolvedFileWithAtSign3() throws Exception {
        testResolved("wc1", "wc2", "file@");
    }

    private void testResolved(String folder1Name,
                              String folder2Name,
                              String fileName) throws Exception {
                        
        File wc1 = createFolder(folder1Name);
        File file1 = createFile(wc1, fileName);
        write(file1, 1);
        importFile(wc1);
        assertStatus(SVNStatusKind.NORMAL, wc1);
        assertStatus(SVNStatusKind.NORMAL, file1);
                        
        File wc2 = createFolder(folder2Name);
        File file2 = new File(wc2, fileName);
        
        SVNUrl url = getTestUrl().appendPath(wc1.getName());
        ISVNClientAdapter c = getNbClient();         
        c.checkout(url, wc2, SVNRevision.HEAD, true);        
        assertStatus(SVNStatusKind.NORMAL, file2);
        assertContents(file2, 1);
        
        write(file2, 2);        
        assertStatus(SVNStatusKind.MODIFIED, file2);
        commit(file2);
        assertStatus(SVNStatusKind.NORMAL, file2);
                        
        write(file1, 3);
        
        c.update(file1, SVNRevision.HEAD, false);
        
        assertStatus(SVNStatusKind.CONFLICTED, file1);
        
        write(file1, 2);
        clearNotifiedFiles();
        c.resolved(file1);
        
        assertStatus(SVNStatusKind.NORMAL, file1);
        
        if (isSvnkit()) {
            // svnkit does not notify about resolved files
            assertNotifiedFiles();
        } else {
            assertNotifiedFiles(file1);
        }
    }        
}
