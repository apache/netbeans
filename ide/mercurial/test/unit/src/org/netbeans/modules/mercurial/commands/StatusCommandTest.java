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
import java.util.Map;
import org.netbeans.modules.mercurial.AbstractHgTestCase;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.util.HgCommand;

/**
 *
 * @author Ondrej Vrabec
 */
public class StatusCommandTest extends AbstractHgTestCase {

    public StatusCommandTest (String arg0) {
        super(arg0);
    }
    
    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", new File(getWorkDir().getParentFile(), "userdir").getAbsolutePath());
        super.setUp();
    }

    public void testNoModifications () throws Exception {
        File root = getWorkTreeDir();
        
        File f = new File(root, "empty");
        write(f, "init");
        commit(f);
        
        StatusCommand cmd = StatusCommand.create(root, Collections.<File>singletonList(root), false);
        Map<File, FileInformation> statuses = cmd.call();
        assertEquals(0, statuses.size());
    }

    public void testModification () throws Exception {
        File root = getWorkTreeDir();
        
        File f = new File(root, "empty");
        write(f, "init");
        commit(f);
        
        write(f, "change");
        
        StatusCommand cmd = StatusCommand.create(root, Collections.<File>singletonList(root), false);
        Map<File, FileInformation> statuses = cmd.call();
        assertEquals(1, statuses.size());
        assertEquals(FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY, statuses.get(f).getStatus());
    }

    public void testCopy () throws Exception {
        File root = getWorkTreeDir();
        
        File f = new File(root, "empty");
        File copy = new File(root, "copy");
        write(f, "init");
        commit(f);
        
        HgCommand.doCopy(root, f, copy, false, NULL_LOGGER);
        
        StatusCommand cmd = StatusCommand.create(root, Collections.<File>singletonList(root), false);
        Map<File, FileInformation> statuses = cmd.call();
        assertEquals(1, statuses.size());
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, statuses.get(copy).getStatus());
        assertNull(statuses.get(copy).getStatus(copy));
        
        cmd.setDetectCopies(true);
        statuses = cmd.call();
        assertEquals(1, statuses.size());
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, statuses.get(copy).getStatus());
        assertTrue(statuses.get(copy).getStatus(copy).isCopied());
        assertEquals(f, statuses.get(copy).getStatus(copy).getOriginalFile());
    }

    public void testConflicts () throws Exception {
        File root = getWorkTreeDir();
        
        File f = new File(root, "empty");
        write(f, "init");
        commit(f);
        
        HgLogMessage.HgRevision initRev = HgCommand.getParent(root, null, null);
        
        write(f, "modif1");
        commit(f);
        HgLogMessage.HgRevision commit1 = HgCommand.getParent(root, null, null);
        
        HgCommand.doUpdateAll(root, true, initRev.getChangesetId());
        
        write(f, "modif2");
        commit(f);
        
        HgCommand.doMerge(root, commit1.getChangesetId());
        
        StatusCommand cmd = StatusCommand.create(root, Collections.<File>singletonList(root), false);
        Map<File, FileInformation> statuses = cmd.call();
        assertEquals(FileInformation.STATUS_VERSIONED_CONFLICT, statuses.get(f).getStatus());
        
        cmd = StatusCommand.create(root, Collections.<File>singletonList(root), false)
                .setDetectConflicts(true);
        statuses = cmd.call();
        assertEquals(FileInformation.STATUS_VERSIONED_CONFLICT, statuses.get(f).getStatus());
        
        cmd = StatusCommand.create(root, Collections.<File>singletonList(root), false)
                .setDetectConflicts(false);
        statuses = cmd.call();
        assertEquals(FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY, statuses.get(f).getStatus());
    }
}
