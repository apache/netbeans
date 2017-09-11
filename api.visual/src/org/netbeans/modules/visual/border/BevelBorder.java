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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.visual.widget.ResourceTable;
import org.netbeans.api.visual.widget.Widget;
import java.awt.*;

/**
 * @author David Kaspar
 */
public final class BevelBorder implements Border {

    private boolean raised;
    private Color color;
    private ResourceTableListener listener = null;

    public BevelBorder (boolean raised, Color color) {
        this.raised = raised;
        this.color = color;
    }
    
    public BevelBorder(boolean raised, String property, Widget associated)
    {
        this(raised, property, associated.getResourceTable());
    }
    
    public BevelBorder (boolean raised, String property, ResourceTable table) {
        this.raised = raised;
        
        Object value = table.getProperty(property);
        if(value instanceof Color)
        {
            this.color = (Color)value;
        }
        
        listener = new ResourceTableListener();
        table.addPropertyChangeListener(property, listener);
    }
    
    public Insets getInsets () {
        return new Insets (2, 2, 2, 2);
    }

    public void paint (Graphics2D gr, Rectangle bounds) {
        gr.setColor (color);
        int h = bounds.height;
        int w = bounds.width;

        gr.translate (bounds.x, bounds.y);

        gr.setColor (raised ? color.brighter ().brighter () : color.darker ().darker ());
        gr.drawLine (0, 0, 0, h - 2);
        gr.drawLine (1, 0, w - 2, 0);

        gr.setColor (raised ? color.brighter () : color.darker ());
        gr.drawLine (1, 1, 1, h - 3);
        gr.drawLine (2, 1, w - 3, 1);

        gr.setColor (raised ? color.darker ().darker () : color.brighter ().brighter ());
        gr.drawLine (0, h - 1, w - 1, h - 1);
        gr.drawLine (w - 1, 0, w - 1, h - 2);

        gr.setColor (raised ? color.darker () : color.brighter ());
        gr.drawLine (1, h - 2, w - 2, h - 2);
        gr.drawLine (w - 2, 1, w - 2, h - 3);

        gr.translate (- bounds.x, - bounds.y);
    }

    public boolean isOpaque () {
        return true;
    }
    
    public class ResourceTableListener implements PropertyChangeListener
    {
        public void propertyChange(PropertyChangeEvent event)
        {
            color = (Color)event.getNewValue();
        }
    }
}
