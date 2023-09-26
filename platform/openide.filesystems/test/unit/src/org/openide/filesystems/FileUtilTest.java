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

package org.openide.filesystems;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.Test;
import static org.junit.Assume.assumeFalse;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.BaseUtilities;
import org.openide.util.Utilities;
import org.openide.util.test.MockLookup;

/**
 * @author Jesse Glick, Jiri Skrivanek
 */
public class FileUtilTest extends NbTestCase {

    public FileUtilTest(String n) {
        super(n);
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();

        // These tests have to be run in correct order, see bug 231316.
        List<String> orderedMethods = new ArrayList<String>();
        orderedMethods.add("testGetMIMETypeConstrained");
        orderedMethods.add("testSetMIMEType");

        for (String methodName : orderedMethods) {
            suite.addTest(new FileUtilTest(methodName));
        }

        // Run other tests in any order.
        for (Method m : FileUtilTest.class.getMethods()) {
            if (m.getName().startsWith("test")
                    && m.getParameterTypes().length == 0
                    && m.getGenericReturnType().equals(Void.TYPE)
                    && !Modifier.isStatic(m.getModifiers())
                    && Modifier.isPublic(m.getModifiers())
                    && !orderedMethods.contains(m.getName())) {
                suite.addTest(new FileUtilTest(m.getName()));
            }
        }
        return suite;
    }

    @Override
    public void setUp() throws IOException {
        // folder of declarative resolvers must exist before MIME resolvers tests
        FileUtil.createFolder(FileUtil.getConfigRoot(), "Services/MIMEResolver");
    }

    public void testLowerAndCapitalNormalization() throws IOException {
        if (!BaseUtilities.isWindows()) {
            return;
        }
        clearWorkDir();
        
        File a = new File(getWorkDir(), "a");
        assertTrue("Lower case file created", a.createNewFile());
        File A = new File(getWorkDir(), "A");

        assertEquals("Normalizes to lower case", a.getAbsolutePath(), FileUtil.normalizeFile(A).getAbsolutePath());
        assertTrue("Can delete the file", a.delete());
        assertTrue("Can create capital file", A.createNewFile());
        assertEquals("Normalizes to capital case", A.getAbsolutePath(), FileUtil.normalizeFile(A).getAbsolutePath());
    }
    public void testNormalizationQuotations() throws IOException {
        clearWorkDir();
        
        File f1 = FileUtil.normalizeFile(new File("\""));
        File f2 = FileUtil.normalizeFile(f1);
        
        assertEquals(f1, f2);
    }

    public void testWrongNormalization() throws Exception {
        CharSequence log = Log.enable("org.openide.filesystems", Level.WARNING);
        final File file = new File("/../../tmp/");
        final File normalizedFile = FileUtil.normalizeFile(file);
        FileUtil.addFileChangeListener(
            new FileChangeAdapter() {},
            normalizedFile
        );
        assertEquals("No warnings:\n" + log, 0, log.length());
    }

    public void testToFileObjectSlash() throws Exception { // #98388
        if (!BaseUtilities.isUnix()) {
            return;
        }
        File root = new File("/");
        assertTrue(root.isDirectory());
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(root);
        final FileObject LFS_ROOT = lfs.getRoot();
        MockLookup.setInstances(new URLMapper() {
            public URL getURL(FileObject fo, int type) {
                return null;
            }
            public FileObject[] getFileObjects(URL url) {
                if (url.toExternalForm().equals("file:/")) {
                    return new FileObject[] {LFS_ROOT};
                } else {
                    return null;
                }
            }
        });
        URLMapper.reset();
        assertEquals(LFS_ROOT, FileUtil.toFileObject(root));
    }

    public void testArchiveConversion() throws Exception {
        final LocalFileSystem lfs = new LocalFileSystem();
        clearWorkDir();
        lfs.setRootDirectory(getWorkDir());
        MockLookup.setInstances(new URLMapper() {
            String rootURL = lfs.getRoot().toURL().toString();
            @Override
            public FileObject[] getFileObjects(URL url) {
                String u = url.toString();
                FileObject f = null;
                if (u.startsWith(rootURL)) {
                    f = lfs.findResource(u.substring(rootURL.length()));
                }
                return f != null ? new FileObject[] {f} : null;
            }
            @Override
            public URL getURL(FileObject fo, int type) {
                return null;
            }
        });
        URLMapper.reset();

        TestFileUtils.writeFile(lfs.getRoot(), "README", "A random file with some stuff in it.");
        assertCorrectURL("README", null, null); // not an archive
        TestFileUtils.writeFile(lfs.getRoot(), "README.txt", "A random file with some stuff in it.");
        assertCorrectURL("README.txt", null, null); // not an archive either
        TestFileUtils.writeFile(lfs.getRoot(), "empty.zip", "");
        assertCorrectURL("empty.zip", "jar:", "empty.zip!/");
        TestFileUtils.writeZipFile(lfs.getRoot(), "normal.zip", "something:text inside a ZIP entry");
        assertCorrectURL("normal.zip", "jar:", "normal.zip!/");
        assertCorrectURL("nonexistent.zip", "jar:", "nonexistent.zip!/");
        lfs.getRoot().createFolder("folder");
        assertCorrectURL("folder", "", "folder/");
        lfs.getRoot().createFolder("some.folder");
        assertCorrectURL("some.folder", "", "some.folder/");
        assertCorrectURL("nonexistent", "", "nonexistent/");
        assertCorrectURL("non existent.zip", "jar:", "non%20existent.zip!/");
        assertCorrectURL("non existent", "", "non%20existent/");

        assertCorrectFile("folder", "", "folder/");
        assertCorrectFile("stuff.zip", "jar:", "stuff.zip!/");
        assertCorrectFile(null, "jar:", "stuff.zip!/subentry/");
        assertCorrectFile(null, "http:", "");
        // Impossible to even construct such a URL: assertCorrectFolder("stuff.zip", "jar:", "stuff.zip");
        assertCorrectFile("stuff.zip", "", "stuff.zip");
        assertCorrectFile("folder", "", "folder");
        assertCorrectFile("fol der", "", "fol%20der/");
        assertCorrectFile("stu ff.zip", "jar:", "stu%20ff.zip!/");
        assertCorrectFile("stu ff.zip", "", "stu%20ff.zip");
        assertCorrectFile("fol der", "", "fol%20der");
    }
    private void assertCorrectURL(String filename, String expectedURLPrefix, String expectedURLSuffix) throws Exception {
        File d = getWorkDir();
        assertEquals(expectedURLSuffix == null ? null : new URL(expectedURLPrefix + BaseUtilities.toURI(d) + expectedURLSuffix),
                FileUtil.urlForArchiveOrDir(new File(d, filename)));
    }
    private void assertCorrectFile(String expectedFilename, String urlPrefix, String urlSuffix) throws Exception {
        assertEquals(expectedFilename == null ? null : new File(getWorkDir(), expectedFilename),
                FileUtil.archiveOrDirForURL(new URL(urlPrefix + BaseUtilities.toURI(getWorkDir()) + urlSuffix)));
    }

    /** Tests translation from jar resource url to jar archive url. */
    public void testGetArchiveFile() throws Exception {
        String urls[][] = {
            // resource url, expected jar url
            {"jar:file:/a.jar!/META-INF/MANIFEST.MF", "file:/a.jar"}, // unix root
            {"jar:file:/a/b/c/a.jar!/META-INF/MANIFEST.MF", "file:/a/b/c/a.jar"}, // unix
            {"jar:file:/C:/a.jar!/META-INF/MANIFEST.MF", "file:/C:/a.jar"}, // windows root
            {"jar:file:/C:/a/b/c/a.jar!/META-INF/MANIFEST.MF", "file:/C:/a/b/c/a.jar"}, // windows
            {"jar:file://computerName/sharedFolder/a.jar!/META-INF/MANIFEST.MF", "file:////computerName/sharedFolder/a.jar"}, // windows UNC root malformed
            {"jar:file://computerName/sharedFolder/a/b/c/a.jar!/META-INF/MANIFEST.MF", "file:////computerName/sharedFolder/a/b/c/a.jar"}, // windows UNC malformed
            {"jar:file:////computerName/sharedFolder/a.jar!/META-INF/MANIFEST.MF", "file:////computerName/sharedFolder/a.jar"}, // windows UNC root
            {"jar:file:////computerName/sharedFolder/a/b/c/a.jar!/META-INF/MANIFEST.MF", "file:////computerName/sharedFolder/a/b/c/a.jar"} // windows UNC
        };
        for (int i = 0; i < urls.length; i++) {
            assertEquals("FileUtil.getArchiveFile failed.", new URL(urls[i][1]), FileUtil.getArchiveFile(new URL(urls[i][0])));
        }
    }

    public void testIsArchiveFileRace() throws Exception {
        final LocalFileSystem lfs = new LocalFileSystem();
        clearWorkDir();
        final File wd = getWorkDir();
        lfs.setRootDirectory(wd);
        MockLookup.setInstances(new URLMapper() {
            String rootURL = lfs.getRoot().toURL().toString();
            @Override
            public FileObject[] getFileObjects(URL url) {
                String u = url.toString();
                FileObject f = null;
                if (u.startsWith(rootURL)) {
                    f = lfs.findResource(u.substring(rootURL.length()));
                }
                return f != null ? new FileObject[] {f} : null;
            }
            @Override
            public URL getURL(FileObject fo, int type) {
                return null;
            }
        });
        URLMapper.reset();
        final File testFile = new File (wd,"test.jar"); //NOI18N
        FileUtil.createData(testFile);

        final Logger log = Logger.getLogger(JarArchiveRootProvider.class.getName());
        log.setLevel(Level.FINEST);
        final Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                if ("isArchiveFile_FILE_RESOLVED".equals(record.getMessage())) {  //NOI18N
                    try {
                        final FileObject fo = (FileObject) record.getParameters()[0];
                        fo.delete();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            @Override
            public void flush() {
            }
            @Override
            public void close() throws SecurityException {
            }
        };
        log.addHandler(handler);
        try {
            final boolean result = FileUtil.isArchiveFile(BaseUtilities.toURI(testFile).toURL());
            assertTrue("The test.jar should be archive.",result);   //NOI18N
        } finally {
            log.removeHandler(handler);
        }
    }

    /** Tests normalizeFile() method. */
    public void testNormalizeFile() throws IOException {
        // pairs of path before and after normalization
        Map<String, String> paths = createNormalizedPaths();

        for (String path : paths.keySet()) {
            File file = new File(path);
            assertTrue("Idempotency violated for path: " + path, FileUtil.normalizeFile(FileUtil.normalizeFile(file)).equals(FileUtil.normalizeFile(file)));
            assertEquals("File not normalized: " + path, paths.get(path), FileUtil.normalizeFile(file).getPath());
        }
    }
    
    public void testNormalizeNonExistingButNotAccessibleRootOnWindows() throws IOException {
        if (!BaseUtilities.isWindows()) {
            return;
        }
        for (File r : File.listRoots()) {
            // hopefully one of them is a CD drive
            File g = new File(r + "\\my\\.");
            File gn = FileUtil.normalizeFile(g);
            File gnn = FileUtil.normalizeFile(gn);
            assertEquals("Normalized: " + g, gn, gnn);
        }
        
    }

    public void testNormalizeFileIsCached() throws Exception {
        File f = new File(getWorkDir(), "text.txt");
        CharSequence log = Log.enable(FileUtil.class.getName(), Level.FINE);
        File one = FileUtil.normalizeFile(f);
        String msg = "FileUtil.normalizeFile for " + f;
        if (log.toString().indexOf(msg) == -1) {
            fail("One query for the file shall be in logs:\n" + log);
        }
        CharSequence log2 = Log.enable(FileUtil.class.getName(), Level.FINE);
        File two = FileUtil.normalizeFile(f);
        if (log2.toString().contains(msg)) {
            fail("No second FileUtil.normalizeFile for in:\n" + log);
        }
        assertEquals("Files are equal", one, two);
    }

    /** Tests normalizePath() method. */
    public void testNormalizePath() throws IOException {
        // pairs of path before and after normalization
        Map<String, String> paths = createNormalizedPaths();

        for (String path : paths.keySet()) {
            assertTrue("Idempotency violated for path: " + path, FileUtil.normalizePath(FileUtil.normalizePath(path)).equals(FileUtil.normalizePath(path)));
            assertEquals("File path not normalized: " + path, paths.get(path), FileUtil.normalizePath(path));
        }
    }

    private Map<String, String> createNormalizedPaths() throws IOException {
        // pairs of path before and after normalization
        Map<String, String> paths = new HashMap<String, String>();
        if (BaseUtilities.isWindows()) {
            paths.put("A:\\", "A:\\");
            paths.put("A:\\dummy", "A:\\dummy");
            paths.put("a:\\", "A:\\");
            try {
                new File("a:\\dummy").getCanonicalPath();
                paths.put("a:\\dummy", "A:\\dummy");
            } catch (IOException e) {
                // if getCanonicalPath fails, normalization returns File.getAbsolutePath
                paths.put("a:\\dummy", "a:\\dummy");
            }
            paths.put("C:\\", "C:\\");
            paths.put("C:\\dummy", "C:\\dummy");
            paths.put("c:\\", "C:\\");
            paths.put("c:\\dummy", "C:\\dummy");
            paths.put("c:\\.", "C:\\");
            paths.put("c:\\..", "C:\\");
            paths.put("c:\\dummy\\.", "C:\\dummy");
            paths.put("c:\\dummy\\..", "C:\\");
            paths.put("c:\\dummy\\.\\foo", "C:\\dummy\\foo");
            paths.put("c:\\dummy\\..\\foo", "C:\\foo");
            paths.put("\\\\", "\\\\");
            paths.put("\\\\computerName\\sharedFolder", "\\\\computerName\\sharedFolder");
            paths.put("\\\\computerName\\sharedFolder\\dummy\\.", "\\\\computerName\\sharedFolder\\dummy");
            paths.put("\\\\computerName\\sharedFolder\\dummy\\..", "\\\\computerName\\sharedFolder");
            paths.put("\\\\computerName\\sharedFolder\\dummy\\.\\foo", "\\\\computerName\\sharedFolder\\dummy\\foo");
            paths.put("\\\\computerName\\sharedFolder\\dummy\\..\\foo", "\\\\computerName\\sharedFolder\\foo");
        } else {
            paths.put("/", "/");
            paths.put("/dummy/.", "/dummy");
            paths.put("/dummy/..", "/");
            paths.put("/dummy/./foo", "/dummy/foo");
            paths.put("/dummy/../foo", "/foo");
        }
        // #137407 - java.io.File(".") should be normalized
        paths.put(".", new File(".").getCanonicalPath());
        paths.put("..", new File("..").getCanonicalPath());
        return paths;
    }
    
    public void testNormalizePathChangeCase() throws Exception {
        if (!BaseUtilities.isWindows()) {
            return;
        }
        clearWorkDir();
        File path = new File(getWorkDir(), "dir");
        path.mkdirs();
        File FILE = new File(path, "FILE");
        FILE.createNewFile();

        File file = new File(path, "file");
        File n2 = FileUtil.normalizeFile(file);
        assertNormalized(n2);
        
        FILE.renameTo(file);
        new FileRenameEvent(FileUtil.getConfigRoot(), "x", "y"); // flushes the caches
        File n1 = FileUtil.normalizeFile(FILE);
        assertNormalized(n1);
        
        assertEquals("now it has to normalize to lowercase", "file", n1.getName());
    }

    public void testNormalizePathIsCached() throws Exception {
        File f = new File(getWorkDir(), "textPath.txt");
        String path = f.getPath();
        CharSequence log = Log.enable(FileUtil.class.getName(), Level.FINE);
        String one = FileUtil.normalizePath(path);
        String msg = "FileUtil.normalizeFile for " + f;
        if (log.toString().indexOf(msg) == -1) {
            fail("One query for the file shall be in logs:\n" + log);
        }
        CharSequence log2 = Log.enable(FileUtil.class.getName(), Level.FINE);
        String two = FileUtil.normalizePath(path);
        if (log2.toString().contains(msg)) {
            fail("No second FileUtil.normalizeFile for in:\n" + log);
        }
        assertEquals("Files are equal", one, two);
    }

    /** Tests that only resolvers are queried which supply at least one of
     * MIME types given in array in FileUtil.getMIMEType(fo, String[]).
     * See issue 137734.
     */
    public void testGetMIMETypeConstrained() throws IOException {
        MyResolver resolver = new MyResolver();
        MockLookup.setInstances(resolver);
        assertNotNull(Lookup.getDefault().lookup(MyResolver.class));
        FileObject testFolder = FileUtil.createMemoryFileSystem().getRoot();

        FileObject fo = FileUtil.createData(testFolder, "fo1.mime1");
        String[] withinMIMETypes = null;
        try {
            fo.getMIMEType(withinMIMETypes);
            fail("FileUtil.getMIMEType(fo, null) should throw IllegalArgumentException.");
        } catch (NullPointerException npe) {
            // exception correctly thrown
        }
        assertNull(fo.getMIMEType("text/x-fakemime", null));
        
        fo = FileUtil.createData(testFolder, "fo2.mime1");
        withinMIMETypes = new String[0];
        fo.getMIMEType(withinMIMETypes);
        assertTrue("Resolver should be queried if array of desired MIME types is empty.", MyResolver.wasQueried());
        
        fo = FileUtil.createData(testFolder, "fo3.mime1");
        withinMIMETypes = new String[]{"mime3", "mime4"};
        FileUtil.getMIMEType(fo, withinMIMETypes);
        assertFalse("Resolver should not be queried if array of desired MIME types doesn't match MIMEResolver.getMIMETypes.", MyResolver.wasQueried());

        fo = FileUtil.createData(testFolder, "fo4.mime1");
        withinMIMETypes = new String[]{"mime1", "mime4"};
        FileUtil.getMIMEType(fo, withinMIMETypes);
        assertTrue("Resolver should be queried if one item in array of desired MIME types matches MIMEResolver.getMIMETypes.", MyResolver.wasQueried());

        fo = FileUtil.createData(testFolder, "fo5.mime1");
        withinMIMETypes = new String[]{"mime1", "mime2"};
        fo.getMIMEType(withinMIMETypes);
        assertTrue("Resolver should be queried if both items in array of desired MIME types matches MIMEResolver.getMIMETypes.", MyResolver.wasQueried());
    }

    private static void assertNormalized(File path) {
        assertEquals("Really normalized", path, FileUtil.normalizeFile(path));
    }

    /** MIMEResolver used in testGetMIMETypeConstrained. */
    public static final class MyResolver extends MIMEResolver {

        public MyResolver() {
            super("mime1", "mime2");
        }

        /** Always returns null and change value to signal it's been queried. */
        public String findMIMEType(FileObject fo) {
            queried = true;
            return null;
        }
        private static boolean queried = false;

        public static boolean wasQueried() {
            boolean wasQueried = queried;
            queried = false;
            return wasQueried;
        }
    }
    
    /** Test recovery of FileUtil.createFolder(FileObject, String) method when
     * other thread created folder in the middle of processing (see #152219).
     */
    public void testFolderAlreadyExists152219() {
        final FileObject folder = FileUtil.createMemoryFileSystem().getRoot();
        final String name = "subfolder";
        Handler handler = new Handler() {

            @Override
            public void publish(LogRecord record) {
                if (record.getMessage().equals("createFolder - before create folder if not exists.")) {
                    try {
                        folder.createFolder(name);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        };
        Logger logger = Logger.getLogger(FileUtil.class.getName());
        logger.addHandler(handler);
        logger.setLevel(Level.FINEST);
        try {
            FileUtil.createFolder(folder, name);
        } catch (IOException ioe) {
            fail("FileUtil.createFolder(FileObject, String) should try to refresh folder because other thread can create folder before.");
        } finally {
            logger.removeHandler(handler);
        }
    }

    /** Tests FileUtil.setMIMEType method (see #153202). */
    public void testSetMIMEType() throws IOException {
        FileObject testRoot = FileUtil.createMemoryFileSystem().getRoot();
        FileObject g1FO = testRoot.createData("a", "g1");
        FileObject g2FO = testRoot.createData("a", "g2");
        FileObject xmlFO = testRoot.createData("a", "xml");
        String gifMIMEType = "image/gif";
        String bmpMIMEType = "image/bmp";
        String xmlMIMEType = "text/xml";
        String unknownMIMEType = "content/unknown";

        assertEquals("Wrong MIME type.", unknownMIMEType, g1FO.getMIMEType());
        assertEquals("Wrong list of extensions.", Collections.EMPTY_LIST, FileUtil.getMIMETypeExtensions(bmpMIMEType));
        assertEquals("Wrong list of extensions.", Collections.EMPTY_LIST, FileUtil.getMIMETypeExtensions(gifMIMEType));
        // xml registered to text/xml as fallback
        assertEquals("Wrong MIME type.", xmlMIMEType, xmlFO.getMIMEType());

         // {image/bmp=[g1]}
        FileUtil.setMIMEType("g1", bmpMIMEType);
        assertEquals("Wrong list of extensions.", Collections.singletonList("g1"), FileUtil.getMIMETypeExtensions(bmpMIMEType));
         // {image/bmp=[g1, g2]}
        FileUtil.setMIMEType("g2", bmpMIMEType);
        assertEquals("Wrong MIME type.", bmpMIMEType, g1FO.getMIMEType());
        assertEquals("Wrong MIME type.", bmpMIMEType, g2FO.getMIMEType());
        assertTrue("Wrong list of extensions.", Arrays.asList("g1", "g2").containsAll(FileUtil.getMIMETypeExtensions(bmpMIMEType)));
         // {image/bmp=[g2], image/gif=[g1]}
        FileUtil.setMIMEType("g1", gifMIMEType);
        assertEquals("Wrong MIME type.", gifMIMEType, g1FO.getMIMEType());
        assertEquals("Wrong list of extensions.", Arrays.asList("g1"), FileUtil.getMIMETypeExtensions(gifMIMEType));
        assertEquals("Wrong list of extensions.", Arrays.asList("g2"), FileUtil.getMIMETypeExtensions(bmpMIMEType));
         // {image/gif=[g1]}
        FileUtil.setMIMEType("g2", null);
        assertEquals("Wrong MIME type.", unknownMIMEType, g2FO.getMIMEType());
        assertEquals("Wrong list of extensions.", Arrays.asList("g1"), FileUtil.getMIMETypeExtensions(gifMIMEType));
        assertEquals("Wrong list of extensions.", Collections.EMPTY_LIST, FileUtil.getMIMETypeExtensions(bmpMIMEType));
    }

    /** Tests getConfigFile method (see #91534). */
    public void testGetConfigFile() throws IOException {
        @SuppressWarnings("deprecation")
        FileObject rootDFS = Repository.getDefault().getDefaultFileSystem().getRoot();
        assertNotNull("Sample FileObject not created.", rootDFS.createFolder("folder1").createFolder("folder2").createData("file.ext"));
        assertNotNull("Existing FileObject not found.", FileUtil.getConfigFile("folder1/folder2/file.ext"));
        assertNull("Path with backslashes is not valid.", FileUtil.getConfigFile("folder1\\folder2\\file.ext"));
        assertEquals("Root should be returned for empty path.", rootDFS, FileUtil.getConfigFile(""));
        assertEquals("Root should be returned from getConfigRoot", rootDFS, FileUtil.getConfigRoot());
        try {
            FileUtil.getConfigFile(null);
            fail("NullPointerException should be thrown for null path.");
        } catch (NullPointerException npe) {
            // OK
        }
    }
    
      public void testGetLocalConfigFile() throws IOException {
            // must be here and not in setUp() - possible [global] config file manipulation
            // MockLookup cannot change instances, so reset() must be called to invalidate cache in Repository
            Repository.reset();
            final FileSystem memFS = new MemoryFileSystem();

            try {
                // the local repo does not delegate to the default one, for simplicity.
                MockLookup.setInstances(
                      new Repository.LocalProvider() {
                            @Override
                            public Repository getRepository() throws IOException {
                                  return new Repository(memFS);
                            }
                      }
                );
                FileObject rootLFS = memFS.getRoot();
                FileObject rootDFS = Repository.getDefault().getDefaultFileSystem().getRoot();
                // folder1 is created by other test, stored in static MemoryFS variable.
                assertNotNull("Sample FileObject not created.", rootDFS.createFolder("folder3").createFolder("folder4").createData("file.ext"));

                rootLFS.createFolder("folderA").createFolder("folderB").createData("foo.bar");

                assertNull("Global file is not visible as local config", FileUtil.getConfigFile("folder3"));
                assertNull("Local file not visible in global repo", rootDFS.getFileObject("folderA"));

                assertNotNull("Configuration is found locally", FileUtil.getConfigFile("folderA"));
                assertNotNull("Global configuration is accessible", FileUtil.getSystemConfigFile("folder3"));
            } finally {
                // cleanup for other tests
                MockLookup.setInstances();
                Repository.reset();
            }
      }

    /** Tests that refreshAll runs just once in time (see #170556). */
    public void testRefreshConcurrency() throws Exception {
        Logger logger = Logger.getLogger(FileUtil.class.getName());
        logger.setLevel(Level.FINE);
        final AtomicInteger concurrencyCounter = new AtomicInteger(0);
        final AtomicInteger maxConcurrency = new AtomicInteger(0);
        final AtomicInteger calledCounter = new AtomicInteger(0);
        
        final RequestProcessor RP = new RequestProcessor("testRefreshConcurrency", 20);
        final List<RequestProcessor.Task> waitFor = new ArrayList<RequestProcessor.Task>();
        
        logger.addHandler(new Handler() {

            private boolean concurrentStarted = false;

            @Override
            public void publish(LogRecord record) {
                if (record.getMessage().equals("refreshAll - started")) {
                    calledCounter.incrementAndGet();
                    concurrencyCounter.incrementAndGet();
                    if (!concurrentStarted) {
                        concurrentStarted = true;
                        waitFor.add(RP.post(new Runnable() {
                            @Override
                            public void run() {
                                FileUtil.refreshAll();
                            }
                        }));
                        synchronized (this) {
                            try {
                                wait(500);
                            } catch (InterruptedException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                } else if (record.getMessage().equals("refreshAll - scheduled")) {
                    if (concurrentStarted) {
                        synchronized (this) {
                            notifyAll();
                        }
                    }
                } else if (record.getMessage().equals("refreshAll - finished")) {
                    concurrencyCounter.decrementAndGet();
                    if (concurrencyCounter.get() > maxConcurrency.get()) {
                        maxConcurrency.set(concurrencyCounter.get());
                    }
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        });
        FileUtil.refreshAll();
        for (RequestProcessor.Task task : waitFor) {
            task.waitFinished();
        }
        assertEquals("FileUtil.refreshAll should not be called concurrently.", 0, maxConcurrency.get());
        assertEquals("FileUtil.refreshAll not called.", 2, calledCounter.get());
    }
    
    public void testUrlForArchiveOrDirDirDeletedRace () throws IOException {
        final File workDir = getWorkDir();
        final File dir = new File(workDir, "testWorkDir");  //NOI18N
        dir.mkdir();
        final Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                if ("urlForArchiveOrDir:toURI:entry".equals(record.getMessage()) && //NOI18N
                    dir.exists()) {
                    dir.delete();
                }
            }

            @Override
            public void flush() {
            }
            @Override
            public void close() throws SecurityException {
            }
        };
        final Logger log = Logger.getLogger(FileUtil.class.getName());
        log.setLevel(Level.FINEST);
        log.addHandler(handler);
        final URL result = FileUtil.urlForArchiveOrDir(dir);
        assertNotNull(result);
        assertTrue(result.toExternalForm().endsWith("/"));  //NOI18N
    }

    public void testCopyPosixPerms() throws Exception {
        assumeFalse(Utilities.isWindows());

        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(new File("/"));
        FileObject workDir = lfs.findResource(getWorkDir().getAbsolutePath());
        clearWorkDir();
        
        FileObject source = workDir.createData("original.file");
        Set<PosixFilePermission> perms = new HashSet<>(Files.getPosixFilePermissions(FileUtil.toPath(source)));
        assertFalse(perms.contains(PosixFilePermission.OWNER_EXECUTE));
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        Files.setPosixFilePermissions(FileUtil.toPath(source), perms);
                
        FileObject dest = FileUtil.copyFile(source, workDir, "copied.file");
        perms = Files.getPosixFilePermissions(FileUtil.toPath(dest));
        assertTrue(perms.contains(PosixFilePermission.OWNER_EXECUTE));
    }
    
    public void testCopyAttributes() throws Exception {
        FileObject testRoot = FileUtil.createMemoryFileSystem().getRoot();
        FileObject aFile = testRoot.createData("a", "file");
        FileObject aTemplate = testRoot.createData("b", "template");
        
        aFile.setAttribute("attr", 1);
        aFile.setAttribute("SystemFileSystem.icon", "fakeValue");
        aFile.setAttribute("templateCategory", "fakeValue2");
        
        aTemplate.setAttribute("template", Boolean.TRUE);
        aTemplate.setAttribute("filtered.one", Boolean.TRUE);
        aTemplate.setAttribute("filtered.two", Boolean.TRUE);
        
        FileObject bFile = testRoot.createData("b", "file");
        FileUtil.copyAttributes(aFile, bFile);
        assertNull(bFile.getAttribute("SystemFileSystem.icon"));
        assertNull(bFile.getAttribute("templateCategory"));
        assertEquals(1, bFile.getAttribute("attr"));
        
        FileObject cFile = testRoot.createData("c", "file");
        FileUtil.copyAttributes(aTemplate, cFile, (n, v) -> {
            if ("filtered.one".equals(n)) {
                return null;
            } else if ("filtered.two".equals(n)) {
                return 42;
            } else {
                return FileUtil.defaultAttributesTransformer().apply(n, v);
            }
        });
        assertEquals(Boolean.TRUE, cFile.getAttribute("template"));
        assertNull(cFile.getAttribute("filtered.one"));
        assertEquals(42, cFile.getAttribute("filtered.two"));
    }

}
