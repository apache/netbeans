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
package org.netbeans.api.java.source.ui;

import com.sun.source.util.TreePath;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.JavaDataLoader;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

public class ElementJavadocTest extends NbTestCase {

    private static final String CARET_MARK = "<caret>";

    public ElementJavadocTest(String testName) {
        super(testName);
    }

    private void prepareTest(String fileName, String code) throws Exception {
        int pos = code.indexOf(CARET_MARK);

        if (pos == (-1)) {
            throw new AssertionError("Does not have caret position!");
        }

        code = code.substring(0, pos) + code.substring(pos + CARET_MARK.length());

        List<Object> extras = new ArrayList<>();
        extras.add(JavaDataLoader.class);
        SourceUtilsTestUtil.prepareTest(new String[] {
                    "org/netbeans/modules/java/platform/resources/layer.xml",
                    "org/netbeans/modules/java/j2seplatform/resources/layer.xml"
                },
                extras.toArray(new Object[0])
        );

        clearWorkDir();

        FileUtil.refreshAll();

        FileObject workFO = FileUtil.toFileObject(getWorkDir());

        assertNotNull(workFO);

        sourceRoot = workFO.createFolder("src");

        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");

        FileObject data = FileUtil.createData(sourceRoot, fileName);
        File dataFile = FileUtil.toFile(data);

        assertNotNull(dataFile);

        TestUtilities.copyStringToFile(dataFile, code);

        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache, new FileObject[0]);

        DataObject od = DataObject.find(data);
        EditorCookie ec = od.getCookie(EditorCookie.class);

        assertNotNull(ec);

        doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());

        JavaSource js = JavaSource.forFileObject(data);

        assertNotNull(js);

        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);

        assertNotNull(info);

        selectedPath = info.getTreeUtilities().pathFor(pos);
        selectedElement = info.getTrees().getElement(selectedPath);

        assertNotNull(selectedElement);
    }

    private FileObject sourceRoot;
    private CompilationInfo info;
    private Document doc;
    private TreePath selectedPath;
    private Element selectedElement;

    protected void performTest(String fileName, String code, int pos, String format, String golden) throws Exception {
        prepareTest(fileName, code);

        TreePath path = info.getTreeUtilities().pathFor(pos);

        assertEquals(golden, ElementHeaders.getHeader(path, info, format));
    }

    public void testMarkdownTables() throws Exception {
        prepareTest("test/Test.java",
                    "///| header1 | header2 |\n" +
                    "///|---------|---------|\n" +
                    "///| cr11    | cr12    |\n" +
                    "///| cr21    | cr22    |\n" +
                    "public class T<caret>est {\n" +
                    "}\n");

        String actualJavadoc = ElementJavadoc.create(info, selectedElement).getText();
        String expectedJavadoc = "<pre>public class <b>Test</b><br>extends <a href='*0'>Object</a></pre><p><table>\n" +
                                 "<thead>\n" +
                                 "<tr><th>header1</th><th>header2</th></tr>\n" +
                                 "</thead>\n" +
                                 "<tbody>\n" +
                                 "<tr><td>cr11</td><td>cr12</td></tr>\n" +
                                 "<tr><td>cr21</td><td>cr22</td></tr>\n" +
                                 "</tbody>\n" +
                                 "</table>\n" +
                                 "<p>";

        assertEquals(expectedJavadoc, actualJavadoc);
    }

    public void testLinkNoRef() throws Exception {
        prepareTest("test/Test.java",
                    "///Hello!\n" +
                    "///{@link }\n" +
                    "public class T<caret>est {\n" +
                    "}\n");

        String actualJavadoc = ElementJavadoc.create(info, selectedElement).getText();
        String expectedJavadoc = "<pre>public class <b>Test</b><br>extends <a href='*0'>Object</a></pre><p>Hello!\n" +
                                 "\n" +
                                 "<p>";

        assertEquals(expectedJavadoc, actualJavadoc);
    }

}
