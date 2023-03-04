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
