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

package org.netbeans.modules.javascript2.editor.formatter;

import java.util.HashMap;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.test.CslTestBase.IndentPrefs;
import org.netbeans.modules.web.indent.api.support.AbstractIndenter;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.indent.HtmlIndentTaskFactory;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

public class JsFormatterEmbeddedTest extends JsFormatterTestBase {

    public JsFormatterEmbeddedTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AbstractIndenter.inUnitTestRun = true;

        MockMimeLookup.setInstances(MimePath.parse("text/javascript"), JsTokenId.javascriptLanguage());
        HtmlIndentTaskFactory htmlReformatFactory = new HtmlIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/html"), htmlReformatFactory, new HtmlKit("text/html"), HTMLTokenId.language());
    }

    @Override
    protected BaseDocument getDocument(FileObject fo, String mimeType, Language language) {
        // for some reason GsfTestBase is not using DataObjects for BaseDocument construction
        // which means that for example Java formatter which does call EditorCookie to retrieve
        // document will get difference instance of BaseDocument for indentation
        try {
             DataObject dobj = DataObject.find(fo);
             assertNotNull(dobj);

             EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
             assertNotNull(ec);

             return (BaseDocument)ec.openDocument();
        }
        catch (Exception ex){
            fail(ex.toString());
            return null;
        }
    }

    public void testEmbeddedSimple1() throws Exception {
        reformatFileContents("testfiles/formatter/embeddedSimple1.html", new IndentPrefs(4,4));
    }

    public void testEmbeddedSimple2() throws Exception {
        reformatFileContents("testfiles/formatter/embeddedSimple2.html", new IndentPrefs(4,4));
    }

    public void testEmbeddedSimple3() throws Exception {
        reformatFileContents("testfiles/formatter/embeddedSimple3.html", new IndentPrefs(4,4));
    }

    public void testEmbeddedSimple4() throws Exception {
        reformatFileContents("testfiles/formatter/embeddedSimple4.html", new IndentPrefs(4,4));
    }

    public void testEmbeddedSimple5() throws Exception {
        reformatFileContents("testfiles/formatter/embeddedSimple5.html", new IndentPrefs(4,4));
    }

    public void testEmbeddedSimple6() throws Exception {
        reformatFileContents("testfiles/formatter/embeddedSimple6.html", new IndentPrefs(4,4));
    }

    public void testEmbeddedSimple7() throws Exception {
        reformatFileContents("testfiles/formatter/embeddedSimple7.html", new IndentPrefs(4,4));
    }

    public void testEmbeddedTrimmed1() throws Exception {
        reformatFileContents("testfiles/formatter/embeddedTrimmed1.html", new IndentPrefs(4,4));
    }

    public void testEmbeddedMultipleSections1() throws Exception {
        reformatFileContents("testfiles/formatter/embeddedMultipleSections1.html", new IndentPrefs(4,4));
    }

    public void testEmbeddedMultipleSections2() throws Exception {
        reformatFileContents("testfiles/formatter/embeddedMultipleSections2.html", new IndentPrefs(4,4));
    }

    public void testJsx1() throws Exception {
        reformatFileContents("testfiles/formatter/jsx1.js",new IndentPrefs(4, 4));
    }

    public void testJsx1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/jsx1.js");
    }

    public void testJsx2() throws Exception {
        reformatFileContents("testfiles/formatter/jsx2.js",new IndentPrefs(4, 4));
    }

    public void testJsx2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/jsx2.js");
    }

    public void testJsx3() throws Exception {
        reformatFileContents("testfiles/formatter/jsx3.js",new IndentPrefs(4, 4));
    }

    public void testJsx3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/jsx3.js");
    }

    public void testJsx4() throws Exception {
        reformatFileContents("testfiles/formatter/jsx4.js",new IndentPrefs(4, 4));
    }

    public void testJsx4Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/jsx4.js");
    }

    public void testJsx5() throws Exception {
        reformatFileContents("testfiles/formatter/jsx5.js",new IndentPrefs(4, 4));
    }

    public void testJsx5Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/jsx5.js");
    }

    public void testJsx6() throws Exception {
        reformatFileContents("testfiles/formatter/jsx6.js",new IndentPrefs(4, 4));
    }

    public void testJsx6Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/jsx6.js");
    }

    public void testJsx7BinaryBeforeIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapAfterBinaryOps, false);
        reformatFileContents("testfiles/formatter/jsx7.js", options, ".binaryBefore.IfLong.formatted");
    }

    public void testJsx7BinaryAfterIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapAfterBinaryOps, true);
        reformatFileContents("testfiles/formatter/jsx7.js", options, ".binaryAfter.IfLong.formatted");
    }

    public void testJsx7Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/jsx7.js");
    }
    
    public void testJsx8BinaryBeforeIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapAfterBinaryOps, false);
        reformatFileContents("testfiles/formatter/jsx8.js", options, ".binaryBefore.IfLong.formatted");
    }

    public void testJsx8BinaryAfterIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<>();
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapAfterBinaryOps, true);
        reformatFileContents("testfiles/formatter/jsx8.js", options, ".binaryAfter.IfLong.formatted");
    }

    public void testJsx8Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/jsx8.js");
    }

    public void testJsx9() throws Exception {
        reformatFileContents("testfiles/formatter/jsx9.js",new IndentPrefs(4, 4));
    }

    public void testJsx9Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/jsx9.js");
    }
    
    public void testJsx10() throws Exception {
        reformatFileContents("testfiles/formatter/jsx10.js",new IndentPrefs(4, 4));
    }
    
    public void testJsx10Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/jsx10.js");
    }

    public void testBroken1() throws Exception {
        reformatFileContents("testfiles/formatter/broken1.js",new IndentPrefs(4, 4));
    }

    public void testIssue225749() throws Exception {
        reformatFileContents("testfiles/formatter/issue225749.html", new IndentPrefs(4,4));
    }

    // XXX also see org.netbeans.modules.php.editor.js.JsFormatterEmbeddedTest
}
