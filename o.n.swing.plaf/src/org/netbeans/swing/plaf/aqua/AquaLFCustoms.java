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

package org.netbeans.swing.plaf.aqua;

import org.netbeans.swing.plaf.LFCustoms;
import org.netbeans.swing.plaf.util.GuaranteedValue;
import org.netbeans.swing.plaf.util.UIUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.image.BufferedImage;


/** Default system-provided customizer for Windows XP LF
 * Public only to be accessible by ProxyLazyValue, please don't abuse.
 */
public final class AquaLFCustoms extends LFCustoms {


    @Override
    public Object[] createLookAndFeelCustomizationKeysAndValues() {
        Integer cus = (Integer) UIManager.get("customFontSize"); //NOI18N
        Object[] result;
        if (cus != null) {
            int uiFontSize = cus.intValue();
            Font controlFont = new GuaranteedValue (new String[] {"controlFont", "Tree.font", "Label.font"},
                                                new FontUIResource("Dialog", Font.PLAIN, uiFontSize)).getFont(); //NOI18N
            result = new Object[] {
                "Button.font", controlFont,
                "Tree.font", controlFont,
                "ToggleButton.font", controlFont,
                "Menu.font", controlFont,
                "MenuBar.font", controlFont,
                "MenuItem.font", controlFont,
                "CheckBoxMenuItem.font", controlFont,
                "RadioButtonMenuItem.font", controlFont,
                "PopupMenu.font", controlFont,
                "List.font", controlFont,
                "Label.font", controlFont,
                "ComboBox.font", controlFont, 
                "PopupMenuSeparatorUI", "org.netbeans.swing.plaf.aqua.AquaSeparatorUI",
                "SeparatorUI", "org.netbeans.swing.plaf.aqua.AquaSeparatorUI",
                "PopupMenu.border", BorderFactory.createEmptyBorder(4, 0, 4, 0),
                 SLIDING_BUTTON_UI, "org.netbeans.swing.tabcontrol.plaf.AquaSlidingButtonUI",

                EDITOR_ERRORSTRIPE_SCROLLBAR_INSETS, new Insets(18, 0, 18, 0),

            }; //NOI18N
        } else {
            result = new Object[] {
                "controlFont", new GuaranteedValue (new String[] {"Label.font", "Tree.font"}, new FontUIResource("Dialog", Font.PLAIN, 14)).getFont(),
                "PopupMenuSeparatorUI", "org.netbeans.swing.plaf.aqua.AquaSeparatorUI",
                "SeparatorUI", "org.netbeans.swing.plaf.aqua.AquaSeparatorUI",
                "PopupMenu.border", BorderFactory.createEmptyBorder(4, 0, 4, 0),
                 SLIDING_BUTTON_UI, "org.netbeans.swing.tabcontrol.plaf.AquaSlidingButtonUI",
		
                EDITOR_ERRORSTRIPE_SCROLLBAR_INSETS, new Insets(18, 0, 18, 0),
            }; 
        }
        return result;
    }

    @Override
    public Object[] createApplicationSpecificKeysAndValues () {
        Border topOnly = BorderFactory.createMatteBorder(1, 0, 0, 0,
            UIManager.getColor("controlShadow").brighter()); //NOI18N

        Border empty = BorderFactory.createEmptyBorder();

        Image explorerIcon = null;
        Icon treeIcon = UIManager.getIcon("Tree.closedIcon"); //NOI18N
        if( null != treeIcon )
            explorerIcon = icon2Image(treeIcon);
        else
            explorerIcon = UIUtils.loadImage(
            "org/netbeans/swing/plaf/resources/osx-folder.png"); //NOI18N

        Object[] result = {
            TOOLBAR_UI, "org.netbeans.swing.plaf.aqua.PlainAquaToolbarUI",

            // XXX  - EXPLORER_STATUS_BORDER,
            DESKTOP_BACKGROUND, new Color(226, 223, 214), //NOI18N
            SCROLLPANE_BORDER_COLOR, new Color(127, 157, 185),
            EXPLORER_FOLDER_ICON ,explorerIcon,
            EXPLORER_FOLDER_OPENED_ICON, explorerIcon,
            DESKTOP_BORDER, empty,
            SCROLLPANE_BORDER, UIManager.get("ScrollPane.border"),
            EXPLORER_STATUS_BORDER, topOnly,
            EDITOR_STATUS_LEFT_BORDER, topOnly,
            EDITOR_STATUS_RIGHT_BORDER, topOnly,
            EDITOR_STATUS_INNER_BORDER, topOnly,
            EDITOR_STATUS_ONLYONEBORDER, topOnly,
            EDITOR_TOOLBAR_BORDER, BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(101,101,101)),

            EDITOR_TAB_OUTER_BORDER, BorderFactory.createEmptyBorder(),

            VIEW_TAB_OUTER_BORDER, BorderFactory.createEmptyBorder(),


            //UI Delegates for the tab control
            EDITOR_TAB_DISPLAYER_UI, "org.netbeans.swing.tabcontrol.plaf.AquaEditorTabDisplayerUI",
            VIEW_TAB_DISPLAYER_UI, "org.netbeans.swing.tabcontrol.plaf.AquaViewTabDisplayerUI",
            SLIDING_TAB_BUTTON_UI, "org.netbeans.swing.tabcontrol.plaf.SlidingTabDisplayerButtonUI$Aqua",
            "NbTabControl.focusedTabBackground", new Color(135,189,255),
            "NbTabControl.selectedTabBrighterBackground", new Color(252,252,252),
            "NbTabControl.selectedTabDarkerBackground", new Color(226,226,226),
            "NbTabControl.mouseoverTabBrighterBackground", new Color(194,194,194),
            "NbTabControl.mouseoverTabDarkerBackground", new Color(156,156,156),
            "NbTabControl.inactiveTabBrighterBackground", new Color(220,220,200),
            "NbTabControl.inactiveTabDarkerBackground", new Color(178,178,178),
            "NbTabControl.borderColor", new Color(49,49,49),
            "NbTabControl.borderShadowColor", new Color(178,178,178),
            "NbTabControl.borderDarkShadowColor", new Color(121,121,121),
            "NbTabControl.editorTabBackground", new Color(101,101,101),
            "NbTabControl.editorBorderShadowColor", new Color(121,121,121),

            EXPLORER_MINISTATUSBAR_BORDER, BorderFactory.createEmptyBorder(),
            
            "floatingBorder", FakeDropShadowBorder.createDefault(),
            "floatingBorder-left", FakeDropShadowBorder.createLeftBorder(),
            "floatingBorder-right", FakeDropShadowBorder.createRightBorder(),
            "floatingBorder-bottom", FakeDropShadowBorder.createBottomBorder(),
            "floatingBorder-top", FakeDropShadowBorder.createTopBorder(),
                    
            TAB_ACTIVE_SELECTION_FOREGROUND, new GuaranteedValue ("textText", Color.BLACK),

            "NbSplitPane.dividerSize", new Integer(3),
            "NbSplitPane.background", new Color(101,101,101),
            "nb.desktop.splitpane.border", BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(49,49,49)),
            "nb.desktop.view.insets", new Insets(0, 0, 0, 0),

            SPLIT_PANE_DIVIDER_SIZE_VERTICAL, new Integer(3),
            SPLIT_PANE_DIVIDER_SIZE_HORIZONTAL, new Integer(3),

            "NbExplorerView.quicksearch.border", new Color(64,64,64),
            "NbExplorerView.quicksearch.background.top", new Color(197,197,197),
            "NbExplorerView.quicksearch.background.bottom", new Color(150,150,150),
            "NbExplorerView.background", new Color(226,226,226),
            "NbEditorStatusBar.background", new Color(226,226,226),
            "NbEditorGlyphGutter.background", new Color(255,255,255),
            "NbSlideBar.rollover", new Color(135,135,135),

            "NbBrushedMetal.darkShadow", new Color(49,49,49),
            "NbBrushedMetal.lightShadow", new Color(178,178,178),

            "PropSheet.selectedSetBackground", new Color(181,213,255),
            "PropSheet.selectionBackground", new Color(181,213,255),
            "PropSheet.setBackground", new Color(226,226,226),
            "PropSheet.selectionForeground", UIManager.getColor("Table.foreground"),
//            "netbeans.ps.background", new Color(226,226,226),

            "Tree.paintLines", Boolean.FALSE,
                    
            // progress component related
            "nbProgressBar.Foreground", new Color(49, 106, 197),
            "nbProgressBar.Background", Color.WHITE,
            "nbProgressBar.popupDynaText.foreground", new Color(141, 136, 122),
            "nbProgressBar.popupText.background", new Color(249, 249, 249),        
            "nbProgressBar.popupText.foreground", UIManager.getColor("TextField.foreground"),
            "nbProgressBar.popupText.selectBackground", UIManager.getColor("List.selectionBackground"),
            "nbProgressBar.popupText.selectForeground", UIManager.getColor("List.selectionForeground"),                    
            PROGRESS_CANCEL_BUTTON_ICON, UIUtils.loadImage("org/netbeans/swing/plaf/resources/cancel_task_linux_mac.png"),
                    
            //slide bar
            "NbSlideBar.GroupSeparator.Gap.Before", 1,
            "NbSlideBar.GroupSeparator.Gap.After", 1,
            "NbSlideBar.RestoreButton.Gap", 3,
            
            "Nb.MainWindow.Toolbar.Border", BorderFactory.createEmptyBorder(0, 0, 0, 3),
            
            "Nb.MenuBar.VerticalAlign", Boolean.FALSE,

            "Nb.SplitPaneUI.clean", "org.netbeans.swing.plaf.aqua.CleanSplitPaneUI",
            
            //browser picker
            "Nb.browser.picker.background.light", new Color(233,239,248),
            "Nb.browser.picker.foreground.light", new Color(130,130,130),
        }; //NOI18N
        return result;
    }
    
    private static final Image icon2Image(Icon icon) {
        Image image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        icon.paintIcon(new JLabel(), g, 0, 0);
        g.dispose();
        return image;
    }
    
}
