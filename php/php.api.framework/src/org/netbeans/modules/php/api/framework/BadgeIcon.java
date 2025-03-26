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

package org.netbeans.modules.php.api.framework;

import java.awt.Image;
import java.net.URL;
import javax.swing.Icon;
import org.openide.util.ImageUtilities;
import org.openide.util.Parameters;

/**
 * Badge icon (8x8) which provides an {@link Image} as well as {@link URL}
 * of the icon.
 * @author Tomas Mysik
 */
public final class BadgeIcon {
    private final Image image;
    private final URL url;

    /**
     * Creates a new badge icon. Image has to have 8x8 dimensions.
     * @param image image of icon
     * @param url URL of icon
     * @throws IllegalArgumentException if the width or height is not 8 pixels (under assertions only)
     */
    public BadgeIcon(Image image, URL url) {
        Parameters.notNull("image", image); // NOI18N
        Parameters.notNull("url", url); // NOI18N

        boolean assertions = false;
        assert assertions = true;
        if (assertions) {
            Icon imageIcon = ImageUtilities.image2Icon(image);
            if (imageIcon.getIconWidth() != 8) {
                throw new IllegalArgumentException("The width of an image must be 8 px");
            }
            if (imageIcon.getIconHeight() != 8) {
                throw new IllegalArgumentException("The height of an image must be 8 px");
            }
        }

        this.image = image;
        this.url = url;
    }

    /**
     * Returns the image of the badge icon.
     * @return the image of the badge icon
     */
    public Image getImage() {
        return image;
    }

    /**
     * Returns the URL of the badge icon.
     * @return the URL of the badge icon
     */
    public URL getUrl() {
        return url;
    }
}
