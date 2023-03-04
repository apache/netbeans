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
package org.netbeans.swing.plaf.gtk;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * A matte border with a few twists - can do drop shadow; also, for toolbars,
 * will check the position of the component and return taller insets for the
 * top row, so it's offset from the menu, but not present a doubled border
 * for lower rows; same thing for toolbars adjacent to each other.
 *
 * @author  Tim Boudreau
 */
public class AdaptiveMatteBorder implements Border {
    private Insets insets;
    private int shadowDepth;
    private boolean topLeftInsets;
    
    /** Creates a new instance of AdaptiveMatteBorder */
    public AdaptiveMatteBorder(boolean t, boolean l, boolean b, boolean r, int shadowDepth, boolean topLeftInsets) {
        insets = new Insets (t ? topLeftInsets ? shadowDepth + 1 : 1 : 0, l ? topLeftInsets ? shadowDepth + 1: 1 : 0, b ? 1 + shadowDepth : shadowDepth, r ? 1 + shadowDepth : shadowDepth);
        this.shadowDepth = shadowDepth;
        this.topLeftInsets = topLeftInsets;
    }
    
    public AdaptiveMatteBorder(boolean t, boolean l, boolean b, boolean r, int shadowDepth) {
        this (t, l, b, r, shadowDepth, false);
    }
    
    private Insets maybeOmitInsets (Insets ins, Component c) {
        if (shadowDepth <= 0 || !topLeftInsets) {
            return ins;
        }
        Insets result = new Insets(ins.top, ins.left, ins.right, ins.bottom);
        if (topLeftInsets) {
            Point p = c.getLocation();
            if (p.x > 10) {
                result.left = 1;
            }
            if (p.y > 10) {
                result.top = 1;
            }
        }
        return result;
    }
    
    public Insets getBorderInsets(Component c) {
        return maybeOmitInsets(insets, c);
    }
    
    public boolean isBorderOpaque() {
        return false;
    }

    
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Color color = g.getColor();
        Insets ins = getBorderInsets(c);
        Point p = c.getLocation();
        
        //This will always really come from the theme on GTK
        g.setColor (UIManager.getColor("controlShadow"));  //NOI18N
        w -= shadowDepth;
        h -= shadowDepth;
        if (topLeftInsets) {
            if (p.y <= 10) {
                y += shadowDepth;
                h -= shadowDepth;
            }
            if (p.x <= 10) {
                x += shadowDepth;
                w -= shadowDepth;
            }
        }
        if (ins.top > 0) {
            g.fillRect(x, y, w, 1);
        }
        if (ins.left > 0) {
            g.fillRect(x, y, 1, h);
        }
        if (ins.right > 0) {
            g.fillRect(x + w - 1, y, 1, h);
        }
        if (ins.bottom > 0) {
            g.fillRect(x, y + h - 1, w, 1);
        }
        
        boolean isViewTab = isViewTab(c);
        
        if (shadowDepth > 1) {
            Rectangle clip = g.getClipBounds();
            boolean clipTouchesRight = ((clip.x + clip.width) >= (x + w));
            boolean clipTouchesBottom = ((clip.y + clip.height) >= (y + h));

            if (clipTouchesBottom || clipTouchesRight) {
                Color ctrl = UIManager.getColor ("control"); //NOI18N
                Color base = UIManager.getColor("controlShadow");
            
                Color curr;
                for (int i = 1; i < shadowDepth; i++) {
                    curr = colorTowards (base, ctrl, shadowDepth, i+1);
                    g.setColor (curr);
                    if (clipTouchesRight && ins.right > 0) {
                        g.fillRect(x + w - 1 + i, y + (isViewTab ? 0 : i), 1, h);
                    }
                    if (clipTouchesBottom && ins.bottom > 0) {
                        g.fillRect(x + i, y + h - 1 + i, w - 1, 1);
                    }
                }
            }
        }
        g.setColor (color);
    }
    
//    private static int[] xpoints = new int[4];
//    private static int[] ypoints = new int[4];
    
    static boolean isViewTab (Component c) {
        if (c.getParent() instanceof JComponent) {
            JComponent jc = (JComponent) c.getParent();
            Object o = jc.getClientProperty("viewType");
            if (o instanceof Integer) {
                return ((Integer) o).intValue() == 0;
            }
        }
        return false;
    }

    private static final float[] comps = new float[4];
    private static final float[] targs = new float[4];
    
    static final Color colorTowards (Color base, Color target, float steps, float step) {
        base.getColorComponents(comps);
        target.getColorComponents(targs);
        
        comps[3] = 1.0f; //No transparency, performance problems
        
        float factor = (step / steps);
        
        for (int i=0; i < 3; i++) {
            comps[i] = saturate(comps[i] - (factor * (comps[i] - targs[i])));
        }
        
        
//        comps[3] = 1f - (step / steps);
        Color result = new Color (comps[0], comps[1], comps[2], comps[3]);
        return result;
    }
    
    private static final float saturate (float f) {
        float orig = f;
        if (f > 1) {
            f = 1;
        }
        if (f < 0) {
            f = 0;
        }
        return f;
    }
    
}

