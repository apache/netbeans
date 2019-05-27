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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.Icon;

/* Package-private for now. At some later point it might be useful to expose the DPI-based caching
functionality in this class as a more general utility. */
/**
 * An Icon implementation that scales a source image to the specified dimensions. Can be used to
 * produce sharp images on HiDPI displays, without relying on MultiResolutionImage, which exists
 * only since JDK 9. This also sidesteps https://bugs.openjdk.java.net/browse/JDK-8212226 on
 * Windows. For HiDPI displays, the source image's dimensions should be at least double those of the
 * icon's logical dimensions. A double-resolution source image will automatically be scaled down
 * to 1x, 1.5x, or other HiDPI scaling factors as needed.
 */
final class ScaledBitmapIcon implements Icon {
    private final Map<Double,Image> cache = new ConcurrentHashMap<>();
    private final Image sourceImage;
    private final int width;
    private final int height;

    public ScaledBitmapIcon(Image sourceImage, int width, int height) {
        if (sourceImage == null)
            throw new NullPointerException();
        if (width <= 0)
            throw new IllegalArgumentException();
        if (height <= 0)
            throw new IllegalArgumentException();
        this.sourceImage = sourceImage;
        this.width = width;
        this.height = height;
    }

    private Image getScaledImage(GraphicsConfiguration gc, double dpiScaling) {
        Image ret = cache.get(dpiScaling);
        if (ret != null) {
            return ret;
        }
        final BufferedImage img = gc.createCompatibleImage(
                (int) Math.ceil(getIconWidth() * dpiScaling),
                (int) Math.ceil(getIconHeight() * dpiScaling), Transparency.TRANSLUCENT);
        final double sourceWidth = sourceImage.getWidth(null);
        final double sourceHeight = sourceImage.getHeight(null);
        if (sourceWidth >= 1 && sourceHeight >= 1) {
          final Graphics2D imgG = (Graphics2D) img.getGraphics();
          try {
              imgG.setTransform(AffineTransform.getScaleInstance(
                      dpiScaling * getIconWidth() / sourceWidth,
                      dpiScaling * getIconHeight() / sourceHeight));
              imgG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
              imgG.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
              imgG.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
              imgG.drawImage(sourceImage, 0, 0, null);
          } finally {
              imgG.dispose();
          }
          if (dpiScaling <= 3.0)
              cache.put(dpiScaling, img);
        }
        return img;
    }

    @Override
    public void paintIcon(Component c, Graphics g0, int x, int y) {
        final Graphics2D g = (Graphics2D) g0;
        final AffineTransform oldTransform = g.getTransform();
        g.translate(x, y);
        final AffineTransform tx = g.getTransform();

        final double dpiScaling;
        final int txType = tx.getType();
        if (txType == AffineTransform.TYPE_UNIFORM_SCALE ||
            txType == (AffineTransform.TYPE_UNIFORM_SCALE | AffineTransform.TYPE_TRANSLATION))
        {
            dpiScaling = tx.getScaleX();
        } else {
            dpiScaling = 1.0;
        }
        // Scale the image down to its logical dimensions, then draw it at the device pixel boundary.
        Image scaledImage = getScaledImage(g.getDeviceConfiguration(), dpiScaling);
        if (dpiScaling != 1.0) {
            AffineTransform tx2 = g.getTransform();
            g.setTransform(new AffineTransform(1, 0, 0, 1,
                (int) tx2.getTranslateX(),
                (int) tx2.getTranslateY()));
        }
        g.drawImage(scaledImage, 0, 0, null);
        g.setTransform(oldTransform);
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }
}
