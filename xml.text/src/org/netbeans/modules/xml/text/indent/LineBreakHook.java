/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.xml.text.indent;

import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
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
        int lineStartPos = Utilities.getRowStart(doc, insertPos);

        TokenHierarchy h = TokenHierarchy.get(doc);
        TokenSequence seq = h.tokenSequence();
        // check the actual tokens
        seq.move(context.getCaretOffset());
        int openOffset = followsOpeningTag(seq);
        
        int nonWhiteBefore = Utilities.getFirstNonWhiteBwd(doc, insertPos, lineStartPos);

        int lineEndPos = Utilities.getRowEnd(doc, caretPos);
        int nonWhiteAfter = Utilities.getFirstNonWhiteFwd(doc, caretPos, lineEndPos);

        // there is a opening tag preceding on the line && something following the insertion point
        if (nonWhiteBefore != -1 && nonWhiteAfter != -1 && openOffset >= 0) {
            // check that the following token (after whitespace(s)) is a 
            // opening tag
            seq.move(nonWhiteAfter);
            // now we need to position the caret at the END of the line immediately 
            // preceding the closing tag. Assuming it's already indented
            if (precedesClosingTag(seq)) {
                int startClosingLine = Utilities.getRowStart(doc, nonWhiteAfter);
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
            desiredIndent = IndentUtils.lineIndent(doc, Utilities.getRowStart(doc, Math.abs(openOffset)));
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
