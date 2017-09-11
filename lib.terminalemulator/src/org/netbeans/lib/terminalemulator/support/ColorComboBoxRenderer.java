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

package org.netbeans.lib.terminalemulator.support;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


/**
 * Renderer and editor for color JComboBox.
 *
 * copied from editor/options.
 * @author theofanis
 */
class ColorComboBoxRenderer extends JComponent implements
ListCellRenderer<ColorValue>, ComboBoxEditor {

    private int             SIZE = 9;
    private ColorValue      value;
    private JComboBox<ColorValue>
                            comboBox;

    ColorComboBoxRenderer (JComboBox<ColorValue> comboBox) {
        this.comboBox = comboBox;
        setPreferredSize (new Dimension (
            50, 
            comboBox.getFontMetrics (comboBox.getFont ()).
                getHeight () + 2
        ));
        setOpaque (true);
        setFocusable (true);
    }

    @Override
    public void paint (Graphics g) {
        
        //AntiAliasing check
        @SuppressWarnings("unchecked") //NOI18N
        Map<?, ?> aa = (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints"); //NOI18N

        if (aa != null) {
            ((Graphics2D) g).setRenderingHints(aa);
        }
        
        Color oldColor = g.getColor ();
        Dimension size = getSize ();
        if (isFocusOwner ()) {
            g.setColor (SystemColor.textHighlight);
        } else {
            g.setColor (getBackground ());
        }
        g.fillRect (0, 0, size.width, size.height);

        if (value != null) {
            int i = (size.height - SIZE) / 2;
            if (value.color != null) {
                g.setColor (Color.black);
                g.drawRect (i, i, SIZE, SIZE);
                g.setColor (value.color);
                g.fillRect (i + 1, i + 1, SIZE - 1, SIZE - 1);
            }
            if (value.text != null) {
                if (isFocusOwner ()) {
                    g.setColor (SystemColor.textHighlightText);
                } else {
                    g.setColor (getForeground ());
                }
                if (value.color != null) {
                    g.drawString (value.text, i + SIZE + 5, i + SIZE);
                } else {
                    g.drawString (value.text, 5, i + SIZE);
                }
            }
        }

        g.setColor (oldColor);
    }

    public @Override void setEnabled (boolean enabled) {
        setBackground (enabled ? 
            SystemColor.text : SystemColor.control
        );
        super.setEnabled (enabled);
    }

    @Override
    public Component getListCellRendererComponent (
        JList<? extends ColorValue>
                    list,
        ColorValue  value,
        int         index,
        boolean     isSelected,
        boolean     cellHasFocus
    ) {
        this.value = value;
        setEnabled (list.isEnabled ());
        setBackground (isSelected ? 
            SystemColor.textHighlight : SystemColor.text
            //Color.RED
        );
        setForeground (isSelected ? 
            SystemColor.textHighlightText : SystemColor.textText
        );
        return this;
    }

    @Override
    public Component getEditorComponent () {
        setEnabled (comboBox.isEnabled ());
        setBackground (comboBox.isFocusOwner () ? 
            SystemColor.textHighlight : SystemColor.text
        );
        setForeground (comboBox.isFocusOwner () ? 
            SystemColor.textHighlightText : SystemColor.textText
        );
        return this;
    }

    @Override
    public void setItem (Object anObject) {
        Object oldValue = this.value;
        this.value = (ColorValue) anObject;
        firePropertyChange(ColorComboBox.PROP_COLOR, oldValue, anObject);
    }

    @Override
    public Object getItem () {
        return value;
    }
    
    @Override
    public void selectAll() {}
    @Override
    public void addActionListener (ActionListener l) {}
    @Override
    public void removeActionListener (ActionListener l) {}   
}
