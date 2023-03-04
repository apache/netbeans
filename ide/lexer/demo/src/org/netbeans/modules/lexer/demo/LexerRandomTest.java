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

package org.netbeans.modules.lexer.demo;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LexerInput;
import org.netbeans.api.lexer.TokenUpdater;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.Lexer;
import org.netbeans.spi.lexer.util.LexerTestDescription;
import org.netbeans.spi.lexer.util.LexerUtilities;

/**
 * Random test that helps to test lexer correctness.
 * Document is created and updated by subsequent modifications.
 * After each modification the token updater updates token elements
 * of the document and compares them to another token list
 * created by batch lexing of the entire document.
 * <BR>If the two token lists do not match it means that the lexer
 * must be fixed otherwise it would not function well
 * in the incremental setting.
 * <BR><CODE>createLexer()</CODE> can be overriden if necessary.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class LexerRandomTest extends DemoTokenUpdater {
    
    private LexerTestDescription td;
    
    private int debugLevel;

    public LexerRandomTest(LexerTestDescription td, boolean maintainLookbacks) {
        super(new PlainDocument(), td.getLanguage(), maintainLookbacks);

        this.td = td;
        debugLevel = td.getDebugLevel();
    }
    
    public void test() {
        LexerTestDescription.TestRound[] rounds = td.getTestRounds();

        // Fill in insertItems list
        List insertItems = new ArrayList();
        LexerTestDescription.TestChar[] testChars = td.getTestChars();
        if (testChars != null) {
            insertItems.addAll(Arrays.asList(testChars));
        }
        LexerTestDescription.TestCharInterval[] testCharIntervals = td.getTestCharIntervals();
        if (testCharIntervals != null) {
            insertItems.addAll(Arrays.asList(testCharIntervals));
        }
        LexerTestDescription.TestString[] testStrings = td.getTestStrings();
        if (testStrings != null) {
            insertItems.addAll(Arrays.asList(testStrings));
        }
    
        // Compute total insertItemsRatioSum
        double insertItemsRatioSum = 0;
        int insertItemsLength = insertItems.size();
        for (int i = 0; i < insertItemsLength; i++) {
            insertItemsRatioSum += getInsertRatio(insertItems.get(i));
        }
        
        int maxDocumentLength = td.getMaxDocumentLength();

        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        System.err.println("Test started ...");
        long tm = System.currentTimeMillis();

        for (int i = 0; i < rounds.length; i++) {
            LexerTestDescription.TestRound r = rounds[i];
            
            System.out.println("Test round: " + r);
            
            int operationCount = r.getOperationCount();
            double insertRatio = r.getInsertRatio();
            int maxInsertLength = r.getMaxInsertLength();
            double removeRatio = r.getRemoveRatio();
            int maxRemoveLength = r.getMaxRemoveLength();

            double operationRatioSum = r.getInsertRatio() + r.getRemoveRatio();

            // Test string for not yet completed insert or null if no incomplete string
            String incompleteString = null;
            int incompleteStringRemainLength = 0;
            int incompleteStringInsertOffset = 0;
            
            // force remove after extra insert due to previous forced insert of incomplete string
            boolean forceRemove = false;
            
            int moduloOperationCount = operationCount / 10;
            while (--operationCount >= 0) {
                double operationRatio = Math.random() * operationRatioSum;
                operationRatio -= insertRatio;
                if (forceRemove || getDocument().getLength() > maxDocumentLength) {
                    operationRatio = 0;
                }

                if (operationRatio <  0 || incompleteString != null) { // do insert
                    StringBuffer insertBuffer = new StringBuffer();
                    int insertLength = (int)(maxInsertLength * Math.random()) + 1;
                    int insertOffset = (int)((getDocument().getLength() + 1) * Math.random());
                    if (incompleteString != null && operationRatio >= 0) { // would be remove normally
                        insertLength = Math.min(insertLength, incompleteStringRemainLength);
                    }
                    
                    if (incompleteString != null) {
                        insertOffset = incompleteStringInsertOffset;

                        int isLen = incompleteString.length();
                        
                        if (incompleteStringRemainLength <= insertLength) {
                            insertLength -= incompleteStringRemainLength;
                            insertBuffer.append(incompleteString.substring(
                                isLen - incompleteStringRemainLength));

                            insertLength -= incompleteStringRemainLength;
                            incompleteString = null;
                            incompleteStringRemainLength = 0;
                            
                        } else { // incomplete string is longer than insert length
                            insertBuffer.append(incompleteString.substring(
                                isLen - incompleteStringRemainLength,
                                isLen - incompleteStringRemainLength + insertLength
                            ));

                            incompleteStringRemainLength -= insertLength;
                            insertLength = 0;
                        }
                    }
                    

                    while (insertLength > 0) {
                        double insertItemsRatio = Math.random() * insertItemsRatioSum;
                        for (int j = 0; j < insertItemsLength; j++) {
                            Object item = insertItems.get(j);
                            insertItemsRatio -= getInsertRatio(item);
                            if (insertItemsRatio < 0) {
                                // Perform insert
                                if (item instanceof LexerTestDescription.TestChar) {
                                    LexerTestDescription.TestChar tc = (LexerTestDescription.TestChar)item;
                                    insertBuffer.append(tc.getChar());
                                    insertLength--;
                                    
                                } else if (item instanceof LexerTestDescription.TestCharInterval) {
                                    LexerTestDescription.TestCharInterval tci
                                        = (LexerTestDescription.TestCharInterval)item;
                                    insertBuffer.append((char)(tci.getChar()
                                        + ((tci.getLastChar() - tci.getChar() + 1) * Math.random())));
                                    insertLength--;
                                    
                                } else if (item instanceof LexerTestDescription.TestString) {
                                    LexerTestDescription.TestString ts = (LexerTestDescription.TestString)item;
                                    String s = ts.getString();
                                    int sLen = s.length();
                                    if (sLen <= insertLength) {
                                        insertBuffer.append(s);
                                        insertLength -= insertLength;
                                        
                                    } else { // sLen > insertLength
                                        insertBuffer.append(s.substring(0, insertLength));
                                        incompleteString = s;
                                        incompleteStringRemainLength = sLen - insertLength;
                                        insertLength = 0;
                                    }
                                    
                                } else { // unsupported
                                    throw new IllegalStateException();
                                }

                                break;
                            }
                        }
                    }

                    String text = insertBuffer.toString();
                    try {
                        if (debugLevel > 0) {
                            System.err.print("+Insert");

                            if (debugLevel >= 2) { // debug text
                                System.err.print(" \"" + LexerUtilities.toSource(text) + '"');
                            }

                            System.err.print(" at offset=" + insertOffset
                                + "(" + getDocument().getLength() + "), length="
                                + text.length()
                            );
                        }

                        getDocument().insertString(insertOffset, text, null);
                        
                        if (debugLevel >= 3) { // debug doc text
                            System.err.print(", docText=\"" + LexerUtilities.toSource(getDocText(getDocument())) + "\"");
                        }
                        
                        incompleteStringInsertOffset = insertOffset + text.length();
                    } catch (BadLocationException e) {
                        throw new IllegalStateException(e.toString());
                    }

                } else { // not insert
                    operationRatio -= removeRatio;
                    if (operationRatio < 0 || forceRemove) { // do remove
                        forceRemove = false;

                        int removeLength = (int)(maxRemoveLength * Math.random()) + 1;
                        removeLength = Math.min(removeLength, getDocument().getLength());

                        int removeOffset = (int)((getDocument().getLength() - removeLength + 1) * Math.random());
                        try {
                            if (debugLevel > 0) {
                                System.err.print("-Remove");

                                if (debugLevel >= 2) {
                                    String text = getDocument().getText(removeOffset, removeLength);
                                    System.err.print(" \"" + LexerUtilities.toSource(text) + '"');
                                }

                                System.err.print(" at offset=" + removeOffset
                                    + "(" + getDocument().getLength() + "), length=" + removeLength);

                            }

                            getDocument().remove(removeOffset, removeLength);

                            if (debugLevel >= 3) {
                                System.err.print(", docText=\"" + LexerUtilities.toSource(getDocText(getDocument())) + "\"");
                            }
                        } catch (BadLocationException e) {
                            throw new IllegalStateException(e.toString());
                        }
                    }
                }

                // Test the correctness of tokens produced by algorithm
                // by true batch lexing with a fresh instance of lexer
                int tokenIndex = 0; // need to reference if exception thrown
                try {
                    ArrayList lbList = new ArrayList();
                    String docText;
                    try {
                        docText = getDocument().getText(0, getDocument().getLength());
                    } catch (BadLocationException e) {
                        throw new IllegalStateException(e.toString());
                    }
                    LexerInput input = new StringLexerInput(docText);
                    Lexer lexer = getLanguage().createLexer();
                    lexer.restart(input, null);

                    int shift = relocate(0);
                    if (shift != 0) {
                        throw new IllegalStateException("Invalid relocate shift="
                            + shift);
                    }

                    int tokenTotalLength = 0;
                    int lbOffset = 0;
                    while (true) {
                        Token token = lexer.nextToken();
                        if (token != null) {
                            Token itToken = next();
                            checkTokensEqual(itToken, token);
                            int tokenLength = token.getText().length();
                            tokenTotalLength += tokenLength;

                            int la = getLookahead();
                            if (input.getReadLookahead() != la) {
                                throw new IllegalStateException("incremental environment lookahead=" + la
                                    + ", batch lexer lookahead=" + input.getReadLookahead());
                            }

                            Object state = getState();
                            Object lexerState = lexer.getState();
                            if (!((state == null && lexerState == null)
                                    || (state != null && state.equals(lexerState))
                            )) {
                                throw new IllegalStateException(
                                    "States do not match incremental environment lexer-state=" + state
                                    + ", batch lexer state=" + lexerState);
                            }

                            lbList.add(new Integer(tokenLength));
                            lbList.add(new Integer(la));

                            while (lbList.size() > 0) {
                                int tlen = ((Integer)lbList.get(0)).intValue();
                                int tla = ((Integer)lbList.get(1)).intValue();
                                if (lbOffset + tlen + tla <= tokenTotalLength) {
                                    lbOffset += tlen;
                                    lbList.remove(0); // remove len
                                    lbList.remove(0); // remove la
                                } else {
                                    break;
                                }
                            }

                            int lb = getLookback();
                            if (lb >= 0) {
                                if (lb != lbList.size() / 2) {
                                    throw new IllegalStateException("iterator-lb="
                                        + lb
                                        + ", lexer-lb=" + (lbList.size() / 2)
                                    );
                                }
                            }

                            tokenIndex++;

                        } else { // no more tokens
                            if (hasNext()) {
                                throw new IllegalStateException();
                            }
                            if (tokenTotalLength != docText.length()) {
                                throw new IllegalStateException();
                            }
                            break;
                        }
                    }

                    if (debugLevel > 0) {
                        System.err.println(", " + tokenIndex + " tokens");
                    }

                    if (operationCount > 0 && (operationCount % moduloOperationCount) == 0) {
                        System.err.println(operationCount
                            + " operations remain. docLength=" + getDocument().getLength()
                            + ", tokenCount=" + tokenIndex
                        );
                    }

                } catch (RuntimeException e) {
                    try {
                        System.err.println("\n\nException thrown - document text=\""
                            + LexerUtilities.toSource(getDocument().getText(
                                0, getDocument().getLength()))
                            + "\"\ntokens:\n" + allTokensToString()
                            + "tokenIndex=" + tokenIndex);

                    } catch (BadLocationException ex) {
                        throw new IllegalStateException(ex.toString());
                    }
                    throw e; // rethrow
                }

            } // while (--operationCount >= 0)
        }

        System.err.println("Test finished in "
            + (System.currentTimeMillis() - tm) / 1000
            + " seconds."
        );
    }
    
    private static double getInsertRatio(Object o) {
        if (o instanceof LexerTestDescription.TestChar) {
            return ((LexerTestDescription.TestChar)o).getInsertRatio();
        } else if (o instanceof LexerTestDescription.TestCharInterval) {
            return ((LexerTestDescription.TestCharInterval)o).getInsertRatio();
        } else if (o instanceof LexerTestDescription.TestString) {
            return ((LexerTestDescription.TestString)o).getInsertRatio();
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    private void dump() {
        System.err.println("Dump of token iterator\n"
            + allTokensToString());
    }
    
    private static void checkTokensEqual(Token t1, Token t2) {
        if (t1.getId() != t2.getId()) {
            throw new IllegalStateException("t1.id=" + t1.getId()
                + ", t2.id=" + t2.getId());
        }
        
        CharSequence t1Text = t1.getText();
        CharSequence t2Text = t2.getText();
        if (t1Text.length() != t2Text.length()) {
            throw new IllegalStateException(
                "t1=\"" + LexerUtilities.toSource(t1Text.toString())
                + "\", t2=\"" + LexerUtilities.toSource(t2Text.toString())
                + '"'
            );
        }
        for (int i = t1Text.length() - 1; i >= 0; i--) {
            if (t1Text.charAt(i) != t2Text.charAt(i)) {
                throw new IllegalStateException();
            }
        }
    }
    
    private static String getDocText(Document doc) {
        try {
            return doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            throw new IllegalStateException();
        }
    }

    public static void main (String[] args) {
        try {
            if (args.length == 0) {
                System.err.println("Usage: java " + LexerRandomTest.class.getName ()
                    + " <test-description-class-name>");
                System.exit(1);
            }

            Class langCls = Class.forName(args[0]);
            LexerTestDescription td = (LexerTestDescription)langCls.newInstance();
            
            new LexerRandomTest(td, false).test();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}

