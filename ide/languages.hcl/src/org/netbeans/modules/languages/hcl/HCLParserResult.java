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
package org.netbeans.modules.languages.hcl;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.languages.hcl.ast.ASTBuilderListener;
import org.netbeans.modules.languages.hcl.ast.HCLDocument;
import org.netbeans.modules.languages.hcl.grammar.HCLLexer;
import org.netbeans.modules.languages.hcl.grammar.HCLParser;
import org.netbeans.modules.languages.hcl.grammar.HCLParserBaseListener;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Laszlo Kishalmi
 */
public class HCLParserResult  extends ParserResult {

    protected final List<DefaultError> errors = new ArrayList<>();
    protected volatile boolean finished;
    public final List<OffsetRange> folds = new ArrayList<>();

    private HCLDocument document;

    public HCLParserResult(Snapshot snapshot) {
        super(snapshot);
    }

    protected final FileObject getFileObject() {
        return getSnapshot().getSource().getFileObject();
    }

    public HCLParserResult get() {
        if (!finished) {
            HCLLexer lexer = new HCLLexer(CharStreams.fromString(getSnapshot().getText().toString()));
            lexer.removeErrorListeners();
            HCLParser parser = new HCLParser(new CommonTokenStream(lexer));

            configureParser(parser);


            parser.configFile();
            
        }
        processDocument(document);

        finished = true;
        return this;
    }

    @Override
    protected boolean processingFinished() {
        return finished;
    }

    @Override
    public List<? extends Error> getDiagnostics() {
        return errors;
    }

    @Override
    protected void invalidate() {
    }

    protected void configureParser(HCLParser parser) {
        parser.removeErrorListeners();
        parser.addErrorListener(createErrorListener());

        parser.addParseListener(createFoldListener());

        ASTBuilderListener astListener = new ASTBuilderListener();
        parser.addParseListener(astListener);

        document = astListener.getDocument();
    }

    protected void processDocument(HCLDocument doc) {
    }

    private void addFold(int start, int stop) {
        OffsetRange range = new OffsetRange(start, stop);
        if(! folds.contains(range)) {
            folds.add(range);
        }
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
        return new HCLParserBaseListener() {

            @Override
            public void exitHeredocTemplate(HCLParser.HeredocTemplateContext ctx) {
                if (ctx.HEREDOC_END() != null) {
                    int start = ctx.HEREDOC_START().getSymbol().getStopIndex();
                    int stop = ctx.HEREDOC_END().getSymbol().getStopIndex() + 1;
                    addFold(start, stop);
                }
            }

            @Override
            public void exitBlock(HCLParser.BlockContext ctx) {
                if (ctx.RBRACE() != null) {
                    addFold(ctx.LBRACE().getSymbol().getStartIndex(), ctx.RBRACE().getSymbol().getStopIndex() + 1);
                }
            }


        };
    }
}
