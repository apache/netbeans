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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
