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
package org.netbeans.modules.languages.toml;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import net.vieiro.toml.TOMLParser;
import net.vieiro.toml.antlr4.TOMLAntlrLexer;
import net.vieiro.toml.antlr4.TOMLAntlrParser;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.text.NbDocument;

/**
 *
 * @author Laszlo Kishalmi
 * @author Antonio Vieiro
 */
public class TomlParser extends Parser {

    private static final Logger LOG = Logger.getLogger(Parser.class.getName());

    private Result lastResult;

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {

        final List<DefaultError> errors = new ArrayList<>();
        BaseErrorListener errorListener = new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                int errorBegin = 0;
                int errorEnd = 0;
                if (offendingSymbol instanceof Token) {
                    Token token = (Token) offendingSymbol;
                    errorBegin = token.getStartIndex();
                    errorEnd = token.getStopIndex() + 1;
                } else if (e != null && e.getOffendingToken() != null) {
                    errorBegin = e.getOffendingToken().getStartIndex();
                    errorEnd = e.getOffendingToken().getStopIndex() + 1;
                } else if (snapshot.getSource() != null) {
                    Document document = snapshot.getSource().getDocument(false);
                    if (document != null && document instanceof StyledDocument) {
                        StyledDocument sd = (StyledDocument) document;
                        errorBegin = NbDocument.findLineOffset(sd, line - 1);
                        errorEnd = errorBegin + charPositionInLine;
                    }
                }
                DefaultError error = new DefaultError(null, msg, null, snapshot.getSource().getFileObject(), errorBegin, errorEnd, Severity.ERROR);
                errors.add(error);
            }

        };

        TOMLAntlrLexer lexer = new TOMLAntlrLexer(CharStreams.fromString(String.valueOf(snapshot.getText())));
        TOMLAntlrParser parser = new TOMLAntlrParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);
        parser.addErrorListener(errorListener);
        TOMLAntlrParser.DocumentContext document = null;
        try {
            document = parser.document();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error parsing TOML " + e.getMessage(), e);
        }
        lastResult = new TomlParserResult(snapshot, document, errors);

    }

    @Override
    public Result getResult(Task task) throws ParseException {
        return lastResult;
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
    }

    public static class TomlParserResult extends ParserResult {

        private final List<? extends Error> errors;
        private TOMLAntlrParser.DocumentContext document;

        public TomlParserResult (Snapshot snapshot, TOMLAntlrParser.DocumentContext document, List<DefaultError> errors) {
            super(snapshot);
            this.errors = errors;
            this.document = document;
        }

        @Override
        public List<? extends Error> getDiagnostics() {
            return errors;
        }

        @Override
        protected void invalidate() {
        }

        public TOMLAntlrParser.DocumentContext getDocument() {
            return document;
        }

    }

}
