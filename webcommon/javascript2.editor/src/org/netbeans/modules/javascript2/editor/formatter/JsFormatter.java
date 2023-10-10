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
package org.netbeans.modules.javascript2.editor.formatter;

import com.oracle.js.parser.ir.FunctionNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.javascript2.json.parser.JsonParser;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Hejl
 */
public class JsFormatter implements Formatter {

    // only for tests
    static final Object CT_HANDLER_DOC_PROPERTY = "code-template-insert-handler"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(JsFormatter.class.getName());

    private static final Pattern MOOTOOLS_COMMENT = Pattern.compile("/\\*\n---\\s*\n((.|\n)+)\n\\.\\.\\.\\s*\n\\*/");

    private static final boolean ELSE_IF_SINGLE_LINE = true;

    private final Language<JsTokenId> language;

    private final Defaults.Provider provider;

    private int lastOffsetDiff = 0;

    private final Set<FormatToken> processed = new HashSet<>();

    private final Stack<FormatToken> openedBraces = new Stack<>();

    public JsFormatter(Language<JsTokenId> language) {
        this.language = language;
        this.provider = Defaults.getInstance(language.mimeType());
    }

    @Override
    public int hangingIndentSize() {
        return CodeStyle.get((Document) null, provider).getContinuationIndentSize();
    }

    @Override
    public int indentSize() {
        return CodeStyle.get((Document) null, provider).getIndentSize();
    }

    @Override
    public boolean needsParserResult() {
        return true;
    }

    @Override
    public void reformat(final Context context, final ParserResult compilationInfo) {
        processed.clear();
        lastOffsetDiff = 0;
        final Document doc = context.document();
        final boolean templateEdit = doc.getProperty(CT_HANDLER_DOC_PROPERTY) != null;
        AtomicLockDocument ald = LineDocumentUtils.as(doc, AtomicLockDocument.class);

        @SuppressWarnings("Convert2Lambda") // Converting this to lambda causes class loading issues
        Runnable r = new Runnable() {
            @Override
            public void run() {
                long startTime = System.nanoTime();

                TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(
                        compilationInfo.getSnapshot().getTokenHierarchy(), context.startOffset(), language);

                if (ts == null) {
                    return;
                }

                FormatTokenStream tokenStream = FormatTokenStream.create(
                        context, ts, 0, context.endOffset());
                LOGGER.log(Level.FINE, "Format token stream creation: {0} ms",
                        (System.nanoTime() - startTime) / 1000000);

                startTime = System.nanoTime();
                JsFormatVisitor visitor = new JsFormatVisitor(tokenStream,
                        ts, context.endOffset());

                if (!(compilationInfo instanceof org.netbeans.modules.javascript2.types.spi.ParserResult)) {
                    throw new IllegalArgumentException(String.valueOf(compilationInfo));
                }
                org.netbeans.modules.javascript2.types.spi.ParserResult result
                        = (org.netbeans.modules.javascript2.types.spi.ParserResult) compilationInfo;
                final FormatContext formatContext;
                final JsonParser.JsonContext json = result.getLookup().lookup(JsonParser.JsonContext.class);
                if (json != null) {
                    JsonFormatVisitor jsonVisitor = new JsonFormatVisitor(tokenStream, ts, context.endOffset());
                    json.accept(jsonVisitor);
                    formatContext = new FormatContext(context, provider, compilationInfo.getSnapshot(),
                            language, null, tokenStream);
                } else {
                    final FunctionNode fun = result.getLookup().lookup(FunctionNode.class);
                    if (fun != null) {
                        fun.accept(visitor);
                    } else {
                        LOGGER.log(Level.FINE, "Format visitor not executed; no root node");
                    }
                    formatContext = new FormatContext(context, provider, compilationInfo.getSnapshot(),
                            language, fun, tokenStream);
                }
                LOGGER.log(Level.FINE, "Format visitor: {0} ms",
                        (System.nanoTime() - startTime) / 1000000);

                startTime = System.nanoTime();

                CodeStyle.Holder codeStyle = CodeStyle.get(formatContext).toHolder();

                int indentLevelSize = codeStyle.indentSize;

                int initialIndent = codeStyle.initialIndent;
                int continuationIndent = codeStyle.continuationIndentSize;

                List<FormatToken> tokens = tokenStream.getTokens();
                if (LOGGER.isLoggable(Level.FINE)) {
                    for (FormatToken token : tokens) {
                        LOGGER.log(Level.FINE, token.toString());
                    }
                }

                // when the start offset != 0 this flag indicates when we
                // got after the start so we may change document now
                boolean started = false;
                boolean firstTokenFound = false;
                Stack<FormatContext.ContinuationBlock> continuations = new Stack<>();

                // clear the stack of currently opened braces
                openedBraces.clear();
                for (int i = 0; i < tokens.size(); i++) {
                    FormatToken token = tokens.get(i);
                    if (!started && !token.isVirtual() && token.getOffset() >= context.startOffset()) {
                        started = true;
                    }

                    // only if not expanding tabs otherwise the real size is
                    // already included in offset diff
                    // count real tab length for potential if-long line wrapping
                    if (!codeStyle.expandTabsToSpaces && !token.isVirtual()) {
                        if (token.getId() == JsTokenId.WHITESPACE) {
                            CharSequence text = token.getText();
                            for (int j = 0; j < text.length(); j++) {
                                if (text.charAt(j) == '\t') { // NOI18N
                                    formatContext.incTabCount();
                                }
                            }
                        } else if (token.getKind() == FormatToken.Kind.EOL) {
                            formatContext.resetTabCount();
                        }
                    }

                    boolean tokenProcessed = false;
                    if (processed.remove(token)) {
                        // we should format brace according to the user's code style
                        // else/while/catch/finally keyword placement as well
                        if (token.getKind().isBraceMarker() || token.getKind().isAlignmentMarker()) {
                            tokenProcessed = true;
                        } else {
                            // we can skip this token, since it has been already processed
                            // when the last EOL had been reached
                            continue;
                        }
                    }

                    if (!token.isVirtual()) {
                        // if there is pending continuation (ususally on the same line)
                        // such as "   .something({..." we need to update
                        // the continuation data on (, { and [ the same way as
                        // it would be on EOL
                        boolean change = false;
                        if (formatContext.isPendingContinuation() && token.getKind() != FormatToken.Kind.EOL) {
                            change = updateContinuationStart(formatContext, token, continuations, false);
                        }
                        if (!change) {
                            updateContinuationEnd(formatContext, token, continuations);
                        }

                        if (!firstTokenFound) {
                            firstTokenFound = true;
                            formatContext.setCurrentLineStart(token.getOffset());
                        }
                        // we do not store last things for potentially
                        // removed/replaced tokens
                        if (token.getKind() != FormatToken.Kind.WHITESPACE
                                && token.getKind() != FormatToken.Kind.EOL) {
                            lastOffsetDiff = formatContext.getOffsetDiff();
                        }
                        initialIndent = formatContext.getEmbeddingIndent(tokenStream, token)
                                + codeStyle.initialIndent + formatContext.getJsxIndentation();
                    }

                    if (started && (token.getKind() == FormatToken.Kind.BLOCK_COMMENT
                            || token.getKind() == FormatToken.Kind.DOC_COMMENT
                            || token.getKind() == FormatToken.Kind.LINE_COMMENT)) {
                        try {
                            int indent = context.lineIndent(context.lineStartOffset(
                                    formatContext.getDocumentOffset(token.getOffset()) + formatContext.getOffsetDiff()));
                            formatComment(token, formatContext, codeStyle, indent);
                        } catch (BadLocationException ex) {
                            LOGGER.log(Level.INFO, null, ex);
                        }
                    } else if (started && token.getId() == JsTokenId.JSX_TEXT) {
                        formatJsx(tokens, formatContext, i, codeStyle, initialIndent, continuationIndent, continuations);
                    } else if (started && token.getKind().isBraceMarker()) {
                        formatBrace(tokens, i, formatContext, codeStyle, initialIndent, continuationIndent, continuations, tokenProcessed);
                    } else if (started && token.getKind().isAlignmentMarker()) {
                        formatKeywordAlignment(tokens, i, formatContext, codeStyle, initialIndent, continuationIndent, continuations);
                    } else if (started && token.getKind().isSpaceMarker()) {
                        formatSpace(tokens, i, formatContext, codeStyle);
                    } else if (started && token.getKind().isLineWrapMarker()) {
                        formatLineWrap(tokens, i, formatContext, codeStyle, initialIndent,
                                continuationIndent, continuations);
                    } else if (token.getKind().isIndentationMarker()
                            || token.getKind() == FormatToken.Kind.BEFORE_JSX_BLOCK_START
                            || token.getKind() == FormatToken.Kind.AFTER_JSX_BLOCK_END) {
                        updateIndentationLevel(token, formatContext, codeStyle);
                    } else if (token.getKind() == FormatToken.Kind.AFTER_END_BRACE) {
                        if (!openedBraces.isEmpty()
                                && getBracePlacement(openedBraces.pop(), codeStyle) == CodeStyle.BracePlacement.NEW_LINE_INDENTED) {
                            // token representing closing brace of block with "new line indented" indentation
                            // therefore decrease the indentation
                            formatContext.decIndentationLevel();
                        }
                    } else if (token.getKind() == FormatToken.Kind.SOURCE_START
                            || token.getKind() == FormatToken.Kind.EOL) {
                        if (started && token.getKind() != FormatToken.Kind.SOURCE_START) {
                            wrapOnEol(tokens, formatContext, i - 1, codeStyle,
                                    initialIndent, continuationIndent, continuations, 0);
                        }

                        if (started) {
                            // remove trailing spaces
                            removeTrailingSpaces(tokens, i, formatContext, token, templateEdit);
                        }
                        if (token.getKind() != FormatToken.Kind.SOURCE_START) {
                            formatContext.setCurrentLineStart(token.getOffset()
                                    + 1 + formatContext.getOffsetDiff());
                            formatContext.setLastLineWrap(null);
                        }

                        // following code handles the indentation
                        // do not do indentation for line comments starting
                        // at the beginning of the line to support comment/uncomment
                        FormatToken next = FormatTokenStream.getNextNonVirtual(token);
                        if (next != null && next.getKind() == FormatToken.Kind.LINE_COMMENT) {
                            continue;
                        }

                        FormatToken indentationStart = null;
                        FormatToken indentationEnd = null;
                        // we add tokens to processed to not to process them twice
                        for (int j = i + 1; j < tokens.size(); j++) {
                            FormatToken nextToken = tokens.get(j);
                            if (!nextToken.isVirtual()) {
                                if (nextToken.getOffset() >= context.startOffset()) {
                                    started = true;
                                }
                                if (nextToken.getKind() != FormatToken.Kind.WHITESPACE) {
                                    indentationEnd = nextToken;
                                    if (indentationStart == null) {
                                        indentationStart = nextToken;
                                    }
                                    break;
                                } else {
                                    if (indentationStart == null) {
                                        indentationStart = nextToken;
                                    }
                                }
                            } else {
                                updateIndentationLevel(nextToken, formatContext, codeStyle);
                            }
                            processed.add(nextToken);
                        }

                        // if it is code template formatting we want to do
                        // proper indentation even on a blank line
                        if (indentationEnd != null
                                && (indentationEnd.getKind() != FormatToken.Kind.EOL || templateEdit)) {

                            indentLine(token, formatContext, codeStyle, continuations, indentationEnd, indentationStart.getOffset(),
                                    initialIndent, indentLevelSize, continuationIndent, started, false, null, null, null);
                        }
                    }
                }
                // if it is end of file yet we have to do wrap if needed
                wrapOnEol(tokens, formatContext, tokens.size() - 1, codeStyle,
                        initialIndent, continuationIndent, continuations, 0);
                LOGGER.log(Level.FINE, "Formatting changes: {0} ms", (System.nanoTime() - startTime) / 1000000);
            }
        };
        if (ald == null) {
            r.run();
        } else {
            // review: shouldn't be runAsUser ?
            ald.runAtomic(r);
        }
    }

    private void indentLine(@NonNull FormatToken token, @NonNull FormatContext formatContext, @NonNull CodeStyle.Holder codeStyle,
            @NonNull Stack<FormatContext.ContinuationBlock> continuations, @NonNull FormatToken indentationEnd, int indentationStartOffset,
            int initialIndent, int indentLevelSize, int continuationIndent, boolean started, boolean noRealEol, @NullAllowed Indentation indentation,
            @NullAllowed Integer indentLevel, @NullAllowed Integer offsetDiff) {

        int indentationSize = initialIndent + (indentLevel != null ? indentLevel : formatContext.getIndentationLevel()) * indentLevelSize;
        int continuationLevel = formatContext.getContinuationLevel();
        if (isContinuation(formatContext, codeStyle, token, noRealEol)) {
            continuationLevel++;
            updateContinuationStart(formatContext, token, continuations, true);
        } else {
            formatContext.setPendingContinuation(false);
        }
        indentationSize += continuationIndent * continuationLevel;
        indentationSize += formatContext.getSuggestedIndentation(indentationEnd);
        if (started) {
            FormatToken indentationEndPrev = indentationEnd.previous();
            // last empty line formatted by html
            if (indentationEnd.getId() == JsTokenId.JSX_EXP_END
                    && !(indentationEndPrev != null && indentationEndPrev.getKind() == FormatToken.Kind.BEFORE_JSX_BLOCK_START)) {
                indentationSize = computeJsxIndentation(formatContext, indentationEnd.getOffset());
            }
            if (indentationSize >= 0) {
                Indentation checked = indentation == null
                        ? checkIndentation(formatContext.getDocument(), token, indentationEnd, formatContext, formatContext.getContext(), indentationSize)
                        : indentation;
                if (offsetDiff != null) {
                    formatContext.indentLineWithOffsetDiff(indentationStartOffset, indentationSize,
                            checked, offsetDiff, codeStyle);
                } else {
                    formatContext.indentLine(indentationStartOffset, indentationSize,
                            checked, codeStyle);
                }
            }
            if (indentationEndPrev != null
                    && indentationEndPrev.getKind() == FormatToken.Kind.BEFORE_JSX_BLOCK_START) {
                // it has just been updated
                formatContext.updateJsxIndentation(indentationEndPrev);
            }
        }
    }

    private static void removeTrailingSpaces(List<FormatToken> tokens, int index,
            FormatContext formatContext, FormatToken limit, boolean templateEdit) {

        // if it is code template we are doing indentation even on a blank line
        if (templateEdit && limit.getKind() == FormatToken.Kind.EOL) {
            return;
        }

        FormatToken start = null;
        for (int j = index - 1; j >= 0; j--) {
            FormatToken nextToken = tokens.get(j);
            if (!nextToken.isVirtual()
                    && nextToken.getKind() != FormatToken.Kind.WHITESPACE) {
                break;
            } else {
                start = tokens.get(j);
            }
        }
        while (start != null && start != limit) {
            if (!start.isVirtual()) {
                formatContext.remove(start.getOffset(),
                        start.getText().length());
            }
            start = start.next();
        }
    }

    private FormatToken wrapLine(FormatContext formatContext, CodeStyle.Holder codeStyle, FormatContext.LineWrap lastWrap,
            int initialIndent, int continuationIndent, Stack<FormatContext.ContinuationBlock> continuations) {
        // we dont have to remove trailing spaces as indentation will fix it
        formatContext.insertWithOffsetDiff(lastWrap.getToken().getOffset()
                + lastWrap.getToken().getText().length(), "\n", lastWrap.getOffsetDiff()); // NOI18N
        // there is + 1 for eol
        formatContext.setCurrentLineStart(lastWrap.getToken().getOffset()
                + lastWrap.getToken().getText().length() + 1 + lastWrap.getOffsetDiff());

        FormatToken indentationEnd = FormatTokenStream.getNextNonWhite(lastWrap.getToken(), false);
        assert indentationEnd != null;

        // do the indentation
        indentLine(lastWrap.getToken(), formatContext, codeStyle, continuations,
                indentationEnd, lastWrap.getToken().getOffset() + lastWrap.getToken().getText().length() + 1,
                initialIndent, IndentUtils.indentLevelSize(formatContext.getDocument()), continuationIndent,
                true, true, Indentation.ALLOWED, lastWrap.getIndentationLevel(), lastWrap.getOffsetDiff());
        formatContext.resetTabCount();
        return indentationEnd;
    }

    private FormatToken wrapOnEol(List<FormatToken> tokens, FormatContext formatContext, int index,
            CodeStyle.Holder codeStyle, int initialIndent, int continuationIndent,
            Stack<FormatContext.ContinuationBlock> continuations, int segmentAddition) {

        // search for token which will be present just before eol
        FormatToken tokenBeforeEol = null;
        for (int j = index; j >= 0; j--) {
            tokenBeforeEol = tokens.get(j);
            if (!tokenBeforeEol.isVirtual()) {
                break;
            }
        }
        if (tokenBeforeEol.getKind() != FormatToken.Kind.SOURCE_START) {
            int segmentLength = tokenBeforeEol.getOffset() + tokenBeforeEol.getText().length()
                    - formatContext.getCurrentLineStart() + lastOffsetDiff + segmentAddition;

            if (segmentLength >= codeStyle.rightMargin) {
                FormatContext.LineWrap lastWrap = formatContext.getLastLineWrap();
                if (lastWrap != null) {
                    // wrap it
                    return wrapLine(formatContext, codeStyle, lastWrap, initialIndent,
                            continuationIndent, continuations);
                }
            }
        }
        return null;
    }

    private void formatLineWrap(List<FormatToken> tokens, int index, FormatContext formatContext, CodeStyle.Holder codeStyle,
            int initialIndent, int continuationIndent, Stack<FormatContext.ContinuationBlock> continuations) {

        FormatToken token = tokens.get(index);
        CodeStyle.WrapStyle style = getLineWrap(token, formatContext, codeStyle);
        CodeStyle.BracePlacement bracePlacement = getBracePlacement(token, codeStyle);
        boolean wrapAlignedKeyword = isKeywordOnNewline(token, codeStyle);
        if (style == null && bracePlacement == CodeStyle.BracePlacement.PRESERVE_EXISTING && !wrapAlignedKeyword) {
            return;
        }

        if (formatContext.isEmbedded() && token.getKind() == FormatToken.Kind.AFTER_STATEMENT) {
            // we are in the embedded code, check whether it is single-line embedding like
            // <a href="#" onclick="edit(id); return false;">Edit</a>
            if (isSingleLineEmbedding(token, formatContext.getDocument(), formatContext.getSnapshot())) {
                // do not wrap line in case of the single-line embedding
                return;
            }
        }

        // search for token which will be present after eol
        FormatToken tokenAfterEol = token.next();
        int startIndex = index;
        while (tokenAfterEol != null && tokenAfterEol.getKind() != FormatToken.Kind.EOL
                && tokenAfterEol.getKind() != FormatToken.Kind.TEXT) {
            tokenAfterEol = tokenAfterEol.next();
            startIndex++;
        }

        // search for token which will be present just before eol
        FormatToken tokenBeforeEol = null;
        for (int j = startIndex - 1; j >= 0; j--) {
            tokenBeforeEol = tokens.get(j);
            if (!tokenBeforeEol.isVirtual()
                    && tokenBeforeEol.getKind() != FormatToken.Kind.WHITESPACE) {
                break;
            }
        }

        if (tokenBeforeEol != null && tokenBeforeEol.getKind() == FormatToken.Kind.EOL) {
            return;
        }
        // assert we can use the lastOffsetDiff and lastIndentationLevel
        assert tokenBeforeEol.getKind() != FormatToken.Kind.WHITESPACE
                && tokenBeforeEol.getKind() != FormatToken.Kind.EOL : tokenBeforeEol;

        if (style == CodeStyle.WrapStyle.WRAP_IF_LONG) {
            int segmentLength = tokenBeforeEol.getOffset() + tokenBeforeEol.getText().length()
                    - formatContext.getCurrentLineStart() + lastOffsetDiff;
            // compute real tab size
            int tabCount = formatContext.getTabCount();
            // otherwise the real size is alredy included in offset diff
            if (!codeStyle.expandTabsToSpaces) {
                segmentLength += (tabCount * codeStyle.tabSize) - tabCount;
            }

            if (segmentLength >= codeStyle.rightMargin) {
                FormatContext.LineWrap lastWrap = formatContext.getLastLineWrap();
                if (lastWrap != null
                        && (tokenAfterEol.getKind() != FormatToken.Kind.EOL || segmentLength > codeStyle.rightMargin)) {
                    // we dont have to remove trailing spaces as indentation will fix it
                    int offsetBeforeChanges = formatContext.getOffsetDiff();
                    // wrap it
                    wrapLine(formatContext, codeStyle, lastWrap, initialIndent,
                            continuationIndent, continuations);

                    List<FormatToken> eols = getExtendedTokenAfterEol(tokenAfterEol);
                    FormatToken extendedTokenAfterEol = eols.get(eols.size() - 1);
                    moveForward(token, extendedTokenAfterEol, formatContext, codeStyle, true);

                    // we need to mark the current wrap
                    formatContext.setLastLineWrap(new FormatContext.LineWrap(
                            tokenBeforeEol, lastOffsetDiff + (formatContext.getOffsetDiff() - offsetBeforeChanges),
                            formatContext.getIndentationLevel(), formatContext.getContinuationLevel()));
                    return;
                }
                // we proceed with wrapping if there is no wrap other than current
                // and we are longer than whats allowed
            } else {
                formatContext.setLastLineWrap(new FormatContext.LineWrap(
                        tokenBeforeEol, lastOffsetDiff,
                        formatContext.getIndentationLevel(), formatContext.getContinuationLevel()));
                return;
            }
        }

        List<FormatToken> eols = getExtendedTokenAfterEol(tokenAfterEol);
        FormatToken extendedTokenAfterEol = eols.get(eols.size() - 1);

        // statement like wrap is a bit special at least for now
        // we dont remove redundant eols for them
        if (tokenAfterEol != null
                // there is no eol
                && (tokenAfterEol.getKind() != FormatToken.Kind.EOL
                // or there are multiple lines and we are not after statement like token
                || extendedTokenAfterEol != tokenAfterEol && !isStatementWrap(token))) {

            // proceed the skipped tokens moving the main loop
            moveForward(token, extendedTokenAfterEol, formatContext, codeStyle, true);

            if (style != CodeStyle.WrapStyle.WRAP_NEVER
                    || bracePlacement == CodeStyle.BracePlacement.NEW_LINE
                    || bracePlacement == CodeStyle.BracePlacement.NEW_LINE_INDENTED
                    || wrapAlignedKeyword) {
                if (tokenAfterEol.getKind() != FormatToken.Kind.EOL) {

                    // we have to check the line length and wrap if needed
                    // FIXME duplicated code
                    int segmentLength = tokenBeforeEol.getOffset() + tokenBeforeEol.getText().length()
                            - formatContext.getCurrentLineStart() + lastOffsetDiff;

                    if (segmentLength >= codeStyle.rightMargin) {
                        FormatContext.LineWrap lastWrap = formatContext.getLastLineWrap();
                        if (lastWrap != null && tokenAfterEol.getKind() != FormatToken.Kind.EOL) {
                            // wrap it
                            wrapLine(formatContext, codeStyle, lastWrap, initialIndent,
                                    continuationIndent, continuations);
                        }
                    }

                    // we dont have to remove trailing spaces as indentation will fix it
                    formatContext.insert(tokenBeforeEol.getOffset() + tokenBeforeEol.getText().length(), "\n"); // NOI18N
                    // there is + 1 for eol
                    formatContext.setCurrentLineStart(tokenBeforeEol.getOffset()
                        + tokenBeforeEol.getText().length() + 1);
                    formatContext.setLastLineWrap(null);
                    // do the indentation
                    indentLine(tokenBeforeEol, formatContext, codeStyle, continuations, tokenAfterEol, tokenBeforeEol.getOffset() + tokenBeforeEol.getText().length(),
                            initialIndent, IndentUtils.indentLevelSize(formatContext.getDocument()), continuationIndent,
                            true, true, Indentation.ALLOWED, null, null);
                }

                if (extendedTokenAfterEol != tokenAfterEol) {

                    if (extendedTokenAfterEol != null) {
                        FormatToken end = getEndingEol(token, codeStyle, eols, extendedTokenAfterEol);
                        formatContext.remove(tokenAfterEol.getOffset(),
                                end.getOffset() - tokenAfterEol.getOffset());
                        // move to eol to do indentation in next cycle
                        // it is safe because we know the token to which we move is eol
                        processed.remove(extendedTokenAfterEol.previous());
                    } else {
                        FormatToken last = tokens.get(tokens.size() - 1);
                        while (last != null && last.isVirtual()) {
                            last = last.previous();
                        }
                        if (last != null) {
                            formatContext.remove(tokenAfterEol.getOffset(),
                                    last.getOffset() + last.getText().length() - tokenAfterEol.getOffset());
                        }
                    }
                }
            } else {
                int start = tokenBeforeEol.getOffset() + tokenBeforeEol.getText().length();

                FormatToken endToken = extendedTokenAfterEol;
                if (endToken == null) {
                    // end of file
                    endToken = tokens.get(tokens.size() - 1);
                    while (endToken != null && endToken.isVirtual()) {
                        endToken = endToken.previous();
                    }
                    if (endToken != null) {
                        formatContext.remove(start, endToken.getOffset() + endToken.getText().length() - start);
                    }
                } else if (endToken.getKind() != FormatToken.Kind.EOL) {
                    // no eol
                    FormatToken spaceStartToken = tokenBeforeEol.next();
                    if (spaceStartToken == null) {
                        spaceStartToken = tokenBeforeEol;
                    }

                    if (isSpace(spaceStartToken, formatContext, codeStyle, true, true)) {
                        formatContext.replace(start, endToken.getOffset() - start, " "); // NOI18N
                    } else {
                        formatContext.remove(start, endToken.getOffset() - start);
                    }
                } else if (tokenAfterEol != endToken) {
                    // multiple eols
                    formatContext.remove(start, endToken.getOffset() - start);
                }
            }
        }
    }

    private void formatSpace(List<FormatToken> tokens, int index, FormatContext formatContext, CodeStyle.Holder codeStyle) {
        FormatToken token = tokens.get(index);
        assert token.isVirtual();

        CodeStyle.WrapStyle style = getLineWrap(tokens, index, formatContext, codeStyle, true);
        // wrapping will take care of everything
        if (style == CodeStyle.WrapStyle.WRAP_ALWAYS) {
            return;
        }

        FormatToken lastEol = null;

        FormatToken start = null;
        for (FormatToken current = token.previous(); current != null;
                current = current.previous()) {

            if (!current.isVirtual()) {
                if (current.getKind() != FormatToken.Kind.WHITESPACE
                        && current.getKind() != FormatToken.Kind.EOL) {
                    start = current;
                    break;
                } else if (lastEol == null && current.getKind() == FormatToken.Kind.EOL) {
                    lastEol = current;
                }
            }
        }

        if (start == null) {
            return;
        }

        FormatToken end = null;
        List<FormatToken> foundEols = null;
        for (FormatToken current = token.next(); current != null;
                current = current.next()) {

            if (!current.isVirtual()) {
                if (current.getKind() != FormatToken.Kind.WHITESPACE
                        && current.getKind() != FormatToken.Kind.EOL) {
                    end = current;
                    break;
                } else if (current.getKind() == FormatToken.Kind.EOL) {
                    if (foundEols == null) {
                        foundEols = new ArrayList<>(2);
                    }
                    foundEols.add(current);
                    lastEol = current;
                }
            }
        }

        // we mark space and WRAP_NEVER tokens as processed
        for (FormatToken current = start; current != null && current != end;
                current = current.next()) {
            if (current.isVirtual()
                    && !current.getKind().isIndentationMarker()
                    && current.getKind() != FormatToken.Kind.BEFORE_JSX_BLOCK_START
                    && current.getKind() != FormatToken.Kind.AFTER_JSX_BLOCK_END
                    && getLineWrap(current, formatContext, codeStyle) != CodeStyle.WrapStyle.WRAP_IF_LONG) {
                processed.add(current);
            }
        }

        // FIXME if end is null we might be at EOF
        if (end != null) {
            // we fetch the space or next token to start
            start = FormatTokenStream.getNextNonVirtual(start);
            if (start == null) {
                return;
            }

            // we must not cross the region boundaries in embedded case (might happen for broken source)
            int regionEnd = formatContext.getEmbeddedRegionEnd(start.getOffset());
            if (regionEnd >= 0) {
                if (end.getOffset() > regionEnd) {
                    while (end != null && end != start && (end.isVirtual() || end.getOffset() > regionEnd)) {
                        end = end.previous();
                    }
                }
                if (lastEol != null && lastEol.getOffset() > regionEnd) {
                    while (lastEol != null && lastEol != start && (lastEol.isVirtual()
                            || lastEol.getOffset() > regionEnd || lastEol.getKind() != FormatToken.Kind.EOL)) {
                        lastEol = lastEol.previous();
                    }
                }
            }
            // end of cross the region fix

            boolean remove = !isSpace(token, formatContext, codeStyle, true, false);

            if (start.getKind() != FormatToken.Kind.WHITESPACE
                    && start.getKind() != FormatToken.Kind.EOL) {
                assert start == end : start + " " + end;
                if (!remove) {
                    formatContext.insert(start.getOffset(), " "); // NOI18N
                }
            } else {
                if (lastEol != null) {
                    end = lastEol;
                }
                if (foundEols != null) {
                    end = getEndingEol(token, codeStyle, foundEols, end);
                }
                // if it should be removed or there is eol (in fact space)
                // which will stay there
                if (remove || end.getKind() == FormatToken.Kind.EOL) {
                    if (start != end) {
                        formatContext.remove(start.getOffset(),
                                end.getOffset() - start.getOffset());
                    }
                    if (lastEol != null && start != end) {
                        moveForward(start, end, formatContext, codeStyle, false);
                    }
                } else {
                    formatContext.replace(start.getOffset(),
                            end.getOffset() - start.getOffset(), " "); // NOI18N
                }
            }
        }
    }

    private static boolean surroundingsContains(FormatToken token, Set<FormatToken.Kind> kinds) {
        assert token.isVirtual();

        FormatToken item = token;
        while (item != null && item.isVirtual()) {
            if (kinds.contains(item.getKind())) {
                return true;
            }
            item = item.next();
        }
        item = token.previous();
        while (item != null && item.isVirtual()) {
            if (kinds.contains(item.getKind())) {
                return true;
            }
            item = item.next();
        }
        return false;
    }

    private void formatComment(FormatToken comment, FormatContext formatContext, CodeStyle.Holder codeStyle, int indent) {
        // this assumes the firts line is already indented by EOL logic
        assert comment.getKind() == FormatToken.Kind.BLOCK_COMMENT
                || comment.getKind() == FormatToken.Kind.DOC_COMMENT
                || comment.getKind() == FormatToken.Kind.LINE_COMMENT;

        if (comment.getKind() == FormatToken.Kind.LINE_COMMENT) {
            return;
        }

        String text = comment.getText().toString();
        if (!text.contains("\n")) { // NOI18N
            return;
        }

        // mootools packager see issue #
        if (comment.getKind() == FormatToken.Kind.BLOCK_COMMENT
                && MOOTOOLS_COMMENT.matcher(text).matches()) { // NOI18N
            return;
        }

        for (int i = 0; i < text.length(); i++) {
            char single = text.charAt(i);
            if (single == '\n') { // NOI18N
                // following lines are + 1 indented
                formatContext.indentLine(comment.getOffset() + i + 1,
                        indent + 1, Indentation.ALLOWED, codeStyle);
            }
        }
    }

    private void formatJsx(List<FormatToken> tokens, FormatContext formatContext, int index,
            CodeStyle.Holder codeStyle, int initialIndent, int continuationIndent,
            Stack<FormatContext.ContinuationBlock> continuations) {
        // this assumes the first line is already indented by EOL logic
        FormatToken jsx = tokens.get(index);
        assert jsx.getId() == JsTokenId.JSX_TEXT;
        String text = jsx.getText().toString();

        for (int i = 0; i < text.length(); i++) {
            char single = text.charAt(i);
            Character next = text.length() > i + 1 ? text.charAt(i + 1) : null;
            formatContext.updateJsxPath(single, next);
            if (single == '\n') {
                // if the line is exceeding right margin we wrap on previous position
                FormatToken indentationEnd = wrapOnEol(tokens, formatContext, index - 1, codeStyle, initialIndent, continuationIndent, continuations, i);
                FormatToken indentationEndNext = indentationEnd == null ? null : indentationEnd.next();
                while (indentationEndNext != null && (indentationEndNext.isVirtual() || indentationEndNext.getOffset() < jsx.getOffset() + i)) {
                    if (indentationEndNext.getKind() == FormatToken.Kind.BEFORE_JSX_BLOCK_START) {
                        formatContext.updateJsxIndentation(indentationEndNext);
                    }
                    indentationEndNext = indentationEndNext.next();
                }
                int indent = computeJsxIndentation(formatContext, jsx.getOffset() + i + 1);
                if (indent >= 0) {
                    formatContext.indentLine(jsx.getOffset() + i + 1, indent, Indentation.ALLOWED, codeStyle);
                }
                formatContext.setCurrentLineStart(jsx.getOffset() + i + 1 + indent);
            }
        }
    }

    private static int computeJsxIndentation(FormatContext formatContext, int offset) {
        try {
            Context context = formatContext.getContext();
            int htmlIndent = context.lineIndent(context.lineStartOffset(offset
                    + formatContext.getOffsetDiff()));
            int indent = (htmlIndent - formatContext.getBaseJsxIndentation()) + formatContext.getJsxIndentation();
            return indent;
        } catch (BadLocationException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return -1;
    }

    private void formatBrace(List<FormatToken> tokens, int index, FormatContext formatContext, CodeStyle.Holder codeStyle,
            int initialIndent, int continuationIndent, Stack<FormatContext.ContinuationBlock> continuations, boolean tokenProcessed) {

        FormatToken token = tokens.get(index);
        if (token.next() != null && token.next().getId() == JsTokenId.BRACKET_LEFT_CURLY) {
            openedBraces.add(token);
        }

        if (getBracePlacement(token, codeStyle) == CodeStyle.BracePlacement.NEW_LINE) {
            formatLineWrap(tokens, index, formatContext, codeStyle, initialIndent,
                    continuationIndent, continuations);
        } else if (getBracePlacement(token, codeStyle) == CodeStyle.BracePlacement.NEW_LINE_INDENTED) {
            if (token.next() != null && token.next().getId() == JsTokenId.BRACKET_LEFT_CURLY) {
                formatLineWrap(tokens, index, formatContext, codeStyle, initialIndent,
                        continuationIndent, continuations);
            }
        } else if (getBracePlacement(token, codeStyle) == CodeStyle.BracePlacement.SAME_LINE) {
            boolean canFormatBrace = true;

            FormatToken startToken = null;
            for (int j = index - 1; j >= 0; j--) {
                FormatToken ft = tokens.get(j);
                if (ft.getKind() != FormatToken.Kind.WHITESPACE
                        && ft.getKind() != FormatToken.Kind.EOL) {
                    if (ft.getKind() == FormatToken.Kind.BLOCK_COMMENT
                            || ft.getKind() == FormatToken.Kind.LINE_COMMENT) {
                        // comment between the right parenthesis and left brace
                        // prevents from formatting on the same line
                        canFormatBrace = false;
                    }
                    break;
                }
                startToken = ft;
            }
            FormatToken endToken = FormatTokenStream.getNextImportant(token);
            if (canFormatBrace && startToken != null && endToken != null && endToken.getId() == JsTokenId.BRACKET_LEFT_CURLY) {
                // set the character before the opening brace to the space or empty string according to the code style
                String spaceBeforeBrace = isSpace(token, formatContext, codeStyle) ? " " : ""; // NOI18N
                formatContext.replace(startToken.getOffset() - formatContext.getOffsetDiff() + lastOffsetDiff,
                        endToken.getOffset() - startToken.getOffset() + formatContext.getOffsetDiff() - lastOffsetDiff, spaceBeforeBrace);
            } else if (canFormatBrace) {
                formatSpace(tokens, index, formatContext, codeStyle);
            }
        } else if (!tokenProcessed) {
            // code style is set to "preserve existing" and brace token hasn't been process yet
            // format is as a space marker
            formatSpace(tokens, index, formatContext, codeStyle);
        }
    }

    private void formatKeywordAlignment(List<FormatToken> tokens, int index, FormatContext formatContext, CodeStyle.Holder codeStyle,
            int initialIndent, int continuationIndent, Stack<FormatContext.ContinuationBlock> continuations) {
        FormatToken token = tokens.get(index);
        final Set<JsTokenId> keywordIds = EnumSet.of(
                JsTokenId.KEYWORD_ELSE,
                JsTokenId.KEYWORD_CATCH,
                JsTokenId.KEYWORD_FINALLY,
                JsTokenId.KEYWORD_WHILE);

        if (isKeywordOnNewline(token, codeStyle)) {
            formatLineWrap(tokens, index, formatContext, codeStyle, initialIndent,
                    continuationIndent, continuations);
        } else {
            boolean canReformat = true;

            FormatToken startToken = null;
            for (int j = index - 1; j >= 0; j--) {
                FormatToken ft = tokens.get(j);
                if (ft.getKind() != FormatToken.Kind.WHITESPACE
                        && ft.getKind() != FormatToken.Kind.EOL) {
                    if (ft.getKind() == FormatToken.Kind.BLOCK_COMMENT
                            || ft.getKind() == FormatToken.Kind.LINE_COMMENT) {
                        // comment that prevents from formatting on the same line
                        canReformat = false;
                    }
                    break;
                }
                startToken = ft;
            }
            FormatToken endToken = FormatTokenStream.getNextImportant(token);
            FormatToken lastBeforeStart = null;
            if (startToken != null) {
                lastBeforeStart = startToken.previous();
                while (lastBeforeStart != null && lastBeforeStart.getKind().isIndentationMarker()) {
                    lastBeforeStart = lastBeforeStart.previous();
                }
            }

            if (canReformat && startToken != null
                    && lastBeforeStart != null && lastBeforeStart.getKind() == FormatToken.Kind.AFTER_END_BRACE
                    && endToken != null && keywordIds.contains(endToken.getId())) {
                // set the character before the keyword to the space or empty string according to the code style
                String spaceBeforeBrace = isSpace(token, formatContext, codeStyle) ? " " : ""; // NOI18N
                formatContext.replace(startToken.getOffset() - formatContext.getOffsetDiff() + lastOffsetDiff,
                        endToken.getOffset() - startToken.getOffset() + formatContext.getOffsetDiff() - lastOffsetDiff, spaceBeforeBrace);
            } else if (canReformat && !(lastBeforeStart != null && lastBeforeStart.getKind() == FormatToken.Kind.AFTER_STATEMENT)) {
                formatSpace(tokens, index, formatContext, codeStyle);
            }
        }
    }

    private boolean updateContinuationStart(FormatContext formatContext, FormatToken token,
            Stack<FormatContext.ContinuationBlock> continuations, boolean continuation) {

        boolean change = false;
        FormatToken nextImportant = continuation ? FormatTokenStream.getNextImportant(token) : token;
        if (nextImportant != null && nextImportant.getKind() == FormatToken.Kind.TEXT) {
            if (JsTokenId.BRACKET_LEFT_CURLY == nextImportant.getId()) {
                // if the pending continuation originates from
                // class X
                //         extends Y {
                // we do not want to increase the continuation on left brace
                FormatToken previous = nextImportant.previous();
                if (previous == null
                        || previous.getKind() != FormatToken.Kind.BEFORE_CLASS_DECLARATION_BRACE
                        || continuation) {
                    continuations.push(new FormatContext.ContinuationBlock(
                            FormatContext.ContinuationBlock.Type.CURLY, true));
                    formatContext.incContinuationLevel();
                    formatContext.setPendingContinuation(false);
                    processed.add(nextImportant);
                    change = true;
                }
            } else if (JsTokenId.BRACKET_LEFT_BRACKET == nextImportant.getId()) {
                continuations.push(new FormatContext.ContinuationBlock(
                        FormatContext.ContinuationBlock.Type.BRACKET, true));
                formatContext.incContinuationLevel();
                formatContext.setPendingContinuation(false);
                processed.add(nextImportant);
                change = true;
            } else if (JsTokenId.BRACKET_LEFT_PAREN == nextImportant.getId()) {
                continuations.push(new FormatContext.ContinuationBlock(
                        FormatContext.ContinuationBlock.Type.PAREN, true));
                formatContext.incContinuationLevel();
                formatContext.setPendingContinuation(false);
                processed.add(nextImportant);
                change = true;
            } else if (JsTokenId.KEYWORD_FUNCTION == nextImportant.getId()) {
                FormatToken curly = nextImportant;
                while (curly != null) {
                    if (!curly.isVirtual()) {
                        if (JsTokenId.BRACKET_RIGHT_CURLY == curly.getId()) {
                            // safety catch - something wrong
                            curly = null;
                            break;
                        }
                        if (JsTokenId.BRACKET_LEFT_CURLY == curly.getId()) {
                            break;
                        }
                    }
                    curly = curly.next();
                }
                if (curly != null) {
                    continuations.push(new FormatContext.ContinuationBlock(
                        FormatContext.ContinuationBlock.Type.CURLY, true));
                    formatContext.incContinuationLevel();
                    formatContext.setPendingContinuation(false);
                    processed.add(curly);
                    change = true;
                }
            } else if (continuation) {
                formatContext.setPendingContinuation(true);
            }
        }
        return change;
    }


    private void updateContinuationEnd(FormatContext formatContext, FormatToken token,
            Stack<FormatContext.ContinuationBlock> continuations) {
        if (token.isVirtual() || token.getKind() != FormatToken.Kind.TEXT) {
            return;
        }
        if (formatContext.isPendingContinuation() && (JsTokenId.BRACKET_RIGHT_CURLY == token.getId()
                || JsTokenId.BRACKET_RIGHT_BRACKET == token.getId()
                || JsTokenId.BRACKET_RIGHT_PAREN == token.getId())) {
            formatContext.setPendingContinuation(false);
        }

        // if the pending continuation originates from
        // class X
        //         extends Y {
        // we want to finish it on left before class declaration brace
        if (formatContext.isPendingContinuation() && JsTokenId.BRACKET_LEFT_CURLY == token.getId()) {
            FormatToken previous = token.previous();
            if (previous != null && previous.getKind() == FormatToken.Kind.BEFORE_CLASS_DECLARATION_BRACE) {
                formatContext.setPendingContinuation(false);
            }
        }

        if (continuations.isEmpty()) {
            return;
        }

        if (JsTokenId.BRACKET_LEFT_CURLY == token.getId()) {
            continuations.push(new FormatContext.ContinuationBlock(
                    FormatContext.ContinuationBlock.Type.CURLY, false));
        } else if (JsTokenId.BRACKET_LEFT_BRACKET == token.getId()) {
            continuations.push(new FormatContext.ContinuationBlock(
                    FormatContext.ContinuationBlock.Type.BRACKET, false));
        } else if (JsTokenId.BRACKET_LEFT_PAREN == token.getId()) {
            continuations.push(new FormatContext.ContinuationBlock(
                    FormatContext.ContinuationBlock.Type.PAREN, false));
        } else if (JsTokenId.BRACKET_RIGHT_CURLY == token.getId()) {
            FormatContext.ContinuationBlock block = continuations.peek();
            if (block.getType() == FormatContext.ContinuationBlock.Type.CURLY) {
                continuations.pop();
                if (block.isChange()) {
                    formatContext.decContinuationLevel();
                }
            }
        } else if (JsTokenId.BRACKET_RIGHT_BRACKET == token.getId()) {
            FormatContext.ContinuationBlock block = continuations.peek();
            if (block.getType() == FormatContext.ContinuationBlock.Type.BRACKET) {
                continuations.pop();
                if (block.isChange()) {
                    formatContext.decContinuationLevel();
                }
            }
        } else if (JsTokenId.BRACKET_RIGHT_PAREN == token.getId()) {
            FormatContext.ContinuationBlock block = continuations.peek();
            if (block.getType() == FormatContext.ContinuationBlock.Type.PAREN) {
                continuations.pop();
                if (block.isChange()) {
                    formatContext.decContinuationLevel();
                }
            }
        }
    }

    private static boolean isContinuation(FormatContext formatContext,
            CodeStyle.Holder codeStyle, FormatToken token, boolean noRealEol) {

        assert noRealEol || token.getKind() == FormatToken.Kind.SOURCE_START
                || token.getKind() == FormatToken.Kind.EOL;

        if (token.getKind() == FormatToken.Kind.SOURCE_START) {
            return false;
        }

        FormatToken next = token.next();
        if (JsTokenId.KEYWORD_IMPORT == next.getId()
                    || JsTokenId.KEYWORD_EXPORT == next.getId()) {
            return false;
        }
        for (FormatToken current = next; current != null && current.isVirtual(); current = current.next()) {
            if (current.getKind() == FormatToken.Kind.AFTER_STATEMENT
                    || current.getKind() == FormatToken.Kind.AFTER_PROPERTY
                    || current.getKind() == FormatToken.Kind.AFTER_ELEMENT
                    || current.getKind() == FormatToken.Kind.AFTER_DECORATOR
                    || current.getKind() == FormatToken.Kind.AFTER_ARRAY_LITERAL_ITEM
                    || current.getKind() == FormatToken.Kind.AFTER_CASE
                    // do not suppose continuation when indentation is changed
                    || current.getKind().isIndentationMarker()) {
                return false;
            }
        }

        FormatToken nonVirtualNext = FormatTokenStream.getNextNonVirtual(next);
        if (nonVirtualNext != null) {
            // this may happen when curly bracket or array bracket is on new line
            if (JsTokenId.BRACKET_LEFT_CURLY == nonVirtualNext.getId()
                    || JsTokenId.BRACKET_LEFT_BRACKET == nonVirtualNext.getId()) {
                FormatToken previous = nonVirtualNext.previous();
                if (previous == null
                        || (previous.getKind() != FormatToken.Kind.BEFORE_OBJECT
                        && previous.getKind() != FormatToken.Kind.BEFORE_ARRAY)) {
                    return false;
                } else if (previous.getKind() == FormatToken.Kind.BEFORE_OBJECT
                        || previous.getKind() == FormatToken.Kind.BEFORE_ARRAY) {
                    // handles continuation before object literal / array (issues #227007 and #250150)
                    // find token before object/array, ignoring WS and EOL
                    FormatToken tokenBeforeObject = previous.previous();
                    while (tokenBeforeObject != null
                            && (tokenBeforeObject.getKind() == FormatToken.Kind.WHITESPACE
                            || tokenBeforeObject.getKind() == FormatToken.Kind.EOL)) {
                        tokenBeforeObject = tokenBeforeObject.previous();
                    }
                    // if we have an object literal / array as function argument
                    // or it's assigned to a variable, indent it according to the settings
                    if (tokenBeforeObject != null) {
                        switch (tokenBeforeObject.getKind()) {
                            case BEFORE_FUNCTION_CALL_ARGUMENT:
                            case AFTER_ASSIGNMENT_OPERATOR:
                            case AFTER_ASSIGNMENT_OPERATOR_WRAP:
                            case AFTER_WITH_PARENTHESIS:
                            case AFTER_TERNARY_OPERATOR:
                                return codeStyle.objectLiteralContinuation;
                            case AFTER_PROPERTY_OPERATOR:
                                FormatToken operatorToken = tokenBeforeObject.previous();
                                if (operatorToken != null && operatorToken.getId() == JsTokenId.OPERATOR_COLON) {
                                    return codeStyle.objectLiteralContinuation;
                                }
                        }
                    }
                }
            } else if (JsTokenId.BRACKET_RIGHT_CURLY == nonVirtualNext.getId()
                    || JsTokenId.BRACKET_RIGHT_BRACKET == nonVirtualNext.getId()
                    || JsTokenId.KEYWORD_IMPORT == nonVirtualNext.getId()
                    || JsTokenId.KEYWORD_EXPORT == nonVirtualNext.getId()) {
                return false;
            // this may happen when comma separating object memebers is on new line
            } else if (JsTokenId.OPERATOR_COMMA == nonVirtualNext.getId()) {
                FormatToken virtualNext = nonVirtualNext.next();
                while (virtualNext != null && virtualNext.isVirtual()) {
                    if (virtualNext.getKind() == FormatToken.Kind.AFTER_PROPERTY) {
                        return false;
                    }
                    virtualNext = virtualNext.next();
                }
            }
        }

        // search backwards for important token
        FormatToken result = null;
        for (FormatToken previous = noRealEol ? token : token.previous(); previous != null;
                previous = previous.previous()) {

            FormatToken.Kind kind = previous.getKind();
            if (kind == FormatToken.Kind.SOURCE_START
                    || kind == FormatToken.Kind.TEXT
                    || kind == FormatToken.Kind.AFTER_STATEMENT
                    || kind == FormatToken.Kind.AFTER_PROPERTY
                    || kind == FormatToken.Kind.AFTER_ELEMENT
                    || kind == FormatToken.Kind.AFTER_DECORATOR
                    || kind == FormatToken.Kind.AFTER_ARRAY_LITERAL_ITEM
                    || kind == FormatToken.Kind.AFTER_CASE
                    // do not suppose continuation when indentation is changed
                    || kind.isIndentationMarker()) {
                result = previous;
                break;
            }
        }
        if (result == null
                || result.getKind() == FormatToken.Kind.SOURCE_START
                || result.getKind() == FormatToken.Kind.AFTER_STATEMENT
                || result.getKind() == FormatToken.Kind.AFTER_ELEMENT
                || result.getKind() == FormatToken.Kind.AFTER_DECORATOR
                || result.getKind() == FormatToken.Kind.AFTER_ARRAY_LITERAL_ITEM
                || result.getKind() == FormatToken.Kind.AFTER_CASE
                // do not suppose continuation when indentation is changed
                || result.getKind().isIndentationMarker()) {
            return false;
        } else if (result.getKind() == FormatToken.Kind.AFTER_PROPERTY) {
            FormatToken nextNonWs = next;
            while (nextNonWs != null
                    && (nextNonWs.getKind() == FormatToken.Kind.WHITESPACE
                    || nextNonWs.getKind() == FormatToken.Kind.EOL)) {
                nextNonWs = nextNonWs.next();
            }
            return nextNonWs != null
                    && nextNonWs.getKind() == FormatToken.Kind.BEFORE_FUNCTION_CALL_ARGUMENT;
        }

        return !(JsTokenId.BRACKET_LEFT_CURLY == result.getId()
                || JsTokenId.BRACKET_RIGHT_CURLY == result.getId()
                || (formatContext.isBrokenSource() && JsTokenId.OPERATOR_SEMICOLON == result.getId())
                || formatContext.isGenerated(result));

    }

    // FIXME can we movet his to FormatContext ?
    private Indentation checkIndentation(Document doc, FormatToken token, FormatToken indentationEnd,
            FormatContext formatContext, Context context, int indentationSize) {

        assert indentationEnd != null && !indentationEnd.isVirtual() : indentationEnd;
        assert token.getKind() == FormatToken.Kind.EOL || token.getKind() == FormatToken.Kind.SOURCE_START;
        // this: (token.getKind() != FormatToken.Kind.SOURCE_START
        // && formatContext.getDocumentOffset(token.getOffset()) >= 0)
        // handles the case when virtual source for embedded code contains
        // non existing eols we must not do indentation on these
        if ((token.getKind() != FormatToken.Kind.SOURCE_START && formatContext.getDocumentOffset(token.getOffset()) >= 0)
                || (context.startOffset() <= 0 && !formatContext.isEmbedded())) {

            // we don't want to touch lines starting with other language
            // it is a bit heuristic but we can't do much
            // see embeddedMultipleSections1.php
            if (formatContext.isGenerated(indentationEnd)) {
                return Indentation.FORBIDDEN;
            }
            return Indentation.ALLOWED;
        }

        // we are sure this is SOURCE_START - no source start indentation in embedded code
        if (formatContext.isEmbedded()) {
            return Indentation.FORBIDDEN;
        }

        try {
            // when we are formatting only selection we
            // have to handle the source start indentation properly
            int lineStartOffset = IndentUtils.lineStartOffset(doc, context.startOffset());
            if (isWhitespace(doc.getText(lineStartOffset, context.startOffset() - lineStartOffset))) {
                int currentIndentation = IndentUtils.lineIndent(doc, lineStartOffset);
                if (currentIndentation != indentationSize) {
                    // fix the indentation if possible
                    if (lineStartOffset + indentationSize >= context.startOffset()) {
                        return new Indentation(true, true);
                    }
                }
            }
        } catch (BadLocationException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return Indentation.FORBIDDEN;
    }

    private static void updateIndentationLevel(FormatToken token,
            FormatContext formatContext, CodeStyle.Holder codeStyle) {
        switch (token.getKind()) {
            case ELSE_IF_INDENTATION_INC:
                if (ELSE_IF_SINGLE_LINE) {
                    break;
                }
            case INDENTATION_INC:
                formatContext.incIndentationLevel();
                break;
            case ELSE_IF_INDENTATION_DEC:
                if (ELSE_IF_SINGLE_LINE) {
                    break;
                }
            case INDENTATION_DEC:
                formatContext.decIndentationLevel();
                break;
            case BEFORE_JSX_BLOCK_START:
                formatContext.incJsxIndentation(token);
                break;
            case AFTER_JSX_BLOCK_END:
                formatContext.decJsxIndentation(token);
                break;
            default:
                break;
        }

        // following code handles indentation of the opening brace
        // if indentation is set to "new line indented"
        if (token.getKind().isBraceMarker()) {
            if (getBracePlacement(token, codeStyle) == CodeStyle.BracePlacement.NEW_LINE_INDENTED) {
                FormatToken nextFt = token.next();
                if (nextFt != null && nextFt.getId() == JsTokenId.BRACKET_LEFT_CURLY) {
                    // indent only in case if the token after brace marker is left brace
                    formatContext.incIndentationLevel();
                }
            }
        }
    }

    // contains all eols in case of multiple empty lines
    // including tokenAfterEol
    private static List<FormatToken> getExtendedTokenAfterEol(FormatToken tokenAfterEol) {
        List<FormatToken> eols = new ArrayList<>(2);
        eols.add(tokenAfterEol);
        for (FormatToken current = tokenAfterEol; current != null && (current.getKind() == FormatToken.Kind.EOL
                || current.getKind() == FormatToken.Kind.WHITESPACE
                || current.isVirtual()); current = current.next()) {
            if (current != tokenAfterEol && current.getKind() == FormatToken.Kind.EOL) {
                eols.add(current);
            }
        }
        return eols;
    }

    private static boolean isStatementWrap(FormatToken token) {
        return token.getKind() == FormatToken.Kind.AFTER_STATEMENT
                || token.getKind() == FormatToken.Kind.AFTER_BLOCK_START
                || token.getKind() == FormatToken.Kind.AFTER_CASE
                || token.getKind() == FormatToken.Kind.ELSE_IF_AFTER_BLOCK_START;
    }

    private static CodeStyle.WrapStyle getLineWrap(List<FormatToken> tokens, int index,
            FormatContext context, CodeStyle.Holder codeStyle, boolean skipWitespace) {
        FormatToken token = tokens.get(index);

        assert token.isVirtual();

        FormatToken next = token;
        while (next != null && (next.isVirtual() || skipWitespace && next.getKind() == FormatToken.Kind.WHITESPACE)) {
            CodeStyle.WrapStyle style = getLineWrap(next, context, codeStyle);
            if (style != null) {
                return style;
            }
            next = next.next();
        }
        return null;
    }

    private static FormatToken getEndingEol(FormatToken token, CodeStyle.Holder codeStyle, List<FormatToken> eols, FormatToken defaultEnd) {
        FormatToken end = defaultEnd;
        if (codeStyle.maxPreservedObjectLines > 0
                && surroundingsContains(token, EnumSet.of(
                        FormatToken.Kind.AFTER_OBJECT_START, FormatToken.Kind.AFTER_PROPERTY))) {
            if (codeStyle.maxPreservedObjectLines < (eols.size() - 1)) {
                end = eols.get(eols.size() - codeStyle.maxPreservedObjectLines - 1);
            } else {
                end = eols.get(0);
            }
        }

        if (codeStyle.maxPreservedArrayLines > 0
                && surroundingsContains(token, EnumSet.of(
                        FormatToken.Kind.AFTER_ARRAY_LITERAL_START, FormatToken.Kind.AFTER_ARRAY_LITERAL_ITEM))) {
            if (codeStyle.maxPreservedArrayLines < (eols.size() - 1)) {
                end = eols.get(eols.size() - codeStyle.maxPreservedArrayLines - 1);
            } else {
                end = eols.get(0);
            }
        }

        if (codeStyle.maxPreservedClassLines > 0
                && surroundingsContains(token, EnumSet.of(
                        FormatToken.Kind.AFTER_CLASS_START, FormatToken.Kind.AFTER_ELEMENT))) {
            if (codeStyle.maxPreservedClassLines < (eols.size() - 1)) {
                end = eols.get(eols.size() - codeStyle.maxPreservedClassLines - 1);
            } else {
                end = eols.get(0);
            }
        }
        return end;
    }

    private static CodeStyle.WrapStyle getLineWrap(FormatToken token, FormatContext context, CodeStyle.Holder codeStyle) {
        switch (token.getKind()) {
            case AFTER_STATEMENT:
                // this is special because of possible an typical "something."
                // dot sanitization see #234385
                FormatToken check = token.previous();
                if (check != null && check.getKind() == FormatToken.Kind.BEFORE_DOT) {
                    return null;
                }
                check = token.next();
                if (check != null && check.getKind() == FormatToken.Kind.BEFORE_DOT) {
                    return null;
                }
                return codeStyle.wrapStatement;
            case AFTER_BLOCK_START:
                // XXX option
                if (isEmptyFunctionBlock(token)) {
                    return CodeStyle.WrapStyle.WRAP_NEVER;
                }
                return CodeStyle.WrapStyle.WRAP_ALWAYS;
            case AFTER_CASE:
                // XXX option
                return CodeStyle.WrapStyle.WRAP_ALWAYS;
            case ELSE_IF_AFTER_BLOCK_START:
                if (ELSE_IF_SINGLE_LINE) {
                    return CodeStyle.WrapStyle.WRAP_NEVER;
                }
                // XXX option
                return CodeStyle.WrapStyle.WRAP_ALWAYS;
            case AFTER_VAR_DECLARATION:
                return codeStyle.wrapVariables;
            case BEFORE_FUNCTION_DECLARATION_PARAMETER:
                return codeStyle.wrapMethodParams;
            case BEFORE_FUNCTION_CALL_ARGUMENT:
                return codeStyle.wrapMethodCallArgs;
            case AFTER_IF_START:
                return codeStyle.wrapIfStatement;
            case AFTER_ELSE_START:
                return codeStyle.wrapIfStatement;
            case AFTER_WHILE_START:
                return codeStyle.wrapWhileStatement;
            case AFTER_DO_START:
                return codeStyle.wrapDoWhileStatement;
            case AFTER_FOR_START:
                return codeStyle.wrapForStatement;
            case AFTER_WITH_START:
                return codeStyle.wrapWithStatement;
            case BEFORE_FOR_TEST:
            case BEFORE_FOR_MODIFY:
                return codeStyle.wrapFor;
            case BEFORE_CHAIN_CALL_DOT:
                if (codeStyle.wrapAfterDotInChainedMethodCalls) {
                    return null;
                }
                return codeStyle.wrapChainedMethodCalls;
            case AFTER_CHAIN_CALL_DOT:
                if (codeStyle.wrapAfterDotInChainedMethodCalls) {
                    return codeStyle.wrapChainedMethodCalls;
                }
                return null;
            case AFTER_BINARY_OPERATOR_WRAP:
                if (codeStyle.wrapAfterBinaryOps) {
                    return codeStyle.wrapBinaryOps;
                }
                return null;
            case BEFORE_BINARY_OPERATOR_WRAP:
                if (codeStyle.wrapAfterBinaryOps) {
                    return null;
                }
                return codeStyle.wrapBinaryOps;
            case AFTER_ASSIGNMENT_OPERATOR_WRAP:
                return codeStyle.wrapAssignOps;
            case AFTER_ARROW_OPERATOR_WRAP:
                return codeStyle.wrapArrowOps;
            case AFTER_TERNARY_OPERATOR_WRAP:
                if (codeStyle.wrapAfterTernaryOps) {
                    return codeStyle.wrapTernaryOps;
                }
                return null;
            case BEFORE_TERNARY_OPERATOR_WRAP:
                if (codeStyle.wrapAfterTernaryOps) {
                    return null;
                }
                return codeStyle.wrapTernaryOps;
            case AFTER_OBJECT_START:
            case BEFORE_OBJECT_END:
                // this is special because people usually don't want to wrap
                // empty objects see #228716
                // add an option wrap empty objects if anybody will complain
                if (isEmptyObject(token)) {
                    return null;
                }
                return codeStyle.wrapObjects;
            case AFTER_CLASS_START:
            case BEFORE_CLASS_END:
                return codeStyle.wrapClasses;
            case BEFORE_CLASS_EXTENDS:
                return codeStyle.wrapClassExtends;
            case AFTER_PROPERTY:
                return codeStyle.wrapProperties;
            case AFTER_ELEMENT:
                return codeStyle.wrapElements;
            case AFTER_DECORATOR:
                return codeStyle.wrapDecorators;
            case AFTER_ARRAY_LITERAL_START:
            case BEFORE_ARRAY_LITERAL_END:
                return codeStyle.wrapArrayInit;
            case AFTER_ARRAY_LITERAL_ITEM:
                return codeStyle.wrapArrayInitItems;
            default:
                return null;
        }
    }

    private static CodeStyle.BracePlacement getBracePlacement(FormatToken token, CodeStyle.Holder codeStyle) {
        switch (token.getKind()) {
            case BEFORE_FUNCTION_DECLARATION_BRACE:
                return codeStyle.functionDeclBracePlacement;
            case BEFORE_CLASS_DECLARATION_BRACE:
                return codeStyle.classDeclBracePlacement;
            case BEFORE_IF_BRACE:
            case BEFORE_ELSE_BRACE:
                return codeStyle.ifBracePlacement;
            case BEFORE_WHILE_BRACE:
            case BEFORE_DO_BRACE:
                return codeStyle.whileBracePlacement;
            case BEFORE_FOR_BRACE:
                return codeStyle.forBracePlacement;
            case BEFORE_SWITCH_BRACE:
                return codeStyle.switchBracePlacement;
            case BEFORE_TRY_BRACE:
            case BEFORE_CATCH_BRACE:
            case BEFORE_FINALLY_BRACE:
                return codeStyle.catchBracePlacement;
            case BEFORE_WITH_BRACE:
                return codeStyle.withBracePlacement;
            default:
                return CodeStyle.BracePlacement.PRESERVE_EXISTING;
        }
    }

    private static boolean isKeywordOnNewline(FormatToken token, CodeStyle.Holder codeStyle) {
        switch (token.getKind()) {
            case BEFORE_ELSE_KEYWORD:
                return codeStyle.placeElseOnNewLine;
            case BEFORE_WHILE_KEYWORD:
                return codeStyle.placeWhileOnNewLine;
            case BEFORE_CATCH_KEYWORD:
                return codeStyle.placeCatchOnNewLine;
            case BEFORE_FINALLY_KEYWORD:
                return codeStyle.placeFinallyOnNewLine;
            default:
                return false;
        }
    }

    private static boolean isSpace(FormatToken token, FormatContext context, CodeStyle.Holder codeStyle,
            boolean skipWitespace, boolean skipEol) {

        if (!(token.isVirtual()
                || skipWitespace && token.getKind() == FormatToken.Kind.WHITESPACE
                || skipEol && token.getKind() == FormatToken.Kind.EOL)) {
            return false;
        }

        boolean hasSpaceMarker = false;
        boolean hasSpace = false;
        FormatToken next = token;
        while (next != null && (next.isVirtual()
                || skipWitespace && next.getKind() == FormatToken.Kind.WHITESPACE
                || skipWitespace && next.getKind() == FormatToken.Kind.EOL)) {
            if (next.getKind() != FormatToken.Kind.WHITESPACE
                    && next.getKind() != FormatToken.Kind.EOL) {
                if (isSpace(next, context, codeStyle)) {
                    return true;
                }
                if (next.getKind().isSpaceMarker()) {
                    hasSpaceMarker = true;
                }
            } else {
                hasSpace = true;
            }
            next = next.next();
        }
        return !hasSpaceMarker && hasSpace;
    }

    private static boolean isSpace(FormatToken token, FormatContext formatContext, CodeStyle.Holder codeStyle) {
        switch (token.getKind()) {
            case BEFORE_ASSIGNMENT_OPERATOR:
                return codeStyle.spaceAroundAssignOps;
            case AFTER_ASSIGNMENT_OPERATOR:
                return codeStyle.spaceAroundAssignOps;
            case BEFORE_ARROW_OPERATOR:
                return codeStyle.spaceAroundArrowOps;
            case AFTER_ARROW_OPERATOR:
                return codeStyle.spaceAroundArrowOps;
            case BEFORE_PROPERTY_OPERATOR:
                return codeStyle.spaceBeforeColon;
            case AFTER_PROPERTY_OPERATOR:
                return codeStyle.spaceAfterColon;
            case BEFORE_BINARY_OPERATOR:
                return codeStyle.spaceAroundBinaryOps;
            case AFTER_BINARY_OPERATOR:
                return codeStyle.spaceAroundBinaryOps;
            case BEFORE_COMMA:
                return codeStyle.spaceBeforeComma;
            case AFTER_COMMA:
                return codeStyle.spaceAfterComma;
            case AFTER_IF_KEYWORD:
                return codeStyle.spaceBeforeIfParen;
            case AFTER_WHILE_KEYWORD:
                return codeStyle.spaceBeforeWhileParen;
            case AFTER_FOR_KEYWORD:
                return codeStyle.spaceBeforeForParen;
            case AFTER_WITH_KEYWORD:
                return codeStyle.spaceBeforeWithParen;
            case AFTER_SWITCH_KEYWORD:
                return codeStyle.spaceBeforeSwitchParen;
            case AFTER_CATCH_KEYWORD:
                return codeStyle.spaceBeforeCatchParen;
            case BEFORE_WHILE_KEYWORD:
                return codeStyle.spaceBeforeWhile;
            case BEFORE_ELSE_KEYWORD:
                return codeStyle.spaceBeforeElse;
            case BEFORE_CATCH_KEYWORD:
                return codeStyle.spaceBeforeCatch;
            case BEFORE_FINALLY_KEYWORD:
                return codeStyle.spaceBeforeFinally;
            case BEFORE_SEMICOLON:
                return codeStyle.spaceBeforeSemi;
            case AFTER_SEMICOLON:
                return codeStyle.spaceAfterSemi;
            case BEFORE_UNARY_OPERATOR:
                return codeStyle.spaceAroundUnaryOps;
            case AFTER_UNARY_OPERATOR:
                return codeStyle.spaceAroundUnaryOps;
            case BEFORE_TERNARY_OPERATOR:
                return codeStyle.spaceAroundTernaryOps;
            case AFTER_TERNARY_OPERATOR:
                return codeStyle.spaceAroundTernaryOps;
            case BEFORE_ANONYMOUS_FUNCTION_DECLARATION:
                return codeStyle.spaceBeforeAnonMethodDeclParen;
            case BEFORE_FUNCTION_DECLARATION:
                return codeStyle.spaceBeforeMethodDeclParen;
            case BEFORE_FUNCTION_CALL:
                return codeStyle.spaceBeforeMethodCallParen;
            case AFTER_FUNCTION_DECLARATION_PARENTHESIS:
                return codeStyle.spaceWithinMethodDeclParens;
            case BEFORE_FUNCTION_DECLARATION_PARENTHESIS:
                return codeStyle.spaceWithinMethodDeclParens;
            case AFTER_FUNCTION_CALL_PARENTHESIS:
                return codeStyle.spaceWithinMethodCallParens;
            case BEFORE_FUNCTION_CALL_PARENTHESIS:
                return codeStyle.spaceWithinMethodCallParens;
            case AFTER_IF_PARENTHESIS:
                return codeStyle.spaceWithinIfParens;
            case BEFORE_IF_PARENTHESIS:
                return codeStyle.spaceWithinIfParens;
            case AFTER_WHILE_PARENTHESIS:
                return codeStyle.spaceWithinWhileParens;
            case BEFORE_WHILE_PARENTHESIS:
                return codeStyle.spaceWithinWhileParens;
            case AFTER_FOR_PARENTHESIS:
                return codeStyle.spaceWithinForParens;
            case BEFORE_FOR_PARENTHESIS:
                return codeStyle.spaceWithinForParens;
            case AFTER_WITH_PARENTHESIS:
                return codeStyle.spaceWithinWithParens;
            case BEFORE_WITH_PARENTHESIS:
                return codeStyle.spaceWithinWithParens;
            case AFTER_SWITCH_PARENTHESIS:
                return codeStyle.spaceWithinSwitchParens;
            case BEFORE_SWITCH_PARENTHESIS:
                return codeStyle.spaceWithinSwitchParens;
            case AFTER_CATCH_PARENTHESIS:
                return codeStyle.spaceWithinCatchParens;
            case BEFORE_CATCH_PARENTHESIS:
                return codeStyle.spaceWithinCatchParens;
            case AFTER_LEFT_PARENTHESIS:
                return codeStyle.spaceWithinParens;
            case BEFORE_RIGHT_PARENTHESIS:
                return codeStyle.spaceWithinParens;
            case AFTER_LEFT_BRACE:
                return codeStyle.spaceWithinBraces;
            case BEFORE_RIGHT_BRACE:
                return codeStyle.spaceWithinBraces;
            case BEFORE_IF_BRACE:
                return codeStyle.spaceBeforeIfLeftBrace;
            case BEFORE_ELSE_BRACE:
                return codeStyle.spaceBeforeElseLeftBrace;
            case BEFORE_WHILE_BRACE:
                return codeStyle.spaceBeforeWhileLeftBrace;
            case BEFORE_FOR_BRACE:
                return codeStyle.spaceBeforeForLeftBrace;
            case BEFORE_DO_BRACE:
                return codeStyle.spaceBeforeDoLeftBrace;
            case BEFORE_TRY_BRACE:
                return codeStyle.spaceBeforeTryLeftBrace;
            case BEFORE_CATCH_BRACE:
                return codeStyle.spaceBeforeCatchLeftBrace;
            case BEFORE_FINALLY_BRACE:
                return codeStyle.spaceBeforeFinallyLeftBrace;
            case BEFORE_SWITCH_BRACE:
                return codeStyle.spaceBeforeSwitchLeftBrace;
            case BEFORE_WITH_BRACE:
                return codeStyle.spaceBeforeWithLeftBrace;
            case BEFORE_FUNCTION_DECLARATION_BRACE:
                return codeStyle.spaceBeforeMethodDeclLeftBrace;
            case BEFORE_CLASS_DECLARATION_BRACE:
                return codeStyle.spaceBeforeClassDeclLeftBrace;
            case AFTER_ARRAY_LITERAL_BRACKET:
                return codeStyle.spaceWithinArrayBrackets;
            case BEFORE_ARRAY_LITERAL_BRACKET:
                return codeStyle.spaceWithinArrayBrackets;
            case AFTER_NEW_KEYWORD:
                // no option as false (removing space) would brake the code
                return true;
            case AFTER_VAR_KEYWORD:
                // no option as false (removing space) would brake the code
                return true;
            case AFTER_TYPEOF_KEYWORD:
                // no option as false (removing space) would brake the code
                return true;
            case BEFORE_DOT:
            case AFTER_DOT:
                return false;
            default:
                return false;
        }
    }

    private static boolean isEmptyObject(FormatToken token) {
        if (token.getKind() == FormatToken.Kind.AFTER_OBJECT_START) {
            FormatToken current = token.next();
            while (current != null && current.isVirtual()) {
                if (current.getKind() == FormatToken.Kind.BEFORE_OBJECT_END) {
                    return true;
                }
                current = current.next();
            }
            return false;
        } else if (token.getKind() == FormatToken.Kind.BEFORE_OBJECT_END) {
            FormatToken current = token.previous();
            while (current != null && current.isVirtual()) {
                if (current.getKind() == FormatToken.Kind.AFTER_OBJECT_START) {
                    return true;
                }
                current = current.previous();
            }
            return false;
        }
        return false;
    }

    private static boolean isEmptyFunctionBlock(FormatToken token) {
        if (token.getKind() == FormatToken.Kind.AFTER_BLOCK_START) {
            // now look back to find out what kind of block we are in
            FormatToken prev = token.previous();
            while (prev != null && (!prev.isVirtual() || prev.getKind().isIndentationMarker())) {
                prev = prev.previous();
            }
            if (prev != null && prev.getKind() == FormatToken.Kind.BEFORE_FUNCTION_DECLARATION_BRACE) {
                // continue only in case of function
                FormatToken current = token.next();
                while (current != null && (current.isVirtual() || current.getKind() == FormatToken.Kind.WHITESPACE)) {
                    current = current.next();
                }
                return current != null && current.getId() == JsTokenId.BRACKET_RIGHT_CURLY;
            }
        }
        return false;
    }

    private int getFormatStableStart(Document doc, Language<JsTokenId> language,
            int offset, int startOffset, boolean embedded) {

        TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(
                TokenHierarchy.get(doc), offset, language);
        if (ts == null) {
            return 0;
        }

        ts.move(startOffset);
        if (!ts.movePrevious()) {
            return 0;
        }

        // Look backwards to find a suitable context
        // which we will assume is properly indented and balanced
        int curlyBalance = 0;
        do {
            Token<?extends JsTokenId> token = ts.token();
            JsTokenId id = token.id();

            // FIXME should we check for more tokens like {, if, else ...
            switch (id) {
                case KEYWORD_FUNCTION:
                    if (curlyBalance > 0 && ts.offset() < offset) {
                        return ts.offset();
                    }
                    break;
                case BRACKET_LEFT_CURLY:
                    curlyBalance++;
                    break;
                case BRACKET_RIGHT_CURLY:
                    curlyBalance--;
                    break;
                default:
                    break;
            }
        } while (ts.movePrevious());

        if (embedded && !ts.movePrevious()) {
            LineDocument ld = LineDocumentUtils.as(doc, LineDocument.class);
            if (ld != null) {
                // I may have moved to the front of an embedded JavaScript area, e.g. in
                // an attribute or in a <script> tag. If this is the end of the line,
                // go to the next line instead since the reindent code will go to the beginning
                // of the stable formatting start.
                int sequenceBegin = ts.offset();
                try {
                    int lineTextEnd = LineDocumentUtils.getLineLastNonWhitespace(ld, sequenceBegin);
                    if (lineTextEnd == -1 || sequenceBegin > lineTextEnd) {
                        return Math.min(doc.getLength(), LineDocumentUtils.getLineEnd(ld, sequenceBegin) + 1);
                    }
                } catch (BadLocationException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
        }

        return ts.offset();
    }

    private boolean isContinuation(Document doc, int offset, int bracketBalance,
            boolean continued, int bracketBalanceDelta, IndentContext.BlockDescription block) throws BadLocationException {
        LineDocument ld = LineDocumentUtils.as(doc, LineDocument.class);
        if (ld == null) {
            return false;
        }
        offset = LineDocumentUtils.getLineLastNonWhitespace(ld, offset);
        if (offset == -1) {
            return false;
        }

        TokenSequence<? extends JsTokenId> ts = LexUtilities.getPositionedSequence(doc, offset, language);
        Token<? extends JsTokenId> token = (ts != null ? ts.token() : null);

        if (ts != null && token != null) {
            int index = ts.index();
            JsTokenId previousId = null;
            if (ts.movePrevious()) {
                Token<? extends JsTokenId> previous = LexUtilities.findPreviousNonWsNonComment(ts);
                if (previous != null) {
                    previousId = previous.id();
                }

                ts.moveIndex(index);
                ts.moveNext();
            }

            JsTokenId id = token.id();

            boolean isContinuationOperator = LexUtilities.isBinaryOperator(id, previousId);

            if (ts.offset() == offset && token.length() > 1 && token.text().toString().startsWith("\\")) {
                // Continued lines have different token types
                isContinuationOperator = true;
            }

            if (id == JsTokenId.OPERATOR_COMMA) {
                // If there's a comma it's a continuation operator, but inside arrays, hashes or parentheses
                // parameter lists we should not treat it as such since we'd "double indent" the items, and
                // NOT the first item (where there's no comma, e.g. you'd have
                //  foo(
                //    firstarg,
                //      secondarg,  # indented both by ( and hanging indent ,
                //      thirdarg)
                // same if there is a comma in object for example
                // var a = {
                //     b : "something", # should not be indented as continuation
                //     c : "else"
                // }
                isContinuationOperator = (bracketBalance == 0) && (block == null || !block.isObject());
            } else if (id == JsTokenId.BRACKET_LEFT_PAREN) {
                isContinuationOperator = true;
            } else if (id == JsTokenId.BRACKET_LEFT_CURLY) {
                isContinuationOperator = (bracketBalanceDelta >= 0) && continued;
            } else if (id == JsTokenId.OPERATOR_COLON) {
                TokenSequence<? extends JsTokenId> inner = LexUtilities.getPositionedSequence(doc, ts.offset(), language);
                Token<? extends JsTokenId> foundToken = LexUtilities.findPreviousIncluding(inner,
                        Arrays.asList(JsTokenId.KEYWORD_CASE, JsTokenId.KEYWORD_DEFAULT, JsTokenId.OPERATOR_COLON));
                if (foundToken != null && (foundToken.id() == JsTokenId.KEYWORD_CASE
                        || foundToken.id() == JsTokenId.KEYWORD_DEFAULT)) {
                    isContinuationOperator = false;
                } else {
                    isContinuationOperator = true;
                }
            } else {
                JsTokenId nextId = null;
                if (ts.moveNext()) {
                    Token<? extends JsTokenId> next = LexUtilities.findNextNonWsNonComment(ts);
                    if (next != null) {
                        nextId = next.id();
                    }
                    ts.moveIndex(index);
                    ts.moveNext();
                }
                if (nextId == JsTokenId.BRACKET_RIGHT_PAREN) {
                    isContinuationOperator = true;
                }
            }
            return isContinuationOperator;
        }

        return false;
    }

    // FIXME we do not reindent multiple regions
    @Override
    public void reindent(final Context context) {

        Document document = context.document();
        int startOffset = context.startOffset();
        int endOffset = context.endOffset();

        final IndentContext indentContext = new IndentContext(context, provider);
        int indentationSize = IndentUtils.indentLevelSize(document);
        int continuationIndent = CodeStyle.get(indentContext).getContinuationIndentSize();

        try {
            final Document doc = document; // document.getText(0, document.getLength())
            LineDocument ld = LineDocumentUtils.as(doc, LineDocument.class);
            if (ld == null) {
                return;
            }
            startOffset = LineDocumentUtils.getLineStart(ld, startOffset);
            int endLineOffset = LineDocumentUtils.getLineStart(ld, endOffset);
            final boolean indentOnly = (startOffset == endLineOffset)
                    && (endOffset == context.caretOffset() || startOffset == context.caretOffset())
                    && (LineDocumentUtils.isLineEmpty(ld, startOffset)
                    || LineDocumentUtils.isLineWhitespace(ld, startOffset)
                    || LineDocumentUtils.getNextNonWhitespace(ld, startOffset) == context.caretOffset());
            if (indentOnly && indentContext.isEmbedded()) {
                // Make sure we're not messing with indentation in HTML
                Token<? extends JsTokenId> token = LexUtilities.getToken(doc, startOffset, language);
                if (token == null) {
                    return;
                }
            }

            if (endOffset > doc.getLength()) {
                endOffset = doc.getLength();
            }


            final int lineStart = startOffset;//Utilities.getRowStart(doc, startOffset);
            int initialOffset = 0;
            int initialIndent = 0;
            if (startOffset > 0) {
                int prevOffset = LineDocumentUtils.getLineStart(ld, startOffset-1);
                initialOffset = getFormatStableStart(doc, language, prevOffset, startOffset, indentContext.isEmbedded());
                initialIndent = GsfUtilities.getLineIndent(doc, initialOffset);
            }

            // Build up a set of offsets and indents for lines where I know I need
            // to adjust the offset. I will then go back over the document and adjust
            // lines that are different from the intended indent. By doing piecemeal
            // replacements in the document rather than replacing the whole thing,
            // a lot of things will work better: breakpoints and other line annotations
            // will be left in place, semantic coloring info will not be temporarily
            // damaged, and the caret will stay roughly where it belongs.

            // When we're formatting sections, include whitespace on empty lines; this
            // is used during live code template insertions for example. However, when
            // wholesale formatting a whole document, leave these lines alone.
            boolean indentEmptyLines = (startOffset != 0 || endOffset + 1 != doc.getLength());

            boolean includeEnd = endOffset == doc.getLength() || indentOnly;

            // TODO - remove initialbalance etc.
            computeIndents(indentContext, initialIndent, indentationSize, continuationIndent, initialOffset, endOffset,
                    indentEmptyLines, includeEnd, indentOnly);

            AtomicLockDocument ald = LineDocumentUtils.asRequired(doc, AtomicLockDocument.class);
            ald.runAtomic(() -> {
                try {
                    List<IndentContext.Indentation> indents = indentContext.getIndentations();
                    // Iterate in reverse order such that offsets are not affected by our edits
                    for (int i = indents.size() - 1; i >= 0; i--) {
                        IndentContext.Indentation indentation = indents.get(i);
                        int indent = indentation.getSize();
                        int lineBegin = indentation.getOffset();

                        if (lineBegin < lineStart) {
                            // We're now outside the region that the user wanted reformatting;
                            // these offsets were computed to get the correct continuation context etc.
                            // for the formatter
                            break;
                        }

                        if (lineBegin == lineStart && i > 0) {
                            // Look at the previous line, and see how it's indented
                            // in the buffer.  If it differs from the computed position,
                            // offset my computed position (thus, I'm only going to adjust
                            // the new line position relative to the existing editing.
                            // This avoids the situation where you're inserting a newline
                            // in the middle of "incorrectly" indented code (e.g. different
                            // size than the IDE is using) and the newline position ending
                            // up "out of sync"
                            IndentContext.Indentation prevIndentation = indents.get(i - 1);
                            int prevOffset = prevIndentation.getOffset();
                            int prevIndent = prevIndentation.getSize();
                            int actualPrevIndent = GsfUtilities.getLineIndent(doc, prevOffset);
                            // NOTE: in embedding this is usually true as we have some nonzero initial indent,
                            // I am just not sure if it is better to add indentOnly check (as I did) or
                            // remove blank lines condition completely?
                            if (actualPrevIndent != prevIndent) {
                                // For blank lines, indentation may be 0, so don't adjust in that case
                                if (indentOnly || !(LineDocumentUtils.isLineEmpty(ld, prevOffset) || LineDocumentUtils.isLineWhitespace(ld, prevOffset))) {
                                    indent = actualPrevIndent + (indent-prevIndent);
                                }
                            }
                        }

                        // Adjust the indent at the given line (specified by offset) to the given indent
                        int currentIndent = GsfUtilities.getLineIndent(doc, lineBegin);

                        if (currentIndent != indent && indent >= 0) {
                            //org.netbeans.editor.Formatter editorFormatter = doc.getFormatter();
                            //editorFormatter.changeRowIndent(doc, lineBegin, indent);
                            context.modifyIndent(lineBegin, indent);
                        }
                    }
                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                }
            });
        } catch (BadLocationException ble) {
            LOGGER.log(Level.FINE, null, ble);
        }
    }

    private void computeIndents(IndentContext context, int initialIndent, int indentSize, int continuationIndent,
            int startOffset, int endOffset, boolean indentEmptyLines, boolean includeEnd, boolean indentOnly) {

        Document doc = context.getDocument();
        LineDocument ld = LineDocumentUtils.as(doc, LineDocument.class);
        if (ld == null) {
            return;
        }
        // PENDING:
        // The reformatting APIs in NetBeans should be lexer based. They are still
        // based on the old TokenID apis. Once we get a lexer version, convert this over.
        // I just need -something- in place until that is provided.

        try {
            // Algorithm:
            // Iterate over the range.
            // Accumulate a token balance ( {,(,[, and keywords like class, case, etc. increases the balance,
            //      },),] and "end" decreases it
            // If the line starts with an end marker, indent the line to the level AFTER the token
            // else indent the line to the level BEFORE the token (the level being the balance * indentationSize)
            // Compute the initial balance and indentation level and use that as a "base".
            // If the previous line is not "done" (ends with a comma or a binary operator like "+" etc.
            // add a "hanging indent" modifier.
            // At the end of the day, we're recording a set of line offsets and indents.
            // This can be used either to reformat the buffer, or indent a new line.

            // State:
            int offset = LineDocumentUtils.getLineStart(ld, startOffset); // The line's offset
            int end = endOffset;


            // Pending - apply comment formatting too?

            // XXX Look up RHTML too
            //int indentSize = EditorOptions.get(RubyInstallation.RUBY_MIME_TYPE).getSpacesPerTab();
            //int hangingIndentSize = indentSize;


            // Build up a set of offsets and indents for lines where I know I need
            // to adjust the offset. I will then go back over the document and adjust
            // lines that are different from the intended indent. By doing piecemeal
            // replacements in the document rather than replacing the whole thing,
            // a lot of things will work better: breakpoints and other line annotations
            // will be left in place, semantic coloring info will not be temporarily
            // damaged, and the caret will stay roughly where it belongs.

            // The token balance at the offset
            int balance = 0;
            // The bracket balance at the offset ( parens, bracket, brace )
            int bracketBalance = 0;
            boolean continued = false;
//            boolean indentHtml = false;
//            if (embeddedJavaScript) {
//                indentHtml = codeStyle.indentHtml();
//            }

            //int originallockCommentIndention = 0;
            int adjustedBlockCommentIndention = 0;

            int endIndents;

            final int IN_CODE = 0;
            final int IN_LITERAL = 1;
            final int IN_BLOCK_COMMENT_START = 2;
            final int IN_BLOCK_COMMENT_MIDDLE = 3;

            // this cycle is written in offset but in fact it iretates over lines
            while ((!includeEnd && offset < end) || (includeEnd && offset <= end)) {
                int indent; // The indentation to be used for the current line

                if (context.isEmbedded()) {
                    // now using JavaScript indent size to indent from <SCRIPT> tag; should it be HTML?
                    initialIndent = context.getEmbeddedIndent() + indentSize;
                }


                int lineType = IN_CODE;
                int pos = LineDocumentUtils.getLineFirstNonWhitespace(ld, offset);
                TokenSequence<?extends JsTokenId> ts = null;

                if (pos != -1) {
                    // I can't look at the first position on the line, since
                    // for a string array that is indented, the indentation portion
                    // is recorded as a blank identifier
                    ts = LexUtilities.getPositionedSequence(doc, pos, false, language);

                    if (ts != null) {
                        JsTokenId id = ts.token().id();
                        int index = ts.index();
                        JsTokenId previousId = null;
                        if (ts.movePrevious()) {
                            Token<? extends JsTokenId> previous = LexUtilities.findPreviousNonWsNonComment(ts);
                            if (previous != null) {
                                previousId = previous.id();
                            }

                            ts.moveIndex(index);
                            ts.moveNext();
                        }
                        // We don't have multiline string literals in JavaScript!
                        if (id == JsTokenId.BLOCK_COMMENT || id == JsTokenId.DOC_COMMENT) {
                            if (ts.offset() == pos) {
                                lineType = IN_BLOCK_COMMENT_START;
                                //originallockCommentIndention = GsfUtilities.getLineIndent(doc, offset);
                            } else {
                                lineType =  IN_BLOCK_COMMENT_MIDDLE;
                            }
                        } else if (LexUtilities.isBinaryOperator(id, previousId)) {
                            // If a line starts with a non unary operator we can
                            // assume it's a continuation from a previous line
                            continued = true;
                        } else if (id == JsTokenId.STRING || id == JsTokenId.STRING_END ||
                                id == JsTokenId.TEMPLATE || id == JsTokenId.TEMPLATE_END ||
                                id == JsTokenId.REGEXP || id == JsTokenId.REGEXP_END) {
                            // You can get multiline literals in JavaScript by inserting a \ at the end
                            // of the line
                            lineType = IN_LITERAL;
                        }
                    } else {
                        // No ruby token -- leave the formatting alone!
                        // (Probably in an RHTML file on a line with no JavaScript)
                        lineType = IN_LITERAL;
                    }
                }

                int hangingIndent = continued ? (continuationIndent) : 0;

                if (lineType == IN_LITERAL) {
                    // Skip this line - leave formatting as it is prior to reformatting
                    indent = GsfUtilities.getLineIndent(doc, offset);

                    // No compound indent for JavaScript
                    //                    if (embeddedJavaScript && indentHtml && balance > 0) {
                    //                        indent += balance * indentSize;
                    //                    }
                } else if (lineType == IN_BLOCK_COMMENT_MIDDLE) {
                    if (doc.getText(pos,1).charAt(0) == '*') {
                        // *-lines get indented to be flushed with the * in /*, other lines
                        // get indented to be aligned with the presumably indented text content!
                        //indent = LexUtilities.getLineIndent(doc, ts.offset())+1;
                        indent = adjustedBlockCommentIndention+1;
                    } else {
                        // Leave indentation of comment blocks alone since they probably correspond
                        // to commented out code - we don't want to lose the indentation.
                        // Possibly, I could shift the code all relative to the first line
                        // in the commented out block... A possible later enhancement.
                        // This shifts by the starting line which is wrong - should use the first comment line
                        //indent = LexUtilities.getLineIndent(doc, offset)-originallockCommentIndention+adjustedBlockCommentIndention;
                        indent = GsfUtilities.getLineIndent(doc, offset);
                    }
                } else if ((!indentOnly || offset < context.getCaretLineStart() || offset > context.getCaretLineEnd()) && (endIndents = isEndIndent(context, offset)) > 0) {
                    indent = (balance-endIndents) * indentSize + hangingIndent + initialIndent;
                } else {
                    assert lineType == IN_CODE || lineType == IN_BLOCK_COMMENT_START;
                    indent = balance * indentSize + hangingIndent + initialIndent;

//                    System.out.println("### indent " + indent + " = " + balance + " * " + indentSize + " + " + hangingIndent + " + " + initialIndent);

                    if (lineType == IN_BLOCK_COMMENT_START) {
                        adjustedBlockCommentIndention = indent;
                    }
                }

                if (indent < 0) {
                    indent = 0;
                }

                int lineBegin = LineDocumentUtils.getLineFirstNonWhitespace(ld, offset);

                // Insert whitespace on empty lines too -- needed for abbreviations expansion
                if (lineBegin != -1 || indentEmptyLines) {
                    // Don't do a hanging indent if we're already indenting beyond the parent level?

                    context.addIndentation(new IndentContext.Indentation(offset, indent, continued));
                }

                int endOfLine = LineDocumentUtils.getLineEnd(ld, offset) + 1;

                if (lineBegin != -1) {
                    balance += getTokenBalance(context, ts, lineBegin, endOfLine, true, indentOnly);
                    int bracketDelta = getTokenBalance(context, ts, lineBegin, endOfLine, false, indentOnly);
                    bracketBalance += bracketDelta;
                    continued = isContinuation(doc, offset, bracketBalance, continued, bracketDelta, context.getBlocks().isEmpty() ? null : context.getBlocks().peek());
                }

                offset = endOfLine;
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
    }

    private int getTokenBalance(IndentContext context, TokenSequence<? extends JsTokenId> ts, int begin, int end, boolean includeKeywords, boolean indentOnly) {
        int balance = 0;
        Document doc = context.getDocument();

        if (ts == null) {
            // remember indent of previous html tag
            context.setEmbeddedIndent(GsfUtilities.getLineIndent(doc, begin));
            return 0;
        }

        ts.move(begin);

        if (!ts.moveNext()) {
            return 0;
        }

        int last = begin;
        do {
            Token<? extends JsTokenId> token = ts.token();
            if (token == null) {
                break;
            }
            JsTokenId id = token.id();

            if (includeKeywords) {
                int delta = getTokenBalanceDelta(context, id, ts, indentOnly);
                balance += delta;
            } else {
                balance += getBracketBalanceDelta(id);
            }
            last = ts.offset() + token.length();
        } while (ts.moveNext() && (ts.offset() < end));

        if (context.isEmbedded() && last < end) {
            // We're not done yet... find the next section...
            TokenSequence<? extends JsTokenId> ets = LexUtilities.getNextJsTokenSequence(doc, last+1, end, language);
            if (ets != null && ets.offset() > begin) {
                return balance + getTokenBalance(context, ets, ets.offset(), end, includeKeywords, indentOnly);
            }
        }

        return balance;
    }

    private int getBracketBalanceDelta(JsTokenId id) {
        if (id == JsTokenId.BRACKET_LEFT_PAREN || id == JsTokenId.BRACKET_LEFT_BRACKET) {
            return 1;
        } else if (id == JsTokenId.BRACKET_RIGHT_PAREN || id == JsTokenId.BRACKET_RIGHT_BRACKET) {
            return -1;
        }
        return 0;
    }

    private int getTokenBalanceDelta(IndentContext context, JsTokenId id, TokenSequence<? extends JsTokenId> ts, boolean indentOnly) {
        try {
            Document doc = context.getDocument();
            LineDocument ld = LineDocumentUtils.asRequired(doc, LineDocument.class);
            OffsetRange range;
            if (id == JsTokenId.BRACKET_LEFT_BRACKET) {
                // block with braces, just record it to stack and return 1
                context.getBlocks().push(new IndentContext.BlockDescription(
                        false, false, new OffsetRange(ts.offset(), ts.offset())));
                return 1;
            } else if (id == JsTokenId.BRACKET_LEFT_CURLY) {
                // block with braces, just record it to stack and return 1
                // also check and mark if it is object literal block
                boolean object = false;
                TokenSequence<? extends JsTokenId> inner = LexUtilities.getPositionedSequence(doc, ts.offset(), language);
                if (inner != null) {
                    inner.movePrevious();
                    Token<? extends JsTokenId> token = LexUtilities.findPreviousNonWsNonComment(inner);
                    // simple detection of "function foo() {" and "function () {"
                    // "if () {", "with () {", "catch () {", "finally {"
                    // "do {", "while () {", "for () {"
                    if (token.id() != JsTokenId.BRACKET_RIGHT_PAREN
                            && token.id() != JsTokenId.KEYWORD_DO
                            && token.id() != JsTokenId.KEYWORD_ELSE
                            && token.id() != JsTokenId.KEYWORD_FINALLY) {
                        object = true;
                    }
                }
                context.getBlocks().push(new IndentContext.BlockDescription(
                        false, object, new OffsetRange(ts.offset(), ts.offset())));
                return 1;
            } else if (id == JsTokenId.KEYWORD_CASE || id == JsTokenId.KEYWORD_DEFAULT) {

                int index = ts.index();

                // find colon ':'
                LexUtilities.findNextIncluding(ts, Collections.singletonList(JsTokenId.OPERATOR_COLON));

                // skip whitespaces, comments and newlines
                Token<? extends JsTokenId> token = LexUtilities.findNextNonWsNonComment(ts);
                JsTokenId tokenId = token.id();

                if (tokenId == JsTokenId.KEYWORD_CASE || tokenId == JsTokenId.KEYWORD_DEFAULT) {
                    return 0;
                } else if (tokenId == JsTokenId.BRACKET_RIGHT_CURLY) {
                    return -1;
                } else {
                    // look at the beginning of next line if there is case or default
                    LexUtilities.findNextIncluding(ts, Collections.singletonList(JsTokenId.EOL));
                    LexUtilities.findNextNonWsNonComment(ts);
                    if (ts.token().id() == JsTokenId.KEYWORD_CASE || ts.token().id() == JsTokenId.KEYWORD_DEFAULT
                            || ts.token().id() == JsTokenId.BRACKET_LEFT_CURLY) {
                        return 0;
                    }
                }

                ts.moveIndex(index);
                ts.moveNext();

                return 1;
            } else if (id == JsTokenId.BRACKET_RIGHT_BRACKET || id == JsTokenId.BRACKET_RIGHT_CURLY) {
                /*
                 * End of braces block.
                 * If we are not on same line where block started, try to push
                 * all braceless blocks from stack and decrease indent for them,
                 * otherwise just decrese indent by 1.
                 * For example:
                 * if (true)
                 *   if (true)
                 *     if (true)
                 *       foo();     // we should decrease indent by 3 levels
                 *
                 * but:
                 * if (true)
                 *   if (true)
                 *     if (map[0]) // at ']' we should decrease only by 1
                 *       foo();
                 */
                int delta = -1;
                IndentContext.BlockDescription lastPop = context.getBlocks().empty() ? null : context.getBlocks().pop();
                if (lastPop != null && lastPop.getRange().getStart() <= (doc.getLength() + 1)
                        && LineDocumentUtils.getLineIndex(ld, lastPop.getRange().getStart()) != LineDocumentUtils.getLineIndex(ld, ts.offset())) {
                    int blocks = 0;
                    while (!context.getBlocks().empty() && context.getBlocks().peek().isBraceless()) {
                        context.getBlocks().pop();
                        blocks++;
                    }
                    delta -= blocks;
                }
                return delta;
            } else if ((range = LexUtilities.getMultilineRange(doc, ts)) != OffsetRange.NONE) {
                // we found braceless block, let's record it in the stack
                context.getBlocks().push(new IndentContext.BlockDescription(true, false, range));
            } else if (id == JsTokenId.EOL) {

                if (!indentOnly) {
                    TokenSequence<? extends JsTokenId> inner = LexUtilities.getPositionedSequence(doc, ts.offset(), language);
                    // skip whitespaces and newlines
                    Token<? extends JsTokenId> nextToken = null;
                    if (inner != null) {
                        nextToken = LexUtilities.findNextNonWsNonComment(inner);
                    }
                    TokenId tokenId = nextToken == null ? null : nextToken.id();
                    if (tokenId == JsTokenId.BRACKET_RIGHT_CURLY) {
                        // if it is end of 'switch'
                        OffsetRange offsetRange = LexUtilities.findBwd(doc, inner, JsTokenId.BRACKET_LEFT_CURLY, JsTokenId.BRACKET_RIGHT_CURLY);
                        if (offsetRange != OffsetRange.NONE) {
                            inner.movePrevious();
                            if (LexUtilities.skipParenthesis(inner, true)) {
                                Token<? extends JsTokenId> token = inner.token();
                                token = LexUtilities.findPreviousNonWsNonComment(inner);
                                if (token.id() == JsTokenId.KEYWORD_SWITCH) {
                                    return -1;
                                }
                            }
                        }
                    } else if (tokenId == JsTokenId.KEYWORD_CASE || tokenId == JsTokenId.KEYWORD_DEFAULT) {
                        inner = LexUtilities.getPositionedSequence(doc, ts.offset(), language);
                        Token<? extends JsTokenId> prevToken = LexUtilities.findPreviousNonWsNonComment(inner);
                        if (prevToken.id() != JsTokenId.BRACKET_LEFT_CURLY) {
                            // it must be case or default
                            inner = LexUtilities.getPositionedSequence(doc, ts.offset(), language);
                            LexUtilities.findPreviousIncluding(inner,
                                    Arrays.asList(JsTokenId.KEYWORD_CASE, JsTokenId.KEYWORD_DEFAULT));

                            int offset = inner.offset();
                            inner = LexUtilities.getPositionedSequence(doc, ts.offset(), language);
                            prevToken = LexUtilities.findPrevious(inner, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.EOL));

                            int beginLine = LineDocumentUtils.getLineIndex(ld, offset);
                            int eolLine = LineDocumentUtils.getLineIndex(ld, ts.offset());

                            // we need to take care of case like this:
                            // case 'a':
                            //      test();
                            //      break;
                            //
                            //      //comment
                            //
                            //    case 'b':
                            //      test();
                            //      break;
                            // note the comment - we would get to this block twice
                            // (eol after break and eol after //comment)
                            // so indentation level change would be -2 instead of -1
                            if (prevToken.id() != JsTokenId.BLOCK_COMMENT
                                    && prevToken.id() != JsTokenId.DOC_COMMENT
                                    && prevToken.id() != JsTokenId.LINE_COMMENT) {
                                if (beginLine != eolLine) {
                                    if (prevToken.id() == JsTokenId.BRACKET_RIGHT_CURLY) {
                                        OffsetRange offsetRange = LexUtilities.findBwd(doc, inner, JsTokenId.BRACKET_LEFT_CURLY, JsTokenId.BRACKET_RIGHT_CURLY);
                                        if (offsetRange != OffsetRange.NONE) {
                                            inner.movePrevious();
                                            Token<? extends JsTokenId> token = LexUtilities.findPreviousNonWsNonComment(inner);
                                            // if this is not the whole case in block
                                            // case 's':
                                            // {
                                            //    something();
                                            // }
                                            if (token.id() != JsTokenId.OPERATOR_COLON) {
                                                return -1;
                                            }
                                        }
                                    } else {
                                        return -1;
                                    }
                                }
                            } else {
                                int commentLine = LineDocumentUtils.getLineIndex(ld, inner.offset());
                                if (beginLine != eolLine && commentLine == beginLine) {
                                    return -1;
                                }
                            }
                        }
                    }
                }

                // other
                if (!context.getBlocks().empty()) {
                    if (context.getBlocks().peek().isBraceless()) {
                        // end of line after braceless block start
                        OffsetRange stackOffset = context.getBlocks().peek().getRange();
                        if (stackOffset.containsInclusive(ts.offset())) {
                            if (indentOnly) {
                                // enter pressed in braceless block
                                return 1;
                            }
                            // we are in the braceless block statement
                            int stackEndLine = LineDocumentUtils.getLineIndex(ld, stackOffset.getEnd());
                            int offsetLine = LineDocumentUtils.getLineIndex(ld, ts.offset());
                            if (stackEndLine == offsetLine) {
                                // if we are at the last line of braceless block statement
                                // increse indent by 1
                                return 1;
                            }
                        } else {
                            // we are not in braceless block statement,
                            // let's decrease indent for all braceless blocks in top of stack (if any)
                            int blocks = 0;
                            while (!context.getBlocks().empty() && context.getBlocks().peek().isBraceless()) {
                                blocks++;
                                context.getBlocks().pop();
                            }
                            return -blocks;
                        }
                    }
                }
            }
        } catch (BadLocationException ble) {
            LOGGER.log(Level.INFO, null, ble);
        }
        return 0;
    }

    private int isEndIndent(IndentContext context, int offset) throws BadLocationException {
        Document doc = context.getDocument();
        LineDocument ld = LineDocumentUtils.as(doc, LineDocument.class);
        if (ld == null) {
            return 0;
        }
        int lineBegin = LineDocumentUtils.getLineFirstNonWhitespace(ld, offset);

        if (lineBegin != -1) {
            Token<?extends JsTokenId> token = getFirstToken(context, offset);

            if (token == null) {
                return 0;
            }

            TokenId id = token.id();

            // If the line starts with an end-marker, such as "end", "}", "]", etc.,
            // find the corresponding opening marker, and indent the line to the same
            // offset as the beginning of that line.
            if (id == JsTokenId.BRACKET_RIGHT_CURLY || id == JsTokenId.BRACKET_RIGHT_BRACKET
                    /*|| id == JsTokenId.BRACKET_RIGHT_PAREN*/) {
                int indents = 1;

                // Check if there are multiple end markers here... if so increase indent level.
                // This should really do an iteration... for now just handling the most common
                // scenario in JavaScript where we have }) in object literals
                int lineEnd = LineDocumentUtils.getLineEnd(ld, offset);
                int newOffset = offset;
                while (newOffset < lineEnd && token != null) {
                    newOffset += token.length();
                    if (newOffset < doc.getLength()) {
                        token = LexUtilities.getToken(doc, newOffset, language);
                        if (token != null) {
                            id = token.id();
                            if (id == JsTokenId.WHITESPACE) {
                                continue;
                            } else {
                                break;
                            }
                        }
                    }
                }

                return indents;
            }
        }

        return 0;
    }

    private Token<? extends JsTokenId> getFirstToken(IndentContext context, int offset) throws BadLocationException {
        Document doc = context.getDocument();
        LineDocument ld = LineDocumentUtils.as(doc, LineDocument.class);
        if (ld == null) {
            return null;
        }
        int lineBegin = LineDocumentUtils.getLineFirstNonWhitespace(ld, offset);
        if (lineBegin != -1) {
            if (context.isEmbedded()) {
                TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(
                        TokenHierarchy.get(doc), lineBegin, language);
                if (ts != null) {
                    ts.moveNext();
                    Token<? extends JsTokenId> token = ts.token();
                    while (token != null && token.id() == JsTokenId.WHITESPACE) {
                        if (!ts.moveNext()) {
                            return null;
                        }
                        token = ts.token();
                    }
                    return token;
                }
            } else {
                return LexUtilities.getToken(doc, lineBegin, language);
            }
        }

        return null;
    }

    /**
     * Iterates tokens from token to limit while properly updating indentation
     * level. Returns the new index in token sequence.
     *
     * @param token start token
     * @param index start index
     * @param limit end token
     * @param formatContext context to update
     * @return the new index
     */
    private void moveForward(FormatToken token, FormatToken limit,
            FormatContext formatContext, CodeStyle.Holder codeStyle, boolean allowComment) {

        for (FormatToken current = token; current != null && current != limit; current = current.next()) {
            assert current.isVirtual()
                    || current.getKind() == FormatToken.Kind.WHITESPACE
                    || current.getKind() == FormatToken.Kind.EOL
                    || allowComment
                        && (current.getKind() == FormatToken.Kind.BLOCK_COMMENT
                        || current.getKind() == FormatToken.Kind.LINE_COMMENT
                        || current.getKind() == FormatToken.Kind.DOC_COMMENT): current;

            processed.add(current);
            updateIndentationLevel(current, formatContext, codeStyle);
            if (current.getKind() == FormatToken.Kind.EOL) {
                formatContext.setCurrentLineStart(current.getOffset()
                        + 1 + formatContext.getOffsetDiff());
                formatContext.setLastLineWrap(null);
            }
        }
    }

    private static boolean isWhitespace(CharSequence charSequence) {
        for (int i = 0; i < charSequence.length(); i++) {
            if (!Character.isWhitespace(charSequence.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSingleLineEmbedding(FormatToken token, Document doc, Snapshot snapshot) {
        final FormatToken prevNonVirtual = FormatTokenStream.getPreviousNonVirtual(token);
        if (prevNonVirtual != null) {
            final int originalOffset = snapshot.getOriginalOffset(prevNonVirtual.getOffset());
            final List<TokenSequence<?>> tokenSeqs = TokenHierarchy.get(doc)
                    .embeddedTokenSequences(originalOffset, false);
            final String snapshotMimePath = snapshot.getMimePath().getPath();
            for (TokenSequence<?> ts : tokenSeqs) {
                if (ts.languagePath().mimePath()
                        .concat("/") //NOI18N
                        .concat(snapshot.getMimeType()).equals(snapshotMimePath)) {
                    ts.move(originalOffset);
                    ts.moveNext();
                    if (ts.token() != null) {
                        final String tokenText = ts.token().text().toString();
                        // if text of the token already contains newline char
                        // it is not single-line embedding
                        return !tokenText.contains("\n"); //NOI18N
                    }
                    return false;
                }
            }
        }
        return false;
    }

    static class Indentation {

        static final Indentation ALLOWED = new Indentation(true, false);

        static final Indentation FORBIDDEN = new Indentation(false, false);

        private final boolean allowed;

        private final boolean exceedLimits;

        public Indentation(boolean allowed, boolean exceedLimits) {
            this.allowed = allowed;
            this.exceedLimits = exceedLimits;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public boolean isExceedLimits() {
            return exceedLimits;
        }
    }
}
