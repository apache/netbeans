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
package org.netbeans.swing.laf.flatlaf.icons;

import com.formdev.flatlaf.util.UIScale;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.plaf.UIResource;
import org.openide.util.ImageUtilities;

/**
 * Adapter class which allows a NetBeans-loaded PNG or SVG icon to be used as a FlatLAF
 * configuration setting. Respects FlatLAF's own scaling properties, as in
 * {@code com.formdev.flatlaf.icons.FlatAbstractIcon}.
 */
abstract class NBFlatAdapterIcon implements Icon, UIResource {
    private final Icon delegate;

    public NBFlatAdapterIcon(String resourcePath) {
        if (resourcePath == null) {
            throw new NullPointerException();
        }
        Image delegateImg = ImageUtilities.loadImage(resourcePath);
        this.delegate = delegateImg == null ? new ImageIcon() : ImageUtilities.image2Icon(delegateImg);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.translate(x, y);
            UIScale.scaleGraphics(g2);
            delegate.paintIcon(c, g2, 0, 0);
        } finally {
            g2.dispose();
        }
    }

    @Override
    public int getIconWidth() {
        return UIScale.scale(delegate.getIconWidth());
    }

    @Override
    public int getIconHeight() {
        return UIScale.scale(delegate.getIconHeight());
    }
}
