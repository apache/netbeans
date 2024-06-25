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
package org.netbeans.modules.java.file.launcher.queries;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.file.launcher.spi.SingleFileOptionsQueryImplementation;
import org.netbeans.modules.java.file.launcher.spi.SingleFileOptionsQueryImplementation.Result;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author lahvac
 */
public class MultiSourceRootProviderTest extends NbTestCase {

    private final TestResultImpl testResult = new TestResultImpl();

    public MultiSourceRootProviderTest(String name) {
        super(name);
    }

    public void testFindPackage() {
        assertEquals("test.pack.nested", MultiSourceRootProvider.findPackage("/*package*/package test/**pack*/\n.pack.//package\nnested;"));
        assertEquals(null, MultiSourceRootProvider.findPackage("/*package pack*/"));
    }

    public void testSourcePathFiltering() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject validTest = FileUtil.createData(wd, "valid/pack/Test1.java");
        FileObject invalidTest1 = FileUtil.createData(wd, "valid/pack/Test2.java");
        FileObject invalidTest2 = FileUtil.createData(wd, "valid/pack/Test3.java");

        TestUtilities.copyStringToFile(validTest, "package valid.pack;");
        TestUtilities.copyStringToFile(invalidTest1, "package invalid.pack;");
        TestUtilities.copyStringToFile(invalidTest2, "package invalid;");

        MultiSourceRootProvider provider = new MultiSourceRootProvider();
        ClassPath valid = provider.findClassPath(validTest, ClassPath.SOURCE);

        assertNotNull(valid);
        assertEquals(1, valid.entries().size());
        assertEquals(wd, valid.getRoots()[0]);

        assertNull(provider.findClassPath(invalidTest1, ClassPath.SOURCE));
        assertNull(provider.findClassPath(invalidTest2, ClassPath.SOURCE));
    }

    public void testRelativePaths() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject test = FileUtil.createData(wd, "src/pack/Test1.java");
        FileObject libJar = FileUtil.createData(wd, "libs/lib.jar");
        FileObject other = FileUtil.createFolder(wd, "other");
        FileObject otherLibJar = FileUtil.createData(other, "libs/lib.jar");
        FileObject otherLib2Jar = FileUtil.createData(other, "libs/lib2.jar");

        TestUtilities.copyStringToFile(test, "package pack;");

        testResult.setOptions("--class-path libs/lib.jar");
        testResult.setWorkDirectory(wd.toURI());

        MultiSourceRootProvider provider = new MultiSourceRootProvider();
        ClassPath compileCP = provider.findClassPath(test, ClassPath.COMPILE);
        AtomicInteger changeCount = new AtomicInteger();

        compileCP.addPropertyChangeListener(evt -> {
            if (ClassPath.PROP_ENTRIES.equals(evt.getPropertyName())) {
                changeCount.incrementAndGet();
            }
        });
        assertEquals(FileUtil.toFile(libJar).getAbsolutePath(), compileCP.toString());

        testResult.setWorkDirectory(other.toURI());

        assertEquals(1, changeCount.get());

        assertEquals(FileUtil.toFile(otherLibJar).getAbsolutePath(), compileCP.toString());

        testResult.setOptions("--class-path libs/lib2.jar");

        assertEquals(2, changeCount.get());

        assertEquals(FileUtil.toFile(otherLib2Jar).getAbsolutePath(), compileCP.toString());
    }

    public void testExpandModularDir() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject test = FileUtil.createData(wd, "src/pack/Test1.java");
        FileObject libsDir = FileUtil.createFolder(wd, "libs");
        FileObject lib1Jar = FileUtil.createData(libsDir, "lib1.jar");
        FileObject lib2Jar = FileUtil.createData(libsDir, "lib2.jar");
        FileObject lib3Dir = FileUtil.createFolder(libsDir, "lib3");

        FileUtil.createData(lib3Dir, "module-info.class");

        TestUtilities.copyStringToFile(test, "package pack;");

        testResult.setOptions("--module-path " + FileUtil.toFile(libsDir).getAbsolutePath());
        testResult.setWorkDirectory(FileUtil.toFileObject(getWorkDir()).toURI());

        MultiSourceRootProvider provider = new MultiSourceRootProvider();
        ClassPath moduleCP = provider.findClassPath(test, JavaClassPathConstants.MODULE_COMPILE_PATH);
        ClassPath compileCP = provider.findClassPath(test, ClassPath.COMPILE);

        assertEquals(new HashSet<>(Arrays.asList(FileUtil.getArchiveRoot(lib1Jar),
                                                 FileUtil.getArchiveRoot(lib2Jar),
                                                 lib3Dir)),
                     new HashSet<>(Arrays.asList(moduleCP.getRoots())));

        assertEquals(new HashSet<>(Arrays.asList(FileUtil.getArchiveRoot(lib1Jar),
                                                 FileUtil.getArchiveRoot(lib2Jar),
                                                 lib3Dir)),
                     new HashSet<>(Arrays.asList(compileCP.getRoots())));

        FileObject lib4Jar = FileUtil.createData(libsDir, "lib4.jar");

        assertEquals(new HashSet<>(Arrays.asList(FileUtil.getArchiveRoot(lib1Jar),
                                                 FileUtil.getArchiveRoot(lib2Jar),
                                                 lib3Dir,
                                                 FileUtil.getArchiveRoot(lib4Jar))),
                     new HashSet<>(Arrays.asList(moduleCP.getRoots())));

        assertEquals(new HashSet<>(Arrays.asList(FileUtil.getArchiveRoot(lib1Jar),
                                                 FileUtil.getArchiveRoot(lib2Jar),
                                                 lib3Dir,
                                                 FileUtil.getArchiveRoot(lib4Jar))),
                     new HashSet<>(Arrays.asList(compileCP.getRoots())));

        testResult.setOptions("--module-path " + FileUtil.toFile(lib1Jar).getAbsolutePath());

        assertEquals(new HashSet<>(Arrays.asList(FileUtil.getArchiveRoot(lib1Jar))),
                     new HashSet<>(Arrays.asList(moduleCP.getRoots())));
        assertEquals(new HashSet<>(Arrays.asList(FileUtil.getArchiveRoot(lib1Jar))),
                     new HashSet<>(Arrays.asList(compileCP.getRoots())));

        testResult.setOptions("--module-path " + FileUtil.toFile(lib3Dir).getAbsolutePath());

        assertEquals(new HashSet<>(Arrays.asList(lib3Dir)),
                     new HashSet<>(Arrays.asList(moduleCP.getRoots())));
        assertEquals(new HashSet<>(Arrays.asList(lib3Dir)),
                     new HashSet<>(Arrays.asList(compileCP.getRoots())));

        testResult.setOptions("--module-path " + FileUtil.toFile(libsDir).getAbsolutePath());

        assertEquals(new HashSet<>(Arrays.asList(FileUtil.getArchiveRoot(lib1Jar),
                                                 FileUtil.getArchiveRoot(lib2Jar),
                                                 lib3Dir,
                                                 FileUtil.getArchiveRoot(lib4Jar))),
                     new HashSet<>(Arrays.asList(moduleCP.getRoots())));

        assertEquals(new HashSet<>(Arrays.asList(FileUtil.getArchiveRoot(lib1Jar),
                                                 FileUtil.getArchiveRoot(lib2Jar),
                                                 lib3Dir,
                                                 FileUtil.getArchiveRoot(lib4Jar))),
                     new HashSet<>(Arrays.asList(compileCP.getRoots())));

        FileObject lib5Dir = FileUtil.createFolder(libsDir, "lib5Dir");

        assertEquals(new HashSet<>(Arrays.asList(FileUtil.getArchiveRoot(lib1Jar),
                                                 FileUtil.getArchiveRoot(lib2Jar),
                                                 lib3Dir,
                                                 FileUtil.getArchiveRoot(lib4Jar),
                                                 lib5Dir)),
                     new HashSet<>(Arrays.asList(moduleCP.getRoots())));

        assertEquals(new HashSet<>(Arrays.asList(FileUtil.getArchiveRoot(lib1Jar),
                                                 FileUtil.getArchiveRoot(lib2Jar),
                                                 lib3Dir,
                                                 FileUtil.getArchiveRoot(lib4Jar),
                                                 lib5Dir)),
                     new HashSet<>(Arrays.asList(compileCP.getRoots())));

        lib5Dir.delete();

        assertEquals(new HashSet<>(Arrays.asList(FileUtil.getArchiveRoot(lib1Jar),
                                                 FileUtil.getArchiveRoot(lib2Jar),
                                                 lib3Dir,
                                                 FileUtil.getArchiveRoot(lib4Jar))),
                     new HashSet<>(Arrays.asList(moduleCP.getRoots())));

        assertEquals(new HashSet<>(Arrays.asList(FileUtil.getArchiveRoot(lib1Jar),
                                                 FileUtil.getArchiveRoot(lib2Jar),
                                                 lib3Dir,
                                                 FileUtil.getArchiveRoot(lib4Jar))),
                     new HashSet<>(Arrays.asList(compileCP.getRoots())));

        lib4Jar.delete();

        assertEquals(new HashSet<>(Arrays.asList(FileUtil.getArchiveRoot(lib1Jar),
                                                 FileUtil.getArchiveRoot(lib2Jar),
                                                 lib3Dir)),
                     new HashSet<>(Arrays.asList(moduleCP.getRoots())));

        assertEquals(new HashSet<>(Arrays.asList(FileUtil.getArchiveRoot(lib1Jar),
                                                 FileUtil.getArchiveRoot(lib2Jar),
                                                 lib3Dir)),
                     new HashSet<>(Arrays.asList(compileCP.getRoots())));

        FileUtil.createData(libsDir, "module-info.class");

        assertEquals(new HashSet<>(Arrays.asList(libsDir)),
                     new HashSet<>(Arrays.asList(moduleCP.getRoots())));
        assertEquals(new HashSet<>(Arrays.asList(libsDir)),
                     new HashSet<>(Arrays.asList(compileCP.getRoots())));
    }

    public void testBrokenOptions() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject test = FileUtil.createData(wd, "src/pack/Test1.java");

        testResult.setOptions("--module-path");
        testResult.setWorkDirectory(FileUtil.toFileObject(getWorkDir()).toURI());

        MultiSourceRootProvider provider = new MultiSourceRootProvider();

        provider.findClassPath(test, JavaClassPathConstants.MODULE_COMPILE_PATH);
    }

    public void testMultiSourceRootProviderOnlySupportedForLocalFiles() throws IOException {
        File supportedFile = null;
        try {
            supportedFile = Files.createTempFile("dummy", ".java").toFile();
            FileObject realFileSource = FileUtil.createData(supportedFile);
            FileObject inMemorySource = FileUtil.createMemoryFileSystem().getRoot().createData("Ahoj.java");

            assertFalse(MultiSourceRootProvider.isSupportedFile(inMemorySource));
            assertTrue(MultiSourceRootProvider.isSupportedFile(realFileSource));
        } finally {
            if(supportedFile != null && supportedFile.exists()) {
                supportedFile.delete();
            }
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    @Override
    protected void runTest() throws Throwable {
        SingleFileOptionsQueryImplementation queryImpl = file -> testResult;
        ProxyLookup newQueryLookup = new ProxyLookup(Lookups.fixed(queryImpl),
                                                     Lookups.exclude(Lookup.getDefault(),
                                                                     SingleFileOptionsQueryImplementation.class));
        Lookups.executeWith(newQueryLookup, () -> {
            try {
                super.runTest();
            } catch (Error err) {
                throw err;
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Throwable ex) {
                throw new IllegalStateException(ex);
            }
        });
    }

    private static class TestResultImpl implements Result {

        private final ChangeSupport cs = new ChangeSupport(this);
        private final AtomicReference<String> options = new AtomicReference<>();
        private final AtomicReference<URI> workdir = new AtomicReference<>();

        public TestResultImpl() {
        }

        @Override
        public String getOptions() {
            return options.get();
        }

        public void setOptions(String options) {
            this.options.set(options);
            cs.fireChange();
        }

        @Override
        public URI getWorkDirectory() {
            return workdir.get();
        }

        public void setWorkDirectory(URI workdir) {
            this.workdir.set(workdir);
            cs.fireChange();
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }
    }

    static {
        MultiSourceRootProvider.SYNCHRONOUS_UPDATES = true;
    }
}
