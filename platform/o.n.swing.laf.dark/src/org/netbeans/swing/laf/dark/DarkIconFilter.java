/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.swing.laf.dark;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

/**
 * For dark LaFs it inverts icon brightness (=inverts icon image to obtain dark icon,
 * then inverts its hue to restore original colors).
 * 
 */
public class DarkIconFilter extends RGBImageFilter {

    /** in dark LaFs brighten all icons; 0.0f = no change, 1.0f = maximum brightening */
    private static final float DARK_ICON_BRIGHTEN = 0.1f;

    @Override
    public int filterRGB(int x, int y, int color) {
        int a = color & 0xff000000;
        int rgb[] = decode(color);
        int inverted[] = invert(rgb);
        int result[] = invertHueBrighten(inverted, DARK_ICON_BRIGHTEN);
        return a | encode(result);
   }

    private int[] invert(int[] rgb) {
        return new int[]{255-rgb[0], 255-rgb[1], 255-rgb[2]};
    }

    private int[] invertHueBrighten(int[] rgb, float brighten) {
        float hsb[] = new float[3];
        Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], hsb);
        return decode(Color.HSBtoRGB(hsb[0] > 0.5f ? hsb[0]-0.5f : hsb[0]+0.5f, hsb[1], hsb[2]+(1.0f-hsb[2])*brighten));
    }

    private int[] decode(int rgb) {
        return new int[]{(rgb & 0x00ff0000) >> 16, (rgb & 0x0000ff00) >> 8, rgb & 0x000000ff};
    }
    private int encode(int[] rgb) {
        return (toBoundaries(rgb[0]) << 16) | (toBoundaries(rgb[1]) << 8) | toBoundaries(rgb[2]);
    }

    private int toBoundaries(int color) {
        return Math.max(0,Math.min(255,color));
    }

}
