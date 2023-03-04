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
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.openide.awt.HtmlRenderer;

import javax.swing.plaf.ComponentUI;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import org.netbeans.swing.tabcontrol.WinsysInfoForTabbedContainer;

/**
 * A view tabs ui for OS-X adapted from the view tabs UI for Metal.
 *
 * @author Tim Boudreau
 */
public class AquaViewTabDisplayerUI extends AbstractViewTabDisplayerUI {

    private static final int TXT_X_PAD = 5;
    private static final int ICON_X_PAD = 2;

    /********* static fields ***********/
    
    private static Map<Integer, String[]> buttonIconPaths;
    
    /**
     * ******* instance fields *********
     */

    private Dimension prefSize;

    /**
     * Should be constructed only from createUI method.
     */
    protected AquaViewTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
        prefSize = new Dimension(100, 19); //XXX huh?
    }

    public static ComponentUI createUI(JComponent c) {
        return new AquaViewTabDisplayerUI((TabDisplayer) c);
    }

    @Override
    protected AbstractViewTabDisplayerUI.Controller createController() {
        return new OwnController();
    }
    
    @Override
    public Dimension getPreferredSize(JComponent c) {
        FontMetrics fm = getTxtFontMetrics();
        int height = fm == null ?
                21 : fm.getAscent() + 2 * fm.getDescent() + 3;
        height += 1; //align with editor tabs
        Insets insets = c.getInsets();
        prefSize.height = height + insets.bottom + insets.top;
        return prefSize;
    }

    /**
     * @return true if tab with given index has mouse cursor above and is not
     * the selected one, false otherwise.
     */
    private boolean isMouseOver(int index) {
        return ((OwnController) getController()).getMouseIndex() == index
                && !isSelected(index);
    }

    @Override
    protected void paintTabContent(Graphics g, int index, String text, int x,
                                   int y, int width, int height) {
        FontMetrics fm = getTxtFontMetrics();

        // setting font already here to compute string width correctly
        g.setFont(getTxtFont());
        int textW = width;

        if (isSelected(index)) {
            Component buttons = getControlButtons();
            if( null != buttons ) {
                Dimension buttonsSize = buttons.getPreferredSize();

                if( width < buttonsSize.width+ICON_X_PAD ) {
                    buttons.setVisible( false );
                } else {
                    buttons.setVisible( true );
                    textW = width - (buttonsSize.width + ICON_X_PAD + 2*TXT_X_PAD);
                    buttons.setLocation( x + textW+2*TXT_X_PAD-2, y + (height-buttonsSize.height)/2 );
                }
            }
        } else {
            textW = width - 2 * TXT_X_PAD;
        }

        if (text.length() == 0) {
            return;
        }

        int textHeight = fm.getHeight();
        int textY;
        int textX = x + TXT_X_PAD;
        if (index == 0)
            textX = x + 5;

        if (textHeight > height) {
            textY = (-1 * ((textHeight - height) / 2)) + fm.getAscent()
                    - 1;
        } else {
            textY = (height / 2) - (textHeight / 2) + fm.getAscent();
        }

        boolean slidedOut = false;
        WinsysInfoForTabbedContainer winsysInfo = displayer.getContainerWinsysInfo();
        if( null != winsysInfo && winsysInfo.isSlidedOutContainer() )
            slidedOut = false;
        if( isTabBusy( index ) && !slidedOut ) {
            Icon busyIcon = BusyTabsSupport.getDefault().getBusyIcon( isSelected( index ) );
            textW -= busyIcon.getIconWidth() - 3 - TXT_X_PAD;
            busyIcon.paintIcon( displayer, g, textX, y+(height-busyIcon.getIconHeight())/2);
            textX += busyIcon.getIconWidth() + 3;
        }

        int realTextWidth = (int)HtmlRenderer.renderString(text, g, textX, textY, textW, height, getTxtFont(),
                          UIManager.getColor("textText"), //NOI18N
                          HtmlRenderer.STYLE_TRUNCATE, false);
        realTextWidth = Math.min(realTextWidth, textW);
        if( textW > realTextWidth )
            textX += (textW - realTextWidth) / 2;


        HtmlRenderer.renderString(text, g, textX, textY, textW, height, getTxtFont(),
                          UIManager.getColor("textText"),
                          HtmlRenderer.STYLE_TRUNCATE, true);
    }

    private static void drawLine(Graphics g, int x1, int y1, int x2, int y2) {
        AquaEditorTabDisplayerUI.drawLine(g, x1, y1, x2, y2);
    }
    
    @Override
    protected void paintTabBorder(Graphics g, int index, int x, int y,
                                  int width, int height) {
        Color borderColor = UIManager.getColor("NbTabControl.borderColor");
        Color borderShadowColor = UIManager.getColor("NbTabControl.borderShadowColor");
        g.setColor(borderColor);
        if( index > 0 ) {
            drawLine(g, x, y, x, y+height);
            if( !isSelected(index) ) {
                g.setColor(borderShadowColor);
                drawLine(g, x+1, y+1, x+1, y+height-1);
            }
        }
        if( index < getDataModel().size()-1 || !isUseStretchingTabs() ) {
            g.setColor(borderColor);
            drawLine(g, x+width, y, x+width, y+height);
            if( !isSelected(index) ) {
                g.setColor(borderShadowColor);
                drawLine(g, x+width-1, y+1, x+width-1, y+height-1);
            }
        }
        g.setColor(borderColor);
        if( !isSelected(index) ) {
            drawLine(g, x, y+height-1, x+width, y+height-1);
        }
        drawLine(g, x, y, x+width, y);
        if( getDataModel().size() == 1 ) {
            g.setColor(UIManager.getColor("NbTabControl.editorTabBackground"));
            drawLine(g, x, y+height-1, x+width, y+height-1);
        }
        if( isSelected(index) && isFocused(index) ) {
            g.setColor(UIManager.getColor("NbTabControl.focusedTabBackground"));
            drawLine(g, x+(index == 0 ? 0 : 1), y+1, x+width-1, y+1);
            drawLine(g, x+(index == 0 ? 0 : 1), y+2, x+width-1, y+2);
        }
    }

    @Override
    protected void paintTabBackground(Graphics g, int index, int x, int y,
                                      int width, int height) {
        Graphics2D g2d = (Graphics2D) g;
        Paint p = g2d.getPaint();
        if( isSelected(index) ) {
            g2d.setPaint( ColorUtil.getGradientPaint(x, y, UIManager.getColor("NbTabControl.selectedTabBrighterBackground"),
                    x, y+height/2, UIManager.getColor("NbTabControl.selectedTabDarkerBackground")) );
        } else if( isMouseOver(index) ) {
            g2d.setPaint( ColorUtil.getGradientPaint(x, y, UIManager.getColor("NbTabControl.mouseoverTabBrighterBackground"),
                    x, y+height/2, UIManager.getColor("NbTabControl.mouseoverTabDarkerBackground")) );
        } else {
            g2d.setPaint( ColorUtil.getGradientPaint(x, y, UIManager.getColor("NbTabControl.inactiveTabBrighterBackground"),
                    x, y+height/2, UIManager.getColor("NbTabControl.inactiveTabDarkerBackground")) );
        }
        g2d.fillRect(x, y, width, height);
        g2d.setPaint(p);
    }

    private static void initIcons() {
        if( null == buttonIconPaths ) {
            buttonIconPaths = new HashMap<Integer, String[]>(7);
            
            //close button
            String[] iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/openide/awt/resources/mac_bigclose_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/openide/awt/resources/mac_bigclose_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/openide/awt/resources/mac_bigclose_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_CLOSE_BUTTON, iconPaths );
            
            //slide/pin button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/mac_slideright_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/mac_slideright_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/mac_slideright_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_RIGHT_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/mac_slideleft_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/mac_slideleft_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/mac_slideleft_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_LEFT_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/mac_slidebottom_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/mac_slidebottom_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/mac_slidebottom_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_DOWN_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/mac_pin_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/mac_pin_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/mac_pin_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_PIN_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/mac_restore_group_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/mac_restore_group_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/mac_restore_group_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_RESTORE_GROUP_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/mac_minimize_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/mac_minimize_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/mac_minimize_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_GROUP_BUTTON, iconPaths );
        }
    }

    @Override
    public Icon getButtonIcon(int buttonId, int buttonState) {
        Icon res = null;
        initIcons();
        String[] paths = buttonIconPaths.get( buttonId );
        if( null != paths && buttonState >=0 && buttonState < paths.length ) {
            res = TabControlButtonFactory.getIcon( paths[buttonState] );
        }
        return res;
    }

    /**
     * Own close icon button controller
     */
    private class OwnController extends Controller {


        /**
         * holds index of tab in which mouse pointer was lastly located. -1
         * means mouse pointer is out of component's area
         */
        // TBD - should be part of model, not controller
        private int lastIndex = -1;

        /**
         * @return Index of tab in which mouse pointer is currently located.
         */
        public int getMouseIndex() {
            return lastIndex;
        }

        /**
         * Triggers visual tab header change when mouse enters/leaves tab in
         * advance to superclass functionality.
         */
        @Override
        public void mouseMoved(MouseEvent e) {
            super.mouseMoved(e);
            Point pos = e.getPoint();
            updateHighlight(getLayoutModel().indexOfPoint(pos.x, pos.y));
        }

        /**
         * Resets tab header in advance to superclass functionality
         */
        @Override
        public void mouseExited(MouseEvent e) {
            super.mouseExited(e);
            if( !inControlButtonsRect(e.getPoint())) {
                updateHighlight(-1);
            }
        }

        /**
         * Invokes repaint of dirty region if needed
         */
        private void updateHighlight(int curIndex) {
            if (curIndex == lastIndex) {
                return;
            }
            // compute region which needs repaint
            TabLayoutModel tlm = getLayoutModel();
            int x, y, w, h;
            Rectangle repaintRect = null;
            if (curIndex != -1) {
                x = tlm.getX(curIndex)-1;
                y = tlm.getY(curIndex);
                w = tlm.getW(curIndex)+2;
                h = tlm.getH(curIndex);
                repaintRect = new Rectangle(x, y, w, h);
            }
            // due to model changes, lastIndex may become invalid, so check
            if ((lastIndex != -1) && (lastIndex < getDataModel().size())) {
                x = tlm.getX(lastIndex)-1;
                y = tlm.getY(lastIndex);
                w = tlm.getW(lastIndex)+2;
                h = tlm.getH(lastIndex);
                if (repaintRect != null) {
                    repaintRect =
                            repaintRect.union(new Rectangle(x, y, w, h));
                } else {
                    repaintRect = new Rectangle(x, y, w, h);
                }
            }
            // trigger repaint if needed, update index
            if (repaintRect != null) {
                getDisplayer().repaint(repaintRect);
            }
            lastIndex = curIndex;
        }
    } // end of OwnController

}
