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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.jellytools.widgets;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import junit.framework.Test;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test WidgetOperator, LabelWidgetOperator and ConnectionWidgetOperator.
 *
 * @author Jiri Skrivanek
 */
public class WidgetOperatorTest extends JellyTestCase {

    /**
     * Creates test case with given name.
     *
     * @param testName name of test case
     */
    public WidgetOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Define test suite.
     *
     * @return suite.
     */
    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(WidgetOperatorTest.class);
        return conf.clusters(".*").enableModules(".*").gui(false).addTest(
                // test cases have to be in particular order
                "testShowScene",
                "testConstructors",
                "testGetSceneOperator",
                "testGetParent",
                "testGetChildren",
                "testGetCenter",
                "testGetLocation",
                "testGetBounds",
                "testPrintDump",
                "testCreateOperator",
                "testPerformPopupAction",
                "testPerformPopupActionNoBlock",
                "testClickMouse",
                "testDragNDrop",
                // must be after testDragNDrop
                "testConnectionWidgetOperator",
                "testLabelWidgetOperator",
                "testCloseScene").suite();
    }

    /**
     * Print out test name.
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        System.out.println("### " + getName() + " ###");
    }
    protected static TopComponentOperator tco;
    protected static Scene scene;

    /**
     * Creates and shows test scene.
     */
    public static void testShowScene() {
        scene = new Utils.TestScene();
        tco = new TopComponentOperator(Utils.showScene(scene));
    }

    /**
     * Tests various constructors.
     */
    public static void testConstructors() {
        WidgetOperator wo = new WidgetOperator(tco, 0);
        assertEquals("First widget should be returned.", wo.getWidget(), scene.getChildren().get(0));
        assertEquals("Secondt widget should be returned.", new WidgetOperator(tco, 1).getWidget(), scene.getChildren().get(1));
        WidgetOperator parentWO = new WidgetOperator(scene);
        assertEquals("First widget should be returned.", new WidgetOperator(parentWO, 0).getWidget(), scene.getChildren().get(0));
        assertEquals("Secondt widget should be returned.", new WidgetOperator(parentWO, 1).getWidget(), scene.getChildren().get(1));
    }

    /**
     * Tests getSceneOperator method.
     */
    public void testGetSceneOperator() {
        assertEquals("Scene operator should hold Scene instance", scene, new WidgetOperator(tco, 0).getSceneOperator().getWidget());
    }

    /**
     * Tests getParent method.
     */
    public void testGetParent() {
        assertEquals("getParent() should hold Scene instance for the first widget", scene, new WidgetOperator(tco, 0).getParent().getWidget());
    }

    /**
     * Tests getChildren method.
     */
    public void testGetChildren() {
        List<WidgetOperator> children = new WidgetOperator(scene).getChildren();
        new WidgetOperator(scene).printDump();
        assertEquals("Scene should have 4 children.", 4, children.size());
        assertEquals("Wrong first child.", new WidgetOperator(tco, 0).getWidget(), children.get(0).getWidget());
        assertEquals("Wrong second child.", new WidgetOperator(tco, 1).getWidget(), children.get(1).getWidget());
        assertEquals("Wrong third child.", new WidgetOperator(tco, 2).getWidget(), children.get(2).getWidget());
    }

    /**
     * Tests getCenter method.
     */
    public void testGetCenter() {
        // remember center will change if you change label
        assertTrue("Wrong center of widget calculated.", new Point(120, 95).distance(new LabelWidgetOperator(tco, "Label 0").getCenter()) < 5.0);
    }

    /**
     * Tests getLocation method.
     */
    public void testGetLocation() {
        assertEquals("Wrong location of widget calculated.", new Point(100, 100), new LabelWidgetOperator(tco, "Label 0").getLocation());
    }

    /**
     * Tests getBounds method.
     */
    public void testGetBounds() {
        // bounds can slightly differ on some platforms => we check it with some tolerance
        Rectangle bounds = new LabelWidgetOperator(tco, "Label 0").getBounds();
        assertTrue("Wrong bounds of widget calculated.",
                bounds.getLocation().distance(new Point(-4, -17)) < 5.0
                && new Point((int) bounds.getWidth(), (int) bounds.getHeight()).distance(new Point(49, 25)) < 5.0);

    }

    /**
     * Tests printDump method.
     */
    public void testPrintDump() {
        StringWriter stringWriter = new StringWriter();
        WidgetOperator sceneOper = new WidgetOperator(scene);
        sceneOper.setOutput(new TestOut(null, new PrintWriter(stringWriter), new PrintWriter(stringWriter)));
        sceneOper.printDump();
        assertTrue("Dump of widgets should contain LabelWidget", stringWriter.toString().contains("LabelWidget"));
    }

    /**
     * Tests createOperator method.
     */
    public void testCreateOperator() {
        LabelWidgetOperator lwo = new LabelWidgetOperator(tco, "Label 0");
        assertTrue("Should create LabelWidgetOperator instance.",
                WidgetOperator.createOperator(lwo.getWidget()) instanceof LabelWidgetOperator);
    }

    /**
     * Test performPopupAction method.
     */
    public void testPerformPopupAction() {
        LabelWidgetOperator lwo = new LabelWidgetOperator(tco, "Label 0");
        lwo.performPopupAction("Open");
        new JDialogOperator("Open").requestClose();
    }

    /**
     * Test performPopupActionNoBlock method.
     */
    public void testPerformPopupActionNoBlock() {
        LabelWidgetOperator lwo = new LabelWidgetOperator(tco, "Label 0");
        lwo.performPopupActionNoBlock("Modal");
        new JDialogOperator("Modal").requestClose();
    }

    /**
     * Tests clickMouse method.
     */
    public void testClickMouse() {
        LabelWidgetOperator lwo = new LabelWidgetOperator(tco, "Label 0");
        lwo.clickMouse(1);
        new JDialogOperator("Mouse Clicked 1").requestClose();
    }

    /**
     * Tests dragNDrop method.
     */
    public void testDragNDrop() {
        LabelWidgetOperator lwo0 = new LabelWidgetOperator(tco, "Label 0");
        LabelWidgetOperator lwo1 = new LabelWidgetOperator(tco, "Label 1");
        // drag from one widget to another
        lwo0.dragNDrop(lwo1);
        // check connection is created
        ConnectionWidgetOperator oper = new ConnectionWidgetOperator(tco);
        LabelWidgetOperator lwoMovable = new LabelWidgetOperator(tco, "Movable Widget");
        Point location1 = lwoMovable.getLocation();
        lwoMovable.dragNDrop(30, 30);
        Point location2 = lwoMovable.getLocation();
        assertFalse("Movable Widget not moved by drag and drop.", location2.equals(location1));
        Point center = lwoMovable.getCenter();
        lwoMovable.dragNDrop(center.x, center.y, center.x + 10, center.y + 10);
        Point location3 = lwoMovable.getLocation();
        assertFalse("Movable Widget not moved by drag and drop.", location3.equals(location2));
    }

    /**
     * Tests ConnectionWidgetOperator.
     */
    public void testConnectionWidgetOperator() {
        ConnectionWidgetOperator cwo = new ConnectionWidgetOperator(tco);
        LabelWidgetOperator lwo0 = new LabelWidgetOperator(tco, "Label 0");
        LabelWidgetOperator lwo1 = new LabelWidgetOperator(tco, "Label 1");
        cwo = new ConnectionWidgetOperator(lwo0, lwo1);
        assertEquals("Wrong source widget.", lwo0.getWidget(), cwo.getSourceWidgetOperator().getWidget());
        assertEquals("Wrong target widget.", lwo1.getWidget(), cwo.getTargetWidgetOperator().getWidget());
        assertEquals("Wrong source control point.", cwo.getSourceControlPoint(), cwo.getControlPoint(0));
        assertEquals("Wrong target control point.", cwo.getTargetControlPoint(), cwo.getControlPoint(1));
        assertEquals("Wrong number of control points.", 2, cwo.getControlPoints().size());
    }

    /**
     * Tests LabelWidgetOperator.
     */
    public void testLabelWidgetOperator() {
        LabelWidgetOperator lwo = new LabelWidgetOperator(tco, "Label 0");
        assertEquals("Wrong widget found.", "Label 0", lwo.getLabel());
    }

    /**
     * Close scene.
     */
    public static void testCloseScene() {
        new JFrameOperator("Test Scene").requestClose();
    }
}
