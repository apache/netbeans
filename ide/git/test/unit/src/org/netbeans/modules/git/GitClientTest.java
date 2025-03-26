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

import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.client.GitCanceledException;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.MockServices;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.libs.git.progress.ProgressMonitor.DefaultProgressMonitor;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.util.IndexingBridge;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author ondra
 */
public class GitClientTest extends AbstractGitTestCase {
    private Logger indexingLogger;

    public GitClientTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(new Class[] {
            GitVCS.class});
        IndexingBridge bridge = IndexingBridge.getInstance();
        Field f = IndexingBridge.class.getDeclaredField("LOG");
        f.setAccessible(true);
        indexingLogger = (Logger) f.get(bridge);
    }

    /**
     * tests that we don't miss any read-only commands.
     * If a method is added to GitClient, we NEED to evaluate and decide if it's read-only.
     * If it is and we miss the command, the support will refresh index timestamp when it's not supposed to.
     * @throws Exception
     */
    public void testIndexReadOnlyMethods () throws Exception {
        Set<String> allTestedMethods = new HashSet<String>(Arrays.asList(
                "add",
                "addNotificationListener",
                "blame",
                "catFile",
                "catIndexEntry",
                "checkout",
                "checkoutRevision",
                "cherryPick",
                "clean",
                "close",
                "commit",
                "copyAfter",
                "createBranch",
                "createTag",
                "deleteBranch",
                "deleteTag",
                "exportCommit",
                "exportDiff",
                "fetch",
                "getBranches",
                "getCommonAncestor",
                "getConflicts",
                "getPreviousRevision",
                "getRemote",
                "getRemotes",
                "getRepositoryState",
                "getStatus",
                "getSubmoduleStatus",  //NOI18N
                "getTags",
                "getUser",
                "ignore",
                "init",
                "initializeSubmodules",
                "listModifiedIndexEntries",
                "listRemoteBranches",
                "listRemoteTags",
                "log",
                "merge",
                "pull",
                "push",
                "rebase",
                "release",
                "remove",
                "removeNotificationListener",
                "removeRemote",
                "rename",
                "reset",
                "revert",
                "setCallback",
                "setRemote",
                "setUpstreamBranch",
                "stashApply",
                "stashDrop",
                "stashDropAll",
                "stashList",
                "stashSave",
                "unignore",
                "updateReference",
                "updateSubmodules"
        ));
        Set<String> readOnlyMethods = new HashSet<String>(Arrays.asList(
                "addNotificationListener",
                "blame",
                "catFile",
                "catIndexEntry",
                "createBranch",
                "createTag",
                "deleteBranch",
                "deleteTag",
                "exportCommit",
                "exportDiff",
                "fetch",
                "getBranches",
                "getCommonAncestor",
                "getConflicts",
                "getPreviousRevision",
                "getRemote",
                "getRemotes",
                "getRepositoryState",
                "getStatus",
                "getSubmoduleStatus",  //NOI18N
                "getTags",
                "getUser",
                "ignore",
                "listModifiedIndexEntries",
                "listRemoteBranches",
                "listRemoteTags",
                "log",
                "removeNotificationListener",
                "removeRemote",
                "setCallback",
                "setRemote",
                "stashList",
                "push",
                "unignore"));
        Field f = GitClient.class.getDeclaredField("WORKING_TREE_READ_ONLY_COMMANDS");
        f.setAccessible(true);
        Set<String> actualReadOnlyMethods = (Set<String>) f.get(GitClient.class);

        Method[] methods = getClientMethods();
        Arrays.sort(methods, new Comparator<Method>() {
            @Override
            public int compare(Method o1, Method o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (Method m : methods) {
            String methodName = m.getName();
            assertTrue(methodName, allTestedMethods.contains(methodName));
            if (readOnlyMethods.contains(methodName)) {
                assertTrue(methodName, actualReadOnlyMethods.contains(methodName));
                readOnlyMethods.remove(methodName);
            }
        }
        assertTrue(readOnlyMethods.isEmpty());
    }

    /**
     * tests that we don't miss any command that results in a need to refresh repository info.
     * If a method is added to GitClient, we NEED to evaluate and decide if it's a command after which we should refresh the repository info (current branch, head and stuff).
     * @throws Exception
     */
    public void testMethodsNeedingRepositoryInfoRefresh () throws Exception {
        Set<String> allTestedMethods = new HashSet<String>(Arrays.asList(
                "add",
                "addNotificationListener",
                "blame",
                "catFile",
                "catIndexEntry",
                "checkout",
                "checkoutRevision",
                "cherryPick",
                "clean",
                "close",
                "commit",
                "copyAfter",
                "createBranch",
                "createTag",
                "deleteBranch",
                "deleteTag",
                "exportCommit",
                "exportDiff",
                "fetch",
                "getBranches",
                "getCommonAncestor",
                "getConflicts",
                "getPreviousRevision",
                "getRemote",
                "getRemotes",
                "getRepositoryState",
                "getStatus",
                "getSubmoduleStatus",
                "getTags",
                "getUser",
                "init",
                "initializeSubmodules",
                "ignore",
                "listModifiedIndexEntries",
                "listRemoteBranches",
                "listRemoteTags",
                "log",
                "merge",
                "pull",
                "push",
                "rebase",
                "release",
                "remove",
                "removeNotificationListener",
                "removeRemote",
                "rename",
                "reset",
                "revert",
                "setCallback",
                "setRemote",
                "setUpstreamBranch",
                "stashApply",
                "stashDrop",
                "stashDropAll",
                "stashList",
                "stashSave",
                "unignore",
                "updateReference",
                "updateSubmodules"
        ));
        Set<String> expectedMethods = new HashSet<String>(Arrays.asList(
                "checkout",
                "checkoutRevision",
                "cherryPick",
                "commit",
                "createBranch",
                "createTag",
                "deleteBranch",
                "deleteTag",
                "fetch",
                "merge",
                "pull",
                "push",
                "rebase",
                "reset",
                "removeRemote",
                "revert",
                "setRemote",
                "setUpstreamBranch",
                "updateReference",
                "updateSubmodules"
        ));
        Field f = GitClient.class.getDeclaredField("NEED_REPOSITORY_REFRESH_COMMANDS");
        f.setAccessible(true);
        Set<String> actualMethods = (Set<String>) f.get(GitClient.class);

        Method[] methods = getClientMethods();
        Arrays.sort(methods, new Comparator<Method>() {
            @Override
            public int compare(Method o1, Method o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (Method m : methods) {
            String methodName = m.getName();
            assertTrue(methodName, allTestedMethods.contains(methodName));
            if (expectedMethods.contains(methodName)) {
                assertTrue(methodName, actualMethods.contains(methodName));
                expectedMethods.remove(methodName);
            }
        }
        assertTrue(expectedMethods.isEmpty());
    }

    /**
     * tests that we don't miss any network commands.
     * If a method is added to GitClient, we NEED to evaluate and decide if it's a network one. If it is and we miss the command, 
     * the NbAuthenticator might pop up an undesired auth dialog (#200692).
     * @throws Exception
     */
    public void testNetworkMethods () throws Exception {
        Set<String> allTestedMethods = new HashSet<String>(Arrays.asList(
                "add",
                "addNotificationListener",
                "blame",
                "catFile",
                "catIndexEntry",
                "checkout",
                "checkoutRevision",
                "cherryPick",
                "clean",
                "close",
                "commit",
                "copyAfter",
                "createBranch",
                "createTag",
                "deleteBranch",
                "deleteTag",
                "exportCommit",
                "exportDiff",
                "fetch",
                "getBranches",
                "getCommonAncestor",
                "getConflicts",
                "getPreviousRevision",
                "getRemote",
                "getRemotes",
                "getRepositoryState",
                "getStatus",
                "getSubmoduleStatus",
                "getTags",
                "getUser",
                "init",
                "initializeSubmodules",
                "ignore",
                "listModifiedIndexEntries",
                "listRemoteBranches",
                "listRemoteTags",
                "log",
                "merge",
                "pull",
                "push",
                "rebase",
                "release",
                "remove",
                "removeNotificationListener",
                "removeRemote",
                "rename",
                "reset",
                "revert",
                "setCallback",
                "setRemote",
                "setUpstreamBranch",
                "stashApply",
                "stashDrop",
                "stashDropAll",
                "stashList",
                "stashSave",
                "unignore",
                "updateReference",
                "updateSubmodules"
        ));
        Set<String> networkMethods = new HashSet<String>(Arrays.asList(
                "fetch",
                "listRemoteBranches",
                "listRemoteTags",
                "pull",
                "push",
                "updateSubmodules"
        ));
        Field f = GitClient.class.getDeclaredField("NETWORK_COMMANDS");
        f.setAccessible(true);
        Set<String> actualNetworkCommands = (Set<String>) f.get(GitClient.class);

        Method[] methods = getClientMethods();
        Arrays.sort(methods, new Comparator<Method>() {
            @Override
            public int compare(Method o1, Method o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (Method m : methods) {
            String methodName = m.getName();
            assertTrue(methodName, allTestedMethods.contains(methodName));
            if (networkMethods.contains(methodName)) {
                assertTrue(methodName, actualNetworkCommands.contains(methodName));
                networkMethods.remove(methodName);
            }
        }
        assertTrue(networkMethods.isEmpty());
    }

    public void testIndexingBridge () throws Exception {
        Git.LOG.setLevel(Level.ALL);
        indexingLogger.setLevel(Level.ALL);
        LogHandler h = new LogHandler();
        indexingLogger.addHandler(h);
        Git.LOG.addHandler(h);

        final File folder = new File(repositoryLocation, "folder");
        final File file = new File(folder, "file");
        folder.mkdirs();
        file.createNewFile();
        
        h.reset();
        h.setExpectedParents(new File[] { folder.getParentFile() });
        GitUtils.runWithoutIndexing(new Callable<Void>() {
            @Override
            public Void call () throws Exception {
                return null;
            }
        }, folder);
        assertEquals(1, h.bridgeAccessed);
        assertTrue(h.expectedParents.isEmpty());
        
        h.reset();
        h.setExpectedParents(new File[0]);
        // does not throw err
        GitUtils.runWithoutIndexing(new Callable<Void>() {
            @Override
            public Void call () throws Exception {
                return null;
            }
        });
        assertEquals(0, h.bridgeAccessed);
        
        h.reset();
        h.setExpectedParents(new File[] { folder.getParentFile(), folder });
        GitUtils.runWithoutIndexing(new Callable<Void>() {
            @Override
            public Void call () throws Exception {
                return null;
            }
        }, file, folder);
        assertEquals(2, h.bridgeAccessed);
        assertTrue(h.expectedParents.isEmpty());
        
        h.reset();
        h.setExpectedParents(new File[] { folder.getParentFile() });
        GitUtils.runWithoutIndexing(new Callable<Void>() {
            @Override
            public Void call () throws Exception {
                GitUtils.runWithoutIndexing(new Callable<Void>() {
                    @Override
                    public Void call () throws Exception {
                        return null;
                    }
                }, file);
                return null;
            }
        }, folder);
        assertEquals(1, h.bridgeAccessed);
        assertTrue(h.expectedParents.isEmpty());
        
        h.reset();
        h.setExpectedParents(new File[] { folder.getParentFile() });
        GitUtils.runWithoutIndexing(new Callable<Void>() {
            @Override
            public Void call () throws Exception {
                boolean error = false;
                try {
                    return GitUtils.runWithoutIndexing(new Callable<Void>() {
                        @Override
                        public Void call () throws Exception {
                            return null;
                        }
                    }, folder);
                } catch (AssertionError err) {
                    assertTrue(err.getMessage().startsWith("Recursive call does not permit different roots"));
                    error = true;
                }
                assertTrue(error);
                return null;
            }
        }, file);
    }

    /**
     * tests that we don't miss any parallelizable commands.
     * If a method is added to GitClient, we NEED to evaluate and decide if it's parallelizable. If it is and we miss the command, the IDE might get blocked.
     * @throws Exception
     */
    public void testExclusiveMethods () throws Exception {
        Set<String> allTestedMethods = new HashSet<String>(Arrays.asList(
                "add",
                "addNotificationListener",
                "blame",
                "catFile",
                "catIndexEntry",
                "checkout",
                "checkoutRevision",
                "cherryPick",
                "clean",
                "close",
                "commit",
                "copyAfter",
                "createBranch",
                "createTag",
                "deleteBranch",
                "deleteTag",
                "exportCommit",
                "exportDiff",
                "fetch",
                "getBranches",
                "getCommonAncestor",
                "getConflicts",
                "getPreviousRevision",
                "getRemote",
                "getRemotes",
                "getRepositoryState",
                "getStatus",
                "getSubmoduleStatus",
                "getTags",
                "getUser",
                "init",
                "initializeSubmodules",
                "ignore",
                "listModifiedIndexEntries",
                "listRemoteBranches",
                "listRemoteTags",
                "log",
                "merge",
                "pull",
                "push",
                "rebase",
                "release",
                "remove",
                "removeNotificationListener",
                "removeRemote",
                "rename",
                "reset",
                "revert",
                "setCallback",
                "setRemote",
                "setUpstreamBranch",
                "stashApply",
                "stashDrop",
                "stashDropAll",
                "stashList",
                "stashSave",
                "unignore",
                "updateReference",
                "updateSubmodules"
        ));
        Set<String> parallelizableMethods = new HashSet<String>(Arrays.asList(
                "addNotificationListener",
                "blame",
                "catFile",
                "catIndexEntry",
                "exportCommit",
                "exportDiff",
                "getBranches",
                "getCommonAncestor",
                "getConflicts",
                "getPreviousRevision",
                "getRemote",
                "getRemotes",
                "getRepositoryState",
                "getStatus",
                "getSubmoduleStatus",
                "getTags",
                "getUser",
                "listModifiedIndexEntries",
                "listRemoteBranches",
                "listRemoteTags",
                "log",
                "removeNotificationListener",
                "removeRemote",
                "setCallback",
                "setRemote",
                "stashList"));
        Field f = GitClient.class.getDeclaredField("PARALLELIZABLE_COMMANDS");
        f.setAccessible(true);
        Set<String> actualParallelizableCommands = (Set<String>) f.get(GitClient.class);

        Method[] methods = getClientMethods();
        Arrays.sort(methods, new Comparator<Method>() {
            @Override
            public int compare(Method o1, Method o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (Method m : methods) {
            String methodName = m.getName();
            assertTrue(methodName, allTestedMethods.contains(methodName));
            if (parallelizableMethods.contains(methodName)) {
                assertTrue(methodName, actualParallelizableCommands.contains(methodName));
                parallelizableMethods.remove(methodName);
            }
        }
        assertTrue(parallelizableMethods.isEmpty());
    }

    public void testExclusiveAccess () throws Exception {
        final File file = new File(repositoryLocation, "aaa");
        file.createNewFile();
        final GitClient client = Git.getInstance().getClient(repositoryLocation);
        final Exception[] exs = new Exception[2];
        final InhibitListener m = new InhibitListener();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.addNotificationListener(m);
                    client.add(new File[] { file }, GitUtils.NULL_PROGRESS_MONITOR);
                } catch (GitException ex) {
                    exs[0] = ex;
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.add(new File[] { file }, GitUtils.NULL_PROGRESS_MONITOR);
                } catch (GitException ex) {
                    exs[1] = ex;
                }
            }
        });
        t1.start();
        m.waitAtBarrier();
        t2.start();
        Thread.sleep(5000);
        assertEquals(Thread.State.BLOCKED, t2.getState());
        m.cont = true;
        t1.join();
        t2.join();
        assertNull(exs[0]);
        assertNull(exs[1]);
    }

    public void testDoNotBlockIndexing () throws Exception {
        final File file = new File(repositoryLocation, "aaa");
        file.createNewFile();
        final GitClient client = Git.getInstance().getClient(repositoryLocation);
        final Exception[] exs = new Exception[2];
        final InhibitListener m = new InhibitListener();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.addNotificationListener(m);
                    client.add(new File[] { file }, GitUtils.NULL_PROGRESS_MONITOR);
                } catch (GitException ex) {
                    exs[0] = ex;
                }
            }
        });

        Git.LOG.setLevel(Level.ALL);
        LogHandler handler = new LogHandler();
        Git.LOG.addHandler(handler);
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.remove(new File[] { file }, false, GitUtils.NULL_PROGRESS_MONITOR);
                } catch (GitException ex) {
                    exs[1] = ex;
                }
            }
        });
        t1.start();
        m.waitAtBarrier();
        t2.start();
        Thread.sleep(5000);
        assertEquals(Thread.State.BLOCKED, t2.getState());
        assertFalse(handler.indexingBridgeCalled);
        m.cont = true;
        t1.join();
        t2.join();
        assertNull(exs[0]);
        assertNull(exs[1]);
    }

    public void testDisableIBInFSEvents () throws Exception {
        File file = new File(repositoryLocation, "aaa");
        file.createNewFile();
        final GitClient client = Git.getInstance().getClient(repositoryLocation);
        client.add(new File[] { file }, GitUtils.NULL_PROGRESS_MONITOR);
        client.commit(new File[] { file }, "msg", null, null, GitUtils.NULL_PROGRESS_MONITOR);

        FileObject fo = FileUtil.toFileObject(file);
        Git.LOG.setLevel(Level.ALL);
        LogHandler handler = new LogHandler();
        Git.LOG.addHandler(handler);

        assertTrue(client.getStatus(new File[] { file }, GitUtils.NULL_PROGRESS_MONITOR).get(file).getStatusHeadIndex() == GitStatus.Status.STATUS_NORMAL);
        fo.delete();
        assertTrue(client.getStatus(new File[] { file }, GitUtils.NULL_PROGRESS_MONITOR).get(file).getStatusHeadIndex() == GitStatus.Status.STATUS_REMOVED);
        assertFalse(handler.indexingBridgeCalled);

        fo.getParent().createData(file.getName());
        assertTrue(client.getStatus(new File[] { file }, GitUtils.NULL_PROGRESS_MONITOR).get(file).getStatusHeadIndex() == GitStatus.Status.STATUS_NORMAL);
        assertFalse(handler.indexingBridgeCalled);

        File copy = new File(repositoryLocation, "copy");
        fo.copy(fo.getParent(), copy.getName(), "");
        assertTrue(client.getStatus(new File[] { copy }, GitUtils.NULL_PROGRESS_MONITOR).get(copy).getStatusHeadIndex() == GitStatus.Status.STATUS_ADDED);
        assertFalse(handler.indexingBridgeCalled);

        File renamed = new File(repositoryLocation, "renamed");
        FileLock lock = fo.lock();
        fo.move(lock, fo.getParent(), renamed.getName(), "");
        lock.releaseLock();
        assertTrue(client.getStatus(new File[] { file }, GitUtils.NULL_PROGRESS_MONITOR).get(file).getStatusHeadIndex() == GitStatus.Status.STATUS_REMOVED);
        assertTrue(client.getStatus(new File[] { renamed }, GitUtils.NULL_PROGRESS_MONITOR).get(renamed).getStatusHeadIndex() == GitStatus.Status.STATUS_ADDED);
        assertFalse(handler.indexingBridgeCalled);
    }

    public void testParallellizableCommands () throws Exception {
        final File file = new File(repositoryLocation, "aaa");
        file.createNewFile();
        final GitClient client = Git.getInstance().getClient(repositoryLocation);
        final Exception[] exs = new Exception[2];
        final InhibitListener m = new InhibitListener();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.addNotificationListener(m);
                    client.add(new File[] { file }, GitUtils.NULL_PROGRESS_MONITOR);
                } catch (GitException ex) {
                    exs[0] = ex;
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.getStatus(new File[] { file }, GitUtils.NULL_PROGRESS_MONITOR);
                } catch (GitException ex) {
                    exs[1] = ex;
                }
            }
        });
        t1.start();
        m.waitAtBarrier();
        t2.start();
        t2.join(5000);
        assertEquals(Thread.State.TERMINATED, t2.getState());
        m.cont = true;
        t1.join();
        assertNull(exs[0]);
        assertNull(exs[1]);
    }

    public void testCancelCommand () throws Exception {
        final File file = new File(repositoryLocation, "aaa");
        file.createNewFile();
        final File file2 = new File(repositoryLocation, "aaa2");
        file2.createNewFile();

        final GitClient client = Git.getInstance().getClient(repositoryLocation);
        final InhibitListener m = new InhibitListener();
        final Exception[] exs = new Exception[2];
        final DefaultProgressMonitor pm = new DefaultProgressMonitor();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.addNotificationListener(m);
                    client.add(new File[] { file, file2 }, pm);
                } catch (GitException ex) {
                    exs[0] = ex;
                }
            }
        });
        t1.start();
        m.waitAtBarrier();
        pm.cancel();
        m.cont = true;
        t1.join();
        assertTrue(pm.isCanceled());
        assertEquals(1, m.count);
    }

    public void testCancelWaitingOnBlockedRepository () throws Exception {
        final File file = new File(repositoryLocation, "aaa");
        file.createNewFile();
        final GitClient client = Git.getInstance().getClient(repositoryLocation);
        final Exception[] exs = new Exception[2];
        final InhibitListener m = new InhibitListener();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.addNotificationListener(m);
                    client.add(new File[] { file }, GitUtils.NULL_PROGRESS_MONITOR);
                } catch (GitException ex) {
                    exs[0] = ex;
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.add(new File[] { file }, GitUtils.NULL_PROGRESS_MONITOR);
                } catch (GitException ex) {
                    exs[1] = ex;
                }
            }
        });
        t1.start();
        m.waitAtBarrier();
        t2.start();
        Thread.sleep(5000);
        assertEquals(Thread.State.BLOCKED, t2.getState());
        t2.interrupt();
        m.cont = true;
        t1.join();
        t2.join();
        assertNull(exs[0]);
        assertTrue(exs[1] instanceof GitCanceledException);
    }

    public void testCancelSupport () throws Exception {
        final File file = new File(repositoryLocation, "aaa");
        file.createNewFile();
        final File file2 = new File(repositoryLocation, "aaa2");
        file2.createNewFile();

        final GitClient client = Git.getInstance().getClient(repositoryLocation);
        final InhibitListener m = new InhibitListener();
        final Exception[] exs = new Exception[2];
        GitProgressSupport supp = new GitProgressSupport() {
            @Override
            public void perform () {
                try {
                    client.addNotificationListener(m);
                    client.add(new File[] { file, file2 }, getProgressMonitor());
                } catch (GitException ex) {
                    exs[0] = ex;
                }
            }
        };
        Task t = supp.start(Git.getInstance().getRequestProcessor(repositoryLocation), repositoryLocation, "Git Add");
        m.waitAtBarrier();
        supp.cancel();
        m.cont = true;
        t.waitFinished();
        assertTrue(supp.isCanceled());
        assertEquals(1, m.count);
    }

    public void testSupportDisplayNames () throws Exception {
        final File file = new File(repositoryLocation, "aaa");
        file.createNewFile();
        final File file2 = new File(repositoryLocation, "aaa2");
        file2.createNewFile();
        final File file3 = new File(repositoryLocation, "aaa3");
        file3.createNewFile();

        final GitClient client = Git.getInstance().getClient(repositoryLocation);
        final FileListener[] ms = new FileListener[1];
        final Exception[] exs = new Exception[2];
        ProgressLogHandler h = new ProgressLogHandler();
        Logger log = Logger.getLogger(GitProgressSupport.class.getName());
        log.addHandler(h);
        log.setLevel(Level.ALL);
        RequestProcessor rp = Git.getInstance().getRequestProcessor(repositoryLocation);

        final boolean[] flags = new boolean[6];

        Task preceedingTask = rp.post(new Runnable() {
            @Override
            public void run () {
                // barrier
                flags[0] = true;
                // wait for asserts
                while (!flags[1]) {
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {}
                }
            }
        });

        final InhibitListener m = new InhibitListener();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.addNotificationListener(m);
                    client.add(new File[] { file3 }, GitUtils.NULL_PROGRESS_MONITOR);
                } catch (GitException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        m.cont = false;
        thread.start();

        GitProgressSupport supp = new GitProgressSupport() {
            @Override
            public void perform () {
                FileListener list;
                ms[0] = list = new FileListener () {
                    @Override
                    public void notifyFile(File file, String relativePathToRoot) {
                        // barrier
                        flags[4] = true;
                        // wait for asserts
                        while (!flags[5]) {
                            try {
                                Thread.sleep(100);
                            } catch (Exception e) {}
                        }
                        setProgress(file.getName());
                    }
                };
                try {
                    // barrier
                    flags[2] = true;
                    // wait for asserts
                    while (!flags[3]) {
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {}
                    }
                    GitClient client = getClient();
                    client.addNotificationListener(list);
                    client.add(new File[] { file, file2 }, getProgressMonitor());
                } catch (GitException ex) {
                    exs[0] = ex;
                }
            }
        };
        List<String> expectedMessages = new LinkedList<String>();
        expectedMessages.add("Git Add - Queued");
        Task t = supp.start(rp, repositoryLocation, "Git Add");
        assertEquals(expectedMessages, h.progressMessages);
        flags[1] = true;
        preceedingTask.waitFinished();
        expectedMessages.add("Git Add");
        for (int i = 0; i < 100; ++i) {
            if (flags[2]) break;
            Thread.sleep(100);
        }
        assertTrue(flags[2]);
        assertEquals(expectedMessages, h.progressMessages);
        flags[3] = true;

        expectedMessages.add("Git Add - Queued on " + repositoryLocation.getName());
        for (int i = 0; i < 100; ++i) {
            if (expectedMessages.equals(h.progressMessages)) break;
            Thread.sleep(100);
        }
        assertEquals(expectedMessages, h.progressMessages);
        m.cont = true;
        thread.join();

        for (int i = 0; i < 100; ++i) {
            if (flags[4]) break;
            Thread.sleep(100);
        }
        assertTrue(flags[4]);
        expectedMessages.add("Git Add");
        assertEquals(expectedMessages, h.progressMessages);
        flags[5] = true;

        t.waitFinished();
        expectedMessages.add("Git Add - " + file.getName());
        expectedMessages.add("Git Add - " + file2.getName());
        assertEquals(expectedMessages, h.progressMessages);
    }

    private Method[] getClientMethods () {
        Set<Method> methods = new LinkedHashSet<Method>(Arrays.asList(org.netbeans.libs.git.GitClient.class.getMethods()));
        methods.removeAll(Arrays.asList(Object.class.getMethods()));
        return methods.toArray(new Method[0]);
    }

    private static class InhibitListener implements FileListener {
        private boolean cont;
        private boolean barrierAccessed;
        private int count;
        @Override
        public void notifyFile (File file, String relativePathToRoot) {
            barrierAccessed = true;
            ++count;
            while (!cont) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            }
        }

        private void waitAtBarrier() throws InterruptedException {
            for (int i = 0; i < 100; ++i) {
                if (barrierAccessed) {
                    break;
                }
                Thread.sleep(100);
            }
            assertTrue(barrierAccessed);
        }
    }

    private class LogHandler extends Handler {

        int bridgeAccessed;
        private HashSet<File> expectedParents;
        private boolean indexingBridgeCalled;

        @Override
        public void publish (LogRecord record) {
            if (record.getLoggerName().equals(indexingLogger.getName())) {
                ++bridgeAccessed;
                for (File f : expectedParents) {
                    if (record.getMessage().equals("scheduling for fs refresh: [" + f + "]")) {
                        expectedParents.remove(f);
                        break;
                    }
                }
            } else if (record.getLoggerName().equals(Git.LOG.getName())) {
                if (record.getMessage().contains("Running block in indexing bridge")) {
                    indexingBridgeCalled = true;
                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        private void reset() {
            bridgeAccessed = 0;
            indexingBridgeCalled = false;
        }

        private void setExpectedParents(File[] files) {
            this.expectedParents = new HashSet<File>(Arrays.asList(files));
        }
    }

    private static class ProgressLogHandler extends Handler {

        List<String> progressMessages = new LinkedList<String>();

        @Override
        public void publish (LogRecord record) {
            if (record.getMessage().equals("New status of progress: {0}")) {
                progressMessages.add((String) record.getParameters()[0]);
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
