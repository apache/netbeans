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
