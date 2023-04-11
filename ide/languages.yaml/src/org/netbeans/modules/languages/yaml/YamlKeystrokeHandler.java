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
package org.netbeans.modules.languages.yaml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.spi.ParserResult;
import org.openide.util.Exceptions;

/**
 * Keystroke handler for YAML; handle newline indentation, auto matching of <%
 * %> etc.
 *
 * @author Tor Norbye
 */
public class YamlKeystrokeHandler implements KeystrokeHandler {

    @Override
    public boolean beforeCharInserted(Document document, int caretOffset, JTextComponent target, char c) throws BadLocationException {
        Caret caret = target.getCaret();
        BaseDocument doc = (BaseDocument) document;

        int dotPos = caret.getDot();
        int length = doc.getLength();

        // Primitive handling of backslash as escape character, far from accurate
        // but would work most of the time.
        if ((dotPos > 0) && "\\".equals(doc.getText(dotPos - 1, 1))) {
            return false;
        }

        if (c == ' ' && dotPos > 0 && dotPos <= length - 1) {
            try {
                String sb = doc.getText(dotPos - 1, 2);
                if ("{}".equals(sb) || "[]".equals(sb)) {
                    doc.insertString(dotPos, "  ", null);
                    caret.setDot(dotPos + 1);
                    return true;
                }
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            }
        }

        // Bracket matching on <% %>
        if (c == ' ' && dotPos >= 2) {
            try {
                String s = doc.getText(dotPos - 2, 2);
                if ("%=".equals(s) && dotPos >= 3) { // NOI18N
                    s = doc.getText(dotPos - 3, 3);
                }
                if ("<%".equals(s) || "<%=".equals(s)) { // NOI18N
                    doc.insertString(dotPos, "  ", null);
                    caret.setDot(dotPos + 1);
                    return true;
                }
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            }

            return false;
        }

        if ((c == '{')) {
            try {
                doc.insertString(dotPos, "{}", null);
                caret.setDot(dotPos + 1);
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            }

            return true;
        }

        if ((c == '[')) {
            try {
                doc.insertString(dotPos, "[]", null);
                caret.setDot(dotPos + 1);
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            }

            return true;
        }

        if (((c == '}') || (c == ']')) && dotPos < doc.getLength()) {
            if (String.valueOf(c).equals(doc.getText(dotPos, 1))) {
                caret.setDot(dotPos + 1);
                return true;
            }
        }
        
        if ((c == '\'') || (c == '"')) {
            int sstart = target.getSelectionStart();
            int send = target.getSelectionEnd();
            if ((sstart != send) && ((dotPos == sstart) || (dotPos == send))) {
                doc.insertString(sstart, String.valueOf(c), null);
                doc.insertString(send + 1, String.valueOf(c), null);
                caret.setDot(send + 2);
                return true;
            }
            int lineStart = LineDocumentUtils.getLineStart(doc, dotPos);
            int lineEnd = LineDocumentUtils.getLineEnd(doc, dotPos);
            char[] line = doc.getChars(lineStart, lineEnd - lineStart);

            int quotes = 0;
            for (int i = 0; i < line.length; i++) {
                char d = line[i];
                if ('\\' == d) {
                    i++;
                    continue;
                }
                if (c == d) quotes++;
            }

            // Try to keep the number of quotes even
            if ( quotes % 2 == 1 ) {
                // Inserting one if the number of quotes are odd
                return false;
            } else {
                if (dotPos > doc.getLength() - 1 || !doc.getText(dotPos, 1).equals(String.valueOf(c))) {
                    // Inserting double if the number of quotes are even
                    // Unless, the next character is a quote as well
                    doc.insertString(sstart, String.valueOf(c) + String.valueOf(c), null);
                }
                caret.setDot(dotPos + 1);
                return true;
            }
        }

        if ((dotPos > 0) && (c == '%' || c == '>')) {
            TokenHierarchy<Document> th = TokenHierarchy.get((Document) doc);
            TokenSequence<?> ts = th.tokenSequence();
            ts.move(dotPos);
            try {
                if (ts.moveNext() || ts.movePrevious()) {
                    Token<?> token = ts.token();
                    if (token.id() == YamlTokenId.TEXT && doc.getText(dotPos - 1, 1).charAt(0) == '<') {
                        // See if there's anything ahead
                        int first = LineDocumentUtils.getNextNonWhitespace(doc, dotPos, LineDocumentUtils.getLineEnd(doc, dotPos));
                        if (first == -1) {
                            doc.insertString(dotPos, "%%>", null); // NOI18N
                            caret.setDot(dotPos + 1);
                            return true;
                        }
                    } else if (token.id() == YamlTokenId.DELIMITER) {
                        String tokenText = token.text().toString();
                        if (tokenText.endsWith("%>")) { // NOI18N
                            // TODO - check that this offset is right
                            int tokenPos = (c == '%') ? dotPos : dotPos - 1;
                            CharSequence suffix = DocumentUtilities.getText(doc, tokenPos, 2);
                            if (CharSequenceUtilities.textEquals(suffix, "%>")) { // NOI18N
                                caret.setDot(dotPos + 1);
                                return true;
                            }
                        } else if (tokenText.endsWith("<")) {
                            // See if there's anything ahead
                            int first = LineDocumentUtils.getNextNonWhitespace(doc, dotPos, LineDocumentUtils.getLineEnd(doc, dotPos));
                            if (first == -1) {
                                doc.insertString(dotPos, "%%>", null); // NOI18N
                                caret.setDot(dotPos + 1);
                                return true;
                            }
                        }
                    } else if ((token.id() == YamlTokenId.RUBY || token.id() == YamlTokenId.RUBY_EXPR) && dotPos >= 1 && dotPos <= doc.getLength() - 3) {
                        // If you type ">" one space away from %> it's likely that you typed
                        // "<% foo %>" without looking at the screen; I had auto inserted %> at the end
                        // and because I also auto insert a space without typing through it, you've now
                        // ended up with "<% foo %> %>". Let's prevent this by interpreting typing a ""
                        // right before %> as a duplicate for %>.   I can't just do this on % since it's
                        // quite plausible you'd have
                        //   <% x = %q(foo) %>  -- if I simply moved the caret to %> when you typed the
                        // % in %q we'd be in trouble.
                        String s = doc.getText(dotPos - 1, 4);
                        if ("% %>".equals(s)) { // NOI18N
                            doc.remove(dotPos - 1, 2);
                            caret.setDot(dotPos + 1);
                            return true;
                        }
                    }
                }
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            }
        }

        return false;
    }

    @Override
    public boolean afterCharInserted(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    @Override
    public boolean charBackspaced(Document doc, int dotPos, JTextComponent target, char ch) throws BadLocationException {
        Caret caret = target.getCaret();
        if (ch == '%' && dotPos > 0 && dotPos <= doc.getLength() - 2) {
            String s = doc.getText(dotPos - 1, 3);
            if ("<%>".equals(s)) { // NOI18N
                doc.remove(dotPos, 2);
                return true;
            }
        }

        if ((ch == ' ') && (dotPos > 0) && (dotPos <= doc.getLength() - 2)) {
            String s = doc.getText(dotPos - 1, 3);
            if ("{ }".equals(s) || "[ ]".equals(s)) {
                doc.remove(dotPos, 1);
                return true;
            }
        }

        if ((ch == '{') && (dotPos <= doc.getLength() - 1)) {
            String s = doc.getText(dotPos, 1);
            if ("}".equals(s)) {
                doc.remove(dotPos, 1);
                return true;
            }
        }

        if ((ch == '[') && (dotPos <= doc.getLength() - 1)) {
            String s = doc.getText(dotPos, 1);
            if ("]".equals(s)) {
                doc.remove(dotPos, 1);
                return true;
            }
        }
        if (((ch == '\'') || (ch == '"')) && (dotPos <= doc.getLength() - 1)) {
            String s = doc.getText(dotPos, 1);
            if (String.valueOf(ch).equals(s)) {
                doc.remove(dotPos, 1);
                return true;
            }
        }
        return false;
    }

    @Override
    public int beforeBreak(Document document, int offset, JTextComponent target) throws BadLocationException {

        Caret caret = target.getCaret();
        BaseDocument doc = (BaseDocument) document;

        // Very simple algorithm for now..
        // Basically, use the same indent as the current line, unless the caret is immediately preceeded by a ":" (possibly with whitespace
        // in between)

        int lineBegin = LineDocumentUtils.getLineStart(doc, offset);
        int lineEnd = LineDocumentUtils.getLineEnd(doc, offset);

        if (lineBegin == offset && lineEnd == offset) {
            // Pressed return on a blank newline - do nothing
            return -1;
        }

        int indent = getLineIndent(doc, offset);
        String linePrefix = doc.getText(lineBegin, offset - lineBegin);
        String lineSuffix = doc.getText(offset, lineEnd + 1 - offset);
        if (linePrefix.trim().endsWith(":") && lineSuffix.trim().length() == 0) {
            // Yes, new key: increase indent
            indent += IndentUtils.getIndentSize(doc);
        } else {
            // No, just use same indent as parent
        }

        // Also remove the whitespace from the caret up to the first nonspace character on the current line
        int remove = 0;
        String line = doc.getText(lineBegin, lineEnd + 1 - lineBegin);
        for (int n = line.length(), i = offset - lineBegin; i < n; i++) {
            char c = line.charAt(i);
            if (c == ' ' || c == '\t') {
                remove++;
            } else {
                break;
            }
        }
        if (remove > 0) {
            doc.remove(offset, remove);
        }
        String str = IndentUtils.getIndentString(indent);
        int newPos = offset + str.length();
        doc.insertString(offset, str, null);
        caret.setDot(offset);
        return newPos + 1;
    }

    @Override
    public OffsetRange findMatching(Document doc, int caretOffset) {
        return OffsetRange.NONE;
    }

    @Override
    public List<OffsetRange> findLogicalRanges(ParserResult info, int caretOffset) {
        YamlParserResult result = (YamlParserResult) info;
        if (result == null) {
            return Collections.emptyList();
        }

        List<? extends StructureItem> items = result.getItems();
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        List<OffsetRange> ranges = new ArrayList<>();
        for (StructureItem item : items) {
            addRanges(ranges, caretOffset, item);
        }

        Collections.reverse(ranges);
        Document doc = info.getSnapshot().getSource().getDocument(false);
        if (doc != null) {
            ranges.add(new OffsetRange(0, doc.getLength()));
        }

        return ranges;
    }

    private void addRanges(List<OffsetRange> ranges, int caretOffset, StructureItem item) {
        int start = (int) item.getPosition();
        int end = (int) item.getEndPosition();
        if (caretOffset >= start && caretOffset <= end) {
            ranges.add(new OffsetRange(start, end));

            for (StructureItem child : item.getNestedItems()) {
                addRanges(ranges, caretOffset, child);
            }
        }
    }

    @Override
    public int getNextWordOffset(Document doc, int caretOffset, boolean reverse) {
        return -1;
    }

    public static int getLineIndent(BaseDocument doc, int offset) {
        try {
            int start = LineDocumentUtils.getLineStart(doc, offset);
            int end;

            if (LineDocumentUtils.isLineWhitespace(doc, start)) {
                end = LineDocumentUtils.getLineEnd(doc, offset);
            } else {
                end = LineDocumentUtils.getLineFirstNonWhitespace(doc, start);
            }

            int indent = Utilities.getVisualColumn(doc, end);

            return indent;
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);

            return 0;
        }
    }

}
