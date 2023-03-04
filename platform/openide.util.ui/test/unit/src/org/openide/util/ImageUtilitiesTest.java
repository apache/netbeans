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
package org.openide.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.function.Function;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import junit.framework.*;
import static org.openide.util.ImageUtilities.icon2Image;
import static org.openide.util.ImageUtilities.image2Icon;

/**
 *
 * @author Radim Kubacki
 */
public class ImageUtilitiesTest extends TestCase {
    
    public ImageUtilitiesTest (String testName) {
        super (testName);
    }

    public void testNullYieldsNullLocalized() throws Exception {
        ImageIcon icon = ImageUtilities.loadImageIcon(null, true);
        assertNull(icon);
    }

    public void testNullYieldsNull() throws Exception {
        ImageIcon icon = ImageUtilities.loadImageIcon(null, false);
        assertNull(icon);
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

    public void testLoadImageCached() {
        Image image1 = ImageUtilities.loadImage("org/openide/util/testimage.png", false);
        Image image2 = ImageUtilities.loadImage("org/openide/util/testimage.png", false);
        assertSame("Expected same instance", image1, image2);
    }

    public void testLoadImageIconCached() {
        ImageIcon icon1 = ImageUtilities.loadImageIcon("org/openide/util/testimage.png", false);
        ImageIcon icon2 = ImageUtilities.loadImageIcon("org/openide/util/testimage.png", false);
        assertSame("Expected same instance", icon1, icon2);
    }

    public void testSerializeImageIcon() throws IOException, ClassNotFoundException {
        // testimage.png is a 16x16 image
        ImageIcon icon = ImageUtilities.loadImageIcon("org/openide/util/testimage.png", false);
        assertNotNull(icon);
        byte data[];
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                /* This confirmed to trigger a java.io.NotSerializableException before the custom
                serialization methods ImageUtilities.IconImageIcon were added.*/
                oos.writeObject(icon);
            }
            data = baos.toByteArray();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
          ImageIcon back = (ImageIcon) ois.readObject();
          assertEquals(icon.getIconWidth(), back.getIconWidth());
          assertEquals(icon.getIconHeight(), back.getIconHeight());
        }
    }

    public void testConversions() {
        /* Note: these are rather implementation-oriented tests. Implementation changes in
        ImageUtilities (addition or removal of caches etc.) might require this test to be
        updated, even when the API is unchanged. */

        for (boolean useExternalImage : new boolean[] {false, true}) {
            Object urlProperty;
            Image image;
            ImageIcon imageIcon;
            if (useExternalImage) {
                // Test an Image and ImageIcon instance that did not originate from ImageUtilities.
                urlProperty = null;
                image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                imageIcon = new ImageIcon(image);
            } else {
                urlProperty = getClass().getResource("/org/openide/util/testimage.png");
                image = ImageUtilities.loadImage("org/openide/util/testimage.png", false);
                imageIcon = ImageUtilities.loadImageIcon("org/openide/util/testimage.png", false);
                assertNotNull("URL found", urlProperty);
                assertNotNull("Should not be null", imageIcon);
                assertNotNull("Should not be null", image);
                assertEquals("URL obtained", urlProperty, image.getProperty("url", null));
            }

            /* These instances will no longer be the same; loadImage will now return a ToolTipImage,
            while loadImageIcon will return a IconImageIcon. (Implementation detail only; could be
            changed in the future.) */
            assertNotSame("Expected different instances in current implementation",
                    imageIcon,
                    image2Icon(image));

            /* An Icon/Image loaded via loadImage can be freely passed through icon2Image/image2Icon
            without a new instance being created. */
            assertSame("Should be same instance",
                    image,
                    icon2Image(imageIcon));

            assertSame("Should be same instance",
                    icon2Image(imageIcon),
                    icon2Image(imageIcon));

            if (!useExternalImage) {
              /* In the useExternalImage case, the original instance will be converted to a
              ToolTipImage, so we won't have the same instance here. */
              assertSame("Should be same instance",
                      image,
                      icon2Image(image2Icon(image)));
            }

            /* Again, loadImageIcon has to wrap its result in an IconImageIcon, so the instances below
            won't be the same. (Implementation detail only; could be changed in the future.) */
            assertNotSame("Expected different instances in current implementation",
                    imageIcon,
                    image2Icon(icon2Image(imageIcon)));

            Icon iconFromImage2Icon = image2Icon(image);
            assertSame("Should be same instance",
                    iconFromImage2Icon,
                    image2Icon(icon2Image(iconFromImage2Icon)));

            Icon iconFromImageIconRoundabout = image2Icon(icon2Image(imageIcon));
            assertSame("Should be same instance",
                    iconFromImageIconRoundabout,
                    image2Icon(icon2Image(iconFromImageIconRoundabout)));

            // An actual BufferedImage will return Image.UndefinedProperty rather than null.
            assertEquals("Url is still there",
                    urlProperty != null ? urlProperty : Image.UndefinedProperty,
                    icon2Image(imageIcon).getProperty("url", null));
            assertEquals("Url is still there", urlProperty, icon2Image(iconFromImage2Icon).getProperty("url", null));
            assertEquals("Url is still there", urlProperty, icon2Image(iconFromImageIconRoundabout).getProperty("url", null));
        }
    }

    public void testConvertNullImageIcon() {
        // A corner case which occured during development.
        ImageIcon imageIcon = new ImageIcon();
        Image image = ImageUtilities.icon2Image(imageIcon);
        if (image == null) {
            throw new AssertionError(
                    "icon2Image should work even with an ImageIcon for which the image is null");
        }
        // Just ensure there are no NPEs.
        image.getProperty("url", null);
    }

    /**
     * @param expectZeroedXY if the implementation is expected to paint the icon at (0,0) rather
     *        than on the supplied coordinates (e.g. because the paint happens on a cached
     *        backbuffer rather than directly on the original Graphics2D).
     */
    private static void testLosslessCustomIconTransformations(
            Function<CustomIcon, Icon> transformation, boolean expectZeroedXY)
    {
        /* Make sure that a custom Icon implementation that is passed through
        the transformation is stilled called on to paint after the icon/image conversions. */
        final CustomIcon origIcon = new CustomIcon();
        Icon iconAgain = transformation.apply(origIcon);
        origIcon.clear();
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        final int TEST_X = 45;
        final int TEST_Y = 23;
        // Also check that we don't unnecessarily crash if the Component parameter is null.
        iconAgain.paintIcon(null, g, TEST_X, TEST_Y);
        g.dispose();
        assertTrue(origIcon.wasPaintCalled);
        assertNull(origIcon.lastObservedComponent);
        if (expectZeroedXY) {
            assertEquals(0.0, origIcon.lastSeenX);
            assertEquals(0.0, origIcon.lastSeenY);
        } else {
            assertEquals((double) TEST_X, origIcon.lastSeenX);
            assertEquals((double) TEST_Y, origIcon.lastSeenY);
        }
    }

    public void testCustomIconImplementationRetained() {
        testLosslessCustomIconTransformations(new Function<CustomIcon, Icon>() {
            @Override
            public Icon apply(CustomIcon origIcon) {
                Image image = ImageUtilities.icon2Image(origIcon);
                assertTrue(origIcon.wasPaintCalled);
                origIcon.clear();
                Icon iconAgain = ImageUtilities.image2Icon(image);
                assertFalse(origIcon.wasPaintCalled);
                return iconAgain;
            }
        }, false);
        testLosslessCustomIconTransformations(new Function<CustomIcon, Icon>() {
            @Override
            public Icon apply(CustomIcon origIcon) {
                return ImageUtilities.createDisabledIcon(origIcon);
            }
        }, true);
        testLosslessCustomIconTransformations(new Function<CustomIcon, Icon>() {
            @Override
            public Icon apply(CustomIcon origIcon) {
                BufferedImage otherImage =
                        new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                // The offset is for otherImage, so should not cause the test to fail.
                return image2Icon(
                        ImageUtilities.mergeImages(icon2Image(origIcon), otherImage, 4, 12));
            }
        }, true);
        testLosslessCustomIconTransformations(new Function<CustomIcon, Icon>() {
            @Override
            public Icon apply(CustomIcon origIcon) {
                BufferedImage otherImage =
                        new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                return image2Icon(
                        ImageUtilities.mergeImages(otherImage, icon2Image(origIcon), 0, 0));
            }
        }, true);
    }

    public void testCustomIconImplementationGetsValidComponent()
            throws InterruptedException, InvocationTargetException
    {
        final CustomIcon origIcon = new CustomIcon();
        /* The dummy Component parameter that is fed to Icon.paintIcon is only guaranteed to be
        initialized once the Event Dispatch Thread has had a chance to run. */
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                assertFalse(origIcon.wasPaintCalled);
                assertNull(origIcon.lastObservedComponent);
                ImageUtilities.icon2Image(origIcon);
                assertTrue(origIcon.wasPaintCalled);
                assertNotNull(origIcon.lastObservedComponent);
            }
        });
        /* Once ImageUtilities has initialized its dummy Component, paintIcon should keep receiving
        a valid instance no matter which thread it is running on. */
        final CustomIcon origIcon2 = new CustomIcon();
        ImageUtilities.icon2Image(origIcon2);
        assertTrue(origIcon2.wasPaintCalled);
        assertNotNull(origIcon2.lastObservedComponent);
    }

    public void testZeroSideIconFilter() {
        // Make sure there are no errors when one side is zero.
        Icon icon = ImageUtilities.createDisabledIcon(new ZeroSideIcon());
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        icon.paintIcon(null, g, 0, 0);
        icon = ImageUtilities.image2Icon(
                ImageUtilities.createDisabledImage(ImageUtilities.icon2Image(new ZeroSideIcon())));
        icon.paintIcon(null, g, 0, 0);
    }

    private static final class ZeroSideIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
        }

        @Override
        public int getIconWidth() {
            return 0;
        }

        @Override
        public int getIconHeight() {
            return 8;
        }
    }

    private static final class CustomIcon implements Icon {
        public volatile Component lastObservedComponent;
        public volatile boolean wasPaintCalled;
        public volatile double lastSeenX;
        public volatile double lastSeenY;

        private void clear() {
            lastObservedComponent = null;
            wasPaintCalled = false;
            lastSeenX = Double.NaN;
            lastSeenY = Double.NaN;
        }

        public CustomIcon() {
            clear();
        }

        @Override
        public void paintIcon(Component c, Graphics g0, int x, int y) {
            this.lastObservedComponent = c;
            wasPaintCalled = true;
            Graphics2D g = (Graphics2D) g0;
            g.translate(x, y);
            AffineTransform tx = g.getTransform();
            final int txType = tx.getType();
            assertTrue(txType == 0 || txType == AffineTransform.TYPE_TRANSLATION);
            lastSeenX = tx.getTranslateX();
            lastSeenY = tx.getTranslateY();
        }

        @Override
        public int getIconWidth() {
          return 16;
        }

        @Override
        public int getIconHeight() {
          return 16;
        }
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
