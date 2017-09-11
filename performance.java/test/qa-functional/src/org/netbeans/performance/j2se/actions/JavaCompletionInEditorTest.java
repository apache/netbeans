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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
