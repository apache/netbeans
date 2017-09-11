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
package org.netbeans.swing.tabcontrol;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.ButtonUI;

/**
 * JToggleButton subclass which maps to an index in the data model, and displays
 * whatever the content of the data model at that index is.  Buttons are added or removed
 * from the tab displayer as the model changes.  This class is public to allow
 * alternate UIs for the buttons to be provided via subclasses of <code>SlidingButtonUI</code>.
 *
 * @author Dafe Simonek, Tim Boudreau
 */
public final class SlidingButton extends JToggleButton {
    /** UI Class ID for IndexButtons, to be used by providers of UI delegates */
    public static final String UI_CLASS_ID = "SlidingButtonUI";

    private static final boolean isAqua = "Aqua".equals(UIManager.getLookAndFeel().getID()); //NOI18N
    
//    /**** XXX temporary - should go into LFDefaults table *********/
//    static {
//        UIManager.getDefaults().put(UI_CLASS_ID, "org.netbeans.core.windows.view.ui.slides.MetalSlidingButtonUI");
//    }
    
    /** orientation of this button */
    private int orientation;
    /** Ascoiated tab data */
    private TabData data;
        
     
    /** Create a new button representing TabData from the model.
     *
     * @param buttonData Tab data as text, icon, tooltip etc.
     * @param orientation horizontal/vertical orientation of the button
     */
    public SlidingButton(TabData buttonData, int orientation) {
        super(buttonData.getText(), buttonData.getIcon(), false);

        // grab the text from client property 
        // XXX please rewrite when API is invented - see issue #55955
        Component comp = buttonData.getComponent();
        if (comp instanceof JComponent) {
            Object slidingName = ((JComponent)comp).getClientProperty("SlidingName");
            if (slidingName instanceof String) {
                setText((String)slidingName);
            }
        }
        
        this.orientation = orientation;
        data = buttonData;
        setFocusable(false);
        setRolloverEnabled(true);
        setIconTextGap(4);
        setVerticalAlignment(SwingConstants.CENTER);
        setHorizontalAlignment(SwingConstants.CENTER);
        if ("Nimbus".equals(UIManager.getLookAndFeel().getID())) {
            Insets insets = UIManager.getInsets("Button.contentMargins");
            if (insets != null) {
                setBorder(BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.right));
            } else {
                setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
            }
        } else if( isAqua ) {
            setBorder(BorderFactory.createEmptyBorder(4,10,4,10));
            putClientProperty("JComponent.sizeVariant", "small");
            setOpaque(false);
        } else {
            setMargin(new Insets(0,3,0,3));
        }
        setBorderPainted(false);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        //XXX register with tooltip manager
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        setBlinking(false);
        //XXX register with tooltip manager
    }
    
    @Override
    public String getToolTipText() {
        return data.getTooltip();
    }

    /************** Swing standard technique for attaching UI class *********/
    
    @Override
    public void updateUI () {
        ButtonUI ui = (ButtonUI)UIManager.getUI(this);
        if (ui == null) {
            // create our default UI class if not found in UIManager
            ui = (ButtonUI)SlidingButtonUI.createUI(this);
        }
        setUI (ui);
    }

    @Override
    public String getUIClassID() {
        return UI_CLASS_ID;
    }
    
    /************ Data accessing methods ***********/

    /** Returns orinetation of this button */
    public int getOrientation() {
        return orientation;
    }
    
    public boolean isBlinking() {
        return blinkState;
    }
    
    private Timer blinkTimer = null;
    private boolean blinkState = false;
    public void setBlinking (boolean val) {
        if (!val && blinkTimer != null) {
            blinkTimer.stop();
            blinkTimer = null;
            boolean wasBlinkState = blinkState;
            blinkState = false;
            if (wasBlinkState) {
                repaint();
            }
        } else if (val && blinkTimer == null) {
            blinkTimer = new Timer(700, new BlinkListener());
            blinkState = true;
            blinkTimer.start();
            repaint();
        }
    }

    private class BlinkListener implements ActionListener {
        public void actionPerformed (ActionEvent ae) {
            blinkState = !blinkState;
            repaint();
        }
    }
    
    /**
     * Used by the UI to determine whether to use the blink
     * color or the regular color
     */
    public final boolean isBlinkState() {
        return blinkState;
    }
    
    @Override
    public final Color getBackground() {
        return isBlinkState() ? 
            new Color(252, 250, 244) : super.getBackground();
    }
    
    public TabData getData() {
        return data;
    }
    
}
