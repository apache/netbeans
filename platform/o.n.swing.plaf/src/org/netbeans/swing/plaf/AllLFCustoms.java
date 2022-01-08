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

package org.netbeans.swing.plaf;

import org.netbeans.swing.plaf.util.GuaranteedValue;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.plaf.metal.MetalLookAndFeel;

/** Customization for all LFs. */
final class AllLFCustoms extends LFCustoms {

    public Object[] createApplicationSpecificKeysAndValues () {
        Object[] uiDefaults = {

            //Tab control in case of unknown look and feel
            TAB_ACTIVE_SELECTION_BACKGROUND,
                new GuaranteedValue (new String[] {"Table.selectionBackground",
                "info"}, Color.BLUE.brighter()),

            TAB_ACTIVE_SELECTION_FOREGROUND,
                new GuaranteedValue ("Table.selectionForeground",
                Color.WHITE),

            TAB_SELECTION_FOREGROUND,
                new GuaranteedValue("textText", Color.BLACK),

            //Likely to be the same for all look and feels - doesn't do anything
            //exciting
            EDITOR_TABBED_CONTAINER_UI,
                "org.netbeans.swing.tabcontrol.plaf.DefaultTabbedContainerUI",

            SLIDING_TAB_DISPLAYER_UI,
                "org.netbeans.swing.tabcontrol.plaf.BasicSlidingTabDisplayerUI",
            
            SLIDING_TAB_BUTTON_UI,
                "org.netbeans.swing.tabcontrol.plaf.SlidingTabDisplayerButtonUI",

            SLIDING_BUTTON_UI, "org.netbeans.swing.tabcontrol.SlidingButtonUI", //NOI18N
                
        
            SCROLLPANE_BORDER_COLOR, new Color(127, 157, 185),
                        
            EDITOR_ERRORSTRIPE_SCROLLBAR_INSETS, new Insets(0, 0, 0, 0),

            SPLIT_PANE_DIVIDER_SIZE_VERTICAL, 4,
            SPLIT_PANE_DIVIDER_SIZE_HORIZONTAL, 4
        }; //NOI18N
        return uiDefaults;
    }

    public Object[] createGuaranteedKeysAndValues () {
        //ColorUIResource errorColor = new ColorUIResource(89, 79, 191);
        // 65358: asked Red color for error messages
        ColorUIResource errorColor = new ColorUIResource (255, 0, 0);
        //#204598 - there's no cross-platform warning-like color...
        ColorUIResource warningColor = new ColorUIResource(51 , 51, 51);

        int fontsize = 11;
        Integer in = (Integer) UIManager.get(CUSTOM_FONT_SIZE); //NOI18N
        boolean hasCustomFontSize = in != null;
        if (hasCustomFontSize) {
            fontsize = in.intValue();
        }
        Object[] uiDefaults = {
            //XXX once jdk 1.5 b2 is out, these can be deleted
            
            "control", new GuaranteedValue ("control", Color.LIGHT_GRAY),
            "controlShadow", new GuaranteedValue ("controlShadow", Color.GRAY),
            "controlDkShadow", new GuaranteedValue ("controlDkShadow", Color.DARK_GRAY),
            "textText", new GuaranteedValue ("textText", Color.BLACK),
            "controlFont", new GuaranteedValue ("controlFont",
                new Font ("Dialog", Font.PLAIN, fontsize)),
            
            DEFAULT_FONT_SIZE, 11,
            ERROR_FOREGROUND, new GuaranteedValue(ERROR_FOREGROUND, errorColor),

            WARNING_FOREGROUND, new GuaranteedValue(WARNING_FOREGROUND, warningColor ),

        };
        return uiDefaults;
    }

    public static void initCustomFontSize (int uiFontSize) {
        Font nbDialogPlain = new FontUIResource("Dialog", Font.PLAIN, uiFontSize); // NOI18N
        Font nbDialogBold = new FontUIResource("Dialog", Font.BOLD, uiFontSize); // NOI18N
        Font nbSerifPlain = new FontUIResource("Serif", Font.PLAIN, uiFontSize); // NOI18N
        Font nbSansSerifPlain = new FontUIResource("SansSerif", Font.PLAIN, uiFontSize); // NOI18N
        Font nbMonospacedPlain = new FontUIResource("Monospaced", Font.PLAIN, uiFontSize); // NOI18N
        
        Map<Font, Font> fontTranslation = new HashMap<Font, Font>(5);
        
        if( "Nimbus".equals( UIManager.getLookAndFeel().getID() ) ) { //NOI18N
            switchFont("defaultFont", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        }
        switchFont("controlFont", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("Button.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("ToggleButton.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("RadioButton.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("CheckBox.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("ColorChooser.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("ComboBox.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("Label.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("List.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("FileChooser.listFont", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("MenuBar.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("MenuItem.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("MenuItem.acceleratorFont", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("RadioButtonMenuItem.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("CheckBoxMenuItem.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("Menu.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("PopupMenu.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("OptionPane.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("OptionPane.messageFont", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("Panel.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("ProgressBar.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("ScrollPane.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("Viewport.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("TabbedPane.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("Table.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("TableHeader.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("TextField.font", fontTranslation, uiFontSize, nbSansSerifPlain); // NOI18N
        switchFont("PasswordField.font", fontTranslation, uiFontSize, nbMonospacedPlain); // NOI18N
        switchFont("TextArea.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("TextPane.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("EditorPane.font", fontTranslation, uiFontSize, nbSerifPlain); // NOI18N
        switchFont("TitledBorder.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("ToolBar.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("ToolTip.font", fontTranslation, uiFontSize, nbSansSerifPlain); // NOI18N
        switchFont("Tree.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("InternalFrame.titleFont", fontTranslation, uiFontSize, nbDialogBold); // NOI18N
        switchFont("windowTitleFont", fontTranslation, uiFontSize, nbDialogBold); // NOI18N
        switchFont("Spinner.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("FormattedTextField.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
        switchFont("Slider.font", fontTranslation, uiFontSize, nbDialogPlain); // NOI18N
    }

    //#144402
    private static final boolean isMetal = null != UIManager.getLookAndFeel() 
            && UIManager.getLookAndFeel().getClass() == MetalLookAndFeel.class;
    private static final boolean isWindows = null != UIManager.getLookAndFeel() 
            && "Windows".equals( UIManager.getLookAndFeel().getID() );
    
    private static void switchFont( String uiKey, Map<Font, Font> fontTranslation, int uiFontSize, Font defaultFont ) {
        Font oldFont = UIManager.getFont(uiKey);
        Font newFont = (null == oldFont || isMetal) ? defaultFont : fontTranslation.get(oldFont);
        if( null == newFont ) {
            if( isWindows ) {
                newFont = oldFont.deriveFont( (float)uiFontSize );
            } else {
                newFont = new FontUIResource( oldFont.getFontName(), oldFont.getStyle(), uiFontSize );
            }
            fontTranslation.put( oldFont, newFont );
        }
        UIManager.put( uiKey, newFont );
    }
}
