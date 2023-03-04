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
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.subversion.FileInformation;
import org.tigris.subversion.svnclientadapter.*;

/**
 *
 * @author tomas
 */
public class TreeConflictsTestHidden extends AbstractCommandTestCase {
    private File wc1, wc2;
    private File f1, f2;
    private File fcopy1, fcopy2;
    private File folder1, folder2;
    
    // XXX terst remote change
    
    private enum StatusCall {
        filearray,
        file
    }
    
    public TreeConflictsTestHidden(String testName) throws Exception {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        importWC = false;
        super.setUp();
        cleanUpRepo(new String[] {CI_FOLDER});
        File ciFolder = importFolder(CI_FOLDER);
        wc1 = checkout(ciFolder.getName(), "checkout1");
        folder1 = new File(wc1, "folder");
        f1 = new File(folder1, "file");
        fcopy1 = new File(folder1, "filecopy");
        wc2 = checkout(ciFolder.getName(), "checkout2");
        folder2 = new File(wc2, "folder");
        f2 = new File(folder2, "file");
        fcopy2 = new File(folder2, "filecopy");
    }

    public void testStatusLocalEditIncomingDeleteAcceptRemote () throws Exception {
        ISVNClientAdapter c = getNbClient();
        prepareTestStatusLocalEditIncomingDelete(c);

        c.remove(new File[] { f2 }, true);
        try {
            assertStatus(f2, false, SVNStatusKind.UNVERSIONED);
        } catch (AssertionError err) {
            assertStatus(f2, false, SVNStatusKind.NONE);
        }
        assertFalse(f2.exists());
    }

    public void testStatusLocalEditIncomingDeleteAcceptLocal () throws Exception {
        ISVNClientAdapter c = getNbClient();
        prepareTestStatusLocalEditIncomingDelete(c);

        c.resolved(f2);
        assertStatus(f2, false, SVNStatusKind.ADDED);
        assertTrue(f2.exists());
    }

    private void prepareTestStatusLocalEditIncomingDelete (ISVNClientAdapter c) throws Exception {
        ISVNStatus st1 = c.getSingleStatus(f1);
        ISVNStatus st2 = c.getSingleStatus(f2);
        assertFalse(st1.hasTreeConflict());
        assertFalse(st2.hasTreeConflict());
        c.move(f1, fcopy1, true);
        assertTrue(fcopy1.exists());
        commit(wc1);
        write(f2, "change");
        assertFalse(fcopy2.exists());
        
        c.update(wc2, SVNRevision.HEAD, true);
        assertStatus(f2, true, SVNStatusKind.ADDED);
        assertTrue(fcopy2.exists());
        assertStatus(fcopy2, false, SVNStatusKind.NORMAL);
    }

    @RandomlyFails
    public void testStatusLocalDeleteIncomingEditAcceptLocal () throws Exception {
        ISVNClientAdapter c = getNbClient();
        prepareTestStatusLocalDeleteIncomingEdit(c);

        assertFalse(f2.exists());
        c.resolved(f2);
        assertStatus(f2, false, SVNStatusKind.DELETED);
        assertFalse(f2.exists());
    }

    @RandomlyFails
    public void testStatusLocalDeleteIncomingEditAcceptRemote () throws Exception {
        ISVNClientAdapter c = getNbClient();
        prepareTestStatusLocalDeleteIncomingEdit(c);

        assertFalse(f2.exists());
        c.revert(f2, true);
        assertStatus(f2, false, SVNStatusKind.NORMAL);
        assertTrue(f2.exists());
    }

    private void prepareTestStatusLocalDeleteIncomingEdit (ISVNClientAdapter c) throws Exception {
        ISVNStatus st1 = c.getSingleStatus(f1);
        ISVNStatus st2 = c.getSingleStatus(f2);
        assertFalse(st1.hasTreeConflict());
        assertFalse(st2.hasTreeConflict());
        write(f1, "change");
        commit(wc1);
        c.move(f2, fcopy2, true);
        assertTrue(fcopy2.exists());

        c.update(wc2, SVNRevision.HEAD, true);
        assertStatus(f2, true, SVNStatusKind.DELETED);
        assertTrue(fcopy2.exists());
        assertStatus(fcopy2, false, SVNStatusKind.ADDED);
    }

    @RandomlyFails
    public void testStatusLocalDeleteIncomingDelete () throws Exception {
        ISVNClientAdapter c = getNbClient();
        ISVNStatus st1 = c.getSingleStatus(f1);
        ISVNStatus st2 = c.getSingleStatus(f2);
        assertFalse(st1.hasTreeConflict());
        assertFalse(st2.hasTreeConflict());
        c.move(f1, fcopy1, true);
        assertFalse(f1.exists());
        assertTrue(fcopy1.exists());
        commit(wc1);

        c.move(f2, fcopy2, true);
        assertFalse(f2.exists());
        assertTrue(fcopy2.exists());

        c.update(wc2, SVNRevision.HEAD, true);
        assertStatus(fcopy2, true, SVNStatusKind.REPLACED);
        assertStatus(f2, true, isJavahl() ? SVNStatusKind.NONE : SVNStatusKind.MISSING);

        c.resolved(f2);
        assertStatus(f2, false, SVNStatusKind.NONE);
        assertFalse(f2.exists());
        c.resolved(fcopy2);
        assertTrue(fcopy2.exists());
        assertStatus(fcopy2, false, SVNStatusKind.REPLACED);
        c.revert(fcopy2, true);
        assertStatus(fcopy2, false, SVNStatusKind.NORMAL);
        update(fcopy2);
        assertTrue(fcopy2.exists());
        assertStatus(fcopy2, false, SVNStatusKind.NORMAL);
    }

    @RandomlyFails
    public void testStatusFolderLocalDeleteIncomingDelete () throws Exception {
        ISVNClientAdapter c = getNbClient();
        ISVNStatus st1 = c.getSingleStatus(f1);
        ISVNStatus st2 = c.getSingleStatus(f2);
        assertFalse(st1.hasTreeConflict());
        assertFalse(st2.hasTreeConflict());
        File folderCopy1 = new File(folder1.getParent(), "folderCopy");
        c.move(folder1, folderCopy1, true);
        assertFalse(folder1.exists());
        assertTrue(folderCopy1.exists());
        commit(wc1);
        assertFalse(folder1.exists());
        assertTrue(folderCopy1.exists());

        File folderCopy2 = new File(folder2.getParent(), "folderCopy");
        c.move(folder2, folderCopy2, true);
        assertFalse(folder2.exists());
        assertTrue(folderCopy2.exists());

        c.update(wc2, SVNRevision.HEAD, true);
        assertFalse(folder2.exists());
        assertTrue(folderCopy2.exists());
        assertStatus(folderCopy2, true, SVNStatusKind.REPLACED);
        assertStatus(folder2, true, isJavahl() ? SVNStatusKind.NONE : SVNStatusKind.MISSING);

        c.resolved(folder2);
        assertStatus(folder2, false, SVNStatusKind.NONE);
        assertFalse(folder2.exists());
        c.resolved(folderCopy2);
        assertTrue(folderCopy2.exists());
        assertStatus(folderCopy2, false, SVNStatusKind.REPLACED);
        c.revert(folderCopy2, true);
        assertStatus(folderCopy2, false, SVNStatusKind.NORMAL);
        update(folderCopy2);
        assertTrue(folderCopy2.exists());
        assertStatus(folderCopy2, false, SVNStatusKind.NORMAL);
    }

    private void assertStatus (File f, boolean hasConflicts, SVNStatusKind svnStatus) throws Exception {
        ISVNClientAdapter c = getNbClient();
        ISVNStatus st = c.getSingleStatus(f);
        assert hasConflicts == st.hasTreeConflict();
        if (isCommandLine()) {
            // not implemented in cli for 1.7
            assert st.getConflictDescriptor() == null;
        } else {
            assert hasConflicts == (st.getConflictDescriptor() != null);
        }
        assertEquals(svnStatus, st.getTextStatus());
        ISVNStatus[] sts = c.getStatus(new File[] {f});
        assertStatus(f, hasConflicts, false, sts, svnStatus);
        sts = c.getStatus(f.getParentFile(), true, true);
        assertStatus(f, hasConflicts, hasConflicts, sts, svnStatus);
        sts = c.getStatus(f.getParentFile(), true, true, false);
        assertStatus(f, hasConflicts, false, sts, svnStatus);

        sts = c.getStatus(f, false, true);
        assertStatus(f, hasConflicts, true, sts, svnStatus);

        assert ((getStatus(f) & FileInformation.STATUS_VERSIONED_CONFLICT_TREE) != 0) == hasConflicts;
    }

    private void assertStatus (File f, boolean hasConflicts, boolean testConflictDescriptor, ISVNStatus[] sts, SVNStatusKind svnStatus) throws Exception {
        for (ISVNStatus st : sts) {
            if (f.equals(st.getFile())) {
                assertEquals(svnStatus, st.getTextStatus());
                if (st.hasTreeConflict() != hasConflicts) {
                    fail("hasConflicts !== status.hasTreeConflicts");
                }
                if (testConflictDescriptor) {
                    if (isCommandLine()) {
                        // not implemented in cli for 1.7
                        assert st.getConflictDescriptor() == null;
                    } else if (hasConflicts == (st.getConflictDescriptor() == null)) {
                        fail("hasConflicts === (status.getConflictDescriptor() == null)");
                    }
                }
                return;
            }
        }
        if (hasConflicts) {
            fail("No such status");
        }
    }
    
    private File importFolder (String repoFolderName) throws Exception {
        File cifolder = createFolder(repoFolderName);
        File folder1 = createFolder(cifolder, "folder");
        File file = createFile(folder1, "file");

        importFile(cifolder);
        return cifolder;
    }

    private File checkout (String ciFolderName,
                                    String targetFolderName) throws Exception {
        File checkout = createFolder(targetFolderName);
        SVNUrl url = getTestUrl().appendPath(ciFolderName);
        ISVNClientAdapter c = getNbClient();
        c.checkout(url, checkout, SVNRevision.HEAD, true);
        return checkout;
    }
}
