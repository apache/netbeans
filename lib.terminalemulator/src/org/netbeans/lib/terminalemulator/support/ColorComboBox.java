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
