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

package org.netbeans.modules.performance.utilities;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import org.w3c.dom.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.*;
import java.util.zip.*;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;
import org.netbeans.junit.NbPerformanceTest.PerformanceData;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JavaProjectsTabOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NewJavaProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.EditAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jellytools.PluginsOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.nodes.JavaProjectRootNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.StringComparator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;


/**
 * Utilities for Performance tests, workarrounds, often used methods, ...
 *
 * @author  mmirilovic@netbeans.org, mrkam@netbeans.org
 */
public class CommonUtilities {
    
    public static final String SOURCE_PACKAGES = "Source Packages";
    public static final String TEST_PACKAGES = "Test Packages";
    private static PerformanceTestCase test = null;
    
    private static DocumentBuilderFactory dbf=null;
    private static DocumentBuilder db=null;
    private static Document allPerfDoc=null;
    private static Element testResultsTag, testTag, perfDataTag, testSuiteTag=null;
    private static String projectsDir; // <nbextra>/data/
    private static String tempDir; // <nbjunit.workdir>/tmpdir/
    
    static {
        String workDir = System.getProperty("nbjunit.workdir");
        String altPath = System.getProperty("nb_perf_alt_path");
        if (workDir != null) {
            if (altPath!=null) {
                projectsDir = altPath + File.separator;
            } else {
                projectsDir = workDir + File.separator;
            }
            try {
                projectsDir = new File(projectsDir + File.separator + ".." 
                        + File.separator + ".." + File.separator + ".." 
                        + File.separator + ".." + File.separator + ".." 
                        + File.separator + ".." + File.separator + ".." 
                        + File.separator + "nbextra" + File.separator + "data")
                        .getCanonicalPath() + File.separator;
            } catch (IOException ex) {
                System.err.println("Exception: " + ex);
            }

            tempDir = workDir + File.separator;
            try {
                File dir = new File(tempDir + File.separator + "tmpdir");
                tempDir = dir.getCanonicalPath() + File.separator;
                dir.mkdirs();
            } catch (IOException ex) {
                System.err.println("Exception: " + ex);
            }
        }
    }
    
    /**
     * Returns data directory path ending with file.separator
     * @return &lt;nbextra&gt;/data/
     */
    public static String getProjectsDir() {
        return projectsDir;
    }

    /**
     * Returns temprorary directory path ending with file.separator
     * @return &lt;nbjunit.workdir&gt;/tmpdir/
     */
    public static String getTempDir() {
        return tempDir;
    }
    
    public static void cleanTempDir() throws IOException {
        File dir = new File(tempDir);
        deleteFile(dir);
        dir.mkdirs();
    }

    // private method for deleting a file/directory (and all its subdirectories/files)
    public static void deleteFile(File file) throws IOException {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            // file is a directory - delete sub files first
            File files[] = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteFile(files[i]);
            }
            
        }
        // file is a File :-)
        boolean result = file.delete();
        if (result == false ) {
            // a problem has appeared
            throw new IOException("Cannot delete file, file = " + file.getPath());
        }
    }
    
    /** Creates a new instance of Utilities */
    public CommonUtilities() {
    }

    public static String getTimeIndex() {
        return new SimpleDateFormat("HHmmssS",Locale.US).format(new Date());
    }
    
    /**
     * Close BluePrints.
     */
    public static void closeBluePrints(){
        new TopComponentOperator(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.blueprints.Bundle","LBL_Tab_Title")).close();
    }
    
    /**
     * Close All Documents.
     */
    public static void closeAllDocuments(){
        EditorOperator.closeDiscardAll();
    }
    
    /**
     * Close Memory Toolbar.
     */
    public static void closeMemoryToolbar(){
        // View|Toolbars|Memory        
        try {  // workaround for Issue #213828
            FileObject fo = FileUtil.getConfigFile("Toolbars/Memory");
            if (fo!=null) {
                fo.delete();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }        
//        closeToolbar(Bundle.getStringTrimmed("org.openide.actions.Bundle","View") + "|" +
//                Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle", "CTL_ToolbarsListAction") + "|" +
//                "Memory");
        maximizeWholeNetbeansWindow();
    }
    
    public static void installPlugin(String name) {

       PluginsOperator po = PluginsOperator.invoke();

       po.install(name);
       po.close();
    }   

    private static void closeToolbar(String menu){
        MainWindowOperator mainWindow = MainWindowOperator.getDefault();
        JMenuBarOperator menuBar = new JMenuBarOperator(mainWindow.getJMenuBar());
        JMenuItemOperator menuItem = menuBar.showMenuItem(menu,"|");
        
        if(menuItem.isSelected())
            menuItem.push();
        else {
            menuItem.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
            mainWindow.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
        }
    }
    
    /**
     * Work around issue 35962 (Main menu popup accidentally rolled up)
     * Issue has been fixed for JDK 1.5, so we will use it only for JDK 1.4.X
     */
    public static void workarroundMainMenuRolledUp() {
        if(System.getProperty("java.version").indexOf("1.4") != -1) {
            String helpMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/Help") + "|" + Bundle.getStringTrimmed("org.netbeans.core.actions.Bundle" , "About");
            String about = Bundle.getStringTrimmed("org.netbeans.core.Bundle_nb", "CTL_About_Title");
            
            new ActionNoBlock(helpMenu, null).perform();
            new NbDialogOperator(about).close();
        }
    }

    public static String jEditProjectOpen() {

/* Temporary solution - download jEdit from internal location */

        OutputStream out = null;
        URLConnection conn = null;
        InputStream in = null;
        int BUFFER = 2048;

        try {
            URL url = new URL("http://spbweb.russia.sun.com/~ok153203/jEdit41.zip");

            out = new BufferedOutputStream(new FileOutputStream(System.getProperty("nbjunit.workdir") + File.separator + "tmpdir" + File.separator + "jEdit41.zip"));
            conn = url.openConnection();
            in = conn.getInputStream();
            byte[] buffer = new byte[1024];
            int numRead;
            while ((numRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, numRead);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException ioe) {
            }
        }

        try {
            BufferedOutputStream dest = null;
            FileInputStream fis = new FileInputStream(new File(System.getProperty("nbjunit.workdir") + File.separator + "tmpdir" + File.separator + "jEdit41.zip"));
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    new File(System.getProperty("nbjunit.workdir") + File.separator + ".." + File.separator + "data" + File.separator + entry.getName()).mkdir();
                    continue;
                }
                int count;
                byte data[] = new byte[BUFFER];
                FileOutputStream fos = new FileOutputStream(System.getProperty("nbjunit.workdir") + File.separator + ".." + File.separator + "data" + File.separator + entry.getName());
                dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
            }
            zis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return System.getProperty("nbjunit.workdir") + File.separator + "tmpdir" + File.separator + "jEdit41.zip";
    }


    /**
     * Open files
     *
     * @param project project which will be used as source for files to be opened
     * @param files_path path to the files to be opened
     */
    public static void openFiles(String project, String[][] files_path){
        Node[] openFileNodes = new Node[files_path.length];
        
        SourcePackagesNode sourcePackagesNode = new SourcePackagesNode(project);
        
        for(int i=0; i<files_path.length; i++) {
            openFileNodes[i] = new Node(sourcePackagesNode, files_path[i][0] + '|' + files_path[i][1]);
            
            // open file one by one, opening all files at once causes never ending loop (java+mdr)
            // new OpenAction().performAPI(openFileNodes[i]);
        }
        
        // try to come back and open all files at-once, rises another problem with refactoring, if you do open file and next expand folder,
        // it doesn't finish in the real-time -> hard to reproduced by hand
        new OpenAction().performAPI(openFileNodes);
    }
    
    /**
     * Copy file f1 to f2
     * @param f1 file 1
     * @param f2 file 2
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public static void copyFile(java.io.File f1, java.io.File f2) throws java.io.FileNotFoundException, java.io.IOException{
        int data;
        java.io.InputStream fis = new java.io.BufferedInputStream(new java.io.FileInputStream(f1));
        java.io.OutputStream fos = new java.io.BufferedOutputStream(new java.io.FileOutputStream(f2));
        
        while((data=fis.read())!=-1){
            fos.write(data);
        }
    }
    
    /**
     * Invoke open action on file and wait for editor
     * @param filename
     * @param waitforeditor
     * @return
     */
    public static EditorOperator openFile(Node fileNode, String filename, boolean waitforeditor) {
        new OpenAction().performAPI(fileNode);
        
        if (waitforeditor) {
            EditorOperator editorOperator = new EditorOperator(filename);
            return editorOperator;
        } else
            return null;
    }
    
    
    public static EditorOperator openFile(String project, String filepackage, String filename, boolean waitforeditor) {
        return openFile(new Node(new SourcePackagesNode(project), filepackage + "|" + filename), filename, waitforeditor);
    }
    
    /**
     * Invoke Edit Action on file and wait for editor
     * @param project
     * @param filepackage
     * @param filename
     * @return
     */
    public static EditorOperator editFile(String project, String filepackage, String filename) {
        Node filenode = new Node(new SourcePackagesNode(project), filepackage + "|" + filename);
        new EditAction().performAPI(filenode);
        EditorOperator editorOperator = new EditorOperator(filename);
        return editorOperator;
    }
    
    
    /**
     * open small form file in the editor
     * @return Form Designer
     */
    public static FormDesignerOperator openSmallFormFile(){
        Node openFile = new Node(new SourcePackagesNode("PerformanceTestData"),"org.netbeans.test.performance|JFrame20kB.java");
        new OpenAction().performAPI(openFile);
        return new FormDesignerOperator("JFrame20kB");
        
    }
    
    
    /**
     * Edit file and type there a text
     * @param filename file that will be eddited
     * @param line line where put the text
     * @param text write the text
     * @param save save at the and
     */
    public static void insertToFile(String filename, int line, String text, boolean save) {
        EditorOperator editorOperator = new EditorOperator(filename);
        editorOperator.setCaretPositionToLine(line);
        editorOperator.insert(text);
        
        if (save)
            editorOperator.save();
    }
    
    /**
     * Create project
     * @param category project's category
     * @param project type of the project
     * @param wait wait for background tasks
     * @return name of recently created project
     */
    public static String createproject(String category, String project, boolean wait) {
        // select Projects tab
        ProjectsTabOperator.invoke();
        
        // create a project
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.next();
        
        NewJavaProjectNameLocationStepOperator wizard_location = new NewJavaProjectNameLocationStepOperator();
        wizard_location.txtProjectLocation().clearText();
        wizard_location.txtProjectLocation().typeText(getTempDir());
        String pname = wizard_location.txtProjectName().getText() + System.currentTimeMillis();
        wizard_location.txtProjectName().clearText();
        wizard_location.txtProjectName().typeText(pname);
        
//        // if the project exists, try to generate new name
//        for (int i = 0; i < 5 && !wizard.btFinish().isEnabled(); i++) {
//            pname = pname+"1";
//            wizard_location.txtProjectName().clearText();
//            wizard_location.txtProjectName().typeText(pname);
//        }
        wizard.finish();
        
        // wait 10 seconds
        waitForProjectCreation(10000, wait);
        
        return pname;
    }
    
    
    protected static void waitForProjectCreation(int delay, boolean wait){
        try {
            Thread.sleep(delay);
        } catch (InterruptedException exc) {
            exc.printStackTrace(System.err);
        }
        
        // wait for classpath scanning finish
        if (wait) {
//            waitScanFinished();
            waitForPendingBackgroundTasks();
        }
    }
    
    
    /**
     * Delete project
     * @param project project to be deleted
     */
    public static void deleteProject(String project) {
        deleteProject(project, false);
    }
    
    
    public static void deleteProject(String project, boolean waitStatus) {
        new DeleteAction().performAPI(ProjectsTabOperator.invoke().getProjectRootNode(project));
        
        //delete project
        NbDialogOperator deleteProject = new NbDialogOperator("Delete Project"); // NOI18N
        JCheckBoxOperator delete_sources = new JCheckBoxOperator(deleteProject);
        
        if(delete_sources.isEnabled())
            delete_sources.changeSelection(true);
        
        deleteProject.yes();
        
        waitForPendingBackgroundTasks();
        
        if(waitStatus)
            MainWindowOperator.getDefault().waitStatusText("Finished building "+project+" (clean)"); // NOI18N
        
        try {
            //sometimes dialog rises
            new NbDialogOperator("Question").yes(); // NOI18N
        }catch(Exception exc){
            System.err.println("No Question dialog rises - no problem this is just workarround!");
            exc.printStackTrace(System.err);
        }
        
    }
    
    
    
    /**
     * Build project and wait for finish
     * @param project
     */
    public static void buildProject(String project) {
        JavaProjectRootNode prn = JavaProjectsTabOperator.invoke().getJavaProjectRootNode(project);
        prn.buildProject();
        StringComparator sc = MainWindowOperator.getDefault().getComparator();        
        MainWindowOperator.getDefault().setComparator(new Operator.DefaultStringComparator(false, true));
        MainWindowOperator.getDefault().waitStatusText("Finished building "); // NOI18N
        MainWindowOperator.getDefault().setComparator(sc);
    }
    
    /**
     * Invoke action on project node from popup menu
     * @param project
     * @param pushAction
     */
    public static void actionOnProject(String project, String pushAction) {
        ProjectRootNode prn;
        try {
            prn = ProjectsTabOperator.invoke().getProjectRootNode(project);
        } catch (TimeoutExpiredException e) {
            prn = new ProjectsTabOperator().getProjectRootNode(project);
        }
        prn.callPopup().pushMenuNoBlock(pushAction);
    }
    
    /**
     * Run project
     * @param project
     */
    public static void runProject(String project) {
        actionOnProject(project,"Run Project"); // NOI18N
        // TODO MainWindowOperator.getDefault().waitStatusText("run"); // NOI18N
    }
    
    /**
     * Debug project
     * @param project
     */
    public static void debugProject(String project) {
        actionOnProject(project,"Debug Project"); // NOI18N
        // TODO MainWindowOperator.getDefault().waitStatusText("debug"); // NOI18N
    }
    
    
    /**
     * Test project
     * @param project
     */
    public static void testProject(String project) {
        actionOnProject(project, "Test Project"); // NOI18N
        // TODO MainWindowOperator.getDefault().waitStatusText("test"); // NOI18N
    }
    
    /**
     * Deploy project and wait for finish
     * @param project
     */
    public static void deployProject(String project) {
        actionOnProject(project, "Deploy Project"); // NOI18N
        waitForPendingBackgroundTasks();
        MainWindowOperator.getDefault().waitStatusText("Finished building "+project+" (run-deploy)"); // NOI18N
    }
    
    /**
     * Verify project and wait for finish
     * @param project
     */
    public static void verifyProject(String project) {
        actionOnProject(project, "Verify Project"); // NOI18N
        MainWindowOperator.getDefault().waitStatusText("Finished building "+project+" (verify)"); // NOI18N
    }
    
    
    /**
     * Open project and wait until it's scanned
     * @param projectFolder Project's location
     */
    public static void waitProjectOpenedScanFinished(String projectFolder){
        //ProjectSupport.openProject(projectFolder);
//        waitScanFinished();
    }
    
    public static void waitForPendingBackgroundTasks() {
 //       waitForPendingBackgroundTasks(5);
    }
    
/*    public static void waitForPendingBackgroundTasks(int n) {
        // wait maximum n minutes
        for (int i=0; i<n*60; i++) {
            if (org.netbeans.progress.module.Controller.getDefault().getModel().getSize()==0)
                return;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException exc) {
                exc.printStackTrace(System.err);
                return;
            }
        }
    }*/
    
    /**
     * Adds GlassFish server using path from glassfish.home property
     */
    public static void addApplicationServer() {

        String glassfishHome = System.getProperty("glassfish.home");
        
        if (glassfishHome == null) {
            throw new Error("Can't add GlassFish server. glassfish.home property is not set.");
        }

        String addServerMenuItem = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.actions.Bundle", "LBL_Add_Server_Instance"); // Add Server...
        String addServerInstanceDialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.wizard.Bundle", "LBL_ASIW_Title"); //"Add Server Instance"

        RuntimeTabOperator rto = RuntimeTabOperator.invoke();
        Node serversNode = new Node(rto.getRootNode(), "Servers");
        // Let's check whether GlassFish is already added
        if (!serversNode.isChildPresent("GlassFish")) {
            // There is no GlassFish node so we'll add it
            serversNode.performPopupActionNoBlock(addServerMenuItem);
            WizardOperator addServerInstanceDialog = new WizardOperator(addServerInstanceDialogTitle);
            new JListOperator(addServerInstanceDialog, 1).selectItem("GlassFish Server");
            addServerInstanceDialog.next();
            new JTextFieldOperator(addServerInstanceDialog).setText(glassfishHome);
            addServerInstanceDialog.next();
            addServerInstanceDialog.finish();
        }
    }

    public static Node getTomcatServerNode(){
        return new Node(RuntimeTabOperator.invoke().getRootNode(), "Servers|Tomcat");
    }

    public static Node getApplicationServerNode(){
        return new Node(RuntimeTabOperator.invoke().getRootNode(), "Servers|GlassFish");
    }
    
    public static Node startTomcatServer() {
        Node node = performTomcatServerAction("Start");  // NOI18N
        new EventTool().waitNoEvent(10000);
        return node;
    }

    public static Node stopTomcatServer() {
        Node node = performTomcatServerAction("Stop");  // NOI18N
        new EventTool().waitNoEvent(10000);
        return node;
    }


    public static void addTomcatServer() {
        String appServerPath = System.getProperty("tomcat.installRoot");
        
        if (appServerPath == null) {
            throw new Error("Can't add tomcat server. tomcat.installRoot property is not set.");
        }
        
        String addServerMenuItem = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.actions.Bundle", "LBL_Add_Server_Instance"); // Add Server...
        String addServerInstanceDialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.wizard.Bundle", "LBL_ASIW_Title"); //"Add Server Instance"
        String nextButtonCaption = Bundle.getStringTrimmed("org.openide.Bundle", "CTL_NEXT");
        String finishButtonCaption = Bundle.getStringTrimmed("org.openide.Bundle", "CTL_FINISH");

        RuntimeTabOperator rto = RuntimeTabOperator.invoke();
        Node serversNode = new Node(rto.getRootNode(), "Servers");
        // Let's check whether GlassFish is already added
        if (!serversNode.isChildPresent("Tomcat")) {
            serversNode.performPopupActionNoBlock(addServerMenuItem);
            NbDialogOperator addServerInstanceDialog = new NbDialogOperator(addServerInstanceDialogTitle);
            new JListOperator(addServerInstanceDialog, 1).selectItem("Tomcat");
            new JButtonOperator(addServerInstanceDialog, nextButtonCaption).push();
            new JTextFieldOperator(addServerInstanceDialog, 1).setText(appServerPath);
            new JCheckBoxOperator(addServerInstanceDialog,1).changeSelection(false);
            new JButtonOperator(addServerInstanceDialog, finishButtonCaption).push();
        }
    }

    private static Node performTomcatServerAction(String action) {
        Node asNode = getTomcatServerNode();
        asNode.select();
        new EventTool().waitNoEvent(10000);
        String serverIDEName = asNode.getText();
        log("ServerNode name = "+serverIDEName);
        JPopupMenuOperator popup = asNode.callPopup();
        if (popup == null) {
            throw new Error("Cannot get context menu for Tomcat server node ");
        }
        boolean startEnabled = popup.showMenuItem(action).isEnabled();
        if(startEnabled) {
            popup.pushMenuNoBlock(action);
        }
        return asNode;
    }

    /**
     * Wait finished scan - repeatedly
     */
    public static void waitScanFinished(){
        try {
            new QueueTool().waitEmpty();
        } catch (TimeoutExpiredException tee) {            
            getLog().println("The following exception is ignored");
            tee.printStackTrace(getLog());
        }
     }
    
    public static void initLog(PerformanceTestCase testCase) {
        test = testCase;
    }
    public static void closeLog() {
        test = null;
    }
    private static void log(String logMessage) {
        System.out.println("Utilities::"+logMessage);
        if( test != null  ) { test.log("Utilities::"+logMessage); }
    }
    private static PrintStream getLog() {
        if( test != null  ) { 
            return test.getLog(); 
        } else {
            return System.out;
        }
    }
    
    public static void killRunOnProject(String project) {
        killProcessOnProject(project, "run");
    }
    
    public static void killDebugOnProject(String project) {
        killProcessOnProject(project, "debug");
    }
    
    private static void killProcessOnProject(String project, String process) {
        // prepare Runtime tab
        RuntimeTabOperator runtime = RuntimeTabOperator.invoke();
        
        // kill the execution
        Node node = new Node(runtime.getRootNode(), "Processes|"+project+ " (" + process + ")");
        node.select();
        node.performPopupAction("Terminate Process");
    }
    
    public static void xmlTestResults(String path, String suite, String name, String classname, String sname, String unit, String pass, long threshold, long[] results, int repeat) {

        PrintStream out = System.out;

        System.out.println();
        System.out.println("#####  Results for "+name+"   #####");
        System.out.print("#####        [");
        for(int i=1;i<=repeat;i++)             
            System.out.print(results[i]+"ms, ");
        System.out.println("]");
        for (int i=1;i<=name.length()+27;i++)
            System.out.print("#");
        System.out.println();
        System.out.println();

        path=System.getProperty("nbjunit.workdir");
        File resGlobal=new File(path+File.separator+"allPerformance.xml");

        try {
            dbf=DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
         } catch (Exception ex) {
            ex.printStackTrace (  ) ;
        }

        if (!resGlobal.exists()) {
            try {
                resGlobal.createNewFile();
                out = new PrintStream(new FileOutputStream(resGlobal));
                out.print("<TestResults>\n");
                out.print("</TestResults>");
                out.close();
            } catch (IOException ex) {
            ex.printStackTrace (  ) ;
            }
         }

        try {
              allPerfDoc = db.parse(resGlobal);
            } catch (Exception ex) {
            ex.printStackTrace (  ) ;
            }
            
        testResultsTag=allPerfDoc.getDocumentElement();
        String buildNumber = System.getProperty("org.netbeans.performance.buildnumber");
        if (buildNumber != null) {
            testResultsTag.setAttribute("buildnumber", buildNumber);
        }

        testTag=null;
        for (int i=0;i<allPerfDoc.getElementsByTagName("Test").getLength();i++) {
            NamedNodeMap attributes = allPerfDoc.getElementsByTagName("Test").item(i).getAttributes();
            String nameFromDoc = attributes.getNamedItem("name").toString();
            String classnameFromDoc = attributes.getNamedItem("classname").toString();
            if (("name=\"" + name + "\"").equalsIgnoreCase(nameFromDoc) && ("classname=\"" + classname + "\"").equalsIgnoreCase(classnameFromDoc)) {
                testTag = (Element) allPerfDoc.getElementsByTagName("Test").item(i);
                break;
            }
        }

        if (testTag!=null) {
            for (int i=1;i<=repeat;i++) {
                perfDataTag=allPerfDoc.createElement("PerformanceData");
                if (i==1) perfDataTag.setAttribute("runOrder", "1");
                    else perfDataTag.setAttribute("runOrder", "2");
                perfDataTag.setAttribute("value", new Long(results[i]).toString());
                testTag.appendChild(perfDataTag);
            }
        }
        else {
            testTag=allPerfDoc.createElement("Test");
            testTag.setAttribute("name", name);
            testTag.setAttribute("unit", unit);
            testTag.setAttribute("results", pass);
            testTag.setAttribute("threshold", new Long(threshold).toString());
            testTag.setAttribute("classname", classname);
            for (int i=1;i<=repeat;i++) {
                perfDataTag=allPerfDoc.createElement("PerformanceData");
                if (i==1) perfDataTag.setAttribute("runOrder", "1");
                    else perfDataTag.setAttribute("runOrder", "2");
                perfDataTag.setAttribute("value", new Long(results[i]).toString());
                testTag.appendChild(perfDataTag);
            }
        }

            testSuiteTag = null;
            if (suite != null) {
                for (int i=0;i<allPerfDoc.getElementsByTagName("Suite").getLength();i++) {
                    final NodeList elem = allPerfDoc.getElementsByTagName("Suite");
                    if (elem == null) {
                        continue;
                    }
                    final org.w3c.dom.Node item = elem.item(i);
                    if (item == null) {
                        continue;
                    }
                    final NamedNodeMap attrs = item.getAttributes();
                    if (attrs == null) {
                        continue;
                    }
                    final org.w3c.dom.Node ni = attrs.getNamedItem("suitename");
                    if (ni == null) {
                        continue;
                    }

                    if (suite.equalsIgnoreCase(ni.getNodeValue())) {
                        testSuiteTag =(Element)item;
                        break;
                    }
                }
            }

            if (testSuiteTag==null) {
                testSuiteTag=allPerfDoc.createElement("Suite");
                testSuiteTag.setAttribute("name", sname);
                testSuiteTag.setAttribute("suitename", suite);
                testSuiteTag.appendChild(testTag);
            } else {
                testSuiteTag.appendChild(testTag);
            }

        testResultsTag.appendChild(testSuiteTag);


        try {
            out = new PrintStream(new FileOutputStream(resGlobal));
        } catch (FileNotFoundException ex) {
        }

        Transformer tr=null;
        try {
            tr = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException ex) {
        }

        tr.setOutputProperty(OutputKeys.INDENT, "no");
        tr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        DOMSource docSrc = new DOMSource(allPerfDoc);
        StreamResult result = new StreamResult(out);

        try {
            tr.transform(docSrc, result);
        } catch (TransformerException ex) {
        }
        out.close();
    }

    public static void processUnitTestsResults(String className, PerformanceData pd) {
        processUnitTestsResults(className, className, pd);
    }
    
    public static void processUnitTestsResults(String className, String suiteName, PerformanceData pd) {
        long[] result=new long[2];
        result[1]=pd.value;
        CommonUtilities.xmlTestResults(System.getProperty("nbjunit.workdir"), "Unit Tests Suite", pd.name, className, suiteName, pd.unit, "passed", 120000 , result, 1);
    }
    
    public static void maximizeWholeNetbeansWindow() {
        MainWindowOperator.getDefault().maximize();
    }
    
    /**
     * Disables or enables spell checking. By default spell checking is enabled.
     *
     * @param enabled true to enable spell checker, false to disable
     */
    public static void setSpellcheckerEnabled(boolean enabled) {
        FileObject root = FileUtil.getConfigFile("Spellcheckers");
        if (root != null) {
            FileObject[] children = root.getChildren();
            for (FileObject fileObject : children) {
                try {
                    fileObject.setAttribute("Hidden", !enabled);
                } catch (IOException ex) {
                    throw new JemmyException("Error while disabling spellchecker.", ex);
                }
            }
        }
    }
}
