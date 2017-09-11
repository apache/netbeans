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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.visual.widget.ResourceTable;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.util.GeomUtil;
import org.netbeans.api.visual.border.Border;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author David Kaspar
 */
public final class ResizeBorder implements Border {

    private static final BasicStroke STROKE = new BasicStroke (1.0f, BasicStroke.JOIN_BEVEL, BasicStroke.CAP_BUTT, 5.0f, new float[] { 6.0f, 3.0f }, 0.0f);

    private int thickness;
    private Color color;
    private boolean outer;
    private ResourceTableListener listener = null;

    public ResizeBorder (int thickness, Color color, boolean outer) {
        this.thickness = thickness;
        this.color = color;
        this.outer = outer;
    }
    
    public ResizeBorder (int thickness, String property, Widget attachedWidget, boolean outer) {
        this(thickness, property, attachedWidget.getResourceTable(), outer);
    }

    public ResizeBorder (int thickness, String property, ResourceTable table, boolean outer)
    {
        this.thickness = thickness;
        this.outer = outer;
        
        Object value = table.getProperty(property);
        if(value instanceof Color)
        {
            this.color = (Color)value;
        }
        
        listener = new ResourceTableListener();
        table.addPropertyChangeListener(property, listener);
    }
    
    public Insets getInsets () {
        return new Insets (thickness, thickness, thickness, thickness);
    }

    public boolean isOuter () {
        return outer;
    }

    public void paint (Graphics2D gr, Rectangle bounds) {
        gr.setColor (color);

        Stroke stroke = gr.getStroke ();
        gr.setStroke (STROKE);
        if (outer)
            gr.draw (new Rectangle2D.Double (bounds.x + 0.5, bounds.y + 0.5, bounds.width - 1.0, bounds.height - 1.0));
        else
            gr.draw (new Rectangle2D.Double (bounds.x + thickness + 0.5, bounds.y + thickness + 0.5, bounds.width - thickness - thickness - 1.0, bounds.height - thickness - thickness - 1.0));
        gr.setStroke (stroke);

        gr.fillRect (bounds.x, bounds.y, thickness, thickness);
        gr.fillRect (bounds.x + bounds.width - thickness, bounds.y, thickness, thickness);
        gr.fillRect (bounds.x, bounds.y + bounds.height - thickness, thickness, thickness);
        gr.fillRect (bounds.x + bounds.width - thickness, bounds.y + bounds.height - thickness, thickness, thickness);

        Point center = GeomUtil.center (bounds);
        if (bounds.width >= thickness * 5) {
            gr.fillRect (center.x - thickness / 2, bounds.y, thickness, thickness);
            gr.fillRect (center.x - thickness / 2, bounds.y + bounds.height - thickness, thickness, thickness);
        }
        if (bounds.height >= thickness * 5) {
            gr.fillRect (bounds.x, center.y - thickness / 2, thickness, thickness);
            gr.fillRect (bounds.x + bounds.width - thickness, center.y - thickness / 2, thickness, thickness);
        }
    }

    public boolean isOpaque () {
        return outer;
    }
    
    public class ResourceTableListener implements PropertyChangeListener
    {
        public void propertyChange(PropertyChangeEvent event)
        {
            color = (Color)event.getNewValue();
        }
    }
    
}
