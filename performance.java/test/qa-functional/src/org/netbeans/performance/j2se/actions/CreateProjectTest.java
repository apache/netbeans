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

import junit.framework.Test;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.performance.j2se.setup.J2SESetup;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NewJavaProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test create projects
 *
 * @author mmirilovic@netbeans.org
 */
public class CreateProjectTest extends PerformanceTestCase {

    private NewJavaProjectNameLocationStepOperator wizard_location;
    private String category, project, project_name, project_type;

    /**
     * Creates a new instance of CreateProject
     *
     * @param testName the name of the test
     */
    public CreateProjectTest(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN = 20000;
    }

    /**
     * Creates a new instance of CreateProject
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateProjectTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN = 20000;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(J2SESetup.class).addTest(CreateProjectTest.class).suite();
    }

    public void testCreateJavaApplicationProject() {
        // "Java"
        category = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "Templates/Project/Standard");
        project = "Java Application";
        project_type = "JavaApplication";
        doMeasurement();
    }

    public void testCreateJavaLibraryProject() {
        // "Java"
        category = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "Templates/Project/Standard");
        project = "Java Class Library";
        project_type = "JavaLibrary";
        doMeasurement();
    }

    @Override
    public void initialize() {
    }

    public void prepare() {
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.next();
        wizard_location = new NewJavaProjectNameLocationStepOperator();
        String directory = System.getProperty("nbjunit.workdir");
        wizard_location.txtProjectLocation().setText("");
        wizard_location.txtProjectLocation().setText(directory);
        project_name = project_type + "_" + CommonUtilities.getTimeIndex();
        wizard_location.txtProjectName().setText("");
        wizard_location.txtProjectName().typeText(project_name);
    }

    public ComponentOperator open() {
        wizard_location.finish();
        return null;
    }

    @Override
    public void close() {
        CommonUtilities.actionOnProject(project_name, "Close");
    }
}
