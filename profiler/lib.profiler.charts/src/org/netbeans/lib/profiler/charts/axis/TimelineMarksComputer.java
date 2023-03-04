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

package org.netbeans.lib.profiler.charts.axis;

import java.util.Iterator;
import org.netbeans.lib.profiler.charts.ChartContext;
import org.netbeans.lib.profiler.charts.Timeline;
import org.netbeans.lib.profiler.charts.swing.Utils;

/**
 *
 * @author Jiri Sedlacek
 */
public class TimelineMarksComputer extends AxisMarksComputer.Abstract {

    private final Timeline timeline;

    private double scale;
    private long step;

    private long firstTimestamp;
    private long lastTimestamp;


    public TimelineMarksComputer(Timeline timeline,
                                 ChartContext context,
                                 int orientation) {

        super(context, orientation);
        this.timeline = timeline;

        scale = -1;
        step = -1;
    }


    protected int getMinMarksDistance() {
            return 120;
        }

    protected boolean refreshConfiguration() {
        double oldScale = scale;
        long oldFirstTimestamp = firstTimestamp;
        long oldLastTimestamp = lastTimestamp;
        
        if ((horizontal && context.getViewWidth() == 0) ||
            (!horizontal && context.getViewHeight() == 0)) {
            scale = -1;
//        } else if (timeline.getTimestampsCount() == 0) {
//            // Initial scale
//            scale = -1;
        } else {
            scale = horizontal ? context.getViewWidth(1d) :
                                 context.getViewHeight(1d);
        }

        int timestampsCount = timeline.getTimestampsCount();
        if (horizontal) {
            firstTimestamp = timestampsCount == 0 ? (long)context.getDataX(0) :
                                                     timeline.getTimestamp(0);
            lastTimestamp = timestampsCount == 0 ? (long)context.getDataX(
                                                    context.getViewportWidth()):
                                                    Math.max(timeline.getTimestamp
                                                    (timestampsCount - 1),
                                                    (long)context.getDataX(
                                                    context.getViewportWidth()));
        } else {
            firstTimestamp = timestampsCount == 0 ? (long)context.getDataY(0) :
                                                     timeline.getTimestamp(0);
            lastTimestamp = timestampsCount == 0 ? (long)context.getDataY(
                                                    context.getViewportWidth()):
                                                    Math.max(timeline.getTimestamp
                                                    (timestampsCount - 1),
                                                    (long)context.getDataY(
                                                    context.getViewportWidth()));
        }
        
        if (oldScale != scale) {

            if (scale == -1) {
                step = -1;
            } else {
                step = TimeAxisUtils.getTimeUnits(scale, getMinMarksDistance());
            }

            oldScale = scale;
            return true;
        } else {
            return oldFirstTimestamp != firstTimestamp ||
                   oldLastTimestamp != lastTimestamp;
        }
    }


    public Iterator<AxisMark> marksIterator(int start, int end) {
        if (step == -1) return EMPTY_ITERATOR;

        final long dataStart = horizontal ?
                               ((long)context.getDataX(start) / step) * step :
                               ((long)context.getDataY(start) / step) * step;
        final long dataEnd = horizontal ?
                               ((long)context.getDataX(end) / step) * step :
                               ((long)context.getDataY(end) / step) * step;
        final long iterCount = Math.abs(dataEnd - dataStart) / step + 2;
        final long[] iterIndex = new long[] { 0 };

        final String format = TimeAxisUtils.getFormatString(step, firstTimestamp,
                                                            lastTimestamp);


        return new AxisMarksComputer.AbstractIterator() {

            public boolean hasNext() {
                return iterIndex[0] < iterCount;
            }

            public AxisMark next() {
                long value = reverse ? dataStart - iterIndex[0] * step :
                                       dataStart + iterIndex[0] * step;
                iterIndex[0]++;
                int position = horizontal ?
                               Utils.checkedInt(Math.ceil(context.getViewX(value))) :
                               Utils.checkedInt(Math.ceil(context.getViewY(value)));
                return new TimeMark(value, position, format);
            }

        };
    }

}
