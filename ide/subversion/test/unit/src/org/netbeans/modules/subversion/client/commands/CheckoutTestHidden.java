/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
// XXX add referenceclient
package org.netbeans.modules.subversion.client.commands;

import org.netbeans.modules.subversion.client.AbstractCommandTestCase;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author tomas
 */
public class CheckoutTestHidden extends AbstractCommandTestCase {
    
    public CheckoutTestHidden(String testName) throws Exception {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        importWC = true;
        if(getName().startsWith("testCheckout")) {
            importWC = false;            
        } 
        super.setUp();
        if(getName().startsWith("testCheckout")) {
            cleanUpRepo(new String[] {CI_FOLDER});
        }        
    }

    public void testCheckoutFile() throws Exception {
        testCheckoutFiles("file");
    }

    public void testCheckoutFileWithAtSign() throws Exception {
        testCheckoutFiles("@file", "fi@le", "file@");
    }

    private void testCheckoutFiles(String... fileNames) throws Exception {
        File folder = createFolder(CI_FOLDER);

        List<File> files = new ArrayList<File>(4);
        for (String fileName : fileNames) {
            files.add(createFile(folder, fileName));
        }

        importFile(folder);

        File checkout = createFolder("checkoutfolder");
        SVNUrl url = getTestUrl().appendPath(folder.getName());
        ISVNClientAdapter c = getNbClient();
        c.checkout(url, checkout, SVNRevision.HEAD, true);

        assertStatus(SVNStatusKind.NORMAL, checkout);

        List<File> chFiles = new ArrayList<File>(files.size());
        for (File file : files) {
            File chFile = new File(checkout, file.getName());

            assertTrue(chFile.exists());
            assertStatus(SVNStatusKind.NORMAL, chFile);

            chFiles.add(chFile);
        }

        assertNotifiedFiles(chFiles.toArray(new File[0]));
    }
    
    public void testCheckoutFolder() throws Exception {
        testCheckoutFolder(CI_FOLDER, "checkoutFolder");
    }

    public void testCheckoutFolderWithAtSign() throws Exception {
        if(!shouldBeTestedWithCurrentClient(true, false)) {
            return;
        }
        testCheckoutFolder('@' + CI_FOLDER, "checkoutFolder");
    }

    public void testCheckoutFolderToDirWithAtSign() throws Exception {
        if(!shouldBeTestedWithCurrentClient(true, false)) {
            return;
        }
        testCheckoutFolder(CI_FOLDER, "@checkoutFolder");
    }

    public void testCheckoutFolderWithAtSignToDirWithAtSign() throws Exception {
        if(!shouldBeTestedWithCurrentClient(true, false)) {
            return;
        }
        testCheckoutFolder('@' + CI_FOLDER, "@checkoutFolder");
    }

    private void testCheckoutFolder(String repoFolderName,
                                    String targetFolderName) throws Exception {
        File cifolder = createFolder(repoFolderName);
        File folder1 = createFolder(cifolder, "folder1");
        File file = createFile(folder1, "file");

        importFile(cifolder);        

        File checkout = createFolder(targetFolderName);
        SVNUrl url = getTestUrl().appendPath(cifolder.getName());
        ISVNClientAdapter c = getNbClient();         
        c.checkout(url, checkout, SVNRevision.HEAD, true);
                
        File chFolder1 = new File(checkout, folder1.getName());
        File chFile = new File(chFolder1, file.getName());
        
        assertTrue(chFolder1.exists());
        assertTrue(chFile.exists());
        assertStatus(SVNStatusKind.NORMAL, checkout);                   
        assertStatus(SVNStatusKind.NORMAL, chFolder1);        
        assertStatus(SVNStatusKind.NORMAL, chFile);

        assertNotifiedFiles(new File[] {chFolder1, chFile});
    }

    public void testCheckoutFolderNonRecursivelly() throws Exception {
        File cifolder = createFolder(CI_FOLDER);
        File folder1 = createFolder(cifolder, "folder1");
        File file = createFile(folder1, "file");
        
        importFile(cifolder);        

        File checkout = createFolder("checkoutfolder");
        SVNUrl url = getTestUrl().appendPath(cifolder.getName());
        ISVNClientAdapter c = getNbClient();         
        c.checkout(url, checkout, SVNRevision.HEAD, false);
                
        File chFolder1 = new File(checkout, folder1.getName());
        File chFile = new File(chFolder1, file.getName());

        if(isCommandLine()) {
            assertFalse(chFolder1.exists());
        } else {
            assertTrue(chFolder1.exists());
        }
        assertFalse(chFile.exists());
        assertStatus(SVNStatusKind.NORMAL, checkout);
        if(isCommandLine()) {
            assertStatus(SVNStatusKind.UNVERSIONED, chFolder1);
        } else {
            assertStatus(SVNStatusKind.NORMAL, chFolder1);
        }
        assertStatus(SVNStatusKind.NONE, chFile); 
        
        assertNotifiedFiles(new File[] {});
    }

    public void testCheckoutFolderPrevRevision() throws Exception {
        testCheckoutFolderPrevRevision(CI_FOLDER);
    }

    public void testCheckoutFolderWithAtSignPrevRevision() throws Exception {
        if(!shouldBeTestedWithCurrentClient(true, false)) {
            return;
        }
        testCheckoutFolderPrevRevision('@' + CI_FOLDER);
    }

    private void testCheckoutFolderPrevRevision(String repoFolderName) throws Exception {
        File cifolder = createFolder(repoFolderName);
        File folder1 = createFolder(cifolder, "folder1");
        File file = createFile(folder1, "file");
        
        importFile(cifolder);        
        SVNRevision revision = getRevision(getRepoUrl());
        File addedFile = createFile(folder1, "addedfile");
        add(addedFile);
        commit(cifolder);
        assertStatus(SVNStatusKind.NORMAL, addedFile);                
        
        File checkout = createFolder("checkoutfolder");
        SVNUrl url = getTestUrl().appendPath(cifolder.getName());
        ISVNClientAdapter c = getNbClient();         
        c.checkout(url, checkout, revision, true);
                
        File chFolder1 = new File(checkout, folder1.getName());
        File chFile = new File(chFolder1, file.getName());
        File chAddedFile = new File(chFolder1, addedFile.getName());
        
        assertTrue(chFolder1.exists());
        assertTrue(chFile.exists());
        assertTrue(!chAddedFile.exists());
        assertStatus(SVNStatusKind.NORMAL, checkout);                
        assertStatus(SVNStatusKind.NORMAL, chFolder1);        
        assertStatus(SVNStatusKind.NORMAL, chFile);                
        assertStatus(SVNStatusKind.NONE, chAddedFile);                
        
        assertNotifiedFiles(new File[] {chFolder1, chFile});
    }


}
