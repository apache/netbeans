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
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * Test how many flyweight tokens gets created over a typical system headers
 * (copy of <istream> and <stdio.h> are used).
 *
 */
public class CppFlyTokensTestCase extends NbTestCase {
    private static final boolean TRACE = false;
    
    public CppFlyTokensTestCase(String testName) {
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

    public void testHCpp() throws Exception {
        File testJComponentFile = new File(getDataDir() + "/testfiles/istream.txt");
        FileReader r = new FileReader(testJComponentFile);
        int fileLen = (int)testJComponentFile.length();
        CharBuffer cb = CharBuffer.allocate(fileLen);
        r.read(cb);
        cb.rewind();
        String text = cb.toString();
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        if (TRACE) {
            System.err.println("Flyweight tokens: " + LexerTestUtilities.flyweightTokenCount(ts)
                    + "\nTotal tokens: " + ts.tokenCount()
                    + "\nFlyweight text length: " + LexerTestUtilities.flyweightTextLength(ts)
                    + "\nTotal text length: " + fileLen
                    + "\nDistribution: " + LexerTestUtilities.flyweightDistribution(ts)
            );
        }
        assertEquals(1606, LexerTestUtilities.flyweightTokenCount(ts));
        assertEquals(2822, LexerTestUtilities.flyweightTextLength(ts));
        assertEquals(2485, ts.tokenCount());         
    }
    
    public void testHC() throws Exception {
        File testJComponentFile = new File(getDataDir() + "/testfiles/stdio.h.txt");
        FileReader r = new FileReader(testJComponentFile);
        int fileLen = (int)testJComponentFile.length();
        CharBuffer cb = CharBuffer.allocate(fileLen);
        r.read(cb);
        cb.rewind();
        String text = cb.toString();
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageC());
        TokenSequence<?> ts = hi.tokenSequence();
        
        if (TRACE) {
            System.err.println("Flyweight tokens: " + LexerTestUtilities.flyweightTokenCount(ts)
                    + "\nTotal tokens: " + ts.tokenCount()
                    + "\nFlyweight text length: " + LexerTestUtilities.flyweightTextLength(ts)
                    + "\nTotal text length: " + fileLen
                    + "\nDistribution: " + LexerTestUtilities.flyweightDistribution(ts)
            );
        }
        
        assertEquals(2500, LexerTestUtilities.flyweightTokenCount(ts));
        assertEquals(4656, LexerTestUtilities.flyweightTextLength(ts));
        assertEquals(3577, ts.tokenCount());        
    }
    
    public void testC() throws Exception {
        File testJComponentFile = new File(getDataDir() + "/testfiles/istream.txt");
        FileReader r = new FileReader(testJComponentFile);
        int fileLen = (int)testJComponentFile.length();
        CharBuffer cb = CharBuffer.allocate(fileLen);
        r.read(cb);
        cb.rewind();
        String text = cb.toString();
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        
        LanguagePath path = LanguagePath.get(CppTokenId.languageCpp());
        path = LanguagePath.get(path, CppTokenId.languagePreproc());
        List<TokenSequence<?>> tsList = hi.tokenSequenceList(path, 0, Integer.MAX_VALUE);
        int fwTokenCount = 0;
        int tokenCount = 0;
        int fwTextLength = 0;
        List<Integer> distribution = new ArrayList<Integer>();
        for (TokenSequence<?> ts : tsList) {
            List<Integer> tsDistribution = LexerTestUtilities.flyweightDistribution(ts);
            if (TRACE) {
                System.err.println("Flyweight tokens: " + LexerTestUtilities.flyweightTokenCount(ts)
                        + "\nTotal tokens: " + ts.tokenCount()
                        + "\nFlyweight text length: " + LexerTestUtilities.flyweightTextLength(ts)
                        + "\nDistribution: " + tsDistribution
                );
            }
            fwTokenCount += LexerTestUtilities.flyweightTokenCount(ts);
            tokenCount += ts.tokenCount();
            fwTextLength += LexerTestUtilities.flyweightTextLength(ts);
            for (int i = 0; i < tsDistribution.size(); i++) {
                while (distribution.size() <= i) {
                    distribution.add(0);
                }
                distribution.set(i, distribution.get(i) + tsDistribution.get(i));
            }
            
        }

        if (TRACE) {
            System.err.println("Flyweight tokens: " + fwTokenCount
                    + "\nTotal tokens: " + tokenCount
                    + "\nFlyweight text length: " + fwTextLength
                    + "\nDistribution: " + distribution
            );
        }
        assertEquals(52, fwTokenCount);
        assertEquals(66, tokenCount);
        assertEquals(114, fwTextLength);
        
    }    
}
