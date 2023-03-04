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

package org.netbeans.modules.profiler.snaptracer.impl.timeline.items;

import org.netbeans.modules.profiler.snaptracer.ItemValueFormatter;
import java.awt.Color;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class XYItemDescriptor extends ValueItemDescriptor {

    private final float lineWidth;
    private final Color lineColor;
    private final Color fillColor;


    XYItemDescriptor(String name, String description,
                     ItemValueFormatter formatter, double dataFactor,
                     long minValue, long maxValue, float lineWidth,
                     Color lineColor, Color fillColor) {

        super(name, description, formatter, dataFactor, minValue, maxValue);
        this.lineWidth = lineWidth;
        this.lineColor = lineColor;
        this.fillColor = fillColor;
    }

    
    public final float getLineWidth() {
        return lineWidth;
    }

    public final Color getLineColor() {
        return lineColor;
    }

    public final Color getFillColor() {
        return fillColor;
    }

}
