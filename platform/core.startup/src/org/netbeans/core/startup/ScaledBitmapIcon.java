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
package org.netbeans.core.startup;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.CachedHiDPIIcon;
import org.openide.util.Parameters;

/**
 * An Icon implementation that scales a source image to the specified dimensions. Can be used to
 * produce sharp images on HiDPI displays, without relying on MultiResolutionImage, which exists
 * only since JDK 9. This also sidesteps https://bugs.openjdk.java.net/browse/JDK-8212226 on
 * Windows. It's recommended to use a source image with dimensions that are exactly twice those of
 * the icon's logical dimensions. A double-resolution source image will automatically be scaled
 * down to 1x, 1.5x, or other HiDPI scaling factors as needed.
 */
final class ScaledBitmapIcon extends CachedHiDPIIcon {
    private final Image sourceImage;
    private final int sourceWidth;
    private final int sourceHeight;

    public ScaledBitmapIcon(Image sourceImage, int width, int height) {
        super(width, height);
        Parameters.notNull("sourceImage", sourceImage);
        this.sourceImage = sourceImage;
        /* Like ImageIcon, we block until the image is fully loaded. Just rely on ImageIcon's
        implementation here (it will ll get a MediaTracker, call waitForID etc.). */
        Icon imageIcon = new ImageIcon(sourceImage);
        sourceWidth = imageIcon.getIconWidth();
        sourceHeight = imageIcon.getIconHeight();
    }

    @Override
    protected Image createAndPaintImage(
        Component c, ColorModel colorModel, int deviceWidth, int deviceHeight, double scale)
    {
        final BufferedImage img = createBufferedImage(colorModel, deviceWidth, deviceHeight);
        if (sourceWidth > 0 && sourceHeight > 0) {
            final Graphics2D g = img.createGraphics();
            try {
                /* Despite these hints, downscaling by more than 2x tends to give low-quality
                results compared to image resizing in, say, Photoshop or IrfanView. This is a known
                quality/performance trade-off in Java2D; see
                https://stackoverflow.com/questions/24745147/java-resize-image-without-losing-quality .
                Our Javadoc recommendation to use a scaling of exactly 2x should avoid these
                problems. */
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.setTransform(AffineTransform.getScaleInstance(
                        scale * getIconWidth() / (double) sourceWidth,
                        scale * getIconHeight() / (double) sourceHeight));
                g.drawImage(sourceImage, 0, 0, null);
            } finally {
                g.dispose();
            }
        }
        return img;
    }
}
