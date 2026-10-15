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
package org.netbeans.modules.gsf.codecoverage;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.ToolTipManager;
import org.openide.awt.GraphicsUtils;

/**
 * Custom component for painting code coverage. I was initially using a JProgressBar, with the
 * BasicProgressBarUI associated with it (to get red/green colors set correctly even on OSX), but it
 * was pretty plain and ugly looking - no nice gradients etc. Hence this component.
 *
 * @todo Add a getBaseline
 *
 * @author Tor Norbye
 */
public class CoverageBar extends JComponent {

    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color NOT_COVERED_COLOR = new Color(180, 50, 50);
    private static final Color COVERED_COLOR = new Color(30, 180, 30);
    private boolean emphasize;
    private boolean selected;
    /**
     * Coverage percentage: 0.0f <= x <= 100f
     */
    private float coveragePercentage;
    private int totalLines;
    private int executedLines;
    private int partialLines;
    private int inferredLines;

    public CoverageBar() {
        addHierarchyListener((HierarchyEvent e) -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (isShowing()) {
                    ToolTipManager.sharedInstance().registerComponent(CoverageBar.this);
                } else {
                    ToolTipManager.sharedInstance().unregisterComponent(CoverageBar.this);
                }
            }
        });
        updateUI();
    }

    public float getCoveragePercentage() {
        return coveragePercentage;
    }

    public void setCoveragePercentage(float coveragePercentage) {
        this.coveragePercentage = coveragePercentage;
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
        return String.format("%.2f %%", coveragePercentage); // NOI18N
    }

    @Override
    public boolean isOpaque() {
        return true;
    }

    @Override
    public final void updateUI() {
        Font f = new JLabel().getFont();
        f = new Font(f.getName(), Font.BOLD, f.getSize());
        setFont(f);
        revalidate();
        repaint();
    }

    public @Override
    void paint(Graphics g) {
        // Antialiasing if necessary
        GraphicsUtils.configureDefaultRenderingHints(g);
        super.paint(g);
    }

    @Override
    protected void paintComponent(Graphics g) {

        if (!(g instanceof Graphics2D)) {
            return;
        }

        int width = getWidth();
        int barRectWidth = width;
        int height = getHeight();
        int barRectHeight = height;

        if (barRectWidth <= 0 || barRectHeight <= 0) {
            return;
        }

        // for font anti aliasing
        GraphicsUtils.configureDefaultRenderingHints(g);

        int amountFull = (int) (barRectWidth * coveragePercentage / 100.0f);

        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(getBackground());

        Color notCoveredDark = NOT_COVERED_COLOR;
        Color coveredDark = COVERED_COLOR;
        if (emphasize || selected) {
            coveredDark = coveredDark.darker();
            notCoveredDark = notCoveredDark.darker();
        }

        g2.setPaint(notCoveredDark);
        g2.fillRect(amountFull, 1, width - 1, height - 1);

        if (coveragePercentage > 0.0f) {
            g2.setColor(coveredDark);
            g2.fillRect(1, 1, amountFull, height - 1);
        }

        paintText(g2, barRectWidth, barRectHeight);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size;
        Insets border = getInsets();
        FontMetrics fontSizer = getFontMetrics(getFont());

        size = new Dimension(146, 12);
        String string = getString();
        int stringWidth = fontSizer.stringWidth(string);
        if (stringWidth > size.width) {
            size.width = stringWidth;
        }
        int stringHeight = fontSizer.getHeight()
            + fontSizer.getDescent();
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

    private void paintText(Graphics g, int w, int h) {
        if (emphasize) {
            g.setColor(Color.YELLOW);
        } else {
            g.setColor(TEXT_COLOR);
        }
        g.setFont(getFont());
        String text = getString();
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g.drawString(text, (w - textWidth) / 2,
            h - fm.getDescent() - ((h - fm.getHeight()) / 2));
    }

    public void setStats(int totalLines, int executedLines, int partialLines, int inferredLines) {
        this.totalLines = totalLines;
        this.executedLines = executedLines;
        this.partialLines = partialLines;
        this.inferredLines = inferredLines;
    }

    @Override
    public String getToolTipText(MouseEvent arg0) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>"); // NOI18N
        sb.append("Total Lines: ");
        sb.append(Integer.toString(totalLines));
        sb.append("<br>"); // NOI18N
        sb.append("Executed Lines: ");
        sb.append(Integer.toString(executedLines));
        sb.append("<br>"); // NOI18N
        if (partialLines >= 0) {
            sb.append("&nbsp;&nbsp;"); // NOI18N
            sb.append("Partial Lines: ");
            sb.append(Integer.toString(partialLines));
            sb.append("<br>"); // NOI18N
        }
        if (inferredLines >= 0) {
            sb.append("&nbsp;&nbsp;"); // NOI18N
            sb.append("Inferred Executed Lines: ");
            sb.append(Integer.toString(inferredLines));
            sb.append("<br>"); // NOI18N
        }

        sb.append("Not Executed Lines: ");
        int notExecutedLines = totalLines - executedLines;
        sb.append(Integer.toString(notExecutedLines));
        sb.append("<br>"); // NOI18N
        return sb.toString();
    }

}
