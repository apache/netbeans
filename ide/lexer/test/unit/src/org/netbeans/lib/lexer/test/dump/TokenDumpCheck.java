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

package org.netbeans.lib.lexer.test.dump;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.lexer.BatchTokenList;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * Check whether generated token dump corresponds to the one read from a file.
 *
 * @author mmetelka
 */
public final class TokenDumpCheck {
    
    public static void checkTokenDump(NbTestCase test, String relFilePath,
    Language<?> language) throws Exception {
        // Request lookaheads and states maintaining
        boolean origMaintainLAState = BatchTokenList.isMaintainLAState();
        BatchTokenList.setMaintainLAState(true);
        try {
            File wholeInputFile = new File(test.getDataDir(), relFilePath);
            if (!wholeInputFile.exists()) {
                NbTestCase.fail("File " + wholeInputFile + " not found.");
            }
            String wholeInput = readFile(test, wholeInputFile);
            // Scan for EOF markers and create multiple char sequences
            TokenHierarchy<?> hi = TokenHierarchy.create(wholeInput, TokenDumpTokenId.language());
            TokenSequence<TokenDumpTokenId> ts = hi.tokenSequence(TokenDumpTokenId.language());
            boolean afterEOF = true; // Ignore newlines when after eof
            boolean newline = false;
            int textStartIndex = 0;
            List<String> inputs = new ArrayList<String>();
            List<String> testNames = new ArrayList<String>();
            // Estimate 4 subtests equally long
            StringBuilder inputBuffer = new StringBuilder(wholeInput.length() / 4);
            String testName = "<Unnamed test>";
            while (ts.moveNext()) {
                Token<TokenDumpTokenId> token = ts.token();
                switch (token.id()) {
                    case NEWLINE:
                        if (newline) { // Was empty line
                            inputBuffer.append('\n');
                        }
                        if (!afterEOF) {
                            newline = true;
                        }
                        break;

                    case TEST_NAME:
                        testName = token.text().toString();
                        ts.moveNext(); // skip newline (might be false for EOF)
                        newline = false;
                        break;

                    case EOF_VIRTUAL:
                        // All except newline-eof_mark-newline
                        ts.moveNext(); // skip newline (might be false for EOF)
                        newline = false;
                        inputs.add(inputBuffer.toString());
                        inputBuffer.setLength(0);
                        testNames.add(testName);
                        testName = "<Unnamed test>";
                        afterEOF = true;
                        break;

                    case TEXT:
                        if (newline) {
                            inputBuffer.append('\n');
                            newline = false;
                        }
                        inputBuffer.append(token.text());
                        afterEOF = false;
                        break;

                    default:
                        if (TokenDumpTokenId.isCharLiteral(token.id())) {
                            Character ch = (Character)token.getProperty(TokenDumpTokenId.UNICODE_CHAR_TOKEN_PROPERTY);
                            assert (ch != null);
                            inputBuffer.append(ch);
                        } else {
                            throw new IllegalStateException("Unknown token id=" + token.id());
                        }
                        ts.moveNext(); // skip newline (might be false for EOF)
                        newline = false;
                        afterEOF = false;
                        
                }
            }
            inputs.add(inputBuffer.toString());
            testNames.add(testName);

            // Check whether token dump file exists
            // Try to remove "/build/" from the dump file name if it exists.
            // Otherwise give a warning.
            File tokenDescInputFile = new File(test.getDataDir(), relFilePath + ".tokens.txt");
            String tokenDescInputFilePath = tokenDescInputFile.getAbsolutePath();
            boolean replaced = false;
            if (tokenDescInputFilePath.indexOf("/build/test/") != -1) {
                tokenDescInputFilePath = tokenDescInputFilePath.replace("/build/test/", "/test/");
                replaced = true;
            }
            if (!replaced && tokenDescInputFilePath.indexOf("/test/work/sys/") != -1) {
                tokenDescInputFilePath = tokenDescInputFilePath.replace("/test/work/sys/", "/test/unit/");
                replaced = true;
            }
            if (!replaced) {
                System.err.println("Warning: Attempt to use tokens dump file " +
                        "from sources instead of the generated test files failed.\n" +
                        "Patterns '/build/test/' or '/test/work/sys/' not found in " + tokenDescInputFilePath
                );
            }
            tokenDescInputFile = new File(tokenDescInputFilePath);
            
            String tokenDescInput = null;
            if (tokenDescInputFile.exists()) {
                tokenDescInput = readFile(test, tokenDescInputFile);
            }
            TokenDescCompare tdc = new TokenDescCompare(tokenDescInput, tokenDescInputFile);

            // Check individual token descriptions
            StringBuilder tokenDesc = new StringBuilder(40);
            for (int i = 0; i < inputs.size(); i++) {
                String input = inputs.get(i);
                testName = testNames.get(i);
                tdc.setTestName(testName);
                TokenHierarchy<?> langHi = TokenHierarchy.create(input, language);
                TokenSequence<?> langTS = langHi.tokenSequence();
                tdc.compareLine(testName, -1);
                while (langTS.moveNext()) {
                    // Debug the token
                    Token<?> token = langTS.token();
                    tokenDesc.append(token.id().name());
                    int spaceCount = 14 - token.id().name().length();
                    while (--spaceCount >= 0) {
                        tokenDesc.append(' ');
                    }
                    tokenDesc.append("  \"");
                    tokenDesc.append(TokenUtilities.debugText(token.text()));
                    tokenDesc.append('"');
                    int lookahead = LexerTestUtilities.lookahead(langTS);
                    if (lookahead > 0) {
                        tokenDesc.append(", la=");
                        tokenDesc.append(lookahead);
                    }
                    Object state = LexerTestUtilities.state(langTS);
                    if (state != null) {
                        tokenDesc.append(", st=");
                        tokenDesc.append(state);
                    }
                    tdc.compareLine(tokenDesc, langTS.index());
                    tokenDesc.setLength(0);
                }
                tdc.compareLine("----- EOF -----\n", -1);
            }
            tdc.finish(); // Write token desc file if necessary

        } finally {
            BatchTokenList.setMaintainLAState(origMaintainLAState);
        }
    }
    
    private static String readFile(NbTestCase test, File f) throws Exception {
        FileReader r = new FileReader(f);
        int fileLen = (int)f.length();
        CharBuffer cb = CharBuffer.allocate(fileLen);
        r.read(cb);
        cb.rewind();
        return cb.toString();
    }
    
    private static final class TokenDescCompare implements CharSequence {
        
        private String input;
        
        private int inputIndex;
        
        private int textLength;
        
        private StringBuilder output;
        
        private File outputFile;
        
        private int[] lineBoundsIndexes = new int[2 * 3]; // last three lines [start,end]
        
        private String testName;
        
        TokenDescCompare(String input, File outputFile) {
            this.input = input;
            this.outputFile = outputFile;
            Arrays.fill(lineBoundsIndexes, -1);
            if (input == null)
                output = new StringBuilder(100);
        }
        
        public void compareLine(CharSequence text, int tokenIndex) {
            if (input != null) {
                textLength = text.length();
                if (input.length() - inputIndex < textLength || !TokenUtilities.equals(text, this)) {
                    StringBuilder msg = new StringBuilder(100);
                    msg.append("\nDump file ");
                    msg.append(outputFile);
                    msg.append(":\n");
                    msg.append(testName);
                    msg.append(":\n");
                    if (tokenIndex >= 0) {
                        msg.append("Invalid token description in dump file (tokenIndex=");
                        msg.append(tokenIndex);
                        msg.append("):");
                    } else {
                        msg.append("Invalid text in dump file:");
                    }
                    msg.append("\n    ");
                    msg.append(input.subSequence(inputIndex, findEOL(inputIndex)));
                    msg.append("\nExpected:\n    ");
                    msg.append(text);
                    msg.append("\nPrevious context:\n");
                    for (int i = 0; i < lineBoundsIndexes.length; i += 2) {
                        int start = lineBoundsIndexes[i];
                        if (start != -1) {
                            msg.append("    ");
                            msg.append(input.subSequence(start, lineBoundsIndexes[i + 1]));
                            msg.append('\n');
                        }
                    }
                    NbTestCase.fail(msg.toString());
                    
                }
                System.arraycopy(lineBoundsIndexes, 2, lineBoundsIndexes, 0, lineBoundsIndexes.length - 2);
                lineBoundsIndexes[lineBoundsIndexes.length - 2] = inputIndex;
                inputIndex += textLength;
                lineBoundsIndexes[lineBoundsIndexes.length - 1] = inputIndex;
                inputIndex = skipEOL(inputIndex);

            } else {
                output.append(text);
                String ls = (String) System.getProperty("line.separator"); // NOI18N
                output.append(ls);
            }
        }
        
        public void finish() throws Exception {
            if (input == null) {
                if (!outputFile.createNewFile()) {
                    NbTestCase.fail("Cannot create file " + outputFile);
                }
                FileWriter fw = new FileWriter(outputFile);
                try {
                    fw.write(output.toString());
                } finally {
                    fw.close();
                }
                NbTestCase.fail("Created tokens dump file " + outputFile + "\nPlease re-run the test.");

            } else {
                if (inputIndex < input.length()) {
                    NbTestCase.fail("Some text left unread:" + input.substring(inputIndex));
                }
            }
        }
        
        public void setTestName(String testName) {
            this.testName = testName;
        }

        public char charAt(int index) {
            CharSequenceUtilities.checkIndexValid(index, length());
            return input.charAt(inputIndex + index);
        }

        public int length() {
            return textLength;
        }

        public CharSequence subSequence(int start, int end) {
            CharSequenceUtilities.checkIndexesValid(this, start, end);
            return input.substring(inputIndex + start, inputIndex + end);
        }
        
        private int findEOL(int start) {
            while (start < input.length()) {
                switch (input.charAt(start)) {
                    case '\r':
                    case '\n':
                        return start;
                }
                start++;
            }
            return start;
        }
        
        private int skipEOL(int index) {
            if (index < input.length()) {
                index++; // skip separator char
                if (input.charAt(index - 1) == '\r')
                    if (index < input.length() && input.charAt(index) == '\n')
                        index++; // CRLF
            }
            return index;
        }
                    
    }
    
}
