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

package org.netbeans.editor;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JToggleButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import javax.swing.text.AbstractDocument;
import javax.swing.text.View;
import javax.swing.undo.UndoManager;
import org.netbeans.api.editor.caret.CaretInfo;
import org.netbeans.api.editor.EditorActionNames;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.EditorActionRegistrations;
import org.netbeans.api.editor.EditorUtilities;
import org.netbeans.api.editor.caret.EditorCaret;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.lib.editor.util.swing.PositionRegion;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.api.editor.NavigationHistory;
import org.netbeans.api.editor.caret.CaretMoveContext;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.spi.editor.caret.CaretMoveHandler;
import org.netbeans.modules.editor.lib2.RectangularSelectionUtils;
import org.netbeans.modules.editor.lib2.view.DocumentView;
import org.netbeans.spi.editor.typinghooks.CamelCaseInterceptor;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
* Actions that are not considered basic and therefore
* they are not included directly in BaseKit, but here.
* Their names however are still part of BaseKit.
*
* @author Miloslav Metelka
* @version 1.00
*
* TODO: I18N in RunMacroAction errors
*/

public class ActionFactory {

    // -J-Dorg.netbeans.editor.ActionFactory.level=FINE
    private static final Logger LOG = Logger.getLogger(ActionFactory.class.getName());
    
    private ActionFactory() {
        // no instantiation
    }

    // No registration since shared instance gets created
    //@EditorActionRegistration(name = BaseKit.removeTabAction)
    public static class RemoveTabAction extends LocalBaseAction {

        static final long serialVersionUID =-1537748600593395706L;

        public RemoveTabAction() {
            super(BaseKit.removeTabAction,
                    MAGIC_POSITION_RESET | ABBREV_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed(final ActionEvent evt, final JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }
                final Caret caret = target.getCaret();
                final BaseDocument doc = (BaseDocument)target.getDocument();
                doc.runAtomicAsUser (new Runnable () {
                    public void run () {
                        DocumentUtilities.setTypingModification(doc, true);
                        try {
                            if(caret instanceof EditorCaret) {
                                EditorCaret editorCaret = (EditorCaret) caret;
                                editorCaret.moveCarets(new CaretMoveHandler() {
                                    @Override
                                    public void moveCarets(CaretMoveContext context) {
                                        BaseDocument doc = (BaseDocument) context.getDocument();
                                        for (CaretInfo caretInfo : context.getOriginalSortedCarets()) {
                                            if (caretInfo.isSelection()) { // block selected
                                                try {
                                                    int start = Math.min(caretInfo.getDot(), caretInfo.getMark());
                                                    int end = Math.max(caretInfo.getDot(), caretInfo.getMark());
                                                    BaseKit.changeBlockIndent(
                                                        doc,
                                                        start,
                                                        end,
                                                        -1);
                                                } catch (GuardedException e) {
                                                    target.getToolkit().beep();
                                                } catch (BadLocationException e) {
                                                    e.printStackTrace();
                                                }
                                            } else { // no selected text
                                                try {
                                                    int dot = caretInfo.getDot();
                                                    int lineStartOffset = LineDocumentUtils.getLineStartOffset(doc, dot);
                                                    int firstNW = Utilities.getRowFirstNonWhite(doc, dot);
                                                    if (firstNW != -1 && dot <= firstNW) {
                                                        // Non-white row and caret inside initial whitespace => decrease text indent
                                                        int lineEndOffset = LineDocumentUtils.getLineEndOffset(doc, dot);
                                                        BaseKit.changeBlockIndent(doc, lineStartOffset, lineEndOffset, -1);
                                                    } else {
                                                        int endNW = (firstNW == -1)
                                                                ? lineStartOffset
                                                                : (Utilities.getRowLastNonWhite(doc, dot) + 1);
                                                        if (dot > endNW) {
                                                            int shiftWidth = doc.getShiftWidth();
                                                            if (shiftWidth > 0) {
                                                                int dotColumn = Utilities.getVisualColumn(doc, dot);
                                                                int targetColumn = Math.max(0,
                                                                        (dotColumn - 1) / shiftWidth * shiftWidth);
                                                                // There may be '\t' chars so remove char-by-char
                                                                // and possibly fill-in spaces to get to targetColumn
                                                                while (dotColumn > targetColumn && --dot >= endNW) {
                                                                    doc.remove(dot, 1);
                                                                    dotColumn = Utilities.getVisualColumn(doc, dot);
                                                                }
                                                                int insertLen;
                                                                if (dot >= endNW && (insertLen = targetColumn - dotColumn) > 0) {
                                                                    char[] spaceChars = new char[insertLen];
                                                                    Arrays.fill(spaceChars, ' ');
                                                                    String spaces = new String(spaceChars);
                                                                    doc.insertString(dot, spaces, null);
                                                                }
                                                            }
                                                        }
                                                    }
                                                } catch (GuardedException e) {
                                                    target.getToolkit().beep();
                                                } catch (BadLocationException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                });
                            } else {
                                if (Utilities.isSelectionShowing(caret)) { // block selected
                                    try {
                                        BaseKit.changeBlockIndent(
                                            doc,
                                            target.getSelectionStart(),
                                            target.getSelectionEnd(),
                                            -1);
                                    } catch (GuardedException e) {
                                        target.getToolkit().beep();
                                    } catch (BadLocationException e) {
                                        e.printStackTrace();
                                    }
                                } else { // no selected text
                                    try {
                                        int dot = caret.getDot();
                                        int lineStartOffset = LineDocumentUtils.getLineStartOffset(doc, dot);
                                        int firstNW = Utilities.getRowFirstNonWhite(doc, dot);
                                        if (firstNW != -1 && dot <= firstNW) {
                                            // Non-white row and caret inside initial whitespace => decrease text indent
                                            int lineEndOffset = LineDocumentUtils.getLineEndOffset(doc, dot);
                                            BaseKit.changeBlockIndent(doc, lineStartOffset, lineEndOffset, -1);
                                        } else {
                                            int endNW = (firstNW == -1)
                                                    ? lineStartOffset
                                                    : (Utilities.getRowLastNonWhite(doc, dot) + 1);
                                            if (dot > endNW) {
                                                int shiftWidth = doc.getShiftWidth();
                                                if (shiftWidth > 0) {
                                                    int dotColumn = Utilities.getVisualColumn(doc, dot);
                                                    int targetColumn = Math.max(0,
                                                            (dotColumn - 1) / shiftWidth * shiftWidth);
                                                    // There may be '\t' chars so remove char-by-char
                                                    // and possibly fill-in spaces to get to targetColumn
                                                    while (dotColumn > targetColumn && --dot >= endNW) {
                                                        doc.remove(dot, 1);
                                                        dotColumn = Utilities.getVisualColumn(doc, dot);
                                                    }
                                                    int insertLen;
                                                    if (dot >= endNW && (insertLen = targetColumn - dotColumn) > 0) {
                                                        char[] spaceChars = new char[insertLen];
                                                        Arrays.fill(spaceChars, ' ');
                                                        String spaces = new String(spaceChars);
                                                        doc.insertString(dot, spaces, null);
                                                    }
                                                }
                                            }
                                        }
                                    } catch (GuardedException e) {
                                        target.getToolkit().beep();
                                    } catch (BadLocationException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } finally {
                            DocumentUtilities.setTypingModification(doc, false);
                        }
                    }
                });
            }

        }

    }

    /**
     * @deprecated use {@link CamelCaseInterceptor} instead
     */
    @Deprecated
    public static class RemoveWordPreviousAction extends LocalBaseAction {

        public RemoveWordPreviousAction() {
            super(BaseKit.removePreviousWordAction,
                    MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed(final ActionEvent evt, final JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                final Caret caret = target.getCaret();
                final BaseDocument doc = (BaseDocument)target.getDocument();
                doc.runAtomicAsUser (new Runnable () {
                    public void run () {
                        DocumentUtilities.setTypingModification(doc, true);
                        try {
                            int dotPos = caret.getDot();
                            int bolPos = LineDocumentUtils.getLineStartOffset(doc, dotPos);
                            int wsPos = Utilities.getPreviousWord(target, dotPos);
                            wsPos = (dotPos == bolPos) ? wsPos : Math.max(bolPos, wsPos);
                            doc.remove(wsPos, dotPos - wsPos);
                        } catch (BadLocationException e) {
                            target.getToolkit().beep();
                        } finally {
                            DocumentUtilities.setTypingModification(doc, false);
                        }
                    }
                });
            }
        }
    }

    /**
     * @deprecated use {@link CamelCaseInterceptor} instead
     */
    @Deprecated
    public static class RemoveWordNextAction extends LocalBaseAction {

        public RemoveWordNextAction() {
            super(BaseKit.removeNextWordAction,
                    MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed (final ActionEvent evt, final JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                final Caret caret = target.getCaret();
                final BaseDocument doc = (BaseDocument)target.getDocument();
                doc.runAtomicAsUser (new Runnable () {
                    public void run () {
                        DocumentUtilities.setTypingModification(doc, true);
                        try {
                            int dotPos = caret.getDot();
                            int eolPos = LineDocumentUtils.getLineEndOffset(doc, dotPos);
                            int wsPos = Utilities.getNextWord(target, dotPos);
                            wsPos = (dotPos == eolPos) ? wsPos : Math.min(eolPos, wsPos);
                            doc.remove(dotPos , wsPos - dotPos);
                        } catch (BadLocationException e) {
                            target.getToolkit().beep();
                        } finally {
                            DocumentUtilities.setTypingModification(doc, false);
                        }
                    }
                });
            }
        }
    }

    
    @EditorActionRegistration(name = BaseKit.removeLineBeginAction)
    public static class RemoveLineBeginAction extends LocalBaseAction {

        static final long serialVersionUID =9193117196412195554L;

        public RemoveLineBeginAction() {
            super(MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed (final ActionEvent evt, final JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                final Caret caret = target.getCaret();
                final BaseDocument doc = (BaseDocument)target.getDocument();
                doc.runAtomicAsUser (new Runnable () {
                    public void run () {
                        DocumentUtilities.setTypingModification(doc, true);
                        try {
                            int dotPos = caret.getDot();
                            int bolPos = LineDocumentUtils.getLineStartOffset(doc, dotPos);
                            if (dotPos == bolPos) { // at begining of the line
                                if (dotPos > 0) {
                                    doc.remove(dotPos - 1, 1); // remove previous new-line
                                }
                            } else { // not at the line begining
                                char[] chars = doc.getChars(bolPos, dotPos - bolPos);
                                if (Analyzer.isWhitespace(chars, 0, chars.length)) {
                                    doc.remove(bolPos, dotPos - bolPos); // remove whitespace
                                } else {
                                    int firstNW = Utilities.getRowFirstNonWhite(doc, bolPos);
                                    if (firstNW >= 0 && firstNW < dotPos) {
                                        doc.remove(firstNW, dotPos - firstNW);
                                    }
                                }
                            }
                        } catch (BadLocationException e) {
                            target.getToolkit().beep();
                        } finally {
                            DocumentUtilities.setTypingModification(doc, false);
                        }
                    }
                });
            }
        }
    }

    @EditorActionRegistration(name = BaseKit.removeLineAction)
    public static class RemoveLineAction extends LocalBaseAction {

        static final long serialVersionUID =-536315497241419877L;

        public RemoveLineAction() {
            super(MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed (final ActionEvent evt, final JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                final Caret caret = target.getCaret();
                final BaseDocument doc = (BaseDocument)target.getDocument();
                doc.runAtomicAsUser (new Runnable () {
                    public void run () {
                        DocumentUtilities.setTypingModification(doc, true);
                        try {
                            if (caret instanceof EditorCaret) {
                                EditorCaret editorCaret = (EditorCaret) caret;
                                editorCaret.moveCarets(new CaretMoveHandler() {
                                    @Override
                                    public void moveCarets(CaretMoveContext context) {
                                        BaseDocument doc = (BaseDocument) context.getDocument();
                                        boolean beeped = false;
                                        for (CaretInfo caretInfo : context.getOriginalSortedCarets()) {
                                            try {
                                                int start = Math.min(caretInfo.getDot(), caretInfo.getMark());
                                                int end = Math.max(caretInfo.getDot(), caretInfo.getMark());
                                                int bolPos = LineDocumentUtils.getLineStartOffset(doc, start);
                                                int eolPos = LineDocumentUtils.getLineEndOffset(doc, end);
                                                if (eolPos == doc.getLength()) {
                                                    // Ending newline can't be removed so instead remove starting newline if it exist
                                                    if (bolPos > 0) {
                                                        bolPos--;
                                                    }
                                                } else { // Not the last line
                                                    eolPos++;
                                                }
                                                doc.remove(bolPos, eolPos - bolPos);
                                                // Caret will be at bolPos due to removal
                                            } catch (BadLocationException e) {
                                                if (!beeped) {
                                                    target.getToolkit().beep();
                                                    beeped = true;
                                                }
                                            }
                                        }
                                    }
                                });
                            } else {
                                try {
                                    int bolPos = Utilities.getRowStart(target, target.getSelectionStart());
                                    int eolPos = Utilities.getRowEnd(target, target.getSelectionEnd());
                                    if (eolPos == doc.getLength()) {
                                        // Ending newline can't be removed so instead remove starting newline if it exist
                                        if (bolPos > 0) {
                                            bolPos--;
                                        }
                                    } else { // Not the last line
                                        eolPos++;
                                    }
                                    doc.remove(bolPos, eolPos - bolPos);
                                    // Caret will be at bolPos due to removal
                                } catch (BadLocationException e) {
                                    target.getToolkit().beep();
                                }
                            }
                        } finally {
                            DocumentUtilities.setTypingModification(doc, false);
                        }
                    }
                });
            }
        }
    }
    
    @EditorActionRegistration(name = BaseKit.moveSelectionElseLineUpAction)
    public static class MoveSelectionElseLineUpAction extends LocalBaseAction {

        static final long serialVersionUID = 1L;

        public MoveSelectionElseLineUpAction() {
            super(MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
        }

        @Override
        public void actionPerformed (final ActionEvent evt, final JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }
                final BaseDocument doc = (BaseDocument) target.getDocument();
                if (doc instanceof GuardedDocument && ((GuardedDocument) doc).isPosGuarded(target.getCaretPosition())) {
                    target.getToolkit().beep();
                    return;
                }
                doc.runAtomicAsUser (new Runnable () {
                    @Override
                    public void run () {
                        DocumentUtilities.setTypingModification(doc, true);
                        try {
                            Element rootElement = doc.getDefaultRootElement();

                            Caret caret = target.getCaret();
                            boolean selection = false;
                            boolean backwardSelection = false;
                            int start = target.getCaretPosition();
                            int end = start;

                            // check if there is a selection
                            if (Utilities.isSelectionShowing(caret)) {
                                int selStart = caret.getDot();
                                int selEnd = caret.getMark();
                                start = Math.min(selStart, selEnd);
                                end =   Math.max(selStart, selEnd) - 1;
                                selection = true;
                                backwardSelection = (selStart >= selEnd);
                            }

                            int zeroBaseStartLineNumber = rootElement.getElementIndex(start);
                            int zeroBaseEndLineNumber = rootElement.getElementIndex(end);

                            if (zeroBaseStartLineNumber == -1) {
                                // could not get line number
                                target.getToolkit().beep();
                            } else if (zeroBaseStartLineNumber == 0) {
                                // already first line
                            } else {
                                try {
                                    // get line text
                                    Element startLineElement = rootElement.getElement(zeroBaseStartLineNumber);
                                    int startLineStartOffset = startLineElement.getStartOffset();

                                    Element endLineElement = rootElement.getElement(zeroBaseEndLineNumber);
                                    int endLineEndOffset = endLineElement.getEndOffset();

                                    String linesText = doc.getText(startLineStartOffset, (endLineEndOffset - startLineStartOffset));

                                    Element previousLineElement = rootElement.getElement(zeroBaseStartLineNumber - 1);
                                    int previousLineStartOffset = previousLineElement.getStartOffset();

                                    int column = start - startLineStartOffset;

                                    // insert the text before the previous line
                                    doc.insertString(previousLineStartOffset, linesText, null);
                                    
                                    // remove the line
                                    if (endLineEndOffset + linesText.length() > doc.getLength()) {
                                        removeLineByLine(doc, startLineStartOffset + linesText.length() - 1, endLineEndOffset - startLineStartOffset);
                                    } else {
                                        removeLineByLine(doc, startLineStartOffset + linesText.length(), endLineEndOffset - startLineStartOffset);
                                    }
                                    
                                    if (selection) {
                                        // select moved lines
                                        if (backwardSelection) {
                                            caret.setDot(previousLineStartOffset + column);
                                            caret.moveDot(previousLineStartOffset + (endLineEndOffset - startLineStartOffset) - (endLineEndOffset - end - 1));
                                        } else {
                                            caret.setDot(previousLineStartOffset + (endLineEndOffset - startLineStartOffset) - (endLineEndOffset - end - 1));
                                            caret.moveDot(previousLineStartOffset + column);
                                        }
                                    } else {
                                        // set caret position
                                        target.setCaretPosition(previousLineStartOffset + column);
                                    }
                                } catch (BadLocationException ex) {
                                    target.getToolkit().beep();
                                }
                            }
                        } finally {
                            DocumentUtilities.setTypingModification(doc, false);
                        }
                    }
                });
            }
        }
    }
    
    @EditorActionRegistration(name = BaseKit.moveSelectionElseLineDownAction)
    public static class MoveSelectionElseLineDownAction extends LocalBaseAction {

        static final long serialVersionUID = 1L;

        public MoveSelectionElseLineDownAction() {
            super(MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
        }

        @Override
        public void actionPerformed (final ActionEvent evt, final JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }
                final BaseDocument doc = (BaseDocument) target.getDocument();
                if (doc instanceof GuardedDocument && ((GuardedDocument) doc).isPosGuarded(target.getCaretPosition())) {
                    target.getToolkit().beep();
                    return;
                }
                doc.runAtomicAsUser (new Runnable () {
                    @Override
                    public void run () {
                        DocumentUtilities.setTypingModification(doc, true);
                        try {
                            Element rootElement = doc.getDefaultRootElement();

                            Caret caret = target.getCaret();
                            boolean selection = false;
                            boolean backwardSelection = false;
                            int start = target.getCaretPosition();
                            int end = start;
                            
                            // check if there is a selection
                            if (Utilities.isSelectionShowing(caret)) {
                                int selStart = caret.getDot();
                                int selEnd = caret.getMark();
                                start = Math.min(selStart, selEnd);
                                end =   Math.max(selStart, selEnd) - 1;
                                selection = true;
                                backwardSelection = (selStart >= selEnd);
                            }

                            int zeroBaseStartLineNumber = rootElement.getElementIndex(start);
                            int zeroBaseEndLineNumber = rootElement.getElementIndex(end);

                            if (zeroBaseEndLineNumber == -1) {
                                // could not get line number
                                target.getToolkit().beep();
                            } else {
                                try {
                                    // get line text
                                    Element startLineElement = rootElement.getElement(zeroBaseStartLineNumber);
                                    int startLineStartOffset = startLineElement.getStartOffset();

                                    Element endLineElement = rootElement.getElement(zeroBaseEndLineNumber);
                                    int endLineEndOffset = endLineElement.getEndOffset();
                                    if (endLineEndOffset > doc.getLength()) {
                                        return;
                                    }

                                    String linesText = doc.getText(startLineStartOffset, (endLineEndOffset - startLineStartOffset));

                                    Element nextLineElement = rootElement.getElement(zeroBaseEndLineNumber + 1);
                                    int nextLineEndOffset = nextLineElement.getEndOffset();

                                    int column = start - startLineStartOffset;

                                    // insert it after next line
                                    if (nextLineEndOffset > doc.getLength()) {
                                        doc.insertString(doc.getLength(), "\n" + linesText.substring(0, linesText.length()-1), null);
                                    } else {
                                        doc.insertString(nextLineEndOffset, linesText, null);
                                    }

                                    // remove original line
                                    removeLineByLine(doc, startLineStartOffset, (endLineEndOffset - startLineStartOffset));
                                    if (selection) {
                                        // select moved lines
                                        if (backwardSelection) {
                                            if (doc.getLength() <  nextLineEndOffset) {
                                                caret.setDot(doc.getLength()  - (endLineEndOffset - startLineStartOffset) + column + 1);
                                                caret.moveDot(doc.getLength());
                                            } else {
                                                caret.setDot(nextLineEndOffset - (endLineEndOffset - startLineStartOffset) + column);
                                                caret.moveDot(nextLineEndOffset - (endLineEndOffset - end - 1));
                                            }
                                        } else {
                                            caret.setDot(nextLineEndOffset - (endLineEndOffset - end - 1));
                                            caret.moveDot(nextLineEndOffset  - (endLineEndOffset - startLineStartOffset) + column);
                                        }
                                    } else {
                                        // set caret position
                                        target.setCaretPosition(Math.min(doc.getLength(), nextLineEndOffset + column - (endLineEndOffset - startLineStartOffset)));
                                    }
                                } catch (BadLocationException ex) {
                                    target.getToolkit().beep();
                                }
                            }
                        } finally {
                            DocumentUtilities.setTypingModification(doc, false);
                        }
                    }
                });
            }
        }
    }
    
    static void removeLineByLine(Document doc, int startPosition, int length) throws BadLocationException {
        String text = doc.getText(startPosition, length);
        BadLocationException ble = null;
        int notDeleted = 0;
        int deleted = 0;
        int line = 0;
        while(true) {
            line = text.indexOf('\n', line+1);
            if (line == -1) {
                break;
            }
            try {
                doc.remove(startPosition + notDeleted, line + 1 - deleted - notDeleted);
                deleted = line + 1 - notDeleted;
            } catch (BadLocationException blee) {
                ble = blee;
                notDeleted = line + 1 - deleted;
            }
        }
        doc.remove(startPosition + notDeleted, length - deleted - notDeleted);
        if (ble != null) {
            throw ble;
        }
    }
    
    @EditorActionRegistration(name = BaseKit.copySelectionElseLineUpAction)
    public static class CopySelectionElseLineUpAction extends LocalBaseAction {

        static final long serialVersionUID = 1L;
        
        public CopySelectionElseLineUpAction() {
            super(MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed (final ActionEvent evt, final JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }
                final BaseDocument doc = (BaseDocument) target.getDocument();
                doc.runAtomicAsUser (new Runnable () {
                    public void run () {
                        DocumentUtilities.setTypingModification(doc, true);
                        try {
                            Element rootElement = doc.getDefaultRootElement();

                            Caret caret = target.getCaret();
                            boolean selection = false;
                            boolean backwardSelection = false;
                            int start = target.getCaretPosition();
                            int end = start;

                            // check if there is a selection
                            if (Utilities.isSelectionShowing(caret)) {
                                int selStart = caret.getDot();
                                int selEnd = caret.getMark();
                                start = Math.min(selStart, selEnd);
                                end =   Math.max(selStart, selEnd) - 1;
                                selection = true;
                                backwardSelection = (selStart >= selEnd);
                            }

                            int zeroBaseStartLineNumber = rootElement.getElementIndex(start);
                            int zeroBaseEndLineNumber = rootElement.getElementIndex(end);

                            if (zeroBaseStartLineNumber == -1) {
                                // could not get line number
                                target.getToolkit().beep();
                                return;
                            } else {
                                try {
                                    // get line text
                                    Element startLineElement = rootElement.getElement(zeroBaseStartLineNumber);
                                    int startLineStartOffset = startLineElement.getStartOffset();

                                    Element endLineElement = rootElement.getElement(zeroBaseEndLineNumber);
                                    int endLineEndOffset = endLineElement.getEndOffset();

                                    String linesText = doc.getText(startLineStartOffset, (endLineEndOffset - startLineStartOffset));

                                    int column = start - startLineStartOffset;

                                    try {
                                        NavigationHistory.getEdits().markWaypoint(target, startLineStartOffset, false, true);
                                    } catch (BadLocationException e) {
                                        LOG.log(Level.WARNING, "Can't add position to the history of edits.", e); //NOI18N
                                    }
                                    // insert it
                                    doc.insertString(startLineStartOffset, linesText, null);

                                    if (selection) {
                                        // select moved lines
                                        if (backwardSelection) {
                                            caret.setDot(startLineStartOffset + column);
                                            caret.moveDot(startLineStartOffset + (endLineEndOffset - startLineStartOffset) - (endLineEndOffset - end - 1));
                                        } else {
                                            caret.setDot(startLineStartOffset + (endLineEndOffset - startLineStartOffset) - (endLineEndOffset - end - 1));
                                            caret.moveDot(startLineStartOffset + column);
                                        }
                                    } else {
                                        // set caret position
                                        target.setCaretPosition(startLineStartOffset + column);
                                    }
                                } catch (BadLocationException ex) {
                                    target.getToolkit().beep();
                                }
                            }
                        } finally {
                            DocumentUtilities.setTypingModification(doc, false);
                        }
                    }
                });
            }
        }
    }
    
    @EditorActionRegistration(name = BaseKit.copySelectionElseLineDownAction)
    public static class CopySelectionElseLineDownAction extends LocalBaseAction {

        static final long serialVersionUID = 1L;

        public CopySelectionElseLineDownAction() {
            super(MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed (final ActionEvent evt, final JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled() || Boolean.TRUE.equals(target.getClientProperty("AsTextField"))) {
                    target.getToolkit().beep();
                    return;
                }
                final BaseDocument doc = (BaseDocument) target.getDocument();
                doc.runAtomicAsUser (new Runnable () {
                    public void run () {
                        DocumentUtilities.setTypingModification(doc, true);
                        try {
                            Element rootElement = doc.getDefaultRootElement();

                            Caret caret = target.getCaret();
                            boolean selection = false;
                            boolean backwardSelection = false;
                            int start = target.getCaretPosition();
                            int end = start;

                            // check if there is a selection
                            if (Utilities.isSelectionShowing(caret)) {
                                int selStart = caret.getDot();
                                int selEnd = caret.getMark();
                                start = Math.min(selStart, selEnd);
                                end =   Math.max(selStart, selEnd) - 1;
                                selection = true;
                                backwardSelection = (selStart >= selEnd);
                            }

                            int zeroBaseStartLineNumber = rootElement.getElementIndex(start);
                            int zeroBaseEndLineNumber = rootElement.getElementIndex(end);

                            if (zeroBaseEndLineNumber == -1) {
                                // could not get line number
                                target.getToolkit().beep();
                                return;
                            } else {
                                try {
                                    // get line text
                                    Element startLineElement = rootElement.getElement(zeroBaseStartLineNumber);
                                    int startLineStartOffset = startLineElement.getStartOffset();

                                    Element endLineElement = rootElement.getElement(zeroBaseEndLineNumber);
                                    int endLineEndOffset = endLineElement.getEndOffset();

                                    String linesText = doc.getText(startLineStartOffset, (endLineEndOffset - startLineStartOffset));

                                    int column = start - startLineStartOffset;

                                    try {
                                        if (endLineEndOffset == doc.getLength() + 1) {
                                            NavigationHistory.getEdits().markWaypoint(target, endLineEndOffset - 1, false, true);
                                        } else {
                                            NavigationHistory.getEdits().markWaypoint(target, endLineEndOffset, false, true);
                                        }
                                    } catch (BadLocationException e) {
                                        LOG.log(Level.WARNING, "Can't add position to the history of edits.", e); //NOI18N
                                    }
                                    // insert it after next line
                                    if (endLineEndOffset == doc.getLength() + 1) { // extra newline at doc end (not included in doc-len)
                                        assert (linesText.charAt(linesText.length() - 1) == '\n');
                                        doc.insertString(endLineEndOffset - 1, "\n" + linesText.substring(0, linesText.length() - 1), null);
                                    } else { // Regular case
                                        doc.insertString(endLineEndOffset, linesText, null);
                                    }

                                    if (selection) {
                                        // select moved lines
                                        if (backwardSelection) {
                                            caret.setDot(endLineEndOffset + column);
                                            caret.moveDot(endLineEndOffset + (endLineEndOffset - startLineStartOffset) - (endLineEndOffset - end - 1));
                                        } else {
                                            caret.setDot(endLineEndOffset + (endLineEndOffset - startLineStartOffset) - (endLineEndOffset - end - 1));
                                            caret.moveDot(endLineEndOffset + column);
                                        }
                                    } else {
                                        // set caret position
                                        target.setCaretPosition(Math.min(doc.getLength() - 1, endLineEndOffset + column));
                                    }
                                } catch (BadLocationException ex) {
                                    target.getToolkit().beep();
                                }
                            }
                        } finally {
                            DocumentUtilities.setTypingModification(doc, false);
                        }
                    }
                });
            }
        }
    }

    /* Useful for popup menu - remove selected block or do nothing */
    // No annotation registration since shared instance exists in BaseKit
    //@EditorActionRegistration(name = BaseKit.removeSelectionAction)
    public static class RemoveSelectionAction extends LocalBaseAction {

        static final long serialVersionUID =-1419424594746686573L;

        public RemoveSelectionAction() {
            super(BaseKit.removeSelectionAction,
                    MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
            //#54893 putValue ("helpID", RemoveSelectionAction.class.getName ()); // NOI18N
        }

        public void actionPerformed(ActionEvent evt, final JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                final BaseDocument doc = (BaseDocument)target.getDocument();
                doc.runAtomicAsUser (new Runnable () {
                    public void run () {
                        DocumentUtilities.setTypingModification(doc, true);
                        try {
                            target.replaceSelection(null);
                        } finally {
                            DocumentUtilities.setTypingModification(doc, false);
                        }
                    }
                });
            }
        }
    }

    /** Switch to overwrite mode or back to insert mode
     * @deprecated Replaced by ToggleTypingModeAction in editor.actions module
     */
    @Deprecated
    public static class ToggleTypingModeAction extends LocalBaseAction {

        static final long serialVersionUID =-2431132686507799723L;

        public ToggleTypingModeAction() {
            super();
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Boolean overwriteMode = (Boolean) target.getClientProperty(EditorUtilities.CARET_OVERWRITE_MODE_PROPERTY);
                // Now toggle
                overwriteMode = (overwriteMode == null || !overwriteMode.booleanValue())
                                ? Boolean.TRUE : Boolean.FALSE;
                target.putClientProperty(EditorUtilities.CARET_OVERWRITE_MODE_PROPERTY, overwriteMode);
            }
        }
    }

    /**
     * @deprecated Without any replacement. This action is not used anymore and
     * is no longer functional.
     */
    @Deprecated
    public static class RunMacroAction extends BaseAction {

        static final long serialVersionUID =1L;

        static HashSet runningActions = new HashSet();
        private String macroName;

        public RunMacroAction( String name ) {
            super( BaseKit.macroActionPrefix + name);
            this.macroName = name;
        }

        protected void error( JTextComponent target, String messageKey ) {
            Utilities.setStatusText( target, LocaleSupport.getString(
                messageKey, "Error in macro: " + messageKey ) // NOI18N
            );
            Toolkit.getDefaultToolkit().beep();
        }
        
        public void actionPerformed(ActionEvent evt, final JTextComponent target) {
            if( !runningActions.add( macroName ) ) { // this macro is already running, beware of loops
                error( target, "loop" ); // NOI18N
                return;
            }

            if( target == null ) return;
           
            final BaseKit kit = Utilities.getKit(target);
            if( kit == null ) return;
            
//            Map macroMap = (Map)Settings.getValue( kit.getClass(), SettingsNames.MACRO_MAP);
//            
//            String commandString = (String)macroMap.get( macroName );
            String commandString = null;
            
            if( commandString == null ) {
                error( target, "macro-not-found" ); // NOI18N
                runningActions.remove( macroName );
                return;
            }

            final StringBuffer actionName = new StringBuffer();
            final char[] command = commandString.toCharArray();
            final int len = command.length;

            final BaseDocument doc = (BaseDocument)target.getDocument();
            doc.runAtomicAsUser (new Runnable () {
                public void run () {
                    try {
                        for( int i = 0; i < len; i++ ) {
                            if( Character.isWhitespace( command[i] ) ) continue;
                            if( command[i] == '"' ) {
                                while( ++i < len && command[i] != '"' ) {
                                    char ch = command[i];
                                    if( ch == '\\' ) {
                                        if( ++i >= len ) { // '\' at the end
                                            error( target, "macro-malformed" ); // NOI18N
                                            return;
                                        }
                                        ch = command[i];
                                        if( ch != '"' && ch != '\\' ) { // neither \\ nor \" // NOI18N
                                            error( target, "macro-malformed" ); // NOI18N
                                            return;
                                        } // else fall through
                                    }
                                    Action a = target.getKeymap().getDefaultAction();

                                    if (a != null) {
                                        ActionEvent newEvt = new ActionEvent( target, 0, new String( new char[] { ch } ) );
                                        if( a instanceof BaseAction ) {
                                            ((BaseAction)a).updateComponent(target);
                                            ((BaseAction)a).actionPerformed( newEvt, target );
                                        } else {
                                            a.actionPerformed( newEvt );
                                        }
                                    }
                                }
                            } else { // parse the action name
                                actionName.setLength( 0 );
                                while( i < len && ! Character.isWhitespace( command[i] ) ) {
                                    char ch = command[i++];
                                    if( ch == '\\' ) {
                                        if( i >= len ) { // macro ending with single '\'
                                            error( target, "macro-malformed" ); // NOI18N
                                            return;
                                        };
                                        ch = command[i++];
                                        if( ch != '\\' && ! Character.isWhitespace( ch ) ) {//
                                            error( target, "macro-malformed" ); // neither "\\" nor "\ " // NOI18N
                                            return;
                                        } // else fall through
                                    }
                                    actionName.append( ch );
                                }
                                // execute the action
                                Action a = kit.getActionByName( actionName.toString() );
                                if (a != null) {
                                    ActionEvent fakeEvt = new ActionEvent( target, 0, "" );
                                    if( a instanceof BaseAction ) {
                                        ((BaseAction)a).updateComponent(target);
                                        ((BaseAction)a).actionPerformed( fakeEvt, target );
                                    } else {
                                        a.actionPerformed( fakeEvt );
                                    }
                                    if(DefaultEditorKit.insertBreakAction.equals(actionName.toString())){
                                        Action def = target.getKeymap().getDefaultAction();
                                        ActionEvent fakeEvt10 = new ActionEvent( target, 0, new String(new byte[]{10}) );
                                        if( def instanceof BaseAction ) {
                                            ((BaseAction)def).updateComponent(target);
                                            ((BaseAction)def).actionPerformed( fakeEvt10, target );
                                        } else {
                                            def.actionPerformed( fakeEvt10 );
                                        }
                                    }
                                } else {
                                    error( target, "macro-unknown-action" ); // NOI18N
                                    return;
                                }
                            }
                        }
                    } finally {
                        runningActions.remove( macroName );
                    }
                }
            });
        }
    } // End of RunMacroAction class
    
    /**
     * @deprecated Without any replacement. This action is not used anymore.
     */
    @Deprecated
    public static class StartMacroRecordingAction extends LocalBaseAction {
    // Not registered by annotation since it's not actively used

        static final long serialVersionUID =1L;

        public StartMacroRecordingAction() {
            super( BaseKit.startMacroRecordingAction, NO_RECORDING );
            putValue(BaseAction.ICON_RESOURCE_PROPERTY,
                "org/netbeans/modules/editor/resources/start_macro_recording.png"); // NOI18N
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if( !startRecording(target) ) target.getToolkit().beep();
            }
        }
    }

    /**
     * @deprecated Without any replacement. This action is not used anymore and
     * is no longer functional.
     */
    @Deprecated
    public static class StopMacroRecordingAction extends LocalBaseAction {
    // Not registered by annotation since it's not actively used

        static final long serialVersionUID =1L;

        public StopMacroRecordingAction() {
            super( BaseKit.stopMacroRecordingAction, NO_RECORDING );
            putValue(BaseAction.ICON_RESOURCE_PROPERTY,
                "org/netbeans/modules/editor/resources/stop_macro_recording.png"); // NOI18N
        }
        
        protected MacroDialogSupport getMacroDialogSupport(Class kitClass){
            return new MacroDialogSupport(kitClass);
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                String macro = stopRecording(target);
                if( macro == null ) { // not recording
                    target.getToolkit().beep();
                } else {
                    // popup a macro dialog
                    BaseKit kit = Utilities.getKit(target);
                    MacroDialogSupport support = getMacroDialogSupport(kit.getClass());
                    support.setBody( macro );
                    support.showMacroDialog();
                }
            }
        }
    }

    /** @deprecated Use Editor Code Templates API instead. */
    @Deprecated
    public static class AbbrevExpandAction extends LocalBaseAction {

        static final long serialVersionUID =-2124569510083544403L;

        public AbbrevExpandAction() {
            super(BaseKit.abbrevExpandAction,
                  MAGIC_POSITION_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
            putValue(BaseAction.NO_KEYBINDING, Boolean.TRUE);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                EditorUI editorUI = ((BaseTextUI)target.getUI()).getEditorUI();
                try {
                    editorUI.getAbbrev().checkAndExpand(evt);
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    /** @deprecated Use Editor Code Templates API instead. */
    @Deprecated
    public static class AbbrevResetAction extends LocalBaseAction {

        static final long serialVersionUID =-2807497346060448395L;

        public AbbrevResetAction() {
            super(BaseKit.abbrevResetAction, ABBREV_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
        }

    }

    public static class ChangeCaseAction extends LocalBaseAction {

        @EditorActionRegistration(name = BaseKit.toUpperCaseAction)
        public static ChangeCaseAction createToUpperCase() {
            return new ChangeCaseAction(Utilities.CASE_UPPER);
        }

        @EditorActionRegistration(name = BaseKit.toLowerCaseAction)
        public static ChangeCaseAction createToLowerCase() {
            return new ChangeCaseAction(Utilities.CASE_LOWER);
        }

        @EditorActionRegistration(name = BaseKit.switchCaseAction)
        public static ChangeCaseAction createSwitchCase() {
            return new ChangeCaseAction(Utilities.CASE_SWITCH);
        }

        int changeCaseMode;

        static final long serialVersionUID =5680212865619897402L;

        private ChangeCaseAction(int changeCaseMode) {
            super(ABBREV_RESET | MAGIC_POSITION_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
            this.changeCaseMode = changeCaseMode;
        }

        public void actionPerformed(ActionEvent evt, final JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }
                final BaseDocument doc = (BaseDocument) target.getDocument();
                final Caret caret = target.getCaret();
                doc.runAtomicAsUser(new Runnable() {
                    @Override
                    public void run() {
                        if (caret instanceof EditorCaret) {
                            EditorCaret editorCaret = (EditorCaret) caret;
                            if (RectangularSelectionUtils.isRectangularSelection(target)) { // no selection - change current char
                                try {
                                    List<Position> positions = RectangularSelectionUtils.regionsCopy(target);
                                    for (int i = 0; i < positions.size(); i += 2) {
                                        int a = positions.get(i).getOffset();
                                        int b = positions.get(i + 1).getOffset();
                                        if (a == b) {
                                            continue;
                                        }
                                        Utilities.changeCase((BaseDocument) target.getDocument(), a, b - a, changeCaseMode);
                                    }
                                } catch (BadLocationException e) {
                                    target.getToolkit().beep();
                                }
                            } else {
                                editorCaret.moveCarets(new CaretMoveHandler() {
                                    @Override
                                    public void moveCarets(CaretMoveContext context) {
                                        boolean beeped = false;
                                        for (CaretInfo caretInfo : context.getOriginalSortedCarets()) {
                                            try {
                                                int dotOffset = caretInfo.getDot();
                                                Position.Bias dotBias = caretInfo.getDotBias();
                                                int markOffset = caretInfo.getMark();
                                                Position.Bias markBias = caretInfo.getMarkBias();
                                                if (dotOffset != markOffset) { // valid selection
                                                    int startOffset = Math.min(dotOffset, markOffset);
                                                    int endOffset = Math.max(dotOffset, markOffset);
                                                    Utilities.changeCase(doc, startOffset, endOffset - startOffset, changeCaseMode);
                                                    // Recreate positions since they might move by removals in changeCase()
                                                    context.setDotAndMark(caretInfo,
                                                            doc.createPosition(dotOffset), dotBias,
                                                            doc.createPosition(markOffset), markBias);
                                                } else { // no selection - change current char
                                                    Utilities.changeCase(doc, dotOffset, 1, changeCaseMode);
                                                    context.setDot(caretInfo, doc.createPosition(dotOffset + 1), Position.Bias.Forward);
                                                }
                                            } catch (BadLocationException e) {
                                                if (!beeped) {
                                                    context.getComponent().getToolkit().beep();
                                                    beeped = true;
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        } else {
                            try {
                                BaseDocument doc = (BaseDocument) target.getDocument();
                                int dotPos = caret.getDot();
                                if (RectangularSelectionUtils.isRectangularSelection(target)) { // no selection - change current char
                                    List<Position> positions = RectangularSelectionUtils.regionsCopy(target);
                                    for (int i = 0; i < positions.size(); i += 2) {
                                        int a = positions.get(i).getOffset();
                                        int b = positions.get(i + 1).getOffset();
                                        if (a == b) {
                                            continue;
                                        }
                                        Utilities.changeCase(doc, a, b - a, changeCaseMode);
                                    }
                                } else if (Utilities.isSelectionShowing(caret)) { // valid selection
                                    int startPos = target.getSelectionStart();
                                    int endPos = target.getSelectionEnd();
                                    Utilities.changeCase(doc, startPos, endPos - startPos, changeCaseMode);
                                    caret.setDot(dotPos == startPos ? endPos : startPos);
                                    caret.moveDot(dotPos == startPos ? startPos : endPos);
                                } else { // no selection - change current char
                                    Utilities.changeCase(doc, dotPos, 1, changeCaseMode);
                                    caret.setDot(dotPos + 1);
                                }
                            } catch (BadLocationException e) {
                                target.getToolkit().beep();
                            }
                        }
                    }
                });
            }
        }
    }

    // Cannot easily use EditorActionRegistration yet for toggle buttons
    public static class ToggleRectangularSelectionAction extends LocalBaseAction
    implements Presenter.Toolbar, ContextAwareAction, PropertyChangeListener, DocumentListener {

        static final long serialVersionUID = 0L;
        
        private Reference<JEditorPane> paneRef;
        
        private Reference<JToggleButton> toggleButtonRef;

        public ToggleRectangularSelectionAction() {
            super(EditorActionNames.toggleRectangularSelection);
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/editor/resources/rect_select_16x16.png", false)); //NOI18N
            putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        }
        
        void setPane(JEditorPane pane) {
            JEditorPane origPane = getPane();
            if (origPane != null) {
                origPane.removePropertyChangeListener(this);
                origPane.getDocument().removeDocumentListener(this);
            }
            assert (pane != null);
            this.paneRef = new WeakReference<>(pane);
            pane.addPropertyChangeListener(this);
            pane.getDocument().addDocumentListener(this);
            updateState();
        }
        
        JEditorPane getPane() {
            return (paneRef != null ? paneRef.get() : null);
        }
        
        JToggleButton getToggleButton() {
            return (toggleButtonRef != null ? toggleButtonRef.get() : null);
        }
        
        void updateState() {
            JEditorPane pane = getPane();
            if (pane != null) {
                boolean rectangleSelection = RectangularSelectionUtils.isRectangularSelection(pane);
                JToggleButton toggleButton = getToggleButton();
                if (toggleButton != null) {
                    toggleButton.setSelected(rectangleSelection);
                    toggleButton.setContentAreaFilled(rectangleSelection);
                    toggleButton.setBorderPainted(rectangleSelection);
                }
            }
        }

        @Override
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null && !Boolean.TRUE.equals(target.getClientProperty("AsTextField"))) {
                boolean newRectSel = !RectangularSelectionUtils.isRectangularSelection(target);
                RectangularSelectionUtils.setRectangularSelection(target, newRectSel);
            }
        }

        @Override
        public Component getToolbarPresenter() {
            JToggleButton toggleButton = new JToggleButton();
            toggleButtonRef = new WeakReference<>(toggleButton);
            toggleButton.putClientProperty("hideActionText", Boolean.TRUE); //NOI18N
            toggleButton.setIcon((Icon) getValue(SMALL_ICON));
            toggleButton.setAction(this); // this will make hard ref to button => check GC
            return toggleButton;
        }

        @Override
        public Action createContextAwareInstance(Lookup actionContext) {
            JEditorPane pane = actionContext.lookup(JEditorPane.class);
            if (pane != null) {
                ToggleRectangularSelectionAction action = new ToggleRectangularSelectionAction();
                action.setPane(pane);
                return action;
            }
            return this;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            JEditorPane pane = getPane();
            if (pane == evt.getSource()) { // Event from pane
                if (RectangularSelectionUtils.getRectangularSelectionProperty().equals(evt.getPropertyName())) {
                    updateState();
                }
            }
        }

        private void documentUpdate(DocumentEvent e) {
            JEditorPane pane = getPane();
            if (pane != null && RectangularSelectionUtils.isRectangularSelection(pane) && !Boolean.TRUE.equals(e.getDocument().getProperty(RectangularSelectionUtils.RECTANGULAR_DO_NOT_RESET_AFTER_DOCUMENT_CHANGE))) {
                RectangularSelectionUtils.resetRectangularSelection(pane);
                e.getDocument().putProperty(RectangularSelectionUtils.RECTANGULAR_DO_NOT_RESET_AFTER_DOCUMENT_CHANGE, Boolean.FALSE);
            }
        }
        
        @Override
        public void insertUpdate(DocumentEvent e) {
            documentUpdate(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            documentUpdate(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            documentUpdate(e);
        }
    }    

    public static class UndoAction extends LocalBaseAction {

        static final long serialVersionUID =8628586205035497612L;

        public UndoAction() {
            super(BaseKit.undoAction, ABBREV_RESET
                  | MAGIC_POSITION_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (!target.isEditable() || !target.isEnabled()) {
                target.getToolkit().beep();
                return;
            }

            Document doc = target.getDocument();
            UndoableEdit undoMgr = (UndoableEdit)doc.getProperty(
                                       BaseDocument.UNDO_MANAGER_PROP);
            if (undoMgr == null) {
                undoMgr = (UndoableEdit) doc.getProperty(UndoManager.class);
            }
            if (target != null && undoMgr != null) {
                try {
                    undoMgr.undo();
                } catch (CannotUndoException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    public static class RedoAction extends LocalBaseAction {

        static final long serialVersionUID =6048125996333769202L;

        public RedoAction() {
            super(BaseKit.redoAction, ABBREV_RESET
                  | MAGIC_POSITION_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (!target.isEditable() || !target.isEnabled()) {
                target.getToolkit().beep();
                return;
            }

            Document doc = target.getDocument();
            UndoableEdit undoMgr = (UndoableEdit)doc.getProperty(
                                       BaseDocument.UNDO_MANAGER_PROP);
            if (undoMgr == null) {
                undoMgr = (UndoableEdit) doc.getProperty(UndoManager.class);
            }
            if (target != null && undoMgr != null) {
                try {
                    undoMgr.redo();
                } catch (CannotRedoException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    @EditorActionRegistrations({
        @EditorActionRegistration(name = BaseKit.wordMatchNextAction,
            iconResource = "org/netbeans/modules/editor/resources/next_matching.png"),
        @EditorActionRegistration(name = BaseKit.wordMatchPrevAction,
            iconResource = "org/netbeans/modules/editor/resources/previous_matching.png")
    })
    public static class WordMatchAction extends LocalBaseAction {

        private boolean matchNext;

        static final long serialVersionUID =595571114685133170L;

        public WordMatchAction() {
            super(ABBREV_RESET | MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
        }

        @Override
        protected void actionNameUpdate(String actionName) {
            super.actionNameUpdate(actionName);
            this.matchNext = BaseKit.wordMatchNextAction.equals(actionName);
        }

        public void actionPerformed(final ActionEvent evt, final  JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                EditorUI editorUI = Utilities.getEditorUI(target);
                Caret caret = target.getCaret();
                final BaseDocument doc = Utilities.getDocument(target);

                if(caret instanceof EditorCaret) {
                    EditorCaret editorCaret = (EditorCaret) caret;
                    if(editorCaret.getCarets().size() > 1) {
                        target.getToolkit().beep();
                        return;
                    }
                }
                
                // Possibly remove selection
                if (Utilities.isSelectionShowing(caret)) {
                    target.replaceSelection(null);
                }

                final int caretOffset = caret.getDot();
                final String s = editorUI.getWordMatch().getMatchWord(caretOffset, matchNext);
                final String prevWord = editorUI.getWordMatch().getPreviousWord();
                if (s != null) {
                    doc.runAtomicAsUser (new Runnable () {
                        public void run () {
                            DocumentUtilities.setTypingModification(doc, true);
                            try {
                                int offset = caretOffset;
                                boolean removePrevWord = (prevWord != null && prevWord.length() > 0);
                                if (removePrevWord) {
                                    offset -= prevWord.length();
                                }
                                // Create position due to possible text replication (e.g. for variable renaming)
                                Position pos = doc.createPosition(offset);
                                doc.remove(offset, prevWord.length());
                                doc.insertString(pos.getOffset(), s, null);
                            } catch (BadLocationException e) {
                                target.getToolkit().beep();
                            } finally {
                                DocumentUtilities.setTypingModification(doc, false);
                            }
                        }
                    });
                }
            }
        }

    }


    @EditorActionRegistrations({
        @EditorActionRegistration(name = BaseKit.shiftLineLeftAction,
            iconResource = "org/netbeans/modules/editor/resources/shift_line_left.png"),
        @EditorActionRegistration(name = BaseKit.shiftLineRightAction,
            iconResource = "org/netbeans/modules/editor/resources/shift_line_right.png")
    })
    public static class ShiftLineAction extends LocalBaseAction {

        static final long serialVersionUID =-5124732597493699582L;

        public ShiftLineAction() {
            super(MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
        }

        @Override
        protected void actionNameUpdate(String actionName) {
            super.actionNameUpdate(actionName);
        }

        public void actionPerformed (final ActionEvent evt, final JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                final Caret caret = target.getCaret();
                final BaseDocument doc = Utilities.getDocument(target);
                doc.runAtomicAsUser (new Runnable () {
                    public void run () {
                        DocumentUtilities.setTypingModification(doc, true);
                        try {
                            boolean right = BaseKit.shiftLineRightAction.equals(getValue(Action.NAME));
                            if (Utilities.isSelectionShowing(caret)) {
                                BaseKit.shiftBlock(
                                    doc,
                                    target.getSelectionStart(), target.getSelectionEnd(),
                                    right);
                            } else {
                                BaseKit.shiftLine(doc, caret.getDot(), right);
                            }
                        } catch (GuardedException e) {
                            target.getToolkit().beep();
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        } finally {
                            DocumentUtilities.setTypingModification(doc, false);
                        }
                    }
                });
            }
        }
    }

    @EditorActionRegistrations({
        @EditorActionRegistration(name = BaseKit.reindentLineAction),
        @EditorActionRegistration(name = BaseKit.reformatLineAction)
    })        
    public static class ReindentLineAction extends LocalBaseAction {

        private boolean reindent;
        
        static final long serialVersionUID =1L;

        public ReindentLineAction() {
            // TODO: figure out what these flags are all about
            super(ABBREV_RESET | MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
            //putValue ("helpID", ReindentLineAction.class.getName ());
        }

        @Override
        protected void actionNameUpdate(String actionName) {
            super.actionNameUpdate(actionName);
            this.reindent = BaseKit.reindentLineAction.equals(actionName);
        }

        public void actionPerformed (final ActionEvent evt, final JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                final Caret caret = target.getCaret();
                final BaseDocument doc = (BaseDocument)target.getDocument();
                final GuardedDocument gdoc = (doc instanceof GuardedDocument)
                                       ? (GuardedDocument)doc : null;

                final Indent indenter = reindent ? Indent.get(doc) : null;
                final Reformat reformat = reindent ? null : Reformat.get(doc);
                if (reindent) {
                    indenter.lock();
                } else {
                    reformat.lock();
                }
                try {
                    doc.runAtomicAsUser (new Runnable () {
                        public void run () {
                            try {
                                int startPos;
                                Position endPosition;

                                if (Utilities.isSelectionShowing(caret)) {
                                    startPos = target.getSelectionStart();
                                    endPosition = doc.createPosition(target.getSelectionEnd());
                                } else {
                                    startPos = LineDocumentUtils.getLineStartOffset(doc, caret.getDot());
                                    endPosition = doc.createPosition(LineDocumentUtils.getLineEndOffset(doc, caret.getDot()));
                                }

                                int pos = startPos;
                                if (gdoc != null) {
                                    pos = gdoc.getGuardedBlockChain().adjustToBlockEnd(pos);
                                }

                                while (pos <= endPosition.getOffset()) {
                                    int stopPos = endPosition.getOffset();
                                    if (gdoc != null) { // adjust to start of the next guarded block
                                        stopPos = gdoc.getGuardedBlockChain().adjustToNextBlockStart(pos);
                                        if (stopPos == -1 || stopPos > endPosition.getOffset()) {
                                            stopPos = endPosition.getOffset();
                                        }
                                    }

                                    Position stopPosition = doc.createPosition(stopPos);
                                    if (reindent) {
                                        indenter.reindent(pos, stopPos);
                                    } else {
                                        reformat.reformat(pos, stopPos);
                                    }
                                    pos = stopPosition.getOffset() + 1;

                                    if (gdoc != null) { // adjust to end of current block
                                        pos = gdoc.getGuardedBlockChain().adjustToBlockEnd(pos);
                                    }
                                }
                            } catch (GuardedException e) {
                                target.getToolkit().beep();
                            } catch (BadLocationException e) {
                                Utilities.annotateLoggable(e);
                            }
                        }
                    });
                } finally {
                    if (reindent) {
                        indenter.unlock();
                    } else {
                        reformat.unlock();
                    }
                }
            }
        }
    }

    
    public static class AdjustWindowAction extends LocalBaseAction {

        @EditorActionRegistration(name = BaseKit.adjustWindowTopAction)
        public static AdjustWindowAction createAdjustTop() {
            return new AdjustWindowAction(0);
        }

        @EditorActionRegistration(name = BaseKit.adjustWindowCenterAction)
        public static AdjustWindowAction createAdjustCenter() {
            return new AdjustWindowAction(50);
        }

        @EditorActionRegistration(name = BaseKit.adjustWindowBottomAction)
        public static AdjustWindowAction createAdjustBottom() {
            return new AdjustWindowAction(100);
        }

        int percentFromWindowTop;

        static final long serialVersionUID =8864278998999643292L;

        public AdjustWindowAction(int percentFromWindowTop) {
            this.percentFromWindowTop = percentFromWindowTop;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Utilities.getEditorUI(target).adjustWindow(percentFromWindowTop);
            }
        }
    }

    public static class AdjustCaretAction extends LocalBaseAction {

        @EditorActionRegistration(name = BaseKit.adjustCaretTopAction)
        public static AdjustCaretAction createAdjustTop() {
            return new AdjustCaretAction(0);
        }

        @EditorActionRegistration(name = BaseKit.adjustCaretCenterAction)
        public static AdjustCaretAction createAdjustCenter() {
            return new AdjustCaretAction(50);
        }

        @EditorActionRegistration(name = BaseKit.adjustCaretBottomAction)
        public static AdjustCaretAction createAdjustBottom() {
            return new AdjustCaretAction(100);
        }

        int percentFromWindowTop;

        static final long serialVersionUID =3223383913531191066L;

        public AdjustCaretAction(int percentFromWindowTop) {
            this.percentFromWindowTop = percentFromWindowTop;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Utilities.getEditorUI(target).adjustCaret(percentFromWindowTop);
            }
        }
    }

    @EditorActionRegistrations({
        @EditorActionRegistration(name = BaseKit.formatAction),
        @EditorActionRegistration(name = BaseKit.indentAction)
    })        
    public static class FormatAction extends LocalBaseAction {

        static final long serialVersionUID =-7666172828961171865L;
        
        private boolean indentOnly;

        public FormatAction() {
            super(ABBREV_RESET | MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
            //#54893 putValue ("helpID", FormatAction.class.getName ()); // NOI18N
        }

        @Override
        protected void actionNameUpdate(String actionName) {
            super.actionNameUpdate(actionName);
            this.indentOnly = BaseKit.indentAction.equals(actionName);
        }
        
        public void actionPerformed (final ActionEvent evt, final JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                final Caret caret = target.getCaret();
                final BaseDocument doc = Utilities.getDocument(target);
                if (doc == null)
                    return;
                // Set hourglass cursor
                final Cursor origCursor = target.getCursor();
                target.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                try {
                final AtomicBoolean canceled = new AtomicBoolean();
                    BaseProgressUtils.runOffEventDispatchThread(new Runnable() {
                    public void run() {
                        if (canceled.get()) return;
                        final Reformat formatter = indentOnly ? null : Reformat.get(doc);
                        final Indent indenter = indentOnly ? Indent.get(doc) : null;
                        if (indentOnly) {
                            indenter.lock();
                        } else {
                            formatter.lock();
                        }
                        try {
                            if (canceled.get()) return;
                            doc.runAtomicAsUser(new Runnable() {

                                public void run() {
                                    try {
                                        int startPos, endPos;
                                        if (Utilities.isSelectionShowing(caret)) {
                                            startPos = target.getSelectionStart();
                                            endPos = target.getSelectionEnd();
                                        } else {
                                            startPos = 0;
                                            endPos = doc.getLength();
                                        }
                                        List<PositionRegion> regions = collectRegions(doc, startPos, endPos);

                                        if (canceled.get()) return;
                                        // Once we start formatting, the task can't be canceled

                                        for (PositionRegion region : regions) {
                                            if (indentOnly) {
                                                indenter.reindent(region.getStartOffset(), region.getEndOffset());
                                            } else {
                                                formatter.reformat(region.getStartOffset(), region.getEndOffset());
                                            }
                                        }
                                    } catch (GuardedException e) {
                                        target.getToolkit().beep();
                                    } catch (BadLocationException e) {
                                        Utilities.annotateLoggable(e);
                                    }
                                }
                            });
                        } finally {
                            if (indentOnly) {
                                indenter.unlock();
                            } else {
                                formatter.unlock();
                            }
                        }
                    }
                }, NbBundle.getMessage(FormatAction.class, indentOnly ? "Indent_in_progress" : "Format_in_progress"), canceled, false); //NOI18N
                } catch (Exception e) {
                    // not sure about this, but was getting j.l.Exception that the operation is too slow - wtf?
                    Logger.getLogger(FormatAction.class.getName()).log(Level.FINE, null, e);
                } finally {
                    target.setCursor(origCursor);
                }
            }
        }
    }

    static void reformat(Reformat formatter, Document doc, int startPos, int endPos, AtomicBoolean canceled) throws BadLocationException {
        List<PositionRegion> regions = collectRegions(doc, startPos, endPos);
        if (canceled.get()) return;
        // Once we start formatting, the task can't be canceled
        for (PositionRegion region : regions) {
            formatter.reformat(region.getStartOffset(), region.getEndOffset());
        }
    }
    
    private static List<PositionRegion> collectRegions(Document doc, int startPos, int endPos) throws BadLocationException {
        final GuardedDocument gdoc = (doc instanceof GuardedDocument)
                ? (GuardedDocument) doc : null;

        int pos = startPos;
        if (gdoc != null) {
            pos = gdoc.getGuardedBlockChain().adjustToBlockEnd(pos);
        }

        LinkedList<PositionRegion> regions = new LinkedList<PositionRegion>();
        while (pos < endPos) {
            int stopPos = endPos;
            if (gdoc != null) { // adjust to start of the next guarded block
                stopPos = gdoc.getGuardedBlockChain().adjustToNextBlockStart(pos) - 1;
                if (stopPos < 0 || stopPos > endPos) {
                    stopPos = endPos;
                }
            }

            if (pos < stopPos) {
                regions.addFirst(new PositionRegion(doc, pos, stopPos));
                pos = stopPos;
            } else {
                pos++; //ensure to make progress
            }

            if (gdoc != null) { // adjust to end of current block
                pos = gdoc.getGuardedBlockChain().adjustToBlockEnd(pos);
            }
        }        
        return regions;
    }
    
    @EditorActionRegistrations({
        @EditorActionRegistration(name = BaseKit.firstNonWhiteAction),
        @EditorActionRegistration(name = BaseKit.selectionFirstNonWhiteAction)
    })
    public static class FirstNonWhiteAction extends LocalBaseAction {

        static final long serialVersionUID =-5888439539790901158L;

        public FirstNonWhiteAction() {
            super(MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                try {
                    int pos = Utilities.getRowFirstNonWhite((BaseDocument)target.getDocument(),
                                                            caret.getDot());
                    if (pos >= 0) {
                        boolean select = BaseKit.selectionFirstNonWhiteAction.equals(getValue(Action.NAME));
                        if (select) {
                            caret.moveDot(pos);
                        } else {
                            caret.setDot(pos);
                        }
                    }
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    @EditorActionRegistrations({
        @EditorActionRegistration(name = BaseKit.lastNonWhiteAction),
        @EditorActionRegistration(name = BaseKit.selectionLastNonWhiteAction)
    })
    public static class LastNonWhiteAction extends LocalBaseAction {

        static final long serialVersionUID =4503533041729712917L;

        public LastNonWhiteAction() {
            super(MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                try {
                    int pos = Utilities.getRowLastNonWhite((BaseDocument)target.getDocument(),
                                                           caret.getDot());
                    if (pos >= 0) {
                        boolean select = BaseKit.selectionLastNonWhiteAction.equals(getValue(Action.NAME));
                        if (select) {
                            caret.moveDot(pos);
                        } else {
                            caret.setDot(pos);
                        }
                    }
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    @EditorActionRegistration(name = BaseKit.selectIdentifierAction)
    public static class SelectIdentifierAction extends LocalBaseAction {

        static final long serialVersionUID =-7288216961333147873L;

        public SelectIdentifierAction() {
            super(MAGIC_POSITION_RESET);
        }

        public void actionPerformed(ActionEvent evt, final JTextComponent target) {
            if (target != null) {
                final Caret caret = target.getCaret();
                Document doc = target.getDocument();
                doc.render(new Runnable() {
                    @Override
                    public void run() {
                        if (caret instanceof EditorCaret) {
                            EditorCaret editorCaret = (EditorCaret) caret;
                            editorCaret.moveCarets(new CaretMoveHandler() {
                                @Override
                                public void moveCarets(CaretMoveContext context) {
                                    boolean beeped = false;
                                    for (CaretInfo caretInfo : context.getOriginalSortedCarets()) {
                                        try {
                                            if (caretInfo.isSelectionShowing()) {
                                                context.setDot(caretInfo, caretInfo.getDotPosition(), caretInfo.getDotBias()); // unselect if anything selected
                                            } else {
                                                BaseDocument doc = (BaseDocument) context.getDocument(); // selection not visible
                                                int block[] = Utilities.getIdentifierBlock(doc,
                                                        caretInfo.getDot());
                                                if (block != null) {
                                                    context.setDotAndMark(caretInfo,
                                                            doc.createPosition(block[0]), Position.Bias.Forward,
                                                            doc.createPosition(block[1]), Position.Bias.Forward);
                                                }
                                            }
                                        } catch (BadLocationException e) {
                                            if (!beeped) {
                                                context.getComponent().getToolkit().beep();
                                                beeped = true;
                                            }
                                        }
                                    }
                                }
                            });
                        } else {
                            try {
                                if (Utilities.isSelectionShowing(caret)) {
                                    caret.setDot(caret.getDot()); // unselect if anything selected
                                } else { // selection not visible
                                    int block[] = Utilities.getIdentifierBlock((BaseDocument) target.getDocument(),
                                            caret.getDot());
                                    if (block != null) {
                                        caret.setDot(block[0]);
                                        caret.moveDot(block[1]);
                                    }
                                }
                            } catch (BadLocationException e) {
                                target.getToolkit().beep();
                            }
                        }
                    }
                });
            }
        }
    }

    @EditorActionRegistration(name = BaseKit.selectNextParameterAction)
    public static class SelectNextParameterAction extends LocalBaseAction {

        static final long serialVersionUID =8045372985336370934L;

        public SelectNextParameterAction() {
            super(BaseKit.selectNextParameterAction, MAGIC_POSITION_RESET | CLEAR_STATUS_TEXT);
            putValue(BaseAction.NO_KEYBINDING, Boolean.TRUE);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                BaseDocument doc = (BaseDocument)target.getDocument();
                int dotPos = caret.getDot();
                int selectStartPos = -1;
                try {
                    if (dotPos > 0) {
                        if (doc.getChars(dotPos - 1, 1)[0] == ',') { // right after the comma
                            selectStartPos = dotPos;
                        }
                    }
                    if (dotPos < doc.getLength()) {
                        char dotChar = doc.getChars(dotPos, 1)[0];
                        if (dotChar == ',') {
                            selectStartPos = dotPos + 1;
                        } else if (dotChar == ')') {
                            caret.setDot(dotPos + 1);
                        }
                    }
                    if (selectStartPos >= 0) {
                        int selectEndPos = doc.find(
                                               new FinderFactory.CharArrayFwdFinder( new char[] { ',', ')' }),
                                               selectStartPos, -1
                                           );
                        if (selectEndPos >= 0) {
                            target.select(selectStartPos, selectEndPos);
                        }
                    }
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }

    /**
     * This implementation is no longer used, see org.netbeans.modules.editor.impl.actions.NavigationHistoryForwardAction
     * in the editor module.
     */
    public static class JumpListNextAction extends LocalBaseAction {
    // Not registered by annotation since it's not actively used

        static final long serialVersionUID =6891721278404990446L;
        PropertyChangeListener pcl;

        public JumpListNextAction() {
            super(BaseKit.jumpListNextAction);
            putValue(BaseAction.ICON_RESOURCE_PROPERTY,
                "org/netbeans/modules/editor/resources/edit_next.png"); // NOI18N
            JumpList.addPropertyChangeListener(pcl = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    setEnabled(JumpList.hasNext());
                }
            });
            setEnabled(JumpList.hasNext());
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                JumpList.jumpNext(target);
            }
        }
    }

    /**
     * This implementation is no longer used, see org.netbeans.modules.editor.impl.actions.NavigationHistoryBackAction
     * in the editor module.
     */
    public static class JumpListPrevAction extends LocalBaseAction {
    // Not registered by annotation since it's not actively used

        static final long serialVersionUID =7174907031986424265L;
        PropertyChangeListener pcl;

        public JumpListPrevAction() {
            super(BaseKit.jumpListPrevAction);
            putValue(BaseAction.ICON_RESOURCE_PROPERTY,
                "org/netbeans/modules/editor/resources/edit_previous.png"); // NOI18N
            JumpList.addPropertyChangeListener(pcl = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    setEnabled(JumpList.hasPrev());
                }
            });
            setEnabled(JumpList.hasPrev());
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                JumpList.jumpPrev(target);
            }
        }
    }

    /**
     * This implementation is no longer used, see org.netbeans.modules.editor.impl.actions.NavigationHistoryForwardAction
     * in the editor module.
     */
    public static class JumpListNextComponentAction extends LocalBaseAction {
    // Not registered by annotation since it's not actively used

        static final long serialVersionUID =-2059070050865876892L;

        public JumpListNextComponentAction() {
            super(BaseKit.jumpListNextComponentAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                JumpList.jumpNextComponent(target);
            }
        }
    }

    /**
     * This implementation is no longer used, see org.netbeans.modules.editor.impl.actions.NavigationHistoryBackAction
     * in the editor module.
     */
    public static class JumpListPrevComponentAction extends LocalBaseAction {
    // Not registered by annotation since it's not actively used

        static final long serialVersionUID =2032230534727849525L;

        public JumpListPrevComponentAction() {
            super(BaseKit.jumpListPrevComponentAction);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                JumpList.jumpPrevComponent(target);
            }
        }
    }

    @EditorActionRegistration(name = BaseKit.scrollUpAction)
    public static class ScrollUpAction extends LocalBaseAction {

        public ScrollUpAction() {
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                EditorUI editorUI = Utilities.getEditorUI(target);
                Rectangle bounds = editorUI.getExtentBounds();
                bounds.y += editorUI.getLineHeight();
                bounds.x += editorUI.getTextMargin().left;
                editorUI.scrollRectToVisible(bounds, EditorUI.SCROLL_SMALLEST);
            }
        }

    }

    @EditorActionRegistration(name = BaseKit.scrollDownAction)
    public static class ScrollDownAction extends LocalBaseAction {

        public ScrollDownAction() {
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                EditorUI editorUI = Utilities.getEditorUI(target);
                Rectangle bounds = editorUI.getExtentBounds();
                bounds.y -= editorUI.getLineHeight();
                bounds.x += editorUI.getTextMargin().left;
                editorUI.scrollRectToVisible(bounds, EditorUI.SCROLL_SMALLEST);
            }
        }

    }

    @EditorActionRegistration(name = BaseKit.insertDateTimeAction)
    public static class InsertDateTimeAction extends LocalBaseAction {
        
        static final long serialVersionUID =2865619897402L;
        
        public InsertDateTimeAction() {
            super(ABBREV_RESET | MAGIC_POSITION_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }
                
                try {
                    Caret caret = target.getCaret();
                    BaseDocument doc = (BaseDocument)target.getDocument();
                    
                    // Format the current time.
                    SimpleDateFormat formatter = new SimpleDateFormat();
                    Date currentTime = new Date();
                    String dateString = formatter.format(currentTime);
                    
                    doc.insertString(caret.getDot(), dateString, null);
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }
    
    /** Select text of whole document */
    @EditorActionRegistration(name = BaseKit.generateGutterPopupAction)
    public static class GenerateGutterPopupAction extends LocalBaseAction {

        static final long serialVersionUID =-3502499718130556525L;

        public GenerateGutterPopupAction() {
            putValue(BaseAction.NO_KEYBINDING, Boolean.TRUE);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
        }

        public JMenuItem getPopupMenuItem(JTextComponent target) {
            EditorUI ui = Utilities.getEditorUI(target);
            try {
                return ui.getDocument().getAnnotations().createMenu(Utilities.getKit(target), Utilities.getLineOffset(ui.getDocument(),target.getCaret().getDot()));
            } catch (BadLocationException ex) {
                return null;
            }
        }
    
    }

    /**
     * Switch visibility of line numbers in editor
     * @deprecated this action is no longer used. It is reimplemented in editor.actions module.
     */
    //@EditorActionRegistration(name = BaseKit.toggleLineNumbersAction)
    // Registration in createActions() due to getPopupMenuItem()
    @Deprecated
    public static class ToggleLineNumbersAction extends LocalBaseAction {

        static final long serialVersionUID =-3502499718130556526L;
        
        private JCheckBoxMenuItem item = null;

        public ToggleLineNumbersAction() {
            super(BaseKit.toggleLineNumbersAction); // Due to creation from MainMenuAction
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            toggleLineNumbers();
        }
        
        public JMenuItem getPopupMenuItem(JTextComponent target) {
            
            item = new JCheckBoxMenuItem(NbBundle.getBundle(BaseKit.class).
                    getString("line-numbers-menuitem"), isLineNumbersVisible());
            item.addItemListener( new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    actionPerformed(null,null);
                }
            });
            return item;
        }
        
        protected boolean isLineNumbersVisible() {
            return false;
        }
        
        protected void toggleLineNumbers() {
        }
        
    }
    
    /** Cycle through annotations on the current line */
    @EditorActionRegistration(name = BaseKit.annotationsCyclingAction)
    public static class AnnotationsCyclingAction extends LocalBaseAction {
        
        public AnnotationsCyclingAction() {
            putValue(BaseAction.NO_KEYBINDING, Boolean.TRUE);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                try {
                    Caret caret = target.getCaret();
                    BaseDocument doc = Utilities.getDocument(target);
                    int caretLine = Utilities.getLineOffset(doc, caret.getDot());
                    AnnotationDesc aDesc = doc.getAnnotations().activateNextAnnotation(caretLine);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /* package */ static abstract class DeprecatedFoldAction extends LocalBaseAction {
        private String delegateId;
        
        DeprecatedFoldAction(String id) {
            this.delegateId = id;
            
        }
        
        public void actionPerformed(ActionEvent evt, final JTextComponent target) {
            BaseKit kit = (target == null) ? BaseKit.getKit(BaseKit.class) : Utilities.getKit(target);
            if (kit != null) {
                Action a = kit.getActionByName(delegateId);
                if ((a instanceof BaseAction) && a != this) {
                    ((BaseAction)a).actionPerformed(evt, target);
                    return;
                } else {
                    a.actionPerformed(evt);
                    return;
                }
            }
            Toolkit.getDefaultToolkit().beep();
        }
    }
    
    /** Collapse a fold. Depends on the current caret position. 
     * 
     * @deprecated Implementation was adopted into editor.fold.nbui module. This implementation is kept for backward compatibility only
     */
    @Deprecated
    public static class CollapseFold extends DeprecatedFoldAction {
        public CollapseFold(){
            super(BaseKit.collapseFoldAction);
        }
        
        public void actionPerformed(ActionEvent evt, final JTextComponent target) {
            super.actionPerformed(evt, target);
        }
    }
    
    /** Expand a fold. Depends on the current caret position. 
     * @deprecated Implementation was adopted into editor.fold.nbui module. This implementation is kept for backward compatibility only
     */
    @Deprecated
    public static class ExpandFold extends DeprecatedFoldAction {
        public ExpandFold() {
            super(BaseKit.expandFoldAction);
        }

        public void actionPerformed(ActionEvent evt, final JTextComponent target) {
            super.actionPerformed(evt, target);
        }
    }
    
    /** Collapse all existing folds in the document. 
     * @deprecated Implementation was adopted into editor.fold.nbui module. This implementation is kept for backward compatibility only
     */
    @Deprecated
    public static class CollapseAllFolds extends DeprecatedFoldAction {
        public CollapseAllFolds(){
            super(BaseKit.collapseAllFoldsAction);
        }

        public void actionPerformed(ActionEvent evt, final JTextComponent target) {
            super.actionPerformed(evt, target);
        }
    }

    /** Expand all existing folds in the document. 
     * @deprecated Implementation was adopted into editor.fold.nbui module. This implementation is kept for backward compatibility only
     */
    @Deprecated
    public static class ExpandAllFolds extends DeprecatedFoldAction {
        public ExpandAllFolds(){
            super(BaseKit.expandAllFoldsAction);
        }

        public void actionPerformed(ActionEvent evt, final JTextComponent target) {
            super.actionPerformed(evt, target);
        }
    }

    /** Expand all existing folds in the document. */
    @EditorActionRegistration(name = "dump-view-hierarchy")
    public static class DumpViewHierarchyAction extends LocalBaseAction {

        public DumpViewHierarchyAction() {
            putValue(BaseAction.NO_KEYBINDING, Boolean.TRUE);
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            AbstractDocument adoc = (AbstractDocument)target.getDocument();

            // Dump fold hierarchy
            FoldHierarchy hierarchy = FoldHierarchy.get(target);
            adoc.readLock();
            try {
                hierarchy.lock();
                try {
                    /*DEBUG*/System.err.println("FOLD HIERARCHY DUMP:\n" + hierarchy); // NOI18N
                    TokenHierarchy<?> th = TokenHierarchy.get(adoc);
                    /*DEBUG*/System.err.println("TOKEN HIERARCHY DUMP:\n" + (th != null ? th : "<NULL-TH>")); // NOI18N

                } finally {
                    hierarchy.unlock();
                }
            } finally {
                adoc.readUnlock();
            }

            View rootView = null;
            TextUI textUI = target.getUI();
            if (textUI != null) {
                rootView = textUI.getRootView(target); // Root view impl in BasicTextUI
                if (rootView != null && rootView.getViewCount() == 1) {
                    rootView = rootView.getView(0); // Get DocumentView
                }
            }
            if (rootView != null) {
                String rootViewDump = (rootView instanceof DocumentView)
                        ? ((DocumentView)rootView).toStringDetail()
                        : rootView.toString();
                /*DEBUG*/System.err.println("DOCUMENT VIEW: " + System.identityHashCode(rootView) + // NOI18N
                        "\n" + rootViewDump); // NOI18N
                int caretOffset = target.getCaretPosition();
                int caretViewIndex = rootView.getViewIndex(caretOffset, Position.Bias.Forward);
                /*DEBUG*/System.err.println("caretOffset=" + caretOffset + ", caretViewIndex=" + caretViewIndex); // NOI18N
                if (caretViewIndex >= 0 && caretViewIndex < rootView.getViewCount()) {
                    View caretView = rootView.getView(caretViewIndex);
                    /*DEBUG*/System.err.println("caretView: " + caretView); // NOI18N
                }
                /*DEBUG*/System.err.println(FixLineSyntaxState.lineInfosToString(adoc));
                // Check the hierarchy correctness
                //org.netbeans.editor.view.spi.ViewUtilities.checkViewHierarchy(rootView);
            }
            
            if (adoc instanceof BaseDocument) {
                /*DEBUG*/System.err.println("DOCUMENT:\n" + ((BaseDocument)adoc).toStringDetail()); // NOI18N
            }
        }
    }
    
    @EditorActionRegistration(name = BaseKit.startNewLineAction)
    public static class StartNewLine extends LocalBaseAction {

        public StartNewLine() {
            super(ABBREV_RESET | MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
        }

        @Override
        public void actionPerformed(final ActionEvent evt, final JTextComponent target) {
            // shift-enter while editing aka startNewLineAction
            if (!target.isEditable() || !target.isEnabled()) {
                target.getToolkit().beep();
                return;
            }

            final BaseDocument doc = (BaseDocument) target.getDocument();
            final Indent indenter = Indent.get(doc);
            indenter.lock();
            doc.runAtomicAsUser(new Runnable() {
                public void run() {
                    try {
                        Caret caret = target.getCaret();
                        if (caret instanceof EditorCaret) {
                            EditorCaret editorCaret = (EditorCaret) caret;
                            editorCaret.moveCarets(new CaretMoveHandler() {
                                @Override
                                public void moveCarets(CaretMoveContext context) {
                                    for (CaretInfo caretInfo : context.getOriginalSortedCarets()) {
                                        try {
                                            BaseDocument doc = (BaseDocument) context.getDocument();
                                            // insert new line, caret moves to the new line
                                            int eolDot = LineDocumentUtils.getLineEndOffset(doc, caretInfo.getDot());
                                            doc.insertString(eolDot, "\n", null); //NOI18N

                                            // reindent the new line
                                            Position newDotPos = doc.createPosition(eolDot + 1);
                                            indenter.reindent(eolDot + 1);

                                            context.setDot(caretInfo, newDotPos, Position.Bias.Forward);
                                        } catch (BadLocationException ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                }
                            });
                        } else {
                            try {
                                // insert new line, caret moves to the new line
                                int eolDot = Utilities.getRowEnd(target, caret.getDot());
                                doc.insertString(eolDot, "\n", null); //NOI18N

                                // reindent the new line
                                Position newDotPos = doc.createPosition(eolDot + 1);
                                indenter.reindent(eolDot + 1);

                                caret.setDot(newDotPos.getOffset());
                            } catch (BadLocationException ex) {
                                ex.printStackTrace();
                            }
                        }
                    } finally {
                        indenter.unlock();
                    }
                }
            });
        }
    }
    
    /**
     * Cut text from the caret position to either begining or end
     * of the line with the caret.
     */
    @EditorActionRegistrations({
        @EditorActionRegistration(name = BaseKit.cutToLineBeginAction),
        @EditorActionRegistration(name = BaseKit.cutToLineEndAction)
    })
    public static class CutToLineBeginOrEndAction extends LocalBaseAction {
        
        /**
         * Construct new action.
         *
         */
        public CutToLineBeginOrEndAction() {
            super(ABBREV_RESET | MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
        }
        
        public void actionPerformed (final ActionEvent evt, final JTextComponent target) {
            // shift-enter while editing aka startNewLineAction
            if (!target.isEditable() || !target.isEnabled()) {
                target.getToolkit().beep();
                return;
            }
            
            final BaseDocument doc = (BaseDocument)target.getDocument();
            
            doc.runAtomicAsUser (new Runnable () {
                public void run () {
                DocumentUtilities.setTypingModification(doc, true);
                try {
                    ActionMap actionMap = target.getActionMap();
                    Action cutAction;
                    if (actionMap != null && (cutAction = actionMap.get(DefaultEditorKit.cutAction)) != null) {
                        Caret caret = target.getCaret();
                        int caretOffset = caret.getDot();
                        boolean toLineEnd = BaseKit.cutToLineEndAction.equals(getValue(Action.NAME));
                        int boundOffset = toLineEnd
                                ? Utilities.getRowEnd(target, caretOffset)
                                : Utilities.getRowStart(target, caretOffset);

                        // Check whether there is only whitespace from caret position
                        // till end of line
                        if (toLineEnd) {
                            String text = target.getText(caretOffset, boundOffset - caretOffset);
                            if (boundOffset < doc.getLength() && text != null && text.matches("^[\\s]*$")) { // NOI18N
                                boundOffset += 1; // Include line separator
                            }
                        }

                        caret.moveDot(boundOffset);

                        // Call the cut action to cut out the selection
                        cutAction.actionPerformed(evt);
                    }
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                } finally{
                    DocumentUtilities.setTypingModification(doc, false);
                }
                }
            });
        }
    }
    
    
}
