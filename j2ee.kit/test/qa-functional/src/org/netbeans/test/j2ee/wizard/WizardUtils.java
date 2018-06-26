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
