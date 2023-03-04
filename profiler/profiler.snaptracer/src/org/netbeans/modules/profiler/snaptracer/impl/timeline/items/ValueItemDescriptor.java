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
import org.netbeans.modules.profiler.snaptracer.ProbeItemDescriptor;

/**
 * 
 * @author Jiri Sedlacek
 */
public abstract class ValueItemDescriptor extends ProbeItemDescriptor {

    private final ItemValueFormatter formatter;
    private final double dataFactor;
    private final long minValue;
    private final long maxValue;


    ValueItemDescriptor(String name, String description,
                        ItemValueFormatter formatter, double dataFactor,
                        long minValue, long maxValue) {

        super(name, description);
        if (formatter == null) {
            throw new IllegalArgumentException("formatter cannot be null"); // NOI18N
        }
        if (dataFactor == 0) {
            throw new IllegalArgumentException("dataFactor cannot be 0"); // NOI18N
        }
        this.formatter = formatter;
        this.dataFactor = dataFactor;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }


    public final String getValueString(long value, int format) {
        return formatter.formatValue(value, format);
    }

    public final String getUnitsString(int format) {
        return formatter.getUnits(format);
    }

    public final double getDataFactor() {
        return dataFactor;
    }

    public final long getMinValue() {
        return minValue;
    }

    public final long getMaxValue() {
        return maxValue;
    }
    
}
