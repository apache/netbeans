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

package org.netbeans.swing.plaf.gtk;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.synth.ColorType;
import javax.swing.plaf.synth.Region;
import org.netbeans.swing.plaf.LFCustoms;
import org.netbeans.swing.plaf.util.UIUtils;

/** UI customizations for GTK look and feel
 *
 * @author  Tim Boudreau
 */
public class GtkLFCustoms extends LFCustoms {
    private Object light = new ThemeValue (Region.PANEL, ThemeValue.WHITE, Color.GRAY);
    private static Object control = new ThemeValue (Region.PANEL, ThemeValue.MID, Color.GRAY);
    private Object controlFont = new ThemeValue (Region.TABBED_PANE_TAB, new FontUIResource ("Dialog", Font.PLAIN, 11)); //NOI18N
    

    //Background colors for winsys tabs


    @Override
    public Object[] createApplicationSpecificKeysAndValues () {
        //Avoid using ThemeValue if it can't work - mainly due to testing issues when trying to run GTK UI customizations
        //on the Mac, which doesn't have a GTKLookAndFeel

        Object selBg = ThemeValue.functioning() ? new ThemeValue (Region.BUTTON, ThemeValue.DARK, Color.CYAN) : (Object) Color.CYAN;
        Object selFg = ThemeValue.functioning() ? new ThemeValue (Region.BUTTON, ColorType.TEXT_FOREGROUND, Color.BLACK) : (Object) Color.BLACK;
        
        Object bg = ThemeValue.functioning() ? ColorType.TEXT_BACKGROUND : Color.WHITE;
        Object fb = new Color (144, 144, 255);
        Object tabBg = ThemeValue.functioning() ? new ThemeValue (Region.INTERNAL_FRAME_TITLE_PANE, ThemeValue.DARK, fb) : (Object) fb;
        
        if (!ThemeValue.functioning()) {
            Integer i = (Integer) UIManager.get("customFontSize"); //NOI18N
            int sz = 11;
            if (i != null) {
                sz = i.intValue();
            }
            controlFont = new Font ("Dialog", Font.PLAIN, sz); //NOI18N
        }

        Color borderColor = (Color) UIManager.get("InternalFrame.borderShadow");
        if (borderColor == null) {
            borderColor = new Color(144,150,162);
        }

        /* This icon pair exists in both PNG and SVG versions; the SVG version will be substituted
        automatically if ImageUtilities and the SVG Loader implementation module is available. */
        Image explorerFolderIcon       = UIUtils.loadImage("org/netbeans/swing/plaf/resources/hidpi-folder-closed.png");
        Image explorerFolderOpenedIcon = UIUtils.loadImage("org/netbeans/swing/plaf/resources/hidpi-folder-open.png");
        
        Object[] result = {
            EDITOR_PREFERRED_COLOR_PROFILE, "NetBeans", //NOI18N
            PROPSHEET_SELECTION_BACKGROUND, selBg,
            PROPSHEET_SELECTION_FOREGROUND, selFg,
            PROPSHEET_SELECTED_SET_BACKGROUND, selBg,
            PROPSHEET_SELECTED_SET_FOREGROUND, selFg,
            PROPSHEET_BUTTON_COLOR, selFg,
            
            PROPSHEET_SET_BACKGROUND, ThemeValue.functioning() ? (Object) control : (Object) Color.CYAN,
            PROPSHEET_DISABLED_FOREGROUND, new Color(161,161,146),
            "Table.selectionBackground", selBg, //NOI18N
            "Table.selectionForeground", selFg, //NOI18N
            PROPSHEET_BACKGROUND, bg,
            "window", light,
            
            VIEW_TAB_OUTER_BORDER, BorderFactory.createEmptyBorder(),
            VIEW_TAB_TABS_BORDER, BorderFactory.createEmptyBorder(),
            VIEW_TAB_CONTENT_BORDER, BorderFactory.createMatteBorder(0,1,1,1,borderColor),
            EDITOR_TAB_OUTER_BORDER, BorderFactory.createEmptyBorder(),
            EDITOR_TAB_CONTENT_BORDER, BorderFactory.createMatteBorder(0,1,1,1,borderColor),
            EDITOR_TAB_TABS_BORDER, BorderFactory.createEmptyBorder(),
            
            EDITOR_STATUS_LEFT_BORDER, new InsetBorder (false, true),
            EDITOR_STATUS_RIGHT_BORDER, new InsetBorder (false, false),
            EDITOR_STATUS_ONLYONEBORDER, new InsetBorder (false, false),
            EDITOR_STATUS_INNER_BORDER, new InsetBorder (false, true),
            
            OUTPUT_BACKGROUND, control,
            OUTPUT_HYPERLINK_FOREGROUND, selFg,
            OUTPUT_SELECTION_BACKGROUND, selBg,
            
            "controlFont", controlFont, //NOI18N

            //UI Delegates for the tab control
            EDITOR_TAB_DISPLAYER_UI, 
                "org.netbeans.swing.tabcontrol.plaf.GtkEditorTabDisplayerUI", //NOI18N
            VIEW_TAB_DISPLAYER_UI, 
                "org.netbeans.swing.tabcontrol.plaf.GtkViewTabDisplayerUI", //NOI18N
            SLIDING_TAB_BUTTON_UI, "org.netbeans.swing.tabcontrol.plaf.SlidingTabDisplayerButtonUI", //NOI18N
            SLIDING_BUTTON_UI, "org.netbeans.swing.tabcontrol.plaf.GtkSlidingButtonUI", //NOI18N

            DESKTOP_BACKGROUND, ThemeValue.functioning() ? new ThemeValue (Region.BUTTON, ThemeValue.LIGHT, Color.GRAY) : (Object) Color.GRAY,

            EXPLORER_FOLDER_ICON ,explorerFolderIcon,
            EXPLORER_FOLDER_OPENED_ICON, explorerFolderOpenedIcon,

            EXPLORER_MINISTATUSBAR_BORDER, BorderFactory.createEmptyBorder(),

            //TOOLBAR_UI, "org.netbeans.swing.plaf.gtk.GtkToolbarUI", //NOI18N
                    
            PROGRESS_CANCEL_BUTTON_ICON, UIUtils.loadImage("org/netbeans/swing/plaf/resources/cancel_task_linux_mac.png"),
            "winclassic_tab_sel_gradient", tabBg,
            SCROLLPANE_BORDER, new JScrollPane().getViewportBorder(),
            //slide bar
            "NbSlideBar.GroupSeparator.Gap.Before", 7,
            "NbSlideBar.GroupSeparator.Gap.After", 2,
            "NbSlideBar.RestoreButton.Gap", 5,
            
            // Options Panel
            OPTIONS_USE_UI_DEFAULT_COLORS, true,
            OPTIONS_CATEGORIES_SEPARATOR_COLOR, UIManager.getColor("Separator.foreground"),
            OPTIONS_CATEGORIES_BUTTON_USE_NIMBUS, true,
        };

        //#108517 - turn off ctrl+page_up and ctrl+page_down mapping
        return UIUtils.addInputMapsWithoutCtrlPageUpAndCtrlPageDown( result );
    }
    
    @Override
    public Object[] createLookAndFeelCustomizationKeysAndValues() {
        if (ThemeValue.functioning()) {
            // Better than nothing detection of GTK dark themes
            ThemeValue textText = new ThemeValue (Region.PANEL, ColorType.TEXT_FOREGROUND, Color.BLACK);
            ThemeValue text = new ThemeValue (Region.PANEL, ColorType.TEXT_BACKGROUND, Color.GRAY);
            Boolean dark = UIUtils.isBrighter(textText.getColor(), text.getColor());
            
            return new Object[] {
                //XXX once the JDK team has integrated support for standard
                //UIManager keys into 1.5 (not there as of b47), these can 
                //probably be deleted, resulting in a performance improvement:
                "control", control,
                "controlHighlight", new ThemeValue (Region.PANEL, ThemeValue.LIGHT, Color.LIGHT_GRAY), //NOI18N
                "controlShadow", new ThemeValue (Region.PANEL, ThemeValue.DARK, Color.DARK_GRAY), //NOI18N
                "controlDkShadow", new ThemeValue (Region.PANEL, ThemeValue.BLACK, Color.BLACK), //NOI18N
                "controlLtHighlight", new ThemeValue (Region.PANEL, ThemeValue.WHITE, Color.WHITE), //NOI18N
                "textText", textText, //NOI18N
                "text", text, //NOI18N
                
                
                "nb.dark.theme", dark, //NOI18N
                "nb.wizard.hideimage", dark, //NOI18N
                
                "tab_unsel_fill", control, //NOI18N
                 
                "SplitPane.dividerSize", 2,  //NOI18N
                
                SYSTEMFONT, controlFont, //NOI18N
                USERFONT, controlFont, //NOI18N
                MENUFONT, controlFont, //NOI18N
                LISTFONT, controlFont, //NOI18N
                "Label.font", controlFont, //NOI18N
                "Panel.font", controlFont, //NOI18N

                // workaround: GTKLookAndFeel FileChooser is unusable, cannot
                // choose a dir and doesn't look native anyway.  We force MetalFileChooserUI
                        
                "FileChooserUI", "javax.swing.plaf.metal.MetalFileChooserUI", // NOI18N
                "FileView.computerIcon",       javax.swing.plaf.metal.MetalIconFactory.getTreeComputerIcon(), // NOI18N
                "FileView.hardDriveIcon",      javax.swing.plaf.metal.MetalIconFactory.getTreeHardDriveIcon(), // NOI18N
                "FileView.floppyDriveIcon",    javax.swing.plaf.metal.MetalIconFactory.getTreeFloppyDriveIcon(), // NOI18N
                "FileChooser.newFolderIcon",   javax.swing.plaf.metal.MetalIconFactory.getFileChooserNewFolderIcon(), // NOI18N
                "FileChooser.upFolderIcon",    javax.swing.plaf.metal.MetalIconFactory.getFileChooserUpFolderIcon(), // NOI18N
                "FileChooser.homeFolderIcon",  javax.swing.plaf.metal.MetalIconFactory.getFileChooserHomeFolderIcon(), // NOI18N
                "FileChooser.detailsViewIcon", javax.swing.plaf.metal.MetalIconFactory.getFileChooserDetailViewIcon(), // NOI18N
                "FileChooser.listViewIcon",    javax.swing.plaf.metal.MetalIconFactory.getFileChooserListViewIcon(), // NOI18N
                "FileChooser.usesSingleFilePane", Boolean.TRUE, // NOI18N
                "FileChooser.ancestorInputMap", // NOI18N
                            new UIDefaults.LazyInputMap(new Object[] {
                                "ESCAPE", "cancelSelection", // NOI18N
                                "F2", "editFileName", // NOI18N
                                "F5", "refresh", // NOI18N
                                "BACK_SPACE", "Go Up", // NOI18N
                                "ENTER", "approveSelection", // NOI18N
                                "ctrl ENTER", "approveSelection" // NOI18N
                            }),
                // special tree icons - only for property sheet
                "Tree.gtk_expandedIcon", new GTKExpandedIcon(),
                "Tree.gtk_collapsedIcon", new GTKCollapsedIcon(),
                "Slider.paintValue", false
            };
        } else {
            Object[] result = new Object[] {
                TOOLBAR_UI, new UIDefaults.ProxyLazyValue("org.netbeans.swing.plaf.gtk.GtkToolbarUI"), //NOI18N
                // special tree icons - only for property sheet
                "Tree.gtk_expandedIcon", new GTKExpandedIcon(),
                "Tree.gtk_collapsedIcon", new GTKCollapsedIcon(),
                "Slider.paintValue", false
            };
            return result;
        }
    }
    
    /** Temporary workaround for GTK L&F */
    private abstract static class GTKIcon implements Icon {
        private static final int SIZE = 11;
        @Override
        public int getIconWidth() {
            return GTKIcon.SIZE;
        }
        
        @Override
        public int getIconHeight() {
            return GTKIcon.SIZE;
        }
    }

    /**
     * Temporary workaround for GTK L&F - they provide an icon which does not
     * know its width or height until it has been painted.  So for it to work
     * correctly, we have this silliness.
     */
    private static final class GTKCollapsedIcon extends GTKIcon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.translate(x, y);
            int mid, height, thick, i, j, up, down;
            int size = Math.min(getIconWidth(),getIconHeight());
            mid = (size / 2);
            height = size / 2 + 1;
            thick = Math.max(1, size / 7);
            
            i = size / 2 - height / 2 - 1;
            
            // Fill in the background of the expander icon.
            g.setColor((Color) UIManager.get("Button.background"));
            for (j = height - 1; j > 0; j--) {
                g.drawLine(i, mid - j + 1, i, mid + j - 1);
                i++;
            }
            
            g.setColor((Color) UIManager.get("Button.foreground"));
            i = size / 2 - height / 2 - 1;
            down = thick - 1;
            // Draw the base of the triangle.
            for (up = 0; up < thick; up++) {
                g.drawLine(i + up, 0 - down, i + up, size + down);
                down--;
            }
            i++;
            
            // Paint sides of triangle.
            for (j = height - 1; j > 0; j--) {
                for (up = 0; up < thick; up++) {
                    g.drawLine(i, mid - j + 1 - up, i, mid - j + 1 - up);
                    g.drawLine(i, mid + j - 1 + up, i, mid + j - 1 + up);
                }
                i++;
            }
            
            // Paint remainder of tip if necessary.
            if (thick > 1) {
                for (up = thick - 2; up >= 0; up--) {
                    g.drawLine(i, mid - up, i, mid + up);
                    i++;
                }
            }
            
            g.translate(-x, -y);
        }
    }

    /**
     * Temporary workaround for GTK L&F - they provide an icon which does not
     * know its width or height until it has been painted.  So for it to work
     * correctly, we have this silliness.
     */
    private static final class GTKExpandedIcon extends GTKIcon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.translate(x, y);
            int mid, height, thick, i, j, up, down;
            int size = Math.min(getIconWidth(),getIconHeight());
            mid = (size / 2);
            height = size / 2 + 1;
            thick = Math.max(1, size / 7);
 
            j = size / 2 - height / 2 - 1;
            // Fill in the background of the expander icon.
            g.setColor((Color) UIManager.get("Button.background"));
            for (i = height - 1; i > 0; i--) {
                g.drawLine(mid - i + 1, j, mid + i - 1, j);
                j++;
            }

            g.setColor((Color) UIManager.get("Button.foreground"));
            j = size / 2 - height / 2 - 1;
            down = thick - 1;
            // Draw the base of the triangle.
            for (up = 0; up < thick; up++) {
                g.drawLine(0 - down, j + up, size + down, j + up);
                down--;
            }
            j++;

            // Paint sides of triangle.
            for (i = height - 1; i > 0; i--) {
                for (up = 0; up < thick; up++ ) {
                    g.drawLine(mid - i + 1 - up, j, mid - i + 1 - up, j);
                    g.drawLine(mid + i - 1 + up, j, mid + i - 1 + up, j);
                }
                j++;
            }

            // Paint remainder of tip if necessary.
            if (thick > 1) {
                for (up = thick - 2; up >= 0; up--) {
                    g.drawLine(mid - up, j, mid + up, j);
                    j++;
                }
            }
             
            g.translate(-x, -y);
        }
    }
    

}
