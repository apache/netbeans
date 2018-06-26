/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.j2ee.web;

import java.io.IOException;
import static junit.framework.Assert.assertEquals;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.netbeans.modules.maven.j2ee.JavaEEMavenTestBase;
import org.netbeans.modules.maven.j2ee.PomBuilder;
import org.netbeans.modules.maven.j2ee.PomBuilder.PomPlugin;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
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
