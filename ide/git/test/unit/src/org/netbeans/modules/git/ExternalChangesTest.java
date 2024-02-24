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

package org.netbeans.modules.git;

import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.masterfs.VersioningAnnotationProvider;
import org.netbeans.modules.versioning.core.VersioningManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author ondra
 */
public class ExternalChangesTest extends AbstractGitTestCase {

    FileObject workdirFO;
    FileObject modifiedFO;
    File modifiedFile;

    public ExternalChangesTest (String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(new Class[] {
            VersioningAnnotationProvider.class,
            GitVCS.class});
        
        System.setProperty("versioning.git.handleExternalEvents", "true");
        // create
        workdirFO = FileUtil.toFileObject(repositoryLocation);
        File folder = new File(new File(repositoryLocation, "folder1"), "folder2");
        folder.mkdirs();
        modifiedFile = new File(folder, "file");
        VersioningManager.getInstance();
        write(modifiedFile, "");
        getClient(repositoryLocation).add(new File[] { modifiedFile }, GitUtils.NULL_PROGRESS_MONITOR);
        modifiedFO = FileUtil.toFileObject(modifiedFile);
        Git.STATUS_LOG.setLevel(Level.ALL);
    }

    // simple test if cache refreshes correctly
    public void testExternalChanges () throws Exception {
        waitForInitialScan();
        assertTrue(getCache().getStatus(modifiedFile).containsStatus(Status.NEW_HEAD_INDEX));

        getClient(repositoryLocation).remove(new File[] { modifiedFile }, true, GitUtils.NULL_PROGRESS_MONITOR);
        assertTrue(getCache().getStatus(modifiedFile).containsStatus(Status.NEW_HEAD_INDEX));
        waitForRefresh();
        assertTrue(getCache().getStatus(modifiedFile).containsStatus(Status.NEW_INDEX_WORKING_TREE));
    }

    // testing if .git events can be disabled with the commandline switch
    public void testDisableExternalEventsHandler () throws Exception {
        waitForInitialScan();
        // dirstate events disabled
        System.setProperty("versioning.git.handleExternalEvents", "false");
        assertTrue(getCache().getStatus(modifiedFile).containsStatus(Status.NEW_HEAD_INDEX));

        getClient(repositoryLocation).remove(new File[] { modifiedFile }, true, GitUtils.NULL_PROGRESS_MONITOR);
        assertTrue(getCache().getStatus(modifiedFile).containsStatus(Status.NEW_HEAD_INDEX));
        failIfRefreshed();
        assertTrue(getCache().getStatus(modifiedFile).containsStatus(Status.NEW_HEAD_INDEX));
        getCache().refreshAllRoots(Collections.singleton(modifiedFile));
        assertTrue(getCache().getStatus(modifiedFile).containsStatus(Status.NEW_INDEX_WORKING_TREE));
    }

    // testing if internal repository commands disable the handler
    // test: refreshTimestamp called manually
    public void testNoExternalEventsManualTimestampRefresh () throws Exception {
        waitForInitialScan();
        assertTrue(getCache().getStatus(modifiedFile).containsStatus(Status.NEW_HEAD_INDEX));
        Git.getInstance().runWithoutExternalEvents(repositoryLocation, "remove", new Callable<Void>() {
            @Override
            public Void call () throws Exception {
                getClient(repositoryLocation).remove(new File[] { modifiedFile }, true, GitUtils.NULL_PROGRESS_MONITOR);
                return null;
            }
        });
        assertTrue(getCache().getStatus(modifiedFile).containsStatus(Status.NEW_HEAD_INDEX));
        failIfRefreshed();
        assertTrue(getCache().getStatus(modifiedFile).containsStatus(Status.NEW_HEAD_INDEX));
        getCache().refreshAllRoots(Collections.singleton(modifiedFile));
        assertTrue(getCache().getStatus(modifiedFile).containsStatus(Status.NEW_INDEX_WORKING_TREE));
    }

    // test: check that the index timestamp is refreshed after WT modifying commands
    public void testChangesInsideIDE () throws Exception {
        waitForInitialScan();
        assertTrue(getCache().getStatus(modifiedFile).containsStatus(Status.NEW_HEAD_INDEX));

        Logger logger = Logger.getLogger(FilesystemInterceptor.class.getName());
        logger.setLevel(Level.ALL);
        final boolean[] refreshed = new boolean[1];
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (record.getMessage().contains("Refreshing index timestamp after")) {
                    refreshed[0] = true;
                }
            }
            @Override
            public void flush() { }
            @Override
            public void close() throws SecurityException {}
        });
        Git.getInstance().getClient(repositoryLocation).remove(new File[] { modifiedFile }, true, GitUtils.NULL_PROGRESS_MONITOR);
        assertTrue(refreshed[0]);
        failIfRefreshed();
        assertTrue(getCache().getStatus(modifiedFile).containsStatus(Status.NEW_HEAD_INDEX));
    }

    // test: check that the repository info is refreshed after commit modifying commands
    public void testRepositoryInfoRefreshInsideIDE () throws Exception {
        waitForInitialScan();
        assertTrue(getCache().getStatus(modifiedFile).containsStatus(Status.NEW_HEAD_INDEX));
        RepositoryInfo info = RepositoryInfo.getInstance(repositoryLocation);
        assertEquals(GitBranch.NO_BRANCH, info.getActiveBranch().getName());
        assertEquals(AbstractGitTestCase.NULL_OBJECT_ID, info.getActiveBranch().getId());
        String newHead = Git.getInstance().getClient(repositoryLocation).commit(new File[] { modifiedFile }, "bla", null, null, GitUtils.NULL_PROGRESS_MONITOR).getRevision();
        for (int i = 0; i < 100; ++i) {
            Thread.sleep(100);
            if (!info.getActiveBranch().getId().isEmpty()) {
                break;
            }
        }
        assertNotNull(info.getActiveBranch());
        assertEquals(newHead, info.getActiveBranch().getId());
    }

    // test: check that the repository info is refreshed after commit outside of IDE
    public void testRepositoryInfoRefreshOutsideIDE () throws Exception {
        waitForInitialScan();
        assertTrue(getCache().getStatus(modifiedFile).containsStatus(Status.NEW_HEAD_INDEX));
        RepositoryInfo info = RepositoryInfo.getInstance(repositoryLocation);
        assertEquals(GitBranch.NO_BRANCH, info.getActiveBranch().getName());
        assertEquals(AbstractGitTestCase.NULL_OBJECT_ID, info.getActiveBranch().getId());
        String newHead = getClient(repositoryLocation).commit(new File[] { modifiedFile }, "bla", null, null, GitUtils.NULL_PROGRESS_MONITOR).getRevision();
        waitForRefresh();
        for (int i = 0; i < 100; ++i) {
            Thread.sleep(100);
            if (!info.getActiveBranch().getId().isEmpty()) {
                break;
            }
        }
        assertNotNull(info.getActiveBranch());
        assertEquals(newHead, info.getActiveBranch().getId());
    }

    // test: check that the index timestamp is NOT refreshed after WT read-only commands
    public void testNoRefreshAfterStatus () throws Exception {
        waitForInitialScan();
        assertTrue(getCache().getStatus(modifiedFile).containsStatus(Status.NEW_HEAD_INDEX));

        Logger logger = Logger.getLogger(GitClient.class.getName());
        logger.setLevel(Level.ALL);
        final boolean[] refreshed = new boolean[1];
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (record.getMessage().contains("Refreshing index timestamp after")) {
                    refreshed[0] = true;
                }
            }
            @Override
            public void flush() { }
            @Override
            public void close() throws SecurityException {}
        });
        Git.getInstance().getClient(repositoryLocation).getStatus(new File[] { modifiedFile }, GitUtils.NULL_PROGRESS_MONITOR);
        assertFalse(refreshed[0]);
        failIfRefreshed();
        assertTrue(getCache().getStatus(modifiedFile).containsStatus(Status.NEW_HEAD_INDEX));
    }

    public void testExternalCommit () throws Exception {
        waitForInitialScan();
        assertTrue(getCache().getStatus(modifiedFile).containsStatus(Status.NEW_HEAD_INDEX));
        getClient(repositoryLocation).commit(new File[] { modifiedFile }, "initial add", null, null, GitUtils.NULL_PROGRESS_MONITOR);
        write(modifiedFile, "modification");
        getClient(repositoryLocation).add(new File[] { modifiedFile }, GitUtils.NULL_PROGRESS_MONITOR);
        // must run an external status since it somehow either modifies the index or sorts it or whatever
        runExternally(repositoryLocation, Arrays.asList(new String[] { "git", "status" } ));
        Thread.sleep(1000);
        waitForRefresh();
        assertTrue(getCache().getStatus(modifiedFile).containsStatus(Status.MODIFIED_HEAD_INDEX));

        // now try an external commit, it should not modify the index in any way
        runExternally(repositoryLocation, Arrays.asList(new String[] { "git", "commit", "-m", "commit message" } ));
        // index stays the same
        assertTrue(getCache().getStatus(modifiedFile).containsStatus(Status.MODIFIED_HEAD_INDEX));
        // the cache should still be refreshed
        waitForRefresh();
        assertTrue(getCache().getStatus(modifiedFile).containsStatus(Status.UPTODATE));
    }
    
    @RandomlyFails
    public void testLogExtCmd () throws Exception {
        waitForInitialScan();
        FileChangeAdapter fca = new FileChangeAdapter();
        workdirFO.addRecursiveListener(fca);
        FileUtil.refreshFor(repositoryLocation);
        Thread.sleep(11000); // some time for initial scans to finish and event logger to settle down
        File gitFolder = new File(repositoryLocation, ".git");
        final File lockFile = new File(gitFolder, "index.lock");
        Logger GESTURES_LOG = Logger.getLogger("org.netbeans.ui.vcs");
        ExternalCommandUsageHandler h = new ExternalCommandUsageHandler();
        GESTURES_LOG.addHandler(h);
        lockFile.createNewFile();
        FileUtil.refreshFor(repositoryLocation);
        pause(); 
        lockFile.delete();
        FileUtil.refreshFor(repositoryLocation);
        
        h.waitForEvent();
        assertNotNull(h.event);
        assertEquals(1, h.numberOfEvents);
        assertTrue(h.event.time > 0);
        assertEquals("GIT", h.event.vcs);
        assertEquals("UNKNOWN", h.event.command);
        assertTrue(h.event.external);
        assertEquals(Long.valueOf(0), h.event.modifications);
        GESTURES_LOG.removeHandler(h);
        workdirFO.removeRecursiveListener(fca);
    }
    
    @RandomlyFails
    public void testLogExtCmdChanges () throws Exception {
        waitForInitialScan();
        FileChangeAdapter fca = new FileChangeAdapter();
        workdirFO.addRecursiveListener(fca);
        final File toAdd = new File(modifiedFile.getParentFile(), "toAdd");
        File toDelete = new File(modifiedFile.getParentFile(), "toDelete");
        toDelete.createNewFile();
        FileUtil.refreshFor(repositoryLocation);
        Thread.sleep(11000); // some time for initial scans to finish and event logger to settle down
        File gitFolder = new File(repositoryLocation, ".git");
        final File lockFile = new File(gitFolder, "index.lock");
        
        Logger.getLogger(FilesystemInterceptor.class.getName()).setLevel(Level.ALL);
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.ALL);
        Logger.getLogger(FilesystemInterceptor.class.getName()).addHandler(ch);
        
        assertTrue(lockFile.createNewFile());
        FileUtil.refreshFor(repositoryLocation);
        pause();
        Thread.sleep(1100);
        assertTrue(lockFile.delete());
        FileUtil.refreshFor(repositoryLocation);
        Thread.sleep(1100);
        
        Logger GESTURES_LOG = Logger.getLogger("org.netbeans.ui.vcs");
        ExternalCommandUsageHandler h = new ExternalCommandUsageHandler();
        GESTURES_LOG.addHandler(h);
        assertTrue(lockFile.createNewFile());
        FileUtil.refreshFor(repositoryLocation);
        // modification
        write(modifiedFile, "testExternalCommandLoggedChanges");
        // delete
        toDelete.delete();
        // create
        toAdd.createNewFile();
        FileUtil.refreshFor(repositoryLocation);
        pause();
        Thread.sleep(1100);
        assertTrue(lockFile.delete());
        FileUtil.refreshFor(repositoryLocation);
        
        h.waitForEvent();
        assertNotNull(h.event);
        assertEquals(1, h.numberOfEvents);
        assertTrue(h.event.time > 0);
        assertEquals("GIT", h.event.vcs);
        assertEquals("UNKNOWN", h.event.command);
        assertTrue(h.event.external);
        assertEquals(Long.valueOf(3), h.event.modifications);
        GESTURES_LOG.removeHandler(h);
        workdirFO.removeRecursiveListener(fca);
        Logger.getLogger(FilesystemInterceptor.class.getName()).removeHandler(ch);
    }
    
    public void testLogIntCmd () throws Exception {
        waitForInitialScan();
        FileChangeAdapter fca = new FileChangeAdapter();
        workdirFO.addRecursiveListener(fca);
        final File toAdd = new File(modifiedFile.getParentFile(), "toAdd");
        final File toDelete = new File(modifiedFile.getParentFile(), "toDelete");
        toDelete.createNewFile();
        FileUtil.refreshFor(repositoryLocation);
        Thread.sleep(11000); // some time for initial scans to finish and event logger to settle down
        File gitFolder = new File(repositoryLocation, ".git");
        final File lockFile = new File(gitFolder, "index.lock");
        Logger GESTURES_LOG = Logger.getLogger("org.netbeans.ui.vcs");
        ExternalCommandUsageHandler h = new ExternalCommandUsageHandler();
        GESTURES_LOG.addHandler(h);
        Git.getInstance().runWithoutExternalEvents(repositoryLocation, "MY_COMMAND", new Callable<Void>() {
            @Override
            public Void call () throws Exception {
                lockFile.createNewFile();
                FileUtil.refreshFor(repositoryLocation);
                // modification
                write(modifiedFile, "testExternalCommandLoggedChanges");
                // delete
                toDelete.delete();
                // create
                toAdd.createNewFile();
                FileUtil.refreshFor(repositoryLocation);
                pause();
                lockFile.delete();
                FileUtil.refreshFor(repositoryLocation);
                return null;
            }
        });
        h.waitForEvent();
        assertNotNull(h.event);
        assertEquals(1, h.numberOfEvents);
        assertTrue(h.event.time > 0);
        assertEquals("GIT", h.event.vcs);
        assertFalse(h.event.external);
        assertEquals("MY_COMMAND", h.event.command);
        assertEquals(Long.valueOf(3), h.event.modifications);
        GESTURES_LOG.removeHandler(h);
        workdirFO.removeRecursiveListener(fca);
    }
    
    @RandomlyFails
    public void testLogIntCmdAfterUnlock () throws Exception {
        waitForInitialScan();
        FileChangeAdapter fca = new FileChangeAdapter();
        workdirFO.addRecursiveListener(fca);
        final File toAdd = new File(modifiedFile.getParentFile(), "toAdd");
        final File toDelete = new File(modifiedFile.getParentFile(), "toDelete");
        toDelete.createNewFile();
        FileUtil.refreshFor(repositoryLocation);
        Thread.sleep(11000); // some time for initial scans to finish and event logger to settle down
        File gitFolder = new File(repositoryLocation, ".git");
        final File lockFile = new File(gitFolder, "index.lock");
        Logger GESTURES_LOG = Logger.getLogger("org.netbeans.ui.vcs");
        ExternalCommandUsageHandler h = new ExternalCommandUsageHandler();
        GESTURES_LOG.addHandler(h);
        Git.getInstance().runWithoutExternalEvents(repositoryLocation, "MY_COMMAND", new Callable<Void>() {
            @Override
            public Void call () throws Exception {
                // modification
                write(modifiedFile, "testExternalCommandLoggedChanges");
                // delete
                toDelete.delete();
                // create
                toAdd.createNewFile();
                pause();
                FileUtil.refreshFor(repositoryLocation);
                return null;
            }
        });
        Thread.sleep(2000);
        // coming with delay after some time
        // still considered as part of internal command
        lockFile.createNewFile();
        FileUtil.refreshFor(repositoryLocation);
        pause();
        lockFile.delete();
        FileUtil.refreshFor(repositoryLocation);
        
        h.waitForEvent();
        assertNotNull(h.event);
        assertEquals(1, h.numberOfEvents);
        assertTrue(h.event.time > 0);
        assertEquals("GIT", h.event.vcs);
        assertFalse(h.event.external);
        assertEquals("MY_COMMAND", h.event.command);
        assertEquals(Long.valueOf(3), h.event.modifications);
        
        Thread.sleep(9000);
        // coming after some reasonable pause, now considered as part of external command
        lockFile.createNewFile();
        FileUtil.refreshFor(repositoryLocation);
        pause();
        lockFile.delete();
        FileUtil.refreshFor(repositoryLocation);
        h.waitForEvent();
        assertNotNull(h.event);
        assertEquals(2, h.numberOfEvents);
        assertEquals("GIT", h.event.vcs);
        assertTrue(h.event.external);
        assertEquals("UNKNOWN", h.event.command);
        assertEquals(Long.valueOf(0), h.event.modifications);
        GESTURES_LOG.removeHandler(h);
        workdirFO.removeRecursiveListener(fca);
    }

    private void waitForRefresh () throws Exception {
        InterceptorRefreshHandler handler = new InterceptorRefreshHandler();
        Git.STATUS_LOG.addHandler(handler);
        FileUtil.refreshFor(repositoryLocation);
        for (int i=0; i<20; ++i) {
            Thread.sleep(1000);
            if (handler.refreshed) {
                break;
            }
        }
        if (!handler.refreshed) {
            fail("cache not refreshed");
        }
        Git.STATUS_LOG.removeHandler(handler);
    }

    private void failIfRefreshed () throws Exception {
        InterceptorRefreshHandler handler = new InterceptorRefreshHandler();
        Git.STATUS_LOG.addHandler(handler);
        FileUtil.refreshFor(repositoryLocation);
        for (int i = 0; i < 25; ++i) {
            Thread.sleep(1000);
            if (handler.refreshed) {
                fail("cache refresh started: " + handler.refreshString);
            }
        }
        Git.STATUS_LOG.removeHandler(handler);
    }

    private void waitForInitialScan() throws Exception {
        StatusRefreshLogHandler handler = new StatusRefreshLogHandler(testBase);
        Git.STATUS_LOG.addHandler(handler);
        handler.setFilesToRefresh(Collections.singleton(repositoryLocation));
        Git.getInstance().getVCSInterceptor().pingRepositoryRootFor(repositoryLocation);
        assertTrue(handler.waitForFilesToRefresh());
    }

    private void pause () throws InterruptedException {
        // uncomment if decided to log only longer commands
//        Thread.sleep(3100); // only commands running longer than 3s are logged
    }

    private class InterceptorRefreshHandler extends Handler {
        private boolean refreshed;
        private boolean refreshStarted;
        private String refreshString;

        @Override
        public void publish(LogRecord record) {
            String message = record.getMessage();
            message = MessageFormat.format(message, record.getParameters());
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