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

package org.netbeans.modules.profiler.snaptracer;

import org.netbeans.modules.profiler.snaptracer.impl.timeline.items.ContinuousXYItemDescriptor;
import org.netbeans.modules.profiler.snaptracer.impl.timeline.items.DiscreteXYItemDescriptor;
import org.netbeans.modules.profiler.snaptracer.impl.timeline.items.ValueItemDescriptor;
import java.awt.Color;
import org.netbeans.modules.profiler.snaptracer.impl.timeline.items.IconItemDescriptor;

/**
 * ProbeItemDescriptor describes a TracerProbe item appearance in the UI.
 * <p>
 *
 * Current version supports two general types of items: continuous and discrete.
 * Continuous items consist of non-rectangular polyline or polygon segments
 * connecting the values. The values define vertices of the segments.
 * <p>
 *
 * Discrete items consist of rectangular segments either connected together or
 * divided into bar segments. The values are located in the middle of the segments.
 * <p>
 *
 * Each descriptor requires a common set of mandatory options:
 * <ul>
 * <li><code>name</code>: item name
 * <li><code>description</code>: item description, may be <code>null</code>
 * <li><code>formatter</code>: ItemValueFormatter instance which defines how the item values are presented in UI
 * </ul>
 * <p>
 *
 * The other options which may be set are:
 * <ul>
 * <li><code>dataFactor</code>: a multiplication factor for item values, useful when displaying multiple items in one graph
 * <li<code>minValue</code>: minimum (initial) item value, typically set for zero-based metrics (heap size)
 * <li><code>maxValue</code>: maximum (initial) item value, may be used for the initial graph scale
 * </ul>
 * <p>
 *
 * There's no need to define line width and/or line/fill colors, the framework
 * guarantees that each item in a graph will be displayed by a different color.
 * If needed, line width and/or line/fill colors may be customized by setting
 * these options:
 * <ul>
 * <li><code>lineWidth</code>: width of the line, default is <code>2f</code>
 * <li><code>lineColor</code>: color of the line, may be <code>null</code>
 * <li><code>fillColor</code>: color of the filled area, may be <code>null</code>
 * </ul>
 * <p>
 *
 * <b>Note:</b> Use the predefined static methods to create instances of ProbeItemDescriptor.
 * Custom instances of ProbeItemDescriptor are not supported and will cause a
 * <code>RuntimeException</code>.
 *
 * @author Jiri Sedlacek
 */
public abstract class ProbeItemDescriptor {

    // --- Public predefined constants -----------------------------------------

    /**
     * Minimum item value is undefined.
     */
    public static final long MIN_VALUE_UNDEFINED = Long.MAX_VALUE;
    /**
     * Maximum item value is undefined.
     */
    public static final long MAX_VALUE_UNDEFINED = Long.MIN_VALUE;
    /**
     * Value is undefined. For minimum/maximum value use MIN_VALUE_UNDEFINED or
     * MAX_VALUE_UNDEFINED.
     */
    public static final long VALUE_UNDEFINED = Long.MIN_VALUE - 1;

    /**
     * Default color.
     */
    public static final Color DEFAULT_COLOR = new Color(0, 0, 0); // use == to identify this instance!

    /**
     * Default line width.
     */
    public static final float DEFAULT_LINE_WIDTH = -1.0F;


    // --- Private instance variables ------------------------------------------

    private final String name;
    private final String description;


    // --- Protected constructor -----------------------------------------------

    protected ProbeItemDescriptor(String name, String description) {
        if (name == null)
            throw new IllegalArgumentException("name cannot be null"); // NOI18N

        // Custom ProbeItemDescriptor subclasses are currently not supported.
        // May be supported in future versions together with custom Painters.
        if (!(this instanceof ValueItemDescriptor))
            throw new UnsupportedOperationException("Custom descriptor not supported. Use the predefined descriptors."); // NOI18N

        this.name = name;
        this.description = description;
    }


    // --- Common implementation -----------------------------------------------

    /**
     * Returns name of the item.
     * @return name of the item
     */
    public final String getName() { return name; }

    /**
     * Returns description of the item.
     * @return description of the item
     */
    public final String getDescription() { return description; }


    // === Public factory methods ==============================================

    // --- Icon items ----------------------------------------------------------

    public static ProbeItemDescriptor iconItem(String name, String description,
                                               ItemValueFormatter formatter) {
        return new IconItemDescriptor(name, description, formatter, DEFAULT_COLOR);
    }


    // --- Continuous items ----------------------------------------------------

    /**
     * Creates descriptor for a continuous item created by line segments.
     *
     * @param name item name
     * @param description item description or null
     * @param formatter item formatter
     * @return descriptor for a continuous item created by line segments
     */
    public static ProbeItemDescriptor continuousLineItem(String name, String description,
                                                         ItemValueFormatter formatter) {

        return continuousItem(name, description, formatter, 1d, 0, MAX_VALUE_UNDEFINED,
                              DEFAULT_LINE_WIDTH, DEFAULT_COLOR, null);
    }

    /**
     * Creates descriptor for a continuous item created by line segments with custom dataFactor ad min/max values.
     *
     * @param name item name
     * @param description item description or null
     * @param formatter item formatter
     * @param dataFactor multiplication factor
     * @param minValue minimum (initial) item value
     * @param maxValue maximum (initial) item value
     * @return descriptor for a continuous item created by line segments
     */
    public static ProbeItemDescriptor continuousLineItem(String name, String description,
                                                         ItemValueFormatter formatter,
                                                         double dataFactor,
                                                         long minValue, long maxValue) {

        return continuousItem(name, description, formatter, dataFactor, minValue,
                              maxValue, DEFAULT_LINE_WIDTH, DEFAULT_COLOR, null);
    }

    /**
     * Creates descriptor for a continuous item created by filled segments.
     *
     * @param name item name
     * @param description item description or null
     * @param formatter item formatter
     * @return descriptor for a continuous item created by filled segments
     */
    public static ProbeItemDescriptor continuousFillItem(String name, String description,
                                                         ItemValueFormatter formatter) {

        return continuousItem(name, description, formatter, 1d, 0, MAX_VALUE_UNDEFINED,
                              DEFAULT_LINE_WIDTH, null, DEFAULT_COLOR);
    }

    /**
     * Creates descriptor for a continuous item created by filled segments with custom dataFactor ad min/max values.
     *
     * @param name item name
     * @param description item description or null
     * @param formatter item formatter
     * @param dataFactor multiplication factor
     * @param minValue minimum (initial) item value
     * @param maxValue maximum (initial) item value
     * @return descriptor for a continuous item created by filled segments
     */
    public static ProbeItemDescriptor continuousFillItem(String name, String description,
                                                         ItemValueFormatter formatter,
                                                         double dataFactor,
                                                         long minValue, long maxValue) {

        return continuousItem(name, description, formatter, dataFactor, minValue,
                              maxValue, DEFAULT_LINE_WIDTH, null, DEFAULT_COLOR);
    }

    /**
     * Creates descriptor for a continuous item created by line and filled segments.
     *
     * @param name item name
     * @param description item description or null
     * @param formatter item formatter
     * @return descriptor for a continuous item created by line and filled segments
     */
    public static ProbeItemDescriptor continuousLineFillItem(String name, String description,
                                                             ItemValueFormatter formatter) {

        return continuousItem(name, description, formatter, 1d, 0, MAX_VALUE_UNDEFINED,
                              DEFAULT_LINE_WIDTH, DEFAULT_COLOR, DEFAULT_COLOR);
    }

    /**
     * Creates descriptor for a continuous item created by line and filled segments with custom dataFactor ad min/max values.
     *
     * @param name item name
     * @param description item description or null
     * @param formatter item formatter
     * @param dataFactor multiplication factor
     * @param minValue minimum (initial) item value
     * @param maxValue maximum (initial) item value
     * @return descriptor for a continuous item created by line and filled segments
     */
    public static ProbeItemDescriptor continuousLineFillItem(String name, String description,
                                                             ItemValueFormatter formatter,
                                                             double dataFactor,
                                                             long minValue, long maxValue) {

        return continuousItem(name, description, formatter, dataFactor, minValue,
                              maxValue, DEFAULT_LINE_WIDTH, DEFAULT_COLOR, DEFAULT_COLOR);
    }

    /**
     * Creates descriptor for a general continuous item with custom dataFactor ad min/max values, and custom line width and line/fill colors.
     *
     * @param name item name
     * @param description item description or null
     * @param formatter item formatter
     * @param dataFactor multiplication factor
     * @param minValue minimum (initial) item value
     * @param maxValue maximum (initial) item value
     * @param lineWidth line width
     * @param lineColor line color or null
     * @param fillColor fill color or null
     * @return descriptor for a general continuous item
     */
    public static ProbeItemDescriptor continuousItem(String name, String description,
                                                     ItemValueFormatter formatter,
                                                     double dataFactor, long minValue,
                                                     long maxValue, float lineWidth,
                                                     Color lineColor, Color fillColor) {
        
        if (lineColor == null && fillColor == null)
            throw new IllegalArgumentException("Either lineColor or fillColor must be defined"); // NOI18N

        return new ContinuousXYItemDescriptor(name, description, formatter, dataFactor,
                                              minValue, maxValue, lineWidth, lineColor,
                                              fillColor);
    }


    // --- Discrete items ------------------------------------------------------

    /**
     * Creates descriptor for a discrete item created by line segments representing the outline.
     *
     * @param name item name
     * @param description item description or null
     * @param formatter item formatter
     * @return descriptor for a discrete item created by line segments representing the outline
     */
    public static ProbeItemDescriptor discreteLineItem(String name, String description,
                                                       ItemValueFormatter formatter) {

        return discreteOutlineItem(name, description, formatter, 1d, 0, MAX_VALUE_UNDEFINED,
                                   DEFAULT_LINE_WIDTH, DEFAULT_COLOR, null);
    }

    /**
     * Creates descriptor for a discrete item created by line segments representing the outline with custom dataFactor ad min/max values.
     *
     * @param name item name
     * @param description item description or null
     * @param formatter item formatter
     * @param dataFactor multiplication factor
     * @param minValue minimum (initial) item value
     * @param maxValue maximum (initial) item value
     * @return descriptor for a discrete item created by line segments representing the outline
     */
    public static ProbeItemDescriptor discreteLineItem(String name, String description,
                                                       ItemValueFormatter formatter,
                                                       double dataFactor,
                                                       long minValue, long maxValue) {

        return discreteOutlineItem(name, description, formatter, dataFactor, minValue,
                                   maxValue, DEFAULT_LINE_WIDTH, DEFAULT_COLOR, null);
    }

    /**
     * Creates descriptor of a discrete item created by filled segments.
     *
     * @param name item name
     * @param description item description or null
     * @param formatter item formatter
     * @return descriptor of a discrete item created by filled segments
     */
    public static ProbeItemDescriptor discreteFillItem(String name, String description,
                                                       ItemValueFormatter formatter) {

        return discreteOutlineItem(name, description, formatter, 1d, 0, MAX_VALUE_UNDEFINED,
                                   DEFAULT_LINE_WIDTH, null, DEFAULT_COLOR);
    }

    /**
     * Creates descriptor of a discrete item created by filled segments with custom dataFactor ad min/max values.
     *
     * @param name item name
     * @param description item description or null
     * @param formatter item formatter
     * @param dataFactor multiplication factor
     * @param minValue minimum (initial) item value
     * @param maxValue maximum (initial) item value
     * @return descriptor of a discrete item created by filled segments
     */
    public static ProbeItemDescriptor discreteFillItem(String name, String description,
                                                       ItemValueFormatter formatter,
                                                       double dataFactor,
                                                       long minValue, long maxValue) {

        return discreteOutlineItem(name, description, formatter, dataFactor, minValue,
                                   maxValue, DEFAULT_LINE_WIDTH, null, DEFAULT_COLOR);
    }

    /**
     * Creates descriptor for a discrete item created by line segments representing the outline and filled segments.
     *
     * @param name item name
     * @param description item description or null
     * @param formatter item formatter
     * @return descriptor for a discrete item created by line segments representing the outline and filled segments
     */
    public static ProbeItemDescriptor discreteLineFillItem(String name, String description,
                                                           ItemValueFormatter formatter) {

        return discreteOutlineItem(name, description, formatter, 1d, 0, MAX_VALUE_UNDEFINED,
                                  DEFAULT_LINE_WIDTH, DEFAULT_COLOR, DEFAULT_COLOR);
    }

    /**
     * Creates descriptor for a discrete item created by line segments representing the outline and filled segments with custom dataFactor ad min/max values.
     *
     * @param name item name
     * @param description item description or null
     * @param formatter item formatter
     * @param dataFactor multiplication factor
     * @param minValue minimum (initial) item value
     * @param maxValue maximum (initial) item value
     * @return descriptor for a discrete item created by line segments representing the outline and filled segments
     */
    public static ProbeItemDescriptor discreteLineFillItem(String name, String description,
                                                           ItemValueFormatter formatter,
                                                           double dataFactor,
                                                           long minValue, long maxValue) {
        
        return discreteOutlineItem(name, description, formatter, dataFactor, minValue,
                                   maxValue, DEFAULT_LINE_WIDTH, DEFAULT_COLOR, DEFAULT_COLOR);
    }

    /**
     * Creates descriptor for a general discrete outlined item with custom dataFactor ad min/max values, and custom line width and line/fill colors.
     *
     * @param name item name
     * @param description item description or null
     * @param formatter item formatter
     * @param dataFactor multiplication factor
     * @param minValue minimum (initial) item value
     * @param maxValue maximum (initial) item value
     * @param lineWidth line width
     * @param lineColor line color or null
     * @param fillColor fill color or null
     * @return descriptor for a general discrete outlined item
     */
    public static ProbeItemDescriptor discreteOutlineItem(String name, String description,
                                                          ItemValueFormatter formatter,
                                                          double dataFactor, long minValue,
                                                          long maxValue, float lineWidth,
                                                          Color lineColor, Color fillColor) {
        
        if (lineColor == null && fillColor == null)
            throw new IllegalArgumentException("Either lineColor or fillColor must be defined"); // NOI18N

        return discreteItem(name, description, formatter, dataFactor, minValue, maxValue,
                            lineWidth, lineColor, fillColor, 0, false, false, true);
    }

    /**
     * Creates descriptor for a discrete item represented by a horizontal line segment, optionally filled, with custom dataFactor ad min/max values.
     *
     * @param name item name
     * @param description item description or null
     * @param formatter item formatter
     * @param dataFactor multiplication factor
     * @param minValue minimum (initial) item value
     * @param maxValue maximum (initial) item value
     * @param filled true if the segments are filled
     * @param width width of/between the segments
     * @param fixedWidth true if width defines segment width, false if width defines segments spacing
     * @return descriptor for a discrete item represented by a horizontal line segment, optionally filled
     */
    public static ProbeItemDescriptor discreteToplineItem(String name, String description,
                                                          ItemValueFormatter formatter,
                                                          double dataFactor, long minValue,
                                                          long maxValue, boolean filled,
                                                          int width, boolean fixedWidth) {

        return discreteItem(name, description, formatter, dataFactor, minValue, maxValue,
                            DEFAULT_LINE_WIDTH, DEFAULT_COLOR, filled ? DEFAULT_COLOR : null,
                            width, fixedWidth, true, false);
    }

    /**
     * Creates descriptor for a discrete item represented by a horizontal line segment, optionally filled, with custom dataFactor ad min/max values, and custom line width and line/fill colors.
     *
     * @param name item name
     * @param description item description or null
     * @param formatter item formatter
     * @param dataFactor multiplication factor
     * @param minValue minimum (initial) item value
     * @param maxValue maximum (initial) item value
     * @param lineWidth line width
     * @param lineColor line color or null
     * @param fillColor fill color or null
     * @param width width width of/between the segments
     * @param fixedWidth fixedWidth true if width defines segment width, false if width defines segments spacing
     * @return descriptor for a discrete item represented by a horizontal line segment, optionally filled
     */
    public static ProbeItemDescriptor discreteToplineItem(String name, String description,
                                                          ItemValueFormatter formatter,
                                                          double dataFactor, long minValue,
                                                          long maxValue, float lineWidth,
                                                          Color lineColor, Color fillColor,
                                                          int width, boolean fixedWidth) {
        
        if (lineColor == null && fillColor == null)
            throw new IllegalArgumentException("Either lineColor or fillColor must be defined"); // NOI18N

        return discreteItem(name, description, formatter, dataFactor, minValue, maxValue,
                            lineWidth, lineColor, fillColor, width, fixedWidth, true, false);
    }

    /**
     * Creates descriptor for a discrete item represented by vertical bars, with custom dataFactor ad min/max values
     *
     * @param name item name
     * @param description item description or null
     * @param formatter item formatter
     * @param dataFactor multiplication factor
     * @param minValue minimum (initial) item value
     * @param maxValue maximum (initial) item value
     * @param outlined true if the bars are outlined
     * @param filled true if the bars are filled
     * @param width width width of/between the bars
     * @param fixedWidth fixedWidth true if width defines bar width, false if width defines bars spacing
     * @return descriptor for a discrete item represented by vertical bars
     */
    public static ProbeItemDescriptor discreteBarItem(String name, String description,
                                                      ItemValueFormatter formatter,
                                                      double dataFactor, long minValue,
                                                      long maxValue, boolean outlined,
                                                      boolean filled, int width,
                                                      boolean fixedWidth) {

        if (!outlined && !filled)
            throw new IllegalArgumentException("Either outlined or filled must be set"); // NOI18N

        return discreteItem(name, description, formatter, dataFactor, minValue, maxValue,
                            DEFAULT_LINE_WIDTH, outlined ? DEFAULT_COLOR : null,
                            filled ? DEFAULT_COLOR : null, width, fixedWidth, false, !filled);
    }

    /**
     * Creates descriptor for a discrete item represented by vertical bars, with custom dataFactor ad min/max values, and custom line width and line/fill colors.
     *
     * @param name item name
     * @param description item description or null
     * @param formatter item formatter
     * @param dataFactor multiplication factor
     * @param minValue minimum (initial) item value
     * @param maxValue maximum (initial) item value
     * @param lineWidth line width
     * @param lineColor line color or null
     * @param fillColor fill color or null
     * @param width width width of/between the bars
     * @param fixedWidth fixedWidth true if width defines bar width, false if width defines bars spacing
     * @return descriptor for a discrete item represented by vertical bars
     */
    public static ProbeItemDescriptor discreteBarItem(String name, String description,
                                                      ItemValueFormatter formatter,
                                                      double dataFactor, long minValue,
                                                      long maxValue, float lineWidth,
                                                      Color lineColor, Color fillColor,
                                                      int width, boolean fixedWidth) {
        
        if (lineColor == null && fillColor == null)
            throw new IllegalArgumentException("Either lineColor or fillColor must be defined"); // NOI18N

        return discreteItem(name, description, formatter, dataFactor, minValue, maxValue,
                            lineWidth, lineColor, fillColor, width, fixedWidth, false, fillColor == null);
    }

    private static ProbeItemDescriptor discreteItem(String name, String description,
                                                    ItemValueFormatter formatter,
                                                    double dataFactor, long minValue,
                                                    long maxValue, float lineWidth,
                                                    Color lineColor, Color fillColor,
                                                    int width, boolean fixedWidth,
                                                    boolean topLineOnly,
                                                    boolean outlineOnly) {

        return new DiscreteXYItemDescriptor(name, description, formatter, dataFactor,
                                            minValue, maxValue, lineWidth, lineColor,
                                            fillColor, width, fixedWidth, topLineOnly,
                                            outlineOnly);
    }

}
