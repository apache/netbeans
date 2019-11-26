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
package org.openide.util;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.swing.Icon;

/**
 * Abstract base class for {@link javax.swing.Icon} implementations that need to cache scaled bitmap
 * representations for HiDPI displays. Bitmaps for multiple HiDPI scaling factors can be cached at
 * the same time, e.g. for multi-monitor setups. Thread-safe.
 */
abstract class CachedHiDPIIcon implements Icon {
    /**
     * The maximum size of the cache, as a multiple of the size of the icon at 100% scaling. For
     * example, storing three images at 100%, 150%, and 200% scaling, respectively, yields a total
     * cache size of 1.0^2 + 1.5^2 + 2^2 = 7.2.
     */
    private static final double MAX_CACHE_SIZE = 10.0;
    private final int width;
    private final int height;
    /**
     * Cache map with least-recently-used iteration order.
     */
    private final Map<CachedImageKey, Image> cache =
            new LinkedHashMap<CachedImageKey, Image>(16, 0.75f, true);
    /**
     * Total size of the images currently in the cache, in the same units as
     * {@link #MAX_CACHE_SIZE}.
     */
    private double cacheSize = 0.0;

    /**
     * Constructor to be used by subclasses.
     */
    protected CachedHiDPIIcon(int width, int height) {
        if (width < 0) {
            throw new IllegalArgumentException();
        }
        if (height < 0) {
            throw new IllegalArgumentException();
        }
        this.width = width;
        this.height = height;
    }

    private synchronized Image getScaledImageCached(Component c, CachedImageKey key) {
        Image ret = cache.get(key);
        if (ret != null) {
            return ret;
        }
        final double scale = key.getScale();
        final int deviceWidth = (int) Math.ceil(getIconWidth() * scale);
        final int deviceHeight = (int) Math.ceil(getIconHeight() * scale);
        final Image img =
                createImage(c, key.getGraphicsConfiguration(), deviceWidth, deviceHeight, scale);
        final double imgSize = key.getSize();
        if (imgSize <= MAX_CACHE_SIZE) {
            /* Evict least-recently-used images from the cache until we have space for the latest
            image. */
            final Iterator<CachedImageKey> iter = cache.keySet().iterator();
            while (cacheSize + imgSize > MAX_CACHE_SIZE && iter.hasNext()) {
                CachedImageKey removeKey = iter.next();
                iter.remove();
                cacheSize -= removeKey.getSize();
            }
            cache.put(key, img);
            cacheSize += imgSize;
        }
        return img;
    }

    @Override
    public final void paintIcon(Component c, Graphics g0, int x, int y) {
        final Graphics2D g = (Graphics2D) g0;
        CachedImageKey key = CachedImageKey.create(g);
        final AffineTransform oldTransform = g.getTransform();
        try {
            g.translate(x, y);
            Image scaledImage = getScaledImageCached(c, key);
            /* Scale the image down to its logical dimensions, then draw it at the device pixel
            boundary. In VectorIcon, we tried to be a lot more conservative, taking great care not
            to draw on any device pixels that were only partially bounded by the icon (due to
            non-integral scaling factors, e.g. 150%). That was probably overkill; it's a lot easier
            to assume that partially bounded pixels are OK to draw on, since all icon bitmaps of a
            given scaling factor then end up being the same number of device pixels wide and tall.
            And we need consistent dimensions to be able keep cached images in any case. For these
            reasons, round the X and Y translations (which denote the position in device pixels)
            _down_ here.*/
            AffineTransform tx2 = g.getTransform();
            g.setTransform(new AffineTransform(1, 0, 0, 1,
                    (int) tx2.getTranslateX(),
                    (int) tx2.getTranslateY()));
            g.drawImage(scaledImage, 0, 0, null);
        } finally {
            g.setTransform(oldTransform);
        }
    }

    @Override
    public final int getIconWidth() {
        return width;
    }

    @Override
    public final int getIconHeight() {
        return height;
    }

    /**
     * Create a scaled image containing the graphics of this icon. The result may be cached.
     *
     * @param c the component that was passed to {@link Icon#paintIcon(Component,Graphics,int,int)}.
     *        The cache will <em>not</em> be invalidated if {@code c} or its state changes, so 
     *        subclasses should avoid depending on it if possible. This parameter exists mainly to
     *        ensure compatibility with existing Icon implementations that may be used as delegates.
     *        Future implementations might also elect to simply pass a dummy Component instance
     *        here.
     * @param graphicsConfiguration the configuration of the surface on which the image will be
     *        painted
     * @param deviceWidth the required width of the image, with scaling already applied
     * @param deviceHeight the required height of the image, with scaling already applied
     * @param scale the HiDPI scaling factor detected in {@code graphicsConfiguration}
     */
    protected abstract Image createImage(Component c, GraphicsConfiguration graphicsConfiguration,
            int deviceWidth, int deviceHeight, double scale);

    private static final class CachedImageKey {
        private final GraphicsConfiguration gconf;
        private final double scale;

        public CachedImageKey(GraphicsConfiguration gconf, double scale) {
            Parameters.notNull("gconf", gconf);
            if (scale <= 0.0) {
                throw new IllegalArgumentException();
            }
            this.gconf = gconf;
            this.scale = scale;
        }

        public static CachedImageKey create(Graphics2D g) {
            final AffineTransform tx = g.getTransform();
            final int txType = tx.getType();
            final double scale;
            if (txType == AffineTransform.TYPE_UNIFORM_SCALE ||
                txType == (AffineTransform.TYPE_UNIFORM_SCALE | AffineTransform.TYPE_TRANSLATION))
            {
                scale = tx.getScaleX();
            } else {
                scale = 1.0;
            }
            return new CachedImageKey(g.getDeviceConfiguration(), scale);
        }

        public double getScale() {
            return scale;
        }

        /**
         * Get the size of this image as a multiple of the original image's size at 100% scaling.
         */
        public double getSize() {
            return Math.pow(getScale(), 2.0);
        }

        public GraphicsConfiguration getGraphicsConfiguration() {
            return gconf;
        }

        @Override
        public int hashCode() {
            return Objects.hash(gconf, scale);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CachedImageKey)) {
                return false;
            }
            final CachedImageKey other = (CachedImageKey) obj;
            return this.gconf.equals(other.gconf) &&
                   this.scale == other.scale;
        }
    }
}
