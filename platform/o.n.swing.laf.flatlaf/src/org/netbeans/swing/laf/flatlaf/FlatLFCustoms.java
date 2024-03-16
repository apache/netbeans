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

package org.netbeans.swing.laf.flatlaf;

import com.formdev.flatlaf.ui.FlatFileChooserUI;
import com.formdev.flatlaf.util.SystemInfo;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.UIDefaults;
import javax.swing.UIDefaults.LazyValue;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.InsetsUIResource;
import org.netbeans.swing.laf.flatlaf.ui.FlatTabControlIcon;
import org.netbeans.swing.plaf.LFCustoms;
import org.netbeans.swing.tabcontrol.plaf.TabControlButton;
import org.openide.util.Utilities;

/**
 * LFCustoms for FlatLaf based LAFs (light, dark, etc).
 * <p>
 * Do not add colors here.
 * Instead put colors into {@code FlatLightLaf.properties} or {@code FlatDarkLaf.properties}.
 * <p>
 * Public only to be accessible by ProxyLazyValue, please don't abuse.
 */
public class FlatLFCustoms extends LFCustoms {

    private static final ModifiableColor unifiedBackground = new ModifiableColor();
    private static final ModifiableColor quicksearchBackground = new ModifiableColor();

    @Override
    public Object[] createApplicationSpecificKeysAndValues() {
        updateUnifiedBackground();

        Color editorContentBorderColor = UIManager.getColor("TabbedContainer.editor.contentBorderColor"); // NOI18N

        Object[] removeCtrlPageUpDownKeyBindings = {
            "ctrl PAGE_UP", null, // NOI18N
            "ctrl PAGE_DOWN", null // NOI18N
        };

        Object[] constants = new Object[] {
            // unified background
            "nb.options.categories.tabPanelBackground", unifiedBackground,
            "nb.quicksearch.background", quicksearchBackground,

            // options
            "TitlePane.useWindowDecorations", FlatLafPrefs.isUseWindowDecorations(),
            "TitlePane.unifiedBackground", FlatLafPrefs.isUnifiedTitleBar(),
            "TitlePane.menuBarEmbedded", FlatLafPrefs.isMenuBarEmbedded(),
            "MenuItem.selectionType", FlatLafPrefs.isUnderlineMenuSelection() ? "underline" : null,
            "Component.hideMnemonics", !FlatLafPrefs.isAlwaysShowMnemonics(),

            // necessary for org.openide.explorer.propertysheet.PropertySheet and others
            CONTROLFONT, UIManager.getFont("Label.font"), // NOI18N

            EDITOR_TAB_DISPLAYER_UI, "org.netbeans.swing.laf.flatlaf.ui.FlatEditorTabDisplayerUI", // NOI18N
            VIEW_TAB_DISPLAYER_UI, "org.netbeans.swing.laf.flatlaf.ui.FlatViewTabDisplayerUI", // NOI18N
            SLIDING_BUTTON_UI, "org.netbeans.swing.laf.flatlaf.ui.FlatSlidingButtonUI", // NOI18N

            EDITOR_TABSCOMPONENT_BORDER, BorderFactory.createEmptyBorder(),
            EDITOR_TOOLBAR_BORDER, new CompoundBorder(DPISafeBorder.matte(0, 0, 1, 0, editorContentBorderColor), BorderFactory.createEmptyBorder(1, 0, 1, 0)),
            EDITOR_TAB_CONTENT_BORDER, DPISafeBorder.matte(0, 1, 1, 1, editorContentBorderColor),
            VIEW_TAB_CONTENT_BORDER, DPISafeBorder.matte(0, 1, 1, 1, UIManager.getColor("TabbedContainer.view.contentBorderColor")), // NOI18N

            // scale on Java 8 and Linux
            SPLIT_PANE_DIVIDER_SIZE_VERTICAL, UIScale.scale(4),
            SPLIT_PANE_DIVIDER_SIZE_HORIZONTAL, UIScale.scale(4),

            // for org.openide.awt.CloseButtonFactory
            "nb.close.tab.icon.enabled.icon", FlatTabControlIcon.get(TabControlButton.ID_CLOSE_BUTTON, TabControlButton.STATE_DEFAULT), // NOI18N
            "nb.close.tab.icon.pressed.icon", FlatTabControlIcon.get(TabControlButton.ID_CLOSE_BUTTON, TabControlButton.STATE_PRESSED), // NOI18N
            "nb.close.tab.icon.rollover.icon", FlatTabControlIcon.get(TabControlButton.ID_CLOSE_BUTTON, TabControlButton.STATE_ROLLOVER), // NOI18N
            "nb.bigclose.tab.icon.enabled.icon", FlatTabControlIcon.get(TabControlButton.ID_CLOSE_BUTTON, TabControlButton.STATE_DEFAULT), // NOI18N
            "nb.bigclose.tab.icon.pressed.icon", FlatTabControlIcon.get(TabControlButton.ID_CLOSE_BUTTON, TabControlButton.STATE_PRESSED), // NOI18N
            "nb.bigclose.tab.icon.rollover.icon", FlatTabControlIcon.get(TabControlButton.ID_CLOSE_BUTTON, TabControlButton.STATE_ROLLOVER), // NOI18N

            // for org.netbeans.core.multitabs.ButtonFactory
            "nb.multitabs.button.dropdown.icon", FlatTabControlIcon.get(TabControlButton.ID_DROP_DOWN_BUTTON), // NOI18N
            "nb.multitabs.button.maximize.icon", FlatTabControlIcon.get(TabControlButton.ID_MAXIMIZE_BUTTON), // NOI18N
            "nb.multitabs.button.left.icon", FlatTabControlIcon.get(TabControlButton.ID_SCROLL_LEFT_BUTTON), // NOI18N
            "nb.multitabs.button.right.icon", FlatTabControlIcon.get(TabControlButton.ID_SCROLL_RIGHT_BUTTON), // NOI18N
            "nb.multitabs.button.rollover", true, // NOI18N

            // for module progress.ui
            "nb.progress.cancel.icon", FlatTabControlIcon.get(TabControlButton.ID_CLOSE_BUTTON, TabControlButton.STATE_DEFAULT), // NOI18N
            "nb.progress.cancel.icon.pressed", FlatTabControlIcon.get(TabControlButton.ID_CLOSE_BUTTON, TabControlButton.STATE_PRESSED), // NOI18N
            "nb.progress.cancel.icon.mouseover", FlatTabControlIcon.get(TabControlButton.ID_CLOSE_BUTTON, TabControlButton.STATE_ROLLOVER), // NOI18N

            // Change some colors from ColorUIResource to Color because they are used as
            // background colors for checkboxes (e.g. in org.netbeans.modules.palette.ui.CategoryButton),
            // which in FlatLaf paint background only if background color is not a UIResource.
            "PropSheet.setBackground", new Color(UIManager.getColor("PropSheet.setBackground").getRGB()), // NOI18N
            "PropSheet.selectedSetBackground", new Color(UIManager.getColor("PropSheet.selectedSetBackground").getRGB()), // NOI18N

            //#108517 - turn off ctrl+page_up and ctrl+page_down mapping
            // Not using UIUtils.addInputMapsWithoutCtrlPageUpAndCtrlPageDown() here because
            // this method replaces all key bindings for List, ScrollPane, Table and Tree.
            // But FlatLaf uses slightly different key bindings. Especially on macOS the
            // key bindings are different for platform specific behavior.
            "List.focusInputMap", new LazyModifyInputMap( "List.focusInputMap", removeCtrlPageUpDownKeyBindings ), // NOI18N
            "ScrollPane.ancestorInputMap", new LazyModifyInputMap( "ScrollPane.ancestorInputMap", removeCtrlPageUpDownKeyBindings ), // NOI18N
            "ScrollPane.ancestorInputMap.RightToLeft", new LazyModifyInputMap( "ScrollPane.ancestorInputMap.RightToLeft", removeCtrlPageUpDownKeyBindings ), // NOI18N
            "Table.ancestorInputMap", new LazyModifyInputMap( "Table.ancestorInputMap", removeCtrlPageUpDownKeyBindings ), // NOI18N
            "Table.ancestorInputMap.RightToLeft", new LazyModifyInputMap( "Table.ancestorInputMap.RightToLeft", removeCtrlPageUpDownKeyBindings ), // NOI18N
            "Tree.focusInputMap", new LazyModifyInputMap( "Tree.focusInputMap", removeCtrlPageUpDownKeyBindings ), // NOI18N
            FILECHOOSER_FAVORITES_ENABLED, FlatLafPrefs.isShowFileChooserFavorites(),
            FILECHOOSER_SHORTCUTS_PANEL_FACTORY, new Function<JFileChooser, JComponent> () {
                @Override public JComponent apply(JFileChooser t) {
                    return new FlatFileChooserUI.FlatShortcutsPanel(t);
                }
            }
        };
        List<Object> result = new ArrayList<>();
        result.addAll(Arrays.asList(constants));
        if (Utilities.isWindows()) {
            /* Make sure button labels appear vertically centered on Windows. On the standard
            Windows LAF, WindowsButtonUI/WindowsRadioButtonUI/WindowsToggleButtonUI.getPreferredSize
            add one pixel to the button's height to ensure that it is odd-numbered. This makes the
            text centered with either Tahoma 11 (the default Swing Windows LAF font) or Segoe UI 12
            (the default font on modern Windows versions, and on FlatLAF on Windows). */
            for (String key : new String[] { "Button", "RadioButton", "ToggleButton" }) {
                UIDefaults defaults = UIManager.getDefaults();
                Font font = defaults.getFont(key + ".font");
                Insets bm = defaults.getInsets(key + ".margin");
                if (font != null && bm instanceof InsetsUIResource &&
                        font.getFamily().equals("Segoe UI") && font.getSize() == 12 &&
                        bm.top == bm.bottom) {
                    result.add(key + ".margin");
                    /* Create an InsetsUIResource rather than an Insets, as FlatLAF treats them
                    differently. Not doing this caused buttons in the main toolbar to become very wide. */
                    result.add(new InsetsUIResource(bm.top, bm.left, bm.bottom + 1, bm.right));
                }
            }
        }
        if (SystemInfo.isLinux) {
            result.add("windowDefaultLookAndFeelDecorated");
            result.add(FlatLafPrefs.isUseWindowDecorations());
        }
        return result.toArray();
    }

    static void updateUnifiedBackground() {
        boolean unified = FlatLafPrefs.isUnifiedTitleBar() && FlatLafPrefs.isUseWindowDecorations();
        unifiedBackground.setRGB(UIManager.getColor(unified ? "Panel.background" : "Tree.background").getRGB()); // NOI18N
        quicksearchBackground.setRGB(UIManager.getColor(unified ? "Panel.background" : "MenuBar.background").getRGB()); // NOI18N
    }

    //---- class ModifiableColor ----------------------------------------------

    private static class ModifiableColor
        extends Color
    {
        private int rgb;

        public ModifiableColor() {
            super(Color.red.getRGB());
            rgb = super.getRGB();
        }

        @Override
        public int getRGB() {
            return rgb;
        }

        public void setRGB(int rgb) {
            this.rgb = rgb;
        }
    }

    //---- class LazyModifyInputMap -------------------------------------------

    /**
     * Lazily gets a base input map from the look and feel defaults and
     * applies modifications to it specified in bindings.
     */
    private static class LazyModifyInputMap
        implements LazyValue
    {
        private final String baseKey;
        private final Object[] bindings;

        LazyModifyInputMap(String baseKey, Object[] bindings) {
            this.baseKey = baseKey;
            this.bindings = bindings;
        }

        @Override
        public Object createValue(UIDefaults table) {
            // get base input map from look and feel defaults (resolves lazy base input map)
            InputMap inputMap = (InputMap) UIManager.getLookAndFeelDefaults().get(baseKey);

            // modify input map (replace or remove)
            for (int i = 0; i < bindings.length; i += 2) {
                KeyStroke keyStroke = KeyStroke.getKeyStroke((String) bindings[i]);
                if (bindings[i + 1] != null) {
                    inputMap.put(keyStroke, bindings[i + 1]);
                } else {
                    inputMap.remove(keyStroke);
                }
            }

            return inputMap;
        }
    }
}
