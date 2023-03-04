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
package org.openide.actions;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.prefs.Preferences;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Implements a widget to show the current heap size / max and to trigger a manual GC.
 * Appearance can be customized using the following properties in the LAF
 * <ul>
 * <li> nb.heapview.background - Color of widget background
 * <li> nb.heapview.foreground - Color of text
 * <li> nb.heapview.chart - Color of area chart
 * <li> nb.heapview.highlight - Color of outline around the text, to provide a contrast against
 *                               the chart (may have a non-opaque alpha value)
 * </ul>
 * @author sky, radim, peter
 */
class HeapView extends JComponent {

    private static final boolean AUTOMATIC_REFRESH = System.getProperty("org.netbeans.log.startup") == null;

    /*
     * How often the display is updated.
     */
    private static final int TICK = 1500;

    /**
     * Foreground color for the chart.
     */
    private static final Color CHART_COLOR;

    /**
     * Color for text.
     */
    private static final Color TEXT_COLOR;

    /**
     * Color for an outline around the text.
     */
    private static final Color OUTLINE_COLOR;

    /**
     * Color for the background.
     */
    private static final Color BACKGROUND_COLOR;

    /**
     * Number of samples to retain in history.
     */
    private static final int GRAPH_COUNT = 100;
    
    /**
     * Key for the Show Text preference.
     */
    private static final String SHOW_TEXT = "showText";

    static {
        //init colors
        Color c = UIManager.getColor("nb.heapview.chart"); //NOI18N
        if (null == c) {
            c = new Color(0x2E90E8);
        }
        CHART_COLOR = c;

        c = UIManager.getColor("nb.heapview.foreground"); //NOI18N
        if (null == c) {
            c = Color.DARK_GRAY;
        }
        TEXT_COLOR = c;

        c = UIManager.getColor("nb.heapview.background"); //NOI18N
        if (null == c) {
            c = new Color(0xCEDBE6);
        }
        BACKGROUND_COLOR = c;

        c = UIManager.getColor("nb.heapview.highlight"); //NOI18N
        if (null == c) {
            c = new Color(BACKGROUND_COLOR.getRed(),
                    BACKGROUND_COLOR.getGreen(),
                    BACKGROUND_COLOR.getBlue(),
                    192);
        }
        OUTLINE_COLOR = c;
    }

    /**
     * MessageFormat used to generate text.
     */
    private final MessageFormat format;

    /**
     * Data for the graph as a percentage of the heap used.
     * It is a circular buffer.
     */
    private final long[] graph = new long[GRAPH_COUNT];

    /**
     * Index into graph for the next tick.
     */
    private int graphIndex;

    /**
     * Last total heap size.
     */
    private long lastTotal;

    /**
     * Timer used to update data.
     */
    private Timer updateTimer;

    /**
     * Max width needed to display 999.9/999.9MB. Used to calculate pref size.
     */
    private int maxTextWidth;

    /**
     * Current text being displayed.
     */
    private String heapSizeText;


    public HeapView() {
        format = new MessageFormat("{0,choice,0#{0,number,0.0}|999<{0,number,0}}/{1,choice,0#{1,number,0.0}|999<{1,number,0}}MB");
        heapSizeText = "";
        // Enable mouse events. This is the equivalent to adding a mouse
        // listener.
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        setToolTipText(NbBundle.getMessage(GarbageCollectAction.class, "CTL_GC"));
        updateUI();
    }

    /**
     * Updates the look and feel for this component.
     */
    @Override
    public void updateUI() {
        Font f = UIManager.getFont("Label.font");
        setFont(f);
        /* Setting this true seems to cause some painting artifacts on 150% scaling, as we don't
        always manage to fill every device pixel with the background color. So leave it off. */
        setOpaque(false);
    }

    /**
     * Sets whether the text displaying the heap size should be shown. The
     * default is true.
     *
     * @param showText whether the text displaying the heap size should be
     * shown.
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
     * Sets the font used to display the heap size.
     *
     * @param font the font used to display the heap size
     */
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        updateTextWidth();
    }

    // Called by GarbageCollectAction
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
        String maxString = format.format(new Object[]{888.8f, 888.8f});
        maxTextWidth = getFontMetrics(getFont()).stringWidth(maxString) + 4;
    }

    /**
     * Processes a mouse event.
     *
     * @param e the MouseEvent
     */
    @Override
    protected void processMouseEvent(MouseEvent e) {
        super.processMouseEvent(e);
        if (!e.isConsumed()) {
            if (e.isPopupTrigger()) {
                // Show a popup allowing to configure the various options
                showPopup(e.getX(), e.getY());
            }
        }

        if (e.getID() == MouseEvent.MOUSE_CLICKED
                && SwingUtilities.isLeftMouseButton(e)
                && e.getClickCount() == 1) {
            // Trigger a gc
            GarbageCollectAction.get(GarbageCollectAction.class).performAction();
        }
    }

    /**
     * Shows a popup at the specified location that allows you to configure the
     * various options.
     */
    private void showPopup(int x, int y) {
        JPopupMenu popup = new JPopupMenu();
        JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(NbBundle.getMessage(HeapView.class, "LBL_ShowText"));
        cbmi.setSelected(getShowText());
        cbmi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setShowText(((JCheckBoxMenuItem) e.getSource()).
                        isSelected());
            }
        });
        popup.add(cbmi);
        popup.show(this, x, y);
    }

    /**
     * Paints the component.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = getWidth();
        int height = getHeight();

        if (width > 0 && height > 0) {
            startTimerIfNecessary();
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g2.clipRect(0, 0, width, height);
                // Draw background.
                g2.setColor(BACKGROUND_COLOR);
                g2.fillRect(0, 0, width, height);
                // Draw samples
                g2.setColor(CHART_COLOR);
                paintSamples(g2, width, height);
                // Draw text if enabled
                if (getShowText()) {
                    paintText(g2, width, height);
                }
            } finally {
              g2.dispose();
            }
        } else {
            stopTimerIfNecessary();
        }
    }

    /**
     * Renders the text using an optional drop shadow.
     */
    private void paintText(Graphics2D g, int w, int h) {
        Font font = getFont();
        String text = getHeapSizeText();
        GlyphVector gv = font.createGlyphVector(g.getFontRenderContext(), text);
        FontMetrics fm = g.getFontMetrics(font);
        Shape outline = gv.getOutline();
        Rectangle2D bounds = outline.getBounds2D();
        double x = Math.max(0, (w - bounds.getWidth()) / 2.0);
        double y = h / 2.0 + fm.getAscent() / 2.0 - 2.0;
        AffineTransform oldTransform = g.getTransform();
        g.translate(x, y);
        g.setColor(OUTLINE_COLOR);
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(outline);
        g.setColor(TEXT_COLOR);
        g.fill(outline);
        g.setTransform(oldTransform);
    }

    private String getHeapSizeText() {
        return heapSizeText;
    }

    /**
     * Invoked when component removed from a heavy weight parent. Stops the
     * timer.
     */
    @Override
    public void removeNotify() {
        super.removeNotify();
        stopTimerIfNecessary();
    }

    /**
     * Restarts the timer.
     */
    private void startTimerIfNecessary() {
        if (!AUTOMATIC_REFRESH) {
            return;
        }
        if (updateTimer == null) {
            updateTimer = new Timer(TICK, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    update();
                }
            });
            updateTimer.setRepeats(true);
            updateTimer.start();
        }
    }

    /**
     * Stops the timer.
     */
    private void stopTimerIfNecessary() {
        if (updateTimer != null) {
            updateTimer.stop();
            updateTimer = null;
            lastTotal = 0;
            Arrays.fill(graph, 0);
            heapSizeText = "";
        }
    }

    /**
     * Invoked when the update timer fires. Updates the necessary data
     * structures and triggers repaints.
     */
    private void update() {
        if (isShowing()) {
            Runtime r = Runtime.getRuntime();
            long total = r.totalMemory();
            long used = total - r.freeMemory();
            graph[graphIndex] = used;
            lastTotal = total;
            ++graphIndex;
            if (graphIndex >= GRAPH_COUNT) {
                graphIndex = 0;
            }
            heapSizeText = format.format(
                    new Object[]{(double) used / (1024.0 * 1024.0), (double) total / (1024.0 * 1024.0)});
            repaint();
        } else {
            // Either we've become invisible, or one of our ancestors has.
            // Stop the timer and bale. Next paint will trigger timer to
            // restart.
            stopTimerIfNecessary();
        }
    }

    /**
     * Draw the graph with the heap samples. It is a simple area chart.
     *
     * @param g Where to draw
     * @param width The width of the chart
     * @param height The height of the chart
     */
    private void paintSamples(Graphics2D g, int width, int height) {
        Path2D path = new Path2D.Double();
        path.moveTo(0, height);
        for (int i = 0; i < GRAPH_COUNT; ++i) {
            int index = (i + graphIndex) % GRAPH_COUNT;
            double x = (double) i / (double) (GRAPH_COUNT - 1) * (double) width;
            double y = (double) height * (1.0 - (double) graph[index] / (double) lastTotal);
            path.lineTo(x, y);
        }
        path.lineTo(width, height);
        path.closePath();
        g.fill(path);
    }
}
