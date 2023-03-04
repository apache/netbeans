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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import org.netbeans.swing.tabcontrol.SlidingButton;
import org.netbeans.swing.tabcontrol.SlidingButtonUI;

/** 
 *
 * @see SlidingButtonUI
 *
 * @author  Milos Kleint
 */
public class WindowsSlidingButtonUI extends SlidingButtonUI {

    //XXX 
    private static final SlidingButtonUI INSTANCE = new WindowsSlidingButtonUI();
    
    // Has the shared instance defaults been initialized?
    private boolean defaults_initialized = false;   
    protected Color focusColor;
    protected static int dashedRectGapX;
    protected static int dashedRectGapY;
    protected static int dashedRectGapWidth;
    protected static int dashedRectGapHeight;
    
    
    /** Private, no need for outer classes to instantiate */
    protected WindowsSlidingButtonUI() {
    }
    
    public static ComponentUI createUI(JComponent c) {
        return INSTANCE;
    }    

    /** Install a border on the button */
    protected void installBorder (AbstractButton b) {
        // XXX
    }
    
    @Override
    public void installDefaults (AbstractButton b) {
        super.installDefaults(b);
	if(!defaults_initialized) {
            try {
                //Null checks so this can be tested on other platforms
                Integer in = ((Integer)UIManager.get("Button.dashedRectGapX"));
                dashedRectGapX = in == null ? 3 : in.intValue();
                in = ((Integer)UIManager.get("Button.dashedRectGapY"));
                dashedRectGapY = in == null ? 3 : in.intValue();
                in = ((Integer)UIManager.get("Button.dashedRectGapWidth"));
                dashedRectGapWidth = in == null ? 3 : in.intValue();
                in = ((Integer)UIManager.get("Button.dashedRectGapHeight"));
                dashedRectGapHeight = in == null ? 3 : in.intValue();
                focusColor = UIManager.getColor(getPropertyPrefix() + "focus");
                defaults_initialized = true;
            } catch (NullPointerException npe) {
                //We're testing on a non windows platform, the defaults don't
                //exist
                dashedRectGapX = 2;
                dashedRectGapY = 2;
                dashedRectGapWidth = 2;
                dashedRectGapHeight = 2;
            }
	}
    }
    
    @Override
    protected void uninstallDefaults(AbstractButton b) {
	super.uninstallDefaults(b);
	defaults_initialized = false;
    }    
    
    @Override
    protected void paintBackground(Graphics2D g, AbstractButton b) {
        if (((SlidingButton) b).isBlinkState()) {
            g.setColor(WinClassicEditorTabCellRenderer.ATTENTION_COLOR);
            g.fillRect (0, 0, b.getWidth(), b.getHeight());
        } else {
            super.paintBackground(g, b);
        }
    }    
    
    @Override
    protected void paintButtonPressed(Graphics g, AbstractButton b) {
        // This is a special case in which the toggle button in the
        // Rollover JToolBar will render the button in a pressed state
        Color oldColor = g.getColor();
        
        if (((SlidingButton) b).isBlinkState()) {
            g.setColor(WinClassicEditorTabCellRenderer.ATTENTION_COLOR);
            g.fillRect (0, 0, b.getWidth(), b.getHeight());
        }
        
        int w = b.getWidth();
        int h = b.getHeight();
        UIDefaults table = UIManager.getLookAndFeelDefaults();
        if (b.getModel().isRollover() && (! b.getModel().isPressed() && ! b.getModel().isSelected())) {
            g.setColor(table.getColor("ToggleButton.highlight"));
            g.drawRect(0, 0, w-1, h-1);
            g.drawRect(0, 0, 0, h-1);
            
            Color shade = table.getColor("ToggleButton.shadow");
            Component p = b.getParent();
            if (p != null && p.getBackground().equals(shade)) {
                shade = table.getColor("ToggleButton.darkShadow");
            }
            g.setColor(shade);
            g.drawLine(w-1, 0, w-1, h-1);
            g.drawLine(0, h-1, w-1, h-1);
        } else {
            Color shade = table.getColor("ToggleButton.shadow");
            Component p = b.getParent();
            if (p != null && p.getBackground().equals(shade)) {
                shade = table.getColor("ToggleButton.darkShadow");
            }
            g.setColor(shade);
            g.drawRect(0, 0, w-1, h-1);
            g.setColor(table.getColor("ToggleButton.highlight"));
            g.drawLine(w-1, 0, w-1, h-1);
            g.drawLine(0, h-1, w-1, h-1);
        }
        g.setColor(oldColor);
    }
    
    @Override
    protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect, Rectangle iconRect){

	int width = b.getWidth();
	int height = b.getHeight();
	g.setColor(getFocusColor());
	BasicGraphicsUtils.drawDashedRect(g, dashedRectGapX, dashedRectGapY,
					  width - dashedRectGapWidth, height - dashedRectGapHeight);
    }
    

    protected Color getFocusColor() {
	return focusColor;
    }
    
    
    
    
    // ********************************
    //          Layout Methods
    // ********************************
    @Override
    public Dimension getPreferredSize(JComponent c) {
	Dimension d = super.getPreferredSize(c);

	/* Ensure that the width and height of the button is odd,
	 * to allow for the focus line if focus is painted
	 */
        AbstractButton b = (AbstractButton)c;
	if (b.isFocusPainted()) {
	    if(d.width % 2 == 0) { d.width += 1; }
	    if(d.height % 2 == 0) { d.height += 1; }
	}
	return d;
    }
    
    
}
