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
package org.netbeans.modules.subversion.client.parser;

import junit.framework.*;
import java.io.File;
import java.util.Date;
import org.netbeans.junit.NbTestCase;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

/**
 *
 * @author Ed Hillmann
 */
public class SvnWcParserTest extends NbTestCase {

    private String dataRootDir;
    private SvnWcParser svnWcParser;

    public SvnWcParserTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        svnWcParser = new SvnWcParser();

        System.setProperty("svnClientAdapterFactory", "commandline");

        //data.root.dir defined in project.properties
        dataRootDir = getDataDir().getAbsolutePath();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SvnWcParserTest.class);

        return suite;
    }

    public void testGetSingleStatusNoChanges() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/no-changes/testapp/Main.java");
        assertTrue(myFile.exists());
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/Main.java", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getTextStatus());
        assertEquals(2, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        Date expectedDate = SvnWcUtils.parseSvnDate("2006-04-12T10:43:46.371180Z");
        assertEquals(expectedDate, parsedStatus.getLastChangedDate());
        assertEquals(2, parsedStatus.getLastChangedRevision().getNumber());
        assertEquals("ed", parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }

    public void testGetSingleStatusNoChangesNewFormat() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/new-format-no-changes/testapp/Main.java");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/Main.java", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getTextStatus());
        assertEquals(16, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        Date expectedDate = SvnWcUtils.parseSvnDate("2006-08-05T03:42:58.306031Z");
        assertEquals(expectedDate, parsedStatus.getLastChangedDate());
        assertEquals(16, parsedStatus.getLastChangedRevision().getNumber());
        assertEquals("ed", parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }
    
    public void testGetSingleStatusFileChanges() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/file-changes/testapp/Main.java");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/Main.java", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.MODIFIED, parsedStatus.getTextStatus());
        assertEquals(2, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        Date expectedDate = SvnWcUtils.parseSvnDate("2006-04-12T10:43:46.371180Z");
        assertEquals(expectedDate, parsedStatus.getLastChangedDate());
        assertEquals(2, parsedStatus.getLastChangedRevision().getNumber());
        assertEquals("ed", parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }

    public void testGetSingleStatusFileChangesNewFormat() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/new-format-file-changes/testapp/AnotherMain.java");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/AnotherMain.java", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.MODIFIED, parsedStatus.getTextStatus());
        assertEquals(16, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        Date expectedDate = SvnWcUtils.parseSvnDate("2006-04-25T07:05:57.738276Z");
        assertEquals(expectedDate, parsedStatus.getLastChangedDate());
        assertEquals(10, parsedStatus.getLastChangedRevision().getNumber());
        assertEquals("ed", parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }
    
    public void testGetSingleStatusFileUnknown() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/file-unknown/testapp/ReadMe.txt");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/ReadMe.txt", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.UNVERSIONED, parsedStatus.getTextStatus());
        assertEquals(0, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        assertNull(parsedStatus.getLastChangedDate());
        assertEquals(0, parsedStatus.getLastChangedRevision().getNumber());
        assertNull(parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.UNKNOWN, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.UNVERSIONED, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }

    public void testGetSingleStatusFileUnknownNewFormat() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/new-format-file-unknown/testapp/readme.txt");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/readme.txt", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.UNVERSIONED, parsedStatus.getTextStatus());
        assertEquals(0, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        assertNull(parsedStatus.getLastChangedDate());
        assertEquals(0, parsedStatus.getLastChangedRevision().getNumber());
        assertNull(parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.UNKNOWN, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.UNVERSIONED, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }
    
    /**
     * Tests a specific case... where the file doesn't exist, and there is no entry in the SVN
     * files, but it's still being queried by the module.  Return unversioned
     */
    public void testGetSingleStatusFileUnknownAnywhere() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/no-changes/testapp/ReadMe.txt");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/ReadMe.txt", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.UNVERSIONED, parsedStatus.getTextStatus());
        assertEquals(0, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        assertNull(parsedStatus.getLastChangedDate());
        assertEquals(0, parsedStatus.getLastChangedRevision().getNumber());
        assertNull(parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.UNKNOWN, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.UNVERSIONED, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }

    /**
     * Tests a specific case... where the file doesn't exist, and there is no entry in the SVN
     * files, but it's still being queried by the module.  Return unversioned.  Working copy is
     * the format as of SVN 1.4.0
     */
    public void testGetSingleStatusFileUnknownAnywhereNewFormat() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/new-format-no-changes/testapp/readme.txt");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/readme.txt", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.UNVERSIONED, parsedStatus.getTextStatus());
        assertEquals(0, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        assertNull(parsedStatus.getLastChangedDate());
        assertEquals(0, parsedStatus.getLastChangedRevision().getNumber());
        assertNull(parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.UNKNOWN, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.UNVERSIONED, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }
    
    public void testGetSingleStatusFileAdded() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/file-added/testapp/ReadMe.txt");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/ReadMe.txt", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.ADDED, parsedStatus.getTextStatus());
        assertEquals(0, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        assertNull(parsedStatus.getLastChangedDate());
        assertEquals(-1, parsedStatus.getLastChangedRevision().getNumber());
        assertNull(parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NONE, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }

    public void testGetSingleStatusFileAddedNewFormat() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/new-format-file-added/testapp/ReadMe.txt");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/ReadMe.txt", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.ADDED, parsedStatus.getTextStatus());
        assertEquals(0, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        assertNull(parsedStatus.getLastChangedDate());
        assertEquals(-1, parsedStatus.getLastChangedRevision().getNumber());
        assertNull(parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NONE, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }
    
    public void testGetSingleStatusFileConflict() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/file-conflicts/testapp/ReadMe.txt");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/ReadMe.txt", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.CONFLICTED, parsedStatus.getTextStatus());
        assertEquals(5, parsedStatus.getRevision().getNumber());
        assertEquals(5, parsedStatus.getLastChangedRevision().getNumber());
        assertNotNull(parsedStatus.getConflictNew());
        assertNotNull(parsedStatus.getConflictOld());
        assertNotNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        Date expectedDate = SvnWcUtils.parseSvnDate("2006-04-25T04:12:27.726955Z");
        assertEquals(expectedDate, parsedStatus.getLastChangedDate());
        assertEquals(5, parsedStatus.getLastChangedRevision().getNumber());
        assertEquals("ed", parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }

    public void testGetSingleStatusFileConflictNewFormat() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/new-format-file-conflicts/testapp/ReadMe.txt");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/ReadMe.txt", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.CONFLICTED, parsedStatus.getTextStatus());
        assertEquals(18, parsedStatus.getRevision().getNumber());
        assertEquals(18, parsedStatus.getLastChangedRevision().getNumber());
        assertNotNull(parsedStatus.getConflictNew());
        assertNotNull(parsedStatus.getConflictOld());
        assertNotNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        Date expectedDate = SvnWcUtils.parseSvnDate("2006-08-16T05:15:12.039161Z");
        assertEquals(expectedDate, parsedStatus.getLastChangedDate());
        assertEquals(18, parsedStatus.getLastChangedRevision().getNumber());
        assertEquals("ed", parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }
    
    public void testGetSingleStatusFileRemoved() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/file-removed/testapp/ReadMe.txt");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/ReadMe.txt", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.DELETED, parsedStatus.getTextStatus());
        assertEquals(6, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        Date expectedDate = SvnWcUtils.parseSvnDate("2006-04-25T04:22:27.194329Z");
        assertEquals(expectedDate, parsedStatus.getLastChangedDate());
        assertEquals(6, parsedStatus.getLastChangedRevision().getNumber());
        assertEquals("ed", parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NONE, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }

    public void testGetSingleStatusFileRemovedNewFormat() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/new-format-file-removed/testapp/ReadMe.txt");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/ReadMe.txt", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.DELETED, parsedStatus.getTextStatus());
        assertEquals(18, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        Date expectedDate = SvnWcUtils.parseSvnDate("2006-08-16T05:15:12.039161Z");
        assertEquals(expectedDate, parsedStatus.getLastChangedDate());
        assertEquals(18, parsedStatus.getLastChangedRevision().getNumber());
        assertEquals("ed", parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NONE, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }
    
    public void testGetSingleStatusFileCopied1() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/file-copied1/testapp/AnotherMain.java");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertTrue(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/AnotherMain.java", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.ADDED, parsedStatus.getTextStatus());
        assertEquals(5, parsedStatus.getRevision().getNumber());        
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        assertNull(parsedStatus.getLastChangedDate());
        assertEquals(-1, parsedStatus.getLastChangedRevision().getNumber());
        assertNull(parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NONE, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }

    public void testGetSingleStatusFileCopied1NewFormat() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/new-format-file-copied1/testapp/AnotherAnotherMain.java");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertTrue(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/AnotherAnotherMain.java", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.ADDED, parsedStatus.getTextStatus());
        assertEquals(18, parsedStatus.getRevision().getNumber());        
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        assertNull(parsedStatus.getLastChangedDate());
        assertEquals(-1, parsedStatus.getLastChangedRevision().getNumber());
        assertNull(parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NONE, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }
    
    public void testGetSingleStatusFileCopied2() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/file-copied2/testapp/AnotherMain.java");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertTrue(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/AnotherMain.java", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.ADDED, parsedStatus.getTextStatus());
        assertEquals(5, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        assertNull(parsedStatus.getLastChangedDate());
        assertEquals(-1, parsedStatus.getLastChangedRevision().getNumber());
        assertNull(parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NONE, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }

    public void testGetSingleStatusFileCopied2NewFormat() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/new-format-file-copied2/testapp/AnotherAnotherMain.java");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertTrue(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/AnotherAnotherMain.java", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.ADDED, parsedStatus.getTextStatus());
        assertEquals(18, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        assertNull(parsedStatus.getLastChangedDate());
        assertEquals(-1, parsedStatus.getLastChangedRevision().getNumber());
        assertNull(parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NONE, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }

    public void testGetSingleStatusPropertyAdded() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/prop-added/testapp/AnotherMain.java");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/AnotherMain.java", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getTextStatus());
        assertEquals(8, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        Date expectedDate = SvnWcUtils.parseSvnDate("2006-04-25T06:55:09.997277Z");
        assertEquals(expectedDate, parsedStatus.getLastChangedDate());
        assertEquals(8, parsedStatus.getLastChangedRevision().getNumber());
        assertEquals("ed", parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.MODIFIED, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }

    public void testGetSingleStatusPropertyAddedNewFormat() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/new-format-prop-added/testapp/AnotherMain.java");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/AnotherMain.java", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getTextStatus());
        assertEquals(19, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        Date expectedDate = SvnWcUtils.parseSvnDate("2006-04-25T07:05:57.738276Z");
        assertEquals(expectedDate, parsedStatus.getLastChangedDate());
        assertEquals(10, parsedStatus.getLastChangedRevision().getNumber());
        assertEquals("ed", parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.MODIFIED, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }
    
    public void testGetSingleStatusPropertyModified() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/prop-modified/testapp/AnotherMain.java");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/AnotherMain.java", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getTextStatus());
        assertEquals(9, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        Date expectedDate = SvnWcUtils.parseSvnDate("2006-04-25T07:01:25.704780Z");
        assertEquals(expectedDate, parsedStatus.getLastChangedDate());
        assertEquals(9, parsedStatus.getLastChangedRevision().getNumber());
        assertEquals("ed", parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.MODIFIED, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }

    public void testGetSingleStatusPropertyModifiedNewFormat() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/new-format-prop-modified/testapp/AnotherMain.java");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/AnotherMain.java", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getTextStatus());
        assertEquals(19, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        Date expectedDate = SvnWcUtils.parseSvnDate("2006-04-25T07:05:57.738276Z");
        assertEquals(expectedDate, parsedStatus.getLastChangedDate());
        assertEquals(10, parsedStatus.getLastChangedRevision().getNumber());
        assertEquals("ed", parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.MODIFIED, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }
    
    public void testGetSingleStatusFileLocked() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/file-locked/testapp/Main.java");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/Main.java", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getTextStatus());
        assertEquals(10, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        Date expectedDate = SvnWcUtils.parseSvnDate("2006-04-12T10:43:46.371180Z");
        assertEquals(expectedDate, parsedStatus.getLastChangedDate());
        assertEquals(2, parsedStatus.getLastChangedRevision().getNumber());
        assertEquals("ed", parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getPropStatus());
        assertEquals("", parsedStatus.getLockComment());
        assertEquals("ed", parsedStatus.getLockOwner());
        expectedDate = SvnWcUtils.parseSvnDate("2006-05-27T04:15:00.168100Z");
        assertEquals(expectedDate, parsedStatus.getLockCreationDate());
    }

    public void testGetSingleStatusFileLockedNewFormat() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/new-format-file-locked/testapp/AnotherMain.java");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/AnotherMain.java", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getTextStatus());
        assertEquals(19, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        Date expectedDate = SvnWcUtils.parseSvnDate("2006-04-25T07:05:57.738276Z");
        assertEquals(expectedDate, parsedStatus.getLastChangedDate());
        assertEquals(10, parsedStatus.getLastChangedRevision().getNumber());
        assertEquals("ed", parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertEquals("ed", parsedStatus.getLockOwner());
        expectedDate = SvnWcUtils.parseSvnDate("2006-08-29T10:28:18.570376Z");
        assertEquals(expectedDate, parsedStatus.getLockCreationDate());
    }

    public void testGetSingleStatusFileLockedWithCommentNewFormat() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/new-format-file-locked-with-comment/testapp/AnotherMain.java");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertEquals("svn://gonzo/testRepos/trunk/testApp/src/testapp/AnotherMain.java", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getTextStatus());
        assertEquals(19, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        Date expectedDate = SvnWcUtils.parseSvnDate("2006-04-25T07:05:57.738276Z");
        assertEquals(expectedDate, parsedStatus.getLastChangedDate());
        assertEquals(10, parsedStatus.getLastChangedRevision().getNumber());
        assertEquals("ed", parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getPropStatus());
        assertEquals("This is my comment", parsedStatus.getLockComment());
        assertEquals("ed", parsedStatus.getLockOwner());
        expectedDate = SvnWcUtils.parseSvnDate("2006-08-29T10:36:02.498983Z");
        assertEquals(expectedDate, parsedStatus.getLockCreationDate());
    }
        
    public void testGetSingleStatusNoChangesKeywords() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/no-changes-keywords/testapp/Main.java");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertFalse(parsedStatus.isCopied());
        assertEquals("file:///data/subversion/trunk/testapp/Main.java", parsedStatus.getUrl().toString());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getTextStatus());
        assertEquals(989, parsedStatus.getRevision().getNumber());
        assertNull(parsedStatus.getConflictNew());
        assertNull(parsedStatus.getConflictOld());
        assertNull(parsedStatus.getConflictWorking());
        assertEquals(myFile, parsedStatus.getFile());
        Date expectedDate = SvnWcUtils.parseSvnDate("2007-06-13T12:02:16.625421Z");
        assertEquals(expectedDate, parsedStatus.getLastChangedDate());
        assertEquals(330, parsedStatus.getLastChangedRevision().getNumber());
        assertEquals("tomas", parsedStatus.getLastCommitAuthor());
        assertEquals(SVNNodeKind.FILE, parsedStatus.getNodeKind());
        assertEquals(myFile.getPath(), parsedStatus.getPath());
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getPropStatus());
        assertNull(parsedStatus.getLockComment());
        assertNull(parsedStatus.getLockOwner());
        assertNull(parsedStatus.getLockCreationDate());
    }
    
    //Haven't added a NewFormat version of the NoChangesKeywords test, as the
    //testGetSingleStatusNoChangesNewFormat() already covers this

    public void testGetSingleStatusSymbolicLink() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/file-symbolic-link/bin/myLink");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getTextStatus());
    }

    public void testGetSingleStatusSymbolicLinkNewFormat() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/new-format-file-symbolic-link/bin/myLink");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertEquals(SVNStatusKind.NORMAL, parsedStatus.getTextStatus());
    }
    
    public void testGetSingleStatusBinaryConflict() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/file-binary-conflicts/bin/image.bmp");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertEquals(SVNStatusKind.CONFLICTED, parsedStatus.getTextStatus());
    }
    
    public void testGetSingleStatusBinaryConflictNewFormat() throws Exception {
        File myFile = new File(dataRootDir + "/SvnWcParser/new-format-file-binary-conflicts/bin/image.bmp");
        ISVNStatus parsedStatus = svnWcParser.getSingleStatus(myFile);
        assertEquals(SVNStatusKind.CONFLICTED, parsedStatus.getTextStatus());
    }
    
}
