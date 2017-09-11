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
