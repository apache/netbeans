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
package org.netbeans.performance.j2se;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.performance.j2se.actions.AddToFavoritesTest;
import org.netbeans.performance.j2se.actions.CloseAllEditorsTest;
import org.netbeans.performance.j2se.actions.CloseEditorModifiedTest;
import org.netbeans.performance.j2se.actions.CloseEditorTabTest;
import org.netbeans.performance.j2se.actions.CloseEditorTest;
import org.netbeans.performance.j2se.actions.CommentingCodeInEditorTest;
import org.netbeans.performance.j2se.actions.CreateNBProjectTest;
import org.netbeans.performance.j2se.actions.CreateProjectTest;
import org.netbeans.performance.j2se.actions.ExpandNodesInComponentInspectorTest;
import org.netbeans.performance.j2se.actions.ExpandNodesProjectsViewTest;
import org.netbeans.performance.j2se.actions.JavaCompletionInEditorTest;
import org.netbeans.performance.j2se.actions.OpenFilesNoCloneableEditorTest;
import org.netbeans.performance.j2se.actions.OpenFilesNoCloneableEditorWithOpenedEditorTest;
import org.netbeans.performance.j2se.actions.OpenFilesTest;
import org.netbeans.performance.j2se.actions.OpenFilesWithOpenedEditorTest;
import org.netbeans.performance.j2se.actions.PageUpPageDownInEditorTest;
import org.netbeans.performance.j2se.actions.PasteInEditorTest;
import org.netbeans.performance.j2se.actions.ProfileProjectTest;
import org.netbeans.performance.j2se.actions.RefactorFindUsagesTest;
import org.netbeans.performance.j2se.actions.SaveModifiedFileTest;
import org.netbeans.performance.j2se.actions.SearchTest;
import org.netbeans.performance.j2se.actions.SelectCategoriesInNewFileTest;
import org.netbeans.performance.j2se.actions.ShiftCodeInEditorTest;
import org.netbeans.performance.j2se.actions.ShowClassMembersInNavigatorTest;
import org.netbeans.performance.j2se.actions.SwitchToFileTest;
import org.netbeans.performance.j2se.actions.SwitchViewTest;
import org.netbeans.performance.j2se.actions.TypingInEditorTest;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author mmirilovic@netbeans.org
 */
public class MeasureJ2SEActionsTest {

    public static NbTestSuite suite() {
        PerformanceTestCase.prepareForMeasurements();

        NbTestSuite suite = new NbTestSuite("UI Responsiveness J2SE Actions suite");
        System.setProperty("suitename", MeasureJ2SEActionsTest.class.getCanonicalName());
        System.setProperty("suite", "UI Responsiveness J2SE Actions suite");

        suite.addTest(JellyTestCase.emptyConfiguration().reuseUserDir(true)
                .addTest(J2SESetup.class)
                .addTest(ExpandNodesInComponentInspectorTest.class)
                .addTest(CloseAllEditorsTest.class)
                .addTest(CloseEditorTest.class)
                .addTest(CloseEditorModifiedTest.class)
                .addTest(CloseEditorTabTest.class)
                .addTest(CommentingCodeInEditorTest.class)
                .addTest(CreateNBProjectTest.class)
                .addTest(CreateProjectTest.class)
                .addTest(AddToFavoritesTest.class)
                .addTest(ExpandNodesProjectsViewTest.class)
                .addTest(JavaCompletionInEditorTest.class)
                .addTest(OpenFilesTest.class)
                .addTest(OpenFilesNoCloneableEditorTest.class)
                .addTest(OpenFilesNoCloneableEditorWithOpenedEditorTest.class)
                .addTest(OpenFilesWithOpenedEditorTest.class)
                .addTest(PageUpPageDownInEditorTest.class)
                .addTest(PasteInEditorTest.class)
                .addTest(ProfileProjectTest.class)
                .addTest(RefactorFindUsagesTest.class)
                .addTest(SaveModifiedFileTest.class)
                .addTest(SearchTest.class)
                .addTest(SelectCategoriesInNewFileTest.class)
                .addTest(ShiftCodeInEditorTest.class)
                .addTest(ShowClassMembersInNavigatorTest.class)
                .addTest(SwitchToFileTest.class)
                .addTest(SwitchViewTest.class)
                .addTest(TypingInEditorTest.class)
                .suite());
        return suite;
    }
}
