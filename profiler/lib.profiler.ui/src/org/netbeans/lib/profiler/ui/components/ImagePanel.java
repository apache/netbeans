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

package org.netbeans.lib.profiler.ui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.PixelGrabber;
import javax.swing.JPanel;
import javax.swing.SwingConstants;


/**
 *
 * @author Jiri Sedlacek
 */
public class ImagePanel extends JPanel {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static MediaTracker mTracker = new MediaTracker(new JPanel());

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Image image;
    private int imageAlign; // Use SwingConstants.TOP, BOTTOM (LEFT & RIGHT not implemented)

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ImagePanel(Image image) {
        this(image, SwingConstants.TOP);
    }

    public ImagePanel(Image image, int imageAlign) {
        setImage(image);
        setImageAlign(imageAlign);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setImage(Image image) {
        this.image = loadImage(image);

        if (this.image == null) {
            throw new RuntimeException("Failed to load image"); // NOI18N
        }

        setPreferredBackground();
        setPreferredSize(new Dimension(this.image.getWidth(null), this.image.getHeight(null)));

        refresh();
    }

    public void setImageAlign(int imageAlign) {
        this.imageAlign = imageAlign;

        setPreferredBackground();

        refresh();
    }

    protected static Image loadImage(Image image) {
        mTracker.addImage(image, 0);

        try {
            mTracker.waitForID(0);
        } catch (InterruptedException e) {
            return null;
        }

        mTracker.removeImage(image, 0);

        return image;
    }

    protected void setPreferredBackground() {
        int[] pixels = new int[1];

        PixelGrabber pg = null;

        switch (imageAlign) {
            case (SwingConstants.TOP):
                pg = new PixelGrabber(image, 0, image.getHeight(null) - 1, 1, 1, pixels, 0, 1);

                break;
            case (SwingConstants.BOTTOM):
                pg = new PixelGrabber(image, 0, 0, 1, 1, pixels, 0, 1);

                break;
            default:
                pg = new PixelGrabber(image, 0, image.getHeight(null) - 1, 1, 1, pixels, 0, 1);
        }

        try {
            if ((pg != null) && pg.grabPixels()) {
                setBackground(new Color(pixels[0]));
            }
        } catch (InterruptedException e) {
        }
    }

    protected void paintComponent(Graphics graphics) {
        graphics.setColor(getBackground());
        graphics.fillRect(0, 0, getWidth(), getHeight());

        switch (imageAlign) {
            case (SwingConstants.TOP):
                graphics.drawImage(image, (getWidth() - image.getWidth(null)) / 2, 0, this);

                break;
            case (SwingConstants.BOTTOM):
                graphics.drawImage(image, (getWidth() - image.getWidth(null)) / 2, getHeight() - image.getHeight(null), this);

                break;
            default:
                graphics.drawImage(image, (getWidth() - image.getWidth(null)) / 2, 0, this);
        }
    }

    private void refresh() {
        if (isShowing()) {
            invalidate();
            repaint();
        }
    }
}
