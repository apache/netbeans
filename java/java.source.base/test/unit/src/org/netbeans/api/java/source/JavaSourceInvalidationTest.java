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
package org.netbeans.api.java.source;

import javax.lang.model.element.TypeElement;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.TestFileUtils;

/**
 *
 * @author Tomas Zezula
 */
public class JavaSourceInvalidationTest extends NbTestCase {
    
    private FileObject wd;
    private FileObject cache;

    public JavaSourceInvalidationTest(@NonNull final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        super.setUp();
        this.clearWorkDir();
        wd = FileUtil.toFileObject(getWorkDir());
        cache = FileUtil.createFolder(wd, "cache"); //NOI18N
        IndexUtil.setCacheFolder(FileUtil.toFile(wd));
    }

    public void testJavacParserInvalidatedAfterScanSingleFileJS() throws Exception {
        final FileObject srcDir = FileUtil.createFolder(wd, "src");         //NOI18N
        final FileObject buildDir = FileUtil.createFolder(wd, "build");     //NOI18N
        final FileObject srcFile = FileUtil.toFileObject(TestFileUtils.writeFile(
                FileUtil.toFile(FileUtil.createData(srcDir,"foo/Src.java")),    //NOI18N
                "package foo; public class Src {}"));                           //NOI18N        
        final FileObject othFile = FileUtil.toFileObject(TestFileUtils.writeFile(
                FileUtil.toFile(FileUtil.createData(srcDir,"foo/Oth.java")),    //NOI18N
                "package foo; public class Oth {}"));                           //NOI18N
        SourceUtilsTestUtil.prepareTest(srcDir, buildDir,  cache);
        final JavaSource js = JavaSource.forFileObject(othFile);
        js.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController cc) throws Exception {
                final TypeElement te = cc.getElements().getTypeElement("foo.Src"); //NOI18N
                assertNull("The source element is not visible before scan", te);    //NOI18N
            }
        }, true);
        IndexingManager.getDefault().refreshIndexAndWait(srcDir.toURL(), null);
        js.runWhenScanFinished(new Task<CompilationController>() {
            @Override
            public void run(CompilationController cc) throws Exception {
                //Barrier
            }
        }, true).get();
        js.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController cc) throws Exception {
                final TypeElement te = cc.getElements().getTypeElement("foo.Src"); //NOI18N
                assertNotNull("The source element is visible after scan", te);      //NOI18N
            }
        }, true);

        final JavaSource js2 = JavaSource.forFileObject(othFile);
        js2.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController cc) throws Exception {
                final TypeElement te = cc.getElements().getTypeElement("foo.Src"); //NOI18N
                assertNotNull("The source element is visible after scan", te);  //NOI18N
            }
        }, true);        
    }

    public void testJavacParserInvalidatedAfterScanDifferentCPJS() throws Exception {
        final FileObject srcDir = FileUtil.createFolder(wd, "src");         //NOI18N
        final FileObject buildDir = FileUtil.createFolder(wd, "build");     //NOI18N
        FileUtil.toFileObject(TestFileUtils.writeFile(
                FileUtil.toFile(FileUtil.createData(srcDir,"foo/Src.java")),    //NOI18N
                "package foo; public class Src {}"));                            //NOI18N
        final ClassPath bootPath = BootClassPathUtil.getBootClassPath();
        final ClassPath compilePath = ClassPath.EMPTY;
        final ClassPath srcPath = ClassPathSupport.createClassPath(wd.getFileObject("src"));    //NOI18N
        SourceUtilsTestUtil.prepareTest(srcDir, buildDir,  cache);

        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, srcPath);
        final JavaSource js = JavaSource.create(cpInfo);
        js.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController cc) throws Exception {
                final TypeElement te = cc.getElements().getTypeElement("foo.Src"); //NOI18N
                assertNull("The source element is not visible before scan", te);    //NOI18N
            }
        }, true);

        IndexingManager.getDefault().refreshIndexAndWait(srcDir.toURL(), null);
        js.runWhenScanFinished(new Task<CompilationController>() {
            @Override
            public void run(CompilationController cc) throws Exception {
                //Barrier
            }
        }, true).get();

        final JavaSource js2 = JavaSource.create(cpInfo);
        js2.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController cc) throws Exception {
                final TypeElement te = cc.getElements().getTypeElement("foo.Src"); //NOI18N
                assertNotNull("The source element is visible after scan", te);      //NOI18N
            }
        }, true);

        js.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController cc) throws Exception {
                final TypeElement te = cc.getElements().getTypeElement("foo.Src"); //NOI18N
                assertNotNull("The source element is visible after scan", te);      //NOI18N
            }
        }, true);
    }

    public void testJavacParserInvalidatedAfterScanSingleCPJS() throws Exception {
        final FileObject srcDir = FileUtil.createFolder(wd, "src");         //NOI18N
        final FileObject buildDir = FileUtil.createFolder(wd, "build");     //NOI18N
        FileUtil.toFileObject(TestFileUtils.writeFile(
                FileUtil.toFile(FileUtil.createData(srcDir,"foo/Src.java")),    //NOI18N
                "package foo; public class Src {}"));                            //NOI18N
        final ClassPath bootPath = BootClassPathUtil.getBootClassPath();
        final ClassPath compilePath = ClassPath.EMPTY;
        final ClassPath srcPath = ClassPathSupport.createClassPath(wd.getFileObject("src"));    //NOI18N
        SourceUtilsTestUtil.prepareTest(srcDir, buildDir,  cache);

        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, srcPath);
        final JavaSource js = JavaSource.create(cpInfo);
        js.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController cc) throws Exception {
                final TypeElement te = cc.getElements().getTypeElement("foo.Src"); //NOI18N
                assertNull("The source element is not visible before scan", te);    //NOI18N
            }
        }, true);

        IndexingManager.getDefault().refreshIndexAndWait(srcDir.toURL(), null);
        js.runWhenScanFinished(new Task<CompilationController>() {
            @Override
            public void run(CompilationController cc) throws Exception {
                //Barrier
            }
        }, true).get();
        js.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController cc) throws Exception {
                final TypeElement te = cc.getElements().getTypeElement("foo.Src"); //NOI18N
                assertNotNull("The source element is visible after scan", te);      //NOI18N
            }
        }, true);
    }
}
