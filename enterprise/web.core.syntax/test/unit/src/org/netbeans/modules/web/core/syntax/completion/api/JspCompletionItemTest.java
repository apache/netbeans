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
package org.netbeans.modules.web.core.syntax.completion.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.netbeans.test.web.core.syntax.TestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JspCompletionItemTest extends TestBase {

    public JspCompletionItemTest(String name) {
        super(name);
    }

    private static void createTestZip(File zipFile, String filePath, String content) throws IOException {
        Path zipPath = zipFile.toPath();
        Files.createDirectories(zipPath.getParent());
        try(OutputStream os = Files.newOutputStream(zipPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                ZipOutputStream zos = new ZipOutputStream(os, UTF_8)) {
            zos.putNextEntry(new ZipEntry(filePath));
            zos.write(content.getBytes(UTF_8));
            zos.closeEntry();
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createTestZip(
                new File(getDataDir(), "testJarLibrary/file.zip"),
                "file.txt",
                "testFile"
        );
        createTestZip(
                new File(getDataDir(), "testJarLibrary/file space.zip"),
                "file.txt",
                "test file with spaces"
        );
        createTestZip(
                new File(getDataDir(), "testJarLibrary/file_space.zip"),
                "file space.txt",
                "test file with spaces"
        );
    }

    public void testGetStreamForUrl() throws Exception {
        URL url = new URL("jar:file:" + getTestFile("testJarLibrary/file.zip").getPath() + "!/file.txt");
        InputStream inputStreamForUrl = JspCompletionItem.getInputStreamForUrl(url);
        assertNotNull(inputStreamForUrl);
        assertEquals("testFile", readContentFromIS(inputStreamForUrl));

        url = new URL("jar:file:" + getTestFile("testJarLibrary/file_space.zip").getPath() + "!/file space.txt");
        inputStreamForUrl = JspCompletionItem.getInputStreamForUrl(url);
        assertNotNull(inputStreamForUrl);
        assertEquals("test file with spaces", readContentFromIS(inputStreamForUrl));

        url = new URL("jar:file:" + getTestFile("testJarLibrary/file space.zip").getPath() + "!/file.txt");
        inputStreamForUrl = JspCompletionItem.getInputStreamForUrl(url);
        assertNotNull(inputStreamForUrl);
        assertEquals("test file with spaces", readContentFromIS(inputStreamForUrl));
    }

    public void testGetHelp() throws Exception {
        Method constructHelpMethod = JspCompletionItem.class.getDeclaredMethod("constructHelp", URL.class);
        constructHelpMethod.setAccessible(true);

        URL url = new URL("jar:file:" + getTestFile("testJarLibrary/file.zip").getPath() + "!/file.txt");
        String help = (String) constructHelpMethod.invoke(null, url);
        assertEquals("testFile", help);

        url = new URL("jar:file:" + getTestFile("testJarLibrary/file_space.zip").getPath() + "!/file space.txt");
        help = (String) constructHelpMethod.invoke(null, url);
        assertEquals("test file with spaces", help.trim());

        url = new URL("jar:file:" + getTestFile("testJarLibrary/file space.zip").getPath() + "!/file.txt");
        help = (String) constructHelpMethod.invoke(null, url);
        assertEquals("test file with spaces", help.trim());
    }

    private String readContentFromIS(InputStream is) throws IOException {
        StringBuilder content = new StringBuilder();

        InputStreamReader isr = new InputStreamReader(is);
        try {
            int data = isr.read();
            while (data != -1) {
                content.append((char) data);
                data = isr.read();
            }
        } finally {
            isr.close();
        }
        return content.toString().trim();
    }

}
