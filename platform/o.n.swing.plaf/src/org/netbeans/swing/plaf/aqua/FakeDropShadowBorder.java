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

package org.netbeans.swing.plaf.aqua;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.border.Border;

/**
 * Replacement for original DropShadowBorder - uses a set of backing bitmaps
 * to draw shadows instead of allocating a huge raster.
 *
 * @author Tim Boudreau
 */
public class FakeDropShadowBorder implements Border {

    private static final int TOP = 8;
    private static final int BOTTOM = 8;
    private static final int LEFT = 8;
    private static final int RIGHT = 8;

    private final Insets insets;

    private FakeDropShadowBorder( Insets insets ) {
        this.insets = insets;
    }

    public static Border createDefault() {
        return new FakeDropShadowBorder(new Insets(TOP, LEFT, BOTTOM, RIGHT));
    }

    public static Border createLeftBorder() {
        return new FakeDropShadowBorder(new Insets(0, 0, BOTTOM, RIGHT));
    }

    public static Border createRightBorder() {
        return new FakeDropShadowBorder(new Insets(0, LEFT, BOTTOM, 0));
    }

    public static Border createBottomBorder() {
        return new FakeDropShadowBorder(new Insets(TOP, LEFT, 0, RIGHT));
    }

    public static Border createTopBorder() {
        return new FakeDropShadowBorder(new Insets(0, LEFT, BOTTOM, RIGHT));
    }
    
    public Insets getBorderInsets(Component c) {
        return new Insets(insets.top, insets.left, insets.bottom, insets.right);
    }
   
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Graphics2D gg = (Graphics2D) g;
        //Tile the shadow pngs around the shape
        BufferedImage b = null;
        if( insets.top > 0 && insets.left > 0 ) {
            b = getImage(upLeft);
            gg.drawImage(b, x, y, null);
        }

        if( insets.right > 0 && insets.bottom > 0 ) {
            b = getImage(downRight);
            int xPos = x + w - b.getWidth();
            if( insets.right == 0 )
                xPos += RIGHT;
            gg.drawImage(b, xPos, y + h - b.getHeight(), null);
        }

        if( insets.top > 0 && insets.right > 0 ) {
            b = getImage(upRight);
            gg.drawImage( b, x + w - b.getWidth(), y, null);
        }

        if( insets.left > 0 && insets.bottom > 0 ) {
            b = getImage(downLeft);
            if( insets.left == 0 )
                x -= LEFT;
            gg.drawImage( b, x, y + h - b.getHeight(), null);
        }
        
        if( insets.left > 0 ) {
            b = getImage (leftEdge);
            gg.drawImage(b, x, y+insets.top, b.getWidth(), h-insets.top-insets.bottom, null);
        }

        if( insets.right > 0 ) {
            b = getImage (rightEdge);
            gg.drawImage(b, x + w - (b.getWidth()), y+insets.top, b.getWidth(), h-insets.top-insets.bottom, null);
        }

        if( insets.bottom > 0 ) {
            b = getImage (bottom);
            gg.drawImage(b, x+insets.left, y + h - b.getHeight(), x+w-insets.left-insets.right, b.getHeight(), null);
        }

        if( insets.top > 0 ) {
            b = getImage (top);
            gg.drawImage(b, x+insets.left, y, x+w-insets.left-insets.right, b.getHeight(), null);
        }
    }   
    
    public boolean isBorderOpaque() {
        return false;
    }
    
    private static final String upLeft = "border_top_left.png"; //NOI18N
    private static final String downRight = "border_bottom_right.png"; //NOI18N
    private static final String downLeft = "border_bottom_left.png"; //NOI18N
    private static final String upRight = "border_top_right.png"; //NOI18N
    private static final String bottom = "border_bottom.png"; //NOI18N
    private static final String leftEdge = "border_left.png"; //NOI18N
    private static final String rightEdge = "border_right.png"; //NOI18N
    private static final String top = "border_top.png";
    
    //Only one instance in VM, so perfectly safe to use instance cache - won't
    //be populated unless used
    private static Map<String, BufferedImage> imgs = new HashMap<String, BufferedImage>();
    private static BufferedImage getImage(String s) {
        BufferedImage result = imgs.get(s);
        if (result == null) {
            Exception e1 = null;
            try {
                result = ImageIO.read(
                        FakeDropShadowBorder.class.getResourceAsStream(s));
            } catch (Exception e) {
                result = new BufferedImage (1, 1, BufferedImage.TYPE_INT_ARGB);
                e1 = e;
            }
            imgs.put (s, result);
            if (e1 != null) {
                throw new IllegalStateException (e1);
            }
        }
        return result;
    }
}
