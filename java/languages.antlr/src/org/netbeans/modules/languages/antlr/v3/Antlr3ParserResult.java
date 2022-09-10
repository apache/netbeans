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
package org.netbeans.modules.languages.antlr.v3;

import org.antlr.parser.antlr3.ANTLRv3Lexer;
import org.antlr.parser.antlr3.ANTLRv3Parser;
import org.antlr.parser.antlr3.ANTLRv3ParserBaseListener;
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
public final class Antlr3ParserResult extends AntlrParserResult<ANTLRv3Parser> {

    public Antlr3ParserResult(Snapshot snapshot) {
        super(snapshot);
    }
    
    @Override
    protected ANTLRv3Parser createParser(Snapshot snapshot) {
        CharStream cs = CharStreams.fromString(String.valueOf(snapshot.getText()));
        ANTLRv3Lexer lexer = new ANTLRv3Lexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new ANTLRv3Parser(tokens);
    }

    @Override
    protected void evaluateParser(ANTLRv3Parser parser) {
        parser.grammarDef();
    }


    @Override
    protected ParseTreeListener createReferenceListener(FileObject source) {
        return new ANTLRv3ParserBaseListener() {
            @Override
            public void exitRule_(ANTLRv3Parser.Rule_Context ctx) {
                Token token = ctx.id_().getStart();
                OffsetRange range = new OffsetRange(token.getStartIndex(), token.getStopIndex() + 1);
                Reference ref = new Reference(token.getText(), source, range);
                references.put(ref.name, ref);
            }

        };


    }

    @Override
    protected ParseTreeListener createImportListener(FileObject source) {
        return new ANTLRv3ParserBaseListener();
    }

    @Override
    protected ParseTreeListener createFoldListener() {
        return new ANTLRv3ParserBaseListener() {

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
            public void exitActionBlock(ANTLRv3Parser.ActionBlockContext ctx) {
                if(ctx.BEGIN_ACTION() != null && ctx.BEGIN_ACTION().getSymbol() != null
                        && ctx.END_ACTION() != null && ctx.END_ACTION().getSymbol() != null) {
                    addFold(ctx.BEGIN_ACTION().getSymbol(), ctx.END_ACTION().getSymbol());
                }
            }

            @Override
            public void exitRule_(ANTLRv3Parser.Rule_Context ctx) {
                if(ctx.getStart() != null && ctx.getStop() != null) {
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
    protected ParseTreeListener createStructureListener(FileObject source) {
        return new ANTLRv3ParserBaseListener() {

            @Override
            public void exitRule_(ANTLRv3Parser.Rule_Context ctx) {
                AntlrStructureItem.RuleStructureItem rule = new AntlrStructureItem.RuleStructureItem(ctx.id_().getText(), source, ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex() + 1);
                structure.add(rule);
            }

        };
    }

    @Override
    protected ParseTreeListener createOccurancesListener() {
        return new ANTLRv3ParserBaseListener() {

            private void addOccurance(Token token) {
                String refName = token.getText();
                Reference ref = references.get(refName);
                if (ref != null) {
                    ref.occurances.add(new OffsetRange(token.getStartIndex(), token.getStopIndex() + 1));
                }
            }

            @Override
            public void exitAtom(ANTLRv3Parser.AtomContext ctx) {
                if (ctx.RULE_REF() != null) {
                    addOccurance(ctx.RULE_REF().getSymbol());
                }
            }

            @Override
            public void exitRewrite_tree_atom(ANTLRv3Parser.Rewrite_tree_atomContext ctx) {
                if (ctx.TOKEN_REF() != null) {
                    addOccurance(ctx.TOKEN_REF().getSymbol());
                }
                if (ctx.RULE_REF() != null) {
                    addOccurance(ctx.RULE_REF().getSymbol());
                }
            }

            @Override
            public void exitNotTerminal(ANTLRv3Parser.NotTerminalContext ctx) {
                if (ctx.TOKEN_REF() != null) {
                    addOccurance(ctx.TOKEN_REF().getSymbol());
                }
            }

            @Override
            public void exitTokenSpec(ANTLRv3Parser.TokenSpecContext ctx) {
                if (ctx.TOKEN_REF() != null) {
                    addOccurance(ctx.TOKEN_REF().getSymbol());
                }
            }

            @Override
            public void exitId_(ANTLRv3Parser.Id_Context ctx) {
                addOccurance(ctx.getStart());
            }

            @Override
            public void exitTerminal_(ANTLRv3Parser.Terminal_Context ctx) {
                if (ctx.TOKEN_REF() != null) {
                    addOccurance(ctx.TOKEN_REF().getSymbol());
                }
            }


        };
    }



}
