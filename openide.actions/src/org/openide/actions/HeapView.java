/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package org.openide.actions;

import java.awt.AWTEvent;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.text.MessageFormat;
import java.util.prefs.Preferences;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 *
 * @author sky, radim
 */
class HeapView extends JComponent {
    private static final boolean AUTOMATIC_REFRESH = System.getProperty("org.netbeans.log.startup") == null;

    /**
     * Style for overlay on top of grid.
     */
    private static final int STYLE_DEFAULT = 0;
    
    /**
     * Grid overlayed on top of heap. This is the default.
     */
    private static final int STYLE_OVERLAY = 1;
    
    /*
     * How often the display is updated.
     */
    private static final int TICK = 1500;
    
    /**
     * Time (in ms) to animate heap growing.
     */
    private static final int HEAP_GROW_ANIMATE_TIME = 1000;
    
    /**
     * Width of the border.
     */
    private static final int BORDER_W = 2;
    
    /**
     * Height of the border area.
     */
    private static final int BORDER_H = 4;
    
    /**
     * Colors for the grid. This is alternating pairs for a linear gradient.
     */
    private static final Color[] GRID_COLORS = new Color[8];
    
    /**
     * Border color.
     */
    private static Color border1Color;

    /**
     * Border color of line below the top.
     */
    private static Color border2Color;

    private static Color border3Color;
    
    /**
     * Start color for the tick gradient.
     */
    private static Color minTickColor;

    /**
     * End color for the tick gradient.
     */
    private static Color maxTickColor;

    /**
     * Color for the text before blurred.
     */
    private static Color textBlurColor;
    
    /**
     * Color for text drawn on top of blurred text.
     */
    private static Color textColor;

    /**
     * Start color for the background gradient.
     */
    private static Color background1Color;

    /**
     * End color for the background gradient.
     */
    private static Color background2Color;
    
    /**
     * Size used for Kernel used to generate drop shadow.
     */
    private static final int KERNEL_SIZE = 3;

    /**
     * Factor used for Kernel used to generate drop shadow.
     */
    private static final float BLUR_FACTOR = 0.1f;
    
    /**
     * How far to shift the drop shadow along the horizontal axis.
     */
    private static final int SHIFT_X = 0;

    /**
     * How far to shift the drop shadow along the vertical axis.
     */
    private static final int SHIFT_Y = 1;

    static {
        //init colors
        Color c = UIManager.getColor( "nb.heapview.border1" ); //NOI18N
        if( null == c )
            c = new Color(0xA6A295);
        border1Color = c;

        c = UIManager.getColor( "nb.heapview.border2" ); //NOI18N
        if( null == c )
            c = new Color(0xC0BCAD);
        border2Color = c;

        c = UIManager.getColor( "nb.heapview.border3" ); //NOI18N
        if( null == c )
            c = Color.WHITE;
        border3Color = c;

        c = UIManager.getColor( "nb.heapview.mintick.color" ); //NOI18N
        if( null == c )
            c = new Color(0xC7D6AD);
        minTickColor = c;

        c = UIManager.getColor( "nb.heapview.maxtick.color" ); //NOI18N
        if( null == c )
            c = new Color(0x615d0f);
        maxTickColor = c;

        c = UIManager.getColor( "nb.heapview.textblur" ); //NOI18N
        if( null == c )
            c = Color.WHITE;
        textBlurColor = c;

        c = UIManager.getColor( "nb.heapview.foreground" ); //NOI18N
        if( null == c )
            c = Color.WHITE;
        textColor = c;

        c = UIManager.getColor( "nb.heapview.background1" ); //NOI18N
        if( null == c )
            c = new Color(0xD0CCBC);
        background1Color = c;

        c = UIManager.getColor( "nb.heapview.background2" ); //NOI18N
        if( null == c )
            c = new Color(0xEAE7D7);
        background2Color = c;

        c = UIManager.getColor( "nb.heapview.grid1.start" );
        if( null == c )
            c = new Color(0xE3DFCF);
        GRID_COLORS[0] = c;

        c = UIManager.getColor( "nb.heapview.grid1.end" );
        if( null == c )
            c = new Color(0xE7E4D3);
        GRID_COLORS[1] = c;

        c = UIManager.getColor( "nb.heapview.grid2.start" );
        if( null == c )
            c = new Color(0xDAD7C6);
        GRID_COLORS[2] = c;

        c = UIManager.getColor( "nb.heapview.grid2.end" );
        if( null == c )
            c = new Color(0xDFDCCB);
        GRID_COLORS[3] = c;

        c = UIManager.getColor( "nb.heapview.grid3.start" );
        if( null == c )
            c = new Color(0xD3CFBF);
        GRID_COLORS[4] = c;

        c = UIManager.getColor( "nb.heapview.grid3.end" );
        if( null == c )
            c = new Color(0xD7D3C3);
        GRID_COLORS[5] = c;

        c = UIManager.getColor( "nb.heapview.grid4.start" );
        if( null == c )
            c = new Color(0xCECABA);
        GRID_COLORS[6] = c;

        c = UIManager.getColor( "nb.heapview.grid4.end" );
        if( null == c )
            c = new Color(0xD0CCBC);
        GRID_COLORS[7] = c;
    }
    
    /**
     * Used to generate drop shadown.
     */
    private final ConvolveOp blur;
    
    /**
     * MessageFormat used to generate text.
     */
    private final MessageFormat format;

    /**
     * Data for the graph as a percentage of the heap used.
     */
    private float[] graph;
    
    /**
     * Index into graph for the next tick.
     */
    private int graphIndex;
    
    /**
     * If true, graph contains all valid data, otherwise valid data starts at
     * 0 and ends at graphIndex - 1.
     */
    private boolean graphFilled;
    
    /**
     * Last total heap size.
     */
    private long lastTotal;
    
    /**
     * Timer used to update data.
     */
    private Timer updateTimer;
    
    /**
     * Image containing the background gradient and tiles.
     */
    private Image bgImage;
    
    /**
     * Width data is cached at.
     */
    private int cachedWidth;

    /**
     * Height data is cached at.
     */
    private int cachedHeight;
    
    /**
     * Image containing text.
     */
    private BufferedImage textImage;
    
    /**
     * Image containing the drop shadow.
     */
    private BufferedImage dropShadowImage;
    
    /**
     * Timer used to animate heap size growing.
     */
    private HeapGrowTimer heapGrowTimer;
    
    /**
     * Max width needed to display 999.9/999.9MB. Used to calcualte pref size.
     */
    private int maxTextWidth;
    
    /**
     * Current text being displayed.
     */
    private String heapSizeText;

    /**
     * Image containing gradient for ticks.
     */
    private Image tickGradientImage;

    /**
     * Image drawn on top of the ticks.
     */
    private BufferedImage gridOverlayImage;

    private final RequestProcessor RP = new RequestProcessor(HeapView.class.getName());
    
    private static final String TICK_STYLE = "tickStyle";
    private static final String SHOW_TEXT = "showText";
    private static final String DROP_SHADOW = "dropShadow";
    
    public HeapView() {
        // Configure structures needed for rendering drop shadow.
        int kw = KERNEL_SIZE, kh = KERNEL_SIZE;
        float blurFactor = BLUR_FACTOR;
        float[] kernelData = new float[kw * kh];
        for (int i = 0; i < kernelData.length; i++) {
            kernelData[i] = blurFactor;
        }
        blur = new ConvolveOp(new Kernel(kw, kh, kernelData));
        format = new MessageFormat("{0,choice,0#{0,number,0.0}|999<{0,number,0}}/{1,choice,0#{1,number,0.0}|999<{1,number,0}}MB");
        heapSizeText = "";
        // Enable mouse events. This is the equivalent to adding a mouse
        // listener.
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        setToolTipText(NbBundle.getMessage(GarbageCollectAction.class, "CTL_GC"));
        updateUI();
    }
    
    /**
     * Overriden to return true, GCComponent paints in its entire bounds in
     * an opaque manner.
     */
    @Override public boolean isOpaque() {
        return true;
    }
    
    /**
     * Updates the look and feel for this component.
     */
    @Override public void updateUI() {
        Font f = new JLabel().getFont();
        f = new Font(f.getName(), Font.BOLD, f.getSize());
        setFont(f);
        revalidate();
        repaint();
    }
    
    /**
     * Sets the style used to draw the ticks. The default is
     * STYLE_DEFAULT.
     *
     * @param style the tick style, one of STYLE_DEFAULT or
     *        STYLE_OVERLAY
     */
    public void setTickStyle(int style) {
        prefs().putInt(TICK_STYLE, style);
        repaint();
    }

    /**
     * Returns the style used to draw ticks.
     *
     * @return the style used to draw ticks, one of STYLE_DEFAULT or
     *         STYLE_OVERLAY
     */
    public int getTickStyle() {
        return prefs().getInt(TICK_STYLE, STYLE_OVERLAY);
    }
    
    /**
     * Sets whether the text displaying the heap size should be shown. The
     * default is true.
     *
     * @param showText whether the text displaying the heap size should be
     *        shown.
     */
    public void setShowText(boolean showText) {
        prefs().putBoolean(SHOW_TEXT, showText);
        repaint();
    }
    
    /**
     * Returns whether the text displaying the heap size should be shown.
     *
     * @return whether the text displaying the heap size should be shown
     */
    public boolean getShowText() {
        return prefs().getBoolean(SHOW_TEXT, true);
    }

    /**
     * Sets whether a drop shadow should be shown around the text. The default
     * is true.
     *
     * @param show whether a drop shadow should be shown around the text
     */
    public void setShowDropShadow(boolean show) {
        prefs().putBoolean(DROP_SHADOW, show);
        repaint();
    }

    /**
     * Returns whether a drop shadow should be shown around the text.
     */
    public boolean getShowDropShadow() {
        return prefs().getBoolean(DROP_SHADOW, true);
    }

    /**
     * Sets the font used to display the heap size.
     *
     * @param font the font used to display the heap size
     */
    @Override public void setFont(Font font) {
        super.setFont(font);
        updateTextWidth();
    }
    
    Dimension heapViewPreferredSize() {
        Dimension size = new Dimension(maxTextWidth + 8, getFontMetrics(
                getFont()).getHeight() + 8);
        return size;
    }

    private Preferences prefs() {
        return NbPreferences.forModule(HeapView.class);
    }
    
    /**
     * Recalculates the width needed to display the heap size string.
     */
    private void updateTextWidth() {
        String maxString = format.format(new Object[] {
            new Float(888.8f), new Float(888.8f) });
        maxTextWidth = getFontMetrics(getFont()).stringWidth(maxString) + 4;
    }

    /**
     * Processes a mouse event.
     *
     * @param e the MouseEvent
     */
    @Override protected void processMouseEvent(MouseEvent e) {
        super.processMouseEvent(e);
        if (!e.isConsumed()) {
            if (e.isPopupTrigger()) {
                // Show a popup allowing to configure the various options
                showPopup(e.getX(), e.getY());
            }  else if (e.getID() == e.MOUSE_ENTERED) {
                containsMouse = true;
                cachedBorderVaild = false;
                repaint();
            } else if (e.getID() == e.MOUSE_EXITED) {
                containsMouse = false;
                cachedBorderVaild = false;
                repaint();
            }

        } 
        
        if (e.getID() == MouseEvent.MOUSE_CLICKED &&
                SwingUtilities.isLeftMouseButton(e) && 
                e.getClickCount() == 1) {
            // Trigger a gc
            GarbageCollectAction.get(GarbageCollectAction.class).performAction();;
        }
    }

    /**
     * Shows a popup at the specified location that allows you to configure
     * the various options.
     */
    private void showPopup(int x, int y) {
        JPopupMenu popup = new JPopupMenu();
        JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(NbBundle.getMessage(HeapView.class, "LBL_ShowText"));
        cbmi.setSelected(getShowText());
        cbmi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setShowText(((JCheckBoxMenuItem)e.getSource()).
                        isSelected());
            }
        });
        popup.add(cbmi);
        cbmi = new JCheckBoxMenuItem(NbBundle.getMessage(HeapView.class, "LBL_DropShadow"));
        cbmi.setSelected(getShowDropShadow());
        cbmi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setShowDropShadow(((JCheckBoxMenuItem)e.getSource()).
                        isSelected());
            }
        });
        popup.add(cbmi);
        cbmi = new JCheckBoxMenuItem(NbBundle.getMessage(HeapView.class, "LBL_OverlayGrid"));
        cbmi.setSelected(getTickStyle() == STYLE_OVERLAY);
        cbmi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int style = ((JCheckBoxMenuItem)e.getSource()).
                        isSelected() ? STYLE_OVERLAY : STYLE_DEFAULT;
                setTickStyle(style);
            }
        });
        popup.add(cbmi);
        popup.show(this, x, y);
    }

    /**
     * Returns the first index to start rendering from.
     */
    private int getGraphStartIndex() {
        if (graphFilled) {
            return graphIndex;
        } else {
            return 0;
        }
    }

    /**
     * Paints the component.
     */
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        int width = getWidth();
        int height = getHeight();
        if (width - BORDER_W > 0 && height - BORDER_H > 0) {
            startTimerIfNecessary();
            updateCacheIfNecessary(width, height);
            paintCachedBackground(g2, width, height);
            g.translate(1, 2);
            if (containsMouse) {
                g.clipRect(1, 0, width - 4, height - 4);
            }
            else {
                g.clipRect(0, 0, width - 2, height - 4);
            }
            int innerW = width - BORDER_W;
            int innerH = height - BORDER_H;
            if (heapGrowTimer != null) {
                // Render the heap growing animation.
                Composite lastComposite = ((Graphics2D)g).getComposite();
                float percent = 1f - heapGrowTimer.getPercent();
                ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, percent));
                g.drawImage(heapGrowTimer.image, 0, 0, null);
                ((Graphics2D)g).setComposite(lastComposite);
            }
            paintTicks(g2, innerW, innerH);
            if (getTickStyle() == STYLE_OVERLAY) {
                g2.drawImage(getGridOverlayImage(), 0, 0, null);
            }
            if (getShowText()) {
                if (getShowDropShadow()) {
                    paintDropShadowText(g, innerW, innerH);
                } else {
                    g.setColor(textColor);
                    paintText(g, innerW, innerH);
                }
            }
            g.translate(-1, -2);
        } else {
            stopTimerIfNecessary();
            // To honor opaque contract, fill in the background
            g.setColor(getBackground());
            g.fillRect(0, 0, width, height);
        }
    }
    
    private void paintTicks(Graphics2D g, int width, int height) {
        if (graphIndex > 0 || graphFilled) {
            int index = getGraphStartIndex();
            int x = 0;
            if (!graphFilled) {
                x = width - graphIndex;
            }
            float[] localGraph = graph;
            if (localGraph == null) {
                return;
            }
            float min = localGraph[index];
            index = (index + 1) % localGraph.length;
            while (index != graphIndex) {
                min = Math.min(min, localGraph[index]);
                index = (index + 1) % localGraph.length;
            }
            int minHeight = (int)(min * (float)height);
            if (minHeight > 0) {
               g.drawImage(tickGradientImage, x, height - minHeight, width, height,
                        x, height - minHeight, width, height, null);
            }
            index = getGraphStartIndex();
            do {
                int tickHeight = (int)(localGraph[index] * (float)height);
                if (tickHeight > minHeight) {
                    g.drawImage(tickGradientImage, x, height - tickHeight, x + 1, height - minHeight,
                            x, height - tickHeight, x + 1, height - minHeight, null);
                }
                index = (index + 1) % localGraph.length;
                x++;
            } while (index != graphIndex);
        }
    }

    /**
     * Renders the text.
     */
    private void paintText(Graphics g, int w, int h) {
        g.setFont(getFont());
        String text = getHeapSizeText();
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g.drawString(text, (w - maxTextWidth) / 2 + (maxTextWidth - textWidth), 
                h / 2 + fm.getAscent() / 2 - 2);
    }

    /**
     * Renders the text using a drop shadow.
     */
    private void paintDropShadowText(Graphics g, final int w, final int h) {
        BufferedImage dsi = dropShadowImage;
        BufferedImage tsi = textImage;
        if (dsi != null && tsi != null) {
            // And finally copy it.
            Graphics2D blurryImageG = dsi.createGraphics();
            blurryImageG.setComposite(AlphaComposite.Clear);
            blurryImageG.fillRect(0, 0, w, h);
            blurryImageG.setComposite(AlphaComposite.SrcOver);
            blurryImageG.drawImage(tsi, blur, SHIFT_X, SHIFT_Y);
            blurryImageG.setColor(textColor);
            blurryImageG.setFont(getFont());

            // Step 3: render the text again on top.
            paintText(blurryImageG, w, h);
            blurryImageG.dispose();
            g.drawImage(dsi, 0, 0, null);
        } else {
            class InitTextAndDropShadow implements Runnable {
                @Override
                public void run() {
                    BufferedImage ti = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                    BufferedImage ds = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                    // Step 1: render the text.
                    Graphics2D textImageG = ti.createGraphics();
                    textImageG.setComposite(AlphaComposite.Clear);
                    textImageG.fillRect(0, 0, w, h);
                    textImageG.setComposite(AlphaComposite.SrcOver);
                    textImageG.setColor(textBlurColor);
                    paintText(textImageG, w, h);
                    textImageG.dispose();

                    textImage = ti;
                    dropShadowImage = ds;
                    repaint();
                }
            }
            RP.post(new InitTextAndDropShadow());
        }
    }
    
    private String getHeapSizeText() {
        return heapSizeText;
    }
    
    /**
     * Paints the grid on top of the ticks.
     */
    private void paintGridOverlay(Graphics2D g, int w, int h) {
        int numCells = GRID_COLORS.length / 2;
        int cellSize = (h - numCells - 1) / numCells;
        int c1 = 0xD0CCBC;
        int c2 = 0xEAE7D7;
        g.setPaint(new GradientPaint(
                0, 0, new Color((c1 >> 16) & 0xFF, (c1 >> 8) & 0xFF, c1 & 0xFF, 0x30),
                0, h, new Color((c2 >> 16) & 0xFF, (c2 >> 8) & 0xFF, c2 & 0xFF, 0x40)));
        for (int x = 0; x < w; x += cellSize + 1) {
            g.fillRect(x, 0, 1, h);
        }
        for (int y = h - cellSize - 1; y >= 0; y -= (cellSize + 1)) {
            g.fillRect(0, y, w, 1);
        }
    }

    private void paintCachedBackground(Graphics2D g, int w, int h) {
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, null);
        }
    }
    
    private void paintBackgroundTiles(Graphics2D g, int w, int h) {
        g.translate(1, 2);
        w -= BORDER_W;
        h -= BORDER_H;
        int numCells = GRID_COLORS.length / 2;
        int cellSize = (h - numCells - 1) / numCells;
        for (int i = 0; i < numCells; i++) {
            int colorIndex = i;
            int y = h - cellSize * (i + 1) - i;
            int x = 1;
            g.setPaint(new GradientPaint(0, y, GRID_COLORS[colorIndex * 2],
                    0, y + cellSize - 1, GRID_COLORS[colorIndex * 2 + 1]));
            while (x < w) {
                int endX = Math.min(w, x + cellSize);
                g.fillRect(x, y, endX - x, cellSize);
                x = endX + 1;
            }
            y += cellSize + 1;
        }
        g.translate(-1, -2);
    }
    
    private void paintBackground(Graphics2D g, int w, int h) {
        g.setPaint(new GradientPaint(0, 0, background1Color,
                0, h, background2Color));
        g.fillRect(0, 0, w, h);
    }
    
    private void paintBorder(Graphics g, int w, int h) {
        // Draw the border
        if (containsMouse) {
            g.setColor(border3Color);
            g.drawRect(0, 0, w - 1, h - 1);
            g.drawRect(1, 1, w - 3, h - 3);
        }
        else {
            g.setColor(border1Color);
            g.drawRect(0, 0, w - 1, h - 2);
            g.setColor(border2Color);
            g.fillRect(1, 1, w - 2, 1);
            g.setColor(border3Color);
            g.fillRect(0, h - 1, w, 1);
        }
    }
    
    private void updateCacheIfNecessary(int w, int h) {
        if (cachedWidth != w || cachedHeight != h || !cachedBorderVaild) {
            cachedWidth = w;
            cachedHeight = h;
            cachedBorderVaild = true;
            updateCache(w, h);
        }
    }
    
    private Image getGridOverlayImage() {
        if (gridOverlayImage == null) {
            gridOverlayImage = new BufferedImage(
                    getInnerWidth(), getInnerHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = gridOverlayImage.createGraphics();
            paintGridOverlay(g, getInnerWidth(), getInnerHeight());
            g.dispose();
        }
        return gridOverlayImage;
    }

    /**
     * Recreates the various state information needed for rendering.
     */
    private void updateCache(int w, int h) {
        disposeImages();
        textImage = null;
        dropShadowImage = null;
        bgImage = createImage(w, h);
        if (bgImage == null) {
            return;
        }
        Graphics2D imageG = (Graphics2D)bgImage.getGraphics();
        paintBackground(imageG, w, h);
        paintBackgroundTiles(imageG, w, h);
        paintBorder(imageG, w, h);
        imageG.dispose();
        w -= BORDER_W;
        h -= BORDER_H;
        if (graph == null || graph.length != w) {
            graph = new float[w];
            graphFilled = false;
            graphIndex = 0;
        }
        GradientPaint tickGradient = new GradientPaint(0, h, minTickColor,
                w, 0, maxTickColor);
        tickGradientImage = createImage(w, h);
        imageG = (Graphics2D)tickGradientImage.getGraphics();
        imageG.setPaint(tickGradient);
        imageG.fillRect(0, 0, w, h);
        imageG.dispose();
        if (gridOverlayImage != null) {
            gridOverlayImage.flush();
            gridOverlayImage = null;
        }
    }
    
    /**
     * Invoked when component removed from a heavy weight parent. Stops the
     * timer.
     */
    @Override public void removeNotify() {
        super.removeNotify();
        stopTimerIfNecessary();
    }
    
    /**
     * Restarts the timer.
     */
    private void startTimerIfNecessary() {
        if (!AUTOMATIC_REFRESH)
            return;
        
        if (updateTimer == null) {
            updateTimer = new Timer(TICK, new ActionHandler());
            updateTimer.setRepeats(true);
            updateTimer.start();
        }
    }
    
    /**
     * Stops the timer.
     */
    private void stopTimerIfNecessary() {
        if (updateTimer != null) {
            graph = null;
            graphFilled = false;
            updateTimer.stop();
            updateTimer = null;
            lastTotal = 0;
            disposeImages();
            cachedHeight = cachedHeight = -1;
            if (heapGrowTimer != null) {
                heapGrowTimer.stop();
                heapGrowTimer = null;
            }
        }
    }

    private void disposeImages() {
        if (bgImage != null) {
            bgImage.flush();
            bgImage = null;
        }
        if (textImage != null) {
            textImage.flush();
            textImage = null;
        }
        if (dropShadowImage != null) {
            dropShadowImage.flush();
            dropShadowImage = null;
        }
        if (tickGradientImage != null) {
            tickGradientImage.flush();
            tickGradientImage = null;
        }
        if (gridOverlayImage != null) {
            gridOverlayImage.flush();
            gridOverlayImage = null;
        }
    }
    
    /**
     * Invoked when the update timer fires. Updates the necessary data
     * structures and triggers repaints.
     */
    private void update() {
        if (!isShowing()) {
            // Either we've become invisible, or one of our ancestors has.
            // Stop the timer and bale. Next paint will trigger timer to
            // restart.
            stopTimerIfNecessary();
            return;
        }
        Runtime r = Runtime.getRuntime();
        long total = r.totalMemory();
        float[] localGraph = graph;
        if (localGraph == null) {
            return;
        }
        if (total != lastTotal) {
            if (lastTotal != 0) {
                // Total heap size has changed, start an animation.
                startHeapAnimate();
                // Readjust the graph size based on the new max.
                int index = getGraphStartIndex();
                do {
                    localGraph[index] = (float)(((double)localGraph[index] *
                            (double)lastTotal) / (double)total);
                    index = (index + 1) % localGraph.length;
                } while (index != graphIndex);
            }
            lastTotal = total;
        }
        if (heapGrowTimer == null) {
            // Not animating a heap size change, update the graph data and text.
            long used = total - r.freeMemory();
            localGraph[graphIndex] = (float)((double)used / (double)total);
            graphIndex = (graphIndex + 1) % localGraph.length;
            if (graphIndex == 0) {
                graphFilled = true;
            }
            heapSizeText = format.format(
                    new Object[] { new Double((double)used / 1024 / 1024),
                                   new Double((double)total / 1024 / 1024) });
        }
        repaint();
    }
    
    private void startHeapAnimate() {
        if (heapGrowTimer == null) {
            heapGrowTimer = new HeapGrowTimer();
            heapGrowTimer.start();
        }
    }
    
    private void stopHeapAnimate() {
        if (heapGrowTimer != null) {
            heapGrowTimer.stop();
            heapGrowTimer = null;
        }
    }

    private int getInnerWidth() {
        return getWidth() - BORDER_W;
    }

    private int getInnerHeight() {
        return getHeight() - BORDER_H;
    }

    
    private final class ActionHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            update();
        }
    }
    
    
    private final class HeapGrowTimer extends Timer {
        private final long startTime;
        private float percent;
        BufferedImage image;
        
        HeapGrowTimer() {
            super(30, null);
            setRepeats(true);
            startTime = System.currentTimeMillis();
            percent = 0f;
            int w = getWidth() - BORDER_W;
            int h = getHeight() - BORDER_H;
            image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            paintTicks(g, w, h);
            g.dispose();
        }
        
        public float getPercent() {
            return percent;
        }
        
        @Override protected void fireActionPerformed(ActionEvent e) {
            long time = System.currentTimeMillis();
            long delta = Math.max(0L, time - startTime);
            if (delta > HEAP_GROW_ANIMATE_TIME) {
                stopHeapAnimate();
            } else {
                percent = (float)delta / (float)HEAP_GROW_ANIMATE_TIME;
                repaint();
            }
        }
    }
    private boolean containsMouse;
    private boolean cachedBorderVaild;
}
