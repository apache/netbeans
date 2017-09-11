/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion.client.commands;

import org.netbeans.modules.subversion.client.AbstractCommandTestCase;
import java.io.File;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

/**
 *
 * @author tomas
 */
public class AddTestHidden extends AbstractCommandTestCase {
    
    public AddTestHidden(String testName) throws Exception {
        super(testName);
    }

    public void testAddFile() throws Exception {
        testAddFile("file");
    }

    public void testAddFileWithAtSign() throws Exception {
        testAddFile("@file");
        testAddFile("fi@le");
        testAddFile("file@");
    }

    public void testAddFileInDir() throws Exception {
        testAddFile("dir/file");
    }

    public void testAddFileInDirWithAtSign() throws Exception {
        testAddFile("dir/@file");
        testAddFile("dir/fi@le");
        testAddFile("dir/file@");
    }

    private void testAddFile(String path) throws Exception {
        createAndAddParentFolders(path);
        File file = createFile(path);
        assertStatus(SVNStatusKind.UNVERSIONED, file);

        ISVNClientAdapter c = getNbClient();
        c.addFile(file);

        assertStatus(SVNStatusKind.ADDED, file);
        assertNotifiedFiles(file);
    }

    public void testAddAddedFile() throws Exception {
        File file = createFile("file");

        assertStatus(SVNStatusKind.UNVERSIONED, file);

        ISVNClientAdapter c = getNbClient();

        c.addFile(file);

        assertStatus(SVNStatusKind.ADDED, file);

        try {
            c.addFile(file);
            fail("should fail");
        } catch (SVNClientException ex) {
            
        }
        assertStatus(SVNStatusKind.ADDED, file);

        assertNotifiedFiles(file);
    }

    public void testAddNoFile() throws Exception {
        File file = new File(getWC(), "fail");

        assertStatus(SVNStatusKind.NONE, file);

        ISVNClientAdapter c = getNbClient();

        SVNClientException e = null;
        try {
            c.addFile(file);
        } catch (SVNClientException ex) {
            e = ex;
        }
        if (isJavahl()) {
            assertNotNull(e);
            assertTrue(e.getMessage().indexOf("is not a working copy") > -1
                    || e.getMessage().indexOf("not found") > -1);
        }

        assertNotifiedFiles(new File[]{});
    }

    public void testAddNotExistingFile() throws Exception {
        File folder = createFolder("folder");
        File file = new File(folder, "fail");

        assertStatus(SVNStatusKind.UNVERSIONED, file);
        
        ISVNClientAdapter c = getNbClient();
        SVNClientException e = null;        
        try {
            c.addFile(file);
        } catch (SVNClientException ex) {
            e = ex;
        }
        assertNotNull(e);
                        
        assertTrue(SvnClientExceptionHandler.isUnversionedResource(e.getMessage()) || e.getMessage().toLowerCase().contains("not found")
                 || e.getMessage().toLowerCase().contains("some targets don't exist"));
        
        assertNotifiedFiles(new File[]{});  
    }

//    XXX no more api !!!
//    public void testAddFileRecursivelly() throws Exception {
//        File folder = createFolder("folder");
//        File file = createFile(folder, "file");
//
//        assertStatus(SVNStatusKind.UNVERSIONED, file);
//        assertStatus(SVNStatusKind.UNVERSIONED, folder);
//
//        ISVNClientAdapter c = getNbClient();
//
//        c.addFile(new File[] {folder}, true);
//
//        assertStatus(SVNStatusKind.ADDED, folder);
//        assertStatus(SVNStatusKind.ADDED, file);
//
//        assertNotifiedFiles(new File[]{file, folder});
//    }
    
    public void testAddFileNonRecursivelly() throws Exception {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");

        assertStatus(SVNStatusKind.UNVERSIONED, file);
        assertStatus(SVNStatusKind.UNVERSIONED, folder);

        ISVNClientAdapter c = getNbClient();
        c.addDirectory(folder, false);

        assertStatus(SVNStatusKind.ADDED, folder);
        assertStatus(SVNStatusKind.UNVERSIONED, file);

        assertNotifiedFiles(folder);
    }

    public void testAddDirectory() throws Exception {
        File folder = createFolder("folder");

        assertStatus(SVNStatusKind.UNVERSIONED, folder);
        ISVNClientAdapter c = getNbClient();
        c.addDirectory(folder, false);
        assertStatus(SVNStatusKind.ADDED, folder);

        assertNotifiedFiles(folder);
    }

    public void testAddAddedFolder() throws Exception {
        File folder = createFolder("folder");

        assertStatus(SVNStatusKind.UNVERSIONED, folder);
        ISVNClientAdapter c = getNbClient();
        c.addDirectory(folder, false);
        assertStatus(SVNStatusKind.ADDED, folder);

        try {
            c.addDirectory(folder, false);
            fail("cmd client cannot throw the exception");
        } catch (SVNClientException ex) {
        }
        assertStatus(SVNStatusKind.ADDED, folder);

        assertNotifiedFiles(new File[] { folder });
    }

    public void testAddNoDirectory() throws Exception {
        File file = new File(getWC(), "fail");

        assertStatus(SVNStatusKind.NONE, file);

        SVNClientException e = null;
        try {
            ISVNClientAdapter c = getNbClient();
            c.addDirectory(file, false);
        } catch (SVNClientException ex) {
            e = ex;
        }
        if (isJavahl()) {
            assertNotNull(e);
            assertTrue(e.getMessage().indexOf("is not a working copy") > -1 || e.getMessage().indexOf("not found") > -1);
        }

        assertNotifiedFiles(new File[] {});
    }

    @RandomlyFails
    public void testAddUnversionedDirectory() throws Exception {
        File parentFolder = createFolder("folder");
        File folder = createFolder(parentFolder, "fail");
        
        assertStatus(SVNStatusKind.UNVERSIONED, parentFolder);        
        assertStatus(SVNStatusKind.UNVERSIONED, folder);        
                
        // javahl and svnkit work as with --parents, auto add also parents.
        // fails with commandline client, --parents should be implemented in cli impl
        getNbClient().addFile(folder);
        assertNotifiedFiles(new File[]{folder, parentFolder});  
    }
    
    public void testAddFolderRecursivelly() throws Exception {
        File folder = createFolder("folder");
        File file = createFolder(folder, "folder");

        assertStatus(SVNStatusKind.UNVERSIONED, file);
        assertStatus(SVNStatusKind.UNVERSIONED, folder);

        ISVNClientAdapter c = getNbClient();
        c.addDirectory(folder, true);

        assertStatus(SVNStatusKind.ADDED, folder);
        assertStatus(SVNStatusKind.ADDED, file);

        assertNotifiedFiles(new File[] {folder, file});
    }

    public void testAddFolderRecursivellyForced() throws Exception {
        File parentFolder = createFolder("folder");
        File folder = createFolder(parentFolder, "folder");

        assertStatus(SVNStatusKind.UNVERSIONED, parentFolder);
        assertStatus(SVNStatusKind.UNVERSIONED, folder);

        ISVNClientAdapter c = getNbClient();
        c.addDirectory(parentFolder, true, true);

        assertStatus(SVNStatusKind.ADDED, parentFolder);
        assertStatus(SVNStatusKind.ADDED, folder);

        assertNotifiedFiles(new File[] { parentFolder, folder });
    }

    public void testAddFolderNonRecursivelly() throws Exception {
        File parentFolder = createFolder("folder");
        File folder = createFolder(parentFolder, "file");

        assertStatus(SVNStatusKind.UNVERSIONED, parentFolder);
        assertStatus(SVNStatusKind.UNVERSIONED, folder);

        ISVNClientAdapter c = getNbClient();
        c.addDirectory(parentFolder, false);

        assertStatus(SVNStatusKind.ADDED, parentFolder);
        assertStatus(SVNStatusKind.UNVERSIONED, folder);

        assertNotifiedFiles(parentFolder );
    }
}
