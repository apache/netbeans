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
package org.netbeans.modules.svg.navigation;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.SVGRenderingHints;
import com.github.weisj.jsvg.geometry.size.FloatSize;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.netbeans.modules.svg.SVGViewerElement;
import org.openide.awt.GraphicsUtils;
import org.openide.util.NbBundle;

/**
 * JPanel used for SVG preview in Navigator window
 *
 * @author christian lenz
 */
public class SVGPreviewPanel extends JPanel {

    private static final Logger LOG = Logger.getLogger(SVGViewerElement.class.getName());

    private SVGDocument svgDocument;
    private final int stringGapSize = 10;
    private final Color background = UIManager.getColor("Table.background");
    private final Color foreground = UIManager.getColor("Table.foreground");

    public void setSVG(SVGDocument svgDoc) {
        this.svgDocument = svgDoc;
        this.setBackground(background);
        this.revalidate();
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        GraphicsUtils.configureDefaultRenderingHints(g);
        super.paintComponent(g);

        if (svgDocument != null) {
            g.setColor(foreground);

            FloatSize size = svgDocument.size();
            int originalWidth = (int) size.getWidth();
            int originalHeight = (int) size.getHeight();
            String sizes = "Dimensions: " + originalWidth + " x " + originalHeight;

            g.drawString(sizes, (int) (this.getWidth() * 0.05), this.getHeight() - stringGapSize);

            int thumbnailMaxSize = 100;

            double widthRatio = (double) originalWidth / thumbnailMaxSize;
            double heightRatio = (double) originalHeight / thumbnailMaxSize;
            double ratio = 1.0;

            if (widthRatio > 1 || heightRatio > 1) {
                ratio = widthRatio > heightRatio ? widthRatio : heightRatio;
            }

            int scaledWidth = (int) (originalWidth / ratio);
            int scaledHeight = (int) (originalHeight / ratio);
            Graphics2D g2d = (Graphics2D) g.create((this.getWidth() - scaledWidth) / 2, (this.getHeight() - scaledHeight) / 2, scaledWidth, scaledHeight);

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2d.setRenderingHint(SVGRenderingHints.KEY_MASK_CLIP_RENDERING, SVGRenderingHints.VALUE_MASK_CLIP_RENDERING_ACCURACY);

            g2d.scale(1.0 / ratio, 1.0 / ratio);

            try {
                svgDocument.render(this, g2d);
            } catch (Exception ex) {
                LOG.log(Level.INFO, ex.getMessage());

                drawErrorMessage(g);
            } finally {
                g2d.dispose();
            }
        } else {
            drawErrorMessage(g);
        }
    }

    private void drawErrorMessage(Graphics g) {
        g.setColor(Color.RED);

        FontMetrics fm = this.getFontMetrics(g.getFont());
        String errMessage = NbBundle.getMessage(SVGPreviewPanel.class, "ERR_Thumbnail");
        int stringWidth = fm.stringWidth(errMessage);

        g.drawString(errMessage, (this.getWidth() - stringWidth) / 2, this.getHeight() / 2);
        g.dispose();
    }
}
