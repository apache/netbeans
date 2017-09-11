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

package org.netbeans.test.junit.utils;

import com.sun.org.apache.bcel.internal.generic.IFEQ;
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
     * @param the file to be deleted
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
     * @param Filename the file to be opened
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
