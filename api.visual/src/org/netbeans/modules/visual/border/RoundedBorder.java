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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.visual.border;

import org.netbeans.api.visual.border.Border;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.visual.widget.ResourceTable;
import org.netbeans.api.visual.widget.Widget;

/**
 * @author David Kaspar
 */
public final class RoundedBorder implements Border {

    private int arcWidth;
    private int arcHeight;
    private int insetWidth;
    private int insetHeight;
    private Color fillColor;
    private Color drawColor;
    private DrawResourceTableListener drawListener = null;
    private FillResourceTableListener fillListener = null;

    public RoundedBorder (int arcWidth, int arcHeight, int insetWidth, int insetHeight, Color fillColor, Color drawColor) {
        this.arcWidth = arcWidth;
        this.arcHeight = arcHeight;
        this.insetWidth = insetWidth;
        this.insetHeight = insetHeight;
        this.fillColor = fillColor;
        this.drawColor = drawColor;
    }
    
    public RoundedBorder (int arcWidth, int arcHeight, int insetWidth, int insetHeight, 
                          String fillProperty,  String drawProperty,  
                          Widget assocaited) {
        this(arcWidth, arcHeight, insetWidth, insetHeight, 
             fillProperty, drawProperty, assocaited.getResourceTable());
    }
    
    public RoundedBorder (int arcWidth, int arcHeight, int insetWidth, int insetHeight, 
                          String fillProperty,  String drawProperty,  
                          ResourceTable table) {
        this.arcWidth = arcWidth;
        this.arcHeight = arcHeight;
        this.insetWidth = insetWidth;
        this.insetHeight = insetHeight;
        
        
        Object value = table.getProperty(fillProperty);
        if(value instanceof Color)
        {
            this.fillColor = (Color)value;
        }
        
        value = table.getProperty(drawProperty);
        if(value instanceof Color)
        {
            this.drawColor = (Color)value;
        }
        
        drawListener = new DrawResourceTableListener();
        fillListener = new FillResourceTableListener();
        table.addPropertyChangeListener(fillProperty, fillListener);
        table.addPropertyChangeListener(drawProperty, drawListener);
    }
  
    public Insets getInsets () {
        return new Insets (insetHeight, insetWidth, insetHeight, insetWidth);
    }

    public void paint (Graphics2D gr, Rectangle bounds) {
        if (fillColor != null) {
            gr.setColor (fillColor);
            gr.fill (new RoundRectangle2D.Float (bounds.x, bounds.y, bounds.width, bounds.height, arcWidth, arcHeight));
        }
        if (drawColor != null) {
            gr.setColor (drawColor);
            gr.draw (new RoundRectangle2D.Float (bounds.x + 0.5f, bounds.y + 0.5f, bounds.width - 1, bounds.height - 1, arcWidth, arcHeight));
        }
    }

    public boolean isOpaque () {
        return false;
    }
    
    public class DrawResourceTableListener implements PropertyChangeListener
    {
        public void propertyChange(PropertyChangeEvent event)
        {
            drawColor = (Color)event.getNewValue();
        }
    }
    
    public class FillResourceTableListener implements PropertyChangeListener
    {
        public void propertyChange(PropertyChangeEvent event)
        {
            fillColor = (Color)event.getNewValue();
        }
    }
    
}
