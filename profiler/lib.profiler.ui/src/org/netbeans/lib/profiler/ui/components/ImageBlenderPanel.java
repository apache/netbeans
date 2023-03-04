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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.UIManager;


/**
 *
 * @author Jiri Sedlacek
 */
public class ImageBlenderPanel extends ImagePanel {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Color background;
    private Image image1;
    private Image image2;
    private float blendAlpha;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ImageBlenderPanel(Image image1, Image image2) {
        this(image1, image2, UIManager.getColor("Panel.background"), 0); // NOI18N
    }

    public ImageBlenderPanel(Image image1, Image image2, Color background, float blendAlpha) {
        super(createBlendedImage(image1, image2, background, blendAlpha));
        this.background = background;
        this.blendAlpha = blendAlpha;
        this.image1 = image1;
        this.image2 = image2;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setBlendAlpha(float blendAlpha) {
        setImage(createBlendedImage(image1, image2, background, blendAlpha));
        this.blendAlpha = blendAlpha;
    }

    private static Image createBlendedImage(Image image1, Image image2, Color background, float blendAlpha) {
        Image i1 = loadImage(image1);
        Image i2 = loadImage(image2);

        int blendedImageWidth = Math.max(i1.getWidth(null), i2.getWidth(null));
        int blendedImageHeight = Math.max(i1.getHeight(null), i2.getHeight(null));

        BufferedImage blendedImage = new BufferedImage(blendedImageWidth, blendedImageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D blendedImageGraphics = (Graphics2D) blendedImage.getGraphics();

        blendedImageGraphics.setColor(background);
        blendedImageGraphics.fillRect(0, 0, blendedImageWidth, blendedImageHeight);
        blendedImageGraphics.drawImage(i1, 0, 0, null);
        blendedImageGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, blendAlpha));
        blendedImageGraphics.drawImage(i2, 0, 0, null);

        return blendedImage;
    }
}
