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
import org.netbeans.performance.j2se.dialogs.AboutDialogTest;
import org.netbeans.performance.j2se.dialogs.AddJDBCDriverDialogTest;
import org.netbeans.performance.j2se.dialogs.AddProfilingPointWizardTest;
import org.netbeans.performance.j2se.dialogs.AttachDialogTest;
import org.netbeans.performance.j2se.dialogs.DeleteFileDialogTest;
import org.netbeans.performance.j2se.dialogs.DocumentsDialogTest;
import org.netbeans.performance.j2se.dialogs.FavoritesWindowTest;
import org.netbeans.performance.j2se.dialogs.FilesWindowTest;
import org.netbeans.performance.j2se.dialogs.FindInProjectsTest;
import org.netbeans.performance.j2se.dialogs.GotoLineDialogTest;
import org.netbeans.performance.j2se.dialogs.HelpContentsWindowTest;
import org.netbeans.performance.j2se.dialogs.InternationalizeDialogTest;
import org.netbeans.performance.j2se.dialogs.JavaPlatformManagerTest;
import org.netbeans.performance.j2se.dialogs.JavadocIndexSearchTest;
import org.netbeans.performance.j2se.dialogs.LibrariesManagerTest;
import org.netbeans.performance.j2se.dialogs.NetBeansPlatformManagerTest;
import org.netbeans.performance.j2se.dialogs.NewBreakpointDialogTest;
import org.netbeans.performance.j2se.dialogs.NewDatabaseConnectionDialogTest;
import org.netbeans.performance.j2se.dialogs.NewFileDialogTest;
import org.netbeans.performance.j2se.dialogs.NewProjectDialogTest;
import org.netbeans.performance.j2se.dialogs.NewWatchDialogTest;
import org.netbeans.performance.j2se.dialogs.OpenFileDialogTest;
import org.netbeans.performance.j2se.dialogs.OpenProjectDialogTest;
import org.netbeans.performance.j2se.dialogs.OptionsTest;
import org.netbeans.performance.j2se.dialogs.OutputWindowTest;
import org.netbeans.performance.j2se.dialogs.PluginManagerTest;
import org.netbeans.performance.j2se.dialogs.ProfilerWindowsTest;
import org.netbeans.performance.j2se.dialogs.ProjectPropertiesWindowTest;
import org.netbeans.performance.j2se.dialogs.ProjectsWindowTest;
import org.netbeans.performance.j2se.dialogs.ProxyConfigurationTest;
import org.netbeans.performance.j2se.dialogs.RefactorFindUsagesDialogTest;
import org.netbeans.performance.j2se.dialogs.RefactorMoveClassDialogTest;
import org.netbeans.performance.j2se.dialogs.RefactorRenameDialogTest;
import org.netbeans.performance.j2se.dialogs.RuntimeWindowTest;
import org.netbeans.performance.j2se.dialogs.ServerManagerTest;
import org.netbeans.performance.j2se.dialogs.TemplateManagerTest;
import org.netbeans.performance.j2se.dialogs.ToDoWindowTest;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author mmirilovic@netbeans.org
 */
public class MeasureJ2SEDialogsTest {

    public static NbTestSuite suite() {
        PerformanceTestCase.prepareForMeasurements();

        NbTestSuite suite = new NbTestSuite("UI Responsiveness J2SE Dialogs suite");
        System.setProperty("suitename", MeasureJ2SEDialogsTest.class.getCanonicalName());
        System.setProperty("suite", "UI Responsiveness J2SE Dialogs suite");

        suite.addTest(JellyTestCase.emptyConfiguration().reuseUserDir(true)
                .addTest(J2SESetup.class)
                .addTest(AboutDialogTest.class)
                .addTest(AddJDBCDriverDialogTest.class)
                .addTest(AttachDialogTest.class)
                .addTest(FavoritesWindowTest.class)
                .addTest(FilesWindowTest.class)
                .addTest(HelpContentsWindowTest.class)
                .addTest(AddProfilingPointWizardTest.class)
                .addTest(DeleteFileDialogTest.class)
                .addTest(DocumentsDialogTest.class)
                .addTest(FindInProjectsTest.class)
                .addTest(GotoLineDialogTest.class)
                .addTest(InternationalizeDialogTest.class)
                .addTest(JavaPlatformManagerTest.class)
                .addTest(JavadocIndexSearchTest.class)
                .addTest(LibrariesManagerTest.class)
                .addTest(NetBeansPlatformManagerTest.class)
                .addTest(NewBreakpointDialogTest.class)
                .addTest(NewDatabaseConnectionDialogTest.class)
                .addTest(NewFileDialogTest.class)
                .addTest(NewProjectDialogTest.class)
                .addTest(NewWatchDialogTest.class)
                .addTest(OpenFileDialogTest.class)
                .addTest(OpenProjectDialogTest.class)
                .addTest(OptionsTest.class)
                .addTest(OutputWindowTest.class)
                .addTest(PluginManagerTest.class)
                .addTest(ProfilerWindowsTest.class)
                .addTest(ProjectPropertiesWindowTest.class)
                .addTest(ProjectsWindowTest.class)
                .addTest(ProxyConfigurationTest.class)
                .addTest(RefactorFindUsagesDialogTest.class)
                .addTest(RefactorMoveClassDialogTest.class)
                .addTest(RefactorRenameDialogTest.class)
                .addTest(RuntimeWindowTest.class)
                .addTest(ServerManagerTest.class)
                .addTest(TemplateManagerTest.class)
                .addTest(ToDoWindowTest.class)
                .suite());
        return suite;
    }
}
