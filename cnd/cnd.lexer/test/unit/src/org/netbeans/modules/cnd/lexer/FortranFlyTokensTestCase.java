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

import java.io.File;
import java.io.FileReader;
import java.nio.CharBuffer;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CndLexerUtilities.FortranFormat;
import org.netbeans.cnd.api.lexer.FortranTokenId;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * Test how many flyweight tokens gets created over a typical system headers
 *
 */
public class FortranFlyTokensTestCase extends NbTestCase {

    public FortranFlyTokensTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    @Override
    protected void tearDown() throws java.lang.Exception {
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    protected InputAttributes getLexerAttributes() {
        InputAttributes lexerAttrs = new InputAttributes();
        lexerAttrs.setValue(FortranTokenId.languageFortran(), CndLexerUtilities.FORTRAN_MAXIMUM_TEXT_WIDTH, 132, true);
        lexerAttrs.setValue(FortranTokenId.languageFortran(), CndLexerUtilities.FORTRAN_FREE_FORMAT, FortranFormat.FREE, true);
        return lexerAttrs;
    }

    public void testF90() throws Exception {
        File testJComponentFile = new File(getDataDir() + "/testfiles/testinput.f.txt");
        FileReader r = new FileReader(testJComponentFile);
        int fileLen = (int)testJComponentFile.length();
        CharBuffer cb = CharBuffer.allocate(fileLen);
        r.read(cb);
        cb.rewind();
        String text = cb.toString();
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes());
        TokenSequence<?> ts = hi.tokenSequence();

        System.err.println("Flyweight tokens: " + LexerTestUtilities.flyweightTokenCount(ts)
                + "\nTotal tokens: " + ts.tokenCount()
                + "\nFlyweight text length: " + LexerTestUtilities.flyweightTextLength(ts)
                + "\nTotal text length: " + fileLen
                + "\nDistribution: " + LexerTestUtilities.flyweightDistribution(ts)
        );
        ts.moveIndex(0);

        assertEquals(140, LexerTestUtilities.flyweightTokenCount(ts));
        assertEquals(806, LexerTestUtilities.flyweightTextLength(ts));
        assertEquals(559, ts.tokenCount());
    }

}
