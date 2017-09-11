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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.languages.parser;

import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.CharInput;
import org.netbeans.api.languages.ParseException;
import java.util.ArrayList;
import java.util.List;
import junit.framework.*;

import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.TestLanguage;
import org.netbeans.modules.languages.TokenType;
import org.netbeans.modules.languages.parser.Parser.Cookie;


/**
 *
 * @author Jan Jancura
 */
public class ParserTest extends TestCase {
    
    public ParserTest (String testName) {
        super (testName);
    }
    
//    public void testRead123 () throws ParseException {
//        Parser abc = Parser.create ("abc", "abc");
//        Parser axd = Parser.create ("a.d", "axd");
//        Parser p = abc.merge (axd);
//        Input in = Input.create (
//            "abd"
//        );
//        assertEquals ("state: " + p.getState () + " text: " + in, "axd", p.read (p.getState (), in));
//        assertEquals (0, in.current ());
//        assertEquals (true, in.eof ());
//    }
    
//    public void testRead () throws ParseException {
//        Parser abc = Parser.create ("(abc)*|(ab)*", "abc");
//        Parser abcd = Parser.create ("abcd", "abcd");
//        Parser whitespace = Parser.create ("[ \n\r\t]+", "whitespace");
//        Parser p = abc.merge (abcd).append (whitespace).star ();
//        Input in = Input.create (
//            "abab  abcd abcabcd abcd"
//        );
//        assertEquals ("state: " + p.getState () + " text: " + in, "abc", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "whitespace", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "abcd", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "whitespace", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "abc", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, null, p.read (p.getState (), in));
//        in.read ();
//        assertEquals ("state: " + p.getState () + " text: " + in, "whitespace", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "abcd", p.read (p.getState (), in));
//        assertEquals (0, in.current ());
//        assertEquals (true, in.eof ());
//    }
//    
//    public void testRead1 () throws ParseException {
//        Parser value = Parser.create ("[^\n]+", "VALUE");
//        Parser eol = Parser.create ("[\n]", "EOL");
//        Parser p = value.merge (eol).star ();
//        Input in = Input.create (
//            "test:ttt\n" +
//            "test\n" +
//            "#comment"
//        );
//        assertEquals ("state: " + p.getState () + " text: " + in, "VALUE", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "EOL", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "VALUE", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "EOL", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "VALUE", p.read (p.getState (), in));
//        assertEquals (0, in.current ());
//        assertEquals (true, in.eof ());
//    }
//    
//    public void testReadManifest () throws ParseException {
//        Parser p = Parser.create ();
//        p.add ("LINE_COMMENT", "#[^\n\r]*");
//        p.add ("EOL", "[\n\r][\n\r]?");
//        p.add (p.DEFAULT_STATE, "NAME", "[^:\n\r]*", "STATE_COLON");
//        p.add ("STATE_COLON", "COLON", ":", "STATE_VALUE");
//        p.add ("STATE_COLON", "EOL", "[\n\r][\n\r]?", p.DEFAULT_STATE);
//        p.add ("STATE_VALUE", "VALUE", "[^\n\r]*", p.DEFAULT_STATE);
//        
//        Input in = Input.create (
//            "#comment\n" +
//            "test:ttt\n" +
//            "#comment\n" +
//            "test\n" +
//            "t:t"
//        );
//        S ystem.out.println(p);
//        assertEquals ("state: " + p.getState () + " text: " + in, "LINE_COMMENT", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "EOL", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "NAME", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "COLON", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "VALUE", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "EOL", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "LINE_COMMENT", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "EOL", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "NAME", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "EOL", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "NAME", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "COLON", p.read (p.getState (), in));
//        assertEquals ("state: " + p.getState () + " text: " + in, "VALUE", p.read (p.getState (), in));
//        assertEquals (0, in.current ());
//        assertEquals (true, in.eof ());
//    }
//    
    
    
    private static TestLanguage create (String[] l) throws ParseException {
        TestLanguage language = new TestLanguage ();
        List<TokenType> rules = new ArrayList<TokenType> ();
        int i, k = l.length;
        for (i = 0; i < k; i+=4) {
            String tokenName = (String) l [i + 1];
            String pattern = (String) l [i + 2];
            language.addToken (
                (int) i / 4,
                tokenName,
                Pattern.create (pattern),
                (String) l [i],
                (String) l [i + 3],
                (int) i / 4,
                null
            );
        }
        return language;
    }
    
    public void testReadJava () throws ParseException {
        TestLanguage language = create (new String[] {
            null, "comment", "'/*'-'*/'", null,
            null, "comment", "'//'[^'\n''\r']*", null,
            null, "keyword", "'if'|'else'|'public'|'static'|'private'|'protected'|'class'|'extends'|'import'|'package'|'try'|'int'|'false'|'true'|'void'", null,
            null, "operator", "['\\\\''/''*''-''+''.'',''=''{''}''('')''!''@''#''$''%''^''&''~''|'';']", null,
            null, "string", "'\\\"'[^'\\\"']*'\\\"'", null,
            null, "char", "'\\\''[^'\\\'']'\\\''", null,
            null, "number", "['0'-'9']['0'-'9''.']*", null,
            null, "whitespace", "[' ''\t''\n''\r']+", null,
            null, "identifier", "['a'-'z''A'-'Z'][^' ''\\t''\\n''\\r''/''*''-''+''.'',''=''{''}''('')''!''@''#''$''%''^''&''~''|''\\\\'';']*", null
        });
        
        CharInput in = new StringInput (
            "/** dsfsf\n" +
            " asd as * asdf */ " +
            "package ifa.vv.b;\n" +
            "import a.v;\n" +
            "\n" +
            "public class Asd extends sd {\n" +
            "    void main (String[] s) {\n" +
            "        return \"asd\" + \'a\' + 123;\n" +
            "    }\n" +
            "}"
        );
        Cookie c = new MyCookie ();
        Parser parser = language.getParser ();
        assertEquals ("state: " + c.getState () + " text: " + in, "comment", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertEquals ("state: " + c.getState () + " text: " + in, "whitespace", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertEquals ("state: " + c.getState () + " text: " + in, "keyword", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertEquals ("state: " + c.getState () + " text: " + in, "whitespace", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertEquals ("state: " + c.getState () + " text: " + in, "identifier", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertEquals ("state: " + c.getState () + " text: " + in, "operator", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertEquals ("state: " + c.getState () + " text: " + in, "identifier", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertEquals ("state: " + c.getState () + " text: " + in, "operator", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertEquals ("state: " + c.getState () + " text: " + in, "identifier", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertEquals ("state: " + c.getState () + " text: " + in, "operator", ((ASTToken) parser.read (c, in, language)).getTypeName ());
    }
    
    public void testReadHtml () throws ParseException {
        TestLanguage language = create (new String[] {
            null, "TEXT", "[^'<']*", null,
            null, "COMMENT", "'<!'-'-->'", null,
            Parser.DEFAULT_STATE, "ELEMENT", "'<'[^' ''>''\\t''\\n''\\r']+", "IN_ELEMENT",
            null, "END_ELEMENT", "'</'[^' ''>''\\t''\\n''\\r']+'>'", null,
            "IN_ELEMENT", "ELEMENT", "'/'?'>'", Parser.DEFAULT_STATE,
            "IN_ELEMENT", "ATTRIBUTE_NAME", "[^' ''>''=''\\t''\\n''\\r']+", "IN_ATTRIBUTE",
            "IN_ATTRIBUTE", "OPERATOR", "'='", "IN_ATTRIBUTE2",
            "IN_ATTRIBUTE2", "ATTRIBUTE_VALUE", "'\\\"'[^'\\\"']*'\\\"'", "IN_ELEMENT",
        
            "IN_ATTRIBUTE", "ELEMENT", "'/'?'>'", Parser.DEFAULT_STATE,
            "IN_ATTRIBUTE", "ATTRIBUTE_NAME", "[^' ''=''\\t''\\n''\\r']+", "IN_ATTRIBUTE",
            "IN_ELEMENT", "WHITESPACE", "[' ''\\t''\\n''\\r']*", "IN_ELEMENT",
            "IN_ATTRIBUTE", "WHITESPACE", "[' ''\\t''\\n''\\r']*", "IN_ATTRIBUTE",
            "IN_ATTRIBUTE2", "WHITESPACE", "[' ''\\t''\\n''\\r']*", "IN_ATTRIBUTE2"
        });
        
        CharInput in = new StringInput (
            "<test a=\"asz\"> \n" +
            " \" sdf\" = sdf" +
            "<!-- NewPage" +
            "  ?!?@? -- > sdf -> -->\n" +
            "<HTML><\\HTML> <test t>"
        );
        Cookie c = new MyCookie ();
        Parser parser = language.getParser ();
        assertEquals ("state: " + c + " text: " + in, "ELEMENT", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertEquals ("state: " + c + " text: " + in, "WHITESPACE", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertEquals ("state: " + c + " text: " + in, "ATTRIBUTE_NAME", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertEquals ("state: " + c + " text: " + in, "OPERATOR", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertEquals ("state: " + c + " text: " + in, "ATTRIBUTE_VALUE", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertEquals ("state: " + c + " text: " + in, "ELEMENT", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertEquals ("state: " + c + " text: " + in, "TEXT", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertEquals ("state: " + c + " text: " + in, "COMMENT", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertEquals ("state: " + c + " text: " + in, "TEXT", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertEquals ("state: " + c + " text: " + in, "ELEMENT", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertEquals ("state: " + c + " text: " + in, "ELEMENT", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertEquals ("state: " + c + " text: " + in, "ELEMENT", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertEquals ("state: " + c + " text: " + in, "ELEMENT", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertEquals ("state: " + c + " text: " + in, "TEXT", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertEquals ("state: " + c + " text: " + in, "ELEMENT", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertEquals ("state: " + c + " text: " + in, "WHITESPACE", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertEquals ("state: " + c + " text: " + in, "ATTRIBUTE_NAME", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertEquals ("state: " + c + " text: " + in, "ELEMENT", ((ASTToken) parser.read (c, in, language)).getTypeName ());
        assertTrue (in.eof ());
    }
    
    private static class MyCookie implements Parser.Cookie {
        
        private int state = -1;
        
        public int getState () {
            return state;
        }

        public void setState (int state) {
            this.state = state;
        }
    
        public void setProperties (Feature properties) {
        }
    }
}
