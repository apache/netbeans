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

package org.netbeans.modules.subversion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.queries.VersioningQuery;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.subversion.utils.TestUtilities;
import org.netbeans.modules.versioning.masterfs.VersioningAnnotationProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Test is written for 1.7 subversion support, do not even try to run it with 1.6 client
 * @author Tomas Stupka
 */
public class InteceptorTest extends NbTestCase {
    public static final String PROVIDED_EXTENSIONS_REMOTE_LOCATION = "ProvidedExtensions.RemoteLocation";
   
    private File dataRootDir;
    private FileStatusCache cache;
    private SVNUrl repoUrl;
    private File wc;
    private File wc2;
    private File repoDir;
    private File repo2Dir;
    private SVNUrl repo2Url;
        
    public InteceptorTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {          
        super.setUp();
        if (!"javahl".equals(System.getProperty("svnClientAdapterFactory", null))
                && !"commandline".equals(System.getProperty("svnClientAdapterFactory", null))
                && !"svnkit".equals(System.getProperty("svnClientAdapterFactory", null))) {
            System.setProperty("svnClientAdapterFactory", "svnkit");
        }
        MockServices.setServices(new Class[] {
            VersioningAnnotationProvider.class,
            SubversionVCS.class});
        System.setProperty("data.root.dir", getDataDir().getAbsolutePath()) ;
        dataRootDir = new File(System.getProperty("data.root.dir")); 
        FileUtil.refreshFor(dataRootDir);
        wc = new File(dataRootDir, getName() + "_wc");
        wc2 = new File(dataRootDir, getName() + "_wc2");
        repoDir = new File(dataRootDir, "repo");
        String repoPath = repoDir.getAbsolutePath();
        if(repoPath.startsWith("/")) repoPath = repoPath.substring(1);
        repoUrl = new SVNUrl("file:///" + repoPath);
        
        repo2Dir = new File(dataRootDir, "repo2");
        repo2Url = new SVNUrl(TestUtilities.formatFileURL(repo2Dir));

        System.setProperty("netbeans.user", System.getProperty("data.root.dir") + "/userdir");
        cache = Subversion.getInstance().getStatusCache();
        cache.cleanUp();
        
        cleanUpWC(wc);
        cleanUpWC(wc2);
        initRepo();      
        
        wc.mkdirs();
        wc2.mkdirs();
        svnimport();                   
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        cleanUpWC(wc);
        cleanUpWC(wc2);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();

        suite.addTest(getAttributeSuite());

        suite.addTest(createSuite());

        suite.addTest(deleteSuite());

        suite.addTest(renameViaDataObjectSuite());
        suite.addTest(renameViaFileObjectSuite());

        suite.addTest(moveViaDataObjectSuite());
        suite.addTest(moveViaFileObjectSuite());

        suite.addTest(copyViaDataObjectSuite());
        suite.addTest(copyViaFileObjectSuite());
        
        suite.addTest(modifySuite());

        return suite;
    }
    
    public static Test modifySuite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new InteceptorTest("modifyFileOnDemandLock"));
        return(suite);
    }
    
    public static Test getAttributeSuite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new InteceptorTest("getWrongAttribute"));
        suite.addTest(new InteceptorTest("getRemoteLocationAttribute"));
        suite.addTest(new InteceptorTest("getIsManaged"));
        return(suite);
    }

    public static Test deleteSuite() {
	TestSuite suite = new TestSuite();
        suite.addTest(new InteceptorTest("deleteCreateChangeCase_issue_157373"));
        suite.addTest(new InteceptorTest("deleteNotVersionedFile"));
        suite.addTest(new InteceptorTest("deleteVersionedFileExternally"));
        suite.addTest(new InteceptorTest("deleteVersionedFile"));
        suite.addTest(new InteceptorTest("deleteVersionedFolder"));
        suite.addTest(new InteceptorTest("deleteNotVersionedFolder"));
        suite.addTest(new InteceptorTest("deleteWCRoot"));
        suite.addTest(new InteceptorTest("deleteVersionedFileTree"));
        suite.addTest(new InteceptorTest("deleteNotVersionedFileTree"));
        return(suite);
    }
   
    public static Test createSuite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new InteceptorTest("createNewFile"));
        suite.addTest(new InteceptorTest("createNewFolder"));
        suite.addTest(new InteceptorTest("deleteA_CreateA"));
        suite.addTest(new InteceptorTest("deleteA_CreateAOnDemandLocking"));
        suite.addTest(new InteceptorTest("deleteA_CreateA_RunAtomic"));
        suite.addTest(new InteceptorTest("afterDelete_AfterCreate_194998"));
        return(suite);
    }
    
    public static Test renameViaDataObjectSuite() {
	TestSuite suite = new TestSuite();
        suite.addTest(new InteceptorTest("renameVersionedFile_DO"));
        suite.addTest(new InteceptorTest("renameUnversionedFile_DO"));
        suite.addTest(new InteceptorTest("renameUnversionedFolder_DO"));
        suite.addTest(new InteceptorTest("renameAddedFile_DO"));
        suite.addTest(new InteceptorTest("renameA2B2A_DO"));
        suite.addTest(new InteceptorTest("renameA2B2C_DO"));
        suite.addTest(new InteceptorTest("renameA2B2C2A_DO"));
        suite.addTest(new InteceptorTest("renameA2B_CreateA_DO"));
        suite.addTest(new InteceptorTest("deleteA_RenameB2A_DO_129805"));
        suite.addTest(new InteceptorTest("renameVersionedFolder_DO"));
        suite.addTest(new InteceptorTest("renameFileTree_DO"));
        suite.addTest(new InteceptorTest("renameA2CB2A_DO"));
        suite.addTest(new InteceptorTest("deleteA_renameB2A2B_DO"));
        suite.addTest(new InteceptorTest("deleteA_renameUnversioned2A_DO"));
        return(suite);
    }
    
    public static Test moveViaDataObjectSuite() {
	TestSuite suite = new TestSuite();
        suite.addTest(new InteceptorTest("moveVersionedFile_DO"));
        suite.addTest(new InteceptorTest("moveUnversionedFile_DO"));
        suite.addTest(new InteceptorTest("moveUnversionedFolder_DO"));
        suite.addTest(new InteceptorTest("moveAddedFile2UnversionedFolder_DO"));
        suite.addTest(new InteceptorTest("moveVersionedFile2IgnoredFolder_DO"));
        suite.addTest(new InteceptorTest("moveAddedFile2VersionedFolder_DO"));
        suite.addTest(new InteceptorTest("moveA2B2A_DO"));
        suite.addTest(new InteceptorTest("moveA2B2C_DO"));
        suite.addTest(new InteceptorTest("moveA2B2C2A_DO"));
        suite.addTest(new InteceptorTest("moveA2B_CreateA_DO"));
        suite.addTest(new InteceptorTest("moveVersionedFolder_DO"));
        suite.addTest(new InteceptorTest("moveFileTree_DO"));
        suite.addTest(new InteceptorTest("moveVersionedFile2Repos_DO"));
        suite.addTest(new InteceptorTest("moveVersionedFolder2Repos_DO"));
        suite.addTest(new InteceptorTest("moveFileTree2Repos_DO"));
        suite.addTest(new InteceptorTest("moveA2CB2A_DO"));
        suite.addTest(new InteceptorTest("deleteA_moveB2A2B_DO"));
        suite.addTest(new InteceptorTest("deleteA_moveUnversioned2A_DO"));
        return(suite);
    }
    
    public static Test renameViaFileObjectSuite() {
	TestSuite suite = new TestSuite();
        suite.addTest(new InteceptorTest("renameVersionedFile_FO"));
        suite.addTest(new InteceptorTest("renameUnversionedFile_FO"));
        suite.addTest(new InteceptorTest("renameUnversionedFolder_FO"));
        suite.addTest(new InteceptorTest("renameAddedFile_FO"));
        suite.addTest(new InteceptorTest("renameA2B2A_FO"));
        suite.addTest(new InteceptorTest("renameA2B2C_FO"));
        suite.addTest(new InteceptorTest("renameA2B2C2A_FO"));
        suite.addTest(new InteceptorTest("renameA2B_CreateA_FO"));
        suite.addTest(new InteceptorTest("deleteA_RenameB2A_FO_129805"));
        suite.addTest(new InteceptorTest("renameVersionedFolder_FO"));
        suite.addTest(new InteceptorTest("renameFileTree_FO"));
        suite.addTest(new InteceptorTest("renameA2CB2A_FO"));
        suite.addTest(new InteceptorTest("renameA2a_FO"));
        suite.addTest(new InteceptorTest("deleteA_renameB2A2B_FO"));
        suite.addTest(new InteceptorTest("deleteA_renameUnversioned2A_FO"));
        return(suite);
    }
    
    public static Test moveViaFileObjectSuite() {
	TestSuite suite = new TestSuite();
        suite.addTest(new InteceptorTest("moveVersionedFile_FO"));
        suite.addTest(new InteceptorTest("moveUnversionedFile_FO"));
        suite.addTest(new InteceptorTest("moveUnversionedFolder_FO"));
        suite.addTest(new InteceptorTest("moveAddedFile2UnversionedFolder_FO"));
        suite.addTest(new InteceptorTest("moveVersionedFile2IgnoredFolder_FO"));
        suite.addTest(new InteceptorTest("moveAddedFile2VersionedFolder_FO"));
        suite.addTest(new InteceptorTest("moveA2B2A_FO"));
        suite.addTest(new InteceptorTest("moveA2B2C_FO"));
        suite.addTest(new InteceptorTest("moveA2B2C2A_FO"));
        suite.addTest(new InteceptorTest("moveA2B_CreateA_FO"));
        suite.addTest(new InteceptorTest("moveVersionedFolder_FO"));
        suite.addTest(new InteceptorTest("moveFileTree_FO"));
        suite.addTest(new InteceptorTest("moveVersionedFile2Repos_FO"));
        suite.addTest(new InteceptorTest("moveVersionedFolder2Repos_FO"));
        suite.addTest(new InteceptorTest("moveFileTree2Repos_FO"));
        suite.addTest(new InteceptorTest("moveA2CB2A_FO"));
        suite.addTest(new InteceptorTest("deleteA_moveB2A2B_FO"));
        suite.addTest(new InteceptorTest("deleteA_moveUnversioned2A_FO"));
        
        return(suite);
    }


    public static Test copyViaDataObjectSuite() {
	TestSuite suite = new TestSuite();
        suite.addTest(new InteceptorTest("copyVersionedFile_DO"));
        suite.addTest(new InteceptorTest("copyUnversionedFile_DO"));
        suite.addTest(new InteceptorTest("copyUnversionedFolder_DO"));
        suite.addTest(new InteceptorTest("copyAddedFile2UnversionedFolder_DO"));
        suite.addTest(new InteceptorTest("copyAddedFile2VersionedFolder_DO"));

        suite.addTest(new InteceptorTest("copyVersionedFile2UnversionedFolder_DO"));
        suite.addTest(new InteceptorTest("copyVersionedFile2IgnoredFolder_DO"));
        suite.addTest(new InteceptorTest("copyVersionedFolder2UnversionedFolder_DO"));

        suite.addTest(new InteceptorTest("copyA2B2C_DO"));
        suite.addTest(new InteceptorTest("copyVersionedFolder_DO"));
        suite.addTest(new InteceptorTest("copyFileTree_DO"));
        suite.addTest(new InteceptorTest("copyVersionedFile2Repos_DO"));
        suite.addTest(new InteceptorTest("copyVersionedFolder2Repos_DO"));
        suite.addTest(new InteceptorTest("copyFileTree2Repos_DO"));
        suite.addTest(new InteceptorTest("deleteA_copyUnversioned2A_DO"));

        return(suite);
    }

    // XXX add tests for move/copy of ignored files
    public static Test copyViaFileObjectSuite() {
	TestSuite suite = new TestSuite();
        suite.addTest(new InteceptorTest("copyVersionedFile_FO"));
        suite.addTest(new InteceptorTest("copyUnversionedFile_FO"));
        suite.addTest(new InteceptorTest("copyUnversionedFolder_FO"));
        suite.addTest(new InteceptorTest("copyAddedFile2UnversionedFolder_FO"));
        suite.addTest(new InteceptorTest("copyVersionedFolder2UnversionedFolder_FO"));
        suite.addTest(new InteceptorTest("copyVersionedFile2UnversionedFolder_FO"));
        suite.addTest(new InteceptorTest("copyVersionedFile2IgnoredFolder_FO"));
        suite.addTest(new InteceptorTest("copyAddedFile2VersionedFolder_FO"));
        suite.addTest(new InteceptorTest("copyA2B2C_FO"));
        suite.addTest(new InteceptorTest("copyVersionedFolder_FO"));
        suite.addTest(new InteceptorTest("copyFileTree_FO"));
        suite.addTest(new InteceptorTest("copyVersionedFile2Repos_FO"));
        suite.addTest(new InteceptorTest("copyVersionedFolder2Repos_FO"));
        suite.addTest(new InteceptorTest("copyFileTree2Repos_FO"));
        suite.addTest(new InteceptorTest("deleteA_copyUnversioned2A_FO"));

        return(suite);
    }
    
    public void modifyFileOnDemandLock () throws Exception {
        // init
        File file = new File(wc, "file");
        file.createNewFile();
        commit(wc);
        getClient().propertySet(file, "svn:needs-lock", "true", false);
        commit(file);
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file).getTextStatus());

        SvnModuleConfig.getDefault().setAutoLock(true);
        // modify
        OutputStream os = FileUtil.toFileObject(file).getOutputStream();
        os.write(new byte[] { 'a', 0 });
        os.close();

        // test
        assertTrue(file.exists());
        assertEquals(SVNStatusKind.MODIFIED, getSVNStatus(file).getTextStatus());

        assertCachedStatus(file, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY_CONTENT | FileInformation.STATUS_LOCKED);

        commit(wc);

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file).getTextStatus());
    }

    public void getWrongAttribute() throws Exception {
        File file = new File(wc, "attrfile");
        file.createNewFile();
        FileObject fo = FileUtil.toFileObject(file);

        String str = (String) fo.getAttribute("peek-a-boo");
        assertNull(str);
    }

    public void getRemoteLocationAttribute() throws Exception {
        File file = new File(wc, "attrfile");
        file.createNewFile();
        FileObject fo = FileUtil.toFileObject(file);

        String str = (String) fo.getAttribute(PROVIDED_EXTENSIONS_REMOTE_LOCATION);
        assertNotNull(str);
        assertEquals(repoUrl.toString(), str);
    }

    public void getIsManaged() throws Exception {
        // unversioned file
        File file = new File(dataRootDir, "unversionedfile");
        file.createNewFile();

        boolean versioned = VersioningQuery.isManaged(Utilities.toURI(file));
        assertFalse(versioned);

        // metadata folder
        file = new File(wc, ".svn");

        versioned = VersioningQuery.isManaged(Utilities.toURI(file));
        assertTrue(versioned);

        // metadata file
        file = new File(new File(wc, ".svn"), "entries");

        versioned = VersioningQuery.isManaged(Utilities.toURI(file));
        assertTrue(versioned);

        // versioned file
        file = new File(wc, "attrfile");
        file.createNewFile();

        versioned = VersioningQuery.isManaged(Utilities.toURI(file));
        assertTrue(versioned);
    }

    public void deleteNotVersionedFile() throws Exception {
        // init        
        File file = new File(wc, "file");
        file.createNewFile();             
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file).getTextStatus());

        // delete
        delete(file);

        // test
        assertFalse(file.exists());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file).getTextStatus());
        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(file));
        
//        commit(wc);
    }

    public void deleteVersionedFile() throws Exception {
        // init
        File file = new File(wc, "file");
        file.createNewFile();
        commit(wc);
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file).getTextStatus());

        // delete
        delete(file);

        // test
        assertFalse(file.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(file).getTextStatus());

        assertCachedStatus(file, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);

        commit(wc);

        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file).getTextStatus());
    }

    public void deleteVersionedFileExternally() throws Exception {
        // init
        File file = new File(wc, "file");
        FileUtil.toFileObject(wc).createData(file.getName());
        assertCachedStatus(file, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        commit(wc);
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(file));

        String prop = System.getProperty("org.netbeans.modules.subversion.deleteMissingFiles", "");
        try {
            System.setProperty("org.netbeans.modules.subversion.deleteMissingFiles", "true");
            // delete externally
            file.delete();

            // test
            assertFalse(file.exists());
            assertEquals(SVNStatusKind.MISSING, getSVNStatus(file).getTextStatus());

            // notify changes
            FileUtil.refreshFor(file);
            assertCachedStatus(file, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        } finally {
            System.setProperty("org.netbeans.modules.subversion.deleteMissingFiles", prop);
        }
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(file).getTextStatus());
        commit(wc);
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file).getTextStatus());
    }

    public void deleteVersionedFolder() throws Exception {
        // init        
        File folder = new File(wc, "folder1");
        folder.mkdirs();
        commit(wc);      
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(folder).getTextStatus());

        // delete
        delete(folder);

        // test
        assertFalse(folder.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(folder).getTextStatus());
        
        assertCachedStatus(folder, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);        
        
        commit(wc);
        
        assertFalse(folder.exists());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());        
    }

    public void deleteNotVersionedFolder() throws IOException, SVNClientException {
        // init        
        File folder = new File(wc, "folder2");
        folder.mkdirs();
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());

        // delete
        delete(folder);
        
        // test
        assertFalse(folder.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(folder));
        
//        commit(wc);
    }    

    public void deleteWCRoot() throws Exception {
        // init        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(wc).getTextStatus());

        // delete
        delete(wc);
        
        // test
        assertTrue(!wc.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(wc).getTextStatus());        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(wc));
    }

    public void deleteVersionedFileTree() throws Exception {
        // init
        File folder = new File(wc, "folder");
        folder.mkdirs();
        File folder1 = new File(folder, "folder1");
        folder1.mkdirs();
        File folder2 = new File(folder, "folder2");
        folder2.mkdirs();        
        File file11 = new File(folder1, "file1");
        file11.createNewFile();
        File file12 = new File(folder1, "file2");
        file12.createNewFile();
        File file21 = new File(folder2, "file1");
        file21.createNewFile();
        File file22 = new File(folder2, "file2");
        file22.createNewFile();
        
        commit(wc);      
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(folder).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(folder1).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(folder2).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file11).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file12).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file21).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file22).getTextStatus());

        // delete
        delete(folder);
        
        // test
        assertFalse(folder.exists());
        assertFalse(folder1.exists());
        assertFalse(folder2.exists());
        assertFalse(file11.exists());
        assertFalse(file12.exists());
        assertFalse(file21.exists());
        assertFalse(file22.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(folder).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(folder1).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(folder2).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(file11).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(file12).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(file21).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(file22).getTextStatus());                
        
        assertCachedStatus(folder, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);        
        assertCachedStatus(folder1, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);        
        assertCachedStatus(folder2, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(file11));        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(file12));        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(file21));        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(file22));        
        
        
        commit(wc);
        assertFalse(folder.exists());
        assertFalse(folder1.exists());
        assertFalse(folder2.exists());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());                
    }

    public void deleteNotVersionedFileTree() throws Exception {
        // init
        File folder = new File(wc, "folder");
        folder.mkdirs();
        File folder1 = new File(folder, "folder1");
        folder1.mkdirs();
        File folder2 = new File(folder, "folder2");
        folder2.mkdirs();        
        File file11 = new File(folder1, "file1");
        file11.createNewFile();
        File file12 = new File(folder1, "file2");
        file12.createNewFile();
        File file21 = new File(folder2, "file1");
        file21.createNewFile();
        File file22 = new File(folder2, "file2");
        file22.createNewFile();
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder1).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder2).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file11).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file12).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file21).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file22).getTextStatus());

        // delete
        delete(folder);
        
        // test
        assertFalse(folder.exists());
        assertFalse(folder1.exists());
        assertFalse(folder2.exists());
        assertFalse(file11.exists());
        assertFalse(file12.exists());
        assertFalse(file21.exists());
        assertFalse(file22.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder1).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder2).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file11).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file12).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file21).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file22).getTextStatus());
        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(folder));        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(folder1));        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(folder2));        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(file11));        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(file12));        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(file21));        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(file22));        
        
        commit(wc);
        assertFalse(folder.exists());
        assertFalse(folder1.exists());
        assertFalse(folder2.exists());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());        
    }

    public void createNewFile() throws Exception {
        // init
        File file = new File(wc, "file");
        
        // create
        FileObject fo = FileUtil.toFileObject(wc);
        fo.createData(file.getName());
                                        
        // test 
        assertTrue(file.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file).getTextStatus());        
        assertCachedStatus(file, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
    }
    
    public void afterDelete_AfterCreate_194998 () throws Exception {
        // init
        File file = new File(wc, "file");
        
        // create
        FileObject fo = FileUtil.toFileObject(wc);
        fo.createData(file.getName());
        add(file);
        commit(file);
        
        // test 
        assertTrue(file.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file).getTextStatus());
        
        file.delete();
        FileUtil.refreshFor(file);
        assertEquals(SVNStatusKind.MISSING, getSVNStatus(file).getTextStatus());
        assertCachedStatus(file, FileInformation.STATUS_VERSIONED_DELETEDLOCALLY);
        
        TestKit.write(file, "modification");
        FileUtil.refreshFor(file.getParentFile());
        assertCachedStatus(file, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY_CONTENT);
        assertEquals(SVNStatusKind.MODIFIED, getSVNStatus(file).getTextStatus());
    }

    public void createNewFolder() throws Exception {
        // init
        File folder = new File(wc, "folder");
        
        // create
        FileObject fo = FileUtil.toFileObject(wc);
        fo.createFolder(folder.getName());
                                        
        // test 
        assertTrue(folder.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());        
        assertCachedStatus(folder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
    }
    
    public void deleteA_CreateA() throws IOException, SVNClientException {
        
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();        
        commit(wc);        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        
        // delete                
        FileObject fo = FileUtil.toFileObject(fileA);
        fo.delete();

        // test if deleted
        assertFalse(fileA.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());

        // create        
        fo.getParent().createData(fo.getName());       
        
        // test 
        assertTrue(fileA.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        
    }

    public void deleteA_CreateAOnDemandLocking() throws IOException, SVNClientException {
        // init
        File file = new File(wc, "A");
        file.createNewFile();
        commit(wc);
        SvnModuleConfig.getDefault().setAutoLock(true);
        getClient().propertySet(file, "svn:needs-lock", "true", false);
        commit(file);
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file).getTextStatus());
        
        // delete
        FileObject fo = FileUtil.toFileObject(file);
        fo.delete();

        // test if deleted
        assertFalse(file.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(file).getTextStatus());

        // create        
        fo.getParent().createData(fo.getName());       
        
        // test 
        assertTrue(file.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file).getTextStatus());        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE | FileInformation.STATUS_LOCKED, getStatus(file));                
    }

    public void deleteA_CreateA_RunAtomic() throws IOException, SVNClientException {
        // init
        final File fileA = new File(wc, "A");
        fileA.createNewFile();        
        commit(wc);        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        
        final FileObject fo = FileUtil.toFileObject(fileA);
        AtomicAction a = new AtomicAction() {
            public void run() throws IOException {             
                fo.delete();
                fo.getParent().createData(fo.getName());
            }
        };
        fo.getFileSystem().runAtomicAction(a);        
        
        waitALittleBit(500); // after create 
        
        // test 
        assertTrue(fileA.exists());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
    }    
    
    public void renameVersionedFile_DO() throws Exception {
        // init
        File fromFile = new File(wc, "fromFile");
        fromFile.createNewFile();
        commit(wc);                       
        File toFile = new File(wc, "toFile");
        
        // rename    
        renameDO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());
        
        assertCachedStatus(fromFile, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        
//        commit(wc);
    }

    public void moveVersionedFile_DO() throws Exception {
        // init
        File fromFile = new File(wc, "file");
        fromFile.createNewFile();
        File toFolder = new File(wc, "toFolder");
        toFolder.mkdirs();
        commit(wc);
        File toFile = new File(toFolder, fromFile.getName());

        // move
        moveDO(fromFile, toFile);

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());

        assertCachedStatus(fromFile, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

//        commit(wc);
    }

    public void moveVersionedFile2Repos_DO() throws Exception {
        // init
        File fromFolder = new File(wc, "folder");
        fromFolder.mkdirs();
        File toFolder = new File(wc2, "toFolder");
        toFolder.mkdirs();
        commit(wc2);
        File fromFile = new File(fromFolder, "file");
        fromFile.createNewFile();
        commit(wc);
        File toFile = new File(toFolder, "file");
        // move
        moveDO(fromFile, toFile);

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());

        assertCachedStatus(fromFile, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

//        commit(wc);
//        commit(wc2);
    }

    public void moveVersionedFolder2Repos_DO() throws Exception {
        // init
        File fromFolder = new File(wc, "folder");
        fromFolder.mkdirs();
        File toFolderParent = new File(wc2, "folderParent");
        toFolderParent.mkdirs();
        File toFolder = new File(toFolderParent, fromFolder.getName());
        File toFile = new File(toFolder, "file");
        commit(wc2);
        File fromFile = new File(fromFolder, toFile.getName());
        fromFile.createNewFile();
        commit(wc);
        // move
        moveDO(fromFolder, toFolder);

        // test
        assertFalse(fromFolder.exists());
        assertFalse(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());

        assertCachedStatus(fromFile, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fromFolder));
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

//        commit(wc);
//        commit(wc2);
    }

    public void moveFileTree2Repos_DO() throws Exception {
        // init
        File fromFolder = new File(wc, "folder");
        fromFolder.mkdirs();
        File fromFolder1 = new File(fromFolder, "folder1");
        fromFolder1.mkdirs();
        File fromFolder2 = new File(fromFolder, "folder2");
        fromFolder2.mkdirs();
        File fromFile11 = new File(fromFolder1, "file11");
        fromFile11.createNewFile();
        File fromFile12 = new File(fromFolder1, "file12");
        fromFile12.createNewFile();
        File fromFile21 = new File(fromFolder2, "file21");
        fromFile21.createNewFile();
        File fromFile22 = new File(fromFolder2, "file22");
        fromFile22.createNewFile();

        File toFolderParent = new File(wc2, "toFolder");
        toFolderParent.mkdirs();
        File toFolder = new File(toFolderParent, fromFolder.getName());
        commit(wc);
        commit(wc2);

        // move
        moveDO(fromFolder, toFolder);

//        // test         t.
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        File toFolder1 = new File(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        File toFolder2 = new File(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        File toFile11 = new File(toFolder1, "file11");
        assertTrue(toFile11.exists());
        File toFile12 = new File(toFolder1, "file12");
        assertTrue(toFile12.exists());
        File toFile21 = new File(toFolder2, "file21");
        assertTrue(toFile21.exists());
        File toFile22 = new File(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder1).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder2).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile11).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile12).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile21).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile22).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder1).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder2).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile11).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile12).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile21).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile22).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fromFolder));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fromFolder1));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fromFolder2));
        assertCachedStatus(fromFile11, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(fromFile12, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(fromFile21, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(fromFile22, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder1, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder2, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile11, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFile12, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFile21, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFile22, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        commit(wc);
        commit(wc2);

        assertFalse(fromFolder.exists());
        assertFalse(fromFolder1.exists());
        assertFalse(fromFolder2.exists());
        assertFalse(fromFile11.exists());
        assertFalse(fromFile12.exists());
        assertFalse(fromFile21.exists());
        assertFalse(fromFile22.exists());

    }

    public void moveVersionedFile2Repos_FO() throws Exception {
        // init
        File fromFolder = new File(wc, "folder");
        fromFolder.mkdirs();
        File toFolder = new File(wc2, "toFolder");
        toFolder.mkdirs();
        commit(wc2);
        File fromFile = new File(fromFolder, "file");
        fromFile.createNewFile();
        commit(wc);
        File toFile = new File(toFolder, "file");
        // move
        moveFO(fromFile, toFile);

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());

        assertCachedStatus(fromFile, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

//        commit(wc);
//        commit(wc2);
    }

    public void moveVersionedFolder2Repos_FO() throws Exception {
        // init
        File fromFolder = new File(wc, "folder");
        fromFolder.mkdirs();
        File toFolderParent = new File(wc2, "folderParent");
        toFolderParent.mkdirs();
        File toFolder = new File(toFolderParent, fromFolder.getName());
        File toFile = new File(toFolder, "file");
        commit(wc2);
        File fromFile = new File(fromFolder, toFile.getName());
        fromFile.createNewFile();
        commit(wc);
        // move
        moveFO(fromFolder, toFolder);

        // test
        assertFalse(fromFolder.exists()); // TODO later delete from folder
        assertFalse(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());

        assertCachedStatus(fromFolder, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

//        commit(wc);
//        commit(wc2);
    }

    public void moveFileTree2Repos_FO() throws Exception {
        // init
        File fromFolder = new File(wc, "folder");
        fromFolder.mkdirs();
        File fromFolder1 = new File(fromFolder, "folder1");
        fromFolder1.mkdirs();
        File fromFolder2 = new File(fromFolder, "folder2");
        fromFolder2.mkdirs();
        File fromFile11 = new File(fromFolder1, "file11");
        fromFile11.createNewFile();
        File fromFile12 = new File(fromFolder1, "file12");
        fromFile12.createNewFile();
        File fromFile21 = new File(fromFolder2, "file21");
        fromFile21.createNewFile();
        File fromFile22 = new File(fromFolder2, "file22");
        fromFile22.createNewFile();

        File toFolderParent = new File(wc2, "toFolder");
        toFolderParent.mkdirs();
        File toFolder = new File(toFolderParent, fromFolder.getName());
        commit(wc);
        commit(wc2);

        // move
        moveFO(fromFolder, toFolder);

//        // test         t.
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        File toFolder1 = new File(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        File toFolder2 = new File(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        File toFile11 = new File(toFolder1, "file11");
        assertTrue(toFile11.exists());
        File toFile12 = new File(toFolder1, "file12");
        assertTrue(toFile12.exists());
        File toFile21 = new File(toFolder2, "file21");
        assertTrue(toFile21.exists());
        File toFile22 = new File(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder1).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder2).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile11).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile12).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile21).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile22).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder1).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder2).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile11).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile12).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile21).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile22).getTextStatus());

        assertCachedStatus(fromFolder, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(fromFolder1, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(fromFolder2, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(fromFile11, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(fromFile12, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(fromFile21, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(fromFile22, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder1, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder2, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile11, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFile12, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFile21, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFile22, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        commit(wc);
        commit(wc2);

        assertFalse(fromFolder.exists());
        assertFalse(fromFolder1.exists());
        assertFalse(fromFolder2.exists());
        assertFalse(fromFile11.exists());
        assertFalse(fromFile12.exists());
        assertFalse(fromFile21.exists());
        assertFalse(fromFile22.exists());

    }
    
    public void renameUnversionedFile_DO() throws Exception {
        // init
        File fromFile = new File(wc, "fromFile");
        fromFile.createNewFile();
        File toFile = new File(wc, "toFile");
                
        // rename
        renameDO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));                
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
//        commit(wc);
    }
    
    public void moveUnversionedFile_DO() throws Exception {
        // init
        File fromFile = new File(wc, "file");
        fromFile.createNewFile();
        File toFolder = new File(wc, "toFolder");
        toFolder.mkdirs();
        
        File toFile = new File(toFolder, fromFile.getName());
        
        // rename
        moveDO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));                
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
//        commit(wc);
    }

    public void renameUnversionedFolder_DO() throws Exception {
        // init
        File fromFolder = new File(wc, "fromFolder");
        fromFolder.mkdirs();
        File toFolder = new File(wc, "toFolder");
        
        // rename
        renameDO(fromFolder, toFolder);
        
        // test 
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFolder).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFolder));                
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
//        commit(wc);
    }
    
    public void moveUnversionedFolder_DO() throws Exception {
        // init
        File fromFolder = new File(wc, "folder");
        fromFolder.mkdirs();
        File toParent = new File(wc, "toFolder");
        toParent.mkdirs();
        File toFolder = new File(toParent, fromFolder.getName());
        
        // move        
        moveDO(fromFolder, toFolder);
        
        // test 
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFolder).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFolder));                
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
//        commit(wc);
    }
    
    public void copyVersionedFile_DO() throws Exception {
        // init
        File fromFile = new File(wc, "file");
        fromFile.createNewFile();
        File toFolder = new File(wc, "toFolder");
        toFolder.mkdirs();
        commit(wc);
        File toFile = new File(toFolder, fromFile.getName());

        // copy
        copyDO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());


        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertTrue(getSVNStatus(toFile).isCopied());

//        commit(wc);
    }

    public void copyUnversionedFile_DO() throws Exception {
        // init
        File fromFile = new File(wc, "file");
        fromFile.createNewFile();
        File toFolder = new File(wc, "toFolder");
        toFolder.mkdirs();

        File toFile = new File(toFolder, fromFile.getName());

        // copy
        copyDO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
    }

    public void copyUnversionedFolder_DO() throws Exception {
        // init
        File fromFolder = new File(wc, "folder");
        fromFolder.mkdirs();
        File toParent = new File(wc, "toFolder");
        toParent.mkdirs();
        File toFolder = new File(toParent, fromFolder.getName());

        // copy
        copyDO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());

        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());

        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getStatus(fromFolder));
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        assertFalse(getSVNStatus(toFolder).isCopied());

//        commit(wc);
    }

    public void copyAddedFile2UnversionedFolder_DO() throws Exception {
        // init
        File fromFile = new File(wc, "file");
        fromFile.createNewFile();
        File toFolder = new File(wc, "toFodler");
        toFolder.mkdirs();

        File toFile = new File(toFolder, fromFile.getName());

        // add
        getClient().addFile(fromFile);

        // copy
        copyDO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
    }

    public void copyVersionedFile2UnversionedFolder_DO() throws Exception {
        // init
        File fromFile = new File(wc, "file");
        fromFile.createNewFile();
        File unversionedFolder = new File(dataRootDir, getName() + "_unversioned");
        unversionedFolder.mkdirs();
        
        File toFile = new File(unversionedFolder, fromFile.getName());

        // add
        getClient().addFile(fromFile);
        commit(wc);

        // copy
        copyDO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile));
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, getStatus(toFile));

        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
    }

    public void copyVersionedFile2IgnoredFolder_DO() throws Exception {
        // init
        File fromFile = new File(wc, "file");
        fromFile.createNewFile();
        File toFolder = new File(wc, "toFolder");
        toFolder.mkdirs();
        
        File toFile = new File(toFolder, fromFile.getName());

        // commit
        commit(fromFile);
        //ignore
        getClient().setIgnoredPatterns(wc, Collections.singletonList(toFolder.getName()));

        // copy
        copyDO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile));
        assertEquals(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, getStatus(toFile));
        assertFalse(getSVNStatus(toFile).isCopied());
    }

    public void copyVersionedFolder2UnversionedFolder_DO() throws Exception {
        // init
        File fromFolder = new File(wc, "folder");
        fromFolder.mkdir();
        File fromFile = new File(fromFolder, "file");
        fromFile.createNewFile();

        File unversionedFolder = new File(dataRootDir, getName() + "_unversioned");
        unversionedFolder.mkdirs();
        File toFolder = new File(unversionedFolder, fromFolder.getName());
        File toFile = new File(toFolder, fromFile.getName());

        // add
        getClient().addFile(fromFolder);
        commit(wc);

        // copy
        copyDO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, getStatus(toFolder));

        assertFalse(getSVNStatus(toFolder).isCopied());

//        commit(wc);
    }

    public void copyAddedFile2VersionedFolder_DO() throws Exception {
        // init
        File toFolder = new File(wc, "toFolder");
        toFolder.mkdirs();
        commit(wc);
        File fromFile = new File(wc, "fromFile");
        fromFile.createNewFile();

        File toFile = new File(toFolder, fromFile.getName());

        // add
        getClient().addFile(fromFile);

        // rename
        copyDO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
    }

    public void copyA2B2C_DO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File folderB = new File(wc, "folderB");
        folderB.mkdirs();
        File folderC = new File(wc, "folderC");
        folderC.mkdirs();
        commit(wc);

        File fileB = new File(folderB, fileA.getName());
        File fileC = new File(folderC, fileA.getName());

        // move
        copyDO(fileA, fileB);
        copyDO(fileB, fileC);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileB.exists());
        assertTrue(fileC.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileB));
        assertCachedStatus(fileC, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertTrue(getSVNStatus(fileB).isCopied());
        assertTrue(getSVNStatus(fileC).isCopied());
        
//        commit(wc);
    }

    public void copyVersionedFolder_DO() throws Exception {
        // init
        File fromFolder = new File(wc, "from");
        fromFolder.mkdirs();
        File toParent = new File(wc, "toFolder");
        toParent.mkdirs();
        commit(wc);

        File toFolder = new File(toParent, fromFolder.getName());

        // copy
        copyDO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        // XXX will fail after fixing in fileentry.copy() !!!
        assertFalse(getSVNStatus(toFolder).isCopied());
//        commit(wc);
    }

    public void copyFileTree_DO() throws Exception {
        // init
        File fromFolder = new File(wc, "from");
        fromFolder.mkdirs();
        File fromFolder1 = new File(fromFolder, "folder1");
        fromFolder1.mkdirs();
        File fromFolder2 = new File(fromFolder, "folder2");
        fromFolder2.mkdirs();
        File fromFile11 = new File(fromFolder1, "file11");
        fromFile11.createNewFile();
        File fromFile12 = new File(fromFolder1, "file12");
        fromFile12.createNewFile();
        File fromFile21 = new File(fromFolder2, "file21");
        fromFile21.createNewFile();
        File fromFile22 = new File(fromFolder2, "file22");
        fromFile22.createNewFile();

        File toFolderParent = new File(wc, "to");
        toFolderParent.mkdirs();

        commit(wc);

        File toFolder = new File(toFolderParent, fromFolder.getName());

        // move
        copyDO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        File toFolder1 = new File(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        File toFolder2 = new File(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        File toFile11 = new File(toFolder1, "file11");
        assertTrue(toFile11.exists());
        File toFile12 = new File(toFolder1, "file12");
        assertTrue(toFile12.exists());
        File toFile21 = new File(toFolder2, "file21");
        assertTrue(toFile21.exists());
        File toFile22 = new File(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder1).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder2).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile11).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile12).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile21).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile22).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder1).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder2).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile11).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile12).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile21).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile22).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder1));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder2));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile11));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile12));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile21));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile22));
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder1, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder2, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile11, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile12, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile21, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile22, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        // XXX should be fixed first in fileentry.copy
        //     afterwards assertTrue(...)
        assertFalse(getSVNStatus(toFolder).isCopied());
        assertFalse(getSVNStatus(toFolder1).isCopied());
        assertFalse(getSVNStatus(toFolder2).isCopied());

        assertTrue(getSVNStatus(toFile11).isCopied());
        assertTrue(getSVNStatus(toFile12).isCopied());
        assertTrue(getSVNStatus(toFile21).isCopied());
        assertTrue(getSVNStatus(toFile22).isCopied());

//        commit(wc);
    }

    public void copyVersionedFile2Repos_DO() throws Exception {
        // init
        File fromFolder = new File(wc, "folder");
        fromFolder.mkdirs();
        File toFolder = new File(wc2, "toFolder");
        toFolder.mkdirs();
        commit(wc2);
        File fromFile = new File(fromFolder, "file");
        fromFile.createNewFile();
        commit(wc);
        File toFile = new File(toFolder, "file");

        // copy
        copyDO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
//        commit(wc2);
    }

    public void copyVersionedFolder2Repos_DO() throws Exception {
        // init
        File fromFolder = new File(wc, "folder");
        fromFolder.mkdirs();
        File toFolderParent = new File(wc2, "folderParent");
        toFolderParent.mkdirs();
        File toFolder = new File(toFolderParent, fromFolder.getName());
        File toFile = new File(toFolder, "file");
        commit(wc2);
        File fromFile = new File(fromFolder, toFile.getName());
        fromFile.createNewFile();
        commit(wc);

        // copy
        copyDO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists()); // TODO later delete from folder
        assertTrue(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertFalse(getSVNStatus(toFolder).isCopied());
        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
//        commit(wc2);
    }

    public void copyFileTree2Repos_DO() throws Exception {
        // init
        File fromFolder = new File(wc, "folder");
        fromFolder.mkdirs();
        File fromFolder1 = new File(fromFolder, "folder1");
        fromFolder1.mkdirs();
        File fromFolder2 = new File(fromFolder, "folder2");
        fromFolder2.mkdirs();
        File fromFile11 = new File(fromFolder1, "file11");
        fromFile11.createNewFile();
        File fromFile12 = new File(fromFolder1, "file12");
        fromFile12.createNewFile();
        File fromFile21 = new File(fromFolder2, "file21");
        fromFile21.createNewFile();
        File fromFile22 = new File(fromFolder2, "file22");
        fromFile22.createNewFile();

        File toFolderParent = new File(wc2, "toFolder");
        toFolderParent.mkdirs();
        File toFolder = new File(toFolderParent, fromFolder.getName());
        commit(wc);
        commit(wc2);

        // copy
        copyDO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        File toFolder1 = new File(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        File toFolder2 = new File(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        File toFile11 = new File(toFolder1, "file11");
        assertTrue(toFile11.exists());
        File toFile12 = new File(toFolder1, "file12");
        assertTrue(toFile12.exists());
        File toFile21 = new File(toFolder2, "file21");
        assertTrue(toFile21.exists());
        File toFile22 = new File(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder1).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder2).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile11).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile12).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile21).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile22).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder1).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder2).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile11).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile12).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile21).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile22).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder1));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder2));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile11));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile12));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile21));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile22));

        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder1, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder2, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile11, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFile12, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFile21, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFile22, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        commit(wc);
        commit(wc2);

        assertTrue(fromFolder.exists());
        assertTrue(fromFolder1.exists());
        assertTrue(fromFolder2.exists());
        assertTrue(fromFile11.exists());
        assertTrue(fromFile12.exists());
        assertTrue(fromFile21.exists());
        assertTrue(fromFile22.exists());

        assertFalse(getSVNStatus(fromFolder).isCopied());
        assertFalse(getSVNStatus(fromFolder1).isCopied());
        assertFalse(getSVNStatus(fromFolder2).isCopied());
        assertFalse(getSVNStatus(fromFile11).isCopied());
        assertFalse(getSVNStatus(fromFile12).isCopied());
        assertFalse(getSVNStatus(fromFile21).isCopied());
        assertFalse(getSVNStatus(fromFile22).isCopied());

    }

    public void copyVersionedFile_FO() throws Exception {
        // init
        File fromFile = new File(wc, "file");
        fromFile.createNewFile();
        File toFolder = new File(wc, "toFolder");
        toFolder.mkdirs();
        commit(wc);
        File toFile = new File(toFolder, fromFile.getName());

        // copy
        copyFO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertTrue(getSVNStatus(toFile).isCopied());

//        commit(wc);
    }

    public void copyUnversionedFile_FO() throws Exception {
        // init
        File fromFile = new File(wc, "file");
        fromFile.createNewFile();
        File toFolder = new File(wc, "toFolder");
        toFolder.mkdirs();

        File toFile = new File(toFolder, fromFile.getName());

        // copy
        copyFO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getStatus(fromFile));

        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
    }

    public void copyUnversionedFolder_FO() throws Exception {
        // init
        File fromFolder = new File(wc, "folder");
        fromFolder.mkdirs();
        File toParent = new File(wc, "toFolder");
        toParent.mkdirs();
        File toFolder = new File(toParent, fromFolder.getName());

        // copy
        copyFO(fromFolder, toFolder);

        // test 
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());

        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getStatus(fromFolder));

        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        assertFalse(getSVNStatus(toFolder).isCopied());

//        commit(wc);
    }

    public void copyAddedFile2UnversionedFolder_FO() throws Exception {
        // init
        File fromFile = new File(wc, "file");
        fromFile.createNewFile();
        File toFolder = new File(wc, "toFolder");
        toFolder.mkdirs();

        File toFile = new File(toFolder, fromFile.getName());

        // add
        getClient().addFile(fromFile);
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fromFile).getTextStatus());

        // copy
        copyFO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertFalse(getSVNStatus(toFolder).isCopied());

//        commit(wc);
    }

    public void copyAddedFile2VersionedFolder_FO() throws Exception {
        // init
        File toFolder = new File(wc, "toFodler");
        toFolder.mkdirs();
        commit(wc);
        File fromFile = new File(wc, "fromFile");
        fromFile.createNewFile();

        File toFile = new File(toFolder, fromFile.getName());

        // add
        getClient().addFile(fromFile);

        // copy
        copyFO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertFalse(getSVNStatus(toFolder).isCopied());

//        commit(wc);
    }

    public void copyA2B2C_FO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File folderB = new File(wc, "folderB");
        folderB.mkdirs();
        File folderC = new File(wc, "folderC");
        folderC.mkdirs();
        commit(wc);

        File fileB = new File(folderB, fileA.getName());
        File fileC = new File(folderC, fileA.getName());

        // copy
        copyFO(fileA, fileB);
        copyFO(fileB, fileC);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileB.exists());
        assertTrue(fileC.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
        assertCachedStatus(fileB, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(fileC, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertTrue(getSVNStatus(fileB).isCopied());
        assertTrue(getSVNStatus(fileC).isCopied());

//        commit(wc);
    }

    public void copyVersionedFile2UnversionedFolder_FO() throws Exception {
        // init
        File fromFile = new File(wc, "file");
        fromFile.createNewFile();
        File toFolder = new File(dataRootDir, getName() + "toFolder");
        toFolder.mkdirs();

        File toFile = new File(toFolder, fromFile.getName());

        // add
        getClient().addFile(fromFile);
        commit(wc);

        // copy
        copyFO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NOTMANAGED);

        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
    }

    public void copyVersionedFile2IgnoredFolder_FO() throws Exception {
        // init
        File fromFile = new File(wc, "file");
        fromFile.createNewFile();
        File toFolder = new File(wc, "toFolder");
        toFolder.mkdirs();
        
        File toFile = new File(toFolder, fromFile.getName());

        // commit
        commit(fromFile);
        //ignore
        getClient().setIgnoredPatterns(wc, Collections.singletonList(toFolder.getName()));

        // copy
        copyFO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile));
        assertEquals(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, getStatus(toFile));
        assertFalse(getSVNStatus(toFile).isCopied());
    }

    public void copyVersionedFolder2UnversionedFolder_FO() throws Exception {
        // init
        File fromFolder = new File(wc, "folder");
        fromFolder.mkdir();
        File fromFile = new File(fromFolder, "file");
        fromFile.createNewFile();
        File toFolder = new File(dataRootDir, getName() + "toFolder");

        File toFile = new File(toFolder, fromFile.getName());

        // add
        getClient().addFile(fromFolder);
        commit(wc);

        // copy
        copyFO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NOTMANAGED);

        assertFalse(getSVNStatus(toFolder).isCopied());

//        commit(wc);
    }

    public void copyVersionedFolder_FO() throws Exception {
        // init
        File fromFolder = new File(wc, "from");
        fromFolder.mkdirs();
        File toParent = new File(wc, "toFolder");
        toParent.mkdirs();
        commit(wc);

        File toFolder = new File(toParent, fromFolder.getName());

        // copy
        copyFO(fromFolder, toFolder);
        
        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertTrue(getSVNStatus(toFolder).isCopied());
        
//        commit(wc);
        
    }

    public void copyFileTree_FO() throws Exception {
        // init
        File fromFolder = new File(wc, "from");
        fromFolder.mkdirs();
        File fromFolder1 = new File(fromFolder, "folder1");
        fromFolder1.mkdirs();
        File fromFolder2 = new File(fromFolder, "folder2");
        fromFolder2.mkdirs();
        File fromFile11 = new File(fromFolder1, "file11");
        fromFile11.createNewFile();
        File fromFile12 = new File(fromFolder1, "file12");
        fromFile12.createNewFile();
        File fromFile21 = new File(fromFolder2, "file21");
        fromFile21.createNewFile();
        File fromFile22 = new File(fromFolder2, "file22");
        fromFile22.createNewFile();

        File toFolderParent = new File(wc, "to");
        toFolderParent.mkdirs();

        commit(wc);

        File toFolder = new File(toFolderParent, fromFolder.getName());

        // copy
        copyFO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        File toFolder1 = new File(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        File toFolder2 = new File(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        File toFile11 = new File(toFolder1, "file11");
        assertTrue(toFile11.exists());
        File toFile12 = new File(toFolder1, "file12");
        assertTrue(toFile12.exists());
        File toFile21 = new File(toFolder2, "file21");
        assertTrue(toFile21.exists());
        File toFile22 = new File(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder1).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder2).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile11).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile12).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile21).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile22).getTextStatus());

        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFolder1).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFolder2).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile11).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile12).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile21).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile22).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder1));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder2));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile11));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile12));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile21));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile22));

        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder1, FileInformation.STATUS_VERSIONED_UPTODATE);
        assertCachedStatus(toFolder2, FileInformation.STATUS_VERSIONED_UPTODATE);
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile11));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile12));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile21));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile22));

        assertTrue(getSVNStatus(toFolder).isCopied());
        assertTrue(getSVNStatus(toFolder1).isCopied());
        assertTrue(getSVNStatus(toFolder2).isCopied());
        assertTrue(getSVNStatus(toFile11).isCopied());
        assertTrue(getSVNStatus(toFile12).isCopied());
        assertTrue(getSVNStatus(toFile21).isCopied());
        assertTrue(getSVNStatus(toFile22).isCopied());

//        commit(wc);
        
    }

    public void copyVersionedFile2Repos_FO() throws Exception {
        // init
        File fromFolder = new File(wc, "folder");
        fromFolder.mkdirs();
        File toFolder = new File(wc2, "toFolder");
        toFolder.mkdirs();
        commit(wc2);
        File fromFile = new File(fromFolder, "file");
        fromFile.createNewFile();
        commit(wc);
        File toFile = new File(toFolder, "file");

        // copy
        copyFO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
//        commit(wc2);
    }

    public void copyVersionedFolder2Repos_FO() throws Exception {
        // init
        File fromFolder = new File(wc, "folder");
        fromFolder.mkdirs();
        File toFolderParent = new File(wc2, "folderParent");
        toFolderParent.mkdirs();
        File toFolder = new File(toFolderParent, fromFolder.getName());
        File toFile = new File(toFolder, "file");
        commit(wc2);

        File fromFile = new File(fromFolder, toFile.getName());
        fromFile.createNewFile();
        commit(wc);

        // copy
        copyFO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists());
        assertTrue(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertFalse(getSVNStatus(toFolder).isCopied());
        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
//        commit(wc2);
    }

    public void copyFileTree2Repos_FO() throws Exception {
        // init
        File fromFolder = new File(wc, "folder");
        fromFolder.mkdirs();
        File fromFolder1 = new File(fromFolder, "folder1");
        fromFolder1.mkdirs();
        File fromFolder2 = new File(fromFolder, "folder2");
        fromFolder2.mkdirs();
        File fromFile11 = new File(fromFolder1, "file11");
        fromFile11.createNewFile();
        File fromFile12 = new File(fromFolder1, "file12");
        fromFile12.createNewFile();
        File fromFile21 = new File(fromFolder2, "file21");
        fromFile21.createNewFile();
        File fromFile22 = new File(fromFolder2, "file22");
        fromFile22.createNewFile();

        File toFolderParent = new File(wc2, "toFolder");
        toFolderParent.mkdirs();
        File toFolder = new File(toFolderParent, fromFolder.getName());
        commit(wc);
        commit(wc2);

        // copy
        copyFO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        File toFolder1 = new File(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        File toFolder2 = new File(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        File toFile11 = new File(toFolder1, "file11");
        assertTrue(toFile11.exists());
        File toFile12 = new File(toFolder1, "file12");
        assertTrue(toFile12.exists());
        File toFile21 = new File(toFolder2, "file21");
        assertTrue(toFile21.exists());
        File toFile22 = new File(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder1).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder2).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile11).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile12).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile21).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile22).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder1).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder2).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile11).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile12).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile21).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile22).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder1));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder2));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile11));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile12));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile21));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile22));
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder1, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder2, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile11, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFile12, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFile21, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFile22, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        assertFalse(getSVNStatus(fromFolder).isCopied());
        assertFalse(getSVNStatus(fromFolder1).isCopied());
        assertFalse(getSVNStatus(fromFolder2).isCopied());
        assertFalse(getSVNStatus(fromFile11).isCopied());
        assertFalse(getSVNStatus(fromFile12).isCopied());
        assertFalse(getSVNStatus(fromFile21).isCopied());
        assertFalse(getSVNStatus(fromFile22).isCopied());

//        commit(wc);
//        commit(wc2);

    }

    public void copyA2CB2A_FO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File folderB = new File(wc, "folderB");
        folderB.mkdirs();
        File fileB = new File(folderB, fileA.getName());
        fileB.createNewFile();
        File folderC = new File(wc, "folderC");
        folderC.mkdirs();
        commit(wc);

        File fileC = new File(folderC, fileA.getName());

        // copy
        copyFO(fileA, fileC);
        Thread.sleep(500);
        copyFO(fileB, fileA);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertTrue(fileB.exists());

        assertEquals(SVNStatusKind.REPLACED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileB));
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileC));

        commit(wc);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertTrue(fileB.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileB));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileC));

        assertTrue(getSVNStatus(fileB).isCopied());
        assertTrue(getSVNStatus(fileA).isCopied());
    }

    public void renameAddedFile_DO() throws Exception {
        // init        
        File fromFile = new File(wc, "fromFile");
        fromFile.createNewFile();
        File toFile = new File(wc, "toFile");
        
        // add
        getClient().addFile(fromFile);                
        
        // rename
        renameDO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        

        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
//        commit(wc);
    }
    
    public void moveAddedFile2UnversionedFolder_DO() throws Exception {
        // init        
        File fromFile = new File(wc, "file");
        fromFile.createNewFile();
        File toFolder = new File(wc, "toFodler");
        toFolder.mkdirs();
        
        File toFile = new File(toFolder, fromFile.getName());
        
        // add
        getClient().addFile(fromFile);                
        
        // move
        moveDO(fromFile, toFile);
                
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));                
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
//        commit(wc);
    }
    
    public void moveVersionedFile2IgnoredFolder_DO() throws Exception {
        // init
        File fromFile = new File(wc, "file");
        fromFile.createNewFile();
        File toFolder = new File(wc, "toFolder");
        toFolder.mkdirs();
        
        File toFile = new File(toFolder, fromFile.getName());

        // add
        commit(fromFile);
        getClient().setIgnoredPatterns(wc, Collections.singletonList(toFolder.getName()));

        // move
        moveDO(fromFile, toFile);

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_EXCLUDED);
    }
    
    public void moveAddedFile2VersionedFolder_DO() throws Exception {
        // init        
        File toFolder = new File(wc, "toFodler");
        toFolder.mkdirs();
        commit(wc);
        File fromFile = new File(wc, "fromFile");
        fromFile.createNewFile();        
        
        File toFile = new File(toFolder, fromFile.getName());  
        
        // add
        getClient().addFile(fromFile);                
        
        // rename
        moveDO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));                
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
//        commit(wc);
    }

    public void renameA2B2A_DO() throws Exception {
        // init
        File fileA = new File(wc, "from");
        fileA.createNewFile();
        commit(wc);  
        
        File fileB = new File(wc, "to");
        
        // rename
        renameDO(fileA, fileB);
        renameDO(fileB, fileA);
        
        // test 
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        
//        commit(wc);
    }
    
    public void moveA2B2A_DO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File folder = new File(wc, "folder");
        folder.mkdirs();        
        commit(wc);  
        
        File fileB = new File(folder, fileA.getName());
        
        // move
        moveDO(fileA, fileB);
        Thread.sleep(500);
        moveDO(fileB, fileA);
        
        // test 
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        
//        commit(wc);
    }
    
    public void renameA2B2C_DO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();        
        commit(wc);  
        
        File fileB = new File(wc, "B");
        File fileC = new File(wc, "C");

        // rename
        renameDO(fileA, fileB);
        renameDO(fileB, fileC);
        
        // test 
        assertFalse(fileA.exists());
        assertFalse(fileB.exists());
        assertTrue(fileC.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());
        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        assertCachedStatus(fileC, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        
//        commit(wc);
    }
    
    public void moveA2B2C_DO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File folderB = new File(wc, "folderB");
        folderB.mkdirs();
        File folderC = new File(wc, "folderC");
        folderC.mkdirs();
        commit(wc);  
        
        File fileB = new File(folderB, fileA.getName());
        File fileC = new File(folderC, fileA.getName());
        
        // move
        moveDO(fileA, fileB);
        moveDO(fileB, fileC);
        
        // test 
        assertFalse(fileA.exists());
        assertFalse(fileB.exists());
        assertTrue(fileC.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());
        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        assertCachedStatus(fileC, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        
//        commit(wc);
    }

    public void renameA2B2C2A_DO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        commit(wc);  
        
        File fileB = new File(wc, "B");
        File fileC = new File(wc, "C");
        
        // rename 
        renameDO(fileA, fileB);
        renameDO(fileB, fileC);
        renameDO(fileC, fileA);
        
        // test 
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertFalse(fileC.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileC).getTextStatus());  
        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileC));                
        
//        commit(wc);
        
    }

    public void moveA2CB2A_DO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File folderB = new File(wc, "folderB");
        folderB.mkdirs();
        File fileB = new File(folderB, fileA.getName());
        fileB.createNewFile();
        File folderC = new File(wc, "folderC");
        folderC.mkdirs();
        commit(wc);

        File fileC = new File(folderC, fileA.getName());

        // move
        moveDO(fileA, fileC);
        Thread.sleep(500);
        moveDO(fileB, fileA);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(SVNStatusKind.REPLACED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileB));
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileC));

        commit(wc);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileC));
    }

    public void moveA2CB2A_FO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File folderB = new File(wc, "folderB");
        folderB.mkdirs();
        File fileB = new File(folderB, fileA.getName());
        fileB.createNewFile();
        File folderC = new File(wc, "folderC");
        folderC.mkdirs();
        commit(wc);

        File fileC = new File(folderC, fileA.getName());

        // move
        moveFO(fileA, fileC);
        Thread.sleep(500);
        moveFO(fileB, fileA);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(SVNStatusKind.REPLACED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileB));
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileC));

        commit(wc);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileC));
    }

    public void renameA2CB2A_DO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File fileB = new File(wc, "B");
        fileB.createNewFile();
        commit(wc);

        File fileC = new File(wc, "C");

        // move
        renameDO(fileA, fileC);
        Thread.sleep(500);
        renameDO(fileB, fileA);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(SVNStatusKind.REPLACED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileB));
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileC));

        commit(wc);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileC));
    }

    public void renameA2CB2A_FO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File fileB = new File(wc, "B");
        fileB.createNewFile();
        commit(wc);

        File fileC = new File(wc, "C");

        // move
        renameFO(fileA, fileC);
        Thread.sleep(500);
        renameFO(fileB, fileA);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(SVNStatusKind.REPLACED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileB));
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileC));

        commit(wc);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileC));
    }
    
    public void renameA2a_FO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        commit(wc);

        File fileB = new File(wc, "a");

        // move
        renameFO(fileA, fileB);

        // test
        // test
        if (!Utilities.isMac() && !Utilities.isWindows()) {
            assertFalse(fileA.exists());
        }
        assertTrue(fileB.exists());

        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileB).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileB));
    }
    
    public void moveA2B2C2A_DO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File folderB = new File(wc, "folderB");
        folderB.mkdirs();
        File folderC = new File(wc, "folderC");
        folderC.mkdirs();
        commit(wc);  
        
        File fileB = new File(folderB, fileA.getName());
        File fileC = new File(folderC, fileA.getName());
        
        // move
        moveDO(fileA, fileB);
        moveDO(fileB, fileC);
        moveDO(fileC, fileA);
        
        // test 
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertFalse(fileC.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileC).getTextStatus());  
        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileC));                
        
        commit(wc);
        
    }        
    
    public void renameA2B_CreateA_DO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        commit(wc);  
        
        // rename
        File fileB = new File(wc, "B");
        renameDO(fileA, fileB);
        
        // create from file
        FileUtil.toFileObject(fileA.getParentFile()).createData(fileA.getName());
        
        // test 
        assertTrue(fileB.exists());
        assertTrue(fileA.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileB).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertCachedStatus(fileB, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        
//        commit(wc);
    }
    
    public void moveA2B_CreateA_DO() throws Exception {
        // init
        File fileA = new File(wc, "file");
        fileA.createNewFile();
        File folderB = new File(wc, "folderB");
        folderB.mkdirs();
        commit(wc);  
        
        File fileB = new File(folderB, fileA.getName());
        
        // move
        moveDO(fileA, fileB);
        Thread.sleep(500);
        
        // create from file
        FileUtil.toFileObject(fileA.getParentFile()).createData(fileA.getName());
        
        // test 
        assertTrue(fileB.exists());
        assertTrue(fileA.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileB).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertCachedStatus(fileB, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        
//        commit(wc);
    }
    
    // fixed - see issue #129805
    public void deleteA_RenameB2A_DO_129805() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File fileB = new File(wc, "B");
        fileB.createNewFile();
        commit(wc);  
        
        // delete A
        delete(fileA);
        // rename B to A
        renameDO(fileB, fileA);
        
        // test 
        assertFalse(fileB.exists());
        assertTrue(fileA.exists());
        
        assertEquals(SVNStatusKind.REPLACED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileB).getTextStatus());
             
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileB));
        
        commit(wc);

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));
    }
    
    public void renameVersionedFolder_DO() throws Exception {
        // init
        File fromFolder = new File(wc, "from");
        fromFolder.mkdirs();
        commit(wc);               
       
        File toFolder = new File(wc, "to");
        
        // rename       
        renameDO(fromFolder, toFolder);
        
        // test 
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());        
        assertCachedStatus(fromFolder, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        commit(wc);
        assertFalse(fromFolder.exists());        
    }

    public void moveVersionedFolder_DO() throws Exception {
        // init
        File fromFolder = new File(wc, "from");
        fromFolder.mkdirs();
        File toParent = new File(wc, "toFolder");
        toParent.mkdirs();
        commit(wc);               
        
        File toFolder = new File(toParent, fromFolder.getName());
        
        // move
        moveDO(fromFolder, toFolder);
        
        // test 
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fromFolder));
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        commit(wc);
        assertFalse(fromFolder.exists());
    }    
    
    public void renameFileTree_DO() throws Exception {
        // init
        File fromFolder = new File(wc, "from");
        fromFolder.mkdirs();
        File fromFolder1 = new File(fromFolder, "folder1");
        fromFolder1.mkdirs();
        File fromFolder2 = new File(fromFolder, "folder2");
        fromFolder2.mkdirs();
        File fromFile11 = new File(fromFolder1, "file11");
        fromFile11.createNewFile();
        File fromFile12 = new File(fromFolder1, "file12");
        fromFile12.createNewFile();
        File fromFile21 = new File(fromFolder2, "file21");
        fromFile21.createNewFile();
        File fromFile22 = new File(fromFolder2, "file22");
        fromFile22.createNewFile();
        commit(wc);               
        
        // rename
        File toFolder = new File(wc, "to");
        renameDO(fromFolder, toFolder);
                                        
        // test 
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        File toFolder1 = new File(toFolder, "folder1");
        assertTrue(toFolder1.exists());
        File toFolder2 = new File(toFolder, "folder2");
        assertTrue(toFolder2.exists());
        File toFile11 = new File(toFolder1, "file11");
        assertTrue(toFile11.exists());
        File toFile12 = new File(toFolder1, "file12");
        assertTrue(toFile12.exists());
        File toFile21 = new File(toFolder2, "file21");
        assertTrue(toFile21.exists());
        File toFile22 = new File(toFolder2, "file22");
        assertTrue(toFile22.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder1).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder2).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile11).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile12).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile21).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile22).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFolder1).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFolder2).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile11).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile12).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile21).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile22).getTextStatus());    
        
        assertCachedStatus(fromFolder, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFolder1, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFolder2, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile11, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile12, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile21, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile22, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        assertCachedStatus(toFolder1, FileInformation.STATUS_VERSIONED_UPTODATE);                
        assertCachedStatus(toFolder2, FileInformation.STATUS_VERSIONED_UPTODATE);                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile11));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile12));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile21));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile22));   
        
        commit(wc);
        assertFalse(fromFolder.exists());
    }
    
    public void moveFileTree_DO() throws Exception {
        // init
        File fromFolder = new File(wc, "from");
        fromFolder.mkdirs();
        File fromFolder1 = new File(fromFolder, "folder1");
        fromFolder1.mkdirs();
        File fromFolder2 = new File(fromFolder, "folder2");
        fromFolder2.mkdirs();
        File fromFile11 = new File(fromFolder1, "file11");
        fromFile11.createNewFile();
        File fromFile12 = new File(fromFolder1, "file12");
        fromFile12.createNewFile();
        File fromFile21 = new File(fromFolder2, "file21");
        fromFile21.createNewFile();
        File fromFile22 = new File(fromFolder2, "file22");
        fromFile22.createNewFile();

        File toFolderParent = new File(wc, "to");
        toFolderParent.mkdirs();

        commit(wc);

        File toFolder = new File(toFolderParent, fromFolder.getName());

        // move
        moveDO(fromFolder, toFolder);

        // test         t.
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        File toFolder1 = new File(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        File toFolder2 = new File(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        File toFile11 = new File(toFolder1, "file11");
        assertTrue(toFile11.exists());
        File toFile12 = new File(toFolder1, "file12");
        assertTrue(toFile12.exists());
        File toFile21 = new File(toFolder2, "file21");
        assertTrue(toFile21.exists());
        File toFile22 = new File(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder1).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder2).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile11).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile12).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile21).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile22).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder1).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder2).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile11).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile12).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile21).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile22).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fromFolder));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fromFolder1));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fromFolder2));
        assertCachedStatus(fromFile11, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(fromFile12, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(fromFile21, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(fromFile22, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder1, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder2, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile11, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile12, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile21, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile22, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        commit(wc);

        assertFalse(fromFolder.exists());
        assertFalse(fromFolder1.exists());
        assertFalse(fromFolder2.exists());
        assertFalse(fromFile11.exists());
        assertFalse(fromFile12.exists());
        assertFalse(fromFile21.exists());
        assertFalse(fromFile22.exists());

    }

    public void renameVersionedFile_FO() throws Exception {
        // init
        File fromFile = new File(wc, "fromFile");
        fromFile.createNewFile();
        commit(wc);                       
        File toFile = new File(wc, "toFile");
        
        // rename    
        renameFO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());        
        
        assertCachedStatus(fromFile, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);   
        
//        commit(wc);
    }

    public void moveVersionedFile_FO() throws Exception {
        // init
        File fromFile = new File(wc, "file");
        fromFile.createNewFile();
        File toFolder = new File(wc, "toFolder");
        toFolder.mkdirs();
        commit(wc);               
        File toFile = new File(toFolder, fromFile.getName());
        
        // move
        moveFO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());        
        
        assertCachedStatus(fromFile, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);   
        
//        commit(wc);
    }
    
    public void renameUnversionedFile_FO() throws Exception {
        // init
        File fromFile = new File(wc, "fromFile");
        fromFile.createNewFile();
        File toFile = new File(wc, "toFile");
                
        // rename
        renameFO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));                
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
//        commit(wc);
    }
    
    public void moveUnversionedFile_FO() throws Exception {
        // init
        File fromFile = new File(wc, "file");
        fromFile.createNewFile();
        File toFolder = new File(wc, "toFolder");
        toFolder.mkdirs();
        
        File toFile = new File(toFolder, fromFile.getName());
        
        // rename
        moveFO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));                
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
//        commit(wc);
    }

    public void renameUnversionedFolder_FO() throws Exception {
        // init
        File fromFolder = new File(wc, "fromFolder");
        fromFolder.mkdirs();
        File toFolder = new File(wc, "toFolder");
        
        // rename
        renameFO(fromFolder, toFolder);
        
        // test 
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFolder).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFolder));                
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
//        commit(wc);
    }
    
    public void moveUnversionedFolder_FO() throws Exception {
        // init
        File fromFolder = new File(wc, "folder");
        fromFolder.mkdirs();
        File toParent = new File(wc, "toFolder");
        toParent.mkdirs();
        File toFolder = new File(toParent, fromFolder.getName());
        
        
        // move        
        moveFO(fromFolder, toFolder);
        
        // test 
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());

        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());

        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFolder));
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

//        commit(wc);
    }
    
    public void renameAddedFile_FO() throws Exception {
        // init        
        File fromFile = new File(wc, "fromFile");
        fromFile.createNewFile();
        File toFile = new File(wc, "toFile");
        
        // add
        getClient().addFile(fromFile);                
        
        // rename
        renameFO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));                
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
//        commit(wc);
    }
    
    public void moveAddedFile2UnversionedFolder_FO() throws Exception {
        // init        
        File fromFile = new File(wc, "file");
        fromFile.createNewFile();
        File toFolder = new File(wc, "toFodler");
        toFolder.mkdirs();
        
        File toFile = new File(toFolder, fromFile.getName());
        
        // add
        getClient().addFile(fromFile);                
        
        // move
        moveFO(fromFile, toFile);
                
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));                
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
//        commit(wc);
    }
    
    public void moveVersionedFile2IgnoredFolder_FO() throws Exception {
        // init
        File fromFile = new File(wc, "file");
        fromFile.createNewFile();
        File toFolder = new File(wc, "toFolder");
        toFolder.mkdirs();
        
        File toFile = new File(toFolder, fromFile.getName());

        // add
        commit(fromFile);
        getClient().setIgnoredPatterns(wc, Collections.singletonList(toFolder.getName()));

        // move
        moveFO(fromFile, toFile);

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_EXCLUDED);
    }
       
    public void moveAddedFile2VersionedFolder_FO() throws Exception {
        // init        
        File toFolder = new File(wc, "toFodler");
        toFolder.mkdirs();
        commit(wc);
        File fromFile = new File(wc, "fromFile");
        fromFile.createNewFile();        
        
        File toFile = new File(toFolder, fromFile.getName());  
        
        // add
        getClient().addFile(fromFile);                
        
        // rename
        moveFO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));                
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
//        commit(wc);
    }

    public void renameA2B2A_FO() throws Exception {
        // init
        File fileA = new File(wc, "from");
        fileA.createNewFile();
        commit(wc);  
        
        File fileB = new File(wc, "to");
        
        // rename
        renameFO(fileA, fileB);
        renameFO(fileB, fileA);
        
        // test 
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        
//        commit(wc);
    }
    
    public void moveA2B2A_FO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        assertFalse(fileA.exists());
        fileA.createNewFile();
        File folder = new File(wc, "folder");
        assertFalse(folder.exists());
        folder.mkdirs();        
        commit(wc);  
        
        File fileB = new File(folder, fileA.getName());
        assertFalse(fileB.exists());
        
        // move
        moveFO(fileA, fileB);
        Thread.sleep(500);
        moveFO(fileB, fileA);
        
        // test 
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        
//        commit(wc);
    }

    public void renameA2B2C_FO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();        
        commit(wc);  
        
        File fileB = new File(wc, "B");
        File fileC = new File(wc, "C");

        // rename
        renameFO(fileA, fileB);
        renameFO(fileB, fileC);
        
        // test 
        assertFalse(fileA.exists());
        assertFalse(fileB.exists());
        assertTrue(fileC.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());
        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        assertCachedStatus(fileC, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        
//        commit(wc);
    }
    
    public void moveA2B2C_FO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File folderB = new File(wc, "folderB");
        folderB.mkdirs();
        File folderC = new File(wc, "folderC");
        folderC.mkdirs();
        commit(wc);  
        
        File fileB = new File(folderB, fileA.getName());
        File fileC = new File(folderC, fileA.getName());
        
        // move
        moveFO(fileA, fileB);
        moveFO(fileB, fileC);
        
        // test 
        assertFalse(fileA.exists());
        assertFalse(fileB.exists());
        assertTrue(fileC.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());
        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        assertCachedStatus(fileC, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        
//        commit(wc);
    }

    public void renameA2B2C2A_FO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        commit(wc);  
        
        File fileB = new File(wc, "B");
        File fileC = new File(wc, "C");
        
        // rename 
        renameFO(fileA, fileB);
        renameFO(fileB, fileC);
        renameFO(fileC, fileA);
        
        // test 
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertFalse(fileC.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileC).getTextStatus());  
        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileC));                
        
//        commit(wc);
        
    }        
    
    public void moveA2B2C2A_FO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File folderB = new File(wc, "folderB");
        folderB.mkdirs();
        File folderC = new File(wc, "folderC");
        folderC.mkdirs();
        commit(wc);  
        
        File fileB = new File(folderB, fileA.getName());
        File fileC = new File(folderC, fileA.getName());
        
        // move
        moveFO(fileA, fileB);
        moveFO(fileB, fileC);
        moveFO(fileC, fileA);
        
        // test 
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertFalse(fileC.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileC).getTextStatus());  
        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileC));                
        
//        commit(wc);
        
    }        
    
    public void renameA2B_CreateA_FO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        commit(wc);  
        
        // rename
        File fileB = new File(wc, "B");
        renameFO(fileA, fileB);
        
        // create from file
        FileUtil.toFileObject(fileA.getParentFile()).createData(fileA.getName());
        
        // test 
        assertTrue(fileB.exists());
        assertTrue(fileA.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileB).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertCachedStatus(fileB, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        
//        commit(wc);
    }
    
    public void moveA2B_CreateA_FO() throws Exception {
        // init
        File fileA = new File(wc, "file");
        fileA.createNewFile();
        File folderB = new File(wc, "folderB");
        folderB.mkdirs();
        commit(wc);  
        
        File fileB = new File(folderB, fileA.getName());
        
        // move
        moveFO(fileA, fileB);
        Thread.sleep(500);
        
        // create from file
        FileUtil.toFileObject(fileA.getParentFile()).createData(fileA.getName());
        
        // test 
        assertTrue(fileB.exists());
        assertTrue(fileA.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileB).getTextStatus());
        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertCachedStatus(fileB, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        
//        commit(wc);
    }
    
    // fixed - see issue #129805
    public void deleteA_RenameB2A_FO_129805() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File fileB = new File(wc, "B");
        fileB.createNewFile();
        commit(wc);  
        
        // delete A
        delete(fileA);
        // rename B to A
        renameFO(fileB, fileA);
        
        // test 
        assertFalse(fileB.exists());
        assertTrue(fileA.exists());
        
        assertEquals(SVNStatusKind.REPLACED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileB).getTextStatus());
             
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileB));
        
        commit(wc);

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));
    }
    
    public void testIsModifiedAttributeFile () throws Exception {
        // file is outside of versioned space, attribute should be unknown
        File file = File.createTempFile("testIsModifiedAttributeFile", "txt");
        file.deleteOnExit();
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        String attributeModified = "ProvidedExtensions.VCSIsModified";
        
        Object attrValue = fo.getAttribute(attributeModified);
        assertNull(attrValue);
        
        // file inside a svn repo
        file = new File(wc, "file");
        TestKit.write(file, "init");
        fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        // new file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        
        getClient().addFile(file);
        // added file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        commit(file);
        
        // unmodified file, returns FALSE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.FALSE, attrValue);
        
        TestKit.write(file, "modification");
        // modified file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        
        TestKit.write(file, "init");
        // back to up to date
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.FALSE, attrValue);
    }
    
    public void renameVersionedFolder_FO() throws Exception {
        // init
        File fromFolder = new File(wc, "from");
        fromFolder.mkdirs();
        commit(wc);               
       
        File toFolder = new File(wc, "to");
        
        // rename       
        renameFO(fromFolder, toFolder);
        
        // test 
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());        
        assertCachedStatus(fromFolder, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        commit(wc);
        assertFalse(fromFolder.exists());        
    }

    public void moveVersionedFolder_FO() throws Exception {
        // init
        File fromFolder = new File(wc, "from");
        fromFolder.mkdirs();
        File toParent = new File(wc, "toFolder");
        toParent.mkdirs();
        commit(wc);               
        
        File toFolder = new File(toParent, fromFolder.getName());
        
        // move
        moveFO(fromFolder, toFolder);
        
        // test 
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());        
        assertCachedStatus(fromFolder, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        commit(wc);
        assertFalse(fromFolder.exists()); 
    }    
    
    public void renameFileTree_FO() throws Exception {
        // init
        File fromFolder = new File(wc, "from");
        fromFolder.mkdirs();
        File fromFolder1 = new File(fromFolder, "folder1");
        fromFolder1.mkdirs();
        File fromFolder2 = new File(fromFolder, "folder2");
        fromFolder2.mkdirs();
        File fromFile11 = new File(fromFolder1, "file11");
        fromFile11.createNewFile();
        File fromFile12 = new File(fromFolder1, "file12");
        fromFile12.createNewFile();
        File fromFile21 = new File(fromFolder2, "file21");
        fromFile21.createNewFile();
        File fromFile22 = new File(fromFolder2, "file22");
        fromFile22.createNewFile();
        commit(wc);               
        
        // rename
        File toFolder = new File(wc, "to");
        renameFO(fromFolder, toFolder);
                                        
        // test 
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        File toFolder1 = new File(toFolder, "folder1");
        assertTrue(toFolder1.exists());
        File toFolder2 = new File(toFolder, "folder2");
        assertTrue(toFolder2.exists());
        File toFile11 = new File(toFolder1, "file11");
        assertTrue(toFile11.exists());
        File toFile12 = new File(toFolder1, "file12");
        assertTrue(toFile12.exists());
        File toFile21 = new File(toFolder2, "file21");
        assertTrue(toFile21.exists());
        File toFile22 = new File(toFolder2, "file22");
        assertTrue(toFile22.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder1).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder2).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile11).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile12).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile21).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile22).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFolder1).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFolder2).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile11).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile12).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile21).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile22).getTextStatus());    
        
        assertCachedStatus(fromFolder, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFolder1, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFolder2, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile11, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile12, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile21, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile22, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        assertCachedStatus(toFolder1, FileInformation.STATUS_VERSIONED_UPTODATE);                
        assertCachedStatus(toFolder2, FileInformation.STATUS_VERSIONED_UPTODATE);                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile11));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile11));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile11));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile11));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile12));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile21));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile22));   
        
        commit(wc);
        assertFalse(fromFolder.exists());
    }
    
    public void moveFileTree_FO() throws Exception {
        // init
        File fromFolder = new File(wc, "from");
        fromFolder.mkdirs();
        File fromFolder1 = new File(fromFolder, "folder1");
        fromFolder1.mkdirs();
        File fromFolder2 = new File(fromFolder, "folder2");
        fromFolder2.mkdirs();
        File fromFile11 = new File(fromFolder1, "file11");
        fromFile11.createNewFile();
        File fromFile12 = new File(fromFolder1, "file12");
        fromFile12.createNewFile();
        File fromFile21 = new File(fromFolder2, "file21");
        fromFile21.createNewFile();
        File fromFile22 = new File(fromFolder2, "file22");
        fromFile22.createNewFile();
        
        File toFolderParent = new File(wc, "to");
        toFolderParent.mkdirs();
        
        commit(wc);               
        
        File toFolder = new File(toFolderParent, fromFolder.getName());
        
        // move
        moveFO(fromFolder, toFolder);
                                                
        // test         t.
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        File toFolder1 = new File(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        File toFolder2 = new File(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        File toFile11 = new File(toFolder1, "file11");
        assertTrue(toFile11.exists());
        File toFile12 = new File(toFolder1, "file12");
        assertTrue(toFile12.exists());
        File toFile21 = new File(toFolder2, "file21");
        assertTrue(toFile21.exists());
        File toFile22 = new File(toFolder2, "file22");
        assertTrue(toFile22.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder1).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder2).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile11).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile12).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile21).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile22).getTextStatus());        
        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFolder1).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFolder2).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile11).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile12).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile21).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile22).getTextStatus());    
        
        assertCachedStatus(fromFolder, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFolder1, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFolder2, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile11, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile12, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile21, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile22, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);     
        
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        assertCachedStatus(toFolder1, FileInformation.STATUS_VERSIONED_UPTODATE);                
        assertCachedStatus(toFolder2, FileInformation.STATUS_VERSIONED_UPTODATE);                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile11));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile12));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile21));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile22));                
        
        commit(wc);
        assertFalse(fromFolder.exists());
    }

    public void deleteCreateChangeCase_issue_157373 () throws Exception {
        // init
        final File fileA = new File(wc, "file");
        FileUtil.toFileObject(wc).createData(fileA.getName());
        assertCachedStatus(fileA, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        commit(wc);
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));

        // rename
        fileA.delete();
        Handler h = new SVNInterceptor();
        Subversion.LOG.addHandler(h);
        RequestProcessor.Task r = Subversion.getInstance().getParallelRequestProcessor().create(new Runnable() {
            public void run() {
                FileUtil.refreshFor(fileA);
            }
        });
        r.run();
        assertFalse(fileA.exists());
        final File fileB = new File(wc, fileA.getName().toUpperCase());
        fileB.createNewFile();
        Thread.sleep(3000);
        assertTrue(fileB.exists());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getStatus(fileB));
        Subversion.LOG.removeHandler(h);
    }
    
    public void deleteA_renameB2A2B_DO() throws Exception {
        // init
        File folder = new File(wc, "folder");
        folder.mkdir();
        File fileA = new File(folder, "A");
        fileA.createNewFile();
        File fileB = new File(folder, "B");
        fileB.createNewFile();
        commit(wc);
        
        //delete
        delete(fileA);
        assertFalse(fileA.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        // move
        renameDO(fileB, fileA);
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertEquals(SVNStatusKind.REPLACED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileB).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileB));
        // move second
        renameDO(fileA, fileB);
        assertFalse(fileA.exists());
        assertTrue(fileB.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileB).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileB));
    }
    
    public void deleteA_renameUnversioned2A_DO() throws Exception {
        // init
        File folder = new File(wc, "folder");
        folder.mkdir();
        File fileA = new File(folder, "A");
        fileA.createNewFile();
        commit(wc);
        
        File fileUnversioned = new File(folder, "Unversioned");
        fileUnversioned.createNewFile();

        //delete
        delete(fileA);
        assertFalse(fileA.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        // move
        renameDO(fileUnversioned, fileA);

        // test
        assertTrue(fileA.exists());
        assertFalse(fileUnversioned.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
    }
    
    public void deleteA_renameB2A2B_FO() throws Exception {
        // init
        File folder = new File(wc, "folder");
        folder.mkdir();
        File fileA = new File(folder, "A");
        fileA.createNewFile();
        File fileB = new File(folder, "B");
        fileB.createNewFile();
        commit(wc);
        
        //delete
        delete(fileA);
        assertFalse(fileA.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        // move
        renameFO(fileB, fileA);
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertEquals(SVNStatusKind.REPLACED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileB).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileB));
        // move second
        renameFO(fileA, fileB);
        assertFalse(fileA.exists());
        assertTrue(fileB.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileB).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileB));
    }
    
    public void deleteA_renameUnversioned2A_FO() throws Exception {
        // init
        File folder = new File(wc, "folder");
        folder.mkdir();
        File fileA = new File(folder, "A");
        fileA.createNewFile();
        commit(wc);
        
        File fileUnversioned = new File(folder, "Unversioned");
        fileUnversioned.createNewFile();

        //delete
        delete(fileA);
        assertFalse(fileA.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        // move
        renameFO(fileUnversioned, fileA);

        // test
        assertTrue(fileA.exists());
        assertFalse(fileUnversioned.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
    }
    
    public void deleteA_moveB2A2B_DO() throws Exception {
        // init
        File folderA = new File(wc, "folderA");
        folderA.mkdir();
        File fileA = new File(folderA, "f");
        fileA.createNewFile();
        File folderB = new File(wc, "folderB");
        folderB.mkdir();
        File fileB = new File(folderB, "f");
        fileB.createNewFile();
        commit(wc);
        
        //delete
        delete(fileA);
        assertFalse(fileA.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        // move
        moveDO(fileB, fileA);
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertEquals(SVNStatusKind.REPLACED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileB).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileB));
        // move second
        moveDO(fileA, fileB);
        assertFalse(fileA.exists());
        assertTrue(fileB.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileB).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileB));
    }
    
    public void deleteA_moveUnversioned2A_DO() throws Exception {
        // init
        File folderA = new File(wc, "folderA");
        folderA.mkdir();
        File fileA = new File(folderA, "f");
        fileA.createNewFile();
        commit(wc);
        
        File folderB = new File(wc, "folderB");
        folderB.mkdir();
        File fileUnversioned = new File(folderB, "f");
        fileUnversioned.createNewFile();

        //delete
        delete(fileA);
        assertFalse(fileA.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        // move
        moveDO(fileUnversioned, fileA);

        // test
        assertTrue(fileA.exists());
        assertFalse(fileUnversioned.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
    }
    
    public void deleteA_moveB2A2B_FO() throws Exception {
        // init
        File folderA = new File(wc, "folderA");
        folderA.mkdir();
        File fileA = new File(folderA, "f");
        fileA.createNewFile();
        File folderB = new File(wc, "folderB");
        folderB.mkdir();
        File fileB = new File(folderB, "f");
        fileB.createNewFile();
        commit(wc);
        
        //delete
        delete(fileA);
        assertFalse(fileA.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        // move
        moveFO(fileB, fileA);
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertEquals(SVNStatusKind.REPLACED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileB).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileB));
        // move second
        moveFO(fileA, fileB);
        assertFalse(fileA.exists());
        assertTrue(fileB.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileB).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileB));
    }
    
    public void deleteA_moveUnversioned2A_FO() throws Exception {
        // init
        File folderA = new File(wc, "folderA");
        folderA.mkdir();
        File fileA = new File(folderA, "f");
        fileA.createNewFile();
        commit(wc);
        
        File folderB = new File(wc, "folderB");
        folderB.mkdir();
        File fileUnversioned = new File(folderB, "f");
        fileUnversioned.createNewFile();

        //delete
        delete(fileA);
        assertFalse(fileA.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        // move
        moveFO(fileUnversioned, fileA);

        // test
        assertTrue(fileA.exists());
        assertFalse(fileUnversioned.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
    }
    
    public void deleteA_copyUnversioned2A_DO() throws Exception {
        // init
        File folderA = new File(wc, "folderA");
        folderA.mkdir();
        File fileA = new File(folderA, "f");
        fileA.createNewFile();
        commit(wc);
        
        File folderB = new File(wc, "folderB");
        folderB.mkdir();
        File fileUnversioned = new File(folderB, "f");
        fileUnversioned.createNewFile();

        //delete
        delete(fileA);
        assertFalse(fileA.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        // move
        copyDO(fileUnversioned, fileA);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileUnversioned.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
    }
    
    public void deleteA_copyUnversioned2A_FO() throws Exception {
        // init
        File folderA = new File(wc, "folderA");
        folderA.mkdir();
        File fileA = new File(folderA, "f");
        fileA.createNewFile();
        commit(wc);
        
        File folderB = new File(wc, "folderB");
        folderB.mkdir();
        File fileUnversioned = new File(folderB, "f");
        fileUnversioned.createNewFile();

        //delete
        delete(fileA);
        assertFalse(fileA.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        // move
        copyFO(fileUnversioned, fileA);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileUnversioned.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
    }
    
    class SVNInterceptor extends Handler {
        public void publish(LogRecord rec) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public void flush() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void close() throws SecurityException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    protected void commit(File folder) throws SVNClientException {
        TestKit.commit(folder);
    }

    protected void add(File file) throws SVNClientException {
        TestKit.add(file);
    }
    
    private void cleanUpRepo() throws SVNClientException {
        ISVNClientAdapter client = getClient();
        ISVNDirEntry[] entries = client.getList(repoUrl, SVNRevision.HEAD, false);
        SVNUrl[] urls = new SVNUrl[entries.length];
        for (int i = 0; i < entries.length; i++) {
            urls[i] = repoUrl.appendPath(entries[i].getPath());            
        }        
        client.remove(urls, "cleanup");
    }

    private void cleanUpWC(File wc) throws IOException {
        if(wc.exists()) {
            File[] files = wc.listFiles();
            if(files != null) {
                for (File file : files) {
                    if(!file.getName().equals("cache")) { // do not delete the cache
                        FileObject fo = FileUtil.toFileObject(file);
                        if (fo != null) {
                            fo.delete();
                        }
                    }
                }
            }
        }
    }

    private void assertStatus(SVNStatusKind status, File wc) throws SVNClientException {
        ISVNStatus[] values = getClient().getStatus(new File[]{wc});
        for (ISVNStatus iSVNStatus : values) {
            assertEquals(status, iSVNStatus.getTextStatus());
        }
    }
 
    private ISVNStatus getSVNStatus(File file) throws SVNClientException {
        return TestKit.getSVNStatus(file);
    }
    
    private ISVNClientAdapter getClient() throws SVNClientException  {
        return TestKit.getClient();
    }   
    
    private void assertCachedStatus(File file, int expectedStatus) throws Exception {
        assert !file.isFile() || expectedStatus != FileInformation.STATUS_VERSIONED_UPTODATE : "doesn't work for dirs with FileInformation.STATUS_VERSIONED_UPTODATE. Use getStatus instead";
        int status = getCachedStatus(file, expectedStatus);
        assertEquals(expectedStatus, status);
    }        

    private int getCachedStatus(File file, int exceptedStatus) throws Exception, InterruptedException {
        FileInformation info = null;
        for (int i = 0; i < 600; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                throw ex;
            }
            info = cache.getCachedStatus(file);
            if (info != null && info.getStatus() == exceptedStatus) {
                break;
            }            
        }
        if (info == null) {
            throw new Exception("Cache timeout!");
        }
        return info.getStatus();
    }
    
    private int getStatus(File file) {
        return cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus();
    }
    
    private void initRepo() throws MalformedURLException, IOException, InterruptedException, SVNClientException {        
        TestKit.initRepo(repoDir, wc);
        TestKit.initRepo(repo2Dir, wc);
    }
    
    private void svnimport() throws SVNClientException, MalformedURLException {
        TestKit.svnimport(repoDir, wc);
        TestKit.svnimport(repo2Dir, wc2);
    }        
    
    private void delete(File file) throws IOException {
        DataObject dao = DataObject.find(FileUtil.toFileObject(file));    
        dao.delete();   
    }   
    
    private void waitALittleBit(long t) {
        try {
            Thread.sleep(t);  
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private boolean isMetadata(File file) {
        return TestKit.isMetadata(file);
    }
    
    private void renameDO(File from, File to) throws DataObjectNotFoundException, IOException {
        DataObject daoFrom = DataObject.find(FileUtil.toFileObject(from));                
        daoFrom.rename(to.getName());               
    }
    
    private void renameFO(File from, File to) throws DataObjectNotFoundException, IOException {
        // ensure parent is known by filesystems
        // otherwise no event will be thrown
        FileObject parent = FileUtil.toFileObject(from.getParentFile());

        FileObject foFrom = FileUtil.toFileObject(from);
        FileLock lock = foFrom.lock();
        try {
            foFrom.rename(lock, to.getName(), null);
        } finally {
            lock.releaseLock();
        }
    }
    
    private void moveDO(File from, File to) throws DataObjectNotFoundException, IOException {
        DataObject daoFrom = DataObject.find(FileUtil.toFileObject(from));    
        DataObject daoTarget = DataObject.find(FileUtil.toFileObject(to.getParentFile()));    
        daoFrom.move((DataFolder) daoTarget);    
    }

    private void copyDO(File from, File to) throws DataObjectNotFoundException, IOException {
        DataObject daoFrom = DataObject.find(FileUtil.toFileObject(from));
        DataObject daoTarget = DataObject.find(FileUtil.toFileObject(to.getParentFile()));
        daoFrom.copy((DataFolder) daoTarget);
    }
    
    private void moveFO(File from, File to) throws DataObjectNotFoundException, IOException {
        FileObject foFrom = FileUtil.toFileObject(from);
        assertNotNull(foFrom);
        FileObject foTarget = FileUtil.toFileObject(to.getParentFile());
        assertNotNull(foTarget);
        FileLock lock = foFrom.lock();
        try {
            foFrom.move(lock, foTarget, to.getName(), null);
        } finally {
            lock.releaseLock();
        }        
    }

    private void copyFO(File from, File to) throws DataObjectNotFoundException, IOException {
        FileObject foFrom = FileUtil.toFileObject(from);
        assertNotNull(foFrom);
        FileObject foTarget = FileUtil.toFileObject(to.getParentFile());
        assertNotNull(foTarget);
        FileLock lock = foFrom.lock();
        try {
            foFrom.copy(foTarget, getName(to), getExt(to));
        } finally {
            lock.releaseLock();
        }
    }

    private String getName(File f) {
        String ret = f.getName();
        int idx = ret.lastIndexOf(".");
        return idx > -1 ? ret.substring(0, idx) : ret;
    }

    private String getExt(File f) {
        String ret = f.getName();
        int idx = ret.lastIndexOf(".");
        return idx > -1 ? ret.substring(idx) : null;
    }
    
}
