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
* RadioInplaceEditor.java
*
* Created on 28 September 2003, 01:41
*/
package org.openide.explorer.propertysheet;

import org.openide.util.WeakSet;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;

import java.beans.PropertyEditor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;


/** An inplace editor that represents the contents of a property editor's
 * getTags() method as a set of radio buttons.  For larger sets of choices
 * it is preferable to use a combo box, but for situations where only 3 to 5
 * options are available, this is preferable.
 *
 * @author  Tim Boudreau
 */
class RadioInplaceEditor extends JPanel implements InplaceEditor, ActionListener {
    private transient List<ActionListener> actionListenerList;
    protected transient PropertyEditor editor = null;
    protected transient PropertyEnv env = null;
    protected transient PropertyModel mdl = null;
    protected transient ButtonGroup group = null;
    private boolean tableUI = false;
    boolean isFirstEvent = false;
    private WeakSet<InvRadioButton> buttonCache = new WeakSet<InvRadioButton>();
    private boolean useTitle = false;

    public RadioInplaceEditor(boolean tableUI) {
        setLayout(new AutoGridLayout(false));
        this.tableUI = tableUI;
        setOpaque(true);
    }

    public void clear() {
        editor = null;
        env = null;
        mdl = null;
        group = null;

        Component[] c = getComponents();

        for (int i = 0; i < c.length; i++) {
            if (c[i] instanceof JRadioButton) {
                ((JRadioButton) c[i]).removeActionListener(this);
            }
        }

        removeAll();
        setEnabled(true);
    }

    /** Overridden to avoid grabbing the AWT tree lock */
    @Override
    public Dimension getPreferredSize() {
        if (getLayout() != null) {
            return getLayout().preferredLayoutSize(this);
        } else {
            return super.getPreferredSize();
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        isFirstEvent = true;
    }

    private InvRadioButton[] getButtons(int count) {
        InvRadioButton[] result = new InvRadioButton[count];

        Iterator<InvRadioButton> i = buttonCache.iterator();
        int idx = 0;

        while (i.hasNext() && (idx < count)) {
            result[idx] = i.next();

            if (result[idx] != null) {
                result[idx].setEnabled(true);
                result[idx].setSelected(false);
                idx++;
            }
        }

        for (; idx < count; idx++) {
            result[idx] = createButton();
            buttonCache.add(result[idx]);
        }

        return result;
    }

    @Override
    public void setEnabled(boolean val) {
        //        System.err.println("RadioEditor.setEnabled " + val);
        super.setEnabled(val);

        Component[] c = getComponents();

        for (int i = 0; i < c.length; i++) {
            c[i].setEnabled(val);
        }
    }

    @Override
    public void setBackground(Color col) {
        super.setBackground(col);

        Component[] c = getComponents();

        for (int i = 0; i < c.length; i++) {
            c[i].setBackground(col);
        }
    }

    @Override
    public void setForeground(Color col) {
        super.setForeground(col);

        Component[] c = getComponents();

        for (int i = 0; i < c.length; i++) {
            c[i].setForeground(col);
        }
    }

    /** In 1.4, panels can and will receive focus; if we define our own
     * focus policy, we're responsible for all possible subcomponents.
     * Therfore just proxy requestFocusInWindow for the selected radio
     * button  */
    @Override
    public void requestFocus() {
        Component[] c = getComponents();

        if (c.length > 0) {
            for (int i = 0; i < c.length; i++) {
                if (c[i] instanceof InvRadioButton && ((InvRadioButton) c[i]).isSelected()) {
                    c[i].requestFocus();

                    return;
                }
            }

            c[0].requestFocus();
        } else {
            super.requestFocus();
        }
    }

    @Override
    public boolean requestFocusInWindow() {
        Component[] c = getComponents();

        for (int i = 0; i < c.length; i++) {
            if (c[i] instanceof InvRadioButton && ((InvRadioButton) c[i]).isSelected()) {
                return c[i].requestFocusInWindow();
            }
        }

        return super.requestFocusInWindow();
    }

    public void setUseTitle(boolean val) {
        if (useTitle != val) {
            useTitle = val;

            if (env != null) {
                setBorder(new TitledBorder(env.getFeatureDescriptor().getDisplayName()));
            }
        }
    }

    public void connect(PropertyEditor pe, PropertyEnv env) {
        if (!tableUI && (env != null) && useTitle) {
            setBorder(new TitledBorder(env.getFeatureDescriptor().getDisplayName()));
        } else {
            setBorder(null);
        }

        editor = pe;

        String[] tags = editor.getTags();
        group = new ButtonGroup();

        InvRadioButton[] buttons = getButtons(tags.length);

        if (env != null) {
            setEnabled(env.isEditable());
        }

        for (int i = 0; i < tags.length; i++) {
            InvRadioButton jr = buttons[i];
            configureButton(jr, tags[i]);
            add(jr);
        }
    }

    /** Renderer version overrides this to create a subclass that won't
     * fire changes */
    protected InvRadioButton createButton() {
        return new InvRadioButton();
    }

    /** Renderer version overrides this */
    protected void configureButton(InvRadioButton ire, String txt) {
        ire.addActionListener(this);

        if (editor.getTags().length == 1) {
            ire.setEnabled(false);
        } else {
            ire.setEnabled(isEnabled());
        }

        if (tableUI) {
            ire.setFocusable(false);
        } else {
            ire.setFocusable(true);
        }

        ire.setText(txt);

        if (txt.equals(editor.getAsText())) {
            ire.setSelected(true);
        } else {
            ire.setSelected(false);
        }

        ire.setFont(getFont());
        ire.setBackground(getBackground());
        ire.setForeground(getForeground());
        group.add(ire);
    }

    public JComponent getComponent() {
        return this;
    }

    public KeyStroke[] getKeyStrokes() {
        return null;
    }

    public PropertyEditor getPropertyEditor() {
        return editor;
    }

    public PropertyModel getPropertyModel() {
        return mdl;
    }

    public Object getValue() {
        Component[] c = getComponents();

        //        System.out.println("GetSelection is " + group.getSelection());
        for (int i = 0; i < c.length; i++) {
            if (c[i] instanceof JRadioButton) {
                if (group.getSelection() == ((JRadioButton) c[i]).getModel()) {
                    String result = ((JRadioButton) c[i]).getText();

                    return result;
                }
            }
        }

        return null;
    }

    public void handleInitialInputEvent(InputEvent e) {
        System.err.println("HandleInitialInputEvent");
        getLayout().layoutContainer(this);

        if (e instanceof MouseEvent) {
            Point p = SwingUtilities.convertPoint((JComponent) e.getSource(), ((MouseEvent) e).getPoint(), this);
            Component c = getComponentAt(p);

            if (c instanceof JRadioButton) {
                ((JRadioButton) c).setSelected(true);
                c.requestFocus();
                fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, InplaceEditor.COMMAND_SUCCESS));
            }
        } else {
            Component[] c = getComponents();

            for (int i = 0; i < c.length; i++) {
                if (c[i] instanceof JRadioButton) {
                    if (((JRadioButton) c[i]).isSelected()) {
                        c[i].requestFocusInWindow();
                    }
                }
            }
        }
    }

    public boolean isKnownComponent(Component c) {
        return (c != null) && ((c == this) || c instanceof InvRadioButton);
    }

    public void reset() {
        setValue(editor.getAsText());
    }

    public void setPropertyModel(PropertyModel pm) {
        mdl = pm;
    }

    public void setValue(Object o) {
        Component[] c = getComponents();

        for (int i = 0; i < c.length; i++) {
            if (c[i] instanceof JRadioButton) {
                if (((JRadioButton) c[i]).getText().equals(o)) {
                    ((JRadioButton) c[i]).setSelected(true);
                } else {
                    //Necessary for renderer, its buttons don't fire changes
                    ((JRadioButton) c[i]).setSelected(false);
                }
            }
        }
    }

    public boolean supportsTextEntry() {
        return false;
    }

    public synchronized void addActionListener(java.awt.event.ActionListener listener) {
        if (actionListenerList == null) {
            actionListenerList = new java.util.ArrayList<ActionListener>();
        }

        actionListenerList.add(listener);
    }

    public synchronized void removeActionListener(java.awt.event.ActionListener listener) {
        if (actionListenerList != null) {
            actionListenerList.remove(listener);
        }
    }

    private void fireActionPerformed(final java.awt.event.ActionEvent event) {
        //        System.err.println("Radio editor firing action performed " + event.getActionCommand());
        List<ActionListener> list;

        synchronized (this) {
            if (actionListenerList == null) {
                return;
            }

            list = new ArrayList<>(actionListenerList);
        }

        final List<ActionListener> theList = list;

        //When used in a table, the typical case is that the editor is instantiated,
        //processes its mouse event, fires an event and is immediately removed.
        //Using invokeLater allows the table to repaint appropriately for selection,
        //etc, reducing flicker.
        if (tableUI) {
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        for (int i = 0; i < theList.size(); i++) {
                            theList.get(i).actionPerformed(event);
                        }
                    }
                }
            );
        } else {
            for (int i = 0; i < list.size(); i++) {
                theList.get(i).actionPerformed(event);
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        ActionEvent ae = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, InplaceEditor.COMMAND_SUCCESS);
        fireActionPerformed(ae);
    }

    @Override
    public void paint(Graphics g) {
        if (isShowing()) {
            super.paint(g);
        } else {
            getLayout().layoutContainer(this);

            Component[] c = getComponents();
            Color col = g.getColor();

            try {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());

                for (int i = 0; i < c.length; i++) {
                    Rectangle r = c[i].getBounds();

                    if (g.hitClip(r.x, r.y, r.width, r.height)) {
                        Graphics g2 = g.create(r.x, r.y, r.width, r.height);

                        try {
                            c[i].paint(g2);
                        } finally {
                            g2.dispose();
                        }
                    }
                }

                if (getBorder() != null) {
                    super.paintBorder(g);
                }
            } finally {
                g.setColor(col);
            }
        }
    }

    @Override
    public void processMouseEvent(MouseEvent me) {
        if (isFirstEvent) {
            handleInitialInputEvent(me);
            isFirstEvent = false;
        } else {
            super.processMouseEvent(me);
        }
    }

    @Override
    public Component getComponentAt(int x, int y) {
        getLayout().layoutContainer(this);

        Component result = super.getComponentAt(x, y);
        System.err.println("getComponentAt " + x + "," + y + " returning " + result.getName());

        return result;
    }

    /** A JRadioButton that can calculate its preferred size when it
     * has no parent */
    class InvRadioButton extends JRadioButton {
        public InvRadioButton() {
            super();
        }

        @Override
        public String getName() {
            return "InvRadioButton - " + getText(); //NOI18N
        }

        @Override
        public void processKeyEvent(KeyEvent ke) {
            super.processKeyEvent(ke);

            if (
                ((ke.getKeyCode() == KeyEvent.VK_ENTER) || (ke.getKeyCode() == KeyEvent.VK_ESCAPE)) &&
                    (ke.getID() == KeyEvent.KEY_PRESSED)
            ) {
                RadioInplaceEditor.this.fireActionPerformed(
                    new ActionEvent(
                        this, ActionEvent.ACTION_PERFORMED,
                        (ke.getKeyCode() == KeyEvent.VK_ENTER) ? COMMAND_SUCCESS : COMMAND_FAILURE
                    )
                );
            }
        }

        @Override
        public Dimension getPreferredSize() {
            int w = 0;
            int h = 0;
            Graphics g = PropUtils.getScratchGraphics(this);
            FontMetrics fm = g.getFontMetrics(getFont());

            if (getIcon() != null) {
                w = getIcon().getIconWidth();
                h = getIcon().getIconHeight();
            }

            if (getBorder() != null) {
                Insets ins = getBorder().getBorderInsets(this);
                w += (ins.left + ins.right);
                h += (ins.bottom + ins.top);
            }

            w += (fm.stringWidth(getText()) + 22);
            h = Math.max(fm.getHeight(), h) + 2;

            return new Dimension(w, h);
        }
    }
}
