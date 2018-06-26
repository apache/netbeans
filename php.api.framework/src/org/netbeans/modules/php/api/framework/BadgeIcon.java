/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.api.framework;

import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;
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
     * @thows IllegalArgumentException if the width or height is not 8 pixels (under assertions only)
     */
    public BadgeIcon(Image image, URL url) {
        Parameters.notNull("image", image); // NOI18N
        Parameters.notNull("url", url); // NOI18N

        boolean assertions = false;
        assert assertions = true;
        if (assertions) {
            ImageIcon imageIcon = new ImageIcon(image);
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
