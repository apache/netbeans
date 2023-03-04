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
package org.netbeans.modules.javascript2.editor.embedding;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.Document;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.html.lexer.HtmlLexerPlugin;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.netbeans.modules.javascript2.editor.embedding.JsEmbeddingProvider.EmbeddingPosition;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.WebUtils;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsEmbeddingProviderTest extends JsTestBase {

    public JsEmbeddingProviderTest(String testName) {
        super(testName);
    }

    public void testExtractJsEmbeddings01() throws Exception {
        String text = "                          ";
        List<EmbeddingPosition> embeddings = JsEmbeddingProvider.extractJsEmbeddings(text, 0);
        assertEquals(embeddings.size(), 1);
        assertEquals(0, embeddings.get(0).getOffset());
        assertEquals(text.length(), embeddings.get(0).getLength());

        embeddings = JsEmbeddingProvider.extractJsEmbeddings(text, 5);
        assertEquals(embeddings.size(), 1);
        assertEquals(5, embeddings.get(0).getOffset());
        assertEquals(text.length(), embeddings.get(0).getLength());
    }

    public void testExtractJsEmbeddings02() throws Exception {
        String text = "    <!-- anything \n -->             ";
        List<EmbeddingPosition> embeddings = JsEmbeddingProvider.extractJsEmbeddings(text, 0);
        assertEquals(embeddings.size(), 2);
        assertEquals(0, embeddings.get(0).getOffset());
        assertEquals(4, embeddings.get(0).getLength());
        assertEquals(19, embeddings.get(1).getOffset());
        assertEquals(17, embeddings.get(1).getLength());

        embeddings = JsEmbeddingProvider.extractJsEmbeddings(text, 5);
        assertEquals(embeddings.size(), 2);
        assertEquals(5, embeddings.get(0).getOffset());
        assertEquals(4, embeddings.get(0).getLength());
        assertEquals(24, embeddings.get(1).getOffset());
        assertEquals(17, embeddings.get(1).getLength());
    }

    public void testExtractJsEmbeddings03() throws Exception {
        String text = "       <!-- Cau Pisliku -->             ";
        List<EmbeddingPosition> embeddings = JsEmbeddingProvider.extractJsEmbeddings(text, 0);
        assertEquals(embeddings.size(), 2);
        assertEquals(0, embeddings.get(0).getOffset());
        assertEquals(text.indexOf("<!-- Cau Pisliku -->"), embeddings.get(0).getLength());

        embeddings = JsEmbeddingProvider.extractJsEmbeddings(text, 5);
        assertEquals(embeddings.size(), 2);
        assertEquals(5, embeddings.get(0).getOffset());
        assertEquals(text.indexOf("<!-- Cau Pisliku -->"), embeddings.get(0).getLength());
    }

    public void testExtractJsEmbeddings04() throws Exception {
        String text = "<!-- comment --> javascript code \n<!-- comment -->";
        List<EmbeddingPosition> embeddings = JsEmbeddingProvider.extractJsEmbeddings(text, 0);
        assertEquals(1, embeddings.size());
        assertEquals(text.indexOf(" javascript code "), embeddings.get(0).getOffset());
        assertEquals(18, embeddings.get(0).getLength());
    }

    public void testExtractJsEmbeddings05() throws Exception {
        String text = "\n"+
"\n"+
"                <!-- aaaaaaaa0 -->asdf\n"+
"                <!-- sdfsdsdsds\n"+
"\n"+
"    asfdasdfasd fasdf asdf\n"+
"\n"+
"    -->\n"+
"\n"+
"            dfasdasd      <!-- aaaaaaaa1 -->\n"+
"\n"+
"              asdfa  <!-- aaaaaaaa2 -->afs dfasdfasd\n"+
"                <!-- aaaaaaaa3 -->\n"+
"\n"+
"                asdfasdfasd\n"+
"\n"+
"                <!-- aaaaaaaa4 -->\n"+
"\n"+
"                afsdasdf\n"+
"\n"+
"        ";
        List<EmbeddingPosition> embeddings = JsEmbeddingProvider.extractJsEmbeddings(text, 193);
        assertEquals(6, embeddings.size());
        assertEquals(193, embeddings.get(0).getOffset());
        assertEquals(text.indexOf("<!-- aaaaaaaa0 -->") + 18 + 193, embeddings.get(1).getOffset());
        assertEquals(text.indexOf("<!-- aaaaaaaa1 -->") + 18 + 193, embeddings.get(2).getOffset());
        assertEquals(text.indexOf("<!-- aaaaaaaa2 -->") + 18 + 193, embeddings.get(3).getOffset());
        assertEquals(text.indexOf("<!-- aaaaaaaa3 -->") + 18 + 193, embeddings.get(4).getOffset());
        assertEquals(text.indexOf("<!-- aaaaaaaa4 -->") + 18 + 193, embeddings.get(5).getOffset());

        assertEquals(18, embeddings.get(0).getLength());
        assertEquals(101, embeddings.get(1).getLength());
        assertEquals(23, embeddings.get(2).getLength());
        assertEquals(30, embeddings.get(3).getLength());
        assertEquals(47, embeddings.get(4).getLength());
        assertEquals(36, embeddings.get(5).getLength());
    }

    public void testExtractJsEmbeddings06() throws Exception {
        String text = "\n  <!--\n  myImage = new Image();\n  myImage = '../templates/images/indicator.gif';\n  //-->";
        List<EmbeddingPosition> embeddings = JsEmbeddingProvider.extractJsEmbeddings(text, 0);
        assertEquals(2, embeddings.size());
        assertEquals(0, embeddings.get(0).getOffset());
        assertEquals(text.indexOf("  myImage = new Image();"), embeddings.get(1).getOffset());
        assertEquals(3, embeddings.get(0).getLength());
        assertEquals(81, embeddings.get(1).getLength());
    }

    public void testIssue223883() throws Exception {
        String text =   "<!--//--><![CDATA[//><!--\n" +
                        "    var abcd ='11111';\n" +
                        "//--><!]]>";
        List<EmbeddingPosition> embeddings = JsEmbeddingProvider.extractJsEmbeddings(text, 0);
        assertEquals(1, embeddings.size());
        assertEquals(text.indexOf("    var abcd ='11111';\n"), embeddings.get(0).getOffset());
        assertEquals(33, embeddings.get(0).getLength());
    }

    public void testIssue217081() throws Exception {
        String text =   "<!--\n" +
                        "    ";
        List<EmbeddingPosition> embeddings = JsEmbeddingProvider.extractJsEmbeddings(text, 0);
        assertEquals(1, embeddings.size());
        assertEquals(text.indexOf("    "), embeddings.get(0).getOffset());
        assertEquals(4, embeddings.get(0).getLength());
    }
    
    /*
     * Tests conversion of the generic templating mark "@@@"
     * to its JS counterpart.
     */
    public void testConvertGenericMarksToJSMark() {
        assertEmbedding("<script>hello @@@ word</script>",
                "hello __UNKNOWN__ word\n");

        //this needs some more "fine tuning" :-)
        //so far works only if the pattern is surrounded by sg.

//        assertEmbedding("<script>x@@@</script>",
//                "x__UNKNOWN__\n");
//        
//        assertEmbedding("<script>@@@x</script>",
//                "__UNKNOWN__x\n");
//        
//        assertEmbedding("<script>@@@</script>",
//                "__UNKNOWN__\n");

//        assertEmbedding("<div onclick=\"@@@\">",
//                "");

        assertEmbedding("<div onclick=\"a@@@b\">",
                "(function(){\n"
                + "a__UNKNOWN__b;\n"
                + "});\n"
                + "");

    }
    
    public void testOnClick() throws ParseException {
        assertEmbedding("<div onclick='alert()'/>",
                "(function(){\n"
                + "alert();\n"
                + "});\n"
                + "");
    }

    public void testCustomJSEmbeddingOnAttributeValue() {
        assertEmbedding("<div controller='MyController'/>",
                "(function(){\n"
                + "MyController;\n"
                + "});\n"
                + "");
    }
    
    private void assertEmbedding(String code, String expectedJsVirtualSource) {
        assertEmbedding(getDocument(code, "text/html"), expectedJsVirtualSource);
    }

    public static void assertEmbedding(Document doc, String expectedJsVirtualSource) {
        try {
            Source source = Source.create(doc);
            final AtomicReference<String> jsCodeRef = new AtomicReference<>();
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ResultIterator jsRi = WebUtils.getResultIterator(resultIterator, "text/javascript");
                    if (jsRi != null) {
                        jsCodeRef.set(jsRi.getSnapshot().getText().toString());
                    } else {
                        //no js embedded code
                    }
                }
            });
            String jsCode = jsCodeRef.get();
            if (expectedJsVirtualSource != null) {
                assertNotNull(jsCode);
                assertEquals(expectedJsVirtualSource, jsCode);
            } else {
                //expected no embedded js code
                assertNull(jsCode);
            }
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @MimeRegistration(mimeType = "text/html", service = HtmlLexerPlugin.class)
    public static class TestHtmlLexerPlugin extends HtmlLexerPlugin {

        @Override
        public String getOpenDelimiter() {
            return "{{";
        }

        @Override
        public String getCloseDelimiter() {
            return "}}";
        }

        @Override
        public String getContentMimeType() {
            return "text/javascript";
        }

        @Override
        public String createAttributeEmbedding(String elementName, String attributeName) {
            if ("controller".equals(attributeName)) {
                return "text/javascript";
            }
            return null;
        }
    }

}
