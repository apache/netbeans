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
