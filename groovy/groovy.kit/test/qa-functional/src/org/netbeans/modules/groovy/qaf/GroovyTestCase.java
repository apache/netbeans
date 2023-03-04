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

package org.netbeans.modules.groovy.qaf;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewJavaProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.BuildJavaProjectAction;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.modules.project.ui.test.ProjectSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lukas
 */
public abstract class GroovyTestCase extends JellyTestCase {

    private static final Logger LOGGER = Logger.getLogger(GroovyTestCase.class.getName());
    private Project project;
    private String projectName;
    private ProjectType projectType;

    /**
     * Enum type to hold project specific settings (like ie. project category
     * label, project template name, etc.)
     */
    protected enum ProjectType {

        JAVASE_APPLICATION,
        GROOVY,
        SAMPLE;

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
                case GROOVY:
                    //Groovy
                    //XXX - where are you hidden?
                    return "Groovy"; //NOI18N
                case SAMPLE:
                    //Samples
                    return Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Templates/Project/Samples");
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
                    //Java Application
                    return Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "Templates/Project/Standard/emptyJ2SE.xml");
                case GROOVY:
                    //Grails Application
                    return Bundle.getStringTrimmed("org.netbeans.modules.groovy.grailsproject.ui.wizards.Bundle", "Templates/Project/Groovy/emptyGrails.xml");
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

    }

    public GroovyTestCase(String name) {
        super(name);
        setProjectName(getProjectName());
        setProjectType(getProjectType());
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
        if (!ProjectType.SAMPLE.equals(getProjectType())) {
            System.out.println("########  TestCase: " + getName() + "  #######"); //NOI18N
            File projectRoot = new File(getDataDir(), "projects/" + getProjectName()); //NOI18N
            if (projectRoot.exists()) {
                project = (Project) ProjectSupport.openProject(new File(getDataDir(), "projects/" + getProjectName()));
            } else {
                projectRoot = new File(getProjectsRootDir(), projectName);
                LOGGER.info("Using project in: " + projectRoot.getAbsolutePath()); //NOI18N
                if (!projectRoot.exists()) {
                    project = createProject(projectName);
                } else {
                    openProjects(projectRoot.getAbsolutePath());
                    FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(projectRoot));
                    assertNotNull("FO cannot be null", fo); //NOI18N
                    project = ProjectManager.getDefault().findProject(fo);
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
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            //ignore
        }
    }

    /**
     * Get the name of the project to be used by test case
     *
     * @return name of the project
     */
    protected abstract String getProjectName();

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
     * Project type set for test case, ProjectType.JAVASE_APPLICATION is used by default
     * Override this method to use different ProjectType
     *
     * @return ProjectType set for test case
     */
    protected ProjectType getProjectType() {
        return ProjectType.JAVASE_APPLICATION;
    }

    /**
     * Get the name of the sample project's category (ie. Web Services)
     *
     * @return name of the project
     */
    protected String getSamplesCategoryName() {
        return ""; //NOI18N
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
    protected Project createProject(String name) throws IOException {
        // project category & type selection step
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.treeCategories().setComparator(new Operator.DefaultStringComparator(true, true));
        npwo.lstProjects().setComparator(new Operator.DefaultStringComparator(true, true));
        if (ProjectType.SAMPLE.equals(getProjectType())) {
            npwo.selectCategory(getProjectType().getCategory() + "|" + getSamplesCategoryName()); //NOI18N
            npwo.selectProject(name);
            name = getProjectName();
        } else {
            npwo.selectCategory(getProjectType().getCategory());
            npwo.selectProject(getProjectType().getProjectTypeName());
        }
        npwo.next();
        // project name & location selection step
        NewJavaProjectNameLocationStepOperator op = new NewJavaProjectNameLocationStepOperator();
        op.txtProjectName().setText(name);
        if (ProjectType.SAMPLE.equals(getProjectType())) {
            op.txtLocation().setText(getWorkDirPath());
        } else {
            File projectLocation = null;
            projectLocation = getProjectsRootDir();
            op.txtProjectLocation().setText(projectLocation.getAbsolutePath());
        }
        LOGGER.info("Creating project in: " + op.txtProjectLocation().getText()); //NOI18N
        op.finish();
        // Opening Projects
        String openingProjectsTitle = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "LBL_Opening_Projects_Progress");
        waitDialogClosed(openingProjectsTitle);
        // wait project appear in projects view
        ProjectRootNode node = ProjectsTabOperator.invoke().getProjectRootNode(name);
        // wait classpath scanning finished
        org.netbeans.junit.ide.ProjectSupport.waitScanFinished();
        // get a project instance to return
        Project p = ((org.openide.nodes.Node) node.getOpenideNode()).getLookup().lookup(Project.class);
        assertNotNull("Project instance has not been found", p); //NOI18N
        return p;
    }

    /**
     * Helper method to be used by subclasses to create new file of given
     * <code>fileType</code> from <i>Groovy</i> category
     *
     * @param p project where to create new file
     * @param fileType file type name from web services category
     */
    protected void createNewGroovyFile(Project p, String fileType) {
        // Groovy
        //XXX - where is it?
        String groovyLabel = "Groovy"; //NOI18N
        createNewFile(p, groovyLabel, fileType);
    }

    /**
     * Helper method to be used by subclasses to create new file of given
     * <code>fileType</code> from <code>fileCategory</code> category
     *
     * @param p project where to create new file
     * @param fileType file type name from web services category
     */
    protected void createNewFile(Project p, String fileCategory, String fileType) {
        // file category & filetype selection step
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        new EventTool().waitNoEvent(500);
        nfwo.treeCategories().setComparator(new Operator.DefaultStringComparator(true, true));
        nfwo.lstFileTypes().setComparator(new Operator.DefaultStringComparator(true, true));
        nfwo.cboProject().selectItem(p.toString());
        nfwo.selectCategory(fileCategory);
        nfwo.selectFileType(fileType);
        nfwo.next();
    }

    /**
     * Wait until dialog with title <code>dialogTitle</code> is closed
     *
     * @param dialogTitle title of the dialog to be closed
     */
    protected void waitDialogClosed(String dialogTitle) {
        try {
            // wait at most 60 second until progress dialog dismiss
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitStateTimeout", 60000); //NOI18N
            new NbDialogOperator(dialogTitle).waitClosed();
        } catch (TimeoutExpiredException e) {
            // ignore when progress dialog was closed before we started to wait for it
        }
    }

    protected File getProjectsRootDir() throws IOException {
        File f = getWorkDir();
        LOGGER.fine("Working directory is set to: " + f.getAbsolutePath()); //NOI18N
        if (f != null) {
            f = f.getParentFile();
            if (f != null) {
                f = f.getParentFile();
            } else {
                return new File(System.getProperty("java.io.tmpdir")); //NOI18N
            }
        } else {
            return new File(System.getProperty("java.io.tmpdir")); //NOI18N
        }
        return f;
    }

    protected void buildProject() {
        OutputOperator oo = OutputOperator.invoke();
        new BuildJavaProjectAction().performPopup(getProjectRootNode());
        oo.getOutputTab(getProjectName()).waitText("BUILD SUCCESSFUL"); //NOI18N
    }

    private void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    private void setProjectType(ProjectType projectType) {
        this.projectType = projectType;
    }

}
