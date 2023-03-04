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
package org.netbeans.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.View;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.editor.lib.drawing.DrawEngineDocView;
import org.netbeans.modules.editor.lib.drawing.DrawEngineLineView;

/**
 *
 * @author vita
 */
public class DrawEngineTest extends NbTestCase {

    // Accessed from DraeEngineLineView when running tests
    public static final int TEST_MARKERS_DIST = 3;
    
    private static final String PYRAMID =
          "\n"
        + "0\n"
        + "012\n"
        + "0123\n"
        + "01234\n"
        + "012345\n"
        + "0123456\n"
        + "01234567\n"
        + "012345678\n"
        + "0123456789\n"
        + "01234567890\n"
        + "012345678901\n"
        + "0123456789012\n";

    private static final String [] TEXTS = {
        // Empty text
            "",
            
        // Single char text
            "0",
            
        // Single line text
            "01",
            "012",
            "0123",
            "0123456789",
            
        // Empty lines
            "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n",
            
// XXX: tabs handling is tricky and the tests are too stupid now
//        // Tabs lines
//            "\t\t\t\t\t\t\t\t\t\t\t\t",
//            
//        // Text with tabs
//            "0\ta\tA\n"
//            + "01\tab\tAB\n"
//            + "012\tabc\tABC\n"
//            + "0123\tabcd\tABCD\n"
//            + "01234\tabcde\tABCDE\n"
//            + "012345\tabcdef\tABCDEF\n",
            
        // Pyramid
            PYRAMID
   };
            
    public DrawEngineTest(String name) {
        super(name);
    }

    // checks that offset == viewToModel(modelToView(offset)) for all offsets in a document
    @RandomlyFails
    public void testModelToViewConsistency() throws Throwable {
        for(String text : TEXTS) {
            JEditorPane jep = createJep(text);
            try {
                checkModelToViewConsistency(jep);
            } catch (Throwable e) {
                System.err.println("testModelToViewConsistency processing {");
                System.err.println(text);
                System.err.println("} failed with:");
                e.printStackTrace();
                throw e;
            } finally {
                JFrame frame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, jep);
                if (frame != null) {
                    frame.dispose();
                }
            }
        }
    }
    
    private void checkModelToViewConsistency(JEditorPane jep) throws Exception {
        Document doc = jep.getDocument();
        for(int i = 0; i <= doc.getLength(); i++) {
            // model-to-view
            Rectangle r = jep.modelToView(i);
            assertNotNull("No m2v translation: offset = " + i + ", docLen = " + doc.getLength(), r);
            
            // view-to-model
            int offset = jep.viewToModel(r.getLocation());
            assertTrue("Invalid v2m translation: " + s(r.getLocation()) + " -> " + offset+ ", docLen = " + doc.getLength(),
                    offset >= 0 && offset <= doc.getLength());
            
            // check
            assertEquals("Inconsistent m2v-v2m translation, offset = " + i 
                + ", rectangle = " + s(r) + ", docLen = " + doc.getLength(), i, offset);
        }
    }
    
    // checks that point == modelToView(viewToModel(point)) for interesting points in a component
    @RandomlyFails
    public void testViewToModelConsistency() throws Throwable {
        for(String text : TEXTS) {
            JEditorPane jep = createJep(text);
            try {
                checkViewToModelConsistency(jep);
            } catch (Throwable e) {
                System.err.println("testViewToModelConsistency processing {");
                System.err.println(text);
                System.err.println("} failed with:");
                e.printStackTrace();
                throw e;
            } finally {
                JFrame frame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, jep);
                if (frame != null) {
                    frame.dispose();
                }
            }
        }
    }
    
    private void checkViewToModelConsistency(JEditorPane jep) throws Exception {
        Document doc = jep.getDocument();
        
        assertTrue("Expecting BaseTextUI", jep.getUI() instanceof BaseTextUI);
        BaseTextUI btui = (BaseTextUI) jep.getUI();
        Insets margin = btui.getEditorUI().getTextMargin();
        int charWidth = btui.getEditorUI().defaultSpaceWidth;
        int charHeight = btui.getEditorUI().getLineHeight();

//        System.out.println("### charWidth = " + charWidth + ", charHeight = " + charHeight
//            + ", docLen = " + doc.getLength()
//            + ", jep.width = " + jep.getWidth()
//            + ", jep.height = " + jep.getHeight());
        
        Rectangle eodRectangle = null;
        Rectangle eolRectangle = null;
        for(int y = charHeight / 2 + margin.top; y < jep.getHeight(); y += charHeight) {
            if (eodRectangle == null) {
                eolRectangle = null;
            }
            for(int x = charWidth / 2 + margin.left; x < jep.getWidth(); x += charWidth) {
                Point p = new Point(x, y);

                // view-to-model translation
                int offset = jep.viewToModel(p);
                assertTrue("Invalid v2m translation: " + s(p) + " -> " + offset+ ", docLen = " + doc.getLength(),
                        offset >= 0 && offset <= doc.getLength());
                
                // model-to-view
                Rectangle r = jep.modelToView(offset);
                assertNotNull("No m2v translation: offset = " + offset + ", docLen = " + doc.getLength(), r);

                // check
                if (eodRectangle == null) {
                    boolean eod = offset == doc.getLength();
                    boolean eol = doc.getText(offset, 1).charAt(0) == '\n';
                    
                    if (eolRectangle == null) {
                        assertTrue("Inconsistent v2m-m2v translation, point = " + s(p) + " not within " + s(r)
                            + ", offset = " + offset + ", docLen = " + doc.getLength(), r.contains(p));
                        if (eol) {
                            eolRectangle = r;
                        }
                    } else {
                        assertEquals("Inconsistent v2m-m2v translation, for point = " + s(p) + " behing eol"
                            + ", offset = " + offset + ", docLen = " + doc.getLength(), eolRectangle, r);
                    }
                    
                    if (eod) {
                        eodRectangle = r;
                    }
                } else {
                    Point pointAtTheLastLine = new Point(Math.min(p.x, eolRectangle.x), eodRectangle.y);
                    assertTrue("Inconsistent v2m-m2v translation, for point = " + s(p)
                        + " behing eod, point at the last line " + s(pointAtTheLastLine) + " is outside of " + s(r)
                        + ", offset = " + offset + ", docLen = " + doc.getLength(), r.contains(pointAtTheLastLine));
                }
            }
        }
    }

    @RandomlyFails
    public void testModelToViewCorrectness() throws Throwable {
        for(String text : TEXTS) {
            JEditorPane jep = createJep(text);
            try {
                checkModelToViewCorrectness(jep);
            } catch (Throwable e) {
                System.err.println("testModelToViewCorrectness processing {");
                System.err.println(text);
                System.err.println("} failed with:");
                e.printStackTrace();
                throw e;
            } finally {
                JFrame frame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, jep);
                if (frame != null) {
                    frame.dispose();
                }
            }
        }
    }
    
    private void checkModelToViewCorrectness(JEditorPane jep) throws Exception {
        Document doc = jep.getDocument();
        
        assertTrue("Expecting BaseTextUI", jep.getUI() instanceof BaseTextUI);
        BaseTextUI btui = (BaseTextUI) jep.getUI();
        Insets margin = btui.getEditorUI().getTextMargin();
        int charWidth = btui.getEditorUI().defaultSpaceWidth;
        int charHeight = btui.getEditorUI().getLineHeight();

//        System.out.println("### charWidth = " + charWidth + ", charHeight = " + charHeight
//            + ", docLen = " + doc.getLength()
//            + ", jep.width = " + jep.getWidth()
//            + ", jep.height = " + jep.getHeight());
        
        for(int offset = 0; offset <= doc.getLength(); offset++) {
            // model-to-view translation
            Rectangle r = jep.modelToView(offset);
            assertNotNull("No m2v translation: offset = " + offset + ", docLen = " + doc.getLength(), r);

            View rootView = Utilities.getRootView(jep, DrawEngineDocView.class);
            int line = rootView.getViewIndex(offset, Position.Bias.Forward);
            int col = offset - rootView.getView(line).getStartOffset();

// XXX: this would be necessary for handling tabs, but it uses DrawEngineLineView
//      and therefore is not independent from the tested code, the inverse transformation
//      will be needed in checkViewToModel
//            int col = Utilities.getVisualColumn((BaseDocument)doc, offset);
//            int nextCol = offset >= rootView.getView(line).getEndOffset() - 1 ? col + 1 : Utilities.getVisualColumn((BaseDocument)doc, offset + 1);
//            System.out.println("### offset = " + offset + ", col = " + col + ", nextCol = " + nextCol + ", docLen = " + doc.getLength());

            Rectangle r2 = new Rectangle(
                margin.left + col * charWidth,
                margin.top + line * charHeight,
// XXX: see above comment about the tabs handling
//                (nextCol - col) * charWidth,
                charWidth,
                charHeight
            );

            assertEquals("Incorrect m2v translation: offset = " + offset + ", docLen = " + doc.getLength(), r2, r);
        }
    }

    @RandomlyFails
    public void testViewToModelCorrectness() throws Throwable {
        for(String text : TEXTS) {
            JEditorPane jep = createJep(text);
            try {
                checkViewToModelCorrectness(jep);
            } catch (Throwable e) {
                System.err.println("testViewToModelCorrectness processing {");
                System.err.println(text);
                System.err.println("} failed with:");
                e.printStackTrace();
                throw e;
            } finally {
                JFrame frame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, jep);
                if (frame != null) {
                    frame.dispose();
                }
            }
        }
    }
    
    private void checkViewToModelCorrectness(JEditorPane jep) throws Exception {
        Document doc = jep.getDocument();
        
        assertTrue("Expecting BaseTextUI", jep.getUI() instanceof BaseTextUI);
        BaseTextUI btui = (BaseTextUI) jep.getUI();
        Insets margin = btui.getEditorUI().getTextMargin();
        int charWidth = btui.getEditorUI().defaultSpaceWidth;
        int charHeight = btui.getEditorUI().getLineHeight();

//        System.out.println("### charWidth = " + charWidth + ", charHeight = " + charHeight
//            + ", docLen = " + doc.getLength()
//            + ", jep.width = " + jep.getWidth()
//            + ", jep.height = " + jep.getHeight());

        for(int y = charHeight / 2 + margin.top; y < jep.getHeight(); y += charHeight) {
            for(int x = charWidth / 2 + margin.left; x < jep.getWidth(); x += charWidth) {
                Point p = new Point(x, y);

                // view-to-model translation
                int offset = jep.viewToModel(p);
                assertTrue("Invalid v2m translation: " + s(p) + " -> " + offset + ", docLen = " + doc.getLength(),
                        offset >= 0 && offset <= doc.getLength());

                View rootView = Utilities.getRootView(jep, DrawEngineDocView.class);
                int line = (y - charHeight / 2 - margin.top) / charHeight;
                if (line >= rootView.getViewCount()) {
                    line = rootView.getViewCount() - 1;
                }
                
                int offset2 = 0;
                for(int l = 0; l < line; l++) {
                    DrawEngineLineView view = (DrawEngineLineView) rootView.getView(l);
                    offset2 += view.getEndOffset() - view.getStartOffset();
                }
                
                DrawEngineLineView view = (DrawEngineLineView) rootView.getView(line);
                int col = (x - charWidth / 2  - margin.left) / charWidth;
                int lineLength = view.getEndOffset() - view.getStartOffset();
                if (col >= lineLength) {
                    col = lineLength - 1;
                }
//                System.out.println("### x = " + x + " -> col = " + col + ", view = <" + view.getStartOffset() + ", " + view.getEndOffset() + ">, lineLength = " + lineLength);
                offset2 += col;

                assertEquals("Incorrect v2m translation: point = " + s(p) + ", docLen = " + doc.getLength(), offset2, offset);
            }
        }
    }

    @RandomlyFails
    public void testBackwardBias() throws Throwable {
        JEditorPane jep = createJep(PYRAMID);
        View rootView = Utilities.getRootView(jep, DrawEngineDocView.class);
        for(int i = 1; i < rootView.getViewCount(); i++) {
            int backwardBiasOffset = rootView.getView(i).getStartOffset();
            int previousLineEOLOffset = rootView.getView(i - 1).getEndOffset() - 1;
            
            Rectangle r1 = jep.getUI().modelToView(jep, backwardBiasOffset, Position.Bias.Backward);
            Rectangle r2 = jep.getUI().modelToView(jep, previousLineEOLOffset, Position.Bias.Forward);
            
            assertEquals("Wrong backward bias offset translation: offset = " + backwardBiasOffset
                + ", previousLineEOLOffset = " + previousLineEOLOffset
                + ", docLen = " + jep.getDocument().getLength(), r2, r1);
        }
    }
    
    private static final int WIDTH = 300;
    private static final int HEIGHT = 250;
    
    private static JEditorPane createJep(final String text) throws Exception {
        final JEditorPane [] jep = new JEditorPane[1];

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Dimension d = new Dimension(WIDTH, HEIGHT);
                
                jep[0] = new JEditorPane();
                jep[0].setEditorKit(new MyKit());
                
                JFrame frame = new JFrame();
                frame.setLayout(new BorderLayout());
                frame.add(jep[0], BorderLayout.CENTER);
                frame.pack();
                
                frame.setMinimumSize(d);
                frame.setMaximumSize(d);
                frame.setSize(d);
                
                frame.setVisible(true);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            }
        });
        
        jep[0].getDocument().insertString(0, text, null);
        Thread.sleep(1000);
        
        return jep[0];
    }
    
    private static String s(Object o) {
        if (o instanceof Point) {
            Point p = (Point)o;
            return "[" + p.x + ", " + p.y + "]";
        } else if (o instanceof Rectangle) {
            Rectangle r = (Rectangle)o;
            return "[" + r.x + ", " + r.y + ", " + r.width + ", " + r.height + "]";
        } else if (o != null) {
            return o.toString();
        } else {
            return "null";
        }
    }
    
    private static final class MyKit extends BaseKit {
        public @Override String getContentType() {
            return "text/x-test";
        }
    } //End of MyKit class
}
