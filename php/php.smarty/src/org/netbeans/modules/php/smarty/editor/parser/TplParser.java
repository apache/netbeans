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
package org.netbeans.modules.php.smarty.editor.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import javax.swing.event.ChangeListener;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.php.smarty.editor.TplSyntax;
import org.netbeans.modules.php.smarty.editor.lexer.TplTokenId;
import org.openide.util.NbBundle;

/**
 * Tpl parser. Inspired by TwigParser.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class TplParser extends Parser {

    private TplParserResult result;

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent sme) throws ParseException {
        result = new TplParserResult(snapshot);

        TokenHierarchy<?> tokenHierarchy = snapshot.getTokenHierarchy();
        // #269903
        if (tokenHierarchy == null) {
            return;
        }
        LanguagePath tplMimePath = null;
        for (LanguagePath path : tokenHierarchy.languagePaths()) {
            if (path.mimePath().endsWith("x-tpl-inner")) { //NOI18N
                tplMimePath = path;
                break;
            }
        }

        if (tplMimePath != null) {

            List<TokenSequence<?>> tsList = tokenHierarchy.tokenSequenceList(tplMimePath, 0, Integer.MAX_VALUE);
            List<Section> sectionList = new ArrayList<Section>();

            for (TokenSequence<?> ts : tsList) {
                ts.moveNext();
                Token<TplTokenId> token = (Token<TplTokenId>) ts.token();
                Section section = new Section();
                int startOffset = ts.offset();
                StringBuilder textBuilder = new StringBuilder();

                if (token.id() == TplTokenId.FUNCTION) {
                    // store function specific information
                    section.function = CharSequenceUtilities.toString(token.text());
                } else {
                    textBuilder.append(token.text());
                }

                // rest of tag processing
                while (ts.moveNext()) {
                    token = (Token<TplTokenId>) ts.token();
                    textBuilder.append(token.text());
                }
                int endOffset = startOffset + ((startOffset == ts.offset()) ? token.length() : ts.offset() - startOffset + token.length());
                section.offsetRange = new OffsetRange(startOffset, endOffset);
                section.text = textBuilder.toString();
                sectionList.add(section);
            }

            /* Analyse functionList structure */
            Stack<Block> blockStack = new Stack<Block>();
            for (Section section : sectionList) {
                if (section.function == null) {
                    // simple tags - like variables
                    TplParserResult.Block block = new TplParserResult.Block(section.toParserResultSection());
                    result.addBlock(block);
                } else if (TplSyntax.isEndingSmartyCommand(section.function) || TplSyntax.isElseSmartyCommand(section.function)) { //NOI18N
                    if (blockStack.empty()) {
                        result.addError(
                                NbBundle.getMessage(TplParser.class, "ERR_Unopened_Tag", TplSyntax.getRelatedBaseCommand(section.function)), //NOI18N
                                section.offsetRange.getStart(),
                                section.offsetRange.getLength());
                    } else if (TplSyntax.isInRelatedCommand(section.function, blockStack.peek().getControlTag().function)) {
                        if (!TplSyntax.isElseSmartyCommand(section.function)) {
                            // ending command to the parent one, create it in parserResult
                            Block block = blockStack.pop();
                            block.sections.add(section);
                            result.addBlock(block.toParserResultBlock());
                        } else {
                            // in else-like commend, store the section into the parent block
                            Block controlTag = blockStack.peek();
                            controlTag.sections.add(section);
                        }
                    } else {
                        // something wrong lies on the stack!
                        // assume that current token is invalid and let it stay on the stack
                        result.addError(
                                NbBundle.getMessage(TplParser.class, "ERR_Unexpected_Tag", new Object[]{section.function, TplSyntax.getEndingCommand(blockStack.peek().getControlTag().function)}), //NOI18N
                                section.offsetRange.getStart(),
                                section.offsetRange.getLength());
                    }

                } else if (TplSyntax.isBlockCommand(section.function)) {
                    // start of the block command, store block to stack
                    Block block = new Block(section);
                    blockStack.push(block);

                } else {
                    // non-paired function tags
                    TplParserResult.Block block = new TplParserResult.Block(section.toParserResultSection());
                    result.addBlock(block);
                }

            }

            // All instructions were parsed. Are there any left on the stack?
            if (!blockStack.empty()) {
                // they were never closed!
                while (!blockStack.empty()) {
                    for (Section section : blockStack.pop().sections) {
                        result.addError(
                                NbBundle.getMessage(TplParser.class, "ERR_Unclosed_Tag", TplSyntax.getRelatedBaseCommand(section.function)), //NOI18N
                                section.offsetRange.getStart(),
                                section.offsetRange.getLength());
                    }
                }
            }
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

    /**
     * Factory to create new {@link TplParser}.
     */
    public static class Factory extends ParserFactory {

        @Override
        public Parser createParser(Collection<Snapshot> clctn) {
            return new TplParser();
        }
    }

    // We are using another data structures than TplParserResult's one to be able to use help/temp values
    private static class Block {

        private final List<Section> sections = new LinkedList<Section>();

        public Block(Section section) {
            sections.add(section);
        }

        private TplParserResult.Block toParserResultBlock() {
            TplParserResult.Block block = new TplParserResult.Block();
            for (Section section : sections) {
                block.addSection(section.toParserResultSection());
            }
            return block;
        }

        private Section getControlTag() {
            return sections.get(0);
        }
    }

    private static class Section {

        private String function = null;
        private OffsetRange offsetRange = new OffsetRange(0, 0);
        private String text;

        private TplParserResult.Section toParserResultSection() {
            return new TplParserResult.Section(function, offsetRange, text);
        }
    }
}
