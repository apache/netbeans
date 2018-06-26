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
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
                                do {

                                    moved = sequence.moveNext();
                                    token = (Token<TwigBlockTokenId>) sequence.token();

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
