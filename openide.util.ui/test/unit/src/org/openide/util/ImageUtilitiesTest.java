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
package org.openide.util;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.UIManager;
import junit.framework.*;

/**
 *
 * @author Radim Kubacki
 */
public class ImageUtilitiesTest extends TestCase {
    
    public ImageUtilitiesTest (String testName) {
        super (testName);
    }

    public void testMergeImages() throws Exception {
        // test if merged image preserves alpha (#90862)
        BufferedImage img1 = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
//        System.out.println("img1 transparency "+img1.getTransparency());
        java.awt.Graphics2D g = img1.createGraphics();
        Color c = new Color(255, 255, 255, 128);
        g.setColor(c);
        g.fillRect(0, 0, 16, 16);
        g.dispose();
        
        BufferedImage img2 = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
//        System.out.println("img2 transparency "+img2.getTransparency());
        g = img2.createGraphics();
        c = new Color(255, 255, 255);
        g.setColor(c);
        g.fillRect(0, 0, 2, 2);
        g.dispose();
        
        Image mergedImg = ImageUtilities.mergeImages(img1, img2, 0, 0);
        if (!(mergedImg instanceof BufferedImage)) {
            fail("It is assumed that mergeImages returns BufferedImage. Need to update test");
        }
                
        BufferedImage merged = (BufferedImage)mergedImg;
//        System.out.println("pixels " + Integer.toHexString(merged.getRGB(10, 10)) +", "+ Integer.toHexString(merged.getRGB(0, 0)));
        assertNotSame("transparency has to be kept for pixel <1,1>", merged.getRGB(10, 10), merged.getRGB(0, 0));
        
        Object ret = mergedImg.getProperty("url", null);
        assertNull("No URL property specified", ret);
    }
    
    public void testMergeImagesWithURL() throws Exception {
        final URL u = new URL("http://netbeans.org");
        // test if merged image preserves alpha (#90862)
        BufferedImage img1 = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB) {

            @Override
            public Object getProperty(String name) {
                if ("url".equals(name)) {
                    return u;
                }
                return super.getProperty(name);
            }
        };
//        System.out.println("img1 transparency "+img1.getTransparency());
        java.awt.Graphics2D g = img1.createGraphics();
        Color c = new Color(255, 255, 255, 128);
        g.setColor(c);
        g.fillRect(0, 0, 16, 16);
        g.dispose();
        
        BufferedImage img2 = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
//        System.out.println("img2 transparency "+img2.getTransparency());
        g = img2.createGraphics();
        c = new Color(255, 255, 255);
        g.setColor(c);
        g.fillRect(0, 0, 2, 2);
        g.dispose();
        
        Image mergedImg = ImageUtilities.mergeImages(img1, img2, 0, 0);
        if (!(mergedImg instanceof BufferedImage)) {
            fail("It is assumed that mergeImages returns BufferedImage. Need to update test");
        }
                
        BufferedImage merged = (BufferedImage)mergedImg;
//        System.out.println("pixels " + Integer.toHexString(merged.getRGB(10, 10)) +", "+ Integer.toHexString(merged.getRGB(0, 0)));
        assertNotSame("transparency has to be kept for pixel <1,1>", merged.getRGB(10, 10), merged.getRGB(0, 0));
        
        Object ret = mergedImg.getProperty("url", null);
        assertEquals("URL property remains from img1", u, ret);
    }
    
    
    public void testMergeBitmaskImages() throws Exception {
        // test if two bitmask images are merged to bitmask again to avoid use of alpha channel
        BufferedImage img1 = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
//        System.out.println("img1 transparency "+img1.getTransparency());
        java.awt.Graphics2D g = img1.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 16, 16);
        g.dispose();
        
        BufferedImage img2 = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
//        System.out.println("img2 transparency "+img2.getTransparency());
        g = img2.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 2, 2);
        g.dispose();
        
        Image mergedImg = ImageUtilities.mergeImages(img1, img2, 0, 0);
        if (!(mergedImg instanceof BufferedImage)) {
            fail("It is assumed that mergeImages returns BufferedImage. Need to update test");
        }
                
        BufferedImage merged = (BufferedImage)mergedImg;
        /* TRANSLUCENT when run in headless mode:
        assertEquals("Should create bitmask image", Transparency.BITMASK, merged.getTransparency());
        */
        assertEquals(Color.RED, new Color(merged.getRGB(1, 1)));
        assertEquals(Color.BLUE, new Color(merged.getRGB(10, 10)));
    }

    public void testImageToolTipWithURL() throws Exception {
        final URL u = new URL("http://netbeans.org");
        BufferedImage img1 = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB) {
            @Override
            public Object getProperty(String name) {
                if ("url".equals(name)) {
                    return u;
                }
                return super.getProperty(name);
            }
        };
        java.awt.Graphics2D g = img1.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 16, 16);
        g.dispose();
        
        assertEquals("Tool tip text should be empty", "", ImageUtilities.getImageToolTip(img1));
        
        String text = "test";
        Image imgTT1 = ImageUtilities.assignToolTipToImage(img1, text);
        assertEquals("URL location is kept", imgTT1.getProperty("url", null), u);
    }
    
    public void testImageToolTip() throws Exception {
        BufferedImage img1 = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = img1.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 16, 16);
        g.dispose();
        
        assertEquals("Tool tip text should be empty", "", ImageUtilities.getImageToolTip(img1));

        String text = "test";
        Image imgTT1 = ImageUtilities.assignToolTipToImage(img1, text);
        assertEquals("Should remain empty", "", ImageUtilities.getImageToolTip(img1));
        String str = ImageUtilities.getImageToolTip(imgTT1);
        assertEquals("We should get what we set", text, str);
        
        Icon icon = ImageUtilities.image2Icon(imgTT1);
        assertNull("URL location is null", imgTT1.getProperty("url", null));

        Image imgTT2 = ImageUtilities.assignToolTipToImage(img1, "test");
        assertSame("Instances should be same", imgTT1, imgTT2);

        imgTT2 = ImageUtilities.addToolTipToImage(img1, "");
        imgTT2 = ImageUtilities.addToolTipToImage(imgTT2, "test");
        str = ImageUtilities.getImageToolTip(imgTT2);
        String expected = "test";
        assertEquals("Tool tip text should be: " + expected + ", but it is " + str, expected, str);

        imgTT2 = ImageUtilities.addToolTipToImage(imgTT1, "test2");
        str = ImageUtilities.getImageToolTip(imgTT2);
        expected = "test" + ImageUtilities.TOOLTIP_SEPAR + "test2";
        assertEquals("Tool tip text should be: " + expected + ", but it is " + str, expected, str);

        BufferedImage img2 = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        g = img2.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 2, 2);
        g.dispose();

        imgTT1 = ImageUtilities.assignToolTipToImage(img1, "Tool tip image1");
        imgTT2 = ImageUtilities.assignToolTipToImage(img2, "Tool tip image2");
        Image result = ImageUtilities.mergeImages(imgTT1, imgTT2, 0, 0);
        expected = "Tool tip image1" + ImageUtilities.TOOLTIP_SEPAR + "Tool tip image2";
        str = ImageUtilities.getImageToolTip(result);
        assertEquals("Tool tip text should be: " + expected + ", but it is " + str, expected, str);
        
        result = ImageUtilities.mergeImages(imgTT1, img2, 0, 0);
        str = ImageUtilities.getImageToolTip(result);
        expected = "Tool tip image1";
        assertEquals("Tool tip text should be: " + expected + ", but it is " + str, expected, str);

        result = ImageUtilities.mergeImages(img1, imgTT2, 0, 0);
        str = ImageUtilities.getImageToolTip(result);
        expected = "Tool tip image2";
        assertEquals("Tool tip text should be: " + expected + ", but it is " + str, expected, str);
        
        result = ImageUtilities.mergeImages(img1, img2, 0, 0);
        str = ImageUtilities.getImageToolTip(result);
        expected = "";
        assertEquals("Tool tip text should be empty, but it is " + str, expected, str);
    }

    public void testConversions() {
        Image image = ImageUtilities.loadImage("org/openide/util/testimage.png", false);
        Icon icon = ImageUtilities.loadImageIcon("org/openide/util/testimage.png", false);

        assertNotNull("Should not be null", icon);
        assertNotNull("Should not be null", image);

        URL u = getClass().getResource("/org/openide/util/testimage.png");
        assertNotNull("URL found", u);
        assertEquals("URL obtained", u, image.getProperty("url", null));

        Icon icon2 = ImageUtilities.image2Icon(image);
        Image image2 = ImageUtilities.icon2Image(icon);

        assertEquals("Should be same instance", icon, icon2);
        assertEquals("Should be same instance", image, image2);

        assertEquals("Url is still there", u, image2.getProperty("url", null));
    }

    public void testLoadingNonExisting() {
        Image image = ImageUtilities.loadImage("org/openide/util/nonexisting.png", false);
        Icon icon = ImageUtilities.loadImageIcon("org/openide/util/nonexisting.png", false);
        assertNull(image);
        assertNull(icon);
    }
    
    public void testLoadDarkImage() {
        UIManager.put("nb.dark.theme", Boolean.FALSE);
        Image img = ImageUtilities.loadImage("org/openide/util/darkimage.png");
        assertEquals("The default image is 16x16 pixels", 16, img.getWidth(null));
        Icon icon = ImageUtilities.loadImageIcon("org/openide/util/darkicon.png", true);
        assertEquals("The default icon is 16x16 pixels", 16, icon.getIconWidth());
        
        UIManager.put("nb.dark.theme", Boolean.TRUE);
        img = ImageUtilities.loadImage("org/openide/util/darkimage.png");
        assertEquals("The special image for dark l&f is 8x8 pixels", 8, img.getWidth(null));
        icon = ImageUtilities.loadImageIcon("org/openide/util/darkicon.png", true);
        assertEquals("The special icon for dark l&f is 8x8 pixels", 8, icon.getIconWidth());

        UIManager.put("nb.dark.theme", Boolean.FALSE);
        img = ImageUtilities.loadImage("org/openide/util/darkimage.png");
        assertEquals("The default image is 16x16 pixels", 16, img.getWidth(null));
        icon = ImageUtilities.loadImageIcon("org/openide/util/darkicon.png", true);
        assertEquals("The default icon is 16x16 pixels", 16, icon.getIconWidth());
    }
}
