/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
