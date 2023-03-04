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
package org.netbeans.modules.languages.go;

import java.util.ArrayList;
import java.util.List;
import org.antlr.parser.golang.GoLexer;
import org.antlr.parser.golang.GoParser;
import org.antlr.parser.golang.GoParserBaseListener;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Laszlo Kishalmi
 */
public class GoLangParserResult extends ParserResult {

    private final List<DefaultError> errors = new ArrayList<>();;
    public final List<OffsetRange> folds = new ArrayList<>();
    volatile boolean finished = false;

    public GoLangParserResult(Snapshot snapshot) {
        super(snapshot);
    }

    @Override
    public List<? extends org.netbeans.modules.csl.api.Error> getDiagnostics() {
        return errors;
    }

    public GoLangParserResult get() {
        if (! finished) {
            GoLexer lexer = new GoLexer(CharStreams.fromString(String.valueOf(getSnapshot().getText())));
            GoParser parser = new GoParser(new CommonTokenStream(lexer));
            parser.addErrorListener(createErrorListener());
            parser.addParseListener(createFoldListener());

            parser.sourceFile();
            finished = true;
        }
        return this;
    }

    @Override
    protected void invalidate() {
    }

    @Override
    protected boolean processingFinished() {
        return finished;
    }

    protected final FileObject getFileObject() {
        return getSnapshot().getSource().getFileObject();
    }

    private ANTLRErrorListener createErrorListener() {
        return new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                int errorPosition = 0;
                if (offendingSymbol instanceof Token) {
                    Token offendingToken = (Token) offendingSymbol;
                    errorPosition = offendingToken.getStartIndex();
                }
                errors.add(new DefaultError(null, msg, null, getFileObject(), errorPosition, errorPosition, Severity.ERROR));
            }

        };
    }

    private ParseTreeListener createFoldListener() {
        return new GoParserBaseListener() {
            private void addFold(Token startToken, Token stopToken) {
                int start = startToken.getStartIndex();
                int stop = stopToken.getStartIndex() + 1;
                if(start >= stop) {
                    return;
                }
                OffsetRange range = new OffsetRange(start, stop);
                if(! folds.contains(range)) {
                    folds.add(range);
                }
            }

            private void addBlockFold(GoParser.BlockContext ctx) {
                if ((ctx.L_CURLY() != null) && (ctx.R_CURLY() != null)) {
                    addFold(ctx.L_CURLY().getSymbol(), ctx.R_CURLY().getSymbol());
                }
            }

            @Override
            public void exitStructType(GoParser.StructTypeContext ctx) {
                if ((ctx.L_CURLY() != null) && (ctx.R_CURLY() != null)) {
                    addFold(ctx.L_CURLY().getSymbol(), ctx.R_CURLY().getSymbol());
                }
            }

            @Override
            public void exitMethodDecl(GoParser.MethodDeclContext ctx) {
                if (ctx.block() != null) {
                    addBlockFold(ctx.block());
                }
            }

            @Override
            public void exitFunctionDecl(GoParser.FunctionDeclContext ctx) {
                if (ctx.block() != null) {
                    addBlockFold(ctx.block());
                }
            }


            @Override
            public void exitImportDecl(GoParser.ImportDeclContext ctx) {
                if ((ctx.L_PAREN() != null) && (ctx.R_PAREN() != null)) {
                    addFold(ctx.L_PAREN().getSymbol(), ctx.R_PAREN().getSymbol());
                }
            }

        };
    }
}
