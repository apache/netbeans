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

package org.netbeans.modules.apisupport.project;

import org.netbeans.modules.apisupport.project.api.Util;
import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.Manifest;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.test.TestFileUtils;

/**
 * @author Martin Krauskopf
 */
public class ApisupportAntUtilsTest extends TestBase {
    
    public ApisupportAntUtilsTest(String name) {
        super(name);
    }
    
    public void testFindLocalizedBundleInfoFromNetBeansOrgModule() throws Exception {
        FileObject dir = nbRoot().getFileObject("apisupport/apisupport.project");
        assertNotNull("have apisupport.project checked out", dir);
        NbModuleProject apisupport = (NbModuleProject) ProjectManager.getDefault().findProject(dir);
        LocalizedBundleInfo info = ApisupportAntUtils.findLocalizedBundleInfo(
                apisupport.getSourceDirectory(), apisupport.getManifest());
        assertApiSupportInfo(info);
    }
    
    public void testFindLocalizedBundleInfoFromSourceDirectory() throws Exception {
        LocalizedBundleInfo info = ApisupportAntUtils.findLocalizedBundleInfo(file("apisupport/apisupport.project"));
        assertApiSupportInfo(info);
    }
    
    public void testFindLocalizedBundleInfoFromSourceDirectory1() throws Exception {
        LocalizedBundleInfo info = ApisupportAntUtils.findLocalizedBundleInfo(resolveEEPFile("suite3/dummy-project"));
        assertNull(info);
    }
    
    public void testFindLocalizedBundleInfoFromBinaryModule() throws Exception {
        File apisupportF = file("nbbuild/netbeans/" + TestBase.CLUSTER_APISUPPORT + "/modules/org-netbeans-modules-apisupport-project.jar");
        assertApiSupportInfo(ApisupportAntUtils.findLocalizedBundleInfoFromJAR(apisupportF));
    }
    
    private void assertApiSupportInfo(LocalizedBundleInfo info) {
        assertNotNull("info loaded", info);
        // XXX ignore this for now, but be careful when editing the module's properties :)
        assertEquals("display name", "NetBeans Module Projects", info.getDisplayName());
        /* Too fragile:
        assertEquals("category", "Developing NetBeans", info.getCategory());
        assertEquals("short description", "Defines an Ant-based project type for NetBeans modules.", info.getShortDescription());
        assertEquals("long description", "Defines a project type for NetBeans " +
                "modules, useful for developing plug-in extensions to NetBeans. " +
                "Provides the logical view for modules, supplies the classpath " +
                "used for code completion, integrates with the NetBeans build " +
                "system (using Ant), etc.", info.getLongDescription());
         */
    }
    
    /** cf. #64782 */
    public void testFindLocalizedBundleInfoLocalization() throws Exception {
        Locale orig = Locale.getDefault();
        Locale.setDefault(Locale.JAPAN);
        try {
            clearWorkDir();
            File dir = getWorkDir();
            Manifest mani = new Manifest();
            mani.getMainAttributes().putValue("OpenIDE-Module-Localizing-Bundle", "pack/age/Bundle.properties");
            // Start with an unlocalized source project.
            File src = new File(dir, "src");
            File f = new File(src, "pack/age/Bundle.properties".replace('/', File.separatorChar));
            f.getParentFile().mkdirs();
            TestBase.dump(f, "OpenIDE-Module-Name=Foo\nOpenIDE-Module-Display-Category=Foo Stuff\nOpenIDE-Module-Short-Description=short\nOpenIDE-Module-Long-Description=Long...");
            // XXX test also ApisupportAntUtils.findLocalizedBundleInfo(File)?
            LocalizedBundleInfo info = ApisupportAntUtils.findLocalizedBundleInfo(FileUtil.toFileObject(src), mani);
            assertEquals("Foo", info.getDisplayName());
            assertEquals("Foo Stuff", info.getCategory());
            assertEquals("short", info.getShortDescription());
            assertEquals("Long...", info.getLongDescription());
            // Now add some locale variants.
            f = new File(src, "pack/age/Bundle_ja.properties".replace('/', File.separatorChar));
            TestBase.dump(f, "OpenIDE-Module-Long-Description=Long Japanese text...");
            f = new File(src, "pack/age/Bundle_ja_JP.properties".replace('/', File.separatorChar));
            TestBase.dump(f, "OpenIDE-Module-Name=Foo Nihon");
            info = ApisupportAntUtils.findLocalizedBundleInfo(FileUtil.toFileObject(src), mani);
            assertEquals("Foo Nihon", info.getDisplayName());
            assertEquals("Foo Stuff", info.getCategory());
            assertEquals("short", info.getShortDescription());
            assertEquals("Long Japanese text...", info.getLongDescription());
            // Now try it on JAR files.
            f = new File(dir, "noloc.jar");
            createJar(f, Collections.singletonMap("pack/age/Bundle.properties", "OpenIDE-Module-Name=Foo"), mani);
            info = ApisupportAntUtils.findLocalizedBundleInfoFromJAR(f);
            assertEquals("Foo", info.getDisplayName());
            assertNull(info.getShortDescription());
            f = new File(dir, "internalloc.jar");
            Map<String,String> contents = new HashMap<String,String>();
            contents.put("pack/age/Bundle.properties", "OpenIDE-Module-Name=Foo\nOpenIDE-Module-Short-Description=short");
            contents.put("pack/age/Bundle_ja_JP.properties", "OpenIDE-Module-Name=Foo Nihon");
            createJar(f, contents, mani);
            info = ApisupportAntUtils.findLocalizedBundleInfoFromJAR(f);
            assertEquals("Foo Nihon", info.getDisplayName());
            assertEquals("short", info.getShortDescription());
            f = new File(dir, "externalloc.jar");
            createJar(f, Collections.singletonMap("pack/age/Bundle.properties", "OpenIDE-Module-Name=Foo\nOpenIDE-Module-Short-Description=short"), mani);
            File f2 = new File(dir, "locale" + File.separatorChar + "externalloc_ja.jar");
            createJar(f2, Collections.singletonMap("pack/age/Bundle_ja.properties", "OpenIDE-Module-Short-Description=short Japanese"), new Manifest());
            info = ApisupportAntUtils.findLocalizedBundleInfoFromJAR(f);
            assertEquals("Foo", info.getDisplayName());
            assertEquals("the meat of #64782", "short Japanese", info.getShortDescription());
        } finally {
            Locale.setDefault(orig);
        }
    }

    public void testfindLocalizedBundleInfoFromOSGi() throws Exception { // #179752
        File jar = new File(getWorkDir(), "x.zip");
        TestFileUtils.writeZipFile(jar, "META-INF/MANIFEST.MF:Bundle-SymbolicName: foo\nBundle-Name: Foo\nBundle-Description: Does some foo stuff.\n");
        LocalizedBundleInfo info = ApisupportAntUtils.findLocalizedBundleInfoFromJAR(jar);
        assertNotNull(info);
        assertEquals("Foo", info.getDisplayName());
        assertEquals("Does some foo stuff.", info.getShortDescription());
        // XXX improve to load Bundle-Localization also if present
    }

    public void testGetJavadoc() throws Exception {
        File oneModuleDoc = new File(getWorkDir(), "org-example-module1");
        assertTrue(oneModuleDoc.mkdir());
        File index = new File(oneModuleDoc, "index.html");
        assertTrue(index.createNewFile());
        
        NbModuleProject project = generateStandaloneModule("module1");
        NbPlatform platform = project.getPlatform(false);
        URL oneModuleDocURL = FileUtil.urlForArchiveOrDir(oneModuleDoc);
        platform.addJavadocRoot(oneModuleDocURL);
        ModuleDependency md = new ModuleDependency(project.getModuleList().getEntry(project.getCodeNameBase()));
        
        URL url = md.getModuleEntry().getJavadoc(platform);
        assertNotNull("url was found", url);
        
        File nbDoc = new File(getWorkDir(), "nbDoc");
        File moduleDoc = new File(nbDoc, "org-example-module1");
        assertTrue(moduleDoc.mkdirs());
        index = new File(moduleDoc, "index.html");
        assertTrue(index.createNewFile());
        
        platform.addJavadocRoot(FileUtil.urlForArchiveOrDir(nbDoc));
        platform.removeJavadocRoots(new URL[] {oneModuleDocURL});
        url = md.getModuleEntry().getJavadoc(platform);
        assertNotNull("url was found", url);
    }
    
    public void testIsValidJavaFQN() throws Exception {
        assertFalse(ApisupportAntUtils.isValidJavaFQN("a.b,c"));
        assertFalse(ApisupportAntUtils.isValidJavaFQN(""));
        assertFalse(ApisupportAntUtils.isValidJavaFQN("a.b.1"));
        assertTrue(ApisupportAntUtils.isValidJavaFQN("a"));
        assertTrue(ApisupportAntUtils.isValidJavaFQN("a.b.c1"));
    }
    
    public void testDisplayName_70363() throws Exception {
        FileObject prjFO = TestBase.generateStandaloneModuleDirectory(getWorkDir(), "module");
        FileUtil.moveFile(prjFO.getFileObject("src"), prjFO, "libsrc");
        FileObject propsFO = FileUtil.createData(prjFO, AntProjectHelper.PROJECT_PROPERTIES_PATH);
        EditableProperties ep = Util.loadProperties(propsFO);
        ep.setProperty("src.dir", "libsrc");
        Util.storeProperties(propsFO, ep);
        LocalizedBundleInfo info = ApisupportAntUtils.findLocalizedBundleInfo(FileUtil.toFile(prjFO));
        assertNotNull("localized info found", info);
        assertEquals("has correct display name", "Testing Module", info.getDisplayName());
    }
    
    public void testAddDependency() throws Exception {
        NbModuleProject p = generateStandaloneModule("module");
        assertEquals("no dependencies", 0, new ProjectXMLManager(p).getDirectDependencies().size());
        assertTrue("successfully added", ApisupportAntUtils.addDependency(p, "org.openide.util", null, new SpecificationVersion("6.1"), true, null));
        ProjectManager.getDefault().saveProject(p);
        assertEquals("one dependency", 1, new ProjectXMLManager(p).getDirectDependencies().size());
        assertFalse("does not exist", ApisupportAntUtils.addDependency(p, "org.openide.i_do_not_exist", null, null, true, null));
        ProjectManager.getDefault().saveProject(p);
        assertEquals("still one dependency", 1, new ProjectXMLManager(p).getDirectDependencies().size());
        assertFalse("already there", ApisupportAntUtils.addDependency(p, "org.openide.util", null, new SpecificationVersion("6.1"), true, null));
        assertTrue("upgraded", ApisupportAntUtils.addDependency(p, "org.openide.util", null, new SpecificationVersion("6.2"), true, null));
        ProjectManager.getDefault().saveProject(p);
        SortedSet<ModuleDependency> deps = new ProjectXMLManager(p).getDirectDependencies();
        assertEquals("still one dependency", 1, deps.size());
        assertEquals("6.2", deps.iterator().next().getSpecificationVersion());
        assertFalse("not downgraded", ApisupportAntUtils.addDependency(p, "org.openide.util", null, new SpecificationVersion("6.0"), true, null));
        new ProjectXMLManager(p).addDependency(new ModuleDependency(p.getModuleList().getEntry("org.openide.awt"), null, null, true, true));
        assertFalse("not switched to spec dep", ApisupportAntUtils.addDependency(p, "org.openide.awt", null, new SpecificationVersion("6.0"), true, null));
        ProjectManager.getDefault().saveProject(p);
        deps = new ProjectXMLManager(p).getDirectDependencies();
        assertEquals("still two deps", 2, deps.size());
        assertTrue(deps.iterator().next().hasImplementationDependency());
    }
    
    public void testScanProjectForPackageNames() throws Exception {
        FileObject prjDir = generateStandaloneModuleDirectory(getWorkDir(), "module");
        FileUtil.createData(prjDir, "src/a/b/c/Test.java");
        SortedSet<String> packages = ApisupportAntUtils.scanProjectForPackageNames(FileUtil.toFile(prjDir));
        assertEquals("one package", 1, packages.size());
        assertEquals("a.b.c package", "a.b.c", packages.first());
    }
    
    public void testScanJarForPackageNames() throws Exception {
        Map<String,String> contents = new HashMap<String,String>();
        contents.put("a/b/A12.class", "");
        contents.put("a/b/c/B123.class", "");
        contents.put("pack/age/noclass/Bundle.properties", "");
        contents.put("1.0/invalid/package/name/A.class", "");
        File jar = new File(getWorkDir(), "some.jar");
        createJar(jar, contents, new Manifest());
        SortedSet<String> packages = new TreeSet<String>();
        ApisupportAntUtils.scanJarForPackageNames(packages, jar);
        assertEquals("two packages", 2, packages.size());
        assertEquals("a.b package", "a.b", packages.first());
        assertEquals("a.b.c package", "a.b.c", packages.last());
    }
    
}
