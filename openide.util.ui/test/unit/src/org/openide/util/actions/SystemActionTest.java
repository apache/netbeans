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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
    
    private static abstract class TestSystemAction extends SystemAction {
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
