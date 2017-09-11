/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
