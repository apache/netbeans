/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.openide.awt;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import org.openide.util.NbBundle;

/**
 * Combo box showing a list of Color values to choose from. Optionally users can
 * also pick a custom color using JColorChooser dialog.
 * 
 * @author S. Aubrecht
 * @since 7.50
 */
public final class ColorComboBox extends JComboBox {

    private final boolean allowCustomColors;

    private ColorValue lastSelection;

    /**
     * C'tor
     * The combo box is initialized with some basic colors and user can also
     * pick a custom color
     */
    public ColorComboBox() {
        this( new Color[] {
            Color.BLACK,
            Color.BLUE,
            Color.CYAN,
            Color.DARK_GRAY,
            Color.GRAY,
            Color.GREEN,
            Color.LIGHT_GRAY,
            Color.MAGENTA,
            Color.ORANGE,
            Color.PINK,
            Color.RED,
            Color.WHITE,
            Color.YELLOW,
            }, new String[0], true);
    }

    /**
     * Initialize the combo with given list of Colors.
     * @param values Color values.
     * @param names Name of colors.
     * @param allowCustomColors True to allow users to pick a custom colors,
     * false if user can choose from given colors only.
     */
    public ColorComboBox( Color[] values, String[] names, boolean allowCustomColors ) {
        super.setModel( createModel(values, names, allowCustomColors) );
        this.allowCustomColors = allowCustomColors;
        setEditable( false );
        if( allowCustomColors ) {
            addItemListener( new ItemListener() {

                @Override
                public void itemStateChanged( ItemEvent e ) {
                    if( e.getStateChange() == ItemEvent.SELECTED ) {
                        SwingUtilities.invokeLater( new Runnable() {
                            @Override
                            public void run() {
                                if( getSelectedItem() == ColorValue.CUSTOM_COLOR ) {
                                    pickCustomColor();
                                }
                                lastSelection = ( ColorValue ) getSelectedItem();
                            }
                        } );
                    }
                }
            });
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        setRenderer( new ColorComboBoxRendererWrapper( this ) );
    }

    /**
     * Change the combo content.
     * @param colors Colors to show in the combo box.
     * @param names  Names of the colors.
     */
    public void setModel( Color[] colors, String[] names ) {
        super.setModel( createModel( colors, names, allowCustomColors ) );
        SwingUtilities.invokeLater( new Runnable() {

            @Override
            public void run() {
                repaint();
            }
        });
    }

    /**
     * Retrieve currently selected color.
     * @return Selected Color or null.
     */
    public Color getSelectedColor() {
        ColorValue cv = ( ColorValue ) getSelectedItem();
        return null == cv ? null : cv.color;
    }

    /**
     * Select given in the combo box.
     * @param newColor Color to be selected or null to clear selection.
     * If the color isn't in the combo box list and custom colors are not allowed
     * the selection does not change.
     * @see #ColorComboBox(java.awt.Color[], java.lang.String[], boolean)
     */
    public void setSelectedColor( Color newColor ) {
        if( null == newColor ) {
            setSelectedIndex( -1 );
            return;
        }
        for( int i=0; i<getItemCount(); i++ ) {
            ColorValue cv = ( ColorValue ) getItemAt( i );
            if( newColor.equals( cv.color ) ) {
                setSelectedItem( cv );
                return;
            }
        }
        if( allowCustomColors ) {
            removeCustomValue();
            ColorValue cv = new ColorValue( newColor, true );
            DefaultComboBoxModel model = ( DefaultComboBoxModel ) getModel();
            model.insertElementAt( cv, 0 );
            setSelectedItem( cv );
        }
    }

    private void removeCustomValue() {
        for( int i=0; i<getItemCount(); i++ ) {
             ColorValue cv = ( ColorValue ) getItemAt( i );
             if( cv.isCustom ) {
                 DefaultComboBoxModel model = ( DefaultComboBoxModel ) getModel();
                 model.removeElementAt( i );
                 return;
             }
        }
    }

    private void pickCustomColor() {
        Color c = JColorChooser.showDialog(
                SwingUtilities.getAncestorOfClass( Dialog.class, this ),
                NbBundle.getMessage( ColorComboBox.class, "SelectColor" ), //NOI18N
                lastSelection != null ? lastSelection.color : null );
        if( c != null ) {
            setSelectedColor( c );
        } else if( lastSelection != null ) {
            setSelectedItem( lastSelection );
        }
    }

    private static DefaultComboBoxModel<ColorValue> createModel( Color[] colors, String[] names, boolean allowCustomColors ) {
        DefaultComboBoxModel<ColorValue> model = new DefaultComboBoxModel<>();

        for( int i=0; i<colors.length; i++ ) {
            Color c = colors[i];
            String text = null;
            if( i < names.length ) {
                text = names[i];
            }
            if( null == text ) {
                text = ColorValue.toText( c );
            }
            model.addElement( new ColorValue( text, c, false ) );
        }
        if( allowCustomColors ) {
            model.addElement( ColorValue.CUSTOM_COLOR );
        }
        return model;
    }
}
