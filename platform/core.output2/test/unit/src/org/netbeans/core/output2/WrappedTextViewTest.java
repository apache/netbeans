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

package org.netbeans.core.output2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;
import org.netbeans.core.output2.ui.AbstractOutputPane;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.Exceptions;
import org.openide.windows.IOContainer;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 *
 * @author Tim Boudreau
 */
@RandomlyFails // "Cannot write XdndAware property" from XDnDDropTargetProtocol.registerDropTarget in setUp
public class WrappedTextViewTest extends NbTestCase {
    
    public WrappedTextViewTest(String testName) {
        super(testName);
    }

    IOContainer iowin;
    private NbIO io;
    JFrame jf = null;
    static int testNum;

    @Override
    protected void setUp() throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                iowin = IOContainer.getDefault();
                JComponent wnd = LifecycleTest.getIOWindow();
                jf = new JFrame();
                jf.getContentPane().setLayout(new BorderLayout());
                jf.getContentPane().add(wnd, BorderLayout.CENTER);
                jf.setBounds(20, 20, 700, 300);
                jf.setVisible(true);
                io = (NbIO) new NbIOProvider().getIO("Test" + testNum++, false);
            }
        });

        sleep();
        io.select();
        io.getOut().println ("Test line 1");
        sleep();
        sleep();
        sleep();
        
        for (int i=0; i < 100; i++) {
            if (i == 42) {
                io.getOut().println ("This is a hyperlink to click which may or may not trigger the problem", new L());
            }
            if (i % 2 == 0) {
                io.getOut().println ("Hello there.  What a short line");
                io.getOut().println("Splead 2 - 148: Wow, we will write a long line of text here.  Very long in fact - who knows just how long it might end up being?  Well, we'll have to see.  Why it's extraordinarily long!  It might even wrap several times!  How do you like them apples, eh?  Maybe we should just go on and on and on, and never stop.  That would be cool, huh?\n");
            } else {
                //io.getErr().println ("aaa: This is a not so long line");
            }
        }
        io.getOut().close();
        sleep();
        
        SwingUtilities.invokeAndWait (new Runnable() {
            public void run() {
                ((OutputTab) iowin.getSelected()).getOutputPane().setWrapped(true);
            }
        });
    }
    
    private final void sleep() {
        try {
            Thread.sleep(200);
            SwingUtilities.invokeAndWait (new Runnable() {
                public void run() {
                    System.currentTimeMillis();
                }
            });
            Thread.sleep(200);
        } catch (Exception e) {
            fail (e.getMessage());
        }
    }
    
    /**
     * tests if caret position is computed correctly (see issue #122492)
     */
    public void testViewToModel() {
        final Integer pos1 = new Integer(-2);
        final Integer pos2 = new Integer(-1);
        class R implements Runnable {
            int charPos;
            int expCharPos;
            public void run() {
                AbstractOutputPane pane = ((OutputTab) iowin.getSelected()).getOutputPane();
                Graphics g = pane.getGraphics();
                FontMetrics fm = g.getFontMetrics(pane.getTextView().getFont());
                int charWidth = fm.charWidth('m');
                int charHeight = fm.getHeight();
                int fontDescent = fm.getDescent();
                float x = charWidth * 50;
                float y = charHeight * 1 + fontDescent;
                charPos = pane.getTextView().getUI().getRootView(null).viewToModel(x, y, new Rectangle(), new Position.Bias[]{});
                expCharPos = 43;
            }
        }
        R r = new R();
        try {
            SwingUtilities.invokeAndWait(r);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        assertTrue("viewToModel returned wrong value (it would result in bad caret position)!", r.charPos == r.expCharPos);
    }
    
    public void testModelToView() throws Exception {
        System.out.println("testModelToView");
        
        if (true) {
            //THIS TEST TAKES ABOUT 10 MINUTES TO RUN!  LEAVE IT COMMENTED OUT
            //FOR PRODUCTION AND USE IT JUST FOR DEBUGGING
            return;
        }
        class R implements Runnable {
            boolean errorsFound;
            java.awt.image.BufferedImage img;
            ArrayList<String> errors;

            public void run() {
                AbstractOutputPane pane = ((OutputTab) iowin.getSelected()).getOutputPane();
                JTextComponent text = pane.getTextView();

                View view = text.getUI().getRootView(text);

                Rectangle r = new Rectangle(1, 1, 1, 1);
                Rectangle alloc = new Rectangle();

                java.awt.image.ColorModel model = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().
                        getDefaultScreenDevice().getDefaultConfiguration().getColorModel(java.awt.Transparency.TRANSLUCENT);
                img = new java.awt.image.BufferedImage(model,
                        model.createCompatibleWritableRaster(text.getWidth() + 10, text.getHeight() + 10), model.isAlphaPremultiplied(), null);

                text.paint(img.getGraphics());
                errorsFound = false;
                errors = new ArrayList<String>();

                System.out.println("...scanning " + (text.getWidth() * text.getHeight() + " pixels to make sure viewToModel() matches modeltoView().  Expect it to take about 10 minutes."));

                for (int y = 0; y < text.getHeight(); y++) {
                    r.y = y;
                    for (int x = 0; x < text.getWidth(); x++) {
                        r.x = x;
                        alloc.setBounds(0, 0, text.getWidth(), text.getHeight());

                        int vtm = view.viewToModel(x, y, alloc, new Position.Bias[1]);

                        Rectangle mtv = null;
                        try {
                            mtv = (Rectangle) view.modelToView(vtm, Position.Bias.Forward, vtm, Position.Bias.Forward, new Rectangle(0, 0, text.getWidth(), text.getHeight()));
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }

                        int xvtm = view.viewToModel(mtv.x, mtv.y, alloc, new Position.Bias[1]);

                        if (vtm != xvtm) {
                            errorsFound = true;
                            try {
                                errors.add("ViewToModel(" + x + "," + y + ") returns character position " + vtm + "; modelToView on " + vtm + " returns " + mtv + "; that Rectangle's corner, passed back to viewToModel maps to a different character position: " + xvtm + "\n");
                                img.setRGB(x, y, vtm > xvtm ? Color.RED.getRGB() : Color.BLUE.getRGB());
                                img.setRGB(x - 1, y - 1, vtm > xvtm ? Color.RED.getRGB() : Color.BLUE.getRGB());
                                img.setRGB(x + 1, y - 1, vtm > xvtm ? Color.RED.getRGB() : Color.BLUE.getRGB());
                                img.setRGB(x + 1, y + 1, vtm > xvtm ? Color.RED.getRGB() : Color.BLUE.getRGB());
                                img.setRGB(x - 1, y + 1, vtm > xvtm ? Color.RED.getRGB() : Color.BLUE.getRGB());
                            } catch (ArrayIndexOutOfBoundsException aioobe) {
                                System.err.println("OUT OF BOUNDS: " + x + "," + y + " image width " + img.getWidth() + " img height " + img.getHeight());
                            }

                            System.err.println(x + "," + y + "=" + vtm + " -> [" + mtv.x + "," + mtv.y + "," + mtv.width + "," + mtv.height + "]->" + xvtm);
                        }

                        r.y = y; //just in case
                        r.width = 1;
                        r.height = 1;
                    }
                }
            }
        }
        R r = new R();
        SwingUtilities.invokeAndWait(r);
        
        if (r.errorsFound) {
            String dir = System.getProperty ("java.io.tmpdir");
            if (!dir.endsWith(File.separator)) {
                dir += File.separator;
            }
            String fname = dir + "outputWindowDiffs.png";
            ImageIO.write (r.img, "png", new File (fname));
            fail ("In a wrapped view, some points as mapped by viewToModel do " +
                "not map back to the same coordinates in viewToModel.  \nA bitmap " +
                "of the problem coordinates is saved in " + fname + "  Problem" +
                "spots are marked in red and blue.\n" + r.errors);
        }
    }

    public class L implements OutputListener {

        public void outputLineSelected(OutputEvent ev) {
        }

        public void outputLineAction(OutputEvent ev) {
        }

        public void outputLineCleared(OutputEvent ev) {
        }
    }    
}
