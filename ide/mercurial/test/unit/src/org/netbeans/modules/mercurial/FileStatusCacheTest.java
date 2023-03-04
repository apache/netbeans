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
