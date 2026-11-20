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
package org.netbeans.modules.git.ui.rebase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitRebaseInteractiveHandler;
import org.netbeans.libs.git.GitRebaseTodoLine;
import org.netbeans.libs.git.GitRebaseTodoLine.RebaseAction;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitUser;
import org.netbeans.modules.git.AbstractGitTestCase;
import org.netbeans.modules.git.ui.rebase.InteractiveRebasePanel.TodoTableModel;
import org.netbeans.modules.git.utils.GitUtils;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Unit tests for the interactive rebase plan logic: the todo table model
 * (moving rows, the oldest row cannot be squashed or fixup-ed) and the
 * handler applying the plan to the driver's list of steps (actions,
 * dropping, reordering, abbreviated id matching).
 *
 * @author mbien
 */
public class InteractiveRebaseTest extends AbstractGitTestCase {

    private GitRevisionInfo a;
    private GitRevisionInfo b;
    private GitRevisionInfo c;
    private GitRevisionInfo d;

    public InteractiveRebaseTest (String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        GitClient client = getClient(repositoryLocation);
        try {
            a = commit(client, "a.txt", "A: base");
            b = commit(client, "b.txt", "B: second");
            c = commit(client, "c.txt", "C: third");
            d = commit(client, "d.txt", "D: fourth");
        } finally {
            client.release();
        }
    }

    private GitRevisionInfo commit(GitClient client, String file, String message) throws Exception {
        File added = new File(repositoryLocation, file);
        java.nio.file.Files.write(added.toPath(), file.getBytes(UTF_8));
        client.add(new File[] { added }, GitUtils.NULL_PROGRESS_MONITOR);
        GitUser user = new GitUser("author", "author@example.com");
        return client.commit(new File[] { added }, message, user, user, GitUtils.NULL_PROGRESS_MONITOR);
    }

    public void testModelMoveRow() {
        TodoTableModel model = new TodoTableModel();
        model.setRevisions(idsList(b, c, d)); // oldest first
        assertEquals(3, model.getRowCount());
        // the numbers are assigned on the fill (1 is the newest commit) and
        // move with the rows
        assertEquals(3, model.getValueAt(0, 0));
        assertEquals(1, model.getValueAt(2, 0));
        model.moveRow(2, 0); // move d to the top
        assertEquals(d.getRevision(), model.getRevision(0).getRevision());
        assertEquals(b.getRevision(), model.getRevision(1).getRevision());
        assertEquals(c.getRevision(), model.getRevision(2).getRevision());
        assertEquals(1, model.getValueAt(0, 0)); // the numbers move with the rows
        assertEquals(3, model.getValueAt(1, 0));
        assertEquals(2, model.getValueAt(2, 0));
    }

    public void testModelMoveSquashToFirstRowNormalizedToPick() {
        TodoTableModel model = new TodoTableModel();
        model.setRevisions(idsList(b, c, d));
        model.setValueAt(RebaseAction.SQUASH, 1, 1);
        model.moveRow(1, 0); // the squash row moves to the first (oldest) row
        assertEquals(RebaseAction.PICK, model.getAction(0));
    }

    public void testRowStatesDefaultToUnchanged() {
        TodoTableModel model = new TodoTableModel();
        model.setRevisions(idsList(b, c, d));
        assertStates(model,
            TodoTableModel.RowState.UNCHANGED,
            TodoTableModel.RowState.UNCHANGED,
            TodoTableModel.RowState.UNCHANGED);
    }

    /**
     * Dropping the middle commit removes it; the commit built on it gets a
     * new hash, the commits below it (the base does not change) keep
     * theirs.
     */
    public void testRowStateDrop() {
        TodoTableModel model = new TodoTableModel();
        model.setRevisions(idsList(b, c, d));
        model.setValueAt(RebaseAction.DROP, 1, 1);
        assertStates(model,
            TodoTableModel.RowState.UNCHANGED,
            TodoTableModel.RowState.REMOVED,
            TodoTableModel.RowState.MODIFIED);
    }

    /**
     * Squashing the newest commit into the one below folds it away; the
     * absorbing row gets a new hash, the commits below keep theirs.
     */
    public void testRowStateSquash() {
        TodoTableModel model = new TodoTableModel();
        model.setRevisions(idsList(b, c, d));
        model.setValueAt(RebaseAction.SQUASH, 2, 1);
        assertStates(model,
            TodoTableModel.RowState.UNCHANGED,
            TodoTableModel.RowState.MODIFIED,
            TodoTableModel.RowState.FOLDED);
    }

    /**
     * Moving the newest commit to the front rebuilds the whole chain:
     * every commit gets a new hash.
     */
    public void testRowStateReorderEverything() {
        TodoTableModel model = new TodoTableModel();
        model.setRevisions(idsList(b, c, d));
        model.moveRow(2, 0);
        assertStates(model,
            TodoTableModel.RowState.MODIFIED,
            TodoTableModel.RowState.MODIFIED,
            TodoTableModel.RowState.MODIFIED);
    }

    /**
     * Swapping the two middle commits only changes their hash and the hash
     * of the commit built on them; the oldest commit still sits on the same
     * branch.
     */
    public void testRowStateReorderPartial() {
        TodoTableModel model = new TodoTableModel();
        model.setRevisions(idsList(b, c, d));
        model.moveRow(1, 2);
        assertStates(model,
            TodoTableModel.RowState.UNCHANGED,
            TodoTableModel.RowState.MODIFIED,
            TodoTableModel.RowState.MODIFIED);
    }

    private static void assertStates(TodoTableModel model, TodoTableModel.RowState... states) {
        for (int i = 0; i < states.length; i++) {
            assertEquals(states[i], model.getRowState(i));
        }
    }

    public void testModelCellEditable() {
        TodoTableModel model = new TodoTableModel();
        model.setRevisions(idsList(b, c, d));
        assertFalse(model.isCellEditable(0, 1)); // the first (oldest) row
        assertTrue(model.isCellEditable(1, 1));
        assertTrue(model.isCellEditable(2, 1));
        assertFalse(model.isCellEditable(0, 0)); // the number column
        assertFalse(model.isCellEditable(1, 2));
        assertFalse(model.isCellEditable(1, 3));
    }

    public void testHandlerAppliesTheActions() {
        GitRebaseInteractiveHandler handler = new InteractiveRebaseDialog.InteractiveRebaseHandler(
                repositoryLocation, plan(
                    b, RebaseAction.PICK,
                    c, RebaseAction.FIXUP,
                    d, RebaseAction.SQUASH));
        List<GitRebaseTodoLine> steps = stepList(b, c, d);
        handler.prepareSteps(steps);
        assertEquals(3, steps.size());
        assertCommit(steps.get(0), b);
        assertCommit(steps.get(1), c);
        assertCommit(steps.get(2), d);
        assertEquals(RebaseAction.PICK, steps.get(0).getAction());
        assertEquals(RebaseAction.FIXUP, steps.get(1).getAction());
        assertEquals(RebaseAction.SQUASH, steps.get(2).getAction());
    }

    public void testHandlerReordersTheSteps() {
        // the table order after a move: d (the newest commit) on top,
        // b (the oldest) at the bottom
        GitRebaseInteractiveHandler handler = new InteractiveRebaseDialog.InteractiveRebaseHandler(
                repositoryLocation, plan(
                    d, RebaseAction.PICK,
                    b, RebaseAction.PICK,
                    c, RebaseAction.PICK));
        List<GitRebaseTodoLine> steps = stepList(b, c, d);
        handler.prepareSteps(steps);
        assertEquals(3, steps.size());
        assertCommit(steps.get(0), d);
        assertCommit(steps.get(1), b);
        assertCommit(steps.get(2), c);
    }

    /**
     * The handler marks the dropped step with the DROP action; the
     * rebase driver removes such a step from its list of steps.
     */
    public void testHandlerMarksTheDroppedStep() {
        GitRebaseInteractiveHandler handler = new InteractiveRebaseDialog.InteractiveRebaseHandler(
                repositoryLocation, plan(
                    b, RebaseAction.PICK,
                    c, RebaseAction.DROP,
                    d, RebaseAction.PICK));
        List<GitRebaseTodoLine> steps = stepList(b, c, d);
        handler.prepareSteps(steps);
        assertEquals(3, steps.size());
        assertCommit(steps.get(0), b);
        assertCommit(steps.get(1), c);
        assertCommit(steps.get(2), d);
        assertEquals(RebaseAction.PICK, steps.get(0).getAction());
        assertEquals(RebaseAction.DROP, steps.get(1).getAction());
        assertEquals(RebaseAction.PICK, steps.get(2).getAction());
    }

    public void testHandlerMarksEveryStepDropped() {
        GitRebaseInteractiveHandler handler = new InteractiveRebaseDialog.InteractiveRebaseHandler(
                repositoryLocation, plan(
                    b, RebaseAction.DROP,
                    c, RebaseAction.DROP,
                    d, RebaseAction.DROP));
        List<GitRebaseTodoLine> steps = stepList(b, c, d);
        handler.prepareSteps(steps);
        assertEquals(3, steps.size());
        for (GitRebaseTodoLine step : steps) {
            assertEquals(RebaseAction.DROP, step.getAction());
        }
    }

    private static void assertCommit(GitRebaseTodoLine step, GitRevisionInfo revision) {
        assertEquals(revision.getRevision().substring(0, 10), step.getCommit());
    }

    private static List<GitRevisionInfo> idsList(GitRevisionInfo... revisions) {
        List<GitRevisionInfo> list = new ArrayList<GitRevisionInfo>();
        for (GitRevisionInfo revision : revisions) {
            list.add(revision);
        }
        return list;
    }

    /**
     * A plan with the given commits in the given order and actions, the
     * commits keep their original messages.
     */
    private static RebasePlan plan(Object... keyValues) {
        List<RebasePlan.Row> rows = new ArrayList<RebasePlan.Row>();
        for (int i = 0; i < keyValues.length; i += 2) {
            GitRevisionInfo revision = (GitRevisionInfo) keyValues[i];
            rows.add(new RebasePlan.Row(revision.getRevision(), (RebaseAction) keyValues[i + 1],
                    revision.getFullMessage(), null, null));
        }
        return RebasePlan.of(rows);
    }

    /**
     * The driver's list of steps: the commits in oldest-first order with
     * abbreviated ids, like the git-rebase-todo file.
     */
    private static List<GitRebaseTodoLine> stepList(GitRevisionInfo... revisions) {
        List<GitRebaseTodoLine> steps = new ArrayList<GitRebaseTodoLine>();
        for (GitRevisionInfo revision : revisions) {
            steps.add(new GitRebaseTodoLine(
                    RebaseAction.PICK,
                    revision.getRevision().substring(0, 10),
                    revision.getShortMessage()));
        }
        return steps;
    }

    /**
     * The settled message of a reworded commit replaces the original
     * message when the driver asks for the modified message of the commit.
     */
    public void testHandlerRewordMessage() {
        RebasePlan plan = RebasePlan.of(List.of(
                new RebasePlan.Row(b.getRevision(), RebaseAction.REWORD, b.getFullMessage(), "B: reworded", null)));
        InteractiveRebaseDialog.InteractiveRebaseHandler handler =
                new InteractiveRebaseDialog.InteractiveRebaseHandler(repositoryLocation, plan);
        assertEquals("B: reworded", handler.modifyCommitMessage(b.getFullMessage()));
        // the message of a commit without a settled message is kept
        assertEquals(c.getFullMessage(), handler.modifyCommitMessage(c.getFullMessage()));
    }

    /**
     * The settled message of a run of squashed commits replaces the combined
     * message the driver composes for the run; a combined message that does
     * not match any run is kept.
     */
    public void testHandlerSquashRunMessage() {
        RebasePlan plan = RebasePlan.of(List.of(
                new RebasePlan.Row(b.getRevision(), RebaseAction.PICK, b.getFullMessage(), null, "combined message"),
                new RebasePlan.Row(c.getRevision(), RebaseAction.SQUASH, c.getFullMessage(), null, null),
                new RebasePlan.Row(d.getRevision(), RebaseAction.FIXUP, d.getFullMessage(), null, null)));
        InteractiveRebaseDialog.InteractiveRebaseHandler handler =
                new InteractiveRebaseDialog.InteractiveRebaseHandler(repositoryLocation, plan);
        // the combined message of the driver: the count of the run, the
        // message of the head commit and the messages of the squashed rows
        String combined = "# This is a combination of 3 commits.\n"
                + b.getFullMessage()
                + "\n# This is the 2nd commit message:\n" + c.getFullMessage();
        assertEquals("combined message", handler.modifyCommitMessage(combined));
        // a combined message of a run of a different size does not match
        String other = "# This is a combination of 2 commits.\n"
                + b.getFullMessage() + "\n# x:\n" + c.getFullMessage();
        assertEquals(other, handler.modifyCommitMessage(other));
    }

    /**
     * The plan persisted in the rebase folder is read back: the order of
     * the rows, the actions and the settled messages are kept.
     */
    public void testPlanRoundTrip() throws Exception {
        RebasePlan plan = RebasePlan.of(List.of(
                new RebasePlan.Row(b.getRevision(), RebaseAction.REWORD, b.getFullMessage(), "B: reworded", null),
                new RebasePlan.Row(c.getRevision(), RebaseAction.SQUASH, c.getFullMessage(), null, null),
                new RebasePlan.Row(d.getRevision(), RebaseAction.DROP, d.getFullMessage(), null, "run message")));
        File folder = new File(getWorkDir(), "rebase-merge");
        assertTrue(folder.mkdirs() || folder.isDirectory());
        plan.persistIfAbsent(folder);
        RebasePlan read = RebasePlan.read(folder);
        assertNotNull(read);
        assertEquals(3, read.getRows().size());
        assertEquals(b.getRevision(), read.getRows().get(0).id());
        assertEquals(RebaseAction.REWORD, read.getRows().get(0).action());
        assertEquals(b.getFullMessage(), read.getRows().get(0).originalMessage());
        assertEquals("B: reworded", read.getRows().get(0).settledMessage());
        assertNull(read.getRows().get(0).settledRunMessage());
        assertEquals(RebaseAction.SQUASH, read.getRows().get(1).action());
        assertNull(read.getRows().get(1).settledMessage());
        assertEquals(RebaseAction.DROP, read.getRows().get(2).action());
        assertEquals("run message", read.getRows().get(2).settledRunMessage());
        // the file is written once, a second persist does not overwrite it
        plan.persistIfAbsent(folder);
        assertEquals(3, RebasePlan.read(folder).getRows().size());
        // a folder without a plan returns null
        File empty = new File(getWorkDir(), "empty");
        assertTrue(empty.mkdirs() || empty.isDirectory());
        assertNull(RebasePlan.read(empty));
    }

    /**
     * The helpers of the dialog that compute the runs of the plan.
     */
    public void testRunHelpers() {
        TodoTableModel model = new TodoTableModel();
        model.setRevisions(idsList(b, c, d));
        model.setValueAt(RebaseAction.SQUASH, 1, 1);
        model.setValueAt(RebaseAction.FIXUP, 2, 1);
        assertEquals(2, InteractiveRebaseDialog.runEnd(0, model));
        assertEquals(2, InteractiveRebaseDialog.runEnd(1, model));
        assertEquals(2, InteractiveRebaseDialog.runEnd(2, model));
        assertTrue(InteractiveRebaseDialog.runContainsSquash(0, 2, model));
        // the run of the row 1 (the head) only contains the fixup row
        assertFalse(InteractiveRebaseDialog.runContainsSquash(1, 2, model));
        assertFalse(InteractiveRebaseDialog.runContainsSquash(2, 2, model));
        String combined = InteractiveRebaseDialog.combinedSquashMessage(model, 1, 2);
        assertTrue(combined.contains("# This is a combination of 2 commits"));
        assertTrue(combined.contains(c.getFullMessage()));
        assertTrue(combined.contains(d.getFullMessage()));
        // the head message is not the message of the head commit, the
        // combined message is
        assertFalse(combined.contains(b.getFullMessage()));
        assertEquals("st", InteractiveRebaseDialog.ordinal(1));
        assertEquals("nd", InteractiveRebaseDialog.ordinal(2));
        assertEquals("rd", InteractiveRebaseDialog.ordinal(3));
        assertEquals("th", InteractiveRebaseDialog.ordinal(4));
    }
}
