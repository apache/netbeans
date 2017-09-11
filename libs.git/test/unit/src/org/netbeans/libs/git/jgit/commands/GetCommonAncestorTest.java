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
import org.eclipse.jgit.lib.Constants;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitMergeResult;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;

/**
 *
 * @author ondra
 */
public class GetCommonAncestorTest extends AbstractGitTestCase {
    private File workDir;
    private static final String BRANCH_NAME = "new_branch";

    public GetCommonAncestorTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
    }

    public void testGetBaseRevisionMerge () throws Exception {
        File f = new File(workDir, "f");
        write(f, "a\nb\nc");
        File[] files = new File[] { f };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        write(f, "a\nb\nc\n");
        add(files);
        GitRevisionInfo revisionBase = client.commit(files, "base revision", null, null, NULL_PROGRESS_MONITOR);
        
        client.createBranch("b", "master", NULL_PROGRESS_MONITOR);
        client.checkoutRevision("b", true, NULL_PROGRESS_MONITOR);
        
        write(f, "modification on branch\nb\nc\n");
        add(files);
        
        client.checkoutRevision("master", true, NULL_PROGRESS_MONITOR);
        write(f, "a\nb\nmodification on master\n");
        add(files);
        client.commit(files, "modification on master", null, null, NULL_PROGRESS_MONITOR);
        
        GitRevisionInfo revisionMerge = client.log(client.merge("b", NULL_PROGRESS_MONITOR).getNewHead(), NULL_PROGRESS_MONITOR);
        
        GitRevisionInfo revision = client.getCommonAncestor(revisionMerge.getParents(), NULL_PROGRESS_MONITOR);
        assertRevisions(revisionBase, revision);
    }
    
    public void testGetBaseRevisionSimpleCommit () throws Exception {
        File f = new File(workDir, "f");
        write(f, "init");
        File[] files = new File[] { f };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        write(f, "modification");
        add(files);
        GitRevisionInfo commit = client.commit(files, "modification", null, null, NULL_PROGRESS_MONITOR);
        GitRevisionInfo revision = client.getCommonAncestor(new String[] { commit.getRevision() }, NULL_PROGRESS_MONITOR);
        assertRevisions(commit, revision);
    }
    
    public void testGetBaseRevisionCommitsInRow () throws Exception {
        File f = new File(workDir, "f");
        write(f, "init");
        File[] files = new File[] { f };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        write(f, "modification 1");
        add(files);
        GitRevisionInfo commit1 = client.commit(files, "modification 1", null, null, NULL_PROGRESS_MONITOR);
        
        write(f, "modification 2");
        add(files);
        GitRevisionInfo commit2 = client.commit(files, "modification 2", null, null, NULL_PROGRESS_MONITOR);
        
        write(f, "modification 3");
        add(files);
        GitRevisionInfo commit3 = client.commit(files, "modification 3", null, null, NULL_PROGRESS_MONITOR);
        GitRevisionInfo revision = client.getCommonAncestor(new String[] { commit1.getRevision(), commit2.getRevision(), commit3.getRevision() }, NULL_PROGRESS_MONITOR);
        assertRevisions(commit1, revision);
    }
    
    public void testGetBaseCrissCross_232904 () throws Exception {
        File f1 = new File(workDir, "f1");
        File f2 = new File(workDir, "f2");
        File f3 = new File(workDir, "f3");
        write(f1, "initial content");
        GitClient client = getClient(workDir);
        File[] files = new File[] { f1, f2, f3 };
        client.add(files, NULL_PROGRESS_MONITOR);
        GitRevisionInfo initial = client.commit(files, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        
        client.createBranch(BRANCH_NAME, "master", NULL_PROGRESS_MONITOR);
        
        // change on master
        write(f1, Constants.MASTER);
        client.add(files, NULL_PROGRESS_MONITOR);
        client.commit(files, "master commit", null, null, NULL_PROGRESS_MONITOR);
        GitRevisionInfo masterCommit = client.log("master", NULL_PROGRESS_MONITOR);
        
        // change on branch
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f2, BRANCH_NAME);
        client.add(files, NULL_PROGRESS_MONITOR);
        client.commit(files, "branch commit", null, null, NULL_PROGRESS_MONITOR);
        GitRevisionInfo branchCommit = client.log(BRANCH_NAME, NULL_PROGRESS_MONITOR);
        
        // merge last master commit (not merge) into branch
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        client.merge(masterCommit.getRevision(), NULL_PROGRESS_MONITOR);
        
        // merge last branch commit (not merge) into master
        client.checkoutRevision(Constants.MASTER, true, NULL_PROGRESS_MONITOR);
        client.merge(branchCommit.getRevision(), NULL_PROGRESS_MONITOR);
        
        GitRevisionInfo ancestor = client.getCommonAncestor(new String[] { Constants.MASTER, BRANCH_NAME }, NULL_PROGRESS_MONITOR);
        assertEquals(initial.getRevision(), ancestor.getRevision());
    }
    
    public void testGetBaseCrissCrossConflict_244222 () throws Exception {
        File f1 = new File(workDir, "f1");
        File f2 = new File(workDir, "f2");
        File f3 = new File(workDir, "f3");
        write(f1, "initial content");
        GitClient client = getClient(workDir);
        File[] files = new File[] { f1, f2, f3 };
        client.add(files, NULL_PROGRESS_MONITOR);
        GitRevisionInfo initial = client.commit(files, "initial commit", null, null, NULL_PROGRESS_MONITOR);
        
        client.createBranch(BRANCH_NAME, "master", NULL_PROGRESS_MONITOR);
        
        // change on master
        write(f1, Constants.MASTER);
        client.add(files, NULL_PROGRESS_MONITOR);
        client.commit(files, "master commit", null, null, NULL_PROGRESS_MONITOR);
        GitRevisionInfo masterCommit = client.log("master", NULL_PROGRESS_MONITOR);
        
        // change on branch
        client.checkoutRevision(BRANCH_NAME, true, NULL_PROGRESS_MONITOR);
        write(f1, BRANCH_NAME);
        client.add(files, NULL_PROGRESS_MONITOR);
        client.commit(files, "branch commit", null, null, NULL_PROGRESS_MONITOR);
        GitRevisionInfo branchCommit = client.log(BRANCH_NAME, NULL_PROGRESS_MONITOR);
        
        // merge last master commit (not merge) into branch
        GitMergeResult res = client.merge(masterCommit.getRevision(), NULL_PROGRESS_MONITOR);
        assertEquals(GitMergeResult.MergeStatus.CONFLICTING, res.getMergeStatus());
        write(f1, BRANCH_NAME);
        client.add(files, NULL_PROGRESS_MONITOR);
        client.commit(new File[0], "Merge master into branch", null, null, NULL_PROGRESS_MONITOR);
        
        // merge last branch commit (not merge) into master
        client.checkoutRevision(Constants.MASTER, true, NULL_PROGRESS_MONITOR);
        res = client.merge(branchCommit.getRevision(), NULL_PROGRESS_MONITOR);
        assertEquals(GitMergeResult.MergeStatus.CONFLICTING, res.getMergeStatus());
        write(f1, Constants.MASTER);
        client.add(files, NULL_PROGRESS_MONITOR);
        client.commit(new File[0], "Merge branch into master", null, null, NULL_PROGRESS_MONITOR);
        
        GitRevisionInfo ancestor = client.getCommonAncestor(new String[] { Constants.MASTER, BRANCH_NAME }, NULL_PROGRESS_MONITOR);
        assertEquals(initial.getRevision(), ancestor.getRevision());
    }

    private void assertRevisions (GitRevisionInfo expected, GitRevisionInfo info) throws GitException {
        assertEquals(expected.getRevision(), info.getRevision());
        assertEquals(expected.getAuthor().toString(), info.getAuthor().toString());
        assertEquals(expected.getCommitTime(), info.getCommitTime());
        assertEquals(expected.getCommitter().toString(), info.getCommitter().toString());
        assertEquals(expected.getFullMessage(), info.getFullMessage());
        assertEquals(expected.getModifiedFiles().size(), info.getModifiedFiles().size());
        assertEquals(expected.getShortMessage(), info.getShortMessage());
    }

}
