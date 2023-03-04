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

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import org.netbeans.swing.tabcontrol.TabDisplayer;

import javax.swing.plaf.ComponentUI;
import org.netbeans.swing.tabcontrol.WinsysInfoForTabbedContainer;


import org.openide.awt.HtmlRenderer;

/**
 * User interface of view type tabs designed to be consistent with Swing metal
 * look and feel.
 *
 * @author Dafe Simonek
 */
public final class MetalViewTabDisplayerUI extends AbstractViewTabDisplayerUI {

    /**
     * *********** constants ******************
     */
    private static final int TXT_X_PAD = 5;

    private static final int ICON_X_LEFT_PAD = 5;
    private static final int ICON_X_RIGHT_PAD = 2;

    private static final int BUMP_X_PAD = 5;
    private static final int BUMP_Y_PAD = 4;
    
    /**
     * ****** static fields **********
     */

    private static Color inactBgColor, actBgColor, borderHighlight, borderShadow;
    
    private static Map<Integer, String[]> buttonIconPaths;

    /**
     * ******* instance fields *********
     */

    private Dimension prefSize;

    /**
     * Reusable Rectangle to optimize rectangle creation/garbage collection
     * during paints
     */
    private Rectangle tempRect = new Rectangle();

    /**
     * Should be constructed only from createUI method.
     */
    private MetalViewTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
        prefSize = new Dimension(100, 19);
    }

    public static ComponentUI createUI(JComponent c) {
        return new MetalViewTabDisplayerUI((TabDisplayer) c);
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        FontMetrics fm = getTxtFontMetrics();
        int height = fm == null ?
                21 : fm.getAscent() + 2 * fm.getDescent() + 4;
        Insets insets = c.getInsets();
        prefSize.height = height + insets.bottom + insets.top;
        return prefSize;
    }

    /**
     * Overrides basic paint mathod, adds painting of overall blue or gray
     * bottom area, depending on activation status value
     */
    @Override
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        paintBottomBorder(g, c);
    }

    /**
     * Paints bottom "activation" line
     */
    private void paintBottomBorder(Graphics g, JComponent c) {
        Color color = isActive() ? getActBgColor() : getInactBgColor();
        g.setColor(color);
        Rectangle bounds = c.getBounds();
        g.fillRect(1, bounds.height - 3, bounds.width - 1, 2);
        g.setColor(getBorderShadow());
        g.drawLine(1, bounds.height - 1, bounds.width - 1, bounds.height - 1);
    }

    @Override
    protected void paintTabContent(Graphics g, int index, String text, int x,
                                   int y, int width, int height) {
        boolean slidedOut = false;
        WinsysInfoForTabbedContainer winsysInfo = displayer.getContainerWinsysInfo();
        if( null != winsysInfo && winsysInfo.isSlidedOutContainer() )
            slidedOut = false;

        FontMetrics fm = getTxtFontMetrics();
        // setting font already here to compute string width correctly
        g.setFont(getTxtFont());
        int txtWidth = width;
        if (isSelected(index)) {
            Component buttons = getControlButtons();
            int buttonsWidth = 0;
            if( null != buttons ) {
                Dimension buttonsSize = buttons.getPreferredSize();
                if( width < buttonsSize.width+ICON_X_LEFT_PAD+ICON_X_RIGHT_PAD ) {
                    buttons.setVisible( false );
                } else {
                    buttons.setVisible( true );
                    buttonsWidth = buttonsSize.width + ICON_X_LEFT_PAD + ICON_X_RIGHT_PAD;
                    txtWidth = width - (buttonsWidth + TXT_X_PAD);
                    buttons.setLocation( x + txtWidth+TXT_X_PAD+ICON_X_LEFT_PAD, y + (height-buttonsSize.height)/2-1 );
                }
            }
            
            if( isTabBusy( index ) && !slidedOut ) {
                Icon busyIcon = BusyTabsSupport.getDefault().getBusyIcon( isSelected( index ) );
                txtWidth -= busyIcon.getIconWidth() - 3 - TXT_X_PAD;
                busyIcon.paintIcon( displayer, g, x+TXT_X_PAD, y+(height-busyIcon.getIconHeight())/2);
                x += busyIcon.getIconWidth() + 3;
            }

            txtWidth = (int)HtmlRenderer.renderString(text, g, x + TXT_X_PAD, height -
                    fm.getDescent() - 4, txtWidth, height, getTxtFont(),
                    UIManager.getColor("textText"),
                    HtmlRenderer.STYLE_TRUNCATE, true);
            int bumpWidth = width
                    - (TXT_X_PAD + txtWidth + BUMP_X_PAD + buttonsWidth);
            if (bumpWidth > 0) {
                paintBump(index, g, x + TXT_X_PAD + txtWidth + BUMP_X_PAD,
                          y + BUMP_Y_PAD, bumpWidth, height - 2 * BUMP_Y_PAD);
            }
        } else {
            if( isTabBusy( index ) && !slidedOut ) {
                Icon busyIcon = BusyTabsSupport.getDefault().getBusyIcon( isSelected( index ) );
                txtWidth -= busyIcon.getIconWidth() - 3 - TXT_X_PAD;
                busyIcon.paintIcon( displayer, g, x+TXT_X_PAD, y+(height-busyIcon.getIconHeight())/2);
                x += busyIcon.getIconWidth() + 3;
            }

            txtWidth = width - 2 * TXT_X_PAD;
            HtmlRenderer.renderString(text, g, x + TXT_X_PAD, height - 
                fm.getDescent() - 4, txtWidth, height, getTxtFont(),
                UIManager.getColor("textText"),
                HtmlRenderer.STYLE_TRUNCATE, true);
        }
    }

    @Override
    protected void paintTabBorder(Graphics g, int index, int x, int y,
                                  int width, int height) {
        Color highlight = getBorderHighlight();
        Color shadow = getBorderShadow();
        boolean isSelected = isSelected(index);

        boolean isFirst = index == 0;
        boolean isLast = index == getDataModel().size() - 1;

        g.translate(x, y);
        
        // paint darker lines
        g.setColor(shadow);
        if (!isFirst) {
            g.drawLine(0, 0, 0, height - 5);
        }
        if (!isSelected) {
            g.drawLine(1, height - 5, isLast ? width - 1 : width, height - 5);
        }
        // paint brighter lines
        g.setColor(highlight);
        g.drawLine(1, 0, width - 1, 0);
        if (isFirst) {
            g.drawLine(0, 0, 0, height - 2);
        }
        if (!isSelected) {
            g.drawLine(0, height - 4, isLast ? width - 1 : width, height - 4);
        } 
        if( isLast ) {
            g.setColor(shadow);
            g.drawLine(width-1, 0, width-1, height - 5);
        }

        g.translate(-x, -y);
    }

    @Override
    protected void paintTabBackground(Graphics g, int index, int x, int y,
                                      int width, int height) {
        boolean selected = isSelected(index);
        boolean highlighted = selected && isActive();
        boolean attention = isAttention (index);
        if (highlighted && !attention) {
            g.setColor(getActBgColor());
            g.fillRect(x, y, width, height - 3);
        } else if (attention) {
            g.setColor(MetalEditorTabCellRenderer.ATTENTION_COLOR);
            g.fillRect(x, y, width, height - 3);
        } else {
            g.setColor(getInactBgColor());
            g.fillRect(x, y, width, height - 3);
        }
    }

    @Override
    int getModeButtonVerticalOffset() {
        return -1;
    }

    private void paintBump(int index, Graphics g, int x, int y, int width,
                           int height) {
        ColorUtil.paintViewTabBump(g, x, y, width, height, isFocused(index) ?
                                                           ColorUtil.FOCUS_TYPE :
                                                           ColorUtil.UNSEL_TYPE);
    }
    
    static Color getInactBgColor() {
        if (inactBgColor == null) {
            inactBgColor = (Color) UIManager.get("inactiveCaption");
            if (inactBgColor == null) {
                inactBgColor = new Color(204, 204, 204);
            }
        }
        return inactBgColor;
    }

    static Color getActBgColor() {
        if (actBgColor == null) {
            actBgColor = (Color) UIManager.get("activeCaption");
            if (actBgColor == null) {
                actBgColor = new Color(204, 204, 255);
            }
        }
        return actBgColor;
    }

    private Color getBorderHighlight() {
        if (borderHighlight == null) {
            borderHighlight = getInactBgColor().brighter();
        }
        return borderHighlight;
    }

    private Color getBorderShadow() {
        if (borderShadow == null) {
            borderShadow = getInactBgColor().darker();
        }
        return borderShadow;
    }


    private static void initIcons() {
        if( null == buttonIconPaths ) {
            buttonIconPaths = new HashMap<Integer, String[]>(7);
            
            //close button
            String[] iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/openide/awt/resources/metal_bigclose_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/openide/awt/resources/metal_bigclose_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/openide/awt/resources/metal_bigclose_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_CLOSE_BUTTON, iconPaths );
            
            //slide/pin button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/metal_slideright_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/metal_slideright_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/metal_slideright_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_RIGHT_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/metal_slideleft_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/metal_slideleft_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/metal_slideleft_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_LEFT_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/metal_slidebottom_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/metal_slidebottom_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/metal_slidebottom_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_DOWN_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/metal_pin_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/metal_pin_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/metal_pin_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_PIN_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/metal_restore_group_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/metal_restore_group_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/metal_restore_group_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_RESTORE_GROUP_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/metal_minimize_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/metal_minimize_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/metal_minimize_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_GROUP_BUTTON, iconPaths );
        }
    }

    public Icon getButtonIcon(int buttonId, int buttonState) {
        Icon res = null;
        initIcons();
        String[] paths = buttonIconPaths.get( buttonId );
        if( null != paths && buttonState >=0 && buttonState < paths.length ) {
            res = TabControlButtonFactory.getIcon( paths[buttonState] );
        }
        return res;
    }
}
