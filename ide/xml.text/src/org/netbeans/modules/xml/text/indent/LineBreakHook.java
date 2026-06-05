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
package org.netbeans.modules.xml.text.indent;

import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor;

/**
 * This Typing Hook works in conjuction with the XML reformatting, which happens
 * when a line break is inserted. If the formatter inserts an empty line, this Hook
 * positions the caret at the end of the line immediately following the opening tag
 * assuming the line is properly indented already.
 * <p/>
 * The code tries to avoid repositioning in all other situations.
 * 
 * 
 * @author sdedic
 */
public class LineBreakHook implements TypedBreakInterceptor {
    private static final Logger LOG = Logger.getLogger(LineBreakHook.class.getName());
    
    @Override
    public void afterInsert(final Context context) throws BadLocationException {
    }
    
    private boolean precedesClosingTag(TokenSequence seq) {
        if (!seq.moveNext()) {
            return false;
        }
        // all whitespace should have been skipped by now
        Token tukac = seq.token();
        if (tukac.id() != XMLTokenId.TAG) {
            return false;
        }
        String text = tukac.text().toString();
        return text.startsWith("</");
    }
    
    /**
     * Determines whether the token sequence immediately follows an opening tag
     * (possibly with some whitespace in between the token sequence and the
     * opening tag ending > sign.
     * <p/>
     * If the preceding tag is self-closing, the returned offset will be negative offset
     * just after the 1st tag character (the opening &lt;)
     * 
     * @param seq positioned sequence
     * @return index just at the opening &lt; sign, or Integer.MIN_VALUE if opening tag is not found
     */
    private int followsOpeningTag(TokenSequence seq) {
        int closingIndex = -1;
        boolean selfClose = false;
        while (seq.movePrevious()) {
            Token tukac = seq.token();
            switch ((XMLTokenId)tukac.id()) {
                case ARGUMENT:
                case OPERATOR:
                case VALUE:
                    if (closingIndex == -1) {
                        // in the middle of a tag
                        return Integer.MIN_VALUE;
                    }
                    
                case WS:
                    continue;

                case TAG: {
                    String text = tukac.text().toString();
                    // it may be the closing tag
                    if (text.endsWith(">")) {
                        if (closingIndex > -1) {
                            return Integer.MIN_VALUE;
                        }
                        closingIndex = seq.offset() + tukac.length();
                        if (text.endsWith("/>")) {
                            selfClose = true;
                        }
                        break;
                    }
                    if (text.startsWith("<") && text.length() > 1 && text.charAt(1) != '/') {
                        // found start tag
                        if (selfClose) {
                            // note: this indicate position just after the opening <
                            return -seq.offset() - 1;
                        } else {
                            return seq.offset();
                        }
                    } 
                    return Integer.MIN_VALUE;
                }
                    
                default:
                    return Integer.MIN_VALUE;
            }
        }
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean beforeInsert(Context context) throws BadLocationException {
        // no op
        return false;
    }

    @Override
    public void cancelled(Context context) {
        // no op
    }

    @Override
    public void insert(MutableContext context) throws BadLocationException {
        if (!(context.getDocument() instanceof BaseDocument)) {
            return;
        }
        BaseDocument doc = (BaseDocument)context.getDocument();

        int insertPos = context.getCaretOffset();
        int caretPos = context.getComponent().getCaretPosition();
        int lineStartPos = LineDocumentUtils.getLineStartOffset(doc, insertPos);

        TokenHierarchy h = TokenHierarchy.get(doc);
        TokenSequence seq = h.tokenSequence();
        // check the actual tokens
        seq.move(context.getCaretOffset());
        int openOffset = followsOpeningTag(seq);
        
        int nonWhiteBefore = LineDocumentUtils.getPreviousNonWhitespace(doc, insertPos, lineStartPos);

        int lineEndPos = LineDocumentUtils.getLineEndOffset(doc, caretPos);
        int nonWhiteAfter = LineDocumentUtils.getNextNonWhitespace(doc, caretPos, lineEndPos);

        // there is a opening tag preceding on the line && something following the insertion point
        if (nonWhiteBefore != -1 && nonWhiteAfter != -1 && openOffset >= 0) {
            // check that the following token (after whitespace(s)) is a 
            // opening tag
            seq.move(nonWhiteAfter);
            // now we need to position the caret at the END of the line immediately 
            // preceding the closing tag. Assuming it's already indented
            if (precedesClosingTag(seq)) {
                int startClosingLine = LineDocumentUtils.getLineStartOffset(doc, nonWhiteAfter);
                int nextLineStart = Utilities.getRowStart(doc, insertPos, 1);
                if (nextLineStart >= startClosingLine - 1) {
                    insertBlankBetweenTabs(context, openOffset);
                }
                return;
            }
        }
        // if the rest of the line is blank, we must insert newline + indent, so the cursor
        // appears at the correct place
        if (nonWhiteAfter != -1) {
            // will be handled by the formatter automatically
            return;
        }

        int desiredIndent;

        if (openOffset != Integer.MIN_VALUE) {
            desiredIndent = IndentUtils.lineIndent(doc, LineDocumentUtils.getLineStartOffset(doc, Math.abs(openOffset)));
            if (openOffset >= 0) {
                desiredIndent += IndentUtils.indentLevelSize(doc);
            }
        } else {
            // align with the current line
            desiredIndent = IndentUtils.lineIndent(doc, lineStartPos);
        }
        String blankLine = "\n" + 
            IndentUtils.createIndentString(doc, desiredIndent);
        context.setText(blankLine, -1, blankLine.length(), 1, blankLine.length());
    }
    
    private void insertBlankBetweenTabs(MutableContext context, int openOffset) throws BadLocationException {
        BaseDocument baseDoc = (BaseDocument)context.getDocument();
        // otherwise inser the newline, followed by a proper indent, followed by another
        // newline :)
        int spacesPerTab = IndentUtils.indentLevelSize(baseDoc);
        int col = Utilities.getVisualColumn(baseDoc, openOffset);
        String blankLine = "\n" + 
                IndentUtils.createIndentString(baseDoc, col + spacesPerTab);
        // must count the actually generated characters - #215134
        int caretOffset = blankLine.length();
        blankLine = blankLine + "\n" + 
                IndentUtils.createIndentString(baseDoc, col);
        context.setText(blankLine, -1, caretOffset, 1, blankLine.length());
    }
    
    @MimeRegistration(mimeType="text/xml", service=TypedBreakInterceptor.Factory.class)
    public static class F implements TypedBreakInterceptor.Factory {

        @Override
        public TypedBreakInterceptor createTypedBreakInterceptor(MimePath mimePath) {
            return new LineBreakHook();
        }
    }
}
