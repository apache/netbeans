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

package org.netbeans.swing.plaf.windows8;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.ColorUIResource;
import org.netbeans.swing.plaf.LFCustoms;
import org.netbeans.swing.plaf.util.UIBootstrapValue;
import org.netbeans.swing.plaf.util.UIUtils;


/** Default system-provided customizer for Windows Vista LF 
 * Public only to be accessible by ProxyLazyValue, please don't abuse.
 * @since 1.30
 */
public final class Windows8LFCustoms extends LFCustoms {
    private static final String TAB_FOCUS_FILL_UPPER = "tab_focus_fill_upper"; //NOI18N
    private static final String TAB_FOCUS_FILL_LOWER = "tab_focus_fill_lower"; //NOI18N
    
    private static final String TAB_UNSEL_FILL_UPPER = "tab_unsel_fill_upper"; //NOI18N
    private static final String TAB_UNSEL_FILL_LOWER = "tab_unsel_fill_lower"; //NOI18N

    private static final String TAB_SEL_FILL = "tab_sel_fill"; //NOI18N
    
    private static final String TAB_MOUSE_OVER_FILL_UPPER = "tab_mouse_over_fill_upper"; //NOI18N
    private static final String TAB_MOUSE_OVER_FILL_LOWER = "tab_mouse_over_fill_lower"; //NOI18N

    private static final String TAB_ATTENTION_FILL_UPPER = "tab_attention_fill_upper"; //NOI18N
    private static final String TAB_ATTENTION_FILL_LOWER = "tab_attention_fill_lower"; //NOI18N

    private static final String TAB_BORDER = "tab_border"; //NOI18N      
    private static final String TAB_SEL_BORDER = "tab_sel_border"; //NOI18N
    private static final String TAB_BORDER_INNER = "tab_border_inner"; //NOI18N      
    
    static final String SCROLLPANE_BORDER_COLOR = "scrollpane_border"; //NOI18N

    private static final String[] DEFAULT_GUI_FONT_PROPERTIES = new String[] {
        "TitledBorder.font", "Slider.font", "PasswordField.font", "TableHeader.font", "TextPane.font",
        "ProgressBar.font", "Viewport.font", "TabbedPane.font", "List.font", "CheckBox.font",
        "Table.font", "ScrollPane.font", "ToggleButton.font", "Panel.font", "RadioButton.font",
        "FormattedTextField.font", "TextField.font", "Spinner.font", "Button.font", "EditorPane.font",
        "Label.font", "ComboBox.font", "Tree.font", "TextArea.font" }; //NOI18N

    @Override
    public Object[] createLookAndFeelCustomizationKeysAndValues() {
        /* Don't try to fetch this font size from the LAF; it won't work reliably for HiDPI
        configurations (see a related workaround below). */
        int fontsize = 11;
        Integer in = (Integer) UIManager.get(CUSTOM_FONT_SIZE); //NOI18N
        if (in != null) {
            fontsize = in.intValue();
        }
        //Work around a bug in windows which sets the text area font to
        //"MonoSpaced", causing all accessible dialogs to have monospaced text
        Font textAreaFont = UIManager.getFont("Label.font"); //NOI18N
        if (textAreaFont == null) {
            textAreaFont = new Font("Dialog", Font.PLAIN, fontsize); //NOI18N
        } else {
            textAreaFont = textAreaFont.deriveFont((float) fontsize);
        }

        Object[] constants = new Object[] {
            "TextArea.font", textAreaFont, //NOI18N

            EDITOR_PREFERRED_COLOR_PROFILE, "NetBeans", //NOI18N
            EDITOR_ERRORSTRIPE_SCROLLBAR_INSETS, new Insets(17, 0, 17, 0),

            /* NETBEANS-1249: Remove excessive spacing between menu items, which appeared going from
                              Java 8 to Java 9. See screeshots in the JIRA ticket. The spacing
                              appeared during the fix for JDK-8067346; it should be safe to set
                              top/bottom margins to zero, however, like they were before. See
                              http://hg.openjdk.java.net/jdk10/master/annotate/be620a591379/src/java.desktop/share/classes/com/sun/java/swing/plaf/windows/WindowsLookAndFeel.java .
                              Current Swing defaults are [2,2,2,2] for all of these (set in
                              javax.swing.plaf.basic.BasicLookAndFeel). */
            "Menu.margin", new Insets(0, 2, 0, 2), //NOI18N
            "MenuItem.margin", new Insets(0, 2, 0, 2), //NOI18N
            "CheckBoxMenuItem.margin", new Insets(0, 2, 0, 2), //NOI18N
            "RadioButtonMenuItem.margin", new Insets(0, 2, 0, 2), //NOI18N
            /* Note that menu separators are still 3 pixels too tall on Windows compared to native
            apps. Fixing that would be a bigger job, though (replacing WindowsPopupMenuSeparatorUI
            to override getPreferredSize). */
        };
        List<Object> result = new ArrayList<>();
        result.addAll(Arrays.asList(constants));

        /* Workaround for Windows LAF JDK bug that causes fonts to appear at the wrong size on
        certain configurations involving HiDPI monitors. Currently, WindowsLookAndFeel derive
        font properties such as Label.font from the Windows API call
        GetStockObject(DEFAULT_GUI_FONT), which appears to be unreliable when HiDPI display
        configurations are changed without logging out of Windows and back in again (as may
        frequently happen, for instance, when an external monitor is connected or
        disconnected). See the "win.defaultGUI.font" property in WindowsLookAndFeel and
        java.desktop/windows/native/libawt/windows/awt_DesktopProperties.cpp . */
        /* If a custom font size is set, the font sizes have already been set in
        AllLFCustoms.initCustomFontSize. */
        if (UIManager.get(CUSTOM_FONT_SIZE) == null) {
            /* Use the same logic as in AllLFCustoms.switchFont, since it seems to take some special
            precautions (e.g. using deriveFont instead of FontUIResource for the Windows case). */
            Map<Font,Font> fontTranslation = new HashMap<>();
            for (String uiKey : DEFAULT_GUI_FONT_PROPERTIES) {
                if (uiKey.equals("TextArea.font")) { //NOI18N
                    // Skip this one; it was set earlier as part of an unrelated workaround.
                    continue;
                }
                Font oldFont = UIManager.getFont(uiKey);
                if (oldFont == null) {
                    continue;
                }
                Font newFont = fontTranslation.get(oldFont);
                if (newFont == null) {
                    newFont = oldFont.deriveFont((float) fontsize);
                    fontTranslation.put(oldFont, newFont);
                }
                result.add(uiKey);
                result.add(newFont);
            }
        }

        if (WindowsDPIWorkaroundIcon.isWorkaroundRequired()) {
            // Use entrySet rather than keySet to actually get all values.
            for (Map.Entry<Object,Object> entry : UIManager.getDefaults().entrySet()) {
                Object key = entry.getKey();
                /* Force loading of lazily loaded values, so we can see if the actual implementation
                type is of the kind that needs to be patched. All currently known icon properties
                are suffixed "icon" or "Icon". All but one is of the kind that needs to be
                patched. */
                Object value = key.toString().toLowerCase(Locale.ROOT).endsWith("icon") //NOI18N
                        ? UIManager.getDefaults().get(key) : null;
                if (value == null) {
                    continue;
                }
                String valueCN = value.getClass().getName();
                if (value instanceof Icon &&
                    (valueCN.startsWith("com.sun.java.swing.plaf.windows.WindowsIconFactory$") || //NOI18N
                    valueCN.startsWith("com.sun.java.swing.plaf.windows.WindowsTreeUI$")) && //NOI18N
                    /* This particular one can't be used as a delegate, as it intentionally behaves
                    differently when the application has overridden UIDefaults. */
                    !valueCN.contains("VistaMenuItemCheckIcon")) //NOI18N
                {
                    result.add(key);
                    result.add(new WindowsDPIWorkaroundIcon((Icon) value));
                }
            }
        }

        /* Workaround for ugly borders on fractional HiDPI scalings (e.g. 150%), including
        NETBEANS-338. It would have been nice to fix this for JComboBox as well, but that one does
        not use the borders from UIManager. */
        for (String key : new String[] {
                "TextField.border", "PasswordField.border", "FormattedTextField.border", //NOI18N
                "ScrollPane.border", "PopupMenu.border", "Menu.border" }) //NOI18N
        {
            Object value = UIManager.getDefaults().get(key);
            if (value instanceof Border) {
                Border adjustedBorder = new DPIUnscaledBorder((Border) value);
                if (adjustedBorder != value) {
                    result.add(key);
                    result.add(adjustedBorder);
                }
            }
        }
        // JSpinner requires some special treatment.
        Object spinnerBorder = UIManager.getDefaults().get("Spinner.border"); //NOI18N
        if (spinnerBorder instanceof CompoundBorder) {
            CompoundBorder cb = (CompoundBorder) spinnerBorder;
            Border ob = cb.getOutsideBorder();
            Border ib = cb.getInsideBorder();
            if (ob instanceof LineBorder && ib instanceof EmptyBorder) {
                result.add("Spinner.border"); //NOI18N
                result.add(new DPIUnscaledBorder(new CompoundBorder(
                        new MatteBorder(1, 1, 1, 1, ((LineBorder) ob).getLineColor()),
                        new MatteBorder(2, 2, 2, 2, Color.WHITE))));
            }
        }

        return result.toArray();
    }

    @Override
    public Object[] createApplicationSpecificKeysAndValues () {
        UIBootstrapValue editorTabsUI = new Windows8EditorColorings (
                "org.netbeans.swing.tabcontrol.plaf.Windows8VectorEditorTabDisplayerUI");

        Object viewTabsUI = editorTabsUI.createShared("org.netbeans.swing.tabcontrol.plaf.Windows8VectorViewTabDisplayerUI");

        //TODO change icon (copy & paste)
        Image explorerIcon = UIUtils.loadImage("org/netbeans/swing/plaf/resources/vista_folder.png");

        Object propertySheetValues = new Windows8PropertySheetColorings();

        Object[] uiDefaults = {
            EDITOR_TAB_DISPLAYER_UI, editorTabsUI,
            VIEW_TAB_DISPLAYER_UI, viewTabsUI,
            
            DESKTOP_BACKGROUND, new Color(226, 223, 214), //NOI18N
            SCROLLPANE_BORDER_COLOR, new Color(127, 157, 185),
            DESKTOP_BORDER, new EmptyBorder(6, 5, 4, 6),
            SCROLLPANE_BORDER, new DPIUnscaledBorder((Border) UIManager.get("ScrollPane.border")),
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
        Object[] kv = new Windows8EditorColorings("").createKeysAndValues();
        Object[] kv2 = new Windows8PropertySheetColorings().createKeysAndValues();
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

    private class Windows8EditorColorings extends UIBootstrapValue.Lazy {
        public Windows8EditorColorings (String name) {
            super (name);
        }

        @Override
        public Object[] createKeysAndValues() {
            return new Object[] {
            //Tab control - XXX REPLACE WITH RelativeColor - need to figure out base
            //colors for each color
            //selected & focused
            TAB_FOCUS_FILL_UPPER, new Color(236,244,252),
            TAB_FOCUS_FILL_LOWER, new Color(221,237,252),
            
            //no selection, no focus
            TAB_UNSEL_FILL_UPPER, new Color(240, 240, 240),
            TAB_UNSEL_FILL_LOWER, new Color(229, 229, 229),
            
            //selected, no focus
            TAB_SEL_FILL, new Color(255,255,255),
            
            //no selection, mouse over
            TAB_MOUSE_OVER_FILL_UPPER, new Color(236,244,252),
            TAB_MOUSE_OVER_FILL_LOWER, new Color(221,237,252),

            TAB_ATTENTION_FILL_UPPER, new Color (255, 255, 128),
            TAB_ATTENTION_FILL_LOWER, new Color (230, 200, 64),
            
            TAB_BORDER, new Color(137,140,149),
            TAB_SEL_BORDER, new Color(60,127,177),
            TAB_BORDER_INNER, new Color(255,255,255),

            //Borders for the tab control
            EDITOR_TAB_OUTER_BORDER, BorderFactory.createEmptyBorder(),
            EDITOR_TAB_CONTENT_BORDER,
                new DPIUnscaledBorder(new MatteBorder(0, 1, 1, 1, new Color(137, 140, 149))),
            EDITOR_TAB_TABS_BORDER, BorderFactory.createEmptyBorder(),

            VIEW_TAB_OUTER_BORDER, BorderFactory.createEmptyBorder(),
            VIEW_TAB_CONTENT_BORDER,
                new DPIUnscaledBorder(new MatteBorder(0, 1, 1, 1, new Color(137, 140, 149))),
            VIEW_TAB_TABS_BORDER, BorderFactory.createEmptyBorder(),
            };
        }
    }

    private class Windows8PropertySheetColorings extends UIBootstrapValue.Lazy {
        public Windows8PropertySheetColorings () {
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
