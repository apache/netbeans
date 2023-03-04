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
package org.netbeans.modules.languages.antlr.v3;

import java.util.function.Consumer;
import java.util.logging.Logger;
import org.antlr.parser.antlr3.ANTLRv3Lexer;
import org.antlr.parser.antlr3.ANTLRv3Parser;
import org.antlr.parser.antlr3.ANTLRv3ParserBaseListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.languages.antlr.AntlrParserResult;
import org.netbeans.modules.languages.antlr.AntlrStructureItem;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author lkishalmi
 */
public final class Antlr3ParserResult extends AntlrParserResult<ANTLRv3Parser> {
    
    private static final Logger LOG = Logger.getLogger(Antlr3ParserResult.class.getName());

    public Antlr3ParserResult(Snapshot snapshot) {
        super(snapshot);
    }

    @Override
    protected ANTLRv3Parser createParser(Snapshot snapshot) {
        CharStream cs = CharStreams.fromString(String.valueOf(snapshot.getText()));
        ANTLRv3Lexer lexer = new ANTLRv3Lexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ANTLRv3Parser ret = new ANTLRv3Parser(tokens);
        ret.removeErrorListener(ConsoleErrorListener.INSTANCE);
        return ret;
    }

    @Override
    protected void evaluateParser(ANTLRv3Parser parser) {
        parser.grammarDef();
    }

    @Override
    protected ParseTreeListener createReferenceListener() {
        return new ANTLRv3ParserBaseListener() {
            @Override
            public void exitGrammarDef(ANTLRv3Parser.GrammarDefContext ctx) {
                grammarType = GrammarType.MIXED;
                if (ctx.LEXER() != null)  grammarType = GrammarType.LEXER;
                if (ctx.PARSER() != null) grammarType = GrammarType.PARSER;
                if (ctx.TREE() != null)   grammarType = GrammarType.TREE;
            }
            
            @Override
            public void exitRule_(ANTLRv3Parser.Rule_Context ctx) {
                Token token = ctx.id_().getStart();
                OffsetRange range = new OffsetRange(token.getStartIndex(), token.getStopIndex() + 1);
                String name = token.getText();
                ReferenceType rtype = Character.isUpperCase(name.charAt(0)) ? ReferenceType.TOKEN : ReferenceType.RULE;
                rtype = ((rtype == ReferenceType.TOKEN) && (ctx.FRAGMENT() != null)) ? ReferenceType.FRAGMENT : rtype;
                
                Reference ref = new Reference(rtype, name, range);
                references.put(ref.name, ref);
            }
        };
    }

    @Override
    protected ParseTreeListener createImportListener() {
        return new ANTLRv3ParserBaseListener();
    }

    @Override
    protected ParseTreeListener createFoldListener() {
        return new ANTLRv3ParserBaseListener() {

            private void addFold(Token startToken, Token stopToken) {
                int start = startToken.getStopIndex() + 1;
                int stop = stopToken.getStartIndex();
                if (start >= stop) {
                    return;
                }
                OffsetRange range = new OffsetRange(start, stop);
                if (!folds.contains(range)) {
                    folds.add(range);
                }
            }

            @Override
            public void exitActionBlock(ANTLRv3Parser.ActionBlockContext ctx) {
                if (ctx.BEGIN_ACTION() != null && ctx.BEGIN_ACTION().getSymbol() != null
                        && ctx.END_ACTION() != null && ctx.END_ACTION().getSymbol() != null) {
                    addFold(ctx.BEGIN_ACTION().getSymbol(), ctx.END_ACTION().getSymbol());
                }
            }

            @Override
            public void exitRule_(ANTLRv3Parser.Rule_Context ctx) {
                if (ctx.getStart() != null && ctx.getStop() != null) {
                    addFold(ctx.getStart(), ctx.getStop());
                }
            }

            @Override
            public void exitTokenSpec(ANTLRv3Parser.TokenSpecContext ctx) {
                if (ctx.getStart() != null && ctx.getStop() != null) {
                    addFold(ctx.getStart(), ctx.getStop());
                }
            }

        };
    }

    @Override
    protected ParseTreeListener createStructureListener() {
        return new ANTLRv3ParserBaseListener() {

            @Override
            public void exitRule_(ANTLRv3Parser.Rule_Context ctx) {
                boolean fragment = ctx.FRAGMENT() != null;
                if (ctx.id_() != null) {
                    AntlrStructureItem.RuleStructureItem rule = new AntlrStructureItem.RuleStructureItem(
                            ctx.id_().getText(), fragment, getFileObject(), ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex() + 1);
                    structure.add(rule);
                }
            }

        };
    }

    @Override
    protected ParseTreeListener createOccurancesListener() {
        return new ANTLRv3OccuranceListener((token) -> {
                String refName = token.getText();
                OffsetRange or = new OffsetRange(token.getStartIndex(), token.getStopIndex() + 1);
                markOccurrence(refName, or);
        });
    }

    private static class ANTLRv3OccuranceListener extends ANTLRv3ParserBaseListener {

        private final Consumer<Token> onOccurance;

        public ANTLRv3OccuranceListener(Consumer<Token> onOccurance) {
            this.onOccurance = onOccurance;
        }

        @Override
        public void exitAtom(ANTLRv3Parser.AtomContext ctx) {
            if (ctx.RULE_REF() != null) {
                onOccurance.accept(ctx.RULE_REF().getSymbol());
            }
        }

        @Override
        public void exitRewrite_tree_atom(ANTLRv3Parser.Rewrite_tree_atomContext ctx) {
            if (ctx.TOKEN_REF() != null) {
                onOccurance.accept(ctx.TOKEN_REF().getSymbol());
            }
            if (ctx.RULE_REF() != null) {
                onOccurance.accept(ctx.RULE_REF().getSymbol());
            }
        }

        @Override
        public void exitNotTerminal(ANTLRv3Parser.NotTerminalContext ctx) {
            if (ctx.TOKEN_REF() != null) {
                onOccurance.accept(ctx.TOKEN_REF().getSymbol());
            }
        }

        @Override
        public void exitTokenSpec(ANTLRv3Parser.TokenSpecContext ctx) {
            if (ctx.TOKEN_REF() != null) {
                onOccurance.accept(ctx.TOKEN_REF().getSymbol());
            }
        }

        @Override
        public void exitId_(ANTLRv3Parser.Id_Context ctx) {
            onOccurance.accept(ctx.getStart());
        }

        @Override
        public void exitTerminal_(ANTLRv3Parser.Terminal_Context ctx) {
            if (ctx.TOKEN_REF() != null) {
                onOccurance.accept(ctx.TOKEN_REF().getSymbol());
            }
        }
    }
}
