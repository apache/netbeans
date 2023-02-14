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
package org.netbeans.modules.rust.grammar;

import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.ErrorListener;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.rust.grammar.antlr4.RustLexer;
import org.netbeans.modules.rust.grammar.antlr4.RustParser;
import org.netbeans.modules.rust.grammar.antlr4.RustParserBaseListener;
import org.openide.filesystems.FileObject;

/**
 *
 * @author antonio
 */
public class RustLanguageParserResult extends ParserResult {

    private final List<DefaultError> errors = new ArrayList<>();
    public final List<OffsetRange> folds = new ArrayList<>();
    private final FileObject fileObject;
    private volatile boolean finished = false;

    public RustLanguageParserResult(Snapshot snapshot) {
        super(snapshot);
        this.fileObject = getSnapshot().getSource().getFileObject();
    }

    public RustLanguageParserResult get() {
        if (!finished) {
            ANTLRErrorListener errorListener = createErrorListener();
            RustLexer lexer = new RustLexer(CharStreams.fromString(String.valueOf(getSnapshot().getText())));
            lexer.addErrorListener(errorListener);
            RustParser parser = new RustParser(new CommonTokenStream(lexer));
            parser.addErrorListener(errorListener);
            parser.addParseListener(createFoldListener());
            parser.crate();
            finished = true;
        }
        return this;
    }

    private ANTLRErrorListener createErrorListener() {
        return new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                int errorPosition = 0;
                if (offendingSymbol instanceof Token) {
                    Token offendingToken = (Token) offendingSymbol;
                    errorPosition = offendingToken.getStartIndex();
                    if (offendingToken.getChannel() == RustLexer.CHANNEL_COMMENT) {
                        return;
                    }
                }
                errors.add(new DefaultError(null, msg, null, fileObject, errorPosition, errorPosition, Severity.ERROR));
            }
        };
    }

    private ParseTreeListener createFoldListener() {
        return new RustParserBaseListener() {
            private void addFold(Token startToken, Token stopToken) {
                int start = startToken.getStartIndex();
                int stop = stopToken.getStartIndex() + 1;
                if (start >= stop) {
                    return;
                }
                OffsetRange range = new OffsetRange(start, stop);
                if (!folds.contains(range)) {
                    folds.add(range);
                }
            }

            // TODO: Define blocks depending on grammar
//            private void addBlockFold(RustParser.BlockContext ctx) {
//                if ((ctx.L_CURLY() != null) && (ctx.R_CURLY() != null)) {
//                    addFold(ctx.L_CURLY().getSymbol(), ctx.R_CURLY().getSymbol());
//                }
//            }
//
//            @Override
//            public void exitStructType(RustParser.StructTypeContext ctx) {
//                if ((ctx.L_CURLY() != null) && (ctx.R_CURLY() != null)) {
//                    addFold(ctx.L_CURLY().getSymbol(), ctx.R_CURLY().getSymbol());
//                }
//            }
//
//            @Override
//            public void exitMethodDecl(RustParser.MethodDeclContext ctx) {
//                if (ctx.block() != null) {
//                    addBlockFold(ctx.block());
//                }
//            }
//
//            @Override
//            public void exitFunctionDecl(RustParser.FunctionDeclContext ctx) {
//                if (ctx.block() != null) {
//                    addBlockFold(ctx.block());
//                }
//            }
//
//            @Override
//            public void exitImportDecl(RustParser.ImportDeclContext ctx) {
//                if ((ctx.L_PAREN() != null) && (ctx.R_PAREN() != null)) {
//                    addFold(ctx.L_PAREN().getSymbol(), ctx.R_PAREN().getSymbol());
//                }
//            }
            @Override
            public void exitTraitImpl(RustParser.TraitImplContext ctx) {
                if (ctx != null
                        && ctx.LCURLYBRACE() != null
                        && ctx.LCURLYBRACE().getSymbol() != null
                        && ctx.RCURLYBRACE() != null
                        && ctx.RCURLYBRACE().getSymbol() != null) {
                    addFold(ctx.LCURLYBRACE().getSymbol(), ctx.RCURLYBRACE().getSymbol());
                }
            }

            @Override
            public void exitEnumeration(RustParser.EnumerationContext ctx) {
                if (ctx != null
                        && ctx.LCURLYBRACE() != null
                        && ctx.LCURLYBRACE().getSymbol() != null
                        && ctx.RCURLYBRACE() != null
                        && ctx.RCURLYBRACE().getSymbol() != null) {
                    addFold(ctx.LCURLYBRACE().getSymbol(), ctx.RCURLYBRACE().getSymbol());
                }
            }

            @Override
            public void exitExternBlock(RustParser.ExternBlockContext ctx) {
                if (ctx != null
                        && ctx.LCURLYBRACE() != null
                        && ctx.LCURLYBRACE().getSymbol() != null
                        && ctx.RCURLYBRACE() != null
                        && ctx.RCURLYBRACE().getSymbol() != null) {
                    addFold(ctx.LCURLYBRACE().getSymbol(), ctx.RCURLYBRACE().getSymbol());
                }
            }

            @Override
            public void exitInherentImpl(RustParser.InherentImplContext ctx) {
                if (ctx != null
                        && ctx.LCURLYBRACE() != null
                        && ctx.LCURLYBRACE().getSymbol() != null
                        && ctx.RCURLYBRACE() != null
                        && ctx.RCURLYBRACE().getSymbol() != null) {
                    addFold(ctx.LCURLYBRACE().getSymbol(), ctx.RCURLYBRACE().getSymbol());
                }
            }

            @Override
            public void exitStructExprStruct(RustParser.StructExprStructContext ctx) {
                if (ctx != null
                        && ctx.LCURLYBRACE() != null
                        && ctx.LCURLYBRACE().getSymbol() != null
                        && ctx.RCURLYBRACE() != null
                        && ctx.RCURLYBRACE().getSymbol() != null) {
                    addFold(ctx.LCURLYBRACE().getSymbol(), ctx.RCURLYBRACE().getSymbol());
                }
            }

            @Override
            public void exitBlockExpression(RustParser.BlockExpressionContext ctx) {
                if (ctx != null
                        && ctx.LCURLYBRACE() != null
                        && ctx.LCURLYBRACE().getSymbol() != null
                        && ctx.RCURLYBRACE() != null
                        && ctx.RCURLYBRACE().getSymbol() != null) {
                    addFold(ctx.LCURLYBRACE().getSymbol(), ctx.RCURLYBRACE().getSymbol());
                }
            }

            @Override
            public void exitStructPattern(RustParser.StructPatternContext ctx) {
                if (ctx != null
                        && ctx.LCURLYBRACE() != null
                        && ctx.LCURLYBRACE().getSymbol() != null
                        && ctx.RCURLYBRACE() != null
                        && ctx.RCURLYBRACE().getSymbol() != null) {
                    addFold(ctx.LCURLYBRACE().getSymbol(), ctx.RCURLYBRACE().getSymbol());
                }
            }

            @Override
            public void exitStructStruct(RustParser.StructStructContext ctx) {
                if (ctx != null
                        && ctx.LCURLYBRACE() != null
                        && ctx.LCURLYBRACE().getSymbol() != null
                        && ctx.RCURLYBRACE() != null
                        && ctx.RCURLYBRACE().getSymbol() != null) {
                    addFold(ctx.LCURLYBRACE().getSymbol(), ctx.RCURLYBRACE().getSymbol());
                }
            }

            @Override
            public void exitEnumExprStruct(RustParser.EnumExprStructContext ctx) {
                if (ctx != null
                        && ctx.LCURLYBRACE() != null
                        && ctx.LCURLYBRACE().getSymbol() != null
                        && ctx.RCURLYBRACE() != null
                        && ctx.RCURLYBRACE().getSymbol() != null) {
                    addFold(ctx.LCURLYBRACE().getSymbol(), ctx.RCURLYBRACE().getSymbol());
                }
            }

        };
    }

    @Override
    public List<? extends Error> getDiagnostics() {
        return errors;
    }

    @Override
    protected void invalidate() {
    }

}
