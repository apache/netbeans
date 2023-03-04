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
