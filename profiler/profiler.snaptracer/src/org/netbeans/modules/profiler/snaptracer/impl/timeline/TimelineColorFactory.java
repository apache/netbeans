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

import java.awt.Color;

/**
 * Utility class to access colors predefined for VisualVM.
 *
 * @author Jiri Sedlacek
 */
final class TimelineColorFactory {
    
    private static final Color[] PREDEFINED_COLORS = new Color[] {
                                                new Color(241, 154,  42),
                                                new Color( 32, 171, 217),
                                                new Color(144,  97, 207),
                                                new Color(158, 156,   0)
    };

    private static final Color[][] PREDEFINED_GRADIENTS = new Color[][] {
        new Color[] { new Color(245, 204, 152), new Color(255, 243, 226) },
        new Color[] { new Color(151, 223, 246), new Color(227, 248, 255) },
        new Color[] { new Color(200, 163, 248), new Color(242, 232, 255) },
        new Color[] { new Color(212, 211, 131), new Color(244, 243, 217) }
    };
    

    /**
     * Returns a color predefined for VisualVM charts.
     *
     * @param index index of the predefined color
     * @return color predefined for VisualVM charts
     */
    private static Color getPredefinedColor(int index) {
        return PREDEFINED_COLORS[index];
    }

    static Color getColor(int index) {
        Color color;

        if (index >= PREDEFINED_COLORS.length) {
            color = getPredefinedColor(index % PREDEFINED_COLORS.length);
            int darkerFactor = index / PREDEFINED_COLORS.length;
            while (darkerFactor-- > 0) color = color.darker();
        } else {
            color = getPredefinedColor(index);
        }

        return color;
    }


    /**
     * Returns a color pair predefined for VisualVM charts gradients.
     *
     * @param index index of the predefined color pair
     * @return color pair predefined for VisualVM charts gradients
     */
    private static Color[] getPredefinedGradient(int index) {
        return PREDEFINED_GRADIENTS[index];
    }

    static Color[] getGradient(int index) {
        Color[] colors = null;

        if (index >= PREDEFINED_GRADIENTS.length) {
            colors = getPredefinedGradient(index % PREDEFINED_GRADIENTS.length);
            int darkerFactor = index / PREDEFINED_GRADIENTS.length;
            while (darkerFactor-- > 0) {
                colors[0] = colors[0].darker();
                colors[1] = colors[1].darker();
            }
        } else {
            colors = getPredefinedGradient(index);
        }

        return colors;

    }

}
