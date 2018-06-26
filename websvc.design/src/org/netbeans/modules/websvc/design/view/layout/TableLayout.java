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

package org.netbeans.modules.websvc.design.view.layout;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.widget.Widget;

/**
 * @author Ajit Bhate
 * @author anjeleevich
 */
public class TableLayout implements Layout {
    
    private int columnCount;
    private int hgap;
    private int vgap;
    private int minColumnWidth;

    public TableLayout(int columnCount, int hgap, int vgap,
            int minColumnWidth) 
    {
        this.minColumnWidth = minColumnWidth;
        this.columnCount = columnCount;
        this.hgap = hgap;
        this.vgap = vgap;
    }
    
    
    public void layout(Widget widget) {
        List<Widget> children = widget.getChildren();

        int y = 0;

        for (int i = 0; i < children.size(); i += columnCount) {
            
            int rowHeight = 0;
            
            for (int j = 0; j < columnCount; j++) {
                Widget w = children.get(i + j);
                Rectangle b = w.getPreferredBounds();
                rowHeight = Math.max(rowHeight, b.height);
            }
            
            int x = 0;
            
            for (int j = 0; j < columnCount; j++) {
                Widget w = children.get(i + j);
                Rectangle b = w.getPreferredBounds();
                b.height = rowHeight;
                b.width = minColumnWidth;
                w.resolveBounds(new Point(x - b.x, y - b.y), b);
                
                x += minColumnWidth + hgap;
            }
            
            y += rowHeight + vgap;
        }
    }

    public boolean requiresJustification(Widget widget) {
        return true;
    }

    public void justify(Widget widget) {
        Rectangle bounds = widget.getClientArea();

        int y0 = bounds.y;

        List<Widget> children = widget.getChildren();

        for (int i = 0; i < children.size(); i += columnCount) {
            int rowHeight = 0;

            for (int j = 0; j < columnCount; j++) {
                Widget w = children.get(i + j);
                Rectangle b = w.getBounds();
                rowHeight = Math.max(rowHeight, b.height);
            }
            
            int x0 = bounds.x;
            int width = bounds.width;
            
            for (int j = 0; j < columnCount; j++) {
                Widget w = children.get(i + j);
                Rectangle b = w.getBounds();
                
                int columnWidth = (width - hgap * (columnCount - 1 - j)) 
                        / (columnCount - j);
                
                b.width = columnWidth;
                b.height = rowHeight;
                
                w.resolveBounds(new Point(x0 - b.x, y0 - b.y), b);
                
                width -= columnWidth + hgap;
                x0 += columnWidth + hgap;
            }
            
            y0 += rowHeight + vgap;
        }
    }
}
