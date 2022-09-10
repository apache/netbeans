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
package org.netbeans.modules.languages.antlr.v4;

import java.util.ArrayList;
import java.util.List;
import org.antlr.parser.antlr4.ANTLRv4Lexer;
import org.antlr.parser.antlr4.ANTLRv4Parser;
import org.antlr.parser.antlr4.ANTLRv4ParserBaseListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.languages.antlr.AntlrParserResult;
import org.netbeans.modules.languages.antlr.AntlrStructureItem;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lkishalmi
 */
public final class Antlr4ParserResult extends AntlrParserResult<ANTLRv4Parser> {

    public Antlr4ParserResult(Snapshot snapshot) {
        super(snapshot);
    }
    
    @Override
    protected ANTLRv4Parser createParser(Snapshot snapshot) {
        CharStream cs = CharStreams.fromString(String.valueOf(snapshot.getText()));
        ANTLRv4Lexer lexer = new org.antlr.parser.antlr4.ANTLRv4Lexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new ANTLRv4Parser(tokens);
    }

    @Override
    protected void evaluateParser(ANTLRv4Parser parser) {
        parser.grammarSpec();
    }

    @Override
    protected ParseTreeListener createReferenceListener(FileObject source) {
        return new ANTLRv4ParserBaseListener() {
            @Override
            public void exitParserRuleSpec(ANTLRv4Parser.ParserRuleSpecContext ctx) {
                Token token = ctx.RULE_REF().getSymbol();
                OffsetRange range = new OffsetRange(token.getStartIndex(), token.getStopIndex() + 1);
                Reference ref = new Reference(token.getText(), source, range);
                references.put(ref.name, ref);
            }

            @Override
            public void exitLexerRuleSpec(ANTLRv4Parser.LexerRuleSpecContext ctx) {
                Token token = ctx.TOKEN_REF().getSymbol();
                OffsetRange range = new OffsetRange(token.getStartIndex(), token.getStopIndex() + 1);
                Reference ref = new Reference(token.getText(), source, range);
                references.put(ref.name, ref);
            }

        };


    }

    @Override
    protected ParseTreeListener createImportListener(FileObject source) {
        return new ANTLRv4ParserBaseListener() {
            private void addImport(String importedGrammar) {
                FileObject importedFo = source.getParent().getFileObject(importedGrammar + ".g4"); //NOI18N
                if (importedFo != null) {
                    addParseTask(importedFo);
                }
            }

            @Override
            public void exitDelegateGrammar(ANTLRv4Parser.DelegateGrammarContext ctx) {
                addImport(ctx.identifier(0).getText());
            }

            @Override
            public void exitOption(ANTLRv4Parser.OptionContext ctx) {
                if ("tokenVocab".equals(ctx.identifier().getText())) {
                    addImport(ctx.optionValue().getText());
                }
            }

        };
    }

    @Override
    protected ParseTreeListener createFoldListener() {
        return new ANTLRv4ParserBaseListener() {

            private void addFold(Token startToken, Token stopToken) {
                int start = startToken.getStopIndex() + 1;
                int stop = stopToken.getStartIndex();
                if(start >= stop) {
                    return;
                }
                OffsetRange range = new OffsetRange(start, stop);
                if(! folds.contains(range)) {
                    folds.add(range);
                }
            }

            @Override
            public void exitLexerRuleSpec(ANTLRv4Parser.LexerRuleSpecContext ctx) {
                if(ctx.TOKEN_REF() != null && ctx.TOKEN_REF().getSymbol() != null
                        && ctx.SEMI() != null && ctx.SEMI().getSymbol() != null) {
                    addFold(ctx.TOKEN_REF().getSymbol(), ctx.SEMI().getSymbol());
                }
            }

            @Override
            public void exitActionBlock(ANTLRv4Parser.ActionBlockContext ctx) {
                if(ctx.BEGIN_ACTION() != null && ctx.BEGIN_ACTION().getSymbol() != null
                        && ctx.END_ACTION() != null && ctx.END_ACTION().getSymbol() != null) {
                    addFold(ctx.BEGIN_ACTION().getSymbol(), ctx.END_ACTION().getSymbol());
                }
            }

            @Override
            public void exitParserRuleSpec(ANTLRv4Parser.ParserRuleSpecContext ctx) {
                if (ctx.RULE_REF() != null && ctx.RULE_REF().getSymbol() != null
                        && ctx.SEMI() != null && ctx.SEMI().getSymbol() != null) {
                    addFold(ctx.RULE_REF().getSymbol(), ctx.SEMI().getSymbol());
                }
            }

            @Override
            public void exitRules(ANTLRv4Parser.RulesContext ctx) {
                if(ctx.getStart() != null && ctx.getStop() != null) {
                    addFold(ctx.getStart(), ctx.getStop());
                }
            }

            @Override
            public void exitModeSpec(ANTLRv4Parser.ModeSpecContext ctx) {
                if (ctx.identifier() != null && ctx.identifier().getStop() != null
                        && ctx.getStop() != null) {
                    addFold(ctx.identifier().getStop(), ctx.getStop());
                }
            }

        };
    }

    @Override
    protected ParseTreeListener createStructureListener(FileObject source) {
        return new ANTLRv4ParserBaseListener() {
            final List<AntlrStructureItem.RuleStructureItem> lexerStructure = new ArrayList<>();

            @Override
            public void exitLexerRuleSpec(ANTLRv4Parser.LexerRuleSpecContext ctx) {
                if (ctx.FRAGMENT() == null) {
                    // Do not represent fragments in the structure
                    AntlrStructureItem.RuleStructureItem rule = new AntlrStructureItem.RuleStructureItem(ctx.TOKEN_REF().getText(), source, ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex() + 1);
                    lexerStructure.add(rule);
                }
            }

            @Override
            public void exitParserRuleSpec(ANTLRv4Parser.ParserRuleSpecContext ctx) {
                AntlrStructureItem.RuleStructureItem rule = new AntlrStructureItem.RuleStructureItem(ctx.RULE_REF().getText(), source, ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex() + 1);
                structure.add(rule);
            }

            @Override
            public void exitRules(ANTLRv4Parser.RulesContext ctx) {
                if (!lexerStructure.isEmpty()) {
                    AntlrStructureItem.ModeStructureItem mode = new AntlrStructureItem.ModeStructureItem(source, ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex() + 1);
                    mode.rules.addAll(lexerStructure);
                    structure.add(mode);
                    lexerStructure.clear();
                }

            }

            @Override
            public void exitModeSpec(ANTLRv4Parser.ModeSpecContext ctx) {
                AntlrStructureItem.ModeStructureItem mode = new AntlrStructureItem.ModeStructureItem(ctx.identifier().getText(), source, ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex() + 1);
                mode.rules.addAll(lexerStructure);
                structure.add(mode);
                lexerStructure.clear();
            }

        };
    }

    @Override
    protected ParseTreeListener createOccurancesListener() {
        return new ANTLRv4ParserBaseListener() {

            private void addOccurance(Token token) {
                String refName = token.getText();
                Reference ref = references.get(refName);
                if (ref != null) {
                    ref.occurances.add(new OffsetRange(token.getStartIndex(), token.getStopIndex() + 1));
                }
            }

            @Override
            public void exitTerminal(ANTLRv4Parser.TerminalContext ctx) {
                if (ctx.TOKEN_REF() != null) {
                    addOccurance(ctx.TOKEN_REF().getSymbol());
                }
            }

            @Override
            public void exitRuleref(ANTLRv4Parser.RulerefContext ctx) {
                if (ctx.RULE_REF() != null) {
                    addOccurance(ctx.RULE_REF().getSymbol());
                }
            }

        };
    }



}
