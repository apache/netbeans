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
/*
 * MetalEditorTabDisplayerUI.java
 *
 * Created on December 2, 2003, 9:40 PM
 */

package org.netbeans.swing.tabcontrol.plaf;

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
 * Tab displayer UI for Metal look and feel
 *
 * @author Tim Boudreau
 */
public final class MetalEditorTabDisplayerUI extends BasicScrollingTabDisplayerUI {
    
    private Rectangle scratch = new Rectangle();
    private static Map<Integer, String[]> buttonIconPaths;

    /**
     * Creates a new instance of MetalEditorTabDisplayerUI
     */
    public MetalEditorTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
    }

    protected TabCellRenderer createDefaultRenderer() {
        return new MetalEditorTabCellRenderer();
    }

    public static ComponentUI createUI(JComponent c) {
        return new MetalEditorTabDisplayerUI((TabDisplayer) c);
    }

    public Dimension getMinimumSize(JComponent c) {
        return new Dimension (80, 28);
    }
    
    public Dimension getPreferredSize(JComponent c) {
        int prefHeight = 28;
        Graphics g = BasicScrollingTabDisplayerUI.getOffscreenGraphics();
        if (g != null) {
            FontMetrics fm = g.getFontMetrics(displayer.getFont());
            Insets ins = getTabAreaInsets();
            prefHeight = fm.getHeight() + ins.top + ins.bottom + 9;
        }
        return new Dimension(displayer.getWidth(), prefHeight);
    }

    protected int createRepaintPolicy () {
        return TabState.REPAINT_ALL_TABS_ON_ACTIVATION_CHANGE
                | TabState.REPAINT_ALL_TABS_ON_SELECTION_CHANGE
                | TabState.REPAINT_ON_MOUSE_PRESSED
                | TabState.REPAINT_ON_CLOSE_BUTTON_PRESSED
                | TabState.REPAINT_ON_MOUSE_ENTER_CLOSE_BUTTON;
    }


    public Insets getTabAreaInsets() {
        Insets results = super.getTabAreaInsets();
        results.bottom += 4;
        results.right += 3;
        return results;
    }

    public void install() {
        super.install();
        displayer.setBackground(UIManager.getColor("control")); //NOI18N
    }

    protected void paintAfterTabs(Graphics g) {
        Rectangle r = new Rectangle();
        getTabsVisibleArea(r);
        r.width = displayer.getWidth();
        g.setColor(displayer.isActive() ?
                   defaultRenderer.getSelectedActivatedBackground() :
                   defaultRenderer.getSelectedBackground());

        Insets ins = getTabAreaInsets();
        g.fillRect(r.x, r.y + r.height, r.x + r.width,
                   displayer.getHeight() - (r.y + r.height));

        g.setColor(UIManager.getColor("controlHighlight")); //NOI18N

        int selEnd = 0;
        int i = selectionModel.getSelectedIndex();
        if (i != -1) {
            getTabRect(i, scratch);
            if (scratch.width != 0) {
                if (r.x < scratch.x) {
                    g.drawLine(r.x, displayer.getHeight() - ins.bottom,
                               scratch.x - 1,
                               displayer.getHeight() - ins.bottom);
                }
                if (scratch.x + scratch.width < r.x + r.width) {
                    selEnd = scratch.x + scratch.width;
                    g.drawLine(selEnd, displayer.getHeight() - ins.bottom,
                               r.x + r.width,
                               displayer.getHeight() - ins.bottom);
                }
            } else {
                //The selected tab is scrolled offscreen
                g.drawLine (0, displayer.getHeight() - ins.bottom,
                        displayer.getWidth(), displayer.getHeight() - ins.bottom);
            }
            g.setColor(UIManager.getColor("controlDkShadow")); //NOI18N
            g.drawLine(selEnd, displayer.getHeight() - 5, displayer.getWidth(),
                       displayer.getHeight() - 5);
            return;
        }

        g.drawLine(r.x, displayer.getHeight() - ins.bottom, r.x + r.width,
                   displayer.getHeight() - ins.bottom);

        g.setColor(UIManager.getColor("controlDkShadow")); //NOI18N
        g.drawLine(0, displayer.getHeight() - 5, displayer.getWidth(),
                   displayer.getHeight() - 5);
    }

    private static void initIcons() {
        if( null == buttonIconPaths ) {
            buttonIconPaths = new HashMap<Integer, String[]>(7);
            
            //left button
            String[] iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/metal_scrollleft_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/metal_scrollleft_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/metal_scrollleft_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/metal_scrollleft_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SCROLL_LEFT_BUTTON, iconPaths );
            
            //right button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/metal_scrollright_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/metal_scrollright_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/metal_scrollright_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/metal_scrollright_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SCROLL_RIGHT_BUTTON, iconPaths );
            
            //drop down button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/metal_popup_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/metal_popup_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/metal_popup_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/metal_popup_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_DROP_DOWN_BUTTON, iconPaths );
            
            //maximize/restore button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/metal_maximize_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/metal_maximize_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/metal_maximize_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/metal_maximize_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_MAXIMIZE_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/metal_restore_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/metal_restore_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/metal_restore_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/metal_restore_pressed.png"; // NOI18N
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
        return new Rectangle( parent.getWidth()-c.getWidth()-3, 3, c.getWidth(), c.getHeight() );
    }
}
