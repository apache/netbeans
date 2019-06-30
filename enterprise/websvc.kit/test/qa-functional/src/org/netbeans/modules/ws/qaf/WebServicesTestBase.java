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
package org.netbeans.modules.ws.qaf;

import java.awt.Component;
import java.awt.Container;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.*;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.*;
import org.netbeans.modules.project.ui.test.ProjectSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Base class for web services UI tests
 * @author lukas
 */
public abstract class WebServicesTestBase extends J2eeTestCase {

    protected static ServerType REGISTERED_SERVER;
    private static final Logger LOGGER = Logger.getLogger(WebServicesTestBase.class.getName());
    private Project project;
    private String projectName;
    private ProjectType projectType;
    private JavaEEVersion javaEEversion;

    /**
     * Enum type to hold project specific settings (like ie. project category
     * label, project template name, etc.)
     */
    protected enum ProjectType {

        JAVASE_APPLICATION,
        WEB,
        EJB,
        APPCLIENT,
        SAMPLE,
        MAVEN_SE,
        MAVEN_WEB,
        MAVEN_EJB;

        /**
         * Get project template category name
         *
         * @return category name
         */
        public String getCategory() {
            switch (this) {
                case JAVASE_APPLICATION:
                    //Java
                    return Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "Templates/Project/Standard");
                case WEB:
                    //Java Web
                    return Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.wizards.Bundle", "Templates/Project/Web");
                case EJB:
                    //Java EE
                    return Bundle.getStringTrimmed("org.netbeans.modules.j2ee.earproject.ui.wizards.Bundle", "Templates/Project/J2EE");
                case APPCLIENT:
                    //Java EE
                    return Bundle.getStringTrimmed("org.netbeans.modules.j2ee.earproject.ui.wizards.Bundle", "Templates/Project/J2EE");
                case SAMPLE:
                    //Samples
                    return Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Templates/Project/Samples");
                case MAVEN_SE:
                case MAVEN_WEB:
                case MAVEN_EJB:
                    //Maven
                    return Bundle.getStringTrimmed("org.netbeans.modules.maven.newproject.Bundle", "Templates/Project/Maven2");
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        /**
         * Get project template project type name
         *
         * @return project type name
         */
        public String getProjectTypeName() {
            switch (this) {
                case JAVASE_APPLICATION:
                    return "Java Application";
                case WEB:
                    //Web Application
                    return Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.wizards.Bundle", "Templates/Project/Web/emptyWeb.xml");
                case EJB:
                    //EJB Module
                    return Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbjarproject.ui.wizards.Bundle", "Templates/Project/J2EE/emptyEjbJar.xml");
                case APPCLIENT:
                    //Enterprise Application Client
                    return Bundle.getStringTrimmed("org.netbeans.modules.j2ee.clientproject.ui.wizards.Bundle", "Templates/Project/J2EE/emptyCar.xml");
                case MAVEN_SE:
                    return "Java Application";
                case MAVEN_WEB:
                    return "Web Application";
                case MAVEN_EJB:
                    return "EJB Module";
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        /**
         * Get index of Server JComboBox in new project wizard
         *
         * @return index of Server JComboBox or -1 if there's none
         */
        public int getServerComboBoxIndex() {
            switch (this) {
                case MAVEN_SE:
                case JAVASE_APPLICATION:
                    return -1;
                case WEB:
                case EJB:
                case APPCLIENT:
                    return 1;
                case MAVEN_WEB:
                case MAVEN_EJB:
                    return 0;
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        /**
         * Get index of Java EE version JComboBox in new project wizard
         *
         * @return index of Java EE version JComboBox or -1 if there's none
         */
        public int getServerVersionComboBoxIndex() {
            switch (this) {
                case MAVEN_SE:
                case JAVASE_APPLICATION:
                    return -1;
                case MAVEN_WEB:
                case MAVEN_EJB:
                    return 1;
                case WEB:
                case APPCLIENT:
                case EJB:
                    return 2;
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        public boolean isAntBasedProject() {
            return this.ordinal() < 5;
        }
    }

    /**
     * Enum type to hold supported JavaEE versions
     */
    protected enum JavaEEVersion {

        J2EE14,
        JAVAEE5,
        JAVAEE6,
        JAVAEE7,
        JAVAEE8;

        @Override
        public String toString() {
            switch (this) {
                case J2EE14:
                    //J2EE 1.4
                    return Bundle.getStringTrimmed("org.netbeans.modules.j2ee.common.Bundle", "LBL_J2EESpec_14");
                case JAVAEE5:
                    //Java EE 5
                    return Bundle.getStringTrimmed("org.netbeans.modules.j2ee.common.Bundle", "LBL_JavaEESpec_5");
                case JAVAEE6:
                    //Java EE 6
                    return Bundle.getStringTrimmed("org.netbeans.api.j2ee.core.Bundle", "JavaEE6Full.displayName");
                case JAVAEE7:
                    //Java EE 7
                    return "Java EE 7";
                case JAVAEE8:
                      //Java EE 8
                      return "Java EE 8";
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }
    }

    /**
     * Enum type to hold supported servers
     */
    protected enum ServerType {

        SJSAS,
        GLASSFISH,
        TOMCAT,
        JBOSS;
    }

    /**
     * Default constructor.
     *
     * @param testName name of particular test case
     */
    public WebServicesTestBase(String name) {
        super(name);
        setProjectName(getProjectName());
        setProjectType(getProjectType());
        setJavaEEversion(getJavaEEversion());
    }

    /**
     * Default constructor.
     *
     * @param testName name of particular test case
     * @param server type of server to be used
     */
    public WebServicesTestBase(String name, Server server) {
        super(name);
        String serverString = server.toString();
        if (serverString.equalsIgnoreCase("tomcat")) {
            REGISTERED_SERVER = ServerType.TOMCAT;
        } else if (serverString.equalsIgnoreCase("glassfish")) {
            REGISTERED_SERVER = ServerType.GLASSFISH;
        } else if (serverString.equalsIgnoreCase("jboss")) {
            REGISTERED_SERVER = ServerType.JBOSS;
        } else {
            REGISTERED_SERVER = null;
        }
        setProjectName(getProjectName());
        setProjectType(getProjectType());
        setJavaEEversion(getJavaEEversion());
    }

    public void assertServerRunning() {
        if (!(REGISTERED_SERVER.equals(ServerType.GLASSFISH))) {
            LOGGER.log(Level.INFO, "not yet supported for server: {0}", REGISTERED_SERVER.toString());
            return;
        }
        J2eeServerNode gf = getServerNode();
        gf.refresh();
        if (gf.isCollapsed()) {
            gf.expand();
        }
        assertTrue("Server is not running", 0 < gf.getChildren().length);
    }

    /**
     * Get the name of the project to be used by test case
     *
     * @return name of the project
     */
    protected abstract String getProjectName();

    /**
     * Get the name of the sample project's category (ie. Web Services)
     *
     * @return name of the project
     */
    protected String getSamplesCategoryName() {
        return "";
    }

    /**
     * Get a Project instance used by test case
     *
     * @return a Project instance
     */
    protected Project getProject() {
        return project;
    }

    /**
     * Get <code>Node</code> for the project used by test case
     *
     * @return an instance of <code>ProjectRootNode</code>
     */
    protected ProjectRootNode getProjectRootNode() {
        return ProjectsTabOperator.invoke().getProjectRootNode(getProjectName());
    }

    /**
     * Get <code>FileObject</code> representing default project source root
     *
     * @return default project source root
     */
    protected FileObject getProjectSourceRoot() {
        Sources s = ProjectUtils.getSources(getProject());
        SourceGroup[] sg = s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        return sg[0].getRootFolder();
    }

    /**
     * Java EE version set for test case, JavaEEVersion.JAVAEE5
     * is used by default
     * Override this method to use different Java EE version
     *
     * @return Java EE version set for test case
     */
    protected JavaEEVersion getJavaEEversion() {
        return JavaEEVersion.JAVAEE5;
    }

    /**
     * Project type set for test case, ProjectType.WEB is used by default
     * Override this method to use different ProjectType
     *
     * @return ProjectType set for test case
     */
    protected ProjectType getProjectType() {
        return ProjectType.WEB;
    }

    /**
     * Method responsible for checking and setting up environment for particular
     * test case, mainly for setting up project to be used by test case.<br/>
     *
     * Following logic is used for setting up a project (Note that
     * <code>getProjectName()</code> method is used for getting correct project
     * name):<br/>
     * <ol>
     *  <li>look for a project in <i>projects</i> directory in data directory
     *      and if project is found there then open it in the IDE
     *  </li>
     *  <li>look for a project in <code>getWorkDir().getParentFile().getParentFile()</code>
     *      or in <code>System.getProperty("java.io.tmpdir")</code> directory
     *      (if some parent file does not exist), if project is found then open it
     *      in the IDE</li>
     *  <li>if project is not found then it will be created from scratch</li>
     * </ol>
     *
     * @throws java.lang.Exception
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        assertNotNull("No server has been found", REGISTERED_SERVER); //NOI18N
        if (!ProjectType.SAMPLE.equals(getProjectType())) {
            if (ServerType.TOMCAT.equals(REGISTERED_SERVER) && !ProjectType.WEB.equals(getProjectType()) && !ProjectType.JAVASE_APPLICATION.equals(getProjectType())) {
                fail("Tomcat does not support: " + getProjectType().getProjectTypeName() + "s."); //NOI18N
            }
            System.out.println("########  TestCase: " + getClass().getSimpleName() + "." + getName() + "  #######"); //NOI18N
            System.out.println("########  Server: " + REGISTERED_SERVER.toString() + "  #######"); //NOI18N
            File projectRoot = new File(getDataDir(), "projects/" + getProjectName()); //NOI18N
            if (projectRoot.exists()) {
                project = (Project) ProjectSupport.openProject(new File(getDataDir(), "projects/" + getProjectName()));
                checkMissingServer(getProjectName());
            } else {
                projectRoot = new File(getProjectsRootDir(), projectName);
                LOGGER.log(Level.INFO, "Using project in: {0}", projectRoot.getAbsolutePath()); //NOI18N
                if (!projectRoot.exists()) {
                    if (!getProjectType().isAntBasedProject()) {
                        runAndCancelUpdateIndex("central");
                        runAndCancelUpdateIndex("Local");
                    }
                    project = createProject(projectName, getProjectType(), getJavaEEversion());
                    // open otuput window
                    OutputOperator.invoke();
                    // not display browser on run for Maven projects
                    if (ProjectType.MAVEN_WEB.equals(getProjectType())) {
                        //Properties
                        String propLabel = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.Bundle", "LBL_Properties_Action");
                        new ActionNoBlock(null, propLabel).performPopup(getProjectRootNode());
                        // "Project Properties"
                        String projectPropertiesTitle = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Customizer_Title");
                        NbDialogOperator propertiesDialogOper = new NbDialogOperator(projectPropertiesTitle);
                        // select "Run" category
                        new Node(new JTreeOperator(propertiesDialogOper), "Run").select();
                        String displayBrowserLabel = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_CustomizeRun_DisplayBrowser_JCheckBox");
                        new JCheckBoxOperator(propertiesDialogOper, displayBrowserLabel).setSelected(false);
                        // confirm properties dialog
                        propertiesDialogOper.ok();
                    }
                } else {
                    openProjects(projectRoot.getAbsolutePath());
                    FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(projectRoot));
                    assertNotNull("FO cannot be null", fo); //NOI18N
                    project = ProjectManager.getDefault().findProject(fo);
                    checkMissingServer(projectName);
                }
            }
            assertNotNull("Project cannot be null!", project); //NOI18N
        }
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        project = null;
        projectName = null;
    }

    /**
     * Start a server
     */
    public void testStartServer() throws IOException {
        J2eeServerNode serverNode = getServerNode();
        serverNode.start();
        dumpOutput();
    }

    /**
     * Stop a server
     */
    public void testStopServer() throws IOException {
        J2eeServerNode serverNode = getServerNode();
        serverNode.stop();
        new EventTool().waitNoEvent(2000);
        dumpOutput();
    }

    /**
     * Helper method to be used by subclasses to create new project according
     * to given parameters. Default server registered in the IDE will be used.
     *
     * @param name project or sample name
     * @param type project type
     * @param javaeeVersion server type, can be null
     * @return created project
     * @throws java.io.IOException
     */
    protected Project createProject(String name, ProjectType type, JavaEEVersion javaeeVersion) throws IOException {
        // project category & type selection step
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.treeCategories().setComparator(new Operator.DefaultStringComparator(true, true));
        npwo.lstProjects().setComparator(new Operator.DefaultStringComparator(true, true));
        if (ProjectType.SAMPLE.equals(type)) {
            npwo.selectCategory(type.getCategory() + "|" + getSamplesCategoryName());
            npwo.selectProject(name);
            name = getProjectName();
        } else {
            npwo.selectCategory(type.getCategory());
            npwo.selectProject(type.getProjectTypeName());
        }
        npwo.next();
        // project name & location selection step
        NewWebProjectNameLocationStepOperator op = new NewWebProjectNameLocationStepOperator();
        op.txtProjectName().setText(name);
        if (ProjectType.SAMPLE.equals(type)) {
            op.txtLocation().setText(getWorkDirPath());
        } else {
            op.txtProjectLocation().setText(getProjectsRootDir().getAbsolutePath());
        }
        LOGGER.log(Level.INFO, "Creating project in: {0}", op.txtProjectLocation().getText()); //NOI18N
        if (!(ProjectType.SAMPLE.equals(type) || ProjectType.JAVASE_APPLICATION.equals(type)
                || ProjectType.MAVEN_SE.equals(type))) {
            //second panel in web project wizards
            op.next();
            //choose server type and Java EE version
            JComboBoxOperator jcboServer = new JComboBoxOperator(op, type.getServerComboBoxIndex());
            JComboBoxOperator jcboVersion = new JComboBoxOperator(op, type.getServerVersionComboBoxIndex());
            if (type.isAntBasedProject()) {
                jcboServer.selectItem(REGISTERED_SERVER.toString());
                jcboVersion.selectItem(javaeeVersion.toString());
            } else {
                // cannot use display name for Maven project
                if (JavaEEVersion.JAVAEE8.equals(javaeeVersion)) {
                    jcboVersion.selectItem("1.8");
                } else if (JavaEEVersion.JAVAEE7.equals(javaeeVersion)) {
                    jcboVersion.selectItem("1.7");
                } else if (JavaEEVersion.JAVAEE6.equals(javaeeVersion)) {
                    jcboVersion.selectItem("1.6");
                } else if (JavaEEVersion.JAVAEE5.equals(javaeeVersion)) {
                    jcboVersion.selectItem("1.5");
                } else if (JavaEEVersion.J2EE14.equals(javaeeVersion)) {
                    jcboVersion.selectItem("1.4");
                }
            }
        }
        op.finish();
        if (ProjectType.SAMPLE.equals(type)) {
            checkMissingServer(name);
        }
        // wait project appear in projects view
        ProjectRootNode node;
        long oldTimeout = JemmyProperties.getCurrentTimeout("JTreeOperator.WaitNextNodeTimeout");
        // need to increase time to wait for project node
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 120000);
        try {
            node = ProjectsTabOperator.invoke().getProjectRootNode(name);
            // expand project to prevent #217775
            node.expand();
        } finally {
            JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", oldTimeout);
        }
        // wait classpath scanning finished
        waitScanFinished();
        // get a project instance to return
        Project p = ((org.openide.nodes.Node) node.getOpenideNode()).getLookup().lookup(Project.class);
        assertNotNull("Project instance has not been found", p);
        return p;
    }

    /**
     * Helper method to be used by subclasses to create new file of given
     * <code>fileType</code> from <i>Web Services</i> category
     *
     * @param p project where to create new file
     * @param fileType file type name from web services category
     */
    protected NewFileWizardOperator createNewWSFile(Project p, String fileType) {
        // Web Services
        String webServicesLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.client.wizard.Bundle", "Templates/WebServices");
        return createNewFile(p, webServicesLabel, fileType);
    }

    /**
     * Helper method to be used by subclasses to create new file of given
     * <code>fileType</code> from <code>fileCategory</code> category
     *
     * @param p project where to create new file
     * @param fileType file type name from web services category
     */
    protected NewFileWizardOperator createNewFile(Project p, String fileCategory, String fileType) {
        // file category & filetype selection step
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        new EventTool().waitNoEvent(500);
        nfwo.treeCategories().setComparator(new Operator.DefaultStringComparator(true, true));
        nfwo.lstFileTypes().setComparator(new Operator.DefaultStringComparator(true, true));
        nfwo.cboProject().selectItem(p.toString());
        nfwo.selectCategory(fileCategory);
        nfwo.selectFileType(fileType);
        nfwo.next();
        return nfwo;
    }

    /**
     * Deploy a project
     *
     * @param projectName name of the project to be deployed
     */
    protected void deployProject(String projectName) throws IOException {
        performProjectAction(projectName, "Clean");
        String deployProjectLabel = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.common.project.ui.Bundle", "LBL_RedeployAction_Name");
        //Deploy
        performProjectAction(projectName, deployProjectLabel);
    }

    /**
     * Run a project
     *
     * @param projectName name of the project to be run
     */
    protected void runProject(String projectName) throws IOException {
        performProjectAction(projectName, "Run");
    }

    /**
     * Undeploy a project
     *
     * @param projectName name of the project to be undeployed
     */
    protected void undeployProject(String projectName) throws IOException {
        assertServerRunning();
        J2eeServerNode serverNode = getServerNode();
        serverNode.expand();
        String applicationsLabel = "Applications";
        String webLabel = "Web Applications";
        Node appsNode = null;
        switch (getProjectType()) {
            case APPCLIENT:
            case SAMPLE:
            case WEB:
            case MAVEN_WEB:
                if (ServerType.TOMCAT.equals(REGISTERED_SERVER)) {
                    appsNode = new Node(serverNode, webLabel);
                } else if (ServerType.GLASSFISH.equals(REGISTERED_SERVER)) {
                    appsNode = new Node(serverNode, applicationsLabel);
                } else {
                    appsNode = new Node(serverNode, applicationsLabel + "|" + webLabel);
                }
                break;
            case EJB:
            case MAVEN_EJB:
                if (ServerType.GLASSFISH.equals(REGISTERED_SERVER)) {
                    appsNode = new Node(serverNode, applicationsLabel);
                } else {
                    appsNode = new Node(serverNode, applicationsLabel + "|" + "EJB Modules");
                }
                break;
        }
        appsNode.expand();
        appsNode.callPopup().pushMenu("Refresh");
        // needed for slower machines
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 30000); //NOI18N
        Node n = new Node(appsNode, projectName);
        n.callPopup().pushMenu("Undeploy");
        new EventTool().waitNoEvent(2000);
        appsNode.callPopup().pushMenu("Refresh");
        new EventTool().waitNoEvent(2000);
        dumpOutput();
    }

    /**
     * Save content of output tabs into test's working directory.
     * Might be useful for diagnosing possible test failures
     *
     * @throws java.io.IOException
     */
    protected void dumpOutput() throws IOException {
        OutputOperator oo = OutputOperator.invoke();
        oo.requestFocus();
        JTabbedPaneOperator jtpo = null;
        if (null != JTabbedPaneOperator.findJTabbedPane((Container) oo.getSource(), ComponentSearcher.getTrueChooser(""))) {
            jtpo = new JTabbedPaneOperator(oo);
        }
        if (jtpo != null) {
            for (int i = 0; i < jtpo.getTabCount(); i++) {
                String tabTitle = jtpo.getTitleAt(i);
                if (!jtpo.getComponentAt(i).isShowing()) {
                    continue;
                }
                jtpo.selectPage(i);
                OutputTabOperator oto;
                if (tabTitle.indexOf("<html>") < 0) { //NOI18N
                    oto = new OutputTabOperator(tabTitle.trim());
                } else {
                    oto = new OutputTabOperator(tabTitle.substring(9, 19).trim());
                }
                oto.requestFocus();
                writeToFile(oto.getText(),
                        new File(getWorkDir(), tabTitle.trim().replace(' ', '_') + ".txt")); //NOI18N
            }
        } else {
            OutputTabOperator oto = oo.getOutputTab(""); //NOI18N
            writeToFile(oto.getText(),
                    new File(getWorkDir(), "default_out.txt")); //NOI18N
        }
    }

    /**
     * Wait at most 120 seconds until dialog with title
     * <code>dialogTitle</code> is closed
     *
     * @param dialogTitle title of the dialog to be closed
     */
    protected void waitDialogClosed(String dialogTitle) {
        waitDialogClosed(dialogTitle, 120000);
    }

    /**
     * Wait until dialog with title
     * <code>dialogTitle</code> is closed
     *
     * @param dialogTitle title of the dialog to be closed
     * @param timeout timeout in miliseconds
     */
    protected void waitDialogClosed(String dialogTitle, int timeout) {
        NbDialogOperator dialogOper;
        try {
            dialogOper = new NbDialogOperator(dialogTitle);
        } catch (TimeoutExpiredException e) {
            // ignore when progress dialog was closed before we started to wait for it
            return;
        }
        dialogOper.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", timeout); //NOI18N
        dialogOper.waitClosed();
    }

    private void performProjectAction(String projectName, String actionName) throws IOException {
        ProjectsTabOperator pto = new ProjectsTabOperator();
        pto.pressMouse(); // to get focus, otherwise performing popup action won't work
        ProjectRootNode node = pto.getProjectRootNode(projectName);
        node.performPopupAction(actionName);
        OutputTabOperator oto = new OutputTabOperator(projectName);
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitStateTimeout", 600000); //NOI18N
        if (!getProjectType().isAntBasedProject()) {
            oto.waitText("Total time:"); //NOI18N
            // wait progress bar dismiss
            final Component comp = MainWindowOperator.getDefault().findSubComponent(new ComponentChooser() {

                @Override
                public boolean checkComponent(Component comp) {
                    return "NbProgressBar".equals(comp.getClass().getSimpleName());  //NOI18N
                }

                @Override
                public String getDescription() {
                    return "NbProgressBar component.";  //NOI18N
                }
            });
            if (comp != null) {
                new ComponentOperator(comp).waitComponentShowing(false);
            }
            dumpOutput();
            assertTrue("Build failed", oto.getText().indexOf("BUILD SUCCESS") > -1); //NOI18N
            assertTrue("Deploy failed", oto.getText().indexOf("[ERROR]") < 0); //NOI18N
        } else {
            //Ant projects
            oto.waitText("(total time: "); //NOI18N
            if (actionName.toLowerCase().contains("clean") && oto.getText().contains("Unable to delete")) {
                // repeat clean if file is locked
                new EventTool().waitNoEvent(2000);
                performProjectAction(projectName, actionName);
                return;
            }
            dumpOutput();
            assertTrue("Build failed", oto.getText().indexOf("BUILD SUCCESSFUL") > -1); //NOI18N
        }


    }

    private void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    private void setJavaEEversion(JavaEEVersion javaEEversion) {
        this.javaEEversion = javaEEversion;
    }

    private void setProjectType(ProjectType projectType) {
        this.projectType = projectType;
    }

    /**
     * Write given <code>text</code> into given <code>file</code>.
     *
     * @param text text to be written
     * @param file file to be created
     */
    private void writeToFile(String text, File file) {
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file));
            os.write(text.getBytes());
            os.flush();
        } catch (IOException ioe) {
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ioe) {
                }
            }
        }
    }

    protected void checkMissingServer(String project) {
        // check missing target server dialog is shown
        // "Open Project"
        String openProjectTitle = Bundle.getString("org.netbeans.modules.j2ee.common.ui.Bundle", "MSG_Broken_Server_Title");
        boolean needToSetServer = false;
        if (JDialogOperator.findJDialog(openProjectTitle, true, true) != null) {
            new NbDialogOperator(openProjectTitle).close();
            needToSetServer = true;
        }
        // Set as Main Project
         new ActionNoBlock("Run|Set Main Project|"+project, null).performMenu();

        if (needToSetServer) {
            // open project properties
            ProjectsTabOperator.invoke().getProjectRootNode(project).properties();
            // "Project Properties"
            String projectPropertiesTitle = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Customizer_Title");
            NbDialogOperator propertiesDialogOper = new NbDialogOperator(projectPropertiesTitle);
            // select "Run" category
            new Node(new JTreeOperator(propertiesDialogOper), "Run").select();
            // not display browser on run
//            String displayBrowserLabel = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_CustomizeRun_DisplayBrowser_JCheckBox");
//            new JCheckBoxOperator(propertiesDialogOper, displayBrowserLabel).setSelected(false);
            // set default server
            new JComboBoxOperator(propertiesDialogOper).setSelectedIndex(0);
            // confirm properties dialog
            propertiesDialogOper.ok();
        }
        // if setting default server, it scans server jars; otherwise it continues immediatelly
        waitScanFinished();
    }

    /**
     * Opens Services tab, go to specified Maven repository, call Update Index
     * and immediately cancel this action.
     * @param repositoryName 
     */
    private static void runAndCancelUpdateIndex(String repositoryName) {
        RuntimeTabOperator servicesOper = RuntimeTabOperator.invoke();
        Node node = new Node(servicesOper.getRootNode(), "Maven Repositories|" + repositoryName);
        new Action(null, "Update Index").perform(node);
        String lblCancelProgress = "Click to cancel process";
        long oldTimeout = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        try {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 10000);
            JButtonOperator btnCancel = new JButtonOperator((JButton) JButtonOperator.waitJComponent((Container) MainWindowOperator.getDefault().getSource(), lblCancelProgress, true, true));
            btnCancel.pushNoBlock();
            new NbDialogOperator("Cancel Running Task").yes();
        } catch (TimeoutExpiredException tee) {
            // ignore - already done in previous tests
        } finally {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", oldTimeout);
        }
    }
    
    protected File getProjectsRootDir() throws IOException {
        File f = getWorkDir();
        LOGGER.log(Level.FINE, "Working directory is set to: {0}", f);
        if (f != null) {
            f = f.getParentFile();
            if (f != null) {
                f = f.getParentFile();
            } else {
                return new File(System.getProperty("java.io.tmpdir"));
            }
        } else {
            return new File(System.getProperty("java.io.tmpdir"));
        }
        return new File(f, getJavaEEversion().name());
    }

    protected J2eeServerNode getServerNode() {
        switch (REGISTERED_SERVER) {
            case GLASSFISH:
                return getServerNode(Server.GLASSFISH);
            case JBOSS:
                return getServerNode(Server.JBOSS);
            case TOMCAT:
                return getServerNode(Server.TOMCAT);
        }
        return getServerNode(Server.ANY);
    }
}
