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
package org.netbeans.modules.gsf.testrunner.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
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
 * @todo Add a getBaseline
 *
 * @author Tor Norbye
 */
public final class ResultBar extends JComponent implements ActionListener{
    private static final Color NOT_COVERED_LIGHT = new Color(255, 160, 160);
    private static final Color NOT_COVERED_DARK = new Color(180, 50, 50);
    private static final Color COVERED_LIGHT = new Color(160, 255, 160);
    private static final Color COVERED_DARK = new Color(30, 180, 30);
    private static final Color NO_TESTS_LIGHT = new Color(200, 200, 200);
    private static final Color NO_TESTS_DARK = new Color(110, 110, 110);
    private static final Color ABORTED_TESTS_LIGHT = new Color(246, 232, 206);
    private static final Color ABORTED_TESTS_DARK = new Color(214, 157, 41);
    private boolean emphasize;
    private boolean selected;
    /** Passed tests percentage:  0.0f <= x <= 100f */
    private float passedPercentage = 0.0f;
    /** Skipped tests percentage:  0.0f <= x <= 100f */
    private float skippedPercentage = 0.0f;
    /** Aborted tests percentage:  0.0f <= x <= 100f */
    private float abortedPercentage = 0.0f;

    private Timer timer = new Timer(100, this);
    private int phase = 1;
    private boolean passedReported = false;
    private boolean skippedReported = false;
    private boolean abortedReported = false;

    public ResultBar() {
        updateUI();
        timer.start();
    }

    public void stop(){
        timer.stop();
    }

    public void actionPerformed(ActionEvent e) {
        phase = (phase < getHeight()-1) ? phase + 1 : 1;
        repaint();
    }

    public float getPassedPercentage() {
        return passedPercentage;
    }

    public void setPassedPercentage(float passedPercentage) {
        if(Float.isNaN(passedPercentage)) { // #167230
            passedPercentage = 0.0f;
        }
        this.passedPercentage = passedPercentage;
        this.passedReported = true;
        repaint();
    }

    public void setSkippedPercentage(float skippedPercentage) {
        if(Float.isNaN(skippedPercentage)) { // #167230
            skippedPercentage = 0.0f;
        }
        this.skippedPercentage = skippedPercentage;
        this.skippedReported = true;
        repaint();
    }

    public void setAbortedPercentage(float abortedPercentage) {
        if(Float.isNaN(abortedPercentage)) { // #167230
            abortedPercentage = 0.0f;
        }
        this.abortedPercentage = abortedPercentage;
        this.abortedReported = true;
        repaint();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isEmphasize() {
        return emphasize;
    }

    public void setEmphasize(boolean emphasize) {
        this.emphasize = emphasize;
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
        int barRectWidth = width;
        int height = getHeight();
        int barRectHeight = height;

        if (barRectWidth <= 0 || barRectHeight <= 0) {
            return;
        }

        int amountFull = (int) (barRectWidth * passedPercentage / 100.0f);
        int amountSkip = (int) (barRectWidth * skippedPercentage / 100.0f);
        int amountAbort = (int) (barRectWidth * abortedPercentage / 100.0f);
        int amountFail = Math.abs(barRectWidth - amountFull - amountSkip - amountAbort);
        if(amountFail <= 1) {
            amountFail = 0;
        }

        Color notCoveredLight = NOT_COVERED_LIGHT;
        Color notCoveredDark = NOT_COVERED_DARK;
        Color coveredLight = COVERED_LIGHT;
        Color coveredDark = COVERED_DARK;
        Color noTestsLight = NO_TESTS_LIGHT;
        Color noTestsDark = NO_TESTS_DARK;
        Color abortedTestsLight = ABORTED_TESTS_LIGHT;
        Color abortedTestsDark = ABORTED_TESTS_DARK;
        if (emphasize) {
            coveredDark = coveredDark.darker();
            notCoveredDark = notCoveredDark.darker();
            noTestsDark = noTestsDark.darker();
            abortedTestsDark = abortedTestsDark.darker();
        } else if (selected) {
            coveredLight = coveredLight.brighter();
            coveredDark = coveredDark.darker();
            notCoveredLight = notCoveredLight.brighter();
            notCoveredDark = notCoveredDark.darker();
            noTestsLight = noTestsLight.brighter();
            noTestsDark = noTestsDark.darker();
            abortedTestsLight = abortedTestsLight.brighter();
            abortedTestsDark = abortedTestsDark.darker();
        }
        Graphics2D g2 = (Graphics2D) g;
        // running with no results yet -> gray
        Color light = noTestsLight;
        Color dark = noTestsDark;

        if (abortedReported || skippedReported || passedReported) {
            // running with at least one result or finished
            if (passedPercentage == 100.0) {
                // contains only successful tests -> green
                light = coveredLight;
                dark = coveredDark;
            } else if (abortedPercentage > 0.0) {
                // contains aborted tests -> abort color
                light = abortedTestsLight;
                dark = abortedTestsDark;
            } else if(100.0f - passedPercentage - abortedPercentage - skippedPercentage > 0.0001) {
                // contains failed tests -> red
                light = notCoveredLight;
                dark = notCoveredDark;
            } else if (skippedPercentage > 0.0) {
                // contains ignored tests -> gray
                light = noTestsLight;
                dark = noTestsDark;
            }
        }
        g2.setPaint(new GradientPaint(0, phase, light, 0, phase + height / 2, dark, true));
        g2.fillRect(0, 0, barRectWidth, height);

        paintText(g2, barRectWidth, barRectHeight);
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
        int stringHeight = fontSizer.getHeight() +
                fontSizer.getDescent();
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

    //@Override JDK6
    public int getBaseline(int w, int h) {
        FontMetrics fm = getFontMetrics(getFont());
        return h - fm.getDescent() - ((h - fm.getHeight()) / 2);
    }

    /**
     * Renders the text with a slightly contrasted outline.
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
        double y = h / 2.0 + fm.getAscent() / 2.0 - 1;
        AffineTransform oldTransform = g.getTransform();
        g.translate(x, y);
        g.setColor(new Color(0, 0, 0, 100));
        g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(outline);
        g.setColor(Color.WHITE);
        g.fill(outline);
        g.setTransform(oldTransform);
    }
}
