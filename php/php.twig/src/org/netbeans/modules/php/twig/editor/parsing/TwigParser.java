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
/*
 * Contributor(s): Sebastian Hörl
 */
package org.netbeans.modules.php.twig.editor.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import javax.swing.event.ChangeListener;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.php.twig.editor.lexer.TwigBlockTokenId;

public class TwigParser extends Parser {

    private TwigParserResult result;
    private static final List<String> PARSE_ELEMENTS = new ArrayList<>();

    static {
        PARSE_ELEMENTS.add("for"); //NOI18N
        PARSE_ELEMENTS.add("endfor"); //NOI18N

        PARSE_ELEMENTS.add("if"); //NOI18N
        //parseElements.add( "else" ); // TODO: Check for enclosing if block!
        //parseElements.add( "elseif" ); // TODO: Same as above!
        PARSE_ELEMENTS.add("endif"); //NOI18N

        PARSE_ELEMENTS.add("block"); //NOI18N
        PARSE_ELEMENTS.add("endblock"); //NOI18N

        PARSE_ELEMENTS.add("set"); //NOI18N
        PARSE_ELEMENTS.add("endset"); //NOI18N

        PARSE_ELEMENTS.add("macro"); //NOI18N
        PARSE_ELEMENTS.add("endmacro"); //NOI18N

        PARSE_ELEMENTS.add("filter"); //NOI18N
        PARSE_ELEMENTS.add("endfilter"); //NOI18N

        PARSE_ELEMENTS.add("autoescape"); //NOI18N
        PARSE_ELEMENTS.add("endautoescape"); //NOI18N

        PARSE_ELEMENTS.add("spaceless"); //NOI18N
        PARSE_ELEMENTS.add("endspaceless"); //NOI18N

        PARSE_ELEMENTS.add("embed"); //NOI18N
        PARSE_ELEMENTS.add("endembed"); //NOI18N

        PARSE_ELEMENTS.add("raw"); //NOI18N
        PARSE_ELEMENTS.add("endraw"); //NOI18N

        PARSE_ELEMENTS.add("verbatim"); //NOI18N
        PARSE_ELEMENTS.add("endverbatim"); //NOI18N

        PARSE_ELEMENTS.add("sandbox"); //NOI18N
        PARSE_ELEMENTS.add("endsandbox"); //NOI18N

        PARSE_ELEMENTS.add("trans"); //NOI18N
        PARSE_ELEMENTS.add("endtrans"); //NOI18N
    }

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent sme) throws ParseException {
        result = new TwigParserResult(snapshot);
        TokenHierarchy<?> tokenHierarchy = snapshot.getTokenHierarchy();
        if (tokenHierarchy == null) {
            return;
        }
        LanguagePath twigPath = null;
        for (LanguagePath path : tokenHierarchy.languagePaths()) {
            if (path.mimePath().endsWith(TwigBlockTokenId.language().mimeType())) {
                twigPath = path;
                break;
            }
        }

        if (twigPath != null) {

            List<TokenSequence<?>> tokenSequenceList = tokenHierarchy.tokenSequenceList(twigPath, 0, Integer.MAX_VALUE);
            List<Block> blockList = new ArrayList<>();

            for (TokenSequence<?> sequence : tokenSequenceList) {

                while (sequence.moveNext()) {

                    Token<TwigBlockTokenId> token = (Token<TwigBlockTokenId>) sequence.token();

                    /* Parse block */

                    Block block = new Block();
                    block.function = "";
                    block.startTokenOffset = sequence.offset();
                    block.endTokenOffset = sequence.offset();
                    block.from = token.offset(tokenHierarchy);

                    while (sequence.moveNext()) {

                        token = (Token<TwigBlockTokenId>) sequence.token();
                        if (token.id() == TwigBlockTokenId.T_TWIG_NAME) {
                            block.extra = token.text();
                        }

                    }
                    block.endTokenOffset = sequence.offset() + sequence.token().length();
                    block.length = token.offset(tokenHierarchy) - block.from + token.length();

                    if (block.startTokenOffset != block.endTokenOffset) { // Closed block found

                        sequence.move(block.startTokenOffset);

                        while (sequence.moveNext()) {

                            token = (Token<TwigBlockTokenId>) sequence.token();
                            if (token.id() == TwigBlockTokenId.T_TWIG_TAG) {

                                block.function = token.text();
                                block.functionFrom = token.offset(tokenHierarchy);
                                block.functionLength = token.length();
                                break;

                            }

                        }

                        if (PARSE_ELEMENTS.contains(block.function.toString())) {
                            /* Have we captured a standalone block? */
                            if (CharSequenceUtilities.equals(block.function, "block")) { //NOI18N

                                boolean standalone = false;
                                int names = 0;
                                boolean moved;
                                do {

                                    moved = sequence.moveNext();
                                    if (!moved) { // #247434
                                        break;
                                    }
                                    token = (Token<TwigBlockTokenId>) sequence.token();

                                    if (token.id() == TwigBlockTokenId.T_TWIG_NAME || token.id() == TwigBlockTokenId.T_TWIG_STRING) {
                                        names++;
                                    }

                                    if (names > 1) {
                                        standalone = true;
                                        break;
                                    }

                                } while (moved && sequence.offset() < block.endTokenOffset);

                                if (!standalone) {
                                    blockList.add(block);
                                } else { // add a inline "block" immediately to the result set
                                    result.addBlock("*inline-block", block.from, block.length, block.extra); //NOI18N
                                }

                            } else if (CharSequenceUtilities.equals(block.function, "set")) { //NOI18N

                                boolean standalone = false;
                                boolean moved;
                                boolean first = true;
                                do {

                                    moved = sequence.moveNext();
                                    token = (Token<TwigBlockTokenId>) sequence.token();

                                    // #271040 check whitespace control
                                    if (first || sequence.offset() == block.endTokenOffset - 1) {
                                        first = false;
                                        if (isWhitespaceControlOperator(token)) {
                                            continue;
                                        }
                                    }
                                    if (token.id() == TwigBlockTokenId.T_TWIG_OPERATOR) {
                                        standalone = true;
                                        break;
                                    }

                                } while (moved && sequence.offset() < block.endTokenOffset);

                                if (!standalone) {
                                    blockList.add(block);
                                }

                            } else {
                                blockList.add(block);
                            }

                        }

                        sequence.move(block.endTokenOffset);

                    }

                }

            } // endfor: All blocks are now saved in blockList

            /* Analyse block structure */

            Stack<Block> blockStack = new Stack<>();

            for (Block block : blockList) {

                if (CharSequenceUtilities.startsWith(block.function, "end")) { //NOI18N

                    if (blockStack.empty()) { // End tag, but no more tokens on stack!

                        result.addError(
                                "Unopened '" + block.function + "' block",
                                block.functionFrom,
                                block.functionLength);

                    } else if (CharSequenceUtilities.endsWith(block.function, blockStack.peek().function)) {
                        // end[sth] found a [sth] on the stack!

                        Block start = blockStack.pop();
                        result.addBlock(start.function, start.from, block.from - start.from + block.length, start.extra);

                    } else {
                        // something wrong lies on the stack!
                        // assume that current token is invalid and let it stay on the stack

                        result.addError(
                                "Unexpected '" + block.function + "', expected 'end" + blockStack.peek().function + "'",
                                block.functionFrom,
                                block.functionLength);

                    }

                } else {
                    blockStack.push(block);
                }

            }

            // All blocks were parsed. Are there any left on the stack?
            if (!blockStack.empty()) {
                // Yep, they were never closed!

                while (!blockStack.empty()) {

                    Block block = blockStack.pop();

                    result.addError(
                            "Unclosed '" + block.function + "'",
                            block.functionFrom,
                            block.functionLength);

                }

            }

            // Parsing done!

        }

    }

    @Override
    public Result getResult(Task task) throws ParseException {
        return result;
    }

    @Override
    public void addChangeListener(ChangeListener cl) {
    }

    @Override
    public void removeChangeListener(ChangeListener cl) {
    }

    private static boolean isWhitespaceControlOperator(Token<TwigBlockTokenId> token) {
        return token.id() == TwigBlockTokenId.T_TWIG_OPERATOR && TokenUtilities.equals("-", token.text()); // NOI18N
    }

    private static class Block {
        CharSequence function = null;
        CharSequence extra = null;
        int startTokenOffset = 0;
        int endTokenOffset = 0;
        int from = 0;
        int length = 0;
        int functionFrom = 0;
        int functionLength = 0;
    }
}
