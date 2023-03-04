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
package org.netbeans.modules.languages.toml;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeListener;
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

/**
 *
 * @author Laszlo Kishalmi
 */
public class TomlParser extends Parser{

    private Result lastResult;

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        org.tomlj.internal.TomlLexer lexer = new org.tomlj.internal.TomlLexer(CharStreams.fromString(String.valueOf(snapshot.getText())));
        org.tomlj.internal.TomlParser parser = new org.tomlj.internal.TomlParser(new CommonTokenStream(lexer));
        final List<DefaultError> errors = new ArrayList<>();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                int errorBegin = 0;
                int errorEnd = 0;
                if (offendingSymbol instanceof Token) {
                    Token token = (Token) offendingSymbol;
                    errorBegin = token.getStartIndex();
                    errorEnd = token.getStopIndex() + 1;
                }
                errors.add(new DefaultError(null, msg, null, snapshot.getSource().getFileObject(), errorBegin, errorEnd, Severity.ERROR));
            }
            
        });
        parser.toml();
        lastResult = new TomlParser.Result(snapshot, errors);
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

    public static class Result extends ParserResult {

        private final List<? extends Error> errors;

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
