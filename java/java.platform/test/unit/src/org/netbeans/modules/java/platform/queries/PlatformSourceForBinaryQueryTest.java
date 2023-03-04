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
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Arrays;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.TestJavaPlatformProvider;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.Utilities;

/**
 * @author Tomas Zezula
 */
public class PlatformSourceForBinaryQueryTest extends NbTestCase {

    public PlatformSourceForBinaryQueryTest(String testName) {
        super(testName);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockServices.setServices(
            NBJRTStreamHandlerFactory.class,
            PlatformSourceForBinaryQuery.class,
            TestJavaPlatformProvider.class);
    }

    public void testUnregisteredPlatform() throws Exception {
        File wd = getWorkDir();
        FileObject wdo = FileUtil.toFileObject(wd);
        assertNotNull(wdo);
        FileObject p1 = wdo.createFolder("platform1");
        FileObject fo = p1.createFolder("jre");
        fo = fo.createFolder("lib");
        FileObject rt1 = fo.createData("rt.jar");
        FileObject src1 = FileUtil.getArchiveRoot(createSrcZip (p1));

        FileObject p2 = wdo.createFolder("platform2");
        fo = p2.createFolder("jre");
        fo = fo.createFolder("lib");
        FileObject rt2 = fo.createData("rt.jar");

        PlatformSourceForBinaryQuery q = new PlatformSourceForBinaryQuery ();

        SourceForBinaryQuery.Result result = q.findSourceRoots(FileUtil.getArchiveRoot(rt1.getURL()));
        assertEquals(1, result.getRoots().length);
        assertEquals(src1, result.getRoots()[0]);

        result = q.findSourceRoots(FileUtil.getArchiveRoot(rt2.getURL()));
        assertNull(result);
    }

    public void testUnregisteredJDK11Platform() throws Exception {
        File wd = getWorkDir();
        FileObject wdo = FileUtil.toFileObject(wd);
        assertNotNull(wdo);
        FileObject p1 = wdo.createFolder("platform1");
        FileObject fo = p1.createFolder("lib");
        FileObject src1 = FileUtil.getArchiveRoot(createSrcZip (fo, "java.base/java/util.Map.java:class Map {}"));
        URL url = new URL("nbjrt:" + p1.toURL() + "!/modules/java.base/");

        PlatformSourceForBinaryQuery q = new PlatformSourceForBinaryQuery ();

        SourceForBinaryQuery.Result result = q.findSourceRoots(url);
        assertNotNull("Result is found", result);
        assertEquals(1, result.getRoots().length);
        assertEquals(src1.getFileObject("java.base"), result.getRoots()[0]);
    }

    public void testTwoPlatformsoverSameSDKSourcesChange() throws Exception {
        final File binDir = new File(getWorkDir(),"boot");  //NOI18N
        binDir.mkdir();
        final File jdocFile1 = new File(getWorkDir(),"src1");   //NOI18N
        jdocFile1.mkdir();
        final File jdocFile2 = new File(getWorkDir(),"src2");  //NOI18N
        jdocFile2.mkdir();
        final TestJavaPlatformProvider provider = TestJavaPlatformProvider.getDefault();
        provider.reset();
        final URL binRoot = Utilities.toURI(binDir).toURL();
        final ClassPath bootCp = ClassPathSupport.createClassPath(binRoot);
        final ClassPath src1 = ClassPathSupport.createClassPath(Utilities.toURI(jdocFile1).toURL());
        final ClassPath src2 = ClassPathSupport.createClassPath(Utilities.toURI(jdocFile2).toURL());
        final TestJavaPlatform platform1 = new TestJavaPlatform("platform1", bootCp);   //NOI18N
        final TestJavaPlatform platform2 = new TestJavaPlatform("platform2", bootCp);   //NOI18N
        platform2.setSources(src2);
        provider.addPlatform(platform1);
        provider.addPlatform(platform2);

        final SourceForBinaryQuery.Result result1 = SourceForBinaryQuery.findSourceRoots(binRoot);
        assertEquals(Arrays.asList(src2.getRoots()), Arrays.asList(result1.getRoots()));

        platform1.setSources(src1);
        assertEquals(Arrays.asList(src1.getRoots()), Arrays.asList(result1.getRoots()));

        final SourceForBinaryQuery.Result result2 = SourceForBinaryQuery.findSourceRoots(binRoot);
        assertEquals(Arrays.asList(src1.getRoots()), Arrays.asList(result2.getRoots()));

        platform1.setSources(ClassPath.EMPTY);
        assertEquals(Arrays.asList(src2.getRoots()), Arrays.asList(result1.getRoots()));
        assertEquals(Arrays.asList(src2.getRoots()), Arrays.asList(result2.getRoots()));
    }

    public void testTwoPlatformsoverSameSDKPlatformChange() throws Exception {
        final File binDir = new File(getWorkDir(),"boot");  //NOI18N
        binDir.mkdir();
        final File jdocFile1 = new File(getWorkDir(),"src1");   //NOI18N
        jdocFile1.mkdir();
        final File jdocFile2 = new File(getWorkDir(),"src2");  //NOI18N
        jdocFile2.mkdir();
        final TestJavaPlatformProvider provider = TestJavaPlatformProvider.getDefault();
        provider.reset();
        final URL binRoot = Utilities.toURI(binDir).toURL();
        final ClassPath bootCp = ClassPathSupport.createClassPath(binRoot);
        final ClassPath src1 = ClassPathSupport.createClassPath(Utilities.toURI(jdocFile1).toURL());
        final ClassPath src2 = ClassPathSupport.createClassPath(Utilities.toURI(jdocFile2).toURL());
        final TestJavaPlatform platform1 = new TestJavaPlatform("platform1", bootCp);   //NOI18N
        final TestJavaPlatform platform2 = new TestJavaPlatform("platform2", bootCp);   //NOI18N
        platform1.setSources(src1);
        platform2.setSources(src2);
        provider.addPlatform(platform1);
        provider.addPlatform(platform2);

        final SourceForBinaryQuery.Result result1 = SourceForBinaryQuery.findSourceRoots(binRoot);
        assertEquals(Arrays.asList(src1.getRoots()), Arrays.asList(result1.getRoots()));

        provider.removePlatform(platform1);
        assertEquals(Arrays.asList(src2.getRoots()), Arrays.asList(result1.getRoots()));

        final SourceForBinaryQuery.Result result2 = SourceForBinaryQuery.findSourceRoots(binRoot);
        assertEquals(Arrays.asList(src2.getRoots()), Arrays.asList(result2.getRoots()));

        provider.insertPlatform(platform2, platform1);
        assertEquals(Arrays.asList(src1.getRoots()), Arrays.asList(result1.getRoots()));
        assertEquals(Arrays.asList(src1.getRoots()), Arrays.asList(result2.getRoots()));
    }


    private static FileObject createSrcZip (FileObject pf, String... entries) throws Exception {
        if (entries == null || entries.length == 0) {
          entries = new String[] { "Test.java:class Test {}" };
        }
        return TestFileUtils.writeZipFile(pf, "src.zip", entries);
    }

    public static final class NBJRTStreamHandlerFactory implements URLStreamHandlerFactory {

        @Override
        public URLStreamHandler createURLStreamHandler(String protocol) {
            if ("nbjrt".equals(protocol)) { //NOI18N
                return new NBJRTURLStreamHandler();
            }
            return null;
        }

        private static class NBJRTURLStreamHandler extends URLStreamHandler {

            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                //Not needed
                return null;
            }
        }
    }

}
