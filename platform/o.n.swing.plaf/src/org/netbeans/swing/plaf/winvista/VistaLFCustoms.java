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

package org.netbeans.swing.plaf.winvista;

import javax.swing.plaf.ColorUIResource;
import org.netbeans.swing.plaf.LFCustoms;
import org.netbeans.swing.plaf.util.GuaranteedValue;
import org.netbeans.swing.plaf.util.UIBootstrapValue;
import org.netbeans.swing.plaf.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;


/** Default system-provided customizer for Windows Vista LF 
 * Public only to be accessible by ProxyLazyValue, please don't abuse.
 */
public final class VistaLFCustoms extends LFCustoms {
    private static final String TAB_FOCUS_FILL_UPPER = "tab_focus_fill_upper"; //NOI18N    
    private static final String TAB_FOCUS_FILL_DARK_LOWER = "tab_focus_fill_dark_lower"; //NOI18N    
    private static final String TAB_FOCUS_FILL_BRIGHT_LOWER = "tab_focus_fill_bright_lower"; //NOI18N 
    
    private static final String TAB_UNSEL_FILL_DARK_UPPER = "tab_unsel_fill_dark_upper"; //NOI18N
    private static final String TAB_UNSEL_FILL_BRIGHT_UPPER = "tab_unsel_fill_bright_upper"; //NOI18N  
    private static final String TAB_UNSEL_FILL_DARK_LOWER = "tab_unsel_fill_dark_lower"; //NOI18N
    private static final String TAB_UNSEL_FILL_BRIGHT_LOWER = "tab_unsel_fill_bright_lower"; //NOI18N  

    private static final String TAB_SEL_FILL = "tab_sel_fill"; //NOI18N
    
    private static final String TAB_MOUSE_OVER_FILL_BRIGHT_UPPER = "tab_mouse_over_fill_bright_upper"; //NOI18N
    private static final String TAB_MOUSE_OVER_FILL_DARK_UPPER = "tab_mouse_over_fill_dark_upper"; //NOI18N
    private static final String TAB_MOUSE_OVER_FILL_BRIGHT_LOWER = "tab_mouse_over_fill_bright_lower"; //NOI18N
    private static final String TAB_MOUSE_OVER_FILL_DARK_LOWER = "tab_mouse_over_fill_dark_lower"; //NOI18N

    private static final String TAB_BORDER = "tab_border"; //NOI18N      
    private static final String TAB_SEL_BORDER = "tab_sel_border"; //NOI18N
    private static final String TAB_BORDER_INNER = "tab_border_inner"; //NOI18N      
    
    static final String SCROLLPANE_BORDER_COLOR = "scrollpane_border"; //NOI18N

    @Override
    public Object[] createLookAndFeelCustomizationKeysAndValues() {
        int fontsize = 11;
        Integer in = (Integer) UIManager.get(CUSTOM_FONT_SIZE); //NOI18N
        if (in != null) {
            fontsize = in.intValue();
        }
        
        Object[] result = new Object[] {
            //Work around a bug in windows which sets the text area font to
            //"MonoSpaced", causing all accessible dialogs to have monospaced text
            "TextArea.font", new GuaranteedValue ("Label.font", new Font("Dialog", Font.PLAIN, fontsize)),

            EDITOR_ERRORSTRIPE_SCROLLBAR_INSETS, new Insets(17, 0, 17, 0),
        };
        
        return result;
    }

    @Override
    public Object[] createApplicationSpecificKeysAndValues () {
        UIBootstrapValue editorTabsUI = new VistaEditorColorings (
                "org.netbeans.swing.tabcontrol.plaf.WinVistaEditorTabDisplayerUI");

        Object viewTabsUI = editorTabsUI.createShared("org.netbeans.swing.tabcontrol.plaf.WinVistaViewTabDisplayerUI");

        //TODO change icon (copy & paste)
        Image explorerIcon = UIUtils.loadImage("org/netbeans/swing/plaf/resources/vista_folder.png");

        Object propertySheetValues = new VistaPropertySheetColorings();

        Object[] uiDefaults = {
            EDITOR_PREFERRED_COLOR_PROFILE, "NetBeans", //NOI18N
            EDITOR_TAB_DISPLAYER_UI, editorTabsUI,
            VIEW_TAB_DISPLAYER_UI, viewTabsUI,
            
            DESKTOP_BACKGROUND, new Color(226, 223, 214), //NOI18N
            SCROLLPANE_BORDER_COLOR, new Color(127, 157, 185),
            DESKTOP_BORDER, new EmptyBorder(6, 5, 4, 6),
            SCROLLPANE_BORDER, UIManager.get("ScrollPane.border"),
            EXPLORER_STATUS_BORDER, new StatusLineBorder(StatusLineBorder.TOP),
            EXPLORER_FOLDER_ICON , explorerIcon,
            EXPLORER_FOLDER_OPENED_ICON, explorerIcon,
            EDITOR_STATUS_LEFT_BORDER, new StatusLineBorder(StatusLineBorder.TOP | StatusLineBorder.RIGHT),
            EDITOR_STATUS_RIGHT_BORDER, new StatusLineBorder(StatusLineBorder.TOP | StatusLineBorder.LEFT),
            EDITOR_STATUS_INNER_BORDER, new StatusLineBorder(StatusLineBorder.TOP | StatusLineBorder.LEFT | StatusLineBorder.RIGHT),
            EDITOR_STATUS_ONLYONEBORDER, new StatusLineBorder(StatusLineBorder.TOP),
            EDITOR_TOOLBAR_BORDER, new EditorToolbarBorder(),
            OUTPUT_SELECTION_BACKGROUND, new Color (164, 180, 255),

            PROPERTYSHEET_BOOTSTRAP, propertySheetValues,

            WORKPLACE_FILL, new Color(226, 223, 214),

            DESKTOP_SPLITPANE_BORDER, BorderFactory.createEmptyBorder(4, 0, 0, 0),
            SLIDING_BUTTON_UI, "org.netbeans.swing.tabcontrol.plaf.WinVistaSlidingButtonUI",

            // progress component related
            "nbProgressBar.Foreground", new Color(49, 106, 197),
            "nbProgressBar.Background", Color.WHITE,
            "nbProgressBar.popupDynaText.foreground", new Color(115, 115, 115),
            "nbProgressBar.popupText.background", new Color(249, 249, 249),        
            "nbProgressBar.popupText.foreground", UIManager.getColor("TextField.foreground"),
            "nbProgressBar.popupText.selectBackground", UIManager.getColor("List.selectionBackground"),
            "nbProgressBar.popupText.selectForeground", UIManager.getColor("List.selectionForeground"),                    
            PROGRESS_CANCEL_BUTTON_ICON, UIUtils.loadImage("org/netbeans/swing/plaf/resources/vista_mini_close_enabled.png"),
            PROGRESS_CANCEL_BUTTON_ROLLOVER_ICON, UIUtils.loadImage("org/netbeans/swing/plaf/resources/vista_mini_close_over.png"),
            PROGRESS_CANCEL_BUTTON_PRESSED_ICON, UIUtils.loadImage("org/netbeans/swing/plaf/resources/vista_mini_close_pressed.png"),

            //slide bar
            "NbSlideBar.GroupSeparator.Gap.Before", 9,
            "NbSlideBar.GroupSeparator.Gap.After", 3,
            "NbSlideBar.RestoreButton.Gap", 2,
            
            //#204646 - Vista l&f shows action icons and check boxes in the same menu column
            "Nb.MenuBar.VerticalAlign", Boolean.FALSE,

            //browser picker
            "Nb.browser.picker.background.light", new Color(255,255,255),
            "Nb.browser.picker.foreground.light", new Color(130,130,130),
        }; //NOI18N
        
        //Workaround for JDK 1.5.0 bug 5080144 - Disabled JTextFields stay white
        //XPTheme uses Color instead of ColorUIResource
        convert ("TextField.background"); //NOI18N
        convert ("TextField.inactiveBackground"); //NOI18N
        convert ("TextField.disabledBackground");  //NOI18N

        //#108517 - turn off ctrl+page_up and ctrl+page_down mapping
        return UIUtils.addInputMapsWithoutCtrlPageUpAndCtrlPageDown( uiDefaults );
    }
    
    /**
     * Takes a UIManager color key and ensures that it is stored as a 
     * ColorUIResource, not a Color. 
     */
    private static void convert (String key) {
        Color c = UIManager.getColor(key);
        if (c != null && !(c instanceof ColorUIResource)) {
            UIManager.put (key, new ColorUIResource(c));
        }
    }
    
    @Override
    protected Object[] additionalKeys() {
        Object[] kv = new VistaEditorColorings("").createKeysAndValues();
        Object[] kv2 = new VistaPropertySheetColorings().createKeysAndValues();
        Object[] result = new Object[(kv.length / 2) + (kv2.length / 2)];
        int ct = 0;
        for (int i=0; i < kv.length; i+=2) {
            result[ct] = kv[i];
            ct++;
        }
        for (int i=0; i < kv2.length; i+=2) {
            result[ct] = kv2[i];
            ct++;
        }
        return result;
    }    

    private class VistaEditorColorings extends UIBootstrapValue.Lazy {
        public VistaEditorColorings (String name) {
            super (name);
        }

        @Override
        public Object[] createKeysAndValues() {
            return new Object[] {
            //Tab control - XXX REPLACE WITH RelativeColor - need to figure out base
            //colors for each color
            //selected & focused
            TAB_FOCUS_FILL_UPPER, new Color(242, 249, 252),
            TAB_FOCUS_FILL_BRIGHT_LOWER, new Color(225, 241, 249),
            TAB_FOCUS_FILL_DARK_LOWER, new Color(216, 236, 246),
            
            //no selection, no focus
            TAB_UNSEL_FILL_BRIGHT_UPPER, new Color(235,235,235),
            TAB_UNSEL_FILL_DARK_UPPER, new Color(229, 229, 229),
            TAB_UNSEL_FILL_BRIGHT_LOWER, new Color(214,214,214),
            TAB_UNSEL_FILL_DARK_LOWER, new Color(203, 203, 203),
            
            //selected, no focus
            TAB_SEL_FILL, new Color(244,244,244),
            
            //no selection, mouse over
            TAB_MOUSE_OVER_FILL_BRIGHT_UPPER, new Color(223,242,252),
            TAB_MOUSE_OVER_FILL_DARK_UPPER, new Color(214,239,252),
            TAB_MOUSE_OVER_FILL_BRIGHT_LOWER, new Color(189,228,250),
            TAB_MOUSE_OVER_FILL_DARK_LOWER, new Color(171,221,248),
            
            TAB_BORDER, new Color(137,140,149),
            TAB_SEL_BORDER, new Color(60,127,177),
            TAB_BORDER_INNER, new Color(255,255,255),

            //Borders for the tab control
            EDITOR_TAB_OUTER_BORDER, BorderFactory.createEmptyBorder(),
            EDITOR_TAB_CONTENT_BORDER,
                new MatteBorder(0, 1, 1, 1, new Color(137, 140, 149)),
            EDITOR_TAB_TABS_BORDER, BorderFactory.createEmptyBorder(),

            VIEW_TAB_OUTER_BORDER, BorderFactory.createEmptyBorder(),
            VIEW_TAB_CONTENT_BORDER,
                new MatteBorder(0, 1, 1, 1, new Color(137, 140, 149)),
            VIEW_TAB_TABS_BORDER, BorderFactory.createEmptyBorder(),
            };
        }
    }

    private class VistaPropertySheetColorings extends UIBootstrapValue.Lazy {
        public VistaPropertySheetColorings () {
            super ("propertySheet");  //NOI18N
        }

        @Override
        public Object[] createKeysAndValues() {
            return new Object[] {
                PROPSHEET_SELECTION_BACKGROUND, new Color(49,106,197),
                PROPSHEET_SELECTION_FOREGROUND, Color.WHITE,
                PROPSHEET_SET_BACKGROUND, new Color(213,213,213),
                PROPSHEET_SET_FOREGROUND, Color.BLACK,
                PROPSHEET_SELECTED_SET_BACKGROUND, new Color(49,106,197),
                PROPSHEET_SELECTED_SET_FOREGROUND, Color.WHITE,
                PROPSHEET_DISABLED_FOREGROUND, new Color(161,161,146),
                PROPSHEET_BUTTON_FOREGROUND, Color.BLACK,
            };
        }
    }
}
