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

package org.netbeans.lib.java.lexer;

import java.io.File;
import java.io.FileReader;
import java.nio.CharBuffer;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.ModificationTextDocument;

/**
 * This is a test that scans the source that is a copy of javax.swing.JComponent
 * and reports the times of creation of all the tokens. It exists
 * mainly because it's easy to run profiler over it.
 *
 * @author mmetelka
 */
public class JavaLexerPerformanceTest extends NbTestCase {
    
    public JavaLexerPerformanceTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        // Disable for performance testing
        // LexerTestUtilities.setTesting(true);
    }

    protected void tearDown() throws java.lang.Exception {
    }
    
    private static final int PREPARE_COUNT = 10;

    public void testAString() throws Exception {
        String text = readJComponentFile();
        // Possibly some extra prepare runs
        int prepareTokenCount = 0;
        for (int i = 0; i < PREPARE_COUNT; i++) { prepareTokenCount += prepareTestAString(text); }

        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
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
        String text = readJComponentFile();
        // Possibly some extra prepare runs
        int prepareTokenCount = 0;
        for (int i = 0; i < PREPARE_COUNT; i++) { prepareTokenCount += prepareTestDocument(text); }

        // Create the document and token hierarchy again and measure time
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, JavaTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<?> ts = hi.tokenSequence();
        long tm = System.currentTimeMillis();
        int tokenCount = 0;
        // Force all the tokens to be initialized
        while (ts.moveNext()) { tokenCount++; }
        tm = System.currentTimeMillis() - tm;
        System.err.println("TH over Swing Document: " + tokenCount + " tokens created in " + tm
                + " ms over document with " + doc.getLength() + " chars; prepareTokenCount="
                + prepareTokenCount + ".");
    }
    
    private int prepareTestAString(String text) throws Exception {
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        int tokenCount = 0;
        while (ts.moveNext()) { tokenCount++; }
        return tokenCount;
    }
    
    private int prepareTestDocument(String text) throws Exception {
        ModificationTextDocument doc = new ModificationTextDocument();
        doc.insertString(0, text, null);
        doc.putProperty(Language.class, JavaTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<?> ts = hi.tokenSequence();
        int tokenCount = 0;
        while (ts.moveNext()) { tokenCount++; }
        return tokenCount;
    }

    private String readJComponentFile() throws Exception {
        File testJComponentFile = new File(getDataDir() + "/testfiles/JComponent.java.txt");
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
