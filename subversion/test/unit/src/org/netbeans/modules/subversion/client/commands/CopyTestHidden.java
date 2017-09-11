/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion.client.commands;

import org.netbeans.modules.subversion.client.AbstractCommandTestCase;
import java.io.File;
import java.io.InputStream;
import org.netbeans.modules.subversion.utils.TestUtilities;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

/**
 *
 * @author tomas
 */
public class CopyTestHidden extends AbstractCommandTestCase {
    
    public CopyTestHidden(String testName) throws Exception {
        super(testName);
    }
    
    public void testCopyURL2URL() throws Exception {
        testCopyURL2URL("file", "filecopy");
    }

    public void testCopyURL2URLWithAtSign() throws Exception {
        if(!shouldBeTestedWithCurrentClient(true, false)) {
            return;
        }
//        testCopyURL2URL("file1", "@filecopy"); // fails until fixed in svn - http://subversion.tigris.org/issues/show_bug.cgi?id=3416
        testCopyURL2URL("file2", "file@copy");
        testCopyURL2URL("file3", "filecopy@");
    }

    public void testCopyURLWithAtSign2URL() throws Exception {
        if(!shouldBeTestedWithCurrentClient(true, false)) {
            return;
        }
        testCopyURL2URL("@file", "filecopy1");
        testCopyURL2URL("fi@le", "filecopy2");
        testCopyURL2URL("file@", "filecopy3");
    }

    public void testCopyURL2URLInDir() throws Exception {
        testCopyURL2URL("folder/file", "filecopy");
    }

    public void testCopyURL2URLWithAtSignInDir() throws Exception {
        if(!shouldBeTestedWithCurrentClient(true, false)) {
            return;
        }
        //        testCopyURL2URL("folder/file1", "@filecopy"); // fails until fixed in svn - http://subversion.tigris.org/issues/show_bug.cgi?id=3416
        testCopyURL2URL("folder/file2", "file@copy");
        testCopyURL2URL("folder/file3", "filecopy@");
    }

    public void testCopyURLWithAtSign2URLInDir() throws Exception {
        if(!shouldBeTestedWithCurrentClient(true, false)) {
            return;
        }
        testCopyURL2URL("folder/@file", "filecopy1");
        testCopyURL2URL("folder/fi@le", "filecopy2");
        testCopyURL2URL("folder/file@", "filecopy3");
    }

    private void testCopyURL2URL(String srcPath, String targetFileName) throws Exception {
        createAndCommitParentFolders(srcPath);
        File file = createFile(srcPath);
        add(file);
        commit(file);

        File filecopy = createFile(renameFile(srcPath, targetFileName));
        filecopy.delete();

        ISVNClientAdapter c = getNbClient();
        c.copy(getFileUrl(file), getFileUrl(filecopy), "copy", SVNRevision.HEAD);

        ISVNInfo info = getInfo(getFileUrl(filecopy));
        assertNotNull(info);
        assertEquals(getFileUrl(filecopy), TestUtilities.decode(info.getUrl()));

        assertNotifiedFiles(new File[] {});
    }

    public void testCopyURL2URLPrevRevision() throws Exception {
        testCopyURL2URLPrevRevision("file", "filecopy");
    }

    public void testCopyURL2URLWithAtSignPrevRevision() throws Exception {
        if(!shouldBeTestedWithCurrentClient(true, false)) {
            return;
        }
        testCopyURL2URLPrevRevision("file1", "@filecopy"); // fails until fixed in svn - http://subversion.tigris.org/issues/show_bug.cgi?id=3416
        testCopyURL2URLPrevRevision("file2", "file@copy");
        testCopyURL2URLPrevRevision("file3", "filecopy@");
    }

    public void testCopyURLWithAtSign2URLPrevRevision() throws Exception {
        if(!shouldBeTestedWithCurrentClient(true, false)) {
            return;
        }
        testCopyURL2URLPrevRevision("@file", "filecopy1");
        testCopyURL2URLPrevRevision("fi@le", "filecopy2");
        testCopyURL2URLPrevRevision("file@", "filecopy3");
    }

    private void testCopyURL2URLPrevRevision(String srcPath,
                                             String targetFileName) throws Exception {
        createAndCommitParentFolders(srcPath);
        File file = createFile(srcPath);
        write(file, 1);
        add(file);
        commit(file);
        SVNRevision prevRev = getRevision(file);
        write(file, 2);
        commit(getWC());

        File filecopy = createFile(renameFile(srcPath, targetFileName));
        filecopy.delete();

        ISVNClientAdapter c = getNbClient();
        c.copy(getFileUrl(file), getFileUrl(filecopy), "copy", prevRev);

        ISVNLogMessage[] logs = getLog(getFileUrl(filecopy));
        assertEquals(((SVNRevision.Number)prevRev).getNumber() ,logs[0].getChangedPaths()[0].getCopySrcRevision().getNumber());

        InputStream is = getContent(getFileUrl(filecopy));
        assertContents(is, 1);
        assertNotifiedFiles(new File[] {});
    }

    public void testCopyFile2URL() throws Exception {
        testCopyFile2URL("file", "filecopy");
    }

    public void testCopyFile2URLInDir() throws Exception {
        testCopyFile2URL("folder/file", "filecopy");
    }

    public void testCopyFileWithAtSign2URL() throws Exception {
        if(!shouldBeTestedWithCurrentClient(true, false)) {
            return;
        }
        testCopyFile2URL("@file", "filecopy1");
        testCopyFile2URL("fi@le", "filecopy2");
        testCopyFile2URL("file@", "filecopy3");
    }

    public void testCopyFile2URLWithAtSign() throws Exception {
        if(!shouldBeTestedWithCurrentClient(true, false)) {
            return;
        }
        //        testCopyFile2URL("file1", "@filecopy"); // fails until fixed in svn - http://subversion.tigris.org/issues/show_bug.cgi?id=3416
        testCopyFile2URL("file2", "file@copy");
        testCopyFile2URL("file3", "filecopy@");
    }

    public void testCopyFileWithAtSign2URLInDir() throws Exception {
        if(!shouldBeTestedWithCurrentClient(true, false)) {
            return;
        }
        testCopyFile2URL("folder/@file", "filecopy1");
        testCopyFile2URL("folder/fi@le", "filecopy2");
        testCopyFile2URL("folder/file@", "filecopy3");
    }

    public void testCopyFile2URLWithAtSignInDir() throws Exception {
        if(!shouldBeTestedWithCurrentClient(true, false)) {
            return;
        }
        //        testCopyFile2URL("folder/file1", "@filecopy"); // fails until fixed in svn - http://subversion.tigris.org/issues/show_bug.cgi?id=3416
        testCopyFile2URL("folder/file2", "file@copy");
        testCopyFile2URL("folder/file3", "filecopy@");
    }

    private void testCopyFile2URL(String srcPath, String targetFileName) throws Exception {
        createAndCommitParentFolders(srcPath);
        File file = createFile(srcPath);
        add(file);
        commit(file);

        // File filecopy = new File(createFolder(file.getParentFile(), targetFileName), file.getName());
        File filecopy = createFile(renameFile(srcPath, targetFileName));

        ISVNClientAdapter c = getNbClient();
//        c.copy(file, getFileUrl(filecopy), "copy"); XXX is failing with javahl and we don't use it anyway
        c.copy(new File[] {file}, getFileUrl(filecopy), "copy", false, true);

        ISVNInfo info = getInfo(getFileUrl(filecopy));
        assertNotNull(info);
        assertEquals(getFileUrl(filecopy), TestUtilities.decode(info.getUrl()));
        assertNotifiedFiles(new File[] {});
    }

    public void testCopyFile2File() throws Exception {
        testCopyFile2File("file", "filecopy");
    }

    public void testCopyFile2FileInDir() throws Exception {
        testCopyFile2File("folder/file", "filecopy");
    }

    public void testCopyFileWithAtSign2File() throws Exception {
        if(!shouldBeTestedWithCurrentClient(true, false)) {
            return;
        }
        testCopyFile2File("@file", "filecopy1");
        testCopyFile2File("fi@le", "filecopy2");
        testCopyFile2File("file@", "filecopy3");
    }

    public void testCopyFile2FileWithAtSign() throws Exception {
        if(!shouldBeTestedWithCurrentClient(true, false)) {
            return;
        }
        //        testCopyFile2URL("file1", "@filecopy"); // fails until fixed in svn - http://subversion.tigris.org/issues/show_bug.cgi?id=3416
        testCopyFile2File("file2", "file@copy");
        testCopyFile2File("file3", "filecopy@");
    }

    public void testCopyFileWithAtSignInDir2FileInDir() throws Exception {
        if(!shouldBeTestedWithCurrentClient(true, false)) {
            return;
        }
        testCopyFile2File("folder/@file", "filecopy1");
        testCopyFile2File("folder/fi@le", "filecopy2");
        testCopyFile2File("folder/file@", "filecopy3");
    }

    public void testCopyFile2FileWithAtSignInDir() throws Exception {
        if(!shouldBeTestedWithCurrentClient(true, false)) {
            return;
        }
        //        testCopyFile2URL("folder/file1", "@filecopy"); // fails until fixed in svn - http://subversion.tigris.org/issues/show_bug.cgi?id=3416
        testCopyFile2File("folder/file2", "file@copy");
        testCopyFile2File("folder/file3", "filecopy@");
    }

    private void testCopyFile2File(String srcPath, String target) throws Exception {
        createAndCommitParentFolders(srcPath);
        File file = createFile(srcPath);
        add(file);
        commit(file);

        // File filecopy = new File(createFolder(file.getParentFile(), targetFileName), file.getName());
        File filecopy = new File(file.getParentFile(), target);

        ISVNClientAdapter c = getNbClient();
//        c.copy(file, getFileUrl(filecopy), "copy"); XXX is failing with javahl and we don't use it anyway
        c.copy(file, filecopy);

        assertTrue(filecopy.exists());

        ISVNInfo info = getInfo(filecopy);
        assertNotNull(info);
        assertEquals(getFileUrl(filecopy), TestUtilities.decode(info.getUrl()));
        assertNotifiedFiles(new File[] {});
    }

    public void testCopyURL2File() throws Exception {
        testCopyURL2File("file", "filecopy");
    }

    public void testCopyURL2FileInDir() throws Exception {
        testCopyURL2File("folder/file", "filecopy");
    }

    public void testCopyURLWithAtSign2File() throws Exception {
        if(!shouldBeTestedWithCurrentClient(true, false)) {
            return;
        }
        testCopyURL2File("@file", "filecopy1");
        testCopyURL2File("fi@le", "filecopy2");
        testCopyURL2File("file@", "filecopy3");
    }

    public void testCopyURL2FileWithAtSign() throws Exception {
        if(!shouldBeTestedWithCurrentClient(true, false)) {
            return;
        }
        testCopyURL2File("file1", "@filecopy"); // fails until fixed in svn - http://subversion.tigris.org/issues/show_bug.cgi?id=3416
        testCopyURL2File("file2", "file@copy");
        testCopyURL2File("file3", "filecopy@");
    }

    public void testCopyURLWithAtSign2FileInDir() throws Exception {
        if(!shouldBeTestedWithCurrentClient(true, false)) {
            return;
        }
        testCopyURL2File("folder/@file", "filecopy1");
        testCopyURL2File("folder/fi@le", "filecopy2");
        testCopyURL2File("folder/file@", "filecopy3");
    }

    public void testCopyURL2FileWithAtSignInDir() throws Exception {
        if(!shouldBeTestedWithCurrentClient(true, false)) {
            return;
        }
        //        testCopyURL2File("folder/file1", "@filecopy"); // fails until fixed in svn - http://subversion.tigris.org/issues/show_bug.cgi?id=3416
        testCopyURL2File("folder/file2", "file@copy");
        testCopyURL2File("folder/file3", "filecopy@");
    }

    private void testCopyURL2File(String srcPath, String targetFileName) throws Exception {
        createAndCommitParentFolders(srcPath);
        File file = createFile(srcPath);
        add(file);
        commit(file);

        File filecopy = createFile(renameFile(srcPath, targetFileName));
        filecopy.delete();

        ISVNClientAdapter c = getNbClient();
        c.copy(getFileUrl(file), filecopy, SVNRevision.HEAD);

        assertTrue(filecopy.exists());
        assertStatus(SVNStatusKind.ADDED, filecopy);
        if (isSvnkit()) {
            // svnkit does not notify about files
            assertNotifiedFiles(new File[] {});
        } else {
            assertNotifiedFiles(new File[] {filecopy});
        }
    }

    public void testCopyURL2FilePrevRevision() throws Exception {
        testCopyURL2FilePrevRevision("file", "filecopy");
    }

    public void testCopyURL2FileWithAtSignPrevRevision() throws Exception {
        if(!shouldBeTestedWithCurrentClient(true, false)) {
            return;
        }
        testCopyURL2FilePrevRevision("file1", "@filecopy"); // fails until fixed in svn - http://subversion.tigris.org/issues/show_bug.cgi?id=3416
        testCopyURL2FilePrevRevision("file2", "file@copy");
        testCopyURL2FilePrevRevision("file3", "filecopy@");
    }

    public void testCopyURLWithAtSign2FilePrevRevision() throws Exception {
        if(!shouldBeTestedWithCurrentClient(true, false)) {
            return;
        }
        testCopyURL2FilePrevRevision("@file", "filecopy1");
        testCopyURL2FilePrevRevision("fi@le", "filecopy2");
        testCopyURL2FilePrevRevision("file@", "filecopy3");
    }

    private void testCopyURL2FilePrevRevision(String srcPath,
                                              String targetFileName) throws Exception {
        createAndCommitParentFolders(srcPath);
        File file = createFile(srcPath);
        write(file, 1);
        add(file);
        commit(file);
        SVNRevision prevRev = getRevision(file);
        write(file, 2);
        commit(getWC());

        File filecopy = createFile(renameFile(srcPath, targetFileName));
        filecopy.delete();

        ISVNClientAdapter c = getNbClient();
        c.copy(getFileUrl(file), filecopy, prevRev);

        assertContents(filecopy, 1);
        if (isSvnkit()) {
            // svnkit does not notify about files
            assertNotifiedFiles(new File[] {});
        } else {
            assertNotifiedFiles(new File[] {filecopy});
        }
    }
    
}
