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

package org.openide.explorer.propertysheet;

import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.FocusListener;

import java.beans.FeatureDescriptor;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;


/** A panel which embeds and displays an inplace editor and which can
 * show property marking for components that are not JLabels but should
 * show an icon either because of hinting or because the state is
 * PropertyEnv.STATE_INVALID.
 *
 * @author  Tim Boudreau
 */
class IconPanel extends JComponent implements InplaceEditor {
    private InplaceEditor inplaceEditor;
    private Icon icon;
    private boolean needLayout = true;
    private PropertyEnv env = null;
    private Component comp;

    /** Creates a new instance of IconValuePanel */
    public IconPanel() {
        setOpaque(true);
    }

    /**
     * Setter for property inplaceEditor.
     * @param inplaceEditor New value of property inplaceEditor.
     */
    public void setInplaceEditor(InplaceEditor inplaceEditor) {
        this.inplaceEditor = inplaceEditor;
        setComponent(inplaceEditor.getComponent());
    }

    public InplaceEditor getInplaceEditor() {
        return inplaceEditor;
    }

    @Override
    public void setEnabled(boolean val) {
        if (comp != null) {
            //Can be called from setUI in superclass constructor
            comp.setEnabled(val);
        }

        super.setEnabled(val);
    }

    @Override
    public void setBackground(Color c) {
        if (comp != null) {
            //Can be called from setUI in superclass constructor
            comp.setBackground(c);
        }

        super.setBackground(c);
    }

    @Override
    public void setForeground(Color c) {
        if (comp != null) {
            //Can be called from setUI in superclass constructor
            comp.setForeground(c);
        }

        super.setForeground(c);
    }

    @Override
    public void setFont(Font f) {
        if (comp != null) {
            comp.setFont(f);
        }

        super.setFont(f);
    }

    /** Set the inner component that will actually display the property */
    private void setComponent(Component c) {
        if (comp != null) {
            remove(comp);
        }

        if (c != null) {
            add(c);
        }

        comp = c;
        needLayout = true;
    }

    /** Set the icon that will be used. */
    public void setIcon(Icon i) {
        this.icon = i;
        needLayout = true;
    }

    /** Overridden to paint the icon */
    @Override
    public void paintComponent(Graphics g) {
        if (needLayout) {
            doLayout();
        }

        if (icon != null) {
            Color c = g.getColor();

            try {
                g.setColor(getBackground());

                int right = (comp != null) ? (comp.getLocation().x + icon.getIconWidth()) : (icon.getIconWidth() + 2);
                g.fillRect(0, 0, right, getHeight());

                Insets ins = getInsets();
                int x = ins.left;
                int y = ins.top + Math.max((getHeight() / 2) - (icon.getIconHeight() / 2), 0);
                icon.paintIcon(this, g, x, y);
            } finally {
                g.setColor(c);
            }
        }

        super.paintComponent(g);
    }

    /** Proxies the embedded inplace editor */
    public void addActionListener(java.awt.event.ActionListener al) {
        inplaceEditor.addActionListener(al);
    }

    /** Proxies the embedded inplace editor */
    public void clear() {
        inplaceEditor.clear();
        setIcon(null);
        setComponent(null);
        env = null;
    }

    /** Proxies the embedded inplace editor */
    public void connect(java.beans.PropertyEditor pe, PropertyEnv env) {
        inplaceEditor.connect(pe, env);
        this.env = env;
        updateIcon();
    }

    private void updateIcon() {
        if (env != null) {
            Icon ic = null;
            FeatureDescriptor fd = env.getFeatureDescriptor();

            if (env.getState() == PropertyEnv.STATE_INVALID) {
                ic = ImageUtilities.loadImageIcon("org/openide/resources/propertysheet/invalid.gif", false); //NOI18N
            } else if (fd != null) {
                ic = (Icon) fd.getValue("valueIcon"); //NOI18N
            }

            setIcon(ic);
            needLayout = true;
        }
    }

    @Override
    public void setOpaque(boolean val) {
        if (getInplaceEditor() != null) {
            getInplaceEditor().getComponent().setOpaque(true);
        }
    }

    /** Proxies the embedded inplace editor */
    public javax.swing.JComponent getComponent() {
        return this;
    }

    /** Proxies the embedded inplace editor */
    public javax.swing.KeyStroke[] getKeyStrokes() {
        return inplaceEditor.getKeyStrokes();
    }

    /** Proxies the embedded inplace editor */
    public java.beans.PropertyEditor getPropertyEditor() {
        return inplaceEditor.getPropertyEditor();
    }

    /** Proxies the embedded inplace editor */
    public PropertyModel getPropertyModel() {
        return inplaceEditor.getPropertyModel();
    }

    /** Proxies the embedded inplace editor */
    public Object getValue() {
        return inplaceEditor.getValue();
    }

    /** Proxies the embedded inplace editor */
    public boolean isKnownComponent(java.awt.Component c) {
        return ((c == this) || inplaceEditor.isKnownComponent(c));
    }

    /** Proxies the embedded inplace editor */
    public void removeActionListener(java.awt.event.ActionListener al) {
        inplaceEditor.removeActionListener(al);
    }

    /** Proxies the embedded inplace editor */
    public void reset() {
        inplaceEditor.reset();
        updateIcon();
    }

    /** Proxies the embedded inplace editor */
    public void setPropertyModel(PropertyModel pm) {
        inplaceEditor.setPropertyModel(pm);
    }

    /** Proxies the embedded inplace editor */
    public void setValue(Object o) {
        inplaceEditor.setValue(o);
    }

    /** Proxies the embedded inplace editor */
    public boolean supportsTextEntry() {
        return inplaceEditor.supportsTextEntry();
    }

    @Override
    public void requestFocus() {
        comp.requestFocus();
    }

    @Override
    public boolean requestFocusInWindow() {
        return comp.requestFocusInWindow();
    }

    @Override
    public void addFocusListener(FocusListener fl) {
        if (comp != null) {
            comp.addFocusListener(fl);
        } else {
            super.addFocusListener(fl);
        }
    }

    @Override
    public void removeFocusListener(FocusListener fl) {
        if (comp != null) {
            comp.removeFocusListener(fl);
        } else {
            super.removeFocusListener(fl);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void layout() {
        Insets ins = getInsets();

        //use a minimum size so typical icons won't cause resizing of the
        //component
        int iconWidth = Math.max(icon.getIconWidth() + PropUtils.getTextMargin(), 18);

        int x = (icon == null) ? ins.left : (ins.left + iconWidth);
        int y = ins.top;

        synchronized (getTreeLock()) {
            Component c = comp;

            if (c == null) {
                return;
            }

            c.setBounds(x, y, getWidth() - (x + ins.right), getHeight() - ins.bottom);

            if (c instanceof Container) {
                ((Container) c).doLayout();
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Insets ins = getInsets();
        Component c = comp;
        Dimension result = new Dimension(0, 0);

        if (icon != null) {
            result.width = icon.getIconWidth() + PropUtils.getTextMargin();
            result.height = icon.getIconHeight();
        }

        if (c != null) {
            Dimension ps = c.getPreferredSize();
            result.width += ps.width;
            result.height = Math.max(ps.height, result.height);
        }

        result.width += (ins.left + ins.right);
        result.height += (ins.top + ins.bottom);

        return result;
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
}
