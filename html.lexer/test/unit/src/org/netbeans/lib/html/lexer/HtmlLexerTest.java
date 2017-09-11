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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
                "<|TAG_OPEN_SYMBOL", "x:customTag|TAG_OPEN", " |WS", "_=|ERROR",
                "'value'/>|TEXT");

    }
    
    public void testCurlyBracesInTag() {
        //error in ws
        checkTokens("<div {a} align=center>",
                "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "{a}|ERROR", 
                " |WS", "align|ARGUMENT", "=|OPERATOR", "center|VALUE", ">|TAG_CLOSE_SYMBOL");
        
        //error before attribute
        checkTokens("<div {a}align=center>",
                "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "{a}align=center|ERROR", ">|TAG_CLOSE_SYMBOL");
        
        //end line in error
        checkTokens("<div {a}align=center\n <div>",
                "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "{a}align=center|ERROR", 
                "\n |WS", "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", ">|TAG_CLOSE_SYMBOL");
        
        // tag close symbol in error
        checkTokens("<div {a> xxx",
                "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "{a|ERROR", ">|TAG_CLOSE_SYMBOL", " xxx|TEXT");
        
        checkTokens("<div {a/> xxx",
                "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "{a|ERROR", "/>|TAG_CLOSE_SYMBOL", " xxx|TEXT");
        
        //eof in error
        checkTokens("<div {a",
                "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "{a|ERROR");
                
    }
    
    public void testCurlyBracesInTagWithClassAttr() {
        //error in ws
        checkTokens("<div {a} class=my>",
                "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "{a}|ERROR", " |WS", "class|ARGUMENT", "=|OPERATOR", "my|VALUE_CSS", ">|TAG_CLOSE_SYMBOL");
        
        //error before attribute
        checkTokens("<div {a}class=my>",
                "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "{a}class=my|ERROR", ">|TAG_CLOSE_SYMBOL");
        
        //after class in tag
        checkTokens("<div class=my {a} >",
                "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "class|ARGUMENT", 
                "=|OPERATOR", "my|VALUE_CSS", " |WS", "{a}|ERROR", " |WS", ">|TAG_CLOSE_SYMBOL" );

        //after class in tag
        checkTokens("<div class=my {a} id=your>",
                "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "class|ARGUMENT", 
                "=|OPERATOR", "my|VALUE_CSS", " |WS", "{a}|ERROR", " |WS", 
                "id|ARGUMENT", "=|OPERATOR", "your|VALUE_CSS", ">|TAG_CLOSE_SYMBOL");

        //after class in tag
        checkTokens("<div class=\"my\" {a} id=\"your\">",
                "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "class|ARGUMENT", 
                "=|OPERATOR", "\"my\"|VALUE_CSS", " |WS", "{a}|ERROR", " |WS", 
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
                "</|TAG_OPEN_SYMBOL", "div|TAG_CLOSE", ">|TAG_CLOSE_SYMBOL");
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
