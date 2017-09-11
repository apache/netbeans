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

import java.awt.*;
import java.awt.geom.AffineTransform;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import javax.swing.text.View;

/** Button UI for SlidingButton component that can paint rotated text.
 * To change appearance, provide a subclass of this class or subclass of ComponentUI
 * and register it via UIDefaults in standard Swing way. 
 * Typically the methods of interest when
 * subclassing are <code>paintBackground</code> and <code>paintIconAndText</code>.
 * <p>
 * Instances of this class should be stateless, taking all data from model,
 * such that a single instance can manage any number of buttons.
 *
 * @see SlidingButton
 *
 * @author  Tim Boudreau, Dafe Simonek
 */
public class SlidingButtonUI extends BasicToggleButtonUI {

    //TODO - just a temporary solution to have some default impl..
    private static final BasicToggleButtonUI INSTANCE = new SlidingButtonUI();
    
    /** Private, no need for outer classes to instantiate */
    protected SlidingButtonUI() {
    }
    
    public static ComponentUI createUI(JComponent c) {
        return INSTANCE;
    }  
    
    @Override
    protected String getPropertyPrefix() {
        //TODO -define own prefix?
        return "ToggleButton.";
    }

    @Override
    public void paint(Graphics g, JComponent c) {
       AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();
        Dimension size = b.getSize();
        Font f = c.getFont();
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        Insets i = c.getInsets();
        Rectangle viewRect = new Rectangle(size);
        Rectangle iconRect = new Rectangle();
        Rectangle textRect = new Rectangle();
        
        Rectangle rotatedViewRect;
        Rectangle rotatedIconRect = new Rectangle();
        Rectangle rotatedTextRect = new Rectangle();
        SlidingButton slide = (SlidingButton)b;
        Graphics2D g2d = (Graphics2D)g;
        int orientation = slide.getOrientation();
        if (orientation == SlideBarDataModel.SOUTH || orientation == SlideBarDataModel.NORTH) {
            rotatedViewRect = new Rectangle(0, 0, viewRect.width, viewRect.height);
        } else {
            rotatedViewRect = new Rectangle(0, 0, viewRect.height, viewRect.width);
        }
        
        // layout the text and icon
        String text = SwingUtilities.layoutCompoundLabel(
            c, fm, b.getText(), b.getIcon(),
            b.getVerticalAlignment(), b.getHorizontalAlignment(),
            b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
            rotatedViewRect, rotatedIconRect, rotatedTextRect,
	    b.getText() == null ? 0 : b.getIconTextGap());
        
        if (orientation == SlideBarDataModel.SOUTH || orientation == SlideBarDataModel.NORTH) {
            iconRect = new Rectangle(viewRect.x + rotatedIconRect.x, viewRect.y + rotatedIconRect.y,
                                     rotatedIconRect.width, rotatedIconRect.height);
            textRect = new Rectangle(viewRect.x + rotatedTextRect.x, viewRect.y + rotatedTextRect.y,    
                                     rotatedTextRect.width, rotatedTextRect.height);
        }
        if (orientation == SlideBarDataModel.WEST) {
            iconRect = new Rectangle(viewRect.x + rotatedIconRect.y,
                                     viewRect.y + viewRect.height - rotatedIconRect.x - rotatedIconRect.width,
                                     rotatedIconRect.height, 
                                     rotatedIconRect.width);
            textRect = new Rectangle(viewRect.x + rotatedTextRect.y,
                                     viewRect.y + viewRect.height - rotatedTextRect.y - rotatedTextRect.width,
                                     rotatedTextRect.height, 
                                     rotatedTextRect.width);
        }
        if (orientation == SlideBarDataModel.EAST) {
            iconRect = new Rectangle(viewRect.x + viewRect.width - rotatedIconRect.y - rotatedIconRect.height,
                                     viewRect.y + rotatedIconRect.x,
                                     rotatedIconRect.height, 
                                     rotatedIconRect.width);
            textRect = new Rectangle(viewRect.x + viewRect.width - rotatedTextRect.y - rotatedTextRect.height,
                                     viewRect.y + rotatedTextRect.x,
                                     rotatedTextRect.height, 
                                     rotatedTextRect.width);
        }

        g.setColor(b.getBackground());

        if (model.isArmed() && model.isPressed() || model.isSelected() || model.isRollover()) {
            paintButtonPressed(g,b);
        } else if (b.isOpaque()) {
            paintBackground(g2d, b);
        }
	
        
        
        // Paint the Icon
        if(b.getIcon() != null) { 
            paintIcon(g, b, iconRect);
        }
	
        // Draw the Text
        if(text != null && !text.equals("")) {
            
            
            AffineTransform saveTr = g2d.getTransform();
            if (orientation == SlideBarDataModel.WEST) {
                // rotate 90 degrees counterclockwise for WEST orientation
                g2d.rotate( -Math.PI / 2 );
                g2d.translate(-c.getHeight(), 0 );
            } else if( orientation == SlideBarDataModel.EAST ) {
                // rotate 90 degrees clockwise for EAST orientation
                g2d.rotate( Math.PI / 2 );
                g2d.translate( 0, - c.getWidth() );
            }
            
            
            View v = (View) c.getClientProperty(BasicHTML.propertyKey);
            if (v != null) {
                v.paint(g, rotatedTextRect);
            } else {
                paintText(g, b, rotatedTextRect, text);
            }
            
            
            // restore transformation
            g2d.setTransform(saveTr);
        }
        
        // draw the dashed focus line.
        if (b.isFocusPainted() && b.hasFocus()) {
	    paintFocus(g, b, viewRect, textRect, iconRect);
        }        
 
    }  
    
    protected void paintBackground(Graphics2D g, AbstractButton b) {
        Dimension size = b.getSize();
        
        Insets insets = b.getInsets();
        Insets margin = b.getMargin();
        if( null == insets || null == margin )
            return;
        g.fillRect(insets.left - margin.left,
            insets.top - margin.top,
        size.width - (insets.left-margin.left) - (insets.right - margin.right),
        size.height - (insets.top-margin.top) - (insets.bottom - margin.bottom));
    }

    
    
    @Override
    public Dimension getMinimumSize(JComponent c) {
        return getPreferredSize(c);
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        SlidingButton b = (SlidingButton) c;
        Dimension prefSize = super.getPreferredSize(c);
        int orientation = b.getOrientation();
        
        if (orientation == SlideBarDataModel.SOUTH || orientation == SlideBarDataModel.NORTH) {
            return prefSize;
        }
        else {
            return new Dimension(prefSize.height, prefSize.width);
        }
    }

    @Override
    public Dimension getMaximumSize(JComponent c) {
        return getPreferredSize(c);
    }
}
