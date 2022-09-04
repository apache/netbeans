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
package org.netbeans.modules.languages.antlr;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.antlr.parser.antlr4.ANTLRv4Lexer;
import org.antlr.parser.antlr4.ANTLRv4Parser;
import org.antlr.parser.antlr4.ANTLRv4ParserBaseListener;
import org.antlr.parser.antlr4.ANTLRv4ParserListener;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lkishalmi
 */
public class AntlrParserResult extends ParserResult {

    final List<DefaultError> errors = new ArrayList<>();
    final Map<String, Reference> references = new TreeMap<>();

    final List<OffsetRange> folds = new ArrayList<>();
    final List<AntlrStructureItem> structure = new ArrayList<>();

    final Deque<ANTLRv4Parser> parsingQueue = new LinkedList<>();

    final AtomicBoolean finished = new AtomicBoolean();

    public AntlrParserResult(Snapshot snapshot) {
        super(snapshot);
        addParseTask(snapshot, false);
    }
    

    public AntlrParserResult get() {
        while (!parsingQueue.isEmpty()) {
            parsingQueue.removeFirst().grammarSpec();
        }
        // Second phase scan
        ANTLRv4Parser secondPhase = createParser(getSnapshot());
        secondPhase.addParseListener(createOccurancesListener());
        secondPhase.grammarSpec();
        finished.set(true);
        return this;
    }

    private void addParseTask(FileObject fo) {
        Source src = Source.create(fo);
        addParseTask(src.createSnapshot(), true);
    }

    private ANTLRv4Parser createParser(Snapshot snapshot) {
        CharStream cs = CharStreams.fromString(String.valueOf(snapshot.getText()));
        ANTLRv4Lexer lexer = new org.antlr.parser.antlr4.ANTLRv4Lexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new ANTLRv4Parser(tokens);
    }

    private void addParseTask(Snapshot snapshot, boolean imported) {
        FileObject fo = snapshot.getSource().getFileObject();
        ANTLRv4Parser parser = createParser(snapshot);


        if (!imported) {
            parser.addErrorListener(createErrorListener(fo));
            parser.addParseListener(createFoldListener());
        }
        parser.addParseListener(createReferenceListener(fo));
        parser.addParseListener(createImportListener(fo));
        parser.addParseListener(createStructureListener(fo));
        parsingQueue.add(parser);
    }

    @Override
    public List<? extends Error> getDiagnostics() {
        return errors;
    }

    @Override
    protected void invalidate() {
        //references.clear();
    }

    @Override
    protected boolean processingFinished() {
        return finished.get();
    }

    static class Reference {
        final String name;
        final FileObject source;
        final OffsetRange defOffset;
        final List<OffsetRange> occurances = new ArrayList<>();

        public Reference(String name, FileObject source, OffsetRange defOffset) {
            this.name = name;
            this.source = source;
            this.defOffset = defOffset;
        }
    }

    ANTLRErrorListener createErrorListener(FileObject source) {
        return new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                int errorPosition = 0;
                if (offendingSymbol instanceof Token) {
                    Token offendingToken = (Token) offendingSymbol;
                    errorPosition = offendingToken.getStartIndex();
                }
                errors.add(new DefaultError(null, msg, null, source, errorPosition, errorPosition, Severity.ERROR));
            }

        };
    }

    ANTLRv4ParserListener createReferenceListener(FileObject source) {
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

    ANTLRv4ParserListener createImportListener(FileObject source) {
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

    ANTLRv4ParserListener createFoldListener() {
        return new ANTLRv4ParserBaseListener() {

            private void addFold(Token start, Token stop) {
                OffsetRange range = new OffsetRange(start.getStopIndex() + 1, stop.getStartIndex());
                folds.add(range);
            }

            @Override
            public void exitLexerRuleSpec(ANTLRv4Parser.LexerRuleSpecContext ctx) {
                addFold(ctx.TOKEN_REF().getSymbol(), ctx.SEMI().getSymbol());
            }

            @Override
            public void exitActionBlock(ANTLRv4Parser.ActionBlockContext ctx) {
                addFold(ctx.BEGIN_ACTION().getSymbol(), ctx.END_ACTION().getSymbol());
            }

            @Override
            public void exitParserRuleSpec(ANTLRv4Parser.ParserRuleSpecContext ctx) {
                addFold(ctx.RULE_REF().getSymbol(), ctx.SEMI().getSymbol());
            }

            @Override
            public void exitRules(ANTLRv4Parser.RulesContext ctx) {
                addFold(ctx.getStart(), ctx.getStop());
            }

            @Override
            public void exitModeSpec(ANTLRv4Parser.ModeSpecContext ctx) {
                addFold(ctx.identifier().getStop(), ctx.getStop());
            }

        };
    }

    ANTLRv4ParserListener createStructureListener(FileObject source) {
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

    ANTLRv4ParserListener createOccurancesListener() {
        return new ANTLRv4ParserBaseListener() {

            private void addOccurance(Token token) {
                String refName = token.getText();
                System.out.println(refName);
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
