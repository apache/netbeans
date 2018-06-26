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
package org.netbeans.performance.languages.actions;

import junit.framework.Test;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.languages.Projects;
import org.netbeans.performance.languages.ScriptingUtilities;
import org.netbeans.performance.languages.setup.ScriptingSetup;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.EditorOperator;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;

/**
 * Test create Web Pack projects
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class CreateScriptingPackFilesTest extends PerformanceTestCase {

    private String doccategory, doctype, docname, suffix, projectfolder, buildedname;
    private NewJavaFileNameLocationStepOperator location;
    private String project_name = "";
    private Node projectRoot;

    /**
     * Creates a new instance of CreateWebPackFiles
     *
     * @param testName the name of the test
     */
    public CreateScriptingPackFilesTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }

    /**
     * Creates a new instance of CreateWebPackFiles
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateScriptingPackFilesTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(ScriptingSetup.class).addTest(CreateScriptingPackFilesTest.class).suite();
    }

    public void testCreatePHPPage() {
        docname = "PHPPage"; //NOI18N
        doccategory = "PHP"; //NOI18N        
        doctype = "PHP Web Page"; //NOI18N
        suffix = ".php";
        projectfolder = ScriptingUtilities.SOURCE_PACKAGES;
        project_name = Projects.PHP_PROJECT;
        doMeasurement();
    }

    public void testCreatePHPFile() {
        docname = "PHPFile"; //NOI18N
        doccategory = "PHP"; //NOI18N        
        doctype = "PHP File"; //NOI18N
        suffix = ".php";
        projectfolder = ScriptingUtilities.SOURCE_PACKAGES;
        project_name = Projects.PHP_PROJECT;
        doMeasurement();
    }

    public ComponentOperator open() {
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
        location.finish();
        return new EditorOperator(buildedname);
    }

    @Override
    public void initialize() {
        closeAllModal();
    }

    public void prepare() {
        try {
            projectRoot = ScriptingUtilities.invokePTO().getProjectRootNode(project_name);
            projectRoot.select();
        } catch (org.netbeans.jemmy.TimeoutExpiredException ex) {
            fail("Cannot find and select project root node");
        }

        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.QUEUE_MODEL_MASK);
        NewFileWizardOperator wizard = NewFileWizardOperator.invoke();
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);

        wizard.selectCategory(doccategory);
        wizard.selectFileType(doctype);
        wizard.next();

        location = new NewJavaFileNameLocationStepOperator();
        buildedname = docname + "_" + System.currentTimeMillis();
        location.txtObjectName().setText(buildedname);
    }

    @Override
    public void close() {
        EditorOperator.closeDiscardAll();
    }
}
