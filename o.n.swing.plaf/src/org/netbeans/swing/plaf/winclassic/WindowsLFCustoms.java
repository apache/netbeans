/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.swing.plaf.winclassic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import org.netbeans.swing.plaf.LFCustoms;
import org.netbeans.swing.plaf.util.GuaranteedValue;
import org.netbeans.swing.plaf.util.RelativeColor;
import org.netbeans.swing.plaf.util.UIBootstrapValue;
import org.netbeans.swing.plaf.util.UIUtils;

/** Default system-provided customizer for Windows LF
 * Public only to be accessible by ProxyLazyValue, please don't abuse.
 */
public final class WindowsLFCustoms extends LFCustoms {

    @Override
    public Object[] createLookAndFeelCustomizationKeysAndValues() {
        int fontsize = 11;
        Integer in = (Integer) UIManager.get(CUSTOM_FONT_SIZE); //NOI18N
        if (in != null) {
            fontsize = in.intValue();
        }
        
        return new Object[] {
            //Workaround for help window selection color
            "EditorPane.selectionBackground", new Color(157, 157, 255), //NOI18N
            
            //Work around a bug in windows which sets the text area font to
            //"MonoSpaced", causing all accessible dialogs to have monospaced text
            "TextArea.font", new GuaranteedValue("Label.font", new Font("Dialog", Font.PLAIN, fontsize)), //NOI18N
            
            EDITOR_ERRORSTRIPE_SCROLLBAR_INSETS, new Insets(17, 0, 17, 0),
        };
    }

    @Override
    public Object[] createApplicationSpecificKeysAndValues () {
        Object propertySheetColorings = new WinClassicPropertySheetColorings();
        Object[] result = {
            DESKTOP_BORDER, new EmptyBorder(4, 2, 1, 2),
            SCROLLPANE_BORDER, UIManager.get("ScrollPane.border"),
            EXPLORER_STATUS_BORDER, new StatusLineBorder(StatusLineBorder.TOP),
            EXPLORER_FOLDER_ICON , UIUtils.loadImage("org/netbeans/swing/plaf/resources/win-explorer-folder.gif"),
            EXPLORER_FOLDER_OPENED_ICON, UIUtils.loadImage("org/netbeans/swing/plaf/resources/win-explorer-opened-folder.gif"),
            EDITOR_STATUS_LEFT_BORDER, new StatusLineBorder(StatusLineBorder.TOP | StatusLineBorder.RIGHT),
            EDITOR_STATUS_RIGHT_BORDER, new StatusLineBorder(StatusLineBorder.TOP | StatusLineBorder.LEFT),
            EDITOR_STATUS_INNER_BORDER, new StatusLineBorder(StatusLineBorder.TOP | StatusLineBorder.LEFT | StatusLineBorder.RIGHT),
            EDITOR_TOOLBAR_BORDER, new EditorToolbarBorder(),
            EDITOR_STATUS_ONLYONEBORDER, new StatusLineBorder(StatusLineBorder.TOP),

            PROPERTYSHEET_BOOTSTRAP, propertySheetColorings,

            EDITOR_TAB_CONTENT_BORDER, new WinClassicCompBorder(),
            EDITOR_TAB_TABS_BORDER, new WinClassicTabBorder(),
            VIEW_TAB_CONTENT_BORDER, new WinClassicCompBorder(),
            VIEW_TAB_TABS_BORDER, new WinClassicTabBorder(),

            DESKTOP_SPLITPANE_BORDER, BorderFactory.createEmptyBorder(4, 2, 1, 2),

            //UI Delegates for the tab control
            EDITOR_TAB_DISPLAYER_UI, "org.netbeans.swing.tabcontrol.plaf.WinClassicEditorTabDisplayerUI",
            SLIDING_BUTTON_UI, "org.netbeans.swing.tabcontrol.plaf.WindowsSlidingButtonUI",
            VIEW_TAB_DISPLAYER_UI, "org.netbeans.swing.tabcontrol.plaf.WinClassicViewTabDisplayerUI",
            "Nb.BusyIcon.Height", 14,

            PROGRESS_CANCEL_BUTTON_ICON, UIUtils.loadImage("org/netbeans/swing/plaf/resources/cancel_task_win_classic.png"),
                    
            //XXX convert to derived colors
            "tab_unsel_fill", UIUtils.adjustColor (
                new GuaranteedValue("InternalFrame.inactiveTitleGradient",
                    Color.GRAY).getColor(),
                -12, -15, -22),

            "tab_sel_fill", new GuaranteedValue("text", Color.WHITE),

            "tab_bottom_border", UIUtils.adjustColor (
                new GuaranteedValue("InternalFrame.borderShadow",
                    Color.GRAY).getColor(),
                20, 17, 12),


             "winclassic_tab_sel_gradient",
                new RelativeColor (
                    new Color(7, 28, 95),
                    new Color(152, 177, 208),
                    "InternalFrame.activeTitleBackground"),

            // progress component related
            "nbProgressBar.Foreground", new Color(49, 106, 197),
            "nbProgressBar.Background", Color.WHITE,
            "nbProgressBar.popupDynaText.foreground", new Color(141, 136, 122),
            "nbProgressBar.popupText.background", new Color(249, 249, 249),        
            "nbProgressBar.popupText.foreground", UIManager.getColor("TextField.foreground"),
            "nbProgressBar.popupText.selectBackground", UIManager.getColor("List.selectionBackground"),
            "nbProgressBar.popupText.selectForeground", UIManager.getColor("List.selectionForeground"),                    
            //slide bar
            "NbSlideBar.GroupSeparator.Gap.Before", 9,
            "NbSlideBar.GroupSeparator.Gap.After", 1,
            "NbSlideBar.RestoreButton.Gap", 1,
            
            //browser picker
            "Nb.browser.picker.background.light", new Color(255,255,255),
            "Nb.browser.picker.foreground.light", new Color(130,130,130),
        }; //NOI18N

        //#108517 - turn off ctrl+page_up and ctrl+page_down mapping
        return UIUtils.addInputMapsWithoutCtrlPageUpAndCtrlPageDown( result );
    }
    
    @Override
    public Object[] createGuaranteedKeysAndValues() {
        return new Object[] {
             "InternalFrame.activeTitleBackground",
                new GuaranteedValue("InternalFrame.activeTitleBackground",
                Color.BLUE),
                
            "InternalFrame.borderShadow",
                new GuaranteedValue("InternalFrame.borderShadow", Color.gray),

            "InternalFrame.borderHighlight",
                new GuaranteedValue("InternalFrame.borderHighlight",
                Color.white),

            "InternalFrame.borderDarkShadow",
                new GuaranteedValue("InternalFrame.borderDarkShadow",
                Color.darkGray),

            "InternalFrame.borderLight",
                new GuaranteedValue("InternalFrame.borderLight",
                Color.lightGray),

            "TabbedPane.background",
                new GuaranteedValue("TabbedPane.background", Color.LIGHT_GRAY),

            "TabbedPane.focus",
                new GuaranteedValue("TabbedPane.focus", Color.GRAY),

            "TabbedPane.highlight",
                new GuaranteedValue("TabbedPane.highlight", Color.WHITE) ,
             
            "Button.dashedRectGapX",
               new GuaranteedValue("Button.dashedRectGapX", Integer.valueOf(5)),
               
            "Button.dashedRectGapY",
               new GuaranteedValue("Button.dashedRectGapY", Integer.valueOf(4)),
               
            "Button.dashedRectGapWidth",
               new GuaranteedValue("Button.dashedRectGapWidth", Integer.valueOf(10)),
               
            "Button.dashedRectGapHeight",
               new GuaranteedValue("Button.dashedRectGapHeight", Integer.valueOf(8)),
                     
            "Tree.expandedIcon", new TreeIcon(false),
            "Tree.collapsedIcon", new TreeIcon(true)
        };
    }

    private static class TreeIcon implements Icon {
        private static final int HALF_SIZE = 4;
        private static final int SIZE = 9;

        private boolean collapsed;
        
        public TreeIcon (boolean collapsed) {
            this.collapsed = collapsed;
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(Color.WHITE);
            g.fillRect(x, y, SIZE-1, SIZE-1);
            g.setColor(Color.GRAY);
            g.drawRect(x, y, SIZE-1, SIZE-1);
            g.setColor(Color.BLACK);
            g.drawLine(x + 2, y + HALF_SIZE, x + (SIZE - 3), y + HALF_SIZE);
            if (collapsed) {
                g.drawLine(x + HALF_SIZE, y + 2, x + HALF_SIZE, y + (SIZE - 3));
            }
        }
        @Override
        public int getIconWidth() { return SIZE; }
        @Override
        public int getIconHeight() { return SIZE; }
    }

    @Override
    protected Object[] additionalKeys() {
        Object[] kv = new WinClassicPropertySheetColorings().createKeysAndValues();
        Object[] result = new Object[kv.length / 2];
        int ct = 0;
        for (int i=0; i < kv.length; i+=2) {
            result[ct] = kv[i];
            ct++;
        }
        return result;
    }

    private class WinClassicPropertySheetColorings extends UIBootstrapValue.Lazy {
        public WinClassicPropertySheetColorings () {
            super (null);
        }

        @Override
        public Object[] createKeysAndValues() {
            return new Object[] {
            //Property sheet settings as defined by HIE
            PROPSHEET_SELECTION_BACKGROUND, new Color(10,36,106),
            PROPSHEET_SELECTION_FOREGROUND, Color.WHITE,
            PROPSHEET_SET_BACKGROUND, new Color(237,233,225),
            PROPSHEET_SET_FOREGROUND, Color.BLACK,
            PROPSHEET_SELECTED_SET_BACKGROUND, new Color(10,36,106),
            PROPSHEET_SELECTED_SET_FOREGROUND, Color.WHITE,
            PROPSHEET_DISABLED_FOREGROUND, new Color(128,128,128),
            PROPSHEET_BUTTON_COLOR, UIManager.getColor("control"),
            };
        }
    }
}
