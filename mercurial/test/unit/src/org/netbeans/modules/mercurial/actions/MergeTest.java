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

package org.netbeans.modules.mercurial.actions;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.modules.mercurial.AbstractHgTestCase;
import org.netbeans.modules.mercurial.ui.merge.MergeAction;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.netbeans.modules.mercurial.util.HgCommand;

/**
 *
 * @author ondra
 */
public class MergeTest extends AbstractHgTestCase {

    public MergeTest(String arg0) {
        super(arg0);
    }

    @Override
    public void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDir().getParentFile().getAbsolutePath());
        super.setUp();
    }

    public void testDetectConflicts () throws Exception {
        final File newRepo = new File(getWorkDir(), "repo");
        List<File> repoAsList = Collections.singletonList(newRepo);
        commit(getWorkTreeDir());
        HgCommand.doClone(getWorkTreeDir(), newRepo, NULL_LOGGER);

        // create a file in original repo
        File mainFol = createFolder("folder2");
        File mainFile;
        commit(mainFile = createFile(mainFol, "file2"));
        // fetch the changes to the clone
        HgCommand.doFetch(newRepo, new HgURL(getWorkTreeDir()), null, true, NULL_LOGGER);

        // do changes in the default repo
        write(mainFile, "a");
        commit(mainFile);
        File fileInOtherRepo = new File(new File(newRepo, mainFol.getName()), mainFile.getName());
        write(fileInOtherRepo, "b");
        HgCommand.doCommit(newRepo, Collections.singletonList(fileInOtherRepo), "conflict", NULL_LOGGER);
        // fetch
        final List<String> list = HgCommand.doFetch(newRepo, new HgURL(getWorkTreeDir()), null, true, NULL_LOGGER);
        assertFalse(list.isEmpty());
        Logger LOG = Logger.getLogger(MergeAction.class.getName());
        final Set<String> conflictedPaths = new HashSet<String>();
        final boolean[] conflictsFound = new boolean[1];
        LOG.setLevel(Level.ALL);
        LOG.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (record.getMessage().equals("Conflicts detected: {0}")) {
                    conflictsFound[0] = true;
                } else if (record.getMessage().equals("File {0} in conflict")) {
                    conflictedPaths.add((String) record.getParameters()[0]);
                }
            }
            @Override
            public void flush() {
            }
            @Override
            public void close() throws SecurityException {
            }
        });
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                MergeAction.handleMergeOutput(newRepo, list, NULL_LOGGER, false);
            }
        });
        t.start();
        for (int i = 0; i < 100; ++i) {
            if (conflictsFound[0]) {
                break;
            }
            Thread.sleep(100);
        }
        assertTrue(conflictsFound[0]);
        assertEquals(Collections.singleton(fileInOtherRepo.getAbsolutePath()), conflictedPaths);
    }
}
