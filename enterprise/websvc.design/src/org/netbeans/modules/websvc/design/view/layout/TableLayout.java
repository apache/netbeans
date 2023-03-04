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
