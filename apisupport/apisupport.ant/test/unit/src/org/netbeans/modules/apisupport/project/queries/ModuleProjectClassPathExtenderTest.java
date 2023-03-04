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

package org.netbeans.modules.apisupport.project.queries;

import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.spi.project.libraries.LibraryFactory;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.util.test.MockLookup;
import org.xml.sax.InputSource;

public class ModuleProjectClassPathExtenderTest extends NbTestCase {

    public ModuleProjectClassPathExtenderTest(String n) {
        super(n);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockLookup.setLayersAndInstances();
        TestBase.initializeBuildProperties(getWorkDir(), getDataDir());
    }

    /**
     * Pass to {@link XPath#setNamespaceContext} to bind {@code nbm:} to the /3 namespace.
     */
    private static final NamespaceContext nbmNamespaceContext() {
        return new NamespaceContext() {
            public String getNamespaceURI(String prefix) {
                return prefix.equals("nbm") ? NbModuleProject.NAMESPACE_SHARED : null; // NOI18N
            }
            public String getPrefix(String namespaceURI) {return null;}
            public Iterator getPrefixes(String namespaceURI) {return null;}
        };
    }

    public void testAddLibraries() throws Exception {
        SuiteProject suite = TestBase.generateSuite(getWorkDir(), "suite");
        TestBase.generateSuiteComponent(suite, "lib");
        TestBase.generateSuiteComponent(suite, "testlib");
        NbModuleProject clientprj = TestBase.generateSuiteComponent(suite, "client");
        Library lib = LibraryFactory.createLibrary(new LibImpl("lib"));
        FileObject src = clientprj.getSourceDirectory();
        assertTrue(ProjectClassPathModifier.addLibraries(new Library[] {lib}, src, ClassPath.COMPILE));
        assertFalse(ProjectClassPathModifier.addLibraries(new Library[] {lib}, src, ClassPath.COMPILE));
        Library testlib = LibraryFactory.createLibrary(new LibImpl("testlib"));
        FileObject testsrc = clientprj.getTestSourceDirectory("unit");
        assertTrue(ProjectClassPathModifier.addLibraries(new Library[] {testlib}, testsrc, ClassPath.COMPILE));
        assertFalse(ProjectClassPathModifier.addLibraries(new Library[] {testlib}, testsrc, ClassPath.COMPILE));
        InputSource input = new InputSource(clientprj.getProjectDirectory().getFileObject("nbproject/project.xml").toURL().toString());
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(nbmNamespaceContext());
        assertEquals("org.example.client", xpath.evaluate("//nbm:data/nbm:code-name-base", input)); // control
        assertEquals("org.example.lib", xpath.evaluate("//nbm:module-dependencies/*/nbm:code-name-base", input));
        assertEquals("org.example.testlib", xpath.evaluate("//nbm:test-dependencies/*/*/nbm:code-name-base", input));
    }

    public void testAddRoots() throws Exception {
        NbModuleProject prj = TestBase.generateStandaloneModule(getWorkDir(), "module");
        FileObject src = prj.getSourceDirectory();
        FileObject jar = TestFileUtils.writeZipFile(FileUtil.toFileObject(getWorkDir()), "a.jar", "entry:contents");
        URL root = FileUtil.getArchiveRoot(jar.toURL());
        assertTrue(ProjectClassPathModifier.addRoots(new URL[] {root}, src, ClassPath.COMPILE));
        assertFalse(ProjectClassPathModifier.addRoots(new URL[] {root}, src, ClassPath.COMPILE));
        FileObject releaseModulesExt = prj.getProjectDirectory().getFileObject("release/modules/ext");
        assertNotNull(releaseModulesExt);
        assertNotNull(releaseModulesExt.getFileObject("a.jar"));
        jar = TestFileUtils.writeZipFile(releaseModulesExt, "b.jar", "entry2:contents");
        root = FileUtil.getArchiveRoot(jar.toURL());
        assertTrue(ProjectClassPathModifier.addRoots(new URL[] {root}, src, ClassPath.COMPILE));
        assertFalse(ProjectClassPathModifier.addRoots(new URL[] {root}, src, ClassPath.COMPILE));
        assertEquals(2, releaseModulesExt.getChildren().length);
        String projectXml = prj.getProjectDirectory().getFileObject("nbproject/project.xml").toURL().toString();
        InputSource input = new InputSource(projectXml);
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(nbmNamespaceContext());
        assertEquals(projectXml, "ext/a.jar", xpath.evaluate("//nbm:class-path-extension[1]/nbm:runtime-relative-path", input));
        assertEquals(projectXml, "release/modules/ext/a.jar", xpath.evaluate("//nbm:class-path-extension[1]/nbm:binary-origin", input));
        assertEquals(projectXml, "ext/b.jar", xpath.evaluate("//nbm:class-path-extension[2]/nbm:runtime-relative-path", input));
        assertEquals(projectXml, "release/modules/ext/b.jar", xpath.evaluate("//nbm:class-path-extension[2]/nbm:binary-origin", input));
    }

    public void testAddJUnit() throws Exception {
        NbModuleProject prj = TestBase.generateStandaloneModule(getWorkDir(), "module");
        FileObject tsrc = FileUtil.createFolder(prj.getProjectDirectory(), "test/unit/src");
        Library junit3 = LibraryFactory.createLibrary(new LibImpl("junit"));
        Library junit4 = LibraryFactory.createLibrary(new LibImpl("junit_4"));
        assertEquals("{}", new ProjectXMLManager(prj).getTestDependencies(prj.getModuleList()).toString());
        assertTrue(ProjectClassPathModifier.addLibraries(new Library[] {junit3}, tsrc, ClassPath.COMPILE));
        assertEquals("{unit=[org.netbeans.libs.junit4;compile]}", new ProjectXMLManager(prj).getTestDependencies(prj.getModuleList()).toString());
        assertFalse(ProjectClassPathModifier.addLibraries(new Library[] {junit3}, tsrc, ClassPath.COMPILE));
        assertFalse(ProjectClassPathModifier.addLibraries(new Library[] {junit4}, tsrc, ClassPath.COMPILE));
        assertEquals("{unit=[org.netbeans.libs.junit4;compile]}", new ProjectXMLManager(prj).getTestDependencies(prj.getModuleList()).toString());
        prj = TestBase.generateStandaloneModule(getWorkDir(), "module2");
        tsrc = FileUtil.createFolder(prj.getProjectDirectory(), "test/unit/src");
        assertTrue(ProjectClassPathModifier.addLibraries(new Library[] {junit4}, tsrc, ClassPath.COMPILE));
        assertEquals("{unit=[org.netbeans.libs.junit4;compile]}", new ProjectXMLManager(prj).getTestDependencies(prj.getModuleList()).toString());
        assertFalse(ProjectClassPathModifier.addLibraries(new Library[] {junit3}, tsrc, ClassPath.COMPILE));
        assertFalse(ProjectClassPathModifier.addLibraries(new Library[] {junit4}, tsrc, ClassPath.COMPILE));
        assertEquals("{unit=[org.netbeans.libs.junit4;compile]}", new ProjectXMLManager(prj).getTestDependencies(prj.getModuleList()).toString());
    }

    private static class LibImpl implements LibraryImplementation {
        private String name;
        LibImpl(String name) {
            this.name = name;
        }
        public String getType() {return "j2se";}
        public String getName() {return name;}
        public String getDescription() {return null;}
        public String getLocalizingBundle() {return null;}
        public List<URL> getContent(String volumeType) throws IllegalArgumentException {
            if (volumeType.equals("classpath")) {
                try {
                    return Collections.singletonList(new URL("jar:nbinst://org.example." + name + "/modules/ext/" + name + ".jar!/"));
                } catch (MalformedURLException x) {
                    throw new AssertionError(x);
                }
            } else {
                return Collections.emptyList();
            }
        }
        public void setName(String name) {}
        public void setDescription(String text) {}
        public void setLocalizingBundle(String resourceName) {}
        public void addPropertyChangeListener(PropertyChangeListener l) {}
        public void removePropertyChangeListener(PropertyChangeListener l) {}
        public void setContent(String volumeType, List<URL> path) throws IllegalArgumentException {}
    }

}
