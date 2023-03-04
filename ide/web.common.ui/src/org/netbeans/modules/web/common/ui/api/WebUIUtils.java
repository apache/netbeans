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
