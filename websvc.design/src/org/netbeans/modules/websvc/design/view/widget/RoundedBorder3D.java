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
package org.netbeans.modules.websvc.design.view.widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;
import org.netbeans.api.visual.border.Border;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import org.netbeans.api.visual.widget.Widget;

/**
 * @author David Kaspar
 */
public class RoundedBorder3D implements Border {
    
    private static final Color SHADOW_COLOR = new Color(208,208,208);
    private static final Color SELECTED_BORDER_COLOR = new Color(255,153,0);
   
    private Widget widget;
    private int radius;
    private int insetWidth;
    private int insetHeight;
    private Color drawColor;
    private int depth = 3;
    
    /**
     *
     * @param radius
     * @param depth
     * @param insetWidth
     * @param insetHeight
     * @param drawColor
     */
    public RoundedBorder3D(Widget widget, int radius, int depth, int insetWidth, int insetHeight, Color drawColor) {
        this.widget = widget;
        this.radius = radius;
        this.depth = depth;
        this.insetWidth = insetWidth;
        this.insetHeight = insetHeight;
        this.drawColor = drawColor;
    }
    
    public Insets getInsets() {
        return new Insets(insetHeight, insetWidth, insetHeight+depth, insetWidth+depth);
    }
    
    public void paint(Graphics2D gr, Rectangle bounds) {
        Paint oldPaint = gr.getPaint();
        RoundRectangle2D rect = new RoundRectangle2D.Double(bounds.x+0.5f,
                bounds.y + 0.5f, bounds.width - depth - 1, 
                bounds.height - depth - 1, radius, radius);
        if (drawColor != null) {
            RoundRectangle2D outerRect = new RoundRectangle2D.Double(
                    bounds.x + depth + 0.5f, bounds.y + depth + 0.5f,
                    bounds.width - depth - 1, bounds.height - depth - 1, radius, radius);
            Area raisedArea = new Area(outerRect);
            raisedArea.subtract(new Area(rect));
            gr.setPaint(SHADOW_COLOR);
            gr.fill(raisedArea);
            gr.setPaint(widget.getState().isSelected()?SELECTED_BORDER_COLOR:drawColor);
            Stroke s = gr.getStroke ();
            if(widget.getState().isFocused())
                gr.setStroke (new BasicStroke(1, BasicStroke.CAP_BUTT, 
                        BasicStroke.JOIN_ROUND, BasicStroke.JOIN_MITER, new float[] {2,2}, 0));
            gr.draw(rect);
            gr.setStroke (s);
        }
        gr.setPaint(oldPaint);
    }
    
    public boolean isOpaque() {
        return true;
    }

}
