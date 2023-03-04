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
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;


/**
 *
 * copied from editor/options.
 * @author theofanis
 */
public class ColorComboBox {
    
    public static final String PROP_COLOR = "color"; //NOI18N
    
    private static ColorValue[] content = new ColorValue[] {
	new ColorValue (Color.BLACK), 
	new ColorValue (Color.BLUE), 
	new ColorValue (Color.CYAN), 
	new ColorValue (Color.DARK_GRAY), 
	new ColorValue (Color.GRAY), 
	new ColorValue (Color.GREEN), 
	new ColorValue (Color.LIGHT_GRAY), 
	new ColorValue (Color.MAGENTA), 
	new ColorValue (Color.ORANGE), 
	new ColorValue (Color.PINK), 
	new ColorValue (Color.RED), 
	new ColorValue (Color.WHITE), 
	new ColorValue (Color.YELLOW), 
	ColorValue.CUSTOM_COLOR, 
	new ColorValue (loc ("CTL_None_Color"), null)                  //NOI18N
    };
    
    
    /** Creates a new instance of ColorChooser */
    static void init (final JComboBox<ColorValue> combo) {
        combo.setModel (new DefaultComboBoxModel<> (content));
        combo.setRenderer (new ColorComboBoxRenderer (combo));
        combo.setEditable (true);
        combo.setEditor (new ColorComboBoxRenderer (combo));
	combo.setSelectedItem (new ColorValue (null, null));
        combo.addActionListener (new ComboBoxListener (combo));
    }
    
    static void setInheritedColor (JComboBox<ColorValue> combo, Color color) {
	ColorValue[] ncontent = new ColorValue [content.length];
	System.arraycopy (content, 0, ncontent, 0, content.length);
        if (color != null)
            ncontent [content.length - 1] = new ColorValue (
                loc ("CTL_Inherited_Color"), color                   //NOI18N
            );
        else
            ncontent [content.length - 1] = new ColorValue (
                loc ("CTL_None_Color"), null                       //NOI18N
            );
	combo.setModel (new DefaultComboBoxModel<> (ncontent));
    }
    
    static void setColor (JComboBox<ColorValue> combo, Color color) {
        if (color == null) {
            combo.setSelectedIndex (content.length - 1);
        } else {
            combo.setSelectedItem (new ColorValue (color));
        }
    }
    
    static Color getColor (JComboBox<ColorValue> combo) {
        // The last item is Inherited Color or None
        if (combo.getSelectedIndex() < combo.getItemCount() - 1) {
            return ((ColorValue) combo.getSelectedItem()).color;
        } else {
            return null;
        }
    }
    
    private static String loc (String key) {
        return Catalog.get(key);
    }
    
    // ..........................................................................
    private static class ComboBoxListener implements ActionListener {
        
        private JComboBox<ColorValue> combo;
        private Object lastSelection;
        
        ComboBoxListener(JComboBox<ColorValue> combo) {
            this.combo = combo;
            lastSelection = combo.getSelectedItem();
        }
        
        @Override
        public void actionPerformed(ActionEvent ev) {
            if (combo.getSelectedItem() == ColorValue.CUSTOM_COLOR) {
                Color c = JColorChooser.showDialog(
                    SwingUtilities.getAncestorOfClass(Dialog.class, combo),
                    loc("SelectColor"), //NOI18N
                    lastSelection != null ? ((ColorValue) lastSelection).color : null
                );
                if (c != null) {
                    setColor (combo, c);
                } else if (lastSelection != null) {
                    combo.setSelectedItem(lastSelection);
                }
            }
            lastSelection = combo.getSelectedItem();
        }
        
    } // ComboListener
    
}
