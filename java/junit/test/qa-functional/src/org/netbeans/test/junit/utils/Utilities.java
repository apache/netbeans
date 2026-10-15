/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.test.junit.utils;

import java.io.File;
import java.io.IOException;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileUtil;
import org.openide.actions.SaveAllAction;

/**
 *
 * @author Max Sauer
 */
public class Utilities {
    /** name of sample project */
    public static final String TEST_PROJECT_NAME = "JunitTestProject";
    
    /** name of sample class */
    public static final String TEST_CLASS_NAME = "TestClass";
    
    /** label when deleting object */
    public static final String CONFIRM_OBJECT_DELETION = "Delete";
//            Bundle.getString("org.openide.explorer.Bundle",
//            "MSG_ConfirmDeleteObjectTitle");
    
    /** default path to bundle file */
    public static final String JUNIT_BUNDLE = "org.netbeans.modules.junit.Bundle";
    
    /** 'Test Packages' string from j2se project bundle */
    public static final String TEST_PACKAGES_PATH =
            Bundle.getString("org.netbeans.modules.java.j2seproject.Bundle",
            "NAME_test.src.dir");
    
    /** 'Run File' action label from j2se project bundle */
    public static final String RUN_FILE =
            Bundle.getString("org.netbeans.modules.java.api.common.project.Bundle",
            "ACTION_run.single");
    
    /** Test project label (j2se project context menu) */
    public static final String TEST_PROJECT =
            Bundle.getString("org.netbeans.modules.project.ui.actions.Bundle",
            "LBL_TestProjectAction_Name");
    
    /** 'Source Packages' string from j2se project bundle */
    public static final String SRC_PACKAGES_PATH =
            Bundle.getString("org.netbeans.modules.java.j2seproject.Bundle",
            "NAME_src.dir");
    
    // default timeout for actions in miliseconds
    public static final int ACTION_TIMEOUT = 1000;
    

    /**
     * Saves all opened files
     */
    public static void saveAll() {
        ((SaveAllAction) SaveAllAction.findObject(SaveAllAction.class, true)).performAction();
    }
    
    /**
     * Deletes a file
     * @param file the file to be deleted
     */
    public static void delete(File file) {
        try {
            DataObject.find(FileUtil.toFileObject(file)).delete();
        } catch (IOException e) {
        }
    }
    
    /**
     * Deletes a node (file, package)
     * using pop-up menu
     */
    public static void deleteNode(String path) {
        try {
            Node pn = new ProjectsTabOperator().getProjectRootNode(
                    Utilities.TEST_PROJECT_NAME);
            if(pn != null && pn.isPresent()) {
                pn.select();
                Node n = new Node(pn, path);
                n.select();
                JPopupMenuOperator jpmo = n.callPopup();
                jpmo.pushMenu("Delete");
                new NbDialogOperator(CONFIRM_OBJECT_DELETION).btOK().push(); //confirm
                takeANap(500);
            }
        } catch (TimeoutExpiredException e) {
            System.out.println("Node hasn't been found!!!");
        }
    }
    
    /**
     * Recursively deletes a directory
     */
    public static void deleteDirectory(File path) {
        if(path.exists()) {
            File[] f = path.listFiles();
            for(int i = 0; i < f.length ; i++) {
                if (f[i].isDirectory())
                    deleteDirectory(f[i]);
                else
                    f[i].delete();
            }
        }
        path.delete();
    }
    
    /**
     * Opens a file from TEST_PROJECT_NAME
     * @param path the file to be opened
     */
    public static Node openFile(String path) {
        Node pn = new ProjectsTabOperator().getProjectRootNode(
                Utilities.TEST_PROJECT_NAME);
        pn.select();
        Node n = new Node(pn, path);
        n.select();
        new OpenAction().perform();
        new EventTool().waitNoEvent(ACTION_TIMEOUT);
        return n;
    }
    
    /**
     * Test whole project (presses 'Test Project from explorer's context menu
     */
    public static void testWholeProject() {
        Node n = new ProjectsTabOperator().getProjectRootNode(
                Utilities.TEST_PROJECT_NAME);
        n.callPopup().pushMenu(TEST_PROJECT);
    }
    
    /**
     * Pushes Tools|Create Junit tests over a node
     * @param n the node where the action will be invoked
     */
    public static void pushCreateTestsPopup(Node n) {
        JPopupMenuOperator jpmo = n.callPopup();
        String[] path = {"Tools", Bundle.getString(Utilities.JUNIT_BUNDLE,
                "LBL_Action_CreateTest")};
                jpmo.pushMenu(path);
    }
    
    /**
     * Sets all checkboxes inside Junit create tests dialog to checked
     */
    public static void checkAllCheckboxes(NbDialogOperator ndo, boolean checkGenerateIntegrationTest) {
        for(int i = (checkGenerateIntegrationTest ? 0 : 1); i < 11; i++) {
            new JCheckBoxOperator(ndo, i).setSelected(true);
        }
    }
    
    /**
     * Sleeps for waitTimeout miliseconds to avoid incorrect test failures.
     */
    public static void takeANap(long waitTimeout) {
        new org.netbeans.jemmy.EventTool().waitNoEvent(waitTimeout);
    }
    
    /** 
     * Returns absolute path to NetBeans projects folder.
     * @return Absolute path to NetBeans projects folder
     */
    public static String pathToProjects() {
        String pathToNbProjects = "";
        try {
            String userHome =  System.getProperty("user.home") != null ? System.getProperty("user.home") : "";
            String system = System.getProperty("os.name") != null ? System.getProperty("os.name") : "";
            if (system.indexOf("Windows") == -1) {
                //unix
                pathToNbProjects = userHome + File.separator + "NetBeansProjects";
            } else {
                //windows
                pathToNbProjects = userHome + File.separator + "My Documents" + File.separator + "NetBeansProjects";
            } 
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return pathToNbProjects;
    }
}
