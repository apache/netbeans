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

package org.netbeans.modules.remote.impl.fs;

import java.util.List;
import junit.framework.Test;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo.FileType;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ShellScriptRunner;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.impl.fs.server.FSSTransportTestAccessor;
import org.netbeans.modules.remote.test.RemoteApiTest;

/**
 *
 */
public class DirectoryReaderTestCase extends RemoteFileTestBase {

    private static class RefEntry {

        public final char fileType;
        public final String access;
        public final String user;
        public final String group;
        public final int size;
        public final String name;
        public final String link;

        public RefEntry(char fileType, String access, String user, String group, int size, String name, String link) {
            this.fileType = fileType;
            this.access = access;
            this.user = user;
            this.group = group;
            this.size = size;
            this.name = name;
            this.link = link;
        }
        
        public boolean isLink() {
            return fileType == 'l';
        }
        
        public boolean isDirectory() {
            return fileType == 'd';
        }
    }

    private RefEntry[] referenceEntries;
    private String script;
    private String remoteDir;

    public DirectoryReaderTestCase(String testName) {
        super(testName);
    }

    public DirectoryReaderTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String user;
        String group;
        assertNotNull(execEnv);
        user = execEnv.getUser();
        if (HostInfoUtils.getHostInfo(execEnv).getOSFamily() == HostInfo.OSFamily.MACOSX) {
            group = "wheel"; // don't know the reason, but mac isn't supported, so it's mostly for my own convenien
        } else {
            group = execute("groups").split(" ")[0];
        }
        remoteDir = mkTempAndRefreshParent(true);

        final String dir_1 = "dir_1";
        final String fifo = "fifo";
        final String dir_with_a_space = "dir with a space";
        final String file_with_a_space = "file with a space";
        final String just_a_file = "just_a_file";
        final String just_a_link = "just_a_link";
        final String link_to_dir = "link_to_dir";
        final String link_with_a_space_to_file_with_a_space = "link with a space to file with a space";
        final String link_to_file_with_a_space = "link_to_file_with_a_space";

        script =
            "umask 0022\n" +
            "cd " + remoteDir + "\n" +
            "echo \"123\" > " + just_a_file + "\n" +
            "echo \"123\" > \""+ file_with_a_space + "\"\n" +
            "mkdir -p \"" + dir_with_a_space + "\"\n" +
            "mkdir -p " + dir_1 + "\n" +
            "ln -s just_a_file "+ just_a_link + "\n" +
            "ln -s dir_1 " + link_to_dir + "\n" +
            "ln -s \"file with a space\" " + link_to_file_with_a_space + "\n" +
            "ln -s \"file with a space\" \"" + link_with_a_space_to_file_with_a_space + "\"\n" +
            "mkfifo " + fifo + "\n";           
//            "chmod 755 " + dir_1 + "\n" +
//            "chmod 644 " + fifo + "\n" +
//            "chmod 755 " + dir_with_a_space + "\n" +
//            "chmod 644 " + file_with_a_space + "\n" +
//            "chmod 644 " + just_a_file + "\n" +
//            "chmod 777 " + just_a_link + "\n" +
//            "chmod 777 " + link_to_dir + "\n" +
//            "chmod 777 " + link_with_a_space_to_file_with_a_space + "\n" +
//            "chmod 777 " + link_to_file_with_a_space + "\n";

        referenceEntries = new RefEntry[] {
            new RefEntry('d', "rwxr-xr-x", user, group, 0, dir_1, null),
            new RefEntry('p', "rw-r--r--", user, group, 0, fifo, null),
            new RefEntry('d', "rwxr-xr-x", user, group, 4, dir_with_a_space, null),
            new RefEntry('-', "rw-r--r--", user, group, 4, file_with_a_space, null),
            new RefEntry('-', "rw-r--r--", user, group, 4, just_a_file, null),
            new RefEntry('l', "rwxrwxrwx", user, group, 0, just_a_link, "just_a_file"),
            new RefEntry('l', "rwxrwxrwx", user, group, 0, link_to_dir, "dir_1"),
            new RefEntry('l', "rwxrwxrwx", user, group, 0, link_with_a_space_to_file_with_a_space, "file with a space"),
            new RefEntry('l', "rwxrwxrwx", user, group, 0, link_to_file_with_a_space, "file with a space")
        };
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (execEnv != null) {
            removeRemoteDirIfNotNull(remoteDir);
        }
    }

    private void prepareDirectory() throws Exception {
        ShellScriptRunner scriptRunner = new ShellScriptRunner(execEnv, script, new LineProcessor() {
            @Override
            public void processLine(String line) {
                System.err.println(line);
            }
            @Override
            public void reset() {}
            @Override
            public void close() {}
        });
        int rc = scriptRunner.execute();
        assertEquals("Error running script", 0, rc);
    }

    @ForAllEnvironments
    public void testDirectoryReaderSftp() throws Exception {
        prepareDirectory();
        List<DirEntry> entries = SftpTransport.getInstance(execEnv).readDirectory(remoteDir).getEntries();
        assertEntriesEqual(referenceEntries, entries, false); // sftp directory reader doesn't recognize FIFO, etc.
    }

    @ForAllEnvironments
    public void testDirectoryReaderFSServer() throws Exception {
        prepareDirectory();
        List<DirEntry> entries = FSSTransportTestAccessor.readDirectory(execEnv, remoteDir);
        assertEntriesEqual(referenceEntries, entries, false); // sftp directory reader doesn't recognize FIFO, etc.
    }

    private void assertEntriesEqual(RefEntry[] refEntries, List<DirEntry> entries, boolean strictTypes) {
        assertEquals("Entries count differs: ", refEntries.length, entries.size());
        for (RefEntry refEntry : refEntries) {
            DirEntry entry = null;
            for (DirEntry e : entries) {
                if (e.getName().equals(refEntry.name)) {
                    entry = e;
                    break;
                }
            }
            assertNotNull("Entry not found for " + refEntry.name, entry);
            assertEquals("isLink() differs for " + refEntry.name, refEntry.isLink(), entry.isLink());
            assertEquals("isDirectory() differs for " + refEntry.name, refEntry.isDirectory(), entry.isDirectory());
            if (strictTypes) {
                assertEquals("File type differs for " + refEntry.name, FileType.fromChar(refEntry.fileType), entry.getFileType());
            }
            //assertEquals("Access differs for " + refEntry.name, refEntry.access, entry.getAccessAsString());
//            assertEquals("Group differs for " + refEntry.name, refEntry.group, entry.getGroup());
            if (!entry.isDirectory() && !entry.isLink()) {
                assertEquals("Size differs for " + refEntry.name, refEntry.size, entry.getSize());
            }

            assertEquals("Link differs for " + refEntry.name, refEntry.link, entry.getLinkTarget());
//            assertEquals("User differs for " + refEntry.name, refEntry.user, entry.getUser());
        }
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(DirectoryReaderTestCase.class);
    }

}
