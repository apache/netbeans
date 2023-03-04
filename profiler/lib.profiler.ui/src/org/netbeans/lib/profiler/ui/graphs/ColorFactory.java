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

import java.awt.Color;
import org.netbeans.lib.profiler.ui.UIUtils;

/**
 * Utility class to access colors predefined for VisualVM.
 *
 * @author Jiri Sedlacek
 */
final class ColorFactory {
    
    private static final Color[] PREDEFINED_COLORS = new Color[] {
                                                new Color(241, 154,  42),
                                                new Color( 32, 171, 217),
                                                new Color(144,  97, 207),
                                                new Color(158, 156,   0)
    };

    private static final Color[][] PREDEFINED_GRADIENTS = !UIUtils.isDarkResultsBackground() ?
    new Color[][] {
        new Color[] { new Color(245, 204, 152), new Color(255, 243, 226) },
        new Color[] { new Color(151, 223, 246), new Color(227, 248, 255) },
        new Color[] { new Color(200, 163, 248), new Color(242, 232, 255) },
        new Color[] { new Color(212, 211, 131), new Color(244, 243, 217) }
    } :
    new Color[][] {
        new Color[] { new Color(145, 104, 052), new Color(155, 143, 126) },
        new Color[] { new Color(051, 123, 146), new Color(127, 148, 155) },
        new Color[] { new Color(100, 063, 148), new Color(142, 132, 155) },
        new Color[] { new Color(112, 111, 031), new Color(144, 143, 117) }
    };
    

    /**
     * Returns number of colors predefined for VisualVM charts.
     * Always contains at least 4 colors.
     *
     * @return number of colors predefined for VisualVM charts
     */
    public static int getPredefinedColorsCount() {
        return PREDEFINED_COLORS.length;
    }

    /**
     * Returns a color predefined for VisualVM charts.
     *
     * @param index index of the predefined color
     * @return color predefined for VisualVM charts
     */
    public static Color getPredefinedColor(int index) {
        return PREDEFINED_COLORS[index];
    }


    /**
     * Returns number of color pairs predefined for VisualVM charts gradients.
     * Always contains at least 4 color pairs.
     *
     * @return number of color pairs predefined for VisualVM charts gradients
     */
    public static int getPredefinedGradientsCount() {
        return PREDEFINED_GRADIENTS.length;
    }

    /**
     * Returns a color pair predefined for VisualVM charts gradients.
     *
     * @param index index of the predefined color pair
     * @return color pair predefined for VisualVM charts gradients
     */
    public static Color[] getPredefinedGradient(int index) {
        return PREDEFINED_GRADIENTS[index];
    }

}
