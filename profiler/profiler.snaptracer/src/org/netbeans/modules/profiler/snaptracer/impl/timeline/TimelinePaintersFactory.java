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

import org.netbeans.modules.profiler.snaptracer.impl.timeline.items.ContinuousXYItemDescriptor;
import org.netbeans.modules.profiler.snaptracer.impl.timeline.items.DiscreteXYItemDescriptor;
import org.netbeans.modules.profiler.snaptracer.impl.timeline.items.ValueItemDescriptor;
import org.netbeans.modules.profiler.snaptracer.impl.timeline.items.XYItemDescriptor;
import java.awt.Color;
import org.netbeans.modules.profiler.snaptracer.ProbeItemDescriptor;
import org.netbeans.modules.profiler.snaptracer.impl.IdeSnapshot;
import org.netbeans.modules.profiler.snaptracer.impl.timeline.items.IconItemDescriptor;

/**
 *
 * @author Jiri Sedlacek
 */
final class TimelinePaintersFactory {

    static TimelineXYPainter createPainter(ProbeItemDescriptor descriptor,
                                           int itemIndex, PointsComputer c,
                                           IdeSnapshot snapshot) {

        // --- ValueItem -------------------------------------------------------
        if (descriptor instanceof ValueItemDescriptor)
            return createValuePainter((ValueItemDescriptor)descriptor, itemIndex, c, snapshot);

        return null;
    }

    private static TimelineXYPainter createValuePainter(
            ValueItemDescriptor descriptor, int itemIndex, PointsComputer c,
            IdeSnapshot snapshot) {

        // --- XYItem ----------------------------------------------------------
        if (descriptor instanceof ContinuousXYItemDescriptor)
            return createContinuousPainter((ContinuousXYItemDescriptor)descriptor, itemIndex, c);
        
        // --- BarItem ---------------------------------------------------------
        if (descriptor instanceof DiscreteXYItemDescriptor)
            return createDiscretePainter((DiscreteXYItemDescriptor)descriptor, itemIndex, c);

        // --- IconItem --------------------------------------------------------
        if (descriptor instanceof IconItemDescriptor)
            return createIconPainter((IconItemDescriptor)descriptor, itemIndex, snapshot);

        return null;
    }

    private static TimelineXYPainter createContinuousPainter(
            XYItemDescriptor descriptor, int itemIndex, PointsComputer c) {

        double dataFactor = descriptor.getDataFactor();

        float lineWidth = descriptor.getLineWidth();
        if (lineWidth == ProbeItemDescriptor.DEFAULT_LINE_WIDTH)
            lineWidth = 2f;

        Color lineColor = descriptor.getLineColor();
        if (lineColor == ProbeItemDescriptor.DEFAULT_COLOR)
            lineColor = TimelineColorFactory.getColor(itemIndex);

        Color fillColor = descriptor.getFillColor();
        if (fillColor == ProbeItemDescriptor.DEFAULT_COLOR) {
            if (lineColor == null)
                fillColor = TimelineColorFactory.getColor(itemIndex);
            else
                fillColor = TimelineColorFactory.getGradient(itemIndex)[0];
        }

        return new ContinuousXYPainter(lineWidth, lineColor, fillColor, dataFactor, c);
    }

    private static DiscreteXYPainter createDiscretePainter(
            DiscreteXYItemDescriptor descriptor, int itemIndex, PointsComputer c) {

        double dataFactor = descriptor.getDataFactor();

        float lineWidth = descriptor.getLineWidth();
        if (lineWidth == ProbeItemDescriptor.DEFAULT_LINE_WIDTH)
            lineWidth = 2f;

        Color lineColor = descriptor.getLineColor();
        if (lineColor == ProbeItemDescriptor.DEFAULT_COLOR)
            lineColor = TimelineColorFactory.getColor(itemIndex);

        Color fillColor = descriptor.getFillColor();
        if (fillColor == ProbeItemDescriptor.DEFAULT_COLOR) {
            if (lineColor == null)
                fillColor = TimelineColorFactory.getColor(itemIndex);
            else
                fillColor = TimelineColorFactory.getGradient(itemIndex)[0];
        }

        return new DiscreteXYPainter(lineWidth, lineColor, fillColor, descriptor.getWidth(),
                                     descriptor.isFixedWidth(), descriptor.isTopLineOnly(),
                                     descriptor.isOutlineOnly(), dataFactor, c);
    }

    private static TimelineIconPainter createIconPainter(
            IconItemDescriptor descriptor, int itemIndex, IdeSnapshot snapshot) {

        Color color = descriptor.getColor();
        if (color == ProbeItemDescriptor.DEFAULT_COLOR)
            color = TimelineColorFactory.getColor(itemIndex);

        return new TimelineIconPainter(color, snapshot);
    }

}
