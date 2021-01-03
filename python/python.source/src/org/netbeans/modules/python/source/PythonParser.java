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
package org.netbeans.modules.python.source;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.python.api.PythonFileEncodingQuery;
import org.openide.filesystems.FileObject;
import org.python.antlr.runtime.ANTLRStringStream;
import org.python.antlr.runtime.BaseRecognizer;
import org.python.antlr.runtime.BitSet;
import org.python.antlr.runtime.CommonToken;
import org.python.antlr.runtime.CommonTokenStream;
import org.python.antlr.runtime.IntStream;
import org.python.antlr.runtime.Lexer;
import org.python.antlr.runtime.MismatchedTokenException;
import org.python.antlr.runtime.RecognitionException;

import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.python.antlr.ListErrorHandler;
import org.python.antlr.ParseException;
import org.python.antlr.PythonLexer;
import org.python.antlr.PythonTokenSource;
import org.python.antlr.PythonTree;
import org.python.antlr.PythonTreeAdaptor;
import org.python.antlr.base.expr;
import org.python.antlr.base.mod;
import org.python.antlr.base.slice;
import org.python.antlr.base.stmt;
import org.python.antlr.runtime.ANTLRReaderStream;
import org.python.antlr.runtime.CharStream;
import org.python.core.PyException;

/**
 * Parser for Python. Wraps Jython.
 * 
 */
public class PythonParser extends Parser {
    /** For unit tests such that they can make sure we didn't have a parser abort */
    static Throwable runtimeException;

    static {
        org.python.core.PySystemState.initialize();
    }
    
    private Result lastResult;
    private final PythonFileEncodingQuery fileEncodingQuery = new PythonFileEncodingQuery();
    private String headerCached = null;
    private String encodingCache = null;

    public mod file_input(CharStream charStream, String fileName) throws RecognitionException {
        ListErrorHandler eh = new ListErrorHandler();
        mod tree = null;
        PythonLexer lexer = new PythonLexer(charStream);
        lexer.setErrorHandler(eh);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.discardOffChannelTokens(true);
        PythonTokenSource indentedSource = new PythonTokenSource(tokens, fileName);
        tokens = new CommonTokenStream(indentedSource);
        org.python.antlr.PythonParser parser = new org.python.antlr.PythonParser(tokens);
        parser.setTreeAdaptor(new PythonTreeAdaptor());
        parser.setErrorHandler(eh);
        org.python.antlr.PythonParser.file_input_return r = parser.file_input();
        tree = (mod)r.getTree();
        return tree;
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {}

    @Override
    public void removeChangeListener(ChangeListener changeListener) {}
    
    public PythonTree parse(InputStream istream, String fileName) throws Exception {
        InputStreamReader reader = new InputStreamReader(istream, "ISO-8859-1");
        return file_input(new ANTLRReaderStream(reader), fileName);
    }
    
    @Override
    public final Result getResult(Task task) throws org.netbeans.modules.parsing.spi.ParseException {
        return lastResult;
    }
    
    private static final Logger LOG = Logger.getLogger(PythonParser.class.getName());

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws org.netbeans.modules.parsing.spi.ParseException {
        Context context = new Context();
        context.snapshot = snapshot;
        context.event = event;
        context.task = task;
        context.caretOffset = GsfUtilities.getLastKnownCaretOffset(snapshot, event);
        context.source = snapshot.getText().toString();
        context.file = snapshot.getSource().getFileObject();
        if(context.file == null) {
            return; // TODO: parse the source, not the file
        }
        /* Let's not sanitize ;-) Would be great if we could have a more robust parser
        if (context.caretOffset != -1) {
            context.sanitized = Sanitize.EDITED_DOT;
        }
        */
        lastResult = parse(context, context.sanitized);
    }
    public PythonParserResult parse(final Context context, Sanitize sanitizing) {
        boolean sanitizedSource = false;
        String sourceCode = context.source;
        if (!((sanitizing == Sanitize.NONE) || (sanitizing == Sanitize.NEVER))) {
            boolean ok = sanitizeSource(context, sanitizing);

            if (ok) {
                assert context.sanitizedSource != null;
                sanitizedSource = true;
                sourceCode = context.sanitizedSource;
            } else {
                // Try next trick
                return sanitize(context, sanitizing);
            }
        }
        final String source = sourceCode;

        if (sanitizing == Sanitize.NONE) {
            context.errorOffset = -1;
        }

        final List<Error> errors = new ArrayList<>();
        final FileObject file = context.file;
        try {
            String fileName = file.getNameExt();
            // TODO - sniff file headers etc. Frank's comment:
            // Longer term for Python compatibility, having NetBeans sniff the top two lines
            // for an encoding would be the right thing to do from a pure Python
            // compatibility standard (see http://www.python.org/dev/peps/pep-0263/) I
            // have pep-0263 code in Jython that I could probably extract for this
            // purpose down the road.
            //String charset = "ISO8859_1"; // NOI18N
            //String charset = "UTF-8"; // NOI18N
            //String charset = "iso8859_1"; // NOI18N
            // TODO: improve this check.
            int cache_len = sourceCode.length() >= 64 ? 64 : sourceCode.length();
            if (headerCached == null || cache_len != headerCached.length() || !headerCached.equals(sourceCode.substring(0, cache_len))) {
                headerCached = sourceCode.substring(0, cache_len);
                encodingCache = fileEncodingQuery.getPythonFileEncoding(sourceCode.split("\n", 2));                
            }
            String charset = encodingCache;            
                
            final boolean ignoreErrors = sanitizedSource;
            ListErrorHandler errorHandler = new ListErrorHandler() {
                @Override
                public void error(String message, PythonTree t) {
                    errors.add(new DefaultError(null, message, null, file, t.getCharStartIndex(), t.getCharStopIndex(), Severity.ERROR));
                    super.error(message, t);
                }

                @Override
                public expr errorExpr(PythonTree t) {
                    return super.errorExpr(t);
                }

                @Override
                public mod errorMod(PythonTree t) {
                    return super.errorMod(t);
                }

                @Override
                public slice errorSlice(PythonTree t) {
                    return super.errorSlice(t);
                }

                @Override
                public stmt errorStmt(PythonTree t) {
                    return super.errorStmt(t);
                }

                @Override
                public boolean mismatch(BaseRecognizer br, IntStream input, int ttype, BitSet follow) {
                    return super.mismatch(br, input, ttype, follow);
                }

                @Override
                public Object recoverFromMismatchedToken(BaseRecognizer br, IntStream input, int ttype, BitSet follow) {
                    MismatchedTokenException mt = new MismatchedTokenException(ttype, input);
                    String message = br.getErrorMessage(mt, br.getTokenNames());
                    if (mt.line >= 1) {
                        int lineOffset = findLineOffset(context.source, mt.line-1);
                        if (mt.charPositionInLine > 0) {
                            lineOffset += mt.charPositionInLine;
                        }
                        int start = lineOffset;//t.getCharStartIndex();
                        int stop = lineOffset;//t.getCharStopIndex();
                        errors.add(new DefaultError(null, message, null, file, start, stop, Severity.ERROR));
                    }
                    return super.recoverFromMismatchedToken(br, input, ttype, follow);
                }

                @Override
                public void recover(Lexer lex, RecognitionException re) {
                    super.recover(lex, re);
                }

                @Override
                public void recover(BaseRecognizer br, IntStream input, RecognitionException re) {
                    super.recover(br, input, re);
                }

                @Override
                public void reportError(BaseRecognizer br, RecognitionException re) {
                    if (!ignoreErrors) {
                        String message = br.getErrorMessage(re, br.getTokenNames());
                        if (message == null || message.length() == 0) {
                            message = re.getMessage();
                        }
                        if (message == null) {
                            //message = re.getUnexpectedType();
                            message = re.toString();
                        }
                        int start = re.index;

                        // Try to find the line offset. re.index doesn't do the trick.
                        start = PythonUtils.getOffsetByLineCol(source, re.line - 1, 0); // -1: 0-based
                        int end = start;
                        if (re.charPositionInLine > 0) {
                            try {
                                end = GsfUtilities.getRowLastNonWhite(source, start) + 1;
                                start += re.charPositionInLine;
                                if (end < start) {
                                    end = start;
                                }
                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                                end = start;
                            }
                            if (end == 0) {
                                end = start;
                            }
                        }

                        // Some errors have better offsets if we look at the token stream
                        if (re instanceof MismatchedTokenException) {
                            MismatchedTokenException m = (MismatchedTokenException)re;
                            if (m.token != null) {
                                if (m.token instanceof org.python.antlr.runtime.CommonToken) {
                                    CommonToken token = (org.python.antlr.runtime.CommonToken)m.token;
                                    start = token.getStartIndex();
                                    end = token.getStopIndex();
                                }
                            }
                        }

                        if (start > source.length()) {
                            start = source.length();
                            end = start;
                        }

                        errors.add(new DefaultError(null, message, null, file, start, end, Severity.ERROR));

                        // In order to avoid a StackOverflowError, the BaseRecognizer must be recreated.
                        // We must keep the names of the tokens to avoid a NullPointerException.
                        // See bz252630
                        final String[] tokenNames = br.getTokenNames();
                        br = new BaseRecognizer() {

                            @Override
                            public String getSourceName() {
                                return file.getName();
                            }

                            @Override
                            public String[] getTokenNames() {
                                return tokenNames;
                            }
                        };

                        super.reportError(br, re);
                    }
                }
            };

            PythonLexer lexer = new PythonLexer(new ANTLRStringStream(sourceCode));
            lexer.setErrorHandler(errorHandler);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.discardOffChannelTokens(true);
            PythonTokenSource indentedSource = new PythonTokenSource(tokens, fileName);
            CommonTokenStream indentedTokens = new CommonTokenStream(indentedSource);
            // Import line ending with a dot raise a NullPointerException in
            // org.python.antlr.GrammarActions.makeDottedText called from parser.file_input
            // sanitizeImportTokens will remove the dot token from the list of tokens in
            // indentedTokens to avoid the bug and add an error at this file.
            // See https://netbeans.org/bugzilla/show_bug.cgi?id=252356
            sanitizeImportTokens(indentedTokens, errors, file);
            org.python.antlr.PythonParser parser;
            if (charset != null) {
                parser = new org.python.antlr.PythonParser(indentedTokens, charset);
            } else {
                parser = new org.python.antlr.PythonParser(indentedTokens);
            }
            parser.setTreeAdaptor(new PythonTreeAdaptor());
            parser.setErrorHandler(errorHandler);
            org.python.antlr.PythonParser.file_input_return r = parser.file_input();
            PythonTree t = (PythonTree)r.getTree();
            PythonParserResult result = new PythonParserResult(t, context.snapshot);
            result.setErrors(errors);

            result.setSanitized(context.sanitized, context.sanitizedRange, context.sanitizedContents);
            result.setSource(sourceCode);

            return result;
        } catch (ParseException pe) {
            if (sanitizing == Sanitize.NONE) {
                PythonParserResult sanitizedResult = sanitize(context, sanitizing);
                if (sanitizedResult.isValid()) {
                    return sanitizedResult;
                } else {
                    int offset = pe.index;
                    assert offset >= 0;
                    String desc = pe.getLocalizedMessage();
                    if (desc == null) {
                        desc = pe.getMessage();
                    }
                    DefaultError error = new DefaultError(null /*key*/, desc, null, file, offset, offset, Severity.ERROR);
                    PythonParserResult parserResult = new PythonParserResult(null, context.snapshot);
                    parserResult.addError(error);
                    for (Error e : errors) {
                        parserResult.addError(e);
                    }

                    return parserResult;
                }
            } else {
                return sanitize(context, sanitizing);
            }

        } catch (PyException e) {
            // This is issue 251705
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, e.getMessage());
            return new PythonParserResult(null, context.snapshot);
        } catch (IllegalArgumentException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, e.getMessage());
            return new PythonParserResult(null, context.snapshot);
        } catch (NullPointerException e) {
            String fileName = "";
            if (file != null) {
                fileName = FileUtil.getFileDisplayName(file);
            }
            e = Exceptions.attachMessage(e, "Was parsing " + fileName);
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, e.getMessage());
            return new PythonParserResult(null, context.snapshot);
        } catch (Throwable t) {
            runtimeException = t;
            StackTraceElement[] stackTrace = t.getStackTrace();
            if (stackTrace != null && stackTrace.length > 0 && stackTrace[0].getClassName().startsWith("org.python.antlr")) {//.runtime.tree.RewriteRuleElementStream")) {
                // This is issue 150921
                // Don't bug user about it -- we already know
                Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Encountered issue #150921", t);
            } else {
                t = Exceptions.attachMessage(t, "Was parsing " + FileUtil.getFileDisplayName(file));
                Exceptions.printStackTrace(t);
            }
            return new PythonParserResult(null, context.snapshot);
        }
    }

    private void sanitizeImportTokens(CommonTokenStream indentedTokens, List errors, FileObject file) {
        List tokens = indentedTokens.getTokens();
        List<CommonToken> tokensToRemove = new ArrayList<>();
        int i = 0;
        while (i < tokens.size()) {
            CommonToken importToken = (CommonToken)tokens.get(i);
            if ("import".equals(importToken.getText()) || "from".equals(importToken.getText())) {
                // sanitizeDotTokens return the index of the token that starts the next line
                i = sanitizeDotTokens(tokens, tokensToRemove, importToken, i + 1, errors, file);
            } else {
                i++;
            }
        }

        for (CommonToken token : tokensToRemove) {
            tokens.remove(token);
        }
    }

    private int sanitizeDotTokens(List tokens, List tokensToRemove, CommonToken importToken,
            int startIndex, List errors, FileObject file) {
        for (int j = startIndex; j < tokens.size() - 1; j++) {
            CommonToken dotToken = (CommonToken)tokens.get(j);
            CommonToken nextToken = (CommonToken)tokens.get(j + 1);
            if (".".equals(dotToken.getText())) {
                if (nextToken.getText().startsWith("\n")) {
                    tokensToRemove.add(dotToken);
                    String rawTokenText;
                    if (nextToken.getText().startsWith("\n")) {
                        rawTokenText = "\\n";
                    } else {
                        rawTokenText = " ";
                    }
                    errors.add(
                        new DefaultError(null, "Mismatch input '.' expecting NAME\nMissing NAME at '" + rawTokenText + "'",
                            null, file, importToken.getStartIndex(), dotToken.getStopIndex(), Severity.ERROR));
                }
            } else if ("\n".equals(nextToken.getText())) { // End of line, must continue looping from external loop
                return j + 1;
            }
        }

        return startIndex;
    }

    private static String asString(CharSequence sequence) {
        if (sequence instanceof String) {
            return (String)sequence;
        } else {
            return sequence.toString();
        }
    }


    @SuppressWarnings("fallthrough")
    private PythonParserResult sanitize(final Context context, final Sanitize sanitizing) {

        switch (sanitizing) {
        case NEVER:
            return new PythonParserResult(null, context.snapshot);

        case NONE:
            if (context.caretOffset != -1) {
                return parse(context, Sanitize.EDITED_DOT);
            }

        case EDITED_DOT:
            // We've tried removing whitespace around the edit location
            // Fall through to try parsing with removing stuff around error location
            // (Don't bother doing this if errorOffset==caretOffset since that would try the same
            // source as EDITED_DOT which has no better chance of succeeding...)
            if (context.errorOffset != -1 && context.errorOffset != context.caretOffset) {
                return parse(context, Sanitize.ERROR_DOT);
            }

        // Fall through to try the next trick
        case ERROR_DOT:

            // We've tried removing dots - now try removing the whole line at the error position
            if (context.errorOffset != -1) {
                return parse(context, Sanitize.ERROR_LINE);
            }

        // Fall through to try the next trick
        case ERROR_LINE:

            // Messing with the error line didn't work - we could try "around" the error line
            // but I'm not attempting that now.
            // Finally try removing the whole line around the user editing position
            // (which could be far from where the error is showing up - but if you're typing
            // say a new "def" statement in a class, this will show up as an error on a mismatched
            // "end" statement rather than here
            if (context.caretOffset != -1) {
                return parse(context, Sanitize.EDITED_LINE);
            }

        // Fall through for default handling
        case EDITED_LINE:
        default:
            // We're out of tricks - just return the failed parse result
            return new PythonParserResult(null, context.snapshot);
        }
    }

    /**
     * Try cleaning up the source buffer around the current offset to increase
     * likelihood of parse success. Initially this method had a lot of
     * logic to determine whether a parse was likely to fail (e.g. invoking
     * the isEndMissing method from bracket completion etc.).
     * However, I am now trying a parse with the real source first, and then
     * only if that fails do I try parsing with sanitized source. Therefore,
     * this method has to be less conservative in ripping out code since it
     * will only be used when the regular source is failing.
     *
     * @todo Automatically close current statement by inserting ";"
     * @todo Handle sanitizing "new ^" from parse errors
     * @todo Replace "end" insertion fix with "}" insertion
     */
    private boolean sanitizeSource(Context context, Sanitize sanitizing) {
        int offset = context.caretOffset;

        // Let caretOffset represent the offset of the portion of the buffer we'll be operating on
        if ((sanitizing == Sanitize.ERROR_DOT) || (sanitizing == Sanitize.ERROR_LINE)) {
            offset = context.errorOffset;
        }

        // Don't attempt cleaning up the source if we don't have the buffer position we need
        if (offset == -1) {
            return false;
        }

        // The user might be editing around the given caretOffset.
        // See if it looks modified
        // Insert an end statement? Insert a } marker?
        String doc = context.source;
        if (offset > doc.length()) {
            return false;
        }

        try {
            // Sometimes the offset shows up on the next line
            if (GsfUtilities.isRowEmpty(doc, offset) || GsfUtilities.isRowWhite(doc, offset)) {
                offset = GsfUtilities.getRowStart(doc, offset) - 1;
                if (offset < 0) {
                    offset = 0;
                }
            }

            if (!(GsfUtilities.isRowEmpty(doc, offset) || GsfUtilities.isRowWhite(doc, offset))) {
                if ((sanitizing == Sanitize.EDITED_LINE) || (sanitizing == Sanitize.ERROR_LINE)) {
                    // See if I should try to remove the current line, since it has text on it.
                    int lineEnd = GsfUtilities.getRowLastNonWhite(doc, offset);

                    if (lineEnd != -1) {
                        lineEnd++; // lineEnd is exclusive, not inclusive
                        StringBuilder sb = new StringBuilder(doc.length());
                        int lineStart = GsfUtilities.getRowStart(doc, offset);
                        if (lineEnd >= lineStart + 2) {
                            sb.append(doc.substring(0, lineStart));
                            sb.append("//");
                            int rest = lineStart + 2;
                            if (rest < doc.length()) {
                                sb.append(doc.substring(rest, doc.length()));
                            }
                        } else {
                            // A line with just one character - can't replace with a comment
                            // Just replace the char with a space
                            sb.append(doc.substring(0, lineStart));
                            sb.append(" ");
                            int rest = lineStart + 1;
                            if (rest < doc.length()) {
                                sb.append(doc.substring(rest, doc.length()));
                            }

                        }

                        assert sb.length() == doc.length();

                        context.sanitizedRange = new OffsetRange(lineStart, lineEnd);
                        context.sanitizedSource = sb.toString();
                        context.sanitizedContents = doc.substring(lineStart, lineEnd);
                        return true;
                    }
                } else {
                    assert sanitizing == Sanitize.ERROR_DOT || sanitizing == Sanitize.EDITED_DOT;
                    // Try nuking dots/colons from this line
                    // See if I should try to remove the current line, since it has text on it.
                    int lineStart = GsfUtilities.getRowStart(doc, offset);
                    int lineEnd = offset - 1;
                    while (lineEnd >= lineStart && lineEnd < doc.length()) {
                        if (!Character.isWhitespace(doc.charAt(lineEnd))) {
                            break;
                        }
                        lineEnd--;
                    }
                    if (lineEnd > lineStart) {
                        StringBuilder sb = new StringBuilder(doc.length());
                        String line = doc.substring(lineStart, lineEnd + 1);
                        int removeChars = 0;
                        int removeEnd = lineEnd + 1;
                        boolean isLineEnd = GsfUtilities.getRowLastNonWhite(context.source, lineEnd) <= lineEnd;

                        if (line.endsWith(".")) { // NOI18N
                            removeChars = 1;
                        } else if (line.endsWith("(")) { // NOI18N
                            if (isLineEnd) {
                                removeChars = 1;
                            }
                        } else if (line.endsWith(",")) { // NOI18N                            removeChars = 1;
                            if (!isLineEnd) {
                                removeChars = 1;
                            }
                        } else if (line.endsWith(", ")) { // NOI18N
                            if (!isLineEnd) {
                                removeChars = 2;
                            }
                        } else if (line.endsWith(",)")) { // NOI18N
                            // Handle lone comma in parameter list - e.g.
                            // type "foo(a," -> you end up with "foo(a,|)" which doesn't parse - but
                            // the line ends with ")", not "," !
                            // Just remove the comma
                            removeChars = 1;
                            removeEnd--;
                        } else if (line.endsWith(", )")) { // NOI18N
                            // Just remove the comma
                            removeChars = 1;
                            removeEnd -= 2;
                        } else if (line.endsWith(" def") && isLineEnd) { // NOI18N
                            removeChars = 3;
                        } else {
//                            // Make sure the line doesn't end with one of the JavaScript keywords
//                            // (new, do, etc) - we can't handle that!
//                            for (String keyword : PythonUtils.PYTHON_KEYWORDS) { // reserved words are okay
//                                if (line.endsWith(keyword)) {
//                                    if ("print".equals(keyword)) { // NOI18N
//                                        // Only remove the keyword if it's the end of the line. Otherwise,
//                                        // it could have just been typed in front of something (e.g. inserted a print) and we don't
//                                        // want to confuse the parser with "va foo" instead of "var foo"
//                                        if (!isLineEnd) {
//                                            continue;
//                                        }
//                                    }
//                                    removeChars = 1;
//                                    break;
//                                }
//                            }
                        }

                        if (removeChars == 0) {
                            return false;
                        }

                        int removeStart = removeEnd - removeChars;

                        sb.append(doc.substring(0, removeStart));

                        for (int i = 0; i < removeChars; i++) {
                            sb.append(' ');
                        }

                        if (removeEnd < doc.length()) {
                            sb.append(doc.substring(removeEnd, doc.length()));
                        }
                        assert sb.length() == doc.length();

                        context.sanitizedRange = new OffsetRange(removeStart, removeEnd);
                        context.sanitizedSource = sb.toString();
                        context.sanitizedContents = doc.substring(removeStart, removeEnd);
                        return true;
                    }
                }
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }

        return false;
    }

    private static int findLineOffset(String source, int line) {
        int offset = -1;
        for (int i = 0; i < line; i++) {
            offset = source.indexOf("\n", offset+1);
            if (offset == -1) {
                return source.length();
            }
        }

        return Math.min(source.length(), offset+1);
    }

    /** Attempts to sanitize the input buffer */
    public static enum Sanitize {
        /** Only parse the current file accurately, don't try heuristics */
        NEVER,
        /** Perform no sanitization */
        NONE,
        /** Try to remove the trailing . or :: at the caret line */
        EDITED_DOT,
        /** Try to remove the trailing . or :: at the error position, or the prior
         * line, or the caret line */
        ERROR_DOT,
        /** Try to cut out the error line */
        ERROR_LINE,
        /** Try to cut out the current edited line, if known */
        EDITED_LINE,
    }

    /** Sanitize context */
    public static class Context {
        private FileObject file;
//        private ParseListener listener;
        private int errorOffset;
        private String source;
        private String sanitizedSource;
        private OffsetRange sanitizedRange = OffsetRange.NONE;
        private String sanitizedContents;
        private int caretOffset;
        private Sanitize sanitized = Sanitize.NONE;
//        private TranslatedSource translatedSource;
//        private Parser.Job job;
        private Snapshot snapshot;
        private Task task;
        private SourceModificationEvent event;
//
//        public Context(ParserFile parserFile, ParseListener listener, String source, int caretOffset, TranslatedSource translatedSource, Parser.Job job) {
//            this.file = parserFile;
//            this.listener = listener;
//            this.source = source;
//            this.caretOffset = caretOffset;
//            this.translatedSource = translatedSource;
//            this.job = job;
//
//
//            if (caretOffset != -1) {
//                sanitized = Sanitize.EDITED_DOT;
//            }
//        }
//
//        @Override
//        public String toString() {
//            return "PythonParser.Context(" + file.toString() + ")"; // NOI18N
//        }
//
//        public OffsetRange getSanitizedRange() {
//            return sanitizedRange;
//        }
//
//        public Sanitize getSanitized() {
//            return sanitized;
//        }
//
//        public String getSanitizedSource() {
//            return sanitizedSource;
//        }
//
//        public int getErrorOffset() {
//            return errorOffset;
//        }
    }
}
