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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.javascript2.editor.embedding.JsEmbeddingProvider;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author Petr Hejl
 */
public final class FormatContext {

    private static final Logger LOGGER = Logger.getLogger(FormatContext.class.getName());

    private static final Pattern SAFE_DELETE_PATTERN = Pattern.compile("\\s*"); // NOI18N

    private static final Comparator<Region> REGION_COMPARATOR = (Region o1, Region o2) -> {
        if (o1.getOriginalStart() < o2.getOriginalStart()) {
            return -1;
        }
        if (o1.getOriginalStart() > o2.getOriginalStart()) {
            return 1;
        }
        return 0;
    };

    private final Context context;

    private final Snapshot snapshot;

    private final Language<JsTokenId> languange;

    private final Defaults.Provider provider;

    private final FunctionNode root;

    private final FormatTokenStream stream;

    private final int initialStart;

    private final int initialEnd;

    private final List<Region> regions;

    private final boolean embedded;

    private final Stack<JsxBlock> jsxIndents = new Stack<>();

    private final Map<FormatToken, JsxBlock> jsxIndentsMap = new HashMap<>();

    private final Deque<JsxElement> jsxPath = new ArrayDeque<>();

    private LineWrap lastLineWrap;

    private int indentationLevel;

    private int continuationLevel;

    private int offsetDiff;

    private int currentLineStart;

    private boolean pendingContinuation;

    private int tabCount;

    public FormatContext(Context context, Defaults.Provider provider,
            Snapshot snapshot, Language<JsTokenId> language, FunctionNode root, FormatTokenStream stream) {
        this.context = context;
        this.snapshot = snapshot;
        this.languange = language;
        this.provider = provider;
        this.root = root;
        this.stream = stream;
        this.initialStart = context.startOffset();
        this.initialEnd = context.endOffset();

        regions = new ArrayList<>(context.indentRegions().size());
        for (Context.Region region : context.indentRegions()) {
            regions.add(new Region(region));
        }
        regions.sort(REGION_COMPARATOR);

        dumpRegions();

        this.embedded = JsParserResult.isEmbedded(snapshot);

        /*
         * What we do here is fix for case like this:
         * <head>
         *     <script>[REGION_START]
         *         var x = 1;
         *         function test() {
         *             x ="";
         *         }
         *     [REGION_END]</script>
         * </head>
         *
         * The last line with REGION_END would be considered empty line and
         * truncated. So we could either avoid that or shift the REGION_END to
         * the line start offset. We do the latter.
         */
        if (embedded) {
            for (Region region : regions) {
                int endOffset = region.getOriginalEnd();
                try {
                    int lineOffset = context.lineStartOffset(endOffset);
                    TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(
                        snapshot, region.getOriginalStart(), language);
                    if (ts != null) {
                        int embeddedOffset = snapshot.getEmbeddedOffset(lineOffset);
                        if (embeddedOffset >= 0) {
                            ts.move(embeddedOffset);
                            if (ts.moveNext()) {
                                Token<? extends JsTokenId> token = ts.token();
                                // BEWARE whitespace must span across the whole line
                                if (token.id() == JsTokenId.WHITESPACE
                                        && (lineOffset + token.length()) == endOffset) {
                                    region.setOriginalEnd(lineOffset);
                                }
                            }
                        }
                    }
                } catch (BadLocationException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }

            LOGGER.log(Level.FINE, "Tuned regions");
            dumpRegions();
        }
    }

    public Context getContext() {
        return context;
    }

    public Defaults.Provider getDefaultsProvider() {
        return provider;
    }

    public void incJsxIndentation(FormatToken token) {
        assert token.getKind() == FormatToken.Kind.BEFORE_JSX_BLOCK_START;
        assert token.next() != null;

        FormatToken next = token.next();
        while (next != null && next.getId() != JsTokenId.JSX_TEXT) {
            next = next.next();
        }
        Integer indent = next != null ? getSuggestedIndentation(next.getOffset()) : null;
        if (indent == null) {
            indent = next != null ? stream.getOriginalIndent(next) : null;
        }
        int value = indent != null ? indent : 0;

        int current = value;
        try {
            current = context.lineIndent(context.lineStartOffset(token.next().getOffset() + offsetDiff));
        } catch (BadLocationException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        JsxBlock block = new JsxBlock(value, 0, 0, current);
        jsxIndents.push(block);
        jsxIndentsMap.put(token, block);
    }

    public void updateJsxIndentation(FormatToken token) {
        assert token.getKind() == FormatToken.Kind.BEFORE_JSX_BLOCK_START;
        assert token.next() != null;
        try {
            int indent = context.lineIndent(context.lineStartOffset(token.next().getOffset() + offsetDiff));
            JsxBlock current = jsxIndentsMap.get(token);
            if (current != null) {
                current.update(indentationLevel, continuationLevel, indent);
            }
        } catch (BadLocationException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

    public void decJsxIndentation(FormatToken token) {
        assert token.getKind() == FormatToken.Kind.AFTER_JSX_BLOCK_END;
        jsxIndentsMap.remove(token);
        jsxIndents.pop();
    }

    public int getJsxIndentation() {
        if (jsxIndents.isEmpty()) {
            return 0;
        }
        return jsxIndents.peek().getIndent();
    }

    public int getBaseJsxIndentation() {
        if (jsxIndents.isEmpty()) {
            return 0;
        }
        return jsxIndents.peek().getBaseIndent();
    }

    public boolean isInsideJsx() {
        return !jsxIndents.isEmpty();
    }

    public void updateJsxPath(char first, Character second) {
        assert isInsideJsx();
        switch (first) {
            case '<':
                jsxPath.push(new JsxElement(JsxElement.Type.TAG, null));
                break;
            case '>':
                JsxElement element = jsxPath.isEmpty() ? null : jsxPath.peek();
                if (element != null && element.getType() == JsxElement.Type.TAG) {
                    jsxPath.pop();
                }
                break;
            case '=':
                if (!jsxPath.isEmpty() && jsxPath.peek().getType() == JsxElement.Type.TAG) {
                    if (second != null) {
                        if (second == '{') {
                            jsxPath.push(new JsxElement(JsxElement.Type.ATTRIBUTE, '}'));
                        } else if (second == '"' || second == '\'') {
                            jsxPath.push(new JsxElement(JsxElement.Type.ATTRIBUTE, second));
                        }
                    }
                }
                break;
            case '\'':
            case '"':
            case '}':
                element = jsxPath.isEmpty() ? null : jsxPath.peek();
                if (element != null && element.getType() == JsxElement.Type.ATTRIBUTE && element.getClosingChar() == first) {
                    jsxPath.pop();
                }
                break;
            default:
                break;
        }
    }

    public Integer getSuggestedIndentation(FormatToken token) {
        if (jsxIndents.isEmpty()) {
            return 0;
        }
        if (!jsxPath.isEmpty() && jsxPath.peek().getType() == JsxElement.Type.ATTRIBUTE) {
            return 0;
        }
        Integer value = getSuggestedIndentation(token.getOffset());
        return value == null ? 0 : value;
    }

    public void setLastLineWrap(LineWrap lineWrap) {
        this.lastLineWrap = lineWrap;
    }

    public LineWrap getLastLineWrap() {
        return lastLineWrap;
    }

    public int getCurrentLineStart() {
        return currentLineStart;
    }

    public void setCurrentLineStart(int currentLineStart) {
        this.currentLineStart = currentLineStart;
    }

    public int getIndentationLevel() {
        if (!jsxIndents.isEmpty()) {
            return indentationLevel - jsxIndents.peek().getIndentationLevel();
        }
        return indentationLevel;
    }

    public void incIndentationLevel() {
        this.indentationLevel++;
    }

    public void decIndentationLevel() {
        this.indentationLevel--;
    }

    public int getContinuationLevel() {
        if (!jsxIndents.isEmpty()) {
            return continuationLevel - jsxIndents.peek().getContinuationLevel();
        }
        return continuationLevel;
    }

    public void incContinuationLevel() {
        this.continuationLevel++;
    }

    public void decContinuationLevel() {
        this.continuationLevel--;
    }

    public boolean isPendingContinuation() {
        return pendingContinuation;
    }

    public void setPendingContinuation(boolean pendingContinuation) {
        this.pendingContinuation = pendingContinuation;
    }

    public void incTabCount() {
        this.tabCount++;
    }

    public void resetTabCount() {
        this.tabCount = 0;
    }

    public int getTabCount() {
        return tabCount;
    }

    public int getOffsetDiff() {
        return offsetDiff;
    }

    private void setOffsetDiff(int offsetDiff) {
        this.offsetDiff = offsetDiff;
    }

    private void dumpRegions() {
        if (!LOGGER.isLoggable(Level.FINE)) {
            return;
        }

        for (Region region : regions) {
            try {
                LOGGER.log(Level.FINE, "{0}:{1}:{2}", new Object[]{
                    region.getOriginalStart(),
                    region.getOriginalEnd(),
                    getDocument().getText(region.getOriginalStart(), region.getOriginalEnd() - region.getOriginalStart())
                });
            } catch (BadLocationException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
        }
    }


    public int getEmbeddedRegionEnd(int offset) {
        if (!embedded) {
            return -1;
        }

        int docOffset = snapshot.getOriginalOffset(offset);
        if (docOffset < 0) {
            return -1;
        }

        for (Region region : regions) {
            if (docOffset >= region.getOriginalStart() && docOffset < region.getOriginalEnd()) {
                return snapshot.getEmbeddedOffset(region.getOriginalEnd());
            }
        }
        return -1;
    }

    public int getDocumentOffset(int offset) {
        return getDocumentOffset(offset, true);
    }

    private int getDocumentOffset(int offset, boolean check) {
        if (!embedded) {
            if (!check || (offset >= initialStart && offset < initialEnd)) {
                return offset;
            }
            return -1;
        }

        int docOffset = snapshot.getOriginalOffset(offset);
        if (docOffset < 0) {
            return -1;
        }

        for (Region region : regions) {
            if (docOffset >= region.getOriginalStart() && docOffset < region.getOriginalEnd()) {
                return docOffset;
            }
        }
        return -1;
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public int getEmbeddingIndent(FormatTokenStream stream, FormatToken token) {
        if (!embedded) {
            return 0;
        }

        int docOffset = snapshot.getOriginalOffset(token.getOffset());
        if (docOffset < 0) {
            return 0;
        }

        Region start = null;
        int i = 0;
        for (Region region : regions) {
            if (docOffset >= region.getOriginalStart() && docOffset < region.getOriginalEnd()) {
                start = region;
                break;
            }
            i++;
        }
        if (start != null && start.getInitialIndentation() < 0) {
            try {
                // this is bit hacky
                boolean nonEmpty = false;
                // the region may unfortunately start in the middle of token
                // so we have to query for token containing the offset
                // covered by embeddedSimple3.php
                FormatToken startToken = stream.getCoveringToken(
                        snapshot.getEmbeddedOffset(start.getOriginalStart()));
                // in case we have different regions and space
                // between those regions is not empty (more precisely
                // there is __UNKNOWN__ marker from embedding provider
                // it means there is some other language fragment included
                // in that case we continue with indentation from previous
                // region
                // sample <script>
                // function foo() {
                //     var x = 1 + ${some_other_lang};
                // }
                // </script>
                if (startToken != null) {
                    FormatToken previous = startToken.previous();
                    while (previous != null) {
                        if (!previous.isVirtual()) {
                            if (getDocumentOffset(previous.getOffset()) >= 0) {
                                // there might be zero real tokens between regions
                                // in case these are not joined
                                nonEmpty = startToken == FormatTokenStream.getNextNonVirtual(previous);
                                break;
                            }
// issue #226147
//                            nonEmpty = previous.getKind() != FormatToken.Kind.WHITESPACE
//                                    && previous.getKind() != FormatToken.Kind.EOL;
                            nonEmpty = JsEmbeddingProvider.isGeneratedIdentifier(previous.getText().toString());
                        }
                        if (nonEmpty) {
                            break;
                        }
                        previous = previous.previous();
                    }
                }
                if (nonEmpty && i > 0) {
                    // some regions may have been skipped (no indentation
                    // query - call to this method) so we have to find
                    // the right one rolling back
                    // covered by embeddedSimple7.php
                    Region regionWithIndentation = null;
                    for (int j = i - 1; j >= 0; j--) {
                        if (regions.get(j).getInitialIndentation() >= 0) {
                            regionWithIndentation = regions.get(j);
                            break;
                        }
                    }
                    if (regionWithIndentation != null) {
                        start.setInitialIndentation(regionWithIndentation.getInitialIndentation());
                    } else {
                        start.setInitialIndentation(0);
                    }
                } else {
                    if (start.getOriginalStart() <= 0) {
                        // see #246093
                        start.setInitialIndentation(0);
                    } else {
                        // for case like <script>\nfoo();\n</script>
                        // we get the inital indentation from already indented
                        // <script> line
                        start.setInitialIndentation(context.lineIndent(context.lineStartOffset(start.getContextRegion().getStartOffset()))
                                + IndentUtils.indentLevelSize(getDocument()));
                    }
                }
            } catch (BadLocationException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
        return start != null ? start.getInitialIndentation() : 0;
    }

    public boolean isGenerated(FormatToken token) {
        // XXX it may be better to replace with
        // return embedded && getDocumentOffset(token.getOffset()) < 0;
        return embedded && JsEmbeddingProvider.isGeneratedIdentifier(token.getText().toString());
    }

    public boolean isBrokenSource() {
        return embedded && root == null;
    }

    public Document getDocument() {
        return context.document();
    }

    private LineDocument getLineDocument() {
        return LineDocumentUtils.as(context.document(), LineDocument.class);
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public void indentLine(int voffset, int indentationSize,
            JsFormatter.Indentation indentationCheck, CodeStyle.Holder codeStyle) {

        indentLineWithOffsetDiff(voffset, indentationSize, indentationCheck, offsetDiff, codeStyle);
    }

    public void indentLineWithOffsetDiff(int voffset, int indentationSize,
            JsFormatter.Indentation indentationCheck, int realOffsetDiff, CodeStyle.Holder codeStyle) {

        if (!indentationCheck.isAllowed()) {
            return;
        }

        int offset = getDocumentOffset(voffset, !indentationCheck.isExceedLimits());
        if (offset < 0) {
            return;
        }

        try {
            int diff = setLineIndentation(getLineDocument(),
                    offset + realOffsetDiff, indentationSize, codeStyle);
            setOffsetDiff(offsetDiff + diff);
        } catch (BadLocationException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

    public void insert(int voffset, String newString) {
        insertWithOffsetDiff(voffset, newString, offsetDiff);
    }

    public void insertWithOffsetDiff(int voffset, String newString, int realOffsetDiff) {
        int offset = getDocumentOffset(voffset);
        if (offset < 0) {
            return;
        }

        Document doc = getDocument();
        try {
            doc.insertString(offset + realOffsetDiff, newString, null);
            setOffsetDiff(offsetDiff + newString.length());
        } catch (BadLocationException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

    public void replace(int voffset, String oldString, String newString) {
        if (oldString.equals(newString)) {
            return;
        }

        replace(voffset, oldString.length(), newString);
    }

    public void replace(int voffset, int vlength, String newString) {
        int offset = getDocumentOffset(voffset);
        if (offset < 0) {
            return;
        }
        int length = computeLength(voffset, vlength);
        if (length <= 0) {
            insert(voffset, newString);
            return;
        }

        Document doc = getDocument();
        try {
            String oldText = doc.getText(offset + offsetDiff, length);
            if (newString.equals(oldText)) {
                return;
            }
            if (SAFE_DELETE_PATTERN.matcher(oldText).matches()) {
                doc.remove(offset + offsetDiff, length);
                doc.insertString(offset + offsetDiff, newString, null);
                setOffsetDiff(offsetDiff + (newString.length() - length));
            } else {
                LOGGER.log(Level.WARNING, "Tried to remove non empty text: {0}",
                        doc.getText(offset + offsetDiff, length));
            }
        } catch (BadLocationException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

    public void remove(int voffset, int vlength) {
        int offset = getDocumentOffset(voffset);
        if (offset < 0) {
            return;
        }
        int length = computeLength(voffset, vlength);
        if (length <= 0) {
            return;
        }

        Document doc = getDocument();
        try {
            if(doc.getText(offset + offsetDiff, length).contains("\n")) {
                LOGGER.log(Level.WARNING, "Tried to remove EOL");
            }
            if (SAFE_DELETE_PATTERN.matcher(doc.getText(offset + offsetDiff, length)).matches()) {
                doc.remove(offset + offsetDiff, length);
                setOffsetDiff(offsetDiff - length);
            } else {
                LOGGER.log(Level.WARNING, "Tried to remove non empty text: {0}",
                        doc.getText(offset + offsetDiff, length));
            }
        } catch (BadLocationException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

    private Integer getSuggestedIndentation(int offset) {
        LineDocument doc = getLineDocument();
        if (doc == null) {
            return null;
        }
        Map<Integer, Integer> suggestedLineIndents = (Map<Integer, Integer>) doc.getProperty("AbstractIndenter.lineIndents");
        if (suggestedLineIndents != null) {
            try {
                int lineIndex = LineDocumentUtils.getLineIndex(doc, offset + offsetDiff);
                Integer indent = suggestedLineIndents.get(lineIndex);
                return indent != null ? indent : null;
            } catch (BadLocationException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
        return null;
    }

    // TODO would be better to hadle on upper levels
    private int computeLength(int voffset, int length) {
        if (!embedded) {
            return length;
        }

        for (int i = 0; i < length; i++) {
            if (getDocumentOffset(voffset + i) < 0) {
                return i;
            }
        }
        return length;
    }

    // XXX copied from GsfUtilities
    private static int setLineIndentation(LineDocument doc, int lineOffset,
            int newIndent, CodeStyle.Holder codeStyle) throws BadLocationException {
        if (doc == null) {
            return 0;
        }
        int lineStartOffset = LineDocumentUtils.getLineStart(doc, lineOffset);

        // Determine old indent first together with oldIndentEndOffset
        int indent = 0;
        int tabSize = -1;
        CharSequence docText = DocumentUtilities.getText(doc);
        int oldIndentEndOffset = lineStartOffset;
        while (oldIndentEndOffset < docText.length()) {
            char ch = docText.charAt(oldIndentEndOffset);
            if (ch == '\n') {
                break;
            } else if (ch == '\t') {
                if (tabSize == -1) {
                    tabSize = codeStyle.tabSize;
                }
                // Round to next tab stop
                indent = (indent + tabSize) / tabSize * tabSize;
            } else if (Character.isWhitespace(ch)) {
                indent++;
            } else { // non-whitespace
                break;
            }
            oldIndentEndOffset++;
        }

        String newIndentString = IndentUtils.createIndentString(newIndent, codeStyle.expandTabsToSpaces, codeStyle.tabSize);
        // Attempt to match the begining characters
        int offset = lineStartOffset;
        boolean different = false;
        int i = 0;
        for (; i < newIndentString.length() && lineStartOffset + i < oldIndentEndOffset; i++) {
            if (newIndentString.charAt(i) != docText.charAt(lineStartOffset + i)) {
                offset = lineStartOffset + i;
                newIndentString = newIndentString.substring(i);
                different = true;
                break;
            }
        }
        if (!different) {
            offset = lineStartOffset + i;
            newIndentString = newIndentString.substring(i);
        }

        // Replace the old indent
        if (offset < oldIndentEndOffset) {
            doc.remove(offset, oldIndentEndOffset - offset);
        }
        if (newIndentString.length() > 0) {
            doc.insertString(offset, newIndentString, null);
        }
        return newIndentString.length() - (oldIndentEndOffset - offset);
    }

    public static class LineWrap {

        private final FormatToken token;

        private final int offsetDiff;

        private final int indentationLevel;

        private final int continuationLevel;

        public LineWrap(FormatToken token, int offsetDiff, int indentationLevel, int continuationLevel) {
            this.token = token;
            this.offsetDiff = offsetDiff;
            this.indentationLevel = indentationLevel;
            this.continuationLevel = continuationLevel;
        }

        public FormatToken getToken() {
            return token;
        }

        public int getOffsetDiff() {
            return offsetDiff;
        }

        public int getIndentationLevel() {
            return indentationLevel;
        }

        public int getContinuationLevel() {
            return continuationLevel;
        }

    }

    public static class ContinuationBlock {

        public enum Type {

            CURLY,

            BRACKET,

            PAREN
        }

        private final ContinuationBlock.Type type;

        private final boolean change;

        public ContinuationBlock(ContinuationBlock.Type type, boolean change) {
            this.type = type;
            this.change = change;
        }

        public ContinuationBlock.Type getType() {
            return type;
        }

        public boolean isChange() {
            return change;
        }
    }

    public static class JsxElement {

        public enum Type {

            TAG,

            ATTRIBUTE
        }

        private final Type type;

        private final Character closingChar;

        public JsxElement(Type type, Character closingChar) {
            assert type == Type.TAG || closingChar != null;
            this.type = type;
            this.closingChar = closingChar;
        }

        public Type getType() {
            return type;
        }

        public Character getClosingChar() {
            return closingChar;
        }
    }

    private static class JsxBlock {

        private final int baseIndent;

        private int indentationLevel;

        private int continuationLevel;

        private int indent;

        public JsxBlock(int baseIndent, int indentationLevel, int continuationLevel, int indent) {
            this.baseIndent = baseIndent;
            this.indentationLevel = indentationLevel;
            this.continuationLevel = continuationLevel;
            this.indent = indent;
        }

        public int getBaseIndent() {
            return baseIndent;
        }

        public int getIndentationLevel() {
            return indentationLevel;
        }

        public int getContinuationLevel() {
            return continuationLevel;
        }

        public int getIndent() {
            return indent;
        }

        public void update(int indentationLevel, int continuationLevel, int indent) {
            this.indentationLevel = indentationLevel;
            this.continuationLevel = continuationLevel;
            this.indent = indent;
        }
    }

    private static class Region {

        private final Context.Region contextRegion;

        private final int originalStart;

        private int originalEnd;

        private int initialIndentation = -1;

        public Region(Context.Region contextRegion) {
            this.contextRegion = contextRegion;
            this.originalStart = contextRegion.getStartOffset();
            this.originalEnd = contextRegion.getEndOffset();
        }

        public Context.Region getContextRegion() {
            return contextRegion;
        }

        public int getOriginalStart() {
            return originalStart;
        }

        public int getOriginalEnd() {
            return originalEnd;
        }

        public void setOriginalEnd(int originalEnd) {
            this.originalEnd = originalEnd;
        }

        public int getInitialIndentation() {
            return initialIndentation;
        }

        public void setInitialIndentation(int initialIndentation) {
            this.initialIndentation = initialIndentation;
        }
    }
}
