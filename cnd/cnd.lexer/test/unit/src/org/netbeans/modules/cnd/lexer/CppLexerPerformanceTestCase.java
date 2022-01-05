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
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.ModificationTextDocument;

/**
 * This is a test that scans the source that is a copy of <stdio.h> and <istream>
 * and reports the times of creation of all the tokens. It exists
 * mainly because it's easy to run profiler over it.
 * 
 * based on JavaLexerPerformanceTest
 */
public class CppLexerPerformanceTestCase extends NbTestCase {
    
    public CppLexerPerformanceTestCase(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        // Disable for performance testing
        // LexerTestUtilities.setTesting(true);
    }

    @Override
    protected void tearDown() throws java.lang.Exception {
    }

    @Override
    protected int timeOut() {
        return 500000;
    }
    
    private static final int PREPARE_COUNT = 10;

    public void testAString() throws Exception {
        String text = readFile();
        // Possibly some extra prepare runs
        int prepareTokenCount = 0;
        for (int i = 0; i < PREPARE_COUNT; i++) { prepareTokenCount += prepareTestAString(text); }

        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageC());
        TokenSequence<?> ts = hi.tokenSequence();
        long tm = System.currentTimeMillis();
        // Force all the tokens to be initialized
        int tokenCount = 0;
        while (ts.moveNext()) { tokenCount++; }
        tm = System.currentTimeMillis() - tm;
        System.err.println("TH over String: " + tokenCount + " tokens created in " + tm
                + " ms over text with " + text.length() + " chars; prepareTokenCount="
                + prepareTokenCount + ".");
    }
    
    public void testDocument() throws Exception {
        String text = readFile();
        // Possibly some extra prepare runs
        int prepareTokenCount = 0;
        for (int i = 0; i < PREPARE_COUNT; i++) { prepareTokenCount += prepareTestDocument(text); }

        // Create the document and token hierarchy again and measure time
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, CppTokenId.languageC());
        final TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        long tm = System.currentTimeMillis();
        final AtomicInteger tokenCount = new AtomicInteger(0);
        // Force all the tokens to be initialized
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                TokenSequence<?> ts = hi.tokenSequence();
                while (ts.moveNext()) {
                    tokenCount.incrementAndGet();
                }
            }
        };
        doc.render(runnable);
        tm = System.currentTimeMillis() - tm;
        System.err.println("TH over Swing Document: " + tokenCount.intValue() + " tokens created in " + tm
                + " ms over document with " + doc.getLength() + " chars; prepareTokenCount="
                + prepareTokenCount + ".");
    }
    
    private int prepareTestAString(String text) throws Exception {
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageC());
        TokenSequence<?> ts = hi.tokenSequence();
        int tokenCount = 0;
        while (ts.moveNext()) { tokenCount++; }
        return tokenCount;
    }
    
    private int prepareTestDocument(String text) throws Exception {
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, CppTokenId.languageC());
        final TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        final AtomicInteger tokenCount = new AtomicInteger(0);
        // Force all the tokens to be initialized
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                TokenSequence<?> ts = hi.tokenSequence();
                while (ts.moveNext()) {
                    tokenCount.incrementAndGet();
                }
            }
        };
        doc.render(runnable);
        return tokenCount.intValue();
    }

    private String readFile() throws Exception {
        File testJComponentFile = new File(getDataDir() + "/testfiles/apr_portable.h.txt");
        FileReader r = new FileReader(testJComponentFile);
        try {
            int fileLen = (int)testJComponentFile.length();
            CharBuffer cb = CharBuffer.allocate(fileLen);
            r.read(cb);
            cb.rewind();
            return cb.toString();
        } finally {
            r.close();
        }
    }
    
}
