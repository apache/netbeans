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

package org.netbeans.modules.javawebstart.ui.customizer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.platform.implspi.JavaPlatformProvider;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.test.MockLookup;

import static org.netbeans.modules.java.api.common.project.ProjectProperties.ENDORSED_CLASSPATH;
/**
 * 
 * @author Petr Somol
 * @author Tomas Zezula
 */
public class JWSProjectPropertiesTest extends NbTestCase {

    public JWSProjectPropertiesTest(String name) {
        super(name);
    }

    private J2SEProject p;
    private String oldJavaHome;
    File wsPrimary = null;
    File wsSecondary = null;
    File pgPrimary = null;
    File pgSecondary = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        oldJavaHome = System.getProperty("java.home");
        MockLookup.setLayersAndInstances(new TestPlatformProvider());
        clearWorkDir();
        File workDir = getWorkDir();
        File jdkDirPrimary = new File(workDir.getPath() + "/foo/bar/jdk");
        jdkDirPrimary.mkdirs();
        final String jreDirPrimaryPath = jdkDirPrimary.getPath() + "/jre";
        System.setProperty("java.home", jreDirPrimaryPath);
        J2SEProjectGenerator.createProject(getWorkDir(), "test", null, null, null, false);        
        p = (J2SEProject) ProjectManager.getDefault().findProject(FileUtil.toFileObject(workDir));                                
        assertEquals(jreDirPrimaryPath, getProperty("java.home"));
        
        File jdkDirSecondary = new File(workDir.getPath() + "/foo/bar/otherjdk");
        jdkDirSecondary.mkdirs();
        setProperty("platforms.otherjdk.home", jdkDirSecondary.getPath());
        System.out.println("Mockup platforms.otherjdk.home = " + getProperty("platforms.otherjdk.home"));

        File libDirPrimary = new File(jdkDirPrimary.getPath() + "/jre/lib");
        File libDirSecondary = new File(jdkDirSecondary.getPath() + "/jre/lib");
        libDirPrimary.mkdirs();
        libDirSecondary.mkdirs();
        wsPrimary = new File(libDirPrimary.getPath() + "/javaws.jar");
        pgPrimary = new File(libDirPrimary.getPath() + "/plugin.jar");
        wsSecondary = new File(libDirSecondary.getPath() + "/javaws.jar");
        pgSecondary = new File(libDirSecondary.getPath() + "/plugin.jar");
        wsPrimary.createNewFile();
        pgPrimary.createNewFile();
        wsSecondary.createNewFile();
        pgSecondary.createNewFile();
    }

    @Override
    protected void tearDown() throws Exception {
        System.setProperty("java.home", oldJavaHome);
        super.tearDown();
    }

    public void testUpdateWebStartJarsOnOpen() throws Exception {
        System.out.println("Test updateWebStartJarsOnOpen():");

        setProperty("platform.active", "default_platform");
        setProperty(ENDORSED_CLASSPATH, "");
        updateWebStartJarsOnOpen(false);
        assertEquals("", getProperty(ENDORSED_CLASSPATH));

        setProperty("platform.active", "default_platform");
        System.out.println("Test preserving of all items except the non-existing and disabled WS references on open:");
        setProperty(ENDORSED_CLASSPATH, new String[]{
            "foo:", // WS unrelated
            "${java.home}/lib/javaws.jar:", // exists
            "${java.home}/lib/plugin.jar:", // exists
            "${foo.bar}:", // WS unrelated
            "foo.bar/javaws.jar:", // does not exist
            "${foo.bar}/javaws.jar"}); // does not exist
        updateWebStartJarsOnOpen(false);
        assertEquals("foo:${foo.bar}", getRawProperty(ENDORSED_CLASSPATH));
        System.out.println("OK");

        setProperty("platform.active", "default_platform");
        setProperty("jnlp.descriptor", "application");        
        System.out.println("Test substitute of missing WS application items on open with WS enabled:");
        setProperty(ENDORSED_CLASSPATH, new String[]{
            "foo:",// WS unrelated
            "${platforms.otherjdk.home}/jre/lib/javaws.jar:", // exists
            "${foo.bar}:", // WS unrelated
            "foo.bar/javaws.jar:", // does not exist
            "${foo.bar}/javaws.jar"}); // does not exist
        updateWebStartJarsOnOpen(true);
        assertEquals("${java.home}/lib/javaws.jar:" +
                "foo:${foo.bar}", getRawProperty(ENDORSED_CLASSPATH));
        System.out.println("OK");
        
        setProperty("platform.active", "default_platform");
        setProperty("jnlp.descriptor", "applet");
        System.out.println("Test substitute of missing WS applet items on open with WS enabled while preserving existing form of references:");
        setProperty(ENDORSED_CLASSPATH, new String[]{
            "foo:",// WS unrelated
            wsPrimary.getAbsolutePath()+":", // exists
            "${platforms.otherjdk.home}/jre/lib/javaws.jar:", // exists
            "${foo.bar}:", // WS unrelated
            "foo.bar/javaws.jar:", // does not exist
            "${foo.bar}/javaws.jar"}); // does not exist
        updateWebStartJarsOnOpen(true);
        assertEquals(wsPrimary.getAbsolutePath()+":" + 
                "${platforms.otherjdk.home}/jre/lib/javaws.jar:" +
                "${java.home}/lib/plugin.jar:" +
                "foo:${foo.bar}", getRawProperty(ENDORSED_CLASSPATH));
        System.out.println("OK");

        setProperty("platform.active", "otherjdk");
        setProperty("jnlp.descriptor", "applet");
        System.out.println("Test substitute of missing WS applet items on open with non-default active platform:");
        setProperty(ENDORSED_CLASSPATH, new String[]{
            "foo:", // WS unrelated
            wsPrimary.getAbsolutePath()+":", // exists
            "${platforms.otherjdk.home}/jre/lib/javaws.jar:", // exists
            "${foo.bar}:", // WS unrelated
            "foo.bar/javaws.jar:", // does not exist
            "${foo.bar}/javaws.jar"}); // does not exist
        updateWebStartJarsOnOpen(true);
        assertEquals(wsPrimary.getAbsolutePath()+":" + 
                "${platforms.otherjdk.home}/jre/lib/javaws.jar:" + 
                "${platforms.otherjdk.home}/jre/lib/plugin.jar:" +
                "foo:${foo.bar}", getRawProperty(ENDORSED_CLASSPATH));
        System.out.println("OK");
    }

    public void testUpdateWebStartJarsOnChange() throws Exception {
        System.out.println("Test updateWebStartJarsOnChange():");

        setProperty("platform.active", "default_platform");
        setProperty(ENDORSED_CLASSPATH, "");
        updateWebStartJarsOnChange(false);
        assertEquals("", getProperty(ENDORSED_CLASSPATH));

        setProperty("platform.active", "default_platform");
        System.out.println("Test preserving of all items except WS references on change (WS disabled):");
        setProperty(ENDORSED_CLASSPATH, new String[]{
            "foo:", // WS unrelated
            "${java.home}/lib/javaws.jar:", // exists
            "${platforms.otherjdk.home}/jre/lib/plugin.jar:", // exists
            "${foo.bar}:", // WS unrelated
            "foo.bar/javaws.jar:", // does not exist
            "${foo.bar}/javaws.jar"}); // does not exist
        updateWebStartJarsOnChange(false);
        assertEquals("foo:${foo.bar}", getRawProperty(ENDORSED_CLASSPATH));
        System.out.println("OK");

        setProperty("platform.active", "default_platform");
        setProperty("jnlp.descriptor", "application");        
        System.out.println("Test replace all WS items by only the necessary WS application items in reference form (WS enabled):");
        setProperty(ENDORSED_CLASSPATH, new String[]{
            "foo:",// WS unrelated
            "${platforms.otherjdk.home}/jre/lib/javaws.jar:", // exists
            "${foo.bar}:", // WS unrelated
            "foo.bar/javaws.jar:", // does not exist
            "${foo.bar}/javaws.jar"}); // does not exist
        updateWebStartJarsOnChange(true);
        assertEquals("${java.home}/lib/javaws.jar:" +
                "foo:${foo.bar}", getRawProperty(ENDORSED_CLASSPATH));
        System.out.println("OK");
        
        setProperty("platform.active", "default_platform");
        setProperty("jnlp.descriptor", "applet");
        System.out.println("Test replace all WS items by only the necessary WS applet items in reference form (WS enabled):");
        setProperty(ENDORSED_CLASSPATH, new String[]{
            "foo:",// WS unrelated
            wsPrimary.getAbsolutePath()+":", // exists
            "${platforms.otherjdk.home}/jre/lib/javaws.jar:", // exists
            "${foo.bar}:", // WS unrelated
            "foo.bar/javaws.jar:", // does not exist
            "${foo.bar}/javaws.jar"}); // does not exist
        updateWebStartJarsOnChange(true);
        assertEquals("${java.home}/lib/javaws.jar:" +
                "${java.home}/lib/plugin.jar:" +
                "foo:${foo.bar}", getRawProperty(ENDORSED_CLASSPATH));
        System.out.println("OK");

        setProperty("platform.active", "otherjdk");
        setProperty("jnlp.descriptor", "applet");
        System.out.println("Test replace all WS items by only the necessary WS applet items in reference form (WS enabled) with non-default active platform:");
        setProperty(ENDORSED_CLASSPATH, new String[]{
            "foo:", // WS unrelated
            wsPrimary.getAbsolutePath()+":", // exists
            "${platforms.otherjdk.home}/jre/lib/javaws.jar:", // exists
            "${foo.bar}:", // WS unrelated
            "foo.bar/javaws.jar:", // does not exist
            "${foo.bar}/javaws.jar"}); // does not exist
        updateWebStartJarsOnChange(true);
        assertEquals("${platforms.otherjdk.home}/jre/lib/javaws.jar:" + 
                "${platforms.otherjdk.home}/jre/lib/plugin.jar:" +
                "foo:${foo.bar}", getRawProperty(ENDORSED_CLASSPATH));
        System.out.println("OK");
    }
    
    private String getProperty(String prop){
         return p.evaluator().getProperty(prop);
    }    

    private String getRawProperty(String prop){
        EditableProperties ep = p.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        return ep.getProperty(prop);
    }    
    
    private void setProperty(String prop, String value) throws IOException {
        EditableProperties ep = p.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty(prop, value);
        p.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ProjectManager.getDefault().saveProject(p);
    }

    private void setProperty(String prop, String[] values) throws IOException {
        EditableProperties ep = p.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty(prop, values);
        p.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ProjectManager.getDefault().saveProject(p);
    }

    private void updateWebStartJarsOnOpen(boolean isWebStart) throws IOException {
        EditableProperties ep = p.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        JWSProjectProperties.updateWebStartJarsOnOpen(ep, p.evaluator(), isWebStart);
        p.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ProjectManager.getDefault().saveProject(p);
    }

    private void updateWebStartJarsOnChange(boolean isWebStart) throws IOException {
        EditableProperties ep = p.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        JWSProjectProperties.updateWebStartJarsOnChange(ep, p.evaluator(), isWebStart);
        p.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ProjectManager.getDefault().saveProject(p);
    }

    private static class TestPlatformProvider implements JavaPlatformProvider {

        private JavaPlatform platformDefault;
        private JavaPlatform platformOther;

        @Override
        public void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
        }

        @Override
        public void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
        }

        @Override
        public JavaPlatform[] getInstalledPlatforms()  {
            return new JavaPlatform[] {
                getDefaultPlatform(),
                getOtherPlatform(),
            };
        }

        @Override
        public JavaPlatform getDefaultPlatform()  {
            if (this.platformDefault == null) {
                this.platformDefault = new TestPlatformDefault ();
            }
            return this.platformDefault;
        }

        public JavaPlatform getOtherPlatform()  {
            if (this.platformOther == null) {
                this.platformOther = new TestPlatformOther ();
            }
            return this.platformOther;
        }
    }

    private static class TestPlatformDefault extends JavaPlatform {

        @Override
        public FileObject findTool(String toolName) {
            return null;
        }

        @Override
        public String getVendor() {
            return "me";
        }

        @Override
        public ClassPath getStandardLibraries() {
            return ClassPathSupport.createClassPath(new URL[0]);
        }

        @Override
        public Specification getSpecification() {
            return new Specification ("j2se", new SpecificationVersion ("1.6"));
        }

        @Override
        public ClassPath getSourceFolders() {
            return null;
        }

        @Override
        public Map<String,String> getProperties() {
            return Collections.singletonMap("platform.ant.name","default_platform");
        }

        @Override
        public List<URL> getJavadocFolders() {
            return null;
        }

        @Override
        public Collection<FileObject> getInstallFolders() {
            return Collections.emptySet();
        }

        @Override
        public String getDisplayName() {
            return "TestPlatformDefault";
        }

        @Override
        public ClassPath getBootstrapLibraries() {
            return ClassPathSupport.createClassPath(new URL[0]);
        }

    }

    private static class TestPlatformOther extends TestPlatformDefault {

        @Override
        public Map<String,String> getProperties() {
            return Collections.singletonMap("platform.ant.name","otherjdk");
        }

        @Override
        public String getDisplayName() {
            return "TestPlatformOther";
        }

    }

}

