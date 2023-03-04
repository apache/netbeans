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

package org.netbeans.libs.git.jgit.commands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitObjectType;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;

/**
 *
 * @author ondra
 */
public class CatTest extends AbstractGitTestCase {
    private Repository repository;
    private File workDir;

    public CatTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }

    public void testCat () throws Exception {
        File folder = new File(workDir, "folder");
        folder.mkdirs();
        File f = new File(folder, "testcat1");
        copyFile(getGoldenFile(), f);
        assertFile(getGoldenFile(), f);
        add(f);
        GitClient client = getClient(workDir);
        try {
            client.catFile(f, Constants.HEAD, new FileOutputStream(f), NULL_PROGRESS_MONITOR);
            fail();
        } catch (GitException.MissingObjectException ex) {
            assertEquals(GitObjectType.COMMIT, ex.getObjectType());
            assertEquals(Constants.HEAD, ex.getObjectName());
        }
        commit(f);

        assertTrue(client.catFile(f, Constants.HEAD, new FileOutputStream(f), NULL_PROGRESS_MONITOR));
        assertFile(f, getGoldenFile());

        String revision = new Git(repository).log().call().iterator().next().getId().getName();
        assertTrue(client.catFile(f, revision, new FileOutputStream(f), NULL_PROGRESS_MONITOR));
        assertFile(f, getGoldenFile());

        write(f, "blablabla");
        add(f);
        commit(f);
        assertTrue(client.catFile(f, revision, new FileOutputStream(f), NULL_PROGRESS_MONITOR));
        assertFile(f, getGoldenFile());
    }

    public void testCatIndex () throws Exception {
        File folder = new File(workDir, "folder");
        folder.mkdirs();
        File f = new File(folder, "testcat1");
        copyFile(getGoldenFile(), f);
        assertFile(getGoldenFile(), f);
        GitClient client = getClient(workDir);
        assertFalse(client.catIndexEntry(f, 0, new FileOutputStream(new File("temp")), NULL_PROGRESS_MONITOR));
        
        add(f);

        assertTrue(client.catIndexEntry(f, 0, new FileOutputStream(f), NULL_PROGRESS_MONITOR));
        assertFile(f, getGoldenFile());
    }

    public void testCatRemoved () throws Exception {
        File f = new File(workDir, "removed");
        copyFile(getGoldenFile(), f);
        assertFile(getGoldenFile(), f);
        add(f);
        commit(f);

        GitClient client = getClient(workDir);
        String revision = new Git(repository).log().call().iterator().next().getId().getName();

        // remove and commit
        client.remove(new File[] { f }, false, NULL_PROGRESS_MONITOR);
        commit(f);
        assertTrue(client.catFile(f, revision, new FileOutputStream(f), NULL_PROGRESS_MONITOR));
        assertFile(f, getGoldenFile());

        assertFalse(client.catFile(f, Constants.HEAD, new FileOutputStream(f), NULL_PROGRESS_MONITOR));
    }
    public void testLineEndingsWindows () throws Exception {
        if (!isWindows()) {
            return;
        }
        // lets turn autocrlf on
        Thread.sleep(1100);
        StoredConfig cfg = repository.getConfig();
        cfg.setString(ConfigConstants.CONFIG_CORE_SECTION, null, ConfigConstants.CONFIG_KEY_AUTOCRLF, "true");
        cfg.save();
        
        File f = new File(workDir, "f");
        write(f, "a\r\nb\r\n");
        File[] roots = new File[] { f };
        
        GitClient client = getClient(workDir);
        runExternally(workDir, Arrays.asList("git.cmd", "add", "f"));
        List<String> res = runExternally(workDir, Arrays.asList("git.cmd", "status", "-s"));
        assertEquals(Arrays.asList("A  f"), res);
        DirCacheEntry e1 = repository.readDirCache().getEntry("f");
        runExternally(workDir, Arrays.asList("git.cmd", "commit", "-m", "hello"));
        
        write(f, "a\r\nb\r\nc\r\n");
        res = runExternally(workDir, Arrays.asList("git.cmd", "status", "-s"));
        assertEquals(Arrays.asList(" M f"), res);
        runExternally(workDir, Arrays.asList("git.cmd", "add", "f"));
        res = runExternally(workDir, Arrays.asList("git.cmd", "status", "-s"));
        assertEquals(Arrays.asList("M  f"), res);
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR), workDir, f, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_MODIFIED, false);
        
        FileOutputStream fo = new FileOutputStream(f);
        client.catFile(f, "HEAD", fo, NULL_PROGRESS_MONITOR);
        fo.close();
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR), workDir, f, true, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_MODIFIED, GitStatus.Status.STATUS_NORMAL, false);
        res = runExternally(workDir, Arrays.asList("git.cmd", "status", "-s"));
        assertEquals(Arrays.asList("MM f"), res);

        client.reset("HEAD", GitClient.ResetType.MIXED, NULL_PROGRESS_MONITOR);
        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR), workDir, f, true, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, GitStatus.Status.STATUS_NORMAL, false);
        assertEquals(e1.getObjectId(), repository.readDirCache().getEntry("f").getObjectId());
        res = runExternally(workDir, Arrays.asList("git.cmd", "status", "-s"));
        assertEquals(0, res.size());
    }
}
