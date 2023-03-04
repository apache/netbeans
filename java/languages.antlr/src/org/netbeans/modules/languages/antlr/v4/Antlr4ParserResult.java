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
package org.netbeans.modules.languages.antlr.v4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;
import org.antlr.parser.antlr4.ANTLRv4Lexer;
import org.antlr.parser.antlr4.ANTLRv4Parser;
import org.antlr.parser.antlr4.ANTLRv4ParserBaseListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.languages.antlr.AntlrParser;
import org.netbeans.modules.languages.antlr.AntlrParserResult;
import org.netbeans.modules.languages.antlr.AntlrStructureItem;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lkishalmi
 */
public final class Antlr4ParserResult extends AntlrParserResult<ANTLRv4Parser> {

    private final List<String> imports = new ArrayList<>();

    private static final Logger LOG = Logger.getLogger(Antlr4ParserResult.class.getName());

    public static final Reference HIDDEN = new Reference(ReferenceType.CHANNEL, "HIDDEN", OffsetRange.NONE);
    public static final Reference DEFAULT_MODE = new Reference(ReferenceType.MODE, "DEFAULT_MODE", OffsetRange.NONE);

    final Set<String> unknownReferences = new HashSet<>();

    public Antlr4ParserResult(Snapshot snapshot) {
        super(snapshot);
        references.put(HIDDEN.name, HIDDEN);
        references.put(DEFAULT_MODE.name, DEFAULT_MODE);
    }
    
    @Override
    protected ANTLRv4Parser createParser(Snapshot snapshot) {
        CharStream cs = CharStreams.fromString(String.valueOf(snapshot.getText()));
        ANTLRv4Lexer lexer = new org.antlr.parser.antlr4.ANTLRv4Lexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ANTLRv4Parser ret = new ANTLRv4Parser(tokens);
        ret.removeErrorListener(ConsoleErrorListener.INSTANCE);
        return ret;
    }

    @Override
    protected void evaluateParser(ANTLRv4Parser parser) {
        parser.grammarSpec();
        checkReferences();
    }

    private void checkReferences() {
        Map<String, Reference> allRefs = new HashMap<>(references.size() * 2);
        for (Antlr4ParserResult pr : allImports().values()) {
            allRefs.putAll(pr.references);
        }
        occurrences.forEach((refName, offsets) -> {
            if (!allRefs.containsKey(refName)) {
                unknownReferences.add(refName);
                for (OffsetRange offset : offsets) {
                    errors.add(new DefaultError(null, "Unknown Reference: " + refName, null, getFileObject(), offset.getStart(), offset.getEnd(), Severity.ERROR));
                }
            }
        });
    }
    
    public List<String> getImports() {
        return Collections.unmodifiableList(imports);
    }

    private static Token getIdentifierToken(ANTLRv4Parser.IdentifierContext ctx) {
        TerminalNode tn = ctx.RULE_REF() != null ? ctx.RULE_REF() : ctx.TOKEN_REF();
        return tn.getSymbol();
    }
    
    @Override
    protected ParseTreeListener createReferenceListener() {
        return new ANTLRv4ParserBaseListener() {
            @Override
            public void exitGrammarType(ANTLRv4Parser.GrammarTypeContext ctx) {
                grammarType = GrammarType.MIXED;
                if (ctx.LEXER() != null)  grammarType = GrammarType.LEXER;
                if (ctx.PARSER() != null) grammarType = GrammarType.PARSER;                
            }
            
            @Override
            public void exitParserRuleSpec(ANTLRv4Parser.ParserRuleSpecContext ctx) {
                if (ctx.RULE_REF() != null) {
                    Token token = ctx.RULE_REF().getSymbol();
                    addReference(ReferenceType.RULE, token);
                }
            }

            @Override
            public void exitLexerRuleSpec(ANTLRv4Parser.LexerRuleSpecContext ctx) {
                if (ctx.TOKEN_REF() != null) {
                    ReferenceType type = ctx.FRAGMENT() != null ? ReferenceType.FRAGMENT : ReferenceType.TOKEN;
                    Token token = ctx.TOKEN_REF().getSymbol();
                    addReference(type, token);
                }
            }

            @Override
            public void exitTokensSpec(ANTLRv4Parser.TokensSpecContext ctx) {
                List<ANTLRv4Parser.IdentifierContext> ids = ctx.idList().identifier();
                for (ANTLRv4Parser.IdentifierContext id : ids) {
                    if (id.TOKEN_REF() != null) {
                        addReference(ReferenceType.TOKEN, id.TOKEN_REF().getSymbol());
                    }
                }
            }

            @Override
            public void exitChannelsSpec(ANTLRv4Parser.ChannelsSpecContext ctx) {
                List<ANTLRv4Parser.IdentifierContext> ids = ctx.idList().identifier();
                for (ANTLRv4Parser.IdentifierContext id : ids) {
                    addReference(ReferenceType.CHANNEL, getIdentifierToken(id));
                }
            }

            @Override
            public void exitModeSpec(ANTLRv4Parser.ModeSpecContext ctx) {
                if (ctx.identifier() != null) {
                    addReference(ReferenceType.MODE, getIdentifierToken(ctx.identifier()));
                }
            }

            public void addReference(ReferenceType type, Token token) {
                OffsetRange range = new OffsetRange(token.getStartIndex(), token.getStopIndex() + 1);
                String name = token.getText();
                Reference ref = new Reference(type, name, range);
                references.put(ref.name, ref);
            }

        };
    }

    @Override
    protected ParseTreeListener createImportListener() {
        return new ANTLRv4ParserBaseListener() {
            private void addImport(String importedGrammar) {
                imports.add(importedGrammar);
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
    protected ParseTreeListener createStructureListener() {
        return new ANTLRv4ParserBaseListener() {
            final List<AntlrStructureItem.RuleStructureItem> lexerStructure = new ArrayList<>();

            @Override
            public void exitLexerRuleSpec(ANTLRv4Parser.LexerRuleSpecContext ctx) {
                boolean fragment = ctx.FRAGMENT() != null;
                if (ctx.TOKEN_REF() != null) {
                    // Do not represent fragments in the structure
                    AntlrStructureItem.RuleStructureItem rule = new AntlrStructureItem.RuleStructureItem(ctx.TOKEN_REF().getText(), fragment, getFileObject(), ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex() + 1);
                    if (fragment) {
                        structure.add(rule);
                    } else {
                        lexerStructure.add(rule);
                    }
                }
            }

            @Override
            public void exitParserRuleSpec(ANTLRv4Parser.ParserRuleSpecContext ctx) {
                if (ctx.RULE_REF() != null) {
                    AntlrStructureItem.RuleStructureItem rule = new AntlrStructureItem.RuleStructureItem(ctx.RULE_REF().getText(), false, getFileObject(), ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex() + 1);
                    structure.add(rule);
                }
            }

            @Override
            public void exitRules(ANTLRv4Parser.RulesContext ctx) {
                if (!lexerStructure.isEmpty()) {
                    AntlrStructureItem.ModeStructureItem mode = new AntlrStructureItem.ModeStructureItem(getFileObject(), ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex() + 1);
                    mode.rules.addAll(lexerStructure);
                    structure.add(mode);
                    lexerStructure.clear();
                }

            }

            @Override
            public void exitModeSpec(ANTLRv4Parser.ModeSpecContext ctx) {
                AntlrStructureItem.ModeStructureItem mode = new AntlrStructureItem.ModeStructureItem(ctx.identifier().getText(), getFileObject(), ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex() + 1);
                mode.rules.addAll(lexerStructure);
                structure.add(mode);
                lexerStructure.clear();
            }

        };
    }

    private void addOccurance(Token token) {
        String refName = token.getText();
        OffsetRange or = new OffsetRange(token.getStartIndex(), token.getStopIndex() + 1);
        markOccurrence(refName, or);
    }

    @Override
    protected ParseTreeListener createOccurancesListener() {
        return new ANTLRv4OccuranceListener(this::addOccurance);
    }
    
    private Optional<Antlr4ParserResult> getParserResult(String grammarName) {
        Optional<Antlr4ParserResult> ret = Optional.empty();
        FileObject fo = getFileObject().getParent().getFileObject(grammarName + ".g4");
        
        if (fo != null) {
            ret = Optional.of(fo.equals(getFileObject()) ? this : (Antlr4ParserResult) AntlrParser.getParserResult(fo));
        }
        return ret;
    }

    private void addImports(Set<String> visited, String importedGrammar) {
        if (visited.add(importedGrammar)) {
            getParserResult(importedGrammar).ifPresent((result) -> {
                for (String im : result.getImports()) {
                    addImports(visited, im);
                }
            });
        }
    }
    

    public Map<String, Antlr4ParserResult> allImports() {
        Set<String> visited = new HashSet<>();
        addImports(visited, getFileObject().getName());
        Map<String, Antlr4ParserResult> ret = new HashMap<>();
        for (String im : visited) {
            getParserResult(im).ifPresent((result) -> ret.put(im, result));
        }
        return ret;
    }
    
    private static class ANTLRv4OccuranceListener extends ANTLRv4ParserBaseListener {
        private final Consumer<Token> onOccurance;

        public ANTLRv4OccuranceListener(Consumer<Token> onOccurance) {
            this.onOccurance = onOccurance;
        }

        @Override
        public void exitTerminal(ANTLRv4Parser.TerminalContext ctx) {
            if (ctx.TOKEN_REF() != null) {
                onOccurance.accept(ctx.TOKEN_REF().getSymbol());
            }
        }

        @Override
        public void exitRuleref(ANTLRv4Parser.RulerefContext ctx) {
            if (ctx.RULE_REF() != null) {
                onOccurance.accept(ctx.RULE_REF().getSymbol());
            }
        }

        @Override
        public void exitLexerCommandExpr(ANTLRv4Parser.LexerCommandExprContext ctx) {
            if (ctx.identifier() != null) {
                onOccurance.accept(getIdentifierToken(ctx.identifier()));
            }
        }

        
        @Override
        public void exitChannelsSpec(ANTLRv4Parser.ChannelsSpecContext ctx) {
            List<ANTLRv4Parser.IdentifierContext> ids = ctx.idList().identifier();
            for (ANTLRv4Parser.IdentifierContext id : ids) {
                onOccurance.accept(getIdentifierToken(id));
            }
        }
    }
}
