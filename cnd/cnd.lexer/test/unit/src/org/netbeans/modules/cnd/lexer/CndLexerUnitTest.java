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

package org.netbeans.modules.cnd.lexer;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestSuite;

/**
 *
 */
public class CndLexerUnitTest extends NbTestSuite {

    public CndLexerUnitTest() {
        super("C/C++ Lexer");
        addTestSuite(CppFlyTokensTestCase.class);
        addTestSuite(CppLexerBatchTestCase.class);
        addTestSuite(CppLexerPerformanceTestCase.class);
        addTestSuite(CppStringLexerTestCase.class);
        addTestSuite(DoxygenLexerTestCase.class);
        addTestSuite(PreprocLexerTestCase.class);
        addTestSuite(EscapeLineTestCase.class);
        addTestSuite(FortranFlyTokensTestCase.class);
        addTestSuite(FortranLexerBatchTestCase.class);
        addTestSuite(FortranLexerPerformanceTestCase.class);
    }

    public static Test suite() {
        TestSuite suite = new CndLexerUnitTest();
        return suite;
    }

    /**
     *
     * @param ts token stream to dump
     * @param tsName name of ts parameter as is in call function,
     *      i.e. "ep" in usage CndLexerUnitTest.dumpTokens(ep, "ep");
     */
    public static void dumpTokens(TokenSequence<?> ts, String tsName) {
        if (ts != null) {
            while (ts.moveNext()) {
                Token<?> token = ts.token();
                String tokIDName = token.id().getClass().getName();
                System.out.println("LexerTestUtilities.assertNextTokenEquals(" + tsName + ", " + tokIDName + "." + token.id() + ", \"" + escapeText(token.text()) + "\");");
            }
        }
        System.out.println("\nassertFalse(\"No more tokens\", " + tsName + ".moveNext());");
        System.out.println("------");
    }
    
    private static String escapeText(CharSequence in) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < in.length(); i++) {
            char ch = in.charAt(i);
            switch (ch) {
                case '\r':
                    out.append("\\r");
                    break;
                case '\n':
                    out.append("\\n");
                    break;
                case '\"':
                    out.append("\\\"");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                default:
                    out.append(ch);
            }
        }
        return out.toString();
    }    
}
