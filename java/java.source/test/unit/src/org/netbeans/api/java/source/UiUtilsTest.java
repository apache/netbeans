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

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.SharedClassObject;

import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.openide.util.Utilities;

/**
 * @author Tomas Zezula
 */
public class UiUtilsTest extends NbTestCase {
    private static final String JTABLE_DATA = "jdk/Table.java";    //NOI18N

    public UiUtilsTest(String testName) {
        super(testName);

    }

    protected void setUp() throws Exception {
        System.setProperty("org.openide.util.Lookup", SourceUtilsTestUtil.class.getName());
        this.clearWorkDir();
        SharedClassObject loader = JavaDataLoader.findObject(JavaDataLoader.class, true);
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[]{
                loader,
                new DummyClassPathProvider()
        });
        File f = new File(this.getWorkDir(), "cache");    //NOI18N
        f.mkdirs();
        IndexUtil.setCacheFolder(f);
    }

    protected void tearDown() throws Exception {
    }


    public void testOpen() throws IOException {
        FileObject workDir = FileUtil.toFileObject(this.getWorkDir());
        assertNotNull(workDir);
        FileObject dataDir = FileUtil.toFileObject(this.getDataDir());
        assertNotNull(dataDir);
        FileObject srcFile = createSource(dataDir, workDir);
        JavaSource js = JavaSource.forFileObject(srcFile);
        ClasspathInfo cpInfo = js.getClasspathInfo();
        CompilationInfo ci = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        Elements elements = ci.getElements();
        Element ce = elements.getTypeElement("test.Table");
        assertNotNull(ce);
        Object[] result = UiUtils.getOpenInfo(cpInfo, ce);
        assertNotNull(result);
        assertTrue(result[0] instanceof FileObject);
        assertTrue(result[1] instanceof Integer);
        assertEquals(srcFile, result[0]);
        assertEquals(824, ((Integer) result[1]).intValue());
    }

    private static FileObject getSrcRoot(FileObject wrkRoot) throws IOException {
        FileObject src = wrkRoot.getFileObject("src");    //NOI18N
        if (src == null) {
            src = wrkRoot.createFolder("src");        //NOI18N
        }
        return src;
    }

    private static FileObject createSource(FileObject dataRoot, FileObject wrkRoot) throws IOException {
        FileObject data = dataRoot.getFileObject(JTABLE_DATA);
        assertNotNull(data);
        FileObject srcRoot = getSrcRoot(wrkRoot);
        assertNotNull(srcRoot);
        FileObject pkg = FileUtil.createFolder(srcRoot, "test");        //NOI18N
        FileObject src = pkg.createData("Table.java");                //NOI18N
        FileLock lock = src.lock();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(data.getInputStream()));
            try {
                PrintWriter out = new PrintWriter(new OutputStreamWriter(src.getOutputStream(lock)));
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        out.println(line);
                    }
                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }
        } finally {
            lock.releaseLock();
        }
        return src;
    }

    private static ClassPath createSourcePath(FileObject wrkRoot) throws IOException {
        return ClassPathSupport.createClassPath(new FileObject[]{getSrcRoot(wrkRoot)});
    }

    private class DummyClassPathProvider implements ClassPathProvider {

        public ClassPath findClassPath(FileObject file, String type) {
            try {
                if (type == ClassPath.SOURCE) {
                    return createSourcePath(FileUtil.toFileObject(getWorkDir()));
                } else if (type == ClassPath.BOOT) {
                    return BootClassPathUtil.getBootClassPath();
                }
            } catch (IOException ioe) {
                //Skeep it
            }
            return ClassPathSupport.createClassPath(Collections.<PathResourceImplementation>emptyList());
        }
    }

}
