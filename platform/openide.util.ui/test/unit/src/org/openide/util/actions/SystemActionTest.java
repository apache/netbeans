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

package org.openide.util.actions;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.util.logging.Level;
import javax.swing.Icon;
import javax.swing.JButton;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.util.HelpCtx;

/** Test general aspects of system actions.
 * Currently, just the icon.
 * @author Jesse Glick
 */
public class SystemActionTest extends NbTestCase {

    public SystemActionTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.OFF;
    }

    /** Test that iconResource really works.
     * @see "#26887"
     */
    public void testIcons() throws Exception {
        Image i = Toolkit.getDefaultToolkit().getImage(SystemActionTest.class.getResource("data/someicon.gif"));
        int h = imageHash("Control icon", i, 16, 16);
        SystemAction a = SystemAction.get(SystemAction1.class);
        CharSequence log = Log.enable("org.openide.util", Level.WARNING);
        assertEquals("Absolute slash-initial iconResource works (though deprecated)", h, imageHash("icon1", icon2Image(a.getIcon()), 16, 16));
        assertTrue(log.toString(), log.toString().contains("Initial slashes in Utilities.loadImage deprecated"));
        a = SystemAction.get(SystemAction2.class);
        assertEquals("Absolute no-slash-initial iconResource works", h, imageHash("icon2", icon2Image(a.getIcon()), 16, 16));
        a = SystemAction.get(SystemAction3.class);
        assertEquals("Relative iconResource works (though deprecated)", h, imageHash("icon3", icon2Image(a.getIcon()), 16, 16));
        assertTrue(log.toString(), log.toString().contains("Deprecated relative path"));
        a = SystemAction.get(SystemAction4.class);
        a.getIcon();
        assertTrue(log.toString(), log.toString().contains("No such icon"));
    }
    
    private abstract static class TestSystemAction extends SystemAction {
        public void actionPerformed(ActionEvent e) {}
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }
        public String getName() {
            return getClass().getName();
        }
    }
    public static final class SystemAction1 extends TestSystemAction {
        protected String iconResource() {
            return "/org/openide/util/actions/data/someicon.gif";
        }
    }
    public static final class SystemAction2 extends TestSystemAction {
        protected String iconResource() {
            return "org/openide/util/actions/data/someicon.gif";
        }
    }
    public static final class SystemAction3 extends TestSystemAction {
        protected String iconResource() {
            return "data/someicon.gif";
        }
    }
    public static final class SystemAction4 extends TestSystemAction {
        protected String iconResource() {
            return "no/such/icon.gif";
        }
    }
    
    private static Image icon2Image(Icon ico) {
        int w = ico.getIconWidth();
        int h = ico.getIconHeight();
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        ico.paintIcon(new JButton(), img.getGraphics(), 0, 0);
        return img;
    }
    
    // Copied from SystemFileSystemTest:
    private static int imageHash(String name, Image img, int w, int h) throws InterruptedException {
        int[] pixels = new int[w * h];
        PixelGrabber pix = new PixelGrabber(img, 0, 0, w, h, pixels, 0, w);
        pix.grabPixels();
        assertEquals(0, pix.getStatus() & ImageObserver.ABORT);
        if (false) {
            // Debugging.
            System.out.println("Pixels of " + name + ":");
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (x == 0) {
                        System.out.print('\t');
                    } else {
                        System.out.print(' ');
                    }
                    int p = pixels[y * w + x];
                    String hex = Integer.toHexString(p);
                    while (hex.length() < 8) {
                        hex = "0" + hex;
                    }
                    System.out.print(hex);
                    if (x == w - 1) {
                        System.out.print('\n');
                    }
                }
            }
        }
        int hash = 0;
        for (int i = 0; i < pixels.length; i++) {
            hash += 172881;
            int p = pixels[i];
            if ((p & 0xff000000) == 0) {
                // Transparent; normalize.
                p = 0;
            }
            hash ^= p;
        }
        return hash;
    }
    
}
