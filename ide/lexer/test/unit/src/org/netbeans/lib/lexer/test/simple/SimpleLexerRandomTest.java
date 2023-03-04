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

package org.netbeans.lib.lexer.test.simple;

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.lang.TestTokenId;
import org.netbeans.lib.lexer.test.FixedTextDescriptor;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.RandomCharDescriptor;
import org.netbeans.lib.lexer.test.RandomModifyDescriptor;
import org.netbeans.lib.lexer.test.RandomTextProvider;
import org.netbeans.lib.lexer.test.TestRandomModify;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class SimpleLexerRandomTest extends NbTestCase {

    public SimpleLexerRandomTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    protected void tearDown() throws java.lang.Exception {
    }

    @Override
    public PrintStream getLog() {
        return System.out;
//        return super.getLog();
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
//        return super.logLevel();
    }

    public void testRandom() throws Exception {
        test(0);
    }
    
    public void testWithSeed_1140557399761L() throws Exception {
        test(1140557399761L);
    }
    
    private void test(long seed) throws Exception {
        TestRandomModify randomModify = new TestRandomModify(seed);
        randomModify.setLanguage(TestTokenId.language());
        
        //randomModify.setDebugOperation(true);
        //randomModify.setDebugDocumentText(true);
        //randomModify.setDebugHierarchy(true);

        // Check for incorrect lookahead counting problem
        // after one of the larger updates of the LexerInputOperation's code
        randomModify.insertText(0, "+--+"); // "+"[2]; "-"[1]; "-"[0]; "+"[1];
        randomModify.removeText(2, 1); // "+-+": "+-+"[0];
        randomModify.clearDocument();

        // Check for error with querying laState.lookahead(-1) after implementing lexer input 
//        Logger.getLogger(org.netbeans.lib.lexer.inc.TokenListUpdater.class.getName()).setLevel(Level.FINE); // Extra logging
        randomModify.insertText(0, "--");
        randomModify.insertText(0, "-");
//        Logger.getLogger(org.netbeans.lib.lexer.inc.TokenListUpdater.class.getName()).setLevel(Level.WARNING); // End of extra logging
        randomModify.clearDocument();

        // Check for incorrect backward elimination of extra relexed tokens.
        // This required to establish lowestMatchIndex in TokenListUpdater.relex().
        randomModify.insertText(0, "+ +");
        randomModify.insertText(2, "\n-\n");
        randomModify.clearDocument();


        // -------------------- SAME-LOOKAHEAD REQUIREMENTS -------------------------
        // Check that the token list updater respects the rule
        // that the lookahead of the incrementally created tokens
        // is the same like in a regular batch lexing.
        // This may not be strictly necessary for correctness but very beneficial because 
        // then the incrementally patched tokens can be compared after each modification
        // with a token list obtained by batch lexing and everything including
        // lookaheads and states can be required to be the same.
        //
        // -------------------- SAME-LOOKAHEAD REQUIREMENT #1 -------------------------
        // Check that when token list updater finds a match (token boundary and states match)
        // that also the lookahead of 
        // of subsequent tokens (already present in the token list)
        // correspond to the lookahead of the relexed token.
        // In the following example a "+-+" token must be created.
        randomModify.insertText(0, "---+"); // "-"[0]; "-"[0]; "-"[0]; "+"[1]; <- see the second token LA=0
        randomModify.insertText(1, "+"); // "-+--+": "-"[0]; "+"[2]; "-"[1]; "-"[0]; "+"[1]; <- seems only "+"[2] was added
        // BUT note the next token "-"[1] has to be relexed too since the original had LA=0

        // Now in addition check that "+-+" will be created.
        randomModify.removeText(3, 1); // "-+-+": "-"[0]; "+-+"[0]
        randomModify.clearDocument();

        
        // -------------------- SAME-LOOKAHEAD REQUIREMENT #2 -------------------------
        // Here check that an original token after the match point would not have unnecesarily high LA.
        // This could happen if the original token before the match point would have LA longer than
        // the length of the token that follows it (the one right after the match point) and so it would affect
        // LA of that next token too. Now if the newly relexed token (before match point) would have small LA
        // the retained token after match point would still hold the extra LA unnecesarilly.
        randomModify.insertText(0, "+--+--"); // "+"[2]; "-"[1]; "-"[0]; "+"[2]; "-"[1]; "-"[0]
        randomModify.removeText(2, 1); // "+-+--": Without extra check: "+-+"[0]; "-"[1]; "-"[0]
        randomModify.clearDocument();
        // BUT in batch lexing the second token would have LA=0.
        // A potential fix is to check that when lexing stops that the 
        
        
   
        // Check for the previous case but this time the relexing would normally
        // be skipped but this would lead to lookahead 1 for the "-" token
        // after the removed "+" (while the batch lexing would produce lookahead 0)
        randomModify.insertText(0, "-+--"); // "-"[0]; "+"[2]; "-"[1]; "-"[0];
        // Without extra care it would become "-"[0]; "-"[1]; "-"[0];
        randomModify.removeText(1, 1); // "---": "-"[0]; "-"[0]; "-"[0]; 
        randomModify.clearDocument();
        
        // Similar case to the previous one but with more tokens
        randomModify.insertText(0, "-+-++--");
        randomModify.removeText(1, 4);
        randomModify.clearDocument();



        // Check for the case when token validation cannot be performed
        // because although the lenghth of the removal is less than
        // the "+-+" token's length the removal spans token boundaries
        randomModify.insertText(0, "-+-+ --");
        randomModify.removeText(3, 2);
        randomModify.clearDocument();


        // Begin really randomized testing
        FixedTextDescriptor[] fixedTexts = new FixedTextDescriptor[] {
            FixedTextDescriptor.create("-+--+-+", 0.2),
            FixedTextDescriptor.create("+-", 0.2),
            FixedTextDescriptor.create("-+", 0.2),
        };
        
        RandomCharDescriptor[] regularChars = new RandomCharDescriptor[] {
            RandomCharDescriptor.letter(0.3),
            RandomCharDescriptor.space(0.3),
            RandomCharDescriptor.lf(0.3),
            RandomCharDescriptor.chars(new char[] { '+', '-', '*', '/'}, 0.3),
        };

        RandomCharDescriptor[] plusMinusChars = new RandomCharDescriptor[] {
            RandomCharDescriptor.chars(new char[] { '+', '-' }, 0.3),
//            RandomCharDescriptor.chars(new char[] { '*', '/' }, 0.1),
            RandomCharDescriptor.space(0.1),
        };

        RandomTextProvider regularTextProvider = new RandomTextProvider(regularChars, fixedTexts);
        RandomTextProvider plusMinusTextProvider = new RandomTextProvider(plusMinusChars, fixedTexts);
        
        RandomTextProvider textProvider = plusMinusTextProvider;

        randomModify.test(
            new RandomModifyDescriptor[] {
                new RandomModifyDescriptor(200, textProvider,
                        0.2, 0.2, 0.1,
                        0.2, 0.2,
                        0.0, 0.0), // snapshots create/destroy
            }
        );

        randomModify.test(
            new RandomModifyDescriptor[] {
                new RandomModifyDescriptor(200, regularTextProvider,
                        0.4, 0.2, 0.2,
                        0.1, 0.1,
                        0.0, 0.0), // snapshots create/destroy
                new RandomModifyDescriptor(200, regularTextProvider,
                        0.2, 0.2, 0.1,
                        0.4, 0.3,
                        0.0, 0.0), // snapshots create/destroy
            }
        );
    }
    
}
