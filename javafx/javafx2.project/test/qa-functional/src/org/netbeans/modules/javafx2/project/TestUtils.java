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
package org.netbeans.modules.javafx2.project;

import java.io.File;
import java.util.Calendar;

import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author stezeb
 */
public class TestUtils {

    /* New Project Wizard */
    public static final String JAVAFX_PROJECT_CATEGORY = Bundle.getStringTrimmed("org.netbeans.modules.javafx2.project.Bundle",
            "Templates/Project/JavaFX");
    public static final String JAVAFX_PROJECT_TYPE_PLAIN = Bundle.getStringTrimmed("org.netbeans.modules.javafx2.project.Bundle",
            "Templates/Project/JavaFX/javafxApp.xml");
    public static final String JAVAFX_PROJECT_TYPE_PRELOADER = Bundle.getStringTrimmed("org.netbeans.modules.javafx2.project.Bundle",
            "Templates/Project/JavaFX/preloaderApp.xml");
    public static final String JAVAFX_PROJECT_TYPE_FXMLAPP = Bundle.getStringTrimmed("org.netbeans.modules.javafx2.project.fxml.Bundle",
            "Templates/Project/JavaFX/fxml.xml");
    public static final String JAVAFX_PROJECT_TYPE_SWINGAPP = Bundle.getStringTrimmed("org.netbeans.modules.javafx2.project.Bundle",
            "Templates/Project/JavaFX/javafxSwingApp.xml");
    
    /* New File Wizard */
    public static final String JAVAFX_FILE_CATEGORY = Bundle.getStringTrimmed("org.netbeans.modules.javafx2.project.Bundle",
            "Templates/javafx");
    public static final String JAVAFX_FILE_TYPE_MAIN = Bundle.getStringTrimmed("org.netbeans.modules.javafx2.project.Bundle",
            "Templates/javafx/FXMain.java");
    public static final String JAVAFX_FILE_TYPE_PRELOADER = Bundle.getStringTrimmed("org.netbeans.modules.javafx2.project.Bundle",
            "Templates/javafx/FXPreloader.java");
    public static final String JAVAFX_FILE_TYPE_FXML = Bundle.getStringTrimmed("org.netbeans.modules.javafx2.project.fxml.Bundle",
            "Templates/javafx/FXML.java");
    public static final String JAVAFX_FILE_TYPE_SWINGMAIN = Bundle.getStringTrimmed("org.netbeans.modules.javafx2.project.Bundle",
            "Templates/javafx/FXSwingMain.java");

    /**
     * Use New Project wizard to generate FX project of given type and name.
     * 
     * @param projectType
     * @param projectName
     * @param workDirPath 
     */
    public static void createJavaFX2Project(String projectType, String projectName, String workDirPath) {
        NewProjectWizardOperator npwop = NewProjectWizardOperator.invoke();
        npwop.selectCategory(JAVAFX_PROJECT_CATEGORY);
        npwop.selectProject(projectType);
        npwop.next();

        new EventTool().waitNoEvent(2000);

        JTextFieldOperator projectNameField = new JTextFieldOperator(npwop, 0);
        projectNameField.clearText();
        projectNameField.typeText(projectName);

        JTextFieldOperator projectLocation = new JTextFieldOperator(npwop, 1);
        projectLocation.clearText();
        projectLocation.typeText(workDirPath + File.separator
                + Calendar.getInstance().getTimeInMillis());

        new EventTool().waitNoEvent(2000);

        npwop.finish();

        new EventTool().waitNoEvent(5000); //wait for complete project creation, nodes it Projects View must be populated
    }
    
    /**
     * Close FX project of given name.
     * 
     * @param projectName 
     */
    public static void closeJavaFX2Project(String projectName) {
        ProjectsTabOperator pto = new ProjectsTabOperator();

        pto.getProjectRootNode(projectName).select();
        pto.getProjectRootNode(projectName).performPopupAction("Close");

        new EventTool().waitNoEvent(2000);
    }

    public static void createJavaFX2File(String fileType, String projectName, String fileName) {
        NewFileWizardOperator nfwo = newJavaFX2FileTemplateSelection(fileType, projectName);

        JTextFieldOperator fileNameField = new JTextFieldOperator(nfwo, 0);
        fileNameField.clearText();
        fileNameField.typeText(fileName);

        nfwo.finish();

        new EventTool().waitNoEvent(2000);        
    }

    public static void createJavaFX2FXMLFile(String fileType, String projectName, String fileName) {
        NewFileWizardOperator nfwo = newJavaFX2FileTemplateSelection(fileType, projectName);
        //fxml name
        JTextFieldOperator fxmlName = new JTextFieldOperator(nfwo, 0);
        fxmlName.clearText();
        fxmlName.typeText(fileName);
        nfwo.next();
        //fxml controller        
        JCheckBoxOperator useCtrlr = new JCheckBoxOperator(nfwo, 0);
        useCtrlr.clickMouse();
        nfwo.next();
        //fxml css
        JCheckBoxOperator useCSS = new JCheckBoxOperator(nfwo, 0);
        useCSS.clickMouse();
        nfwo.finish();

        new EventTool().waitNoEvent(2000);
    }
    
    private static NewFileWizardOperator newJavaFX2FileTemplateSelection(
            String fileType, String projectName) {
        ProjectsTabOperator pto = new ProjectsTabOperator();

        pto.getProjectRootNode(projectName).select();
        pto.getProjectRootNode(projectName).expand();

        Node srcNode = new Node(pto.getProjectRootNode(projectName), "Source Packages");
        srcNode.expand();

        Node mainPackageNode = new Node(srcNode, projectName.toLowerCase());
        mainPackageNode.expand();

        mainPackageNode.performPopupAction("New|Other...");

        new EventTool().waitNoEvent(2000);

        NewFileWizardOperator nfwo = new NewFileWizardOperator();
        nfwo.selectCategory(JAVAFX_FILE_CATEGORY);
        nfwo.selectFileType(fileType);
        nfwo.next();

        new EventTool().waitNoEvent(2000);
        
        return nfwo;
    }
}
