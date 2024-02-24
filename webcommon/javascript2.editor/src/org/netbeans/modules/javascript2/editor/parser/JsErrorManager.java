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
package org.netbeans.modules.javascript2.editor.parser;

import com.oracle.js.parser.Token;
import com.oracle.js.parser.TokenType;
import com.oracle.js.parser.ErrorManager;
import com.oracle.js.parser.ParserException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.css.lib.api.FilterableError;
import org.netbeans.modules.javascript2.editor.embedding.JsEmbeddingProvider;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hejl, Petr Pisl
 */
public class JsErrorManager extends ErrorManager implements ANTLRErrorListener {

    private static final Logger LOGGER = Logger.getLogger(JsErrorManager.class.getName());

    private static final int MAX_MESSAGE_LENGTH = 100;

    private static final boolean SHOW_BADGES_EMBEDDED = Boolean.getBoolean(JsErrorManager.class.getName() + ".showBadgesEmbedded");

    private static final Comparator<SimpleError> POSITION_COMPARATOR = (SimpleError o1, SimpleError o2) -> {
        if (o1.getStartPosition() < o2.getStartPosition()) {
            return -1;
        }
        if (o1.getStartPosition() > o2.getStartPosition()) {
            return 1;
        }
        return 0;
    };

    // message pattern is for example "index.html:2:16 Exepcted ;"
    private static final Pattern ERROR_MESSAGE_PATTERN = Pattern.compile(".*:\\d+:\\d+ (.*)", Pattern.DOTALL); // NOI18N

    // used to replace pointers from mesage such as
    // Expected ( but found else
    // else
    // ^
    // with this pattern we replace last two lines and related new lines
    private static final Pattern REPLACE_POINTER_PATTERN = Pattern.compile("(\\n)+.*\\n\\s*\\^\\s*"); // NOI18N

    /** Keyword from the error.message which identifies missing char in the JS source. */
    private static final String EXPECTED = "Expected"; //NOI18N

    private final Snapshot snapshot;

    private final Language<JsTokenId> language;

    private List<ParserError> parserErrors;

    private List<JsParserError> convertedErrors;

    private static final Map<String, JsTokenId> JS_TEXT_TOKENS = new HashMap<>();

    static {
        for (JsTokenId jsTokenId : JsTokenId.values()) {
            if (jsTokenId.fixedText() != null) {
                JS_TEXT_TOKENS.put(jsTokenId.fixedText(), jsTokenId);
            }
        }
    }

    public JsErrorManager(Snapshot snapshot, Language<JsTokenId> language) {
        this.snapshot = snapshot;
        this.language = language;
    }

    Error getMissingCurlyError() {
        if (parserErrors == null) {
            return null;
        }
        final FileObject file = snapshot != null ? snapshot.getSource().getFileObject() : null;
        Collection<FilterableError.SetFilterAction> enableFilterAction = file != null
                ? ParsingErrorFilter.getEnableFilterAction(file)
                : Collections.emptyList();
        FilterableError.SetFilterAction disableFilterAction = file != null
                ? ParsingErrorFilter.getDisableFilterAction(file)
                : null;
        for (ParserError error : parserErrors) {
            if (error.message != null
                    && (error.message.contains("Expected }") || error.message.contains("but found }"))) { // NOI18N
                return new JsParserError(error.toSimpleError(snapshot, language),
                        snapshot != null ? snapshot.getSource().getFileObject() : null,
                        Severity.ERROR, null, false, false,
                        enableFilterAction, disableFilterAction);
            }
        }
        return null;
    }

    Error getMissingSemicolonError() {
        if (parserErrors == null) {
            return null;
        }
        final FileObject file = snapshot != null ? snapshot.getSource().getFileObject() : null;
        Collection<FilterableError.SetFilterAction> enableFilterAction = file != null
                ? ParsingErrorFilter.getEnableFilterAction(file)
                : Collections.emptyList();
        FilterableError.SetFilterAction disableFilterAction = file != null
                ? ParsingErrorFilter.getDisableFilterAction(file)
                : null;
        for (ParserError error : parserErrors) {
            if (error.message != null
                    && error.message.contains("Expected ;")) { // NOI18N
                return new JsParserError(error.toSimpleError(snapshot, language),
                        snapshot != null ? snapshot.getSource().getFileObject() : null,
                        Severity.ERROR, null, false, false,
                        enableFilterAction, disableFilterAction);
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return parserErrors == null;
    }

    @Override
    public void error(ParserException e) {
        addParserError(new NashornParserError(e.getMessage(), e.getLineNumber(), e.getColumnNumber(), e.getToken()));
    }

    @Override
    public void error(String message) {
        LOGGER.log(Level.FINE, "Error {0}", message);
        addParserError(new NashornParserError(message));
    }

    @Override
    public void warning(ParserException e) {
        LOGGER.log(Level.FINE, null, e);
    }

    @Override
    public void warning(String message) {
        LOGGER.log(Level.FINE, "Warning {0}", message);
    }

    public List<? extends FilterableError> getErrors() {
        if (convertedErrors == null) {
            if (parserErrors == null) {
                convertedErrors = Collections.emptyList();
            } else {
                ArrayList<SimpleError> errors = new ArrayList<>(parserErrors.size());
                for (ParserError error : parserErrors) {
                    errors.add(error.toSimpleError(snapshot, language));
                }
                errors.sort(POSITION_COMPARATOR);
                convertedErrors = convert(snapshot, errors);
            }
        }
        return Collections.unmodifiableList(convertedErrors);
    }

    JsErrorManager fillErrors(JsErrorManager original) {
        assert this.snapshot == original.snapshot : this.snapshot + ":" + original.snapshot;
        assert this.language == original.language : this.language + ":" + original.language;

        if (original.parserErrors != null) {
            this.parserErrors = new ArrayList<>(original.parserErrors);
        } else {
            this.parserErrors = null;
        }
        this.convertedErrors = null;
        return this;
    }

    private void addParserError(ParserError error) {
        convertedErrors = null;
        if (parserErrors == null) {
            parserErrors = new ArrayList<>();
        }
        parserErrors.add(error);
    }

    private static List<JsParserError> convert(Snapshot snapshot, List<SimpleError> errors) {
        // basically we are solwing showExplorerBadge attribute here
        List<JsParserError> ret = new ArrayList<>(errors.size());
        final FileObject file = snapshot != null ? snapshot.getSource().getFileObject() : null;
        Collection<FilterableError.SetFilterAction> enableFilterAction = file != null
                ? ParsingErrorFilter.getEnableFilterAction(file)
                : Collections.emptyList();
        FilterableError.SetFilterAction disableFilterAction = file != null
                ? ParsingErrorFilter.getDisableFilterAction(file)
                : null;

        if (snapshot != null && BaseParserResult.isEmbedded(snapshot)) {
            int nextCorrect = -1;
            boolean afterGeneratedIdentifier = false;
            for (SimpleError error : errors) {
                boolean showInEditor = true;
                // if the error is in embedded code we ignore it
                // as we don't know what the other language will add
                int pos = snapshot.getOriginalOffset(error.getStartPosition());
                if (pos >= 0 && nextCorrect <= error.getStartPosition()
                        && !JsEmbeddingProvider.containsGeneratedIdentifier(error.getMessage())) {
                    TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsPositionedSequence(
                            snapshot, error.getStartPosition());
                    if (ts != null && ts.movePrevious()) {
                        // check also a previous token - is it generated ?
                        org.netbeans.api.lexer.Token<? extends JsTokenId> token =
                                LexUtilities.findPreviousNonWsNonComment(ts);
                        if (JsEmbeddingProvider.containsGeneratedIdentifier(token.text().toString())) {
                            // usually we may expect a group of errors
                            // so we disable them until next } .... \n
                            nextCorrect = findNextCorrectOffset(ts, error.getStartPosition());
                            showInEditor = false;
                            afterGeneratedIdentifier = true;
                        } else if (afterGeneratedIdentifier && error.getMessage().contains(EXPECTED)) {
                            // errors after generated identifiers can display farther - see issue #229985
                            String expected = getExpected(error.getMessage());
                            if ("eof".equals(expected)) { //NOI18N
                                // unexpected end of script, probably missing at some earlier place : ; } etc.
                                showInEditor = false;
                            } else {
                                JsTokenId expectedToken = getJsTokenFromString(expected);
                                ts.movePrevious();
                                org.netbeans.api.lexer.Token<? extends JsTokenId> previousNonWsToken = LexUtilities.findPreviousNonWsNonComment(ts);
                                if (expectedToken != null && expectedToken == previousNonWsToken.id()) {
                                    // char is available, doesn't show the error
                                    showInEditor = false;
                                }
                            }
                        }
                    }
                } else {
                    showInEditor = false;
                }
                ret.add(new JsParserError(error, file, Severity.ERROR, null, SHOW_BADGES_EMBEDDED, showInEditor, enableFilterAction, disableFilterAction));
            }
        } else {
            for (SimpleError error : errors) {
                ret.add(new JsParserError(error, file, Severity.ERROR, null, true, true, enableFilterAction, disableFilterAction));
            }
        }
        return ret;
    }

    private static String getExpected(String errorMessage) {
        int expectedIndex = errorMessage.indexOf(EXPECTED);
        String afterExpected = errorMessage.substring(expectedIndex + 9);
        int indexOfSpace = afterExpected.indexOf(" "); //NOI18N
        return (indexOfSpace != -1) ? afterExpected.substring(0, indexOfSpace) : afterExpected;
    }

    public static JsTokenId getJsTokenFromString(String name) {
        return JS_TEXT_TOKENS.get(name);
    }

    private static int findNextCorrectOffset(TokenSequence<? extends JsTokenId> ts, int offset) {
        ts.move(offset);
        if (ts.moveNext()) {
            LexUtilities.findNextIncluding(ts, Collections.singletonList(JsTokenId.BRACKET_LEFT_CURLY));
            LexUtilities.findNextIncluding(ts, Collections.singletonList(JsTokenId.EOL));
        }
        return ts.offset();
    }

    //--- Antlr4 ---
    @Override
    public void syntaxError(
            final Recognizer<?, ?> recognizer,
            final Object offendingSymbol,
            final int line,
            final int charPositionInLine,
            final String msg,
            final RecognitionException e) {
        if (recognizer instanceof Parser) {
            //Recognizer can be either Parser or Lexer
            List<String> stack = ((Parser) recognizer).getRuleInvocationStack();
            Collections.reverse(stack);
        }
        addParserError(new AntlrParserError(msg, line, charPositionInLine, offendingSymbol));
    }

    @Override
    public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean bln, BitSet bitset, ATNConfigSet atncs) {
        //Not important
    }

    @Override
    public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitset, ATNConfigSet atncs) {
        //Not important
    }

    @Override
    public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atncs) {
        //Not important
    }

    static final class SimpleError {

        private final String message;
        private final boolean lineError;
        private final int startPosition;
        private final int endPosition;

        private SimpleError(
                final String message,
                final boolean lineError,
                final int startPosition,
                final int endPosition) {
            this.message = message;
            this.lineError = lineError;
            this.startPosition = startPosition;
            this.endPosition = endPosition;
        }

        public String getMessage() {
            return message;
        }

        public boolean isLineError() {
            return lineError;
        }

        public int getStartPosition() {
            return startPosition;
        }

        public int getEndPosition() {
            return endPosition;
        }
    }

    private abstract static class ParserError {
        final String message;
        final int line;
        final int column;

        ParserError(String message, int line, int column) {
            if (message.length() > MAX_MESSAGE_LENGTH) {
                int index = message.indexOf('\n', MAX_MESSAGE_LENGTH);
                this.message = message.substring(0, (index < MAX_MESSAGE_LENGTH && index > 0) ? index : MAX_MESSAGE_LENGTH);
                LOGGER.log(Level.FINE, "Too long error message {0}", message);
            } else {
                this.message = message;
            }
            this.line = line;
            this.column = column;
        }
        abstract SimpleError toSimpleError(@NonNull Snapshot snapshot, @NonNull Language<JsTokenId> language);
    }

    private static final class NashornParserError extends ParserError {

        final long token;

        NashornParserError(String message, int line, int column, long token) {
            super(message, line, column);
            this.token = token;
        }

        NashornParserError(String message, long token) {
            this(message, -1, -1, token);
        }

        NashornParserError(String message) {
            this(message, -1, -1, -1);
        }

        @Override
        SimpleError toSimpleError(
                @NonNull final Snapshot snapshot,
                @NonNull final Language<JsTokenId> language) {
            String message = this.message;
            int offset = -1;
            Matcher matcher = ERROR_MESSAGE_PATTERN.matcher(message);
            if (matcher.matches()) {
                message = matcher.group(1);
            }
            message = REPLACE_POINTER_PATTERN.matcher(message).replaceAll(""); // NOI18N

            if (this.token > 0) {
                offset = Token.descPosition(this.token);
                if (Token.descType(this.token) == TokenType.EOF
                        && snapshot.getOriginalOffset(offset) == -1) {

                    int realOffset = -1;
                    TokenSequence<? extends JsTokenId> ts =
                            LexUtilities.getPositionedSequence(snapshot, offset, language);
                    while (ts.movePrevious()) {
                        if (snapshot.getOriginalOffset(ts.offset()) > 0) {
                            realOffset = ts.offset() + ts.token().length() - 1;
                            break;
                        }
                    }

                    if (realOffset > 0) {
                        offset = realOffset;
                    }
                }
            } else if (this.line == -1 && this.column == -1) {
                // is this still used ?
                String parts[] = this.message.split(":");
    //            if (parts.length > 4) {
    //                message = parts[4];
    //                int index = message.indexOf('\n');
    //                if (index > 0) {
    //                    message = message.substring(0, index);
    //                }
    //
    //            }
                if (parts.length > 3) {
                    try {
                        offset = Integer.parseInt(parts[3]);
                    } catch (NumberFormatException nfe) {
                        // do nothing
                    }
                }
            }
            return new SimpleError(message, true, offset, offset+1);
        }
    }

    private static final class AntlrParserError extends ParserError {

        final Object token;

        public AntlrParserError(String message, int line, int column, Object token) {
            super(message, line, column);
            this.token = token;
        }

        @Override
        SimpleError toSimpleError(Snapshot snapshot, Language<JsTokenId> language) {
            String message = this.message;
            LineDocument doc = (LineDocument)snapshot.getSource().getDocument(false);
            if (doc == null) {
                LOGGER.log(Level.WARNING, "No document found");
                return new SimpleError(message, false, 0, 0);
            }
            int lineOffset = LineDocumentUtils.getLineStartFromIndex((LineDocument)snapshot.getSource().getDocument(false), this.line - 1);
            int offset = lineOffset + this.column;
            if (offset > -1 && offset < snapshot.getText().length()
                    && snapshot.getOriginalOffset(offset) == -1) {

                int realOffset = -1;
                TokenSequence<? extends JsTokenId> ts
                        = LexUtilities.getPositionedSequence(snapshot, offset, language);
                while (ts.movePrevious()) {
                    if (snapshot.getOriginalOffset(ts.offset()) > 0) {
                        realOffset = ts.offset() + ts.token().length() - 1;
                        break;
                    }
                }

                if (realOffset > 0) {
                    offset = realOffset;
                }
            }
            int endOffset = -1;
            if (offset >= 0) {
                endOffset = offset + 1;
                if (token instanceof org.antlr.v4.runtime.Token) {
                    org.antlr.v4.runtime.Token t = (org.antlr.v4.runtime.Token)token;
                    int len = t.getStopIndex() - t.getStartIndex();
                    if (len > 0) {
                        endOffset = offset + len + 1;
                    }
                }
            }

            return new SimpleError(
                    message,
                    false,
                    offset,
                    endOffset);
        }
    }
}
