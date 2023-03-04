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

package org.netbeans.performance.j2se.dialogs;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;
import org.netbeans.modules.performance.utilities.CommonUtilities;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test of Go To Line dialog.
 *
 * @author  mmirilovic@netbeans.org
 */
public class GotoLineDialogTest extends PerformanceTestCase {

    private static EditorOperator editor;
    private String TITLE;
    
    /** Creates a new instance of GotoLineDialog */
    public GotoLineDialogTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /** Creates a new instance of GotoLineDialog */
    public GotoLineDialogTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.createConfiguration(J2SESetup.class)
                .addTest(GotoLineDialogTest.class)
                .enableModules(".*").clusters("ide|java|apisupport").suite());
        return suite;
    }

    public void testGotoLineDialog() {
        doMeasurement();
    }
    
    @Override
    public void initialize() {
        TITLE = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.editor.Bundle", "goto-title");
        editor = CommonUtilities.openFile("PerformanceTestData","org.netbeans.test.performance","Main20kB.java", true);
    }
    
    public void prepare() {
   }
    
    public ComponentOperator open(){
        new ActionNoBlock(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_MASK)).performShortcut(editor);
        return new NbDialogOperator(TITLE); // NOI18N
    }
    
    @Override
    public void shutdown(){
        if(editor!=null && editor.isShowing()) {
            editor.closeDiscard();
        }
    }
    
}
