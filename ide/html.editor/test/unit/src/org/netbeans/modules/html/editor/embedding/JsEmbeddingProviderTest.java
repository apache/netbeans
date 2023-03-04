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
package org.netbeans.modules.html.editor.embedding;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.html.lexer.HtmlLexerPlugin;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.WebUtils;

/**
 *
 * @author marekfukala
 */
public class JsEmbeddingProviderTest extends CslTestBase {

    public JsEmbeddingProviderTest(String name) {
        super(name);
    }

    public void testScriptTag() throws ParseException {
        assertEmbedding("<script>alert();</script>", "alert();\n");

        assertEmbedding("<script>\n"
                + "function hello() {\n"
                + "    alert('hello!');\n"
                + "}\n"
                + "</script>",
                "\n"
                + "function hello() {\n"
                + "    alert('hello!');\n"
                + "}\n"
                + "\n"
                + "");

        assertEmbedding("<script type=\"module\">alert();</script>", "alert();\n");
    }

    public void testCustomEL() {
        //the default JsEmbeddingProvider creates no virtual js embedding
        //at the place of the expression language. The js source is provided
        //by the frameworks plugins (KO, Angular).
        assertEmbedding("<div>{{hello}}</div>",
                null);
    }

    public void testIssue231633() {
        assertEmbedding(
                "<script type=\"text/javascript\">\n"
                + "   <!--   \n"
                + "   window.alert(\"Hello World!\");\n"
                + "   -->\n"
                + " </script>",
                "\n"
                + "      window.alert(\"Hello World!\");\n"
                + "   \n");

        assertEmbedding(
                "<script type=\"text/javascript\">\n"
                + "   <!--//-->   \n"
                + "   window.alert(\"Hello World!\");\n"
                + "   <!--//-->\n"
                + " </script>",
                "\n"
                + "      window.alert(\"Hello World!\");\n"
                + "   \n"
                + " \n");
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
}