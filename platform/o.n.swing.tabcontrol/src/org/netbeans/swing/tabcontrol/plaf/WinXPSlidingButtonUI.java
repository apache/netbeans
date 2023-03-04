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

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.plaf.ComponentUI;
import org.netbeans.swing.tabcontrol.SlidingButton;
import org.netbeans.swing.tabcontrol.SlidingButtonUI;

/** 
 *
 * @see SlidingButtonUI
 *
 * @author  Milos Kleint
 */
public class WinXPSlidingButtonUI extends WindowsSlidingButtonUI {

    //XXX 
    private static final SlidingButtonUI INSTANCE = new WinXPSlidingButtonUI();
    
    // Has the shared instance defaults been initialized?
    private boolean defaults_initialized = false;   
    protected JToggleButton hiddenToggle;
    
    
    WinXPSlidingButtonUI() {
    }
    
    public static ComponentUI createUI(JComponent c) {
        return INSTANCE;
    }    

    /** Install a border on the button */
    @Override
    protected void installBorder (AbstractButton b) {
        // XXX
    }
    
    @Override
    public void installDefaults (AbstractButton b) {
        super.installDefaults(b);
	if(!defaults_initialized) {
            hiddenToggle = new JToggleButton();
            hiddenToggle.setText("");
            JToolBar bar = new JToolBar();
            bar.add(hiddenToggle);
	    defaults_initialized = true;
	}
    }
    
    @Override
    protected void uninstallDefaults(AbstractButton b) {
	super.uninstallDefaults(b);
	defaults_initialized = false;
    }   
    
    @Override
    public void paint(Graphics g, JComponent c) {
        ColorUtil.setupAntialiasing(g);
        AbstractButton button = (AbstractButton)c;
        hiddenToggle.setBorderPainted(button.isBorderPainted());
        hiddenToggle.setRolloverEnabled(button.isRolloverEnabled());
        hiddenToggle.setFocusable(button.isFocusable());
        hiddenToggle.setFocusPainted(button.isFocusPainted());
        hiddenToggle.setMargin(button.getMargin());
        hiddenToggle.setBorder(button.getBorder());
        hiddenToggle.getModel().setRollover(button.getModel().isRollover());
        hiddenToggle.getModel().setPressed(button.getModel().isPressed());
        hiddenToggle.getModel().setArmed(button.getModel().isArmed());
        hiddenToggle.getModel().setSelected(button.getModel().isSelected());
        
        hiddenToggle.setBounds(button.getBounds());
        super.paint(g, c);
    }
    
    @Override
    protected void paintBackground(Graphics2D g, AbstractButton b) {
        if (((SlidingButton) b).isBlinkState()) {
            g.setColor(WinClassicEditorTabCellRenderer.ATTENTION_COLOR);
            g.fillRect (0, 0, b.getWidth(), b.getHeight());
            hiddenToggle.setFont(b.getFont());
        } else {
            hiddenToggle.paint(g);
        }
    }
    
    @Override
    protected void paintButtonPressed(Graphics g, AbstractButton b) {    
        hiddenToggle.paint(g);
    }
}
