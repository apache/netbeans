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

package org.netbeans.modules.editor.lib.drawing;

import java.awt.Graphics;
import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.text.View;
import org.netbeans.editor.Analyzer;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.AnnotationTypes;
import org.netbeans.editor.Annotations;
import org.netbeans.editor.FontMetricsCache;
import org.netbeans.editor.PrintContainer;
import org.openide.util.ImageUtilities;

/** Draw graphics functions as abstraction over various kinds of drawing. It's used
* for drawing into classic graphics, printing and measuring.
* Generally there are only the setters for some properties because 
* the draw-engine doesn't retrieve the values that it previously
* set.
*
* @author Miloslav Metelka
* @version 1.00
*/
public interface DrawGraphics {
    
    /** Set foreground color */
    public void setForeColor(Color foreColor);

    /** Set background color */
    public void setBackColor(Color backColor);

    /** Inform the draw-graphics about the current
    * background color of the component.
    */
    public void setDefaultBackColor(Color defaultBackColor);
    
    public void setStrikeThroughColor(Color strikeThroughColor);
    
    public void setUnderlineColor(Color underlineColor);
    
    public void setWaveUnderlineColor(Color waveUnderlineColor);

    /**
     * @since 1.22
     */
    public void setTopBorderLineColor(Color topBorderLineColor);
    /**
     * @since 1.22
     */
    public void setRightBorderLineColor(Color rightBorderLineColor);
    /**
     * @since 1.22
     */
    public void setBottomBorderLineColor(Color bottomBorderLineColor);
    /**
     * @since 1.22
     */
    public void setLeftBorderLineColor(Color leftBorderLineColor);

    /** Set current font */
    public void setFont(Font font);

    /** Set the current x-coordinate */
    public void setX(int x);

    /** Set the current y-coordinate */
    public void setY(int y);

    /** Set the height of the line. */
    public void setLineHeight(int lineHeight);

    /** Set the ascent of the line. */
    public void setLineAscent(int lineAscent);

    /** Get the AWT-graphics to determine whether this draws to a graphics.
    * This is useful for fast line numbering and others.
    */
    public Graphics getGraphics();

    /** Whether draw graphics supports displaying of line numbers.
    * If not line number displaying is not done.
    */
    public boolean supportsLineNumbers();

    /** Initialize this draw graphics before drawing */
    public void init(DrawContext ctx);

    /** Called when whole drawing ends. Can be used to deallocate
    * some resources etc.
    */
    public void finish();

    /** Fill rectangle at the current [x, y] with the current
    * background color.
    * @param width width of the rectangle to fill in points. The current x-coordinate
    *  must be increased by width automatically.
    */
    public void fillRect(int width);

    /** Draw characters from the specified offset in the buffer
    * @param offset offset in the buffer for drawn text; if the text contains
    *   tabs, then offset is set to -1 and length contains the count
    *   of the space characters that correspond to the expanded tabs
    * @param length length of the text being drawn
    * @param width width of the text being drawn in points. The current
    *   x-coordinate must be increased by width automatically.
    */
    public void drawChars(int offset, int length, int width);

    /** Draw the expanded tab characters.
    * @param offset offset in the buffer where the tab characters start.
    * @param length number of the tab characters
    * @param spaceCount number of spaces that replace the tabs
    * @param width width of the spaces in points. The current x-coordinate
    *   must be increased by width automatically.
    */
    public void drawTabs(int offset, int length, int spaceCount, int width);

    /** Set character buffer from which the characters are drawn. */
    public void setBuffer(char[] buffer);

    /** This method is called to notify this draw graphics in response
    * from targetPos parameter passed to draw().
    * @param offset position that was reached during the drawing.
    * @param ch character at offset
    * @param charWidth visual width of the character ch
    * @param ctx current draw context containing 
    * @return whether the drawing should continue or not. If it returns
    *   false it's guaranteed that this method will not be called again
    *   and the whole draw() method will be stopped. <BR>The only
    *   exception is when the -1 is used as the target offset
    *   when draw() is called which means that every offset
    *   is a potential target offset and must be checked.
    *   In this case the binary search is used when finding
    *   the target offset inside painted fragment. That greatly
    *   improves performance for long fragments because
    *   the font metrics measurements are relatively expensive.
    */
    public boolean targetOffsetReached(int offset, char ch, int x,
                                       int charWidth, DrawContext ctx);

    /** EOL encountered and should be handled. */
    public void eol();
    
    /** Setter for painted view */
    public void setView(javax.swing.text.View view);


    /** Abstract draw-graphics that maintains a fg and bg color, font,
    * current x and y coordinates.
    */
    public abstract static class AbstractDG implements DrawGraphics {

        /** Current foreground color */
        Color foreColor;

        /** Current background color */
        Color backColor;

        /** Default background color */
        Color defaultBackColor;

        /** Current font */
        Font font;

        /** Character buffer from which the data are drawn */
        char[] buffer;

        /** Current x-coordinate */
        int x;

        /** Current y-coordinate */
        int y;

        /** Height of the line being drawn */
        int lineHeight;

        /** Ascent of the line being drawn */
        int lineAscent;

        public Color getForeColor() {
            return foreColor;
        }

        public void setForeColor(Color foreColor) {
            this.foreColor = foreColor;
        }

        public Color getBackColor() {
            return backColor;
        }

        public void setBackColor(Color backColor) {
            this.backColor = backColor;
        }

        public Color getDefaultBackColor() {
            return defaultBackColor;
        }

        public void setDefaultBackColor(Color defaultBackColor) {
            this.defaultBackColor = defaultBackColor;
        }

        public Font getFont() {
            return font;
        }

        public void setFont(Font font) {
            this.font = font;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getLineHeight() {
            return lineHeight;
        }

        public void setLineHeight(int lineHeight) {
            this.lineHeight = lineHeight;
        }

        public int getLineAscent() {
            return lineAscent;
        }

        public void setLineAscent(int lineAscent) {
            this.lineAscent = lineAscent;
        }

        public char[] getBuffer() {
            return buffer;
        }

        public void setBuffer(char[] buffer) {
            this.buffer = buffer;
        }

        public void drawChars(int offset, int length, int width) {
            x += width;
        }

        public void drawTabs(int offset, int length, int spaceCount, int width) {
            x += width;
        }

        public void setStrikeThroughColor(Color strikeThroughColor) {
        }
        
        public void setUnderlineColor(Color underlineColor) {
        }
        
        public void setWaveUnderlineColor(Color waveUnderlineColor) {
        }
        
        public void setView(javax.swing.text.View view) {
        }
        
        public void setTopBorderLineColor(Color topBorderLineColor) {
        }
        
        public void setRightBorderLineColor(Color rightBorderLineColor) {
        }
        
        public void setBottomBorderLineColor(Color bottomBorderLineColor) {
        }
        
        public void setLeftBorderLineColor(Color leftBorderLineColor) {
        }

    } // End of AbstractDG class

    public static class SimpleDG extends AbstractDG {

        public Graphics getGraphics() {
            return null;
        }

        public boolean supportsLineNumbers() {
            return false;
        }

        public void init(DrawContext ctx) {
        }

        public void finish() {
        }

        public void fillRect(int width) {
        }

        public boolean targetOffsetReached(int offset, char ch, int x,
                                           int charWidth, DrawContext ctx) {
            return true; // shouldn't reach this place
        }

        public void eol() {
        }

    } // End of SimpleDG class

    /** Implementation of DrawGraphics to delegate to some Graphics.
    * It optimizes the drawing by joining together the pieces of
    * the text drawn with the same font and fg/bg color.
    */
    public static final class GraphicsDG extends SimpleDG {

        /** Whether debug messages should be displayed */
        private static final boolean debug
            = Boolean.getBoolean("netbeans.debug.editor.draw.graphics"); // NOI18N

        private Graphics graphics;

        /** Start of the chars that were not drawn yet. It can be -1
        * to indicate the buffered characters were just flushed.
        */
        private int startOffset = -1;

        /** End of the chars that were not drawn yet */
        private int endOffset;

        /** X coordinate where the drawing of chars should occur */
        private int startX;

        /** Y coordinate where the drawing of chars should occur */
        private int startY;

        private int width;

        private Color strikeThroughColor;

        private Color underlineColor;
        
        private Color waveUnderlineColor;
        
        private Color topBorderLineColor;
        private Color rightBorderLineColor;
        private Color bottomBorderLineColor;
        private Color leftBorderLineColor;
        
        /** Whether annotations were drawn on the current line already */
        private int lastDrawnAnnosY;
        private int lastDrawnAnnosX;
        
        /** Annotation description cached for the lastDrawnAnnosY */
        private AnnotationDesc[] passiveAnnosAtY;

        /** Alpha used for drawing the glyphs on the background */
        private AlphaComposite alpha = null;

        /** Access to annotations for this document which will be
         * drawn on the background */
        private Annotations annos = null;
        
        private boolean drawTextLimitLine;
        private int textLimitWidth;
        private int defaultSpaceWidth;
        private Color textLimitLineColor;
        private int absoluteX;
        private int maxWidth;
        private View view;

        private int bufferStartOffset;

        private JComponent component;

        public GraphicsDG(Graphics graphics) {
            this.graphics = graphics;
            // #33165 - set invalid y initially
            this.y = -1;
        }

        public @Override void setForeColor(Color foreColor) {
            if (!foreColor.equals(this.foreColor)) {
                flush();
                this.foreColor = foreColor;
            }
        }

        public @Override void setBackColor(Color backColor) {
            if (!backColor.equals(this.backColor)) {
                flush();
                this.backColor = backColor;
            }
        }

        public @Override void setStrikeThroughColor(Color strikeThroughColor) {
            if ((strikeThroughColor != this.strikeThroughColor)
                && (strikeThroughColor == null
                    || !strikeThroughColor.equals(this.strikeThroughColor))
            ) {
                flush();
                this.strikeThroughColor = strikeThroughColor;
            }
        }

        public @Override void setUnderlineColor(Color underlineColor) {
            if ((underlineColor != this.underlineColor)
                && (underlineColor == null
                    || !underlineColor.equals(this.underlineColor))
            ) {
                flush();
                this.underlineColor = underlineColor;
            }
        }

        public @Override void setWaveUnderlineColor(Color waveUnderlineColor) {
            if ((waveUnderlineColor != this.waveUnderlineColor)
                && (waveUnderlineColor == null
                    || !waveUnderlineColor.equals(this.waveUnderlineColor))
            ) {
                flush();
                this.waveUnderlineColor = waveUnderlineColor;
            }
        }

        public @Override void setTopBorderLineColor(Color color) {
            if ((color != this.topBorderLineColor)
                && (color == null
                    || !color.equals(this.topBorderLineColor))
            ) {
                flush();
                this.topBorderLineColor = color;
            }
        }

        public @Override void setRightBorderLineColor(Color color) {
            if ((color != this.rightBorderLineColor)
                && (color == null
                    || !color.equals(this.rightBorderLineColor))
            ) {
                flush();
                this.rightBorderLineColor = color;
            }
        }

        public @Override void setBottomBorderLineColor(Color color) {
            if ((color != this.bottomBorderLineColor)
                && (color == null
                    || !color.equals(this.bottomBorderLineColor))
            ) {
                flush();
                this.bottomBorderLineColor = color;
            }
        }

        public @Override void setLeftBorderLineColor(Color color) {
            if ((color != this.leftBorderLineColor)
                && (color == null
                    || !color.equals(this.leftBorderLineColor))
            ) {
                flush();
                this.leftBorderLineColor = color;
            }
        }

        public @Override void setFont(Font font) {
            if (!font.equals(this.font)) {
                flush();
                this.font = font;
            }
        }

        public @Override void setX(int x) {
            if (x != this.x) {
                flush();
                this.x = x;
            }
        }

        public @Override void setY(int y) {
            if (y != this.y) {
                flush();
                this.y = y;
            }
        }

        public @Override void init(DrawContext ctx) {
            EditorUiAccessor accessor = EditorUiAccessor.get();
            annos = ctx.getEditorUI().getDocument().getAnnotations();
            drawTextLimitLine = accessor.getTextLimitLineVisible(ctx.getEditorUI());
            textLimitWidth = accessor.getTextLimitWidth(ctx.getEditorUI());
            defaultSpaceWidth = accessor.getDefaultSpaceWidth(ctx.getEditorUI());
            textLimitLineColor = accessor.getTextLimitLineColor(ctx.getEditorUI());
            absoluteX = ctx.getEditorUI().getTextMargin().left;
            maxWidth = ctx.getEditorUI().getExtentBounds().width;
            component = ctx.getEditorUI().getComponent();
        }

        public @Override void finish() {
            // flush() already performed in setBuffer(null) and might cause problems here
            // as this code is typically called from finally clause.
            //flush();
        }
        
        public @Override void setView(View view){
            this.view = view;
        }

        private void flush() {
            flush(false);
        }


        private void flush(boolean atEOL) {
            if (y < 0) { // not yet initialized
                return ;
            }
            
            if (startOffset >= 0 && startOffset != endOffset) { // some text on the line
                // First possibly fill the rectangle
                fillRectImpl(startX, startY, x - startX);
            }
            
            // #33165 - for each fragment getPassiveAnnotationsForLine() was called
            // but it can done just once per line.
            if (lastDrawnAnnosY != y) {
                lastDrawnAnnosY = y;
                lastDrawnAnnosX = 0;
                if (AnnotationTypes.getTypes().isBackgroundDrawing().booleanValue()) {
                    if (alpha == null)
                        alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, AnnotationTypes.getTypes().getBackgroundGlyphAlpha().intValue() / 100f);
                    if (view!=null){
                        passiveAnnosAtY = annos.getPassiveAnnotations(view.getStartOffset());                        
                    }
                } else {
                    passiveAnnosAtY = null;
                }
            }
            
            int glyphX=2;
            if (passiveAnnosAtY != null) {
                Graphics2D g2d = (Graphics2D) graphics;

                Shape shape = graphics.getClip();

                // set alpha composite
                Composite origin = g2d.getComposite();
                g2d.setComposite(alpha);

                // clip the drawing area
                int clipX = atEOL ? Integer.MAX_VALUE : x;
                int clipY = Math.min(lastDrawnAnnosX, this.startX);
                Rectangle clip = new Rectangle(clipY, y, clipX - clipY, lineHeight);
                lastDrawnAnnosX = clipX;
                clip = clip.intersection(shape.getBounds());
                graphics.setClip(clip);

                for (int i=0; i < passiveAnnosAtY.length; i++) {
                    Icon glyphIcon = ImageUtilities.image2Icon(passiveAnnosAtY[i].getGlyph());
                    glyphIcon.paintIcon(null, g2d, glyphX, y);
                    glyphX += glyphIcon.getIconWidth() + 1;
                }

                // restore original clip region
                graphics.setClip(shape);

                // restore original ocmposite
                g2d.setComposite(origin);
            }

            // If no text on the line then return + handle incorrect conditions
            if (startOffset < 0 || startOffset >= endOffset || startOffset - endOffset > buffer.length) {
                startOffset = -1;
                return;
            }

            
            if (drawTextLimitLine && textLimitWidth > 0) { // draw limit line
                Rectangle clip = graphics.getClipBounds();
                int lineX = absoluteX + textLimitWidth * defaultSpaceWidth;
                if (lineX >= startX && lineX <= x){
                    Color bakColor = graphics.getColor();
                    graphics.setColor(textLimitLineColor);
                    graphics.drawLine(lineX, startY, lineX, startY + lineHeight);
                    graphics.setColor(bakColor);
                }
            }

            // Text framing support
            if (topBorderLineColor != null) {
                graphics.setColor(topBorderLineColor);
                graphics.drawLine(startX, startY, x - 1, startY);
            }
            if (rightBorderLineColor != null) {
                graphics.setColor(rightBorderLineColor);
                graphics.drawLine(x - 1, startY, x - 1, startY + lineHeight - 1);
            }
            if (bottomBorderLineColor != null) {
                graphics.setColor(bottomBorderLineColor);
                graphics.drawLine(startX, startY + lineHeight - 1, x - 1, startY + lineHeight - 1);
            }
            if (leftBorderLineColor != null) {
                graphics.setColor(leftBorderLineColor);
                graphics.drawLine(startX, startY, startX, startY + lineHeight - 1);
            }
            
            // Check whether the graphics uses right color
            graphics.setColor(foreColor);
            // Check whether the graphics uses right font
            graphics.setFont(font);

            if (debug) {
                String text = new String(buffer, startOffset, endOffset - startOffset);
                System.out.println("DrawGraphics: text='" + text // NOI18N
                    + "', text.length=" + text.length() // NOI18N
                    + ", x=" + startX + ", y=" + startY // NOI18N
                    + ", ascent=" + lineAscent // NOI18N
                    + ", clip=" + graphics.getClipBounds() // NOI18N
                    + ", color=" + graphics.getColor() // NOI18N
                );
            }

            // Use TextLayout drawing
            drawStringTextLayout(component, graphics,
                    new String(buffer, startOffset, endOffset - startOffset),
                    startX, startY + lineAscent);

//            graphics.drawChars(buffer, startOffset, endOffset - startOffset,
//                               startX, startY + lineAscent);

            if (strikeThroughColor != null) { // draw strike-through
                FontMetricsCache.Info fmcInfo = FontMetricsCache.getInfo(font);
                graphics.setColor(strikeThroughColor);
                graphics.fillRect(startX,
                                  (int)(startY + fmcInfo.getStrikethroughOffset(graphics) + lineAscent),
                                  x - startX,
                                  Math.max(1, Math.round(fmcInfo.getStrikethroughThickness(graphics)))
                                 );
            }

            if (waveUnderlineColor != null && bottomBorderLineColor == null) { // draw wave underline
                FontMetricsCache.Info fmcInfo = FontMetricsCache.getInfo(font);
                graphics.setColor(waveUnderlineColor);

                int waveLength = x - startX;                
                if (waveLength > 0) {
                    int[] wf = {0, 0, -1, -1};
                    int[] xArray = new int[waveLength + 1];
                    int[] yArray = new int[waveLength + 1];
                    
                    int yBase = (int)(startY + fmcInfo.getUnderlineOffset(graphics) + lineAscent + 0.5);
                    for (int i=0;i<=waveLength;i++) {
                        xArray[i]=startX + i;
                        yArray[i]=yBase + wf[xArray[i] % 4];                    
                    }                    
                    graphics.drawPolyline(xArray, yArray, waveLength);
                }
            }

            if (underlineColor != null && bottomBorderLineColor == null) { // draw underline
                FontMetricsCache.Info fmcInfo = FontMetricsCache.getInfo(font);
                graphics.setColor(underlineColor);
                // Underline offset points to lower baseline of letters for Monospaced font (Linux, Mac)
                // -> therefore adding a contant
                // On Linux the default font is "Monospaced 12"; on Mac it's "Monospaced 13".
                // When constant == 0 then a line of char '_':
                //      on Linux it's one pixel above underline line (no space between the lines).
                //      on Mac it's two pixels below underline line (one pixel space between lines).
                // When constant == 0.5 then a line of char '_':
                //      on Linux it's one pixel above underline line.
                //      on Mac it's one pixel below underline line
                // When constant == 1.5 then a line of char '_':
                //      on Linux it's two pixels above underline line.
                //      on Mac it's the same pixel as underline line
                // TODO - we should collect other fonts (eg. Lucida Console) and sizes.
                graphics.fillRect(startX,
                                  (int)(startY + fmcInfo.getUnderlineOffset(graphics) + lineAscent + 0.5),
                                  x - startX,
                                  Math.max(1, Math.round(fmcInfo.getUnderlineThickness(graphics)))
                                 );
            }

            startOffset = -1; // signal no characters to draw
        }

        public @Override Graphics getGraphics() {
            return graphics;
        }

        public @Override boolean supportsLineNumbers() {
            return true;
        }

        public @Override void fillRect(int width) {
            fillRectImpl(x, y, width);
            x += width;
        }

        private void fillRectImpl(int rx, int ry, int width) {
            if (width > 0) { // only for non-zero width
                // only fill for different color than current background
                if (!backColor.equals(defaultBackColor)) {
                    graphics.setColor(backColor);
                    graphics.fillRect(rx, ry, width, lineHeight);
                }

            }
        }


        public @Override void drawChars(int offset, int length, int width) {
            if (length >= 0) {
                if (startOffset < 0) { // no token yet
                    startOffset = offset;
                    endOffset = offset + length;
                    this.startX = x;
                    this.startY = y;
                    this.width = width;

                } else { // already token before
                    endOffset += length;
                }
            }

            x += width;
        }

        public @Override void drawTabs(int offset, int length, int spaceCount, int width) {
            if (width > 0) {
                flush();
                fillRectImpl(x, y, width);
                x += width;
            }
        }

        public @Override void setBuffer(char[] buffer) {
            flush();
            this.buffer = buffer;
            startOffset = -1;
            bufferStartOffset = -1;
        }
        
        void setBufferStartOffset(int bufferStartOffset) {
            this.bufferStartOffset = bufferStartOffset;
        }

        public @Override void eol() {
            if (drawTextLimitLine && textLimitWidth > 0) { // draw limit line
                int lineX = absoluteX + textLimitWidth * defaultSpaceWidth;
                if (lineX >= x-defaultSpaceWidth){
                    Color bakColor = graphics.getColor();
                    graphics.setColor(textLimitLineColor);
                    Rectangle clipB = graphics.getClipBounds();
                    if (clipB.width + clipB.x <= lineX && clipB.x < maxWidth) {
                        graphics.setClip(clipB.x, clipB.y, maxWidth - clipB.x, clipB.height);
                        graphics.drawLine(lineX, y, lineX, y + lineHeight);
                        graphics.setClip(clipB.x, clipB.y, clipB.width, clipB.height);
                    }else{
                        graphics.drawLine(lineX, y, lineX, y + lineHeight);
                    }
                    graphics.setColor(bakColor);
                }
            }
            flush(true);
        }

        private static void drawStringTextLayout(JComponent c, Graphics g, String text, int x, int baselineY) {
            if (!(g instanceof Graphics2D)) {
                g.drawString(text, x, baselineY);
            } else { // Graphics2D available
                Graphics2D g2d = (Graphics2D)g;
                FontRenderContext frc = g2d.getFontRenderContext();
                TextLayout layout = new TextLayout(text, g2d.getFont(), frc);
                layout.draw(g2d, x, baselineY);
            }
        }

    } // End of GraphicsDG class

    public static final class PrintDG extends SimpleDG {

        PrintContainer container;

        /** Whether there were some paints already on the line */
        boolean lineInited;

        /** Construct the new print graphics
        * @param container print container to which the tokens
        *   are added.
        */
        public PrintDG(PrintContainer container) {
            this.container = container;
        }

        public @Override boolean supportsLineNumbers() {
            return true;
        }

        public @Override void drawChars(int offset, int length, int width) {
            if (length > 0) {
                lineInited = true; // Fixed 42536
                char[] chars = new char[length];
                System.arraycopy(buffer, offset, chars, 0, length);
                container.add(chars, font, foreColor, backColor);
            }
        }

        private void printSpaces(int spaceCount) {
            char[] chars = new char[spaceCount];
            System.arraycopy(Analyzer.getSpacesBuffer(spaceCount), 0, chars, 0, spaceCount);
            container.add(chars, font, foreColor, backColor);
        }

        public @Override void drawTabs(int offset, int length, int spaceCount, int width) {
            lineInited = true; // Fixed 42536
            printSpaces(spaceCount);
        }

        public @Override void eol() {
            if (!lineInited && container.initEmptyLines()) {
                printSpaces(1);
            }
            container.eol();
            lineInited = false; // signal that the next line is not inited yet
        }

    } // End of PrintDG class

}
