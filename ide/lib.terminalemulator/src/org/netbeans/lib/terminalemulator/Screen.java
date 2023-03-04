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
 * "Screen.java"
 * Screen.java 1.9 01/07/26
 */
package org.netbeans.lib.terminalemulator;

import java.awt.*;
import java.awt.event.*;
import javax.accessibility.*;
import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

// We can _almost_ inherit from awt.Canvas, except that then we lose various
// very handy abilities:
// - Tool tips end up not working right since the AWT Canvas won't cooperate
//   with our containing JRootPane.
// - We loose the ability to use DebugGraphics.
// - JComponent does double-buffering for us, so we need not reimplement it.
//   (there's still an issue of whose double buffering is quicker).
class Screen extends JComponent implements Accessible {

    private final Term term;		// back pointer
    private static final boolean DEBUG = false;

    public Screen(Term term, int dx, int dy) {
        this.term = term;
        Dimension dim = new Dimension(dx, dy);
        setSize(dim);
        setPreferredSize(dim);
        // setOpaque(true);	// see comment in Term.repaint()

        setGrabTab(true);

        if (DEBUG) {
            // Just turning our double buffering isn't enough, need to
            // turn it off everywhere.
            RepaintManager repaintManager = RepaintManager.currentManager(this);
            repaintManager.setDoubleBufferingEnabled(false);

            setDebugGraphicsOptions(DebugGraphics.FLASH_OPTION |
                    DebugGraphics.BUFFERED_OPTION |
                    DebugGraphics.LOG_OPTION);
        }
    }

    // debugging hooks:

    /* TMP
    public void setSize(int width, int height) {
    super.setSize(width, height);
    }
    public void setSize(Dimension dim) {
    super.setSize(dim);
    }
     */

    // When under NB we sometime get resizes when validate() happens
    // while Term !isShowing() so the sizes are <= 0.
    // The result is a 1-column output.
    // In the following we discard "bad" resizes as a workaround.
    @Override
    public void setBounds(int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        super.setBounds(x, y, width, height);
    }

    @Override
    public void setBounds(Rectangle r) {
        super.setBounds(r);
    }

    /**
     * Allow Tab's to come through to us.
     * This is deprecated under 1.4 but works runtime-wise.
     * Once we shipt to building under 1.4  should do things as the comment
     * below suggests.
     */
    public boolean OLD_isManagingFocus() {
        return true;
    }

    /* LATER

    This code to be used when we want Term to only see Tab but not CtrlTab.
    This is easily accomplished by overriding isManagingFocus (in Screen).
    But that function is deprecated and when we switch to 1.4 as a base
    we'll have to use the below methodology. There are a lot of new 1.4 
    classes involved so I haven't bothered writing it introspectively.

    Also the below way isn't the best. It assumes that all java
    implementations will follow the Swing guides for focus traversal keys 
    and that no container of us will modify the set. A better way would be to
    use a read-modify-write approach where we "subtract" out the keystrokes
    we want to see. Note though that the Set returned by
    getFocusTraversalKeys is immutable and you'll need to achieve the
    subtraction through copying instead of deleting.
     */
    private java.util.Set<AWTKeyStroke> original_fwd_keys = null;
    private java.util.Set<AWTKeyStroke> original_bwd_keys = null;

    private void setGrabTab(boolean grabTab) {

        if (original_fwd_keys == null) {
            original_fwd_keys = new java.util.HashSet<>();
            original_fwd_keys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB,
                    InputEvent.CTRL_MASK));
            original_bwd_keys = new java.util.HashSet<>();
            original_bwd_keys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB,
                    InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
        }

        if (grabTab) {
            // install our simplified version allowing Ctrl-TAB to traverse
            setFocusTraversalKeys(
                    KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, original_fwd_keys);
            setFocusTraversalKeys(
                    KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, original_bwd_keys);

        } else {
            // revert to default
            setFocusTraversalKeys(
                    KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
            setFocusTraversalKeys(
                    KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
        }
    }

    @Override
    @SuppressWarnings("AssignmentToMethodParameter")
    public void paint(Graphics g) {

        // No need to double buffer as our caller, the repaint manager,
        // already does so.

        if (DEBUG) {
            // HACK, normally, by the time we get the Graphics
            // the components getComponentGraphics() should've retrieved
            // a DebugGraphics for us, _but_ it only does that if we have
            // a 'ui' which we don't, so I have to do this myself.
            g = new DebugGraphics(g, this);
        }

        // Let the term do the actual work of rendering
        term.do_paint(g);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(1000, 1000);
    }

    //..........................................................................
    // Accessibility stuff is all here
    //..........................................................................
    private AccessibleContext accessible_context;
    private AccessibleScreenText accessible_screen_text;

    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessible_context == null) {
            accessible_context = new AccessibleScreen();
        }
        return accessible_context;
    }

    protected class AccessibleScreenText implements AccessibleText {

        AccessibleScreenText() {
        }

        @Override
        public int getCaretPosition() {
            return term.CoordToPosition(term.getCursorCoord());
        }

        // cache for getCharacterAttribute
        private int last_attr;
        private MutableAttributeSet last_as;

        @Override
        public AttributeSet getCharacterAttribute(int index) {
            Coord c = term.PositionToCoord(index);
            if (c == null) {
                return null;
            }
            BCoord b = c.toBCoord(term.firsta());
            int attr;
            try {
                Line l = term.buf().lineAt(b.row);
                int[] attrs = l.attrArray();
                attr = attrs[b.col];
            } catch (Exception x) {
                return null;
            }

            if (attr == last_attr) {
                return last_as;
            }

            MutableAttributeSet as = new SimpleAttributeSet();

            if (Attr.UNDERSCORE.isSet(attr)) {
                as.addAttribute(StyleConstants.Underline, Boolean.TRUE);
            }
            if (Attr.BRIGHT.isSet(attr)) {
                as.addAttribute(StyleConstants.Bold, Boolean.TRUE);
            }

            boolean reverse = Attr.REVERSE.isSet(attr);

            Color color;
            color = term.foregroundColor(reverse, attr);
            if (color != Color.black) {
                as.addAttribute(StyleConstants.Foreground, color);
            }

            color = term.backgroundColor(reverse, attr);
            if (color != null) {
                as.addAttribute(StyleConstants.Background, color);
            }

            last_attr = attr;
            last_as = as;

            return as;
        }

        @Override
        public Rectangle getCharacterBounds(int index) {
            return term.getCharacterBounds(term.PositionToCoord(index));
        }

        @Override
        public int getCharCount() {
            return term.getCharCount();
        }

        @Override
        public int getSelectionStart() {
            Extent x = term.getSelectionExtent();
            if (x == null) {
                return getCaretPosition();
            }
            return term.CoordToPosition(x.begin);
        }

        @Override
        public int getSelectionEnd() {
            Extent x = term.getSelectionExtent();
            if (x == null) {
                return getCaretPosition();
            }
            return term.CoordToPosition(x.end);
        }

        @Override
        public String getSelectedText() {
            return term.getSelectedText();
        }

        private String getHelper(int part, BCoord b) {
            if (b == null) {
                return null;
            }

            Line l = term.buf().lineAt(b.row);

            switch (part) {
                case CHARACTER:
                    // return new String(l.charArray(), b.col, 1);
                    return String.valueOf(l.charAt(b.col));
                case WORD:
                    BExtent bword = term.buf().find_word(term.getWordDelineator(), b);
                    Extent word = bword.toExtent(term.firsta());
                    return term.textWithin(word.begin, word.end);
                case SENTENCE:
                    // return new String(l.charArray());
                    return l.toString();
            }
            return null;
        }

        @Override
        public String getAfterIndex(int part, int index) {
            Coord c = term.PositionToCoord(index);
            if (c == null) {
                return null;
            }
            BCoord b = c.toBCoord(term.firsta());
            b = term.buf().advance(b);
            return getHelper(part, b);
        }

        @Override
        public String getAtIndex(int part, int index) {
            Coord c = term.PositionToCoord(index);
            if (c == null) {
                return null;
            }
            BCoord b = c.toBCoord(term.firsta());
            return getHelper(part, b);
        }

        @Override
        public String getBeforeIndex(int part, int index) {
            Coord c = term.PositionToCoord(index);
            if (c == null) {
                return null;
            }
            BCoord b = c.toBCoord(term.firsta());
            b = term.buf().backup(b);
            return getHelper(part, b);
        }

        @Override
        public int getIndexAtPoint(Point p) {
            BCoord v = term.toViewCoord(p);
            BCoord b = term.toBufCoords(v);
            return term.CoordToPosition(new Coord(b, term.firsta()));
        }
    }

    protected class AccessibleScreen extends AccessibleJComponent {

        @Override
        public String getAccessibleDescription() {
            return "Terminal emulator"; // NOI18N
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.TEXT;
        }

        @Override
        public AccessibleText getAccessibleText() {
            if (accessible_screen_text == null) {
                accessible_screen_text = new AccessibleScreenText();
            }
            return accessible_screen_text;
        }
    }
    private int oldPos;

    void possiblyUpdateCaretText() {
        /*
         * Called from Term.putc_work().
         *
         * This is very crude. It works on the assumption that Term is just
         * getting regular characters and on each one we modify the text and
         * the cursor advances so we fire both simultaneously.
         */

        // don't bother with this stuff if no-one cares
        if (accessible_screen_text == null) {
            return;
        }

        int pos = term.CoordToPosition(term.getCursorCoord());

        accessible_context.firePropertyChange(AccessibleContext.ACCESSIBLE_TEXT_PROPERTY,
                null, pos);
        // sending null, pos is how JTextComponent does it.

        accessible_context.firePropertyChange(AccessibleContext.ACCESSIBLE_CARET_PROPERTY, pos, oldPos);

        oldPos = pos;
    }
}
