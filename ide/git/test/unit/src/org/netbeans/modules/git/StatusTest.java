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

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.junit.MockServices;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.ui.ignore.IgnoreAction;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.masterfs.VersioningAnnotationProvider;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author ondra
 */
public class StatusTest extends AbstractGitTestCase {

    public StatusTest (String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(new Class[] {
            VersioningAnnotationProvider.class,
            GitVCS.class});
        Git.STATUS_LOG.setLevel(Level.ALL);
        setAutomaticRefreshEnabled(true);
    }

    public void testStatusOnNoRepository () throws Exception {
        File folder = createFolder(repositoryLocation.getParentFile(), "folder");
        GitClient client = getClient(folder);
        Map<File, GitStatus> statuses = client.getStatus(new File[] { folder }, GitUtils.NULL_PROGRESS_MONITOR);
        assertTrue(statuses.isEmpty());
    }

    public void testStatusDifferentTree () throws IOException {
        try {
            File folder = createFolder("folder");
            getCache().refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repositoryLocation, Collections.singleton(folder)));
            fail("different tree, exception should be thrown");
        } catch (IllegalArgumentException ex) {
            // OK
        }
    }

    public void testCacheRefresh () throws Exception {
        setAutomaticRefreshEnabled(false);
        FileStatusCache cache = getCache();
        File unversionedFile = new File(testBase, "file");
        unversionedFile.createNewFile();
        // create new files
        Set<File> newFiles = new HashSet<File>();
        File newFile;
        newFiles.add(newFile = new File(repositoryLocation, "file"));
        newFile.createNewFile();
        Thread.sleep(500);
        assertTrue(cache.getStatus(unversionedFile).getStatus().equals(EnumSet.of(Status.NOTVERSIONED_NOTMANAGED)));
        Thread.sleep(500);
        assertTrue(cache.getStatus(newFile).getStatus().equals(EnumSet.of(Status.UPTODATE)));

        cache.refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repositoryLocation, Collections.singleton(repositoryLocation)));
        assertSameStatus(newFiles, EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE));
        assertEquals(1, cache.listFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES).length);
        assertTrue(cache.containsFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES, true));

        newFiles.add(newFile = new File(repositoryLocation, "file2"));
        newFile.createNewFile();
        Thread.sleep(500);
        assertTrue(cache.getStatus(unversionedFile).getStatus().equals(EnumSet.of(Status.NOTVERSIONED_NOTMANAGED)));
        Thread.sleep(500);
        assertTrue(cache.getStatus(newFile).getStatus().equals(EnumSet.of(Status.UPTODATE)));
        cache.refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repositoryLocation, Collections.singleton(repositoryLocation)));
        assertSameStatus(newFiles, EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE));
        assertEquals(2, cache.listFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES).length);
        assertTrue(cache.containsFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES, true));

        // try refresh on a single file, other statuses should not change
        newFiles.add(newFile = new File(repositoryLocation, "file3"));
        newFile.createNewFile();
        Thread.sleep(500);
        assertTrue(cache.getStatus(unversionedFile).getStatus().equals(EnumSet.of(Status.NOTVERSIONED_NOTMANAGED)));
        Thread.sleep(500);
        assertTrue(cache.getStatus(newFile).getStatus().equals(EnumSet.of(Status.UPTODATE)));
        cache.refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repositoryLocation, Collections.singleton(newFile)));
        assertSameStatus(newFiles, EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE));
        assertEquals(3, cache.listFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES).length);
        assertTrue(cache.containsFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES, true));

        // try refresh on a subfolder file, other statuses should not change
        File folder = new File(repositoryLocation, "folder");
        folder.mkdirs();
        Thread.sleep(500);
        assertTrue(cache.getStatus(unversionedFile).getStatus().equals(EnumSet.of(Status.NOTVERSIONED_NOTMANAGED)));
        cache.refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repositoryLocation, Collections.singleton(folder)));
        assertSameStatus(newFiles, EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE));
        assertEquals(3, cache.listFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES).length);
        assertTrue(cache.containsFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES, true));

        // try refresh on a subfolder file, other statuses should not change
        newFiles.add(newFile = new File(folder, "file4"));
        newFile.createNewFile();
        Thread.sleep(500);
        assertTrue(cache.getStatus(unversionedFile).getStatus().equals(EnumSet.of(Status.NOTVERSIONED_NOTMANAGED)));
        Thread.sleep(500);
        assertTrue(cache.getStatus(newFile).getStatus().equals(EnumSet.of(Status.UPTODATE)));
        cache.refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repositoryLocation, Collections.singleton(newFile)));
        assertSameStatus(newFiles, EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE));
        assertEquals(4, cache.listFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES).length);
        assertTrue(cache.containsFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES, true));
    }

    public void testPingRepository_Refresh () throws Exception {
        File folderA = new File(repositoryLocation, "folderA");
        File fileA1 = new File(folderA, "file1");
        File fileA2 = new File(folderA, "file2");
        folderA.mkdirs();
        fileA1.createNewFile();
        fileA2.createNewFile();
        File folderB = new File(repositoryLocation, "folderB");
        File fileB1 = new File(folderB, "file1");
        File fileB2 = new File(folderB, "file2");
        folderB.mkdirs();
        fileB1.createNewFile();
        fileB2.createNewFile();
        File folderC = new File(repositoryLocation, "folderC");
        File fileC1 = new File(folderC, "file1");
        File fileC2 = new File(folderC, "file2");
        folderC.mkdirs();
        fileC1.createNewFile();
        fileC2.createNewFile();

        StatusRefreshLogHandler handler = new StatusRefreshLogHandler(testBase);
        Git.STATUS_LOG.addHandler(handler);
        handler.setFilesToRefresh(Collections.singleton(folderA));
        FileInformation status = getCache().getStatus(folderA);
        assertTrue(status.containsStatus(Status.UPTODATE));
        assertTrue(handler.waitForFilesToRefresh());
        status = getCache().getStatus(fileA1);
        assertTrue(status.containsStatus(Status.NEW_INDEX_WORKING_TREE));
        status = getCache().getStatus(fileA2);
        assertTrue(status.containsStatus(Status.NEW_INDEX_WORKING_TREE));

        handler.setFilesToRefresh(Collections.singleton(fileA2));
        status = getCache().getStatus(fileA2);
        assertTrue(status.containsStatus(Status.NEW_INDEX_WORKING_TREE));
        assertFalse(handler.waitForFilesToRefresh());
        assertFalse(handler.getFilesRefreshed());

        handler.setFilesToRefresh(Collections.singleton(fileB1));
        status = getCache().getStatus(fileB1);
        assertTrue(status.containsStatus(Status.UPTODATE));
        assertTrue(handler.waitForFilesToRefresh());
        status = getCache().getStatus(fileB1);
        assertTrue(status.containsStatus(Status.NEW_INDEX_WORKING_TREE));

        handler.setFilesToRefresh(Collections.singleton(fileB2));
        status = getCache().getStatus(fileB2);
        assertTrue(status.containsStatus(Status.UPTODATE));
        assertTrue(handler.waitForFilesToRefresh());
        status = getCache().getStatus(fileB1);
        assertTrue(status.containsStatus(Status.NEW_INDEX_WORKING_TREE));
        status = getCache().getStatus(fileB2);
        assertTrue(status.containsStatus(Status.NEW_INDEX_WORKING_TREE));

        handler.setFilesToRefresh(Collections.singleton(folderC));
        status = getCache().getStatus(folderC);
        assertTrue(status.containsStatus(Status.UPTODATE));
        assertTrue(handler.waitForFilesToRefresh());
        status = getCache().getStatus(folderC);
        assertTrue(status.containsStatus(Status.UPTODATE));
        status = getCache().getStatus(fileC1);
        assertTrue(status.containsStatus(Status.NEW_INDEX_WORKING_TREE));
        status = getCache().getStatus(fileC2);
        assertTrue(status.containsStatus(Status.NEW_INDEX_WORKING_TREE));

        handler.setFilesToRefresh(Collections.singleton(folderB));
        status = getCache().getStatus(folderB);
        assertTrue(status.containsStatus(Status.UPTODATE));
        assertTrue(handler.waitForFilesToRefresh());
        status = getCache().getStatus(fileB1);
        assertTrue(status.containsStatus(Status.NEW_INDEX_WORKING_TREE));
        status = getCache().getStatus(fileB2);
        assertTrue(status.containsStatus(Status.NEW_INDEX_WORKING_TREE));

        handler.setFilesToRefresh(Collections.singleton(folderB));
        status = getCache().getStatus(folderB);
        assertFalse(handler.waitForFilesToRefresh());

        handler.setFilesToRefresh(Collections.singleton(repositoryLocation));
        status = getCache().getStatus(repositoryLocation);
        assertTrue(handler.waitForFilesToRefresh());
    }

    public void testIgnoredFile () throws Exception {
        File folderA = new File(repositoryLocation, "folderA");
        File fileA1 = new File(folderA, "file1");
        File fileA2 = new File(folderA, "file2");
        folderA.mkdirs();
        fileA1.createNewFile();
        fileA2.createNewFile();

        File ignoreFile = new File(repositoryLocation, ".gitignore");
        write(ignoreFile, "file1");

        StatusRefreshLogHandler handler = new StatusRefreshLogHandler(testBase);
        Git.STATUS_LOG.addHandler(handler);
        handler.setFilesToRefresh(Collections.singleton(fileA1));
        FileInformation status = getCache().getStatus(fileA1);
        assertTrue(status.containsStatus(Status.UPTODATE));
        assertTrue(handler.waitForFilesToRefresh());
        status = getCache().getStatus(fileA1);
        assertTrue(status.containsStatus(Status.NOTVERSIONED_EXCLUDED));

        write(ignoreFile, "");
        handler.setFilesToRefresh(Collections.singleton(fileA1));
        getCache().refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repositoryLocation, Collections.singleton(fileA1)));
        status = getCache().getStatus(fileA1);
        assertTrue(status.containsStatus(Status.NEW_INDEX_WORKING_TREE));

        write(ignoreFile, "folderA");
        handler.setFilesToRefresh(Collections.singleton(fileA2));
        status = getCache().getStatus(fileA2);
        assertTrue(status.containsStatus(Status.UPTODATE));
        assertTrue(handler.waitForFilesToRefresh());
        status = getCache().getStatus(fileA2);
        assertTrue(status.containsStatus(Status.NOTVERSIONED_EXCLUDED));
        handler.setFilesToRefresh(Collections.singleton(folderA));
        status = getCache().getStatus(folderA);
        assertTrue(status.containsStatus(Status.UPTODATE));
        assertTrue(handler.waitForFilesToRefresh());
        status = getCache().getStatus(fileA1);
        assertTrue(status.containsStatus(Status.NOTVERSIONED_EXCLUDED));

        write(ignoreFile, "");
        getCache().refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repositoryLocation, Collections.singleton(folderA)));
        status = getCache().getStatus(fileA1);
        assertTrue(status.containsStatus(Status.NEW_INDEX_WORKING_TREE));
        status = getCache().getStatus(fileA2);
        assertTrue(status.containsStatus(Status.NEW_INDEX_WORKING_TREE));

        ignoreFile = new File(folderA, ".gitignore");
        write(ignoreFile, "file1");
        getCache().refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repositoryLocation, Collections.singleton(folderA)));
        status = getCache().getStatus(fileA1);
        assertTrue(status.containsStatus(Status.NOTVERSIONED_EXCLUDED));
        status = getCache().getStatus(fileA2);
        assertTrue(status.containsStatus(Status.NEW_INDEX_WORKING_TREE));
    }

    public void testIgnoredBySharability () throws Exception {
        skeletonIgnoredBySharability();
    }

    public void testIgnoredBySharabilityAWT () throws Throwable {
        final Throwable[] th = new Throwable[1];
        Future<Project[]> projectOpenTask = OpenProjects.getDefault().openProjects();
        if (!projectOpenTask.isDone()) {
            try {
                projectOpenTask.get();
            } catch (Exception ex) {
                // not interested
            }
        }
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    skeletonIgnoredBySharability();
                } catch (Throwable t) {
                    th[0] = t;
                }
            }
        });
        if (th[0] != null) {
            throw th[0];
        }
    }

    private void skeletonIgnoredBySharability () throws Exception {
        File folder = new File(repositoryLocation, "folderA");
        File file1 = new File(folder, "notSharable");
        File file2 = new File(folder, "file2");
        folder.mkdirs();
        file1.createNewFile();
        file2.createNewFile();
        StatusRefreshLogHandler handler = new StatusRefreshLogHandler(testBase);
        Git.STATUS_LOG.addHandler(handler);
        handler.setFilesToRefresh(Collections.singleton(file1));
        FileInformation status = getCache().getStatus(file1);
        assertTrue(status.containsStatus(Status.UPTODATE) || status.containsStatus(Status.NOTVERSIONED_EXCLUDED));
        assertTrue(handler.waitForFilesToRefresh());
        status = getCache().getStatus(file1);
        assertTrue(status.containsStatus(Status.NOTVERSIONED_EXCLUDED));

        File newFolder = new File(repositoryLocation, "notSharable");
        folder.renameTo(newFolder);
        file1 = new File(newFolder, file1.getName());
        file2 = new File(newFolder, file2.getName());
        handler.setFilesToRefresh(Collections.singleton(file2));
        status = getCache().getStatus(file2);
        assertTrue(status.containsStatus(Status.UPTODATE) || status.containsStatus(Status.NOTVERSIONED_EXCLUDED));
        handler.waitForFilesToRefresh();
        status = getCache().getStatus(file2);
        assertTrue(status.containsStatus(Status.NOTVERSIONED_EXCLUDED));

        folder = new File(repositoryLocation, "notSharableFolder");
        folder.mkdirs();
        file1 = new File(folder, "file1");
        file1.createNewFile();
        handler.setFilesToRefresh(Collections.singleton(folder));
        status = getCache().getStatus(folder);
        assertTrue(status.containsStatus(Status.UPTODATE) || status.containsStatus(Status.NOTVERSIONED_EXCLUDED));
        handler.waitForFilesToRefresh();
        waitForStatus(folder, Status.NOTVERSIONED_EXCLUDED);
        waitForStatus(file1, Status.NOTVERSIONED_EXCLUDED);
    }

    public void testPurgeRemovedIgnoredFiles () throws Exception {
        File folder = new File(repositoryLocation, "folder");
        final File file1 = new File(folder, "ignored");
        folder.mkdirs();
        file1.createNewFile();

        File ignoreFile = new File(repositoryLocation, ".gitignore");
        write(ignoreFile, "ignored");
        getCache().refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repositoryLocation, Collections.singleton(file1)));
        assertTrue(getCache().getStatus(file1).containsStatus(Status.NOTVERSIONED_EXCLUDED));
        file1.delete();
        assertFalse(file1.exists());
        final boolean[] cleaned = new boolean[1];
        Git.STATUS_LOG.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (record.getMessage().contains("refreshAllRoots() uninteresting file: {0}") && file1.equals(record.getParameters()[0])) {
                    cleaned[0] = true;
                }
            }
            @Override
            public void flush() {
            }
            @Override
            public void close() throws SecurityException {
            }
        });
        getCache().refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repositoryLocation, Collections.singleton(folder)));
        assertTrue(cleaned[0]);
    }

    public void testSkipIgnores () throws Exception {
        File folder = new File(repositoryLocation, "folder");
        File file = new File(repositoryLocation, "file");
        file.createNewFile();
        File file1 = new File(folder, "file1");
        File file2 = new File(folder, "file2");
        folder.mkdirs();
        file1.createNewFile();
        file2.createNewFile();

        File ignoreFile = new File(repositoryLocation, ".gitignore");
        write(ignoreFile, "folder");

        StatusRefreshLogHandler handler = new StatusRefreshLogHandler(repositoryLocation);
        Git.STATUS_LOG.addHandler(handler);
        handler.setFilesToRefresh(Collections.singleton(repositoryLocation));
        getCache().refreshAllRoots(Collections.singleton(repositoryLocation));
        handler.waitForFilesToRefresh();
        assertEquals(new HashSet(Arrays.asList(file.getAbsolutePath(), folder.getAbsolutePath(), ignoreFile.getAbsolutePath())), handler.getInterestingFiles());

        handler.setFilesToRefresh(Collections.singleton(repositoryLocation));
        getCache().refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repositoryLocation, Collections.singleton(repositoryLocation)));
        handler.waitForFilesToRefresh();
        assertEquals(new HashSet(Arrays.asList(file.getAbsolutePath(), folder.getAbsolutePath(), ignoreFile.getAbsolutePath())), handler.getInterestingFiles());
    }

    public void testToggleIgnoreFolder () throws Exception {
        File file1 = new File(repositoryLocation, "file1");
        file1.createNewFile();
        File folder = new File(repositoryLocation, "folder");
        folder.mkdirs();
        File file2 = new File(folder, "file2");
        file2.createNewFile();
        File subFolder = new File(folder, "subfolder");
        subFolder.mkdirs();
        File file3 = new File(subFolder, "file3");
        file3.createNewFile();

        getCache().refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repositoryLocation, Collections.singleton(repositoryLocation)));
        List<File> newFiles = Arrays.asList(getCache().listFiles(Collections.singleton(folder), EnumSet.of(Status.NEW_INDEX_WORKING_TREE)));
        List<File> ignoredFiles = Arrays.asList(getCache().listFiles(Collections.singleton(folder), EnumSet.of(Status.NOTVERSIONED_EXCLUDED)));
        assertTrue(newFiles.contains(file2));
        assertTrue(newFiles.contains(file3));
        assertTrue(ignoredFiles.isEmpty());

        File ignoreFile = new File(repositoryLocation, ".gitignore");
        write(ignoreFile, "subfolder");
        getCache().getStatus(file3);
        getCache().refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repositoryLocation, Collections.singleton(subFolder)));
        getCache().getStatus(file3);
        Thread.sleep(500);
        newFiles = Arrays.asList(getCache().listFiles(Collections.singleton(folder), EnumSet.of(Status.NEW_INDEX_WORKING_TREE)));
        ignoredFiles = Arrays.asList(getCache().listFiles(Collections.singleton(folder), EnumSet.of(Status.NOTVERSIONED_EXCLUDED)));
        assertTrue(newFiles.contains(file2));
        assertFalse(newFiles.contains(file3));
        assertTrue(ignoredFiles.contains(subFolder));
        assertTrue(ignoredFiles.contains(file3));

        write(ignoreFile, "subfolder2");
        getCache().refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repositoryLocation, Collections.singleton(repositoryLocation)));
        newFiles = Arrays.asList(getCache().listFiles(Collections.singleton(folder), EnumSet.of(Status.NEW_INDEX_WORKING_TREE)));
        ignoredFiles = Arrays.asList(getCache().listFiles(Collections.singleton(folder), EnumSet.of(Status.NOTVERSIONED_EXCLUDED)));
        assertTrue(newFiles.contains(file2));
        assertTrue(newFiles.contains(file3));
        assertTrue(ignoredFiles.isEmpty());

        write(ignoreFile, "folder");
        getCache().getStatus(file2);
        getCache().getStatus(file3);
        getCache().refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repositoryLocation, Collections.singleton(repositoryLocation)));
        getCache().getStatus(file2);
        getCache().getStatus(file3);
        Thread.sleep(500);
        newFiles = Arrays.asList(getCache().listFiles(Collections.singleton(folder), EnumSet.of(Status.NEW_INDEX_WORKING_TREE)));
        ignoredFiles = Arrays.asList(getCache().listFiles(Collections.singleton(folder), EnumSet.of(Status.NOTVERSIONED_EXCLUDED)));
        assertTrue(newFiles.isEmpty());
        assertTrue(ignoredFiles.contains(folder));
        assertTrue(ignoredFiles.contains(subFolder));
        assertTrue(ignoredFiles.contains(file2));
        assertTrue(ignoredFiles.contains(file3));

        write(ignoreFile, "subfolder");
        getCache().refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repositoryLocation, Collections.singleton(repositoryLocation)));
        newFiles = Arrays.asList(getCache().listFiles(Collections.singleton(folder), EnumSet.of(Status.NEW_INDEX_WORKING_TREE)));
        ignoredFiles = Arrays.asList(getCache().listFiles(Collections.singleton(folder), EnumSet.of(Status.NOTVERSIONED_EXCLUDED)));
        assertTrue(newFiles.contains(file2));
        assertFalse(newFiles.contains(file3));
        assertFalse(ignoredFiles.contains(folder));
        assertTrue(ignoredFiles.contains(subFolder));
        assertTrue(ignoredFiles.contains(file3));

        write(ignoreFile, "");
        getCache().refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repositoryLocation, Collections.singleton(repositoryLocation)));
        newFiles = Arrays.asList(getCache().listFiles(Collections.singleton(repositoryLocation), EnumSet.of(Status.NEW_INDEX_WORKING_TREE)));
        ignoredFiles = Arrays.asList(getCache().listFiles(Collections.singleton(repositoryLocation), EnumSet.of(Status.NOTVERSIONED_EXCLUDED)));
        assertTrue(newFiles.contains(file2));
        assertTrue(newFiles.contains(file3));
        assertTrue(ignoredFiles.isEmpty());
    }

    public void testIgnoredFilesAreNotTracked () throws Exception {
        File file = new File(repositoryLocation, "ignoredFile");
        file.createNewFile();
        File folder = new File(repositoryLocation, "ignoredFolder");
        folder.mkdirs();
        File folder2 = new File(repositoryLocation, "ignoredFolder2");
        folder2.mkdirs();
        File file2 = new File(folder2, "addedFile");
        file2.createNewFile();
        File ignoreFile = new File(repositoryLocation, ".gitignore");
        write(ignoreFile, "ignored*");

        add(repositoryLocation);
        commit(repositoryLocation);
        Map<File, GitStatus> statuses = getClient(repositoryLocation).getStatus(new File[] { repositoryLocation }, GitUtils.NULL_PROGRESS_MONITOR);
        assertFalse(statuses.get(file).isTracked());
        assertFalse(statuses.get(folder).isTracked());
        assertFalse(statuses.get(folder2).isTracked());

        FileInformation fi;
        fi = new FileInformation(statuses.get(file));
        assertTrue(fi.containsStatus(Status.NOTVERSIONED_EXCLUDED));
        fi = new FileInformation(statuses.get(folder));
        assertTrue(fi.containsStatus(Status.NOTVERSIONED_EXCLUDED));
        fi = new FileInformation(statuses.get(folder2));
        assertTrue(fi.containsStatus(Status.NOTVERSIONED_EXCLUDED));
    }

    public void testStatusAddFiles () throws Exception {
        File file = new File(repositoryLocation, "file");
        file.createNewFile();
        File folder = new File(repositoryLocation, "folder");
        folder.mkdirs();
        File file2 = new File(folder, "otherFile");
        file2.createNewFile();

        getCache().refreshAllRoots(Collections.singleton(repositoryLocation));
        assertTrue(getCache().getStatus(file).containsStatus(Status.NEW_INDEX_WORKING_TREE));
        assertTrue(getCache().getStatus(folder).containsStatus(Status.UPTODATE));
        assertTrue(getCache().getStatus(file).containsStatus(Status.NEW_INDEX_WORKING_TREE));

        add(file, folder);
        getCache().refreshAllRoots(Collections.singleton(repositoryLocation));
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(file).getStatus());
        assertTrue(getCache().getStatus(folder).containsStatus(Status.UPTODATE));
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(file2).getStatus());

        write(file2, "i am modified");
        getCache().refreshAllRoots(Collections.singleton(repositoryLocation));
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(file).getStatus());
        assertTrue(getCache().getStatus(folder).containsStatus(Status.UPTODATE));
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE, Status.MODIFIED_INDEX_WORKING_TREE), getCache().getStatus(file2).getStatus());
    }

    public void testStatusRemoveFiles () throws Exception {
        File file = new File(repositoryLocation, "file");
        file.createNewFile();
        File folder = new File(repositoryLocation, "folder");
        folder.mkdirs();
        File file2 = new File(folder, "otherFile");
        file2.createNewFile();

        getCache().refreshAllRoots(Collections.singleton(repositoryLocation));
        assertTrue(getCache().getStatus(file).containsStatus(Status.NEW_INDEX_WORKING_TREE));
        assertTrue(getCache().getStatus(folder).containsStatus(Status.UPTODATE));
        assertTrue(getCache().getStatus(file).containsStatus(Status.NEW_INDEX_WORKING_TREE));

        add();
        commit();
        getCache().refreshAllRoots(Collections.singleton(repositoryLocation));
        assertTrue(getCache().getStatus(file).getStatus().equals(EnumSet.of(Status.UPTODATE)));
        assertTrue(getCache().getStatus(folder).containsStatus(Status.UPTODATE));
        assertTrue(getCache().getStatus(file2).getStatus().equals(EnumSet.of(Status.UPTODATE)));

        delete(true, file2);
        getCache().refreshAllRoots(Collections.singleton(repositoryLocation));
        assertTrue(getCache().getStatus(file).getStatus().equals(EnumSet.of(Status.UPTODATE)));
        assertTrue(getCache().getStatus(folder).containsStatus(Status.UPTODATE));
        assertTrue(getCache().getStatus(file2).getStatus().equals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.NEW_INDEX_WORKING_TREE)));
        assertTrue(file2.exists());

        commit();
        getCache().refreshAllRoots(Collections.singleton(repositoryLocation));
        assertTrue(getCache().getStatus(file2).getStatus().equals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE)));

        delete(false, file);
        getCache().refreshAllRoots(Collections.singleton(repositoryLocation));
        assertTrue(getCache().getStatus(folder).containsStatus(Status.UPTODATE));
        assertTrue(getCache().getStatus(file).getStatus().equals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE)));
        assertFalse(file.exists());

        commit();
        getCache().refreshAllRoots(Collections.singleton(repositoryLocation));
        assertTrue(getCache().getStatus(file).getStatus().equals(EnumSet.of(Status.UPTODATE)));
    }

    public void testStatusModifyFiles () throws Exception {
        File file = new File(repositoryLocation, "file");
        file.createNewFile();
        File folder = new File(repositoryLocation, "folder");
        folder.mkdirs();
        File file2 = new File(folder, "otherFile");
        file2.createNewFile();

        add();
        commit();
        getCache().refreshAllRoots(Collections.singleton(repositoryLocation));
        assertTrue(getCache().getStatus(file).getStatus().equals(EnumSet.of(Status.UPTODATE)));
        assertTrue(getCache().getStatus(folder).containsStatus(Status.UPTODATE));
        assertTrue(getCache().getStatus(file2).getStatus().equals(EnumSet.of(Status.UPTODATE)));

        write(file, "hello");
        getCache().refreshAllRoots(Collections.singleton(repositoryLocation));
        assertTrue(getCache().getStatus(file).getStatus().equals(EnumSet.of(Status.MODIFIED_HEAD_WORKING_TREE, Status.MODIFIED_INDEX_WORKING_TREE)));
        assertTrue(getCache().getStatus(file2).getStatus().equals(EnumSet.of(Status.UPTODATE)));

        add(file);
        getCache().refreshAllRoots(Collections.singleton(repositoryLocation));
        assertTrue(getCache().getStatus(file).getStatus().equals(EnumSet.of(Status.MODIFIED_HEAD_WORKING_TREE, Status.MODIFIED_HEAD_INDEX)));
        assertTrue(getCache().getStatus(file2).getStatus().equals(EnumSet.of(Status.UPTODATE)));

        commit();
        getCache().refreshAllRoots(Collections.singleton(repositoryLocation));
        assertTrue(getCache().getStatus(file).getStatus().equals(EnumSet.of(Status.UPTODATE)));
        assertTrue(getCache().getStatus(file2).getStatus().equals(EnumSet.of(Status.UPTODATE)));
    }

    public void testAnnotations () throws Exception {
        VersioningSupport.getPreferences().putBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, true);
        Annotator annotator = (Annotator) Git.getInstance().getVCSAnnotator();
        File f = new File(repositoryLocation, "file");
        String name = f.getName();
        File[] files = new File[] { f };
        VCSContext context = VCSContext.forNodes(new Node[] { new AbstractNode(Children.LEAF, Lookups.fixed(f)) });

        // file is New and not added to index
        f.createNewFile();
        getCache().refreshAllRoots(files);
        assertEquals("<font color=\"#008000\">file</font><font color=\"#999999\"> [-/A]</font>", annotator.annotateNameHtml(name, getCache().getStatus(f), f));
        assertIconTooltip(annotator, context, "<font color=\"#008000\">-/Added</font>");

        // file is added to index and not modified
        add();
        getCache().refreshAllRoots(files);
        assertEquals("<font color=\"#008000\">file</font><font color=\"#999999\"> [A/-]</font>", annotator.annotateNameHtml(name, getCache().getStatus(f), f));
        assertIconTooltip(annotator, context, "<font color=\"#008000\">Added/-</font>");

        // file is added to index and modified
        write(f, "blabla");
        getCache().refreshAllRoots(files);
        assertEquals("<font color=\"#008000\">file</font><font color=\"#999999\"> [A/M]</font>", annotator.annotateNameHtml(name, getCache().getStatus(f), f));
        assertIconTooltip(annotator, context, "<font color=\"#008000\">Added/Modified</font>");

        // file is added to index and deleted in WT, so NO CHANGE between head and WT
        f.delete();
        getCache().refreshAllRoots(files);
        assertEquals("file<font color=\"#999999\"> [A/D]</font>", annotator.annotateNameHtml(name, getCache().getStatus(f), f));
        assertIconTooltip(annotator, context, "Added/Deleted");

        // file is up-to-date
        f.createNewFile();
        commit();
        getCache().refreshAllRoots(files);
        assertEquals("file", annotator.annotateNameHtml(name, getCache().getStatus(f), f));
        assertIconTooltip(annotator, context, "");

        // file is modified in WT
        write(f, "blabla");
        getCache().refreshAllRoots(files);
        assertEquals("<font color=\"#0000ff\">file</font><font color=\"#999999\"> [-/M]</font>", annotator.annotateNameHtml(name, getCache().getStatus(f), f));
        assertIconTooltip(annotator, context, "<font color=\"#0000ff\">-/Modified</font>");

        // file is modified in index
        add();
        getCache().refreshAllRoots(files);
        assertEquals("<font color=\"#0000ff\">file</font><font color=\"#999999\"> [M/-]</font>", annotator.annotateNameHtml(name, getCache().getStatus(f), f));
        assertIconTooltip(annotator, context, "<font color=\"#0000ff\">Modified/-</font>");

        // file is modified both in index and wt
        write(f, "bubu");
        getCache().refreshAllRoots(files);
        assertEquals("<font color=\"#0000ff\">file</font><font color=\"#999999\"> [M/M]</font>", annotator.annotateNameHtml(name, getCache().getStatus(f), f));
        assertIconTooltip(annotator, context, "<font color=\"#0000ff\">Modified/Modified</font>");

        // file is modified in index, but in wt it is the same as head - so should be painted as up to date
        write(f, "");
        getCache().refreshAllRoots(files);
        assertEquals("file<font color=\"#999999\"> [M/M]</font>", annotator.annotateNameHtml(name, getCache().getStatus(f), f));
        assertIconTooltip(annotator, context, "Modified/Modified");

        // file is modified in index, but deleted in WT
        f.delete();
        getCache().refreshAllRoots(files);
        assertEquals("<font color=\"#999999\">file</font><font color=\"#999999\"> [M/D]</font>", annotator.annotateNameHtml(name, getCache().getStatus(f), f));
        assertIconTooltip(annotator, context, "<font color=\"#999999\">Modified/Deleted</font>");

        // file is removed both in index and WT
        delete(true, f);
        getCache().refreshAllRoots(files);
        assertEquals("<font color=\"#999999\">file</font><font color=\"#999999\"> [D/-]</font>", annotator.annotateNameHtml(name, getCache().getStatus(f), f));
        assertIconTooltip(annotator, context, "<font color=\"#999999\">Deleted/-</font>");

        // file is removed in index, but in WT the same as HEAD
        f.createNewFile();
        getCache().refreshAllRoots(files);
        assertEquals("file<font color=\"#999999\"> [D/A]</font>", annotator.annotateNameHtml(name, getCache().getStatus(f), f));
        assertIconTooltip(annotator, context, "Deleted/Added");

        // file is removed in index, but modified HEAD/WT
        write(f, "blabla");
        getCache().refreshAllRoots(files);
        assertEquals("<font color=\"#0000ff\">file</font><font color=\"#999999\"> [D/A]</font>", annotator.annotateNameHtml(name, getCache().getStatus(f), f));
        assertIconTooltip(annotator, context, "<font color=\"#0000ff\">Deleted/Added</font>");


        // rename
        add(f);
        String commitId = getClient(repositoryLocation).commit(files, "commit", null, null, GitUtils.NULL_PROGRESS_MONITOR).getRevision();
        delete(false, f);
        File f2 = new File(repositoryLocation, "copy");
        write(f2, "blabla");
        files  = new File[] { f, f2 };
        add();
        getCache().refreshAllRoots(files);
        assertEquals("<font color=\"#008000\">file</font><font color=\"#999999\"> [R/-]</font>", annotator.annotateNameHtml(name, getCache().getStatus(f2), f2));
        assertIconTooltip(annotator, VCSContext.forNodes(new Node[] { new AbstractNode(Children.LEAF, Lookups.fixed(f2)) }), "<font color=\"#008000\">Renamed/-</font>");

        assertEquals("reposka<font color=\"#999999\"> [master]</font>", annotator.annotateName("reposka", VCSContext.forNodes(new Node[] { new AbstractNode(Children.LEAF, Lookups.fixed(repositoryLocation)) })));
        // test annotation for detached head
        Thread.sleep(1100);
        write(new File(new File(repositoryLocation, ".git"), "HEAD"), commitId);
        RepositoryInfo.getInstance(repositoryLocation).refresh();
        assertEquals("reposka<font color=\"#999999\"> [" + commitId.substring(0, 7) + "...]</font>", annotator.annotateName("reposka", VCSContext.forNodes(new Node[] { new AbstractNode(Children.LEAF, Lookups.fixed(repositoryLocation)) })));
    }

    public void testAllStatusAreComparable () throws Exception {
        for (Status status : FileInformation.Status.values()) {
            FileInformation fi = new FileInformation(EnumSet.of(status), false);
            assertTrue(fi.getComparableStatus() >= 0);
        }
    }

    public void testComparableStatus () throws Exception {
        // conflict is the most important
        FileInformation fiConflict = new FileInformation(EnumSet.of(Status.IN_CONFLICT), false);
        for (Status status : FileInformation.Status.values()) {
            FileInformation fi = new FileInformation(EnumSet.of(status), false);
            if (!fi.containsStatus(Status.IN_CONFLICT)) {
                assertTrue(fi.getComparableStatus() > fiConflict.getComparableStatus());
            }
        }
    }

    /**
     * It seems that cache.listFiles and cache.containsFiles called getStatus also on folders. Because folders are usually up-to-date (git does not track them),
     * this results in unnecessary call to getOwner, ignore logic or sharability.
     */
    public void testSkipFoldersBug196702 () throws Exception {
        final File f1 = new File(repositoryLocation, "1");
        final File f2 = new File(f1, "2");
        final File f3 = new File(f2, "3");
        f3.mkdirs();
        File f = new File(f3, "f");
        f.createNewFile();
        
        FileStatusCache cache = getCache();
        cache.refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repositoryLocation, Collections.singleton(f1)));
        Logger log = Logger.getLogger("org.netbeans.modules.git.status.cache");
        final boolean[] flags = new boolean[1];
        log.addHandler(new Handler() {
            @Override
            public void publish (LogRecord record) {
                if (record.getMessage().startsWith("getCachedStatus for file {0}:")) {
                    if (Arrays.asList(f1, f2, f3).contains((File) record.getParameters()[0])) {
                        flags[0] = true;
                    }
                }
            }

            @Override
            public void flush () { }

            @Override
            public void close () throws SecurityException { }
        });
        
        Collection<File> newFiles = Arrays.asList(cache.listFiles(new File[] { f1 }, EnumSet.of(FileInformation.Status.NEW_INDEX_WORKING_TREE)));
        assertEquals(Arrays.asList(f), newFiles);
        assertFalse(flags[0]);
        flags[0] = false;
        assertTrue(cache.containsFiles(Collections.singleton(f1), EnumSet.of(FileInformation.Status.NEW_INDEX_WORKING_TREE), true));
        assertFalse(flags[0]);
        assertFalse(cache.containsFiles(Collections.singleton(f1), EnumSet.of(FileInformation.Status.MODIFIED_INDEX_WORKING_TREE), true));
        assertFalse(flags[0]);
        
        // test we do not break anything with the bugfix: ignored files/folders should be recognized even without getStatus call
        SystemAction.get(IgnoreAction.class).performAction(VCSContext.forNodes(new Node[] { new AbstractNode(Children.LEAF, Lookups.fixed(f1)) }));
        flags[0] = false;
        Collection<File> ignoredFiles = Arrays.asList(cache.listFiles(new File[] { f1 }, EnumSet.of(FileInformation.Status.NOTVERSIONED_EXCLUDED)));
        assertEquals(Arrays.asList(f1), ignoredFiles);
        assertFalse(flags[0]);
        flags[0] = false;
        assertTrue(cache.containsFiles(Collections.singleton(f1), EnumSet.of(FileInformation.Status.NOTVERSIONED_EXCLUDED), true));
        assertFalse(flags[0]);
    }
    
    public void testIgnoredSubrepos () throws Exception {
        File subrepo = new File(repositoryLocation, "subrepository");
        subrepo.mkdirs();
        getClient(repositoryLocation).ignore(new File[] { subrepo }, GitUtils.NULL_PROGRESS_MONITOR);
        
        File file = new File(subrepo, "file");
        file.createNewFile();
        
        getCache().refreshAllRoots(repositoryLocation);
        FileInformation fi = getCache().getStatus(file);
        assertTrue(fi.containsStatus(FileInformation.Status.NOTVERSIONED_EXCLUDED));
        
        GitClient client = getClient(subrepo);
        client.init(GitUtils.NULL_PROGRESS_MONITOR);
        Git.getInstance().versionedFilesChanged();
        
        getCache().refreshAllRoots(subrepo);
        fi = getCache().getStatus(file);
        assertTrue(fi.containsStatus(FileInformation.Status.NEW_INDEX_WORKING_TREE));
    }

    public void testStatusExcludedFiles () throws Exception {
        File folder = new File(repositoryLocation, "folder");
        folder.mkdirs();
        File file = new File(folder, "otherFile");
        file.createNewFile();

        add();
        commit();
        getCache().refreshAllRoots(Collections.singleton(repositoryLocation));
        assertTrue(getCache().getStatus(file).getStatus().equals(EnumSet.of(Status.UPTODATE)));
        assertTrue(getCache().getStatus(folder).containsStatus(Status.UPTODATE));

        write(file, "hello");
        getCache().refreshAllRoots(Collections.singleton(repositoryLocation));
        GitModuleConfig.getDefault().removeExclusionPaths(Collections.<String>singleton(file.getAbsolutePath()));
        assertTrue(getCache().containsFiles(Collections.<File>singleton(folder), EnumSet.of(Status.MODIFIED_INDEX_WORKING_TREE), false));
        assertEquals(Collections.<File>singletonList(file), Arrays.asList(getCache().listFiles(Collections.<File>singleton(folder), EnumSet.of(Status.MODIFIED_INDEX_WORKING_TREE))));
        assertTrue(getCache().containsFiles(Collections.<File>singleton(file), EnumSet.of(Status.MODIFIED_INDEX_WORKING_TREE), false));
        assertEquals(Collections.<File>singletonList(file), Arrays.asList(getCache().listFiles(Collections.<File>singleton(file), EnumSet.of(Status.MODIFIED_INDEX_WORKING_TREE))));
        
        // exclude
        GitModuleConfig.getDefault().addExclusionPaths(Collections.<String>singleton(file.getAbsolutePath()));
        
        // now contains on parent should be false
        assertFalse(getCache().containsFiles(Collections.<File>singleton(folder), EnumSet.of(Status.MODIFIED_INDEX_WORKING_TREE), false));
        assertTrue(getCache().containsFiles(Collections.<File>singleton(file), EnumSet.of(Status.MODIFIED_INDEX_WORKING_TREE), false));
        assertEquals(Collections.<File>singletonList(file), Arrays.asList(getCache().listFiles(Collections.<File>singleton(file), EnumSet.of(Status.MODIFIED_INDEX_WORKING_TREE))));
        
        // clean
        GitModuleConfig.getDefault().removeExclusionPaths(Collections.<String>singleton(file.getAbsolutePath()));
    }
    
    // tests that list files does not return modifications frm subrepositories
    public void testCacheListFilesSubrepos () throws Exception {
        File subrepo = new File(repositoryLocation, "subrepository");
        subrepo.mkdirs();
        getClient(subrepo).init(GitUtils.NULL_PROGRESS_MONITOR);
        Git.getInstance().clearAncestorCaches();
        Git.getInstance().versionedFilesChanged();
        
        getClient(repositoryLocation).add(new File[] { subrepo }, GitUtils.NULL_PROGRESS_MONITOR);
        getClient(repositoryLocation).commit(new File[] { subrepo }, "message", null, null, GitUtils.NULL_PROGRESS_MONITOR);
        
        File file = new File(subrepo, "file");
        file.createNewFile();
        
        getCache().refreshAllRoots(subrepo);
        getCache().refreshAllRoots(repositoryLocation);
        FileInformation fi = getCache().getStatus(file);
        assertTrue(fi.containsStatus(FileInformation.Status.NEW_INDEX_WORKING_TREE));
        
        File[] listFiles = getCache().listFiles(new File[] { repositoryLocation }, FileInformation.STATUS_LOCAL_CHANGES);
        assertEquals(0, listFiles.length);
        
        listFiles = getCache().listFiles(new File[] { subrepo }, FileInformation.STATUS_LOCAL_CHANGES);
        assertEquals(1, listFiles.length);
        assertEquals(file, listFiles[0]);
    }
    
    // tests that contains files does not return modifications frm subrepositories
    public void testCacheContainsFilesSubrepos () throws Exception {
        File subrepo = new File(repositoryLocation, "subrepository");
        subrepo.mkdirs();
        getClient(subrepo).init(GitUtils.NULL_PROGRESS_MONITOR);
        Git.getInstance().clearAncestorCaches();
        Git.getInstance().versionedFilesChanged();
        
        getClient(repositoryLocation).add(new File[] { subrepo }, GitUtils.NULL_PROGRESS_MONITOR);
        getClient(repositoryLocation).commit(new File[] { subrepo }, "message", null, null, GitUtils.NULL_PROGRESS_MONITOR);
        
        File file = new File(subrepo, "file");
        file.createNewFile();
        
        getCache().refreshAllRoots(subrepo);
        getCache().refreshAllRoots(repositoryLocation);
        FileInformation fi = getCache().getStatus(file);
        assertTrue(fi.containsStatus(FileInformation.Status.NEW_INDEX_WORKING_TREE));
        
        assertFalse(getCache().containsFiles(Collections.<File>singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES, true));
        
        assertTrue(getCache().containsFiles(Collections.<File>singleton(subrepo), FileInformation.STATUS_LOCAL_CHANGES, true));
    }
    
    private void assertSameStatus(Set<File> files, EnumSet<Status> status) {
        for (File f : files) {
            assertEquals(status, getCache().getStatus(f).getStatus());
        }
    }

    private void setAutomaticRefreshEnabled (boolean flag) throws Exception {
        Field f = FilesystemInterceptor.class.getDeclaredField("AUTOMATIC_REFRESH_ENABLED");
        f.setAccessible(true);
        f.setBoolean(FilesystemInterceptor.class, flag);
        assert ((Boolean) f.get(FilesystemInterceptor.class)).equals(flag);
    }

    private void assertIconTooltip (Annotator annotator, VCSContext context, String string) {
        Image icon = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        icon = annotator.annotateIcon(icon, context);
        assertEquals(string, ImageUtilities.getImageToolTip(icon));
    }

    private void waitForStatus (File file, Status expectedStatus) throws InterruptedException {
        FileInformation status = null;
        for (int i = 0; i < 100; ++i) {
            status = getCache().getStatus(file);
            if (status.containsStatus(expectedStatus)) {
                break;
            }
            Thread.sleep(200);
        }
        assertTrue("Status of " + file + " - " + status.getStatusText() + "; expected " + expectedStatus, status.containsStatus(expectedStatus));
    }

    @ServiceProvider(service=SharabilityQueryImplementation.class)
    public static class DummySharabilityQuery implements SharabilityQueryImplementation {

        @Override
        public int getSharability (File file) {
            if (file.getAbsolutePath().contains("notSharable")) {
                return SharabilityQuery.NOT_SHARABLE;
            }
            return SharabilityQuery.UNKNOWN;
        }

    }
}
