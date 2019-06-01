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
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * A filtered variation of a provided delegate icon. Any kind of delegate implementation can be
 * used. In particular, this class preserves the full fidelity of HiDPI icons, such as instances
 * of {@link VectorIcon}, or {@link ImageIcon} instances delegating to a
 * {@code java.awt.image.MultiResolutionImage} (available since Java 9 and above).
 *
 * <p>Note that state passed through the {@code Component} parameter of the
 * {@link Icon#paintIcon(Component,Graphics,int,int)} method will only be current as of the time the
 * icon is initially entered into the cache.
 */
final class FilteredIcon extends CachedHiDPIIcon {
    private final RGBImageFilter filter;
    private final Icon delegate;

    private FilteredIcon(RGBImageFilter filter, Icon delegate) {
        super(delegate.getIconWidth(), delegate.getIconHeight());
        Parameters.notNull("filter", filter);
        Parameters.notNull("delegate", delegate);
        this.filter = filter;
        this.delegate = delegate;
    }

    public static Icon create(RGBImageFilter filter, Icon delegate) {
        return new FilteredIcon(filter, delegate);
    }

    @Override
    protected Image createAndPaintImage(
            Component c, ColorModel colorModel, int deviceWidth, int deviceHeight, double scale)
    {
        final BufferedImage img = createBufferedImage(colorModel, deviceWidth, deviceHeight);
        final Graphics2D imgG = img.createGraphics();
        try {
            imgG.clip(new Rectangle(0, 0, img.getWidth(), img.getHeight()));
            imgG.scale(scale, scale);
            delegate.paintIcon(c, imgG, 0, 0);
        } finally {
            imgG.dispose();
        }
        return Toolkit.getDefaultToolkit().createImage(
                new FilteredImageSource(img.getSource(), filter));
    }
}
