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

package org.netbeans.modules.mercurial;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.util.test.MockLookup;

/**
 *
 * @author ondra
 */
public class FileStatusCacheTest extends AbstractHgTestCase {

    private File workdir;

    public FileStatusCacheTest(String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", new File(getWorkDir().getParentFile(), "userdir").getAbsolutePath());
        super.setUp();
        MockLookup.setLayersAndInstances();
        // create
        workdir = getWorkTreeDir();
        Mercurial.STATUS_LOG.setLevel(Level.FINE);
    }

    public void testNestedRepositoriesRefresh () throws Exception {
        File folder1 = createFolder(workdir, "f1");
        File f1 = createFile(workdir, "file1");
        File f2 = createFile(folder1, "file2");
        commit(workdir);

        getCache().refreshAllRoots(Collections.singletonMap(workdir, Collections.singleton(workdir)));
        File[] files = getCache().listFiles(new File[] {workdir}, FileInformation.STATUS_LOCAL_CHANGE);
        assertModified(files, 0);

        File repo2 = createFolder(folder1, "r2");
        NestedReposLogHandler handler = new NestedReposLogHandler(repo2);
        attachCacheLogHandler(handler);
        HgCommand.doCreate(repo2, Mercurial.getInstance().getLogger(null));
        Mercurial.getInstance().versionedFilesChanged();
        File folder2 = createFolder(repo2, "folder2");
        File f3 = createFile(repo2, "file3");
        File f4 = createFile(folder2, "file4");
        commitIntoRepository(repo2, repo2);
        getCache().refreshAllRoots(Collections.singletonMap(repo2, Collections.singleton(repo2)));
        files = getCache().listFiles(new File[] {repo2}, FileInformation.STATUS_LOCAL_CHANGE);
        assertModified(files, 0);

        write(f1, "hello");
        write(f2, "hello");
        getCache().refreshAllRoots(Collections.singletonMap(workdir, Collections.singleton(workdir)));
        files = getCache().listFiles(new File[] {workdir}, FileInformation.STATUS_LOCAL_CHANGE);
        assertModified(files, 2);

        write(f3, "hello");
        write(f4, "hello");
        getCache().refreshAllRoots(Collections.singletonMap(repo2, Collections.singleton(repo2)));
        files = getCache().listFiles(new File[] {repo2}, FileInformation.STATUS_LOCAL_CHANGE);
        assertModified(files, 2);

        getCache().refreshAllRoots(Collections.singletonMap(workdir, Collections.singleton(workdir)));
        files = getCache().listFiles(new File[] {workdir}, FileInformation.STATUS_LOCAL_CHANGE);
        assertModified(files, 4);

        files = getCache().listFiles(new File[] {repo2}, FileInformation.STATUS_LOCAL_CHANGE);
        assertModified(files, 2);

        getCache().refreshAllRoots(Collections.singletonMap(repo2, Collections.singleton(repo2)));
        files = getCache().listFiles(new File[] {repo2}, FileInformation.STATUS_LOCAL_CHANGE);
        assertModified(files, 2);

        files = getCache().listFiles(new File[] {workdir}, FileInformation.STATUS_LOCAL_CHANGE);
        assertModified(files, 4);

        if (handler.occurances != 1) {
            fail("Expected occurrences 1, was " + handler.occurances);
        }
    }

    private void assertModified (File[] files, int count) {
        LinkedList<File> modifiedFiles = new LinkedList<File>();
        for (File f : files) {
            if (!f.getName().endsWith(".log")) {                        //NOI18N
                modifiedFiles.add(f);
            }
        }
        if (modifiedFiles.size() != count) {
            fail("Modified files: " + modifiedFiles + ", expected size " + count);
        }
    }

    private void attachCacheLogHandler(NestedReposLogHandler handler) throws Exception {
        Field f = null;
        try {
            f = FileStatusCache.class.getDeclaredField("LOG");
        } catch (Exception ex) {
            throw ex;
        }
        f.setAccessible(true);
        Logger log = (Logger) f.get(getCache());
        log.addHandler(handler);
    }

    private class NestedReposLogHandler extends Handler {

        public String nestedRepoMessage;
        public int occurances;
        private final File expectedRepo;

        public NestedReposLogHandler(File repo) {
            this.expectedRepo = repo;
        }

        @Override
        public void publish(LogRecord record) {
            if (record.getMessage().contains("refreshAllRoots: nested repository found:")
                    && record.getParameters() != null
                    && record.getParameters().length == 2
                    && record.getParameters()[1].toString().equals(expectedRepo.getAbsolutePath())) {
                nestedRepoMessage = record.getMessage();
                ++occurances;
            }
        }

        @Override
        public void flush() {

        }

        @Override
        public void close() throws SecurityException {
            
        }

    }
}
