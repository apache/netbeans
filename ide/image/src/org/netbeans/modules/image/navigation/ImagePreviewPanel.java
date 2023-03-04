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
package org.netbeans.modules.image.navigation;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.openide.util.NbBundle;

/**
 * JPanel used for image preview in Navigator window
 *
 * @author jpeska
 */
public class ImagePreviewPanel extends JPanel {

    BufferedImage image;
    private final int stringGapSize = 10;
    private final Color background = UIManager.getColor("Table.background");
    private final Color foreground = UIManager.getColor("Table.foreground");

    public void setImage(BufferedImage image) {
        this.image = image;
        this.setBackground(background);
        this.revalidate();
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.setColor(foreground);

            int width = image.getWidth();
            int height = image.getHeight();
            String sizes = "Dimensions: " + width + " x " + height;

            g.drawString(sizes, (int) (this.getWidth() * 0.05), this.getHeight() - stringGapSize);
            // adapt image width and height to the size of Navigator window
            double widthRatio = ((double) image.getWidth()) / (((double) this.getWidth()) * 0.9);
            double heightRatio = ((double) image.getHeight()) / (((double) this.getHeight()) * 0.9 - stringGapSize - 20);
            if (widthRatio > 1 || heightRatio > 1) {
                double ratio = widthRatio > heightRatio ? widthRatio : heightRatio;
                width = (int) (((double) image.getWidth()) / ratio);
                height = (int) (((double) image.getHeight()) / ratio);
            }
            g.drawImage(image, (this.getWidth() - width) / 2, (this.getHeight() - height) / 2, width, height, this);
        } else {
            g.setColor(Color.RED);
            FontMetrics fm = this.getFontMetrics(g.getFont()) ;
            String errMessage = NbBundle.getMessage(ImagePreviewPanel.class, "ERR_Thumbnail");
            int stringWidth = fm.stringWidth(errMessage);
            g.drawString(errMessage, (this.getWidth() - stringWidth) / 2, this.getHeight() / 2);
        }
    }
}
