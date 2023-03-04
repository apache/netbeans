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

import java.text.NumberFormat;

/**
 *
 * @author Jiri Sedlacek
 */
public class PercentLongMarksPainter extends AxisMarksPainter.Abstract {

    protected final long minValue;
    protected final long maxValue;

    protected NumberFormat format;


    public PercentLongMarksPainter(long minValue, long maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;

        format = NumberFormat.getPercentInstance();
    }


    protected String formatMark(AxisMark mark) {
        if (!(mark instanceof LongMark)) return mark.toString();
        long value = ((LongMark)mark).getValue();
        float relValue = (float)(value - minValue) / (float)maxValue;
        return format.format(relValue);
    }

}
