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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.api.visual.widget;

import org.netbeans.modules.visual.util.GeomUtil;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * A widget representing a text. The widget is not opaque and is checking clipping for by default.
 * <p>
 * It allows to set 4 types of horizontal and vertical alignments (by default LEFT as horizontal and BASELINE as vertical).
 * <p>
 * Swing Font-hinting feature may cause the labels are not rendered completely.
 * Using <code>setUseGlyphVector</code> you may force the label to be converted to a glyph vector
 * which scales correctly for any zoom factor.
 *
 * @author David Kaspar
 */
public class LabelWidget extends Widget {

    /**
     * The text alignment
     */
    public enum Alignment {
        LEFT, RIGHT, CENTER, BASELINE
    }

    /**
     * The text vertical alignment
     */
    public enum VerticalAlignment {
        TOP, BOTTOM, CENTER, BASELINE
    }
    
    /**
     * The text orientation
     * @since 2.1
     */
    public enum Orientation {
        NORMAL, ROTATE_90//, ROTATE_180, ROTATE_270, MIRROR, MIRROR_ROTATE_90, MIRROR_ROTATE_180, MIRROR_ROTATE_270
    }

    private String label;
    private Alignment alignment = Alignment.LEFT;
    private VerticalAlignment verticalAlignment = VerticalAlignment.BASELINE;
    private Orientation orientation = Orientation.NORMAL;
    private boolean paintAsDisabled;
    private boolean useGlyphVector = false;

    private GlyphVector cacheGlyphVector;
    private String cacheLabel;
    private Font cacheFont;

    /**
     * Creates a label widget.
     * @param scene the scene
     */
    public LabelWidget (Scene scene) {
        this (scene, null);
    }

    /**
     * Creates a label widget with a label.
     * @param scene the scene
     * @param label the label
     */
    public LabelWidget (Scene scene, String label) {
        super (scene);
        setOpaque (false);
//        setCursor (new Cursor (Cursor.TEXT_CURSOR));
        setLabel (label);
        setCheckClipping (true);
    }

    /**
     * Returns a label.
     * @return the label
     */
    public String getLabel () {
        return label;
    }

    /**
     * Sets a label.
     * @param label the label
     */
    public void setLabel (String label) {
        if (GeomUtil.equals (this.label, label))
            return;
        this.label = label;
        revalidate ();
    }

    /**
     * Returns a text horizontal alignment.
     * @return the text horizontal alignment
     */
    public Alignment getAlignment () {
        return alignment;
    }

    /**
     * Sets a text horizontal alignment.
     * @param alignment the text horizontal alignment
     */
    public void setAlignment (Alignment alignment) {
        this.alignment = alignment;
        repaint ();
    }

    /**
     * Gets a text vertical alignment.
     * @return the text vertical alignment
     */
    public VerticalAlignment getVerticalAlignment () {
        return verticalAlignment;
    }

    /**
     * Sets a text vertical alignment.
     * @param verticalAlignment the text vertical alignment
     */
    public void setVerticalAlignment (VerticalAlignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
        repaint ();
    }

    /**
     * Gets a text orientation.
     * @return the text orientation
     * @since 2.1
     */
    public Orientation getOrientation() {
        return orientation;
    }

    /**
     * Sets a text orientation.
     * @param orientation the text orientation
     * @since 2.1
     */
    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
        revalidate();
    }

    /**
     * Returns whether the label is painted as disabled.
     * @return true, if the label is painted as disabled
     */
    public boolean isPaintAsDisabled () {
        return paintAsDisabled;
    }

    /**
     * Sets whether the label is painted as disabled.
     * @param paintAsDisabled if true, then the label is painted as disabled
     */
    public void setPaintAsDisabled (boolean paintAsDisabled) {
        boolean repaint = this.paintAsDisabled != paintAsDisabled;
        this.paintAsDisabled = paintAsDisabled;
        if (repaint)
            repaint ();
    }

    /**
     * Returns whether the label widget is using glyph vector for rendering text.
     * @return true, if the label widget is using glyph vector
     * @since 2.7
     */
    public boolean isUseGlyphVector () {
        return useGlyphVector;
    }

    /**
     * Sets whether the label widget is using glyph vector for rendering text.
     * <p>
     * Note that using glyph vector could slow-down the rendering performance.
     * Note that if you are not using glyph vector then the text may be clipped when a scene has zoom factor different from 1.0.
     * @param useGlyphVector if true, then a glyph vector is used for rendering text
     * @since 2.7
     */
    public void setUseGlyphVector (boolean useGlyphVector) {
        if (this.useGlyphVector == useGlyphVector)
            return;
        this.useGlyphVector = useGlyphVector;
        cacheGlyphVector = null;
        cacheLabel = null;
        cacheFont = null;
        revalidate ();
    }

    private void assureGlyphVector () {
        Font font = getFont ();
        FontRenderContext fontRenderContext = getGraphics ().getFontRenderContext ();
        if (cacheGlyphVector != null  &&  cacheFont == font  &&  cacheLabel == label)
            return;
        cacheFont = font;
        cacheLabel = label;
        cacheGlyphVector = font.createGlyphVector (new FontRenderContext (new AffineTransform (), fontRenderContext.isAntiAliased (), fontRenderContext.usesFractionalMetrics ()), cacheLabel);
    }

    /**
     * Calculates a client area for the label.
     * @return the client area
     */
    protected Rectangle calculateClientArea () {
        if (label == null)
            return super.calculateClientArea ();
        Rectangle rectangle;
        if (useGlyphVector) {
            assureGlyphVector ();
            rectangle = GeomUtil.roundRectangle (cacheGlyphVector.getVisualBounds ());
            rectangle.grow (1, 1); // WORKAROUND - even text antialiasing is included into the boundary
        } else {
            Graphics2D gr = getGraphics ();
            if (gr == null) { // #192529
                return super.calculateClientArea();
            }
            FontMetrics fontMetrics = gr.getFontMetrics (getFont ());
            Rectangle2D stringBounds = fontMetrics.getStringBounds (label, gr);
            rectangle = GeomUtil.roundRectangle (stringBounds);
        }
        switch (orientation) {
            case NORMAL:
                return rectangle;
            case ROTATE_90:
                return new Rectangle (rectangle.y, - rectangle.x - rectangle.width, rectangle.height, rectangle.width);
            default:
                throw new IllegalStateException ();
        }
    }

    /**
     * Paints the label widget.
     */
    protected void paintWidget () {
        if (label == null)
            return;
        Graphics2D gr = getGraphics ();
        if (useGlyphVector)
            assureGlyphVector ();
        else
            gr.setFont (getFont ());

        FontMetrics fontMetrics = gr.getFontMetrics ();
        Rectangle clientArea = getClientArea ();

        int x;
        int y;

        switch (orientation) {
            case NORMAL:

                switch (alignment) {
                    case BASELINE:
                        x = 0;
                        break;
                    case LEFT:
                        x = clientArea.x;
                        break;
                    case CENTER:
                        if (useGlyphVector)
                            x = clientArea.x + (clientArea.width - getCacheGlyphVectorWidth ()) / 2;
                        else
                            x = clientArea.x + (clientArea.width - fontMetrics.stringWidth (label)) / 2;
                        break;
                    case RIGHT:
                        if (useGlyphVector)
                            x = clientArea.x + clientArea.width - getCacheGlyphVectorWidth ();
                        else
                            x = clientArea.x + clientArea.width - fontMetrics.stringWidth (label);
                        break;
                    default:
                        return;
                }

                switch (verticalAlignment) {
                    case BASELINE:
                        y = 0;
                        break;
                    case TOP:
                        y = clientArea.y + fontMetrics.getAscent ();
                        break;
                    case CENTER:
                        y = clientArea.y + (clientArea.height + fontMetrics.getAscent () - fontMetrics.getDescent ()) / 2;
                        break;
                    case BOTTOM:
                        y = clientArea.y + clientArea.height - fontMetrics.getDescent ();
                        break;
                    default:
                        return;
                }

                break;
            case ROTATE_90:

                switch (alignment) {
                    case BASELINE:
                        x = 0;
                        break;
                    case LEFT:
                        x = clientArea.x + fontMetrics.getAscent ();
                        break;
                    case CENTER:
                        x = clientArea.x + (clientArea.width + fontMetrics.getAscent () - fontMetrics.getDescent ()) / 2;
                        break;
                    case RIGHT:
                        x = clientArea.x + clientArea.width - fontMetrics.getDescent ();
                        break;
                    default:
                        return;
                }

                switch (verticalAlignment) {
                    case BASELINE:
                        y = 0;
                        break;
                    case TOP:
                        if (useGlyphVector)
                            y = clientArea.y + getCacheGlyphVectorWidth ();
                        else
                        y = clientArea.y + fontMetrics.stringWidth (label);
                        break;
                    case CENTER:
                        if (useGlyphVector)
                            y = clientArea.y + (clientArea.height + getCacheGlyphVectorWidth ()) / 2;
                        else
                            y = clientArea.y + (clientArea.height + fontMetrics.stringWidth (label)) / 2;
                        break;
                    case BOTTOM:
                        y = clientArea.y + clientArea.height;
                        break;
                    default:
                        return;
                }

                break;
            default:
                return;
        }

        AffineTransform previousTransform = gr.getTransform ();
        gr.translate (x, y);
        switch (orientation) {
            case NORMAL:
                break;
            case ROTATE_90:
                gr.rotate (- GeomUtil.M_PI_2);
                break;
            default:
                throw new IllegalStateException ();
        }

        Paint background = getBackground ();
        if (paintAsDisabled  &&  background instanceof Color) {
            Color color = ((Color) background);
            gr.setColor (color.brighter ());
            if (useGlyphVector)
                gr.fill (cacheGlyphVector.getOutline (1, 1));
            else
                gr.drawString (label, 1, 1);
            gr.setColor (color.darker ());
            if (useGlyphVector)
                gr.fill (cacheGlyphVector.getOutline ());
            else
                gr.drawString (label, 0, 0);
        } else {
            gr.setColor (getForeground ());
            if (useGlyphVector)
                gr.fill (cacheGlyphVector.getOutline ());
            else
                gr.drawString (label, 0, 0);
        }
        
        gr.setTransform(previousTransform);
    }

    private int getCacheGlyphVectorWidth () {
        return GeomUtil.roundRectangle (cacheGlyphVector.getVisualBounds ()).width;
    }

}
