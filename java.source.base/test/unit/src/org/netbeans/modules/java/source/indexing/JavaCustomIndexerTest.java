/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.indexing;

import java.io.File;
import java.io.IOException;
import static org.junit.Assert.*;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.spi.indexing.ErrorsCache;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lahvac
 */
public class JavaCustomIndexerTest extends NbTestCase {

    public JavaCustomIndexerTest(String name) {
        super(name);
    }

    private FileObject src;

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
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        
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
}
