/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.git.ui.rebase;

import java.io.File;
import java.util.List;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitRebaseTodoLine.RebaseAction;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitUser;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.AbstractGitTestCase;
import org.netbeans.modules.git.utils.GitUtils;

/**
 * Headless tests of {@link RebaseSimulator}: the plan is replayed in a
 * temporary repository that borrows the objects of the test repository, the
 * real repository is never modified. The setup is a linear history where the
 * two newer commits touch the same line of the same file: in their original
 * order they apply cleanly, reordering them makes the rebase stop with a
 * conflict.
 *
 * @author mbien
 */
public class RebaseSimulatorTest extends AbstractGitTestCase {

    private static final String SHARED = "shared.txt"; //NOI18N

    private GitRevisionInfo a;
    private GitRevisionInfo b;
    private GitRevisionInfo c;

    public RebaseSimulatorTest (String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        GitClient client = getClient(repositoryLocation);
        try {
            a = commit(client, SHARED, "line1\nline2\n", "A: base");
            b = commit(client, SHARED, "line1\nB\nline2\n", "B: insert B");
            c = commit(client, SHARED, "line1\nC\nline2\n", "C: B becomes C");
        } finally {
            client.release();
        }
    }

    private GitRevisionInfo commit(GitClient client, String file, String content, String message) throws Exception {
        File added = new File(repositoryLocation, file);
        write(added, content);
        client.add(new File[] { added }, GitUtils.NULL_PROGRESS_MONITOR);
        GitUser user = new GitUser("author", "author@example.com");
        return client.commit(new File[] { added }, message, user, user, GitUtils.NULL_PROGRESS_MONITOR);
    }

    /**
     * Replays the plan on the range {@code a}..{@code c} (the commits
     * {@code b} and {@code c}) with a handler built from the given rows.
     */
    private RebaseSimulator.Result simulate(List<RebasePlan.Row> rows) {
        RebasePlan plan = RebasePlan.of(rows);
        return RebaseSimulator.simulate(
                repositoryLocation,
                a.getRevision(), // the base of the range
                c.getRevision(), // the head of the range
                scratch -> new InteractiveRebaseDialog.InteractiveRebaseHandler(scratch, plan),
                new ProgressMonitor.DefaultProgressMonitor()
        );
    }

    private static RebasePlan.Row row(GitRevisionInfo revision, RebaseAction action) {
        return new RebasePlan.Row(revision.getRevision(), action, revision.getFullMessage(), null, null);
    }

    /**
     * The commits in their original order apply cleanly on the base.
     */
    public void testCleanOriginalOrder() {
        RebaseSimulator.Result result = simulate(List.of(row(b, RebaseAction.PICK), row(c, RebaseAction.PICK)));
        assertEquals(RebaseSimulator.Result.Kind.CLEAN, result.kind());
        assertNull(result.stoppedCommit());
        assertTrue(result.conflicts().isEmpty());
    }

    /**
     * Reordering the commits makes the rebase stop with a conflict at the
     * first commit it cannot apply; the conflicting file is reported
     * relative to the root of the repository.
     */
    public void testReorderConflicts() {
        RebaseSimulator.Result result = simulate(List.of(row(c, RebaseAction.PICK), row(b, RebaseAction.PICK)));
        assertEquals(RebaseSimulator.Result.Kind.STOPPED, result.kind());
        assertEquals(c.getRevision(), result.stoppedCommit());
        assertEquals(List.of(SHARED), result.conflicts());
    }

    /**
     * Dropping the commit that would conflict leaves a plan that applies
     * cleanly.
     */
    public void testDropConflictingCommit() {
        RebaseSimulator.Result result = simulate(List.of(row(c, RebaseAction.DROP), row(b, RebaseAction.PICK)));
        assertEquals(RebaseSimulator.Result.Kind.CLEAN, result.kind());
        assertNull(result.stoppedCommit());
    }

    /**
     * The simulation never modifies the real repository: the head of the
     * current branch is the tip of the range, and its history is unchanged.
     */
    public void testRealRepositoryUntouched() throws Exception {
        GitClient client = getClient(repositoryLocation);
        try {
            String headBefore = client.log(new org.netbeans.libs.git.SearchCriteria(), false,
                    new ProgressMonitor.DefaultProgressMonitor())[0].getRevision();
            simulate(List.of(row(c, RebaseAction.PICK), row(b, RebaseAction.PICK)));
            GitRevisionInfo headAfter = client.log(new org.netbeans.libs.git.SearchCriteria(), false,
                    new ProgressMonitor.DefaultProgressMonitor())[0];
            // the branch is unchanged: the head is still the tip of the
            // range and its history is in its original order
            assertEquals(headBefore, headAfter.getRevision());
            assertEquals(c.getRevision(), headAfter.getRevision());
            assertEquals(b.getRevision(), headAfter.getParents()[0]);
            assertEquals(a.getRevision(), b.getParents()[0]);
        } finally {
            client.release();
        }
    }
}