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

/*
 * RelativeColor.java
 *
 * Created on March 13, 2004, 1:37 PM
 */

package org.netbeans.swing.laf.dark;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * COPIED FROM O.N.SWING.PLAF
 *
 * A color which can be placed into UIDefaults, which is computed from:
 * <ul>
 * <li>A base color, as defined in a UI spec - this might be the expected value
 *     for window titlebars, for example </li>
 * <li>A target color, as defined in a UI spec, whose color is not the base
 *     color, but has a relation to it (such as brighter or darker, or
 *     hue shifted)</li>
 * <li>The actual color - which may differ from the base color if the user has
 *     customized their UI them (for example, changing the color defaults in
 *     Windows)</li>
 * <li>(optional) A color that the result must contrast with sufficiently that
 *     text will be readable</li>
 * </ul>
 * When constructing the real value, a color will be generated which has the 
 * same relationship to the original value as the base color has to the target
 * color.
 *
 * <h2>What this class is for</h2>
 * A number of components in NetBeans have colors that should be based on a
 * color taken from the desktop theme.  Swing provides a mechanism for getting
 * these colors, via UIManager, which will supply correct colors based on the
 * desktop theme for a variety of operating systems.
 * <p>
 * But often the color in a UI specification is not the same as, but related to
 * the color that should be used.  For example, in windows classic, the tabs
 * have a gradient based on a light blue color.  The color should be related to
 * the dark blue color normally used in Windows for window titles.  However,
 * if the user has set the window titlebar color to red, a reddish color should
 * be used.  
 * <p>
 * This class allows you to provide a base value (the default Windows
 * titlebar color, hardcoded) and a prototype value (the blue color that should
 * be used <i>if</i> the desktop colors are the defaults), and the <i>actual</i>
 * value retrieved from the UI.  The instance of this class is then dropped
 * into <code>UIDefaults</code>; code can simply call 
 * <code>UIManager.getColor("someColor")</code> and get the right color without
 * being cluttered with the details of deriving colors.
 * 
 * <h2>How it does what it does</h2>
 * The base and prototype are split into HSB color components.  The relationship
 * between the base and prototype values in saturation and brightness is then
 * computed.  This same relationship is then applied to the actual value 
 * <i>as a function of the divergence between the base and actual values</i>
 * such that the more a color diverges, the less the relationship is applied - 
 * so that, if the base color is dark blue and the prototype color is light
 * blue, but the actual color is light yellow, you get light yellow (as opposed
 * to pure white, which a naive application of the relationship would get).
 *
 * <p><strong>Note:</strong> It <strong>is</strong> possible to create cyclic
 * references between RelativeColor instances (for example, a RelativeColor
 * that has its own key as one of the keys it should fetch).  Don't do that.
 *
 */
public class RelativeColor implements UIDefaults.LazyValue {
    private Color value = null;
    private Color fallback = null;
    /** Creates a new instance of RelativeColor.
     *
     * @param base A Color or UIManager key for a color that the target color is related to
     * @param target A Color or UIManager key for a color that is what the target color should be if the 
     *  actual color is equal to the base color
     * @param actual Either a Color object or a UIManager String key resolvable 
     *  to a color, which represents the
     *  actual color, which may or may not match the target color
     * @param mustContrast Either a Color object or a UIManager String key 
     *  resolvable to a color which must contrast sufficiently with the derived
     *  color that text will be readable.  This parameter may be null; the others
     *  may not. */
    public RelativeColor(Object base, Object target, Object actual, Object mustContrast) {
        if (base == null || target == null || actual == null) {
            throw new NullPointerException ("Null argument(s): " + base + ',' 
                + target + ',' + actual + ',' + mustContrast);
        }
        if (base instanceof String) {
            baseColorKey = (String) base;
        } else {
            baseColor = (Color) base;
        }
        if (target instanceof String) {
            targetColorKey = (String) target;
        } else {
            targetColor = (Color) target;
        }
        if (actual instanceof String) {
            actualColorKey = (String) actual;
        } else {
            actualColor = (Color) actual;
        }
        if (mustContrast != null) {
            if (mustContrast instanceof String) {
                mustContrastColorKey = (String) mustContrast;
            } else {
                mustContrastColor = (Color) mustContrast;
            }
        }
    }
    
    /** Creates a new instance of RelativeColor.
     *
     * @param base A Color that the target color is related to
     * @param target A Color that is what the target color should be if the 
     *  actual color is equal to the base color
     * @param actual Either a Color object or a UIManager String key resolvable 
     *  to a color, which represents the
     *  actual color, which may or may not match the target color
     * @param mustContrast Either a Color object or a UIManager String key 
     *  resolvable to a color which must contrast sufficiently with the derived
     *  color that text will be readable
     */
    public RelativeColor(Color base, Color target, Object actual) {
        this (base, target, actual, null);
    }
    
    public void clear() {
        value = null;
        if (actualColorKey != null) {
            actualColor = null;
        }
        if (targetColorKey != null) {
            targetColor = null;
        }
        if (mustContrastColorKey != null) {
            mustContrastColor = null;
        }
        if (baseColorKey != null) {
            baseColor = null;
        }
    }
    
    public Object createValue(UIDefaults table) {
        if (value != null) {
            return value;
        }
        Color actual = getActualColor();
        if( null == actual ) {
            Logger.getLogger(RelativeColor.class.getName()).log(Level.INFO, "'actual' color not available");
            return Color.gray;
        }
        Color base = getBaseColor();
        if( null == base ) {
            Logger.getLogger(RelativeColor.class.getName()).log(Level.INFO, "'base' color not available");
            return Color.gray;
        }
        if (actual.equals(base)) {
            value = getTargetColor();
        } else {
            value = deriveColor (base, actual, getTargetColor());
        }
        if (hasMustContrastColor()) {
            value = ensureContrast(value, getMustContrastColor());
        }
        return value;
    }

    /** Convenience getter, as this class is reasonably useful for creating
     * derived colors without putting them into UIDefaults */
    public Color getColor() {
        return (Color) createValue(null);
    }
    
    private Color targetColor = null;
    private String targetColorKey = null;
    private Color getTargetColor() {
        if (checkState (targetColor, targetColorKey)) {
            targetColor = fetchColor(targetColorKey);
        }
        return targetColor;
    }
    
    private Color baseColor = null;
    private String baseColorKey = null;
    private Color getBaseColor() {
        if (checkState (baseColor, baseColorKey)) {
            baseColor = fetchColor(baseColorKey);
        }
        return baseColor;
    }
    
    private Color mustContrastColor = null;
    private String mustContrastColorKey = null;
    private Color getMustContrastColor() {
        if (checkState (mustContrastColor, mustContrastColorKey)) {
            mustContrastColor = fetchColor(mustContrastColorKey);
        }
        return mustContrastColor;
    }
    
    private Color actualColor = null;
    private String actualColorKey = null;
    private Color getActualColor() {
        if (checkState (actualColor, actualColorKey)) {
            actualColor = fetchColor(actualColorKey);
        }
        return actualColor;
    }
    
    private boolean hasMustContrastColor() {
        return mustContrastColor != null || mustContrastColorKey != null;
    }
    
    /** Ensures that the key and color are not null, and returns true if the
     * color needs to be loaded. */
    private boolean checkState(Color color, String key) {
        if (color == null && key == null) {
            throw new NullPointerException("Both color and key are null for " + 
                this);
        }
        return color == null;
    }
    
    private Color fetchColor(String key) {
        //Todo - check for cyclic references
        Color result = UIManager.getColor(key);
        if (result == null) {
            result = fallback;
        }
        return result;
    }
    
    /** Does the actual leg-work of deriving the color */
    static Color deriveColor (Color base, Color actual, Color target) {
        float[] baseHSB = Color.RGBtoHSB(base.getRed(), base.getGreen(), 
            base.getBlue(), null);
        
        float[] targHSB = Color.RGBtoHSB(target.getRed(), target.getGreen(), 
            target.getBlue(), null);
        
        float[] actualHSB = Color.RGBtoHSB(actual.getRed(), actual.getGreen(), 
            actual.getBlue(), null);
        
        float[] resultHSB = new float[3];
        float[] finalHSB = new float[3];
        
        float[] diff = percentageDiff (actualHSB, baseHSB);
        
        resultHSB[0] = actualHSB[0] + (diff[0] * (targHSB[0] - baseHSB[0]));
        resultHSB[1] = actualHSB[1] + (diff[1] * (targHSB[1] - baseHSB[1]));
        resultHSB[2] = actualHSB[2] + (diff[2] * (targHSB[2] - baseHSB[2]));
        
        finalHSB[0] = saturate (resultHSB[0]);
        finalHSB[1] = saturate (resultHSB[1]);
        finalHSB[2] = saturate (resultHSB[2]);
        
        //If the target had *some* color, so should our result - if it pretty
        //much doesn't, redistribute some of the brightness to the saturation value
        if (targHSB[1] > 0.1 && resultHSB[1] <= 0.1) {
            resultHSB[1] = resultHSB[2] * 0.25f;
            resultHSB[2] = resultHSB[2] - (resultHSB[2] * 0.25f);
        }

        Color result = new Color (Color.HSBtoRGB(finalHSB[0], finalHSB[1], finalHSB[2]));
        return result;
    }
    
    private static float[] percentageDiff (float[] a, float[] b) {
        float[] result = new float[3];
        for (int i=0; i < 3; i++) {
            result[i] = 1 - Math.abs(a[i] - b[i]);
            if (result[i] == 0) {
                result[i] = 1- a[i];
            }
        }
        return result;
    }
    
    private static final void out (String nm, float[] f) {
        //XXX for debugging - deleteme
        StringBuffer sb = new StringBuffer(nm);
        sb.append(": ");
        for (int i=0; i < f.length; i++) {
            sb.append (Math.round(f[i] * 100));
            if (i != f.length-1) {
                sb.append(',');
                sb.append(' ');
            }
        }
        System.err.println(sb.toString());
    }
    
    /** Saturate a float value, clamping values below 0 to 0 and above 1 to 1 */
    private static float saturate (float f) {
        return Math.max(0, Math.min(1, f));
    }


    static Color ensureContrast (Color target, Color contrast) {
        //XXX - this needs some work.  What it should really do:
        //Determine the distance from 0.5 for brightness and saturation of the contrasting color, to 
        //determine the direction in which to adjust.  Then adjust in that 
        //direction as a function of the diff between 0.25 and 0.5 of the
        //diff between the colors...or something like that.  The point is
        //there's a danger zone around 0.5 where things that should be 
        //adjusted away from each other aren't being
        
        float[] contHSB = Color.RGBtoHSB(contrast.getRed(), contrast.getGreen(), 
            contrast.getBlue(), null);
        
        float[] targHSB = Color.RGBtoHSB(target.getRed(), target.getGreen(), 
            target.getBlue(), null);
        
        float[] resultHSB = new float[3];
        System.arraycopy(targHSB, 0, resultHSB, 0, 3);

        float satDiff = Math.abs (targHSB[1] - contHSB[1]);
        float briDiff = Math.abs (targHSB[2] - contHSB[2]);

        if (targHSB[1] > 0.6 && resultHSB[1] > 0.6 || (briDiff < 0.45f && satDiff < 0.4f)) {
            resultHSB[1] /= 3;
//            System.err.println("adjusting saturation to " + resultHSB[1] + " from " + targHSB[1]);
            satDiff = Math.abs (targHSB[1] - contHSB[1]);
        }
        
        if (briDiff < 0.3 || (satDiff < 0.3 && briDiff < 0.5)) {
            float dir = 1.5f * (0.5f - contHSB[2]);
            resultHSB[2] = saturate (resultHSB[2] + dir);
//            System.err.println("adjusting brightness to " + resultHSB[2] + " from " + targHSB[2]);
        }
        
        Color result = new Color (Color.HSBtoRGB(resultHSB[0], resultHSB[1], resultHSB[2]));
        return result;
    }
}
