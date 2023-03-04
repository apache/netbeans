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

package org.netbeans.modules.ide.ergonomics.ant;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.apache.tools.ant.types.Resource;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class IconResource extends Resource {
    private Resource r;
    private BufferedImage badgeIcon;

    public IconResource(Resource r, BufferedImage badgeIcon) {
        super(r.getName(), r.isExists(), r.getLastModified(), r.isDirectory(), r.getSize());
        this.r = r;
        this.badgeIcon = badgeIcon;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (getName().endsWith(".html")) {
            return r.getInputStream();
        }
        try {
            InputStream is = r.getInputStream();
            BufferedImage img = ImageIO.read(is);
            if (img == null) {
                return r.getInputStream();
            }
            BufferedImage merge = merge(img, badgeIcon);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(merge, "png", os);
            return new ByteArrayInputStream(os.toByteArray());
        } catch (IOException ex) {
            return r.getInputStream();
        }
    }

    private static BufferedImage merge(BufferedImage main, BufferedImage badge) {
        int w = Math.max(main.getWidth(), badge.getWidth());
        int h = Math.max(main.getHeight(), badge.getHeight());
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int badgeColor = x < badge.getWidth() && y < badge.getHeight() ? badge.getRGB(x, y) : 0;
                int mainColor = x < main.getWidth() && y < main.getHeight() ? main.getRGB(x, y) : 0;
                int mergedColor = packARGB(overARGB(unpackARGB(badgeColor), unpackAGray(mainColor)));
                result.setRGB(x, y, mergedColor);
            }
        }
//        System.err.println("main:");
//        dump(main);
//        System.err.println("badge:");
//        dump(badge);
//        System.err.println("result:");
//        dump(result);
        return result;
    }
    private static int[] unpackAGray(int c) {
        int[] res = unpackARGB(c);
        int gray = (res[1] * 30 + res[2] * 59 + res[3] * 11) / 100;
        return new int[] {res[0], gray, gray, gray};
    }
    private static int[] unpackARGB(int c) {
        return new int[] {(c >> 24) & 0xFF, (c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF};
    }
    private static int packARGB(int[] c) {
        return (c[0] << 24) | (c[1] << 16) | (c[2] << 8) | c[3];
    }
    /** @see <a href="http://en.wikipedia.org/wiki/Alpha_blending#Description">Alpha blending</a> */
    private static int[] overARGB(int[] a, int[] b) {
        int[] r = new int[4];
        for (int c = 0; c < 4; c++) {
            assert a[c] >= 0 && a[c] <= 255: a[c];
            assert b[c] >= 0 && b[c] <= 255: b[c];
        }
        float alphaA = a[0] / 255.0f;
        float alphaB = b[0] / 255.0f;
        float alphaANeg = 1.0f - alphaA;
        float alphaO = alphaA + alphaB * alphaANeg;
        r[0] = (int) (alphaO * 255 + 0.5f);
        for (int c = 1; c <= 3; c++) {
            float cA = a[c] / 255.0f;
            float cB = b[c] / 255.0f;
            float cR = (cA * alphaA + cB * alphaB * alphaANeg) / alphaO;
            r[c] = (int) (cR * 255 + 0.5f);
        }
        for (int c = 0; c < 4; c++) {
            assert r[c] >= 0 && r[c] <= 255: r[c];
        }
//        System.err.println("overARGB(" + show(a) + "," + show(b) + ")=" + show(r));
        return r;
    }
//    private static void dump(BufferedImage img) {
//        for (int y = 0; y < img.getHeight(); y++) {
//            for (int x = 0; x < img.getWidth(); x++) {
//                System.err.format("%08X ", img.getRGB(x, y));
//            }
//            System.err.println();
//        }
//    }
    static {
        int[] argb = {0xAB, 0x12, 0xCD, 0x34};
        int p = packARGB(argb);
        assert p == 0xAB12CD34 : p;
        assert Arrays.equals(argb, unpackARGB(p));
        String s = show(argb);
        assert s.equals("AB12CD34") : s;
        int[] a = unpackARGB(0x0096989A);
        int[] b = unpackARGB(0xFF50557D);
        int[] r = overARGB(a, b);
        assert packARGB(r) == 0xFF50557D : show(r);
    }
    private static String show(int[] argb) {
        return String.format("%08X", packARGB(argb));
    }

}
