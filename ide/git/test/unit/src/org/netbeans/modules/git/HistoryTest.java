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

package org.netbeans.modules.git;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import org.netbeans.junit.MockServices;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitMergeResult;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitUser;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.masterfs.VersioningAnnotationProvider;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider.HistoryEntry;
import org.netbeans.modules.versioning.spi.VersioningSupport;

/**
 *
 * @author ondra
 */
public class HistoryTest extends AbstractGitTestCase {

    public HistoryTest (String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(new Class[] {
            VersioningAnnotationProvider.class,
            GitVCS.class});
    }

    public void testOriginalFile () throws Exception {
        File f = new File(repositoryLocation, "file");
        File tmp = new File(repositoryLocation.getParentFile(), "tmp");
        write(f, read(getGoldenFile()));
        assertFile(getGoldenFile(), f);
        GitVCS git = new GitVCS();

        git.getOriginalFile(f, tmp);
        assertEquals(0, tmp.length());
        add(f);
        git.getOriginalFile(f, tmp);
        assertEquals(0, tmp.length());
        commit(f);

        git.getOriginalFile(f, tmp);
        assertFile(tmp, getGoldenFile());

        write(f, "blablabla");
        git.getOriginalFile(f, tmp);
        assertFile(tmp, getGoldenFile());
    }
    
    public void testHistoryProviderSimple () throws Exception {
        File f = new File(repositoryLocation, "file");
        write(f, "a\nb\nc\nd\n");
        File[] roots = new File[] { f };
        GitClient client = getClient(repositoryLocation);
        client.add(roots, GitUtils.NULL_PROGRESS_MONITOR);
        GitRevisionInfo rev1 = client.commit(roots, "first commit", new GitUser("author", "author@author.com"), null, GitUtils.NULL_PROGRESS_MONITOR);
        Thread.sleep(1100);
        write(f, "a\nb\nc\nd\ne\n");
        client.add(roots, GitUtils.NULL_PROGRESS_MONITOR);
        GitRevisionInfo rev2 = client.commit(roots, "second commit", new GitUser("author2", "author2@author.com"), null, GitUtils.NULL_PROGRESS_MONITOR);
        Thread.sleep(1100);
        
        VCSHistoryProvider p = VersioningSupport.getOwner(f).getVCSHistoryProvider();
        assertTrue(p instanceof HistoryProvider);
        VCSHistoryProvider.HistoryEntry[] entries = p.getHistory(new File[] { f }, new Date(0));
        assertEquals(2, entries.length);
        assertEntry(rev2, entries[0]);
        assertEntry(rev1, entries[1]);
        assertNull(entries[1].getParentEntry(f));
        assertEntry(entries[1], entries[0].getParentEntry(f));
    }
    
    public void testHistoryProviderBranches () throws Exception {
        File f = new File(repositoryLocation, "file");
        write(f, "a\nb\nc\nd\n");
        File[] roots = new File[] { f };
        GitClient client = getClient(repositoryLocation);
        client.add(roots, GitUtils.NULL_PROGRESS_MONITOR);
        GitRevisionInfo revBase = client.commit(roots, "base commit", new GitUser("author", "author@author.com"), null, GitUtils.NULL_PROGRESS_MONITOR);
        Thread.sleep(1100);
        
        // new branch
        client.createBranch("branch", "master", GitUtils.NULL_PROGRESS_MONITOR);

        // change on master
        write(f, "x\nb\nc\nd\n");
        client.add(roots, GitUtils.NULL_PROGRESS_MONITOR);
        GitRevisionInfo revMaster = client.commit(roots, "master commit", new GitUser("author2", "author2@author.com"), null, GitUtils.NULL_PROGRESS_MONITOR);
        Thread.sleep(1100);
        
        client.checkoutRevision("branch", true, GitUtils.NULL_PROGRESS_MONITOR);
        
        // change on branch
        write(f, "a\nb\nc\nz\n");
        client.add(roots, GitUtils.NULL_PROGRESS_MONITOR);
        GitRevisionInfo revBranch = client.commit(roots, "branch commit", new GitUser("author3", "author3@author.com"), null, GitUtils.NULL_PROGRESS_MONITOR);
        Thread.sleep(1100);
        
        // back to master
        client.checkoutRevision("master", true, GitUtils.NULL_PROGRESS_MONITOR);

        // history displayed only for the current branch
        VCSHistoryProvider p = VersioningSupport.getOwner(f).getVCSHistoryProvider();
        assertTrue(p instanceof HistoryProvider);
        VCSHistoryProvider.HistoryEntry[] entries = p.getHistory(new File[] { f }, new Date(0));
        assertEquals(2, entries.length);
        assertEntry(revMaster, entries[0]);
        assertEntry(revBase, entries[1]);
        assertNull(entries[1].getParentEntry(f));
        assertEntry(entries[1], entries[0].getParentEntry(f));
        
        // let's merge and test the merge
        GitMergeResult mergeResult = client.merge("branch", GitUtils.NULL_PROGRESS_MONITOR);
        Thread.sleep(100);
        GitRevisionInfo revMerge = client.log(mergeResult.getNewHead(), GitUtils.NULL_PROGRESS_MONITOR);
        entries = p.getHistory(new File[] { f }, new Date(0));
        // merges are not displayed in history tab
        assertEquals(3, entries.length);
        assertEntry(revBranch, entries[0]);
        assertEntry(revMaster, entries[1]);
        assertEntry(revBase, entries[2]);
        assertNull(entries[2].getParentEntry(f));
        assertEntry(entries[2], entries[0].getParentEntry(f));
        assertEntry(entries[2], entries[1].getParentEntry(f));
    }

    private void assertEntry (GitRevisionInfo rev, HistoryEntry historyEntry) throws GitException {
        assertEquals(rev.getFullMessage(), historyEntry.getMessage());
        assertEquals(rev.getRevision(), historyEntry.getRevision());
        assertEquals(rev.getAuthor().toString(), historyEntry.getUsername());
        assertEquals(new Date(rev.getCommitTime()), historyEntry.getDateTime());
        assertEquals(rev.getModifiedFiles().keySet(), new HashSet<File>(Arrays.asList(historyEntry.getFiles())));
    }

    private void assertEntry (HistoryEntry expected, HistoryEntry entry) {
        assertEquals(expected.getMessage(), entry.getMessage());
        assertEquals(expected.getRevision(), entry.getRevision());
        assertEquals(expected.getUsername(), entry.getUsername());
        assertEquals(expected.getDateTime(), entry.getDateTime());
        assertEquals(new HashSet<File>(Arrays.asList(expected.getFiles())), new HashSet<File>(Arrays.asList(entry.getFiles())));
    }
}
