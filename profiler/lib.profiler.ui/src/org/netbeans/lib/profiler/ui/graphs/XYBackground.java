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

package org.netbeans.lib.profiler.ui.graphs;

import org.netbeans.lib.profiler.charts.ChartContext;
import org.netbeans.lib.profiler.charts.ChartDecorator;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import org.netbeans.lib.profiler.charts.swing.Utils;
import org.netbeans.lib.profiler.ui.UIUtils;

/**
 *
 * @author Jiri Sedlacek
 */
class XYBackground implements ChartDecorator {

    private static final Color GRADIENT_TOP = !UIUtils.isDarkResultsBackground() ?
                               new Color(240, 240, 240) : new Color(60, 60, 60);
    private static final Color GRADIENT_BOTTOM = !UIUtils.isDarkResultsBackground() ?
                               new Color(250, 250, 250) : new Color(75, 75, 75);

    public void paint(Graphics2D g, Rectangle dirtyArea, ChartContext context) {
        if (Utils.forceSpeed()) g.setPaint(GRADIENT_BOTTOM);
        else g.setPaint(new GradientPaint(
                        new Point(0, Utils.checkedInt(context.getViewportOffsetY())),
                        GRADIENT_TOP,
                        new Point(0, context.getViewportHeight()),
                        GRADIENT_BOTTOM));
        g.fill(dirtyArea);
    }

}
