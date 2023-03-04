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
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.modules.mercurial.ui.ignore.IgnoreAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/**
 *
 * @author tomas
 */
public class IgnoresTest extends AbstractHgTestCase {

    public IgnoresTest(String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", new File(new File(getWorkDirPath()).getParentFile(), "userdir").getAbsolutePath());
        super.setUp();        
        MockLookup.setLayersAndInstances();
        // create
        FileObject fo = FileUtil.toFileObject(getWorkTreeDir());
    }

    // ignore patterns - issue 171378 - should pass
    public void testIgnores () throws IOException {
        File workDir = getWorkTreeDir();
        File ignoreFile = new File(getDataDir().getAbsolutePath() + "/ignore/hgignore");
        File toFile = new File(workDir, ".hgignore");
        ignoreFile.renameTo(toFile);

        File ignoredFolder = new File(workDir, "ignoredFolderLevel1");
        ignoredFolder.mkdirs();
        FileInformation info = getCache().getCachedStatus(ignoredFolder);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);

        File ignoredFile = new File(ignoredFolder, "file");
        ignoredFile.createNewFile();
        info = getCache().getCachedStatus(ignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);

        File unignoredFile = new File(workDir, "file");
        unignoredFile.createNewFile();
        info = getCache().getCachedStatus(unignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) == 0);


        File unignoredFolder = new File(workDir, "unignoredFolderLevel1");
        unignoredFolder.mkdirs();
        info = getCache().getCachedStatus(unignoredFolder);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) == 0);

        ignoredFolder = new File(unignoredFolder, "ignoredFolderLevel2");
        ignoredFolder.mkdirs();
        info = getCache().getCachedStatus(ignoredFolder);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);

        ignoredFolder = new File(unignoredFolder, "ignoredFolderLevel2_2");
        ignoredFolder.mkdirs();
        info = getCache().getCachedStatus(ignoredFolder);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);

        ignoredFile = new File(ignoredFolder, "ignoredFile");
        ignoredFile.createNewFile();
        info = getCache().getCachedStatus(ignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);

        ignoredFile = new File(workDir, "file.ignore");
        ignoredFile.createNewFile();
        info = getCache().getCachedStatus(ignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);

        ignoredFile = new File(unignoredFolder, "file.ignore");
        ignoredFile.createNewFile();
        info = getCache().getCachedStatus(ignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);


        unignoredFolder = new File(unignoredFolder, "project");
        unignoredFolder.mkdirs();
        info = getCache().getCachedStatus(unignoredFolder);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) == 0);

        unignoredFile = new File(unignoredFolder, "project");
        unignoredFile.createNewFile();
        info = getCache().getCachedStatus(unignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) == 0);

        ignoredFile = new File(workDir, ".project");
        ignoredFile.createNewFile();
        info = getCache().getCachedStatus(ignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);

        ignoredFile = new File(unignoredFolder, ".project");
        ignoredFile.createNewFile();
        info = getCache().getCachedStatus(ignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0);

        unignoredFile = new File(workDir, "file.ignore2");
        unignoredFile.createNewFile();
        info = getCache().getCachedStatus(unignoredFile);
        assertTrue((info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) == 0);
    }
    
    public void testIgnoreAction () throws Exception {
        File workDir = getWorkTreeDir();
        new File(workDir, ".hgignore").delete();
        File folderA = new File(workDir, "folderA");
        File folderAA = new File(folderA, "folderAA");
        File folderAB = new File(folderA, "folderAB");
        File fileAAA = new File(folderAA, "fileAAA");
        File fileAAB = new File(folderAA, "fileAAB");
        File fileABA = new File(folderAB, "fileABA");
        File fileABB = new File(folderAB, "fileABB");
        folderAA.mkdirs();
        folderAB.mkdirs();
        fileAAA.createNewFile();
        fileAAB.createNewFile();
        fileABA.createNewFile();
        fileABB.createNewFile();

        getCache().refreshAllRoots(Collections.singleton(workDir));

        Set<File> ignoredFiles = new HashSet<File>();
        File[] parentFiles = new File[] { workDir };
        // ignoredFiles is empty
        assertIgnoreStatus(parentFiles, ignoredFiles);
        // ignoring folderAA and all its descendants
        toggleIgnore(folderAA, ignoredFiles);
        ignoredFiles.addAll(getFiles(folderAA));
        assertIgnoreStatus(parentFiles, ignoredFiles);
        // ignoring folderA and all its descendants
        toggleIgnore(folderA, ignoredFiles);
        ignoredFiles.addAll(getFiles(folderA));
        assertIgnoreStatus(parentFiles, ignoredFiles);
        // unignoring folderAA and all its descendants - but has no effect since folderA is still ignored
        toggleIgnore(folderAA, ignoredFiles);
        assertIgnoreStatus(parentFiles, ignoredFiles);
        // unignoring folderA
        toggleIgnore(folderA, ignoredFiles);
        ignoredFiles.removeAll(getFiles(folderA));
        assertIgnoreStatus(parentFiles, ignoredFiles);
        // ignoring folderAB and all its descendants
        toggleIgnore(folderAB, ignoredFiles);
        ignoredFiles.addAll(getFiles(folderAB));
        assertIgnoreStatus(parentFiles, ignoredFiles);
        Thread.sleep(2000); // time for refresh
        // ignoring folderA and all its descendants
        toggleIgnore(folderA, ignoredFiles);
        ignoredFiles.addAll(getFiles(folderA));
        assertIgnoreStatus(parentFiles, ignoredFiles);
        Thread.sleep(2000); // time for refresh
        // unignoring folderA and all its descendants - folder AB remains ignored
        toggleIgnore(folderA, ignoredFiles);
        ignoredFiles.removeAll(getFiles(folderA));
        ignoredFiles.addAll(getFiles(folderAB));
        assertIgnoreStatus(parentFiles, ignoredFiles);
        Thread.sleep(2000); // time for refresh
        // unignoring folderAB and all its descendants - no file is ignored
        toggleIgnore(folderAB, ignoredFiles);
        ignoredFiles.removeAll(getFiles(folderAB));
        assertIgnoreStatus(parentFiles, ignoredFiles);

        // bug #187304
        ignoredFiles.clear();
        File obscureFile = new File(folderA, "This + File + Might + Crash + Mercurial");
        obscureFile.createNewFile();
        getCache().refreshAllRoots(Collections.singleton(workDir));
        // ignoring the file
        toggleIgnore(obscureFile, ignoredFiles);
        ignoredFiles.add(obscureFile);
        assertIgnoreStatus(parentFiles, ignoredFiles);
        // unignoring the file
        toggleIgnore(obscureFile, ignoredFiles);
        ignoredFiles.clear();
        assertIgnoreStatus(parentFiles, ignoredFiles);
    }
    
    public void testRefreshPatternsAfterExternalModification () throws Exception {
        File workDir = getWorkTreeDir();
        File ignoreFile = new File(workDir, ".hgignore");
        ignoreFile.delete();
        File file = new File(workDir, "ignored");
        File file2 = new File(workDir, "ignored2");
        write(file, "aaa");
        write(file2, "aaa");
        
        getCache().refreshAllRoots(Collections.singleton(workDir));
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCache().getCachedStatus(file).getStatus());
        assertIgnoreStatus(file, Collections.<File>emptySet());
        
        Thread.sleep(1100);
        write(ignoreFile, "^ignored$");
        
        getCache().refreshAllRoots(Collections.singleton(workDir));
        assertIgnoreStatus(file, Collections.singleton(file));
        
        Thread.sleep(1100);
        write(ignoreFile, "^ignored$\n^ignored2$\n");
        
        getCache().refreshAllRoots(Collections.singleton(workDir));
        assertIgnoreStatus(file, new HashSet<File>(Arrays.asList(file, file2)));
        
        Thread.sleep(1100);
        ignoreFile.delete();
        
        getCache().refreshAllRoots(Collections.singleton(workDir));
        assertIgnoreStatus(file, Collections.<File>emptySet());
    }

    private void assertIgnoreStatus (File[] parents, Set<File> ignoredFiles) throws InterruptedException {
        for (File parent : parents) {
            assertIgnoreStatus(parent, ignoredFiles);
            File[] files = parent.listFiles();
            if (files != null) {
                assertIgnoreStatus(files, ignoredFiles);
            }
        }
    }

    private void assertIgnoreStatus (File file, Set<File> ignoredFiles) throws InterruptedException {
        FileInformation info = getCache().getCachedStatus(file);
        String msg = null;
        for (int i = 0; i < 100; ++i) {
            int status = info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
            msg = null;
            if (expectedIgnored(file, ignoredFiles)) {
                if (status == 0) {
                    msg = "Supposed to be ignored: " + file.getAbsolutePath();
                }
            } else {
                if (status != 0) {
                    msg = "Supposed to be normal: " + file.getAbsolutePath();
                }
            }
            if (msg == null) {
                break;
            } else {
                Thread.sleep(200);
            }
        }
        assertNull(msg, msg);
    }

    private boolean expectedIgnored (File file, Set<File> ignoredFiles) {
        File parent = file;
        while (parent != null && !ignoredFiles.contains(parent)) {
            parent = parent.getParentFile();
        }
        return ignoredFiles.contains(parent);
    }

    private void toggleIgnore (File folder, Set<File> ignoredFiles) throws InterruptedException {
        Logger logger = Logger.getLogger("org.netbeans.modules.mercurial.fileStatusCacheNewGeneration");
        logger.setLevel(Level.ALL);
        LogHandler handler = new LogHandler(folder);
        logger.addHandler(handler);
        TestIgnoreAction tia = SystemAction.get(TestIgnoreAction.class);
        tia.performContextAction(new Node[] { new AbstractNode(Children.LEAF, Lookups.singleton(folder)) });
        synchronized (handler) {
            if (!handler.flag) {
                handler.wait(20000);
            }
        }
        assert handler.flag : "Ignore action failed";
        logger.removeHandler(handler);
    }

    private Set<File> getFiles (File file) {
        Set<File> files = new HashSet<File>();
        files.add(file);
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                files.addAll(getFiles(child));
            }
        }
        return files;
    }

    public static class TestIgnoreAction extends IgnoreAction {

        @Override
        public void performContextAction(Node[] nodes) {
            super.performContextAction(nodes);
        }

    }

    private class LogHandler extends Handler {
        private final File expectedFile;
        private boolean flag;

        private LogHandler(File folderAA) {
            this.expectedFile = folderAA;
        }

        @Override
        public void publish(LogRecord record) {
            if (record.getMessage().contains("refreshIgnores: File {0} refreshed") && record.getParameters().length > 0 && expectedFile.equals(record.getParameters()[0])) {
                synchronized (this) {
                    flag = true;
                    notify();
                }
            }
        }

        @Override
        public void flush() {
            //
        }

        @Override
        public void close() throws SecurityException {
            //
        }

    }
}
