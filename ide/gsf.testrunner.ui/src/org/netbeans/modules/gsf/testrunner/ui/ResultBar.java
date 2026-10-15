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
package org.netbeans.modules.gsf.testrunner.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.Timer;
import org.openide.awt.GraphicsUtils;

/**
 * <strong>This is a copy of <code>CoverageBar</code> from the gsf.codecoverage</code>
 * module with minor changes only. TODO is to look at to which API the class could be put.</strong>.
 * <p/>
 *
 * Custom component for painting code coverage.
 * I was initially using a JProgressBar, with the BasicProgressBarUI associated with it
 * (to get red/green colors set correctly even on OSX), but it was pretty plain
 * and ugly looking - no nice gradients etc. Hence this component.
 *
 * @author Tor Norbye
 */
public final class ResultBar extends JComponent implements ActionListener {

    private static final Color NOT_COVERED_COLOR = new Color(180, 50, 50);
    private static final Color COVERED_COLOR = new Color(30, 180, 30);
    private static final Color NO_TESTS_COLOR = new Color(110, 110, 110);
    private static final Color ABORTED_TESTS_COLOR = new Color(214, 157, 41);
    private static final Color TEXT_COLOR = new Color(255, 255, 255);
    private static final Color ANIMATION_COLOR = new Color(190, 190, 190);

    /** Passed tests percentage:  0.0f <= x <= 100f */
    private float passedPercentage = 0.0f;
    /** Skipped tests percentage:  0.0f <= x <= 100f */
    private float skippedPercentage = 0.0f;
    /** Aborted tests percentage:  0.0f <= x <= 100f */
    private float abortedPercentage = 0.0f;

    private final Timer timer = new Timer(100, this);
    private final long startTime;
    private boolean passedReported = false;
    private boolean skippedReported = false;
    private boolean abortedReported = false;

    public ResultBar() {
        updateUI();
        timer.start();
        startTime = System.currentTimeMillis();
    }

    public void stop(){
        timer.stop();
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    public float getPassedPercentage() {
        return passedPercentage;
    }

    public void setPassedPercentage(float passedPercentage) {
        this.passedPercentage = Float.isNaN(passedPercentage) ? 0.0f : passedPercentage; // #167230
        this.passedReported = true;
        repaint();
    }

    public void setSkippedPercentage(float skippedPercentage) {
        this.skippedPercentage = Float.isNaN(skippedPercentage) ? 0.0f : skippedPercentage; // #167230
        this.skippedReported = true;
        repaint();
    }

    public void setAbortedPercentage(float abortedPercentage) {
        this.abortedPercentage = Float.isNaN(abortedPercentage) ? 0.0f : abortedPercentage; // #167230
        this.abortedReported = true;
        repaint();
    }

    private String getString() {
        // #183996 (PHP project) requires to use the format "%.2f".
        // It lets to have not rounding a value if number of tests <= 10000
        
        // make it clearer what the shown percentage stands for, since now
        // the color bar schema has been changed/simplyfied
        return "Tests passed: ".concat(String.format("%.2f %%", passedPercentage)); // NOI18N
    }

    @Override
    public boolean isOpaque() {
        return true;
    }

    @Override
    public void updateUI() {
        Font f = new JLabel().getFont();
        f = new Font(f.getName(), Font.BOLD, f.getSize());
        setFont(f);
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!(g instanceof Graphics2D)) {
            return;
        }
        // Antialiasing if necessary
        GraphicsUtils.configureDefaultRenderingHints(g);

        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        // running with no results yet -> gray
        Color fillColor = NO_TESTS_COLOR;

        if (abortedReported || skippedReported || passedReported) {
            // running with at least one result or finished
            if (passedPercentage == 100.0) {
                // contains only successful tests -> green
                fillColor = COVERED_COLOR;
            } else if (abortedPercentage > 0.0) {
                // contains aborted tests -> abort color
                fillColor = ABORTED_TESTS_COLOR;
            } else if(100.0f - passedPercentage - abortedPercentage - skippedPercentage > 0.0001) {
                // contains failed tests -> red
                fillColor = NOT_COVERED_COLOR;
            } else if (skippedPercentage > 0.0) {
                // contains ignored tests -> gray
                fillColor = NO_TESTS_COLOR;
            }
        }
        g2.setPaint(fillColor);
        g2.fillRect(0, 0, width-1, height-1);

        if (timer.isRunning()) {
            g2.setPaint(ANIMATION_COLOR);
            float step = (System.currentTimeMillis()-startTime) / 150.0f;
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] {10.0f}, step));
            g2.drawRect(2, 2, width-6, height-6);
        }

        paintText(g2, width, height);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size;
        Insets border = getInsets();
        FontMetrics fontSizer = getFontMetrics(getFont());
        Container parent = getParent(); // JToolBar registered in ResultPanelTree
        Insets insets = parent.getInsets();
        size = new Dimension(parent.getWidth() - insets.left - insets.right, parent.getHeight()- insets.top - insets.bottom);
        String string = getString();
        int stringWidth = fontSizer.stringWidth(string);
        if (stringWidth > size.width) {
            size.width = stringWidth;
        }
        int stringHeight = fontSizer.getHeight() + fontSizer.getDescent();
        if (stringHeight > size.height) {
            size.height = stringHeight;
        }
        size.width += border.left + border.right;
        size.height += border.top + border.bottom;
        
        return size;
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension pref = getPreferredSize();
        pref.width = 40;
        return pref;
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension pref = getPreferredSize();
        pref.width = Short.MAX_VALUE;
        return pref;
    }

    @Override
    public int getBaseline(int w, int h) {
        FontMetrics fm = getFontMetrics(getFont());
        return h - fm.getDescent() - ((h - fm.getHeight()) / 2);
    }

    /**
     * Renders the text.
     */
    private void paintText(Graphics2D g, int w, int h) {
        // Similar to org.openide.actions.HeapView.paintText.
        Font font = getFont();
        String text = getString();
        GlyphVector gv = font.createGlyphVector(g.getFontRenderContext(), text);
        FontMetrics fm = g.getFontMetrics(font);
        Shape outline = gv.getOutline();
        Rectangle2D bounds = outline.getBounds2D();
        double x = Math.max(0, (w - bounds.getWidth()) / 2.0);
        double y = h / 2.0 + fm.getAscent() / 2.0 - 2;
        AffineTransform oldTransform = g.getTransform();
        g.translate(x, y);
        g.setColor(TEXT_COLOR);
        g.fill(outline);
        g.setTransform(oldTransform);
    }
}
