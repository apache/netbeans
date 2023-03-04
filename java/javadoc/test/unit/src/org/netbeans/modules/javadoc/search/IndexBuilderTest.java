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

package org.netbeans.modules.javadoc.search;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.javadoc.search.IndexBuilder.SimpleTitleParser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;

/**
 *
 * @author Jan Pokorsky
 */
public class IndexBuilderTest extends NbTestCase {

    private LocalFileSystem fs;
    private static final String JDK14_INDEX_PATH = "docs_jdk14/api/index-files";
    private static final String JDK14_JA_INDEX_PATH = "docs_jdk14_ja/api/index-files";
    private static final String JDK15_INDEX_PATH = "docs_jdk15/api/index-files";
    private static final String JDK15_JA_INDEX_PATH = "docs_jdk15_ja/api/index-files";
    private static final String JDK7_INDEX_PATH = "docs_jdk7/api/index-files";
    private static final String JDK8_INDEX_PATH = "docs_jdk8/api/index-files";

    /** Creates a new instance of IndexBuilderTest */
    public IndexBuilderTest(String testName) {
        super(testName);
    }
    
    protected @Override void setUp() throws Exception {
        File dataFile = getDataDir();
        assertNotNull("missing data file", dataFile);
        fs = new LocalFileSystem();
        fs.setRootDirectory(dataFile);
    }

    public void testTitleInJDK14() throws Exception {
        FileObject html = fs.findResource(JDK14_INDEX_PATH + "/index-4.html");

        try (InputStream is = new BufferedInputStream(html.getInputStream(), 1024)) {
            SimpleTitleParser tp = new SimpleTitleParser(is);
            tp.parse();
            String titlestr = tp.getTitle();
            assertEquals("wrong title", "D-Index (Java 2 Platform SE v1.4.2)", titlestr);
        }
    }

    public void testTitleInJDK15() throws Exception {
        FileObject html = fs.findResource(JDK15_INDEX_PATH + "/index-4.html");

        try (InputStream is = new BufferedInputStream(html.getInputStream(), 1024)) {
            SimpleTitleParser tp = new SimpleTitleParser(is);
            tp.parse();
            String titlestr = tp.getTitle();
            assertEquals("wrong title", "D-Index (Java 2 Platform SE 5.0)", titlestr);
        }
    }
    
    public void testTitleInJDK7() throws Exception {
        FileObject html = fs.findResource(JDK7_INDEX_PATH + "/index-4.html");

        try (InputStream is = new BufferedInputStream(html.getInputStream(), 1024)) {
            SimpleTitleParser tp = new SimpleTitleParser(is);
            tp.parse();
            String titlestr = tp.getTitle();
            assertEquals("wrong title", "D-Index (Java Platform SE 7 )", titlestr);
        }
    }
    
    public void testTitleInJDK8() throws Exception {
        FileObject html = fs.findResource(JDK8_INDEX_PATH + "/index-4.html");

        try (InputStream is = new BufferedInputStream(html.getInputStream(), 1024)) {
            SimpleTitleParser tp = new SimpleTitleParser(is);
            tp.parse();
            String titlestr = tp.getTitle();
            assertEquals("wrong title", "D-Index (Java Platform SE 8 )", titlestr);
        }
    }

    public void testEmptyTitle() throws Exception {
        String content = "<HTML><HEAD><TITLE></TITLE></HEAD></HTML>";
        
        try (InputStream is = new ByteArrayInputStream(content.getBytes())) {
            SimpleTitleParser tp = new SimpleTitleParser(is);
            tp.parse();
            String titlestr = tp.getTitle();
            assertEquals("wrong title", "", titlestr);
        }
    }

    public void testMissingTitle() throws Exception {
        String content = "<HTML><HEAD></HEAD></HTML>";
        
        try (InputStream is = new ByteArrayInputStream(content.getBytes())) {
            SimpleTitleParser tp = new SimpleTitleParser(is);
            tp.parse();
            String titlestr = tp.getTitle();
            assertNull("wrong title", titlestr);
        }
    }
    
    public void testEscapedHtmlTitle() throws Exception {
        String content = "<HTML><HEAD><TITLE>Overview (Java SE 11 &amp; JDK 11 )</TITLE></HEAD></HTML>";
        
        try (InputStream is = new ByteArrayInputStream(content.getBytes())) {
            SimpleTitleParser tp = new SimpleTitleParser(is);
            tp.parse();
            String titlestr = tp.getTitle();
            assertEquals("wrong title", "Java SE 11 & JDK 11", titlestr);
        }
    }

    public void testTitleInJDK14_ja() throws Exception {
        FileObject html = fs.findResource(JDK14_JA_INDEX_PATH + "/index-4.html");
        FileObject html2 = fs.findResource(JDK14_JA_INDEX_PATH + "/index-4.title");
        japanaseIndexes(html, html2, "iso-2022-jp");
    }

    public void testTitleInJDK15_ja() throws Exception {
        FileObject html = fs.findResource(JDK15_JA_INDEX_PATH + "/index-4.html");
        FileObject html2 = fs.findResource(JDK15_JA_INDEX_PATH + "/index-4.title");
        japanaseIndexes(html, html2, "euc-jp");
    }

    private void japanaseIndexes(FileObject html, FileObject title, String charset) throws Exception {
        assertNotNull(html);
        assertNotNull(title);

        Reader r = new InputStreamReader(title.getInputStream(), charset);

        int ic;
        StringBuilder sb = new StringBuilder();
        try {
            while ((ic = r.read()) != -1) {
                sb.append((char) ic);
            }
        } finally {
            r.close();
        }

        try (InputStream is = new BufferedInputStream(html.getInputStream(), 1024)) {
            SimpleTitleParser tp = new SimpleTitleParser(is);
            tp.parse();
            String titlestr = tp.getTitle();
            assertEquals("wrong title", sb.toString(), titlestr);
        }
    }
}
