/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
