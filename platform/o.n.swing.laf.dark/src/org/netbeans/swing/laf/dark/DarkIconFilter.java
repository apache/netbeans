/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
