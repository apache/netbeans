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

package org.netbeans.swing.tabcontrol.plaf;


import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for manipulating colors, caching gradient paint objects, creating a
 * bitmap cache of the metal bumps texture and other generally useful stuff.
 *
 * @author Dafe Simonek, Tim Boudreau
 */
final class ColorUtil {
    private static Map<Integer, GradientPaint> gpCache = null;
    private static Map<RenderingHints.Key, Object> hintsMap = null;
    private static final boolean noGpCache = Boolean.getBoolean(
            "netbeans.winsys.nogpcache");  //NOI18N
    private static final boolean noAntialias = 
        Boolean.getBoolean("nb.no.antialias"); //NOI18N
    
    //Values for checking if we should flush the cache bitmap
    private static int focusedHeight = -1;
    private static int unfocusedHeight = -1;
    //Some handy ImageIcons for drawing the cache bitmap
    private static Icon unfocused = null;
    private static Icon focused = null;
    //A scratch rectangle for computing clip intersections
    private static Rectangle scratch = new Rectangle();
    //The width for the backing cache bitmap for the drag texture.
    //May want to profile and tune this a bit for optimum between memory
    //use and painting time
    private static final int DEFAULT_IMAGE_WIDTH = 200;
    /**
     * Constants for allowed texture types, to be passed to drag texture painter
     * methods
     */
    public static final int SEL_TYPE = 1;
    public static final int UNSEL_TYPE = 2;
    public static final int FOCUS_TYPE = 4;
    /**
     * constants for types of tab headers
     */
    public static final int XP_REGULAR_TAB = 0;
    public static final int XP_HIGHLIGHTED_TAB = 1;
    /**
     * constants for gradient borders
     */
    public static final int XP_BORDER_RIGHT = 1;
    public static final int XP_BORDER_BOTTOM = 2;
    /**
     * holds icon of XP style tab drag texture
     */
    private static Icon XP_DRAG_IMAGE;
    /**
     * holds icon of Vista style tab drag texture
     */
    private static Icon VISTA_DRAG_IMAGE;

    /**
     * Utility class, no instances should be created.
     */
    private ColorUtil() {
    }

    /**
     * Computes "middle" color in terms of rgb color space. Ignores alpha
     * (transparency) channel
     */
    public static Color getMiddle(Color c1, Color c2) {
        return new Color((c1.getRed() + c2.getRed()) / 2,
                         (c1.getGreen() + c2.getGreen()) / 2,
                         (c1.getBlue() + c2.getBlue()) / 2);
    }


    public static GradientPaint getGradientPaint(float x1, float y1,
                                                 Color upper, float x2,
                                                 float y2, Color lower) {
        return getGradientPaint(x1, y1, upper, x2, y2, lower, false);
    }

    /**
     * GradientPaint creation is somewhat expensive.  This method keeps cached
     * GradientPaint instances, and normalizes the resulting GradientPaint for
     * horizontal and vertical cases.  Note that this depends entirely on the
     * hashing algorithm for accuracy - there are hypothetical situations 
     * where the return value could be wrong, though none have as yet been 
     * encountered, and given that the number of gradient heights and widths
     * actually used in any UI are very small, such a situation is highly 
     * unlikely.
     */
    public static GradientPaint getGradientPaint(float x1, float y1,
                                                 Color upper, float x2,
                                                 float y2, Color lower,
                                                 boolean repeats) {
        if (noGpCache) {
            return new GradientPaint(x1, y1, upper, x2, y2, lower, repeats);
        }
        
        //Only for test runs with disabled customizations
        if (upper == null) {
            upper = Color.BLUE;
        }
        
        if (lower == null) {
            lower = Color.ORANGE;
        }
        
        if (gpCache == null) {
            gpCache = new HashMap<Integer, GradientPaint>(20);
        }
        //Normalize any non-repeating gradients
        boolean horizontal = x1 == x2;
        boolean vertical = y1 == y2;
        if (horizontal && vertical) {
            //Hack: gradient paint w/ 2 matching points causes endless loop 
            //in native code on mac os, so pick a random number to make sure
            //that can't happen
            y1 = x1 + 28; 
        } else if (horizontal && !repeats) {
            x1 = 0;
            x2 = 0;
        } else if (vertical && !repeats) {
            y1 = 0;
            y2 = 0;
        }
        //TODO: Normalize non-planar repeating gp's by vector/relative location
        
        //Generate a hash code for looking up an existing paint
        long bits = Double.doubleToLongBits(x1)
                + Double.doubleToLongBits(y1) * 37 + Double.doubleToLongBits(
                        x2) * 43 + Double.doubleToLongBits(y2) * 47;
        int hash = ((((int) bits) ^ ((int) (bits >> 32)))
                ^ upper.hashCode() ^ (lower.hashCode() * 17)) * (repeats ? 31 : 1);

        Integer key = new Integer(hash);
        GradientPaint result = (GradientPaint) gpCache.get(key);
        if (result == null) {
            result =
                    new GradientPaint(x1, y1, upper, x2, y2, lower, repeats);
            if (gpCache.size() > 40) {
                gpCache.clear();
            }
            gpCache.put(key, result);
        }
        return result;
    }

    private static Map getHints() {
        if (hintsMap == null) {
            //Thanks to Phil Race for making this possible
            hintsMap = (Map<RenderingHints.Key, Object>)(Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints")); //NOI18N
            if (hintsMap == null) {
                hintsMap = new HashMap<RenderingHints.Key, Object>();
                if (shouldAntialias()) {
                    hintsMap.put(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
            }
            if (shouldAntialias() && (hintsMap == null || !hintsMap.containsKey(RenderingHints.KEY_TEXT_ANTIALIASING))) {
                hintsMap.put(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_ON);
            }
        }
        return hintsMap;
        
    }

    public static final void setupAntialiasing(Graphics g) {
        if (noAntialias) return;
        
        ((Graphics2D) g).addRenderingHints(getHints());
    }
    
    private static final boolean antialias = Boolean.getBoolean(
        "nb.cellrenderer.antialiasing") || //NOI18N
        ("GTK".equals(UIManager.getLookAndFeel().getID()) && //NOI18N
        gtkShouldAntialias()) || 
        Boolean.getBoolean ("swing.aatext") || //NOI18N
        "Aqua".equals(UIManager.getLookAndFeel().getID()); 
    
    public static final boolean shouldAntialias() {
        return antialias;
    }
    
    private static final boolean gtkShouldAntialias() {
        Object o = Toolkit.getDefaultToolkit().getDesktopProperty("gnome.Xft/Antialias"); //NOI18N
        return new Integer(1).equals(o);
    }

    //**************Some static utility methods for color manipulation**********
    public static boolean isBrighter(Color a, Color b) {
        int[] ac = new int[]{a.getRed(), a.getGreen(), a.getBlue()};
        int[] bc = new int[]{b.getRed(), b.getGreen(), b.getBlue()};
        int dif = 0;

        for (int i = 0; i < 3; i++) {
            int currDif = ac[i] - bc[i];
            if (Math.abs(currDif) > Math.abs(dif)) {
                dif = currDif;
            }
        }
        return dif > 0;
    }

    private static int minMax(int i) {
        if (i < 0) {
            return 0;
        } else if (i > 255) {
            return 255;
        } else {
            return i;
        }
    }

    public static int averageDifference(Color a, Color b) {
        int[] ac = new int[]{a.getRed(), a.getGreen(), a.getBlue()};
        int[] bc = new int[]{b.getRed(), b.getGreen(), b.getBlue()};
        int dif = 0;
        for (int i = 0; i < 3; i++) {
            dif += bc[i] - ac[i];
        }
        return dif / 3;
    }

    public static Color adjustComponentsTowards(Color toAdjust, Color towards) {
        int r = toAdjust.getRed();
        int g = toAdjust.getGreen();
        int b = toAdjust.getBlue();

        int ra = towards.getRed();
        int ga = towards.getGreen();
        int ba = towards.getBlue();

        r += minMax((ra - r) / 3);
        g += minMax((ga - g) / 3);
        b += minMax((ba - b) / 3);

        return new Color(r, g, b);
    }

    public static Color adjustTowards(Color toAdjust, int amount,
                                      Color towards) {
        int r = toAdjust.getRed();
        int g = toAdjust.getGreen();
        int b = toAdjust.getBlue();
        int factor = isBrighter(towards, toAdjust) ? 1 : -1;
        r = minMax(r + (factor * amount));
        g = minMax(g + (factor * amount));
        b = minMax(b + (factor * amount));
        return new Color(r, g, b);
    }

    public static Color adjustBy(Color toAdjust, int amount) {
        int r = minMax(toAdjust.getRed() + amount);
        int g = minMax(toAdjust.getGreen() + amount);
        int b = minMax(toAdjust.getBlue() + amount);
        return new Color(r, g, b);
    }

    public static Color adjustBy(Color toAdjust, int[] amounts) {
        int r = minMax(toAdjust.getRed() + amounts[0]);
        int g = minMax(toAdjust.getGreen() + amounts[1]);
        int b = minMax(toAdjust.getBlue() + amounts[2]);
        return new Color(r, g, b);
    }

    /**
     * Rotates a float value around 0-1
     */
    private static float minMax(float f) {
        return Math.max(0, Math.min(1, f));
    }

    /**
     * Draws drag texture of given dimensions. Texture is appropriate for Metal
     * like view tabs
     */
    public static void paintViewTabBump(Graphics g, int x, int y, int width,
                                        int height, int type) {
        drawTexture(g, x, y, width, height, type, 0);
    }

    /**
     * Draws drag texture of given dimensions. Texture is appropriate for Metal
     * like document tabs
     */
    public static void paintDocTabBump(Graphics g, int x, int y, int width,
                                       int height, int type) {
        // decline set to 2, so that bump matches the spec
        drawTexture(g, x, y, width, height, type, 2);
    }

    /**
     * Actually draws the texture. yDecline parameter is initial y-coordination
     * decline in pixels, effective values are <0,3>, will change the "shape" of
     * texture a bit.
     */
    private static void _drawTexture(Graphics g, int x, int y, int width,
                                     int height, int type, int yDecline) {
        Color brightC = UIManager.getColor("TabbedPane.highlight");
        Color darkC;
        if (type == FOCUS_TYPE) {
            darkC = UIManager.getColor("TabbedPane.focus");
        } else {
            darkC = UIManager.getColor("controlDkShadow");
        }
        // assure that last column and row will be dark - make width even
        // and height odd
        if (width % 2 != 0) {
            width--;
        }
        if (height % 2 != 0) {
            height--;
        }
        for (int curX = x; curX < x + width; curX++) {
            g.setColor((curX - x) % 2 == 0 ? brightC : darkC);
            for (int curY = y + ((curX - x + yDecline) % 4); curY < y + height; curY +=
                    4) {
                g.drawLine(curX, curY, curX, curY);
            }
        }
    }

    /**
     * Draws the texture from a backing bitmap, creating it on the first call.
     * Profiling shows that 95% of the main window drawing time is spent
     * painting this texture on the output window border.  So instead, we
     * generate a bitmap on the first call and subsequent calls just blit it to
     * the screen.
     */
    private static void drawTexture(Graphics g, int x, int y, int width,
                                    int height, int type, int yDecline) {


        if (!g.hitClip(x, y, width, height)) {
            return;
        }
        if (type == FOCUS_TYPE) {
            if (focused == null || height > focusedHeight * 2) {
                //Create the focused backing bitmap
                Image img = createBitmap(height, type, yDecline);
                //Store the height in case somebody asks to paint a region
                //larger than our stored bitmap.  Probably will never happen,
                //but it would be difficult to diagnose if it did
                focusedHeight = height;

                focused = new ImageIcon(img);
            }
            blitBitmap(g, focused, x, y, width, height);

        } else {
            if (unfocused == null || unfocusedHeight > height * 2) {
                //create the unfocused backing bitmap
                Image img = createBitmap(height, type, yDecline);
                //Store the height in case somebody asks to paint a region
                //larger than our stored bitmap.  Probably will never happen,
                //but it would be difficult to diagnose if it did
                unfocusedHeight = height;
                unfocused = new ImageIcon(img);
            }
            blitBitmap(g, unfocused, x, y, width, height);
        }
    }

    /**
     * Create a backing bitmap, painting the texture into it with the specified
     * parameters.  The bitmap will be created at 2*height, so that even if
     * there is some minor variation in height, it will not force recreating the
     * bitmap
     */
    private static BufferedImage createBitmap(int height, int type,
                                              int yDecline) {

        //Create an optimal image for blitting to the screen with no format conversion
        BufferedImage result = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(
                        200, height * 2);

        Graphics g = result.getGraphics();

        if (result.getAlphaRaster() == null) {
            Color c = type == FOCUS_TYPE ?
                    MetalViewTabDisplayerUI.getActBgColor() :
                    MetalViewTabDisplayerUI.getInactBgColor();
            g.setColor(c);
            g.fillRect(0, 0, DEFAULT_IMAGE_WIDTH, height * 2);
        }

        //draw the texture into the offscreen image
        _drawTexture(g, 0, 0, DEFAULT_IMAGE_WIDTH, height * 2, type, yDecline);
        return result;
    }

    /**
     * Paint a backing bitmap into the specified rectangle of the passed
     * Graphics object.  Sets the clipping rectangle to match the coordinates
     * and loops until the rectangle has been filled with the texture.
     */
    private static void blitBitmap(Graphics g, Icon icon, int x, int y, int w,
                                   int h) {
        //Store the current clip to reset it after
        Shape clip = g.getClip();

        if (clip == null) {
            //Limit it to the space we're requested to paint
            g.setClip(x, y, w, h);
        } else {
            //If there is an existing clip, get the intersection with the
            //rectangle we want to paint and set that

            scratch.setBounds(x, y, w, h);
            Area area = new Area(clip);
            area.intersect(new Area(scratch));
            g.setClip(area);
        }
        int iwidth = icon.getIconWidth();
        int widthPainted = 0;
        while (widthPainted < w) {
            //Loop until we've covered the entire area
            icon.paintIcon(null, g, x + widthPainted, y);
            widthPainted += iwidth;
        }

        //restore the clip
        g.setClip(clip);
    }

    /**
     * Paints XP style tab highlight on given coordinates and with given width.
     */
    public static void paintXpTabHeader(int type, Graphics g, int x, int y,
                                        int width) {
        Color capBorderC = getXpHeaderColor(type, false);
        Color capFillC = getXpHeaderColor(type, true);
        // paint header "cap" shape
        g.setColor(capBorderC);
        g.drawLine(x + 2, y, x + width - 3, y);
        g.drawLine(x + 2, y, x, y + 2);
        g.drawLine(x + width - 3, y, x + width - 1, y + 2);
        g.setColor(capFillC);
        g.drawLine(x + 2, y + 1, x + width - 3, y + 1);
        g.drawLine(x + 1, y + 2, x + width - 2, y + 2);
        // antialised effect around corners
        // TBD
    }

    /**
     * Gradient fill with left top light direction.
     */
    public static void xpFillRectGradient(Graphics2D g, Rectangle rect,
                                          Color brightC, Color darkC) {
        xpFillRectGradient(g, rect.x, rect.y, rect.width, rect.height, brightC,
                           darkC, XP_BORDER_BOTTOM | XP_BORDER_RIGHT);
    }

    /**
     * Fills given rectangle in gradient style from bright to dark colors, with
     * virtual light shining from left top direction.
     */
    public static void xpFillRectGradient(Graphics2D g, int x, int y,
                                          int width, int height, Color brightC,
                                          Color darkC) {
        xpFillRectGradient(g, x, y, width, height, brightC, darkC,
                           XP_BORDER_BOTTOM | XP_BORDER_RIGHT);
    }

    /**
     * Fills given rectangle in gradient style from bright to dark colors, with
     * optional emphasized borders.
     */
    public static void xpFillRectGradient(Graphics2D g, int x, int y,
                                          int width, int height, Color brightC,
                                          Color darkC, int borderType) {
        paintXpGradientBorder(g, x, y, width, height, darkC, borderType);
        int gradWidth = ((borderType & XP_BORDER_RIGHT) != 0) ?
                width - 2 : width;
        int gradHeight = ((borderType & XP_BORDER_BOTTOM) != 0) ?
                height - 2 : height;
        paintXpGradientFill(g, x, y, gradWidth, gradHeight, brightC, darkC);
    }

    /**
     * Draws drag texture of the tab in specified bounds.
     */
    public static void paintXpTabDragTexture(Component control, Graphics g,
                                             int x, int y, int height) {
        if (XP_DRAG_IMAGE == null) {
            XP_DRAG_IMAGE = initXpDragTextureImage();
        }
        int count = height / 4;
        int ypos = y;
        for (int i = 0; i < count; i++) {
            XP_DRAG_IMAGE.paintIcon(control, g, x, ypos);
            ypos += 4;
        }
    }

    /**
     * Fills given the upper and lower halves of the given rectangle 
     * in gradient style from bright to dark colors.
     */
    public static void vistaFillRectGradient(Graphics2D g, Rectangle rect,
                                          Color brightUpperC, Color darkUpperC,
                                          Color brightLowerC, Color darkLowerC) {
        vistaFillRectGradient(g, rect.x, rect.y, rect.width, rect.height, 
                brightUpperC, darkUpperC, brightLowerC, darkLowerC);
    }

    /**
     * Fills given the upper and lower halves of the given rectangle 
     * in gradient style from bright to dark colors.
     */
    public static void vistaFillRectGradient(Graphics2D g, int x, int y,
                                          int width, int height, 
                                          Color brightUpperC, Color darkUpperC,
                                          Color brightLowerC, Color darkLowerC) {
        paintVistaGradientFill( g, x, y, width, height/2, 
                brightUpperC, darkUpperC );
        paintVistaGradientFill( g, x, y+height/2, width, height-height/2, 
                brightLowerC, darkLowerC );
    }

    /**
     * Fills given rectangle in gradient style from bright to dark colors,
     * the upper half of the rectangle has a single color fill.
     */
    public static void vistaFillRectGradient(Graphics2D g, Rectangle rect,
                                          Color upperC,
                                          Color brightLowerC, Color darkLowerC) {
        vistaFillRectGradient( g, rect.x, rect.y, rect.width, rect.height, 
                upperC, brightLowerC, darkLowerC );
    }
    /**
     * Fills given rectangle in gradient style from bright to dark colors,
     * the upper half of the rectangle has a single color fill.
     */
    public static void vistaFillRectGradient(Graphics2D g, int x, int y,
                                          int width, int height, 
                                          Color upperC,
                                          Color brightLowerC, Color darkLowerC) {
        g.setColor( upperC );
        g.fillRect( x, y, width, height/2 );
        paintVistaGradientFill( g, x, y+height/2, width, height-height/2, 
                brightLowerC, darkLowerC );
    }
    
    /**
     * Draws drag texture of the tab in specified bounds.
     */
    public static void paintVistaTabDragTexture(Component control, Graphics g,
                                             int x, int y, int height) {
        if (VISTA_DRAG_IMAGE == null) {
            VISTA_DRAG_IMAGE = initVistaDragTextureImage();
        }
        int count = height / 4;
        int ypos = y;
        g.setColor( Color.WHITE );
        for (int i = 0; i < count; i++) {
            VISTA_DRAG_IMAGE.paintIcon(control, g, x, ypos);
            g.drawLine( x+1, ypos+2, x+2, ypos+2 );
            g.drawLine( x+2, ypos+1, x+2, ypos+1 );
            ypos += 4;
        }
    }
    /**
     * Adjusts color by given values, positive values means brightening,
     * negative values darkening of original color.
     *
     * @return adjusted color
     */
    public static Color adjustColor(Color c, int rDiff, int gDiff, int bDiff) {
        if (c == null) {
            c = Color.GRAY;
        }
        int red = Math.max(0, Math.min(255, c.getRed() + rDiff));
        int green = Math.max(0, Math.min(255, c.getGreen() + gDiff));
        int blue = Math.max(0, Math.min(255, c.getBlue() + bDiff));
        return new Color(red, green, blue);
    }

    /**
     * Paints border of given rectangle, which enhances "light shining" effect
     */
    private static void paintXpGradientBorder(Graphics g, int x, int y,
                                              int width, int height,
                                              Color darkC, int borderType) {
        // right and bottom border, darker
        if ((borderType & XP_BORDER_RIGHT) != 0) {
            Color color = adjustColor(darkC, -6, -5, -3);
            g.setColor(color);
            g.drawLine(x + width - 2, y, x + width - 2, y + height - 2);
            color = adjustColor(darkC, -27, -26, -20);
            g.setColor(color);
            g.drawLine(x + width - 1, y, x + width - 1, y + height - 1);
        }
        if ((borderType & XP_BORDER_BOTTOM) != 0) {
            Color color = adjustColor(darkC, -6, -5, -3);
            g.setColor(color);
            g.drawLine(x, y + height - 2, x + width - 2, y + height - 2);
            color = adjustColor(darkC, -27, -26, -20);
            g.setColor(color);
            g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
        }
    }

    /**
     * Fills given rectangle using top-down gradient fill of specified colors
     */
    private static void paintXpGradientFill(Graphics2D g, int x, int y,
                                            int width, int height,
                                            Color brightC, Color darkC) {
        GradientPaint gradient = getGradientPaint(x, y, brightC, x, y + height,
                                                  darkC);
        g.setPaint(gradient);
        g.fillRect(x, y, width, height);
    }

    /**
     * @return Header color of tab depending on tab type
     */
    private static Color getXpHeaderColor(int type, boolean fill) {
        String colorKey = null;
        switch (type) {
            case XP_REGULAR_TAB:
                colorKey = fill ? "tab_unsel_fill_bright" : "tab_border";
                break;
            case XP_HIGHLIGHTED_TAB:
                colorKey = fill ?
                        "tab_highlight_header_fill" : "tab_highlight_header";
                break;
            default:
                throw new IllegalArgumentException(
                        "Unknown type of tab header: " + type);
        }
        return UIManager.getColor(colorKey);
    }

    /**
     * Dynamically creates and returns drag texture icon
     */
    private static final Icon initXpDragTextureImage() {
        BufferedImage i = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
        Color hl = UIManager.getColor("controlLtHighlight"); //NOI18N
        i.setRGB(2, 2, hl.getRGB());
        i.setRGB(2, 1, hl.getRGB());
        i.setRGB(1, 2, hl.getRGB());
        Color dk = UIManager.getColor("TabbedPane.darkShadow"); //NOI18N
        i.setRGB(1, 1, dk.getRGB());
        Color corners = UIManager.getColor("TabbedPane.light"); //NOI18N
        i.setRGB(0, 2, corners.getRGB());
        i.setRGB(2, 0, corners.getRGB());
        Color dk2 = UIManager.getColor("TabbedPane.shadow"); //NOI18N
        i.setRGB(0, 1, dk2.getRGB());
        i.setRGB(1, 0, dk2.getRGB());
        Color up = UIManager.getColor("inactiveCaptionBorder"); //NOI18N
        i.setRGB(0, 0, up.getRGB());
        return new ImageIcon(i);
    }

    /**
     * Fills given rectangle using top-down gradient fill of specified colors
     */
    public static void paintMacGradientFill(Graphics2D g, Rectangle rect,
                                            Color brightC, Color darkC) {
        Paint oldPaint = g.getPaint();

        //#161755 - for some reason UIManager doesn't have to find colors defined in AquaLFCustoms class
        if( null == brightC )
            brightC = Color.gray;
        if( null == darkC )
            darkC = Color.gray;

        g.setPaint( new GradientPaint(rect.x, rect.y, brightC, rect.x, rect.y+rect.height/2, darkC) );
        g.fillRect(rect.x, rect.y, rect.width, rect.height);
        g.setPaint(oldPaint);
    }
    
    /**
     * Fills given rectangle using top-down gradient fill of specified colors
     */
    private static void paintVistaGradientFill(Graphics2D g, int x, int y,
                                            int width, int height,
                                            Color brightC, Color darkC) {
        GradientPaint gradient = getGradientPaint(x, y, brightC, x, y + height,
                                                  darkC);
        g.setPaint(gradient);
        g.fillRect(x, y, width, height);
    }

    /**
     * Dynamically creates and returns drag texture icon
     */
    private static final Icon initVistaDragTextureImage() {
        BufferedImage i = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        int grey = new Color(124,124,124).getRGB();
        i.setRGB(1, 0, grey);
        i.setRGB(0, 1, grey);
        i.setRGB(0, 0, new Color(162,163,164).getRGB());
        i.setRGB(1, 1, new Color(107,107,107).getRGB());
        return new ImageIcon(i);
    }
    
    public boolean isBlueprintTheme() {
            return ("blueprint".equals(//NOI18N
                Toolkit.getDefaultToolkit().getDesktopProperty(
                "gnome.Net/ThemeName"))); //NOI18N
    }
}
