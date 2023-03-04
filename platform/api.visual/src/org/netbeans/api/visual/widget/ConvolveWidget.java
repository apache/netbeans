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
package org.netbeans.api.visual.widget;

import org.netbeans.api.visual.border.BorderFactory;

import java.awt.image.ConvolveOp;
import java.awt.image.BufferedImage;
import java.awt.image.Kernel;
import java.awt.*;

/**
 * The widget which applies a convolve filter to a graphics rendered by the children.
 * <p>
 * Children are painted to an offscreen buffer which is later painted with a convolve filter applied to it.
 * <p>
 * Because of the offscreen buffer, be careful about the size of the widget. The buffer stays allocated
 * even after the painting and it is also expanding only (when required). You can clear the buffer using clearCache method.
 *
 * @author David Kaspar
 */
public class ConvolveWidget extends Widget {

    private static final Color TRANSPARENT = new Color (0, 0, 0, 0);

    private ConvolveOp convolveOp;
    private BufferedImage image;
    private Graphics2D imageGraphics;

    /**
     * Creates a convolve widget with a specified ColvolveOp.
     * @param scene the scene
     * @param convolveOp the convolve operation
     */
    public ConvolveWidget (Scene scene, ConvolveOp convolveOp) {
        super (scene);
        this.convolveOp = convolveOp;
        Kernel kernel = convolveOp.getKernel ();
        setBorder (BorderFactory.createEmptyBorder (kernel.getWidth (), kernel.getHeight ()));
    }

    /**
     * Returns a convolve operation.
     * @return the convolve operation
     */
    public ConvolveOp getConvolveOp () {
        return convolveOp;
    }

    /**
     * Sets a convolve operation.
     * @param convolveOp the convolve operation
     */
    public void setConvolveOp (ConvolveOp convolveOp) {
        this.convolveOp = convolveOp;
        repaint ();
    }

    /**
     * Clears an offscreen buffer.
     */
    public void clearCache () {
        if (imageGraphics != null)
            imageGraphics.dispose ();
        image = null;

    }

    /**
     * Paints the children into the offscreen buffer and then the buffer is rendered regularly using the convolve operation.
     */
    protected void paintChildren () {
        Rectangle bounds = getBounds ();
        if (image == null  ||  image.getWidth () < bounds.width  ||  image.getHeight () < bounds.height) {
            clearCache ();
            image = new BufferedImage (bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
            imageGraphics = image.createGraphics ();
        }

        Graphics2D previousGraphics = getScene ().getGraphics ();
        imageGraphics.translate (- bounds.x, - bounds.y);
        imageGraphics.setBackground (TRANSPARENT);
        imageGraphics.clearRect (bounds.x, bounds.y, bounds.width, bounds.height);

        getScene ().setGraphics (imageGraphics);
        super.paintChildren ();
        getScene ().setGraphics (previousGraphics);

        imageGraphics.translate (bounds.x, bounds.y);

        getGraphics ().drawImage (image, convolveOp, bounds.x, bounds.y);
    }

}
