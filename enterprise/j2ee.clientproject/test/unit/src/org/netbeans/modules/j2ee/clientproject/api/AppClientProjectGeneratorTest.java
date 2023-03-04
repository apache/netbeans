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

package org.netbeans.modules.j2ee.clientproject.api;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.clientproject.TestPlatformProvider;
import org.netbeans.modules.j2ee.clientproject.test.TestUtil;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Lukas Jungmann
 */
public class AppClientProjectGeneratorTest extends NbTestCase {
    
    private static final String[] createdFiles = {
        "build.xml",
        "nbproject/build-impl.xml",
        "nbproject/genfiles.properties",
        "nbproject/project.xml",
        "nbproject/project.properties",
        "nbproject/private/private.properties",
        "src/conf",
        "src/conf/MANIFEST.MF",
        "src/java",
        "test"
    };
    
    private static final String[] createdFilesExtSources = {
        "build.xml",
        "nbproject/build-impl.xml",
        "nbproject/genfiles.properties",
        "nbproject/project.xml",
        "nbproject/project.properties",
        "nbproject/private/private.properties",
    };
    
    private static final String[] createdProperties = {
        "build.classes.dir",
        "build.classes.excludes",
        "build.dir",
        "build.ear.classes.dir",
        "build.generated.dir",
        "build.generated.sources.dir",
        "build.sysclasspath",
        "build.test.classes.dir",
        "build.test.results.dir",
        "debug.classpath",
        "debug.test.classpath",
        "dist.dir",
        "dist.ear.jar",
        "dist.jar",
        "dist.javadoc.dir",
        //"j2ee.appclient.mainclass.args",
        "j2ee.platform",
        "j2ee.platform.classpath",
        "j2ee.server.type",
        "jar.compress",
        "jar.name",
        "javac.classpath",
        "javac.compilerargs",
        "javac.deprecation",
        "javac.source",
        "javac.target",
        "javac.test.classpath",
        "javadoc.additionalparam",
        "javadoc.author",
        "javadoc.encoding",
        "javadoc.noindex",
        "javadoc.nonavbar",
        "javadoc.notree",
        "javadoc.private",
        "javadoc.splitindex",
        "javadoc.use",
        "javadoc.version",
        "javadoc.windowtitle",
        "main.class",
        "manifest.file",
        "meta.inf",
        "platform.active",
        "resource.dir",
        "run.classpath",
        "run.jvmargs",
        "run.test.classpath",
        "source.root",
        "src.dir",
        "test.src.dir"
    };
    
    private static final String[] createdPropertiesExtSources = {
        "build.classes.dir",
        "build.classes.excludes",
        "build.dir",
        "build.ear.classes.dir",
        "build.generated.dir",
        "build.generated.sources.dir",
        "build.sysclasspath",
        "build.test.classes.dir",
        "build.test.results.dir",
        "debug.classpath",
        "debug.test.classpath",
        "dist.dir",
        "dist.ear.jar",
        "dist.jar",
        "dist.javadoc.dir",
        //"j2ee.appclient.mainclass.args",
        "j2ee.platform",
        "j2ee.platform.classpath",
        "j2ee.server.type",
        "jar.compress",
        "jar.name",
        "javac.classpath",
        "javac.compilerargs",
        "javac.deprecation",
        "javac.source",
        "javac.target",
        "javac.test.classpath",
        "javadoc.additionalparam",
        "javadoc.author",
        "javadoc.encoding",
        "javadoc.noindex",
        "javadoc.nonavbar",
        "javadoc.notree",
        "javadoc.private",
        "javadoc.splitindex",
        "javadoc.use",
        "javadoc.version",
        "javadoc.windowtitle",
        "manifest.file",
        "meta.inf",
        "platform.active",
        "resource.dir",
        "run.classpath",
        "run.jvmargs",
        "run.test.classpath",
        //"source.root",
        "src.dir",
        "test.src.dir"
    };
    
    public AppClientProjectGeneratorTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.makeScratchDir(this);
        
        MockLookup.setLayersAndInstances(new TestPlatformProvider());
    }

    public void testCreateProject() throws Exception {
        File root = new File(getWorkDir(), "projects");
        File proj = new File(root, "TestCreateACProject");
        AntProjectHelper aph = AppClientProjectGenerator.createProject(proj, "test-project",
                "test.MyMain", J2eeModule.JAVA_EE_5, TestUtil.SERVER_URL);
        assertNotNull(aph);
        FileObject fo = aph.getProjectDirectory();
        for (int i=0; i<createdFiles.length; i++) {
            assertNotNull(createdFiles[i]+" file/folder cannot be found", fo.getFileObject(createdFiles[i]));
        }
        EditableProperties props = aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        @SuppressWarnings("unchecked")
        List<Object> l = new ArrayList<Object>(props.keySet());
        for (int i=0; i<createdProperties.length; i++) {
            assertNotNull(createdProperties[i]+" property cannot be found in project.properties", props.getProperty(createdProperties[i]));
            l.remove(createdProperties[i]);
        }
        assertEquals("Found unexpected property: "+l,createdProperties.length, props.keySet().size());
    }
    
    public void testImportProject() throws Exception {
        File root = new File(getWorkDir(), "projects");
        File proj = new File(root, "ProjectDir");
        File rootToImport = TestUtil.copyFolder(getWorkDir(), new File(getDataDir(), "projects/importTest"));
        File srcRoot = new File(rootToImport, "src/java");
        File confRoot = new File(rootToImport, "src/conf");
        File testRoot = new File(proj, "test");
        AntProjectHelper helper = AppClientProjectGenerator.importProject(proj,
                "test-project-ext-src", new File[] {srcRoot}, new File[] {testRoot},
                confRoot, null, J2eeModule.JAVA_EE_5, TestUtil.SERVER_URL);
        assertNotNull(helper);
        FileObject importedDirFO = FileUtil.toFileObject(proj);
        for (int i=0; i<createdFilesExtSources.length; i++) {
            assertNotNull(createdFilesExtSources[i]+" file/folder cannot be found", importedDirFO.getFileObject(createdFilesExtSources[i]));
        }
        assertNotNull("MANIFEST.MF was created", new File(confRoot, "MANIFEST.MF"));
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        @SuppressWarnings("unchecked")
        List<Object> l = new ArrayList<Object>(props.keySet());
        int extFileRefCount = 0;
        for (int i=0; i<createdPropertiesExtSources.length; i++) {
            String propName = createdPropertiesExtSources[i];
            String propValue = props.getProperty(propName);
            assertNotNull(propName+" property cannot be found in project.properties", propValue);
            l.remove(propName);
            if ("manifest.file".equals(propName)) {
                assertEquals("Invalid value of manifest.file property.", "${meta.inf}/MANIFEST.MF", propValue);
            } else if ("src.dir".equals (propName)) {
                PropertyEvaluator eval = helper.getStandardPropertyEvaluator();
                //Remove the file.reference to the source.dir, it is implementation detail
                //depending on the presence of the AlwaysRelativeCollocationQuery
                assertTrue("Value of the external source dir should be file reference",propValue.startsWith("${file.reference."));
                if (l.remove (propValue.subSequence(2,propValue.length()-1))) {
                    extFileRefCount++;
                }
                File file = helper.resolveFile(eval.evaluate(propValue));
                assertEquals("Invalid value of src.dir property.", srcRoot, file);
            } else if ("test.src.dir".equals(propName)) {
                PropertyEvaluator eval = helper.getStandardPropertyEvaluator();
                //Remove the file.reference to the source.dir, it is implementation detail
                //depending on the presence of the AlwaysRelativeCollocationQuery
                assertTrue("Value of the external test dir should be file reference",propValue.startsWith("${file.reference."));
                if (l.remove (propValue.subSequence(2,propValue.length()-1))) {
                    extFileRefCount++;
                }
                File file = helper.resolveFile(eval.evaluate(propValue));
                assertEquals("Invalid value of test.src.dir property.", testRoot, file);
            }
        }
        assertEquals("Found unexpected property: "+l,createdPropertiesExtSources.length, props.keySet().size() - extFileRefCount);
    }
    
    public void testSetPlatform() throws Exception {
        File root = new File(getWorkDir(), "projects");
        File proj = new File(root, "ImportProjectsNBDir");
        File importRoot = new File(getDataDir(), "projects/importTest");
        File srcRoot = new File(importRoot, "src/java");
        File confRoot = new File(importRoot, "src/conf");
        File libDir = new File(root, "libs");
        FileUtil.createFolder(libDir);
        AntProjectHelper helper = AppClientProjectGenerator.importProject(proj,
                "test-project-ext-src2", new File[] {srcRoot}, new File[] {},
                confRoot, libDir, J2eeModule.JAVA_EE_5, TestUtil.SERVER_URL);
        assertNotNull(helper);
        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals("default_platform", ep.getProperty("platform.active"));
        assertEquals("1.5", ep.getProperty("javac.source"));
        assertEquals("1.5", ep.getProperty("javac.target"));
        AppClientProjectGenerator.setPlatform(helper, "ExplicitPlatform", "1.4");
        ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals("ExplicitPlatform", ep.getProperty("platform.active"));
        assertEquals("1.4", ep.getProperty("javac.source"));
        assertEquals("1.4", ep.getProperty("javac.target"));
    }
    
}
