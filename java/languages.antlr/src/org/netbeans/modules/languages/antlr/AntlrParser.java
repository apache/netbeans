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
import java.util.List;
import javax.swing.event.ChangeListener;
import org.antlr.parser.antlr4.ANTLRv4Lexer;
import org.antlr.parser.antlr4.ANTLRv4Parser;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;

/**
 *
 * @author lkishalmi
 */
public class AntlrParser extends org.netbeans.modules.parsing.spi.Parser {

    ANTLRv4Parser.GrammarSpecContext grammarSpec;

    Result lastResult;

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        final List<DefaultError> errors = new ArrayList<>();

        CharStream src = CharStreams.fromString(String.valueOf(snapshot.getText()));
        ANTLRv4Lexer lexer = new org.antlr.parser.antlr4.ANTLRv4Lexer(src);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ANTLRv4Parser parser = new ANTLRv4Parser(tokens);
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                int errorPosition = 0;
                if (offendingSymbol instanceof Token) {
                    Token offendingToken = (Token) offendingSymbol;
                    errorPosition = ((Token) offendingSymbol).getStartIndex();
                }
                errors.add(new DefaultError(null, msg, null, snapshot.getSource().getFileObject(), errorPosition, errorPosition, Severity.ERROR));
            }

        });
        parser.addParseListener(new ParserListener());
        grammarSpec = parser.grammarSpec();
        lastResult = new AntlrParser.Result(snapshot, errors);
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

    static class ParserListener implements ParseTreeListener {

        @Override
        public void visitTerminal(TerminalNode tn) {
        }

        @Override
        public void visitErrorNode(ErrorNode en) {
        }

        @Override
        public void enterEveryRule(ParserRuleContext prc) {
        }

        @Override
        public void exitEveryRule(ParserRuleContext prc) {
        }
    }
    static class Result extends ParserResult {

        private List<? extends Error> errors;

        public Result(Snapshot snapshot, List<? extends Error> errors) {
            super(snapshot);
            this.errors = errors;
        }


        @Override
        public List<? extends Error> getDiagnostics() {
            return errors;
        }

        @Override
        protected void invalidate() {
        }

    }
}
