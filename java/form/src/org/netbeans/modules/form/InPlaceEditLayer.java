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

package org.netbeans.modules.form;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.JTextComponent;
import java.util.ArrayList;


/** A layer used by FormDesigner for in-place editing of components'
 * labels and text. Can be used in two modes - layer editing or
 * direct editing. In layer editing mode, a JTextField-based component
 * is set up on the layer for editing labels and buttons. In direct editing
 * mode, the edited component is used also for editing, the layer just
 * ensures that other components are inaccessible.
 *
 * @author Tomas Pavek
 */
public class InPlaceEditLayer extends JPanel
{
    private boolean layerEditing = true;

    private boolean changeDone = false;

    private String editedText;
    private String oldText;
    private boolean enabled;
    private boolean madeEditable;

    private Component editedComp;
    private Container superContainer;
    private JTextComponent editingTextComp;
    private InPlaceTextField inPlaceField;

    private ComponentListener layerResizeListener;
    private KeyListener compKeyListener;
    private FocusListener compFocusListener;
    private ActionListener compActionListener;

    private java.util.List<FinishListener> listeners;

    private Cursor defaultCursor;

    // ---------

    InPlaceEditLayer() {
        setLayout(null);
        defaultCursor = getCursor();
    }

    void setEditedComponent(Component comp, String text) {
        if (!comp.isShowing() || comp.getParent() == null)
            throw new IllegalArgumentException();

        editedComp = comp;
        editedText = text;
        if (inPlaceField != null) {
            remove(inPlaceField);
            inPlaceField = null;
        }

        if (comp instanceof JLabel || comp instanceof AbstractButton || comp instanceof JTabbedPane) {
            layerEditing = true;
            superContainer = null;
            createInPlaceField();
        }
        else if (comp instanceof JTextField || comp instanceof JTextArea) {
            layerEditing = false;
            superContainer = comp.getParent();

            Container cont = superContainer;
            do {
                if (cont.getParent() instanceof JLayeredPane) {
                    superContainer = cont;
                    break;
                }
                else cont = cont.getParent();
            }
            while (cont != null);

            editingTextComp = (JTextComponent)editedComp;
            oldText = editingTextComp.getText();
            editingTextComp.setText(editedText);

            // enable focus on component in component layer
            editingTextComp.setFocusable(true);
            if (!editingTextComp.isEnabled()) {
                editingTextComp.setEnabled(true);
                enabled = true;
            }
            if (!editingTextComp.isEditable()) {
                editingTextComp.setEditable(true);
                madeEditable = true;
            }
        }
        else throw new IllegalArgumentException();

        if (editingTextComp != null) {
            FormUtils.setupTextUndoRedo(editingTextComp);
        }

        attachListeners();
    }

    // ------------

    static boolean supportsEditingFor(Class compClass, boolean layerRequired) {
        return JLabel.class.isAssignableFrom(compClass)
               || AbstractButton.class.isAssignableFrom(compClass)
               || JTabbedPane.class.isAssignableFrom(compClass)
               || (!layerRequired
                   && (JTextField.class.isAssignableFrom(compClass)
                       || JTextArea.class.isAssignableFrom(compClass)));
    }

    boolean isEditingInitialized() {
        return editingTextComp != null;
    }

    boolean isLayerEditing() {
        return layerEditing;
    }

    String getEditedText() {
        return editedText;
    }

    boolean isTextChanged() {
        return changeDone;
    }

    void finishEditing(boolean applyChanges) {
        if (applyChanges) {
            String text = editingTextComp.getText();
            if (text.equals(editedText))
                applyChanges = false;
            else editedText = text;
        }
        else if (!isLayerEditing()) {
            editingTextComp.setText(oldText);
            editingTextComp.setFocusable(false);
        }

        editingTextComp.removeKeyListener(compKeyListener);
        editingTextComp.removeFocusListener(compFocusListener);
        if (editingTextComp instanceof JTextField)
            ((JTextField)editingTextComp).removeActionListener(compActionListener);
        if (editingTextComp == editedComp) {
            if (enabled) {
                editingTextComp.setEnabled(false);
                enabled = false;
            }
            if (madeEditable) {
                editingTextComp.setEditable(false);
                madeEditable = false;
            }
        }
        editingTextComp = null;

        changeDone = applyChanges;
        fireEditingFinished();
    }

    // ----------------

    private void createInPlaceField() {
        if (editedComp instanceof JLabel) {
            JLabel label = (JLabel)editedComp;
            inPlaceField = new InPlaceTextField(editedText);
            inPlaceField.setFont(label.getFont());
            inPlaceField.setHorizontalAlignment(label.getHorizontalAlignment());
//            inPlaceField.setNextFocusableComponent(this);
//            inPlaceField.setBorder(new javax.swing.border.EmptyBorder(0,0,0,0));
//            inPlaceField.setBackground(label.getBackground());
//            inPlaceField.setForeground(label.getForeground());
//            inPlaceField.setSelectedTextColor(label.getForeground());
        }
        else if (editedComp instanceof AbstractButton) {
            AbstractButton button = (AbstractButton)editedComp;
            inPlaceField = new InPlaceTextField(editedText);
            inPlaceField.setFont(button.getFont());
            inPlaceField.setHorizontalAlignment(button.getHorizontalAlignment());
//            inPlaceField.setNextFocusableComponent(this);
        }
        else if (editedComp instanceof JTabbedPane) {
            inPlaceField = new InPlaceTextField(editedText);
            inPlaceField.setFont(((JTabbedPane)editedComp).getFont());
            inPlaceField.setHorizontalAlignment(SwingConstants.CENTER);
            Insets insets = inPlaceField.getInsets();
            inPlaceField.setMargin(new Insets(0, insets.left, 0, insets.right));
        }
        else return; // should not happen
/*        else if (editedComp instanceof JTextField) {
            JTextField field = (JTextField)editedComp;
            JTextField textField = new InPlaceTextField(editedText);
            textField.setFont(field.getFont());
            textField.setHorizontalAlignment(field.getHorizontalAlignment());

            editingComp = textField;
            editingTextComp = textField;
        }
        else if (editedComp instanceof JTextArea) {
            JTextArea textA = (JTextArea)editedComp;
            JTextArea textArea = new JTextArea(editedText);
            textArea.setFont(textA.getFont());
            textArea.setTabSize(textA.getTabSize());
            textArea.setRows(textA.getRows());
            textArea.setColumns(textA.getColumns());
            textArea.setLineWrap(textA.getLineWrap());
//            textArea.setBorder(new javax.swing.border.LineBorder(Color.black));
            // TODO: scrollpane, resizing
            JScrollPane scroll = new JScrollPane(textArea);

            editingComp = scroll; //textArea;
            editingTextComp = textArea;
        } */

        editingTextComp = inPlaceField;
        add(inPlaceField);
        placeInPlaceField();
    }

    private void attachListeners() {
        if (layerResizeListener != null)
            removeComponentListener(layerResizeListener);
        else
            createListeners();

        if (isLayerEditing())
            addComponentListener(layerResizeListener);

        editingTextComp.addKeyListener(compKeyListener);
        editingTextComp.addFocusListener(compFocusListener);
        if (editingTextComp instanceof JTextField)
            ((JTextField)editingTextComp).addActionListener(compActionListener);
    }

    private void createListeners() {
        // listening for mouse events
        MouseInputListener layerMouseListener = new MouseInputListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                processMouse(e);
            }
            @Override
            public void mousePressed(MouseEvent e) {
                processMouse(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                processMouse(e);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                processMouse(e);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                processMouse(e);
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                processMouse(e);
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                processMouse(e);
            }
        };
        addMouseListener(layerMouseListener);
        addMouseMotionListener(layerMouseListener);

        // listening for layer resizing
        layerResizeListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (InPlaceEditLayer.this.isVisible() && editedComp != null && editedComp.getGraphics() != null) {
                    placeInPlaceField();
                }
            }
        };

        // listening for Escape and Ctrl+Enter
        compKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    finishEditing(false);
                else if (e.getModifiers() == InputEvent.CTRL_MASK
                         && (e.getKeyCode() == 10 || e.getKeyCode() == KeyEvent.VK_ENTER))
                    finishEditing(true);
            }
        };
        
        // listening for focus lost
        compFocusListener = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent event) {
                finishEditing(true);
            }
        };

        // listening for Enter
        compActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                finishEditing(true);
            }
        };
    }

    private void processMouse(MouseEvent e) {
        if (!isEditingInitialized()) return;

        if (isLayerEditing()) {
            if (e.getID() == MouseEvent.MOUSE_PRESSED)
                finishEditing(true); // or false?
        }
        else {
            Point p = SwingUtilities.convertPoint(this, e.getPoint(), superContainer);
            Component comp = SwingUtilities.getDeepestComponentAt(superContainer, p.x, p.y);
            if (comp != editedComp) {
                Container cont = editedComp.getParent();
                if (comp != cont || !(cont instanceof JScrollPane))
                    comp = null;
            }
            if (comp != null) {
                comp.dispatchEvent(SwingUtilities.convertMouseEvent(this, e, comp));

                if (e.getID() == MouseEvent.MOUSE_MOVED) {
                    Cursor cursor = comp.getCursor();
                    if (getCursor() != cursor)
                        setCursor(cursor);
                }
            }
            else {
                if (e.getID() == MouseEvent.MOUSE_PRESSED)
                    finishEditing(true); // or false?
                else if (e.getID() == MouseEvent.MOUSE_MOVED)
                    if (getCursor() != defaultCursor)
                        setCursor(defaultCursor);
            }
        }
    }

    private void placeInPlaceField() {
//        if (!(editedComp instanceof JLabel) && !(editedComp instanceof AbstractButton))
//            return;
        Rectangle bounds = SwingUtilities.convertRectangle(editedComp.getParent(),
                                                           editedComp.getBounds(),
                                                           this);
        Insets editedIns = ((JComponent)editedComp).getInsets();
        Insets editingIns = inPlaceField.getInsets();

        int hA, hTP, vA, vTP;
        Icon icon;
        int itGap;
        String text;

        if (editedComp instanceof JLabel) {
            JLabel label = (JLabel)editedComp;

            hA = label.getHorizontalAlignment();
            hTP = label.getHorizontalTextPosition();
            vA = label.getVerticalAlignment();
            vTP = label.getVerticalTextPosition();

            icon = label.getIcon();
            itGap = icon != null ? label.getIconTextGap() : 0;
            text = label.getText();
        }
        else if (editedComp instanceof AbstractButton) {
            AbstractButton button = (AbstractButton)editedComp;

            hA = button.getHorizontalAlignment();
            hTP = button.getHorizontalTextPosition();
            vA = button.getVerticalAlignment();
            vTP = button.getVerticalTextPosition();

            icon = button.getIcon();
            if (icon != null || editedComp instanceof JMenuItem) {
                Integer gap = (Integer)UIManager.get("Button.textIconGap"); // NOI18N
                itGap = gap != null ? gap.intValue() : 4;
            }
            else itGap = 0;
            text = button.getText();

            if (editedComp instanceof JCheckBox || editedComp instanceof JRadioButton) {
                if (icon == null) {
                    javax.swing.plaf.ComponentUI cui = UIManager.getUI((JComponent)editedComp);
                    if (cui instanceof javax.swing.plaf.basic.BasicRadioButtonUI) {
                        icon = ((javax.swing.plaf.basic.BasicRadioButtonUI)cui).getDefaultIcon();
                        itGap = ((javax.swing.plaf.basic.BasicRadioButtonUI)cui).getDefaultTextIconGap(button);
                    }
                }
                // hack: border at the aligned side is always 0 (bug?)
                if (hA == SwingConstants.LEFT || hA == SwingConstants.LEADING) {
                    editedIns.right += editedIns.left;
                    editedIns.left = 0;
                }
                else if (hA == SwingConstants.RIGHT || hA == SwingConstants.TRAILING) {
                    editedIns.left += editedIns.right;
                    editedIns.right = 0;
                }
                if (vA == SwingConstants.TOP) {
                    editedIns.bottom += editedIns.top;
                    editedIns.top = 0;
                }
                else if (vA == SwingConstants.BOTTOM) {
                    editedIns.top += editedIns.bottom;
                    editedIns.bottom = 0;
                }
            }
        } else if (editedComp instanceof JTabbedPane) {
            JTabbedPane tabbedPane = (JTabbedPane)editedComp;
            int index = tabbedPane.getSelectedIndex();
            text = tabbedPane.getTitleAt(index);
            Rectangle relBounds = tabbedPane.getBoundsAt(index);
            relBounds.x += bounds.x+4;
            relBounds.y += bounds.y;
            bounds = relBounds;
            bounds.width -= 8;
            icon = tabbedPane.getIconAt(index);
            itGap = UIManager.getInt("TabbedPane.textIconGap"); // NOI18N
            vA = hA = vTP = SwingConstants.CENTER;
            hTP = SwingConstants.TRAILING;
            editedIns = new Insets(0, 0, 0, 0);
        }
        else return; // should not happen

        bounds.x += editedIns.left;
        bounds.y += editedIns.top;
        bounds.width -= editedIns.left + editedIns.right;
        bounds.height -= editedIns.top + editedIns.bottom;
        Rectangle iR = new Rectangle(); // icon rectangle
        Rectangle tR = new Rectangle(); // text rectangle

        
        //HACK: a hack to account for the extra space on the right edge of the menu item
        //we should derive this value instead of hard coding it
        //don't set this value if it's really a toplevel jmenu
        if (editedComp instanceof JMenuItem && 
                    editedComp.getParent() != null && !(editedComp.getParent() instanceof JMenuBar)) {
            int menugap = 13;
            bounds.x += menugap;
            bounds.width -= menugap;
        }
        
        SwingUtilities.layoutCompoundLabel(
            (JComponent)editedComp,
            editedComp.getGraphics().getFontMetrics(),
            text, icon,
            vA, hA, vTP, hTP,
            bounds,
            iR, tR, itGap);

        if (icon != null && hTP != SwingConstants.CENTER) {
            if (hA == SwingConstants.LEFT || hA == SwingConstants.LEADING) {
                if (hTP == SwingConstants.RIGHT || hTP == SwingConstants.TRAILING) {
                    bounds.width -= tR.x - bounds.x;
                    bounds.x = tR.x;
                    inPlaceField.setHorizontalAlignment(SwingConstants.LEFT);
                }
                else if (hTP == SwingConstants.LEFT || hTP == SwingConstants.LEADING) {
                    bounds.width = tR.width;
                    inPlaceField.setHorizontalAlignment(SwingConstants.RIGHT);
                }
            }
            else if (hA == SwingConstants.RIGHT || hA == SwingConstants.TRAILING) {
                if (hTP == SwingConstants.RIGHT || hTP == SwingConstants.TRAILING) {
                    bounds.x = tR.x;
                    bounds.width = tR.width;
                    inPlaceField.setHorizontalAlignment(SwingConstants.LEFT);
                }
                if (hTP == SwingConstants.LEFT || hTP == SwingConstants.LEADING) {
                    bounds.width = tR.x - bounds.x + tR.width;
                    inPlaceField.setHorizontalAlignment(SwingConstants.RIGHT);
                }
            }
            else { // hA == SwingConstants.CENTER
                if (hTP == SwingConstants.RIGHT || hTP == SwingConstants.TRAILING) {
                    bounds.width -= tR.x - bounds.x;
                    bounds.x = tR.x;
                    inPlaceField.setHorizontalAlignment(SwingConstants.LEFT);
                }
                else if (hTP == SwingConstants.LEFT || hTP == SwingConstants.LEADING) {
                    bounds.width = tR.x - bounds.x + tR.width;
                    inPlaceField.setHorizontalAlignment(SwingConstants.RIGHT);
                }
                else if (bounds.width > tR.width) {
                    bounds.x++;
                    bounds.width--;
                }
            }
        }
        else if (hA == SwingConstants.CENTER && bounds.width > tR.width) {
            bounds.x++;
            bounds.width--;
        }

        bounds.x -= editingIns.left;
        bounds.width += editingIns.left + editingIns.right + 1;
        if (bounds.width < 10)
            bounds.width = 10;

        bounds.y = tR.y - editingIns.top;
        bounds.height = inPlaceField.getPreferredSize().height;

/*        else if (editedComp instanceof JTextField) {
//            JTextField field = (JTextField)editedComp;
            int height = editingTextComp.getPreferredSize().height;

            bounds.x += editedIns.left - editingIns.left;
            bounds.y += bounds.height/2 - height/2;
            bounds.width -= editedIns.left + editedIns.right - editingIns.left - editingIns.right - 1;
            if (bounds.width < 32)
                bounds.width = 32;
            bounds.height = height;
        }
        else if (editedComp instanceof JTextArea) {
            bounds.x += editedIns.left - editingIns.left;
            bounds.y += editedIns.top - editingIns.top;
            bounds.width -= editedIns.left + editedIns.right - editingIns.left - editingIns.right - 1;
            if (bounds.width < 64)
                bounds.width = 64;
            int height = editingTextComp.getPreferredSize().height;
//            System.out.println("height: "+height);
            if (((JTextArea)editingTextComp).getRows() == 0 && ((JTextArea)editingTextComp).getLineCount() < 2) {
                height *= 2;
            }
//            System.out.println("zvetsit? "+(((JTextArea)editingTextComp).getRows() == 0 && ((JTextArea)editingTextComp).getLineCount() < 2));
            if (bounds.height < height)
                bounds.height = height; //editingTextComp.getPreferredSize().height; // + editingIns.top + editingIns.bottom;
//            bounds.height -= editedIns.top + editedIns.bottom - editingIns.top - editingIns.bottom;
            // TODO: scrollpane?
            editingTextComp.setSize(bounds.width, bounds.height);
            editingComp.setBounds(bounds.x-1, bounds.y-1, bounds.width+3, bounds.height+3);
        } */

//        if (!(editedComp instanceof JTextArea))
        inPlaceField.setBounds(bounds);
        inPlaceField.baseBounds = bounds;
        if (!(inPlaceField.getText().equals(editedText)))
            inPlaceField.adjustSize();
    }

    // ----------------

    @Override
    public void requestFocus() {
        if (editingTextComp != null) {
//            System.out.println("bounds: "+editingTextComp.getBounds()
//                               +", visible: "+editingTextComp.isVisible()
//                               +", valid: "+editingTextComp.isValid()
//                               +", showing: "+editingTextComp.isShowing());
            editingTextComp.requestFocus();
            int n = editingTextComp.getText().length();
            editingTextComp.setCaretPosition(n);
            editingTextComp.moveCaretPosition(0);
        }
        else super.requestFocus();
    }

    @Override
    public boolean isOpaque() {
        return false;
    }

    // -------------

    public interface FinishListener extends java.util.EventListener {
        public void editingFinished(boolean changed);
    }

    public synchronized void addFinishListener(FinishListener l) {
        if (listeners == null)
            listeners = new ArrayList<FinishListener>();
        listeners.add(l);
    }

    public synchronized void removeFinishListener(FinishListener l) {
        if (listeners != null)
            listeners.remove(l);
    }

    private void fireEditingFinished() {
        java.util.List<FinishListener> targets;
        synchronized (this) {
            if (listeners == null) return;
            targets = new ArrayList<FinishListener>(listeners);
        }
        for (int i=0, n=targets.size(); i < n; i++)
            targets.get(i).editingFinished(changeDone);
    }

    // -----------

    /** Custom JTextField used as editing component on the layer.
     * It prevents focus manager from switching to another components
     * (using TAB and Shift+TAB). It also changes its size according to
     * entered text, with respect to underlying (edited) component's size.
     */
    class InPlaceTextField extends JTextField {
        Rectangle baseBounds;

        public InPlaceTextField() {
            super();
        }

        public InPlaceTextField(String text) {
            super(text);
        }

        @Override
        protected void processKeyEvent(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_TAB || e.getKeyChar() == '\t') {
                e.consume();
            }
            else {
                super.processKeyEvent(e);
                if (e.getID() == KeyEvent.KEY_TYPED)
                    adjustSize();
            }
        }

        void adjustSize() {
            int prefWidth = getPreferredSize().width + 1;
            if (prefWidth < baseBounds.width) prefWidth = baseBounds.width;
            Rectangle bounds = getBounds();

            if (prefWidth != bounds.width) {
                Rectangle newBounds;
                if (prefWidth == baseBounds.width)
                    newBounds = baseBounds;
                else {
                    int layerWidth =  InPlaceEditLayer.this.getSize().width;
                    int leftX = baseBounds.x < 0 ? baseBounds.x : 0;
                    int rightX = baseBounds.x + baseBounds.width > layerWidth ?
                                 baseBounds.x + baseBounds.width : layerWidth;

                    newBounds = new Rectangle(bounds);

                    int hA = getHorizontalAlignment();
                    if (hA == SwingConstants.LEFT
                            || hA == SwingConstants.LEADING) {
                        newBounds.x = rightX - prefWidth;
                        if (newBounds.x < baseBounds.x) { // can't grow to right
                            if (newBounds.x < leftX) newBounds.x = leftX;
                            newBounds.width = rightX - newBounds.x;
                        }
                        else {
                            newBounds.x = baseBounds.x;
                            newBounds.width = prefWidth;
                        }
                    }
                    else if (hA == SwingConstants.RIGHT
                             || hA == SwingConstants.TRAILING) {
                        newBounds.x = baseBounds.x + baseBounds.width - prefWidth;
                        if (newBounds.x < leftX) {
                            newBounds.x = leftX;
                            newBounds.width = prefWidth > rightX - leftX ?
                                              rightX - leftX : prefWidth;
                        }
                        else newBounds.width = prefWidth;
                    }
                    else { // CENTER
                        int cX = baseBounds.x + baseBounds.width/2;
                        int dX1 = prefWidth/2;
                        int dX2 = prefWidth - dX1;

                        int over1, over2;
                        if (cX - leftX < dX1) over2 = dX1 - (cX - leftX);
                        else over2 = 0;
                        if (rightX - cX < dX2) over1 = dX2 - (rightX - cX);
                        else over1 = 0;

                        if (cX - leftX < dX1 + over1) dX1 = cX - leftX;
                        else dX1 += over1;
                        if (rightX - cX < dX2 + over2) dX2  = rightX - cX;
                        else dX2 += over2;

                        newBounds.x = cX - dX1;
                        newBounds.width = dX1 + dX2;
                    }
                }

                if (!(newBounds.equals(bounds)))
                    setBounds(newBounds);
            }
        }
    }
}
