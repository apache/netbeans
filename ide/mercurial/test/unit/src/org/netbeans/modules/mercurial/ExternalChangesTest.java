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
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.versioning.core.VersioningManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.test.MockLookup;

/**
 * Testing cache refresh after external changes - IZ #126156
 * @author ondra
 */
public class ExternalChangesTest extends AbstractHgTestCase {

    FileObject workdirFO;
    File workdir;
    FileObject modifiedFO;
    File modifiedFile;

    public ExternalChangesTest (String arg0) {
        super(arg0);
    }

    @Override
    public void setUp() throws Exception {
        System.setProperty("netbeans.user", new File(getWorkDir().getParentFile(), "userdir").getAbsolutePath());
        super.setUp();
        MockLookup.setLayersAndInstances();

        // create
        workdirFO = FileUtil.toFileObject(workdir = getWorkTreeDir());
        File folder = new File(new File(workdir, "folder1"), "folder2");
        folder.mkdirs();
        modifiedFile = new File(folder, "file");
        VersioningManager.getInstance();
        write(modifiedFile, "");
        commit(modifiedFile);
        modifiedFO = FileUtil.toFileObject(modifiedFile);
        System.setProperty("mercurial.handleDirstateEvents", "true");
        Mercurial.STATUS_LOG.setLevel(Level.FINE);


    }

    // simple test if cache refreshes correctly
    public void testRefreshAfterFSChange () throws Exception {
        Thread.sleep(5000); // some time for initial scans to finish
        write(modifiedFile, "testRefreshAfterFSChange");
        commit(modifiedFile);
        waitForRefresh();
        assertCacheStatus(modifiedFile, FileInformation.STATUS_VERSIONED_UPTODATE);
    }

    // testing if dirstate events are disabled for internal commit action
    public void testInternalCommitNoEvents () throws Exception {
        Thread.sleep(5000); // some time for initial scans to finish
        write(modifiedFile, "testInternalCommitNoDirstate");
        waitForRefresh();
        assertCacheStatus(modifiedFile, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY);
        Thread.sleep(5000); // some time for initial scans to finish
        failIfRefreshed(new HgProgressSupport() {
            @Override
            protected void perform() {
                try {
                    HgCommand.doCommit(workdir, Collections.singletonList(modifiedFile), "testInternalCommitNoDirstate", NULL_LOGGER);
                } catch (HgException ex) {
                    fail(ex.getMessage());
                }
                FileUtil.refreshFor(workdir);
            }
        });
    }

    public void testExternalCommit () throws Exception {
        Mercurial.getInstance().getMercurialInterceptor().pingRepositoryRootFor(workdir);
        Thread.sleep(5000); // some time for initial scans to finish
        write(modifiedFile, "testExternalCommitDirstate");
        waitForRefresh();
        assertCacheStatus(modifiedFile, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY);
        Thread.sleep(5000); // some time for initial scans to finish
        addNoTSRefreshCommand("commit");
        HgCommand.doCommit(workdir, Collections.singletonList(modifiedFile), "", NULL_LOGGER);
        removeNoTSRefreshCommand("commit");
        waitForRefresh();
        assertCacheStatus(modifiedFile, FileInformation.STATUS_VERSIONED_UPTODATE);
    }

    // testing if dirstate events can be disabled with the commandline switch
    public void testNoExternalEvents () throws Exception {
        Mercurial.getInstance().getMercurialInterceptor().pingRepositoryRootFor(workdir);
        Thread.sleep(5000); // some time for initial scans to finish
        // dirstate events disabled
        System.setProperty("mercurial.handleDirstateEvents", "false");
        write(modifiedFile, "testNoExternalEvents");
        waitForRefresh();
        assertCacheStatus(modifiedFile, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY);
        Thread.sleep(5000); // some time for initial scans to finish
        addNoTSRefreshCommand("commit");
        HgCommand.doCommit(workdir, Collections.singletonList(modifiedFile), "testNoExternalEvents", null);
        removeNoTSRefreshCommand("commit");
        failIfRefreshed();
        assertCacheStatus(modifiedFile, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY);
        System.setProperty("mercurial.handleDirstateEvents", "true");
    }

    // change of modif TS of the .hg folder must be ignored
    public void testNoEventsOnHgFolderChange () throws Exception {
        Mercurial.getInstance().getMercurialInterceptor().pingRepositoryRootFor(workdir);
        Thread.sleep(5000); // some time for initial scans to finish
        // dirstate events disabled
        write(modifiedFile, "testNoEventsOnHgFolderChange");
        waitForRefresh();
        assertCacheStatus(modifiedFile, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY);
        Thread.sleep(5000); // some time for initial scans to finish
        new File(workdir, ".hg").setLastModified(System.currentTimeMillis());
        failIfRefreshed();
        assertCacheStatus(modifiedFile, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY);
    }

    public void testExternalRollback () throws Exception {
        Mercurial.getInstance().getMercurialInterceptor().pingRepositoryRootFor(workdir);
        Thread.sleep(5000); // some time for initial scans to finish
        // dirstate events enabled
        write(modifiedFile, "testExternalRollback");
        waitForRefresh();
        assertCacheStatus(modifiedFile, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY);
        Thread.sleep(5000); // some time for initial scans to finish
        addNoTSRefreshCommand("commit");
        HgCommand.doCommit(workdir, Collections.singletonList(modifiedFile), "testExternalRollback", null);
        removeNoTSRefreshCommand("commit");
        waitForRefresh();
        assertCacheStatus(modifiedFile, FileInformation.STATUS_VERSIONED_UPTODATE);

        Thread.sleep(5000); // some time for initial scans to finish
        addNoTSRefreshCommand("rollback");
        HgCommand.doRollback(workdir, null);
        removeNoTSRefreshCommand("rollback");
        waitForRefresh();
        assertCacheStatus(modifiedFile, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY);
    }

    public void testExternalRevert () throws Exception {
        Mercurial.getInstance().getMercurialInterceptor().pingRepositoryRootFor(workdir);
        Thread.sleep(5000); // some time for initial scans to finish
        // dirstate events enabled
        write(modifiedFile, "testExternalRevert");
        waitForRefresh();
        assertCacheStatus(modifiedFile, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY);
        Thread.sleep(5000); // some time for initial scans to finish
        addNoTSRefreshCommand("revert");
        HgCommand.doRevert(workdir, Collections.singletonList(workdir), null, false, NULL_LOGGER);
        removeNoTSRefreshCommand("revert");
        waitForRefresh();
        assertCacheStatus(modifiedFile, FileInformation.STATUS_VERSIONED_UPTODATE);
    }
    
    public void testExternalCommandLoggedNoChanges () throws Exception {
        Mercurial.getInstance().getMercurialInterceptor().pingRepositoryRootFor(workdir);
        FileChangeAdapter fca = new FileChangeAdapter();
        workdirFO.addRecursiveListener(fca);
        FileUtil.refreshFor(workdir);
        Thread.sleep(11000); // some time for initial scans to finish and event logger to settle down
        File hgFolder = new File(workdir, ".hg");
        final File lockFile = new File(hgFolder, "wlock");
        Logger GESTURES_LOG = Logger.getLogger("org.netbeans.ui.vcs");
        ExternalCommandUsageHandler h = new ExternalCommandUsageHandler();
        GESTURES_LOG.addHandler(h);
        lockFile.createNewFile();
        FileUtil.refreshFor(workdir);
        pause(); 
        lockFile.delete();
        FileUtil.refreshFor(workdir);
        
        h.waitForEvent();
        assertNotNull(h.event);
        assertEquals(1, h.numberOfEvents);
        assertTrue(h.event.time > 0);
        assertEquals("HG", h.event.vcs);
        assertEquals("UNKNOWN", h.event.command);
        assertTrue(h.event.external);
        assertEquals(Long.valueOf(0), h.event.modifications);
        GESTURES_LOG.removeHandler(h);
        workdirFO.removeRecursiveListener(fca);
    }
    
    public void testExternalCommandLoggedChanges () throws Exception {
        Mercurial.getInstance().getMercurialInterceptor().pingRepositoryRootFor(workdir);
        FileChangeAdapter fca = new FileChangeAdapter();
        workdirFO.addRecursiveListener(fca);
        File toAdd = new File(modifiedFile.getParentFile(), "toAdd");
        File toDelete = new File(modifiedFile.getParentFile(), "toDelete");
        toDelete.createNewFile();
        FileUtil.refreshFor(workdir);
        Thread.sleep(11000); // some time for initial scans to finish and event logger to settle down
        File hgFolder = new File(workdir, ".hg");
        final File lockFile = new File(hgFolder, "wlock");
        Logger GESTURES_LOG = Logger.getLogger("org.netbeans.ui.vcs");
        ExternalCommandUsageHandler h = new ExternalCommandUsageHandler();
        GESTURES_LOG.addHandler(h);
        createLockFile(lockFile);
        FileUtil.refreshFor(workdir);
        // modification
        write(modifiedFile, "testExternalCommandLoggedChanges");
        // delete
        toDelete.delete();
        // create
        toAdd.createNewFile();
        FileUtil.refreshFor(workdir);
        pause();        
        lockFile.delete();
        FileUtil.refreshFor(workdir);
        
        h.waitForEvent();
        assertNotNull(h.event);
        assertEquals(1, h.numberOfEvents);
        assertTrue(h.event.time > 0);
        assertEquals("HG", h.event.vcs);
        assertEquals("UNKNOWN", h.event.command);
        assertTrue(h.event.external);
        assertEquals(Long.valueOf(3), h.event.modifications);
        GESTURES_LOG.removeHandler(h);
        workdirFO.removeRecursiveListener(fca);
    }
    
    public void testInternalCommandLoggedChanges () throws Exception {
        Mercurial.getInstance().getMercurialInterceptor().pingRepositoryRootFor(workdir);
        FileChangeAdapter fca = new FileChangeAdapter();
        workdirFO.addRecursiveListener(fca);
        final File toAdd = new File(modifiedFile.getParentFile(), "toAdd");
        final File toDelete = new File(modifiedFile.getParentFile(), "toDelete");
        toDelete.createNewFile();
        FileUtil.refreshFor(workdir);
        Thread.sleep(11000); // some time for initial scans to finish and event logger to settle down
        File hgFolder = new File(workdir, ".hg");
        final File lockFile = new File(hgFolder, "wlock");
        Logger GESTURES_LOG = Logger.getLogger("org.netbeans.ui.vcs");
        ExternalCommandUsageHandler h = new ExternalCommandUsageHandler();
        GESTURES_LOG.addHandler(h);
        Mercurial.getInstance().runWithoutExternalEvents(workdir, "MY_COMMAND", new Callable<Void>() {
            @Override
            public Void call () throws Exception {
                createLockFile(lockFile);
                FileUtil.refreshFor(workdir);
                // modification
                write(modifiedFile, "testExternalCommandLoggedChanges");
                // delete
                toDelete.delete();
                // create
                toAdd.createNewFile();
                FileUtil.refreshFor(workdir);
                pause();
                lockFile.delete();
                FileUtil.refreshFor(workdir);
                return null;
            }
        });
        h.waitForEvent();
        assertNotNull(h.event);
        assertEquals(1, h.numberOfEvents);
        assertTrue(h.event.time > 0);
        assertEquals("HG", h.event.vcs);
        assertFalse(h.event.external);
        assertEquals("MY_COMMAND", h.event.command);
        assertEquals(Long.valueOf(3), h.event.modifications);
        GESTURES_LOG.removeHandler(h);
        workdirFO.removeRecursiveListener(fca);
    }
    
    public void testInternalCommandLoggedChangesAfterUnlock () throws Exception {
        Mercurial.getInstance().getMercurialInterceptor().pingRepositoryRootFor(workdir);
        FileChangeAdapter fca = new FileChangeAdapter();
        workdirFO.addRecursiveListener(fca);
        final File toAdd = new File(modifiedFile.getParentFile(), "toAdd");
        final File toDelete = new File(modifiedFile.getParentFile(), "toDelete");
        toDelete.createNewFile();
        FileUtil.refreshFor(workdir);
        Thread.sleep(11000); // some time for initial scans to finish and event logger to settle down
        File hgFolder = new File(workdir, ".hg");
        final File lockFile = new File(hgFolder, "wlock");
        Logger GESTURES_LOG = Logger.getLogger("org.netbeans.ui.vcs");
        ExternalCommandUsageHandler h = new ExternalCommandUsageHandler();
        GESTURES_LOG.addHandler(h);
        Mercurial.getInstance().runWithoutExternalEvents(workdir, "MY_COMMAND", new Callable<Void>() {
            @Override
            public Void call () throws Exception {
                // modification
                write(modifiedFile, "testExternalCommandLoggedChanges");
                // delete
                toDelete.delete();
                // create
                toAdd.createNewFile();
                FileUtil.refreshFor(workdir);
                pause();
                return null;
            }
        });
        Thread.sleep(2000);
        // coming with delay after some time
        // still considered as part of internal command
        createLockFile(lockFile);
        FileUtil.refreshFor(workdir);
        pause();
        lockFile.delete();
        FileUtil.refreshFor(workdir);
        
        h.waitForEvent();
        assertNotNull(h.event);
        assertEquals(1, h.numberOfEvents);
        assertTrue(h.event.time > 0);
        assertEquals("HG", h.event.vcs);
        assertFalse(h.event.external);
        assertEquals("MY_COMMAND", h.event.command);
        assertEquals(Long.valueOf(3), h.event.modifications);
        
        Thread.sleep(9000);
        // coming after some reasonable pause, now considered as part of next external command
        createLockFile(lockFile);
        FileUtil.refreshFor(workdir);
        write(modifiedFile, "anotherchange");
        FileUtil.refreshFor(workdir);
        pause();        
        lockFile.delete();
        FileUtil.refreshFor(workdir);
        h.event = null;
        h.waitForEvent();
        assertNotNull(h.event);
        assertEquals(2, h.numberOfEvents);
        assertEquals("HG", h.event.vcs);
        assertTrue(h.event.external);
        assertEquals("UNKNOWN", h.event.command);
        assertEquals(Long.valueOf(1), h.event.modifications);
        GESTURES_LOG.removeHandler(h);
        workdirFO.removeRecursiveListener(fca);
    }

    private void waitForRefresh () throws Exception {
        InterceptorRefreshHandler handler = new InterceptorRefreshHandler();
        Mercurial.STATUS_LOG.addHandler(handler);
        FileUtil.refreshFor(workdir);
        for (int i=0; i<20; ++i) {
            Thread.sleep(1000);
            if (handler.refreshed) {
                break;
            }
        }
        if (!handler.refreshed) {
            fail("cache not refresh");
        }
        Mercurial.STATUS_LOG.removeHandler(handler);
    }

    private void failIfRefreshed () throws Exception {
        failIfRefreshed(null);
    }

    private void failIfRefreshed (HgProgressSupport supp) throws Exception {
        InterceptorRefreshHandler handler = new InterceptorRefreshHandler();
        Mercurial.STATUS_LOG.addHandler(handler);
        FileUtil.refreshFor(workdir);
        RequestProcessor.Task task = supp == null ? null : supp.start(RequestProcessor.getDefault());
        for (int i = 0; i < 25; ++i) {
            Thread.sleep(1000);
            if (handler.refreshed) {
                fail("cache refresh started: " + handler.refreshString);
            }
        }
        if (task != null) {
            task.waitFinished();
        }
        Mercurial.STATUS_LOG.removeHandler(handler);
    }

    private void addNoTSRefreshCommand (String command) throws Exception {
        Field f = HgCommand.class.getDeclaredField("REPOSITORY_NOMODIFICATION_COMMANDS");
        f.setAccessible(true);
        Set set = (Set) f.get(HgCommand.class);
        set.add(command);
    }

    private void removeNoTSRefreshCommand (String command) throws Exception {
        Field f = HgCommand.class.getDeclaredField("REPOSITORY_NOMODIFICATION_COMMANDS");
        f.setAccessible(true);
        Set set = (Set) f.get(HgCommand.class);
        set.remove(command);
    }

    private void createLockFile (File lockFile) throws Exception {
        if (Utilities.isWindows()) {
            lockFile.createNewFile();
        } else {
            ProcessBuilder pb = new ProcessBuilder().directory(lockFile.getParentFile()).command(new String[] { "ln", "-s", "AAA", lockFile.getName() });
            pb.start().waitFor();
            assertFalse(lockFile.exists());
            assertTrue(Arrays.asList(lockFile.getParentFile().list()).contains(lockFile.getName()));
//            assertNotNull(FileUtil.toFileObject(lockFile));
        }
    }

    private void pause () throws InterruptedException {
        // uncomment if events only about longer commands are logged
//        Thread.sleep(3100); // only commands running longer than 3s are logged
    }

    private class InterceptorRefreshHandler extends Handler {
        private boolean refreshed;
        private boolean refreshStarted;
        private String refreshString;

        @Override
        public void publish(LogRecord record) {
            String message = record.getMessage();
            if (message.startsWith("refreshAll: starting status scan for ") && (
                    message.contains(workdirFO.getPath() + ",")
                    || message.contains(workdirFO.getPath() + "]")
                    || message.contains(modifiedFile.getParentFile().getParentFile().getAbsolutePath()))) {
                refreshStarted = true;
                refreshString = message;
            } else if (refreshStarted && message.startsWith("refreshAll: finishes status scan after ")) {
                refreshed = true;
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

    }
    
    private class ExternalCommandUsageHandler extends Handler {
        
        volatile CommandUsageEvent event;
        volatile int numberOfEvents;
        
        @Override
        public void publish(LogRecord record) {
            String message = record.getMessage();
            if ("USG_VCS_CMD".equals(message)) {
                ++numberOfEvents;
                event = new CommandUsageEvent();
                event.vcs = (String) record.getParameters()[0];
                event.time = (Long) record.getParameters()[1];
                event.modifications = (Long) record.getParameters()[2];
                event.command = (String) record.getParameters()[3];
                event.external = "EXTERNAL".equals(record.getParameters()[4]);
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        private void waitForEvent () throws Exception {
            for (int i = 0; i < 20; ++i) {
                Thread.sleep(1000);
                if (event != null) {
                    break;
                }
            }
            if (event == null) {
                fail("no event logged");
            }
        }

    }

    static class CommandUsageEvent {
        private boolean external;
        private String command;
        private Long modifications;
        private Long time;
        private String vcs;

    }
}