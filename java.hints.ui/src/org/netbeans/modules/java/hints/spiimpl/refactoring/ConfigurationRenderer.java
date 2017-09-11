/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.spiimpl.refactoring;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.UIResource;

/**
 *
 * @author Jan Becicka
 */
public class ConfigurationRenderer extends JLabel implements ListCellRenderer, UIResource {

    public ConfigurationRenderer() {
        setOpaque(true);
    }

    private boolean bordersInitialized;
    private Border originalBorder;
    private Border separatorBorder;
    
    @Override
    public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        
        if (value != null) {
            if (value instanceof Configuration) {
                setText(((Configuration) value).getDisplayName());
            } else {
                setText(value.toString());
            }
        }
    
        if (!bordersInitialized) {
            //#222894: the original border must be kept for GTK
            originalBorder = getBorder();
            Separator separator = new Separator(list.getForeground());
            if (originalBorder != null) separatorBorder = new CompoundBorder(originalBorder, separator);
            else separatorBorder = separator;
            bordersInitialized = true;
        }
        
        if (index == list.getModel().getSize()-5 && ((ConfigurationsComboModel) list.getModel()).canModify() ) {
            setBorder(separatorBorder);
        } else {
            setBorder(originalBorder);
        }
        
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        // #89393: GTK needs name to render cell renderer "natively"
        setName("ComboBox.listRenderer"); // NOI18N
        return this;
    }
    
    // #89393: GTK needs name to render cell renderer "natively"
    @Override
    public String getName() {
        String name = super.getName();
        return name == null ? "ComboBox.renderer" : name;  // NOI18N
    }
    
    
    private class Separator implements Border {

        private Color fgColor;

        Separator(Color color) {
            fgColor = color;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics gr = g.create();
            if (gr != null) {
                try {
                    gr.translate(x, y);
                    gr.setColor(fgColor);
                    gr.drawLine(0, height - 1, width - 1, height - 1);
                } finally {
                    gr.dispose();
                }
            }
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 1, 0);
        }
    }
}
