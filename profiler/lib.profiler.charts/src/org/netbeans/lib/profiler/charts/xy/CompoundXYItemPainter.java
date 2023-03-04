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

package org.netbeans.lib.profiler.charts.xy;

import org.netbeans.lib.profiler.charts.CompoundItemPainter;
import org.netbeans.lib.profiler.charts.ChartContext;

/**
 *
 * @author Jiri Sedlacek
 */
public class CompoundXYItemPainter extends CompoundItemPainter implements XYItemPainter {

    public CompoundXYItemPainter(XYItemPainter painter1, XYItemPainter painter2) {
        super(painter1, painter2);
    }


    public double getItemView(double dataY, XYItem item, ChartContext context) {
        return getPainter1().getItemView(dataY, item, context);
    }

    public double getItemValue(double viewY, XYItem item, ChartContext context) {
        return getPainter1().getItemValue(viewY, item, context);
    }

    public double getItemValueScale(XYItem item, ChartContext context) {
        return getPainter1().getItemValueScale(item, context);
    }


    protected XYItemPainter getPainter1() {
        return (XYItemPainter)super.getPainter1();
    }

    protected XYItemPainter getPainter2() {
        return (XYItemPainter)super.getPainter2();
    }

}
