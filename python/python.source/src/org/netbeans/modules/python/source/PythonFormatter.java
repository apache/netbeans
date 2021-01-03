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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.python.source.lexer.PythonLexerUtils;
import org.netbeans.modules.python.source.lexer.PythonTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.Context;
import org.openide.util.Exceptions;

/**
 * Implement formatting for Python. Since there are no {}'s etc. to uniquely
 * impose indentation on Python, this formatter really just tries to enforce
 * spaces-versus-tabs, and indentation width. E.g. it uses the existing indentation
 * to determine whether the next line should be idented more, same or less as the
 * current line and then enforces the current space and indent size settings.
 *
 * @todo Implement pretty printing: inserting newlines, removing spaces inside
 *  parentheses, etc. See the recommendations in
 *   http://www.python.org/dev/peps/pep-0008/
 *  Do import statement cleanup too.
 * @todo Line up comment lines (# as a suffix, continued from a previous line)
 * @todo Handle continuation lines with extra indentation
 * @todo Line up list initializations better?
 * 
 */
public class PythonFormatter implements Formatter {
    private int indentSize;
    private int continuationIndentSize;
    private CodeStyle codeStyle;

    public PythonFormatter() {
    }

    public PythonFormatter(CodeStyle codeStyle) {
        this.codeStyle = codeStyle;
    }

    @Override
    public void reformat(Context context, ParserResult compilationInfo) {

        // No AST pretty printing yet
        // I should offer to go and do space insert/removal around commas, parentheses, etc.
        //  as well as balancing long argument lists across lines
        Document document = context.document();
        int startOffset = context.startOffset();
        int endOffset = context.endOffset();

        reformat(context, document, startOffset, endOffset, (PythonParserResult) compilationInfo);
    }

    public void reformat(final Context context, Document document, int startOffset, int endOffset, PythonParserResult info) {
        if (codeStyle == null) {
            codeStyle = CodeStyle.getDefault(context.document());
        }
        if (info != null && codeStyle != null && codeStyle.formatImports() && !GsfUtilities.isCodeTemplateEditing(document) &&
                PythonAstUtils.getParseResult(info) != null) {
            new ImportManager(info, (BaseDocument)document, codeStyle).cleanup(null, startOffset, endOffset, false);
        }

        if (codeStyle != null) {
            cleanup(document, info, startOffset, endOffset);
        }

        reindent(context, document, startOffset, endOffset);
    }

    @Override
    public boolean needsParserResult() {
//        if (SourceUtils.isScanInProgress()) {
//            return false;
//        }

        // If we're going to format imports, then yes, we need the parser result
        JTextComponent target = EditorRegistry.lastFocusedComponent();
        if (target != null) {
            CodeStyle cs = CodeStyle.getDefault(target.getDocument());
            return cs != null ? cs.formatImports() : false;
        }
        return false;
    }

    @Override
    public int indentSize() {
        // 4 spaces: See http://www.python.org/dev/peps/pep-0008/
        return 4;
    }

    @Override
    public int hangingIndentSize() {
        return 4;
    }

    // Challenge: Two inconsistently formatted
    // Idea: Given a list of offsets and indentation, produce a graph (or recurse) where I mark all
    // siblings the exact same level

    // Algorithm:
    //   Find smallest indent: That's the top level
    //   Build a graph? Each indent line.
    //
    @Override
    public void reindent(final Context context) {
        Document document = context.document();
        int startOffset = context.startOffset();
        int endOffset = context.endOffset();

        reindent(context, document, startOffset, endOffset);
    }

    @SuppressWarnings("deprecation") // For doc.getFormatter()
    public void reindent(final Context context, Document document, int startOffset, int endOffset) {
        endOffset = Math.min(endOffset, document.getLength());
        startOffset = Math.min(startOffset, endOffset);

        continuationIndentSize = indentSize = IndentUtils.indentLevelSize(document);


        final BaseDocument doc = (BaseDocument)document;
        try {
            // Plan: Go through the lines, one by one, and compute the indentation levels relative to each other,
            // then normalize them (except inside strings), then apply!!
            // Also track whether we are used for newline indentation and if so, do smart bracket stuff

            // Current indentation for the given line. -1 means that it should be left alone (e.g.
            // we don't mess with multiline string literals.
            final List<Integer> offsets = new ArrayList<>();

            // Current indentation for the given line. -1 means that it should be left alone (e.g.
            // we don't mess with multiline string literals. Other negative numbers are offsets
            // pointing at a particular left parenthesis that this line should be aligned with
            final List<Integer> indentation = new ArrayList<>();
            final List<Integer> lParenOffsets = new ArrayList<>();

            try {
                doc.readLock(); // For token hierarchy usage

                TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPythonSequence(doc, startOffset);

                int currentOffset = Utilities.getRowStart(doc, startOffset);
                int balance = 0;
                while (currentOffset <= endOffset) {
                    if (!(Utilities.isRowEmpty(doc, currentOffset) || Utilities.isRowWhite(doc, currentOffset))) {
                        Token<? extends PythonTokenId> token = PythonLexerUtils.getToken(doc, currentOffset);
                        int indent = GsfUtilities.getLineIndent(doc, currentOffset);
                        if (token != null) {
                            if (token.id() == PythonTokenId.STRING_LITERAL || token.id() == PythonTokenId.STRING_END) {
                                indent = -1;
                            }
                        }

                        if (indent != -1) {
                            if (balance <= 0) {
                                indentation.add(indent);
                                offsets.add(currentOffset);
                            } else {
                                assert balance <= lParenOffsets.size();
                                int parenOffset = lParenOffsets.get(lParenOffsets.size()-balance);
                                indentation.add(-parenOffset);
                                offsets.add(currentOffset);
                            }
                        }
                    }

                    // TODO - look up the tokens to make sure we don't have a problem with literal nodes

                    if (currentOffset > doc.getLength()) {
                        break;
                    }

                    // Update the line balance
                    int begin = Utilities.getRowStart(doc, currentOffset);
                    int end = Utilities.getRowEnd(doc, currentOffset);

                    ts.move(begin);

                    if (ts.moveNext()) {
                        do {
                            Token<? extends PythonTokenId> token = ts.token();
                            TokenId id = token.id();

                            if (id == PythonTokenId.LPAREN) {
                                balance++;
                                lParenOffsets.add(ts.offset());
                            } else if (id == PythonTokenId.RPAREN) {
                                balance--;
                                if (!lParenOffsets.isEmpty()) {
                                    lParenOffsets.remove(lParenOffsets.size()-1);
                                }
                            }
                        } while (ts.moveNext() && (ts.offset() <= end));
                    }

                    currentOffset = Utilities.getRowEnd(doc, currentOffset) + 1;
                }
            } finally {
                doc.readUnlock();
            }

            // Nothing to do
            if (offsets.size() == 0) {
                return;
            }

            assert indentation.size() == offsets.size();

            final Map<Integer, Integer> offsetToLevel = new HashMap<>();
            final Map<Integer,Integer> offsetToIndex = new HashMap<>();
            List<Integer> parentIndentations = new ArrayList<>();
            int currentParentIndent = -1;
            int currentLevel = -1;

            int firstIndent = indentation.get(0);
            List<Integer> sorted = new ArrayList<>(indentation);
            Collections.sort(sorted);
            // Attempt to shift the computed indentation to fit the right indentation levels
            // that are currently in the file?
            int firstNonNeg = 0;
            for (; firstNonNeg < sorted.size(); firstNonNeg++) {
                if (sorted.get(firstNonNeg) >= 0) {
                    break;
                }
            }
            boolean shiftToCurrent = true;
            if (firstIndent > sorted.get(firstNonNeg)) {
                shiftToCurrent = false;
                // The start is not at the top level... e.g. we have something like
                //      foo
                //   else
                //      bar
                // (e.g. we are formatting a fragment of code which doesn't include
                // the top). Here we need to find the "true" top levels, so we
                // push levels on to the stack
                int prev = -1;
                for (int indent : sorted) {
                    if (prev == indent) {
                        continue;
                    }
                    prev = indent;
                    if (indent < firstIndent) {
                        parentIndentations.add(currentParentIndent);
                        currentParentIndent = indent;
                        currentLevel++;
                    } else {
                        break;
                    }
                }
            }


            // TODO: What if I start in the middle of an expression such that I outdent
            // more than I indent? I have to build up the index levels if necessary
            // Go count popping levels

            for (int i = 0, n = offsets.size(); i < n; i++) {
                int offset = offsets.get(i);
                int indent = indentation.get(i);
                if (indent == -1) {
                    // Leave line alone
                    offsetToLevel.put(offset, -1);
                    continue;
                }
                offsetToIndex.put(offset, i);

                if (indent < 0) {
                    // Want to keep everything the same as the prev, plus delta
                } else if (indent > currentParentIndent) {
                    // New level
                    currentLevel++;
                    parentIndentations.add(currentParentIndent);
                    currentParentIndent = indent;
                } else if (indent < currentParentIndent) {
                    while (currentParentIndent > indent) {
                        currentLevel--;
                        if (parentIndentations.size() > 0) {
                            currentParentIndent = parentIndentations.remove(parentIndentations.size() - 1);
                        } else {
                            currentParentIndent = indent;
                        }
                    }
                }

                offsetToLevel.put(offset, currentLevel);
            }

            // Compute relative shift
            int firstLineIndent = indentation.get(0);
            int firstLineLevel = offsetToLevel.get(offsets.get(0));
            int computedIndent = firstLineLevel * indentSize;
            final int relativeShift = shiftToCurrent ? computedIndent - firstLineIndent : 0;

            doc.runAtomic(new Runnable() {
                @Override
                public void run() {
                    int[] computedIndents = new int[offsets.size()];
                    // Process backwards so I don't have to worry about updating offsets affected by
                    // indentation changes
                    for (int i = offsets.size() - 1; i >= 0; i--) {
                        int indent = indentation.get(i);
                        if (indent == -1) {
                            // Leave line alone
                            continue;
                        }
                        if (indent >= 0) {
                            int offset = offsets.get(i);
                            int level = offsetToLevel.get(offset);
                            int computedIndent = level * indentSize - relativeShift;
                            if (computedIndent < 0) {
                                computedIndent = 0;
                            }
                            computedIndents[i] =computedIndent;
                        } else {
                            computedIndents[i] = -1;
                        }
                    }

                    for (int i = offsets.size() - 1; i >= 0; i--) {
                        int indent = indentation.get(i);
                        if (indent < -1) {
                            try {
                                // Negative offset pointing to a left parenthesis we should align with
                                int parenOffset = -indent;
                                int lineStart = Utilities.getRowStart(doc, parenOffset);
                                if (lineStart != -1) {
                                    int parenLineIndent = computedIndents[offsetToIndex.get(lineStart)];
                                    assert parenLineIndent >= 0;
                                    int textBegin = Utilities.getRowFirstNonWhite(doc, lineStart);
                                    assert textBegin != -1;
                                    // Indent to new indentation + text up to paren plus the paren itself
                                    int newIndent = parenLineIndent + (parenOffset-textBegin) + 1;
                                    computedIndents[i] = newIndent;
                                }
                            } catch (BadLocationException ble) {
                                Exceptions.printStackTrace(ble);
                            }
                        }
                    }

                    // Process backwards so I don't have to worry about updating offsets affected by
                    // indentation changes
                    for (int i = offsets.size() - 1; i >= 0; i--) {
                        int indent = indentation.get(i);
                        if (indent == -1) {
                            // Leave line alone
                            continue;
                        }
                        int offset = offsets.get(i);
                        int computedIndent = computedIndents[i];
                        if (computedIndent < 0) {
                            computedIndent = 0;
                        }

                        if (computedIndent != indent && context != null) {
                            try {
                                context.modifyIndent(offset, computedIndent);
                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }
            });
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
    }

    private boolean isLinePrefix(BaseDocument doc, int offset) throws BadLocationException {
        return Utilities.getRowFirstNonWhite(doc, offset) == offset;
    }

    private void cleanup(Document document, PythonParserResult info, int startOffset, int endOffset) {
        BaseDocument doc = (BaseDocument)document;
        final EditList edits = new EditList(doc);
        try {
            doc.readLock(); // For token hierarchy usage

            TokenSequence<? extends PythonTokenId> ts = PythonLexerUtils.getPythonSequence(doc, startOffset);
            if (ts == null) {
                return;
            }

            ts.move(startOffset);



            // TODO:
            // Control whether I collapse spaces to a single space, or just ensure there is at least one
            // "None", "1", "At least 1", "Leave Alone"

            // TODO: Insert and remove needed or unnecessary parentheses!
            // TODO: Alignment! Especially of trailing line comments on adjacent lines!
            // TODO: Collapse blank newlines!
            boolean addSpaceAroundOperators = true;
            boolean removeSpaceInsideParens = true; // also applies to braces and brackets
            boolean addSpaceAfterComma = true;
            //    boolean spaceArondParens = false;
            //    boolean spaceBeforeArgs = false; // before parentheses in a call
            boolean removeSpaceBeforeSep = true; // before comma, semicolon or colon
            //    boolean alignAssignments = false; // Only one space around assignments
            boolean removeSpaceInParamAssign = true; // Around assignment in parameter list, e.g.
            boolean collapseSpaces = true;
            //def complex(real, imag=0.0):
            //       return magic(r=real, i=imag)
            if (codeStyle != null) {
                addSpaceAroundOperators = codeStyle.addSpaceAroundOperators();
                removeSpaceInsideParens = codeStyle.removeSpaceInsideParens();
                addSpaceAfterComma = codeStyle.addSpaceAfterComma();
                removeSpaceBeforeSep = codeStyle.removeSpaceBeforeSep();
                removeSpaceInParamAssign = codeStyle.removeSpaceInParamAssign();
                collapseSpaces = codeStyle.collapseSpaces();
            }

            // TODO - back up to the nearest function or class or beginning of the document to get the right
            // parenthesis balance.
            int parenBalance = 0;

            Token<? extends PythonTokenId> prev = null;
            Token<? extends PythonTokenId> token = null;
            Token<? extends PythonTokenId> next = null;
            int tokenOffset = 0;
            int nextOffset = 0;
            int prevOffset = -1;
            if (ts.moveNext()) {
                token = ts.token();
                tokenOffset = ts.offset();
                if (ts.moveNext()) {
                    next = ts.token();
                    nextOffset = ts.offset();
                } else {
                    return;
                }
            }
            boolean prevRemoved = false;
            boolean tokenRemoved = false;
            boolean nextRemoved = false;
            while (token != null) {
                TokenId prevId = prev != null ? prev.id() : null;
                TokenId id = token.id();
                TokenId nextId = next != null ? next.id() : null;

                if (id == PythonTokenId.LPAREN) {
                    parenBalance++;
                } else if (id == PythonTokenId.RPAREN) {
                    parenBalance--;
                }

                if (removeSpaceInsideParens) {
                    if (id == PythonTokenId.LPAREN) {
                        if (nextId == PythonTokenId.WHITESPACE && !nextRemoved) {
                            edits.replace(nextOffset, next.length(), null, false, 0);
                            nextRemoved = true;
                        }
                    } else if (id == PythonTokenId.RPAREN) {
                        if (prevId == PythonTokenId.WHITESPACE && !prevRemoved && !isLinePrefix(doc, tokenOffset)) {
                            // I don't remove space in front of paren's at the beginning of the line; these might have
                            // been aligned with indented content above
                            edits.replace(prevOffset, prev.length(), null, false, 0);
                            prevRemoved = true;
                        }
                    } else if (id == PythonTokenId.LBRACKET) {
                        if (nextId == PythonTokenId.WHITESPACE && !nextRemoved) {
                            edits.replace(nextOffset, next.length(), null, false, 0);
                            nextRemoved = true;
                        }
                    } else if (id == PythonTokenId.RBRACKET) {
                        if (prevId == PythonTokenId.WHITESPACE && !prevRemoved && !isLinePrefix(doc, tokenOffset)) {
                            edits.replace(prevOffset, prev.length(), null, false, 0);
                            prevRemoved = true;
                        }
                    } else if (id == PythonTokenId.LBRACE) {
                        if (nextId == PythonTokenId.WHITESPACE && !nextRemoved) {
                            edits.replace(nextOffset, next.length(), null, false, 0);
                            nextRemoved = true;
                        }
                    } else if (id == PythonTokenId.RBRACE) {
                        if (prevId == PythonTokenId.WHITESPACE && !prevRemoved && !isLinePrefix(doc, tokenOffset)) {
                            edits.replace(prevOffset, prev.length(), null, false, 0);
                            prevRemoved = true;
                        }
                    }
                }

                if (addSpaceAfterComma) {
                    if (id == PythonTokenId.COMMA) {
                        if (collapseSpaces && nextId == PythonTokenId.WHITESPACE && next.length() > 1) {
                            edits.replace(nextOffset, next.length() - 1, null, false, 1); // NOI18N
                        } else if (next == null ||
                                (nextId != PythonTokenId.WHITESPACE && nextId != PythonTokenId.NEWLINE)) {
                            edits.replace(nextOffset, 0, " ", false, 1); // NOI18N
                        }
                    }
                }

                if (removeSpaceBeforeSep &&
                        (id == PythonTokenId.COMMA || id == PythonTokenId.COLON ||
                        (id == PythonTokenId.ANY_OPERATOR && TokenUtilities.equals(token.text(), ";"))) && // NOI18N
                        prevId == PythonTokenId.WHITESPACE && !prevRemoved && !isLinePrefix(doc, tokenOffset)) {
                    edits.replace(prevOffset, prev.length(), null, false, 2);
                    prevRemoved = true;
                }

                if (addSpaceAroundOperators && id == PythonTokenId.ANY_OPERATOR) {
                    CharSequence seq = token.text();

                    // These aren't binary, and ; isn't really an operator and has its own setting
                    if (!(TokenUtilities.equals(seq, "@") || // NOI18N
                            TokenUtilities.equals(seq, "`") || // NOI18N
                            TokenUtilities.equals(seq, ";"))) { // NOI18N

                        boolean insertSpace = true;
                        if (removeSpaceInParamAssign && TokenUtilities.equals(seq, "=")) { // NOI18N
                            // Special handling: keyword arguments should typically NOT
                            // have space inserted
                            if (parenBalance > 0) {
                                insertSpace = false;
                                // Remove spaces around the =
                                if (prevId == PythonTokenId.WHITESPACE && !prevRemoved) {
                                    edits.replace(prevOffset, prev.length(), null, false, 5); // NOI18N
                                    prevRemoved = true;
                                }
                                if (nextId == PythonTokenId.WHITESPACE && !nextRemoved) {
                                    edits.replace(nextOffset, next.length(), null, false, 6); // NOI18N
                                    nextRemoved = true;
                                }
                            }
                        }

                        if (insertSpace && TokenUtilities.equals(seq, "-")/* && (nextId == PythonTokenId.FLOAT_LITERAL || nextId == PythonTokenId.INT_LITERAL)*/) {
                            // Leave -'s alone for now. The code is a little unclear on the difference between
                            //  x-1 and =-1 etc. For numbers (floating and integer) the minus isn't part of the lexical token for the number;
                            // it's a separate operator. However, it's tricky to tell this apart from the binary subtraction, since it depends
                            // on what came before. For now play it safe an leave these alone.
                            // TODO - implement this properly.
                            insertSpace = false;
                        }

                        if (insertSpace && TokenUtilities.equals(seq, "*")) { // NOI18N
                            // "*" in (*foo) doesn't mean multiplication; it's not a binary operator here,
                            // it's many args.
                            if (prevId == PythonTokenId.COMMA || prevId == PythonTokenId.LPAREN) {
                                insertSpace = false;
                            }
                        }

                        if (insertSpace) {
                            // Ensure that we have space on both sides
                            if (collapseSpaces && prevId == PythonTokenId.WHITESPACE && next.length() > 1 &&
                                    !isLinePrefix(doc, tokenOffset)) {
                                edits.replace(prevOffset, prev.length() - 1, null, false, 1); // NOI18N
                            } else if (prevId != PythonTokenId.WHITESPACE) {
                                edits.replace(tokenOffset, 0, " ", false, 3); // NOI18N
                            }

                            if (collapseSpaces && nextId == PythonTokenId.WHITESPACE && next.length() > 1) {
                                edits.replace(nextOffset, next.length() - 1, null, false, 1); // NOI18N
                            } else if (nextId != PythonTokenId.WHITESPACE && nextId != PythonTokenId.NEWLINE) {
                                edits.replace(nextOffset, 0, " ", false, 4); // NOI18N
                            }
                        }
                    }
                }

                if (tokenOffset + token.length() >= endOffset) {
                    break;
                }

                prevRemoved = tokenRemoved;
                tokenRemoved = nextRemoved;
                nextRemoved = false;

                prev = token;
                token = next;
                prevOffset = tokenOffset;
                tokenOffset = nextOffset;
                if (ts.moveNext()) {
                    next = ts.token();
                    nextOffset = ts.offset();
                } else {
                    next = null;
                }
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        } finally {
            doc.readUnlock();
        }

        doc.runAtomic(new Runnable() {
            @Override
            public void run() {
                edits.apply();
            }
        });
    }
}
