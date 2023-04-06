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
/*
 * ButtonPanel.java
 *
 * Created on December 15, 2002, 5:45 PM
 */
package org.openide.explorer.propertysheet;

import java.awt.*;
import java.awt.event.FocusListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.*;


/** This class acts as a container for property table cell
 * editors that support custom editors, and as a cell
 * renderer proxy that will display the custom editor button.
 * This ensures that renderers appear identical
 * to editors, and that any changes to the appearance of the
 * button that launches the custom editor are made, they will
 * appear automatically in both renderers and editors.
 * <p>
 * It implements InplaceEditor to proxy the real inplace editor it contains wraps,
 * and desplays the component returned by InplaceEditor.getComponent on the
 * inplace editor it is currently wrapping.
 *
 * @author  Tim Boudreau
 */
class ButtonPanel extends javax.swing.JComponent implements InplaceEditor {
    public static final Object editorActionKey = "openCustomEditor"; //NOI18N

    /** Store this value since we will check it for every paint or layout */
    private final boolean log = PropUtils.isLoggable(ButtonPanel.class);

    /** The component to be rendered in the left side of the component or the
     *full component in the case the custom editor button should not be displayed. */
    JComponent comp = null;
    private ConditionallyFocusableButton button;
    boolean needLayout = true;
    private InplaceEditor inplace = null;
    boolean clearing = false;

    /** Creates a new instance of ButtonPanel */
    public ButtonPanel() {
        createButton();
        setOpaque(true);
    }

    private void createButton() {
        button = new ConditionallyFocusableButton();

        int buttonWidth = PropUtils.getCustomButtonWidth();
        button.setBounds(getWidth() - buttonWidth, 0, buttonWidth, getHeight());
        button.setIcon(PropUtils.getCustomButtonIcon());
        button.setRolloverIcon(PropUtils.getCustomButtonIcon());
        button.setMargin(null);
        button.setName("Custom editor button - editor instance"); //NOI18N
        button.setText(null);

        //undocumented (?) call to hide action text - see JButton line 234
        button.putClientProperty("hideActionText", Boolean.TRUE); //NOI18N

        //Don't allow button to receive focus - otherwise it can when it's
        //removed
        //        button.setFocusable(false); 
        add(button);

        //setFocusable(false);
    }

    void setButtonAction(Action a) {
        button.setAction(a);
        button.setIcon(PropUtils.getCustomButtonIcon());
        button.setRolloverIcon(PropUtils.getCustomButtonIcon());
    }

    @Override
    public void setOpaque(boolean b) {
        if (getInplaceEditor() != null) {
            getInplaceEditor().getComponent().setOpaque(true);
        }
    }

    @Override
    public void setFont(Font f) {
        if (comp != null) {
            comp.setFont(f);
        }

        super.setFont(f);
    }

    public InplaceEditor getInplaceEditor() {
        return inplace;
    }

    public void setCustomButtonBackground(Color c) {
        button.setBackground(c);
    }

    public void setRolloverPoint(Point p) {
        if (p != null) {
            if (p.x < (getWidth() - PropUtils.getCustomButtonWidth())) {
                button.getModel().setRollover(false);

                if (comp instanceof AbstractButton) {
                    ((AbstractButton) comp).getModel().setRollover(true);
                }
            } else {
                button.getModel().setRollover(true);

                if (comp instanceof AbstractButton) {
                    ((AbstractButton) comp).getModel().setRollover(false);
                }
            }
        } else {
            button.getModel().setRollover(false);

            if (comp instanceof AbstractButton) {
                ((AbstractButton) comp).getModel().setRollover(false);
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension result;

        if (comp != null) {
            result = new Dimension(comp.getPreferredSize());
            result.width += button.getWidth();
            result.height = Math.max(result.height, button.getPreferredSize().height);
        } else {
            result = new Dimension(button.getPreferredSize());
        }

        return result;
    }

    /** Overridden to forward the setEnabled call to the contained
     *   component - the custom editor button should always be
     *   enabled if present */
    @Override
    public void setEnabled(boolean val) {
        super.setEnabled(val);

        if (comp != null) {
            comp.setEnabled(val);
        }

        button.setEnabled(true);
    }

    /**  Set the component that will render (or be
     * editor for) the property value.  The component
     * <strong>must</strong> be set <strong>before</strong>
     * the instance is added to a container (the add will
     * happen on <code>addNotify()</code>)*/
    private void setComponent(JComponent c) {
        if (c == comp) {
            return;
        }

        if ((comp != null) && (comp.getParent() == this)) {
            remove(comp);
        }

        if (log) {
            PropUtils.log(ButtonPanel.class, "Button panel setComponent to " + c);
        }

        comp = c;

        if (comp != null) {
            comp.setBackground(getBackground());
            comp.setForeground(getForeground());

            if (comp.isEnabled() != isEnabled()) {
                comp.setEnabled(isEnabled());
            }

            add(comp);
        }

        needLayout = true;
    }

    @Override
    public void setBackground(Color c) {
        super.setBackground(c);

        if (comp != null) {
            comp.setBackground(c);

            Color bttn = PropUtils.getButtonColor();

            if (bttn == null) {
                button.setBackground(c);
            } else {
                button.setBackground(bttn);
            }
        }
    }

    @Override
    public void setForeground(Color c) {
        super.setForeground(c);

        if (comp != null) {
            comp.setForeground(c);

            if (PropUtils.getButtonColor() == null) {
                button.setForeground(c);
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        if (isShowing()) {
            super.paint(g);

            return;
        }

        if (needLayout) {
            doLayout();
        }

        int width = getWidth();

        //We're painting in a PropertyRenderer, no parent present
        Graphics cg = g.create(0, 0, width - button.getWidth(), getHeight());
        if( cg instanceof Graphics2D ) {
            ((Graphics2D)cg).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        try {
            if (comp instanceof InplaceEditor) {
                comp.paint(cg);

                if (comp.getParent() != this) {
                    add(comp);
                }
            }
        } finally {
            cg.dispose();
        }

        cg = g.create(width - button.getWidth(), 0, button.getWidth(), getHeight());
        if( cg instanceof Graphics2D ) {
            ((Graphics2D)cg).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        try {
            button.paint(cg);
        } finally {
            cg.dispose();
        }

        /** Problem with endless looping in windows look and feel - painting
         * causes some fiddling with component hierarchy */
        if (getParent() instanceof CellRendererPane) {
            RepaintManager.currentManager(this).markCompletelyClean(this);
        }
    }

    /** Overridden to flag that a layout needs to be performed.  This is
     * needed since the component may be painted without a parent, so
     * invalidate will not do anything */
    @SuppressWarnings("deprecation")
    @Override
    public void reshape(int x, int y, int w, int h) {
        super.reshape(x, y, w, h);
        needLayout = true;
    }

    /** Overridden to force focus requests to the contained editor
     *  component - setting focus to this component directly will
     *  never be desirable. */
    @Override
    public void requestFocus() {
        if (comp != null) {
            comp.requestFocus();
        }
    }

    /** Overridden to force focus requests to the contained editor
     *  component - setting focus to this component directly will
     *  never be desirable. */
    @Override
    public boolean requestFocusInWindow() {
        if (comp != null) {
            return comp.requestFocusInWindow();
        } else {
            return false;
        }
    }

    /** Overridden to proxy adds to the custom editor button and the
     * installed component */
    @Override
    public void addFocusListener(FocusListener l) {
        if (comp != null) {
            button.addFocusListener(l);
            comp.addFocusListener(l);
        }
    }

    /** Overridden to proxy removes to the custom editor button and the
     * installed component */
    @Override
    public void removeFocusListener(FocusListener l) {
        if (comp != null) {
            button.removeFocusListener(l);
            comp.removeFocusListener(l);
        }
    }

    public void setInplaceEditor(InplaceEditor ed) {
        if (inplace == ed) {
            if (isAncestorOf(inplace.getComponent())) {
                return;
            }
        }

        if (inplace != null) {
            setComponent(null);
        }

        inplace = ed;
        setComponent(inplace.getComponent());
        needLayout = true;
    }

    //*********InplaceEditor impl that proxies to the embedded inplace editor*****    
    public void addActionListener(java.awt.event.ActionListener al) {
        inplace.addActionListener(al);
    }

    public void clear() {
        clearing = true;

        try {
            inplace.clear();
            inplace = null;
            setComponent(null);
        } finally {
            clearing = false;
        }
    }

    /** Get the component currently assigned as the real editor
     *  embedded in this component.  While not strictly necessary,
     *  this is useful if there are issues with focus bugs stemming
     *  from specific component types which need to be handled by
     *  the parent table.   */
    public JComponent getComponent() {
        return this;
    }

    public void connect(java.beans.PropertyEditor pe, PropertyEnv env) {
        inplace.connect(pe, env);
    }

    public KeyStroke[] getKeyStrokes() {
        return inplace.getKeyStrokes();
    }

    public java.beans.PropertyEditor getPropertyEditor() {
        return inplace.getPropertyEditor();
    }

    public PropertyModel getPropertyModel() {
        return inplace.getPropertyModel();
    }

    public Object getValue() {
        return inplace.getValue();
    }

    public boolean isKnownComponent(Component c) {
        //        return c == this || c == button || inplace.isKnownComponent(c);
        return (c == this) || inplace.isKnownComponent(c);
    }

    public void removeActionListener(java.awt.event.ActionListener al) {
        inplace.removeActionListener(al);
    }

    public void reset() {
        inplace.reset();
    }

    public void setPropertyModel(PropertyModel pm) {
        inplace.setPropertyModel(pm);
    }

    public void setValue(Object o) {
        inplace.setValue(o);
    }

    public boolean supportsTextEntry() {
        return inplace.supportsTextEntry();
    }

    @Override
    public void doLayout() {
        if (comp != null) {
            comp.setBounds(0, 0, getWidth() - PropUtils.getCustomButtonWidth(), getHeight());
            comp.doLayout();
        }

        button.setBounds(
            getWidth() - PropUtils.getCustomButtonWidth(), 0, PropUtils.getCustomButtonWidth(), getHeight()
        );

        if (log) {
            PropUtils.log(
                ButtonPanel.class,
                "Laying out button panel.  Bounds" + " are " + getBounds() + ", custom editor button bounds: " +
                button.getBounds() + " comp is " + comp
            ); //NOI18N
        }

        needLayout = false;
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    /** This handles the problem that the order of removal is that when
     * the editor is removed, first the inner component is removed.  At
     * that point, the focus subsystem will try to give focus to the first
     * focusable sibling of the inner component, which is the custom editor
     * button, which is still there.  However, when the editor is removed,
     * focus never returns to the table - it stays on the now-offscreen
     * custom editor button.
     * <p>
     * This class also contains the ability to create an image buffer of itself
     * and use it for its lifetime.  On XP and Aqua L&amp;Fs, button painting is
     * expensive, and a huge amount of a treetable or property sheet's painting
     * cycle gets spent scaling the backing bitmap for a button that will
     * always be painted exactly the same size.
     */
    private class ConditionallyFocusableButton extends JButton {
        private AffineTransform at = AffineTransform.getTranslateInstance(0, 0);
        private BufferedImage snapshot = null;

        public ConditionallyFocusableButton() {
        }

        @Override
        public boolean isFocusable() {
            return (ButtonPanel.this.getParent() != null) && !clearing;
        }

        @Override
        public void paint(Graphics g) {
            if (PropUtils.useOptimizedCustomButtonPainting() && !hasFocus()) {
                if (log) {
                    PropUtils.log(
                        ButtonPanel.class,
                        "Blitting custom editor " + "button backing store for button at " + getBounds() + " in " +
                        ((getParent() == null) ? " null parent" : (getParent() + "editor=" + inplace))
                    ); //NOI18N
                }

                ((Graphics2D) g).drawRenderedImage(getSnapshot(), at);
            } else {
                if (log) {
                    PropUtils.log(
                        ButtonPanel.class,
                        "Painting unoptimized custom editor " + "button button at " + getBounds() + " in " +
                        ((getParent() == null) ? " null parent" : (getParent() + "editor=" + inplace))
                    ); //NOI18N
                }

                super.paint(g);
            }
        }

        public BufferedImage getSnapshot() {
            if (snapshot == null) {
                snapshot = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                                              .getDefaultConfiguration().createCompatibleImage(getWidth(), getHeight());

                if (log) {
                    PropUtils.log(ButtonPanel.class, "Created " + snapshot + " custom editor button backing image");
                }

                if (snapshot.getAlphaRaster() == null) {
                    //Alpha not supported, could cause corruption (issue
                    //39280) - use a bufferedImage which will support alpha,
                    //although less efficient to blit
                    snapshot = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                }

                Graphics g = snapshot.getGraphics();
                super.paint(g);
            }

            return snapshot;
        }
    }
}
