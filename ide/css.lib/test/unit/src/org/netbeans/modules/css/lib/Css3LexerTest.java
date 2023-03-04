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
package org.netbeans.modules.css.lib;

import org.antlr.runtime.Lexer;
import org.antlr.runtime.Token;
import org.netbeans.modules.csl.api.test.CslTestBase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mfukala@netbeans.org
 */
public class Css3LexerTest extends CslTestBase {

    public Css3LexerTest(String name) {
        super(name);
    }

    public void testUnrecognizedTokenLexing() {
        String source = "@";
        ExtCss3Lexer lexer = createLexer(source);

        Token t = lexer.nextToken();
        assertNotNull(t);
        assertEquals(Token.INVALID_TOKEN_TYPE, t.getType());

    }

    public void testURL() {
        ExtCss3Lexer lexer = createLexer("url('hello.png')");
        assertANTLRToken("url('hello.png')", Css3Lexer.URI, lexer.nextToken());

        lexer = createLexer("url(hello.png)");
        assertANTLRToken("url(hello.png)", Css3Lexer.URI, lexer.nextToken());
//        TestUtil.dumpTokens(lexer);
        lexer = createLexer("url(http://site.org/hello.png)");
        assertANTLRToken("url(http://site.org/hello.png)", Css3Lexer.URI, lexer.nextToken());

    }

    public void testLexingOfUPlusWSChar() throws Exception {
        //lexing of 'u'+' ' chars is wrong, it produces error token instead of IDENT+WS tokens
        String source = "u ";

        //now do the same with the netbeans lexer
        ExtCss3Lexer lexer = createLexer(source);

        assertANTLRToken("u", Css3Lexer.IDENT, lexer.nextToken());
        assertANTLRToken(" ", Css3Lexer.WS, lexer.nextToken());
        assertANTLRToken(null, Css3Lexer.EOF, lexer.nextToken());
    }

   public void testLexingOfImportSymbol() throws Exception {
        String source = "@import xxx";

        //now do the same with the netbeans lexer
        ExtCss3Lexer lexer = createLexer(source);

        assertANTLRToken("@import", Css3Lexer.IMPORT_SYM, lexer.nextToken());
        assertANTLRToken(" ", Css3Lexer.WS, lexer.nextToken());
        assertANTLRToken("xxx", Css3Lexer.IDENT, lexer.nextToken());
        assertANTLRToken(null, Css3Lexer.EOF, lexer.nextToken());
    }

   public void testLexingOfPseudoElement() throws Exception {
        String source = "div::before";

        //now do the same with the netbeans lexer
        ExtCss3Lexer lexer = createLexer(source);

        assertANTLRToken("div", Css3Lexer.IDENT, lexer.nextToken());
        assertANTLRToken("::", Css3Lexer.DCOLON, lexer.nextToken());
        assertANTLRToken("before", Css3Lexer.IDENT, lexer.nextToken());
    }

   public void testNumbers() throws Exception {
        String source = "200 ";

        //now do the same with the netbeans lexer
        ExtCss3Lexer lexer = createLexer(source);

        assertANTLRToken("200", Css3Lexer.NUMBER, lexer.nextToken());

        source = "200px ";
        //now do the same with the netbeans lexer
        lexer = createLexer(source);

        assertANTLRToken("200px", Css3Lexer.LENGTH, lexer.nextToken());
    }

    public void testErrorCase1() throws Exception {
        /*
        java.lang.ArrayIndexOutOfBoundsException: Array index out of range: 4
	at java.util.Vector.get(Vector.java:694)
	at org.netbeans.modules.css.lib.nblexer.NbLexerCharStream.rewind(NbLexerCharStream.java:131)
	at org.antlr.runtime.DFA.predict(DFA.java:149)
	at org.netbeans.modules.css.lib.Css3Lexer.mNUMBER(Css3Lexer.java:7440)
        */
        String source = "padding: .5em; ";

        ExtCss3Lexer lexer = createLexer(source);

        assertANTLRToken("padding", Css3Lexer.IDENT, lexer.nextToken());
        assertANTLRToken(":", Css3Lexer.COLON, lexer.nextToken());
        assertANTLRToken(" ", Css3Lexer.WS, lexer.nextToken());
        assertANTLRToken(".5em", Css3Lexer.EMS, lexer.nextToken());


    }

    public void testMediaQueriesTokens() throws Exception {
        String source = "AND NOT ONLY 100dpi 50dpcm ";

        ExtCss3Lexer lexer = createLexer(source);

        assertANTLRToken(null ,Css3Lexer.IDENT, lexer.nextToken());
        assertANTLRToken(null, Css3Lexer.WS, lexer.nextToken());
        assertANTLRToken(null ,Css3Lexer.NOT, lexer.nextToken());
        assertANTLRToken(null, Css3Lexer.WS, lexer.nextToken());
        assertANTLRToken(null ,Css3Lexer.IDENT, lexer.nextToken());
        assertANTLRToken(null, Css3Lexer.WS, lexer.nextToken());
        assertANTLRToken(null ,Css3Lexer.RESOLUTION, lexer.nextToken());
        assertANTLRToken(null, Css3Lexer.WS, lexer.nextToken());
        assertANTLRToken(null ,Css3Lexer.RESOLUTION, lexer.nextToken());

    }
    
    public void testSupportsToken() throws Exception {
        String source = "@supports ";
        
        Lexer lexer = createLexer(source);
        
        assertANTLRToken("@supports", Css3Lexer.SUPPORTS_SYM, lexer.nextToken());
        
    }
    
    public void testSimpleSupportsStatementTokens() throws Exception {
        String source = "@supports not (text-align: center) {} ";
        
        Lexer lexer = createLexer(source);
        
        assertANTLRToken("@supports", Css3Lexer.SUPPORTS_SYM, lexer.nextToken());
        assertANTLRToken(null ,Css3Lexer.WS, lexer.nextToken());
        assertANTLRToken("not" ,Css3Lexer.NOT, lexer.nextToken());
        assertANTLRToken(null ,Css3Lexer.WS, lexer.nextToken());
        assertANTLRToken("(" ,Css3Lexer.LPAREN, lexer.nextToken());
        assertANTLRToken("text-align" ,Css3Lexer.IDENT, lexer.nextToken());
        assertANTLRToken(":", Css3Lexer.COLON, lexer.nextToken());
        assertANTLRToken(null ,Css3Lexer.WS, lexer.nextToken());
        assertANTLRToken("center" ,Css3Lexer.IDENT, lexer.nextToken());
        assertANTLRToken(")" ,Css3Lexer.RPAREN, lexer.nextToken());
        assertANTLRToken(null ,Css3Lexer.WS, lexer.nextToken());
        assertANTLRToken("{" ,Css3Lexer.LBRACE, lexer.nextToken());
        assertANTLRToken("}" ,Css3Lexer.RBRACE, lexer.nextToken());
        
    }
    
    public void testCaseInsensivityOfSomeAtTokens() throws Exception {
        String source = "@FONT-face @charset @CHARSET @charSeT ";

        Lexer lexer = createLexer(source);

        assertANTLRToken("@FONT-face" ,Css3Lexer.FONT_FACE_SYM, lexer.nextToken());
        assertANTLRToken(null ,Css3Lexer.WS, lexer.nextToken());
        assertANTLRToken("@charset" ,Css3Lexer.CHARSET_SYM, lexer.nextToken());
        assertANTLRToken(null ,Css3Lexer.WS, lexer.nextToken());
        assertANTLRToken("@CHARSET" ,Css3Lexer.CHARSET_SYM, lexer.nextToken());
        assertANTLRToken(null ,Css3Lexer.WS, lexer.nextToken());
        assertANTLRToken("@charSeT" ,Css3Lexer.CHARSET_SYM, lexer.nextToken());

    }

    public void testRemUnit() throws Exception {
        String source = "10rad 20rem ";

        Lexer lexer = createLexer(source);

        assertANTLRToken(null ,Css3Lexer.ANGLE, lexer.nextToken());
        assertANTLRToken(null ,Css3Lexer.WS, lexer.nextToken());

        assertANTLRToken(null ,Css3Lexer.REM, lexer.nextToken());
        assertANTLRToken(null ,Css3Lexer.WS, lexer.nextToken());

    }

     public void testLexingURLToken() throws Exception {
        String source = "url(http://fonts.googleapis.com/css?family=Syncopate) ";
        Lexer lexer = createLexer(source);
        assertANTLRToken(null ,Css3Lexer.URI, lexer.nextToken());
    }

     public void testSassVar() throws Exception {
        String source = "$var ";
        Lexer lexer = createLexer(source);
        assertANTLRToken(null ,Css3Lexer.SASS_VAR, lexer.nextToken());
    }

     public void testCPLineComment() throws Exception {
        String source = "//line comment\n";
        Lexer lexer = createLexer(source);
        assertANTLRToken(null ,Css3Lexer.LINE_COMMENT, lexer.nextToken());

    }

    public void testExtendOnlySelector() throws Exception {
        String source = "body%my";
        Lexer lexer = createLexer(source);
        assertANTLRToken(null ,Css3Lexer.IDENT, lexer.nextToken());
        assertANTLRToken(null ,Css3Lexer.SASS_EXTEND_ONLY_SELECTOR, lexer.nextToken());
    }

    public void testExtendOnlySelector2() throws Exception {
        String source = "#context a%extreme {";
        Lexer lexer = createLexer(source);
        assertANTLRToken(null ,Css3Lexer.HASH, lexer.nextToken());
        assertANTLRToken(null ,Css3Lexer.WS, lexer.nextToken());
        assertANTLRToken(null ,Css3Lexer.IDENT, lexer.nextToken());
        assertANTLRToken(null ,Css3Lexer.SASS_EXTEND_ONLY_SELECTOR, lexer.nextToken());
        assertANTLRToken(null ,Css3Lexer.WS, lexer.nextToken());
        assertANTLRToken(null ,Css3Lexer.LBRACE, lexer.nextToken());
    }

//    public void testSASS_ElseIf() throws Exception {
//        String source = "@else if cau";
//        Lexer lexer = createLexer(source);
//        assertANTLRToken(null ,Css3Lexer.SASS_ELSEIF, lexer.nextToken());
//        assertANTLRToken(null ,Css3Lexer.WS, lexer.nextToken());
//        assertANTLRToken(null ,Css3Lexer.IDENT, lexer.nextToken());
//
//        source = "@elseif cau";
//        lexer = createLexer(source);
//        assertANTLRToken(null ,Css3Lexer.SASS_ELSEIF, lexer.nextToken());
//        assertANTLRToken(null ,Css3Lexer.WS, lexer.nextToken());
//        assertANTLRToken(null ,Css3Lexer.IDENT, lexer.nextToken());
//
//        source = "@else        if cau";
//        lexer = createLexer(source);
//        assertANTLRToken(null ,Css3Lexer.SASS_ELSEIF, lexer.nextToken());
//        assertANTLRToken(null ,Css3Lexer.WS, lexer.nextToken());
//        assertANTLRToken(null ,Css3Lexer.IDENT, lexer.nextToken());
//    }

    public void testSASS_Else() throws Exception {
        String source = "@else cau";
        Lexer lexer = createLexer(source);
        assertANTLRToken(null ,Css3Lexer.SASS_ELSE, lexer.nextToken());
        assertANTLRToken(null ,Css3Lexer.WS, lexer.nextToken());
        assertANTLRToken(null ,Css3Lexer.IDENT, lexer.nextToken());
    }

    public void testLineComment() throws Exception {
        String source = "//comment\na";
        Lexer lexer = createLexer(source);
        assertANTLRToken("//comment" ,Css3Lexer.LINE_COMMENT, lexer.nextToken());
        assertANTLRToken(null ,Css3Lexer.NL, lexer.nextToken());
        assertANTLRToken("a",Css3Lexer.IDENT, lexer.nextToken());
    }

    public void testLineCommentAtTheFileEnd() throws Exception {
        String source = "//comment";
        Lexer lexer = createLexer(source);
        assertANTLRToken("//comment" ,Css3Lexer.LINE_COMMENT, lexer.nextToken());
    }

    public void testLexingOfPercentageWithoutNumberPrefix() throws Exception {
        String source = "font: %/20 ";
        Lexer lexer = createLexer(source);
        assertANTLRToken("font" ,Css3Lexer.IDENT, lexer.nextToken());
        assertANTLRToken(":" ,Css3Lexer.COLON, lexer.nextToken());
        assertANTLRToken(" " ,Css3Lexer.WS, lexer.nextToken());
        assertANTLRToken("%" , Css3Lexer.PERCENTAGE_SYMBOL, lexer.nextToken());
        assertANTLRToken("/" ,Css3Lexer.SOLIDUS, lexer.nextToken());
        assertANTLRToken("20" , Css3Lexer.NUMBER, lexer.nextToken());
        assertANTLRToken(" " , Css3Lexer.WS, lexer.nextToken());
    }

    public void testLESS_JS_STRING() throws Exception {
        String source = "`\"hello\".toUpperCase() + '!'`;";
        Lexer lexer = createLexer(source);
        assertANTLRToken("`\"hello\".toUpperCase() + '!'`" ,Css3Lexer.LESS_JS_STRING, lexer.nextToken());
        assertANTLRToken(";" ,Css3Lexer.SEMI, lexer.nextToken());
    }

    public void testURLWithAtSign() throws Exception {
        String source = "url(bottom@2x.png)";
        Lexer lexer = createLexer(source);
        assertANTLRToken("url(bottom@2x.png)" ,Css3Lexer.URI, lexer.nextToken());
    }

    public void testIssue236649() throws Exception {
        String source = "url(http://fonts.googleapis.com/css?family=Josefin+Sans|Sigmar+One|Maven+Pro)";
        Lexer lexer = createLexer(source);
        assertANTLRToken("url(http://fonts.googleapis.com/css?family=Josefin+Sans|Sigmar+One|Maven+Pro)", Css3Lexer.URI, lexer.nextToken());
    }

    public void testIssue237975_01() throws Exception {
        String source = "@import (css) \"theme\"";
        ExtCss3Lexer lexer = createLexer(source);
        assertANTLRToken("@import", Css3Lexer.IMPORT_SYM, lexer.nextToken());
        assertANTLRToken(" ", Css3Lexer.WS, lexer.nextToken());
        assertANTLRToken("(", Css3Lexer.LPAREN, lexer.nextToken());
        assertANTLRToken("css", Css3Lexer.IDENT, lexer.nextToken());
        assertANTLRToken(")", Css3Lexer.RPAREN, lexer.nextToken());
        assertANTLRToken(" ", Css3Lexer.WS, lexer.nextToken());
        assertANTLRToken("\"theme\"", Css3Lexer.STRING, lexer.nextToken());
        assertANTLRToken(null, Css3Lexer.EOF, lexer.nextToken());
    }

    public void testIssue237975_02() throws Exception {
        String source = "@import (less) \"theme\"";
        ExtCss3Lexer lexer = createLexer(source);
        assertANTLRToken("@import", Css3Lexer.IMPORT_SYM, lexer.nextToken());
        assertANTLRToken(" ", Css3Lexer.WS, lexer.nextToken());
        assertANTLRToken("(", Css3Lexer.LPAREN, lexer.nextToken());
        assertANTLRToken("less", Css3Lexer.IDENT, lexer.nextToken());
        assertANTLRToken(")", Css3Lexer.RPAREN, lexer.nextToken());
        assertANTLRToken(" ", Css3Lexer.WS, lexer.nextToken());
        assertANTLRToken("\"theme\"", Css3Lexer.STRING, lexer.nextToken());
        assertANTLRToken(null, Css3Lexer.EOF, lexer.nextToken());
    }
    
    //https://netbeans.org/bugzilla/show_bug.cgi?id=238864
    public void testIssue238864() throws Exception {
        FileObject testFile = getTestFile("testfiles/scss/large_empty.scss.txt");
        String source = testFile.asText();
         
        ExtCss3Lexer lexer = createLexer(source);
        
        assertANTLRToken(null, Css3Lexer.NL, lexer.nextToken());
        assertANTLRToken(null, Css3Lexer.EOF, lexer.nextToken());
    }


    public void testVariableLexing() {
        ExtCss3Lexer lexer = createLexer("h1{--test: 1em;margin: var(--test);}");
        assertANTLRToken("h1", Css3Lexer.IDENT, lexer.nextToken());
        assertANTLRToken("{", Css3Lexer.LBRACE, lexer.nextToken());
        assertANTLRToken("--test", Css3Lexer.VARIABLE, lexer.nextToken());
        assertANTLRToken(":", Css3Lexer.COLON, lexer.nextToken());
        assertANTLRToken(" ", Css3Lexer.WS, lexer.nextToken());
        assertANTLRToken("1em", Css3Lexer.EMS, lexer.nextToken());
        assertANTLRToken(";", Css3Lexer.SEMI, lexer.nextToken());
        assertANTLRToken("margin", Css3Lexer.IDENT, lexer.nextToken());
        assertANTLRToken(":", Css3Lexer.COLON, lexer.nextToken());
        assertANTLRToken(" ", Css3Lexer.WS, lexer.nextToken());
        assertANTLRToken("var", Css3Lexer.IDENT, lexer.nextToken());
        assertANTLRToken("(", Css3Lexer.LPAREN, lexer.nextToken());
        assertANTLRToken("--test", Css3Lexer.VARIABLE, lexer.nextToken());
        assertANTLRToken(")", Css3Lexer.RPAREN, lexer.nextToken());
        assertANTLRToken(";", Css3Lexer.SEMI, lexer.nextToken());
        assertANTLRToken("}", Css3Lexer.RBRACE, lexer.nextToken());
        assertANTLRToken(null, Css3Lexer.EOF, lexer.nextToken());
    }

     /**
    * @param expectedImage - use null if you do not want to check the image
    */
    private void assertANTLRToken(String expectedImage, int expectedType, org.antlr.runtime.Token token) {
        assertNotNull(token);

        assertEquals(
                String.format("Expected %s type, but was %s.",
                expectedType == Css3Lexer.EOF ? "<eof>" : Css3Parser.tokenNames[expectedType],
                token.getType() == Css3Lexer.EOF ? "<eof>" : Css3Parser.tokenNames[token.getType()]), expectedType, token.getType());

        if(expectedImage != null) {
            assertEquals(expectedImage, token.getText());
        }
    }

    private ExtCss3Lexer createLexer(String source) {
        return new ExtCss3Lexer(source);
    }
}
