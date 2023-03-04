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

package org.netbeans.modules.profiler.snaptracer.impl.timeline;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.List;
import org.netbeans.lib.profiler.charts.ItemSelection;
import org.netbeans.lib.profiler.charts.swing.Utils;
import org.netbeans.lib.profiler.charts.xy.XYItem;
import org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYChartContext;

/**
 *
 * @author Jiri Sedlacek
 */
final class ContinuousXYPainter extends TimelineXYPainter {

    private static final Polygon POLYGON = new Polygon();
    
    protected final int lineWidth;
    protected final Color lineColor;
    protected final Color fillColor;
    protected final Color definingColor;

    protected final Stroke lineStroke;

    private final PointsComputer computer;


    ContinuousXYPainter(float lineWidth, Color lineColor, Color fillColor,
                        double dataFactor, PointsComputer computer) {

        super((int)Math.ceil(lineWidth), fillColor != null, dataFactor);

        if (lineColor == null && fillColor == null)
            throw new IllegalArgumentException("lineColor or fillColor must not be null"); // NOI18N

        this.lineWidth = (int)Math.ceil(lineWidth);
        this.lineColor = Utils.checkedColor(lineColor);
        this.fillColor = Utils.checkedColor(fillColor);

        definingColor = lineColor != null ? lineColor : fillColor;

        this.lineStroke = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND,
                                          BasicStroke.JOIN_ROUND);

        this.computer = computer;
    }


    protected Color getDefiningColor() {
        return definingColor;
    }

    protected void paint(XYItem item, List<ItemSelection> highlighted,
                         List<ItemSelection> selected, Graphics2D g,
                         Rectangle dirtyArea, SynchronousXYChartContext context) {

        int valuesCount = item.getValuesCount();
        int extraTrailing = fillColor != null ? 2 : 0;

        Rectangle dirtyExtended = new Rectangle(dirtyArea);
        dirtyExtended.x -= lineWidth;
        dirtyExtended.width += lineWidth * 2;
        int[][] idxs = computer.getVisible(dirtyExtended, valuesCount, context, 1,
                                           extraTrailing);
        if (idxs == null) return;
        int[] visibleIndexes = idxs[0];
        int npoints = idxs[1][0];
        int[][] points = computer.createPoints(visibleIndexes, npoints, item,
                                               dataFactor, context);

        if (fillColor != null) {
            points[0][npoints - 2] = points[0][npoints - 3];
            points[1][npoints - 2] = computer.getZeroY(context);
            points[0][npoints - 1] = points[0][0];
            points[1][npoints - 1] = points[1][npoints - 2];

            POLYGON.xpoints = points[0];
            POLYGON.ypoints = points[1];
            POLYGON.npoints = npoints;

            g.setPaint(fillColor);
            g.fill(POLYGON);
        }

        if (lineColor != null) {
            g.setPaint(lineColor);
            g.setStroke(lineStroke);
            g.drawPolyline(points[0], points[1], npoints - extraTrailing);
        }
    }

}
