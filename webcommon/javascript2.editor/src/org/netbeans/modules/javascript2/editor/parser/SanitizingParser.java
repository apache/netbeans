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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Petr Hejl
 */
public abstract class SanitizingParser<R extends BaseParserResult> extends Parser {

    private static final Logger LOGGER = Logger.getLogger(SanitizingParser.class.getName());
    
    public static final boolean PARSE_BIG_FILES = Boolean.getBoolean("nb.js.parse.big.files"); //NOI18N
    public static final long MAX_FILE_SIZE_TO_PARSE = Integer.getInteger("nb.js.big.file.size", 1024 * 1024); //NOI18N
    private static final long MAX_MINIMIZE_FILE_SIZE_TO_PARSE = Integer.getInteger("nb.js.big.minimize.file.size", 0); //NOI18N
    /**
     * This is count of closing curly brackets that follows at the end of a json file. 
     * If the file has sequence of MAX_RICHT_CURLY_BRACKETS, then it's not parse due stack overflow.
     */
    private static final int MAX_RIGHT_CURLY_BRACKETS = 30;

    private final Language<JsTokenId> language;
    private final ChangeSupport listeners;

    private R lastResult = null;

    public SanitizingParser(Language<JsTokenId> language) {
        this.language = language;
        this.listeners = new ChangeSupport(this);
    }

    @Override
    public final void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, snapshot.getText().toString());
        }
        try {
            JsErrorManager errorManager = new JsErrorManager(snapshot, language);
            lastResult = parseSource(snapshot, event, getSanitizeStrategy(), errorManager);
            lastResult.setErrors(errorManager.getErrors());
        } catch (Exception ex) {
            LOGGER.log (Level.INFO, "Exception during parsing", ex);
            // TODO create empty result
            lastResult = createErrorResult(snapshot);
        }
    }

    protected abstract String getDefaultScriptName();

    @CheckForNull
    protected abstract R parseSource(Context context, JsErrorManager errorManager) throws Exception;

    protected abstract String getMimeType();

    @NonNull
    protected abstract R createErrorResult(@NonNull Snapshot snapshot);

    protected Sanitize getSanitizeStrategy() {
        return Sanitize.NONE;
    }

    final R parseSource(Snapshot snapshot, SourceModificationEvent event,
            Sanitize sanitizing, JsErrorManager errorManager) throws Exception {

        FileObject fo = snapshot.getSource().getFileObject();
        long startTime = System.nanoTime();
        String scriptName;
        if (fo != null) {
            scriptName = snapshot.getSource().getFileObject().getNameExt();
        } else {
            scriptName = getDefaultScriptName();
        }
        if (!isParsable(snapshot)) {
            return createErrorResult(snapshot);
        }
        int caretOffset = GsfUtilities.getLastKnownCaretOffset(snapshot, event);

        Context context = new Context(scriptName, snapshot, caretOffset, language);
        R result = parseContext(context, sanitizing, errorManager);
        if (!result.success() && context.isModule()) {
            // module may be broken completely by broken/unfinished export
            // try to at least parse it as normal source
            context.isModule = false;
            result = parseContext(context, sanitizing, errorManager, false);
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Parsing took: {0} ms; source: {1}",
                    new Object[]{(System.nanoTime() - startTime) / 1000000, scriptName});
        }
        return result;
    }

    /**
     * This method try to analyze the text and says whether the snapshot should be file
     * @param snapshot
     * @return whether the snapshot should be parsed
     */
    private boolean isParsable (Snapshot snapshot) {
        FileObject fo = snapshot.getSource().getFileObject();
        boolean isEmbeded = !getMimeType().equals(snapshot.getMimePath().getPath());
        Long size;
        CharSequence text = snapshot.getText();
        String scriptName;
        scriptName = (fo != null) ? snapshot.getSource().getFileObject().getNameExt() : getDefaultScriptName();
        size = (fo != null && !isEmbeded) ? fo.getSize() : (long)text.length();

        if (!PARSE_BIG_FILES) {
            if (size > MAX_FILE_SIZE_TO_PARSE) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "The file {0} was not parsed because the size is too big.", scriptName);
                }
                return false;
            }

            if (size > MAX_MINIMIZE_FILE_SIZE_TO_PARSE && !(snapshot.getMimeType().equals(JsTokenId.JSON_MIME_TYPE) || snapshot.getMimeType().equals(JsTokenId.PACKAGE_JSON_MIME_TYPE)||snapshot.getMimeType().equals(JsTokenId.BOWER_JSON_MIME_TYPE))) {
                // try to find only for the file that has size bigger then 1/3 of the max size
                boolean isMinified = false;
                TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(snapshot, 0, language);
                if (ts != null) {
                    int offset = 0;
                    int countedLines = 0;
                    int countChars = 0;
                    while (!isMinified && ts.moveNext() && countedLines < 5) {
                        LexUtilities.findNext(ts, Arrays.asList(JsTokenId.DOC_COMMENT, JsTokenId.BLOCK_COMMENT, JsTokenId.LINE_COMMENT, JsTokenId.EOL, JsTokenId.WHITESPACE));
                        offset = ts.offset();
                        LexUtilities.findNextToken(ts, Arrays.asList(JsTokenId.EOL));
                        countChars += (ts.offset() - offset);
                        countedLines++;
                    }
                    if (countedLines > 0 && (countChars / countedLines) > 200) {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.log(Level.FINE, "The file {0} was not parsed because it is minimized and the size is too big.", scriptName);
                        }
                        return false;
                    }
                }
            } else if (snapshot.getMimeType().equals(JsTokenId.JSON_MIME_TYPE)) {
                int index = text.length() - 1;
                if (index < 0) {
                    // NETBEANS-2881
                    return false;
                }
                char ch = text.charAt(index);
                while (index > 0 && ch != '}') {
                    index--;
                    ch = text.charAt(index);
                }
                int count = 0;
                while (index > 0 && ch == '}' && count <= MAX_RIGHT_CURLY_BRACKETS) {
                    index--;
                    count++;
                    ch = text.charAt(index);
                    
                }
                if (count >= MAX_RIGHT_CURLY_BRACKETS) {   // See issue 247274
                    return false;
                }
            }
        }
        return true;
    }
    
    R parseContext(Context context, Sanitize sanitizing,
            JsErrorManager errorManager) throws Exception {
        return parseContext(context, sanitizing, errorManager, true);
    }
    
    private R parseContext(Context context, Sanitize sanitizing,
            JsErrorManager errorManager, boolean copyErrors) throws Exception {
        
        boolean sanitized = false;
        if ((sanitizing != Sanitize.NONE) && (sanitizing != Sanitize.NEVER)) {
            boolean ok = sanitizeSource(context, sanitizing, errorManager);

            if (ok) {
                sanitized = true;
                assert context.getSanitizedSource() != null;
            } else {
                // Try next trick
                return parseContext(context, sanitizing.next(), errorManager, false);
            }
        }
        
        JsErrorManager current = new JsErrorManager(context.getSnapshot(), language);
        R r = parseSource(context, current);

        if (copyErrors) {
            errorManager.fillErrors(current);
        }
        
        if (sanitizing != Sanitize.NEVER) {
            if (!sanitized) {
                if (current.getMissingCurlyError() != null) {
                    return parseContext(context, Sanitize.MISSING_CURLY, errorManager, false);
                }
                if (current.getMissingSemicolonError() != null) {
                    return parseContext(context, Sanitize.MISSING_SEMICOLON, errorManager, false);
                }
            }
            // TODO not very clever check
            if (r == null || !current.isEmpty()) {
                return parseContext(context, sanitizing.next(), errorManager, false);
            }
        }
        return r != null ?
            r :
            createErrorResult(context.getSnapshot());
    }

    private boolean sanitizeSource(Context context, Sanitize sanitizing, JsErrorManager errorManager) {
        if (sanitizing == Sanitize.MISSING_CURLY) {
            org.netbeans.modules.csl.api.Error error = errorManager.getMissingCurlyError();
            if (error != null) {
                int offset = error.getStartPosition();
                return sanitizeBrackets(sanitizing, context, offset, '{', '}'); // NOI18N
            }
        } else if (sanitizing == Sanitize.MISSING_SEMICOLON) {
            org.netbeans.modules.csl.api.Error error = errorManager.getMissingSemicolonError();
            if (error != null) {
                String source = context.getOriginalSource();

                boolean ok = false;
                StringBuilder builder = new StringBuilder(source);
                if (error.getStartPosition() >= source.length()) {
                    builder.append(';'); // NOI18N
                    ok = true;
                } else {
                    int replaceOffset = error.getStartPosition();
                    if (replaceOffset >= 0 && Character.isWhitespace(replaceOffset)) {
                        builder.delete(replaceOffset, replaceOffset + 1);
                        builder.insert(replaceOffset, ';'); // NOI18N
                        ok = true;
                    }
                }

                if (ok) {
                    context.setSanitizedSource(builder.toString());
                    context.setSanitization(sanitizing);
                    return true;
                }
            }
        } else if (sanitizing == Sanitize.SYNTAX_ERROR_CURRENT) {
            List<? extends org.netbeans.modules.csl.api.Error> errors = errorManager.getErrors();
            if (!errors.isEmpty()) {
                org.netbeans.modules.csl.api.Error error = errors.get(0);
                int offset = error.getStartPosition();
                TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(
                        context.getSnapshot(), 0, language);
                if (ts != null) {
                    ts.move(offset);
                    if (ts.moveNext()) {
                        int start = ts.offset();
                        if (start >= 0 && ts.moveNext()) {
                            int end = ts.offset();
                            ts.movePrevious();
                            while(ts.movePrevious() && ts.token().id() == JsTokenId.WHITESPACE) {
                            }
                            if (ts.token().id() == JsTokenId.OPERATOR_DOT) {
                                start = ts.offset();
                            }
                            StringBuilder builder = new StringBuilder(context.getOriginalSource());
                            erase(builder, start, end);
                            context.setSanitizedSource(builder.toString());
                            context.setSanitization(sanitizing);
                            return true;
                        }
                    }
                }
            }
        } else if (sanitizing == Sanitize.SYNTAX_ERROR_PREVIOUS) {
            List<? extends org.netbeans.modules.csl.api.Error> errors = errorManager.getErrors();
            if (!errors.isEmpty()) {
                org.netbeans.modules.csl.api.Error error = errors.get(0);
                int offset = error.getStartPosition();
                return sanitizePrevious(sanitizing, context, offset, new TokenCondition() {

                    @Override
                    public boolean found(JsTokenId id) {
                        return id != JsTokenId.WHITESPACE
                                && id != JsTokenId.EOL
                                && id != JsTokenId.DOC_COMMENT
                                && id != JsTokenId.LINE_COMMENT
                                && id != JsTokenId.BLOCK_COMMENT;
                    }
                });
            }
        } else if (sanitizing == Sanitize.MISSING_PAREN) {
            List<? extends org.netbeans.modules.csl.api.Error> errors = errorManager.getErrors();
            if (!errors.isEmpty()) {
                org.netbeans.modules.csl.api.Error error = errors.get(0);
                int offset = error.getStartPosition();
                return sanitizeBrackets(sanitizing, context, offset, '(', ')'); // NOI18N
            }
        } else if (sanitizing == Sanitize.ERROR_DOT) {
            List<? extends org.netbeans.modules.csl.api.Error> errors = errorManager.getErrors();
            if (!errors.isEmpty()) {
                org.netbeans.modules.csl.api.Error error = errors.get(0);
                int offset = error.getStartPosition();
                return sanitizePrevious(sanitizing, context, offset, new TokenCondition() {

                    @Override
                    public boolean found(JsTokenId id) {
                        return id == JsTokenId.OPERATOR_DOT;
                    }
                });
            }
        } else if (sanitizing == Sanitize.ERROR_LINE) {
            List<? extends org.netbeans.modules.csl.api.Error> errors = errorManager.getErrors();
            if (!errors.isEmpty()) {
                org.netbeans.modules.csl.api.Error error = errors.get(0);
                int offset = error.getStartPosition();
                return sanitizeLine(sanitizing, context, offset);
            }
        } else if (sanitizing == Sanitize.EDITED_LINE) {
            int offset = context.getCaretOffset();
            return sanitizeLine(sanitizing, context, offset);
        } else if (sanitizing == Sanitize.PREVIOUS_LINES) {
            StringBuilder result = new StringBuilder(context.getOriginalSource());
            for (org.netbeans.modules.csl.api.Error error : errorManager.getErrors()) {
                TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(
                        context.getSnapshot(), error.getStartPosition(), language);
                if (ts != null) {
                    ts.move(error.getStartPosition());
                    if (ts.movePrevious()) {
                        LexUtilities.findPreviousIncluding(ts, Collections.singletonList(JsTokenId.EOL));
                        if (!sanitizeLine(context.getOriginalSource(), result, ts.offset())) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            context.setSanitizedSource(result.toString());
            context.setSanitization(sanitizing);
            return true;
        }
        return false;
    }

    private boolean sanitizePrevious(Sanitize sanitizing, Context context, int offset, TokenCondition condition) {
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(
                context.getSnapshot(), 0, language);
        if (ts != null) {
            ts.move(offset);
            int start = -1;
            while (ts.movePrevious()) {
                if (condition.found(ts.token().id())) {
                    start = ts.offset();
                    break;
                }
            }
            if (start >= 0) {
                int end = offset;
                if (ts.moveNext()) {
                    end = ts.offset();
                }
                StringBuilder builder = new StringBuilder(context.getOriginalSource());
                erase(builder, start, end);
                context.setSanitizedSource(builder.toString());
                context.setSanitization(sanitizing);
                return true;
            }
        }
        return false;
    }

    private boolean sanitizeLine(Sanitize sanitizing, Context context, int offset) {
        if (offset > -1) {
            String source = context.getOriginalSource();

            StringBuilder builder = new StringBuilder(source);
            if (!sanitizeLine(source, builder, offset)) {
                return false;
            }
            context.setSanitizedSource(builder.toString());
            context.setSanitization(sanitizing);
            return true;
        }
        return false;
    }

    private boolean sanitizeLine(String source, StringBuilder result, int offset) {
        if (offset > -1 && !source.isEmpty()) {
            int start = offset > 0 ? offset - 1 : offset;
            int end = start + 1;
            // fix until new line or }
            boolean incPosition = false;
            char c = source.charAt(start);
            while (start > 0 && c != '\n' && c != '\r' && c != '{' && c != '}') { // NOI18N
                c = source.charAt(--start);
                if (start <= 0) {
                    incPosition = false;
                } else {
                    incPosition = true;
                }
            }
            if (incPosition) {
                start++;
            }
            boolean decPosition = false;
            if (end < source.length()) {
                c = source.charAt(end);
                while (end < source.length() && c != '\n' && c != '\r' && c != '{' && c != '}') { // NOI18N
                    c = source.charAt(end++);
                    if (end >= source.length()) {
                        decPosition = false;
                    } else {
                        decPosition = true;
                    }
                }
            }
            if (decPosition) {
                end--;
            }

            erase(result, start, end);
            return true;
        }
        return false;
    }

    private boolean sanitizeBrackets(Sanitize sanitizing, Context context, int offset,
            char left, char right) {
        String source = context.getOriginalSource();
        int balance = 0;
        for (int i = 0; i < source.length(); i++) {
            char current = source.charAt(i);
            if (current == left) {
                balance++;
            } else if (current == right) {
                balance--;
            }
        }
        if (balance != 0) {
            StringBuilder builder = new StringBuilder(source);
            if (balance < 0) {
                while (balance < 0) {
                    int index = builder.lastIndexOf(Character.toString(right));
                    if (index < 0) {
                        break;
                    }
                    erase(builder, index, index + 1);
                    balance++;
                }
            } else if (balance > 0) {
                if (offset >= source.length()) {
                    while (balance > 0) {
                        builder.append(right);
                        balance--;
                    }
                } else {
                    while (balance > 0 && offset - balance >= 0) {
                        // we try to insert them if there are enough whitespaces
                        char current = source.charAt(offset - balance);
                        if (Character.isWhitespace(current)) {
                            builder.replace(offset - balance,
                                    offset - balance + 1, Character.toString(right));
                            balance--;
                        } else {
                            return false;
                        }
                    }
                    if (balance > 0) {
                        return false;
                    }
                }
            }
            context.setSanitizedSource(builder.toString());
            context.setSanitization(sanitizing);
            return true;
        }
        return false;
    }
    
    @Override
    public final Result getResult(Task task) throws ParseException {
        return lastResult;
    }

    @Override
    public final void addChangeListener(@NonNull final ChangeListener changeListener) {
        LOGGER.log(Level.FINE, "Adding changeListener: {0}", changeListener); //NOI18N)
        listeners.addChangeListener(changeListener);
    }

    @Override
    public final void removeChangeListener(@NonNull final ChangeListener changeListener) {
        LOGGER.log(Level.FINE, "Removing changeListener: {0}", changeListener); //NOI18N)
        listeners.removeChangeListener(changeListener);
    }

    protected final void fireChange() {
        listeners.fireChange();
    }

    private static void erase(StringBuilder builder, int start, int end) {
        builder.delete(start, end);
        for (int i = start; i < end; i++) {
            builder.insert(i, ' ');
        }
    }
    
    /**
     * Parsing context
     */
    protected final static class Context {

        private static final List<JsTokenId> IMPORT_EXPORT = new ArrayList<JsTokenId>(2);

        static {
            Collections.addAll(IMPORT_EXPORT, JsTokenId.KEYWORD_IMPORT, JsTokenId.KEYWORD_EXPORT);
        }
        
        private final String name;
        
        private final Snapshot snapshot;

        private final int caretOffset;
        
        private final Language<JsTokenId> language;
        
        private String source;

        private String sanitizedSource;

        private Sanitize sanitization;
        
        private Boolean isModule = null;

        Context(String name, Snapshot snapshot, int caretOffset, Language<JsTokenId> language) {
            this.name = name;
            this.snapshot = snapshot;
            this.caretOffset = caretOffset;
            this.language = language;
        }

        public String getName() {
            return name;
        }

        public Snapshot getSnapshot() {
            return snapshot;
        }

        public int getCaretOffset() {
            return snapshot.getEmbeddedOffset(caretOffset);
        }

        public String getSource() {
            if (sanitizedSource != null) {
                return sanitizedSource;
            }
            return getOriginalSource();
        }

        public String getOriginalSource() {
            if (source == null) {
                source = snapshot.getText().toString();
            }
            return source;
        }
        
        public String getSanitizedSource() {
            return sanitizedSource;
        }

        public void setSanitizedSource(String sanitizedSource) {
            this.sanitizedSource = sanitizedSource;
        }

        public Sanitize getSanitization() {
            return sanitization;
        }

        public void setSanitization(Sanitize sanitization) {
            this.sanitization = sanitization;
        }

        public boolean isModule() {
            if (isModule == null) {
                isModule = isModule(snapshot, language);
            }
            return isModule;
        }

        private static boolean isModule(Snapshot snapshot, Language<JsTokenId> language) {
            if (BaseParserResult.isEmbedded(snapshot)) {
                return isModule(snapshot, language, 0, Integer.MAX_VALUE);
            } else {
                TokenSequence<? extends JsTokenId> seq = LexUtilities.getJsPositionedSequence(snapshot, 0);
                if (seq == null) {
                    return false;
                } else {
                    Token<? extends JsTokenId> token = LexUtilities.findNextToken(seq, IMPORT_EXPORT);
                    if (token != null && IMPORT_EXPORT.contains(token.id())) {
                        return true;
                    }
                }
            }
            return false;
        }

        private static boolean isModule(Snapshot snapshot, Language<JsTokenId> language, int offset, int max) {
            assert BaseParserResult.isEmbedded(snapshot);
            TokenSequence<? extends JsTokenId> seq = LexUtilities.getNextJsTokenSequence(
                snapshot, offset, Integer.MAX_VALUE, language);
            if (seq != null) {
                Token<? extends JsTokenId> token = LexUtilities.findNextToken(seq, IMPORT_EXPORT);
                if (token != null) {
                    if (IMPORT_EXPORT.contains(token.id())) {
                        return true;
                    } else {
                        return isModule(snapshot, language, seq.offset() + token.length(), max);
                    }
                }
            }
            return false;
        }
    }

    /** Attempts to sanitize the input buffer */
    public static enum Sanitize {
        /** Only parse the current file accurately, don't try heuristics */
        NEVER {

            @Override
            public Sanitize next() {
                return NEVER;
            }
        },

        /** Perform no sanitization */
        NONE {

            @Override
            public Sanitize next() {
                return MISSING_CURLY;
            }
        },
        
        /** Attempt to fix missing } */
        MISSING_CURLY {

            @Override
            public Sanitize next() {
                return MISSING_SEMICOLON;
            }
        },

        /** Attempt to fix missing } */
        MISSING_SEMICOLON {

            @Override
            public Sanitize next() {
                return SYNTAX_ERROR_CURRENT;
            }
        },
        
        /** Remove current error token */
        SYNTAX_ERROR_CURRENT {

            @Override
            public Sanitize next() {
                return SYNTAX_ERROR_PREVIOUS;
            }
        },
        
        /** Remove token before error */
        SYNTAX_ERROR_PREVIOUS {

            @Override
            public Sanitize next() {
                return MISSING_PAREN;
            }
        },

        MISSING_PAREN {

            @Override
            public Sanitize next() {
                return EDITED_DOT;
            }
        },
        
        /** Try to remove the trailing . or :: at the caret line */
        EDITED_DOT {

            @Override
            public Sanitize next() {
                return ERROR_DOT;
            }
        },
        
        /** 
         * Try to remove the trailing . at the error position, or the prior
         * line.
         */
        ERROR_DOT {

            @Override
            public Sanitize next() {
                return ERROR_LINE;
            }
        },
        
        /** Try to cut out the error line */
        ERROR_LINE {

            @Override
            public Sanitize next() {
                return EDITED_LINE;
            }
        },
        
        /** Try to cut out the current edited line, if known */
        EDITED_LINE {

            @Override
            public Sanitize next() {
                return PREVIOUS_LINES;
            }
        },

        PREVIOUS_LINES {

            @Override
            public Sanitize next() {
                return NEVER;
            }
        };

        
        public abstract Sanitize next();
    }

    private static abstract class TokenCondition {

        public abstract boolean found(JsTokenId id);

    }

}
