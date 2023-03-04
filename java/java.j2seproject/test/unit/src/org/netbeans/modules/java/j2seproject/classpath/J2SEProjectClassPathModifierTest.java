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

package org.netbeans.modules.java.j2seproject.classpath;

import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.openide.util.test.MockLookup;

public class J2SEProjectClassPathModifierTest extends NbTestCase {

    private FileObject scratch;
    private AntProjectHelper helper;
    private PropertyEvaluator eval;
    private FileObject src;
    private FileObject test;
    private Project prj;

    public J2SEProjectClassPathModifierTest(String testName) {
        super(testName);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        MockLookup.setLayersAndInstances(new TestLibraryProvider());
        clearWorkDir();
        scratch = FileUtil.toFileObject(getWorkDir());
        FileObject projdir = scratch.createFolder("proj");  //NOI18N
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.6"));   //NOI18N
        this.helper = J2SEProjectGenerator.createProject(FileUtil.toFile(projdir),"proj",null,null,null, false); //NOI18N
        this.eval = this.helper.getStandardPropertyEvaluator();
        J2SEProjectGenerator.setDefaultSourceLevel(null);
        this.prj = FileOwnerQuery.getOwner(projdir);
        assertNotNull (this.prj);
        this.src = projdir.getFileObject("src");
        assertNotNull (this.src);
        this.test = projdir.getFileObject("test");
        assertNotNull (this.test);

    }

    public void testAddRemoveRoot () throws Exception {
        final FileObject rootFolder = this.scratch.createFolder("Root");
        final FileObject jarFile = TestFileUtils.writeZipFile(scratch, "archive.jar", "Test.properties:");
        final FileObject jarRoot = FileUtil.getArchiveRoot(jarFile);
        ProjectClassPathModifier.addRoots (new URL[] {rootFolder.toURL()}, this.src, ClassPath.COMPILE);
        String cp = this.eval.getProperty("javac.classpath");
        assertNotNull (cp);
        String[] cpRoots = PropertyUtils.tokenizePath (cp);
        assertNotNull (cpRoots);
        assertEquals(1,cpRoots.length);
        assertEquals(rootFolder,this.helper.resolveFileObject(cpRoots[0]));
        ProjectClassPathModifier.removeRoots (new URL[] {rootFolder.toURL()},this.src, ClassPath.COMPILE);
        cp = this.eval.getProperty("javac.classpath");
        assertNotNull (cp);
        cpRoots = PropertyUtils.tokenizePath (cp);
        assertNotNull (cpRoots);
        assertEquals(0,cpRoots.length);
        ProjectClassPathModifier.addRoots(new URL[] {jarRoot.toURL()},this.test,ClassPath.EXECUTE);
        cp = this.eval.getProperty("run.test.classpath");
        assertNotNull (cp);
        cpRoots = PropertyUtils.tokenizePath (cp);
        assertNotNull (cpRoots);
        assertEquals(5,cpRoots.length);
        assertEquals(this.helper.resolveFileObject(cpRoots[4]),jarFile);
    }

    public void testAddRemoveArtifact () throws Exception {
        FileObject projdir = scratch.createFolder("libPrj");  //NOI18N
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.6"));   //NOI18N
        AntProjectHelper h = J2SEProjectGenerator.createProject(FileUtil.toFile(projdir),"libProj",null,null,null, false); //NOI18N
        J2SEProjectGenerator.setDefaultSourceLevel(null);
        Project libPrj = FileOwnerQuery.getOwner(projdir);
        assertNotNull (this.prj);
        AntArtifactProvider ap = libPrj.getLookup().lookup(AntArtifactProvider.class);
        AntArtifact[] aas = ap.getBuildArtifacts();
        AntArtifact output = null;
        for (int i=0; i<aas.length; i++) {
            if (JavaProjectConstants.ARTIFACT_TYPE_JAR.equals(aas[i].getType())) {
                output = aas[i];
                break;
            }
        }
        assertNotNull (output);
        ProjectClassPathModifier.addAntArtifacts(new AntArtifact[] {output}, new URI[] {output.getArtifactLocations()[0]}, this.src, ClassPath.COMPILE);
        String cp = this.eval.getProperty("javac.classpath");
        assertNotNull (cp);
        String[] cpRoots = PropertyUtils.tokenizePath (cp);
        assertNotNull (cpRoots);
        assertEquals(1,cpRoots.length);
        URI projectURI = URI.create(output.getProject().getProjectDirectory().toURL().toExternalForm());
        URI expected = projectURI.resolve(output.getArtifactLocations()[0]);
        assertEquals(expected,Utilities.toURI(this.helper.resolveFile(cpRoots[0])));
        ProjectClassPathModifier.removeAntArtifacts(new AntArtifact[] {output}, new URI[] {output.getArtifactLocations()[0]},this.src, ClassPath.COMPILE);
        cp = this.eval.getProperty("javac.classpath");
        assertNotNull (cp);
        cpRoots = PropertyUtils.tokenizePath (cp);
        assertNotNull (cpRoots);
        assertEquals(0,cpRoots.length);
    }

    public void testAddRemoveLibrary () throws Exception {
        LibraryProvider lp = Lookup.getDefault().lookup(LibraryProvider.class);
        assertNotNull (lp);
        LibraryImplementation[] impls = lp.getLibraries();
        assertNotNull (impls);
        assertEquals(1,impls.length);
        FileObject libRoot = this.scratch.createFolder("libRoot");
        impls[0].setContent("classpath",Collections.singletonList(libRoot.toURL()));
        Library[] libs =LibraryManager.getDefault().getLibraries();
        assertNotNull (libs);
        assertEquals(1,libs.length);
        ProjectClassPathModifier.addLibraries(libs, this.src, ClassPath.COMPILE);
        String cp = this.eval.getProperty("javac.classpath");
        assertNotNull (cp);
        String[] cpRoots = PropertyUtils.tokenizePath (cp);
        assertNotNull (cpRoots);
        assertEquals(1,cpRoots.length);
        assertEquals("${libs.Test.classpath}",cpRoots[0]);    //There is no build.properties filled, the libraries are not resolved
        ProjectClassPathModifier.removeLibraries(libs,this.src, ClassPath.COMPILE);
        cp = this.eval.getProperty("javac.classpath");
        assertNotNull (cp);
        cpRoots = PropertyUtils.tokenizePath (cp);
        assertNotNull (cpRoots);
        assertEquals(0,cpRoots.length);
    }

    @RandomlyFails // NB-Core-Build #3223; javac.classpath="" after addition
    public void testProjectLibrary() throws Exception {
        assertEquals(Collections.emptyList(), getPrjLibRefs());
        URL base = Utilities.toURI(getWorkDir()).toURL();
        helper.setLibrariesLocation(".."+File.separatorChar+"defs.properties");
        Library lib = LibraryManager.forLocation(new URL(base, "defs.properties")).createLibrary("j2se", "test",
                Collections.singletonMap("classpath", Collections.singletonList(new URL(base, "stuff/"))));
        ProjectClassPathModifier.addLibraries(new Library[] {lib}, src, ClassPath.COMPILE);
        assertEquals(getWorkDir().getPath() + File.separatorChar + "stuff", eval.getProperty("javac.classpath"));
        assertEquals(Collections.singletonList(".."+ File.separatorChar +"defs.properties"), getPrjLibRefs());
        ProjectClassPathModifier.removeLibraries(new Library[] {lib}, src, ClassPath.COMPILE);
        assertEquals("", eval.getProperty("javac.classpath"));
    }
    private List<String> getPrjLibRefs() {
        List<String> l = new ArrayList<String>();
        Element libs = helper.createAuxiliaryConfiguration().getConfigurationFragment("libraries", "http://www.netbeans.org/ns/ant-project-libraries/1", true);
        if (libs != null) {
            NodeList nl = libs.getElementsByTagName("definitions");
            for (int i = 0; i < nl.getLength(); i++) {
                l.add(nl.item(i).getFirstChild().getNodeValue());
            }
        }
        return l;
    }
    
    @SuppressWarnings("deprecation")
    public void testClassPathExtenderCompatibility () throws Exception {
        final FileObject rootFolder = this.scratch.createFolder("Root");
        final FileObject jarFile = TestFileUtils.writeZipFile(scratch, "archive.jar", "Test.properties:");
        org.netbeans.spi.java.project.classpath.ProjectClassPathExtender extender =
                prj.getLookup().lookup(org.netbeans.spi.java.project.classpath.ProjectClassPathExtender.class);
        assertNotNull (extender);
        extender.addArchiveFile(rootFolder);
        extender.addArchiveFile(jarFile);
        String cp = this.eval.getProperty("javac.classpath");
        assertNotNull (cp);
        String[] cpRoots = PropertyUtils.tokenizePath (cp);
        assertNotNull (cpRoots);
        assertEquals(2,cpRoots.length);
        assertEquals(rootFolder,this.helper.resolveFileObject(cpRoots[0]));
        assertEquals(jarFile,this.helper.resolveFileObject(cpRoots[1]));
    }

    public void testRemoveBrokenRoot () throws Exception {
        final FileObject rootFolder = this.scratch.createFolder("BrokenRoot");
        final FileObject jarFile = TestFileUtils.writeZipFile(scratch, "brokenarchive.jar", "Test.properties:");
        final File jar = FileUtil.toFile(jarFile);
        assertNotNull(jar);
        final FileObject jarRoot = FileUtil.getArchiveRoot(jarFile);
        final URL rootFolderURL = rootFolder.toURL();
        ProjectClassPathModifier.addRoots (new URL[] {rootFolderURL}, this.src, ClassPath.COMPILE);
        String cp = this.eval.getProperty("javac.classpath");
        assertNotNull (cp);
        String[] cpRoots = PropertyUtils.tokenizePath (cp);
        assertNotNull (cpRoots);
        assertEquals(1,cpRoots.length);
        assertEquals(rootFolder,this.helper.resolveFileObject(cpRoots[0]));
        rootFolder.delete();
        assertFalse(rootFolder.isValid());
        ProjectClassPathModifier.removeRoots (new URL[] {rootFolderURL},this.src, ClassPath.COMPILE);
        cp = this.eval.getProperty("javac.classpath");
        assertNotNull (cp);
        cpRoots = PropertyUtils.tokenizePath (cp);
        assertNotNull (cpRoots);
        assertEquals(0,cpRoots.length);
        final URL jarRootURL = jarRoot.toURL();
        ProjectClassPathModifier.addRoots(new URL[] {jarRootURL},this.test,ClassPath.EXECUTE);
        cp = this.eval.getProperty("run.test.classpath");
        assertNotNull (cp);
        cpRoots = PropertyUtils.tokenizePath (cp);
        assertNotNull (cpRoots);
        assertEquals(5,cpRoots.length);
        assertEquals(this.helper.resolveFileObject(cpRoots[4]),jarFile);
        jarFile.delete();
        assertFalse (jarRoot.isValid());
        ProjectClassPathModifier.removeRoots (new URL[] {jarRootURL},this.test, ClassPath.EXECUTE);
        cp = this.eval.getProperty("run.test.classpath");
        assertNotNull (cp);
        cpRoots = PropertyUtils.tokenizePath (cp);
        assertNotNull (cpRoots);
        assertEquals(4,cpRoots.length);
        for (String path : cpRoots) {
            File f = helper.resolveFile(path);
            assertFalse(jar.equals(f));
        }
    }

    public void testAddToPreprocessorPath() throws Exception {
        assertNotNull("Project not created!",prj);          //NOI18N
        final FileObject libRoot = prj.getProjectDirectory().createFolder("mylib");
        assertNotNull("Library not created!",libRoot);      //NOI18N
        assertNotNull("No src folder in the proj dir!",src);  //NOI18N
        SourceGroup srcGrp = null;
        for (SourceGroup sg : ProjectUtils.getSources(prj).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            FileObject root = sg.getRootFolder();
            if (root == src) {
                srcGrp = sg;
                break;
            }
        }
        assertNotNull("No sources group found!",srcGrp);    //NOI18N
        final ClassPath cp = ClassPath.getClassPath(srcGrp.getRootFolder(), JavaClassPathConstants.PROCESSOR_PATH);
        assertFalse(Arrays.asList(cp.getRoots()).contains(libRoot));
        ProjectClassPathModifier.addRoots(
            new URI[] {libRoot.toURI()},
            srcGrp.getRootFolder(),
            JavaClassPathConstants.PROCESSOR_PATH);
        assertTrue("No lib on processor path!", Arrays.asList(cp.getRoots()).contains(libRoot));    //NOI18N
    }


    private static class TestLibraryProvider implements LibraryProvider {

        private LibraryImplementation[] libs;

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public LibraryImplementation[] getLibraries() {
            if (libs == null) {
                this.libs = new LibraryImplementation[] { new TestLibrary ("Test")};
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
