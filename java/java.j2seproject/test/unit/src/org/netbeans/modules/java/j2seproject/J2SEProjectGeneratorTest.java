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

package org.netbeans.modules.java.j2seproject;

import java.beans.Customizer;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Collections;
import junit.framework.Test;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seproject.api.J2SEProjectBuilder;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryProvider;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.test.MockLookup;

/**
 * Tests for J2SEProjectGenerator
 *
 * @author David Konecny
 */
public class J2SEProjectGeneratorTest extends NbTestCase {
    
    public J2SEProjectGeneratorTest(String testName) {
        super(testName);
    }

    private static final String[] createdFiles = {
        "build.xml",
        "nbproject/build-impl.xml",
        "nbproject/project.xml",
        "nbproject/project.properties",
        "src",
    };
    
    private static final String[] createdFilesExtSources = {
        "build.xml",
        "nbproject/build-impl.xml",
        "nbproject/project.xml",
        "nbproject/project.properties",
//        "nbproject/private/private.properties",       no private.properties are created when project and source roots are collocated
    };
    
    public static Test suite() {
        //Prevent initialization of ModuleSystem from wrong thread causing deadlock.
        return NbModuleSuite.
                emptyConfiguration().
                gui(false).
                addTest(J2SEProjectGeneratorTest.class).
                suite();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockLookup.setLayersAndInstances(
                new TestLibTypeProvider(),
                new TestLibProvider());
    }


            
    public void testCreateProject() throws Exception {
        File proj = getWorkDir();        
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.6"));   //NOI18N
        AntProjectHelper aph = J2SEProjectGenerator.createProject(proj, "test-project", null, "manifest.mf", null, false);
        J2SEProjectGenerator.setDefaultSourceLevel(null);
        assertNotNull(aph);
        FileObject fo = aph.getProjectDirectory();
        for (int i=0; i<createdFiles.length; i++) {
            assertNotNull(createdFiles[i]+" file/folder cannot be found", fo.getFileObject(createdFiles[i]));
        }
    } 
    
    public void testCreateProjectFromExtSources () throws Exception {
        File root = getWorkDir();
        File proj = new File (root, "ProjectDir");
        proj.mkdir();
        File srcRoot = new File (root, "src");
        srcRoot.mkdir ();
        File testRoot = new File (root, "test");
        testRoot.mkdir ();
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.6"));   //NOI18N
        AntProjectHelper helper = J2SEProjectGenerator.createProject(proj, "test-project-ext-src", new File[] {srcRoot}, new File[] {testRoot}, "manifest.mf", null, null);
        J2SEProjectGenerator.setDefaultSourceLevel(null);   //NOI18N
        assertNotNull (helper);
        FileObject fo = FileUtil.toFileObject(proj);
        for (int i=0; i<createdFilesExtSources.length; i++) {
            assertNotNull(createdFilesExtSources[i]+" file/folder cannot be found", fo.getFileObject(createdFilesExtSources[i]));
        } 
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        for (String propName : new String[] {"src.dir", "test.src.dir"}) {
            String propValue = props.getProperty(propName);
            assertNotNull(propName+" property cannot be found in project.properties", propValue);
            PropertyEvaluator eval = helper.getStandardPropertyEvaluator();
            //Remove the file.reference to the source.dir, it is implementation detail
            //depending on the presence of the AlwaysRelativeCollocationQuery
            assertTrue("Value of " + propName + " should be file reference", propValue.startsWith("${file.reference."));
            File file = helper.resolveFile(eval.evaluate(propValue));
            assertEquals("Invalid value of " + propName + " property", propName.equals("src.dir") ? srcRoot : testRoot, file);
        }
    }
    
    //Tests issue: #147128:J2SESources does not register new external roots immediately
    public void testProjectFromExtSourcesOwnsTheSources () throws Exception {
        File root = getWorkDir();
        File proj = new File (root, "ProjectDir");
        proj.mkdir();
        File srcRoot = new File (root, "src");
        srcRoot.mkdir ();
        File testRoot = new File (root, "test");
        testRoot.mkdir ();
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.6"));   //NOI18N
        AntProjectHelper helper = J2SEProjectGenerator.createProject(proj, "test-project-ext-src", new File[] {srcRoot}, new File[] {testRoot}, "manifest.mf", null, null);
        final Project expected = FileOwnerQuery.getOwner(helper.getProjectDirectory());
        assertNotNull(expected);
        assertEquals(expected, FileOwnerQuery.getOwner(Utilities.toURI(srcRoot)));
        assertEquals(expected, FileOwnerQuery.getOwner(Utilities.toURI(testRoot)));
    }
    
    public void testProjectBuilder() throws Exception {        
        final File root = getWorkDir();
        final File projectDir = new File(root,"proj");
        final String projectName = "Test Project";
        final Library[] compileTimeLibs = new Library[] {
            LibraryManager.getDefault().getLibrary("foo1"),
            LibraryManager.getDefault().getLibrary("foo2")
        };
        assertNotNull(compileTimeLibs[0]);
        assertNotNull(compileTimeLibs[1]);
        final Library runtimeLib = LibraryManager.getDefault().getLibrary("foo3");
        assertNotNull(runtimeLib);
        final J2SEProjectBuilder generator = new J2SEProjectBuilder(projectDir, projectName);
        
        generator.addDefaultSourceRoots().
        setBuildXmlName("my-build.xml").
        setMainClass("org.me.Main").
        addJVMArguments("-Xmx100M").
        addCompileLibraries(compileTimeLibs).
        addRuntimeLibraries(runtimeLib).
        build();
        
        EditableProperties props = new EditableProperties(true);
        props.load(new FileInputStream(new File(projectDir,"nbproject"+File.separatorChar+"project.properties")));
        assertEquals("-Xmx100M", props.getProperty("run.jvmargs"));
        assertEquals("my-build.xml", props.getProperty("buildfile"));
        assertEquals("org.me.Main", props.getProperty("main.class"));
        assertEquals("src",props.getProperty("src.dir"));
        assertEquals("test",props.getProperty("test.src.dir"));
        assertEquals("${libs.foo1.classpath}:${libs.foo2.classpath}",props.get("javac.classpath"));
        assertEquals("${libs.foo3.classpath}:${javac.classpath}:${build.classes.dir}",props.get("run.classpath"));
    }
    
    
    // <editor-fold defaultstate="collapsed" desc="Libraries Mock Services">
    public static class TestLibProvider implements LibraryProvider<LibraryImplementation> {
        
        private LibraryImplementation[] libs;

        @Override
        public LibraryImplementation[] getLibraries() {
            if (libs == null) {
                libs = createLibs();
            }
            return libs;
        }
        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {            
        }
        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {            
        }                
        private LibraryImplementation[] createLibs() {
            final TestLibTypeProvider tlp = Lookup.getDefault().lookup(TestLibTypeProvider.class);
            final LibraryImplementation lib1 = tlp.createLibrary();
            lib1.setName("foo1");
            lib1.setContent("classpath", Collections.<URL>emptyList());
            final LibraryImplementation lib2 = tlp.createLibrary();
            lib2.setName("foo2");
            lib2.setContent("classpath", Collections.<URL>emptyList());
            final LibraryImplementation lib3 = tlp.createLibrary();
            lib3.setName("foo3");
            lib3.setContent("classpath", Collections.<URL>emptyList());
            return new LibraryImplementation[] {
                lib1,
                lib2,
                lib3
            };
        }
    }
    
    public static class TestLibTypeProvider implements LibraryTypeProvider {
        @Override
        public String getDisplayName() {
            return "j2se";
        }
        @Override
        public String getLibraryType() {
            return "j2se";
        }
        @Override
        public String[] getSupportedVolumeTypes() {
            return new String[] {"classpath","src","javadoc"};
        }
        @Override
        public LibraryImplementation createLibrary() {
            return LibrariesSupport.createLibraryImplementation("j2se", getSupportedVolumeTypes());
        }
        @Override
        public void libraryDeleted(LibraryImplementation libraryImpl) {            
        }
        @Override
        public void libraryCreated(LibraryImplementation libraryImpl) {            
        }
        @Override
        public Customizer getCustomizer(String volumeType) {
            return null;
        }
        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }        
    }
    //</editor-fold>
}
