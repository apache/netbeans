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

package org.netbeans.modules.subversion.client.commands;

import org.netbeans.modules.subversion.client.AbstractCommandTestCase;
import java.io.File;
import java.nio.file.Files;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.HashSet;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author tomas
 */
public class StatusTestHidden extends AbstractCommandTestCase {
    
    // XXX terst remote change
    
    private enum StatusCall {
        filearray,
        file
    }
    
    public StatusTestHidden(String testName) throws Exception {
        super(testName);
    }

    public void testStatusFileWrong() throws Exception {                                
        statusWrong(StatusCall.file);
    }
    
    public void testStatusFileArrayWrong() throws Exception {                                
        statusWrong(StatusCall.filearray);
    }
        
    private void statusWrong(StatusCall call) throws Exception {
        // XXX add ref client
        // does not exist
        File file = new File(getWC(), "file") ;

        ISVNClientAdapter c = getNbClient();
        SVNClientException ex = null;        
        try {
            switch (call) {
                case file:                    
                    c.getStatus(new File[]{ file });                    
                    break;                
                case filearray:                    
                    c.getStatus(file, true, true, true, true);                    
                    break;
            }
        } catch (SVNClientException e) {
            ex = e;
        }
        assertNull(ex);
    }
//
    public void testStatusFileArray() throws Exception {
        File uptodate = createFile("uptodate");
        add(uptodate);        
        File deleted = createFile("deleted");
        add(deleted);        
        commit(getWC());                                        
        
        File ignoredFile = createFile("ignoredfile");
        ignore(ignoredFile);
        File ignoredFolder = createFolder("ignoredfolder");
        ignore(ignoredFolder);
        File fileInIgnoredFolder = createFile(ignoredFolder, "fileInIgnoredFolder");                
        commit(getWC());                                        
        
        File notmanagedfolder = createFolder("notmanagedfolder");
        File notmanagedfile = createFile(notmanagedfolder, "notmanagedfile");
        File unversioned = Files.createTempFile("unversioned", null).toFile();        // XXX extra test in unversioned WC
        File added = createFile("added");
        add(added);
        remove(deleted);
        
        File external = createExternal("externals");
        
        File[] files = new File[] { notmanagedfile, notmanagedfolder, unversioned, added, uptodate, deleted, ignoredFile, ignoredFolder, fileInIgnoredFolder, external };
        ISVNStatus[] sNb;
        if (isCommandLine()) {
            try {
                getNbClient().getStatus(files);
                fail("Should fail with this client, status throws a warning");
            } catch (SVNClientException ex) {
                assertTrue(SvnClientExceptionHandler.isUnversionedResource(ex.getMessage()));
                files = new File[] { notmanagedfolder, added, uptodate, deleted, ignoredFile, ignoredFolder, external };
            }
        }
        sNb = getNbClient().getStatus(files);
        
        assertEquals(files.length, sNb.length);
        ISVNStatus[] sRef = getFullWorkingClient().getStatus(files);
        assertStatus(sRef, sNb);
    }

    /**
     * Simulates wrong implementation of CommandlineClient.getStatus(File, boolean, boolean)
     * This status call does not return status for deleted (svn remove) files. Unfortunately it's called from FileStatusCache, so there's a chance we're missing something
     */
    public void testStatusDeletedFile () throws Exception {
        File deleted = createFile("deleted");
        File folder = createFolder("f");
        folder = createFolder(folder, "f");
        File file = createFile(folder, "f");
        add(deleted);
        add(folder.getParentFile());
        add(file);
        commit(getWC());

        remove(deleted);
        ISVNStatus[] sNb = getNbClient().getStatus(deleted.getParentFile(), true, true);
        assertEquals(5, sNb.length);
        assertFiles(sNb, new File[] {deleted, deleted.getParentFile()});

        // now test for regression of the fix - no additional statuses may be returned and we should not miss locally new files either
        sNb = getNbClient().getStatus(folder.getParentFile(), true, true);
        assertEquals(3, sNb.length);
        assertFiles(sNb, new File[] {folder, folder.getParentFile(), file});

        sNb = getNbClient().getStatus(folder.getParentFile(), false, true);
        assertEquals(2, sNb.length);
        assertFiles(sNb, new File[] {folder, folder.getParentFile()});

        sNb = getNbClient().getStatus(folder, false, true);
        assertEquals(2, sNb.length);
        assertFiles(sNb, new File[] {file, folder});

        File file2 = createFile(folder, "f2");
        sNb = getNbClient().getStatus(folder, false, true);
        assertEquals(3, sNb.length);
        assertFiles(sNb, new File[] {file, file2, folder});
    }

    private void assertFiles (ISVNStatus[] sNb, File[] files) {
        int foundFiles = 0;
        HashSet<File> fileSet = new HashSet<File>(Arrays.asList(files));
        for (ISVNStatus status : sNb) {
            if (fileSet.contains(status.getFile())) {
                ++foundFiles;
            }
        }
        assertEquals(files.length, foundFiles);
    }
    
    public void testStatusFile() throws Exception {
        File uptodate = createFile("uptodate");
        add(uptodate);        
        File deleted = createFile("deleted");
        add(deleted);        
        commit(getWC());                                        
        
        File ignoredFile = createFile("ignoredfile");
        ignore(ignoredFile);
        File ignoredFolder = createFolder("ignoredfolder");
        ignore(ignoredFolder);
        File fileInIgnoredFolder = createFile(ignoredFolder, "fileInIgnoredFolder");                
        commit(getWC());                                        
        
        File notmanagedfolder = createFolder("notmanagedfolder");
        File notmanagedfile = createFile(notmanagedfolder, "notmanagedfile");
        File unversioned = Files.createTempFile("unversioned", null).toFile();        // XXX extra test in unversioned WC
        File added = createFile("added");
        add(added);
        remove(deleted);
                        
        //                          descend  getAll contactServer  ignoreExternals
        status(notmanagedfolder,    false,   true,  false,          true            , 1, false);               
        status(notmanagedfolder,    false,   false, false,          true            , 1, false);               
        status(notmanagedfile,      false,   true,  false,          true            , 1, false);               
        status(notmanagedfile,      false,   false, false,          true            , 1, false);               
        status(unversioned,         false,   true,  false,          true            , 1, false);               
        status(unversioned,         false,   false, false,          true            , 1, false);               
        status(added,               false,   true,  false,          true            , 1, false);               
        status(added,               false,   false, false,          true            , 1, false);               
        status(uptodate,            false,   true,  false,          true            , 1, false);               
        status(uptodate,            false,   false, false,          true            , 0, false);               
        status(deleted,             false,   true,  false,          true            , 1, false);               
        status(deleted,             false,   false, false,          true            , 1, false);               
        status(ignoredFile,         false,   false, false,          true            , 1, false);
        status(ignoredFolder,       false,   false, false,          true            , 1, false);
        status(fileInIgnoredFolder, false,   false, false,          true            , 1, false);
  
    }        
    
    public void testStatusFolder() throws Exception {
        File notmanagedFolder1 = createFolder("notmanagedfolder1");
        File notmanagedFolder2 = createFolder(notmanagedFolder1, "notmanagedfolder2");
        File notmanagedFile = createFile(notmanagedFolder2, "notmanagedfile");
        
        File unversionedFolder1 = createFolder("unversionedfolder1");        
        File unversionedFolder2 = createFolder(unversionedFolder1, "unversionedfolder2");        
        File unversionedFile = createFile(unversionedFolder2, "unversionedfile");        
        
        File addedFolder1 = createFolder("addedfolder1");
        File addedFolder2 = createFolder(addedFolder1, "addedfolder2");
        File addedFile = createFile(addedFolder2, "addedfile");        
        add(addedFolder1);
        add(addedFolder2);
        add(addedFile);
        
        File uptodateFolder1 = createFolder("uptodatefolder1");
        File uptodateFolder2 = createFolder(uptodateFolder1, "uptodatefolder2");
        File uptodateFile = createFile(uptodateFolder2, "uptodatefile");
        add(uptodateFolder1);
        add(uptodateFolder2);
        add(uptodateFile);
        commit(uptodateFolder1);        
        
        File ignoredFolder = createFolder("ignoredfolder");
        ignore(ignoredFolder);
        File fileInIgnoredFolder = createFile(ignoredFolder, "fileInIgnoredFolder");                
        
        File deletedFolder1 = createFolder("deletedfolder1");
        File deletedFolder2 = createFolder(deletedFolder1, "deletedfolder2");
        File deletedFile = createFile(deletedFolder2, "deletedfile");
        add(deletedFolder1);
        add(deletedFolder2);
        add(deletedFile);
        commit(deletedFolder1);        
        remove(deletedFolder1);
        
        File externals = createExternal("externals");
        
        //                        descend  getAll contactServer  ignoreExternals
        status(notmanagedFolder1,  false,  true,  false,          true           , 1, false);               
        status(notmanagedFolder1,  true,   true,  false,          true           , 1, false);               
        status(notmanagedFolder2,  false,  false, false,          true           , 1, false);               
        status(notmanagedFolder2,  true,   false, false,          true           , 1, false);               
                
        status(unversionedFolder1, false,  true,  false,          true          , 1, false);               
        status(unversionedFolder1, true,   true,  false,          true          , 1, false);                       
        status(unversionedFolder2, false,  true,  false,          true          , 1, false);               
        status(unversionedFolder2, true,   true,  false,          true          , 1, false);                       
        
        status(addedFolder1,       false,  true,  false,          true          , 2, false);               
        status(addedFolder1,       true,   true,  false,          true          , 3, false);                       
        
        status(uptodateFolder1,    false,  true,  false,          true          , 2, false);               
        status(uptodateFolder1,    true,   true,  false,          true          , 3, false);               
        status(uptodateFolder1,    false,  false, false,          true          , 0, false);               
        status(uptodateFolder1,    true,   false, false,          true          , 0, false);               
        
        status(deletedFolder1,     false,  true,  false,          true          , 2, false);               
        status(deletedFolder1,     true,   false, false,          true          , 3, false);               
        
        status(ignoredFolder,      false,  true,  false,          true          , 1, false);               
        status(ignoredFolder,      true,   false, false,          true          , 1, false);               
        
        status(externals,          false,  true,  false,          false         , 2, false);               
        status(externals,          true,   true,  false,          false         , 4, false);               
        status(externals,          true,   false, false,          false         , 1, false);               
        status(externals,          false,  false, false,          false         , 1, false);               
        
        status(externals,          false,  true,  false,          true          , 2, false);               
        status(externals,          true,   false, false,          true          , 1, false);               
    }        
        
    private void status(File file, boolean descend, boolean getAll, boolean contactServer, boolean ignoreExternals, int c, boolean shouldFail) throws Exception {        
        ISVNStatus[] sNb;
        try {
            sNb  = getNbClient().getStatus(file, descend, getAll, contactServer, ignoreExternals);
            if (shouldFail) {
                // there's an issue with tyhe selected client, works differently from javahl client
                // when this starts failing, it means the client's behavior is fixed
                fail("This should fail: " + file);
            }
        } catch (SVNClientException ex) {
            assertTrue("Not supposed to fail:" + ex.getMessage(), shouldFail);
            assertTrue(SvnClientExceptionHandler.isUnversionedResource(ex.getMessage()));
            return;
        }
        assertEquals(c, sNb.length);
        ISVNStatus[] sRef = getFullWorkingClient().getStatus(file, descend, getAll, contactServer, ignoreExternals);
        assertStatus(sRef, sNb);
    }

    private void assertStatus(ISVNStatus[] refs, ISVNStatus[] nbs) {
        assertEquals(refs.length, nbs.length);
        for (int i = 0; i < nbs.length; i++) {
            
            Subversion.LOG.info("assertStatus " + refs[i].getPath());
            
            assertEquals(refs[i].getConflictNew(),          nbs[i].getConflictNew());
            assertEquals(refs[i].getConflictOld(),          nbs[i].getConflictOld());
            assertEquals(refs[i].getConflictWorking(),      nbs[i].getConflictWorking());
            if (refs[i].getLastChangedDate() != null) {
                assertEquals(DateFormat.getDateTimeInstance().format(refs[i].getLastChangedDate()),
                        DateFormat.getDateTimeInstance().format(nbs[i].getLastChangedDate()));
            }
            assertEquals(refs[i].getFile(),                 nbs[i].getFile());
            if (refs[i].getLastChangedRevision() != null) {
                assertEquals(refs[i].getLastChangedRevision(),  nbs[i].getLastChangedRevision());
            }
            if (refs[i].getLastCommitAuthor() != null) {
                assertEquals(refs[i].getLastCommitAuthor(),     nbs[i].getLastCommitAuthor());
            }
            assertEquals(refs[i].getLockComment(),          nbs[i].getLockComment());
            assertEquals(refs[i].getLockCreationDate(),     nbs[i].getLockCreationDate());
            assertEquals(refs[i].getLockOwner(),            nbs[i].getLockOwner());
            assertEquals(refs[i].getNodeKind(),             nbs[i].getNodeKind());
            assertEquals(refs[i].getPath(),                 nbs[i].getPath());
            assertEquals(refs[i].getPropStatus(),           nbs[i].getPropStatus());
            assertEquals(refs[i].getRepositoryPropStatus(), nbs[i].getRepositoryPropStatus());
            assertEquals(refs[i].getRepositoryTextStatus(), nbs[i].getRepositoryTextStatus());
            if (refs[i].getRevision() != null) {
                assertEquals(refs[i].getRevision(),             nbs[i].getRevision());
            }
            assertEquals(refs[i].getTextStatus(),           nbs[i].getTextStatus());
            assertEquals(refs[i].getUrl(),                  nbs[i].getUrl());
            assertEquals(refs[i].getUrlString(),            nbs[i].getUrlString());                       
        }
    } 
    
    private File createExternal(String fileName) throws Exception {
        File externals = createFolder(fileName);                
        add(externals);
        SVNUrl repo2Url = copyRepo("createExternal-" + System.currentTimeMillis());

        try {
            mkdir(repo2Url.appendPath("e1"));
            mkdir(repo2Url.appendPath("e1").appendPath("e2"));
            mkdir(repo2Url.appendPath("e1").appendPath("e2").appendPath("e3"));
        } catch (SVNClientException e) {
            if(e.getMessage().indexOf("file already exists") > -1) {
                throw e;
            }
        }
        setProperty(externals, "svn:externals", "e1/e2\t" + repo2Url.appendPath("e1").appendPath("e2").toString().replace(" ", "%20"));
        
        commit(externals);
        update(externals);        
        
        assertTrue(new File(new File(new File(externals, "e1"), "e2"), "e3").exists());
        
        return externals;
    }
}
