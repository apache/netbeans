/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.core.windows;

import java.awt.GraphicsEnvironment;
import static junit.framework.TestCase.assertNotNull;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.windows.Mode;
import org.openide.windows.ModeUtility;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * This test exercises the WindowManager.ModeManager interface, 
 * as implemented by WindowManagerImpl, through the API of ModeUtility.
 *
 * @author Mark Phipps
 */
public class ModeManagerTest extends NbTestCase {

    public static junit.framework.Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(ModeManagerTest.class);
    }

    private static boolean loaded = false;

    private final String anonymousModeXml
            = "<mode version=\"2.4\">"
            + "<name unique=\"anonymousMode_1\"/>"
            + "<kind type=\"editor\"/>"
            + "<state type=\"joined\"/>"
            + "<constraints>"
            + "<path weight=\"0.5\" number=\"1\" orientation=\"horizontal\"/>"
            + "</constraints>"
            + "<bounds height=\"0\" width=\"0\" y=\"0\" x=\"0\"/>"
            + "<frame state=\"0\"/>"
            + "<empty-behavior permanent=\"false\"/>"
            + "</mode>";

    private final String editorModeXml
            = "<mode version=\"2.4\">"
            + "<name unique=\"editor\"/>"
            + "<kind type=\"editor\"/>"
            + "<state type=\"joined\"/>"
            + "<constraints>"
            + "<path weight=\"0.5\" number=\"0\" orientation=\"horizontal\" />"
            + "</constraints>"
            + "<bounds height=\"0\" width=\"0\" y=\"0\" x=\"0\" />"
            + "<frame state=\"0\" />"
            + "<empty-behavior permanent=\"true\" />"
            + "</mode>";

    public ModeManagerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        if (!loaded) {
            // Load just once for all tests in this class
            Lookup.getDefault().lookup(ModuleInfo.class);
            PersistenceHandler.getDefault().load();
            loaded = true;
        }
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testGetEditorModeXml() {

        WindowManager wm = WindowManager.getDefault();
        assertNotNull(wm);

        Mode mode = wm.findMode("editor");

        String xml = ModeUtility.toXml(mode);
        assertNotNull("editor Mode XML should not be null", xml);

    }

    public void testCreateAndRemoveAnonymousMode() {

        WindowManager wm = WindowManager.getDefault();
        assertNotNull(wm);

        ModeUtility.createModeFromXml(wm, anonymousModeXml);
        Mode mode = wm.findMode("anonymousMode_1");
        assertNotNull("Anonymous Mode should have been created", mode);

        assertTrue("Anonymous Mode should have been removed", ModeUtility.removeMode(wm, mode));
    }

    public void testExampleWorkFlowSaveAndLoadWorkSpace() {
        WindowManager wm = WindowManager.getDefault();
        assertNotNull(wm);

        // User creates TopComponents in the application, drags them around.
        TopComponent testTc = Component00.getDefault();
        wm.findMode("editor").dockInto(testTc);
        testTc.open();

        // Let's pretend thet the user dragged testTc into a new anonymous Mode.
        // Now wm.findMode(testTc).getName() would be "anonymousMode_1";
        
        // User decides to save the layout.
        for (Mode mode : wm.getModes()) {
            String xml = ModeUtility.toXml(mode);
            // Save the Mode xml somehow...
        }
        
        for (TopComponent tc : wm.getRegistry().getOpened()) {
            // Save the state of the TopComponent somehow...
            // Also save the name of the Mode the TopComponent was docked into:
            String modeName = wm.findMode(tc).getName();
        }
                
        // Later you restore the layout...
        // Close open TopComponents.
        for (TopComponent tc : wm.getRegistry().getOpened()) {
            tc.close();
        }
        Component00.clearRef();
        
        // Remove unwanted Modes.
        for (Mode mode: wm.getModes()) {
            if (mode.getName().startsWith("anonymous")) {
                ModeUtility.removeMode(wm, mode);
            }
        }
        
        // Restore the XML of the Modes somehow...
        String[] modeXmls = new String[] {editorModeXml, anonymousModeXml};
        for (String modeXml: modeXmls) {
            // Use some XML magic of your choice to determine if this is
            // an anonymous Mode or a defined Mode.
            if (modeXml.contains("anonymous")) {
                // Create the new Mode
                ModeUtility.createModeFromXml(wm, modeXml);
            } else {
                // Adjust the constraints of defined Modes.
                ModeUtility.updateModeConstraintsFromXml(wm, modeXml);
            }
        }
        
        // Restore the TopComponents and the names of their Modes somehow...
        testTc = Component00.getDefault();
        // Earlier in the test we pretended that testTc was dragged into anonymousMode_1.
        wm.findMode("anonymousMode_1").dockInto(testTc);
        assertEquals("anonymousMode_1", wm.findMode(testTc).getName());
        
    }
}
