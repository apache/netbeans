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

package org.netbeans.modules.uihandler;

import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.beans.BeanInfo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.Level;
import junit.framework.TestCase;
import java.util.logging.LogRecord;
import org.netbeans.lib.uihandler.LogRecords;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

/**
 *
 * @author Jaroslav Tulach
 */
public class UINodeTest extends TestCase {
    
    public UINodeTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testDisplayNameOfTheNode() throws Exception {
        LogRecord r = new LogRecord(Level.INFO, "test_msg");
        r.setResourceBundleName("org.netbeans.modules.uihandler.TestBundle");
        r.setResourceBundle(ResourceBundle.getBundle("org.netbeans.modules.uihandler.TestBundle"));
        r.setParameters(new Object[] { new Integer(1), "Ahoj" });
        
        Node n = UINode.create(r);
        assertEquals("Name is taken from the message", "test_msg", n.getName());
        
        if (!n.getDisplayName().matches(".*Ahoj.*1.*")) {
            fail("wrong display name, shall contain Ahoj and 1: " + n.getDisplayName());
        }
        assertSerializedWell(n);
    }

    public void testIconOfTheNode() throws Exception {
        LogRecord r = new LogRecord(Level.INFO, "icon_msg");
        r.setResourceBundleName("org.netbeans.modules.uihandler.TestBundle");
        r.setResourceBundle(ResourceBundle.getBundle("org.netbeans.modules.uihandler.TestBundle"));
        r.setParameters(new Object[] { new Integer(1), "Ahoj" });
        
        Node n = UINode.create(r);
        assertEquals("Name is taken from the message", "icon_msg", n.getName());
        
        if (!n.getDisplayName().matches(".*Ahoj.*")) {
            fail("wrong display name, shall contain Ahoj: " + n.getDisplayName());
        }
        
        Image img = n.getIcon(BeanInfo.ICON_COLOR_32x32);
        assertNotNull("Some icon", img);
        IconInfo imgReal = new IconInfo(img);
        IconInfo template = new IconInfo(getClass().getResource("testicon.png"));
        assertEquals("Icon from ICON_BASE used", template, imgReal);
        
        assertSerializedWell(n);
    }
    
    public void testSomeNPE() throws Exception {
        LogRecord r = new LogRecord(Level.FINE, "UI_ACTION_EDITOR");
        Node n = UINode.create(r);
        assertNotNull(n);
        assertEquals("No name", "", n.getDisplayName());
        assertNotNull(n.getName());
        assertSerializedWell(n);
    }
    
    public void testHasNonNullName() throws Exception {
        LogRecord r = new LogRecord(Level.WARNING, null);
        r.setThrown(new Exception());
        Node n = UINode.create(r);
        assertNotNull(n);
        assertNotNull(n.getName());
        assertSerializedWell(n);
    }
    public void testHasNonNullNameWhenMessageIsGiven() throws Exception {
        Exception my = new Exception("Ahoj");
        LogRecord r = new LogRecord(Level.WARNING, my.getMessage());
        r.setThrown(my);
        Node n = UINode.create(r);
        assertNotNull(n);
        assertNotNull(n.getName());
        assertSerializedWell(n);
    }
    
    private static void assertSerializedWell(Node n) throws Exception {
        LogRecord r = n.getLookup().lookup(LogRecord.class);
        assertNotNull("There is a log record", r);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        LogRecords.write(os, r);
        os.close();

        {
            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
            class H extends Handler {
                public LogRecord nr;
                
                public void publish(LogRecord arg0) {
                    assertNull("First call", nr);
                    nr = arg0;
                }

                public void flush() {
                }

                public void close() throws SecurityException {
                }
            }
            
            H handler = new H();
            LogRecords.scan(is, handler);
            LogRecord nr = handler.nr;
            is.close();

            Node newNode = UINode.create(nr);

            assertEquals("name", n.getName(), newNode.getName());
            assertEquals("displayName", n.getDisplayName(), newNode.getDisplayName());
            assertEquals("htmlName", n.getHtmlDisplayName(), newNode.getHtmlDisplayName());
            IconInfo old = new IconInfo(n.getIcon(BeanInfo.ICON_COLOR_16x16));
            assertEquals("16x16", old, new IconInfo(newNode.getIcon(BeanInfo.ICON_COLOR_16x16)));
        }
        class H extends Handler {
            LogRecord one;
            
            public void publish(LogRecord a) {
                assertNull("This is first one: " + a, one);
                one = a;
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }
        }
        
        H handler = new H();
        
        {
            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
            LogRecords.scan(is, handler);
            is.close();

            Node newNode = UINode.create(handler.one);

            assertEquals("name", n.getName(), newNode.getName());
            assertEquals("displayName", n.getDisplayName(), newNode.getDisplayName());
            assertEquals("htmlName", n.getHtmlDisplayName(), newNode.getHtmlDisplayName());
            
            IconInfo old = new IconInfo(n.getIcon(BeanInfo.ICON_COLOR_16x16));
            assertEquals("16x16", old, new IconInfo(newNode.getIcon(BeanInfo.ICON_COLOR_16x16)));
        }
    }

    static final java.awt.image.BufferedImage createBufferedImage(int width, int height) {
        if (Utilities.isMac()) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
        }

        ColorModel model = colorModel(java.awt.Transparency.TRANSLUCENT);
        java.awt.image.BufferedImage buffImage = new java.awt.image.BufferedImage(
                model, model.createCompatibleWritableRaster(width, height), model.isAlphaPremultiplied(), null
            );

        return buffImage;
    }
    static private ColorModel colorModel(int transparency) {
        ColorModel model;
        try {
            model = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration()
                .getColorModel(transparency);
        }
        catch(HeadlessException he) {
            model = ColorModel.getRGBdefault();
        }

        return model;
    }
    
    static final BufferedImage toBufferedImage(Image img) {
        // load the image
        new javax.swing.ImageIcon(img, "");

        java.awt.image.BufferedImage rep = createBufferedImage(img.getWidth(null), img.getHeight(null));
        java.awt.Graphics g = rep.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        img.flush();

        return rep;
    }
    
    private static final class IconInfo implements Comparable<IconInfo> {
        final int hash;
        
        public IconInfo(URL u) throws IOException {
            this(Toolkit.getDefaultToolkit().getImage(u));
        }
        
        public IconInfo(Image img) throws IOException {
            BufferedImage image = toBufferedImage(img);
            
            int hash;
            try {
                int w = image.getWidth();
                int h = image.getHeight();
                hash = w * 3 + h * 7;
                
                for (int i = 0; i < w; i++) {
                    for (int j = 0; j < h; j++) {
                        int rgb = image.getRGB(i, j);
                        hash += (rgb >> 2);
                    }
                }
            } catch (IndexOutOfBoundsException ex) {
                fail("Error: " + ex.getMessage());
                throw new Error();
            }
            
            this.hash = hash;
        }
        
        public IconInfo(String name, String path, int hash) {
            this.hash = hash;
        }
        
        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final IconInfo other = (IconInfo) obj;

            if (this.hash != other.hash)
                return false;
            return true;
        }
    
        public int compareTo(IconInfo another) {
            if (hash != another.hash) {
                return hash - another.hash;
            }
            
            return 0;
        }
        
        public String toString() {
            String h = Integer.toHexString(hash);
            if (h.length() < 8) {
                h = "00000000".substring(h.length()) + h;
            }
            
            return MessageFormat.format("Icon #{0}", h);
        }
    } // end of IconInfo
    
}
