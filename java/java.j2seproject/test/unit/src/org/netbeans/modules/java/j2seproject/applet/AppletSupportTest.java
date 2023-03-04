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

package org.netbeans.modules.java.j2seproject.applet;

import org.netbeans.modules.java.api.common.applet.AppletSupport;
import java.net.URL;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Collection;
import java.util.List;
import java.util.Collections;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.netbeans.junit.NbTestCase;
import org.netbeans.api.project.TestUtil;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.modules.java.j2seproject.J2SEProjectUtil;
import org.netbeans.modules.java.platform.implspi.JavaPlatformProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.util.test.MockLookup;

public class AppletSupportTest extends NbTestCase {

    private FileObject scratch;
    private FileObject projdir;
    private AntProjectHelper helper;
    private FileObject source;
    private FileObject buildFolder;
    private FileObject classesFolder;

    public AppletSupportTest (String name) {
        super (name);
    }

     protected void setUp() throws Exception {
        super.setUp();
        JavaPlatform platform1 = new TestPlatform("TP1","tp1",new Specification("j2se", new SpecificationVersion("1.4")));
        JavaPlatform platform2 = new TestPlatform("TP2","tp2",new Specification("j2se", new SpecificationVersion("1.5")));
        JavaPlatform platform3 = new TestPlatform("TP2","tp3",new Specification("j2se", new SpecificationVersion("1.5.1")));
        MockLookup.setLayersAndInstances(
            new PlatformProviderImpl(new JavaPlatform[] {
                platform1,
                platform2,
                platform3
            })
        );
        scratch = TestUtil.makeScratchDir(this);
        FileObject folderWithSpaces = scratch.createFolder("Folder With Spaces");
        projdir = folderWithSpaces.createFolder("proj");
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.6"));   //NOI18N
        helper = J2SEProjectGenerator.createProject(FileUtil.toFile(projdir),"proj",null,null,null, false); //NOI18N
        J2SEProjectGenerator.setDefaultSourceLevel(null);
        Project p = ProjectManager.getDefault().findProject(projdir);

        FileObject src = projdir.getFileObject("src");
        FileObject pkg = src.createFolder("pkg");
        source = pkg.createData("Applet","java");
        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String buildFolderName = (String) J2SEProjectUtil.getEvaluatedProperty (p, ep.getProperty("build.dir"));
        buildFolder = FileUtil.createFolder(projdir,buildFolderName);
        String classesFolderName = (String) J2SEProjectUtil.getEvaluatedProperty(p, ep.getProperty("build.classes.dir"));
        classesFolder = FileUtil.createFolder(projdir,classesFolderName);
    }

    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        helper = null;
        super.tearDown();
    }

    public void testgenerateHtmlFileURL () throws Exception {
        //Tests the JDK issue #6193279
        URL url = AppletSupport.generateHtmlFileURL(source,buildFolder,classesFolder,"tp1");
        String[] parts = url.toExternalForm().split("/");
        assertTrue (parts.length>4);
        assertEquals (parts[parts.length-1],"Applet.html");
        assertEquals (parts[parts.length-2],"build");
        assertEquals (parts[parts.length-3],"proj");
        assertEquals (parts[parts.length-4],"Folder%20With%20Spaces");
        url = AppletSupport.generateHtmlFileURL(source,buildFolder,classesFolder,"tp2");
        parts = url.toExternalForm().split("/");
        assertTrue (parts.length>4);
        assertEquals (parts[parts.length-1],"Applet.html");
        assertEquals (parts[parts.length-2],"build");
        assertEquals (parts[parts.length-3],"proj");
        assertEquals (parts[parts.length-4],"Folder With Spaces");
        url = AppletSupport.generateHtmlFileURL(source,buildFolder,classesFolder,null);
        parts = url.toExternalForm().split("/");
        assertTrue (parts.length>4);
        assertEquals (parts[parts.length-1],"Applet.html");
        assertEquals (parts[parts.length-2],"build");
        assertEquals (parts[parts.length-3],"proj");
        assertEquals (parts[parts.length-4],"Folder%20With%20Spaces");
        url = AppletSupport.generateHtmlFileURL(source,buildFolder,classesFolder,"tp3");
        parts = url.toExternalForm().split("/");
        assertTrue (parts.length>4);
        assertEquals (parts[parts.length-1],"Applet.html");
        assertEquals (parts[parts.length-2],"build");
        assertEquals (parts[parts.length-3],"proj");
        assertEquals (parts[parts.length-4],"Folder With Spaces");
    }

    private static class PlatformProviderImpl implements JavaPlatformProvider {
        private JavaPlatform[] platforms;

        public PlatformProviderImpl (JavaPlatform[] platforms) {
            this.platforms = platforms;
        }

        public JavaPlatform[] getInstalledPlatforms() {
            return platforms;
        }

        public JavaPlatform getDefaultPlatform() {
            return this.platforms[0];
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }

    private static class TestPlatform extends JavaPlatform {

        private String displayName;
        private Map<String,String> props;
        private Specification spec;

        public TestPlatform (String displayName, String antName, Specification spec) {
            this.displayName = displayName;
            this.props = Collections.singletonMap("platform.ant.name",antName);
            this.spec = spec;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public Specification getSpecification() {
            return spec;
        }

        public Map<String,String> getProperties() {
            return this.props;
        }

        public ClassPath getBootstrapLibraries() {
            return ClassPathSupport.createClassPath(new URL[0]);
        }

        public ClassPath getStandardLibraries() {
            return ClassPathSupport.createClassPath(new URL[0]);
        }

        public String getVendor() {
            return null;
        }

        public Collection<FileObject> getInstallFolders() {
            return null;
        }

        public FileObject findTool(String toolName) {
            return null;
        }

        public ClassPath getSourceFolders() {
            return null;
        }

        public List<URL> getJavadocFolders() {
            return null;
        }
    }
}
