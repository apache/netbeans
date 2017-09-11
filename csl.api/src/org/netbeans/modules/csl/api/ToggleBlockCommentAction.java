/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.csl.api;

import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.spi.CommentHandler;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.openide.util.Exceptions;

/**
 * @deprecated use {@link CslActions#createToggleBlockCommentAction() } instead.
 */
public class ToggleBlockCommentAction extends BaseAction {

    // -J-Dorg.netbeans.modules.csl.api.ToggleBlockCommentAction.level=FINE
    private static final Logger LOG = Logger.getLogger(ToggleBlockCommentAction.class.getName());
    
    static final long serialVersionUID = -1L;

    private final CommentHandler commentHandler;

    /**
     * Creates an action, which toggles block comments according to the supplied
     * <code>CommentHandler</code>.
     *
     * @param commentHandler The <code>CommentHandler</code> to use in this action.
     * 
     * @deprecated Use the no-arg constructir and supply your <code>CommentHandler</code> implementation
     *   from <code>DefaultLanguageConfig.getCommentHandler</code>.
     */
    public ToggleBlockCommentAction(CommentHandler commentHandler) {
        super(ExtKit.toggleCommentAction);
        this.commentHandler = commentHandler;
    }

    /**
     * Creates a general toggle comment action. This action will dynamically determine
     * the language of the document section, where it is invoked and use correct comments.
     *
     * <p class="nonnormative">
     * It uses <code>CommentHandler</code> implementations from <code>DefaultLanguageConfig.getCommentHandler</code>
     * for the section's language. If there is no <code>CommentHandler</code> the action
     * assumes that the language uses line comments and the action will use <code>DefaultLanguageConfig.getLineCommentPrefix</code>.
     *
     * @since 2.2
     */
    public ToggleBlockCommentAction() {
        this(null);
    }

    @Override
    public void actionPerformed(ActionEvent evt, final JTextComponent target) {
        if (target != null) {
            if (!target.isEditable() || !target.isEnabled() || !(target.getDocument() instanceof BaseDocument)) {
                target.getToolkit().beep();
                return;
            }

            BaseDocument doc = (BaseDocument) target.getDocument();
            doc.runAtomic(new Runnable() {
                public @Override void run() {
                    try {
                        int offset = target.getCaretPosition();
                        if (Utilities.isSelectionShowing(target)) {
                            offset = target.getSelectionStart();
                        } else {
                            offset = Utilities.getRowFirstNonWhite((BaseDocument) target.getDocument(), offset);
                            if (offset == -1) {
                                offset = target.getCaretPosition();
                            }
                        }

                        TokenHierarchy<?> th = TokenHierarchy.get(target.getDocument());

                        if (ToggleBlockCommentAction.this.commentHandler == null) {
                            List<TokenSequence<?>> seqs = th.embeddedTokenSequences(offset, false);
                            if (seqs.isEmpty()) {
                                return;
                            }
                            
                            Language lang = null;
                            LanguagePath langPath = null;
                            for(int i = seqs.size() - 1; i >= 0; i--) {
                                TokenSequence<?> ts = seqs.get(i);
                                String mimeType = ts.language().mimeType();
                                lang = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
                                if (lang != null) {
                                    langPath = ts.languagePath();
                                    offset = i < seqs.size() - 1 ? ts.offset() : offset;
                                    if (LOG.isLoggable(Level.FINE)) {
                                        LOG.log(Level.FINE, "offset={0}, lang={1}, mimeType={2}, ts={3}", new Object [] { offset, lang, mimeType, ts }); //NOI18N
                                    }
                                    break;
                                }
                            }
                            if (lang == null) {
                                return;
                            }

                            CommentHandler commentHandler = null;
                            if (lang.getGsfLanguage() instanceof DefaultLanguageConfig) {
                                commentHandler = ((DefaultLanguageConfig) lang.getGsfLanguage()).getCommentHandler();
                            }

                            if (commentHandler != null) {
                                commentUncommentBlock(target, th, commentHandler, true);
                            } else if (lang.getGsfLanguage().getLineCommentPrefix() != null) {
                                commentUncommentLines(target, offset, langPath, th, lang.getGsfLanguage().getLineCommentPrefix());
                            }
                        } else {
                            commentUncommentBlock(target, th, ToggleBlockCommentAction.this.commentHandler, false);
                        }
                    } catch (BadLocationException e) {
                        target.getToolkit().beep();
                    }
                }
            });
        }
    }
    
    private int findCommentStart(BaseDocument doc, CommentHandler handler, int offsetFrom, int offsetTo) throws BadLocationException {
        int from = Utilities.getFirstNonWhiteFwd(doc, offsetFrom, offsetTo);
        if (from == -1) {
            return offsetFrom;
        }
        String startDelim = handler.getCommentStartDelimiter();
        if (CharSequenceUtilities.equals(
            DocumentUtilities.getText(doc).subSequence(
                from, Math.min(offsetTo, from + startDelim.length())),
            startDelim)) {
            return from;
        }
        
        return offsetFrom;
    }
    
    private int findCommentEnd(BaseDocument doc, CommentHandler handler, int offsetFrom, int offsetTo) throws BadLocationException {
        int to = Utilities.getFirstNonWhiteBwd(doc, offsetTo, offsetFrom);
        if (to == -1) {
            return offsetTo;
        }
        String endDelim = handler.getCommentEndDelimiter();
        if (DocumentUtilities.getText(doc).subSequence(
                Math.max(offsetFrom, to - endDelim.length() + 1), to + 1).equals(endDelim)) {
            // after end of the delimiter
            return to + 1;
        }
        
        return offsetFrom;

    }

    private void commentUncommentBlock(JTextComponent target, TokenHierarchy<?> th, final CommentHandler commentHandler, boolean dynamicCH) throws BadLocationException {
        final Caret caret = target.getCaret();
        final BaseDocument doc = (BaseDocument) target.getDocument();

        int from, to;
        if (Utilities.isSelectionShowing(caret)) {
            from = Utilities.getRowStart(doc, target.getSelectionStart());
            to = target.getSelectionEnd();
            if (to > 0 && Utilities.getRowStart(doc, to) == to) {
                to--;
            }
            to = Utilities.getRowEnd(doc, to);
        } else { // selection not visible
            from = Utilities.getRowStart(doc, caret.getDot());
            to = Utilities.getRowEnd(doc, caret.getDot());
        }

        boolean lineSelection = false;
        boolean inComment = isInComment(doc, commentHandler, caret.getDot());
        if (!Utilities.isSelectionShowing(caret)) {
            //no selection
                //check for commenting empty line
            if (Utilities.isRowEmpty(doc, from) || Utilities.isRowWhite(doc, from)) {
                return;
            }
            if (!inComment) {
                //extend the range to the whole line
                from = Utilities.getFirstNonWhiteFwd(doc, Utilities.getRowStart(doc, from));
                to = Utilities.getFirstNonWhiteBwd(doc, Utilities.getRowEnd(doc, to)) + 1;
                lineSelection = true;
            } else {
                // check if the line does not begin with WS+comment start or end with WS+comment end
                from = findCommentStart(doc, commentHandler, from, to);
                to = findCommentEnd(doc, commentHandler, from, to);
            }
                    
        }

        if(!inComment && from == to) {
            return ; //no-op
        }

        // check that we are still using the same handler (ie. we are still in the same language section)
        if (dynamicCH && !sameCommentHandler(th, from, false, commentHandler)) {
            return;
        }

        int[] adjustedRange = commentHandler.getAdjustedBlocks(doc, from, to);
        if(adjustedRange.length == 0) {
            return; //no-op
        }

        from = adjustedRange[0];
        to = adjustedRange[1];

        // check that we are still using the same handler (ie. we are still in the same language section)
        // and that the start language section (from) is the same as the end language section (to)
        if (dynamicCH && (!sameCommentHandler(th, from, false, commentHandler) || !sameCommentHandler(th, to, true, commentHandler))) {
            return;
        }

        if(!inComment && from == to) {
            return ; //no-op
        }

        final int comments[] = commentHandler.getCommentBlocks(doc, from, to);

        assert comments != null;

//            debug(doc, comments, from, to);

        check(comments, from, to);

        final int _from = from;
        final int _to = to;
        final boolean _lineSelection = lineSelection;

        int[] commentRange = getCommentRange(comments, _from, commentHandler);
        if (commentRange == null) {
            //comment
            if (!forceDirection(false)) {
                comment(target, doc, commentHandler, comments, _from, _to, _lineSelection);
            } else {
                target.getToolkit().beep();
            }
        } else if (comments.length > 0) {
            if (!forceDirection(true)) {
                //uncomment
                uncomment(target, doc, commentHandler, comments, _from, _to, _lineSelection);
            } else {
                target.getToolkit().beep();
            }
        }
    }

    private boolean forceDirection(boolean comment) {
        Object force = comment ?
            getValue("force-comment") : // NOI18N
            getValue("force-uncomment");  // NOI18N
        if (force instanceof Boolean) {
            return ((Boolean)force).booleanValue();
        }
        return false;
    }

    private void comment(JTextComponent target, BaseDocument doc, CommentHandler commentHandler, int[] comments, int from, int to, boolean lineSelection) throws BadLocationException {
//        System.out.println("comment");

        int diff = 0;
        String startDelim = commentHandler.getCommentStartDelimiter();
        String endDelim = commentHandler.getCommentEndDelimiter();

        //put the comment start
        boolean startInComment = false;
        
        if (comments.length > 0) {
            int cStart = comments[0];
            int cEnd = comments[1];
            
            if (cStart <= from && cEnd > from) {
                // the to-be-commented area starts in this comment
                startInComment = true;
            }
        }
        if (!startInComment) {
            // insert start comment mark
            diff += insert(doc, from, startDelim);
        }

        for (int i = 0; i < comments.length; i += 2) {
            int commentStart = comments[i];
            int commentEnd = comments[i + 1];
            if (commentStart >= from) {
                diff += remove(doc, commentStart + diff, startDelim.length());
            }

            if (commentEnd <= to) {
                diff += remove(doc, commentEnd + diff - endDelim.length(), endDelim.length());
            }

        }

        //add closing comment if the last comment doesn't contain the 'to' offset
        if (comments.length == 0 || comments[comments.length - 1] <= to) {
            diff += insert(doc, to + diff, endDelim);
        }

        if (!lineSelection) {
            //update the selection range, we always add the starting delimiter out of the selection
            target.setSelectionStart(from);
            target.setSelectionEnd(to + diff);
        }

    }

    private void uncomment(JTextComponent target, BaseDocument doc, CommentHandler commentHandler, int[] comments, int from, int to, boolean lineSelection) throws BadLocationException {
//        System.out.println("uncomment");

        int diff = 0;
        String startDelim = commentHandler.getCommentStartDelimiter();
        String endDelim = commentHandler.getCommentEndDelimiter();

        //no selection handling
        if (from == to) {
            //extend the range to the only possible comment
            assert comments.length == 2;

            from = comments[0];
            to = comments[1];

            lineSelection = true;
        }

        if (comments[0] < from) {
            //we need to end the existing comment
            diff += insert(doc, from, endDelim);
        }

        int selectionStart = from + diff;

        for (int i = 0; i < comments.length; i += 2) {
            int commentStart = comments[i];
            int commentEnd = comments[i + 1];

            if (commentStart >= from) {
                diff += remove(doc, commentStart + diff, startDelim.length());
            }

            if (commentEnd <= to) {
                diff += remove(doc, commentEnd + diff - endDelim.length(), endDelim.length());
            }

        }

        int selectionEnd = to + diff;
        //add opening comment if the last comment doesn't contain the 'to' offset
        if (comments[comments.length - 1] > to) {
            diff += insert(doc, to + diff, commentHandler.getCommentStartDelimiter());
        }

        if (!lineSelection) {
            //update the selection range, we always add the starting delimiter out of the selection
            target.setSelectionStart(selectionStart);
            target.setSelectionEnd(selectionEnd);
        }

    }

    private int insert(Document doc, int offset, String text) throws BadLocationException {
        doc.insertString(offset, text, null);
        return text.length();
    }

    private int remove(Document doc, int offset, int length) throws BadLocationException {
        doc.remove(offset, length);
        return -length;
    }

    private int[] getCommentRange(int[] comments, int offset, CommentHandler handler) {
        CharSequence commentEnd = handler.getCommentEndDelimiter();
        
        //linear search
        for (int i = 0; i < comments.length; i++) {
            int from = comments[i];
            int to = comments[++i];

            if (from <= offset && to > offset && (commentEnd == null || offset <= (to - commentEnd.length()))) { //end offset exclusive
                return new int[]{from, to};
            }
        }

        return null; //not comment offset

    }

    private boolean isInComment(Document doc, CommentHandler commentHandler, int offset) {
        CharSequence text = DocumentUtilities.getText(doc); //shared instance, low cost
        int lastCommentStartIndex = CharSequenceUtilities.lastIndexOf(text, commentHandler.getCommentStartDelimiter(), offset);
        int lastCommentEndIndex = CharSequenceUtilities.lastIndexOf(text, commentHandler.getCommentEndDelimiter(), offset);

        return lastCommentStartIndex > -1 && (lastCommentStartIndex > lastCommentEndIndex || lastCommentEndIndex == -1 || lastCommentEndIndex == offset);

    }

    private boolean sameCommentHandler(TokenHierarchy<?> th, int offset, boolean backwardBias, CommentHandler cH) {
        CommentHandler cH2 = null;
        List<TokenSequence<?>> seqs = th.embeddedTokenSequences(offset, backwardBias);
        if (!seqs.isEmpty()) {
            for(int i = seqs.size() - 1; i >= 0; i--) {
                TokenSequence<?> ts = seqs.get(i);
                Language lang = LanguageRegistry.getInstance().getLanguageByMimeType(ts.language().mimeType());
                if (lang != null) {
                    if (lang.getGsfLanguage() instanceof DefaultLanguageConfig) {
                        cH2 = ((DefaultLanguageConfig) lang.getGsfLanguage()).getCommentHandler();
                    }
                    break;
                }
            }
        }

        return cH2 != null && cH.getClass() == cH2.getClass();
    }

    private void debug(Document doc, List<int[]> blocks, Level level) {
        LOG.log(level, "TOGGLE_COMENT"); //NOI18N
        for (int [] block : blocks) {
            try {
                int from = block[0];
                int to = block[1];
                if (from != -1 && to != -1) {
                    LOG.log(level, "[{0}, {1}]: ''{2}''", new Object[] { from, to, doc.getText(from, to - from) }); //NOI18N
                } else {
                    LOG.log(level, "[{0}, {1}]", new Object[] { from, to }); //NOI18N
                }
            } catch (BadLocationException ex) {
                LOG.log(level, null, ex);
            }
        }
        LOG.log(level, "----------------"); //NOI18N
    }

    private void check(int[] comments, int from, int to) {
        if (comments.length % 2 != 0) {
            throw new IllegalArgumentException("Comments array size must be even, e.g. contain just pairs.");
        }

        for (int i = 0; i < comments.length; i++) {
            int cfrom = comments[i];
            int cto = comments[++i];
            if (cfrom < from && cto < from || cto > to && cfrom > to) {
                throw new IllegalArgumentException("Comment [" + cfrom + " - " + cto + " is out of the range [" + from + " - " + to + "]!");
            }
        }
    }

    // -- copied from ExtKit.ToggleCommentAction

    private void commentUncommentLines(JTextComponent target, int offset, LanguagePath lp, TokenHierarchy<?> th, String lineCommentString) throws BadLocationException {
        final BaseDocument doc = (BaseDocument)target.getDocument();

        // determine all language blocks between <startPos, endPos>
        int from = offset, to;
        if (Utilities.isSelectionShowing(target)) {
            to = target.getSelectionEnd();
            if (to > 0 && Utilities.getRowStart(doc, to) == to) {
                to--;
            }
        } else { // selection not visible
            to = Utilities.getRowEnd(doc, target.getCaretPosition());
        }

        int fromLineStartOffset = Utilities.getRowStart(doc, from);
        List<TokenSequence<?>> seqs = th.tokenSequenceList(lp, fromLineStartOffset, to);
        List<int []> blocks = new LinkedList<int []>();

        for(int i = 0; i < seqs.size(); i++) {
            int startPos = -1;
            int endPos = -1;

            TokenSequence<?> ts = seqs.get(i);
            ts.move(fromLineStartOffset);
            while (ts.moveNext()) {
                TokenSequence<?> embeddedSeq = ts.embedded();
                if (embeddedSeq != null && !ts.token().id().primaryCategory().contains("comment")) { //NOI18N
                    if (startPos != -1 && endPos != -1) {
                        blocks.add(new int [] { startPos, endPos });
                    }
                    startPos = endPos = -1;
                    continue;
                }

                if (startPos == -1) {
                    startPos = Math.max(ts.offset(), fromLineStartOffset);
                }

                int tokenEnd = ts.offset() + ts.token().length();
                if (endPos < tokenEnd) {
                    endPos = Math.min(tokenEnd, to);
                }

                if (tokenEnd > to) {
                    break;
                }
            }

            if (startPos != -1 && endPos != -1) {
                blocks.add(new int [] { startPos, endPos });
            }
        }
        
        if (blocks.isEmpty() && from == to) {
            comment(doc, from, 1, lineCommentString);
        }

        if (LOG.isLoggable(Level.FINE)) {
            debug(doc, blocks, Level.FINE);
        }
        
        int lastLineOffset = -1;
        int lastLineEndOffset = -1;
        for(int [] block : blocks) {
            if (lastLineOffset != -1) {
                if (block[0] <= lastLineEndOffset) {
                    // this block starts at the same line where the previous block ends
                    if (block[1] <= lastLineEndOffset) {
                        // this block ends at the same line where the previous block ends
                        // and so we can safely ignore this block
                        block[0] = block[1] = -1;
                        continue;
                    } else {
                        // this block ends on some furter line, move the beginning of this block
                        // to the following line and update the last* offsets
                        block[0] = lastLineEndOffset + 1;
                    }
                }
            }

            lastLineOffset = Math.max(Utilities.getRowStart(doc, block[1]), block[0]);
            lastLineEndOffset = Utilities.getRowEnd(doc, block[1]);
        }

        if (LOG.isLoggable(Level.FINE)) {
            debug(doc, blocks, Level.FINE);
        }

        for (ListIterator<int []> i = blocks.listIterator(blocks.size()); i.hasPrevious(); ) {
            int [] block = i.previous();
            int startPos = block[0];
            int endPos = block[1];

            if (startPos == -1 || endPos == -1) {
                continue;
            }

            int lineCount = Utilities.getRowCount(doc, startPos, endPos);
            boolean comment = !allComments(doc, startPos, lineCount, lineCommentString);

            if (comment) {
                if (!forceDirection(false)) {
                    comment(doc, startPos, lineCount, lineCommentString);
                 } else {
                    target.getToolkit().beep();
                 }
            } else {
                if (!forceDirection(true)) {
                    uncomment(doc, startPos, lineCount, lineCommentString);
                } else {
                    target.getToolkit().beep();
                }
            }
        }
    }

    private boolean allComments(BaseDocument doc, int startOffset, int lineCount, String lineCommentString) throws BadLocationException {
        final int lineCommentStringLen = lineCommentString.length();
        for (int offset = startOffset; lineCount > 0; lineCount--) {
            int firstNonWhitePos = Utilities.getRowFirstNonWhite(doc, offset);
            if (firstNonWhitePos == -1) {
                return false;
            }

            if (Utilities.getRowEnd(doc, firstNonWhitePos) - firstNonWhitePos < lineCommentStringLen) {
                return false;
            }

            CharSequence maybeLineComment = DocumentUtilities.getText(doc, firstNonWhitePos, lineCommentStringLen);
            if (!CharSequenceUtilities.textEquals(maybeLineComment, lineCommentString)) {
                return false;
            }

            offset = Utilities.getRowStart(doc, offset, +1);
        }
        return true;
    }

    private void comment(BaseDocument doc, int startOffset, int lineCount, String lineCommentString) throws BadLocationException {
        for (int offset = startOffset; lineCount > 0; lineCount--) {
            doc.insertString(offset, lineCommentString, null); // NOI18N
            offset = Utilities.getRowStart(doc, offset, +1);
        }
    }

    private void uncomment(BaseDocument doc, int startOffset, int lineCount, String lineCommentString) throws BadLocationException {
        final int lineCommentStringLen = lineCommentString.length();
        for (int offset = startOffset; lineCount > 0; lineCount--) {
            // Get the first non-whitespace char on the current line
            int firstNonWhitePos = Utilities.getRowFirstNonWhite(doc, offset);

            // If there is any, check wheter it's the line-comment-chars and remove them
            if (firstNonWhitePos != -1) {
                if (Utilities.getRowEnd(doc, firstNonWhitePos) - firstNonWhitePos >= lineCommentStringLen) {
                    CharSequence maybeLineComment = DocumentUtilities.getText(doc, firstNonWhitePos, lineCommentStringLen);
                    if (CharSequenceUtilities.textEquals(maybeLineComment, lineCommentString)) {
                        doc.remove(firstNonWhitePos, lineCommentStringLen);
                    }
                }
            }

            offset = Utilities.getRowStart(doc, offset, +1);
        }
    }


}

