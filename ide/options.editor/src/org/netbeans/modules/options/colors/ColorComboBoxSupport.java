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

package org.netbeans.modules.options.colors;

import java.awt.Color;
import java.util.HashSet;
import org.openide.awt.ColorComboBox;
import org.openide.util.NbBundle;


/**
 * Utility class to manipulate 'Inherited' colors in ColorComboBox.
 * @author Administrator
 * @author S. Aubrecht
 * @see ColorComboBox
 */
public class ColorComboBoxSupport {
    
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
	new ColorValue (loc ("CTL_None_Color"), null)                  //NOI18N
    };
    
    private static final HashSet<ColorComboBox> cbWithInheritedColor = new HashSet<ColorComboBox>();
    
    /**
     * Fill given combo box with some basic colors.
     * @param combo
     * @param color 'Inherited' color to add to the end of colors list.
     */
    static void setInheritedColor (ColorComboBox combo, Color color) {
	ColorValue[] ncontent = new ColorValue [content.length];
	System.arraycopy (content, 0, ncontent, 0, content.length);
        Color[] colors = new Color[content.length];
        String[] names = new String[content.length];
        for( int i=0; i<colors.length; i++ ) {
            colors[i] = content[i].color;
            names[i] = content[i].text;
        }
        if (color != null) {
            colors[content.length - 1] = color;
            names[content.length - 1] = loc ("CTL_Inherited_Color"); //NOI18N
            cbWithInheritedColor.add(combo);
        } else {
            colors[content.length - 1] = null;
            names[content.length - 1] = loc ("CTL_None_Color"); //NOI18N
        }
	combo.setModel (colors, names);
    }

    /**
     * Make given color the selected one in the combo box.
     * @param combo
     * @param color Color to select or null to select 'Inherited' color.
     */
    static void setSelectedColor(ColorComboBox combo, Color color) {
        if( null != color ) {
            combo.setSelectedColor( color );
        } else {
            //select inherited color
            combo.setSelectedIndex(combo.getItemCount()-2);
        }
    }

    /**
     * Retrieve selected color from the combo box.
     * @param combo
     * @return Selected color or null if 'Inherited' color is selected.
     */
    static Color getSelectedColor(ColorComboBox combo) {
        int selIndex = combo.getSelectedIndex();
        if (selIndex == combo.getItemCount() - 2 && cbWithInheritedColor.contains(combo))
            return null; //inherited color
        return combo.getSelectedColor();
    }

    private static String loc (String key) {
        return NbBundle.getMessage (ColorComboBoxSupport.class, key);
    }
}
