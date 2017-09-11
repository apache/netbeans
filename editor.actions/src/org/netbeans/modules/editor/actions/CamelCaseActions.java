/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.actions;

import java.awt.event.ActionEvent;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.caret.CaretInfo;
import org.netbeans.api.editor.caret.CaretMoveContext;
import org.netbeans.api.editor.caret.EditorCaret;
import org.netbeans.editor.BaseCaret;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib2.RectangularSelectionUtils;
import org.netbeans.modules.editor.lib2.typinghooks.CamelCaseInterceptorsManager;
import org.netbeans.spi.editor.AbstractEditorAction;
import org.netbeans.spi.editor.caret.CaretMoveHandler;


public class CamelCaseActions {

    /* package */ static final String deleteNextCamelCasePosition = "delete-next-camel-case-position"; //NOI18N
    static final String SYSTEM_ACTION_CLASS_NAME_PROPERTY = "systemActionClassName";

    public static abstract class CamelCaseAction extends AbstractEditorAction {

        public CamelCaseAction(Map<String, ?> attrs) {
            super(attrs);
        }

        public CamelCaseAction() {
        }

        @Override
        public void actionPerformed(final ActionEvent evt, final JTextComponent target) {
            if (target != null) {
                if (!target.isEditable() || !target.isEnabled()) {
                    target.getToolkit().beep();
                    return;
                }

                final Caret caret = target.getCaret();
                final BaseDocument doc = (BaseDocument)target.getDocument();
                
                if(caret instanceof EditorCaret) {
                    final EditorCaret editorCaret = (EditorCaret) caret;
                    // Document's lock must come before carets' lock
                    doc.runAtomicAsUser(new Runnable() {
                        @Override
                        public void run() {
                            editorCaret.moveCarets(new CaretMoveHandler() {
                                @Override
                                public void moveCarets(final CaretMoveContext context) {
                                    for (final CaretInfo caretInfo : editorCaret.getSortedCarets()) {
                                        // Do a transaction for each modification
                                        // TBD beforeChange() and afterChange() are called under doc-lock but imho this semantics of interceptors should be revisited anyway
                                        final CamelCaseInterceptorsManager.Transaction t = CamelCaseInterceptorsManager.getInstance().openTransaction(target, caretInfo.getDot(), !isForward());
                                        try {
                                            if (!t.beforeChange()) {
                                                boolean result = false;
                                                if (doesTypingModification()) {
                                                    DocumentUtilities.setTypingModification(doc, true);
                                                }
                                                Object[] r = t.change();
                                                try {
                                                    int dotPos = caretInfo.getDot();
                                                    int wsPos;
                                                    if (r == null) {
                                                        if (isForward()) {
                                                            int eolPos = Utilities.getRowEnd(doc, dotPos);
                                                            wsPos = Utilities.getNextWord(target, dotPos);
                                                            wsPos = (dotPos == eolPos) ? wsPos : Math.min(eolPos, wsPos);
                                                        } else {
                                                            int bolPos = Utilities.getRowStart(doc, dotPos);
                                                            wsPos = Utilities.getPreviousWord(target, dotPos);
                                                            wsPos = (dotPos == bolPos) ? wsPos : Math.max(bolPos, wsPos);
                                                        }
                                                    } else {
                                                        wsPos = (Integer) r[0];
                                                    }

                                                    if (isForward()) {
                                                        moveToNewOffset(context, caretInfo, dotPos, wsPos - dotPos);
                                                    } else {
                                                        moveToNewOffset(context, caretInfo, wsPos, dotPos - wsPos);
                                                    }
                                                    result = true;
                                                } catch (BadLocationException e) {
                                                    target.getToolkit().beep();
                                                } finally {
                                                    if (doesTypingModification()) {
                                                        DocumentUtilities.setTypingModification(doc, false);
                                                    }
                                                }
                                                if (result) {
                                                    t.afterChange();
                                                }
                                            }
                                        } finally {
                                            t.close();
                                        }
                                }
                                }
                            });
                        }
                    });

                } else {
                    final CamelCaseInterceptorsManager.Transaction t = CamelCaseInterceptorsManager.getInstance().openTransaction(target, caret.getDot(), !isForward());
                    try {
                        if (!t.beforeChange()) {
                            final Boolean [] result = new Boolean [] { Boolean.FALSE };
                            doc.runAtomicAsUser(new Runnable() {
                                @Override
                                public void run() {
                                    if (doesTypingModification()) {
                                        DocumentUtilities.setTypingModification(doc, true);
                                    }
                                    Object[] r = t.change();
                                    try {
                                        int dotPos = caret.getDot();
                                        int wsPos;
                                        if (r == null) {
                                            if (isForward()) {
                                                int eolPos = Utilities.getRowEnd(doc, dotPos);
                                                wsPos = Utilities.getNextWord(target, dotPos);
                                                wsPos = (dotPos == eolPos) ? wsPos : Math.min(eolPos, wsPos);
                                            } else {
                                                int bolPos = Utilities.getRowStart(doc, dotPos);
                                                wsPos = Utilities.getPreviousWord(target, dotPos);
                                                wsPos = (dotPos == bolPos) ? wsPos : Math.max(bolPos, wsPos);
                                            }
                                        } else  {
                                            wsPos = (Integer) r[0];
                                        }

                                        if (isForward()) {
                                            moveToNewOffset(target, dotPos, wsPos - dotPos);
                                        } else {
                                            moveToNewOffset(target, wsPos, dotPos - wsPos);
                                        }
                                        result[0] = Boolean.TRUE;
                                    } catch (BadLocationException e) {
                                        target.getToolkit().beep();
                                    } finally {
                                        if (doesTypingModification()) {
                                            DocumentUtilities.setTypingModification(doc, false);
                                        }
                                    }
                                }
                            });

                            if(result[0].booleanValue()) {
                                t.afterChange();
                            }
                        }
                    } finally {
                        t.close();
                    }
                }
            }
        }

        protected abstract boolean isForward();
        protected boolean doesTypingModification() {
            return false;
        }
        protected abstract void moveToNewOffset(JTextComponent target, int offset, int length) throws BadLocationException;
        protected abstract void moveToNewOffset(CaretMoveContext context, CaretInfo caretInfo, int offset, int length) throws BadLocationException;
    }

    @EditorActionRegistration(name = BaseKit.removeNextWordAction)
    public static class RemoveWordNextAction extends CamelCaseAction {

        @Override
        protected boolean isForward() {
            return true;
        }

        @Override
        protected void moveToNewOffset(JTextComponent target, int offset, int length) throws BadLocationException {
            target.getDocument().remove(offset, length);
        }

        @Override
        protected void moveToNewOffset(CaretMoveContext context, CaretInfo caretInfo, int offset, int length) throws BadLocationException {
            context.getDocument().remove(offset, length);
        }

        @Override
        protected boolean doesTypingModification() {
            return true;
        }
    }

    @EditorActionRegistration(name = BaseKit.removePreviousWordAction)
    public static class RemoveWordPreviousAction extends CamelCaseAction {

        @Override
        protected boolean isForward() {
            return false;
        }

        @Override
        protected void moveToNewOffset(JTextComponent target, int offset, int length) throws BadLocationException {
            target.getDocument().remove(offset, length);
        }
        
        @Override
        protected void moveToNewOffset(CaretMoveContext context, CaretInfo caretInfo, int offset, int length) throws BadLocationException {            
            context.getDocument().remove(offset, length);
        }
        
        @Override
        protected boolean doesTypingModification() {
            return true;
        }
    }

    @EditorActionRegistration(name = DefaultEditorKit.nextWordAction)
    public static class NextCamelCasePosition extends CamelCaseAction {

        @Override
        protected boolean isForward() {
            return true;
        }

        @Override
        protected void moveToNewOffset(JTextComponent target, int offset, int length) throws BadLocationException {
            target.setCaretPosition(offset+length);
        }
        
        @Override
        protected void moveToNewOffset(CaretMoveContext context, CaretInfo caretInfo, int offset, int length) throws BadLocationException {            
            Position pos = context.getDocument().createPosition(offset + length);
            context.setDot(caretInfo, pos, Position.Bias.Forward);
        }
    }


    @EditorActionRegistration(name = DefaultEditorKit.previousWordAction)
    public static class PreviousCamelCasePosition extends CamelCaseAction {

        @Override
        protected boolean isForward() {
            return false;
        }

        @Override
        protected void moveToNewOffset(JTextComponent target, int offset, int length) throws BadLocationException {
            target.setCaretPosition(offset);
        }
        
        @Override
        protected void moveToNewOffset(CaretMoveContext context, CaretInfo caretInfo, int offset, int length) throws BadLocationException {            
            Position pos = context.getDocument().createPosition(offset);
            context.setDot(caretInfo, pos, Position.Bias.Forward);
        }
    }

    @EditorActionRegistration(name = DefaultEditorKit.selectionNextWordAction)
    public static class SelectNextCamelCasePosition extends CamelCaseAction {

        @Override
        protected boolean isForward() {
            return true;
        }

        protected void moveToNewOffset(JTextComponent target, int offset, int length) throws BadLocationException {
            Caret caret = target.getCaret();
            if (caret instanceof BaseCaret && RectangularSelectionUtils.isRectangularSelection(target)) {
                ((BaseCaret) caret).extendRectangularSelection(true, true);
            } else {
                target.getCaret().moveDot(offset + length);
            }
        }

        @Override
        protected void moveToNewOffset(CaretMoveContext context, CaretInfo caretInfo, int offset, int length) throws BadLocationException {            
            Position pos = context.getDocument().createPosition(offset + length);
            context.moveDot(caretInfo, pos, Position.Bias.Forward);
        }
    }

    @EditorActionRegistration(name = DefaultEditorKit.selectionPreviousWordAction)
    public static class SelectPreviousCamelCasePosition extends CamelCaseAction {
        @Override
        protected boolean isForward() {
            return false;
        }

        protected void moveToNewOffset(JTextComponent target, int offset, int length) throws BadLocationException {
            Caret caret = target.getCaret();
            if (caret instanceof BaseCaret && RectangularSelectionUtils.isRectangularSelection(target)) {
                ((BaseCaret) caret).extendRectangularSelection(false, true);
            } else {
                target.getCaret().moveDot(offset);
            }
        }
        
        @Override
        protected void moveToNewOffset(CaretMoveContext context, CaretInfo caretInfo, int offset, int length) throws BadLocationException {            
            Position pos = context.getDocument().createPosition(offset);
            context.moveDot(caretInfo, pos, Position.Bias.Forward);
        }
    }

}
