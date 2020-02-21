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

package org.netbeans.modules.git.remote.cli.jgit.commands;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitConstants;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitStatus;
import org.netbeans.modules.git.remote.cli.GitStatus.Status;
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitConfig;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class AddTest extends AbstractGitTestCase {
    private JGitRepository repository;
    private VCSFileProxy workDir;

    public AddTest (String testName) throws IOException {
        super(testName);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList().contains(getName());
    }

    @Override
    protected boolean isRunAll() {return false;}
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getLocalGitRepository();
    }

    public void testAddNoRoots () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxySupport.createNew(file);
        assertNullDirCacheEntry(Collections.singleton(file));
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.add(new VCSFileProxy[0], m);
        assertEquals(Collections.singleton(file), m.notifiedFiles);
        assertDirCacheSize(1);
        assertDirCacheEntry(Collections.singleton(file));
    }
    
    public void testAddFileToEmptyIndex () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxySupport.createNew(file);

        assertNullDirCacheEntry(Collections.singleton(file));
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.add(new VCSFileProxy[] { file }, m);
        assertEquals(Collections.singleton(file), m.notifiedFiles);
        assertDirCacheSize(1);
        assertDirCacheEntry(Collections.singleton(file));

        // no error while adding the same file twice
        m = new Monitor();
        client.addNotificationListener(m);
        client.add(new VCSFileProxy[] { file }, m);
        assertEquals(Collections.<VCSFileProxy>emptySet(), m.notifiedFiles);
        assertDirCacheEntry(Collections.singleton(file));

        write(file, "hello, i've changed");
        assertDirCacheEntryModified(Collections.singleton(file));
        m = new Monitor();
        client.addNotificationListener(m);
        client.add(new VCSFileProxy[] { file }, m);
        assertEquals(Collections.singleton(file), m.notifiedFiles);
        assertDirCacheSize(1);
        assertDirCacheEntry(Collections.singleton(file));
    }

    public void testAddFileToNonEmptyIndex () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxySupport.createNew(file);
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(workDir, "file2");
        VCSFileProxySupport.createNew(file2);
        VCSFileProxy file3 = VCSFileProxy.createFileProxy(workDir, "file3");
        VCSFileProxySupport.createNew(file3);

        assertNullDirCacheEntry(Arrays.asList(file, file2, file3));
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.add(new VCSFileProxy[] { file }, m);
        assertEquals(Collections.singleton(file), m.notifiedFiles);
        assertDirCacheSize(1);
        assertDirCacheEntry(Arrays.asList(file));
        assertNullDirCacheEntry(Arrays.asList(file2, file3));

        m = new Monitor();
        client.addNotificationListener(m);
        client.add(new VCSFileProxy[] { file2 }, m);
        assertEquals(Collections.singleton(file2), m.notifiedFiles);
        assertDirCacheSize(2);
        assertDirCacheEntry(Arrays.asList(file, file2));
        assertNullDirCacheEntry(Arrays.asList(file3));

        m = new Monitor();
        client.addNotificationListener(m);
        client.add(new VCSFileProxy[] { file, file2 }, m);
        assertEquals(Collections.<VCSFileProxy>emptySet(), m.notifiedFiles);
        assertDirCacheSize(2);
        assertDirCacheEntry(Arrays.asList(file, file2));
        assertNullDirCacheEntry(Arrays.asList(file3));
    }

    public void testAddFolder () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        write(file, "file");
        VCSFileProxy folder1 = VCSFileProxy.createFileProxy(workDir, "folder1");
        VCSFileProxySupport.mkdirs(folder1);
        VCSFileProxy file1_1 = VCSFileProxy.createFileProxy(folder1, "file1");
        write(file1_1, "file1_1");
        VCSFileProxy file1_2 = VCSFileProxy.createFileProxy(folder1, "file2");
        write(file1_2, "file1_2");
        VCSFileProxy subfolder1 = VCSFileProxy.createFileProxy(folder1, "subfolder");
        VCSFileProxySupport.mkdirs(subfolder1);
        VCSFileProxy file1_1_1 = VCSFileProxy.createFileProxy(subfolder1, "file1");
        write(file1_1_1, "file1_1_1");
        VCSFileProxy file1_1_2 = VCSFileProxy.createFileProxy(subfolder1, "file2");
        write(file1_1_2, "file1_1_2");

        VCSFileProxy folder2 = VCSFileProxy.createFileProxy(workDir, "folder2");
        VCSFileProxySupport.mkdirs(folder2);
        VCSFileProxy file2_1 = VCSFileProxy.createFileProxy(folder2, "file1");
        write(file2_1, "file2_1");
        VCSFileProxy file2_2 = VCSFileProxy.createFileProxy(folder2, "file2");
        write(file2_2, "file2_2");
        VCSFileProxy subfolder2 = VCSFileProxy.createFileProxy(folder2, "subfolder");
        VCSFileProxySupport.mkdirs(subfolder2);
        VCSFileProxy file2_1_1 = VCSFileProxy.createFileProxy(subfolder2, "file1");
        write(file2_1_1, "file2_1_1");
        VCSFileProxy file2_1_2 = VCSFileProxy.createFileProxy(subfolder2, "file2");
        write(file2_1_2, "file2_1_2");

        assertNullDirCacheEntry(Arrays.asList(file, file1_1, file1_2, file1_1_1, file1_1_2, file2_1, file2_2, file2_1_1, file2_1_2));
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        client.add(new VCSFileProxy[] { subfolder1 }, m);
        assertEquals(new HashSet<VCSFileProxy>(Arrays.asList(file1_1_1, file1_1_2)), m.notifiedFiles);
        assertDirCacheSize(2);
        assertDirCacheEntry(Arrays.asList(file1_1_1, file1_1_2));
        assertNullDirCacheEntry(Arrays.asList(file, file1_1, file1_2, file2_1, file2_2, file2_1_1, file2_1_2));

        m = new Monitor();
        client.addNotificationListener(m);
        client.add(new VCSFileProxy[] { folder1 }, m);
        assertEquals(new HashSet<VCSFileProxy>(Arrays.asList(file1_1, file1_2)), m.notifiedFiles);
        assertDirCacheSize(4);
        assertDirCacheEntry(Arrays.asList(file1_1, file1_2, file1_1_1, file1_1_2));
        assertNullDirCacheEntry(Arrays.asList(file, file2_1, file2_1_1, file2_1_2));

        m = new Monitor();
        client.addNotificationListener(m);
        client.add(new VCSFileProxy[] { folder2 }, m);
        assertEquals(new HashSet<VCSFileProxy>(Arrays.asList(file2_1, file2_2, file2_1_1, file2_1_2)), m.notifiedFiles);
        assertDirCacheSize(8);
        assertDirCacheEntry(Arrays.asList(file1_1, file1_2, file1_1_1, file1_1_2, file2_1, file2_2, file2_1_1, file2_1_2));
    }

    public void testAddIgnored () throws Exception {
        VCSFileProxy folder1 = VCSFileProxy.createFileProxy(workDir, "folder1");
        VCSFileProxySupport.mkdirs(folder1);
        VCSFileProxy file1_1 = VCSFileProxy.createFileProxy(folder1, "file1_1");
        write(file1_1, "file1_1");
        VCSFileProxy file1_2 = VCSFileProxy.createFileProxy(folder1, "file1_2");
        write(file1_2, "file1_2");

        VCSFileProxy folder2 = VCSFileProxy.createFileProxy(workDir, "folder2");
        VCSFileProxySupport.mkdirs(folder2);
        VCSFileProxy file2_1 = VCSFileProxy.createFileProxy(folder2, "file2_1");
        write(file2_1, "file2_1");
        VCSFileProxy file2_2 = VCSFileProxy.createFileProxy(folder2, "file2_2");
        write(file2_2, "file2_2");

        write(VCSFileProxy.createFileProxy(workDir, GitConstants.GITIGNORE_FILENAME), "file1_1\nfolder2");

        assertNullDirCacheEntry(Arrays.asList(file1_1, file2_1, file1_2, file2_2));
        GitClient client = getClient(workDir);
        Monitor m = new Monitor();
        client.addNotificationListener(m);
        try {
            client.add(new VCSFileProxy[] { folder2 }, m);
            fail();
        } catch (GitException ex) {
            // add do not allow ignored files
        }
        client.add(new VCSFileProxy[] { folder1 }, m);
        assertEquals(new HashSet<VCSFileProxy>(Arrays.asList(file1_2)), m.notifiedFiles);
        assertDirCacheSize(1);
        assertDirCacheEntry(Arrays.asList(file1_2));
        assertNullDirCacheEntry(Arrays.asList(file1_1, file2_1, file2_2));
    }
    
    public void testAddIgnoreExecutable () throws Exception {
        if (isWindows()) {
            // no reason to test on windows
            return;
        }
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        write(f, "hi, i am executable");
        VCSFileProxySupport.setExecutable(f, true);
        VCSFileProxy[] roots = { f };
        GitClient client = getClient(workDir);
        JGitConfig config = repository.getConfig();
        config.setBoolean(JGitConfig.CONFIG_CORE_SECTION, null, JGitConfig.CONFIG_KEY_FILEMODE, false);
        config.save();
        // add should not set executable bit in index
        add(roots);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(roots, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, f, true, Status.STATUS_ADDED, Status.STATUS_NORMAL, Status.STATUS_ADDED, false);
        
        // index should differ from wt
        config.setBoolean(JGitConfig.CONFIG_CORE_SECTION, null, JGitConfig.CONFIG_KEY_FILEMODE, true);
        config.save();
        statuses = client.getStatus(roots, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, f, true, Status.STATUS_ADDED, Status.STATUS_MODIFIED, Status.STATUS_ADDED, false);
    }
    
    public void testAddKeepExecutableInIndex () throws Exception {
        if (isWindows()) {
            // no reason to test on windows
            return;
        }
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        write(f, "hi, i am executable");
        VCSFileProxySupport.setExecutable(f, true);
        VCSFileProxy[] roots = { f };
        GitClient client = getClient(workDir);
        add(roots);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(roots, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, f, true, Status.STATUS_ADDED, Status.STATUS_NORMAL, Status.STATUS_ADDED, false);
        
        JGitConfig config = repository.getConfig();
        config.setBoolean(JGitConfig.CONFIG_CORE_SECTION, null, JGitConfig.CONFIG_KEY_FILEMODE, false);
        config.save();
        // add should not overwrite executable bit in index
        VCSFileProxySupport.setExecutable(f, false);
        add(roots);
        statuses = client.getStatus(roots, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, f, true, Status.STATUS_ADDED, Status.STATUS_NORMAL, Status.STATUS_ADDED, false);
        
        // index should differ from wt
        config.setBoolean(JGitConfig.CONFIG_CORE_SECTION, null, JGitConfig.CONFIG_KEY_FILEMODE, true);
        config.save();
        statuses = client.getStatus(roots, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, f, true, Status.STATUS_ADDED, Status.STATUS_MODIFIED, Status.STATUS_ADDED, false);
    }
    
    public void testUpdateIndexIgnoreExecutable () throws Exception {
        if (isWindows()) {
            // no reason to test on windows
            return;
        }
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        write(f, "hi, i am not executable");
        VCSFileProxy[] roots = { f };
        add(roots);
        commit(roots);
        VCSFileProxySupport.setExecutable(f, true);
        GitClient client = getClient(workDir);
        JGitConfig config = repository.getConfig();
        config.setBoolean(JGitConfig.CONFIG_CORE_SECTION, null, JGitConfig.CONFIG_KEY_FILEMODE, false);
        config.save();
        write(f, "hi, i am executable");
        // add should not set executable bit in index
        add(roots);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(roots, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, f, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false);
        
        // index should differ from wt
        config.setBoolean(JGitConfig.CONFIG_CORE_SECTION, null, JGitConfig.CONFIG_KEY_FILEMODE, true);
        config.save();
        statuses = client.getStatus(roots, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, f, true, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false);
    }

    public void testCancel () throws Exception {
        final VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
        VCSFileProxySupport.createNew(file);
        final VCSFileProxy file2 = VCSFileProxy.createFileProxy(workDir, "file2");
        VCSFileProxySupport.createNew(file2);

        final Monitor m = new Monitor();
        final GitClient client = getClient(workDir);
        final Exception[] exs = new Exception[1];
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.addNotificationListener(m);
                    client.add(new VCSFileProxy[] { file, file2 },m);
                } catch (GitException ex) {
                    exs[0] = ex;
                }
            }
        });
        m.cont = false;
        t1.start();
        m.waitAtBarrier();
        m.cancel();
        m.cont = true;
        t1.join();
        assertTrue(m.isCanceled());
        assertEquals(1, m.count);
        assertEquals(null, exs[0]);
    }
    
    public void testAddNested () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        write(f, "file");
        
        GitClient client = getClient(workDir);
        client.add(new VCSFileProxy[] { f }, NULL_PROGRESS_MONITOR);
        client.commit(new VCSFileProxy[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        Thread.sleep(1100);
        VCSFileProxy nested = VCSFileProxy.createFileProxy(workDir, "nested");
        VCSFileProxySupport.mkdirs(nested);
        VCSFileProxy f2 = VCSFileProxy.createFileProxy(nested, "f");
        write(f2, "file");
        GitClient clientNested = getClient(nested);
        clientNested.init(NULL_PROGRESS_MONITOR);
        clientNested.add(new VCSFileProxy[] { f2 }, NULL_PROGRESS_MONITOR);
        clientNested.commit(new VCSFileProxy[] { f2 }, "aaa", null, null, NULL_PROGRESS_MONITOR);
        write(f2, "change");
        
        Thread.sleep(1000);
        client.add(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, f, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        // nested should be added as gitlink
        if (StatusCommand.KIT)
        assertStatus(statuses, workDir, nested, true, Status.STATUS_ADDED, Status.STATUS_NORMAL, Status.STATUS_ADDED, false);
        else
        assertStatus(statuses, workDir, nested, true, Status.STATUS_ADDED, Status.STATUS_MODIFIED, Status.STATUS_ADDED, false);
//        DirCacheEntry e = repository.getRepository().readDirCache().getEntry("nested");
//        assertEquals(FileMode.GITLINK, e.getFileMode());
//        assertEquals(VCSFileProxySupport.length(nested), e.getLength());
//        assertNotSame(ObjectId.zeroId().name(), e.getObjectId().getName());
        
        statuses = clientNested.getStatus(new VCSFileProxy[] { nested }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, nested, f2, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false);
    }
    
    public void testAddMixedLineEndings () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        String content = "";
        for (int i = 0; i < 10000; ++i) {
            content += i + "\r\n";
        }
        write(f, content);
        VCSFileProxy[] files = new VCSFileProxy[] { f };
        GitClient client = getClient(workDir);
        client.add(files, NULL_PROGRESS_MONITOR);
        client.commit(files, "commit", null, null, NULL_PROGRESS_MONITOR);
        
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, f, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        
        // lets turn autocrlf on
        JGitConfig cfg = repository.getConfig();
        cfg.setString(JGitConfig.CONFIG_CORE_SECTION, null, JGitConfig.CONFIG_KEY_AUTOCRLF, "true");
        cfg.save();
        
        // when this starts failing, remove the work around
//        ObjectInserter inserter = repository.getRepository().newObjectInserter();
//        TreeWalk treeWalk = new TreeWalk(repository.getRepository());
//        treeWalk.setFilter(PathFilterGroup.createFromStrings("f"));
//        treeWalk.setRecursive(true);
//        treeWalk.reset();
//        treeWalk.addTree(new FileTreeIterator(repository.getRepository()));
//        while (treeWalk.next()) {
//            String path = treeWalk.getPathString();
//            assertEquals("f", path);
//            WorkingTreeIterator fit = treeWalk.getTree(0, WorkingTreeIterator.class);
//            InputStream in = fit.openEntryStream();
//            try {
//                inserter.insert(Constants.OBJ_BLOB, fit.getEntryLength(), in);
//                fail("this should fail, remove the work around");
//            } catch (EOFException ex) {
//                assertEquals("Input did not match supplied length. 10000 bytes are missing.", ex.getMessage());
//            } finally {
//                in.close();
//                inserter.release();
//            }
//            break;
//        }
        
        // no err should occur
        write(f, content + "hello");
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, f, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false);
        client.add(files, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, f, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false);
        client.commit(files, "message", null, null, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, f, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
    }
    
    public void testLineEndingsWindows () throws Exception {
        if (!isWindows()) {
            return;
        }
        // lets turn autocrlf on
        JGitConfig cfg = repository.getConfig();
        cfg.setString(JGitConfig.CONFIG_CORE_SECTION, null, JGitConfig.CONFIG_KEY_AUTOCRLF, "true");
        cfg.save();
        
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        write(f, "a\r\nb\r\n");
        VCSFileProxy[] roots = new VCSFileProxy[] { f };
        
        GitClient client = getClient(workDir);
        runExternally(workDir, Arrays.asList("add", "f"));
        //DirCacheEntry e1 = repository.getRepository().readDirCache().getEntry("f");
        client.add(roots, NULL_PROGRESS_MONITOR);
        //DirCacheEntry e2 = repository.getRepository().readDirCache().getEntry("f");
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR),
                workDir, f, true, Status.STATUS_ADDED, Status.STATUS_NORMAL, Status.STATUS_ADDED, false);
        List<String> res = runExternally(workDir, Arrays.asList("status", "-s"));
        assertEquals(Arrays.asList("A  f"), res);
        //assertEquals(e1.getFileMode(), e2.getFileMode());
        //assertEquals(e1.getPathString(), e2.getPathString());
        //assertEquals(e1.getRawMode(), e2.getRawMode());
        //assertEquals(e1.getStage(), e2.getStage());
        //assertEquals(e1.getLength(), e2.getLength());
        //assertEquals(e1.getObjectId(), e2.getObjectId());

        write(f, "a\nb\n");
        res = runExternally(workDir, Arrays.asList("status", "-s"));
        assertEquals(Arrays.asList("AM f"), res);
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR),
                workDir, f, true, Status.STATUS_ADDED, Status.STATUS_MODIFIED, Status.STATUS_ADDED, false);
        
        res = runExternally(workDir, Arrays.asList("commit", "-m", "gugu"));
        res = runExternally(workDir, Arrays.asList("checkout", "--", "f"));
        
//        RevCommit commit = Utils.findCommit(repository.getRepository(), GitConstants.HEAD);
//        TreeWalk walk = new TreeWalk(repository.getRepository());
//        walk.reset();
//        walk.addTree(commit.getTree());
//        walk.setFilter(PathFilter.create("f"));
//        walk.setRecursive(true);
//        walk.next();
//        assertEquals("f", walk.getPathString());
//        ObjectLoader loader = repository.getRepository().getObjectDatabase().open(walk.getObjectId(0));
//        assertEquals(4, loader.getSize());
//        assertEquals("a\nb\n", new String(loader.getBytes()));
//        assertEquals(e1.getObjectId(), walk.getObjectId(0));
        
        res = runExternally(workDir, Arrays.asList("status", "-s"));
        assertEquals(0, res.size());
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR),
                workDir, f, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
    }
    
//    public void testAddSymlink () throws Exception {
//        if (isWindows()) {
//            return;
//        }
//        String path = "folder/file";
//        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, path);
//        VCSFileProxySupport.mkdirs(f.getParentFile());
//        write(f, "file");
//        add(f);
//        commit(f);
//        
//        Thread.sleep(1100);
//        
//        // try with commandline client
//        VCSFileProxy link = VCSFileProxy.createFileProxy(workDir, "link");
//        runExternally(workDir, Arrays.asList("ln", "-s", path, link.getName()));
//        long ts = getLinkLastModified(link);
//        runExternally(workDir, Arrays.asList("git", "add", link.getName()));
//        DirCacheEntry e = repository.getRepository().readDirCache().getEntry(link.getName());
//        assertEquals(FileMode.SYMLINK, e.getFileMode());
//        ObjectId id = e.getObjectId();
//        assertEquals(ts, e.getLastModified() / 1000 * 1000);
//        ObjectReader reader = repository.getRepository().getObjectDatabase().newReader();
//        assertTrue(reader.has(e.getObjectId()));
//        byte[] bytes = reader.open(e.getObjectId()).getBytes();
//        assertEquals(path, RawParseUtils.decode(bytes));
//        
//        // now with internal
//        VCSFileProxy link2 = VCSFileProxy.createFileProxy(workDir, "link2");
//        VCSFileProxySupport.createSymbolicLink(link2, path);
//        ts = getLinkLastModified(link2);
//        getClient(workDir).add(new VCSFileProxy[] { link2 }, NULL_PROGRESS_MONITOR);
//        
//        DirCacheEntry e2 = repository.getRepository().readDirCache().getEntry(link2.getName());
//        assertEquals(FileMode.SYMLINK, e2.getFileMode());
//        assertEquals(id, e2.getObjectId());
//        assertEquals(0, e2.getLength());
//        assertEquals(ts, e2.getLastModified() / 1000 * 1000);
//        assertTrue(reader.has(e2.getObjectId()));
//        bytes = reader.open(e2.getObjectId()).getBytes();
//        assertEquals(path, RawParseUtils.decode(bytes));
//        reader.release();
//    }
    
    public void testAddMissingSymlink () throws Exception {
        if (isWindows()) {
            return;
        }
        String path = "folder/file";
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, path);
        
        // try with commandline client
        VCSFileProxy link = VCSFileProxy.createFileProxy(workDir, "link");
        VCSFileProxySupport.createSymbolicLink(link, path);
        getClient(workDir).add(new VCSFileProxy[] { link }, NULL_PROGRESS_MONITOR);
        //DirCacheEntry e = repository.getRepository().readDirCache().getEntry(link.getName());
        //assertEquals(FileMode.SYMLINK, e.getFileMode());
        //assertEquals(0, e.getLength());
        //ObjectReader reader = repository.getRepository().getObjectDatabase().newReader();
        //assertTrue(reader.has(e.getObjectId()));
        //byte[] bytes = reader.open(e.getObjectId()).getBytes();
        //assertEquals(path, RawParseUtils.decode(bytes));
        //reader.release();
    }
    
    public void testAdd_243092 () throws Exception {
        VCSFileProxy folder1 = VCSFileProxy.createFileProxy(workDir, "folder1");
        VCSFileProxy file1 = VCSFileProxy.createFileProxy(folder1, "file1");
        VCSFileProxy folder2 = VCSFileProxy.createFileProxy(workDir, "folder2");
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(folder2, "file2");
        VCSFileProxySupport.mkdirs(folder1);
        VCSFileProxySupport.mkdirs(folder2);
        
        write(file1, "init");
        add();
        commit();
        
        VCSFileProxySupport.delete(file1);
        VCSFileProxySupport.delete(folder1);
        assertFalse(folder1.exists());
        
        write(file2, "init");
        getClient(workDir).add(new VCSFileProxy[0], NULL_PROGRESS_MONITOR);
        Map<VCSFileProxy, GitStatus> statuses = getClient(workDir).getStatus(new VCSFileProxy[0], NULL_PROGRESS_MONITOR);
        if (false)
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_REMOVED, Status.STATUS_REMOVED, false);
        else
        assertStatus(statuses, workDir, file1, true, Status.STATUS_REMOVED, Status.STATUS_NORMAL, Status.STATUS_REMOVED, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_ADDED, Status.STATUS_NORMAL, Status.STATUS_ADDED, false);
    }

    private void assertDirCacheEntry (Collection<VCSFileProxy> files) throws IOException {
        assertDirCacheEntry(repository, workDir, files);
    }

    private void assertDirCacheEntryModified (Collection<VCSFileProxy> files) throws IOException {
//        DirCache cache = repository.getRepository().lockDirCache();
//        for (VCSFileProxy f : files) {
//            String relativePath = Utils.getRelativePath(workDir, f);
//            DirCacheEntry e = cache.getEntry(relativePath);
//            assertNotNull(e);
//            assertEquals(relativePath, e.getPathString());
//            InputStream in = f.getInputStream(false);
//            try {
//                assertNotSame(e.getObjectId(), repository.getRepository().newObjectInserter().idFor(Constants.OBJ_BLOB, VCSFileProxySupport.length(f), in));
//            } finally {
//                in.close();
//            }
//        }
//        cache.unlock();
    }

    private void assertNullDirCacheEntry (Collection<VCSFileProxy> files) throws Exception {
//        DirCache cache = repository.getRepository().lockDirCache();
//        for (VCSFileProxy f : files) {
//            DirCacheEntry e = cache.getEntry(Utils.getRelativePath(workDir, f));
//            assertNull(e);
//        }
//        cache.unlock();
    }

    private void assertDirCacheSize (int expectedSize) throws IOException {
//        DirCache cache = repository.getRepository().lockDirCache();
//        try {
//            assertEquals(expectedSize, cache.getEntryCount());
//        } finally {
//            cache.unlock();
//        }
    }
}
