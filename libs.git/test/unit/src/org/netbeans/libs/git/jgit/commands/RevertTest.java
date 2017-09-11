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

package org.netbeans.libs.git.jgit.commands;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.eclipse.jgit.lib.Repository;
import org.netbeans.libs.git.GitRevertResult;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class RevertTest extends AbstractGitTestCase {
    private File workDir;
    private Repository repository;

    public RevertTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }
    
    public void testRevertLastCommitOneFile () throws Exception {
        File f = new File(workDir, "f");
        File[] files = new File[] { f };
        write(f, "init");
        add(f);
        commit(f);
        
        // modify and commit
        write(f, "change");
        add(f);
        GitRevisionInfo commit = getClient(workDir).commit(files, "modification", null, null, NULL_PROGRESS_MONITOR);
        assertEquals("change", read(f));
        getClient(workDir).revert(commit.getRevision(), null, true, NULL_PROGRESS_MONITOR);
        assertEquals("init", read(f));
        assertEquals("Revert \"modification\"\n\nThis reverts commit " + commit.getRevision() + ".", getClient(workDir).log("master", NULL_PROGRESS_MONITOR).getFullMessage());
    }
    
    public void testRevertLastCommitTwoFiles () throws Exception {
        File f1 = new File(workDir, "f");
        File f2 = new File(workDir, "f2");
        File[] files = new File[] { f1, f2 };
        write(f1, "init1");
        write(f2, "init2");
        add(files);
        commit(files);
        
        // modify and commit
        write(f1, "change1");
        write(f2, "change2");
        add(files);
        GitRevisionInfo commit = getClient(workDir).commit(files, "modification", null, null, NULL_PROGRESS_MONITOR);
        assertEquals("change1", read(f1));
        assertEquals("change2", read(f2));
        getClient(workDir).revert(commit.getRevision(),  null, true, NULL_PROGRESS_MONITOR);
        assertEquals("init1", read(f1));
        assertEquals("init2", read(f2));
    }
    
    public void testRevertCommitBeforeLast () throws Exception {
        File f = new File(workDir, "f");
        File[] files = new File[] { f };
        write(f, "a\nb\nc");
        add(f);
        commit(f);
        
        // modify and commit
        write(f, "z\nb\nc");
        add(f);
        GitRevisionInfo commit = getClient(workDir).commit(files, "modification", null, null, NULL_PROGRESS_MONITOR);
        // second commit
        write(f, "z\nb\ny");
        add(f);
        commit(f);
        assertEquals("z\nb\ny", read(f));
        getClient(workDir).revert(commit.getRevision(),  null, true, NULL_PROGRESS_MONITOR);
        assertEquals("a\nb\ny", read(f));
    }
    
    public void testRevertLastTwoCommits () throws Exception {
        File f = new File(workDir, "f");
        File[] files = new File[] { f };
        write(f, "a\nb\nc");
        add(f);
        commit(f);
        
        // modify and commit
        write(f, "z\nb\nc");
        add(f);
        GitRevisionInfo commit = getClient(workDir).commit(files, "modification", null, null, NULL_PROGRESS_MONITOR);
        // second commit
        write(f, "z\nb\ny");
        add(f);
        GitRevisionInfo commit2 = getClient(workDir).commit(files, "modification 2", null, null, NULL_PROGRESS_MONITOR);
        assertEquals("z\nb\ny", read(f));
        getClient(workDir).revert(commit2.getRevision(),  null, true, NULL_PROGRESS_MONITOR);
        assertEquals("z\nb\nc", read(f));
        getClient(workDir).revert(commit.getRevision(),  null, true, NULL_PROGRESS_MONITOR);
        assertEquals("a\nb\nc", read(f));
    }
    
    public void testRevertFailure () throws Exception {
        File f = new File(workDir, "f");
        File[] files = new File[] { f };
        write(f, "init");
        add(f);
        commit(f);
        
        // modify and commit
        write(f, "change");
        add(f);
        GitRevisionInfo commit = getClient(workDir).commit(files, "modification", null, null, NULL_PROGRESS_MONITOR);
        write(f, "local change");
        GitRevertResult result = getClient(workDir).revert(commit.getRevision(),  null, true, NULL_PROGRESS_MONITOR);
        assertEquals("local change", read(f));
        assertEquals(Arrays.asList(f), result.getFailures());
        assertEquals(GitRevertResult.Status.FAILED, result.getStatus());
        assertNull(repository.readMergeCommitMsg());
    }
    
    public void testRevertConflict () throws Exception {
        File f = new File(workDir, "f");
        File[] files = new File[] { f };
        write(f, "init");
        add(f);
        commit(f);
        
        // modify and commit
        write(f, "change");
        add(f);
        GitRevisionInfo commit = getClient(workDir).commit(files, "modification", null, null, NULL_PROGRESS_MONITOR);
        write(f, "local change");
        add(f);
        commit(f);
        GitRevertResult result = getClient(workDir).revert(commit.getRevision(),  null, true, NULL_PROGRESS_MONITOR);
        assertEquals("<<<<<<< OURS\nlocal change\n=======\ninit\n>>>>>>> THEIRS", read(f));
        assertEquals(Arrays.asList(f), result.getConflicts());
        assertEquals(GitRevertResult.Status.CONFLICTING, result.getStatus());
        assertEquals("Revert \"modification\"\n\nThis reverts commit " + commit.getRevision() + ".\n\nConflicts:\n\tf\n", repository.readMergeCommitMsg());
    }
    
    public void testRevertNotIncluded () throws Exception {
        File f = new File(workDir, "f");
        File[] files = new File[] { f };
        write(f, "init");
        add(f);
        commit(f);
        
        getClient(workDir).createBranch("branch", "master", NULL_PROGRESS_MONITOR);
        
        // modify and commit
        write(f, "change");
        add(f);
        GitRevisionInfo commit = getClient(workDir).commit(files, "modification", null, null, NULL_PROGRESS_MONITOR);
        getClient(workDir).checkoutRevision("branch", true, NULL_PROGRESS_MONITOR);
        GitRevertResult result = getClient(workDir).revert(commit.getRevision(),  null, true, NULL_PROGRESS_MONITOR);
        assertEquals(GitRevertResult.Status.NO_CHANGE, result.getStatus());
    }

    public void testRevertNoCommit () throws Exception {
        File f = new File(workDir, "f");
        File[] files = new File[] { f };
        write(f, "init");
        add(f);
        commit(f);
        
        // modify and commit
        write(f, "change");
        add(f);
        GitRevisionInfo commit = getClient(workDir).commit(files, "modification", null, null, NULL_PROGRESS_MONITOR);
        assertEquals("change", read(f));
        GitRevertResult result = getClient(workDir).revert(commit.getRevision(), null, false, NULL_PROGRESS_MONITOR);
        assertEquals("init", read(f));
        assertEquals(commit.getRevision(), getClient(workDir).getBranches(false, NULL_PROGRESS_MONITOR).get("master").getId());
        assertEquals(Arrays.asList(files), Arrays.asList(getClient(workDir).listModifiedIndexEntries(files, NULL_PROGRESS_MONITOR)));
        assertEquals(GitRevertResult.Status.REVERTED_IN_INDEX, result.getStatus());
    }

    public void testRevertMessage () throws Exception {
        File f = new File(workDir, "f");
        File[] files = new File[] { f };
        write(f, "init");
        add(f);
        commit(f);
        
        // modify and commit
        write(f, "change");
        add(f);
        GitRevisionInfo commit = getClient(workDir).commit(files, "modification", null, null, NULL_PROGRESS_MONITOR);
        assertEquals("change", read(f));
        getClient(workDir).revert(commit.getRevision(), "blablabla message", true, NULL_PROGRESS_MONITOR);
        assertEquals("init", read(f));
        assertEquals("blablabla message", getClient(workDir).log("master", NULL_PROGRESS_MONITOR).getFullMessage());
    }
}
