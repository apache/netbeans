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
package org.netbeans.modules.languages.env.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.languages.env.grammar.antlr4.parser.EnvAntlrLexer;
import org.netbeans.modules.languages.env.grammar.antlr4.parser.EnvAntlrParser;
import org.netbeans.modules.languages.env.grammar.antlr4.parser.EnvAntlrParserBaseListener;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

public class EnvParserResult extends ParserResult {

    private final List<DefaultError> errors = new ArrayList<>();
    private final Map<String, OffsetRange> definedKeys = new HashMap<>();
    private final Map<String, List<OffsetRange>> keyDefintions = new HashMap<>();
    private final Map<String, List<OffsetRange>> interpolationOccurences = new HashMap<>();
    volatile boolean finished = false;

    public EnvParserResult(Snapshot snapshot) {
        super(snapshot);
    }

    public EnvParserResult get() {
        if (!finished) {
            EnvAntlrLexer lexer = new EnvAntlrLexer(CharStreams.fromString(String.valueOf(getSnapshot().getText())));
            EnvAntlrParser parser = new EnvAntlrParser(new CommonTokenStream(lexer));
            
            parser.setBuildParseTree(false);//faster parser
            parser.addErrorListener(createErrorListener());
            parser.addParseListener(new EnvKeyListener());
            parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
            parser.envFile();
            finished = true;
        }
        return this;
    }

    @Override
    public List<? extends Error> getDiagnostics() {
        return errors;
    }

    @Override
    protected void invalidate() {

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
                errors.add(new BadgingDefaultError(null, msg, null, getFileObject(), errorPosition, errorPosition, Severity.ERROR, true));
            }

        };
    }

    public final List<? extends OffsetRange> getOccurrences(String refName) {
        ArrayList<OffsetRange> ret = new ArrayList<>();
        if (keyDefintions.containsKey(refName)) {
            ret.add(definedKeys.get(refName));
        }
        if (interpolationOccurences.containsKey(refName)) {
            ret.addAll(interpolationOccurences.get(refName));
        }
        return ret;
    }

    private class EnvKeyListener extends EnvAntlrParserBaseListener {

        @Override
        public void exitVarAssign(EnvAntlrParser.VarAssignContext ctx) {
            Token keyToken = ctx.start;

            if (keyToken == null) {
                return;
            }

            String keyName = keyToken.getText();
            OffsetRange range = new OffsetRange(keyToken.getStartIndex(), keyToken.getStopIndex() + 1);
            definedKeys.putIfAbsent(keyName, range);
            keyDefintions.computeIfAbsent(keyName, s -> new ArrayList<>()).add(range);
        }

        @Override
        public void exitInterpolatedKey(EnvAntlrParser.InterpolatedKeyContext ctx) {
            Token keyToken = ctx.keyName;

            if (keyToken == null) {
                return;
            }

            OffsetRange range = new OffsetRange(keyToken.getStartIndex(), keyToken.getStopIndex() + 1);
            interpolationOccurences.computeIfAbsent(keyToken.getText(), s -> new ArrayList<>()).add(range);
        }
    }

    public Map<String, OffsetRange> getDefinedKeys() {
        return Collections.unmodifiableMap(definedKeys);
    }

    public Map<String, List<OffsetRange>> getInterpolationOccurences() {
        return Collections.unmodifiableMap(interpolationOccurences);
    }
        
    public Map<String, List<OffsetRange>> getKeyDefinitions() {
        return Collections.unmodifiableMap(keyDefintions);
    }
    
    protected final FileObject getFileObject() {
        return getSnapshot().getSource().getFileObject();
    }
    
    private class BadgingDefaultError extends DefaultError implements Error.Badging {

        private boolean badging;
        
        public BadgingDefaultError(String key, String displayName, String description, FileObject file, int start, int end, Severity severity, boolean badging) {
            super(key, displayName, description, file, start, end, severity);
            this.badging = badging;
        }

        @Override
        public boolean showExplorerBadge() {
            return badging;
        }
        
        
    }
}
