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
package org.netbeans.modules.javascript2.editor.parser;

import com.oracle.js.parser.ir.FunctionNode;
import javax.swing.text.Document;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.editor.parser.SanitizingParser.Context;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.openide.util.Pair;

/**
 *
 * @author Petr Hejl
 */
public class JsParserTest extends JsTestBase {

    public JsParserTest(String testName) {
        super(testName);
    }
    
    public void testSimpleCurly1() throws Exception {
        parse("var global1 = new Foo.Bar();\n"
            + "var global2 = new Array();\n"
            + "if (true) {\n"
            + "   var global3 = new org.foo.bar.Baz();\n"
            + "   gl\n"
            + "{\n"
            + "\n"
            + "DonaldDuck.Mickey.Baz.boo.boo = function(param) {\n"
            + "    return true;\n"
            + "}\n",
            "var global1 = new Foo.Bar();\n"
            + "var global2 = new Array();\n"
            + "if (true) {\n"
            + "   var global3 = new org.foo.bar.Baz();\n"
            + "   gl\n"
            + "{\n"
            + "\n"
            + "DonaldDuck.Mickey.Baz.boo.boo = function(param) {\n"
            + "    return true;\n"
            + "}\n}}",
            1,
            JsParser.Sanitize.MISSING_CURLY);
    }
    
    public void testSimpleCurly2() throws Exception {
        parse("var global1 = new Foo.Bar();\n"
            + "var global2 = new Array();\n"
            + "if (true) {\n"
            + "   var global3 = new org.foo.bar.Baz();\n"
            + "   gl\n"
            + "{\n"
            + "\n"
            + "DonaldDuck.Mickey.Baz.boo.boo = function(param) {\n"
            + "    return true;\n"
            + "}\n}}}",
            "var global1 = new Foo.Bar();\n"
            + "var global2 = new Array();\n"
            + "if (true) {\n"
            + "   var global3 = new org.foo.bar.Baz();\n"
            + "   gl\n"
            + "{\n"
            + "\n"
            + "DonaldDuck.Mickey.Baz.boo.boo = function(param) {\n"
            + "    return true;\n"
            + "}\n}} ",
            1,
            JsParser.Sanitize.MISSING_CURLY);
    }
    
    public void testSimpleSemicolon1() throws Exception {
        parse("\n"
            + "label:\n"
            + "\n",
            "\n"
            + "label:\n"
            + "\n;",
            1,
            JsParser.Sanitize.MISSING_SEMICOLON);
    }

    public void testSimpleCurrentError1() throws Exception {
        parse("var global1 = new Foo.Bar();\n"
            + "var global2 = new Array();\n"
            + "\n"
            + "a = 0x1G\n"
            + "\n"
            + "var global3 = 7\n",
            "var global1 = new Foo.Bar();\n"
            + "var global2 = new Array();\n"
            + "\n"
            + "a = 0x1 \n"
            + "\n"
            + "var global3 = 7\n",
            1,
            JsParser.Sanitize.SYNTAX_ERROR_CURRENT);
    }
    
    public void testSimplePreviousError1() throws Exception {
        parse("var global1 = new Foo.Bar();\n"
            + "var global2 = new Array();\n"
            + "if (true) {\n"
            + "   var global3 = new org.foo.bar.Baz();\n"
            + "   gl.\n"
            + "}\n"
            + "\n"
            + "DonaldDuck.Mickey.Baz.boo.boo = function(param) {\n"
            + "    return true;\n"
            + "}\n",
            "var global1 = new Foo.Bar();\n"
            + "var global2 = new Array();\n"
            + "if (true) {\n"
            + "   var global3 = new org.foo.bar.Baz();\n"
            + "   gl \n"
            + "}\n"
            + "\n"
            + "DonaldDuck.Mickey.Baz.boo.boo = function(param) {\n"
            + "    return true;\n"
            + "}\n",
            1,
            JsParser.Sanitize.SYNTAX_ERROR_PREVIOUS);
    }

    public void testSimplePreviousError2() throws Exception {
        parse("window.history.",
            "window.history ",
            1,
            JsParser.Sanitize.SYNTAX_ERROR_PREVIOUS);
    }

    public void testSimpleErrorDot1() throws Exception {
        parse("window.history.\n"
            + "function test(){"
            + "}",
            "window.history \n"
            + "function test(){"
            + "}",
            1,
            JsParser.Sanitize.ERROR_DOT);
    }

    public void testSimpleErrorLine1() throws Exception {
        parse("var global1 = new Foo.Bar();\n"
            + "var global2 = new Array();\n"
            + "if (true) {\n"
            + "   var global3 = new org.foo.bar.Baz();\n"
            + "   gl./d /\n"
            + "}\n"
            + "\n"
            + "DonaldDuck.Mickey.Baz.boo.boo = function(param) {\n"
            + "    return true;\n"
            + "}\n",
            "var global1 = new Foo.Bar();\n"
            + "var global2 = new Array();\n"
            + "if (true) {\n"
            + "   var global3 = new org.foo.bar.Baz();\n"
            + "          \n"
            + "}\n"
            + "\n"
            + "DonaldDuck.Mickey.Baz.boo.boo = function(param) {\n"
            + "    return true;\n"
            + "}\n",
            2,
            JsParser.Sanitize.ERROR_LINE);
    }
    
    public void testSimpleErrorLine2() throws Exception {
        parse("function A() {\n"
            + "}\n"
            + "A.prototype.say = function() {\n"
            + "    return \"ahoj\";\n"
            + "}\n"
            + "var a = new A();\n"
            + "function B() {\n"
            + "}\n" 
            + "B.prototype = new A();\n"
            + "var b = new B();\n"
            + "b.\n",
            "function A() {\n"
            + "}\n"
            + "A.prototype.say = function() {\n"
            + "    return \"ahoj\";\n"
            + "}\n"
            + "var a = new A();\n"
            + "function B() {\n"
            + "}\n" 
            + "B.prototype = new A();\n"
            + "var b = new B();\n"
            + "b \n",
            1,
            JsParser.Sanitize.SYNTAX_ERROR_PREVIOUS);
    }

    public void testSimpleParen1() throws Exception {
        parse("if (data != null) {\n"
            + "$.each(data, function(i,item) {\n"
            + "    text = \"test\";\n"
            + "    item = item.test\n"
            + "}\n"
            + "}",
            "if (data != null) {\n"
            + "$.each(data, function(i,item) {\n"
            + "    text = \"test\";\n"
            + "    item = item.test\n"
            + "})"
            + "}", 1, SanitizingParser.Sanitize.MISSING_PAREN);
    }

    public void testPreviousLines() throws Exception {
        parse("$('#selectorId').SomePlugin({ \n"
            + "    inline: true, \n"
            + "    calendars: 3,\n"
            + "    mode: 'range',\n"
            + "  _UNKNOWN_\n"
            + "    date: [c_from, c_to],\n"
            + "    current: new Date(c_to.getFullYear(), c_to.getMonth(), 1),\n"
            + "  _UNKNOWN_\n"
            + "    onChange: function(dates,el) {\n"
            + "        a\n"
            + "      }\n"
            + "});",
            "$('#selectorId').SomePlugin({ \n"
            + "    inline: true, \n"
            + "    calendars: 3,\n"
            + "    mode: 'range',\n"
            + "           \n"
            + "    date: [c_from, c_to],\n"
            + "    current: new Date(c_to.getFullYear(), c_to.getMonth(), 1),\n"
            + "  _UNKNOWN_\n"
            + "    onChange: function(dates,el) {\n"
            + "         \n"
            + "      }\n"
            + "});",
            2,
            SanitizingParser.Sanitize.PREVIOUS_LINES);
    }

    public void testBrokenModule() throws Exception {
        Pair<FunctionNode, Integer> result = parse("function x() {\n"
                + "\n"
                + "}\n"
                + "\n"
                + "export {\n");
        assertNotNull(result.first());
        assertEquals(1, result.second().intValue());
    }

    public void testRegexp() throws Exception {
        parse("$?c.onreadystatechange=function(){/loaded|complete/.test(c.readyState)&&d()}:c.onload=c.onerror=d;\n",
                null, 0, null);
    }

    private void parse(String original, String expected, int errorCount,
            JsParser.Sanitize sanitization) throws Exception {

        JsParser parser = new JsParser();
        Document doc = getDocument(original);
        Snapshot snapshot = Source.create(doc).createSnapshot();
        Context context = new JsParser.Context("test.js", snapshot, -1, JsTokenId.javascriptLanguage());
        JsErrorManager manager = new JsErrorManager(snapshot, JsTokenId.javascriptLanguage());
        parser.parseContext(context, JsParser.Sanitize.NONE, manager);
        
        assertEquals(expected, context.getSanitizedSource());
        assertEquals(errorCount, manager.getErrors().size());
        assertEquals(sanitization, context.getSanitization());
    }
    
    private Pair<FunctionNode, Integer> parse(String text) throws Exception {
        JsParser parser = new JsParser();
        Document doc = getDocument(text);
        Snapshot snapshot = Source.create(doc).createSnapshot();
        JsErrorManager manager = new JsErrorManager(snapshot, JsTokenId.javascriptLanguage());
        JsParserResult result = parser.parseSource(snapshot, null, SanitizingParser.Sanitize.NONE, manager);
        return Pair.of(result.getRoot(), manager.getErrors().size());
    }
}
