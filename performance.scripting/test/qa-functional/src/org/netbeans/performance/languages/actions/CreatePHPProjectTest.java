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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.performance.languages.actions;

import junit.framework.Test;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.languages.setup.ScriptingSetup;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.EditorOperator;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;

/**
 *
 * @author mkhramov@netbeans.org, mrkam@netbeans.org
 */
public class CreatePHPProjectTest extends PerformanceTestCase {

    private NewProjectWizardOperator wizard;
    public String category, project, project_name, project_type, editor_name;

    public CreatePHPProjectTest(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN = 20000;
    }

    public CreatePHPProjectTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN = 20000;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(ScriptingSetup.class).addTest(CreatePHPProjectTest.class).suite();
    }

    @Override
    public void initialize() {
        closeAllModal();
    }

    @Override
    public void prepare() {
        repaintManager().addRegionFilter(LoggingRepaintManager.IGNORE_STATUS_LINE_FILTER);
        repaintManager().addRegionFilter(LoggingRepaintManager.IGNORE_DIFF_SIDEBAR_FILTER);
        wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.next();
    }

    public ComponentOperator open() {
        wizard.finish();
        return null;
    }

    @Override
    public void close() {
        repaintManager().resetRegionFilters();
        EditorOperator.closeDiscardAll();
    }

    public void testCreatePhpProject() {
        category = "PHP";
        project = Bundle.getString("org.netbeans.modules.php.project.ui.wizards.Bundle", "Templates/Project/PHP/PHPProject.php");
        project_type = "PHPApplication";
        editor_name = "index.php";
        doMeasurement();
    }

}
