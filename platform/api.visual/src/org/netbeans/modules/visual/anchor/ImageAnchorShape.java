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
package org.netbeans.modules.visual.anchor;

import org.netbeans.api.visual.anchor.AnchorShape;

import java.awt.*;

/**
 * @author David Kaspar
 */
public class ImageAnchorShape implements AnchorShape {

    private Image image;
    private boolean lineOriented;
    private int radius;
    private int x, y;

    public ImageAnchorShape (Image image, boolean lineOriented) {
        this.lineOriented = lineOriented;
        assert image != null;
        this.image = image;
        x = image.getWidth (null);
        y = image.getHeight (null);
        radius = Math.max (x, y);
        x = - (x / 2);
        y = - (y / 2);
    }

    public boolean isLineOriented () {
        return lineOriented;
    }

    public int getRadius () {
        return radius;
    }

    public double getCutDistance () {
        return 0.0;
    }

    public void paint (Graphics2D graphics, boolean source) {
        graphics.drawImage (image, x, y, null);
    }

}
