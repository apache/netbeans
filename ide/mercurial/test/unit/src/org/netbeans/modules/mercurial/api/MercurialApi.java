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

package org.netbeans.modules.mercurial.api;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import org.netbeans.modules.mercurial.AbstractHgTestCase;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.util.HgCommand;

/**
 *
 * @author ondra vrabec
 */
public class MercurialApi extends AbstractHgTestCase {

    private File workDir;

    public MercurialApi(String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", new File(getWorkDir().getParentFile(), "userdir").getAbsolutePath());
        super.setUp();

        // create
        workDir = getWorkTreeDir();
    }

    public void testIsRepository () {
        assertEquals(false, Mercurial.isRepository("blablabla"));
        assertEquals(false, Mercurial.isRepository("http://www.yahoo.com/"));
        assertEquals(true, Mercurial.isRepository("http://hg.netbeans.org/cdev/"));
        assertEquals(true, Mercurial.isRepository("https://kenai.com/hg/andunix~libdatabase"));
        assertEquals(true, Mercurial.isRepository("ssh://peterp.czech.sun.com//share/NetBeans/mercurial/AnagramGame"));
    }

    public void testCommit () throws Exception {
        File folder1 = createFolder(workDir, "f1");
        File f1 = createFile(workDir, "file1");
        File f2 = createFile(folder1, "file2");
        commit(workDir);
        HgLogMessage repo1Tip1 = HgCommand.doTip(workDir, NULL_LOGGER);

        File repo2 = createFolder(folder1, "r2");
        HgCommand.doCreate(repo2, NULL_LOGGER);
        org.netbeans.modules.mercurial.Mercurial.getInstance().versionedFilesChanged();
        File folder2 = createFolder(repo2, "f1");
        File f3 = createFile(repo2, "file1");
        File f4 = createFile(folder2, "file2");
        HgCommand.doAdd(repo2, Arrays.asList(new File[] {f3, f4}), NULL_LOGGER);
        HgCommand.doCommit(repo2, Collections.singletonList(repo2), "repo2 commit", NULL_LOGGER);
        HgLogMessage repo2Tip1 = HgCommand.doTip(repo2, NULL_LOGGER);

        write(f1, "repo1 change");
        write(f2, "repo1 change 2");
        write(f3, "repo2 change");
        write(f4, "repo2 change 2");

        Mercurial.commit(new File[] {workDir}, "repo1 commit");
        HgLogMessage repo1Tip2 = HgCommand.doTip(workDir, NULL_LOGGER);
        HgLogMessage repo2Tip2 = HgCommand.doTip(repo2, NULL_LOGGER);
        assertEquals((Integer)(Integer.valueOf(repo1Tip1.getRevisionNumber()) + 1), Integer.valueOf(repo1Tip2.getRevisionNumber()));
        assertEquals(Integer.valueOf(repo2Tip1.getRevisionNumber()), Integer.valueOf(repo2Tip2.getRevisionNumber()));

        Mercurial.commit(new File[] {repo2}, "repo2 commit");
        repo2Tip2 = HgCommand.doTip(repo2, NULL_LOGGER);
        assertEquals((Integer)(Integer.valueOf(repo2Tip1.getRevisionNumber()) + 1), Integer.valueOf(repo2Tip2.getRevisionNumber()));
    }
}
