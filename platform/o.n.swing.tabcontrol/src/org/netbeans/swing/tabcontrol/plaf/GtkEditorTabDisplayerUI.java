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
 * GtkEditorTabDisplayerUI.java
 *
 * Created on 09 December 2003, 16:53
 */

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
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

/**
 * Gtk impl of tabs ui
 *
 * @author Marek Slama
 */
public final class GtkEditorTabDisplayerUI extends BasicScrollingTabDisplayerUI {
    
    private static Map<Integer, String[]> buttonIconPaths;
    
    /**
     * Creates a new instance of GtkEditorTabDisplayerUI
     */
    public GtkEditorTabDisplayerUI(TabDisplayer displayer) {
        super (displayer);
    }
    
    public static ComponentUI createUI(JComponent c) {
        return new GtkEditorTabDisplayerUI ((TabDisplayer) c);
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
    }

    public Dimension getPreferredSize(JComponent c) {
        int prefHeight = 28;
        Graphics g = BasicScrollingTabDisplayerUI.getOffscreenGraphics(c);
        if (g != null) {
            FontMetrics fm = g.getFontMetrics(displayer.getFont());
            Insets ins = getTabAreaInsets();
            prefHeight = fm.getHeight() + ins.top + ins.bottom + 12;
        }
        return new Dimension(displayer.getWidth(), prefHeight);
    }
    
    protected void paintAfterTabs(Graphics g) {
        Rectangle bounds = displayer.getBounds();

        int lineY = bounds.y + bounds.height - 1;
        int sel = displayer.getSelectionModel().getSelectedIndex();

        Color shadowC = UIManager.getColor("controlShadow");
        shadowC = shadowC != null ? shadowC : Color.DARK_GRAY;

        if (sel != -1) {
            Color controlC = UIManager.getColor("control");
            controlC = controlC != null ? controlC : Color.GRAY;

            Rectangle tabRect = new Rectangle();
            displayer.getTabRect(sel, tabRect);
            g.setColor(shadowC);
            g.drawLine(bounds.x, lineY, bounds.x + tabRect.x - 1, lineY);
            g.drawLine(bounds.x + tabRect.x + tabRect.width, lineY,
                    bounds.x + bounds.width - 1, lineY);
            g.setColor(controlC);
            g.drawLine(bounds.x + tabRect.x, lineY,
                    bounds.x + tabRect.x + tabRect.width - 1, lineY);
        } else {
            g.setColor(shadowC);
            g.drawLine(bounds.x, lineY, bounds.x + bounds.width - 1, lineY);
        }
    }

    protected TabCellRenderer createDefaultRenderer() {
        return new GtkEditorTabCellRenderer();
    }

    private static void initIcons() {
        if( null == buttonIconPaths ) {
            buttonIconPaths = new HashMap<Integer, String[]>(7);
            
            //left button
            String[] iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/gtk_scrollleft_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_PRESSED] = iconPaths[TabControlButton.STATE_DEFAULT];
            buttonIconPaths.put( TabControlButton.ID_SCROLL_LEFT_BUTTON, iconPaths );

            //right button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/gtk_scrollright_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_PRESSED] = iconPaths[TabControlButton.STATE_DEFAULT];
            buttonIconPaths.put( TabControlButton.ID_SCROLL_RIGHT_BUTTON, iconPaths );

            //drop down button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/gtk_popup_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_PRESSED] = iconPaths[TabControlButton.STATE_DEFAULT];
            buttonIconPaths.put( TabControlButton.ID_DROP_DOWN_BUTTON, iconPaths );

            //maximize/restore button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/gtk_maximize_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_PRESSED] = iconPaths[TabControlButton.STATE_DEFAULT];
            buttonIconPaths.put( TabControlButton.ID_MAXIMIZE_BUTTON, iconPaths );

            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/gtk_restore_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_PRESSED] = iconPaths[TabControlButton.STATE_DEFAULT];
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
        retValue.bottom += 1;
        return retValue;
    }
}
