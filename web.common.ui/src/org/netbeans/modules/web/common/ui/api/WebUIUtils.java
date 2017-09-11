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
package org.netbeans.modules.web.common.ui.api;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.openide.filesystems.FileObject;

/**
 * Various web UI utilities
 *
 * @author marekfukala
 */
public class WebUIUtils {

    private static final Logger LOGGER = Logger.getLogger(WebUIUtils.class.getName());
    static boolean UNIT_TESTING = false;
    static FileObject WEB_ROOT;

    /**
     * Returns hex color code in the #xxyyzz form.
     */
    public static String toHexCode(Color color) {
        return new StringBuilder().append('#').append(toTwoDigitsHexCode(color.getRed())).append(toTwoDigitsHexCode(color.getGreen())).append(toTwoDigitsHexCode(color.getBlue())).toString();
    }

    private static String toTwoDigitsHexCode(int code) {
        StringBuilder sb = new StringBuilder(Integer.toHexString(code));
        if (sb.length() == 1) {
            sb.insert(0, '0');
        }
        return sb.toString();
    }

    private static final byte COLOR_ICON_SIZE = 16; //px
    private static final byte COLOR_RECT_SIZE = 10; //px
    private static String WHITE_COLOR_HEX_CODE = "ffffff"; //NOI18N
    private static Map<String, ImageIcon> ICONS_WEAK_CACHE = new WeakHashMap<String, ImageIcon>();

    /**
     * Creates a custom icon according to the given color code.
     *
     * Creates a 16x16 pixels icon with black border and the inner area filled
     * with the color of the given color code or white with diagonal black line
     * if the color code is null.
     *
     * The implementation caches the created icons weakly by their color codes.
     *
     * @since 1.24
     * @param colorCode 3 or 6 digits hex color code (examples: aabbcc, #ff0012). May or may not start with hash char.
     * @return an instance of ImageIcon.
     */
    public static ImageIcon createColorIcon(String colorCode) {
        if(colorCode != null && colorCode.length() > 0 && '#' == colorCode.charAt(0)) {
            //strip the leading hash
            colorCode = colorCode.substring(1);
        }

        ImageIcon icon = ICONS_WEAK_CACHE.get(colorCode);
        if(icon == null) {
            icon = _createColorIcon(colorCode);
            ICONS_WEAK_CACHE.put(colorCode, icon);
        }
        return icon;
    }

    private static ImageIcon _createColorIcon(String colorCode) {
        BufferedImage i = new BufferedImage(COLOR_ICON_SIZE, COLOR_ICON_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = i.createGraphics();

        boolean defaultIcon = colorCode == null;
        if (defaultIcon) {
            //unknown color code, we still want a generic icon
            colorCode = WHITE_COLOR_HEX_CODE;
        }

        if (colorCode.length() == 3) {
            //shorthand color code #fc0 means #ffcc00
            colorCode = new StringBuilder().append(colorCode.charAt(0)).
                    append(colorCode.charAt(0)).
                    append(colorCode.charAt(1)).
                    append(colorCode.charAt(1)).
                    append(colorCode.charAt(2)).
                    append(colorCode.charAt(2)).toString();
        }

        Color transparent = new Color(0x00ffffff, true);
        g.setColor(transparent);
        g.fillRect(0, 0, COLOR_ICON_SIZE, COLOR_ICON_SIZE);

        try {
            g.setColor(Color.decode("0x" + colorCode)); //NOI18N
        } catch (NumberFormatException ignoredException) {
            //unparseable code
            colorCode = WHITE_COLOR_HEX_CODE;
            defaultIcon = true;
        }
        g.fillRect(COLOR_ICON_SIZE - COLOR_RECT_SIZE,
                COLOR_ICON_SIZE - COLOR_RECT_SIZE - 1,
                COLOR_RECT_SIZE - 1,
                COLOR_RECT_SIZE - 1);

        g.setColor(Color.DARK_GRAY);
        g.drawRect(COLOR_ICON_SIZE - COLOR_RECT_SIZE - 1,
                COLOR_ICON_SIZE - COLOR_RECT_SIZE - 2,
                COLOR_RECT_SIZE,
                COLOR_RECT_SIZE);

        if (defaultIcon) {
            //draw the X inside the icon
            g.drawLine(COLOR_ICON_SIZE - COLOR_RECT_SIZE - 1,
                    COLOR_ICON_SIZE - 2,
                    COLOR_ICON_SIZE - 1,
                    COLOR_ICON_SIZE - COLOR_RECT_SIZE - 2);
        }

        return new ImageIcon(i);
    }

}
