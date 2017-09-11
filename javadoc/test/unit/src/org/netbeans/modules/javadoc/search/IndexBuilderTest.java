/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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

        InputStream is = new BufferedInputStream(html.getInputStream(), 1024);
        SimpleTitleParser tp = new SimpleTitleParser(is);
        try {
            tp.parse();
            String titlestr = tp.getTitle();
            assertEquals("wrong title", "D-Index (Java 2 Platform SE v1.4.2)", titlestr);
        } finally {
            is.close();
        }
    }

    public void testTitleInJDK15() throws Exception {
        FileObject html = fs.findResource(JDK15_INDEX_PATH + "/index-4.html");

        InputStream is = new BufferedInputStream(html.getInputStream(), 1024);
        SimpleTitleParser tp = new SimpleTitleParser(is);
        try {
            tp.parse();
            String titlestr = tp.getTitle();
            assertEquals("wrong title", "D-Index (Java 2 Platform SE 5.0)", titlestr);
        } finally {
            is.close();
        }
    }
    
    public void testTitleInJDK7() throws Exception {
        FileObject html = fs.findResource(JDK7_INDEX_PATH + "/index-4.html");

        InputStream is = new BufferedInputStream(html.getInputStream(), 1024);
        SimpleTitleParser tp = new SimpleTitleParser(is);
        try {
            tp.parse();
            String titlestr = tp.getTitle();
            assertEquals("wrong title", "D-Index (Java Platform SE 7 )", titlestr);
        } finally {
            is.close();
        }
    }
    
    public void testTitleInJDK8() throws Exception {
        FileObject html = fs.findResource(JDK8_INDEX_PATH + "/index-4.html");

        InputStream is = new BufferedInputStream(html.getInputStream(), 1024);
        SimpleTitleParser tp = new SimpleTitleParser(is);
        try {
            tp.parse();
            String titlestr = tp.getTitle();
            assertEquals("wrong title", "D-Index (Java Platform SE 8 )", titlestr);
        } finally {
            is.close();
        }
    }

    public void testEmptyTitle() throws Exception {
        String content = "<HTML><HEAD><TITLE></TITLE></HEAD></HTML>";
        InputStream is = new ByteArrayInputStream(content.getBytes());
        SimpleTitleParser tp = new SimpleTitleParser(is);
        try {
            tp.parse();
            String titlestr = tp.getTitle();
            assertEquals("wrong title", "", titlestr);
        } finally {
            is.close();
        }
    }

    public void testMissingTitle() throws Exception {
        String content = "<HTML><HEAD></HEAD></HTML>";
        InputStream is = new ByteArrayInputStream(content.getBytes());
        SimpleTitleParser tp = new SimpleTitleParser(is);
        try {
            tp.parse();
            String titlestr = tp.getTitle();
            assertNull("wrong title", titlestr);
        } finally {
            is.close();
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

        InputStream is = new BufferedInputStream(html.getInputStream(), 1024);
        SimpleTitleParser tp = new SimpleTitleParser(is);
        try {
            tp.parse();
            String titlestr = tp.getTitle();
            assertEquals("wrong title", sb.toString(), titlestr);
        } finally {
            is.close();
        }
    }
}
