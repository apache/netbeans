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
package org.netbeans.test.j2ee.lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.swing.JComboBox;
import junit.framework.AssertionFailedError;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.NbTestCase;
import org.netbeans.test.ide.WatchProjects;

/**
 *
 * @author lm97939
 */
public class Utils {

    private NbTestCase nbtestcase;

    public Utils(NbTestCase nbtestcase) {
        this.nbtestcase = nbtestcase;
    }

    public static String getTimeIndex() {
        return new SimpleDateFormat("HHmmssS", Locale.US).format(new Date());
    }

    /**
     * Starts or Stops AppServer
     * @param start if true, starts appserver, if false stops appserver.
     */
    public static void startStopServer(boolean start) {
        J2eeServerNode glassFishNode = J2eeServerNode.invoke("GlassFish");
        if (start) {
            glassFishNode.start();
        } else {
            glassFishNode.stop();
        }
    }

    public static void prepareDatabase() {
        try {
            new Node(new RuntimeTabOperator().getRootNode(), "Databases|/sample").performPopupActionNoBlock("Connect");
        } catch (TimeoutExpiredException tee) {
            // try once more to prevent re-selection of Databases node by IDE and "Has right selection" error
            new Node(new RuntimeTabOperator().getRootNode(), "Databases|/sample").performPopupActionNoBlock("Connect");
        }
        NbDialogOperator connectingOper = null;
        try {
            connectingOper = new NbDialogOperator("Connecting to Database");
        } catch (TimeoutExpiredException e) {
            // ignore if already connected
        }
        if (connectingOper != null) {
            connectingOper.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 120000);
            connectingOper.waitClosed();
        }
    }

    public void assertFiles(File dir, String fileNames[], String goldenFilePrefix) throws IOException {
        AssertionFailedError firstExc = null;
        for (int i = 0; i < fileNames.length; i++) {
            File file = new File(dir, fileNames[i]);
            try {
                File goldenFile = nbtestcase.getGoldenFile(goldenFilePrefix + fileNames[i]);
                NbTestCase.assertFile("File " + file.getAbsolutePath() + " is different than golden file " + goldenFile.getAbsolutePath() + ".",
                        file,
                        goldenFile,
                        new File(nbtestcase.getWorkDir(), fileNames[i] + ".diff"),
                        new FilteringLineDiff());
            } catch (AssertionFailedError e) {
                if (firstExc == null) {
                    firstExc = e;
                }
                File copy = new File(nbtestcase.getWorkDirPath(), goldenFilePrefix + fileNames[i]);
                copyFile(file, copy);
            }
        }
        if (firstExc != null) {
            throw firstExc;
        }
    }

    /**
     * Copy file in to out
     * @param in File
     * @param out File
     * @throws Exception
     */
    public static void copyFile(File in, File out) {
        try {
            out.createNewFile();
            FileChannel srcChannel = new FileInputStream(in).getChannel();
            FileChannel dstChannel = new FileOutputStream(out).getChannel();
            dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
            srcChannel.close();
            dstChannel.close();
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }

    /**
     * Loads page specified by URL
     * @param url_string URL
     * @throws java.io.IOException
     * @return downloaded page
     */
    public static String loadFromURL(String url_string) throws IOException {
        URL url = new URL(url_string);
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }

    /**
     * Deploys Application
     *
     * @return downloaded page, null if url parameter was null
     * @param projectName Name of Project to deploy
     * @param url URL of page that should be downloaded, can be null.
     * @throws java.io.IOException
     */
    public static String deploy(String projectName, String url, boolean projectNameInStatus) throws IOException {
        JTreeOperator tree = ProjectsTabOperator.invoke().tree();
        tree.setComparator(new Operator.DefaultStringComparator(true, true));
        Node node = new ProjectRootNode(tree, projectName);
        node.performPopupAction(Bundle.getStringTrimmed("org.netbeans.modules.javaee.project.api.ant.ui.logicalview.Bundle", "LBL_RedeployAction_Name"));
        MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 180000);
        MainWindowOperator.getDefault().waitStatusText(Bundle.getString("org.apache.tools.ant.module.run.Bundle", "FMT_finished_target_status", new String[]{(projectNameInStatus ? projectName : "build.xml") + " (run-deploy)"}));
        if (url != null) {
            return Utils.loadFromURL(url);
        }
        return null;
    }

    public static String deploy(String projectnName, String url) throws IOException {
        return deploy(projectnName, url, false);
    }

    /** Undeploys Application. Verifies that application node in runtime disappears.
     * @param app Name of application to undeploy
     */
    public static void undeploy(String app) {
        J2eeServerNode glassFishNode = J2eeServerNode.invoke("GlassFish");
        // "Applications"
        String applicationsLabel = Bundle.getStringTrimmed("org.netbeans.modules.glassfish.common.nodes.Bundle", "LBL_Apps");
        Node applicationsNode = new Node(glassFishNode, applicationsLabel);
        Node node = new Node(applicationsNode, app);
        // "Undeploy"
        node.performPopupAction(Bundle.getStringTrimmed("org.netbeans.modules.glassfish.common.nodes.actions.Bundle", "LBL_UndeployAction"));
        applicationsNode.waitChildNotPresent(app);
    }

    public static void buildProject(String projectName) {
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        Node node = pto.getProjectRootNode(projectName);
        node.performPopupAction("Clean and Build");
        MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 60000);
        MainWindowOperator.getDefault().waitStatusText(Bundle.getString(
                "org.apache.tools.ant.module.run.Bundle", "FMT_finished_target_status",
                new String[]{projectName.replace(' ', '_') + " (clean,dist)"}));
        new EventTool().waitNoEvent(2500);
    }

    public static void cleanProject(String projectName) {
        Action cleanAction = new Action(null, Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_CleanProjectAction_Name_popup"));
        cleanAction.setComparator(new Operator.DefaultStringComparator(true, true));
        cleanAction.perform(new ProjectsTabOperator().getProjectRootNode(projectName));
        MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 60000);
        MainWindowOperator.getDefault().waitStatusText(Bundle.getString(
                "org.apache.tools.ant.module.run.Bundle", "FMT_finished_target_status",
                new String[]{projectName.replace(' ', '_') + " (clean)"}));
        new EventTool().waitNoEvent(2500);
    }

    public static void createLibrary(String name, String[] jars, String[] srcs, String[] javadocs) {
        if ((name == null) || (name.indexOf(" ") > -1)) {
            throw new IllegalArgumentException("Name cannot be null nor contain spaces");
        }
        if (jars == null) {
            jars = new String[0];
        }
        if (srcs == null) {
            srcs = new String[0];
        }
        if (javadocs == null) {
            javadocs = new String[0];
        }
        new ActionNoBlock("Tools|Libraries", null).performMenu();
        NbDialogOperator ndo = new NbDialogOperator(
                Bundle.getString("org.netbeans.api.project.libraries.Bundle", "TXT_LibrariesManager"));
        new JButtonOperator(ndo, Bundle.getStringTrimmed(
                "org.netbeans.modules.project.libraries.ui.Bundle", "CTL_NewLibrary")).push();
        NbDialogOperator ndo2 = new NbDialogOperator(
                Bundle.getString("org.netbeans.modules.project.libraries.ui.Bundle", "CTL_CreateLibrary"));
        JTextFieldOperator jtfo = new JTextFieldOperator(ndo2, 0);
        jtfo.clearText();
        jtfo.typeText(name);
        ndo2.ok();
        JTabbedPaneOperator jtpo = new JTabbedPaneOperator(ndo, "Classpath");
        for (int i = 0; i < jars.length; i++) {
            new JButtonOperator(jtpo, "Add JAR/Folder...").push();
            ndo2 = new NbDialogOperator("Browse JAR/Folder");
            jtfo = new JTextFieldOperator(ndo2, 0);
            jtfo.clearText();
            jtfo.typeText(jars[i]);
            new JButtonOperator(ndo2, "Add JAR/Folder").push();
        }
        jtpo.selectPage("Sources");
        for (int i = 0; i < srcs.length; i++) {
            new JButtonOperator(jtpo, "Add JAR/Folder...").push();
            ndo2 = new NbDialogOperator("Browse JAR/Folder");
            jtfo = new JTextFieldOperator(ndo2, 0);
            jtfo.clearText();
            jtfo.typeText(srcs[i]);
            new JButtonOperator(ndo2, "Add JAR/Folder").push();
        }
        jtpo.selectPage("Javadoc");
        for (int i = 0; i < javadocs.length; i++) {
            new JButtonOperator(jtpo, "Add ZIP/Folder...").push();
            ndo2 = new NbDialogOperator("Browse ZIP/Folder");
            jtfo = new JTextFieldOperator(ndo2, 0);
            jtfo.clearText();
            jtfo.typeText(javadocs[i]);
            new JButtonOperator(ndo2, "Add ZIP/Folder").push();
        }
        ndo.ok();
    }

    public static boolean checkMissingServer(String projectName) {
        // check missing target server dialog is shown    
        // "Open Project"
        String openProjectTitle = Bundle.getString("org.netbeans.modules.j2ee.common.ui.Bundle", "MSG_Broken_Server_Title");
        boolean needToSetServer = false;
        if (JDialogOperator.findJDialog(openProjectTitle, true, true) != null) {
            new NbDialogOperator(openProjectTitle).close();
            needToSetServer = true;
        }
        // open project properties
        ProjectsTabOperator.invoke().getProjectRootNode(projectName).properties();
        // "Project Properties"
        String projectPropertiesTitle = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Customizer_Title");
        NbDialogOperator propertiesDialogOper = new NbDialogOperator(projectPropertiesTitle);
        // select "Run" category
        new Node(new JTreeOperator(propertiesDialogOper), "Run").select();
        if (needToSetServer) {
            // set default server
            JComboBox comboBox = (JComboBox) new JLabelOperator(propertiesDialogOper, "Server").getLabelFor();
            new JComboBoxOperator(comboBox).setSelectedIndex(0);
        }
        // confirm properties dialog
        propertiesDialogOper.ok();
        // if setting default server, it scans server jars; otherwise it continues immediatelly
        WatchProjects.waitScanFinished();
        return needToSetServer;
    }
}
