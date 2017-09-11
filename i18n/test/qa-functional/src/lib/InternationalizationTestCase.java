/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package lib;

import java.io.File;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * @author Jana Maleckova
 */
public class InternationalizationTestCase extends JellyTestCase {

    //Variables
    public String DEFAULT_PROJECT_NAME = "ProjectI18n";
    public String DEFAUL_BUNDLE_NAME = "Bundle";
    public String ROOT_PACKAGE_NAME = "Source Packages";
    public String DEF_DIR = "src";
    public String TREE_SEPARATOR = "|";
    public String SEP = System.getProperty("file.separator");    //Variables from bundle
    public String TITLE_INTERNATIONALIZE_DIALOG = Bundle.getString("org.netbeans.modules.i18n.Bundle", "CTL_I18nDialogTitle");
    public String TITLE_SELECT_BUNDLE = Bundle.getString("org.netbeans.modules.i18n.Bundle", "CTL_SelectPropDO_Dialog_Title");
    public String CLOSE_BUTTON = Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "CTL_CloseButton");
    public String REPLACE_BUTTON = Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "CTL_ReplaceButton");
    public ProjectsTabOperator pto;

    /** This constructor only creates operator's object and then does nothing. */
    public InternationalizationTestCase(String testMethodName) {
        super(testMethodName);
    }

    public void openProject(String projectName) {
        this.DEFAULT_PROJECT_NAME = projectName;

        File projectPath = new File(this.getDataDir() + "/projects/" + DEFAULT_PROJECT_NAME);

        //Check if project is not already opened
        pto = new ProjectsTabOperator().invoke();
        int nodeCount = pto.tree().getChildCount(pto.tree().getRoot());

        for (int i = 0; i < nodeCount; i++) {
            String testNode = pto.tree().getChild(pto.tree().getRoot(), i).toString();

            if (testNode.equals(DEFAULT_PROJECT_NAME)) {
                log("Project " + projectName + " has been already opened but should not be");
                return;
            }
        }

        //Open project
        Object prj = ProjectSupport.openProject(projectPath);
        log("Project " + projectName + "was opened");

        //Check if project was opened
        pto.invoke();
        nodeCount = pto.tree().getChildCount(pto.tree().getRoot());
        for (int i = 0; i < nodeCount; i++) {
            String str = pto.tree().getChild(pto.tree().getRoot(), i).toString();
            if (str.equals(projectName)) {
                log("Project " + projectName + " is open. (Ok)");
                return;
            }
        }
        log("Project " + projectName + " is not open, but should be!");
        fail("Project is not open");

    }

    public Node getClassNode(String projectName, String pathToClass) {
        pto = new ProjectsTabOperator().invoke();
        ProjectRootNode prn = pto.getProjectRootNode(projectName);
        Node node = new Node(prn, pathToClass);
        return node;
    }
    //Method which create new Bundle
    public void createNewPropertiesFile(Node node, String fileName) {
        NewFileWizardOperator newWizard = NewFileWizardOperator.invoke(node, "Other", "Properties File");
        NewFileNameLocationStepOperator nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.setObjectName(fileName);
        newWizard.finish();
    }

    public void createNewPropertiesFile(Node node, String packageName, String fileName) {
        try {
            NewFileWizardOperator newWizard = NewFileWizardOperator.invoke(node, "Other", "Properties File");
            NewFileNameLocationStepOperator nfnlso = new NewFileNameLocationStepOperator();
            nfnlso.setObjectName(fileName);
            JTextFieldOperator jtfo = new JTextFieldOperator(nfnlso, 2);
            jtfo.setText(DEF_DIR + SEP + packageName);
            newWizard.finish();
            log("Properties file was correctly created");
        } catch (TimeoutExpiredException ex) {
            log("Creating of new properties file failed !!!");
        }

    }
    //Close file in editor window save/unsave
    public boolean closeFileInEditor(String fileName, boolean save) {

        try {
            EditorOperator eo = new EditorOperator(fileName);
            eo.close(save);
            return true;
        } catch (TimeoutExpiredException ex) {
            return false;
        }
    }
    // method which compare new bundle with golden files
    public void compareBundle(String fileName) {

        File fileToCompare = new File(getDataDir() + File.separator + "projects" + File.separator + DEFAULT_PROJECT_NAME + File.separator + DEF_DIR + File.separator + DEFAULT_PROJECT_NAME.toLowerCase() + File.separator + fileName + ".properties");
        File goldenFile = getGoldenFile(fileName + ".pass");
        //compare
        assertFile(fileToCompare, goldenFile);
    }
    
    // method which compare java files with golden java file
    public void compareJavaFile(String fileName) {

        File fileToCompare = new File(getDataDir() + File.separator + "projects" + File.separator + DEFAULT_PROJECT_NAME + File.separator + DEF_DIR + File.separator + DEFAULT_PROJECT_NAME.toLowerCase() + File.separator + fileName + ".java");
        File goldenFile = getGoldenFile(fileName + ".pass");
        //compare
        assertFile(fileToCompare, goldenFile);
    }
}
