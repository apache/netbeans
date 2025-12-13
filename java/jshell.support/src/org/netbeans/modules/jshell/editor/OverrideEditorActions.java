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
package org.netbeans.modules.jshell.editor;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.Action;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.EditorActionRegistrations;
import org.netbeans.editor.BaseAction;

import static javax.swing.text.DefaultEditorKit.beginLineAction;
import static javax.swing.text.DefaultEditorKit.selectionBeginLineAction;
import static javax.swing.text.DefaultEditorKit.upAction;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.undo.UndoManager;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;

import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.lib.editor.util.swing.PositionRegion;
import org.netbeans.modules.jshell.model.ConsoleModel;
import org.netbeans.modules.jshell.model.ConsoleSection;
import org.netbeans.modules.jshell.support.ShellSession;
import org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor;
import org.openide.awt.UndoRedo;
import org.openide.cookies.EditorCookie;
import org.openide.text.EditorSupport.Editor;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;

/**
 *
 * @author sdedic
 */
public class OverrideEditorActions {
    public static final String PROP_NAVIGATE_BOUNDARIES = "NetBeansEditor.navigateBoundaries"; // NOI18N

    /** Name of the action moving caret to the first column on the line */
    public static final String lineFirstColumnAction = "caret-line-first-column"; // NOI18N

    public static final String selectionLineFirstColumnAction = "selection-line-first-column"; // NOI18N

    public static class BeginLineAction extends BaseAction {

        @EditorActionRegistrations({
            @EditorActionRegistration(name = beginLineAction, mimeType = "text/x-repl"),
            @EditorActionRegistration(name = selectionBeginLineAction, mimeType = "text/x-repl")
        })
        public static BeginLineAction create() {
            return new BeginLineAction(false);
        }

        @EditorActionRegistrations({
            @EditorActionRegistration(name = lineFirstColumnAction, mimeType = "text/x-repl"),
            @EditorActionRegistration(name = selectionLineFirstColumnAction, mimeType = "text/x-repl")
        })
        public static BeginLineAction createColumnOne() {
            return new BeginLineAction(true);
        }

        /** Whether the action should go to the begining of
         * the text on the line or to the first column on the line*/
        boolean homeKeyColumnOne;

        static final long serialVersionUID =3269462923524077779L;

        public BeginLineAction(boolean columnOne) {
            super(MAGIC_POSITION_RESET | ABBREV_RESET | UNDO_MERGE_RESET
                  | WORD_MATCH_RESET | CLEAR_STATUS_TEXT);
            homeKeyColumnOne = columnOne;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                Caret caret = target.getCaret();
                BaseDocument doc = (BaseDocument)target.getDocument();
                try {
                    int dot = caret.getDot();
                    // #232675: if bounds are defined, use them rather than line start/end
                    Object o = target.getClientProperty(PROP_NAVIGATE_BOUNDARIES);
                    PositionRegion bounds = null;
                    int lineStartPos = Utilities.getRowStart(target, dot);
                    
                    if (o instanceof PositionRegion) {
                        bounds = (PositionRegion)o;
                        int start = bounds.getStartOffset();
                        int end = bounds.getEndOffset();
                        int boundLineStart = Utilities.getRowStart(target, start);
                        // refinement: only use the boundaries if the caret is at the same line
                        // as boundary start; otherwise ignore the boundary and use document lines.
                        if (boundLineStart == lineStartPos && dot > start && dot <= end) {
                            // move to the region start
                            dot = start;
                        } else {
                            bounds = null;
                        }
                    }
                    
                    if (bounds == null) {
                        if (homeKeyColumnOne) { // to first column
                            dot = lineStartPos;
                        } else { // either to line start or text start
                            int textStartPos = LineDocumentUtils.getLineFirstNonWhitespace(doc, lineStartPos);
                            if (textStartPos < 0) { // no text on the line
                                textStartPos = Utilities.getRowEnd(target, lineStartPos);
                            }
                            if (dot == lineStartPos) { // go to the text start pos
                                dot = textStartPos;
                            } else if (dot <= textStartPos) {
                                dot = lineStartPos;
                            } else {
                                dot = textStartPos;
                            }
                        }
                    }
                    // For partial view hierarchy check bounds
                    dot = Math.max(dot, target.getUI().getRootView(target).getStartOffset());
                    String actionName = (String) getValue(Action.NAME);
                    boolean select = selectionBeginLineAction.equals(actionName)
                            || selectionLineFirstColumnAction.equals(actionName);
                    
                    // If possible scroll the view to its begining horizontally
                    // to ease user's orientation in the code.
                    Rectangle r = target.modelToView(dot);
                    Rectangle visRect = target.getVisibleRect();
                    if (r.getMaxX() < visRect.getWidth()) {
                        r.x = 0;
                        target.scrollRectToVisible(r);
                    }
                    target.putClientProperty("navigational.action", SwingConstants.WEST);
                    if (select) {
                        caret.moveDot(dot);
                    } else {
                        caret.setDot(dot);
                    }
                } catch (BadLocationException e) {
                    target.getToolkit().beep();
                }
            }
        }
    }
    
    private static volatile Reference<EditorKit>    plainKitRef = new WeakReference<>(null);
    
    private static EditorKit   getBaseEditorKit() {
        Reference<EditorKit> r = plainKitRef;
        EditorKit ek = r.get();
        if (ek != null) {
            return ek;
        }
        ek = MimeLookup.getLookup(MimePath.parse("text/plain")).lookup(EditorKit.class);
        synchronized (OverrideEditorActions.class) {
            if (r == plainKitRef) {
                plainKitRef = new WeakReference<>(ek);
            }
        }
        return ek;
    }
    
    private static Action findDelegate(String id) {
        EditorKit ek = getBaseEditorKit();
        if (ek == null) {
            return null;
        }
        if (ek instanceof BaseKit) {
            BaseKit bk = (BaseKit)ek;
            return bk.getActionByName(id);
        } else {
            // PENDING: find the action by ID from the Action[] map-array.
            return null;
        }
    }
    
    
    /**
     * Basis for editor actions which modify behaviour in the console.
     * They delegate to some action for the standard implementation, and
     * divert under certain conditions (i.e. caret in the editable area etc)
     */
    public abstract static class DelegatingAction extends BaseAction {
        /**
         * Action ID to delegate, by default the action ID itself
         */
        private final String    delegateID;
        
        public DelegatingAction(String name, int updateMask) {
            super(name, updateMask);
            this.delegateID = name;
        }

        public DelegatingAction(String name, int updateMask, String delegateID) {
            super(name, updateMask);
            this.delegateID = delegateID;
        }
        
        public final String getDelegateId() {
            return delegateID;
        }

        @Override
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (delegates(target)) {
                delegateActionPerformed(evt, target);
            } else {
                performAction(evt, target);
            }
        }
        
        private void delegateActionPerformed(ActionEvent evt, JTextComponent target) {
            Action del = findDelegate(getDelegateId());
            del.actionPerformed(evt);
        }
        
        protected abstract void performAction(ActionEvent evt, JTextComponent target);
        

        protected boolean delegates(JTextComponent target) {
            ConsoleModel model = ConsoleModel.get(target.getDocument());
            if (model == null) {
                return true;
            }
            ConsoleSection input = model.getInputSection();
            if (input == null) {
                return true;
            }
            int offset = target.getCaretPosition();
            if (offset < input.getPartBegin() || offset > input.getEnd()) {
                return true;
            }
            Document doc = target.getDocument();
            
            LineDocument ld = LineDocumentUtils.as(doc, LineDocument.class);
            if (ld == null) {
                return true;
            }
            try {
                int end = LineDocumentUtils.getLineEndOffset(ld, input.getPartBegin());
                return offset > end;
            } catch (BadLocationException ex) {
                return true;
            }
        }
        
        protected boolean delegates(JTextComponent target, PositionRegion region, int pos) {
            return region.getStartOffset() > pos && region.getEndOffset() <= pos;
        }
    }
    
    @EditorActionRegistrations({
        @EditorActionRegistration(name = upAction, mimeType = "text/x-repl")
    })
    public static class UpAndHistory extends DelegatingAction {
        public UpAndHistory() {
            super(upAction, ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET | CLEAR_STATUS_TEXT);
        }

        @Override
        protected void performAction(ActionEvent evt, JTextComponent target) {
            // bring up a history
            target.putClientProperty("history-completion", true);
            Completion.get().showCompletion();
        }

        @Override
        protected boolean delegates(JTextComponent target, PositionRegion region, int pos) {
            if (super.delegates(target, region, pos)) {
                return true;
            }
            LineDocument ld = LineDocumentUtils.as(target.getDocument(), LineDocument.class);
            if (ld == null) {
                return true;
            }
            try {
                int end = LineDocumentUtils.getLineEndOffset(ld, region.getStartOffset());
                // delegate for all but the first line
                return pos > end;
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
                return true;
            }
        }
    }

    public static class ExecuteInterceptor implements TypedBreakInterceptor {
        private boolean execute;
        
        @Override
        public boolean beforeInsert(Context context) throws BadLocationException {
            return false;
        }

        @Override
        public void insert(MutableContext context) throws BadLocationException {
        }

        /**
         * The possible execution status is checked after the insert, as various
         * typing hooks may insert e.g. braces
         */
        @Override
        public void afterInsert(Context context) throws BadLocationException {
            Document doc = context.getDocument();
            ShellSession session = ShellSession.get(doc);
            if (session == null) {
                return;
            }
            ConsoleModel mod = session.getModel();
            ConsoleSection sec = mod.processInputSection(true);
            // the interceptor is executed even if a break insertion fails, e.g. is
            // filtered out by DocumentFilter. Accept and process only those inserts,
            // which happen in the input section
            if (sec == null || sec.getPartBegin() > context.getBreakInsertOffset()) {
                return;
            }
            
            context.getDocument().render(() -> {
                execute = false;
                if (mod == null) {
                    return;
                }
                if (sec == null || sec.isIncomplete()) {
                    return;
                }
                if (sec.getContents(doc).trim().isEmpty()) {
                    return;
                }
                if (context.getBreakInsertOffset() >= sec.getEnd()) {
                    execute = true;
                } else {
                    String s = sec.getContents(doc).substring(sec.offsetToContents(context.getCaretOffset(), true)).trim();
                    execute = s.isEmpty();
                }
            });
            if (execute) {
                flushUndoQueue(doc);
                try {
                    ShellSession.get(context.getDocument()).evaluate(null);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    return;
                }
            }
        }

        @Override
        public void cancelled(Context context) {
        }
    }
    
    @MimeRegistrations({
        @MimeRegistration(service = TypedBreakInterceptor.Factory.class, mimeType = "text/x-repl/text/x-java"),
        @MimeRegistration(service = TypedBreakInterceptor.Factory.class, mimeType = "text/x-repl")
    })
    public static final class ExecF implements TypedBreakInterceptor.Factory {

        @Override
        public TypedBreakInterceptor createTypedBreakInterceptor(MimePath mimePath) {
            return new ExecuteInterceptor();
        }
    }
    
    static void flushUndoQueue(Document d) {
        SwingUtilities.invokeLater(() -> {
        if (d == null) {
            return;
        }
        for (TopComponent tc : TopComponent.getRegistry().getOpened()) {
            if (!(tc instanceof ConsoleEditor)) {
                continue;
            }
            ConsoleEditor cake = (ConsoleEditor)tc;
            if (cake.getEditorPane() == null) {
                continue;
            }
            Document check = cake.getEditorPane().getDocument();
            if (check != d) {
                continue;
            }
            UndoRedo ur = tc.getUndoRedo();
            if (ur instanceof UndoManager) {
                ((UndoManager)ur).discardAllEdits();
            }
        }});
    }
    
    public static final String JSHELL_EXECUTE = "jshell-execute"; // NOI18N
    
    @EditorActionRegistrations({
        @EditorActionRegistration(name = JSHELL_EXECUTE, mimeType = "text/x-repl"),
        @EditorActionRegistration(name = JSHELL_EXECUTE, mimeType = "text/x-repl/text/x-java")
    })
    public static final class ForceExecute extends BaseAction {

        public ForceExecute() {
            super(JSHELL_EXECUTE, MAGIC_POSITION_RESET | UNDO_MERGE_RESET
                  | WORD_MATCH_RESET | CLEAR_STATUS_TEXT);
        }
        
        @Override
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target == null || target.getDocument() == null) {
                return;
            }
            Document doc = target.getDocument();
            AtomicLockDocument ald = LineDocumentUtils.asRequired(doc, AtomicLockDocument.class);
            
            ShellSession s = ShellSession.get(doc);
            if (s == null) {
                return;
            }
            ConsoleModel mod = s.getModel();
            ConsoleSection sec = mod.processInputSection(true);
            if (sec == null || sec.isIncomplete()) {
                return;
            }
            flushUndoQueue(doc);
            try {
                s.evaluate(null);
                target.setCaretPosition(doc.getLength());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
    }
}
