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
package org.openide.awt;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.RepaintManager;
import junit.framework.TestCase;
import static org.junit.Assert.assertNotEquals;

/**
 *
 * @author mkleint
 */
public class HtmlRendererTest extends TestCase {

    private Graphics graphic;

    public HtmlRendererTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        BufferedImage waitingForPaintDummyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        graphic = waitingForPaintDummyImage.getGraphics();

    }

    public void testCreatedLabelBehavesLikeLabel() throws Throwable {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        Throwable[] thrown = new Throwable[1];
        CountDownLatch latch = new CountDownLatch(1);
        EventQueue.invokeLater(() -> {
            Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    thrown[0] = e;
                    latch.countDown();
                }
            });
            try {
                validationBehaviorTest();
            } catch (Throwable t) {
                thrown[0] = t;
            } finally {
                latch.countDown();
            }
        });
        latch.await(10, TimeUnit.SECONDS);
        if (thrown[0] != null) {
            throw thrown[0];
        }
    }

    private void validationBehaviorTest() throws Throwable {
        assert EventQueue.isDispatchThread();

        OffscreenRepaintManager mgr = new OffscreenRepaintManager();

        CRP crp = new CRP();
        RepaintManager.setCurrentManager(mgr);

        Rectangle oldBounds = crp.lbl1.getBounds();

        crp.lbl1.setText("<html><i>This text is longer!");
        crp.assertLabelFired("text");

        mgr.assertInvalidated(crp.lbl1);
        mgr.validateInvalidComponents();
        // We can't fool Swing quite *this* well without a peer, so manually
        // trigger layout
        crp.layout();

        Rectangle newBounds = crp.lbl1.getBounds();

        assertNotEquals(oldBounds, newBounds);
        assertTrue(newBounds.width > oldBounds.width);
    }

    static final class OffscreenRepaintManager extends RepaintManager {
        Set<JComponent> toValidate = new HashSet<>();

        void assertInvalidated(Object... o) {
            assertTrue(toValidate.toString(), toValidate.containsAll(Arrays.asList(o)));
        }

        @Override
        public void paintDirtyRegions() {
            super.paintDirtyRegions(); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void validateInvalidComponents() {
            super.validateInvalidComponents();
            for (Iterator<JComponent> it=toValidate.iterator(); it.hasNext();) {
                JComponent jc = it.next();
                it.remove();
                jc.validate();
                jc.getParent().validate();
            }
        }

        @Override
        public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
            super.addDirtyRegion(c, x, y, w, h); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public synchronized void addInvalidComponent(JComponent invalidComponent) {
            super.addInvalidComponent(invalidComponent);
            toValidate.add(invalidComponent);
        }

    }

    private static final class CRP extends CellRendererPane implements LayoutManager, PropertyChangeListener {

        final JLabel lbl1;
        final JLabel lbl2;
        int doLayoutCount;
        int layoutCount;
        private final Set<String> firedProperties = new HashSet<>();

        CRP() {
            setLayout(this);
            lbl1 = HtmlRenderer.createLabel();
            lbl2 = HtmlRenderer.createLabel();
            lbl1.setText("<html><b>Bold</b> Timid");
            lbl2.setText("<html><i>Italic</i> Espanic");
            add(lbl1);
            add(lbl2);
            setSize(preferredLayoutSize(this));
            doLayoutContainer(this);
            lbl1.addPropertyChangeListener(this);
        }

        @Override
        public void doLayout() {
            doLayoutCount++;
            super.doLayout();
        }

        @Override
        public boolean isShowing() {
            return true;
        }

        @Override
        public boolean isDisplayable() {
            return true;
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
            // do nothing
        }

        @Override
        public void removeLayoutComponent(Component comp) {
            // do nothing
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            Dimension d = new Dimension();
            for (Component c : parent.getComponents()) {
                Dimension ps = c.getPreferredSize();
                if (ps == null) {
                    throw new IllegalStateException("Null pref size from " + c);
                }
                d.width += ps.width;
                d.height = Math.max(d.height, ps.height);
            }
            return d;
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }

        @Override
        public void layoutContainer(Container parent) {
            layoutCount++;
            doLayoutContainer(this);
        }

        private void doLayoutContainer(Container parent) {
            int x = 0;
            for (Component c : parent.getComponents()) {
                Dimension ps = c.getPreferredSize();
                c.setBounds(x, 0, ps.width, ps.height);
                x += ps.width;
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            firedProperties.add(evt.getPropertyName());
        }

        void assertLabelFired(String... props) {
            assert props.length > 0;
            Set<String> copy = new HashSet<>(firedProperties);
            firedProperties.clear();
            assertTrue(copy.toString(), copy.containsAll(Arrays.asList(props)));
        }

    }

    /**
     * Test of renderHTML method, of class org.openide.awt.HtmlRenderer.
     */
    public void testRenderHTML() throws Exception {
        doTestRender("<html>text</html>");
        doTestRender("<html>text</html");
        doTestRender("<html>text</h");
        doTestRender("<html>text</");
        doTestRender("<html>text<");
        doTestRender("<html>text");
        doTestRender("<html>text</html<html/>");
        doTestRender("<html>text</h</html>");
        doTestRender("<html>text</</html>");
        doTestRender("<html>text<</html>");
        doTestRender("<html>text<</html>&");
        doTestRender("<html>text<sometag");
        doTestRender55310();
        doTestRender("<html><body>text</body>");
    }

    private void doTestRender(String text) {
        HtmlRenderer.renderHTML(text, graphic, 0, 0, 1000, 1000,
                Font.getFont("Dialog"), Color.RED, HtmlRenderer.STYLE_TRUNCATE, true);
    }

    /**
     * Test issue #55310: AIOOBE from HtmlRenderer.
     *
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=55310
     */
    private void doTestRender55310() {
        doTestRender("<html><b>a </b></html> ");
    }
}
