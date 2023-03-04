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
package org.netbeans.test.j2ee.wizard;

import java.io.File;
import javax.swing.JComboBox;
import org.netbeans.api.project.Project;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewJavaProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 *
 * @author jungi, Jiri Skrivanek
 */
public class WizardUtils {
    
    /** Creates a new instance of WizardUtils */
    private WizardUtils() {
    }
    
    public static NewProjectWizardOperator createNewProject(String category,
            String project) {
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.treeCategories().setComparator(new Operator.DefaultStringComparator(true, true));
        npwo.lstProjects().setComparator(new Operator.DefaultStringComparator(true, true));
        npwo.selectCategory(category);
        npwo.selectProject(project);
        npwo.next();
        return npwo;
    }
    
    public static NewJavaProjectNameLocationStepOperator setProjectNameLocation(
            String name, String location) {
        NewJavaProjectNameLocationStepOperator op = new NewJavaProjectNameLocationStepOperator();
        op.txtProjectName().setText(name);
        op.txtProjectLocation().setText(location);
        return op;
    }
    
    public static NewFileWizardOperator createNewFile(Project p,
            String category, String filetype) {
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        new EventTool().waitNoEvent(500);
        nfwo.treeCategories().setComparator(new Operator.DefaultStringComparator(true, true));
        nfwo.lstFileTypes().setComparator(new Operator.DefaultStringComparator(true, true));
        nfwo.cboProject().selectItem(p.toString());
        nfwo.selectCategory(category);
        nfwo.selectFileType(filetype);
        nfwo.next();
        return nfwo;
    }
    
    public static NewJavaFileNameLocationStepOperator setFileNameLocation(String name,
            String pkg, String srcRoot) {
        NewJavaFileNameLocationStepOperator op = new NewJavaFileNameLocationStepOperator();
        op.setObjectName(name);
        if (srcRoot != null) {
            op.cboLocation().selectItem(srcRoot);
        }
        op.setPackage(pkg);
        return op;
    }

    /** Finds Java EE Version combo box in wizard and sets it to requested version.
     * @param op wizard operator
     * @param version sub string of requested Java EE version
     * @return same wizard operator instance
     */
    public static NewJavaProjectNameLocationStepOperator setJ2eeSpecVersion(
            NewJavaProjectNameLocationStepOperator op, String version) {
        op.next();
        // "Java EE Version"
        String javaEEVersionLabel = Bundle.getStringTrimmed("org.netbeans.modules.javaee.project.api.ant.ui.wizard.Bundle", "LBL_NWP1_J2EESpecLevel_Label");
        JLabelOperator lblJavaEEVersion = new JLabelOperator(op, javaEEVersionLabel);
        JComboBoxOperator cboVersion = new JComboBoxOperator((JComboBox)lblJavaEEVersion.getLabelFor());
        cboVersion.selectItem(version);
        return op;
    }

    /**
     * Creates new EJB project.
     * @param parentDir parent dir of project
     * @param projectName project name
     * @param version sub string of requested Java EE version
     * @throws Exception 
     */
    public static void createEJBProject(String parentDir, String projectName, String version) throws Exception {
        File targetDir = new File(parentDir, projectName);
        deleteAll(targetDir);
        NewProjectWizardOperator wiz = WizardUtils.createNewProject("Java EE", "EJB Module");
        NewJavaProjectNameLocationStepOperator op = WizardUtils.setProjectNameLocation(projectName, parentDir);
        WizardUtils.setJ2eeSpecVersion(op, version);
        wiz.finish();
    }
    
    /**
     * Creates new Web project.
     * @param parentDir parent dir of project
     * @param projectName project name
     * @param version sub string of requested Java EE version
     * @throws Exception 
     */
    public static void createWebProject(String parentDir, String projectName, String version) throws Exception {
        File targetDir = new File(parentDir, projectName);
        deleteAll(targetDir);
        NewProjectWizardOperator wiz = WizardUtils.createNewProject("Java Web", "Web Application");
        NewJavaProjectNameLocationStepOperator op = WizardUtils.setProjectNameLocation(projectName, parentDir);
        WizardUtils.setJ2eeSpecVersion(op, version);
        wiz.finish();
    }
    
    /** Deletes specified file/directory and its sub directories.
     * @param file file to be deleted
     * @throws IOException if cannot delete file
     */
    static void deleteAll(File file) {
        File files[] = file.listFiles();
        if (files != null && files.length != 0) {
            for (File f : files) {
                deleteAll(f);
            }
        }
        file.delete();
    }
}
