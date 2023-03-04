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
package org.netbeans.test.git.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.Logger;
import javax.swing.JCheckBoxMenuItem;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.test.git.operators.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.test.git.operators.NewJavaProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JCheckBoxMenuItemOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.modules.versioning.util.IndexingBridge;
import org.netbeans.test.git.operators.CommitOperator;
//import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * @author kanakmar
 */
public final class TestKit {

    public static final String MODIFIED_COLOR = "#0000FF";
    public static final String NEW_COLOR = "#008000";
    public static final String CONFLICT_COLOR = "#FF0000";
    public static final String IGNORED_COLOR = "#999999";
    public static final String MODIFIED_STATUS = "[-/M]";
    public static final String NEW_STATUS = "[-/A]";
    public static final String CONFLICT_STATUS = "[Conflict ]";
    public static final String IGNORED_STATUS = "[I]";
    public static final String UPTODATE_STATUS = "";
    private static final String TMP_PATH = "/tmp";
    private static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "JavaApp";
    public static final String PROJECT_TYPE = "Java Application";
    public static final String PROJECT_CATEGORY = "Java with Ant";
    public static final String CLONE_SUF_0 = "_clone0";
    public static final String CLONE_SUF_1 = "_clone1";
    public static final String LOGGER_NAME = "org.netbeans.modules.git.t9y";
    public static int TIME_OUT = 15;

    public static File prepareProject(String prj_category, String prj_type, String prj_name) throws Exception {
        //create temporary folder for test
        String folder = "work" + File.separator + "w" + System.currentTimeMillis();
        File file = new File("/tmp", folder); // NOI18N
        file.mkdirs();
        RepositoryMaintenance.deleteFolder(file);
        file.mkdirs();
        //PseudoVersioned project
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.selectCategory(prj_category);
        npwo.selectProject(prj_type);
        npwo.next();
        NewJavaProjectNameLocationStepOperator npnlso = new NewJavaProjectNameLocationStepOperator();
        new JTextFieldOperator(npnlso, 1).setText(file.getAbsolutePath());
        new JTextFieldOperator(npnlso, 0).setText(prj_name);
        new NewProjectWizardOperator().finish();

        waitForScanFinishedSimple();//ProjectSupport.waitScanFinished();//AndQueueEmpty(); // test fails if there is waitForScanAndQueueEmpty()...

        return file;
    }

    public static File prepareGitProject(String prj_category, String prj_type, String prj_name) throws Exception {
        NbDialogOperator ndo;
        //create temporary folder for test
        String folder = "work" + File.separator + "w" + System.currentTimeMillis();
        File file = new File("/tmp", folder); // NOI18N
        file.mkdirs();
        RepositoryMaintenance.deleteFolder(file);
        file.mkdirs();
        File file1 = new File("/tmp", folder + File.separator + "clone");
        file1.mkdirs();
        //PseudoVersioned project
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.selectCategory(prj_category);
        npwo.selectProject(prj_type);
        npwo.next();
        NewJavaProjectNameLocationStepOperator npnlso = new NewJavaProjectNameLocationStepOperator();
        new JTextFieldOperator(npnlso, 1).setText(file.getAbsolutePath());
        new JTextFieldOperator(npnlso, 0).setText(prj_name);
        new JTextFieldOperator(npnlso, 4).setText("javaapp.Main");
        new NewProjectWizardOperator().finish();

        waitForScanFinishedSimple();//ProjectSupport.waitScanFinished();//AndQueueEmpty(); // test fails if there is waitForScanAndQueueEmpty()...

        new EventTool().waitNoEvent(2000);
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(prj_name);
        rootNode.performPopupActionNoBlock("Versioning|Initialize Git Repository");

        new EventTool().waitNoEvent(2000);
        ndo = new NbDialogOperator("Initialize a Git Repository");
        ndo.ok();

        waitForScanFinishedSimple();

        new EventTool().waitNoEvent(2000);
        CommitOperator cmo = CommitOperator.invoke(rootNode);
        cmo.setCommitMessage("init");
        cmo.commit();

        waitForScanFinishedSimple();
        
        return file;
    }

    public static String getColor(String nodeHtmlDisplayName) {

        if (nodeHtmlDisplayName == null || nodeHtmlDisplayName.length() < 1) {
            return "";
        }
        int hashPos = nodeHtmlDisplayName.indexOf('#');
        if (hashPos == -1) {
            return null;
        }
        nodeHtmlDisplayName = nodeHtmlDisplayName.substring(hashPos);
        hashPos = nodeHtmlDisplayName.indexOf('"');
        nodeHtmlDisplayName = nodeHtmlDisplayName.substring(0, hashPos);
        return nodeHtmlDisplayName;
    }

    public static String getColor(String fileName, String nodeHtmlDisplayName) {

        if (nodeHtmlDisplayName == null || nodeHtmlDisplayName.length() < 1) {
            return "";
        }
        int hashPos = nodeHtmlDisplayName.indexOf('#');
        if (hashPos == -1) {
            return null;
        }
        if (!nodeHtmlDisplayName.contains(">" + fileName)) {
            return null;
        }
        nodeHtmlDisplayName = nodeHtmlDisplayName.substring(hashPos);
        hashPos = nodeHtmlDisplayName.indexOf('"');
        nodeHtmlDisplayName = nodeHtmlDisplayName.substring(0, hashPos);
        return nodeHtmlDisplayName;
    }

    public static String getStatus(String nodeHtmlDisplayName) {
        if (nodeHtmlDisplayName == null || nodeHtmlDisplayName.length() < 1) {
            return "";
        }
        String status;
        int pos1 = nodeHtmlDisplayName.indexOf('[');
        int pos2 = nodeHtmlDisplayName.indexOf(']');
        if ((pos1 != -1) && (pos2 != -1)) {
            status = nodeHtmlDisplayName.substring(pos1, pos2 + 1);
        } else {
            status = "";
        }
        return status;
    }

    public static void removeAllData(String projectName) {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        rootNode.performPopupActionNoBlock("Delete Project");
        NbDialogOperator ndo = new NbDialogOperator("Delete");
        JCheckBoxOperator cb = new JCheckBoxOperator(ndo, "Also");
        cb.setSelected(true);
        ndo.yes();
        ndo.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        ndo.waitClosed();
        //TestKit.deleteRecursively(file);
    }

    public static void closeProject(String projectName) {
        try {
            Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
            Thread.sleep(1000);
            rootNode.performPopupAction("Close");
        } catch (Exception e) {
        } finally {
            /**
             * Dekanek: this try block was needed on my machine to succesfully
             * run test. It seems as if "new ProjectsTabOperator().tree()"
             * cannot be invoked, probably, because all projects are closed..
             *
             * (Java: 1.6.0_10; Java HotSpot(TM) Client VM 11.0-b15 System:
             * Linux version 2.6.24-23-generic running on i386; UTF-8; en_US
             * (nb))
             */
            try {
                new ProjectsTabOperator().tree().clearSelection();
            } catch (Exception e) {
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static String getProjectAbsolutePath(String projectName) {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        rootNode.performPopupActionNoBlock("Properties");
        NbDialogOperator ndo = new NbDialogOperator("Project Properties");
        String result = new JTextFieldOperator(ndo).getText();
        ndo.cancel();
        return result;
    }

    public static void waitForScanFinishedAndQueueEmpty() {
        waitForScanFinishedSimple();//ProjectSupport.waitScanFinished();
        new QueueTool().waitEmpty(1000);
        waitForScanFinishedSimple();//ProjectSupport.waitScanFinished();
    }

    public static void finalRemove() throws Exception {
        closeProject("JavaApp");
        RepositoryMaintenance.deleteFolder(new File("/tmp/work"));
    }

    public static int compareThem(Object[] expected, Object[] actual, boolean sorted) {
        int result = 0;
        if (expected == null || actual == null) {
            return -1;
        }
        if (sorted) {
            if (expected.length != actual.length) {
                return -1;
            }
            for (int i = 0; i < expected.length; i++) {
                if (((String) expected[i]).equals((String) actual[i])) {
                    result++;
                } else {
                    return -1;
                }
            }
        } else {
            if (expected.length > actual.length) {
                return -1;
            }
            Arrays.sort(expected);
            Arrays.sort(actual);
            boolean found = false;
            for (int i = 0; i < expected.length; i++) {
                if (((String) expected[i]).equals((String) actual[i])) {
                    result++;
                } else {
                    return -1;
                }
            }
            return result;
        }
        return result;
    }

    public static void createNewElements(String projectName, String packageName, String name) {
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Java");
        nfwo.selectFileType("Java Package");
        nfwo.next();
        NewJavaFileNameLocationStepOperator nfnlso = new NewJavaFileNameLocationStepOperator();
        nfnlso.txtObjectName().clearText();
        nfnlso.txtObjectName().typeText(packageName);
        nfnlso.finish();

        nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Java");
        nfwo.selectFileType("Java Class");
        nfwo.next();
        nfnlso = new NewJavaFileNameLocationStepOperator();
        nfnlso.txtObjectName().clearText();
        nfnlso.txtObjectName().typeText(name);
        nfnlso.selectPackage(packageName);
        nfnlso.finish();
    }

    public static void createNewPackage(String projectName, String packageName) {
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Java");
        nfwo.selectFileType("Java Package");
        nfwo.next();
        NewJavaFileNameLocationStepOperator nfnlso = new NewJavaFileNameLocationStepOperator();
        nfnlso.txtObjectName().clearText();
        nfnlso.txtObjectName().typeText(packageName);
        nfnlso.finish();
    }

    public static void createNewElement(String projectName, String packageName, String name) {
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(projectName);
        nfwo.selectCategory("Java");
        nfwo.selectFileType("Java Class");
        nfwo.next();
        NewJavaFileNameLocationStepOperator nfnlso = new NewJavaFileNameLocationStepOperator();
        nfnlso.txtObjectName().clearText();
        nfnlso.txtObjectName().typeText(name);
        nfnlso.selectPackage(packageName);
        nfnlso.finish();
    }

    public static void copyTo(String source, String destination) {
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(source));
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destination));
            boolean available = true;
            byte[] buffer = new byte[1024];
            int size;
            try {
                while (available) {
                    size = bis.read(buffer);
                    if (size != -1) {
                        bos.write(buffer, 0, size);
                    } else {
                        available = false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                bos.flush();
                bos.close();
                bis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printLogStream(PrintStream stream, String message) {
        if (stream != null) {
            stream.println(message);
        }
    }

    public static void showStatusLabels() {
        JMenuBarOperator mbo = new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar());
        JMenuItemOperator mo = mbo.showMenuItem("View|Show Versioning Labels");
        JCheckBoxMenuItemOperator cbmio = new JCheckBoxMenuItemOperator((JCheckBoxMenuItem) mo.getSource());
        if (!cbmio.getState()) {
            cbmio.doClick();
        }
    }

    public static void hideStatusLabels() {
        JMenuBarOperator mbo = new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar());
        JMenuItemOperator mo = mbo.showMenuItem("View|Show Versioning Labels");
        JCheckBoxMenuItemOperator cbmio = new JCheckBoxMenuItemOperator((JCheckBoxMenuItem) mo.getSource());
        if (cbmio.getState()) {
            cbmio.doClick();
        }
    }

    public static void openProject(File location, String project) throws Exception {
        if (getOsName().indexOf("Mac") > -1) {
            new NewProjectWizardOperator().invoke().close();
        }
        new ActionNoBlock("File|Open Project", null).perform();
        NbDialogOperator nb = new NbDialogOperator("Open Project");
        JFileChooserOperator fco = new JFileChooserOperator(nb);
        fco.setCurrentDirectory(new File(location, project));
        fco.approve();
        waitForScanFinishedSimple();//ProjectSupport.waitScanFinished();
    }

    public static String getOsName() {
        String osName = "uknown";
        try {
            osName = System.getProperty("os.name");
        } catch (Throwable e) {
        }
        return osName;
    }

    public static File loadOpenProject(String projectName, File dataDir) throws Exception {
        File work = new File(TMP_PATH + File.separator + WORK_PATH + File.separator + "w" + System.currentTimeMillis());
        work.mkdirs();
        File project = new File(work, projectName);
        RepositoryMaintenance.loadRepositoryFromFile(project, dataDir.getCanonicalPath() + File.separator + projectName + "_repo.zip");
        //update project
        RepositoryMaintenance.updateRepository(project);
        openProject(work, projectName);
        return work;
    }

    public static boolean waitText(MessageHandler handler) {
        int i = 0;

        while (!handler.isFinished()) {
            i++;
            if (i > TIME_OUT) {
                throw new TimeoutExpiredException("Text [" + handler.message + "] hasn't been found in reasonable time!");
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        return true;
    }

    public static void removeHandlers(Logger log) {
        if (log != null) {
            Handler[] handlers = log.getHandlers();
            for (int i = 0; i < handlers.length; i++) {
                log.removeHandler(handlers[i]);
            }
        }
    }

    public static void waitForScanFinishedSimple() {
        while (IndexingBridge.getInstance().isIndexingInProgress()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                //wake up
            }
        }
    }
}
