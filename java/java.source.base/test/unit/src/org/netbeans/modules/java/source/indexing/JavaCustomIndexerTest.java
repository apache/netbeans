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
package org.netbeans.modules.java.source.indexing;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.spi.indexing.ErrorsCache;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation2;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lahvac
 */
public class JavaCustomIndexerTest extends NbTestCase {
    private BinQueryImpl2 binQuery2;

    public JavaCustomIndexerTest(String name) {
        super(name);
    }

    private FileObject src;

    public void testCopyTheClassInsteadOfCompiling() throws Exception {
        FileObject classesRoot = FileUtil.createFolder(src.getParent(), "mockclasses");

        FileObject testFile = FileUtil.createData(src, "test2/NoCompileTestData.java");
        TestUtilities.copyStringToFile(testFile, "package test2; public class Test { error!; }");

        FileObject testClass = FileUtil.createData(classesRoot, "test2/NoCompileTestData.class");
        try (
            OutputStream os = testClass.getOutputStream();
            InputStream is = getClass().getResourceAsStream("/test2/NoCompileTestData.class")
        ) {
            assertNotNull("test2/NoCompileTestData found", is);
            FileUtil.copy(is, os);
        }

        binQuery2.root = classesRoot.toURL();
        binQuery2.source = src.toURL();

        SourceUtilsTestUtil.compileRecursively(src);
        assertFalse("Nobody shall notice there is an error", ErrorsCache.isInError(src, true));
    }

    public void testMoveClassLivingElseWhere() throws Exception {
        FileObject testFile = FileUtil.createData(src, "test/Test.java");
        TestUtilities.copyStringToFile(testFile, "package test2; public class Test {}");
        SourceUtilsTestUtil.compileRecursively(src);
        FileObject test2Package = FileUtil.createFolder(src, "test2");
        FileLock lock = testFile.lock();
        testFile.move(lock, test2Package, "Test", "java");
        lock.releaseLock();
        SourceUtilsTestUtil.compileRecursively(src);
        assertFalse(ErrorsCache.isInError(src, true));
    }
    
    public void testFileNamesWithDots1() throws Exception {
        createSrcFile("test/Bar.java",
                      "package test;\n" +
                      "public class Bar {\n" +
                      "error\n" +
                      "}\n");
        SourceUtilsTestUtil.compileRecursively(src);
        createSrcFile("test/Bar.java.BACKUP.20991.java",
                      "package test;\n" +
                      "public class Bar {\n" +
                      "error\n" +
                      "}\n");
        IndexingManager.getDefault().refreshAllIndices(false, true, src);
        SourceUtils.waitScanFinished();
        File classFolder = JavaIndex.getClassFolder(src.toURL());
        assertExistsAndContent(classFolder, "test/Bar.java.BACKUP.20991.rs", "test.Bar\n");
    }
    
    public void testFileNamesWithDots2() throws Exception {
        createSrcFile("test/Bar.java.BACKUP.20991.java",
                      "package test;\n" +
                      "public class Bar {\n" +
                      "error\n" +
                      "}\n");
        SourceUtilsTestUtil.compileRecursively(src);
        createSrcFile("test/Bar.java",
                      "package test;\n" +
                      "public class Bar {\n" +
                      "error\n" +
                      "}\n");
        IndexingManager.getDefault().refreshAllIndices(false, true, src);
        SourceUtils.waitScanFinished();
        deleteFile("test/Bar.java.BACKUP.20991.java");
        createSrcFile("test/Bar.java",
                      "package test;\n" +
                      "public class Bar {\n" +
                      "}\n");
        IndexingManager.getDefault().refreshAllIndices(false, true, src);
        SourceUtils.waitScanFinished();
        assertFalse(ErrorsCache.isInError(src, true));
    }
    
    public void testInnerClassesDeleted() throws Exception {
        createSrcFile("test/Bar.java",
                      "package test;\n" +
                      "public class Bar {\n" +
                      " static class Inner { }\n" +
                      "}\n");
        SourceUtilsTestUtil.compileRecursively(src);
        createSrcFile("test/Bar.java",
                      "package test;\n" +
                      "public class Bar {\n" +
                      "}\n");
        SourceUtilsTestUtil.compileRecursively(src);
        File classFolder = JavaIndex.getClassFolder(src.toURL());
        assertFalse(new File(classFolder, "test/Bar$Inner." + FileObjects.SIG).canRead());
    }

    public void test199293() throws Exception {
        createSrcFile("test/A.java",
                      "package test;\n" +
                      "public class A implements I {\n" +
                      "}\n");
        createSrcFile("test/I.java",
                      "package test;\n" +
                      "public interface I {\n" +
                      "    public void t();\n" +
                      "}\n");
        SourceUtilsTestUtil.compileRecursively(src);
        assertTrue(ErrorsCache.isInError(src, true));
        Thread.sleep(1000);
        createSrcFile("test/I.java",
                      "package test;\n" +
                      "public interface I {\n" +
                      "}\n");
        IndexingManager.getDefault().refreshAllIndices(false, true, src);
        SourceUtils.waitScanFinished();
        assertFalse(ErrorsCache.getAllFilesInError(src.toURL()).toString(), ErrorsCache.isInError(src, true));
    }
    
    @Override
    protected void setUp() throws Exception {
        binQuery2 = new BinQueryImpl2();
        SourceUtilsTestUtil.prepareTest(new String[0], binQuery2);
        
        clearWorkDir();
        File wdFile = getWorkDir();
        FileUtil.refreshFor(wdFile);

        FileObject wd = FileUtil.toFileObject(wdFile);
        assertNotNull(wd);
        src = FileUtil.createFolder(wd, "src");
        FileObject buildRoot = FileUtil.createFolder(wd, "build");
        FileObject cache = FileUtil.createFolder(wd, "cache");

        SourceUtilsTestUtil.prepareTest(src, buildRoot, cache);
    }
    
    private FileObject createSrcFile(String pathAndName, String content) throws Exception {
        FileObject testFile = FileUtil.createData(src, pathAndName);
        TestUtilities.copyStringToFile(testFile, content);
        
        return testFile;
    }
    
    private void assertExistsAndContent(File dir, String pathAndName, String content) throws Exception {
        File target = new File(dir, pathAndName);
        assertTrue(target.getPath(), target.canRead());
        assertEquals(content.replace("\n", System.getProperty("line.separator", "\n")), TestUtilities.copyFileToString(target));
    }
    
    private void deleteFile(String pathAndName) throws IOException {
        FileObject testFile = src.getFileObject(pathAndName);
        assertNotNull(testFile);
        testFile.delete();
    }

    public static final class BinQueryImpl2 implements BinaryForSourceQueryImplementation2<URL> {
        URL source;
        URL root;

        @Override
        public URL findBinaryRoots2(URL sourceRoot) {
            if (source != null && source.equals(sourceRoot)) {
                return root;
            }
            return null;
        }

        @Override
        public URL[] computeRoots(URL result) {
            return new URL[]{root};
        }

        @Override
        public boolean computePreferBinaries(URL result) {
            return true;
        }

        @Override
        public void computeChangeListener(URL result, boolean add, ChangeListener l) {
        }
    }
}
