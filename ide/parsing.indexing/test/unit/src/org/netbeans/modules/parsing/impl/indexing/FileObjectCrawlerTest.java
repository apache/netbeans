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

package org.netbeans.modules.parsing.impl.indexing;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;

/**
 *
 * @author Jan Lahoda
 */
public class FileObjectCrawlerTest extends NbTestCase {

    //Random test options
    private static final boolean RUN_RANDOM_TESTS = false;
    private static final int SINGLE_TEST_SET_SIZE = 5;
    private static final int TEST_COUNT = 100000;
    private static final int TREE_DEPTH = 5;
    private static final int TREE_CHILD_COUNT = 3;

    private static final CancelRequest CR = new CancelRequest() {
        @Override
        public boolean isRaised() {
            return false;
        }
    };

    public FileObjectCrawlerTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws IOException {
        clearWorkDir();
        File wd = getWorkDir();
        final FileObject wdFO = FileUtil.toFileObject(wd);
        final FileObject cache = FileUtil.createFolder(wdFO, "cache");

        CacheFolder.setCacheFolder(cache);
    }

    @Override
    protected void tearDown() throws IOException {
        FileObjectCrawler.mockLinkTypes = null;
    }

    public void testIncludesExcludes() throws IOException {
        final FileObject src = FileUtil.createFolder(FileUtil.toFileObject(getWorkDir()), "src");
        assertNotNull(src);

        populateFolderStructure(FileUtil.createFolder(new File(getWorkDir(), "src")),
            "p1/Included1.java",
            "p1/Included2.java",
            "p1/a/Included3.java",
            "p1/a/Included4.java",
            "p2/Excluded1.java",
            "p2/Excluded2.java",
            "p2/a/Excluded3.java",
            "p2/a/Excluded4.java"
        );

        ClassPath cp = ClassPathSupport.createClassPath(Arrays.asList(new FilteringPathResourceImplementation() {
            private final Pattern p = Pattern.compile("p1/.*");

            public boolean includes(URL root, String resource) {
                return p.matcher(resource).matches();
            }

            public URL[] getRoots() {
                try {
                    return new URL[]{src.getURL()};
                } catch (FileStateInvalidException ex) {
                    throw new IllegalStateException(ex);
                }
            }

            public ClassPathImplementation getContent() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void addPropertyChangeListener(PropertyChangeListener listener) {}
            public void removePropertyChangeListener(PropertyChangeListener listener) {}
        }));

        FileObjectCrawler crawler = new FileObjectCrawler(src, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), cp.entries().get(0), CR, SuspendSupport.NOP);
        assertCollectedFiles("Wrong files collected", crawler.getAllResources(),
                "p1/Included1.java",
                "p1/Included2.java",
                "p1/a/Included3.java",
                "p1/a/Included4.java"
        );
        assertCollectedFiles("Wrong files collected", crawler.getResources(),
                "p1/Included1.java",
                "p1/Included2.java",
                "p1/a/Included3.java",
                "p1/a/Included4.java"
        );
    }

    public void testRelativePaths() throws IOException {
        FileObject root = FileUtil.createFolder(new File(getWorkDir(), "src"));
        String [] paths = new String [] {
                "org/pckg1/file1.txt",
                "org/pckg1/pckg2/file1.txt",
                "org/pckg1/pckg2/file2.txt",
                "org/pckg2/"
        };
        populateFolderStructure(root, paths);

        FileObjectCrawler crawler1 = new FileObjectCrawler(root, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), null, CR, SuspendSupport.NOP);
        assertCollectedFiles("Wrong files collected", crawler1.getResources(), paths);
        assertCollectedFiles("Wrong files collected", crawler1.getAllResources(), paths);
        
        FileObject folder = root.getFileObject("org/pckg1/pckg2");
        FileObjectCrawler crawler2 = new FileObjectCrawler(root, new FileObject [] { folder }, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), null, CR, SuspendSupport.NOP);
        assertCollectedFiles("Wrong files collected from " + folder, crawler2.getResources(),
            "org/pckg1/pckg2/file1.txt",
            "org/pckg1/pckg2/file2.txt"
        );
        assertNull("All resources should not be computed for subtree", crawler2.getAllResources());
    }

    public void testDeletedFiles() throws IOException {
        FileObject root = FileUtil.createFolder(new File(getWorkDir(), "src"));
        String [] paths = new String [] {
                "org/pckg1/file1.txt",
                "org/pckg1/pckg2/file1.txt",
                "org/pckg1/pckg2/file2.txt",
                "org/pckg2/"
        };
        populateFolderStructure(root, paths);

        FileObjectCrawler crawler1 = new FileObjectCrawler(root, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), null, CR, SuspendSupport.NOP);
        assertCollectedFiles("Wrong files collected", crawler1.getResources(), paths);
        assertCollectedFiles("Wrong files collected", crawler1.getAllResources(), paths);

        FileObject pckg2 = root.getFileObject("org/pckg1/pckg2");
        FileObject org = root.getFileObject("org");
        org.delete();

        FileObjectCrawler crawler2 = new FileObjectCrawler(root, new FileObject [] { pckg2 }, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), null, CR, SuspendSupport.NOP);
        assertCollectedFiles("There should be no files in " + root, crawler2.getResources());
        assertNull("All resources should not be computed for subtree", crawler2.getAllResources());

        FileObjectCrawler crawler3 = new FileObjectCrawler(root, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), null, CR, SuspendSupport.NOP);
        assertCollectedFiles("There should be no files in " + root, crawler3.getResources());
        assertCollectedFiles("There should be no files in " + root, crawler3.getAllResources());
        assertCollectedFiles("All files in " + root + " should be deleted", crawler3.getDeletedResources());
    }

    public void testAllFilesIndexing() throws Exception {
        FileObject root = FileUtil.createFolder(new File(getWorkDir(), "src"));
        String [] paths = new String [] {
                "org/pckg1/file1.txt",
                "org/pckg1/pckg2/file1.txt",
                "org/pckg1/pckg2/file2.txt",
                "org/pckg2/"
        };
        populateFolderStructure(root, paths);

        //First scan with timestamps enabled (project open)
        FileObjectCrawler crawler1 = new FileObjectCrawler(root, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE, Crawler.TimeStampAction.CHECK), null, CR, SuspendSupport.NOP);
        assertCollectedFiles("Wrong all files collected", crawler1.getAllResources(), paths);
        assertTrue(crawler1.getAllResources() != crawler1.getResources());
        assertEquals(crawler1.getAllResources().size(), crawler1.getResources().size());
        assertCollectedFiles("Wrong files collected", crawler1.getResources(), paths);
        crawler1.storeTimestamps();

        //Second scan with timestamps enabled (project reopen)
        FileObjectCrawler crawler2 = new FileObjectCrawler(root, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE, Crawler.TimeStampAction.CHECK), null, CR, SuspendSupport.NOP);
        assertCollectedFiles("Wrong all files collected", crawler2.getAllResources(), paths);
        assertTrue(crawler2.getAllResources() != crawler2.getResources());
        assertEquals(0, crawler2.getResources().size());
        crawler2.storeTimestamps();

        //Rescan of root with force == false
        FileObjectCrawler crawler3 = new FileObjectCrawler(root, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE, Crawler.TimeStampAction.CHECK), null, CR, SuspendSupport.NOP);
        assertCollectedFiles("Wrong all files collected", crawler3.getAllResources(), paths);
        assertTrue(crawler3.getAllResources() != crawler3.getResources());
        assertEquals(0, crawler3.getResources().size());
        crawler3.storeTimestamps();

        //Rescan of root with force == true
        FileObjectCrawler crawler4 = new FileObjectCrawler(root, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), null, CR, SuspendSupport.NOP);
        assertCollectedFiles("Wrong all files collected", crawler4.getAllResources(), paths);
        assertTrue(crawler4.getAllResources() == crawler4.getResources());
        crawler4.storeTimestamps();

        //Rescan of specified files (no timestamps)
        final FileObject expFile1 = root.getFileObject("org/pckg1/pckg2/file1.txt");
        final FileObject expFile2 = root.getFileObject("org/pckg1/pckg2/file2.txt");
        FileObjectCrawler crawler5 = new FileObjectCrawler(root, new FileObject[] {expFile1, expFile2}, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), null, CR, SuspendSupport.NOP);
        assertNull(crawler5.getAllResources());
        assertCollectedFiles("Wrong files collected", crawler5.getResources(), new String[] {"org/pckg1/pckg2/file1.txt","org/pckg1/pckg2/file2.txt"});
        crawler5.storeTimestamps();

        //Rescan of specified files with timestamps
        FileObjectCrawler crawler6 = new FileObjectCrawler(root, new FileObject[] {expFile1, expFile2}, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE, Crawler.TimeStampAction.CHECK), null, CR, SuspendSupport.NOP);
        assertNull(crawler6.getAllResources());
        assertEquals(0, crawler6.getResources().size());
        crawler6.storeTimestamps();
    }

    public void testSymLinksInRoot() throws Exception {
        final File workDir = getWorkDir();
        final FileObject wd = FileUtil.toFileObject(workDir);
        final FileObject rootWithCycle = wd.createFolder("rootWithCycle");
        final FileObject folder1 = rootWithCycle.createFolder("folder1");
        final FileObject folder2 = rootWithCycle.createFolder("folder2");
        final FileObject inFolder1 = folder1.createFolder("infolder1");
        final FileObject inFolder2 = folder2.createFolder("folder2");
        folder1.createData("data1.txt");
        inFolder1.createData("data2.txt");
        folder2.createData("data3.txt");
        inFolder2.createData("data4.txt");
        final Map<Pair<FileObject,FileObject>,Boolean> linkMap = new HashMap<>();
        linkMap.put(
            Pair.<FileObject,FileObject>of(folder2,inFolder2), Boolean.TRUE
        );
        FileObjectCrawler.mockLinkTypes = linkMap;
        final FileObjectCrawler c = new FileObjectCrawler(rootWithCycle, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), null, CR, SuspendSupport.NOP);
        final Collection<Indexable> indexables = c.getAllResources();
        assertCollectedFiles("Wring collected files", indexables,
                "folder1/data1.txt",
                "folder1/infolder1/data2.txt",
                "folder2/data3.txt");
    }

    public void testSymLinksFromRoot() throws Exception {
        final File workDir = getWorkDir();
        final FileObject wd = FileUtil.toFileObject(workDir);
        final FileObject cycleTarget= wd.createFolder("cycleTarget");
        final FileObject rootWithCycle = cycleTarget.createFolder("rootWithExtLink");
        final FileObject folder1 = rootWithCycle.createFolder("folder1");
        final FileObject folder2 = rootWithCycle.createFolder("folder2");
        final FileObject inFolder1 = folder1.createFolder("infolder1");
        final FileObject inFolder2 = folder2.createFolder("cycleTarget");
        folder1.createData("data1.txt");
        inFolder1.createData("data2.txt");
        folder2.createData("data3.txt");
        inFolder2.createData("data4.txt");
        final Map<Pair<FileObject,FileObject>,Boolean> linkMap = new HashMap<>();
        linkMap.put(
            Pair.<FileObject,FileObject>of(cycleTarget, inFolder2), Boolean.TRUE
        );
        FileObjectCrawler.mockLinkTypes = linkMap;
        final FileObjectCrawler c = new FileObjectCrawler(rootWithCycle, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), null, CR, SuspendSupport.NOP);
        final Collection<Indexable> indexables = c.getAllResources();
        assertCollectedFiles("Wring collected files", indexables,
                "folder1/data1.txt",
                "folder1/infolder1/data2.txt",
                "folder2/data3.txt");
    }

    public void testDuplicateResults1() throws IOException {
        FileObject root = FileUtil.createFolder(new File(getWorkDir(), "src"));
        String [] paths = new String [] {
                "org/pckg1/file1.txt",
                "org/pckg1/pckg2/file1.txt",
                "org/pckg1/pckg2/file2.txt",
        };

        populateFolderStructure(root, paths);

        FileObjectCrawler crawler = new FileObjectCrawler(root, new FileObject[] {root.getFileObject("org/pckg1/pckg2/file1.txt"), root.getFileObject("org/pckg1/pckg2")}, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), null, CR, SuspendSupport.NOP);
        assertCollectedFiles("Wrong files collected", crawler.getResources(), new String[] {"org/pckg1/pckg2/file1.txt", "org/pckg1/pckg2/file2.txt"});
    }

    public void testDuplicateResults2() throws IOException {
        FileObject root = FileUtil.createFolder(new File(getWorkDir(), "src"));
        String [] paths = new String [] {
                "org/pckg1/file1.txt",
                "org/pckg1/pckg2/file1.txt",
                "org/pckg1/pckg2/file2.txt",
        };

        populateFolderStructure(root, paths);

        FileObjectCrawler crawler = new FileObjectCrawler(root, new FileObject[] {root.getFileObject("org/pckg1/pckg2/file1.txt"), root.getFileObject("org/pckg1/pckg2/file2.txt")}, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), null, CR, SuspendSupport.NOP);
        assertCollectedFiles("Wrong files collected", crawler.getResources(), new String[] {"org/pckg1/pckg2/file1.txt", "org/pckg1/pckg2/file2.txt"});
    }

    public void testDuplicateResults3() throws IOException {
        FileObject root = FileUtil.createFolder(new File(getWorkDir(), "src"));
        String [] paths = new String [] {
                "org/pckg1/file1.txt",
                "org/pckg1/pckg2/file1.txt",
                "org/pckg1/pckg2/file2.txt",
        };

        populateFolderStructure(root, paths);

        FileObjectCrawler crawler = new FileObjectCrawler(root, new FileObject[] {root.getFileObject("org/pckg1/pckg2/file1.txt"), root.getFileObject("org/pckg1/pckg2/file1.txt")}, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), null, CR, SuspendSupport.NOP);
        assertCollectedFiles("Wrong files collected", crawler.getResources(), new String[] {"org/pckg1/pckg2/file1.txt"});
    }

    public void testDuplicateResults4() throws IOException {
        FileObject root = FileUtil.createFolder(new File(getWorkDir(), "src"));
        String [] paths = new String [] {
                "org/bar/file1.txt",
                "org/foo/file2.txt",
        };

        populateFolderStructure(root, paths);

        FileObjectCrawler crawler = new FileObjectCrawler(root, new FileObject[] {root.getFileObject("org/foo/file2.txt"), root.getFileObject("org/bar")}, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), null, CR, SuspendSupport.NOP);
        assertCollectedFiles("Wrong files collected", crawler.getResources(), new String[] {"org/bar/file1.txt","org/foo/file2.txt"});
    }

    public void testDuplicateResults5() throws IOException {
        FileObject root = FileUtil.createFolder(new File(getWorkDir(), "src"));
        String [] paths = new String [] {
                "org/bar/file1.txt",
                "org/foo/file2.txt",
        };

        populateFolderStructure(root, paths);

        FileObjectCrawler crawler = new FileObjectCrawler(root, new FileObject[] {root.getFileObject("org/bar"), root.getFileObject("org/foo/file2.txt")}, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), null, CR, SuspendSupport.NOP);
        assertCollectedFiles("Wrong files collected", crawler.getResources(), new String[] {"org/bar/file1.txt","org/foo/file2.txt"});
    }

    public void testDuplicateResults6() throws IOException {
        FileObject root = FileUtil.createFolder(new File(getWorkDir(), "src"));
        String [] paths = new String [] {
                "org/me/prj/foo/file2.txt",
                "org/me/prj/bar/file3.txt",
        };

        populateFolderStructure(root, paths);

        FileObjectCrawler crawler = new FileObjectCrawler(root, new FileObject[] {root.getFileObject("org/me/prj/bar/file3.txt"), root.getFileObject("org/me/prj/foo"), root.getFileObject("org/me/prj/bar")}, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), null, CR, SuspendSupport.NOP);
        assertCollectedFiles("Wrong files collected", crawler.getResources(), new String[] {"org/me/prj/foo/file2.txt","org/me/prj/bar/file3.txt"});
    }

    public void testDuplicateResults7() throws IOException {
        FileObject root = FileUtil.createFolder(new File(getWorkDir(), "src"));
        String [] paths = new String [] {
                "org/me/prj/foo/file2.txt",
                "org/me/prj/bar/file3.txt",
        };

        populateFolderStructure(root, paths);

        FileObjectCrawler crawler = new FileObjectCrawler(root, new FileObject[] {root.getFileObject("org/me/prj/bar/file3.txt"), root.getFileObject("org/me/prj/foo/file2.txt"), root.getFileObject("org/me")}, EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE), null, CR, SuspendSupport.NOP);
        assertCollectedFiles("Wrong files collected", crawler.getResources(), new String[] {"org/me/prj/foo/file2.txt","org/me/prj/bar/file3.txt"});
    }

    public void testDuplicateResults8() throws IOException {
        FileObject root = FileUtil.createFolder(new File(getWorkDir(), "src"));
        String [] paths = new String [] {
                "org/me/lib/impl/file1.txt",
                "org/me/prj/foo/file2.txt",
                "org/me/prj/bar/file3.txt",
        };

        populateFolderStructure(root, paths);

        FileObjectCrawler crawler = new FileObjectCrawler(
                root,
                new FileObject[] {
                    root.getFileObject("org/me/prj/bar/file3.txt"),
                    root.getFileObject("org/me/prj/foo/file2.txt"),
                    root.getFileObject("org/me/lib"),
                    root.getFileObject("org/me/prj")},
                EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE),
                null,
                CR,
                SuspendSupport.NOP);
        assertCollectedFiles("Wrong files collected", crawler.getResources(), new String[] {"org/me/lib/impl/file1.txt","org/me/prj/foo/file2.txt","org/me/prj/bar/file3.txt"});
    }

    public void testDuplicateRandomTest() throws IOException {
        if (!RUN_RANDOM_TESTS) {
            return;
        }
        final FileSystem fs = FileUtil.createMemoryFileSystem();
        String[] paths = generateRandomTree(TREE_DEPTH,TREE_CHILD_COUNT);
        FileObject root = fs.getRoot();
        populateFolderStructure(root, paths);
        Random r = new Random(System.currentTimeMillis());
        for (int runCount=0; runCount<TEST_COUNT; runCount++) {
            System.out.println("Run: " + runCount);
            final Set<FileObject> testSet = new HashSet<FileObject>();
            while (testSet.size() < SINGLE_TEST_SET_SIZE) {
                final String s = paths[r.nextInt(paths.length)];
                FileObject fo = root.getFileObject(s);
                final int shift = r.nextInt(TREE_DEPTH);
                for (int i = 0; i<shift; i++) {
                    fo = fo.getParent();
                }
                testSet.add(fo);
            }
            FileObjectCrawler crawler = new FileObjectCrawler(
                root,
                testSet.toArray(new FileObject[0]),
                EnumSet.<Crawler.TimeStampAction>of(Crawler.TimeStampAction.UPDATE),
                null,
                CR,
                SuspendSupport.NOP);
            assertCollectedFiles(
                    "Wrong files collected for: " + testSet,
                    crawler.getResources(),
                    crawl(testSet, root));
        }
    }

    private static String[] generateRandomTree(int depth, int childCount) {
        final Collection<String> res = generateRandomTree("",depth, childCount);
        return res.toArray(new String[0]);
    }

    private static Collection<String> generateRandomTree(String prefix, int depth, int childCount) {
        final Collection<String> res = new ArrayList<String>();
        if (depth > 0) {
            for (int i=0; i< childCount; i++) {
                res.addAll(generateRandomTree(String.format("%sdir%d/",prefix,i), depth-1, childCount));
            }
        } else {
            for (int i=0; i< childCount; i++) {
                res.add(String.format("%sfile%d.txt",prefix,i));
            }
        }
        return res;
    }

    private static String[] crawl(final Set<FileObject> files, final FileObject root) {
        final Set<String> collector = new HashSet<String>();
        for (FileObject fo : files) {
            crawl(fo, root, collector);
        }
        return collector.toArray(new String[0]);
    }

    private static void crawl(final FileObject file, final FileObject root, final Collection<? super String> collector) {
        if (file.isData()) {
            collector.add(FileUtil.getRelativePath(root,file));
        } else {
            for (FileObject fo : file.getChildren()) {
                crawl(fo, root, collector);
            }
        }
    }


    protected void assertCollectedFiles(String message, Collection<Indexable> resources, String... expectedPaths) throws IOException {
        List<String> collectedPaths = new ArrayList<String>();
        for(Indexable ii : resources) {
            collectedPaths.add(ii.getRelativePath());
        }
        List<String> expectedPathsFiltered = new ArrayList<String>();
        for(String path : expectedPaths) {
            if (!path.endsWith("/")) { // crawler only collects files
                expectedPathsFiltered.add(path);
            }
        }
        Collections.sort(collectedPaths);
        Collections.sort(expectedPathsFiltered);
        assertEquals(message, expectedPathsFiltered, collectedPaths);
    }

    private static void populateFolderStructure(FileObject root, String... filesOrFolders) throws IOException {
        for(String fileOrFolder : filesOrFolders) {
            if (fileOrFolder.endsWith("/")) {
                // folder
                FileObject folder = FileUtil.createFolder(root, fileOrFolder.substring(0, fileOrFolder.length() - 1));
            } else {
                // file
                FileObject file = FileUtil.createData(root, fileOrFolder);
            }
        }
    }
}