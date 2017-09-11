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

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
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
 * Windows classic-like user interface of view type tabs. Implements Border just
 * to save one class, change if apropriate.
 *
 * @author Dafe Simonek
 */
public final class WinClassicViewTabDisplayerUI extends AbstractViewTabDisplayerUI {

    private static final boolean isGenericUI =
        !"Windows".equals(UIManager.getLookAndFeel().getID()); //NOI18N
    
    private static final Color GTK_TABBED_PANE_BACKGROUND_1 = new Color(255, 255, 255);
    
    /**
     * ******** constants ************
     */

    private static final int BUMP_X_PAD = isGenericUI ? 0 : 3;
    private static final int BUMP_WIDTH = isGenericUI ? 0 : 3;
    private static final int TXT_X_PAD = isGenericUI ? 3 : BUMP_X_PAD + BUMP_WIDTH + 5;
    private static final int TXT_Y_PAD = 3;

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
    private WinClassicViewTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
        prefSize = new Dimension(100, 19); 
    }

    public static ComponentUI createUI(JComponent c) {
        return new WinClassicViewTabDisplayerUI((TabDisplayer) c);
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        FontMetrics fm = getTxtFontMetrics();
        int height = fm == null ?
                19 : fm.getAscent() + 2 * fm.getDescent() + 2;
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
        if (isGenericUI) {
            return;
        }
        Rectangle r = c.getBounds();
        g.setColor(UIManager.getColor("InternalFrame.borderDarkShadow")); //NOI18N
        g.drawLine(0, r.height - 1, r.width - 1, r.height - 1);
    }
    
    @Override
    protected Font getTxtFont() {
        if (isGenericUI) {
            Font result = UIManager.getFont("controlFont");
            if (result != null) {
                return result;
            }
        }
        return super.getTxtFont();
    }     

    @Override
    protected void paintTabContent(Graphics g, int index, String text, int x,
                                   int y, int width, int height) {
        // substract lower border
        height--;
        y -= 2; //align to center
        FontMetrics fm = getTxtFontMetrics();
        // setting font already here to compute string width correctly
        g.setFont(getTxtFont());
        int txtWidth = width;
        if (isSelected(index)) {
            Component buttons = getControlButtons();
            if( null != buttons ) {
                Dimension buttonsSize = buttons.getPreferredSize();
                if( width < buttonsSize.width+ICON_X_PAD ) {
                    buttons.setVisible( false );
                } else {
                    buttons.setVisible( true );
                    txtWidth = width - (buttonsSize.width + ICON_X_PAD + TXT_X_PAD);
                    buttons.setLocation( x + txtWidth+TXT_X_PAD, y + (height-buttonsSize.height)/2+1 );
                }
            }
        } else {
            txtWidth = width - 2 * TXT_X_PAD;
        }
        if( isUseStretchingTabs() ) {
            // draw bump (dragger)
            drawBump(g, index, x + 4, y + 6, BUMP_WIDTH, height - 8);
        }

        boolean slidedOut = false;
        WinsysInfoForTabbedContainer winsysInfo = displayer.getContainerWinsysInfo();
        if( null != winsysInfo && winsysInfo.isSlidedOutContainer() )
            slidedOut = false;
        if( isTabBusy( index ) && !slidedOut ) {
            Icon busyIcon = BusyTabsSupport.getDefault().getBusyIcon( isSelected( index ) );
            txtWidth -= busyIcon.getIconWidth() + 3 + TXT_X_PAD;
            busyIcon.paintIcon( displayer, g, x+TXT_X_PAD, y+(height-busyIcon.getIconHeight())/2+1);
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
                                      
        // subtract lower border
        height--;
        boolean isSelected = isSelected(index);

        g.translate(x, y);

        g.setColor(UIManager.getColor("InternalFrame.borderShadow")); //NOI18N
        g.drawLine(0, height - 1, width - 2, height - 1);
        g.drawLine(width - 1, height - 1, width - 1, 0);

        g.setColor(isSelected ? UIManager.getColor(
                "InternalFrame.borderHighlight") //NOI18N
                   : UIManager.getColor("InternalFrame.borderLight")); //NOI18N
        g.drawLine(0, 0, 0, height - 1);
        g.drawLine(1, 0, width - 2, 0);

        g.translate(-x, -y);
    }

    @Override
    protected void paintTabBackground(Graphics g, int index, int x, int y,
                                      int width, int height) {
        // substract lower border
        height--;
        ((Graphics2D) g).setPaint(
                getBackgroundPaint(g, index, x, y, width, height));
        if (isFocused(index)) {
            g.fillRect(x, y, width, height);
        } else {
            g.fillRect(x + 1, y + 1, width - 2, height - 2);
        }
    }

    private Paint getBackgroundPaint(Graphics g, int index, int x, int y,
                                     int width, int height) {
        // background body, colored according to state
        boolean selected = isSelected(index);
        boolean focused = isFocused(index);
        boolean attention = isAttention(index);
        
        Paint result = null;
        if (focused && !attention) {
            result = ColorUtil.getGradientPaint(x, y, getSelGradientColor(), x + width, y, getSelGradientColor2());
        } else if (selected && !attention) {
            result = UIManager.getColor("TabbedPane.background"); //NOI18N
        } else if (attention) {
            result = WinClassicEditorTabCellRenderer.ATTENTION_COLOR;
        } else {
            result = UIManager.getColor("tab_unsel_fill");
        }
        return result;
    }

    /**
     * Paints dragger in given rectangle
     */
    private void drawBump(Graphics g, int index, int x, int y, int width,
                          int height) {
        if (isGenericUI) {
            //This look and feel is also used as the default UI on non-JDS
            return;
        }
                              
        // prepare colors
        Color highlightC, bodyC, shadowC;
        if (isFocused(index)) {
            bodyC = new Color(210, 220, 243); //XXX
            highlightC = bodyC.brighter();
            shadowC = bodyC.darker();
        } else if (isSelected(index)) {
            highlightC =
                    UIManager.getColor("InternalFrame.borderHighlight"); //NOI18N
            bodyC = UIManager.getColor("InternalFrame.borderLight"); //NOI18N
            shadowC = UIManager.getColor("InternalFrame.borderShadow"); //NOI18N
        } else {
            highlightC = UIManager.getColor("InternalFrame.borderLight"); //NOI18N
            bodyC = UIManager.getColor("tab_unsel_fill");
            shadowC = UIManager.getColor("InternalFrame.borderShadow"); //NOI18N
        }
        // draw
        for (int i = 0; i < width / 3; i++, x += 3) {
            g.setColor(highlightC);
            g.drawLine(x, y, x, y + height - 1);
            g.drawLine(x, y, x + 1, y);
            g.setColor(bodyC);
            g.drawLine(x + 1, y + 1, x + 1, y + height - 2);
            g.setColor(shadowC);
            g.drawLine(x + 2, y, x + 2, y + height - 1);
            g.drawLine(x, y + height - 1, x + 1, y + height - 1);
        }
    }

    private static Color getSelGradientColor() {
        if ("GTK".equals(UIManager.getLookAndFeel().getID())) { // NOI18N
            return GTK_TABBED_PANE_BACKGROUND_1; // #68200
        } else {
            return UIManager.getColor("winclassic_tab_sel_gradient"); // NOI18N
        }
    }
    
    private static Color getSelGradientColor2() {
        return UIManager.getColor("TabbedPane.background"); // NOI18N
    }

    private static void initIcons() {
        if( null == buttonIconPaths ) {
            buttonIconPaths = new HashMap<Integer, String[]>(7);
            
            //close button
            String[] iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/openide/awt/resources/win_bigclose_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/openide/awt/resources/win_bigclose_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/openide/awt/resources/win_bigclose_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/openide/awt/resources/win_bigclose_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_CLOSE_BUTTON, iconPaths );
            
            //slide/pin button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/win_slideright_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/win_slideright_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/win_slideright_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_RIGHT_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/win_slideleft_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/win_slideleft_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/win_slideleft_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_LEFT_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/win_slidebottom_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/win_slidebottom_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/win_slidebottom_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_DOWN_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/win_pin_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/win_pin_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/win_pin_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_PIN_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/win_restore_group_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/win_restore_group_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/win_restore_group_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_RESTORE_GROUP_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/win_minimize_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/win_minimize_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/win_minimize_rollover.png"; // NOI18N
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
