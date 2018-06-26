/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.modules.html.editor.completion.HtmlCompletionTestSupport;
import org.netbeans.modules.html.editor.completion.HtmlCompletionTestSupport.Match;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author marekfukala
 */
public class JsfHtmlExtensionTest extends TestBaseForTestProject {

    public JsfHtmlExtensionTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testAttributeValueCompletion01() throws Exception {
        // attribute completion for legacy namespaces
        testCC("testWebProject/web/cctest.xhtml", "<h:selectManyCheckbox layout=\"|\"/>", new String[]{"pageDirection", "lineDirection"}, Match.EXACT);
        testCC("testWebProject/web/cctest.xhtml", "<f:ajax immediate=\"|\"/>", new String[]{"true", "false"}, Match.EXACT);
    }

    public void testAttributeValueCompletion02() throws Exception {
        // attribute completion for new namespaces
        testCC("testWebProject/web/cctest_newns.xhtml", "<h:selectManyCheckbox layout=\"|\"/>", new String[]{"pageDirection", "lineDirection"}, Match.EXACT);
        testCC("testWebProject/web/cctest_newns.xhtml", "<f:ajax immediate=\"|\"/>", new String[]{"true", "false"}, Match.EXACT);
    }

    public void testSrcAttributeCompletion01() throws Exception {
        // src attribute completion in ui:include element
        testCC("testWebProject/web/cctest_newns.xhtml", "<ui:include src=\"|\"/>", new String[]{"index.xhtml", "ccusage.xhtml", "test.xhtml", "WEB-INF/"}, Match.CONTAINS);
    }

    public void testJavaTypesCompletion01() throws Exception {
        // type attribute completion in cc:attribute element
        testCC("testWebProject/web/resources/ezcomp/test2.xhtml", "<cc:attribute name=\"testAttr\" type=\"|\"/>", new String[]{"beans", "org"}, Match.CONTAINS);
    }

    public void testJavaTypesCompletion02() throws Exception {
        // type attribute completion in cc:attribute element
        testCC("testWebProject/web/resources/ezcomp/test2.xhtml", "<cc:attribute name=\"testAttr\" type=\"beans.|\"/>", new String[]{"Product", "Company", "MBean"}, Match.CONTAINS);
    }

    public void testJavaTypesCompletion03() throws Exception {
        // type attribute completion in cc:attribute element
        testCC("testWebProject/web/resources/ezcomp/test2.xhtml", "<cc:attribute name=\"testAttr\" type=\"org.mysite.|\"/>", new String[]{"classtaglib"}, Match.EXACT);
    }

    public void testCompletionInsertName01() throws Exception {
        // type attribute completion in cc:attribute element
        testCC("testWebProject/web/cctest.xhtml", "<ui:composition template=\"./template.xhtml\">\n" +
"            <ui:define name=\"|\">\n" +
"            </ui:define>\n" +
"        </ui:composition>", new String[]{"body", "title"}, Match.CONTAINS);
    }

    public void testCompletionInsertName02() throws Exception {
        // type attribute completion in cc:attribute element
        testCC("testWebProject/web/cctest.xhtml", "<ui:composition template=\"./template_custom.xhtml\">\n" +
"            <ui:define name=\"|\">\n" +
"            </ui:define>\n" +
"        </ui:composition>", new String[]{"body", "title"}, Match.CONTAINS);
    }

    protected void testCC(String filePath, String testText, String[] expected, Match matchType) throws Exception {
        testCC(filePath, testText, expected, matchType, -1);
    }
    
    /**
     * The testText will be inserted into the body of testWebProject/web/cctest.xhtml and then the completion will be called.
     * In the case you need more imports, modify the template or make the support generic (no template based)
     */
    protected void testCC(String filePath, String testText, String[] expected, Match matchType, int expectedAnchor) throws BadLocationException, ParseException, IOException {
        //load the testing template
        FileObject file = getTestFile(filePath);
        Document doc = getDocument(file);

        StringBuilder content = new StringBuilder(doc.getText(0, doc.getLength()));
        final int documentPipeIndex = content.indexOf("|");
        assertFalse(documentPipeIndex < 0);

        //remove the pipe
        content.deleteCharAt(documentPipeIndex);

        //insert test text, extract the pipe first
        content.insert(documentPipeIndex, testText);

        copyStringToFile(content.toString(), FileUtil.toFile(FileUtil.toFileObject(getWorkDir()).getFileObject(filePath)));
        FileObject testFile = FileUtil.toFileObject(getWorkDir()).getFileObject(filePath);
        Document testdoc = getDocument(testFile, JsfUtils.XHTML_MIMETYPE, Language.find(JsfUtils.XHTML_MIMETYPE));
        IndexingManager.getDefault().refreshIndexAndWait(testFile.getParent().toURL(), Arrays.asList(testFile.toURL()));

        HtmlCompletionTestSupport.assertItems(
                testdoc,
                expected,
                matchType,
                expectedAnchor);
    }

    public static void copyStringToFile(String string, File path) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(string.getBytes("UTF-8"))) {
            copyStreamToFile(inputStream, path);
        }
    }

    private static void copyStreamToFile(InputStream inputStream, File path) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(path, false)) {
            FileUtil.copy(inputStream, outputStream);
        }
    }
    
}
