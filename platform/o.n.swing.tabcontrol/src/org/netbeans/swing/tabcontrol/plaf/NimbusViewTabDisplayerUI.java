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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import javax.swing.plaf.ComponentUI;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import org.netbeans.swing.tabcontrol.WinsysInfoForTabbedContainer;
import org.openide.awt.HtmlRenderer;

/**
 * Nimbus user interface of view type tabs. It uses native engine to paint tab
 * background.
 *
 * @author Dafe Simonek
 */
public final class NimbusViewTabDisplayerUI extends AbstractViewTabDisplayerUI {
    
    /**
     * ******** constants ************
     */

    private static final int BUMP_X_PAD = 0;
    private static final int BUMP_WIDTH = 0;
    private static final int TXT_X_PAD = 7;
    private static final int TXT_Y_PAD = 4;

    private static final int ICON_X_PAD = 2;
    
    private static Map<Integer, String[]> buttonIconPaths;
    
    /**
     * ******** instance fields ********
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
    private NimbusViewTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
        prefSize = new Dimension(100, 19);
    }
    
    public static ComponentUI createUI(JComponent c) {
        return new NimbusViewTabDisplayerUI((TabDisplayer) c);
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        FontMetrics fm = getTxtFontMetrics();
        int height = fm == null ?
                19 : fm.getAscent() + 2 * fm.getDescent() + 5;
        Insets insets = c.getInsets();
        prefSize.height = height + insets.bottom + insets.top;
        return prefSize;
    }

    /**
     * adds painting of overall border
     */
    @Override
    public void paint(Graphics g, JComponent c) {

        ColorUtil.setupAntialiasing(g);

        Color col = c.getBackground();
        if (col != null) {
            g.setColor (col);
            g.fillRect (0, 0, c.getWidth(), c.getHeight());
        }
        paintOverallBorder(g, c);
        super.paint(g, c);
    }

    /**
     * Paints lower border, bottom line, separating tabs from content
     */
    protected void paintOverallBorder(Graphics g, JComponent c) {
        return;
    }
    
    @Override
    protected Font getTxtFont() {
        Font result = UIManager.getFont("TabbedPane.font");
        if (result != null) {
            return result;
        }
        result = UIManager.getFont("controlFont");
        if (result != null) {
            return result;
        }
        return super.getTxtFont();
    }     

    @Override
    protected void paintTabContent(Graphics g, int index, String text, int x,
                                   int y, int width, int height) {
        // substract lower border
        height--;
        FontMetrics fm = getTxtFontMetrics();
        // setting font already here to compute string width correctly
        g.setFont(getTxtFont());
        int txtWidth = width;
        if (isSelected(index)) {
            Component buttons = getControlButtons();
            if( null != buttons ) {
                Dimension buttonsSize = buttons.getPreferredSize();
                if( width < buttonsSize.width+2*ICON_X_PAD ) {
                    buttons.setVisible( false );
                } else {
                    buttons.setVisible( true );
                    txtWidth = width - (buttonsSize.width + 2*ICON_X_PAD + TXT_X_PAD);
                    buttons.setLocation(x + txtWidth +  TXT_X_PAD+ICON_X_PAD, y + (height - buttonsSize.height)/2 + (TXT_Y_PAD / 2));
                }
            }
        } else {
            txtWidth = width - 2 * TXT_X_PAD;
        }
        // draw bump (dragger)
        drawBump(g, index, x + 4, y + 6, BUMP_WIDTH, height - 8);
        
        boolean slidedOut = false;
        WinsysInfoForTabbedContainer winsysInfo = displayer.getContainerWinsysInfo();
        if( null != winsysInfo && winsysInfo.isSlidedOutContainer() )
            slidedOut = false;
        if( isTabBusy( index ) && !slidedOut ) {
            Icon busyIcon = BusyTabsSupport.getDefault().getBusyIcon( isSelected( index ) );
            txtWidth -= busyIcon.getIconWidth() - 3 - TXT_X_PAD;
            busyIcon.paintIcon( displayer, g, x+TXT_X_PAD, y+(height-busyIcon.getIconHeight())/2);
            x += busyIcon.getIconWidth() + 3;
        }

        // draw text in right color
        Color txtC = UIManager.getColor("TabbedPane.foreground"); //NOI18N
        
        HtmlRenderer.renderString(text, g, x + TXT_X_PAD, y + fm.getAscent()
            + TXT_Y_PAD,
            txtWidth, height, getTxtFont(),
            txtC,
            HtmlRenderer.STYLE_TRUNCATE, true);
    }

    @Override
    protected void paintTabBorder(Graphics g, int index, int x, int y,
                                  int width, int height) {

        return;
    }
    
    private static void paintTabBackgroundNative (Graphics g, int index, int state,
    int x, int y, int w, int h) {
    }
    /*
key:TabbedPane:TabbedPaneTab[Disabled+Selected].backgroundPainter
key:TabbedPane:TabbedPaneTab[Disabled].backgroundPainter
key:TabbedPane:TabbedPaneTab[Enabled+MouseOver].backgroundPainter
key:TabbedPane:TabbedPaneTab[Enabled+Pressed].backgroundPainter
key:TabbedPane:TabbedPaneTab[Enabled].backgroundPainter
key:TabbedPane:TabbedPaneTab[Focused+MouseOver+Selected].backgroundPainter
key:TabbedPane:TabbedPaneTab[Focused+Pressed+Selected].backgroundPainter
key:TabbedPane:TabbedPaneTab[Focused+Selected].backgroundPainter
key:TabbedPane:TabbedPaneTab[MouseOver+Selected].backgroundPainter
key:TabbedPane:TabbedPaneTab[Pressed+Selected].backgroundPainter
key:TabbedPane:TabbedPaneTab[Selected].backgroundPainter
*/
    @Override
    protected void paintTabBackground(Graphics g, int index, int x, int y,
                                      int width, int height) {
        boolean isLast = index == getDataModel().size()-1;
        if (!isLast) {
            width++;
        }

        Shape clip = g.getClip();
        boolean isPreviousTabSelected = index-1 == displayer.getSelectionModel().getSelectedIndex();
        if (isPreviousTabSelected) {
            g.setClip(x+1, y, width-1, height);
        }

        final boolean attention = isAttention(index);
        Object o = null;
        if (isSelected(index)) {
            String mouseOver = "TabbedPane:TabbedPaneTab[MouseOver+Selected].backgroundPainter";
            String selected = "TabbedPane:TabbedPaneTab[Selected].backgroundPainter";
            if (isActive()) {
                o = UIManager.get( attention ? selected : mouseOver);
            } else {
                o = UIManager.get( attention ? mouseOver: selected);
            }
        } else {
            if( attention ) {
                o = UIManager.get("TabbedPane:TabbedPaneTab[Disabled].backgroundPainter");
            } else {
                o = UIManager.get("TabbedPane:TabbedPaneTab[Enabled].backgroundPainter");
            }
        }

        if (o instanceof javax.swing.Painter) {
            javax.swing.Painter painter = (javax.swing.Painter) o;
            BufferedImage bufIm = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = bufIm.createGraphics();
            g2d.setBackground(UIManager.getColor("Panel.background"));
            g2d.clearRect(0, 0, width, height);
            painter.paint(g2d, null, width, height);
            g.drawImage(bufIm, x, y, null);
        }

        if (isPreviousTabSelected) {
            g.setClip(clip);
        }
    }

    @Override
    protected void paintDisplayerBackground( Graphics g, JComponent c ) {

        int x = 0;
        int y = 0;
        int width = c.getWidth();
        int height = c.getHeight();
        
        Object o = null;
        o = UIManager.get("TabbedPane:TabbedPaneTab[Enabled].backgroundPainter");

        if (o instanceof javax.swing.Painter) {
            javax.swing.Painter painter = (javax.swing.Painter) o;
            BufferedImage bufIm = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = bufIm.createGraphics();
            g2d.setBackground(UIManager.getColor("Panel.background"));
            g2d.clearRect(0, 0, width, height);
            painter.paint(g2d, null, width, height);
            g.drawImage(bufIm, x, y, null);
        }
    }

    /**
     * Paints dragger in given rectangle
     */
    private void drawBump(Graphics g, int index, int x, int y, int width,
                          int height) {
            //This look and feel is also used as the default UI on non-JDS
        return;
    }
    
    private static void initIcons() {
        if( null == buttonIconPaths ) {
            buttonIconPaths = new HashMap<Integer, String[]>(7);
            
            //close button
            String[] iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/openide/awt/resources/gtk_bigclose_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/openide/awt/resources/gtk_bigclose_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/openide/awt/resources/gtk_bigclose_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_CLOSE_BUTTON, iconPaths );
            
            //slide/pin button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/gtk_slideright_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/gtk_slideright_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/gtk_slideright_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_RIGHT_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/gtk_slideleft_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/gtk_slideleft_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/gtk_slideleft_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_LEFT_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/gtk_slidebottom_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/gtk_slidebottom_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/gtk_slidebottom_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_DOWN_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/gtk_pin_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/gtk_pin_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/gtk_pin_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_PIN_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/nimbus_restore_group_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/nimbus_restore_group_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/nimbus_restore_group_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_RESTORE_GROUP_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/nimbus_minimize_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/nimbus_minimize_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/nimbus_minimize_rollover.png"; // NOI18N
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
    
}
