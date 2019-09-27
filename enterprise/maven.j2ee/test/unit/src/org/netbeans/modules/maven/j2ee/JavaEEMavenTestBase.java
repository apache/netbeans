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
package org.netbeans.modules.maven.j2ee;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

/**
 * <p>
 * Base class for Java EE maven tests. Encapsulate basic stuff needed in every test case such as creating new project
 * in a proper folder, setting logger and so on. Allows to easily create projects of different types (Ejb, War, EA, 
 * Package) and offers various methods for creating/updating pom.xml, nb-configuration.xml etc.
 * </p>
 * 
 * <p>
 * By default Web project is created for each subclass test. For creating different projects just use for example
 * <code>project = createMavenEjbProject(getWorkDir());</code> and so
 * </p>
 * 
 * @author Martin Janicek
 */
public abstract class JavaEEMavenTestBase extends NbTestCase {
    
    public  final String WEB_INF = "WEB-INF"; //NOI18N
    public  final String WEB_XML = "web.xml"; //NOI18N
    
    public  final String WEBLOGIC = "WebLogic"; //NOI18N
    public  final String GLASSFISH = "gfv5ee8"; //NOI18N
    public  final String TOMCAT = "Tomcat"; //NOI18N
    public  final String JBOSS = "JBoss"; //NOI18N
    
    private  final StringBuilder sb = new StringBuilder();
    protected Project project;
    
    
    protected JavaEEMavenTestBase(String name) {
        super(name);
    }
    
    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    @Override
    protected String logRoot() {
        return "org.netbeans.modules.maven.j2ee"; //NOI18N
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        project = createMavenWebProject();
    }
    
    /**
     * <p>Creates default Maven Web project structure which could be used for tests
     * In file system it seems like this:</p>
     * 
     * <pre>
     * |-- pom.xml
     * |
     * `-- src
     *     `-- main
     *         |-- java
     *         |-- resources
     *         `-- webapp
     * </pre>
     * 
     * For creation of additional files like nb-configuration.xml see MavenTestSupport methods.
     * 
     * @param projectDir root directory of the project
     * @return created project with structure described above
     */
    protected Project createMavenWebProject() {
        try {
            return createMavenWebProject(FileUtil.toFileObject(getWorkDir()));
        } catch (IOException ex) {
            return null;
        }
    }
    
    protected Project createMavenWebProject(String pom) {
        try {
            return createMavenWebProject(FileUtil.toFileObject(getWorkDir()), pom);
        } catch (IOException ex) {
            return null;
        }
    }
    
    protected Project createMavenWebProject(FileObject projectDir) {
        return createMavenWebProject(projectDir, null);
    }
    
    protected Project createMavenWebProject(FileObject projectDir, String pom) {
        try {
            FileObject src = FileUtil.createFolder(projectDir, "src"); //NOI18N
            FileObject main = FileUtil.createFolder(src, "main"); //NOI18N
            FileObject java = FileUtil.createFolder(main, "java"); //NOI18N
            FileObject resources = FileUtil.createFolder(main, "resources"); //NOI18N
            FileObject webapp = FileUtil.createFolder(main, "webapp"); //NOI18N

            return createProject(projectDir, pom);
        } catch (IOException ex) {
            return null;
        }
    }
    
    /**
     * <p>Creates default Maven EJB project structure which could be used for tests
     * In file system it seems like this:</p>
     * 
     * <pre>
     * |-- pom.xml
     * |
     * `-- src
     *     `-- main
     *         |-- java
     *         `-- resources
     * </pre>
     * 
     * For creation of additional files like nb-configuration.xml see MavenTestSupport methods.
     * 
     * @param projectDir root directory of the project
     * @return created project with structure described above
     */
    protected Project createMavenEjbProject() {
        try {
            return createMavenEjbProject(FileUtil.toFileObject(getWorkDir()));
        } catch (IOException ex) {
            return null;
        }
    }
    
    protected Project createMavenEjbProject(FileObject projectDir) {
        try {
            FileObject src = FileUtil.createFolder(projectDir, "src"); //NOI18N
            FileObject main = FileUtil.createFolder(src, "main"); //NOI18N
            FileObject java = FileUtil.createFolder(main, "java"); //NOI18N
            FileObject resources = FileUtil.createFolder(main, "resources"); //NOI18N

            PomBuilder pomBuilder = new PomBuilder();
            pomBuilder.appendPomContent(NbMavenProject.TYPE_EJB);
            
            return createProject(projectDir, pomBuilder.buildPom());
        } catch (IOException ex) {
            return null;
        }
    }
    
    /**
     * <p>Creates default Maven Ear project structure which could be used for tests
     * In file system it seems like this:</p>
     * 
     * <pre>
     * |-- pom.xml
     * |
     * `-- src
     *     `-- main
     *         `-- application
     * </pre>
     * 
     * For creation of additional files like nb-configuration.xml see MavenTestSupport methods.
     * 
     * @param projectDir root directory of the project
     * @return created project with structure described above
     */
    protected Project createMavenEarProject() {
        try {
            return createMavenEarProject(FileUtil.toFileObject(getWorkDir()));
        } catch (IOException ex) {
            return null;
        }
    }
    
    protected Project createMavenEarProject(FileObject projectDir) {
        try {
            FileObject src = FileUtil.createFolder(projectDir, "src"); //NOI18N
            FileObject main = FileUtil.createFolder(src, "main"); //NOI18N
            FileObject application = FileUtil.createFolder(main, "application"); //NOI18N
            
            PomBuilder pomBuilder = new PomBuilder();
            pomBuilder.appendPomContent(NbMavenProject.TYPE_EAR);

            return createProject(projectDir, pomBuilder.buildPom());
        } catch (IOException ex) {
            return null;
        }
    }
    
    /**
     * <p>Creates default Maven Ear project structure which could be used for tests
     * In file system it seems like this:</p>
     * 
     * <pre>
     * |-- pom.xml
     * |
     * |-- projectName-ear
     * |-- projectName-ejb
     * `-- projectName-web
     * </pre>
     * 
     * Each of these subdirectories contains default files/folders as well - see 
     * {@link #createMavenEarProject(File projectDir)}, {@link #createMavenEjbProject(File projectDir)},
     * {@link #createMavenWebProject(File projectDir)}
     * 
     * @param projectDir root directory of the project
     * @return created project with structure described above
     */
    protected Project createMavenEAProject(File projectDir) {
        return createMavenEAProject(FileUtil.toFileObject(projectDir));
    }
    
    protected Project createMavenEAProject(FileObject projectDir) {
        try {
            String name = projectDir.getName();
            FileObject ear = FileUtil.createFolder(projectDir, name + "-ear"); //NOI18N
            FileObject ejb = FileUtil.createFolder(projectDir, name + "-ejb"); //NOI18N
            FileObject web = FileUtil.createFolder(projectDir, name + "-web"); //NOI18N

            createMavenEarProject(ear);
            createMavenEjbProject(ejb);
            createMavenWebProject(web);

            return createProject(projectDir);
        } catch (IOException ex) {
            return null;
        }
    }
    
    /**
     * <p>Creates default Maven OSGI project structure which could be used for tests
     * In file system it seems like this:</p>
     * 
     * <pre>
     * |-- pom.xml
     * |
     * `-- src
     *     `-- main
     *         |-- assembly
     *         |-- java
     *         `-- resources
     * </pre>
     * 
     * For creation of additional files like nb-configuration.xml see MavenTestSupport methods.
     * 
     * @param projectDir root directory of the project
     * @return created project with structure described above
     */
    protected Project createMavenOSGIProject() {
        try {
            return createMavenOSGIProject(FileUtil.toFileObject(getWorkDir()));
        } catch (IOException ex) {
            return null;
        }
    }
    
    protected Project createMavenOSGIProject(FileObject projectDir) {
        try {
            FileObject src = FileUtil.createFolder(projectDir, "src"); //NOI18N
            FileObject main = FileUtil.createFolder(src, "main"); //NOI18N
            FileObject java = FileUtil.createFolder(main, "java"); //NOI18N
            FileObject resources = FileUtil.createFolder(main, "resources"); //NOI18N
            FileObject webapp = FileUtil.createFolder(main, "webapp"); //NOI18N

            PomBuilder pomBuilder = new PomBuilder();
            pomBuilder.appendPomContent(NbMavenProject.TYPE_OSGI);
            
            return createProject(projectDir, pomBuilder.buildPom());
        } catch (IOException ex) {
            return null;
        }
    }
    
    private Project createProject(FileObject projectDir) throws IOException {
        return createProject(projectDir, null);
    }
    
    private Project createProject(FileObject projectDir, String pom) throws IOException {
        if (pom != null) {
            createPom(projectDir, pom);
        } else {
            createPom(projectDir);
        }
        
        Project createdProject = ProjectManager.getDefault().findProject(projectDir);

        return createdProject;
    }
    
    protected FileObject createPom(Project project) throws IOException {
        return TestFileUtils.writeFile(project.getProjectDirectory(), "pom.xml", createDefaultPom()); //NOI18N
    }
 
    protected FileObject createPom(Project project, String pomContent) throws IOException {
        return TestFileUtils.writeFile(project.getProjectDirectory(), "pom.xml", pomContent); //NOI18N
    }
    
    protected FileObject createPom(FileObject workDir) throws IOException {
        return TestFileUtils.writeFile(workDir, "pom.xml", createDefaultPom()); //NOI18N
    }
    
    protected FileObject createPom(FileObject workDir, String pomContent) throws IOException {
        return TestFileUtils.writeFile(workDir, "pom.xml", pomContent); //NOI18N
    }
    
    protected String createSimplePom(String modelVersion, String groupID, String artifactID, String packaging, String version) {
        PomBuilder builder = new PomBuilder();
        
        builder.appendPomContent(modelVersion, groupID, artifactID, packaging, version);
        return builder.buildPom();
    }
    
    private String createDefaultPom() {
        PomBuilder builder = new PomBuilder();
        
        builder.appendDefaultTestValues();
        return builder.buildPom();
    }

    protected FileObject createWebXml(FileObject projectDir) throws IOException {
        return DDHelper.createWebXml(Profile.JAVA_EE_6_WEB, getWebInf(projectDir));
    }
    
    private FileObject getWebInf(FileObject projectDir) throws IOException {
        return FileUtil.createFolder(projectDir, "src/main/webapp/WEB-INF"); //NOI18N
    }
    
    protected FileObject createNbActions(Project project) throws IOException {
        return TestFileUtils.writeFile(project.getProjectDirectory(), "nbactions.xml", createNbActionContent()); //NOI18N
    }
    
    // TODO should be parametrizeable
    private String createNbActionContent() {
        return "<actions>" +
                    "<action>" + 
                        "<actionName>run</actionName>" +
                        "<goals>" +
                            "<goal>package</goal>" +
                        "</goals>" +
                    "</action>" +
                "</actions>"; //NOI18N
    }
    
    protected FileObject createNbConfiguration(Project project) throws IOException {
        return TestFileUtils.writeFile(project.getProjectDirectory(), "nb-configuration.xml", createNbConfigContent()); //NOI18N
    }
    
    private String createNbConfigContent() {
        return createNbConfigContent(null);
    }
    
    private String createNbConfigContent(String compileOnSave) {
        sb.delete(0, sb.length());
        sb.append("<project-shared-configuration>"); //NOI18N
        sb.append("    <properties xmlns=\"http://www.netbeans.org/ns/maven-properties-data/1\">"); //NOI18N
        
        if (compileOnSave != null) {
            sb.append("<netbeans.compile.on.save>"); //NOI18N
            sb.append(compileOnSave);
            sb.append("</netbeans.compile.on.save>"); //NOI18N
        }
        
        sb.append("    </properties>"); //NOI18N
        sb.append("</project-shared-configuration>"); //NOI18N
        
        return sb.toString();
    }
    
    protected boolean isWebDDpresent(FileObject projectDir) {
        FileObject src = projectDir.getFileObject("src"); //NOI18N
        FileObject main = src.getFileObject("main"); //NOI18N
        FileObject webapp = main.getFileObject("webapp"); //NOI18N
        FileObject webInf = webapp.getFileObject(WEB_INF);
        
        if (webInf == null) {
            return false;
        }
        
        return webInf.getFileObject(WEB_XML) != null ? true : false;
    }
    
    protected boolean isWebDDpresent(Project project) {
        return isWebDDpresent(project.getProjectDirectory());
    }
}
