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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import org.netbeans.swing.tabcontrol.TabDisplayer;

/**
 * Windows8-like user interface of view type tabs.
 *
 * @author S. Aubrecht
 * @since 1.41
 */
public final class Windows8ViewTabDisplayerUI extends AbstractWinViewTabDisplayerUI {

    /**
     * True when colors were already initialized, false otherwise
     */
    private static boolean colorsReady = false;

    private static Color 
            unselFillUpperC,
            unselFillLowerC,
            selFillC,
            focusFillUpperC,
            focusFillLowerC, 
            mouseOverFillUpperC,
            mouseOverFillLowerC,
            attentionFillUpperC,
            attentionFillLowerC;

    private static Map<Integer, String[]> buttonIconPaths;
    /**
     * Should be constructed only from createUI method.
     */
    private Windows8ViewTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
    }

    public static ComponentUI createUI(JComponent c) {
        return new Windows8ViewTabDisplayerUI((TabDisplayer)c);
    }
     
    @Override
    public void installUI (JComponent c) {
        super.installUI(c);
        initColors();
    }

    @Override
    protected void paintTabBackground(Graphics g, int index, int x, int y,
                                      int width, int height) {
        // shrink rectangle - don't affect border and tab header
        y += 2;
        height -= 2;
        // background body, colored according to state
        boolean selected = isSelected(index);
        boolean focused = selected && isActive();
        boolean attention = isAttention(index);
        boolean mouseOver = isMouseOver(index);
        paintTabBackground( (Graphics2D)g, x, y, width, height, selected, focused, attention, mouseOver );
    }

    int getButtonYPadding() {
        return 1;
    }

    static void paintTabBackground( Graphics2D g2d, int x, int y, int width, int height, boolean selected, boolean focused, boolean attention, boolean mouseOver ) {
        initColors();
        if (focused && !attention) {
            g2d.setPaint( ColorUtil.getGradientPaint( x, y, focusFillUpperC, x, y+height, focusFillLowerC) );
        } else if (selected && !attention) {
            g2d.setColor(selFillC);
        } else if (mouseOver && !attention) {
            g2d.setPaint( ColorUtil.getGradientPaint( x, y, mouseOverFillUpperC, x, y+height, mouseOverFillLowerC) );
        } else if (attention) {
            g2d.setPaint( ColorUtil.getGradientPaint( x, y, attentionFillUpperC, x, y+height, attentionFillLowerC) );
        } else {
            g2d.setPaint( ColorUtil.getGradientPaint( x, y, unselFillUpperC, x, y+height, unselFillLowerC) );
        }
        g2d.fillRect(x, y, width, height);
    }

    /**
     * Initialization of colors
     */
    private static void initColors() {
        if (!colorsReady) {
            selFillC = UIManager.getColor("tab_sel_fill"); // NOI18N
            focusFillUpperC = UIManager.getColor("tab_focus_fill_upper"); // NOI18N
            focusFillLowerC = UIManager.getColor("tab_focus_fill_lower"); // NOI18N
            unselFillUpperC = UIManager.getColor("tab_unsel_fill_upper"); // NOI18N
            unselFillLowerC = UIManager.getColor("tab_unsel_fill_lower"); // NOI18N
            mouseOverFillUpperC = UIManager.getColor("tab_mouse_over_fill_upper"); // NOI18N
            mouseOverFillLowerC = UIManager.getColor("tab_mouse_over_fill_lower"); // NOI18N
            attentionFillUpperC = UIManager.getColor("tab_attention_fill_upper"); // NOI18N
            attentionFillLowerC = UIManager.getColor("tab_attention_fill_lower"); // NOI18N
            
            colorsReady = true;
        }
    }

    private static void initIcons() {
        if( null == buttonIconPaths ) {
            buttonIconPaths = new HashMap<Integer, String[]>(7);

            //close button
            String[] iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/openide/awt/resources/win8_bigclose_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/openide/awt/resources/win8_bigclose_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/openide/awt/resources/win8_bigclose_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_CLOSE_BUTTON, iconPaths );

            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/win8_pin_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/win8_pin_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/win8_pin_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_PIN_BUTTON, iconPaths );

            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/win8_restore_group_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/win8_restore_group_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/win8_restore_group_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_RESTORE_GROUP_BUTTON, iconPaths );

            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/win8_minimize_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/win8_minimize_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/win8_minimize_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_GROUP_BUTTON, iconPaths );
        }
    }

    @Override
    public Icon getButtonIcon( int buttonId, int buttonState ) {
        Icon res = null;
        initIcons();
        String[] paths = buttonIconPaths.get( buttonId );
        if( null != paths && buttonState >=0 && buttonState < paths.length ) {
            res = TabControlButtonFactory.getIcon( paths[buttonState] );
        }
        if( null == res )
            return super.getButtonIcon( buttonId, buttonState );
        return res;
    }

}
