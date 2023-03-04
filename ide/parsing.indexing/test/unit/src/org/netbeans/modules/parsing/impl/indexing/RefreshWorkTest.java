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

package org.netbeans.modules.parsing.impl.indexing;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;
import org.openide.util.Utilities;

/**
 *
 * @author vita
 */
public class RefreshWorkTest  extends IndexingTestBase {

    public RefreshWorkTest(String name) {
        super(name);
    }

    private File outerFolder1;
    private File outerFolder2;
    private URL rootAUrl;
    private URL rootBUrl;
    private URL rootCUrl;
    private Map<URL, List<URL>> scannedRoots2Dependencies = new HashMap<URL, List<URL>>();

    protected @Override void setUp() throws IOException {
        this.clearWorkDir();
        final File _wd = this.getWorkDir();
        final FileObject wd = FileUtil.toFileObject(_wd);
        final FileObject cache = wd.createFolder("cache");
        CacheFolder.setCacheFolder(cache);
        
        outerFolder1 = new File(getWorkDir(), "OuterFolder-1");
        outerFolder1.mkdirs();
        outerFolder2 = new File(getWorkDir(), "OuterFolder-2");
        outerFolder2.mkdirs();

        File rootAFile = new File(outerFolder1, "rootA");
        rootAFile.mkdirs();
        rootAUrl = Utilities.toURI(rootAFile).toURL();
        scannedRoots2Dependencies.put(rootAUrl, Collections.<URL>emptyList());
        File rootBFile = new File(outerFolder1, "rootB");
        rootBFile.mkdirs();
        rootBUrl = Utilities.toURI(rootBFile).toURL();
        scannedRoots2Dependencies.put(rootBUrl, Collections.<URL>emptyList());

        File rootCFile = new File(outerFolder2, "rootC");
        rootCFile.mkdirs();
        populateFolderStructure(rootCFile,
                "org/pckg1/file1.txt",
                "org/pckg1/pckg2/file1.txt",
                "org/pckg1/pckg2/file2.txt",
                "org/pckg2/"
        );
        rootCUrl = Utilities.toURI(rootCFile).toURL();
        scannedRoots2Dependencies.put(rootCUrl, Collections.<URL>emptyList());

        RepositoryUpdater.getDefault().rootsListeners.setListener(new FileChangeAdapter(), new FileChangeAdapter());
    }

    protected @Override void tearDown() throws Exception {
        RepositoryUpdater.getDefault().rootsListeners.setListener(null, null);
    }

    public void testOuterFolders() {
        RepositoryUpdater.RefreshWork rw = new RepositoryUpdater.RefreshWork(
            scannedRoots2Dependencies,
            Collections.<URL,List<URL>>emptyMap(), // scannedBinaries
            Collections.<URL,List<URL>>emptyMap(), // scannedRoots2Peers
            Collections.<URL>emptySet(),    //incompleteSeenRoots
            Collections.<URL>emptySet(), // sourceForBinaryRoots
            false, // fullRescan
            false, // logStatistics
            Collections.singleton(outerFolder1), // suspectFilesOrFolders
            new RepositoryUpdater.FSRefreshInterceptor(),
            SuspendSupport.NOP,
            null);

        Indexer indexer = new Indexer();
        MockMimeLookup.setInstances(MimePath.EMPTY, new IndexerFactory(indexer));

        assertTrue("Work has not finished", rw.getDone());
        assertEquals("Wrong number of scanned roots", 2, indexer.indexCalls.size());
        assertTrue("Expecting " + rootAUrl + " to be scanned", indexer.indexedFiles.containsKey(rootAUrl));
        assertTrue("Expecting " + rootBUrl + " to be scanned", indexer.indexedFiles.containsKey(rootBUrl));
        assertFalse("Expecting " + rootCUrl + " not to be scanned", indexer.indexedFiles.containsKey(rootCUrl));
    }

    public void testOuterFiles() throws IOException {
        FileObject f = FileUtil.toFileObject(outerFolder1);
        FileObject ff = FileUtil.createData(f, "file.txt");

        RepositoryUpdater.RefreshWork rw = new RepositoryUpdater.RefreshWork(
            scannedRoots2Dependencies,
            Collections.<URL,List<URL>>emptyMap(), // scannedBinaries
            Collections.<URL,List<URL>>emptyMap(), // scannedRoots2Peers
            Collections.<URL>emptySet(),    //incompleteSeenRoots
            Collections.<URL>emptySet(), // sourceForBinaryRoots
            false, // fullRescan
            false, // logStatistics
            Collections.singleton(ff), // suspectFilesOrFolders
            new RepositoryUpdater.FSRefreshInterceptor(),
            SuspendSupport.NOP,
            null);

        Indexer indexer = new Indexer();
        MockMimeLookup.setInstances(MimePath.EMPTY, new IndexerFactory(indexer));

        assertTrue("Work has not finished", rw.getDone());
        assertEquals("No roots should be scanned", 0, indexer.indexCalls.size());
    }

    public void testInnerFolders() throws IOException {
        File innerFolder = new File(outerFolder2, "rootC/org/pckg1/pckg2/");

        RepositoryUpdater.RefreshWork rw = new RepositoryUpdater.RefreshWork(
            scannedRoots2Dependencies,
            Collections.<URL,List<URL>>emptyMap(), // scannedBinaries
            Collections.<URL,List<URL>>emptyMap(), // scannedRoots2Peers
            Collections.<URL>emptySet(),    //incompleteSeenRoots
            Collections.<URL>emptySet(), // sourceForBinaryRoots
            false, // fullRescan
            false, // logStatistics
            Collections.singleton(innerFolder), // suspectFilesOrFolders
            new RepositoryUpdater.FSRefreshInterceptor(),
            SuspendSupport.NOP,
            null);

        Indexer indexer = new Indexer();
        MockMimeLookup.setInstances(MimePath.EMPTY, new IndexerFactory(indexer));

        assertTrue("Work has not finished", rw.getDone());
        assertEquals("Wrong number of scanned roots", 1, indexer.indexCalls.size());
        assertFalse("Expecting " + rootAUrl + " not to be scanned", indexer.indexedFiles.containsKey(rootAUrl));
        assertFalse("Expecting " + rootBUrl + " not to be scanned", indexer.indexedFiles.containsKey(rootBUrl));
        assertTrue("Expecting " + rootCUrl + " to be scanned", indexer.indexedFiles.containsKey(rootCUrl));

        Set<String> files = indexer.indexedFiles.get(rootCUrl);
        assertEquals("Wrong files scanned", new HashSet<String>(Arrays.asList(new String [] {
            "org/pckg1/pckg2/file1.txt",
            "org/pckg1/pckg2/file2.txt"
        })), files);
    }

    public void testInnerFiles() throws IOException {
        File innerFile = new File(outerFolder2, "rootC/org/pckg1/pckg2/file1.txt");

        RepositoryUpdater.RefreshWork rw = new RepositoryUpdater.RefreshWork(
            scannedRoots2Dependencies,
            Collections.<URL,List<URL>>emptyMap(), // scannedBinaries
            Collections.<URL,List<URL>>emptyMap(), // scannedRoots2Peers
            Collections.<URL>emptySet(),    //incompleteSeenRoots
            Collections.<URL>emptySet(), // sourceForBinaryRoots
            false, // fullRescan
            false, // logStatistics
            Collections.singleton(innerFile), // suspectFilesOrFolders
            new RepositoryUpdater.FSRefreshInterceptor(),
            SuspendSupport.NOP,
            null);

        Indexer indexer = new Indexer();
        MockMimeLookup.setInstances(MimePath.EMPTY, new IndexerFactory(indexer));

        assertTrue("Work has not finished", rw.getDone());
        assertEquals("Wrong number of scanned roots", 1, indexer.indexCalls.size());
        assertFalse("Expecting " + rootAUrl + " not to be scanned", indexer.indexedFiles.containsKey(rootAUrl));
        assertFalse("Expecting " + rootBUrl + " not to be scanned", indexer.indexedFiles.containsKey(rootBUrl));
        assertTrue("Expecting " + rootCUrl + " to be scanned", indexer.indexedFiles.containsKey(rootCUrl));

        Set<String> files = indexer.indexedFiles.get(rootCUrl);
        assertEquals("Wrong files scanned", new HashSet<String>(Arrays.asList(new String [] {
            "org/pckg1/pckg2/file1.txt"
        })), files);
    }

    private static final class Indexer extends CustomIndexer {

        public final List<Pair<URL, List<String>>> indexCalls = new LinkedList<Pair<URL, List<String>>>();
        public final Map<URL, Set<String>> indexedFiles = new HashMap<URL, Set<String>>();

        @Override
        protected void index(Iterable<? extends Indexable> files, Context context) {
            List<String> list = new LinkedList<String>();
            for(Indexable i : files) {
                list.add(i.getRelativePath());
            }
            indexCalls.add(Pair.of(context.getRootURI(), list));

            Set<String> indexables = indexedFiles.get(context.getRootURI());
            if (indexables == null) {
                indexables = new HashSet<String>();
                indexedFiles.put(context.getRootURI(), indexables);
            }
            indexables.addAll(list);
        }

    } // End of Indexer class

    private static final class IndexerFactory extends CustomIndexerFactory {

        private final Indexer indexer;

        public IndexerFactory(Indexer indexer) {
            this.indexer = indexer;
        }

        @Override
        public CustomIndexer createIndexer() {
            return indexer;
        }

        @Override
        public boolean supportsEmbeddedIndexers() {
            return true;
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
        }

        @Override
        public String getIndexerName() {
            return indexer.getClass().getName();
        }

        @Override
        public int getIndexVersion() {
            return 1;
        }

    } // End of IndexerFactory class

    private static void populateFolderStructure(File root, String... filesOrFolders) throws IOException {
        for(String fileOrFolder : filesOrFolders) {
            if (fileOrFolder.endsWith("/")) {
                // folder
                File folder = new File(root, fileOrFolder.substring(0, fileOrFolder.length() - 1));
                folder.mkdirs();
            } else {
                // file
                File file = new File(root, fileOrFolder);
                File folder = file.getParentFile();
                folder.mkdirs();
                FileUtil.createData(FileUtil.toFileObject(folder), file.getName());
            }
        }
    }
}
