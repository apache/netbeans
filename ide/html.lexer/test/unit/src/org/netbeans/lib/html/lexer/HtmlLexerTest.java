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
package org.netbeans.lib.html.lexer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 *
 * @author Tor Norbye
 */
public class HtmlLexerTest extends NbTestCase {

    public HtmlLexerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    public static Test Xsuite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new HtmlLexerTest("testAttributeNameWithUnderscorePrefix"));
        return suite;
    }

    public void testInput() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/testInput.rb.txt",
                HTMLTokenId.language());
    }

    public void test146930() {
        TokenHierarchy th = TokenHierarchy.create("<<body>", HTMLTokenId.language());
        TokenSequence ts = th.tokenSequence();
        ts.moveStart();

        assertTrue(ts.moveNext());
        assertEquals("<", ts.token().text().toString());
        assertEquals(HTMLTokenId.TEXT, ts.token().id());

        assertTrue(ts.moveNext());
        assertEquals("<", ts.token().text().toString());
        assertEquals(HTMLTokenId.TAG_OPEN_SYMBOL, ts.token().id());

        assertTrue(ts.moveNext());
        assertEquals("body", ts.token().text().toString());
        assertEquals(HTMLTokenId.TAG_OPEN, ts.token().id());

        assertTrue(ts.moveNext());
        assertEquals(">", ts.token().text().toString());
        assertEquals(HTMLTokenId.TAG_CLOSE_SYMBOL, ts.token().id());

        assertFalse(ts.moveNext());
    }

    public void test149018() throws Exception { //JSP editor not recognizing valid end-of-html comment
        LexerTestUtilities.checkTokenDump(this, "testfiles/testInput.html.txt",
                HTMLTokenId.language());
    }

    public void testFlyweightTokens() {
        assertEquals("a", HtmlElements.getCachedTagName("a"));
        assertEquals("div", HtmlElements.getCachedTagName("div"));
        assertEquals("td", HtmlElements.getCachedTagName("td"));
        assertNull(HtmlElements.getCachedTagName("Xdiv"));
        assertNull(HtmlElements.getCachedTagName("divX"));
        assertNull(HtmlElements.getCachedTagName("t"));
        assertNull(HtmlElements.getCachedTagName("fKHl"));

        assertEquals("onclick", HtmlElements.getCachedAttrName("onclick"));
        assertNull(HtmlElements.getCachedAttrName("fKHl"));
        assertNull(HtmlElements.getCachedAttrName("ht"));

        String code = "<div align='center'></div>";
        TokenHierarchy th = TokenHierarchy.create(code, HTMLTokenId.language());
        TokenSequence<HTMLTokenId> ts = th.tokenSequence();
        ts.moveStart();

        while(ts.moveNext()) {
            HTMLTokenId id = ts.token().id();
            switch (id) {
                case ARGUMENT:
                case TAG_CLOSE:
                case TAG_OPEN:
                case TAG_CLOSE_SYMBOL:
                case TAG_OPEN_SYMBOL:
                case OPERATOR:

                    assertTrue(ts.token().isFlyweight());

                    break;
            }
        }

    }

    public void testEmptyTag() {
        checkTokens("<div/>", "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", "/>|TAG_CLOSE_SYMBOL");
    }

    public void testUnfinishedTag() {
        checkTokens("<div/", "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", "/|TEXT");
    }

    public void testIssue149968() {
        checkTokens("<div @@@/>", "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " @@@|WS", "/>|TAG_CLOSE_SYMBOL");
    }

    public void testEmbeddedCss() {
        //not css attribute
        checkTokens("<div align=\"center\"/>", "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "align|ARGUMENT",
                "=|OPERATOR", "\"center\"|VALUE", "/>|TAG_CLOSE_SYMBOL");

        //css attribute
        checkTokens("<div class=\"myclass\"/>", "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "class|ARGUMENT",
                "=|OPERATOR", "\"myclass\"|VALUE_CSS", "/>|TAG_CLOSE_SYMBOL");
    }

    public void testGenericCssClassEmbedding() {
        Map<String, Collection<String>> map = new HashMap<>();
        map.put("c:button", Collections.singletonList("styleClass"));

        InputAttributes inputAttributes = new InputAttributes();
        inputAttributes.setValue(HTMLTokenId.language(), "cssClassTagAttrMap", map, false); //NOI18N

        String text = "<c:button styleClass=\"myclass\"/>";
        TokenHierarchy th = TokenHierarchy.create(text, true, HTMLTokenId.language(), Collections.<HTMLTokenId>emptySet(), inputAttributes);
        TokenSequence ts = th.tokenSequence();

        checkTokens(ts, "<|TAG_OPEN_SYMBOL", "c:button|TAG_OPEN", " |WS", "styleClass|ARGUMENT",
                "=|OPERATOR", "\"myclass\"|VALUE_CSS", "/>|TAG_CLOSE_SYMBOL");
        
    }

    public void testEmbeddedScripting() {
        //javascript embedding w/o type specification
        checkTokens("<script>x</script>", "<|TAG_OPEN_SYMBOL", "script|TAG_OPEN", ">|TAG_CLOSE_SYMBOL", "x|SCRIPT", "</|TAG_OPEN_SYMBOL", "script|TAG_CLOSE", ">|TAG_CLOSE_SYMBOL");


        //javascript embedding w/ explicit type specification
        checkTokens("<script type=\"text/javascript\">x</script>", 
                "<|TAG_OPEN_SYMBOL", "script|TAG_OPEN", " |WS", "type|ARGUMENT",
                "=|OPERATOR", "\"text/javascript\"|VALUE", ">|TAG_CLOSE_SYMBOL", "x|SCRIPT", "</|TAG_OPEN_SYMBOL", "script|TAG_CLOSE", ">|TAG_CLOSE_SYMBOL");

        //javascript embedding w/ explicit unknown type specification
        checkTokens("<script type=\"text/xxx\">x</script>",
                "<|TAG_OPEN_SYMBOL", "script|TAG_OPEN", " |WS", "type|ARGUMENT",
                "=|OPERATOR", "\"text/xxx\"|VALUE", ">|TAG_CLOSE_SYMBOL", "x|SCRIPT", "</|TAG_OPEN_SYMBOL", "script|TAG_CLOSE", ">|TAG_CLOSE_SYMBOL");

        //javascript embedding w/ explicit type specification of known but excluded type 
        checkTokens("<script type=\"text/vbscript\">x</script>",
                "<|TAG_OPEN_SYMBOL", "script|TAG_OPEN", " |WS", "type|ARGUMENT",
                "=|OPERATOR", "\"text/vbscript\"|VALUE", ">|TAG_CLOSE_SYMBOL", "x|SCRIPT", "</|TAG_OPEN_SYMBOL", "script|TAG_CLOSE", ">|TAG_CLOSE_SYMBOL");

        //check also single quotes
        //javascript embedding w/ explicit unknown type specification
        checkTokens("<script type='text/xxx'>x</script>",
                "<|TAG_OPEN_SYMBOL", "script|TAG_OPEN", " |WS", "type|ARGUMENT",
                "=|OPERATOR", "'text/xxx'|VALUE", ">|TAG_CLOSE_SYMBOL", "x|SCRIPT", "</|TAG_OPEN_SYMBOL", "script|TAG_CLOSE", ">|TAG_CLOSE_SYMBOL");


    }

    public void testEscapedQuotesInAttrValueSingleQuoted() {
        //             <div onclick='alert(\'hello\')'/>
        String code = "<div onclick='alert(\\'hello\\')'/>";

        checkTokens(code,  "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN",
                " |WS", "onclick|ARGUMENT", "=|OPERATOR",
                "'alert(\\'hello\\')'|VALUE_JAVASCRIPT", "/>|TAG_CLOSE_SYMBOL" );
    }

    public void testEscapedQuotesInAttrValueDoubleQuoted() {
        //             <div onclick="alert(\"hello\")"/>
        String code = "<div onclick=\"alert(\\\"hello\\\")\"/>";

        checkTokens(code,  "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN",
                " |WS", "onclick|ARGUMENT", "=|OPERATOR",
                "\"alert(\\\"hello\\\")\"|VALUE_JAVASCRIPT", "/>|TAG_CLOSE_SYMBOL" );
    }

    //issue 192803 requires the attribute name allowing underscore prefix
    public void testAttributeNameWithUnderscorePrefix() {
        checkTokens("<x:customTag _myattr='value'/>",
                "<|TAG_OPEN_SYMBOL", "x:customTag|TAG_OPEN", " |WS", "_myattr|ARGUMENT",
                "=|OPERATOR", "'value'|VALUE","/>|TAG_CLOSE_SYMBOL");

        //just the underscore cannot be the argument name
        checkTokens("<x:customTag _='value'/>",
                "<|TAG_OPEN_SYMBOL", "x:customTag|TAG_OPEN", " |WS", "_|ARGUMENT",
                "=|OPERATOR", "'value'|VALUE", "/>|TAG_CLOSE_SYMBOL");

    }
    
    public void testCurlyBracesInTag() {
        // standalone
        checkTokens("<div {a} align=center>",
                "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "{a}|ARGUMENT",
                " |WS", "align|ARGUMENT", "=|OPERATOR", "center|VALUE", ">|TAG_CLOSE_SYMBOL");
        
        //in attribute
        checkTokens("<div {a}align=center>",
                "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "{a}align|ARGUMENT",
                "=|OPERATOR", "center|VALUE", ">|TAG_CLOSE_SYMBOL");
        
        //end line in error
        checkTokens("<div {a}align=center\n <div>",
                "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "{a}align|ARGUMENT",
                "=|OPERATOR", "center|VALUE", "\n |WS", "<div|ARGUMENT",
                ">|TAG_CLOSE_SYMBOL");
        
        // tag close symbol
        checkTokens("<div {a> xxx",
                "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "{a|ARGUMENT", ">|TAG_CLOSE_SYMBOL", " xxx|TEXT");
        
        checkTokens("<div {a/> xxx",
                "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "{a|ARGUMENT", "/>|TAG_CLOSE_SYMBOL", " xxx|TEXT");
        
        //eof in error
        checkTokens("<div {a",
                "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "{a|ARGUMENT");
                
    }
    
    public void testCurlyBracesInTagWithClassAttr() {
        // standalone
        checkTokens("<div {a} class=my>",
                "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "{a}|ARGUMENT", " |WS", "class|ARGUMENT", "=|OPERATOR", "my|VALUE_CSS", ">|TAG_CLOSE_SYMBOL");
        
        //before class attribute
        checkTokens("<div {a}class=my>",
                "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "{a}class|ARGUMENT", "=|OPERATOR", "my|VALUE", ">|TAG_CLOSE_SYMBOL");
        
        //after class in tag
        checkTokens("<div class=my {a} >",
                "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "class|ARGUMENT", 
                "=|OPERATOR", "my|VALUE_CSS", " |WS", "{a}|ARGUMENT", " |WS", ">|TAG_CLOSE_SYMBOL" );

        //after class in tag
        checkTokens("<div class=my {a} id=your>",
                "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "class|ARGUMENT", 
                "=|OPERATOR", "my|VALUE_CSS", " |WS", "{a}|ARGUMENT", " |WS",
                "id|ARGUMENT", "=|OPERATOR", "your|VALUE_CSS", ">|TAG_CLOSE_SYMBOL");

        //after class in tag
        checkTokens("<div class=\"my\" {a} id=\"your\">",
                "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "class|ARGUMENT", 
                "=|OPERATOR", "\"my\"|VALUE_CSS", " |WS", "{a}|ARGUMENT", " |WS",
                "id|ARGUMENT", "=|OPERATOR", "\"your\"|VALUE_CSS", ">|TAG_CLOSE_SYMBOL");
        
        
                
    }
    
    public void testXMLPI() {
        checkTokens("<?xml version=\"1.0\" ?>", "<?xml version=\"1.0\" ?>|XML_PI");
        checkTokens("<div><? ?></div>", "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", ">|TAG_CLOSE_SYMBOL", "<? ?>|XML_PI", "</|TAG_OPEN_SYMBOL", "div|TAG_CLOSE", ">|TAG_CLOSE_SYMBOL");
    }
    
    public void testUnfinishedXMLPI() {
        checkTokens("<?xml", "<?xml|XML_PI");
    }

     //Bug 213332 - IllegalStateException: A bug #212445 just happended for source text " scrollbar-arrow-color:"black"; } </STYLE> <TITLE>Cyprus :: Larnaca</TITLE></HEAD> <BOD". Please report a new bug or r 
    //http://netbeans.org/bugzilla/show_bug.cgi?id=213332
    public void testIssue213332() {
        checkTokens("<style type=text/>", 
                "<|TAG_OPEN_SYMBOL", "style|TAG_OPEN", " |WS", "type|ARGUMENT", "=|OPERATOR", "text|VALUE", "/>|TAG_CLOSE_SYMBOL");
        
        checkTokens("<style type=text/css>", 
                "<|TAG_OPEN_SYMBOL", "style|TAG_OPEN", " |WS", "type|ARGUMENT", "=|OPERATOR", "text/css|VALUE", ">|TAG_CLOSE_SYMBOL");
    }
    
    public void testIssue213332_2() {
        checkTokens("<div align= </div>",
                "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "align|ARGUMENT", "=|OPERATOR", " |WS", 
                "<|ARGUMENT", "/|ERROR", "div|ARGUMENT", ">|TAG_CLOSE_SYMBOL");
    }

    //Bug 218629 - Wrong syntax coloring in HTML for non-english text 
    public void testIssue218629() {
        String text = "<div>שלום עולם</div>";
        //             01234567890123456789
        TokenHierarchy<String> th = TokenHierarchy.create(text, HTMLTokenId.language());
        TokenSequence<HTMLTokenId> ts = th.tokenSequence(HTMLTokenId.language());
        
        StringBuilder b = new StringBuilder();
        ts.moveStart();
        while(ts.moveNext()) {
            Token<HTMLTokenId> t = ts.token();
            b.append('"');
            b.append(t.text());
            b.append('"');
            b.append("\t\t");
            b.append(t.id().name());
            b.append("\t\t");
            b.append(ts.offset());
            b.append('-');
            b.append(ts.offset() + t.length());
            b.append('\n');
        }
        
//        System.out.println(b);
    }
    
    public void testBoldAmp() {
        checkTokens("&that", "&that|TEXT");
        checkTokens("&that;", "&that;|CHARACTER");
    }
    
    public void testLexingOfScriptTagWithHtmlContent() {
        checkTokens("<script type='text/html'><div></div></script>",
                "<|TAG_OPEN_SYMBOL", "script|TAG_OPEN", " |WS", "type|ARGUMENT",
                "=|OPERATOR", "'text/html'|VALUE", ">|TAG_CLOSE_SYMBOL",
                "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", ">|TAG_CLOSE_SYMBOL",
                "</|TAG_OPEN_SYMBOL", "div|TAG_CLOSE", ">|TAG_CLOSE_SYMBOL",
                "</|TAG_OPEN_SYMBOL", "script|TAG_CLOSE", ">|TAG_CLOSE_SYMBOL");
    }

    public void testParsingAngularAttributes() {
        // property bind
        checkTokens("<input x='y' [ngModel]='test' />",
            "<|TAG_OPEN_SYMBOL", "input|TAG_OPEN", " |WS", "x|ARGUMENT",
            "=|OPERATOR", "'y'|VALUE", " |WS", "[ngModel]|ARGUMENT",
            "=|OPERATOR", "'test'|VALUE", " |WS", "/>|TAG_CLOSE_SYMBOL");
        // event bind
        checkTokens("<input x='y' (change)='test' />",
            "<|TAG_OPEN_SYMBOL", "input|TAG_OPEN", " |WS", "x|ARGUMENT",
            "=|OPERATOR", "'y'|VALUE", " |WS", "(change)|ARGUMENT",
            "=|OPERATOR", "'test'|VALUE", " |WS", "/>|TAG_CLOSE_SYMBOL");
        // twoway bind
        checkTokens("<input x='y' [(value)]='test' />",
            "<|TAG_OPEN_SYMBOL", "input|TAG_OPEN", " |WS", "x|ARGUMENT",
            "=|OPERATOR", "'y'|VALUE", " |WS", "[(value)]|ARGUMENT",
            "=|OPERATOR", "'test'|VALUE", " |WS", "/>|TAG_CLOSE_SYMBOL");
        // template reference
        checkTokens("<input x='y' #ref />",
            "<|TAG_OPEN_SYMBOL", "input|TAG_OPEN", " |WS", "x|ARGUMENT",
            "=|OPERATOR", "'y'|VALUE", " |WS", "#ref|ARGUMENT",
            " |WS", "/>|TAG_CLOSE_SYMBOL");
        // structural directive
        checkTokens("<input x='y' *ngIf='test' />",
            "<|TAG_OPEN_SYMBOL", "input|TAG_OPEN", " |WS", "x|ARGUMENT",
            "=|OPERATOR", "'y'|VALUE", " |WS", "*ngIf|ARGUMENT",
            "=|OPERATOR", "'test'|VALUE", " |WS", "/>|TAG_CLOSE_SYMBOL");
    }


    //--------------------------------------------------------------------------
    
    public static void checkTokens(String text, String... descriptions) {
        TokenHierarchy<String> th = TokenHierarchy.create(text, HTMLTokenId.language());
        TokenSequence<HTMLTokenId> ts = th.tokenSequence(HTMLTokenId.language());
//        System.out.println(ts);
        checkTokens(ts, descriptions);
    }

    public static void checkTokens(TokenSequence<HTMLTokenId> ts, String... descriptions) {
        ts.moveStart();
        for(String descr : descriptions) {
            //parse description
            int slashIndex = descr.indexOf('|');
            assert slashIndex >= 0;

            String image = descr.substring(0, slashIndex);
            String id = descr.substring(slashIndex + 1);

            assertTrue(ts.moveNext());
            Token t = ts.token();
            assertNotNull(t);

            if(image.length() > 0) {
                assertEquals(image, t.text().toString());
            }

            if(id.length() > 0) {
                assertEquals(id, t.id().name());
            }
        }

        StringBuilder b = new StringBuilder();
        while(ts.moveNext()) {
            Token t = ts.token();
            b.append("\"");
            b.append(t.text());
            b.append('|');
            b.append(t.id().name());
            b.append("\"");
            b.append(", ");
        }
        assertTrue("There are some tokens left: " + b.toString(), b.length() == 0);
    }

     public void testScriptType_value() {
        TokenHierarchy th = TokenHierarchy.create("<script type=\"text/plain\">plain</script>", HTMLTokenId.language());
        TokenSequence ts = th.tokenSequence();
        ts.moveStart();

        while(ts.moveNext()) {
            Token t = ts.token();
            if(t.id() == HTMLTokenId.SCRIPT) {
                String scriptType = (String)t.getProperty(HTMLTokenId.SCRIPT_TYPE_TOKEN_PROPERTY);
                assertNotNull(scriptType);
                assertEquals("text/plain", scriptType);
                return ;
            }
        }
        
        assertTrue("Couldn't find any SCRIPT token!", false);
    }
     
    public void testScriptType_empty() {
        TokenHierarchy th = TokenHierarchy.create("<script type=\"\">plain</script>", HTMLTokenId.language());
        TokenSequence ts = th.tokenSequence();
        ts.moveStart();

        while(ts.moveNext()) {
            Token t = ts.token();
            if(t.id() == HTMLTokenId.SCRIPT) {
                String scriptType = (String)t.getProperty(HTMLTokenId.SCRIPT_TYPE_TOKEN_PROPERTY);
                assertNotNull(scriptType);
                assertEquals("", scriptType);
                return ;
            }
        }
        
        assertTrue("Couldn't find any SCRIPT token!", false);
    }
    
    public void testScriptType_missing() {
        TokenHierarchy th = TokenHierarchy.create("<script>plain</script>", HTMLTokenId.language());
        TokenSequence ts = th.tokenSequence();
        ts.moveStart();

        while(ts.moveNext()) {
            Token t = ts.token();
            if(t.id() == HTMLTokenId.SCRIPT) {
                String scriptType = (String)t.getProperty(HTMLTokenId.SCRIPT_TYPE_TOKEN_PROPERTY);
                assertNull(scriptType);
                return ;
            }
        }
        
        assertTrue("Couldn't find any SCRIPT token!", false);
    }
    
    }
