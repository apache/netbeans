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
