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

package org.netbeans.modules.options.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.geom.Line2D;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.border.Border;

/**
 * VariableBorder is a simple Border that only draw a line if a color is given.
 *
 * @author Christopher Atlan
 */
public class VariableBorder implements Border {
    private Color topColor;
    private Color leftColor;
    private Color bottomColor;
    private Color rightColor;
    
    /** Creates a new instance of VariableBorder */
    public VariableBorder(final Color topColor,
            final Color leftColor,
            final Color bottomColor,
            final Color rightColor) {
        this.topColor = topColor;
        this.leftColor = leftColor;
        this.bottomColor = bottomColor;
        this.rightColor = rightColor;
    }
    
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D)g;
        Shape s;
        
        if(topColor != null) {
            s = new Line2D.Double(x,y,x+width, y);
            g2d.setColor(topColor);
            g2d.fill(s);
        }
        
        if(leftColor != null) {
            s = new Line2D.Double(x,y,x, y+height);
            g2d.setColor(leftColor);
            g2d.fill(s);
        }
        
        if(bottomColor != null) {
            s = new Line2D.Double(x,y+height-1,x+width, y+height-1);
            g2d.setColor(bottomColor);
            g2d.fill(s);
        }
        
        if(rightColor != null) {
            s = new Line2D.Double(x+width-1,y,x+width-1, y+height);
            g2d.setColor(rightColor);
            g2d.fill(s);
        }
        

    }
    
    public Insets getBorderInsets(Component c) {
        Insets i = new Insets(0, 0, 0, 0);
        
        if(topColor != null)
            i.top = 1;
        
        if(leftColor != null)
            i.left = 1;
        
        if(bottomColor != null)
            i.bottom = 1;
        
        if(rightColor != null)
            i.right = 1;
        
        if (c instanceof JToolBar) {
            Insets toolBarInsets = ((JToolBar)c).getMargin();
            i.top += toolBarInsets.top;
            i.left += toolBarInsets.left;
            i.right += toolBarInsets.right;
            i.bottom += toolBarInsets.bottom;
        }
        
        if (c instanceof JToggleButton) {
            Insets buttonInsets = ((JToggleButton)c).getMargin();
            i.top += buttonInsets.top;
            i.left += buttonInsets.left;
            i.right += buttonInsets.right;
            i.bottom += buttonInsets.bottom;
        }
        
        return i;
    }
    
    public boolean isBorderOpaque() {
        return false;
    }
}
