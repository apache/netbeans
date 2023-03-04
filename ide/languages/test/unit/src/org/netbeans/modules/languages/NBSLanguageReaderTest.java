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

