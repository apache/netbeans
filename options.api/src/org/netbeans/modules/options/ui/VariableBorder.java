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
