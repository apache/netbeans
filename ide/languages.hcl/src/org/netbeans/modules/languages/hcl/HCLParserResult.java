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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.languages.hcl.ast.HCLBlockFactory;
import org.netbeans.modules.languages.hcl.ast.HCLDocument;
import org.netbeans.modules.languages.hcl.ast.HCLElement;
import org.netbeans.modules.languages.hcl.grammar.HCLLexer;
import org.netbeans.modules.languages.hcl.grammar.HCLParser;
import org.netbeans.modules.languages.hcl.grammar.HCLParserBaseListener;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.spi.lexer.antlr4.AntlrTokenSequence;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Laszlo Kishalmi
 */
public class HCLParserResult  extends ParserResult {

    protected final List<DefaultError> errors = new ArrayList<>();
    protected volatile boolean finished;
    
    public final Map<String,List<OffsetRange>> folds = new HashMap<>();

    private HCLDocument document;
    private final SourceRef references;

    public HCLParserResult(Snapshot snapshot) {
        super(snapshot);
        references = new SourceRef(snapshot);
    }

    protected final FileObject getFileObject() {
        return getSnapshot().getSource().getFileObject();
    }

    public void compute() {
        if (!finished) {
            CodePointCharStream source = CharStreams.fromString(getSnapshot().getText().toString());
            HCLLexer lexer = new HCLLexer(source);
            lexer.removeErrorListeners();
            collectCommentFolds(lexer);
            
            HCLParser parser = new HCLParser(new CommonTokenStream(lexer));

            configureParser(parser);


            HCLBlockFactory bf = new HCLBlockFactory(references::elementCreated);
            document = bf.process(parser.configFile());
            lexer.reset();
            
        }
        processDocument(document, references);

        finished = true;
    }

    private void collectCommentFolds(HCLLexer lexer) {
        boolean firstComment = true;
        AntlrTokenSequence tokens = new AntlrTokenSequence(lexer);
        while (tokens.hasNext()) {
            Token token = tokens.next().get();
            if (token.getChannel() != HCLLexer.HIDDEN) {
                if (token.getType() == HCLLexer.BLOCK_COMMENT) {
                    if (token.getText().contains("\n")) {
                        addFold(firstComment ? FoldType.INITIAL_COMMENT: FoldType.COMMENT, token);
                    }                    
                }
                firstComment = false;
            }
        }
        lexer.reset();
    }

    @Override
    protected boolean processingFinished() {
        return finished;
    }

    @Override
    public List<? extends Error> getDiagnostics() {
        return errors;
    }

    public final HCLDocument getDocument() {
        return document;
    }

    public final SourceRef getReferences() {
        return references;
    }

    @Override
    protected void invalidate() {
    }

    protected void configureParser(HCLParser parser) {
        parser.removeErrorListeners();
        parser.addErrorListener(createErrorListener());

        parser.addParseListener(createFoldListener());
    }

    protected void processDocument(HCLDocument doc, SourceRef references) {
    }

    protected void addError(HCLElement e, String message) {
        references.getOffsetRange(e).ifPresent((range) -> addError(message, range));
    }
    
    private void addError(String message, OffsetRange range) {
        DefaultError error = new DefaultError(null, message, null, getFileObject(), range.getStart() , range.getEnd(), false, Severity.ERROR);
        errors.add(error);
    }

    private void addFold(FoldType ft, Token token) {
        if (token.getText().contains("\n") && (token.getStartIndex() < token.getStopIndex())) {
            List<OffsetRange> foldBag = folds.computeIfAbsent(ft.code(), (t) ->  new ArrayList<>());
            OffsetRange range = new OffsetRange(token.getStartIndex(), token.getStopIndex() + 1);
            foldBag.add(range);
        }
    }
    private void addFold(FoldType ft, Token start, Token stop) {
        if (start.getLine() < stop.getLine()) {
            int startPos = start.getStartIndex();
            int stopPos = stop.getStopIndex() + 1;
            if (startPos < stopPos) {
                List<OffsetRange> foldBag = folds.computeIfAbsent(ft.code(), (t) ->  new ArrayList<>());
                OffsetRange range = new OffsetRange(startPos, stopPos);
                foldBag.add(range);
            }
        }
    }

    private void addFold(FoldType ft, TerminalNode start, TerminalNode stop) {
        if ((start != null) && (stop != null)) {
            addFold(ft, start.getSymbol(), stop.getSymbol());
        }
    }

    private ANTLRErrorListener createErrorListener() {
        return new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                int errorStart = 0;
                int errorEnd = 0;
                if (offendingSymbol instanceof Token) {
                    Token offendingToken = (Token) offendingSymbol;
                    errorStart = offendingToken.getStartIndex();
                    errorEnd = offendingToken.getStopIndex() + 1;
                }
                errors.add(new DefaultError(null, msg, null, getFileObject(), errorStart, errorEnd, errorStart == errorEnd, Severity.ERROR));
            }

        };
    }

    private ParseTreeListener createFoldListener() {
        return new HCLParserBaseListener() {

            @Override
            public void exitHeredoc(HCLParser.HeredocContext ctx) {
                addFold(HCLLanguage.HCLFold.HEREDOC, ctx.HEREDOC_START(), ctx.HEREDOC_END());
            }

            @Override
            public void exitBlock(HCLParser.BlockContext ctx) {
                addFold(FoldType.CODE_BLOCK, ctx.LBRACE(), ctx.RBRACE());
            }

            @Override
            public void exitForObjectExpr(HCLParser.ForObjectExprContext ctx) {
                addFold(HCLLanguage.HCLFold.OBJECT, ctx.LBRACE(), ctx.RBRACE());
            }

            @Override
            public void exitForTupleExpr(HCLParser.ForTupleExprContext ctx) {
                addFold(HCLLanguage.HCLFold.TUPLE, ctx.LBRACK(), ctx.RBRACK());
            }

            @Override
            public void exitObject(HCLParser.ObjectContext ctx) {
                addFold(HCLLanguage.HCLFold.OBJECT, ctx.LBRACE(), ctx.RBRACE());
            }

            @Override
            public void exitTuple(HCLParser.TupleContext ctx) {
                addFold(HCLLanguage.HCLFold.TUPLE, ctx.LBRACK(), ctx.RBRACK());
            }


        };
    }
}
