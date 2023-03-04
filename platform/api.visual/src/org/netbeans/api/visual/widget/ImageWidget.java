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

import org.openide.ErrorManager;

import javax.swing.*;
import java.awt.*;
import java.awt.image.ImageObserver;

/**
 * A widget representing image. The origin of the widget is at its top-left corner.
 * @author David Kaspar
 */
// TODO - alignment
public class ImageWidget extends Widget {

    private Image image;
    private Image disabledImage;
    private int width, height;
    private boolean paintAsDisabled;
    private ImageObserver observer = new ImageObserver() {
        public boolean imageUpdate (Image img, int infoflags, int x, int y, int width, int height) {
//            System.out.println ("INFO: " + infoflags);
            setImageCore (image);
            getScene ().validate ();
            return (infoflags & (ImageObserver.ABORT | ImageObserver.ERROR)) == 0;
        }
    };

    /**
     * Creates an image widget.
     * @param scene the scene
     */
    public ImageWidget (Scene scene) {
        super (scene);
    }

    /**
     * Creates an image widget.
     * @param scene the scene
     * @param image the image
     */
    public ImageWidget (Scene scene, Image image) {
        super (scene);
        setImage (image);
    }

    /**
     * Returns an image.
     * @return the image
     */
    public Image getImage () {
        return image;
    }

    /**
     * Sets an image
     * @param image the image
     */
    public void setImage (Image image) {
        if (this.image == image)
            return;
        setImageCore (image);
    }

    private void setImageCore (Image image) {
        if (image == this.image) {
            return;
        }
        int oldWidth = width;
        int oldHeight = height;

        this.image = image;
        this.disabledImage = null;
        width = image != null ? image.getWidth (null) : 0;
        height = image != null ? image.getHeight (null) : 0;

        if (oldWidth == width  &&  oldHeight == height)
            repaint ();
        else
            revalidate ();
    }

    /**
     * Returns whether the label is painted as disabled.
     * @return true, if the label is painted as disabled
     */
    public boolean isPaintAsDisabled () {
        return paintAsDisabled;
    }

    /**
     * Sets whether the label is painted as disabled.
     * @param paintAsDisabled if true, then the label is painted as disabled
     */
    public void setPaintAsDisabled (boolean paintAsDisabled) {
        boolean repaint = this.paintAsDisabled != paintAsDisabled;
        this.paintAsDisabled = paintAsDisabled;
        if (repaint)
            repaint ();
    }

    /**
     * Calculates a client area of the image
     * @return the calculated client area
     */
    protected Rectangle calculateClientArea () {
        if (image != null)
            return new Rectangle (0, 0, width, height);
        return super.calculateClientArea ();
    }

    /**
     * Paints the image widget.
     */
    protected void paintWidget () {
        if (image == null)
            return;
        Graphics2D gr = getGraphics ();
        if (image != null) {
            if (paintAsDisabled) {
                if (disabledImage == null) {
                    disabledImage = GrayFilter.createDisabledImage (image);
                    MediaTracker tracker = new MediaTracker (getScene ().getView ());
                    tracker.addImage (disabledImage, 0);
                    try {
                        tracker.waitForAll ();
                    } catch (InterruptedException e) {
                        ErrorManager.getDefault ().notify (e);
                    }
                }
                gr.drawImage (disabledImage, 0, 0, observer);
            } else
                gr.drawImage (image, 0, 0, observer);
        }
    }

}
