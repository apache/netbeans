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
/*
 * MetalEditorTabDisplayerUI.java
 *
 * Created on December 2, 2003, 9:40 PM
 */

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import javax.swing.plaf.ComponentUI;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;

/**
 * A provisional look and feel for OS-X, round 2, using Java2d to emulate the
 * aqua look.
 *
 * @author Tim Boudreau
 */
public class AquaEditorTabDisplayerUI extends BasicScrollingTabDisplayerUI {

    private static Map<Integer, String[]> buttonIconPaths;

    public AquaEditorTabDisplayerUI (TabDisplayer displayer) {
        super (displayer);
    }

    protected TabCellRenderer createDefaultRenderer() {
        return new AquaEditorTabCellRenderer();
    }

    public static ComponentUI createUI(JComponent c) {
        return new AquaEditorTabDisplayerUI ((TabDisplayer) c);
    }

    @Override
    protected boolean isAntialiased() {
        return true;
    }
    
    private Font txtFont;
    
    @Override
    protected Font createFont() {
        if (txtFont == null) {

            txtFont = (Font) UIManager.get("windowTitleFont");
            if (txtFont == null) {
                txtFont = new Font("Dialog", Font.PLAIN, 11);
            } else if (txtFont.isBold()) {
                // don't use deriveFont() - see #49973 for details
                txtFont = new Font(txtFont.getName(), Font.PLAIN, txtFont.getSize());
            }
        }
        return txtFont;
    }

    protected Font getTxtFont() {
        return createFont();
    }
    
    @Override
    protected int createRepaintPolicy () {
        return TabState.REPAINT_SELECTION_ON_ACTIVATION_CHANGE
                | TabState.REPAINT_ON_SELECTION_CHANGE
                | TabState.REPAINT_ALL_ON_MOUSE_ENTER_TABS_AREA
                | TabState.REPAINT_ON_MOUSE_ENTER_CLOSE_BUTTON
                | TabState.REPAINT_ON_CLOSE_BUTTON_PRESSED
                | TabState.REPAINT_ON_MOUSE_PRESSED;
    }
    
    @Override
    public Dimension getPreferredSize(JComponent c) {
        int prefHeight = 28;
        //Never call getGraphics() on the control, it resets in-process
        //painting on OS-X 1.4.1 and triggers gratuitous repaints
        Graphics g = BasicScrollingTabDisplayerUI.getOffscreenGraphics();
        if (g != null) {
            FontMetrics fm = g.getFontMetrics(displayer.getFont());
            Insets ins = getTabAreaInsets();
            prefHeight = fm.getHeight() + ins.top + ins.bottom + 7;
        }
        if (prefHeight % 2 == 0) {
            prefHeight += 1;
        }
        return new Dimension(displayer.getWidth(), prefHeight);
    }

    @Override
    protected void paintBackground(Graphics g) {
        g.setColor(UIManager.getColor("NbTabControl.editorTabBackground"));
        g.fillRect(0, 0, displayer.getWidth(), displayer.getHeight());
    }


    @Override
    protected void paintAfterTabs(Graphics g) {
        int rightLineStart = getTabsAreaWidth();
        int rightLineEnd = displayer.getWidth();

        int y = displayer.getHeight();

        if (displayer.getModel().size() > 0 && !scroll().isLastTabClipped()) {
            //Extend the line out to the edge of the last visible tab
            //if none are clipped
            int idx = scroll().getLastVisibleTab(displayer.getWidth());
            rightLineStart = scroll().getX(idx) + scroll().getW(idx);
        } else if (displayer.getModel().size() == 0) {
            rightLineStart = 6;
        }
        g.setColor(UIManager.getColor("NbTabControl.borderColor"));
        g.drawLine(rightLineStart, y-1, rightLineEnd, y-1);
    }
    

    private static void initIcons() {
        if( null == buttonIconPaths ) {
            buttonIconPaths = new HashMap<Integer, String[]>(7);
            
            //left button
            String[] iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/mac_scrollleft_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/mac_scrollleft_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/mac_scrollleft_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/mac_scrollleft_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SCROLL_LEFT_BUTTON, iconPaths );
            
            //right button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/mac_scrollright_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/mac_scrollright_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/mac_scrollright_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/mac_scrollright_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SCROLL_RIGHT_BUTTON, iconPaths );
            
            //drop down button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/mac_popup_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/mac_popup_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/mac_popup_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/mac_popup_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_DROP_DOWN_BUTTON, iconPaths );
            
            //maximize/restore button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/mac_maximize_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/mac_maximize_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/mac_maximize_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/mac_maximize_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_MAXIMIZE_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/mac_restore_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/mac_restore_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/mac_restore_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/mac_restore_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_RESTORE_BUTTON, iconPaths );
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
