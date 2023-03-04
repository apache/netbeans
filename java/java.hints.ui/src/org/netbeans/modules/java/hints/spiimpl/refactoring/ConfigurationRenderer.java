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
