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
package org.netbeans.modules.groovy.editor.language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.codehaus.groovy.ast.ASTNode;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
import org.openide.util.Exceptions;

/** 
 * 
 * @author Tor Norbye
 * @author Martin Adamek
 */
public class GroovyBracketCompleter implements KeystrokeHandler {

    @Override
    public boolean beforeCharInserted(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    @Override
    public boolean afterCharInserted(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    @Override
    public boolean charBackspaced(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    @Override
    public int beforeBreak(Document doc, int caretOffset, JTextComponent target) throws BadLocationException {
        return -1;
    }

    @Override
    public OffsetRange findMatching(Document doc, int caretOffset) {
        return OffsetRange.NONE;
    }

    @Override
    public List<OffsetRange> findLogicalRanges(ParserResult info, int caretOffset) {
        ASTNode root = ASTUtils.getRoot(info);

        if (root == null) {
            return Collections.emptyList();
        }

        GroovyParserResult gpr = ASTUtils.getParseResult(info);

        int astOffset = ASTUtils.getAstOffset(info, caretOffset);
        if (astOffset == -1) {
            return Collections.emptyList();
        }

        List<OffsetRange> ranges = new ArrayList<OffsetRange>();
        
        /** Furthest we can go back in the buffer (in RHTML documents, this
         * may be limited to the surrounding &lt;% starting tag
         */
        int min = 0;
        int max = Integer.MAX_VALUE;
        int length;

        // Check if the caret is within a comment, and if so insert a new
        // leaf "node" which contains the comment line and then comment block
        try {
            BaseDocument doc = LexUtilities.getDocument(gpr, false);
            if (doc == null) {
                return ranges;
            }
            AstPath path = new AstPath(root, astOffset, doc);
            length = doc.getLength();

            TokenSequence<GroovyTokenId> ts = LexUtilities.getPositionedSequence(doc, caretOffset);
            if (ts != null) {
                Token<GroovyTokenId> token = ts.token();

                if (token != null && token.id() == GroovyTokenId.BLOCK_COMMENT) {
                    // First add a range for the current line
                    int begin = ts.offset();
                    int end = begin+token.length();
                    ranges.add(new OffsetRange(begin, end));
                } else if ((token != null) && (token.id() == GroovyTokenId.LINE_COMMENT)) {
                    // First add a range for the current line
                    int begin = LineDocumentUtils.getLineStartOffset(doc, caretOffset);
                    int end = LineDocumentUtils.getLineEndOffset(doc, caretOffset);

                    if (LexUtilities.isCommentOnlyLine(doc, caretOffset)) {
                        ranges.add(new OffsetRange(Utilities.getRowFirstNonWhite(doc, begin), 
                                Utilities.getRowLastNonWhite(doc, end)+1));

                        int lineBegin = begin;
                        int lineEnd = end;

                        while (begin > 0) {
                            int newBegin = LineDocumentUtils.getLineStartOffset(doc, begin - 1);

                            if ((newBegin < 0) || !LexUtilities.isCommentOnlyLine(doc, newBegin)) {
                                begin = Utilities.getRowFirstNonWhite(doc, begin);
                                break;
                            }

                            begin = newBegin;
                        }

                        while (true) {
                            int newEnd = LineDocumentUtils.getLineEndOffset(doc, end + 1);

                            if ((newEnd >= length) || !LexUtilities.isCommentOnlyLine(doc, newEnd)) {
                                end = Utilities.getRowLastNonWhite(doc, end)+1;
                                break;
                            }

                            end = newEnd;
                        }

                        if ((lineBegin > begin) || (lineEnd < end)) {
                            ranges.add(new OffsetRange(begin, end));
                        }
                    } else {
                        // It's just a line comment next to some code; select the comment
                        TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
                        int offset = token.offset(th);
                        ranges.add(new OffsetRange(offset, offset + token.length()));
                    }
                }
            }
            Iterator<ASTNode> it = path.leafToRoot();

            OffsetRange previous = OffsetRange.NONE;
            while (it.hasNext()) {
                ASTNode node = it.next();

                OffsetRange range = ASTUtils.getRange(node, doc);

                // The contains check should be unnecessary, but I end up getting
                // some weird positions for some Rhino AST nodes
                if (range.containsInclusive(astOffset) && !range.equals(previous)) {
                    range = LexUtilities.getLexerOffsets(gpr, range);
                    if (range != OffsetRange.NONE) {
                        if (range.getStart() < min) {
                            ranges.add(new OffsetRange(min, max));
                            ranges.add(new OffsetRange(0, length));
                            break;
                        }
                        ranges.add(range);
                        previous = range;
                    }
                }
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
            return ranges;
        }


        return ranges;
    }

    @Override
    public int getNextWordOffset(Document document, int offset, boolean reverse) {
        BaseDocument doc = (BaseDocument)document;
        TokenSequence<GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, offset);
        if (ts == null) {
            return -1;
        }
        ts.move(offset);
        if (!ts.moveNext() && !ts.movePrevious()) {
            return -1;
        }
        if (reverse && ts.offset() == offset) {
            if (!ts.movePrevious()) {
                return -1;
            }
        }

        Token<GroovyTokenId> token = ts.token();
        TokenId id = token.id();

        if (id == GroovyTokenId.WHITESPACE) {
            // Just eat up the space in the normal IDE way
            if ((reverse && ts.offset() < offset) || (!reverse && ts.offset() > offset)) {
                return ts.offset();
            }
            while (id == GroovyTokenId.WHITESPACE) {
                if (reverse && !ts.movePrevious()) {
                    return -1;
                } else if (!reverse && !ts.moveNext()) {
                    return -1;
                }

                token = ts.token();
                id = token.id();
            }
            if (reverse) {
                int start = ts.offset()+token.length();
                if (start < offset) {
                    return start;
                }
            } else {
                int start = ts.offset();
                if (start > offset) {
                    return start;
                }
            }
            
        }

        if (id == GroovyTokenId.IDENTIFIER) {
            String s = token.text().toString();
            int length = s.length();
            int wordOffset = offset-ts.offset();
            if (reverse) {
                // Find previous
                int offsetInImage = offset - 1 - ts.offset(); 
                if (offsetInImage < 0) {
                    return -1;
                }
                if (offsetInImage < length && Character.isUpperCase(s.charAt(offsetInImage))) {
                    for (int i = offsetInImage - 1; i >= 0; i--) {
                        char charAtI = s.charAt(i);
                        if (charAtI == '_') {
                            // return offset of previous uppercase char in the identifier
                            return ts.offset() + i + 1;
                        } else if (!Character.isUpperCase(charAtI)) {
                            // return offset of previous uppercase char in the identifier
                            return ts.offset() + i + 1;
                        }
                    }
                    return ts.offset();
                } else {
                    for (int i = offsetInImage - 1; i >= 0; i--) {
                        char charAtI = s.charAt(i);
                        if (charAtI == '_') {
                            return ts.offset() + i + 1;
                        }
                        if (Character.isUpperCase(charAtI)) {
                            // now skip over previous uppercase chars in the identifier
                            for (int j = i; j >= 0; j--) {
                                char charAtJ = s.charAt(j);
                                if (charAtJ == '_') {
                                    return ts.offset() + j+1;
                                }
                                if (!Character.isUpperCase(charAtJ)) {
                                    // return offset of previous uppercase char in the identifier
                                    return ts.offset() + j + 1;
                                }
                            }
                            return ts.offset();
                        }
                    }
                    
                    return ts.offset();
                }
            } else {
                // Find next
                int start = wordOffset+1;
                if (wordOffset < 0 || wordOffset >= s.length()) {
                    // Probably the end of a token sequence, such as this:
                    // <%s|%>
                    return -1;
                }
                if (Character.isUpperCase(s.charAt(wordOffset))) { 
                    // if starting from a Uppercase char, first skip over follwing upper case chars
                    for (int i = start; i < length; i++) {
                        char charAtI = s.charAt(i);
                        if (!Character.isUpperCase(charAtI)) {
                            break;
                        }
                        if (s.charAt(i) == '_') {
                            return ts.offset()+i;
                        }
                        start++;
                    }
                }
                for (int i = start; i < length; i++) {
                    char charAtI = s.charAt(i);
                    if (charAtI == '_' || Character.isUpperCase(charAtI)) {
                        return ts.offset()+i;
                    }
                }
            }
        }
        
        // Default handling in the IDE
        return -1;
    }
}
