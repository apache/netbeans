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
package org.netbeans.test.permanentUI;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import javax.swing.tree.TreeModel;
import junit.framework.Test;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.diff.LineDiff;
import org.netbeans.test.permanentUI.utils.ProjectContext;
import org.netbeans.test.permanentUI.utils.Utilities;

/**
 *
 * @author Lukas Hasik, Petr Chytil, Marian.Mirilovic@oracle.com
 */
public class NewProjectTest extends PermUITestCase {

    private NewProjectWizardOperator npwo;

    /**
     * Need to be defined because of JUnit
     */
    public NewProjectTest(String name) {
        super(name);
    }

    public static Test suite() {
        return NewProjectTest.emptyConfiguration().
                addTest(NewProjectTest.class, "testNewProjectWizardLayout").
                addTest(NewProjectTest.class, "testNewProjectCategories"). // move it here to avoid situation when 'wait' node is shown
                addTest(NewProjectTest.class, "testNewProjectsJava").
                addTest(NewProjectTest.class, "testNewProjectsJavaFX").
                addTest(NewProjectTest.class, "testNewProjectsJavaWeb").
                addTest(NewProjectTest.class, "testNewProjectsJavaEE").
                addTest(NewProjectTest.class, "testNewProjectsHTML5").
                addTest(NewProjectTest.class, "testNewProjectsJavaCard").
                addTest(NewProjectTest.class, "testNewProjectsJavaME").
                addTest(NewProjectTest.class, "testNewProjectsMaven").
                addTest(NewProjectTest.class, "testNewProjectsPHP").
                addTest(NewProjectTest.class, "testNewProjectsGroovy").
                addTest(NewProjectTest.class, "testNewProjectsCpp").
                addTest(NewProjectTest.class, "testNewProjectsNetBeansModules").
                clusters(".*").enableModules(".*").
                suite();
    }

    @Override
    public void initialize() {
        if (npwo == null || !npwo.isVisible()) {
            npwo = NewProjectWizardOperator.invoke();
        }
    }

    @Override
    public ProjectContext getContext() {
        return ProjectContext.NONE;
    }

    @Override
    protected void tearDown() throws Exception {
    }

    /**
     * Verify layout
     */
    public void testNewProjectWizardLayout() {
        npwo = NewProjectWizardOperator.invoke();
        npwo.verify();
    }

    /**
     * tests the that the File > New Project categories match
     * http://wiki.netbeans.org/NewProjectWizard
     */
    public void testNewProjectCategories() {
        npwo = NewProjectWizardOperator.invoke();
        File goldenfile = getGoldenFile("newproject", "newprojects-Categories");
        ArrayList<String> permanentCategories = Utilities.parseFileByLinesLeaveSpaces(goldenfile);
        JTreeOperator categoriesOperator = npwo.treeCategories();
        ArrayList<String> actualCategories = getChildren(categoriesOperator.getModel(), categoriesOperator.getRoot(), "");

        compareAndAssert(permanentCategories, actualCategories);
    }

    public void testNewProjectsJava() throws InterruptedException {
        oneCategoryTest("Java");
    }

    public void testNewProjectsJavaFX() throws InterruptedException {
        oneCategoryTest("JavaFX");
    }

    public void testNewProjectsJavaWeb() throws InterruptedException {
        oneCategoryTest("Java Web", "Java_Web");
    }

    public void testNewProjectsJavaEE() throws InterruptedException {
        oneCategoryTest("Java EE", "Java_EE");
    }

    public void testNewProjectsHTML5() throws InterruptedException {
        oneCategoryTest("HTML5", "HTML5");
    }

    public void testNewProjectsJavaCard() throws InterruptedException {
        oneCategoryTest("Java Card", "Java_Card");
    }

    public void testNewProjectsJavaME() throws InterruptedException {
        oneCategoryTest("Java ME", "Java_ME");
    }

    public void testNewProjectsCpp() throws InterruptedException {
        oneCategoryTest("C/C++", "Cpp");
    }

    public void testNewProjectsNetBeansModules() throws InterruptedException {
        oneCategoryTest("NetBeans Modules", "NetBeans_Modules");

    }

    public void testNewProjectsGroovy() throws InterruptedException {
        oneCategoryTest("Groovy");

    }

    public void testNewProjectsPHP() throws InterruptedException {
        oneCategoryTest("PHP");

    }

    public void testNewProjectsMaven() throws InterruptedException {
        oneCategoryTest("Maven");

    }

    /**
     * For categories with simple names, which can be used as filename of the
     * golden file.
     *
     * @param categoryName - name of the category = name of the godlen file
     * @param newProjectOperator
     * @return
     */
    private void oneCategoryTest(String categoryName) throws InterruptedException {
        oneCategoryTest(categoryName, categoryName);
    }

    /**
     * This method should be used when category is too complicated and couldn't
     * be used as golden file's filename.
     *
     * @param categoryName
     * @param goldenFileName
     * @param newProjectOperator
     * @return
     */
    private void oneCategoryTest(String categoryName, String goldenFileName) throws InterruptedException {
        npwo = NewProjectWizardOperator.invoke();
        File goldenfile = getGoldenFile("newproject", goldenFileName);
        ArrayList<String> permanentProjects = Utilities.parseFileByLines(goldenfile);
        wait(2000);
        npwo.selectCategory(categoryName);
        JListOperator jlo = npwo.lstProjects();
        ArrayList<String> actualProjects = new ArrayList<String>();
        actualProjects.add(categoryName); /// add category as the first item
        actualProjects.addAll(getProjectsList(jlo));

        compareAndAssert(permanentProjects, actualProjects);
    }

    private void compareAndAssert(ArrayList<String> permanent, ArrayList<String> ide) {
        PrintStream ideFileStream = null;
        PrintStream goldenFileStream = null;
        final String pathToIdeLogFile = getWorkDirPath() + File.separator + getName() + "_ide.txt";
        final String pathToGoldenLogFile = getWorkDirPath() + File.separator + getName() + "_golden.txt";
        final String pathToDiffLogFile = getWorkDirPath() + File.separator + getName() + ".diff";
        try {
            ideFileStream = new PrintStream(pathToIdeLogFile);
            goldenFileStream = new PrintStream(pathToGoldenLogFile);

            for (String actual : permanent) {
                System.out.println("- " + actual);
                goldenFileStream.println(actual);
            }

            for (String actual : ide) {
                System.out.println("+ " + actual);
                ideFileStream.println(actual);
            }

            // create a diff of IDE state and permUI state
            new LineDiff().diff(pathToIdeLogFile, pathToGoldenLogFile, pathToDiffLogFile);
            String message = Utilities.readFileToString(pathToDiffLogFile);
            assertNull(message, message);

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        } finally {
            ideFileStream.close();
            goldenFileStream.close();
        }
    }

    private ArrayList<String> getProjectsList(JListOperator projectsListOperator) {
        ArrayList<String> projectsList = new ArrayList<String>();
        int catSize = projectsListOperator.getModel().getSize();
        for (int j = 0; j <= catSize - 1; j++) { // last (null) nodes
            projectsList.add(projectsListOperator.getModel().getElementAt(j).toString());
        }
        return projectsList;

    }

    private ArrayList<String> getChildren(TreeModel tree, Object root, String spaces) {
        int categoriesCount = tree.getChildCount(root);
        ArrayList<String> returnList = new ArrayList<String>();
        for (int i = 0; i <= categoriesCount - 1; i++) {
            Object actualChild = tree.getChild(root, i);
            returnList.add(spaces + actualChild.toString());

            if (!tree.isLeaf(actualChild)) {
                spaces = "+-" + spaces;
                returnList.addAll(getChildren(tree, actualChild, spaces));
                spaces = spaces.substring(2);
            }
        }
        return returnList;
    }

}
