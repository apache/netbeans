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
package org.netbeans.modules.team.commons;

import java.awt.Color;
import javax.swing.UIManager;
import org.openide.explorer.propertysheet.PropertySheet;

/**
 *
 * @author S. Aubrecht
 */
public class ColorManager {

    private static final String TITLE_BACKGROUND = "TreeList.titleBackground"; // NOI18N
    private static final String TITLE_SELECTION_BACKGROUND = "TreeList.titleSelectionBackground"; // NOI18N
    private static final String ROOT_BACKGROUND = "TreeList.rootBackground"; // NOI18N
    private static final String ROOT_SELECTION_BACKGROUND = "TreeList.rootSelectionBackground"; // NOI18N

    private static ColorManager theInstance;
    private final boolean isAqua = "Aqua".equals(UIManager.getLookAndFeel().getID()); // NOI18N
    private final boolean isGtk = "GTK".equals(UIManager.getLookAndFeel().getID()); //NOI18N
    private final boolean isNimbus = "Nimbus".equals(UIManager.getLookAndFeel().getID()); //NOI18N
    private final boolean isWindows = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel".equals(UIManager.getLookAndFeel().getClass().getName()); //NOI18N
    private Color defaultBackground = UIManager.getColor("Tree.textBackground") == null //NOI18N
            ? UIManager.getColor("white") //NOI18N
            : UIManager.getColor("Tree.textBackground"); //NOI18N
    private Color defaultForeground = UIManager.getColor("black"); //NOI18N
    private Color disabledColor = Color.gray;
    private Color linkColor = null;
    private Color errorColor = new Color(153, 0, 0);
    private Color stableBuildColor = new Color(0, 153, 0);
    private Color unstableBuildColor = Color.yellow.darker().darker();
    private Color titleBackground;
    private Color titleSelectedBackground;
    private Color expandableRootBackground = null;
    private Color expandableRootForeground = null;
    private Color expandableRootSelectedBackground = null;
    private Color expandableRootSelectedForeground = null;

    private ColorManager() {
        deriveTreeListColors();
    }

    public static ColorManager getDefault() {
        if (null == theInstance) {
            theInstance = new ColorManager();
        }
        return theInstance;
    }

    public Color getDefaultBackground() {
        if (isAqua) {
            return UIManager.getColor("NbExplorerView.background"); // NOI18N
        }
        return defaultBackground;
    }

    public Color getDefaultForeground() {
        return defaultForeground;
    }

    public Color getDisabledColor() {
        return disabledColor;
    }

    public Color getErrorColor() {
        return errorColor;
    }

    public Color getLinkColor() {
        return linkColor;
    }

    public Color getStableBuildColor() {
        return stableBuildColor;
    }

    public static ColorManager getTheInstance() {
        return theInstance;
    }

    public Color getUnstableBuildColor() {
        return unstableBuildColor;
    }

    public Color getTitleBackground() {
        return titleBackground;
    }

    public Color getTitleSelectedBackground() {
        return titleSelectedBackground;
    }

    public Color getExpandableRootBackground() {
        return expandableRootBackground;
    }

    public Color getExpandableRootForeground() {
        return expandableRootForeground;
    }

    public Color getExpandableRootSelectedBackground() {
        return expandableRootSelectedBackground;
    }

    public Color getExpandableRootSelectedForeground() {
        return expandableRootSelectedForeground;
    }

    public boolean isAqua() {
        return isAqua;
    }

    public boolean isGtk() {
        return isGtk;
    }

    public boolean isNimbus() {
        return isNimbus;
    }

    public boolean isWindows() {
        return isWindows;
    }

    private void deriveTreeListColors() {
        //make sure UIManager constants for property sheet are initialized
        new PropertySheet();

        Color controlColor = UIManager.getColor("control"); //NOI18N

        if (controlColor == null) {
            controlColor = Color.LIGHT_GRAY;
        }

        int red;
        int green;
        int blue;

        if (isNimbus || isGtk) {
            expandableRootBackground = UIManager.getColor("Menu.background");//NOI18N
            expandableRootSelectedBackground = UIManager.getColor("Tree.selectionBackground"); //NOI18N
        } else {
            expandableRootBackground = UIManager.getColor(ROOT_BACKGROUND);
            if (expandableRootBackground == null) {
                expandableRootBackground = UIManager.getColor("PropSheet.setBackground"); //NOI18N
            }
            expandableRootSelectedBackground = UIManager.getColor(ROOT_SELECTION_BACKGROUND);
            if (expandableRootSelectedBackground == null) {
                expandableRootSelectedBackground = UIManager.getColor("PropSheet.selectedSetBackground"); //NOI18N
            }
        }

        if (expandableRootBackground == null) {
            if (expandableRootBackground == null) {
                red = adjustColorComponent(controlColor.getRed(), -25, -25);
                green = adjustColorComponent(controlColor.getGreen(), -25, -25);
                blue = adjustColorComponent(controlColor.getBlue(), -25, -25);
                expandableRootBackground = new Color(red, green, blue);
            }
        }
        if (isAqua) {
            expandableRootBackground = new Color((int) Math.max(0.0, expandableRootBackground.getRed() * 0.85), (int) Math.max(0.0, expandableRootBackground.getGreen() * 0.85), (int) Math.max(0.0, expandableRootBackground.getBlue() * 0.85));
        }

        if (expandableRootSelectedBackground == null) {
            Color col = isWindows ? UIManager.getColor("Table.selectionBackground") //NOI18N
                    : UIManager.getColor("activeCaptionBorder"); //NOI18N

            if (col == null) {
                col = Color.BLUE;
            }

            red = adjustColorComponent(col.getRed(), -25, -25);
            green = adjustColorComponent(col.getGreen(), -25, -25);
            blue = adjustColorComponent(col.getBlue(), -25, -25);
            expandableRootSelectedBackground = new Color(red, green, blue);
        }

        titleBackground = UIManager.getColor(TITLE_BACKGROUND);
        if (titleBackground == null) {
            titleBackground = Color.gray;
        }
        titleSelectedBackground = UIManager.getColor(TITLE_SELECTION_BACKGROUND);
        if (titleSelectedBackground == null) {
            titleSelectedBackground = expandableRootSelectedBackground;
        }

        expandableRootForeground = UIManager.getColor("PropSheet.setForeground"); //NOI18N

        if (isNimbus || isGtk) {
            expandableRootForeground = new Color(UIManager.getColor("Menu.foreground").getRGB()); //NOI18N
        }
        if (expandableRootForeground == null) {
            expandableRootForeground = UIManager.getColor("Table.foreground"); //NOI18N

            if (expandableRootForeground == null) {
                expandableRootForeground = UIManager.getColor("textText"); // NOI18N

                if (expandableRootForeground == null) {
                    expandableRootForeground = Color.BLACK;
                }
            }
        }

        expandableRootSelectedForeground = UIManager.getColor("PropSheet.selectedSetForeground"); //NOI18N

        if (expandableRootSelectedForeground == null) {
            expandableRootSelectedForeground = UIManager.getColor("Table.selectionForeground"); //NOI18N

            if (expandableRootSelectedForeground == null) {
                expandableRootSelectedForeground = Color.WHITE;
            }
        }
        if (isAqua) {
            expandableRootSelectedForeground = Color.black;
        }

        linkColor = UIManager.getColor("nb.html.link.foreground"); //NOI18N
        if (null == linkColor) {
            Color labelColor = javax.swing.UIManager.getDefaults().getColor("Label.foreground"); // NOI18N
            if (labelColor == null || (labelColor.getRed() < 192 && labelColor.getGreen() < 192 && labelColor.getBlue() < 192)) {
                linkColor = new Color(0x164B7B);
            } else { // hack for high-contrast black
                linkColor = new Color(0x2170B8);
            }
        }
    }

    /**
     * Adjust an rgb color component.
     *
     * @param base the color, an RGB value 0-255
     * @param adjBright the amount to subtract if base > 128
     * @param adjDark the amount to add if base <=128
     */
    private static int adjustColorComponent(int base, int adjBright, int adjDark) {
        if (base > 128) {
            base -= adjBright;
        } else {
            base += adjDark;
        }

        if (base < 0) {
            base = 0;
        }

        if (base > 255) {
            base = 255;
        }

        return base;
    }
}
