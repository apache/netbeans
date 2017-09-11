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


package org.netbeans.modules.form.fakepeer;

import java.awt.*;

/**
 *
 * @author Tomas Pavek
 */

class FakePeerUtils
{
    static void drawButton(Graphics g,int x,int y,int w,int h) {
        g.fillRect(x,y,w,h);

        // button has a raised border (Windows)
        g.setColor(SystemColor.controlHighlight);
        g.drawLine(x,y+h-2,x,y);
        g.drawLine(x,y,x+w-2,y);
        g.setColor(SystemColor.controlDkShadow);
        g.drawLine(x,y+h-1,x+w-1,y+h-1);
        g.drawLine(x+w-1,y+h-1,x+w-1,y);
        if (w >=4 && h >= 4) {
            g.setColor(SystemColor.controlLtHighlight);
            g.drawLine(x+1,y+h-3,x+1,y+1);
            g.drawLine(x+1,y+1,x+w-3,y+1);
            g.setColor(SystemColor.controlShadow);
            g.drawLine(x+1,y+h-2,x+w-2,y+h-2);
            g.drawLine(x+w-2,y+h-2,x+w-2,y+1);
        }
    }

    static void drawArrowButton(Graphics g,
                                int x, int y, int w, int h,
                                int type, boolean enabled) {
        g.setColor(SystemColor.control);
        drawButton(g,x,y,w,h);

        int minWH = w < h ? w : h,
            size; // size of the arrow - from 0 to 4
        if (minWH >= ABUT_SIZE) size = 4;
        else if (minWH >= 12) size = 3;
        else if (minWH >= 8) size = 2;
        else if (minWH >= 6) size = 1;
        else size = 0;

        if (enabled)
            g.setColor(SystemColor.controlText);
        else {
            g.setColor(SystemColor.controlLtHighlight);
            x++;
            y++;
        }

        // draw the arrow
        while (true) {
            if (type == 1) { // left <
                int ax = x+w/2-size/2,
                    ay = y+h/2-1;
                for (int i=0; i < size; i++)
                    g.drawLine(ax+i,ay-i,ax+i,ay+i);
            } else if (type == 2) { // right >
                int ax = x+w/2+size/2,
                    ay = y+h/2-1;
                for (int i=0; i < size; i++)
                    g.drawLine(ax-i,ay-i,ax-i,ay+i);
            } else if (type == 3) { // upper ^
                int ax = x+w/2-1,
                    ay = y+h/2-size/2;
                for (int i=0; i < size; i++)
                    g.drawLine(ax-i,ay+i,ax+i,ay+i);
            } else if (type == 4) { // lower v
                int ax = x+w/2-1,
                    ay = y+h/2+size/2;
                for (int i=0; i < size; i++)
                    g.drawLine(ax-i,ay-i,ax+i,ay-i);
            }

            if (enabled) break;
            else {
                enabled = true;
                g.setColor(SystemColor.controlShadow);
                x--;
                y--;
            }
        }
    }

    static void drawChoiceButton(Graphics g,int x,int y,int w,int h,boolean enabled) {
        // Windows-like style - a button with an arrow
        drawArrowButton(g,x,y,w,h,4,enabled);
    }

    static void drawScrollThumb(Graphics g,int x,int y,int w,int h) {
        // Windows-like style - thumb looks just like a button
        drawButton(g,x,y,w,h);
    }

    static void drawLoweredBox(Graphics g,int x,int y,int w,int h) {
        g.fillRect(x,y,w,h);

        g.setColor(SystemColor.controlShadow);
        g.drawLine(x,y+h-2,x,y);
        g.drawLine(x,y,x+w-2,y);
        g.setColor(SystemColor.controlLtHighlight);
        g.drawLine(x,y+h-1,x+w-1,y+h-1);
        g.drawLine(x+w-1,y+h-1,x+w-1,y);
        if (w >=4 && h >= 4) {
            g.setColor(SystemColor.controlDkShadow);
            g.drawLine(x+1,y+h-3,x+1,y+1);
            g.drawLine(x+1,y+1,x+w-3,y+1);
            g.setColor(SystemColor.controlHighlight);
            g.drawLine(x+1,y+h-2,x+w-2,y+h-2);
            g.drawLine(x+w-2,y+h-2,x+w-2,y+1);
        }
    }

    static void drawScrollbar(Graphics g,
                              int x,int y,int w,int h,
                              int orientation,
                              boolean enabled,
                              boolean border,int relValue,int amount,int range) {
        g.fillRect(x, y, w, h); // color (for background) is expected to be set outside

        if (border) { // border (Windows style)
            g.setColor(SystemColor.controlShadow);
            g.drawRect(x,y,w-1,h-1);
            g.setColor(SystemColor.control);
            g.drawRect(x+1,y+1,w-3,h-3);
        } else 
            g.setColor(SystemColor.control);

        if (orientation == Scrollbar.HORIZONTAL) {
            int butW;
            if (w >= 2*SCROLL_W) {
                butW = SCROLL_W;
                int wFT = w - 2*butW; // width that remains for the "thumb"
                if (wFT >= 4 && enabled) { // paint the thumb
                    int thumbW = range > 0 ? wFT * amount / range : wFT;
                    if (thumbW < 6) thumbW = 6;
                    if (thumbW > wFT) thumbW = wFT;
                    range -= relValue;
                    int thumbX = (range > 0 ? relValue * (wFT - thumbW) / range : 0) + x + butW;

                    drawScrollThumb(g,thumbX,y,thumbW,h);
                }
            } else butW = w/2;
            if (butW >= 4) { // paint "arrow" buttons
                drawArrowButton(g,x,y,butW,h,1,enabled); // the left one <
                drawArrowButton(g,x+w-butW,y,butW,h,2,enabled); // the right one >
            }
        } else { // == Scrollbar.VERTICAL
            int butH;
            if (h >= 2*SCROLL_H) {
                butH = SCROLL_H;
                int hFT = h - 2*butH; // height that remains for the "thumb"
                if (hFT >= 4 && enabled) { // paint the thumb
                    int thumbH = range > 0 ? hFT * amount / range : hFT;
                    if (thumbH < 6) thumbH = 6;
                    if (thumbH > hFT) thumbH = hFT;
                    range -= relValue;
                    int thumbY = (range > 0 ? relValue * (hFT - thumbH) / range : 0) + y + butH;
                        
                    drawScrollThumb(g,x,thumbY,w,thumbH);
                }
            } else butH = h/2;
            if (butH >= 4) { // paint "arrow" buttons
                drawArrowButton(g,x,y,w,butH,3,enabled); // the upper one ^
                drawArrowButton(g,x,y+h-butH,w,butH,4,enabled); // the lower one v
            }
        }
    }

    private static int ABUT_SIZE = 16; // standard arrow button's width & height
    static final int SCROLL_W = 16, SCROLL_H = 16;
}
