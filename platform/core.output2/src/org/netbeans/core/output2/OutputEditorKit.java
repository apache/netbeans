/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * OutputEditorKit.java
 *
 * Created on May 9, 2004, 4:34 PM
 */

package org.netbeans.core.output2;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.TextAction;
import org.openide.util.Exceptions;

/**
 * A simple editor kit which provides instances of ExtPlainView/ExtWrappedPlainView as its views.
 *
 * @author  Tim Boudreau
 */
final class OutputEditorKit extends DefaultEditorKit implements javax.swing.text.ViewFactory, ChangeListener {
    private final boolean wrapped;
    private final JTextComponent comp;
    private static final Action[] actions = prepareActions();
    private final PropertyChangeListener propertyChangeListener;

    /** Creates a new instance of OutputEditorKit */
    OutputEditorKit(boolean wrapped, JTextComponent comp,
            PropertyChangeListener propertyChangeListener) {
        this.comp = comp;
        this.wrapped = wrapped;
        this.propertyChangeListener = propertyChangeListener;        
    }
    
    @Override
    public Action[] getActions() {
        return actions;
    }
    
    public WrappedTextView view() {
        return lastWrappedView;
    }

    private WrappedTextView lastWrappedView = null;
    public javax.swing.text.View create(Element element) {
        javax.swing.text.View result = wrapped
                ? new WrappedTextView(element, comp, propertyChangeListener)
                : new ExtPlainView(element);
        lastWrappedView = wrapped ? (WrappedTextView) result : null;
        return result;
    }

    @Override
    public javax.swing.text.ViewFactory getViewFactory() {
        return this;
    }

    public boolean isWrapped() {
        return wrapped;
    }
    
    @Override
    public void install(JEditorPane c) {
        super.install(c);
        if (wrapped) {
            c.getCaret().addChangeListener(this);
        }
    }
    
    @Override
    public void deinstall(JEditorPane c) {
        super.deinstall(c);
        if (wrapped) {
            c.getCaret().removeChangeListener(this);
        }
    }    
    
    private int lastMark = -1;
    private int lastDot = -1;
    private static final Rectangle scratch = new Rectangle();
    
    /**
     * Manages repainting when the selection changes
     */
    public void stateChanged (ChangeEvent ce) {
        int mark = comp.getSelectionStart();
        int dot = comp.getSelectionEnd();
        boolean hasSelection = mark != dot;
        boolean hadSelection = lastMark != lastDot;
        
//        System.err.println("Change: " + mark + " : " + dot + "/" + lastMark + ":" + lastDot + " hadSelection " + hadSelection + " hasSelection " + hasSelection);
        
        if (lastMark != mark || lastDot != dot) {
            int begin = Math.min (mark, dot);
            int end = Math.max (mark, dot);
            int oldBegin = Math.min (lastMark, lastDot);
            int oldEnd = Math.max (lastMark, lastDot);
            
            if (hadSelection && hasSelection) {
                if (begin != oldBegin) {
                    int startChar = Math.min (begin, oldBegin);
                    int endChar = Math.max (begin, oldBegin);
                    repaintRange (startChar, endChar);
                } else {
                    int startChar = Math.min (end, oldEnd);
                    int endChar = Math.max (end, oldEnd);
                    repaintRange (startChar, endChar);
                }
            } else if (hadSelection && !hasSelection) {
                repaintRange (oldBegin, oldEnd);
            } 
            
        }
        lastMark = mark;
        lastDot = dot;
    }
    
    private void repaintRange (int start, int end) {
        try {
            Rectangle r = (Rectangle) view().modelToView(end, scratch, Position.Bias.Forward);
            int y1 = r.y + r.height;
            r = (Rectangle) view().modelToView(start, scratch, Position.Bias.Forward);
            r.x = 0;
            r.width = comp.getWidth();
            r.height = y1 - r.y;
//            System.err.println("RepaintRange " + start + " to " + end + ": " + r);
            comp.repaint (r);
        } catch (BadLocationException e) {
            comp.repaint();
            Exceptions.printStackTrace(e);
        }
    }
    
    /*
     * Replaces DefaultEditorKit actions which uses Utilities.getRowEnd()/getRowStart()
     * which are very expensive for long lines in unwrapped mode
     */
    static Action[] prepareActions() {
        DefaultEditorKit dek = new DefaultEditorKit();
        Action[] defActions = dek.getActions();
        Action[] newActions = new Action[defActions.length];
        for (int i = 0; i < defActions.length; i++) {
            Object actionName = defActions[i].getValue(Action.NAME);
            if (actionName.equals(beginLineAction)) {
                newActions[i] = new OutputBeginLineAction(beginLineAction, false);
            } else if (actionName.equals(selectionBeginLineAction)) {
                newActions[i] = new OutputBeginLineAction(selectionBeginLineAction, true);
            } else if (actionName.equals(endLineAction)) {
                newActions[i] = new OutputEndLineAction(endLineAction, false);
            } else if (actionName.equals(selectionEndLineAction)) {
                newActions[i] = new OutputEndLineAction(selectionEndLineAction, true);
            } else {
                newActions[i] = defActions[i];
            }            
        }
        return newActions;
    }    

    /*
     * Position the caret to the beginning of the line.
     * @see DefaultEditorKit#beginLineAction
     * @see DefaultEditorKit#selectBeginLineAction
     * @see DefaultEditorKit#getActions
     */
    static class OutputBeginLineAction extends TextAction {

        /** 
         * Create this action with the appropriate identifier. 
         * @param nm  the name of the action, Action.NAME.
         * @param select whether to extend the selection when
         *  changing the caret position.
         */
        OutputBeginLineAction(String nm, boolean select) {
            super(nm);
            this.select = select;
        }

        /** The operation to perform when this action is triggered. */
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                Document doc = target.getDocument();
                Element map = doc.getDefaultRootElement();
                int offs = target.getCaretPosition();
                int lineIndex = map.getElementIndex(offs);
                int lineStart = map.getElement(lineIndex).getStartOffset();

                if (select) {
                    target.moveCaretPosition(lineStart);
                } else {
                    target.setCaretPosition(lineStart);
                }
            }
        }

        private boolean select;
    }
    
    /*
     * Position the caret to the end of the line.
     * @see DefaultEditorKit#endLineAction
     * @see DefaultEditorKit#selectEndLineAction
     * @see DefaultEditorKit#getActions
     */
    static class OutputEndLineAction extends TextAction {

        /** 
         * Create this action with the appropriate identifier. 
         * @param nm  the name of the action, Action.NAME.
         * @param select whether to extend the selection when
         *  changing the caret position.
         */
        OutputEndLineAction(String nm, boolean select) {
            super(nm);
            this.select = select;
        }

        /** The operation to perform when this action is triggered. */
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                Document doc = target.getDocument();
                Element map = doc.getDefaultRootElement();
                int offs = target.getCaretPosition();
                int lineIndex = map.getElementIndex(offs);
                int lineEnd = map.getElement(lineIndex).getEndOffset() - 1;

                if (select) {
                    target.moveCaretPosition(lineEnd);
                } else {
                    target.setCaretPosition(lineEnd);
                }
            }            
        }

        private boolean select;
    }    
}
