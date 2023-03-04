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

package org.netbeans.swing.plaf.nimbus;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.MatteBorder;
import org.netbeans.swing.plaf.LFCustoms;

import org.netbeans.swing.plaf.util.UIUtils;

/** Default system-provided customizer for Metal LF
 * Public only to be accessible by ProxyLazyValue, please don't abuse.
 */
public final class NimbusLFCustoms extends LFCustoms {

    @Override
    public Object[] createLookAndFeelCustomizationKeysAndValues() {
        int fontsize = 11;
        Integer in = (Integer) UIManager.get(CUSTOM_FONT_SIZE); //NOI18N
        if (in != null) {
            fontsize = in.intValue();
        }

        //XXX fetch the custom font size here instead
        Font controlFont = new Font("Dialog", Font.PLAIN, fontsize); //NOI18N
        Object[] result = {
            "JXDateTimePicker.arrowIcon", UIUtils.loadImage("org/netbeans/swing/plaf/resources/nimbus_expander.png") //NOI18N
        };
        /*Object[] result = {
            //The assorted standard NetBeans metal font customizations
            CONTROLFONT, controlFont,
            SYSTEMFONT, controlFont,
            USERFONT, controlFont,
            MENUFONT, controlFont,
            WINDOWTITLEFONT, controlFont,
            LISTFONT, controlFont,
            TREEFONT, controlFont,
            PANELFONT, controlFont,
            SUBFONT, new Font ("Dialog", Font.PLAIN, Math.min(fontsize - 1, 6)),
            //Bug in JDK 1.5 thru b59 - pale blue is incorrectly returned for this
            "textInactiveText", Color.GRAY, //NOI18N
            // #61395
            SPINNERFONT, controlFont,
            EDITOR_ERRORSTRIPE_SCROLLBAR_INSETS, new Insets(16, 0, 16, 0),
        };*/
        return result;
    }

    @Override
    public Object[] createApplicationSpecificKeysAndValues () {
        /*Border outerBorder = BorderFactory.createLineBorder(UIManager.getColor("controlShadow")); //NOI18N
        Object propertySheetColorings = new MetalPropertySheetColorings();
        Color unfocusedSelBg = UIManager.getColor("controlShadow");
        if (!Color.WHITE.equals(unfocusedSelBg.brighter())) { // #57145
            unfocusedSelBg = unfocusedSelBg.brighter();
        }*/

        Object[] result = {
            EDITOR_PREFERRED_COLOR_PROFILE, "NetBeans", //NOI18N
            //UI Delegates for the tab control
            EDITOR_TAB_DISPLAYER_UI,
                "org.netbeans.swing.tabcontrol.plaf.NimbusEditorTabDisplayerUI", //NOI18N
            VIEW_TAB_DISPLAYER_UI,
                "org.netbeans.swing.tabcontrol.plaf.NimbusViewTabDisplayerUI", //NOI18N
            SLIDING_TAB_BUTTON_UI, "org.netbeans.swing.tabcontrol.plaf.SlidingTabDisplayerButtonUI", //NOI18N
            SLIDING_BUTTON_UI, "org.netbeans.swing.tabcontrol.plaf.NimbusSlidingButtonUI", //NOI18N
            SCROLLPANE_BORDER, new UIDefaults.LazyValue() {
                @Override
                public Object createValue(UIDefaults table) {
                    return new JScrollPane().getViewportBorder();
                }
            }, 
            //Borders for the tab control
            EDITOR_TAB_OUTER_BORDER, BorderFactory.createEmptyBorder(),
            EDITOR_TAB_CONTENT_BORDER,
                new MatteBorder(0, 1, 1, 1, UIManager.getColor("nimbusBorder")), //NOI18N
            EDITOR_TAB_TABS_BORDER, BorderFactory.createEmptyBorder(),

            VIEW_TAB_OUTER_BORDER, BorderFactory.createEmptyBorder(),
            VIEW_TAB_CONTENT_BORDER,
                new MatteBorder(0, 1, 1, 1, UIManager.getColor("nimbusBorder")), //NOI18N
            VIEW_TAB_TABS_BORDER, BorderFactory.createEmptyBorder(),
            //slide bar
            "NbSlideBar.GroupSeparator.Gap.Before", 12,
            "NbSlideBar.GroupSeparator.Gap.After", 4,
            "NbSlideBar.RestoreButton.Gap", 8,
            //#212453
            "Nb.EmptyEditorArea.border", BorderFactory.createEtchedBorder( EtchedBorder.LOWERED ),

            //browser picker
            "Nb.browser.picker.background.light", new Color(249,249,249),
            "Nb.browser.picker.foreground.light", new Color(130,130,130),
            
            // Options Panel
            OPTIONS_USE_UI_DEFAULT_COLORS, true,
            OPTIONS_CATEGORIES_BUTTON_USE_NIMBUS, true,
        };
        /*Object[] result = {
            DESKTOP_BORDER, new EmptyBorder(1, 1, 1, 1),
            SCROLLPANE_BORDER, new NimbusScrollPaneBorder(),
            EXPLORER_STATUS_BORDER, new StatusLineBorder(StatusLineBorder.TOP),
            EDITOR_STATUS_LEFT_BORDER, new StatusLineBorder(StatusLineBorder.TOP | StatusLineBorder.RIGHT),
            EDITOR_STATUS_RIGHT_BORDER, new StatusLineBorder(StatusLineBorder.TOP | StatusLineBorder.LEFT),
            EDITOR_STATUS_INNER_BORDER, new StatusLineBorder(StatusLineBorder.TOP | StatusLineBorder.LEFT | StatusLineBorder.RIGHT),
            EDITOR_STATUS_ONLYONEBORDER, new StatusLineBorder(StatusLineBorder.TOP),
            EDITOR_TOOLBAR_BORDER, new EditorToolbarBorder(),

            PROPERTYSHEET_BOOTSTRAP, propertySheetColorings,

            //UI Delegates for the tab control
            EDITOR_TAB_DISPLAYER_UI, "org.netbeans.swing.tabcontrol.plaf.MetalEditorTabDisplayerUI",
            VIEW_TAB_DISPLAYER_UI, "org.netbeans.swing.tabcontrol.plaf.MetalViewTabDisplayerUI",
            SLIDING_BUTTON_UI, "org.netbeans.swing.tabcontrol.plaf.MetalSlidingButtonUI",

            EDITOR_TAB_OUTER_BORDER, outerBorder,
            VIEW_TAB_OUTER_BORDER, outerBorder,

            EXPLORER_MINISTATUSBAR_BORDER, BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("controlShadow")),

            //#48951 invisible unfocused selection background in Metal L&F
            "nb.explorer.unfocusedSelBg", unfocusedSelBg,

            PROGRESS_CANCEL_BUTTON_ICON, UIUtils.loadImage("org/netbeans/swing/plaf/resources/cancel_task_win_linux_mac.png"),


            // progress component related
//            "nbProgressBar.Foreground", new Color(49, 106, 197),
//            "nbProgressBar.Background", Color.WHITE,
            "nbProgressBar.popupDynaText.foreground", new Color(115, 115, 115),
//            "nbProgressBar.popupText.background", new Color(231, 249, 249),
            "nbProgressBar.popupText.foreground", UIManager.getColor("TextField.foreground"),
            "nbProgressBar.popupText.selectBackground", UIManager.getColor("List.selectionBackground"),
            "nbProgressBar.popupText.selectForeground", UIManager.getColor("List.selectionForeground"),

        };*/

        //#108517 - turn off ctrl+page_up and ctrl+page_down mapping
        return UIUtils.addInputMapsWithoutCtrlPageUpAndCtrlPageDown( result );
    }

    /*private class MetalPropertySheetColorings extends UIBootstrapValue.Lazy {
        public MetalPropertySheetColorings () {
            super (null);
        }

        public Object[] createKeysAndValues() {
            return new Object[] {
                //Property sheet settings as defined by HIE
                 PROPSHEET_SELECTION_BACKGROUND, new Color(204,204,255),
                 PROPSHEET_SELECTION_FOREGROUND, Color.BLACK,
                 PROPSHEET_SET_BACKGROUND, new Color(224,224,224),
                 PROPSHEET_SET_FOREGROUND, Color.BLACK,
                 PROPSHEET_SELECTED_SET_BACKGROUND, new Color(204,204,255),
                 PROPSHEET_SELECTED_SET_FOREGROUND, Color.BLACK,
                 PROPSHEET_DISABLED_FOREGROUND, new Color(153,153,153),
            };
        }
    }*/

}
