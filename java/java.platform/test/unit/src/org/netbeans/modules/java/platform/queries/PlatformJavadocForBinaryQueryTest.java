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
package org.netbeans.modules.java.platform.queries;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.TestJavaPlatformProvider;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Zezula
 */
public class PlatformJavadocForBinaryQueryTest extends NbTestCase {

    public PlatformJavadocForBinaryQueryTest(@NonNull final String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockServices.setServices(PlatformJavadocForBinaryQuery.class,
            TestJavaPlatformProvider.class);
    }

    public void testJavadocFolders () throws Exception {
        final File wd = this.getWorkDir();
        final FileObject wdfo = FileUtil.toFileObject(wd);
        final FileObject golden1 = FileUtil.createFolder(wdfo,"test1/docs/api/index-files").getParent();        //NOI18N
        final FileObject golden2 = FileUtil.createFolder(wdfo,"test2/docs/ja/api/index-files").getParent();     //NOI18N
        FileObject testFo = wdfo.getFileObject("test1");                                                        //NOI18N
        FileObject res = PlatformJavadocForBinaryQuery.R.findIndexFolder(testFo);
        assertEquals(res, golden1);
        testFo = wdfo.getFileObject("test1/docs");                                                              //NOI18N
        res = PlatformJavadocForBinaryQuery.R.findIndexFolder(testFo);
        assertEquals(res, golden1);
        testFo = wdfo.getFileObject("test2");                                                                   //NOI18N
        res = PlatformJavadocForBinaryQuery.R.findIndexFolder(testFo);
        assertEquals(res, golden2);
        testFo = wdfo.getFileObject("test2/docs");                                                              //NOI18N
        res = PlatformJavadocForBinaryQuery.R.findIndexFolder(testFo);
        assertEquals(res, golden2);
        testFo = wdfo.getFileObject("test2/docs/ja");                                                           //NOI18N
        res = PlatformJavadocForBinaryQuery.R.findIndexFolder(testFo);
        assertEquals(res, golden2);
    }

    public void testTwoPlatformsoverSameSDK() throws Exception {
        final File binDir = new File(getWorkDir(),"boot");  //NOI18N
        binDir.mkdir();
        final File jdocFile1 = new File(getWorkDir(),"jdoc1");   //NOI18N
        jdocFile1.mkdir();
        final File jdocFile2 = new File(getWorkDir(),"jdoc2");  //NOI18N
        jdocFile2.mkdir();
        TestJavaPlatformProvider provider = Lookup.getDefault().lookup(TestJavaPlatformProvider.class);
        final URL binRoot = BaseUtilities.toURI(binDir).toURL();
        final ClassPath bootCp = ClassPathSupport.createClassPath(binRoot);
        final List<URL> javadoc1 = Collections.singletonList(BaseUtilities.toURI(jdocFile1).toURL());
        final List<URL> javadoc2 = Collections.singletonList(BaseUtilities.toURI(jdocFile2).toURL());
        final TestJavaPlatform platform1 = new TestJavaPlatform("platform1", bootCp);   //NOI18N
        final TestJavaPlatform platform2 = new TestJavaPlatform("platform2", bootCp);   //NOI18N
        platform2.setJavadoc(javadoc2);
        provider.addPlatform(platform1);
        provider.addPlatform(platform2);

        final JavadocForBinaryQuery.Result result1 = JavadocForBinaryQuery.findJavadoc(binRoot);
        assertEquals(javadoc2, Arrays.asList(result1.getRoots()));

        platform1.setJavadoc(javadoc1);
        assertEquals(javadoc1, Arrays.asList(result1.getRoots()));

        final JavadocForBinaryQuery.Result result2 = JavadocForBinaryQuery.findJavadoc(binRoot);
        assertEquals(javadoc1, Arrays.asList(result2.getRoots()));

        platform1.setJavadoc(Collections.<URL>emptyList());
        assertEquals(javadoc2, Arrays.asList(result1.getRoots()));
        assertEquals(javadoc2, Arrays.asList(result2.getRoots()));
    }
}
