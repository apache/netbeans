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
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.menus.EditorDownButtonPopupMenuTest;
import org.netbeans.performance.j2se.menus.FilesViewPopupMenuTest;
import org.netbeans.performance.j2se.menus.FormInspectorNodePopupMenuTest;
import org.netbeans.performance.j2se.menus.MainMenuTest;
import org.netbeans.performance.j2se.menus.MainSubMenusTest;
import org.netbeans.performance.j2se.menus.ProjectsViewPopupMenuTest;
import org.netbeans.performance.j2se.menus.ProjectsViewSubMenusTest;
import org.netbeans.performance.j2se.menus.RuntimeViewPopupMenuTest;
import org.netbeans.performance.j2se.menus.SourceEditorPopupMenuTest;
import org.netbeans.performance.j2se.menus.ToolsMenuTest;
import org.netbeans.performance.j2se.setup.J2SESetup;

public class MeasureJ2SEMenusTest {

    public static NbTestSuite suite() {
        PerformanceTestCase.prepareForMeasurements();

        NbTestSuite suite = new NbTestSuite("UI Responsiveness J2SE Menus suite");
        System.setProperty("suitename", MeasureJ2SEMenusTest.class.getCanonicalName());
        System.setProperty("suite", "UI Responsiveness J2SE Menus suite");

        suite.addTest(JellyTestCase.emptyConfiguration().reuseUserDir(true)
                .addTest(J2SESetup.class)
                .addTest(MainMenuTest.class)
                .addTest(MainSubMenusTest.class)
                .addTest(EditorDownButtonPopupMenuTest.class)
                .addTest(FilesViewPopupMenuTest.class)
                .addTest(FormInspectorNodePopupMenuTest.class)
                .addTest(ProjectsViewPopupMenuTest.class)
                .addTest(ProjectsViewSubMenusTest.class)
                .addTest(RuntimeViewPopupMenuTest.class)
                .addTest(SourceEditorPopupMenuTest.class)
                .addTest(ToolsMenuTest.class, "testJavaToolsMenu", "testXmlToolsMenu", "testTxtToolsMenu")
                .suite());
        return suite;
    }
}
