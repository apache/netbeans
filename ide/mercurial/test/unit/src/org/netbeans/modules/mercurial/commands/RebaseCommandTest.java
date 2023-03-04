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
package org.netbeans.modules.mercurial.commands;

import java.io.File;
import java.util.Collections;
import java.util.List;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.mercurial.AbstractHgTestCase;
import org.netbeans.modules.mercurial.commands.RebaseCommand.Result.State;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.util.HgCommand;

/**
 *
 * @author Ondrej Vrabec
 */
public class RebaseCommandTest extends AbstractHgTestCase {

    public RebaseCommandTest (String arg0) {
        super(arg0);
    }
    
    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", new File(getWorkDir().getParentFile(), "userdir").getAbsolutePath());
        super.setUp();
    }

    public void testRebaseNoOp () throws Exception {
        File root = getWorkTreeDir();
        
        File f = new File(root, "file");
        write(f, "init");
        commit(f);
        
        RebaseCommand cmd = new RebaseCommand(root, RebaseCommand.Operation.START, NULL_LOGGER);
        RebaseCommand.Result result = cmd.call();
        assertEquals(State.OK, result.getState());
        assertTrue(cmd.getOutput().contains("nothing to rebase"));
    }

    public void testRebaseSimple () throws Exception {
        File root = getWorkTreeDir();
        
        File f = new File(root, "file");
        write(f, "a\nb\nc");
        commit(f);
        
        HgLogMessage.HgRevision parent = HgCommand.getParent(root, null, null);
        
        // make 1st head
        write(f, "x\nb\nc");
        commit(f);
        HgLogMessage.HgRevision head1 = HgCommand.getParent(root, null, null);
        
        // make 2nd head
        HgCommand.doUpdateAll(root, true, parent.getChangesetId());
        write(f, "a\nb\nz");
        commit(f);
        
        // rebase without parameters
        HgCommand.doUpdateAll(root, true, head1.getChangesetId());
        assertEquals(2, HgCommand.getHeadRevisions(root).size());
        RebaseCommand cmd = new RebaseCommand(root, RebaseCommand.Operation.START, NULL_LOGGER);
        RebaseCommand.Result result = cmd.call();
        assertEquals(State.OK, result.getState());
        assertEquals("x\nb\nz", read(f));
        assertEquals(0, HgCommand.getStatus(root, Collections.<File>singletonList(f), null, null).size());
        assertEquals(1, HgCommand.getHeadRevisions(root).size());
    }

    public void testRebaseParameters () throws Exception {
        File root = getWorkTreeDir();
        
        File f = new File(root, "file");
        write(f, "a\nb\nc");
        commit(f);
        
        HgLogMessage.HgRevision parent = HgCommand.getParent(root, null, null);
        
        // make 1st head
        write(f, "x\nb\nc");
        commit(f);
        HgLogMessage.HgRevision head1 = HgCommand.getParent(root, null, null);
        
        // make 2nd head
        HgCommand.doUpdateAll(root, true, parent.getChangesetId());
        write(f, "a\nb\nz");
        commit(f);
        HgLogMessage.HgRevision head2 = HgCommand.getParent(root, null, null);
        
        // rebase with parameters
        assertEquals(2, HgCommand.getHeadRevisions(root).size());
        RebaseCommand cmd = new RebaseCommand(root, RebaseCommand.Operation.START, NULL_LOGGER)
                .setRevisionDest(head2.getChangesetId())
                .setRevisionBase(head1.getChangesetId());
        RebaseCommand.Result result = cmd.call();
        assertEquals(State.OK, result.getState());
        assertEquals("x\nb\nz", read(f));
        assertEquals(0, HgCommand.getStatus(root, Collections.<File>singletonList(f), null, null).size());
        assertEquals(1, HgCommand.getHeadRevisions(root).size());
    }

    @RandomlyFails
    public void testRebaseParametersSource () throws Exception {
        File root = getWorkTreeDir();
        
        File f = new File(root, "file");
        write(f, "a\nb\nc");
        commit(f);
        
        HgLogMessage.HgRevision parent = HgCommand.getParent(root, null, null);
        
        // make 1st head
        write(f, "x\nb\nc");
        commit(f);
        HgLogMessage.HgRevision head1Parent = HgCommand.getParent(root, null, null);
        write(f, "y\nb\nc");
        commit(f);
        HgLogMessage.HgRevision head1 = HgCommand.getParent(root, null, null);
        
        // make 2nd head
        HgCommand.doUpdateAll(root, true, parent.getChangesetId());
        write(f, "a\nb\nz");
        commit(f);
        HgLogMessage.HgRevision head2 = HgCommand.getParent(root, null, null);
        
        // rebase with parameters - rebase only last commit of head 1
        assertEquals(2, HgCommand.getHeadRevisions(root).size());
        RebaseCommand cmd = new RebaseCommand(root, RebaseCommand.Operation.START, NULL_LOGGER)
                .setRevisionDest(head2.getChangesetId())
                .setRevisionSource(head1.getChangesetId());
        RebaseCommand.Result result = cmd.call();
        assertEquals(State.MERGING, result.getState());
        assertEquals("<<<<<<< local\n" +
            "a\n" +
            "=======\n" +
            "y\n" +
            ">>>>>>> other\nb\nz", read(f));
        
        // resolve conflicts
        write(f, "y\nb\nz");
        HgCommand.markAsResolved(root, f, NULL_LOGGER);
        
        // continue rebase
        cmd = new RebaseCommand(root, RebaseCommand.Operation.CONTINUE, NULL_LOGGER);
        result = cmd.call();
        assertEquals(State.OK, result.getState());
        assertEquals("y\nb\nz", read(f));
        assertEquals(0, HgCommand.getStatus(root, Collections.<File>singletonList(f), null, null).size());
        List<String> heads = HgCommand.getHeadRevisions(root);
        assertEquals(2, heads.size());
        assertTrue(heads.contains(head1Parent.getRevisionNumber()));
    }

    public void testRebaseAbort () throws Exception {
        File root = getWorkTreeDir();
        
        File f = new File(root, "file");
        write(f, "a\nb\nc");
        commit(f);
        
        HgLogMessage.HgRevision parent = HgCommand.getParent(root, null, null);
        
        // make 1st head
        write(f, "x\nb\nc");
        commit(f);
        HgLogMessage.HgRevision head1 = HgCommand.getParent(root, null, null);
        
        // make 2nd head
        HgCommand.doUpdateAll(root, true, parent.getChangesetId());
        write(f, "y\nb\nc");
        commit(f);
        HgLogMessage.HgRevision head2 = HgCommand.getParent(root, null, null);
        
        // rebase without parameters
        HgCommand.doUpdateAll(root, true, head1.getChangesetId());
        assertEquals(2, HgCommand.getHeadRevisions(root).size());
        RebaseCommand cmd = new RebaseCommand(root, RebaseCommand.Operation.START, NULL_LOGGER);
        RebaseCommand.Result result = cmd.call();
        assertEquals(State.MERGING, result.getState());
        assertEquals("<<<<<<< local\n" +
            "y\n" +
            "=======\n" +
            "x\n" +
            ">>>>>>> other\nb\nc", read(f));
        
        // abort rebase
        cmd = new RebaseCommand(root, RebaseCommand.Operation.ABORT, NULL_LOGGER);
        result = cmd.call();
        assertEquals(State.ABORTED, result.getState());
        assertEquals("x\nb\nc", read(f));
        List<String> heads = HgCommand.getHeadRevisions(root);
        assertEquals(2, heads.size());
        assertTrue(heads.contains(head1.getRevisionNumber()));
        assertTrue(heads.contains(head2.getRevisionNumber()));
    }
}
