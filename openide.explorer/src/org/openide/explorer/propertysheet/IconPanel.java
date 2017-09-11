/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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

    public void setEnabled(boolean val) {
        if (comp != null) {
            //Can be called from setUI in superclass constructor
            comp.setEnabled(val);
        }

        super.setEnabled(val);
    }

    public void setBackground(Color c) {
        if (comp != null) {
            //Can be called from setUI in superclass constructor
            comp.setBackground(c);
        }

        super.setBackground(c);
    }

    public void setForeground(Color c) {
        if (comp != null) {
            //Can be called from setUI in superclass constructor
            comp.setForeground(c);
        }

        super.setForeground(c);
    }

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

            if (env.getState() == env.STATE_INVALID) {
                ic = ImageUtilities.loadImageIcon("org/openide/resources/propertysheet/invalid.gif", false); //NOI18N
            } else if (fd != null) {
                ic = (Icon) fd.getValue("valueIcon"); //NOI18N
            }

            setIcon(ic);
            needLayout = true;
        }
    }

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

    public void requestFocus() {
        comp.requestFocus();
    }

    public boolean requestFocusInWindow() {
        return comp.requestFocusInWindow();
    }

    public void addFocusListener(FocusListener fl) {
        if (comp != null) {
            comp.addFocusListener(fl);
        } else {
            super.addFocusListener(fl);
        }
    }

    public void removeFocusListener(FocusListener fl) {
        if (comp != null) {
            comp.removeFocusListener(fl);
        } else {
            super.removeFocusListener(fl);
        }
    }

    @SuppressWarnings("deprecation")
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

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
}
