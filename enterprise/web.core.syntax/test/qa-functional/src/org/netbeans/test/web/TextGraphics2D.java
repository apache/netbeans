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

package org.netbeans.test.web;

import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;


/** Renders all drawString(...) into StringBuffer.
 *
 * @author Martin.Schovanek@sun.com
 */
public class TextGraphics2D extends Graphics2D {
    StringBuffer buf = new StringBuffer();
    Component dummy;
    Font font;
    
    /**
     * Creates a new instance of TextGraphics2D
     */
    public TextGraphics2D(Component component) {
        dummy = component;
    }
    
    public TextGraphics2D() {
        this(null);
    }
    
    public String getText() {
        return buf.toString();
    }
    
    
    public String getTextUni() {
        String str;
        int start;
        StringBuffer buf = new StringBuffer().append(this.buf);
        while ((start=buf.indexOf("  ")) > -1) {
            buf.deleteCharAt(start);
        }
        str = buf.toString();
        // remove '...' ussually rendered when the left epression is too long
        str = str.replaceAll("[.]{3} ?", "");
        return str;
    }
    
    public void clearRect(int param, int param1, int param2, int param3) {
    }
    
    public void clipRect(int param, int param1, int param2, int param3) {
    }
    
    public void copyArea(int param, int param1, int param2, int param3,
            int param4, int param5) {
    }
    
    public Graphics create() {
        return new TextGraphics2D(dummy);
    }
    
    public void dispose() {
    }
    
    public void drawArc(int param, int param1, int param2, int param3,
            int param4, int param5) {
    }
    
    public boolean drawImage(Image image, int param, int param2,
            ImageObserver imageObserver) {
        return true;
    }
    
    public boolean drawImage(Image image, int param, int param2, Color color,
            ImageObserver imageObserver) {
        return true;
    }
    
    public boolean drawImage(Image image, int param, int param2, int param3,
            int param4, ImageObserver imageObserver) {
        return true;
    }
    
    public boolean drawImage(Image image, int param, int param2, int param3,
            int param4, Color color, ImageObserver imageObserver) {
        return true;
    }
    
    public boolean drawImage(Image image, int param, int param2, int param3,
            int param4, int param5, int param6, int param7, int param8,
            ImageObserver imageObserver) {
        return true;
    }
    
    public boolean drawImage(Image image, int param, int param2, int param3,
            int param4, int param5, int param6, int param7, int param8,
            Color color, ImageObserver imageObserver) {
        return true;
    }
    
    public void drawLine(int param, int param1, int param2, int param3) {
    }
    
    public void drawOval(int param, int param1, int param2, int param3) {
    }
    
    public void drawPolygon(int[] values, int[] values1, int param) {
    }
    
    public void drawPolyline(int[] values, int[] values1, int param) {
    }
    
    public void drawRoundRect(int param, int param1, int param2, int param3,
            int param4, int param5) {
    }
    
    public void drawString(String str, int x, int y) {
        drawString(str, (float) x, (float) y);
    }
    
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        drawString(iterator, (float) x, (float) y);
    }
    
    public void fillArc(int param, int param1, int param2, int param3, int param4,
            int param5) {
    }
    
    public void fillOval(int param, int param1, int param2, int param3) {
    }
    
    public void fillPolygon(int[] values, int[] values1, int param) {
    }
    
    public void fillRect(int param, int param1, int param2, int param3) {
    }
    
    public void fillRoundRect(int param, int param1, int param2, int param3,
            int param4, int param5) {
    }
    
    public Shape getClip() {
        return new Rectangle();
    }
    
    public Rectangle getClipBounds() {
        return new Rectangle();
    }
    
    public Color getColor() {
        return Color.WHITE;
    }
    
    public Font getFont() {
        if(font != null) return font;
        if (dummy == null) return null;
        return dummy.getFont();
    }
    
    public FontMetrics getFontMetrics(Font font) {
        if (dummy == null) return null;
        return dummy.getFontMetrics(dummy.getFont());
    }
    
    public void setClip(Shape shape) {
    }
    
    public void setClip(int param, int param1, int param2, int param3) {
    }
    
    public void setColor(Color color) {
    }
    
    public void setFont(Font font) {
        this.font=font;
    }
    
    public void setPaintMode() {
    }
    
    public void setXORMode(Color color) {
    }
    
    public void translate(int param, int param1) {
    }
    
    public void addRenderingHints(Map map) {
    }
    
    public void clip(Shape shape) {
    }
    
    public void draw(Shape shape) {
    }
    
    public void drawGlyphVector(GlyphVector glyphVector, float param,
            float param2) {
    }
    
    public boolean drawImage(Image image, AffineTransform affineTransform,
            ImageObserver imageObserver) {
        return true;
    }
    
    public void drawImage(BufferedImage bufferedImage,
            BufferedImageOp bufferedImageOp, int param, int param3) {
    }
    
    public void drawRenderableImage(RenderableImage renderableImage,
            AffineTransform affineTransform) {
    }
    
    public void drawRenderedImage(RenderedImage renderedImage,
            AffineTransform affineTransform) {
    }
    
    public void drawString(String str, float x, float y) {
        if (buf.length()>0 && buf.charAt(buf.length()-1) != ' ') {
            buf.append(' ');
        }
        buf.append(str);
    }
    
    public void drawString(AttributedCharacterIterator iterator,
            float x, float y) {
        if (buf.length()>0 && buf.charAt(buf.length()-1)!=' ') {
            buf.append(' ');
        }
        for (char c=iterator.first(); c != iterator.DONE; c=iterator.next()) {
            buf.append(c);
        }
    }
    
    public void fill(Shape shape) {
    }
    
    public Color getBackground() {
        if (dummy == null) return null;
        return dummy.getBackground();
    }
    
    public Composite getComposite() {
        return null;
    }
    
    public GraphicsConfiguration getDeviceConfiguration() {
        return null;
    }
    
    public FontRenderContext getFontRenderContext() {
        if (dummy == null) return null;
        Graphics2D graphics = (Graphics2D) dummy.getGraphics();
        if (graphics == null) return null;
        return graphics.getFontRenderContext();
    }
    
    public Paint getPaint() {
        return null;
    }
    
    public Object getRenderingHint(RenderingHints.Key key) {
        return null;
    }
    
    public RenderingHints getRenderingHints() {
        return null;
    }
    
    public Stroke getStroke() {
        return null;
    }
    
    public AffineTransform getTransform() {
        return null;
    }
    
    public boolean hit(Rectangle rectangle, Shape shape, boolean param) {
        return true;
    }
    
    public void rotate(double param) {
    }
    
    public void rotate(double param, double param1, double param2) {
    }
    
    public void scale(double param, double param1) {
    }
    
    public void setBackground(Color color) {
    }
    
    public void setComposite(Composite composite) {
    }
    
    public void setPaint(Paint paint) {
    }
    
    public void setRenderingHint(RenderingHints.Key key, Object obj) {
    }
    
    public void setRenderingHints(Map map) {
    }
    
    public void setStroke(Stroke stroke) {
    }
    
    public void setTransform(AffineTransform affineTransform) {
    }
    
    public void shear(double param, double param1) {
    }
    
    public void transform(AffineTransform affineTransform) {
    }
    
    public void translate(double param, double param1) {
    }
}
