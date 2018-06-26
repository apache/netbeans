/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.languages.apacheconf.lexer;

import java.io.File;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ApacheConfLexerTest extends ApacheConfLexerTestBase {

    public ApacheConfLexerTest(String name) {
        super(name);
    }

    public void testTest1() throws Exception {
        performTest("test1");
    }

    public void testTest2() throws Exception {
        performTest("test2");
    }

    public void testTest3() throws Exception {
        performTest("test3");
    }

    public void testTest4() throws Exception {
        performTest("test4");
    }

    public void testTest5() throws Exception {
        performTest("test5");
    }

    public void testTest6() throws Exception {
        performTest("test6");
    }

    public void testTest7() throws Exception {
        performTest("test7");
    }

    public void testTest8() throws Exception {
        performTest("test8");
    }

    public void testTest9() throws Exception {
        performTest("test9");
    }

    public void testTest10() throws Exception {
        performTest("test10");
    }

    public void testNegativeFloat() throws Exception {
        performTest("negativeFloat");
    }

    public void testIssue215891() throws Exception {
        performTest("issue215891");
    }

    public void testIssue2236943() throws Exception {
        performTest("issue236943");
    }

    @Override
    protected String getTestResult(String filename) throws Exception {
        String content = ApacheConfLexerUtils.getFileContent(new File(getDataDir(), "testfiles/lexer/" + filename + ".conf"));
        TokenSequence<?> ts = ApacheConfLexerUtils.seqForText(content, ApacheConfTokenId.language());
        return createResult(ts);
    }

    private String createResult(TokenSequence<?> ts) throws Exception {
        StringBuilder result = new StringBuilder();
        while (ts.moveNext()) {
            TokenId tokenId = ts.token().id();
            CharSequence text = ts.token().text();
            result.append("token #");
            result.append(ts.index());
            result.append(" ");
            result.append(tokenId.name());
            result.append(" ");
            result.append(ApacheConfLexerUtils.replaceLinesAndTabs(text.toString()));
            result.append("\n");
        }
        return result.toString();
    }

}
