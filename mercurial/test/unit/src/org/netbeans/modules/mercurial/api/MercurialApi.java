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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
