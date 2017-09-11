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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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
package org.netbeans.modules.spellchecker.options;

import java.awt.Component;
import java.awt.Color;
import java.awt.Rectangle;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;


public class CheckBoxRenderrer extends JCheckBox implements ListCellRenderer, Serializable {

    private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder (1, 1, 1, 1);
    private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder (1, 1, 1, 1);

    protected static Border     noFocusBorder = DEFAULT_NO_FOCUS_BORDER;

    public CheckBoxRenderrer () {
        super ();
        setOpaque (true);
        setBorder (getNoFocusBorder ());
        setName ("List.cellRenderer");
    }

    private Border getNoFocusBorder () {
        Border border = UIManager.getBorder("List.cellNoFocusBorder");
        if (System.getSecurityManager () != null) {
            if (border != null) {
                return border;
            }
            return SAFE_NO_FOCUS_BORDER;
        } else {
            if (border != null &&
                (noFocusBorder == null ||
                noFocusBorder == DEFAULT_NO_FOCUS_BORDER)) {
                return border;
            }
            return noFocusBorder;
        }
    }

    public Component getListCellRendererComponent (
        JList                   list,
        Object                  value,
        int                     index,
        boolean                 isSelected,
        boolean                 cellHasFocus
    ) {
        setComponentOrientation (list.getComponentOrientation ());

        Color bg = null;
        Color fg = null;

        JList.DropLocation dropLocation = list.getDropLocation ();
        if (dropLocation != null && !dropLocation.isInsert () && dropLocation.getIndex () == index) {

            bg = UIManager.getColor("List.dropCellBackground");
            fg = UIManager.getColor("List.dropCellForeground");

            isSelected = true;
        }

        if (isSelected) {
            setBackground (bg == null ? list.getSelectionBackground () : bg);
            setForeground (fg == null ? list.getSelectionForeground () : fg);
        } else {
            setBackground (list.getBackground ());
            setForeground (list.getForeground ());
        }

        String name = (String) value;
        setText (name.substring (1));
        setSelected (name.charAt (0) == '+');

        setEnabled (list.isEnabled ());
        setFont (list.getFont ());

        Border border = null;
        if (cellHasFocus) {
            if (isSelected) {
                border = UIManager.getBorder("List.focusSelectedCellHighlightBorder");
            }
            if (border == null) {
                border = UIManager.getBorder("List.focusCellHighlightBorder");
            }
        } else {
            border = getNoFocusBorder ();
        }
        if (border != null) { //#189786: rarely, the border is null - reasons are unknown
            setBorder (border);
        } else {
            Logger.getLogger(CheckBoxRenderrer.class.getName()).log(Level.INFO, "Cannot set any border");
        }

        return this;
    }

    @Override
    public boolean isOpaque () {
        Color back = getBackground ();
        Component p = getParent ();
        if (p != null) {
            p = p.getParent ();
        }
        // p should now be the JList.
        boolean colorMatch = (back != null) && (p != null) &&
            back.equals (p.getBackground ()) &&
            p.isOpaque ();
        return !colorMatch && super.isOpaque ();
    }

    @Override
    public void validate () {
    }

    @Override
    public void invalidate () {
    }

    @Override
    public void repaint () {
    }

    @Override
    public void revalidate () {
    }

    @Override
    public void repaint (long tm, int x, int y, int width, int height) {
    }

    @Override
    public void repaint (Rectangle r) {
    }

    @Override
    protected void firePropertyChange (String propertyName, Object oldValue, Object newValue) {
        // Strings get interned...
        if (propertyName == "text" || ((propertyName == "font" || propertyName == "foreground") &&
            oldValue != newValue &&
            getClientProperty (javax.swing.plaf.basic.BasicHTML.propertyKey) != null)
        ) {
            super.firePropertyChange (propertyName, oldValue, newValue);
        }
    }

    @Override
    public void firePropertyChange (String propertyName, byte oldValue, byte newValue) {
    }

    @Override
    public void firePropertyChange (String propertyName, char oldValue, char newValue) {
    }

    @Override
    public void firePropertyChange (String propertyName, short oldValue, short newValue) {
    }

    @Override
    public void firePropertyChange (String propertyName, int oldValue, int newValue) {
    }

    @Override
    public void firePropertyChange (String propertyName, long oldValue, long newValue) {
    }

    @Override
    public void firePropertyChange (String propertyName, float oldValue, float newValue) {
    }

    @Override
    public void firePropertyChange (String propertyName, double oldValue, double newValue) {
    }

    @Override
    public void firePropertyChange (String propertyName, boolean oldValue, boolean newValue) {
    }
}
