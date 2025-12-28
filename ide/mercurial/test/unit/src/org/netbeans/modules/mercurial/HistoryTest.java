/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage.HgRevision;
import org.netbeans.modules.mercurial.ui.log.HgLogMessageChangedPath;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.historystore.Storage;
import org.netbeans.modules.versioning.historystore.StorageManager;
import org.netbeans.modules.versioning.util.Utils;

/**
 *
 * @author ondra
 */
public class HistoryTest extends AbstractHgTestCase {

    private File workdir;

    public HistoryTest (String arg0) {
        super(arg0);
    }
    
    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", new File(getWorkDir().getParentFile(), "userdir").getAbsolutePath());
        super.setUp();
        // create
        workdir = getWorkTreeDir();
        Mercurial.STATUS_LOG.setLevel(Level.FINE);
    }
    
    public void testCache () throws Exception {
        File folder = new File(workdir, "folder");
        folder.mkdirs();
        File file = new File(folder, "file");
        List<File> revisions = prepareVersions(file);
        testContents(revisions, file, false);
    }
    
    public void testCacheAfterRollback () throws Exception {
        File folder = new File(workdir, "folder");
        folder.mkdirs();
        File file = new File(folder, "file");
        List<File> revisions = prepareVersions(file);
        testContents(revisions, file, false);
        HgCommand.doRollback(workdir, NULL_LOGGER);
        revisions.remove(0);
        File newRevision = new File(new File(getDataDir(), "versionscache"), "rollback");
        revisions.add(0, newRevision);
        Utils.copyStreamsCloseAll(new FileOutputStream(file), new FileInputStream(newRevision));
        commit(new File[]{file});
        testContents(revisions, file, true);
    }
    
    public void testRevisionChangePaths () throws Exception {
        File folder = new File(workdir, "folder");
        folder.mkdirs();
        File fileModification = new File(folder, "fileM");
        File fileDelete = new File(folder, "fileD");
        File fileAdd = new File(folder, "fileA");
        File fileRenameFrom = new File(folder, "fileRF");
        File fileRenameTo = new File(folder, "fileRT");
        File fileCopied = new File(folder, "fileC");
        write(fileModification, "fileM");
        write(fileDelete, "fileD");
        write(fileAdd, "fileA");
        write(fileRenameFrom, "fileRF");
        commit(fileModification, fileDelete, fileRenameFrom);
        
        HgLogMessage rev1 = HgCommand.doTip(workdir, NULL_LOGGER);
        // prepare to commit
        write(fileModification, "fileM Change");
        HgCommand.doRemove(workdir, fileDelete, NULL_LOGGER);
        HgCommand.doRename(workdir, fileRenameFrom, fileRenameTo, NULL_LOGGER);
        assertFalse(fileRenameFrom.exists());
        HgCommand.doCopy(workdir, fileModification, fileCopied, false, NULL_LOGGER);
        HgCommand.doAdd(workdir, fileAdd, NULL_LOGGER);
        HgCommand.doCommit(workdir, Arrays.asList(fileModification, fileDelete, fileAdd, fileRenameFrom, fileRenameTo, fileCopied),
                "paths", NULL_LOGGER);
        HgLogMessage rev2 = HgCommand.doTip(workdir, NULL_LOGGER);
        
        Map<String, String> paths = new HashMap<String, String>();
        paths.put("folder/fileM", "A");
        paths.put("folder/fileD", "A");
        paths.put("folder/fileRF", "A");
        assertChangePaths(rev1.getCSetShortID(), paths);
        
        paths = new HashMap<String, String>();
        paths.put("folder/fileM", "M");
        paths.put("folder/fileD", "D");
        paths.put("folder/fileA", "A");
        paths.put("folder/fileRT", "R folder/fileRF");
        paths.put("folder/fileC", "C folder/fileM");
        assertChangePaths(rev2.getCSetShortID(), paths);
    }

    private List<File> prepareVersions (File file) throws Exception {
        List<File> revisionList = new LinkedList<File>();
        File dataDir = new File(getDataDir(), "versionscache");
        File[] revisions = dataDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("rev");
            }
        });
        for (File rev : revisions) {
            if (rev.isFile()) {
                revisionList.add(0, rev);
                Utils.copyStreamsCloseAll(new FileOutputStream(file), new FileInputStream(rev));
                commit(new File[] {file});
            }
        }
        return revisionList;
    }

    private void testContents (List<File> revisions, File file, boolean cacheFilled) throws Exception {
        HgLogMessage tip = HgCommand.doTip(workdir, NULL_LOGGER);
        long lastRev = tip.getRevisionAsLong();
        VersionsCache cache = VersionsCache.getInstance();
        Storage storage = StorageManager.getInstance().getStorage(workdir.getAbsolutePath());
        for (File golden : revisions) {
            File content;
            HgRevision hgRev = HgCommand.getLogMessages(workdir, Collections.singleton(file), String.valueOf(lastRev), String.valueOf(lastRev), false, false, false, 1, Collections.<String>emptyList(), NULL_LOGGER, true)[0].getHgRevision();
            if (!cacheFilled) {
                content = storage.getContent(HgUtils.getRelativePath(file), file.getName(), hgRev.getChangesetId());
                assertEquals(0, content.length());
            }
            content = cache.getFileRevision(file, hgRev);
            assertFile(content, golden, null);
            content = storage.getContent(HgUtils.getRelativePath(file), file.getName(), hgRev.getChangesetId());
            assertFile(content, golden, null);
            --lastRev;
        }
    }

    private void assertChangePaths (String revision, Map<String, String> expected) {
        // simple test for HgCommand
        LogCommandHandler h = new LogCommandHandler(revision);
        Mercurial.LOG.addHandler(h);
        Logger.getLogger("org.netbeans.modules.mercurial.HistoryRegistry").addHandler(h);
        HgLogMessage[] logs = HgCommand.getLogMessages(workdir, 
                null,
                revision, 
                revision, 
                true, 
                true, 
                false, 
                1, 
                Collections.<String>emptyList(), 
                NULL_LOGGER, 
                true);
        assertEquals(1, h.logCommandsCalled());
        assertEquals(1, logs.length);
        HgLogMessage info = logs[0];
        HgLogMessageChangedPath[] paths = info.getChangedPaths();
        assertPaths(expected, paths);
        
        // now let's test that HistoryRegistry works correctly
        // no changePaths
        logs = HgCommand.getLogMessages(workdir, 
                null,
                revision, 
                revision, 
                true, 
                false, 
                false, 
                1, 
                Collections.<String>emptyList(), 
                NULL_LOGGER, 
                true);
        info = logs[0];
        assertTrue(info.getChangedPaths().length == 0);
        h.clear();
        // cached fails, there is no cached revision
        HistoryRegistry.getInstance().initializeChangePaths(workdir, new HistoryRegistry.DefaultChangePathCollector(workdir, NULL_LOGGER, revision),
                info, true);
        assertEquals(0, info.getChangedPaths().length);
        assertEquals(0, h.logCommandsCalled());
        
        // EMPTY VERSIONS CACHE
        Storage diskCache = StorageManager.getInstance().getStorage(workdir.getAbsolutePath());
        byte[] buff = diskCache.getRevisionInfo(revision);
        assertNull(buff);
        
        assertFalse(h.persisted);
        HistoryRegistry.getInstance().initializeChangePaths(workdir, new HistoryRegistry.DefaultChangePathCollector(workdir, NULL_LOGGER, revision),
                info, false);
        assertTrue(h.persisted);
        assertEquals(1, h.logCommandsCalled());
        assertPaths(expected, paths);
        assertPaths(expected, getCachedPaths(revision));
        
        // do not call any command and do not even access disk cache
        logs = HgCommand.getLogMessages(workdir, 
                null,
                revision, 
                revision, 
                true, 
                false, 
                false, 
                1, 
                Collections.<String>emptyList(), 
                NULL_LOGGER, 
                true);
        info = logs[0];
        h.clear();
        assertFalse(h.persisted);
        assertFalse(h.loadedFromCache);
        HistoryRegistry.getInstance().initializeChangePaths(workdir, new HistoryRegistry.DefaultChangePathCollector(workdir, NULL_LOGGER, revision),
                info, true);
        assertFalse(h.persisted);
        assertFalse(h.loadedFromCache);
        assertPaths(expected, paths);
        assertPaths(expected, getCachedPaths(revision));
        assertEquals(0, h.logCommandsCalled());
        
        // throw away from memory and load only from disk
        logs = HgCommand.getLogMessages(workdir, 
                null,
                revision, 
                revision, 
                true, 
                false, 
                false, 
                1, 
                Collections.<String>emptyList(), 
                NULL_LOGGER, 
                true);
        info = logs[0];
        h.clear();
        HistoryRegistry.getInstance().flushCached();
        assertNull(HistoryRegistry.getInstance().getCachedPaths(revision));
        assertFalse(h.persisted);
        assertFalse(h.loadedFromCache);
        HistoryRegistry.getInstance().initializeChangePaths(workdir, new HistoryRegistry.DefaultChangePathCollector(workdir, NULL_LOGGER, revision),
                info, true);
        assertTrue(h.loadedFromCache);
        assertFalse(h.persisted);
        assertPaths(expected, paths);
        assertPaths(expected, getCachedPaths(revision));
        assertEquals(0, h.logCommandsCalled());
        
        Mercurial.LOG.removeHandler(h);
        Logger.getLogger("org.netbeans.modules.mercurial.HistoryRegistry").removeHandler(h);
    }

    private HgLogMessageChangedPath[] getCachedPaths (String revision) {
        List<HgLogMessageChangedPath> paths = HistoryRegistry.getInstance().getCachedPaths(revision);
        return paths == null ? new HgLogMessageChangedPath[0] : paths.toArray(new HgLogMessageChangedPath[0]);
    }

    private void assertPaths (Map<String, String> expected, HgLogMessageChangedPath[] paths) {
        assertEquals(expected.size(), paths.length);
        for (HgLogMessageChangedPath path : paths) {
            String actionPlusSourcePath = String.valueOf(path.getAction());
            if (path.getCopySrcPath() != null) {
                actionPlusSourcePath += " " + path.getCopySrcPath();
            }
            assertEquals(expected.get(path.getPath()), actionPlusSourcePath);
        }
    }

    private static class LogCommandHandler extends Handler {
        private boolean persisted;
        private boolean loadedFromCache;
        private int called;
        private final String revision;

        private LogCommandHandler (String revision) {
            this.revision = revision;
        }

        private void clear () {
            called = 0;
            persisted = loadedFromCache = false;
        }

        private int logCommandsCalled () {
            return called;
        }

        @Override
        public void publish (LogRecord record) {
            if (record.getMessage().contains("execEnv():")) {
                for (Object o : record.getParameters()) {
                    if (o.toString().contains("hg") && o.toString().contains("log") && o.toString().contains(revision)) {
                        ++called;
                        break;
                    }
                }
            } else if (record.getMessage().contains("persisting changePaths to disk cache")) {
                persisted = true;
            } else if (record.getMessage().contains("loading changePaths from disk cache")) {
                loadedFromCache = true;
            }
        }

        @Override
        public void flush () {
            //
        }

        @Override
        public void close () throws SecurityException {
            //
        }
    }
}
