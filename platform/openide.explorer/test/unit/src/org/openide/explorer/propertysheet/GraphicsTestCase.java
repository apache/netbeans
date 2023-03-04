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

package org.openide.explorer.propertysheet;

import java.awt.Color;
import java.awt.Component;
import java.awt.DisplayMode;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import org.openide.explorer.propertysheet.ExtTestCase.WaitWindow;
import org.openide.explorer.propertysheet.ExtTestCase.WrapperRunnable;

/**
 * @author  Tim Boudreau
 */
public class GraphicsTestCase extends ExtTestCase {
    private static int count=0;
    
    /** Creates a new instance of GraphicsExtTestCase */
    public GraphicsTestCase(String name) {
        super(name);
    }
    
    public void testNothing() {
        //do nothing - method just here to keep JUnit happy
    }
    
    public static void main(String[] args) {
        ExtTestCase.main(args);
        //        SwingUtilities.invokeLater (new Runnable() {
        //            public void run() {
        System.err.println("Can safely run pixel tests: " + canSafelyRunPixelTests());
        //            }
        //        });
    }
    
    /** Determine if the graphics environment is 24 bit and has standard fonts,
     * so pixel position and color tests will not erroneously fail.  This method is
     * thread safe.
     */
    public static boolean canSafelyRunPixelTests() {
        if (graphicsTestsSafe != null) {
            return graphicsTestsSafe.booleanValue();
        }
        
        try {
            boolean result = false;
            if (GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadless()) {
                System.err.println("Cannot run test in a headless environment");
                graphicsTestsSafe = Boolean.FALSE;
                return false;
            }
            
            DisplayMode dm =
                    GraphicsEnvironment.getLocalGraphicsEnvironment().
                    getDefaultScreenDevice().getDisplayMode();
            
            int i = dm.getBitDepth();
            if (i == dm.BIT_DEPTH_MULTI || i >= 16) {
                result = true;
                
                Font f = UIManager.getFont("controlFont");
                if (f == null) {
                    f = new JTable().getFont();
                }
                Graphics g = GraphicsEnvironment.getLocalGraphicsEnvironment().
                        getDefaultScreenDevice().getDefaultConfiguration().
                        createCompatibleImage(10,10).getGraphics();
                
                FontMetrics fm = g.getFontMetrics(f);
                if (fm.getHeight() != 16) {
                    System.err.println("Cannot run this test - default font size is not " + 16 + " pixels in height - could lead to false fails");
                    System.err.println("Basic font size is " + fm.getHeight());
                    //Some environments, such as Mandrake linux or Windows with
                    //large fonts will supply fonts bigger than the error icon,
                    //causing the pixel tests to fail due to icon positioning
                    //differences
                    result = false;
                }
            }
            if (result) {
                result = tryPrototypePixelTest();
            }
            return result;
        } catch (Exception e) {
            graphicsTestsSafe = Boolean.FALSE;
            e.printStackTrace();
            return false;
        }
    }
    
    private static Boolean graphicsTestsSafe = null;
    /** Does a slightly more involved test, displaying actual icons and images
     * that should always match and seeing if they do.  */
    private static boolean tryPrototypePixelTest() throws Exception {
        if (graphicsTestsSafe != null) {
            return graphicsTestsSafe.booleanValue();
        }
        try {
            jf = new JFrame();
            final JLabel jl = new JLabel("Show the dialog");
            jl.setOpaque(true);
            jl.setIcon(new TestIcon());
            
            jf.getContentPane().setLayout(new FlowLayout());
            jf.getContentPane().add(jl);
            jf.setBounds(20,20, 100,100);
            new WaitWindow(jf);
            jf.pack();
            
            boolean frameGotFocus = checkFocusedContainer(jf);
            
            sleep();
            Toolkit.getDefaultToolkit().sync();
            
            int y = jl.getHeight() / 2;
            int xoff = jl.getLocation().x;
            
            boolean result = checkColorOnComponent(jl, Color.GREEN, new Point(5,y));
            
            final BufferedImage img =
                    loadImage("org/netbeans/core/resources/defaultOpenFolder.gif");
            
            if (result) {
                maybeInvokeLater(new Runnable() {
                    public void run() {
                        try {
                            jl.setIcon(new ImageIcon(img));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                result = checkPixelFromImage(img, jl, 5, 8, 5, 8);
            }
            
            graphicsTestsSafe = result ? Boolean.TRUE : Boolean.FALSE;
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (jf != null) {
                //      jf.hide();
                //      jf.dispose();
            }
        }
    }
    
    private static class TestIcon implements Icon {
        public int getIconHeight() {
            return 20;
        }
        
        public int getIconWidth() {
            return 20;
        }
        
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(Color.GREEN);
            g.fillRect(x, y, 20, 20);
        }
    }
    
    /** Assert that the pixel at the specified point is of the color <code>color</code> */
    protected static void assertColorOnComponent(Component c, Color color, Point p) throws Exception {
        ComponentPixelChecker cpc = new ComponentPixelChecker(c, color, p);
        click(c);
        maybeInvokeLater(cpc);
        cpc.assertMatch();
    }
    
    /** Assert that the pixel at the specified coordinates is of the color <code>color</code> */
    protected static void assertColorOnComponent(Component c, Color color, int x, int y) throws Exception {
        assertColorOnComponent(c, color, new Point(x,y));
    }
    
    
    /** Asserts that a pixel at a given position in an image matches a
     * pixel in a given position in a component */
    protected static void assertPixelFromImage(final Image i, final Component c, final int imageX, final int imageY, final int compX, final int compY) throws Exception {
        ImagePixelChecker pix = new ImagePixelChecker(i, c, imageX, imageY, compX, compY);
        maybeInvokeLater(pix);
        pix.assertMatch();
    }
    
    /** Check that the pixel at compX, compY on the component is of the same
     * color as the pixel in the passed image at imageX, imageY.  Used by the
     * generic tests that should always pass to decide if it is safe to run
     * pixel tests. */
    private static boolean checkPixelFromImage(final Image i, final Component c, final int imageX, final int imageY, final int compX, final int compY) throws Exception {
        ImagePixelChecker pix = new ImagePixelChecker(i, c, imageX, imageY, compX, compY);
        maybeInvokeLater(pix);
        return pix.compare();
    }
    
    
    /** Check that the pixel at compX, compY on the component is of the passed
     * color. Used by the
     * generic tests that should always pass to decide if it is safe to run
     * pixel tests. */
    private static boolean checkColorOnComponent(Component c, Color color, Point p) throws Exception {
        System.err.println("Checking for color " + color);
        click(c);
        ComponentPixelChecker cpc = new ComponentPixelChecker(c, color, p);
        maybeInvokeLater(cpc);
        return cpc.compare();
    }
    
    /** Runnable class that ensures a component is drawn and compares a
     * pixel at some coordinates on it with a color.  */
    private static class ComponentPixelChecker extends WrapperRunnable {
        private Component comp;
        private Point pos;
        private Color color;
        public ComponentPixelChecker(Component c, Color color, Point pos) {
            this.comp = c;
            this.color = color;
            this.pos = pos;
        }
        
        private Color compColor = null;
        private Boolean finalResult = null;
        public Color getCompColor() throws Exception {
            if (compColor == null) {
                compare();
            }
            return compColor;
        }
        
        public Color getColor() {
            return color;
        }
        
        BufferedImage compImg = null;
        public boolean compare() throws Exception {
            if (finalResult != null) {
                return finalResult.booleanValue();
            }
            BufferedImage img = getImageOfComponent();
            int[] cArr = new int[3];
            img.getData().getPixel(pos.x, pos.y, cArr);
            Color compColor = new Color(cArr[0], cArr[1], cArr[2]);
            
            System.err.println("Comparing " + compColor + " with " + getColor());
            
            return getColor().equals(compColor);
        }
        
        public void assertMatch() throws Exception {
            assertTrue("Component pixel at " + pos + " is " + getCompColor() + " but should be " + getColor(), compare());
        }
        
        public BufferedImage getImageOfComponent() throws Exception {
            //Block to force graphics initialization
            ((JComponent)comp).paintImmediately(0,0,comp.getWidth(), comp.getHeight());
            
            sleep();
            sleep();
            final BufferedImage result = new BufferedImage(comp.getWidth(), comp.getHeight(), BufferedImage.TYPE_INT_RGB);
            sleep();
            sleep();
            Toolkit.getDefaultToolkit().sync();
            ((JComponent) comp).paintAll(result.getGraphics());
            sleep();
            showFrameForImage(result);
            return result;
        }
        
        private void showFrameForImage(final BufferedImage bi) throws Exception {
            JFrame jf = new JFrame("assertPixelFromImage " + (count ++) + " (look for the yellow line)") {
                @Override
                public void paint(Graphics g) {
                    new ImageIcon(bi).paintIcon(this, g, 25, 25);
                    g.setColor(Color.YELLOW);
                    g.drawLine(pos.x+20, pos.y+25, pos.x+25, pos.y+25);
                }
            };
            jf.setBounds(100,100, pos.x + 100, pos.y + 100);
            new WaitWindow(jf);
        }
    }
    
    /** Runnable class that compares a pixel on an image and a pixel on a
     * component.  */
    private static class ImagePixelChecker extends WrapperRunnable {
        private Component comp;
        private Image image;
        private Point imgPoint;
        private Point compPoint;
        
        public ImagePixelChecker(Image i, Component c, int imageX, int imageY, int compX, int compY) {
            this(i, c, new Point(imageX, imageY), new Point(compX, compY));
        }
        
        public ImagePixelChecker(Image i, Component c, Point imgPoint, Point compPoint) {
            this.image = i;
            this.comp = c;
            this.imgPoint = imgPoint;
            this.compPoint = compPoint;
        }
        
        public BufferedImage getImageOfComponent() throws Exception {
            sleep();
            sleep();
            int maxX = Math.max(imgPoint.x, compPoint.x) + 20;
            int maxY = Math.max(imgPoint.y, compPoint.y) + 20;
            final BufferedImage result = new BufferedImage(comp.getWidth(), comp.getHeight(), BufferedImage.TYPE_INT_RGB);
            sleep();
            sleep();
            ((JComponent) comp).paintAll(result.getGraphics());
            sleep();
            showFrameForImage(result, compPoint);
            return result;
        }
        
        private void showFrameForImage(final BufferedImage bi, final Point p) throws Exception {
            JFrame jf = new JFrame("assertPixelFromImage " + (count ++) + " (look for the yellow line)") {
                @Override
                public void paint(Graphics g) {
                    new ImageIcon(bi).paintIcon(this, g, 25, 25);
                    g.setColor(Color.YELLOW);
                    g.drawLine(p.x+20, p.y+25, p.x+25, p.y+25);
                }
            };
            jf.setBounds(100,100, p.x + 100, p.y + 100);
            new WaitWindow(jf);
        }
        
        public BufferedImage getImage() throws Exception {
            sleep();
            BufferedImage result = image instanceof BufferedImage ? (BufferedImage) image
                    : toBufferedImage(image);
            
            result.getSampleModel().getNumBands();
            showFrameForImage(result, imgPoint);
            sleep();
            sleep();
            return result;
        }
        
        private Boolean finalResult = null;
        
        private Color compColor = null;
        private Color imgColor = null;
        
        public Color getCompColor() throws Exception {
            if (compColor == null) {
                compare();
            }
            return compColor;
        }
        
        public Color getImageColor() throws Exception {
            if (imgColor == null) {
                compare();
            }
            return imgColor;
        }
        
        public boolean compare() throws Exception {
            if (finalResult != null) {
                return finalResult.booleanValue();
            }
            BufferedImage compImg = getImageOfComponent();
            BufferedImage img = getImage();
            compColor = new Color(compImg.getRGB(compPoint.x, compPoint.y));
            imgColor = new Color(img.getRGB(imgPoint.x, imgPoint.y));
            
            finalResult = imgColor.equals(compColor) ? Boolean.TRUE :
                Boolean.FALSE;
            
            System.err.println("Comparing " + compColor + " at " + compPoint + " with " + imgColor + " at " + imgPoint);
            
            Graphics g = comp.getGraphics();
            
            g.setColor(Color.YELLOW);
            g.drawLine(compPoint.x-5, compPoint.y, compPoint.x, compPoint.y);
            
            System.err.println("Pixel comparison returns " + finalResult);
            return finalResult.booleanValue();
        }
        
        public void assertMatch() throws Exception {
            assertTrue("Image pixel at " + imgPoint + " is " + getImageColor() + " and component should match at point " + compPoint + " but component color at those coordinates is " + getCompColor(), compare());
        }
        
        public void run() {
            try {
                compare();
            } catch (Exception e){
                exception = e;
            }
        }
    }
    
    /** Just a handy routine to load images. */
    protected static final BufferedImage loadImage(String resName) throws Exception {
        URL url = GraphicsTestCase.class.getClassLoader().getResource(resName);
        return ImageIO.read(url);
    }
    
    //Shamelessly stolen from util.IconManager
    private static final BufferedImage toBufferedImage(Image img) {
        // load the image
        new ImageIcon(img);
        BufferedImage rep = createBufferedImage(img.getWidth(null), img.getHeight(null));
        Graphics g = rep.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        img.flush();
        return rep;
    }
    
    /** Creates BufferedImage 16x16 and Transparency.BITMASK */
    private static final BufferedImage createBufferedImage(int width, int height) {
        ColorModel model = GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration().getColorModel(Transparency.BITMASK);
        BufferedImage buffImage = new BufferedImage(model,
                model.createCompatibleWritableRaster(width, height), model.isAlphaPremultiplied(), null);
        return buffImage;
    }
}
