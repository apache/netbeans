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
package org.netbeans.swing.tabcontrol.plaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowListener;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;

/*
 * GenericAquaPaintingThing.java
 *
 * Created on February 16, 2004, 12:17 AM
 */

/**
 * The name pretty much says it all.
 *
 * @author Tim Boudreau
 */
class GenericGlowingChiclet {
    public static final int STATE_PRESSED = 1;
    public static final int STATE_SELECTED = 2;
    public static final int STATE_ACTIVE = 4;
    public static final int STATE_CLOSING = 8;
    public static final int STATE_ATTENTION = 16;

    //Basic apple colors.  Package access so that GtkChiclet can install its own
    //defaults if the GTK classes it proxies for colors are not available or
    //have incompatibly changed
    static Color[] selectedActive = new Color[]{
        new Color(220, 238, 255), new Color(139, 187, 238),
        new Color(90, 143, 229), new Color(190, 247, 255)};

    static Color[] selectedPressedActive = new Color[]{
        selectedActive[0], new Color(50, 150, 229),
        new Color(80, 80, 200), selectedActive[3]};

    static Color[] inactive = new Color[]{
        Color.WHITE, new Color(222, 222, 227), new Color(205, 205, 205),
        new Color(246, 243, 249)};

    static Color[] active = new Color[]{
        Color.WHITE, new Color(222, 222, 227), new Color(205, 205, 205),
        new Color(246, 243, 249)};

    static Color[] selectedInactive = new Color[]{
        new Color(240, 250, 255), new Color(212, 222, 242),
        new Color(180, 190, 200), new Color(230, 230, 255)};

    static Color[] closing = new Color[]{
        new Color(255, 238, 220), new Color(238, 137, 109),
        new Color(255, 50, 50), new Color(255, 237, 40)};

    static Color[] attention = new Color[]{
        new Color(255, 255, 220), new Color(238, 237, 109),
        new Color(255, 255, 50), new Color(255, 237, 40)};
        
    private Color upperTop = selectedActive[0];
    private Color upperBottom = selectedActive[1];
    private Color lowerTop = selectedActive[2];
    private Color lowerBottom = selectedActive[3];

    private Rectangle scratch = new Rectangle();
    private float fupperLeftArc = 0;
    private float fupperRightArc = 0;
    private float flowerLeftArc = 0;
    private float flowerRightArc = 0;
    private int upperLeftArc = 0;
    private int upperRightArc = 0;
    private int lowerLeftArc = 0;
    private int lowerRightArc = 0;
    private boolean usePercentages = false;

    private boolean notchRight = false;
    private boolean notchLeft = false;

    protected boolean changed = false;
    protected int state = STATE_ACTIVE | STATE_SELECTED;

    public static final GenericGlowingChiclet INSTANCE = new GenericGlowingChiclet();

    protected GenericGlowingChiclet() {
        //Could listen for change in look and feel, but probably not worth doing for now -
        //unlikely anyone will ever be able to change between GTK and Aqua L&Fs.  Possibly
        //useful for desktop theme changes on GTK, though.  Support it later if need be.
    }

    public void setColors(Color upperTop, Color upperBottom, Color lowerTop,
                          Color lowerBottom) {
        changed |= !upperTop.equals(this.getUpperTop())
                || !upperBottom.equals(this.getUpperBottom())
                || !lowerTop.equals(this.getLowerTop())
                || !lowerBottom.equals(this.getLowerBottom());
        this.upperTop = upperTop;
        this.upperBottom = upperBottom;
        this.lowerTop = lowerTop;
        this.lowerBottom = lowerBottom;
    }

    public Color[] getColors() {
        return new Color[] {
            getUpperTop(), getUpperBottom(), getLowerTop(), getLowerBottom()
        };
    }

    protected int getState() {
        return state;
    }

    public void setState(int i) {
        changed |= state != i;
        if (state != i) {
            if ((state & STATE_PRESSED) != 0) {
                state |= STATE_ACTIVE;
            }
            state = i;
            Color[] nue;
            if ((state & STATE_CLOSING) != 0) {
                nue = closing;
            } else if ((state & STATE_ATTENTION) != 0) {
                nue = attention;
            } else {
                switch (state) {
                    case STATE_PRESSED | STATE_ACTIVE:
                    case STATE_PRESSED | STATE_ACTIVE | STATE_SELECTED:
                        nue = selectedPressedActive;
                        break;
                    case STATE_ACTIVE | STATE_SELECTED:
                        nue = selectedActive;
                        break;
                    case STATE_SELECTED:
                        nue = selectedInactive;
                        break;
                    case STATE_ACTIVE:
                        nue = active;
                        break;
                    default :
                        nue = inactive;
                }
            }
            upperTop = nue[0];
            upperBottom = nue[1];
            lowerTop = nue[2];
            lowerBottom = nue[3];
        }
    }

    private Rectangle bounds = new Rectangle();

    public void setBounds(int x, int y, int w, int h) {
        changed |= x != bounds.x || y != bounds.y || w != bounds.width
                || h != bounds.height;
        bounds.setBounds(x, y, w, h - 1);
    }

    private boolean allowVertical = false;

    public void setAllowVertical(boolean val) {
        if (val != allowVertical) {
            allowVertical = val;
            changed = true;
        }
    }

    private Rectangle getBounds() {
        scratch.setBounds(bounds);
        return scratch;
    }

    public void setArcs(int upperLeft, int upperRight, int lowerLeft,
                        int lowerRight) {
        changed |= upperLeft != upperLeftArc || upperRight != upperRightArc
                || lowerLeft != lowerLeftArc || lowerRight != lowerRightArc
                || usePercentages;

        upperLeftArc = upperLeft;
        upperRightArc = upperRight;
        lowerLeftArc = lowerLeft;
        lowerRightArc = lowerRight;
        usePercentages = false;
    }

    public void setArcs(float upperLeft, float upperRight, float lowerLeft,
                        float lowerRight) {
        changed |= upperLeft != fupperLeftArc || upperRight != fupperRightArc
                || lowerLeft != flowerLeftArc || lowerRight != flowerRightArc
                || !usePercentages;

        fupperLeftArc = upperLeft;
        fupperRightArc = upperRight;
        flowerLeftArc = lowerLeft;
        flowerRightArc = lowerRight;
        usePercentages = true;
    }

    public void setNotch(boolean right, boolean left) {
        changed |= right != notchRight || left != notchLeft;
        notchRight = right;
        notchLeft = left;
    }

    private int getNotchRightArc() {
        int arc = getUpperRightArc();
        if (arc == 0) {
            arc = bounds.height / 2;
        }
        return arc / 3;
    }

    private int getNotchLeftArc() {
        int arc = getUpperLeftArc();
        if (arc == 0) {
            arc = bounds.height / 2;
        }
        return arc / 3;
    }

    private int getUpperLeftArc() {
        if (!usePercentages) {
            return upperLeftArc;
        } else {
            return Math.round(fupperLeftArc
                              * Math.min(getBounds().height,
                                         getBounds().width));
        }
    }

    private int getUpperRightArc() {
        if (!usePercentages) {
            return upperRightArc;
        } else {
            return Math.round(fupperRightArc
                              * Math.min(getBounds().height,
                                         getBounds().width));
        }
    }

    private int getLowerLeftArc() {
        if (!usePercentages) {
            return lowerLeftArc;
        } else {
            return Math.round(flowerLeftArc
                              * Math.min(getBounds().height,
                                         getBounds().width));
        }
    }

    private int getLowerRightArc() {
        if (!usePercentages) {
            return lowerRightArc;
        } else {
            return Math.round(flowerRightArc
                              * Math.min(getBounds().height,
                                         getBounds().width));
        }
    }

    public void draw(Graphics2D g) {
        if (bounds.width == 0 || bounds.height == 0) {
            return;
        }
        drawInterior(g);
        if (drawOutline) {
            drawOutline(g);
        }
        changed = false;
    }

    private boolean drawOutline = true;
    public void setDrawOutline (boolean b) {
        drawOutline = b;
    }

    private void drawOutline(Graphics2D g) {
        Shape s = getClipShape();
        g.setColor(dark());
        g.setStroke(new BasicStroke(0.95f));
        Rectangle r = getBounds();
        r.height += 1;

        Shape clip = g.getClip();
        if (clip != null) {
            Area a = new Area(clip);
            a.intersect(new Area(r));
            g.setClip(a);
        } else {
            g.setClip(r);
        }

        g.draw(s);
        g.setColor(getOutlineDark());

        r = getBounds();
        g.setStroke(new BasicStroke(0.70f));
	if (getLowerRightArc() != 0) {
	    g.drawLine(Math.max(r.x, r.x + getLowerLeftArc() - 3),
		r.y + r.height - 1, 
		Math.min(r.x + r.width - getLowerRightArc() + 3, r.x + r.width) - 1,
		r.y + r.height- 1);
	} else {
	    g.drawLine(Math.max(r.x, r.x + getLowerLeftArc() - 3),
		r.y + r.height - 1, 
		Math.min(r.x + r.width - getLowerRightArc() + 3, r.x + r.width),
		r.y + r.height- 1);
	}
        g.setClip(clip);
    }
    
    protected Color getOutlineDark() {
        return new Color(50, 50, 50);
    }

    private void drawInterior(Graphics2D g) {
        Shape s = getClipShape();
        Area a = new Area(s);

        Shape clip = g.getClip();
        if (clip != null) {
            a.intersect(new Area(clip));
        }

        Rectangle r;
        if (isVertical()) {
            r = getBounds();
            r.width /= 2;
            a.intersect(new Area(r));
            g.setClip(a);

            g.setPaint(getLeftPaint());
            g.fill(s);

            r = getBounds();
            r.width /= 2;
            r.x += r.width;
            a = new Area(s);
            if (clip != null) {
                a.intersect(new Area(clip));
            }
            a.intersect(new Area(r));
            g.setClip(a);

            g.setPaint(getRightPaint());
            g.fill(s);
        } else {
            //paint the upper gradient into the top half of the shape
            r = getBounds();
            r.height /= 2;
            a.intersect(new Area(r));

            g.setClip(a);

            g.setPaint(getUpperPaint());
            g.fill(s);

            //paint the lower gradient into the bottom half of the shape
            a = new Area(s);
            if (clip != null) {
                a.intersect(new Area(clip));
            }
            r = getBounds();
            r.y += r.height / 2;
            r.height -= r.height / 2;
            a.intersect(new Area(r));

            g.setClip(a);
            g.setPaint(getLowerPaint());
            g.fill(s);
        }


        Composite composite = g.getComposite();
        AlphaComposite comp = AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, 0.8f);
        g.setComposite(comp);


        int arc = getUpperLeftArc();
        r = getBounds();
        r.width = arc;
        r.height = r.height / 2;

        a = new Area(s);
        if (clip != null) {
            a.intersect(new Area(clip));
        }
        a.intersect(new Area(r));
        g.setClip(a);
        g.setPaint(getUpperLeftPaint());
        g.fill(s);

        arc = getUpperRightArc();
        r = getBounds();
        r.x = r.x + r.width - arc;
        r.width = arc;
        r.height = r.height / 2;

        a = new Area(s);
        if (clip != null) {
            a.intersect(new Area(clip));
        }
        a.intersect(new Area(r));
        g.setClip(a);
        g.setPaint(getUpperRightPaint());
        g.fill(s);

        arc = getLowerRightArc();
        r = getBounds();
        r.x = r.x + r.width - arc;
        r.width = arc;
        r.y = r.y + (r.height / 2);
        r.height = r.height / 2;

        a = new Area(s);
        if (clip != null) {
            a.intersect(new Area(clip));
        }
        a.intersect(new Area(r));
        g.setClip(a);
        g.setPaint(getLowerRightPaint());
        g.fill(s);


        arc = getLowerLeftArc();
        r = getBounds();
        r.width = arc;
        r.y = r.y + (r.height / 2);
        r.height = r.height / 2;

        a = new Area(s);
        if (clip != null) {
            a.intersect(new Area(clip));
        }
        a.intersect(new Area(r));
        g.setClip(a);
        g.setPaint(getLowerLeftPaint());
        g.fill(s);


        g.setClip(clip);
        g.setComposite(composite);
    }

    private boolean isVertical() {
        if (!allowVertical) {
            return false;
        } else {
            return bounds.height > bounds.width;
        }
    }

    private GradientPaint getUpperPaint() {
        Rectangle r = getBounds();
        return ColorUtil.getGradientPaint(r.x, r.y + (r.height / 9),
                getUpperTop(), r.x,
                r.y + (r.height / 2), getUpperBottom(),
                true);
    }

    private GradientPaint getLowerPaint() {
        Rectangle r = getBounds();
        return ColorUtil.getGradientPaint(r.x, r.y + (r.height / 2),
                getLowerTop(), r.x, r.y + r.height,
                getLowerBottom(), false);
    }

    private GradientPaint getLeftPaint() {
        Rectangle r = getBounds();
        return ColorUtil.getGradientPaint(r.x, r.y, getUpperTop(),
                                          r.x + (r.width / 2), r.y,
                getUpperBottom());
    }

    private GradientPaint getRightPaint() {
        Rectangle r = getBounds();
        return ColorUtil.getGradientPaint(r.x + (r.width / 2), r.y, getLowerTop(),
                                          r.x + r.width, r.y, getLowerBottom());
    }

    private GradientPaint getUpperLeftPaint() {
        Rectangle r = getBounds();
        int arc = getUpperLeftArc();
        if (!isVertical()) {
            return ColorUtil.getGradientPaint(r.x, r.y + (r.height / 2), dark(),
                r.x + (arc / 2), r.y + (r.height / 2) - arc / 2, light());
        } else {
            return ColorUtil.getGradientPaint(r.x + (r.width / 2), r.y,
              dark(),
              r.x + (r.width / 2) - (arc / 2), r.y + arc,
              light());
        }
    }

    private GradientPaint getUpperRightPaint() {
        Rectangle r = getBounds();
        int arc = getUpperRightArc();
        if (!isVertical()) {
            return ColorUtil.getGradientPaint(r.x + r.width, r.y
                + (r.height / 2),
                dark(),
                r.x + r.width - (arc / 2), r.y + (r.height / 2) - arc / 2,
                light());
        } else {
            return ColorUtil.getGradientPaint(r.x + (r.width / 2), r.y,
                dark(), r.x + (r.width / 2) + (arc / 2), r.y + arc, light());
        }
    }

    private GradientPaint getLowerRightPaint() {
        Rectangle r = getBounds();
        int arc = getLowerRightArc();
        if (!isVertical()) {
            return ColorUtil.getGradientPaint(r.x + r.width, r.y
                + (r.height / 2), dark(), r.x + r.width - (arc / 2),
                r.y + (r.height / 2) + (arc / 2), light());
        } else {
            return ColorUtil.getGradientPaint(r.x + (r.width / 2),
              r.y + r.height, dark(), r.x + (r.width / 2) + (arc / 2),
              r.y + r.height - arc, light());
        }
    }

    private GradientPaint getLowerLeftPaint() {
        Rectangle r = getBounds();
        int arc = getLowerLeftArc();
        if (!isVertical()) {
            return ColorUtil.getGradientPaint(r.x, r.y + (r.height / 2), dark(),
                r.x + (arc / 2), r.y + (r.height / 2) + (arc / 2), light());
        } else {
            return ColorUtil.getGradientPaint(r.x + (r.width / 2),
                r.y + r.height, dark(), r.x + (r.width / 2) - (arc / 2),
                r.y + r.height - arc, light());
        }
    }

    private Shape clip = null;

    private Shape getClipShape() {
        if (changed)
            update();
        if (clip == null) {
            clip = createClip();
        }
        return clip;
    }

    protected Color dark() {
        if ((getState() & STATE_SELECTED) != 0 && (getState() & STATE_ACTIVE) != 0) {
            return new Color(80, 80, 150);
        } else {
            return new Color(130, 130, 150);
        }
    }

    private Color light() {
        Color dark = dark();
        return new Color(dark.getRed(), dark.getGreen(), dark.getBlue(), 0);
    }

    private void update() {
        clip = null;
    }

    private Shape createClip() {
        Rectangle bds = getBounds();
        if (!notchLeft && !notchRight && !usePercentages
                && upperRightArc == lowerRightArc
                && lowerRightArc == lowerLeftArc
                && lowerLeftArc == upperLeftArc && upperLeftArc == 0) {
            return new Rectangle(getBounds());
        }
        int upperRightArc = getUpperRightArc();
        int lowerRightArc = getLowerRightArc();
        int upperLeftArc = getUpperLeftArc();
        int lowerLeftArc = getLowerLeftArc();
        int notchR = getNotchRightArc();
        int notchL = getNotchLeftArc();

        GeneralPath gp = new GeneralPath();
        if (notchLeft) {
            gp.moveTo(bds.x + notchL, bds.y + (bds.height / 2));
            gp.curveTo(bds.x + notchL, bds.y + (bds.height / 2),
                       bds.x + notchL, bds.y + (bds.height / 2) - notchL,
                       bds.x, bds.y + (bds.height / 2) - notchL);
            if (bds.y + (bds.height / 2) - notchL > bds.y + upperLeftArc) {
                gp.lineTo(bds.x, bds.y + upperLeftArc);
            }
            gp.curveTo(bds.x, Math.min(bds.y + upperLeftArc,
                                       bds.y + (bds.height / 2) - notchL),
                       bds.x, bds.y, bds.x + upperLeftArc, bds.y);
        } else {
            gp.moveTo(bds.x, bds.y + bds.height - lowerLeftArc);
            if (bds.y + bds.height - lowerLeftArc > bds.y + upperLeftArc) {
                gp.lineTo(bds.x, bds.y + upperLeftArc);
            }
            gp.curveTo(bds.x, bds.y + upperLeftArc, bds.x, bds.y,
                       bds.x + upperLeftArc, bds.y);
        }
        if (bds.x + bds.width - upperLeftArc > bds.x + upperRightArc) {
            gp.lineTo(bds.x + bds.width - upperRightArc, bds.y);
        }

        if (notchRight) {
            gp.curveTo(bds.x + bds.width - upperRightArc - 1, bds.y, bds.x
                + bds.width - 1,
                bds.y, bds.x + bds.width - 1,
                Math.min( bds.y + upperRightArc,
                bds.y + (bds.height / 2) - notchR));

            if (bds.y + upperRightArc < bds.y + (bds.height / 2) - notchR) {
                gp.lineTo(bds.x + bds.width - 1,
                          bds.y + (bds.height / 2) - notchR);
            }
            gp.curveTo(bds.x + bds.width - 1, bds.y + (bds.height / 2) - notchR,
                       bds.x + bds.width - notchR - 1,
                       bds.y + (bds.height / 2) - notchR,
                       bds.x + bds.width - notchR - 1, bds.y + (bds.height / 2));

            gp.curveTo(bds.x + bds.width - notchR - 1, bds.y + (bds.height / 2),
                       bds.x + bds.width - notchR - 1,
                       bds.y + (bds.height / 2) + notchR, bds.x + bds.width - 1,
                       bds.y + (bds.height / 2) + notchR);

            if (bds.y + (bds.height / 2) + notchR
                    < bds.y + bds.height - lowerRightArc) {
                gp.lineTo(bds.x + bds.width - 1,
                          bds.y + bds.height - lowerRightArc);
            }

            gp.curveTo(bds.x + bds.width - 1, Math.max(
                    bds.y + (bds.height / 2) + notchR,
                    bds.y + bds.height - lowerRightArc),
                       bds.x + bds.width - 1, bds.y + bds.height,
                       bds.x + bds.width - lowerRightArc - 1, bds.y + bds.height);

        } else {
            if (upperRightArc != 0) {
                gp.curveTo(bds.x + bds.width - upperRightArc - 1, bds.y,
                       bds.x + bds.width - 1, bds.y,
		       bds.x + bds.width - 1, bds.y + upperRightArc);
	    } else {
                gp.curveTo(bds.x + bds.width - upperRightArc, bds.y,
                       bds.x + bds.width, bds.y, bds.x + bds.width,
                       bds.y + upperRightArc);
            }
            if (bds.y + upperRightArc < bds.y + bds.height - lowerRightArc) {
                if (upperRightArc != 0 && lowerRightArc != 0) {
                    gp.lineTo(bds.x + bds.width - 1,
                          bds.y + bds.height - lowerRightArc);
                } else {
                    gp.lineTo(bds.x + bds.width,
                          bds.y + bds.height - lowerRightArc);
                }
            }
            if (lowerRightArc != 0) {
		gp.curveTo(bds.x + bds.width - 1, bds.y + bds.height - lowerRightArc,
                       bds.x + bds.width - 1, bds.y + bds.height,
                       bds.x + bds.width - lowerRightArc - 1, bds.y + bds.height);
	    } else {
		gp.curveTo(bds.x + bds.width, bds.y + bds.height - lowerRightArc,
                       bds.x + bds.width, bds.y + bds.height,
                       bds.x + bds.width - lowerRightArc, bds.y + bds.height);
	    }
        }
        if (bds.x + bds.width - lowerRightArc > bds.x + lowerLeftArc) {
            gp.lineTo(bds.x + lowerLeftArc, bds.y + bds.height);
        }

        if (notchLeft) {
            gp.curveTo(bds.x + lowerLeftArc, bds.y + bds.height, bds.x, bds.y
                + bds.height, bds.x,
                Math.max(bds.y + bds.height - lowerLeftArc,
                bds.y + (bds.height / 2) + notchL));
            if (bds.y + bds.height - lowerLeftArc
                    > bds.y + (bds.height / 2) + notchL) {
                gp.lineTo(bds.x, bds.y + (bds.height / 2) + notchL);
            }
            gp.curveTo(bds.x, bds.y + (bds.height / 2) + notchL,
                       bds.x + notchL, bds.y + (bds.height / 2) + notchL,
                       bds.x + notchL, bds.y + (bds.height / 2));
        } else {
            gp.curveTo(bds.x + lowerLeftArc, bds.y + bds.height, bds.x,
                       bds.y + bds.height, bds.x,
                       bds.y + bds.height - lowerLeftArc);
        }
        return gp;
    }

    protected Color getUpperTop() {
        return upperTop;
    }

    protected Color getUpperBottom() {
        return upperBottom;
    }

    protected Color getLowerTop() {
        return lowerTop;
    }

    protected Color getLowerBottom() {
        return lowerBottom;
    }




}
