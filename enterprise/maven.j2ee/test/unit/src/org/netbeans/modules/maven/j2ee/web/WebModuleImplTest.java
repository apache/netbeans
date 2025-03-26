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
package org.netbeans.modules.maven.j2ee.web;

import java.io.IOException;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.maven.j2ee.JavaEEMavenTestBase;
import org.netbeans.modules.maven.j2ee.PomBuilder;
import org.netbeans.modules.maven.j2ee.PomBuilder.PomPlugin;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Janicek
 */
public class WebModuleImplTest extends JavaEEMavenTestBase {

    private PomBuilder builder;

    private WebModuleProviderImpl provider;
    private WebModuleImpl webModule;


    public WebModuleImplTest(String name) {
        super(name);
        builder = new PomBuilder();
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        builder.clear();
    }


    public void testCreateWebInf() throws IOException {
        setUpDefaultPom();
        assertNull(webModule.getWebInf());
        assertNotNull(webModule.createWebInf());
        assertNotNull(webModule.getWebInf());
    }

    public void testGetDocumentBase() throws IOException {
        setUpDefaultPom();
        assertEquals(true, webModule.getDocumentBase().getName().endsWith("webapp")); //NOI18N
    }

    public void testGetArchive_noExistingArchive() throws IOException {
        builder.appendDefaultTestValues();
        builder.appendPlugin(new PomPlugin("org.apache.maven.plugins", "maven-war-plugin", "2.1.1")); //NOI18N
        createProject(builder);

        assertNull(webModule.getArchive());
    }

    public void testGetArchive_archiveExists() throws IOException {
        String artifactID = "projectArtifactID"; //NOI18N
        String archiveType = "war"; //NOI18N
        String version = "12345"; //NOI18N

        builder.appendPomContent("4.0.0", "group", artifactID, archiveType, version); //NOI18N
        //builder.appendPlugin(new PomPlugin("org.apache.maven.plugins", "maven-war-plugin", "2.1.1")); //NOI18N
        createProject(builder);

        FileObject targetDir = project.getProjectDirectory().createFolder("target"); //NOI18N
        FileObject warFile = FileUtil.createData(targetDir, artifactID + "-" + version + "." + archiveType); //NOI18N
        FileObject archiveFile = webModule.getArchive();

        assertNotNull(archiveFile);
        assertEquals(warFile, archiveFile);
        assertEquals(archiveType, archiveFile.getExt());
        assertEquals(true, archiveFile.getName().startsWith(artifactID));
        assertEquals(true, archiveFile.getName().contains(version.subSequence(0, version.length())));
    }

    public void testGetJ2eeProfile_javaEE5FullSpecification() throws IOException {
        checkJ2eeProfile(Profile.JAVA_EE_5, "javaee", "javaee-api", "5.0"); //NOI18N
    }

    public void testGetJ2eeProfile_warProject_javaEE6FullSpecification() throws IOException {
        checkJ2eeProfile(Profile.JAVA_EE_6_WEB, "javax", "javaee-api", "6.0"); //NOI18N
    }

    public void testGetJ2eeProfile_javaEE6WebSpecification() throws IOException {
        checkJ2eeProfile(Profile.JAVA_EE_6_WEB, "javax", "javaee-web-api", "6.0"); //NOI18N
    }

    public void testGetJ2eeProfile_warProject_javaEE7FullSpecification() throws IOException {
        checkJ2eeProfile(Profile.JAVA_EE_7_WEB, "javax", "javaee-api", "7.0"); //NOI18N
    }

    public void testGetJ2eeProfile_javaEE7WebSpecification() throws IOException {
        checkJ2eeProfile(Profile.JAVA_EE_7_WEB, "javax", "javaee-web-api", "7.0"); //NOI18N
    }

    public void testGetJ2eeProfile_warProject_javaEE8FullSpecification() throws IOException {
        checkJ2eeProfile(Profile.JAVA_EE_8_WEB, "javax", "javaee-api", "8.0"); //NOI18N
    }

    public void testGetJ2eeProfile_javaEE8WebSpecification() throws IOException {
        checkJ2eeProfile(Profile.JAVA_EE_8_WEB, "javax", "javaee-web-api", "8.0"); //NOI18N
    }
    
    public void testGetJ2eeProfile_warProject_jakartaEE8FullSpecification() throws IOException {
        checkJ2eeProfile(Profile.JAKARTA_EE_8_WEB, "jakarta.platform", "jakarta.jakartaee-api", "8.0.0"); //NOI18N
    }

    public void testGetJ2eeProfile_jakartaEE8WebSpecification() throws IOException {
        checkJ2eeProfile(Profile.JAKARTA_EE_8_WEB, "jakarta.platform", "jakarta.jakartaee-web-api", "8.0.0"); //NOI18N
    }
    
    public void testGetJ2eeProfile_warProject_jakartaEE9FullSpecification() throws IOException {
        checkJ2eeProfile(Profile.JAKARTA_EE_9_WEB, "jakarta.platform", "jakarta.jakartaee-api", "9.0.0"); //NOI18N
    }

    public void testGetJ2eeProfile_jakartaEE9WebSpecification() throws IOException {
        checkJ2eeProfile(Profile.JAKARTA_EE_9_WEB, "jakarta.platform", "jakarta.jakartaee-web-api", "9.0.0"); //NOI18N
    }
    
    public void testGetJ2eeProfile_warProject_jakartaEE91FullSpecification() throws IOException {
        checkJ2eeProfile(Profile.JAKARTA_EE_9_1_WEB, "jakarta.platform", "jakarta.jakartaee-api", "9.1.0"); //NOI18N
    }

    public void testGetJ2eeProfile_jakartaEE91WebSpecification() throws IOException {
        checkJ2eeProfile(Profile.JAKARTA_EE_9_1_WEB, "jakarta.platform", "jakarta.jakartaee-web-api", "9.1.0"); //NOI18N
    }

    public void testGetJ2eeProfile_warProject_jakartaEE10FullSpecification() throws IOException {
        checkJ2eeProfile(Profile.JAKARTA_EE_10_WEB, "jakarta.platform", "jakarta.jakartaee-api", "10.0.0"); //NOI18N
    }

    public void testGetJ2eeProfile_jakartaEE10WebSpecification() throws IOException {
        checkJ2eeProfile(Profile.JAKARTA_EE_10_WEB, "jakarta.platform", "jakarta.jakartaee-web-api", "10.0.0"); //NOI18N
    }
    
    public void testGetJ2eeProfile_warProject_jakartaEE11FullSpecification() throws IOException {
        checkJ2eeProfile(Profile.JAKARTA_EE_11_WEB, "jakarta.platform", "jakarta.jakartaee-api", "11.0.0-M1"); //NOI18N
    }

    public void testGetJ2eeProfile_jakartaEE11WebSpecification() throws IOException {
        checkJ2eeProfile(Profile.JAKARTA_EE_11_WEB, "jakarta.platform", "jakarta.jakartaee-web-api", "11.0.0-M1"); //NOI18N
    }

    public void testGetJ2eeProfile_javaEE5Full_glassfish() throws IOException {
        checkJ2eeProfile(Profile.JAVA_EE_5, "org.glassfish.main.extras", "glassfish-embedded-all", "2"); //NOI18N
    }

    public void testGetJ2eeProfile_javaEE5Web_glassfish() throws IOException {
        checkJ2eeProfile(Profile.JAVA_EE_5, "org.glassfish.main.extras", "glassfish-embedded-web", "2"); //NOI18N
    }

    public void testGetJ2eeProfile_warProject_javaEE6Full_glassfish() throws IOException {
        checkJ2eeProfile(Profile.JAVA_EE_6_WEB, "org.glassfish.main.extras", "glassfish-embedded-all", "3.1.1"); //NOI18N
    }

    public void testGetJ2eeProfile_javaEE6Web_glassfish() throws IOException {
        checkJ2eeProfile(Profile.JAVA_EE_6_WEB, "org.glassfish.main.extras", "glassfish-embedded-web", "3.1.2.2"); //NOI18N
    }

    public void testGetJ2eeProfile_warProject_javaEE7Full_glassfish() throws IOException {
        checkJ2eeProfile(Profile.JAVA_EE_7_WEB, "org.glassfish.main.extras", "glassfish-embedded-all", "4.0"); //NOI18N
    }

    public void testGetJ2eeProfile_javaEE7Web_glassfish() throws IOException {
        checkJ2eeProfile(Profile.JAVA_EE_7_WEB, "org.glassfish.main.extras", "glassfish-embedded-web", "4.0.1"); //NOI18N
    }

    public void testGetJ2eeProfile_warProject_javaEE8Full_glassfish() throws IOException {
        checkJ2eeProfile(Profile.JAVA_EE_8_WEB, "org.glassfish.main.extras", "glassfish-embedded-all", "5.0"); //NOI18N
    }

    public void testGetJ2eeProfile_javaEE8Web_glassfish() throws IOException {
        checkJ2eeProfile(Profile.JAVA_EE_8_WEB, "org.glassfish.main.extras", "glassfish-embedded-web", "5.0"); //NOI18N
    }
    
    public void testGetJ2eeProfile_warProject_jakartaEE8Full_glassfish() throws IOException {
        checkJ2eeProfile(Profile.JAKARTA_EE_8_WEB, "org.glassfish.main.extras", "glassfish-embedded-all", "5.1.0"); //NOI18N
    }

    public void testGetJ2eeProfile_jakartaEE8Web_glassfish() throws IOException {
        checkJ2eeProfile(Profile.JAKARTA_EE_8_WEB, "org.glassfish.main.extras", "glassfish-embedded-web", "5.1.0"); //NOI18N
    }
    
    public void testGetJ2eeProfile_warProject_jakartaEE9Full_glassfish() throws IOException {
        checkJ2eeProfile(Profile.JAKARTA_EE_9_WEB, "org.glassfish.main.extras", "glassfish-embedded-all", "6.0.0"); //NOI18N
    }

    public void testGetJ2eeProfile_jakartaEE9Web_glassfish() throws IOException {
        checkJ2eeProfile(Profile.JAKARTA_EE_9_WEB, "org.glassfish.main.extras", "glassfish-embedded-web", "6.0.0"); //NOI18N
    }
    
    public void testGetJ2eeProfile_warProject_jakartaEE91Full_glassfish() throws IOException {
        checkJ2eeProfile(Profile.JAKARTA_EE_9_1_WEB, "org.glassfish.main.extras", "glassfish-embedded-all", "6.2.5"); //NOI18N
    }

    public void testGetJ2eeProfile_jakartaEE91Web_glassfish() throws IOException {
        checkJ2eeProfile(Profile.JAKARTA_EE_9_1_WEB, "org.glassfish.main.extras", "glassfish-embedded-web", "6.2.5"); //NOI18N
    }
    
    public void testGetJ2eeProfile_warProject_jakartaEE10Full_glassfish() throws IOException {
        checkJ2eeProfile(Profile.JAKARTA_EE_10_WEB, "org.glassfish.main.extras", "glassfish-embedded-all", "7.0.11"); //NOI18N
    }

    public void testGetJ2eeProfile_jakartaEE10Web_glassfish() throws IOException {
        checkJ2eeProfile(Profile.JAKARTA_EE_10_WEB, "org.glassfish.main.extras", "glassfish-embedded-web", "7.0.11"); //NOI18N
    }
    
    public void testGetJ2eeProfile_warProject_jakartaEE11Full_glassfish() throws IOException {
        checkJ2eeProfile(Profile.JAKARTA_EE_11_WEB, "org.glassfish.main.extras", "glassfish-embedded-all", "8.0.0-M1"); //NOI18N
    }

    public void testGetJ2eeProfile_jakartaEE11Web_glassfish() throws IOException {
        checkJ2eeProfile(Profile.JAKARTA_EE_11_WEB, "org.glassfish.main.extras", "glassfish-embedded-web", "8.0.0-M1"); //NOI18N
    }

    public void testGetJ2eeProfile_javaEE5_weblogic() throws IOException {
        checkJ2eeProfile(Profile.JAVA_EE_5, "weblogic", "weblogic", "10.3.6"); //NOI18N
    }

    public void testGetJ2eeProfile_warProject_javaEE6Full_weblogic() throws IOException {
        checkJ2eeProfile(Profile.JAVA_EE_6_WEB, "weblogic", "weblogic", "12.1.1"); //NOI18N
    }

    public void testGetJ2eeProfile_javaEE5_jboss() throws IOException {
        checkJ2eeProfile(Profile.JAVA_EE_5, "org.jboss.spec", "jboss-javaee-5.0", "1.0.0.GA"); //NOI18N
    }

    public void testGetJ2eeProfile_javaEE5Full_jboss() throws IOException {
        checkJ2eeProfile(Profile.JAVA_EE_5, "org.jboss.spec", "jboss-javaee-all-5.0", "	1.0.0.GA"); //NOI18N
    }

    public void testGetJ2eeProfile_warProject_javaEE6_jboss() throws IOException {
        checkJ2eeProfile(Profile.JAVA_EE_6_WEB, "org.jboss.spec", "jboss-javaee-6.0", "3.0.2.Final"); //NOI18N
    }

    public void testGetJ2eeProfile_warProject_javaEE6Full_jboss() throws IOException {
        checkJ2eeProfile(Profile.JAVA_EE_6_WEB, "org.jboss.spec", "jboss-javaee-all-6.0", "3.0.0.Final"); //NOI18N
    }

    public void testGetJ2eeProfile_javaEE6Web_jboss() throws IOException {
        checkJ2eeProfile(Profile.JAVA_EE_6_WEB, "org.jboss.spec", "jboss-javaee-web-6.0", "2.0.0.Final"); //NOI18N
    }

    private void checkJ2eeProfile(Profile profile, String groupID, String artifactID, String version) throws IOException {
        builder.appendPomContent("war"); //NOI18N
        builder.appendDependency(new PomBuilder.PomDependency(groupID, artifactID, version));
        createProject(builder);

        assertEquals(profile, webModule.getJ2eeProfile());
    }

    /*
    // We need to find a way how to set server properly first
    public void testSetContextPath() throws IOException {
        setUpDefaultPom();
        MavenProjectSupport.setServerID(project, "gfv3ee6");
        FileObject webXml = JavaEEMavenTestSupport.createWebXml(project.getProjectDirectory());
        String contextPath = "whatever";

        assertEquals(-1, webXml.asText().indexOf(contextPath));
        webModule.setContextPath(contextPath);
        assertEquals(true, webXml.asText().indexOf(contextPath) > 0);
    }
     */

    private void setUpDefaultPom() throws IOException {
        builder.appendDefaultTestValues();
        createProject(builder);
    }

    private void createProject(PomBuilder builder) throws IOException {
        project = createMavenWebProject(builder.buildPom());
        assertNotNull(project);
        provider = project.getLookup().lookup(WebModuleProviderImpl.class);
        assertNotNull(provider);
        webModule = provider.getModuleImpl();
        assertNotNull(webModule);
    }
}
