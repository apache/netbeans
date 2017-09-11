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

package org.netbeans.modules.languages;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;

import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.languages.parser.LLSyntaxAnalyser;


/**
 *
 * @author Jan Jancura
 */
public class NBSLanguageReaderTest extends TestCase {
    
    public NBSLanguageReaderTest(String testName) {
        super(testName);
    }
    
    
    public void testOK () throws ParseException, IOException {
        NBSLanguageReader reader = NBSLanguageReader.create (
            "TOKEN:TAG:( '<' ['a'-'z']+ )\n" +
            "TOKEN:SYMBOL:( '>' | '=')\n" +
            "TOKEN:ENDTAG:( '</' ['a'-'z']+ )\n" +
            "TOKEN:ATTRIBUTE:( ['a'-'z']+ )\n" +
            "TOKEN:ATTR_VALUE:( '\\\"' [^ '\\n' '\\r' '\\\"' '\\uffff']+ '\\\"' )\n" +
            "TOKEN:VALUE:( '\\\"' [^ '\\n' '\\r' '\\\"']+ '\\\"' )\n" +
            "TOKEN:TEXT:( [^'<']+ )\n" +
            "\n" +
            "S = tags;\n" +
            "tags = (startTag | endTag | etext)*;\n" +
            "startTag = <TAG> (attribute)* ( <SYMBOL, '>'> | <SYMBOL, '/>'> );\n" +
            "endTag = <ENDTAG> <SYMBOL, '>'>; \n" +
            "attribute = <ATTRIBUTE>;\n" +
            "attribute = <ATTR_VALUE>; \n" +
            "attribute = <ATTRIBUTE> <SYMBOL,'='> <VALUE>; \n" +
            "etext = (<TEXT>)*;\n",
            "test.nbs", 
            "text/x-test"
        );
        LanguageImpl language = new LanguageImpl ("test/test", reader);
        language.read ();
        assertEquals (21, reader.getRules (language).size ());
    }
    
    public void testUnexpectedToken () throws IOException {
        try {
            LanguageImpl language = TestUtils.createLanguage (
                "TOKEN:TAG:( '<' ['a'-'z']+ )\n" +
                "TOKEN:SYMBOL:( '>' | '=')\n" +
                "TOKEN:ENDTAG:( '</' ['a'-'z']+ )\n" +
                "TOKEN:ATTRIBUTE:( ['a'-'z']+ )\n" +
                "TOKEN:ATTR_VALUE:( '\\\"' [^ '\\n' '\\r' '\\\"']+ '\\\"' )\n" +
                "TOKEN:VALUE:( '\\\"' [^ '\\n' '\\r' '\\\"']+ '\\\"' )\n" +
                "TOKEN:TEXT:( [^'<']+ )\n" +
                "\n" +
                "S = tags;\n" +
                "tags = (startTag | endTag | etext)*\n" +
                "startTag = <TAG> (attribute)* ( <SYMBOL, '>'> | <SYMBOL, '/>'> );\n" +
                "endTag = <ENDTAG> <SYMBOL, '>'>; \n" +
                "attribute = <ATTRIBUTE>;\n" +
                "attribute = <ATTR_VALUE>; \n" +
                "attribute = <ATTRIBUTE> <SYMBOL,'='> <VALUE>; \n" +
                "etext = (<TEXT>)*;\n"
            );
            language.read ();
            assert (false);
        } catch (ParseException ex) {
            assertEquals ("test.nbs 11,10: Unexpected token <operator,'='>. Expecting <operator,';'>", ex.getMessage ());
        }
    }
    
    public void testUnexpectedCharacter () throws IOException {
        try {
            LanguageImpl language = TestUtils.createLanguage (
                "TOKEN:TAG:( '<' ['a'-'z']+ )\n" +
                "TOKEN:SYMBOL:( '>' | '=')\n" +
                "TOKEN:ENDTAG:( '</' ['a'-'z']+ )\n" +
                "TOKEN:ATTRIBUTE:( ['a'-'z']+ )\n" +
                "TOKEN:ATTR_VALUE:( '\\\"' [^ '\\nw' '\\r' '\\\"']+ '\\\"' )\n" +
                "TOKEN:VALUE:( '\\\"' [^ '\\n' '\\r' '\\\"']+ '\\\"' )\n" +
                "TOKEN:TEXT:( [^'<']+ )\n" +
                "\n" +
                "S = tags;\n" +
                "tags = (startTag | endTag | etext)*;\n" +
                "startTag = <TAG> (attribute)* ( <SYMBOL, '>'> | <SYMBOL, '/>'> );\n" +
                "endTag = <ENDTAG> <SYMBOL, '>'>; \n" +
                "attribute = <ATTRIBUTE>;\n" +
                "attribute = <ATTR_VALUE>; \n" +
                "attribute = <ATTRIBUTE> <SYMBOL,'='> <VALUE>; \n" +
                "etext = (<TEXT>)*;\n"
            );
            language.read ();
            assert (false);
        } catch (ParseException ex) {
            assertEquals ("test.nbs 5,29: Unexpected character 'w'.", ex.getMessage ());
        }
    }
    
    public void testNoRule () throws IOException {
        try {
            NBSLanguageReader reader = NBSLanguageReader.create (
                "TOKEN:TAG:( '<' ['a'-'z']+ )\n" +
                "TOKEN:SYMBOL:( '>' | '=')\n" +
                "TOKEN:ENDTAG:( '</' ['a'-'z']+ )\n" +
                "TOKEN:ATTRIBUTE:( ['a'-'z']+ )\n" +
                "TOKEN:ATTR_VALUE:( a '\\\"' [^ '\\n' '\\r' '\\\"']+ '\\\"' )\n" +
                "TOKEN:VALUE:( '\\\"' [^ '\\n' '\\r' '\\\"']+ '\\\"' )\n" +
                "TOKEN:TEXT:( [^'<']+ )\n" +
                "\n" +
                "S = tags;\n" +
                "tags = (startTag | endTag | etext)*;\n" +
                "startTag = <TAG> (attribute)* ( <SYMBOL, '>'> | <SYMBOL, '/>'> );\n" +
                "endTag = <ENDTAG> <SYMBOL, '>'>; \n" +
                "attribute = <ATTRIBUTE>;\n" +
                "attribute = <ATTR_VALUE>; \n" +
                "attribute = <ATTRIBUTE> <SYMBOL,'='> <VALUE>; \n" +
                "etext = (<TEXT>)*;\n",
                "test.nbs", 
                "text/x-test"
            );
            reader.getTokenTypes ();
            assert (false);
        } catch (ParseException ex) {
            assertEquals ("test.nbs 5,20: Syntax error (nt: rePart, tokens: <identifier,'a'> <whitespace,' '>.", ex.getMessage ());
        }
    }
}

