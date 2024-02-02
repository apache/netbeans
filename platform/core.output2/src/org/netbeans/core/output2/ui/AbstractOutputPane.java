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

package org.netbeans.core.output2.ui;

import javax.swing.plaf.TextUI;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import org.netbeans.core.output2.FoldingSideBar;
import org.netbeans.core.output2.Lines;
import org.netbeans.core.output2.OutputDocument;
import org.netbeans.core.output2.options.OutputOptions;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * A scroll pane containing an editor pane, with special handling of the caret
 * and scrollbar - until a keyboard or mouse event, after a call to setDocument(),
 * the caret and scrollbar are locked to the last line of the document.  This avoids
 * "jumping" scrollbars as the position of the caret (and thus the scrollbar) get updated
 * to reposition them at the bottom of the document on every document change.
 *
 * @author  Tim Boudreau
 */
public abstract class AbstractOutputPane extends JScrollPane implements DocumentListener, MouseListener, MouseMotionListener, KeyListener, ChangeListener, MouseWheelListener, Runnable {
    private boolean locked = true;
    
    private int fontHeight = -1;
    private int fontWidth = -1;
    protected JEditorPane textView;
    private FoldingSideBar foldingSideBar;
    int lastCaretLine = 0;
    int caretBlinkRate = 500;
    boolean hadSelection = false;
    boolean recentlyReset = false;
    private static boolean copyingOfLargeParts = false;

    public AbstractOutputPane() {
        textView = createTextView();
        init();
    }

    //#114290
    public void doUpdateCaret() {
        Caret car = textView.getCaret();
        if (car instanceof DefaultCaret) {
            ((DefaultCaret)car).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        }
    }

    public void dontUpdateCaret() {
        Caret car = textView.getCaret();
        if (car instanceof DefaultCaret) {
            ((DefaultCaret)car).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }
    }

    @Override
    public void requestFocus() {
        textView.requestFocus();
    }
    
    @Override
    public boolean requestFocusInWindow() {
        return textView.requestFocusInWindow();
    }

    public Font getViewFont() {
        return textView.getFont();
    }

    public void setViewFont (Font f) {
        textView.setFont(f);
        updateFont(getGraphics());
    }

    protected abstract JEditorPane createTextView();

    protected void documentChanged() {
        lastLength = -1;
        if (lineToScroll != -1) {
            if (scrollToLine(lineToScroll)) {
                lineToScroll = -1;
            }
        } else {
            ensureCaretPosition();
        }

        if (recentlyReset && isShowing()) {
            recentlyReset = false;
        }
        if (locked) {
            resetCursor();
        }
        if (isWrapped()) {
            //Saves having OutputEditorKit have to do its own listening
            getViewport().revalidate();
            getViewport().repaint();
        }
    }
    
    public abstract boolean isWrapped();
    public abstract void setWrapped (boolean val);

    public boolean hasSelection() {
        return textView.getSelectionStart() != textView.getSelectionEnd();
    }

    public boolean isScrollLocked() {
        return locked;
    }

    /**
     * Ensure that the document is scrolled all the way to the bottom (unless
     * some user event like scrolling or placing the caret has unlocked it).
     * <p>
     * Note that this method is always called on the event queue, since 
     * OutputDocument only fires changes on the event queue.
     */
    public final void ensureCaretPosition() {
        if (!enqueued) {
            //Make sure the scrollbar is updated *after* the document change
            //has been processed and the scrollbar model's maximum updated
            enqueued = true;
            SwingUtilities.invokeLater(this);
        }
    }
    
    /** True when invokeLater has already been called on this instance */
    private boolean enqueued = false;
    /**
     * Scrolls the pane to the bottom, invokeLatered to ensure all state has
     * been updated, so the bottom really *is* the bottom.
     */
    @Override
    public void run() {
        enqueued = false;
        if (locked) {
            getVerticalScrollBar().setValue(getVerticalScrollBar().getModel().getMaximum());
            getHorizontalScrollBar().setValue(getHorizontalScrollBar().getModel().getMinimum());
        }
        ensureCaretAtVisiblePosition();
    }

    /**
     * Ensure that the caret is at a visible position, not inside a collapsed
     * fold. If not, move it up to nearest visible line above the current
     * (hidden) line.
     */
    private void ensureCaretAtVisiblePosition() {
        assert EventQueue.isDispatchThread();
        final Lines lines = getLines();
        if (lines != null) {
            int caretLine = lines.getLineAt(getCaretPos());
            int origCaretLine = caretLine;
            while (caretLine >= 0 && !lines.isVisible(caretLine)) {
                caretLine = lines.getParentFoldStart(caretLine);
            }
            if (caretLine != origCaretLine && caretLine >= 0) {
                getCaret().setDot(lines.getLineStart(caretLine));
            }
        }
    }

    public int getSelectionStart() {
        return textView.getSelectionStart();
    }
    
    public int getSelectionEnd() {
        return textView.getSelectionEnd();
    }

    public String getSelectedText() {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        String str = null;
        if (start > 0 && end > start) {
            try {
                str = getDocument().getText(start, end - start);
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }
        return str;
    }

    public void setSelection (int start, int end) {
        int rstart = Math.min (start, end);
        int rend = Math.max (start, end);
        if (rstart == rend) {
            getCaret().setDot(rstart);
        } else {
            textView.setSelectionStart(rstart);
            textView.setSelectionEnd(rend);
        }
    }

    @NbBundle.Messages({
        "MSG_TooMuchTextSelected=Selecting large parts of text can cause "
                + "Out-Of-Memory errors. Do you want to continue?"
    })
    public void selectAll() {
        unlockScroll();
        getCaret().setVisible(true);
        int start = 0;
        int end = getLength();
        if (end - start > 20000000 && !copyingOfLargeParts) { // 40 MB
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                    Bundle.MSG_TooMuchTextSelected(),
                    NotifyDescriptor.YES_NO_OPTION);
            Object result = DialogDisplayer.getDefault().notify(nd);
            if (result == NotifyDescriptor.YES_OPTION) {
                copyingOfLargeParts = true;
            } else {
                return;
            }
        }
        textView.setSelectionStart(start);
        textView.setSelectionEnd(end);
    }

    public boolean isAllSelected() {
        return textView.getSelectionStart() == 0 && textView.getSelectionEnd() == getLength();
    }

    protected void init() {
        setRowHeaderView(foldingSideBar = new FoldingSideBar(textView, this));
        setViewportView(textView);
        textView.setEditable(false);

        textView.addMouseListener(this);
        textView.addMouseMotionListener(this);
        textView.addKeyListener(this);
        textView.addMouseWheelListener(this);
        //#107354
        OCaret oc = new OCaret();
        oc.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        textView.setCaret (oc);
        
        getCaret().setSelectionVisible(true);
        
        getVerticalScrollBar().getModel().addChangeListener(this);
        getVerticalScrollBar().addMouseMotionListener(this);
        
        getViewport().addMouseListener(this);
        getVerticalScrollBar().addMouseListener(this);
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
        addMouseListener(this);

        getCaret().addChangeListener(this);
        textView.setFont(OutputOptions.getDefault().getFont(isWrapped()));
        setBorder (BorderFactory.createEmptyBorder());
        setViewportBorder (BorderFactory.createEmptyBorder());
        
        Color c = UIManager.getColor("nb.output.selectionBackground");
        if (c != null) {
            textView.setSelectionColor(c);
        }
    }

    /**
     * Accessed reflectively from org.netbeans.jellytools.OutputTabOperator.
     */
    public final Document getDocument() {
        return textView.getDocument();
    }
    
    /**
     * This method is here for use *only* by unit tests.
     */
    public final JTextComponent getTextView() {
        return textView;
    }

    public final FoldingSideBar getFoldingSideBar() {
        return foldingSideBar;
    }

    public final void copy() {
        if (getCaret().getDot() != getCaret().getMark()) {
            textView.copy();
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    public final void paste() {
        textView.paste();
    }

    protected void setDocument (Document doc) {
        if (hasSelection()) {
            hasSelectionChanged(false);
        }
        hadSelection = false;
        lastCaretLine = 0;
        lastLength = -1;
        lineToScroll = -1;
        Document old = textView.getDocument();
        old.removeDocumentListener(this);
        if (doc != null) {
            textView.setDocument(doc);
            doc.addDocumentListener(this);
            lockScroll();
            recentlyReset = true;
        } else {
            textView.setDocument (new PlainDocument());
            textView.setEditorKit(new DefaultEditorKit());
        }
    }
    
    protected void setEditorKit(EditorKit kit) {
        Document doc = textView.getDocument();
        
        textView.setEditorKit(kit);
        textView.setDocument(doc);
        updateKeyBindings();
    }
    
    /**
     * Setting the editor kit will clear the action map/key map connection
     * to the TopComponent, so we reset it here.
     */
    protected final void updateKeyBindings() {
        Keymap keymap = textView.getKeymap();
        keymap.removeKeyStrokeBinding(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
    }
    
    protected EditorKit getEditorKit() {
        return textView.getEditorKit();
    }
    
    public final int getLineCount() {
        return textView.getDocument().getDefaultRootElement().getElementCount();
    }

    private int lastLength = -1;
    public final int getLength() {
        if (lastLength == -1) {
            lastLength = textView.getDocument().getLength();
        }
        return lastLength;
    }

    public void scrollTo(int pos) {
        getCaret().setDot(pos);
        try {
            Rectangle rect = textView.modelToView(pos);
            if (rect != null) {
                int spaceAround
                        = (textView.getVisibleRect().height - rect.height) / 2;
                Rectangle centeredRect = new Rectangle(
                        rect.x, Math.max(0, rect.y - spaceAround + rect.height),
                        rect.width, spaceAround * 2 + rect.height);
                textView.scrollRectToVisible(centeredRect);
            }
            locked = false;
        } catch (BadLocationException ex) {
        }
    }

    public final void sendCaretToPos(int startPos, int endPos, boolean select) {
        sendCaretToPos(startPos, endPos, select, true);
    }

    public final void sendCaretToPos(int startPos, int endPos, boolean select, boolean scroll) {
        inSendCaretToLine = true;
        try {
            getCaret().setVisible(true);
            getCaret().setSelectionVisible(true);
            if (select) {
                if (scroll) {
                    scrollTo(endPos);
                }
                getCaret().setDot(endPos);
                getCaret().moveDot(startPos);
                textView.repaint();
            } else {
                getCaret().setDot(startPos);
            }
        } catch (Error sie) {
            if (sie.getClass().getName().equals("javax.swing.text.StateInvariantError")) {
                Exceptions.attachMessage(sie, "sendCaretToPos("+startPos+", "+endPos+", "+select+"), caret = "+getCaret()+", highlighter = "+textView.getHighlighter()+", document length = "+textView.getDocument().getLength());
            }
            Exceptions.printStackTrace(sie);
        } finally {
            locked = false;
            inSendCaretToLine = false;
        }
    }

    private boolean inSendCaretToLine = false;
    private int lineToScroll = -1;

    public final boolean sendCaretToLine(int idx, boolean select) {
        return sendCaretToLine(idx, select, true);
    }

    public final boolean sendCaretToLine(int idx, boolean select, boolean scroll) {
        int lastLine = getLineCount() - 1;
        if (idx > lastLine) {
            idx = lastLine;
        }
        inSendCaretToLine = true;
        try {
            getCaret().setVisible(true);
            getCaret().setSelectionVisible(true);
            Element el = textView.getDocument().getDefaultRootElement().getElement(idx);
            int position = el.getStartOffset();
            if (select) {
                getCaret().setDot(el.getEndOffset() - 1);
                getCaret().moveDot(position);
                textView.repaint();
            } else {
                getCaret().setDot(position);
            }

            if (scroll && !scrollToLine(idx + 3) && isScrollLocked()) {
                lineToScroll = idx + 3;
            }
        } catch (Error sie) {
            if (sie.getClass().getName().equals("javax.swing.text.StateInvariantError")) {
                Exceptions.attachMessage(sie, "sendCaretToLine("+idx+", "+select+"), caret = "+getCaret()+", highlighter = "+textView.getHighlighter()+", document length = "+textView.getDocument().getLength());
            }
            Exceptions.printStackTrace(sie);
        } finally {
            locked = false;
            inSendCaretToLine = false;
        }
        return true;
    }

    boolean scrollToLine(int line) {
        int lineIdx = Math.min(getLineCount() - 1, line);
        Rectangle rect = null;
        try {
            rect = textView.modelToView(textView.getDocument().getDefaultRootElement().getElement(lineIdx).getStartOffset());
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (rect == null) {
            return false;
        }

        boolean oldLocked = locked;
        textView.scrollRectToVisible(rect);
        locked = oldLocked;

        Rectangle visRect = textView.getVisibleRect();
        return line == lineIdx && visRect.y + visRect.height == rect.y + rect.height;
    }

    public final void lockScroll() {
        if (!locked) {
            locked = true;
        }
    }
    
    public final void unlockScroll() {
        if (locked) {
            locked = false;
        }
        lineToScroll = -1;
    }

    protected abstract void caretPosChanged(int pos);
    
    protected abstract void lineClicked (int line, int pos);
    
    protected abstract void enterPressed();

    protected abstract void postPopupMenu (Point p, Component src);
    
    public final int getCaretLine() {
        int result = 0;
        int charPos = getCaret().getDot();
        if (charPos > 0) {
            result = textView.getDocument().getDefaultRootElement().getElementIndex(charPos);
        }
        return result;
    }

    public final boolean isLineSelected(int idx) {
        Element line = textView.getDocument().getDefaultRootElement().getElement(idx);
        return line.getStartOffset() == getSelectionStart() && line.getEndOffset()-1 == getSelectionEnd();
    }

    public final int getCaretPos() {
        return getCaret().getDot();
    }

    @Override
    public final void paint (Graphics g) {
        if (fontHeight == -1) {
            updateFont(g);
        }
        super.paint(g);
    }

    void updateFont(Graphics g) {
        if (g == null) {
            fontHeight = fontWidth = -1;
            return;
        }
        fontHeight = g.getFontMetrics(textView.getFont()).getHeight();
        fontWidth = g.getFontMetrics(textView.getFont()).charWidth('m'); //NOI18N
        getVerticalScrollBar().setUnitIncrement(fontHeight);
        getHorizontalScrollBar().setUnitIncrement(fontWidth);
    }

//***********************Listener implementations*****************************

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == getVerticalScrollBar().getModel()) {
            if (!locked) { //XXX check if doc is still being written?
                BoundedRangeModel mdl = getVerticalScrollBar().getModel();
                if (mdl.getValue() + mdl.getExtent() == mdl.getMaximum()) {
                    lockScroll();
                }
            }
        } else {
            if (!locked) {
                maybeSendCaretEnteredLine();
            }
            boolean hasSelection = textView.getSelectionStart() != textView.getSelectionEnd();
            if (hasSelection != hadSelection) {
                hadSelection = hasSelection;
                hasSelectionChanged (hasSelection);
            }
        }
    }

    private void maybeSendCaretEnteredLine() {
        if (EventQueue.getCurrentEvent() instanceof MouseEvent) {
            //User may have clicked a hyperlink, in which case, we'll test
            //it and see if it's really in the text of the hyperlink - so
            //don't do anything here
            return;
        }
        //Don't message the controller if we're programmatically setting
        //the selection, or if the caret moved because output was written - 
        //it can cause the controller to send events to OutputListeners which
        //should only happen for user events
        if (!locked && !inSendCaretToLine) {
            boolean sel = textView.getSelectionStart() != textView.getSelectionEnd();
            if (!sel) {
                caretPosChanged(getCaretPos());
            }
            if (sel != hadSelection) {
                hadSelection = sel;
                hasSelectionChanged (sel);
            }
        }
    }


    private void hasSelectionChanged(boolean sel) {
        AbstractOutputTab parent = (AbstractOutputTab) getParent();
        if (parent != null) { // #243686
            parent.hasSelectionChanged(sel);
        }
    }

    @Override
    public final void changedUpdate(DocumentEvent e) {
        //Ensure it is consumed
        e.getLength();
        documentChanged();
        if (e.getOffset() + e.getLength() >= getCaretPos() && (locked || !(e instanceof OutputDocument.DO))) {
            //#119985 only move caret when not in editable section
            OutputDocument doc = (OutputDocument)e.getDocument();
            if (! (e instanceof OutputDocument.DO) && getCaretPos() >= doc.getOutputLength()) {
                return ;
            }
            
            getCaret().setDot(e.getOffset() + e.getLength());
        }
    }

    @Override
    public final void insertUpdate(DocumentEvent e) {
        //Ensure it is consumed
        e.getLength();
        documentChanged();
        if (e.getOffset() + e.getLength() >= getCaretPos() && (locked || !(e instanceof OutputDocument.DO))) {
            //#119985 only move caret when not in editable section
            OutputDocument doc = (OutputDocument)e.getDocument();
            if (! (e instanceof OutputDocument.DO) && getCaretPos() >= doc.getOutputLength()) {
                return ;
            }
            
            getCaret().setDot(e.getOffset() + e.getLength());
        }
    }

    @Override
    public final void removeUpdate(DocumentEvent e) {
        //Ensure it is consumed
        e.getLength();
        documentChanged();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.isAltDown() || e.isAltGraphDown() || e.isControlDown()
                && SwingUtilities.isMiddleMouseButton(e)) {
            int currentSize = getViewFont().getSize();
            int defaultSize = OutputOptions.getDefaultFont().getSize();
            changeFontSizeBy(defaultSize - currentSize);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        resetCursor();
    }

    boolean isOnHyperlink(Point p) {
        Document doc = getDocument();
        if (doc instanceof OutputDocument) {
            int pos = textView.viewToModel(p);
            if (pos >= getLength()) {
                return false;
            }
            int line = getDocument().getDefaultRootElement().getElementIndex(pos);
            int lineStart = getDocument().getDefaultRootElement().getElement(line).getStartOffset();
            int lineLength = getDocument().getDefaultRootElement().getElement(line).getEndOffset() - lineStart;
            try {
                Rectangle r = textView.modelToView(lineStart + lineLength - 1);
                boolean onLine = p.x <= r.x + r.width || (isWrapped() && p.y < r.y);
                if (onLine) {
                    return ((OutputDocument) doc).getLines().getListener(pos, null) != null;
                }
            } catch (BadLocationException ex) {
            }
        }
        return false;
    }

    private void resetCursor() {
        Cursor txtCursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
        if (textView.getCursor() != txtCursor) {
            textView.setCursor(txtCursor);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (e.getSource() == textView) {
            if (isOnHyperlink(e.getPoint())) {
                Cursor hand = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
                if (textView.getCursor() != hand) {
                    textView.setCursor(hand);
                }
            } else {
                resetCursor();
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (e.getSource() == getVerticalScrollBar()) {
            int y = e.getY();
            if (y > getVerticalScrollBar().getHeight()) {
                lockScroll();
            }
        }
    }

    /** last pressed position for hyperlink test */
    private int lastPressedPos = -1;

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getSource() == textView && SwingUtilities.isLeftMouseButton(e)) {
            lastPressedPos = textView.viewToModel(e.getPoint());
        }
        if (locked && !e.isPopupTrigger()) {
            Element el = getDocument().getDefaultRootElement().getElement(getLineCount()-1);
            getCaret().setDot(el.getStartOffset());
            unlockScroll();
            //We should now set the caret position so the caret doesn't
            //seem to ignore the first click
            if (e.getSource() == textView) {
                getCaret().setDot (textView.viewToModel(e.getPoint()));
            }
        }
        if (e.isPopupTrigger()) {
            //Convert immediately to our component space - if the 
            //text view scrolls before the component is opened, popup can
            //appear above the top of the screen
            Point p = SwingUtilities.convertPoint((Component) e.getSource(), 
                e.getPoint(), this);
            
            postPopupMenu (p, this);
        }
    }

    @Override
    public final void mouseReleased(MouseEvent e) {
        if (e.getSource() == textView && SwingUtilities.isLeftMouseButton(e)) {
            int pos = textView.viewToModel(e.getPoint());
            if (pos != -1 && pos == lastPressedPos) {
                int line = textView.getDocument().getDefaultRootElement().getElementIndex(pos);
                if (line >= 0) {
                    lineClicked(line,pos);
                    e.consume(); //do NOT allow this window's caret to steal the focus from editor window
                }
            }
            lastPressedPos = -1;
        }
        if (e.isPopupTrigger()) {
            Point p = SwingUtilities.convertPoint((Component) e.getSource(), 
            //Convert immediately to our component space - if the 
            //text view scrolls before the component is opened, popup can
            //appear above the top of the screen
                e.getPoint(), this);
            
            postPopupMenu (p, this);
        }
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_END:
                if (keyEvent.isControlDown()) {
                    lockScroll();
                }
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_HOME:
            case KeyEvent.VK_PAGE_UP:
            case KeyEvent.VK_PAGE_DOWN:
                unlockScroll();
                break;
            case KeyEvent.VK_ENTER:
                enterPressed();
                break;
            case KeyEvent.VK_C:
                if (keyEvent.isControlDown()) {
                    askTerminate(keyEvent); // see bug 250245
                }
                break;
        }
    }

    @NbBundle.Messages({
        "MSG_TerminateProcess=Terminate the process?"
    })
    private void askTerminate(KeyEvent keyEvent) {
        Container parent = getParent();
        if (parent instanceof AbstractOutputTab) {
            Caret c = getCaret();
            if (c.getDot() != c.getMark()) {
                return; // some text is selected, copy action will handle this
            }
            AbstractOutputTab tab = (AbstractOutputTab) parent;
            Action[] actions = tab.getToolbarActions();
            for (Action a : actions) {
                if ("stop".equals(a.getValue("OUTPUT_ACTION_TYPE"))     //NOI18N
                        && a.isEnabled()) {
                    NotifyDescriptor desc = new NotifyDescriptor.Confirmation(
                            Bundle.MSG_TerminateProcess(),
                            NotifyDescriptor.YES_NO_OPTION);
                    Object res = DialogDisplayer.getDefault().notify(desc);
                    if (NotifyDescriptor.YES_OPTION.equals(res)) {
                        a.actionPerformed(
                                new ActionEvent(this, 0, "stop"));      //NOI18N
                    }
                    keyEvent.consume();
                    break;
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
    }

    protected abstract void changeFontSizeBy(int change);

    @Override
    public final void mouseWheelMoved(MouseWheelEvent e) {
        if (e.isAltDown() || e.isAltGraphDown() || e.isControlDown()) {
            int change = -e.getWheelRotation();
            changeFontSizeBy(change);
            e.consume();
            return;
        } else if (e.isShiftDown()) { // horizontal scrolling
            BoundedRangeModel sbmodel = getHorizontalScrollBar().getModel();
            int currPosition = sbmodel.getValue();
            int newPosition = Math.max(0, Math.min(sbmodel.getMaximum(), currPosition
                    + (int) (e.getPreciseWheelRotation() * e.getScrollAmount() * fontWidth)));
            sbmodel.setValue (newPosition);
            return;
        }
        BoundedRangeModel sbmodel = getVerticalScrollBar().getModel();
        int max = sbmodel.getMaximum();
        int range = sbmodel.getExtent();

        int currPosition = sbmodel.getValue();
        if (e.getSource() == textView) {
            int newPosition = Math.max(0, Math.min(sbmodel.getMaximum(), currPosition
                    + (int) (e.getPreciseWheelRotation() * e.getScrollAmount() * fontHeight)));
            // height is a magic constant because of #57532
            sbmodel.setValue (newPosition);
            if (newPosition + range >= max) {
                lockScroll();
                return;
            }
        }
        unlockScroll();
    }

    Caret getCaret() {
        return textView.getCaret();
    }
    
    public void collapseFold() {
        Lines l = getLines();
        if (l != null) {
            l.hideFold(l.getFoldStart(l.visibleToRealLine(getCaretLine())));
        }
    }

    public void expandFold() {
        Lines l = getLines();
        if (l != null) {
            l.showFold(l.getFoldStart(l.visibleToRealLine(getCaretLine())));
        }
    }

    public void collapseAllFolds() {
        Lines l = getLines();
        if (l != null) {
            l.hideAllFolds();
        }
    }

    public void expandAllFolds() {
        Lines l = getLines();
        if (l != null) {
            l.showAllFolds();
        }
    }

    public void collapseFoldTree() {
        Lines l = getLines();
        if (l != null) {
            l.hideFoldTree(l.getFoldStart(l.visibleToRealLine(getCaretLine())));
        }
    }

    public void expandFoldTree() {
        Lines l = getLines();
        if (l != null) {
            l.showFoldTree(l.getFoldStart(l.visibleToRealLine(getCaretLine())));
        }
    }

    private Lines getLines() {
        Document d = getDocument();
        if (d instanceof OutputDocument) {
            return ((OutputDocument) d).getLines();
        }
        return null;
    }

    private class OCaret extends DefaultCaret {
        @Override
        public void paint(Graphics g) {
            JTextComponent component = textView;
            if(isVisible() && y >= 0) {
                try {
                    TextUI mapper = component.getUI();
                    Rectangle r = mapper.modelToView(component, getDot(), Position.Bias.Forward);

                    if ((r == null) || ((r.width == 0) && (r.height == 0))) {
                        return;
                    }
                    if (width > 0 && height > 0 &&
                                    !this._contains(r.x, r.y, r.width, r.height)) {
                        // We seem to have gotten out of sync and no longer
                        // contain the right location, adjust accordingly.
                        Rectangle clip = g.getClipBounds();

                        if (clip != null && !clip.contains(this)) {
                            // Clip doesn't contain the old location, force it
                            // to be repainted lest we leave a caret around.
                            repaint();
                        }
 //                       System.err.println("WRONG! Caret dot m2v = " + r + " but my bounds are " + x + "," + y + "," + width + "," + height);
                        
                        // This will potentially cause a repaint of something
                        // we're already repainting, but without changing the
                        // semantics of damage we can't really get around this.
                        damage(r);
                    }
                    g.setColor(component.getCaretColor());
                    g.drawLine(r.x, r.y, r.x, r.y + r.height - 1);
                    g.drawLine(r.x+1, r.y, r.x+1, r.y + r.height - 1);

                } catch (BadLocationException e) {
                    // can't render I guess
//                    System.err.println("Can't render cursor");
                }
            }
        }
        
        private boolean _contains(int X, int Y, int W, int H) {
            int w = this.width;
            int h = this.height;
            if ((w | h | W | H) < 0) {
                // At least one of the dimensions is negative...
                return false;
            }
            // Note: if any dimension is zero, tests below must return false...
            int x = this.x;
            int y = this.y;
            if (X < x || Y < y) {
                return false;
            }
            if (W > 0) {
                w += x;
                W += X;
                if (W <= X) {
                    // X+W overflowed or W was zero, return false if...
                    // either original w or W was zero or
                    // x+w did not overflow or
                    // the overflowed x+w is smaller than the overflowed X+W
                    if (w >= x || W > w) {
                        return false;
                    }
                } else {
                    // X+W did not overflow and W was not zero, return false if...
                    // original w was zero or
                    // x+w did not overflow and x+w is smaller than X+W
                    if (w >= x && W > w) {
                        //This is the bug in DefaultCaret - returns false here
                        return true;
                    }
                }
            }
            else if ((x + w) < X) {
                return false;
            }
            if (H > 0) {
                h += y;
                H += Y;
                if (H <= Y) {
                    if (h >= y || H > h) return false;
                } else {
                    if (h >= y && H > h) return false;
                }
            }
            else if ((y + h) < Y) {
                return false;
            }
            return true;
        }        

        @Override
        public void mouseReleased(MouseEvent e) {
            if( !e.isConsumed() ) {
                super.mouseReleased(e);
            }
        }

        @Override
        public void focusGained(FocusEvent e) {
            getCaret().setBlinkRate(caretBlinkRate);
            getCaret().setVisible(true);
        }

        @Override
        public void focusLost(FocusEvent e) {
            getCaret().setVisible(false);
        }

        @Override
        public void setSelectionVisible(boolean vis) {
            if (vis) {
                super.setSelectionVisible(vis);
            }
        }
    }
}
