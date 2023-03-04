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
package org.netbeans.core.windows;

import java.awt.GraphicsEnvironment;
import static junit.framework.TestCase.assertNotNull;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.windows.Mode;
import org.openide.windows.ModeUtilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * This test exercises the Mode management methods of WindowManager 
 * and Mode XML extraction via ModeUtilities.
 * <br>
 * <br>
 * It would be better placed in Windows System API but it needs
 * PersistenceHandler to load the window system.
 *
 * @author Mark Phipps
 */
public class WindowManagerModeTest extends NbTestCase {

    public static junit.framework.Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(WindowManagerModeTest.class);
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

    public WindowManagerModeTest(String testName) {
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

        String xml = ModeUtilities.toXml(mode);
        assertNotNull("editor Mode XML should not be null", xml);

    }

    public void testCreateAndRemoveAnonymousMode() {

        WindowManager wm = WindowManager.getDefault();
        assertNotNull(wm);

        Mode mode = wm.createModeFromXml(anonymousModeXml);
        assertNotNull("Anonymous Mode should have been created", mode);
        
        assertEquals("Anonymous Mode should be find-able", mode, wm.findMode(mode.getName()));

        assertTrue("Anonymous Mode should have been removed", wm.removeMode(mode));
    }
    
    public void testUpdateModeConstraints() {
        
        WindowManager wm = WindowManager.getDefault();
        assertNotNull(wm);
        
        boolean updated = wm.updateModeConstraintsFromXml(editorModeXml);
        
        assertTrue("Should have found and updated the editor Mode", updated);
    }

    public void testExampleWorkFlowSaveAndLoadWorkSpace() {
        WindowManager wm = WindowManager.getDefault();
        assertNotNull(wm);

        // User creates TopComponents in the application, drags them around.
        TopComponent testTc = new TopComponent();
        wm.findMode("editor").dockInto(testTc);
        testTc.open();

        // Let's pretend thet the user dragged testTc into a new anonymous Mode.
        // Now wm.findMode(testTc).getName() would be "anonymousMode_1";
        
        // User decides to save the layout.
        for (Mode mode : wm.getModes()) {
            String xml = ModeUtilities.toXml(mode);
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
        
        // Remove unwanted Modes.
        for (Mode mode: wm.getModes()) {
            if (mode.getName().startsWith("anonymous")) {
                wm.removeMode(mode);
            }
        }
        
        // Restore the XML of the Modes somehow...
        String[] modeXmls = new String[] {editorModeXml, anonymousModeXml};
        for (String modeXml: modeXmls) {
            // Use some XML magic of your choice to determine if this is
            // an anonymous Mode or a defined Mode.
            if (modeXml.contains("anonymous")) {
                // Create the new Mode
                wm.createModeFromXml(modeXml);
            } else {
                // Adjust the constraints of defined Modes.
                wm.updateModeConstraintsFromXml(modeXml);
            }
        }
        
        // Restore the TopComponents and the names of their Modes somehow...
        testTc = new TopComponent();
        // Earlier in the test we pretended that testTc was dragged into anonymousMode_1.
        wm.findMode("anonymousMode_1").dockInto(testTc);
        assertEquals("anonymousMode_1", wm.findMode(testTc).getName());
        
    }
}
