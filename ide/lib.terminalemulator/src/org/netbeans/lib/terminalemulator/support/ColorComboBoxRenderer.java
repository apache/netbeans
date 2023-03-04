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

package org.netbeans.lib.terminalemulator.support;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.SystemColor;
import java.awt.event.ActionListener;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.openide.awt.GraphicsUtils;


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
        GraphicsUtils.configureDefaultRenderingHints(g);

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
