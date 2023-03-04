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
package org.netbeans.qa.form.jda;

import java.util.ArrayList;
import junit.framework.Test;
import org.netbeans.jellytools.DocumentsDialogOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.qa.form.ExtJellyTestCase;

/**
 *
 * @author Jiri Vagner
 * 
 * <b>Adam Senk</b>
 * 20 April 2011 WORKS
 * 
 * NO JDA PROJECT SINCE 7.1
 */
public class SimpleJDAProjectTest extends ExtJellyTestCase {
    
    /** Constructor required by JUnit */
    public SimpleJDAProjectTest(String testName) {
        super(testName);
        
        setTestProjectName("JDABasic" + this.getTimeStamp()); // NOI18N
        setTestPackageName(getTestProjectName().toLowerCase());
        
    }
    
    @Override
    public void setUp(){
        
    }
    
    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(SimpleJDAProjectTest.class).addTest(
                "testCreation",
                "testFilesAndPackages",
                "testBuild"
                ).gui(true).clusters(".*").enableModules(".*"));
    }

    /** Creating JDA Basic project */
    public void testCreation() throws InterruptedException {
        new ActionNoBlock("File|New Project",null).perform(); // NOI18N

        NewProjectWizardOperator op = new NewProjectWizardOperator();
        op.selectProject("Java Desktop Application"); // NOI18N
        op.next();
        op.next();
        
        NbDialogOperator newJDAOp = new NbDialogOperator("New Desktop Application"); // NOI18N
        new JTextFieldOperator(newJDAOp,2).setText(getTestProjectName());
        new JButtonOperator(newJDAOp, "Finish").push(); // NOI18N
        Thread.sleep(15000);
        
        FormDesignerOperator fdo= new FormDesignerOperator(getTestPackageName()+"View.java");
        fdo.editor();
        Thread.sleep(500);
        DocumentsDialogOperator ddo= DocumentsDialogOperator.invoke();
        int[] array={0,1,2};
        ddo.selectDocuments(array);
        Thread.sleep(500);
        ddo.btCloseDocuments().doClick();
        
    }
    
    //** Is project buildable? */
    public void testBuild() {
        new ActionNoBlock("Window|Output|Output",null).perform(); // NOI18N       
        
        new ActionNoBlock("Run|Build Main Project",null).perform(); // NOI18N
        
        OutputTabOperator outputOp = new OutputTabOperator(getTestProjectName() +" (jar)"); // NOI18N
        outputOp.waitText("BUILD SUCCESSFUL"); // NOI18N
    }

    /** Contains packages,form files, properties files for form files,... */
    public void testFilesAndPackages() {
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(getTestProjectName());
        prn.select();
        
        String basePackagePath = "Source Packages|" + getTestPackageName(); // NOI18N

        ArrayList<String> nodePaths = new ArrayList<String>();

        nodePaths.add(""); // NOI18N
        nodePaths.add("|" + getTestProjectName() + "AboutBox.java"); // NOI18N
        nodePaths.add("|" + getTestProjectName() + "App.java"); // NOI18N
        nodePaths.add("|" + getTestProjectName() + "View.java"); // NOI18N

        nodePaths.add(".resources"); // NOI18N
        nodePaths.add(".resources|" + getTestProjectName() + "AboutBox.properties"); // NOI18N
        nodePaths.add(".resources|" + getTestProjectName() + "App.properties"); // NOI18N
        nodePaths.add(".resources|" + getTestProjectName() + "View.properties"); // NOI18N

        nodePaths.add(".resources.busyicons"); // NOI18N
        nodePaths.add(".resources.busyicons|busy-icon0.png"); // NOI18N
        
        for (String nodePath : nodePaths) {
            new Node(prn, basePackagePath + nodePath).select();            
        }
    }
}
