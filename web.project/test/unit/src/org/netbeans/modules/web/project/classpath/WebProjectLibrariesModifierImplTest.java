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

package org.netbeans.modules.web.project.classpath;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.test.TestUtil;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 * Test for {@link WebProjectClassPathModifier}.
 * @author tmysik
 */
public class WebProjectLibrariesModifierImplTest extends NbTestCase {
    
    private WebProject webProject;
    private WebProject webProject2;
    
    public WebProjectLibrariesModifierImplTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockLookup.setLayersAndInstances(new TestLibraryProvider());
        File dest = new File(getWorkDir().getAbsolutePath(), "WebApplication1");
        dest.mkdir();
        TestUtil.copyDir(new File(getDataDir().getAbsolutePath(), "projects/WebApplication1"), dest);
        webProject = (WebProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(dest));
        dest = new File(getWorkDir().getAbsolutePath(), "WebApplication2");
        dest.mkdir();
        TestUtil.copyDir(new File(getDataDir().getAbsolutePath(), "projects/WebApplication1"), dest);
        webProject2 = (WebProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(dest));
    }

    public void testAllMethods() throws Exception {
        assertEquals("", webProject.getAntProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL));
        Library l = LibraryManager.getDefault().getLibrary("test");
        webProject.getLibrariesModifier().addPackageLibraries(new Library[]{l}, "/apath");
        AntArtifact[] artifact = AntArtifactQuery.findArtifactsByType(webProject2, EjbProjectConstants.ARTIFACT_TYPE_J2EE_MODULE_IN_EAR_ARCHIVE);
        webProject.getLibrariesModifier().addPackageAntArtifacts(new AntArtifact[]{artifact[0]}, 
                new URI[] {artifact[0].getArtifactLocations()[0]}, "/bpath");
        File ff = new File(getWorkDir(), "c.jar");
        URL u = new URL("jar:"+ff.toURI().toURL().toExternalForm()+"!/");
        webProject.getLibrariesModifier().addPackageRoots(new URL[]{u}, "/cpath");
        String prop = webProject.getAntProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL);
        assertEquals("${libs.test.classpath}:${reference.WebApplication1.dist-ear}:${file.reference.c.jar}", prop);
        
        List<Item> items = webProject.getClassPathModifier().getClassPathSupport().itemsList(prop, "web-module-additional-libraries");
        assertEquals(3, items.size());
        assertEquals("/apath", items.get(0).getAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT));
        assertEquals("${libs.test.classpath}", items.get(0).getReference());
        assertEquals("/bpath", items.get(1).getAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT));
        assertEquals("${reference.WebApplication1.dist-ear}", items.get(1).getReference());
        assertEquals("/cpath", items.get(2).getAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT));
        assertEquals("${file.reference.c.jar}", items.get(2).getReference());
        
        webProject.getLibrariesModifier().removePackageLibraries(new Library[]{l}, "/apath");
        webProject.getLibrariesModifier().removePackageAntArtifacts(new AntArtifact[]{artifact[0]}, 
                new URI[] {artifact[0].getArtifactLocations()[0]}, "/bpath");
        webProject.getLibrariesModifier().removePackageRoots(new URL[]{u}, "/cpath");
        prop = webProject.getAntProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL);
        assertEquals("", prop);

        webProject.getLibrariesModifier().addCompileLibraries(new Library[]{l});
        webProject.getLibrariesModifier().addCompileAntArtifacts(new AntArtifact[]{artifact[0]}, 
                new URI[] {artifact[0].getArtifactLocations()[0]});
        webProject.getLibrariesModifier().addCompileRoots(new URL[]{u});
        prop = webProject.getAntProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(ProjectProperties.JAVAC_CLASSPATH);
        assertEquals("${file.reference.jar0.jar}:${libs.test.classpath}:${reference.WebApplication1.dist-ear}:${file.reference.c.jar}", prop);
        
        items = webProject.getClassPathModifier().getClassPathSupport().itemsList(prop, "web-module-libraries");
        assertEquals(4, items.size());
        assertEquals(null, items.get(1).getAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT));
        assertEquals("${libs.test.classpath}", items.get(1).getReference());
        assertEquals(null, items.get(2).getAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT));
        assertEquals("${reference.WebApplication1.dist-ear}", items.get(2).getReference());
        assertEquals(null, items.get(3).getAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT));
        assertEquals("${file.reference.c.jar}", items.get(3).getReference());
        
        webProject.getLibrariesModifier().removeCompileLibraries(new Library[]{l});
        webProject.getLibrariesModifier().removeCompileAntArtifacts(new AntArtifact[]{artifact[0]}, 
                new URI[] {artifact[0].getArtifactLocations()[0]});
        webProject.getLibrariesModifier().removeCompileRoots(new URL[]{u});
        prop = webProject.getAntProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(ProjectProperties.JAVAC_CLASSPATH);
        assertEquals("${file.reference.jar0.jar}", prop);
    }

    private static class TestLibraryProvider implements LibraryProvider {

        private LibraryImplementation[] libs;

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public LibraryImplementation[] getLibraries() {
            if (libs == null) {
                this.libs = new LibraryImplementation[] { new TestLibrary ("test")};
            }
            return this.libs;
        }

    }

    private static class TestLibrary implements LibraryImplementation {

        private String name;
        private List<URL> cp = Collections.emptyList();
        private List<URL> src = Collections.emptyList();
        private List<URL> jdoc = Collections.emptyList();
        
        public TestLibrary (String name) {
            this.name = name;
        }

        public void setName(String name) {
        }

        public void setLocalizingBundle(String resourceName) {
        }

        public void setDescription(String text) {
        }

        public List<URL> getContent(String volumeType) throws IllegalArgumentException {
            if ("classpath".equals(volumeType)) {
                return this.cp;
            }
            else if ("src".equals(volumeType)) {
                return this.src;
            }
            else if ("jdoc".equals(volumeType)) {
                return this.jdoc;
            }
            throw new IllegalArgumentException ();
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
        }

        public void setContent(String volumeType, List<URL> path) throws IllegalArgumentException {
            if ("classpath".equals(volumeType)) {
                this.cp = path;
            }
            else if ("src".equals(volumeType)) {
                this.src = path;
            }
            else if ("jdoc".equals(volumeType)) {
                this.jdoc = path;
            }
            else {
                throw new IllegalArgumentException ();
            }
        }

        public String getType() {
            return "j2se";
        }

        public String getName() {
            return this.name;
        }

        public String getLocalizingBundle() {
            return null;
        }

        public String getDescription() {
            return null;
        }

    }
    
}
