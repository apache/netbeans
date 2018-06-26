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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.ejbjarproject;

import java.io.File;
import static junit.framework.Assert.assertEquals;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbjarproject.test.TestBase;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class EjbJarProviderTest extends TestBase {
    
    private static final String EJBJAR_XML = "ejb-jar.xml";
    
    private Project project;
    private AntProjectHelper helper;
    
    public EjbJarProviderTest(String testName) {
        super(testName);
    }
    
    /**
     * Tests that the deployment descriptor and beans are returned correctly.
     */
    public void testPathsAreReturned() throws Exception {
        File f = new File(getDataDir().getAbsolutePath(), "projects/EJBModule1");
        project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        // XXX should not cast a Project
        helper = ((EjbJarProject)project).getAntProjectHelper();
        
        // first ensure meta.inf exists
        String metaInf = helper.getStandardPropertyEvaluator().getProperty("meta.inf");
        assertTrue(metaInf.endsWith("conf"));
        FileObject metaInfFO =helper.resolveFileObject(metaInf);
        assertNotNull(metaInfFO);
        
        // ensuer ejb-jar.xml and webservices.xml exist
        FileObject ejbJarXmlFO = metaInfFO.getFileObject(EJBJAR_XML);
        assertNotNull(ejbJarXmlFO);
        assertNotNull(metaInfFO.getFileObject("webservices.xml"));

        // ensure deployment descriptor files are returned
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        assertEquals(ejbJarXmlFO, FileUtil.toFileObject(provider.getJ2eeModule().getDeploymentConfigurationFile(EJBJAR_XML)));
        
        EjbJarImplementation ejbJar = project.getLookup().lookup(EjbJarImplementation.class);
        assertEquals(metaInfFO, ejbJar.getMetaInf());
        assertEquals(ejbJarXmlFO, ejbJar.getDeploymentDescriptor());
    }

    public void testJavaEEProjectSettingsInEjbJar() throws Exception {
        File f = new File(getDataDir().getAbsolutePath(), "projects/EJBModule_6_0");
        project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        Profile obtainedProfile = JavaEEProjectSettings.getProfile(project);
        assertEquals(Profile.JAVA_EE_6_FULL, obtainedProfile);
        JavaEEProjectSettings.setProfile(project, Profile.JAVA_EE_7_FULL);
        obtainedProfile = JavaEEProjectSettings.getProfile(project);
        assertEquals(Profile.JAVA_EE_7_FULL, obtainedProfile);
    }
    
    public void testMetadataModel()throws Exception {
        File f = new File(getDataDir().getAbsolutePath(), "projects/EJBModule1");
        project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        J2eeModule j2eeModule = provider.getJ2eeModule();
        assertNotNull(j2eeModule.getMetadataModel(EjbJarMetadata.class));
        assertNotNull(j2eeModule.getMetadataModel(WebservicesMetadata.class));
    }
    
    /**
     * Tests that null is silently returned for files in the configuration files directory
     * (meta.inf) when this directory does not exist.
     */
    public void testMetaInfBasedPathsAreNullWhenMetaInfIsNullIssue65888() throws Exception {
        File f = new File(getDataDir().getAbsolutePath(), "projects/BrokenEJBModule1");
        project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        // XXX should not cast a Project
        helper = ((EjbJarProject)project).getAntProjectHelper();
        
        // first ensure meta.inf does not exist
        String metaInf = helper.getStandardPropertyEvaluator().getProperty("meta.inf");
        assertTrue(metaInf.endsWith("conf"));
        assertNull(helper.resolveFileObject(metaInf));
        
        // ensure meta.inf-related files are silently returned as null
        
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        J2eeModule j2eeModule = provider.getJ2eeModule();
        assertNotNull(j2eeModule.getDeploymentConfigurationFile(EJBJAR_XML));
        assertNull(FileUtil.toFileObject(j2eeModule.getDeploymentConfigurationFile(EJBJAR_XML)));
        assertNotNull(j2eeModule.getMetadataModel(EjbJarMetadata.class));
        assertNotNull(j2eeModule.getMetadataModel(WebservicesMetadata.class));
        
        EjbJarImplementation ejbJar = project.getLookup().lookup(EjbJarImplementation.class);
        assertNull(ejbJar.getMetaInf());
        assertNull(ejbJar.getDeploymentDescriptor());
    }
    
    public void testNeedConfigurationFolder() {
        assertTrue("1.3 needs configuration folder",
                EjbJarProvider.needConfigurationFolder(Profile.J2EE_13));
        assertTrue("1.4 needs configuration folder",
                EjbJarProvider.needConfigurationFolder(Profile.J2EE_14));
        assertFalse("5.0 does not need configuration folder",
                EjbJarProvider.needConfigurationFolder(Profile.JAVA_EE_5));
        assertFalse("Anything else does not need configuration folder",
                EjbJarProvider.needConfigurationFolder(Profile.JAVA_EE_6_FULL));
        assertFalse("Anything else does not need configuration folder",
                EjbJarProvider.needConfigurationFolder(Profile.JAVA_EE_6_WEB));
        assertFalse("Even null does not need configuration folder",
                EjbJarProvider.needConfigurationFolder(null));
    }
    
}
