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

package org.netbeans.lib.uihandler;

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
import org.openide.util.Utilities;

/**
 *
 * @author Jaroslav Tulach
 */
public class DecorableTest extends TestCase {
    private static class Node implements Decorable {
        private String name, displayName, base, shortDescription;
        
        public void setName(String n) {
            name = n;
        }

        public void setDisplayName(String n) {
            displayName = n;
        }

        public void setIconBaseWithExtension(String base) {
            this.base = base;
        }

        public void setShortDescription(String format) {
            shortDescription = format;
        }

        public String getName() {
            return name;
        }
        
        public String getDisplayName() {
            return displayName;
        }

        public Image getIcon(int type) {
            URL u = getClass().getClassLoader().getResource(base);
            assertNotNull("icon found", u);
            return Toolkit.getDefaultToolkit().createImage(u);
        }
        
        public String getHtmlDisplayName() {
            return displayName;
        }
    }
    
    private static class UINode extends Node {
        public static Node create(LogRecord r) {
            Node n = new UINode();
            LogRecords.decorate(r, n);
            return n;
        }
    }
    
    
    public DecorableTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public void testDisplayNameOfTheNode() throws Exception {
        LogRecord r = new LogRecord(Level.INFO, "test_msg");
        r.setResourceBundleName("org.netbeans.lib.uihandler.TestBundle");
        r.setResourceBundle(ResourceBundle.getBundle("org.netbeans.lib.uihandler.TestBundle"));
        r.setParameters(new Object[] { new Integer(1), "Ahoj" });
        
        Node n = UINode.create(r);
        assertEquals("Name is taken from the message", "test_msg", n.getName());
        
        if (!n.getDisplayName().matches(".*Ahoj.*1.*")) {
            fail("wrong display name, shall contain Ahoj and 1: " + n.getDisplayName());
        }
        assertSerializedWell(r, n);
    }

    public void testIconOfTheNode() throws Exception {
        LogRecord r = new LogRecord(Level.INFO, "icon_msg");
        r.setResourceBundleName("org.netbeans.lib.uihandler.TestBundle");
        r.setResourceBundle(ResourceBundle.getBundle("org.netbeans.lib.uihandler.TestBundle"));
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
        
        assertSerializedWell(r, n);
    }
    
    public void testSomeNPE() throws Exception {
        LogRecord r = new LogRecord(Level.FINE, "UI_ACTION_EDITOR");
        Node n = UINode.create(r);
        assertNotNull(n);
        assertEquals("No name", "", n.getDisplayName());
        assertNotNull(n.getName());
        assertSerializedWell(r, n);
    }
    
    public void testHasNonNullName() throws Exception {
        LogRecord r = new LogRecord(Level.WARNING, null);
        r.setThrown(new Exception());
        Node n = UINode.create(r);
        assertNotNull(n);
        assertNotNull(n.getName());
        assertSerializedWell(r, n);
    }
    public void testHasNonNullNameWhenMessageIsGiven() throws Exception {
        Exception my = new Exception("Ahoj");
        LogRecord r = new LogRecord(Level.WARNING, my.getMessage());
        r.setThrown(my);
        Node n = UINode.create(r);
        assertNotNull(n);
        assertNotNull(n.getName());
        assertSerializedWell(r, n);
    }
    
    private static void assertSerializedWell(LogRecord r, Node n) throws Exception {
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
    private static ColorModel colorModel(int transparency) {
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
