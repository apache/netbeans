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
package org.netbeans.modules.testng.ui.actions;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.SharedClassObject;

/**
 *
 * @author lukas
 */
public class RetoucheTestBase extends NbTestCase {

    private FileObject testFO;

    public RetoucheTestBase(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        final FileObject sd = SourceUtilsTestUtil.makeScratchDir(this);
        final FileObject src = sd.createFolder("src");
        ClassPathProvider cpp = new ClassPathProvider() {

            public ClassPath findClassPath(FileObject file, String type) {
                if (type.equals(ClassPath.SOURCE)) {
                    return ClassPathSupport.createClassPath(new FileObject[]{src});
                }
                if (type.equals(ClassPath.COMPILE)) {
                    return ClassPathSupport.createClassPath(new FileObject[0]);
                }
                if (type.equals(ClassPath.BOOT)) {
                    return createClassPath(System.getProperty("sun.boot.class.path"));
                }
                return null;
            }
        };
        SharedClassObject loader = JavaDataLoader.findObject(JavaDataLoader.class, true);

        SourceUtilsTestUtil.prepareTest(src, sd.createFolder("build"), sd.createFolder("cache"));
        SourceUtilsTestUtil.prepareTest(
                new String[]{},
                new Object[]{loader, cpp});
        testFO = FileUtil.createFolder(src, "sample/pkg/").createData("Test.java");
    }

    protected FileObject getTestFO() {
        return testFO;
    }

    private static ClassPath createClassPath(String classpath) {
        StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
        List<PathResourceImplementation> list = new ArrayList<PathResourceImplementation>();
        while (tokenizer.hasMoreTokens()) {
            String item = tokenizer.nextToken();
            File f = FileUtil.normalizeFile(new File(item));
            URL url = getRootURL(f);
            if (url != null) {
                list.add(ClassPathSupport.createResource(url));
            }
        }
        return ClassPathSupport.createClassPath(list);
    }

    private static URL getRootURL(File f) {
        URL url = null;
        try {
            if (isArchiveFile(f)) {
                url = FileUtil.getArchiveRoot(f.toURI().toURL());
            } else {
                url = f.toURI().toURL();
                String surl = url.toExternalForm();
                if (!surl.endsWith("/")) {
                    url = new URL(surl + "/");
                }
            }
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
        return url;
    }

    private static boolean isArchiveFile(File f) {
        // the f might not exist and so you cannot use e.g. f.isFile() here
        String fileName = f.getName().toLowerCase();
        return fileName.endsWith(".jar") || fileName.endsWith(".zip");    //NOI18N
    }
}
