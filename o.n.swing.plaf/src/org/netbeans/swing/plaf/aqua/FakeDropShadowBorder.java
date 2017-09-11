/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
