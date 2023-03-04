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

package org.netbeans.lib.profiler.charts.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.lang.reflect.Method;
import javax.swing.UIManager;

/**
 *
 * @author Jiri Sedlacek
 */
public final class Utils {

    private static boolean FORCE_SPEED = Boolean.getBoolean("graphs.forceSpeed"); // NOI18N

    private static boolean forceSpeed = FORCE_SPEED || !isLocalDisplay();
//    private static boolean forceSpeed = true;


    public static boolean forceSpeed() {
        return forceSpeed;
    }


    public static Color checkedColor(Color color) {
        if (color == null) return null;
        if (!forceSpeed || color.getAlpha() == 255) return color;
        return new Color(color.getRed(), color.getGreen(), color.getBlue());
    }

    public static RenderingHints checkedRenderingHints(RenderingHints rHints) {
        if (!forceSpeed) return rHints;
        RenderingHints hints = (RenderingHints)rHints.clone();
        hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
        hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        return hints;
    }


    /**
     * Returns width of the Stroke. Note that this only works correctly for instances
     * of BasicStroke, for other Strokes it always returns 1. Returns zero width
     * for null Stroke.
     *
     * @param stroke Stroke
     * @return width of Stroke for instances of BasicStroke, 1 for other Stroke, 0 for null Stroke
     */
    public static float getStrokeWidth(Stroke stroke) {
        if (stroke == null) return 0f;
        if (!(stroke instanceof BasicStroke)) return 1f;
        return ((BasicStroke)stroke).getLineWidth();
    }


    public static Color getSystemSelection() {
        Color sel = UIManager.getColor("List.selectionBackground"); // NOI18N
        if (sel == null) sel = UIManager.getColor("nimbusSelectionBackground"); // NOI18N
        if (sel == null) sel = new Color(0, 0, 200);
        return sel;
    }


    private static boolean isLocalDisplay() {
        try {
            Class x11Class = Class.forName("sun.swing.SwingUtilities2"); // NOI18N
            Method isDisplayLocalMethod = x11Class.getMethod(
                      "isLocalDisplay", new Class[0]); // NOI18N
            return (Boolean)isDisplayLocalMethod.invoke(null, (Object[])null);
        } catch (Throwable t) {
            return true;
        }
    }


    // --- long <-> int conversions --------------------------------------------

    public static final int VALUE_OUT_OF_RANGE_NEG = Integer.MIN_VALUE;
    public static final int VALUE_OUT_OF_RANGE_POS = Integer.MAX_VALUE;


    public static final int checkedInt(double value) {
        if (value < Integer.MIN_VALUE) return VALUE_OUT_OF_RANGE_NEG;
        if (value > Integer.MAX_VALUE) return VALUE_OUT_OF_RANGE_POS;
        else return (int)value;
    }

    public static final Rectangle checkedRectangle(LongRect rect) {
        // TODO: this is incorrect, width/height don't reflect x/y truncation!
        return new Rectangle(checkedInt(rect.x),
                            checkedInt(rect.y),
                            checkedInt(rect.width),
                            checkedInt(rect.height));
    }

}
