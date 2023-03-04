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
/*
 * WinClassicEditorTabDisplayerUI.java
 *
 * Created on 09 December 2003, 16:53
 */

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import org.netbeans.swing.tabcontrol.TabDisplayer;

import javax.swing.plaf.ComponentUI;

/**
 * Windows classic impl of tabs ui
 *
 * @author Tim Boudreau
 */
public final class WinClassicEditorTabDisplayerUI extends BasicScrollingTabDisplayerUI {
    
    private static final Rectangle scratch5 = new Rectangle();
    private static Map<Integer, String[]> buttonIconPaths;
    
    private static boolean isGenericUI = !"Windows".equals( //NOI18N
        UIManager.getLookAndFeel().getID());

    /**
     * Creates a new instance of WinClassicEditorTabDisplayerUI
     */
    public WinClassicEditorTabDisplayerUI(TabDisplayer displayer) {
        super (displayer);
    }
    
    public static ComponentUI createUI(JComponent c) {
        return new WinClassicEditorTabDisplayerUI ((TabDisplayer) c);
    }

    public Rectangle getTabRect(int idx, Rectangle rect) {
        Rectangle r = super.getTabRect (idx, rect);
        //For win classic, take up the full space, even the insets, to match
        //earlier appearance
        r.y = 0;
        r.height = displayer.getHeight();
        return r;
    }  

    public void install() {
        super.install();
        if (!isGenericUI) {
            displayer.setBackground( UIManager.getColor("tab_unsel_fill") );
            displayer.setOpaque(true);
        }
    }

    public Dimension getPreferredSize(JComponent c) {
        int prefHeight = 28;
        Graphics g = BasicScrollingTabDisplayerUI.getOffscreenGraphics(c);
        if (g != null) {
            FontMetrics fm = g.getFontMetrics(displayer.getFont());
            Insets ins = getTabAreaInsets();
            prefHeight = fm.getHeight() + ins.top + ins.bottom + (isGenericUI ? 5 : 6);
        }
        return new Dimension(displayer.getWidth(), prefHeight);
    }

    private void genericPaintAfterTabs (Graphics g) {
        g.setColor (UIManager.getColor("controlShadow")); //NOI18N
        Insets ins = displayer.getInsets();
        Rectangle r = new Rectangle();
        getTabsVisibleArea(r);
        r.width = displayer.getWidth();
        int selEnd = 0;
        int last = getLastVisibleTab();
        if (last > -1) {
            getTabRect (last, scratch5);
            g.drawLine (scratch5.x + scratch5.width, displayer.getHeight() -1, 
                displayer.getWidth() - (ins.left + ins.right) - 4, 
                displayer.getHeight() - 1);
            g.drawLine (0, displayer.getHeight() - 2, 2, displayer.getHeight() -2);
            //TODO remove when a specific GTK l&f UI class is available
            if ("GTK".equals(UIManager.getLookAndFeel().getID())) {
                boolean sel = last == displayer.getSelectionModel().getSelectedIndex();
                //paint a fading shadow to match the view tabs
                int x = scratch5.x + scratch5.width;
                g.setColor (sel ? UIManager.getColor("controlShadow") :
                    ColorUtil.adjustTowards(g.getColor(), 20,
                    UIManager.getColor("control"))); //NOI18N
                g.drawLine (x, 
                    scratch5.y + 5, x,
                    scratch5.y + scratch5.height -2);
                g.setColor (ColorUtil.adjustTowards(g.getColor(), 20,
                    UIManager.getColor("control"))); //NOI18N
                g.drawLine (x + 1, 
                    scratch5.y + 6, x + 1,
                    scratch5.y + scratch5.height -2);
            }
            if ((tabState.getState(getFirstVisibleTab()) & TabState.CLIP_LEFT)
                !=0 && getFirstVisibleTab() != 
                displayer.getSelectionModel().getSelectedIndex()) {
                    //Draw a small gradient line continuing the left edge of
                    //the displayer up the left side of a left clipped tab
                GradientPaint gp = ColorUtil.getGradientPaint(
                    0, displayer.getHeight() / 2, UIManager.getColor("control"),
                    0, displayer.getHeight(), UIManager.getColor("controlShadow"));
                ((Graphics2D) g).setPaint(gp);
                g.drawLine (0, displayer.getHeight() / 2, 0, displayer.getHeight());
            } else {
                //Fill the small gap between the top of the content displayer
                //and the bottom of the tabs, caused by the tab area bottom inset
                g.setColor (UIManager.getColor("controlShadow"));
                g.drawLine (0, displayer.getHeight(), 0, displayer.getHeight() - 2);
            }
            if ((tabState.getState(getLastVisibleTab()) & TabState.CLIP_RIGHT) != 0
                && getLastVisibleTab() != 
                displayer.getSelectionModel().getSelectedIndex()) {
                GradientPaint gp = ColorUtil.getGradientPaint(
                    0, displayer.getHeight() / 2, UIManager.getColor("control"),
                    0, displayer.getHeight(), UIManager.getColor("controlShadow"));
                ((Graphics2D) g).setPaint(gp);
                getTabRect (getLastVisibleTab(), scratch5);
                g.drawLine (scratch5.x + scratch5.width, displayer.getHeight() / 2, 
                    scratch5.x + scratch5.width, displayer.getHeight());
            }
            
        } else {
            g.drawLine(r.x, displayer.getHeight() - ins.bottom, r.x + r.width - 4,
                       displayer.getHeight() - ins.bottom);
        }
    }
    
    protected void paintAfterTabs(Graphics g) {
        if (isGenericUI) {
            genericPaintAfterTabs(g);
            return;
        }
        Rectangle r = new Rectangle();
        getTabsVisibleArea(r);
        r.width = displayer.getWidth();
        g.setColor(displayer.isActive() ?
                   defaultRenderer.getSelectedActivatedBackground() :
                   defaultRenderer.getSelectedBackground());

        Insets ins = getTabAreaInsets();
        ins.bottom++;
        g.fillRect(r.x, r.y + r.height, r.x + r.width,
                   displayer.getHeight() - (r.y + r.height));

        g.setColor(UIManager.getColor("controlLtHighlight")); //NOI18N

        int selEnd = 0;
        int i = selectionModel.getSelectedIndex();
        if (i != -1) {
            getTabRect(i, scratch5);
            if (scratch5.width != 0) {
                if (r.x < scratch5.x) {
                    g.drawLine(r.x, displayer.getHeight() - ins.bottom,
                               scratch5.x - 1,
                               displayer.getHeight() - ins.bottom);
                }
                if (scratch5.x + scratch5.width < r.x + r.width) {
                    selEnd = scratch5.x + scratch5.width;
                    //If the last tab is not clipped, the final tab is one
                    //pixel smaller; we need to overwrite one pixel of the
                    //border or there will be a small stub sticking down
                    if (!scroll().isLastTabClipped()) {
                        selEnd--;
                    }
                    g.drawLine(selEnd, displayer.getHeight() - ins.bottom,
                               r.x + r.width,
                               displayer.getHeight() - ins.bottom);
                }
            }
            return;
        }

        g.drawLine(r.x, displayer.getHeight() - ins.bottom, r.x + r.width,
                   displayer.getHeight() - ins.bottom);
    }

    protected TabCellRenderer createDefaultRenderer() {
        return new WinClassicEditorTabCellRenderer();
    }

    private static void initIcons() {
        if( null == buttonIconPaths ) {
            buttonIconPaths = new HashMap<Integer, String[]>(7);
            
            //left button
            String[] iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/win_scrollleft_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/win_scrollleft_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/win_scrollleft_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/win_scrollleft_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SCROLL_LEFT_BUTTON, iconPaths );
            
            //right button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/win_scrollright_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/win_scrollright_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/win_scrollright_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/win_scrollright_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SCROLL_RIGHT_BUTTON, iconPaths );
            
            //drop down button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/win_popup_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/win_popup_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/win_popup_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/win_popup_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_DROP_DOWN_BUTTON, iconPaths );
            
            //maximize/restore button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/win_maximize_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/win_maximize_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/win_maximize_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/win_maximize_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_MAXIMIZE_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/win_restore_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/win_restore_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/win_restore_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/win_restore_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_RESTORE_BUTTON, iconPaths );
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
    
    protected Rectangle getControlButtonsRectangle( Container parent ) {
        Component c = getControlButtons();
        return new Rectangle( parent.getWidth()-c.getWidth()-4, 4, c.getWidth(), c.getHeight() );
    }

    public Insets getTabAreaInsets() {
        Insets retValue = super.getTabAreaInsets();
        retValue.right += 4;
        return retValue;
    }
}
