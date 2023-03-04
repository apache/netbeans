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

package org.openide.awt;

import java.awt.Component;
import javax.swing.*;
import javax.swing.plaf.UIResource;


/**
 * Renderer for color JComboBox.
 *
 * @author S. Aubrecht
 */
class ColorComboBoxRendererWrapper implements ListCellRenderer, UIResource {

    private final ListCellRenderer renderer;
    private static final boolean isGTK = "GTK".equals(UIManager.getLookAndFeel().getID()); //NOI18N

    ColorComboBoxRendererWrapper (JComboBox comboBox) {
        this.renderer = comboBox.getRenderer();
        if( renderer instanceof ColorComboBoxRendererWrapper ) {
            throw new IllegalStateException("Custom renderer is already initialized."); //NOI18N
        }
        comboBox.setRenderer( this );
    }

    @Override
    public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus ) {
        Component res = renderer.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
        if( res instanceof JLabel ) {
            synchronized( renderer ) {
                JLabel label = ( JLabel ) res;
                int height = isGTK ? 10 : Math.max( res.getPreferredSize().height - 4, 4 );
                Icon icon = null;
                if( value instanceof ColorValue ) {
                    ColorValue color = ( ColorValue ) value;
                    if( value == ColorValue.CUSTOM_COLOR ) {
                        icon = null;
                    } else {
                        icon = new ColorIcon( color.color, height );
                    }
                    label.setText( color.text );
                } else {
                    icon = null;
                }
                label.setIcon( icon );
            }
        }
        return res;
    }


}