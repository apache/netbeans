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

package org.netbeans.performance.j2se.actions;

import org.netbeans.modules.performance.guitracker.LoggingRepaintManager.RegionFilter;
import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.OptionsViewAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;


/**
 * Test of java completion in opened source editor.
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class JavaCompletionInEditorTest extends PerformanceTestCase {
    
    private static final int lineNumber = 61;
    private static final String ccText = "        System";
    private EditorOperator editorOperator;
    private OptionsOperator oo;
    
    /** Creates a new instance of JavaCompletionInEditor */
    public JavaCompletionInEditorTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;
    }
    
    /** Creates a new instance of JavaCompletionInEditor */
    public JavaCompletionInEditorTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(J2SESetup.class)
             .addTest(JavaCompletionInEditorTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testJavaCompletionInEditor(){
        doMeasurement();
    }
    
    @Override
    public void initialize() {
        repaintManager().addRegionFilter(COMPLETION_FILTER);
        new OptionsViewAction().performMenu();
        oo = new OptionsOperator();
        oo.selectEditor();
        new JTabbedPaneOperator(oo).selectPage("Code Completion");
        new JCheckBoxOperator(oo,"Auto Popup Documentation Window").changeSelection(false);
        oo.ok();
        new OpenAction().performAPI(new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|Main.java"));
        editorOperator = EditorWindowOperator.getEditor("Main.java");
        editorOperator.setCaretPositionToLine(lineNumber);
        editorOperator.insert(ccText);
        MY_END_EVENT = ActionTracker.TRACK_COMPONENT_SHOW;
    }
    
    public void prepare() {
        EditorWindowOperator.getEditor("Main.java");
        editorOperator.setCaretPositionToEndOfLine(lineNumber);
   }
    
    public ComponentOperator open(){
        editorOperator.typeKey('.');
        return new CompletionJListOperator();
    }
    
    @Override
    public void close() {
        super.close();
        editorOperator.setCaretPositionRelative(-1);
        editorOperator.delete(1);
    }
    
    @Override
    public void shutdown() {
        repaintManager().resetRegionFilters();
        editorOperator.closeDiscard();
    }
 
    private static final RegionFilter COMPLETION_FILTER =
            new RegionFilter() {

                public boolean accept(javax.swing.JComponent c) {
                    return c.getClass().getName().equals("org.netbeans.modules.editor.completion.CompletionScrollPane") ||
                           c.getClass().getName().equals("org.openide.text.QuietEditorPane");
                }

                public String getFilterName() {
                    return "Accept paints from org.netbeans.modules.editor.completion.CompletionScrollPane || org.openide.text.QuietEditorPane";
                }
            };
    
}
