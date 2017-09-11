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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import junit.framework.TestCase;

import org.netbeans.api.languages.CharInput;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.TokenInput;
import org.netbeans.modules.languages.LanguageImpl;
import org.netbeans.modules.languages.TestLanguage;
import org.netbeans.modules.languages.TestUtils;


/**
 *
 * @author Jan Jancura
 */
public class AnalyserTest extends TestCase {
    
    public AnalyserTest (String testName) {
        super (testName);
    }

    private static String mimeType = "text/test";

    public void test1 () throws ParseException {
        TestLanguage language = new TestLanguage ();
        language.addToken (0, "identifier");
        language.addToken (1, "operator");
        
        
        language.addRule ("S", Arrays.asList (new Object[] {
            ASTToken.create (language, "identifier", null, 0, "identifier".length (), null), 
            "S"
        }));
        language.addRule ("S", Arrays.asList (new Object[] {
            ASTToken.create (language, "operator", "{", 0, "operator".length (), null), 
            "S", 
            ASTToken.create (language, "operator", "}", 0, "operator".length (), null), 
            "S"
        }));
        language.addRule ("S", Arrays.asList (new Object[] {
        }));
        
        //PetraTest.print (Petra.first (r, 5));
        TokenInput input = TokenInputUtils.create (new ASTToken[] {
            ASTToken.create (language, "identifier", "asd", 0, "asd".length (), null),
            ASTToken.create (language, "identifier", "ss", 0, "ss".length (), null),
            ASTToken.create (language, "operator", "{", 0, "{".length (), null),
            ASTToken.create (language, "identifier", "a", 0, "a".length (), null),
            ASTToken.create (language, "operator", "{", 0, "{".length (), null),
            ASTToken.create (language, "operator", "}", 0, "}".length (), null),
            ASTToken.create (language, "identifier", "asd", 0, "asd".length (), null),
            ASTToken.create (language, "operator", "}", 0, "}".length (), null),
        });
        assertNotNull (language.getAnalyser ().read (input, false, new ArrayList<SyntaxError> (), new boolean [] {false}));
        assert (input.eof ());
    }

    public void test4 () throws ParseException, IOException {
        LanguageImpl language = TestUtils.createLanguage (
            "TOKEN:operator:( '{' | '}' | '.' | ',' | '(' | ')' )" +
            "TOKEN:separator:( ';' )" +
            "TOKEN:whitespace:( ['\\n' '\\r' ' ' '\\t']+ )" +
            "TOKEN:keyword:( 'void' | 'public' )" +
            "TOKEN:identifier:( ['a'-'z' 'A'-'Z' '0'-'9' '_' '-' '$' '%']+ )" +
            "SKIP:whitespace " +
            "S = variable S;" +
            "S = ;" +
            "variable = modifiers <keyword> <identifier> <separator,';'>;" +
            "variable = modifiers <identifier> <identifier> <separator,';'>;" +
            "modifiers = <keyword,'public'> modifiers;" +
            "modifiers = ;"
        );
        language.read ();
        CharInput input = new StringInput (
            "void a;" +
            "public ii name;"
        );
        ASTNode n = language.getAnalyser ().read (
            TokenInputUtils.create (
                language,
                language.getParser (),
                input
            ),
            false,
            new ArrayList<SyntaxError> (), 
            new boolean[] {false}
        );
        assertNotNull (n);
        assertTrue (input.eof ());
    }
    
    public void test5 () throws ParseException, IOException {
        LanguageImpl language = TestUtils.createLanguage (
            "TOKEN:TAG:( '<' ['a'-'z']+ )" +
            "TOKEN:SYMBOL:( '>' | '=')" +
            "TOKEN:ENDTAG:( '</' ['a'-'z']+ )" +
            "TOKEN:ATTRIBUTE:( ['a'-'z']+ )" +
            "TOKEN:ATTR_VALUE:( '\\\"' [^ '\\n' '\\r' '\\\"']+ '\\\"' )" +
            "TOKEN:VALUE:( '\\\"' [^ '\\n' '\\r' '\\\"']+ '\\\"' )" +
            "TOKEN:TEXT:( [^'<']+ )" +
            "S = tags;" +
            "tags = (startTag | endTag | etext)*;" + 
            "startTag = <TAG> (attribute)* ( <SYMBOL, '>'> | <SYMBOL, '/>'> );" + 
            "endTag = <ENDTAG> <SYMBOL, '>'>;" + 
            "attribute = <ATTRIBUTE>;" + 
            "attribute = <ATTR_VALUE>;" + 
            "attribute = <ATTRIBUTE> <SYMBOL,'='> <VALUE>;" + 
            "etext = (<TEXT>)*;"
        );
        language.read ();
        CharInput input = new StringInput (
            "<a></a>"
        );
        ASTNode n = language.getAnalyser ().read (
            TokenInputUtils.create (
                language,
                language.getParser (),
                input
            ),
            false,
            new ArrayList<SyntaxError> (), 
            new boolean[] {false}
        );
        //S ystem.out.println(n.print ());
        assertTrue (input.eof ());
        assertEquals (1, n.getChildren ().size ());
        assertEquals ("S", n.getNT ());
        n = (ASTNode) n.getChildren ().get (0);
        assertEquals (2, n.getChildren ().size ());
        assertEquals ("tags", n.getNT ());
        assertEquals ("startTag", ((ASTNode) n.getChildren ().get (0)).getNT ());
        assertEquals ("endTag", ((ASTNode) n.getChildren ().get (1)).getNT ());
        n = (ASTNode) n.getChildren ().get (0);
        assertEquals (2, n.getChildren ().size ());
        assertEquals ("TAG", ((ASTToken) n.getChildren ().get (0)).getTypeName ());
        assertEquals ("SYMBOL", ((ASTToken) n.getChildren ().get (1)).getTypeName ());
    }
    
    private void print (List l, String indent) {
        Iterator it = l.iterator ();
        while (it.hasNext ()) {
            Object next = it.next ();
            System.out.println (indent + next);
            if (next instanceof ASTToken) continue;
            print ((List) it.next (), indent + "  ");
        }
    }
}
