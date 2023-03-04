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

package org.netbeans.swing.plaf.windows8;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
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

    // There is also a SCROLLPANE_BORDER_COLOR constant in the superclass. Both seem to be in use.
    static final String SCROLLPANE_BORDER_COLOR2 = "scrollpane_border"; //NOI18N

    /**
     * A list of {@link UIDefaults} font properties which may need adjustment of the font family
     * and/or font size.
     */
    private static final String[] DEFAULT_GUI_FONT_PROPERTIES = new String[] {
        /* These font properties are usually set to Tahoma 11 by Swing's Windows LAF. Since
        Windows Vista, the default Windows font has switched to Segoe UI 12. Swing kept Tahoma 11
        for backwards compatibility reasons only; see https://www.pushing-pixels.org/page/213?m and
        JDK-6669448.

        There's also a JDK Swing LAF bug which causes these font properties to be assigned the wrong
        size under certain HiDPI configurations. Currently, JDK's WindowsLookAndFeel derives font
        properties such as Label.font from the Windows API call GetStockObject(DEFAULT_GUI_FONT),
        which appears to be unreliable when HiDPI display configurations are changed without logging
        out of Windows and back in again (as may frequently happen, for instance, when an external
        monitor is connected or disconnected). See the "win.defaultGUI.font" property in
        WindowsLookAndFeel and
        java.desktop/windows/native/libawt/windows/awt_DesktopProperties.cpp . The
        "win.messagebox.font" property is not affected by this problem, however, so we fetch the
        default font using that one instead. FlatLAF does the same, in
        com.formdev.flatlaf.FlatLaf.initialize(). Note that the font size in the
        "win.defaultGUI.font" property may still be affected by the "Make text bigger" option in the
        Windows 10 control panel, which exists independently of monitor-level HiDPI scaling
        settings. */
        "TitledBorder.font", "Slider.font", "PasswordField.font", "TableHeader.font", "TextPane.font",
        "ProgressBar.font", "Viewport.font", "TabbedPane.font", "List.font", "CheckBox.font",
        "Table.font", "ScrollPane.font", "ToggleButton.font", "Panel.font", "RadioButton.font",
        "FormattedTextField.font", "TextField.font", "Spinner.font", "Button.font", "EditorPane.font",
        "Label.font", "ComboBox.font", "Tree.font",
        /* This one is Monospaced 13 by default, but should be switched to the standard UI font.
        (This particular font substitution has been part of NetBeans since at least 2004.) */
        "TextArea.font",
        /* These font properties seem to be unaffected by the aforementioned HiDPI bug, and are also
        set to Segoe UI 12 by Swing's Windows LAF. But include them in the list of fonts to update,
        for consistency in case of future changes. */
        "CheckBoxMenuItem.font", "OptionPane.font", "Menu.font", "ToolTip.font", "PopupMenu.font",
        "RadioButtonMenuItem.font", "MenuItem.font", "ToolBar.font", "MenuBar.font",
        /* This one is usually set to "Dialog 12" by default. Include it in the list to switch it to
        Segoe UI as well. */
        "ColorChooser.font"
    }; //NOI18N

    // Copied from com.formdev.flatlaf.FlatLAF.createCompositeFont.
    private static FontUIResource createCompositeFont(String family, int style, int size) {
      // using StyleContext.getFont() here because it uses
      // sun.font.FontUtilities.getCompositeFontUIResource()
      // and creates a composite font that is able to display all Unicode characters
      Font font = StyleContext.getDefaultStyleContext().getFont(family, style, size);
      return (font instanceof FontUIResource) ? (FontUIResource) font : new FontUIResource(font);
    }

    final Color TAB_CONTENT_BORDER_COLOR = new Color(156, 156, 156);

    @Override
    public Object[] createLookAndFeelCustomizationKeysAndValues() {
        Object[] constants = new Object[] {
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

            /* Let the quick search area be flush with the rest of the menu bar. There's already a
            thin border under the menu bar, and the quick search icon + the "Search (Ctrl+I)" string
            to show the user that the quick search component is there. */
            "nb.quicksearch.border", new EmptyBorder(0, 0, 0, 0),

            // Let the HeapView component be flush with the toolbar background.
            "nb.heapview.background", new Color(240, 240, 240),
            "nb.heapview.foreground", new Color(45, 45, 45),
            "nb.heapview.highlight", new Color(240, 240, 240, 240),
            // Use the same color as EditorTab/ViewTab.underlineColor.
            "nb.heapview.chart", new Color(61, 129, 245)
        };
        List<Object> result = new ArrayList<>();
        result.addAll(Arrays.asList(constants));

        // Adjust fonts; see comments on DEFAULT_GUI_FONT_PROPERTIES.
        {
            // Fallback values.
            int fontSize = 11;
            String fontFamily = "Dialog";
            Object messageBoxFont =
                Toolkit.getDefaultToolkit().getDesktopProperty("win.messagebox.font");
            if (messageBoxFont instanceof Font) {
                fontSize = ((Font) messageBoxFont).getSize();
                fontFamily = ((Font) messageBoxFont).getFamily();
            }
            Object customFontSize = UIManager.get(CUSTOM_FONT_SIZE); //NOI18N
            if (customFontSize instanceof Integer) {
                /* In this case, AllLFCustoms.switchFont will already have run, but we probably need
                to change the font family, too, so still add the customizations here. */
                fontSize = (Integer) customFontSize;
            }
            Font useFont = createCompositeFont(fontFamily, Font.PLAIN, fontSize);
            for (String uiKey : DEFAULT_GUI_FONT_PROPERTIES) {
                result.add(uiKey);
                result.add(useFont);
            }
        }

        if (WindowsDPIWorkaroundIcon.isWorkaroundRequired()) {
            // Use entrySet rather than keySet to actually get all values.
            for (Map.Entry<Object,Object> entry : UIManager.getDefaults().entrySet()) {
                Object key = entry.getKey();
                /* Force loading of lazily loaded values, so we can see if the actual implementation
                type is of the kind that needs to be patched. All currently known icon properties
                are suffixed "icon" or "Icon", and all are of the kind that needs to be patched. */
                Object value = key.toString().toLowerCase(Locale.ROOT).endsWith("icon") //NOI18N
                        ? UIManager.getDefaults().get(key) : null;
                if (value == null) {
                    continue;
                }
                String valueCN = value.getClass().getName();
                if (value instanceof Icon &&
                    (valueCN.startsWith("com.sun.java.swing.plaf.windows.WindowsIconFactory$") || //NOI18N
                    valueCN.startsWith("com.sun.java.swing.plaf.windows.WindowsTreeUI$"))) //NOI18N
                {
                    result.add(key);
                    result.add(new WindowsDPIWorkaroundIcon(key, (Icon) value));
                }
            }
        }

        /* Workaround for ugly borders on fractional HiDPI scalings (e.g. 150%), including
        NETBEANS-338. It would have been nice to fix this for JComboBox as well, but that one does
        not use the borders from UIManager. */
        for (String key : new String[] {
                "TextField.border", "PasswordField.border", "FormattedTextField.border", //NOI18N
                "ScrollPane.border", "PopupMenu.border", "Menu.border", "ToolTip.border" }) //NOI18N
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

        /* JSpinner has an odd border, and seemingly two borders on top of each other. Setting an
        empty border for Spinner.border makes the component look better on various HiDPI
        scalings. */
        result.add("Spinner.border");
        result.add(new EmptyBorder(3, 3, 3, 3));

        return result.toArray();
    }

    @Override
    public Object[] createApplicationSpecificKeysAndValues () {
        UIBootstrapValue editorTabsUI = new Windows8EditorColorings (
                "org.netbeans.swing.tabcontrol.plaf.WinFlatEditorTabDisplayerUI");

        Object viewTabsUI = editorTabsUI.createShared("org.netbeans.swing.tabcontrol.plaf.WinFlatViewTabDisplayerUI");

        /* This icon pair exists in both PNG and SVG versions; the SVG version will be substituted
        automatically if ImageUtilities and the SVG Loader implementation module is available. */
        Image explorerFolderIcon       = UIUtils.loadImage("org/netbeans/swing/plaf/resources/hidpi-folder-closed.png");
        Image explorerFolderOpenedIcon = UIUtils.loadImage("org/netbeans/swing/plaf/resources/hidpi-folder-open.png");

        Object propertySheetValues = new Windows8PropertySheetColorings();

        Object[] uiDefaults = {
            EDITOR_TAB_DISPLAYER_UI, editorTabsUI,
            VIEW_TAB_DISPLAYER_UI, viewTabsUI,

            // Use the same neutral grey as in the Windows 10 "Settings" app sidebar.
            DESKTOP_BACKGROUND, new Color(230, 230, 230), //NOI18N
            SCROLLPANE_BORDER_COLOR, TAB_CONTENT_BORDER_COLOR,
            SCROLLPANE_BORDER_COLOR2, TAB_CONTENT_BORDER_COLOR,
            DESKTOP_BORDER, new EmptyBorder(6, 5, 4, 6),
            SCROLLPANE_BORDER, new DPIUnscaledBorder((Border) UIManager.get("ScrollPane.border")),
            EXPLORER_STATUS_BORDER, new StatusLineBorder(StatusLineBorder.TOP),
            EXPLORER_FOLDER_ICON , explorerFolderIcon,
            EXPLORER_FOLDER_OPENED_ICON, explorerFolderOpenedIcon,
            EDITOR_STATUS_LEFT_BORDER, new StatusLineBorder(StatusLineBorder.TOP | StatusLineBorder.RIGHT),
            EDITOR_STATUS_RIGHT_BORDER, new StatusLineBorder(StatusLineBorder.TOP | StatusLineBorder.LEFT),
            EDITOR_STATUS_INNER_BORDER, new StatusLineBorder(StatusLineBorder.TOP | StatusLineBorder.LEFT | StatusLineBorder.RIGHT),
            EDITOR_STATUS_ONLYONEBORDER, new StatusLineBorder(StatusLineBorder.TOP),
            EDITOR_TOOLBAR_BORDER, new DPIUnscaledBorder(new EditorToolbarBorder()),
            OUTPUT_SELECTION_BACKGROUND, new Color (164, 180, 255),

            PROPERTYSHEET_BOOTSTRAP, propertySheetValues,

            WORKPLACE_FILL, new Color(230, 230, 230), // Same as DESKTOP_BACKGROUND

            DESKTOP_SPLITPANE_BORDER, BorderFactory.createEmptyBorder(0, 0, 0, 0),

            "MenuBar.border", new DPIUnscaledBorder(new MatteBorder(0, 0, 1, 0, TAB_CONTENT_BORDER_COLOR)),
            "nb.quicksearch.background", Color.WHITE, // Match text box background.

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

            // On Windows 10, tooltip backgrounds are white rather than yellowish.
            "ToolTip.background", new Color(255, 255, 255)
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
            final Color TAB_CONTENT_BORDER_COLOR = new Color(140, 140, 140);

            return new Object[] {
            // ==== Windows8*TabDisplayerUI (no longer used, but keep around for now) ==============
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

            // ==== TabbedContainerUI ==============================================================

            //Borders for the tab control
            EDITOR_TAB_OUTER_BORDER, BorderFactory.createEmptyBorder(),
            EDITOR_TAB_CONTENT_BORDER,
                new DPIUnscaledBorder(new MatteBorder(0, 1, 1, 1, TAB_CONTENT_BORDER_COLOR), true),
            EDITOR_TAB_TABS_BORDER, BorderFactory.createEmptyBorder(),

            VIEW_TAB_OUTER_BORDER, BorderFactory.createEmptyBorder(),
            VIEW_TAB_CONTENT_BORDER,
                new DPIUnscaledBorder(new MatteBorder(0, 1, 1, 1, TAB_CONTENT_BORDER_COLOR), true),
            VIEW_TAB_TABS_BORDER, BorderFactory.createEmptyBorder(),

            // ==== WinFlat*TabDisplayerUI =========================================================
            /* Configure the tab displayers to look like native Windows 10 tabs, except show shorter
            separator lines instead of full borders between unselected tabs, and do not offset the
            Y position of the selected tab. Also highlight the selected tab with a blue indicator
            line. */

            "EditorTab.showSelectedTabBorder", true,
            "ViewTab.showSelectedTabBorder", true,
            // On the Windows LAF, borders stay 1 device pixel wide regardless of HiDPI scaling.
            "EditorTab.unscaledBorders", true,
            "ViewTab.unscaledBorders", true,

            // Left means left of the icon. Right means right of the caption, not of the "X" icon.
            "EditorTab.tabInsets", new Insets(3,6,3,6),
            "EditorTab.underlineHeight", 2,
            "EditorTab.underlineAtTop", true,
            "EditorTab.showTabSeparators", true,

            /* Left/top means left/top of the caption. Right means right of the caption. The X is
            centered vertically. */
            "ViewTab.tabInsets", new Insets(2,7,4,3),
            "ViewTab.underlineHeight", 2,
            "ViewTab.underlineAtTop", true,
            "ViewTab.showTabSeparators", false,

            "EditorTab.background", new Color(240, 240, 240), // Same as JPanel background.
            // "EditorTab.foreground", null,

            /* As on native Windows tabs, don't show a hover effect for the selected tab. So set
            this to the same as selectedBackground. */
            "EditorTab.hoverBackground", new Color(255, 255, 255),
            // Use the hover color from native Windows tabs.
            "EditorTab.unselectedHoverBackground", new Color(216, 234, 249),
            "EditorTab.attentionBackground", new Color(230, 200, 64),
            "EditorTab.underlineColor", new Color(46, 144, 232),
            /* Don't show emphasis for selected but unfocused tabs, so set alpha 0 here. (This makes
            the sidebar tabs look less busy when focus is in the editor. We keep the editor tabs
            behaving the same for consistency.) */
            "EditorTab.inactiveUnderlineColor", new Color(0, 0, 0, 0),
            "EditorTab.tabSeparatorColor", TAB_CONTENT_BORDER_COLOR,
            /* Don't use a special highlight for the entire active tab row. The blue highlight on
            the active tab already communicates this. */
            // "EditorTab.activeBackground", new Color(255, 255, 255),
            /* On Windows 10, the selected tab is typically white, while unselected tabs are on the
            grey panel color. But for NetBeans editor tabs, the component immediately below is
            usually the editor toolbar, which has a grey background. To make the tab "connect" with
            the background of the component below, while still getting the contrast of the white
            color against the grey tab color in unselected tabs, use a gradient from white to
            grey. This style is also used in the Aqua LAF. */
            "EditorTab.selectedBackground", new Color(255, 255, 255),
            "EditorTab.selectedBackgroundBottomGradient", new Color(240, 240, 240),
            // "EditorTab.activeForeground", null,
            "EditorTab.selectedForeground", new Color(0, 0, 0),
            // "EditorTab.hoverForeground", null,
            "EditorTab.attentionForeground", new Color(0, 0, 0),

            // The style for ViewTab (sidebar tabs) is mostly the same as that for EditorTab.
            "ViewTab.background", new Color(240, 240, 240),
            "ViewTab.hoverBackground", new Color(255, 255, 255),
            "ViewTab.unselectedHoverBackground", new Color(216, 234, 249),
            "ViewTab.attentionBackground", new Color(230, 200, 64),
            "ViewTab.underlineColor", new Color(46, 144, 232),
            "ViewTab.inactiveUnderlineColor", new Color(0, 0, 0, 0),
            "ViewTab.tabSeparatorColor", TAB_CONTENT_BORDER_COLOR,
            // "ViewTab.activeBackground", new Color(255, 255, 255),
            /* For view tabs, there is no need for a color gradient from white to grey, since the
            background of the component below is usually white (e.g. for the Projects and Navigator
            panes). */
            "ViewTab.selectedBackground", new Color(255, 255, 255),
            // "ViewTab.foreground", null,
            // "ViewTab.activeForeground", null,
            // "ViewTab.selectedForeground", null,
            // "ViewTab.hoverForeground", null,
            "ViewTab.attentionForeground", new Color(0, 0, 0),

            "TabbedContainer.editor.contentBorderColor", TAB_CONTENT_BORDER_COLOR,
            "TabbedContainer.view.contentBorderColor", TAB_CONTENT_BORDER_COLOR,
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
