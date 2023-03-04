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

package org.netbeans.modules.java.source.parsing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Hrebejk, Tomas Zezula
 */
public class CachingFileManagerTest extends NbTestCase {

    public CachingFileManagerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }


    public void testGetFileForInputWithFolderArchive() throws  Exception {
        final File wd = getWorkDir();
        final org.openide.filesystems.FileObject root = FileUtil.createFolder(new File (wd,"src"));
        final org.openide.filesystems.FileObject data = FileUtil.createData(root, "org/me/resources/test.txt");
        final URI expectedURI = data.getURL().toURI();
        doTestGetFileForInput(ClassPathSupport.createClassPath(root),Arrays.asList(
            Pair.<Pair<String,String>,URI>of(Pair.<String,String>of("","org/me/resources/test.txt"), expectedURI),
            Pair.<Pair<String,String>,URI>of(Pair.<String,String>of("org.me","resources/test.txt"), expectedURI),
            Pair.<Pair<String,String>,URI>of(Pair.<String,String>of("org.me","resources/doesnotexist.txt"), null)
        ));

    }

    public void testGetFileForInputWithCachingArchive() throws  Exception {
        final File wd = getWorkDir();
        final File archiveFile = new File (wd, "src.zip");
        final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(archiveFile));
        try {
            out.putNextEntry(new ZipEntry("org/me/resources/test.txt"));
            out.write("test".getBytes());
        } finally {
            out.close();
        }
        final URL archiveRoot = FileUtil.getArchiveRoot(Utilities.toURI(archiveFile).toURL());
        final URI expectedURI = new URL (archiveRoot.toExternalForm()+"org/me/resources/test.txt").toURI();
        doTestGetFileForInput(ClassPathSupport.createClassPath(archiveRoot),
        Arrays.asList(
            Pair.<Pair<String,String>,URI>of(Pair.<String,String>of("","org/me/resources/test.txt"), expectedURI),
            Pair.<Pair<String,String>,URI>of(Pair.<String,String>of("org.me","resources/test.txt"), expectedURI),
            Pair.<Pair<String,String>,URI>of(Pair.<String,String>of("org.me","resources/doesnotexist.txt"), null)
        ));

    }

    private void doTestGetFileForInput(
            final ClassPath cp,
            final List<? extends Pair<Pair<String,String>,URI>> testCases) throws IOException, URISyntaxException {
        final CachingArchiveProvider provider = CachingArchiveProvider.getDefault();
        final CachingFileManager manager = new CachingFileManager(provider, cp, null, false, true);
        for (Pair<Pair<String,String>,URI> testCase : testCases) {
            final Pair<String,String> name = testCase.first();
            final URI expectedURI = testCase.second();
            FileObject fo = manager.getFileForInput(StandardLocation.CLASS_PATH, name.first(), name.second());
            if (expectedURI == null) {
                assertNull(
                    String.format("Lookup: %s/%s expected: null",
                    name.first(),
                    name.second()),
                    fo);
            } else {
                assertEquals(
                    String.format("Lookup: %s/%s expected: %s",
                    name.first(),
                    name.second(),
                    expectedURI),
                    expectedURI,
                    fo.toUri());
            }
        }        
    }
    
}
