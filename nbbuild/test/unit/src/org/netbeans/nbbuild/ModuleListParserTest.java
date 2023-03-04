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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

/**
 * Test {@link ModuleListParser}.
 * @author Jesse Glick
 */
public class ModuleListParserTest extends TestBase {
    public ModuleListParserTest(String name) {
        super(name);
    }

    private File nball;

    private File file(File root, String relpath) {
        return new File(root, relpath.replace('/', File.separatorChar));
    }
    
    private String filePath(File root, String relpath) {
        return file(root, relpath).getAbsolutePath();
    }

    static void deleteCaches() throws IOException {
        for (File cache : new File(System.getProperty("java.io.tmpdir")).listFiles()) {
            if (cache.getName().matches("nb-scan-cache-.+[.]ser") && !cache.delete()) {
                throw new IOException(cache.getName());
            }
        }
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        String prop = System.getProperty("nb_all");
        assertNotNull("${nb_all} defined", prop);
        nball = new File(prop);
        deleteCaches();
    }

    public void testScanSourcesInNetBeansOrg() throws Exception {
        Hashtable<String,Object> properties = new Hashtable<>();
        properties.put("nb_all", nball.getAbsolutePath());
        File build = file(nball, "nbbuild/netbeans");
        properties.put("basedir", new File(nball, "nbbuild").getAbsolutePath());
        properties.put("netbeans.dest.dir", build.getAbsolutePath());
        properties.put("nb.cluster.foo", "beans,clazz");
        properties.put("nb.cluster.foo.dir", "foodir");
        properties.put("nb.cluster.bar", "core.startup");
        properties.put("nb.cluster.bar.dir", "bardir");
        properties.put("nb.cluster.java.dir", "java");
        properties.put("nb.cluster.platform.dir", "platform");
        properties.put("nb.cluster.ide.dir", "ide");
        properties.put("nb.clusters.list", "nb.cluster.foo,nb.cluster.bar");
        properties.put("clusters.config.full.list", "nb.cluster.foo,nb.cluster.bar,nb.cluster.java,nb.cluster.platform,nb.cluster.ide");

        long start = System.currentTimeMillis();
        ModuleListParser p = new ModuleListParser(properties, ModuleType.NB_ORG, null);
        System.err.println("Scanned " + nball + " sources in " + (System.currentTimeMillis() - start) + "msec");
        ModuleListParser.Entry e = p.findByCodeNameBase("org.netbeans.modules.beans");
        assertNotNull(e);
        assertEquals("org.netbeans.modules.beans", e.getCnb());
        assertEquals(file(build, "foodir/modules/org-netbeans-modules-beans.jar"), e.getJar());
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.netbeans.libs.xerces");
        assertNotNull("found module in a subdir", e);
        assertEquals("org.netbeans.libs.xerces", e.getCnb());
        assertEquals("unknown module put in extra cluster by default", file(build, "extra/modules/org-netbeans-libs-xerces.jar"), e.getJar());
        assertEquals("single classpath extension", 1, e.getClassPathExtensions().length);
        assertTrue("correct CP extensions (using <binary-origin> and relative paths)",
            e.getClassPathExtensions()[0].getPath().endsWith("libs.xerces/external/xercesImpl-2.8.0.jar"));
        e = p.findByCodeNameBase("org.netbeans.swing.tabcontrol");
        assertNotNull("found module in a subsubdir", e);
        e = p.findByCodeNameBase("org.netbeans.core.startup");
        assertNotNull(e);
        assertEquals("org.netbeans.core.startup", e.getCnb());
        assertEquals("handling special JAR names correctly", file(build, "bardir/core/core.jar"), e.getJar());
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.netbeans.modules.xml.tax");
        assertNotNull("found xml/tax", e);
        assertEquals("org.netbeans.modules.xml.tax", e.getCnb());
        assertEquals(file(build, "extra/modules/org-netbeans-modules-xml-tax.jar"), e.getJar());
        assertEquals("correct CP extensions (using runtime-relative-path)", Arrays.asList(new File[] {
            file(build, "extra/modules/ext/org-netbeans-tax.jar"),
        }), Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.netbeans.modules.masterfs");
        /* too fragile:
        assertNotNull(e);
        String testDeps[] = e.getTestDependencies().get("unit");
        assertNotNull(testDeps);
        assertEquals("depends on one test entry",1,testDeps.length);
        assertEquals("org.openide.filesystems",testDeps[0]);
         */
    }
    
    public void testScanSourcesAndBinariesForExternalSuite() throws Exception {
        Project fakeproj = new Project();
        fakeproj.addBuildListener(new BuildListener() {
            public void messageLogged(BuildEvent buildEvent) {
                if (buildEvent.getPriority() <= Project.MSG_VERBOSE) {
                    System.err.println(buildEvent.getMessage());
                }
            }
            public void taskStarted(BuildEvent buildEvent) {}
            public void taskFinished(BuildEvent buildEvent) {}
            public void targetStarted(BuildEvent buildEvent) {}
            public void targetFinished(BuildEvent buildEvent) {}
            public void buildStarted(BuildEvent buildEvent) {}
            public void buildFinished(BuildEvent buildEvent) {}
        });
        Hashtable<String,Object> properties = new Hashtable<>();
        properties.put("cluster.path.final", filePath(nball, "nbbuild/netbeans/platform")
                + File.pathSeparator + filePath(nball, "nbbuild/netbeans/ide"));
        properties.put("basedir", filePath(nball, "apisupport/apisupport.ant/test/unit/data/example-external-projects/suite1/action-project"));
        properties.put("suite.dir", filePath(nball, "apisupport/apisupport.ant/test/unit/data/example-external-projects/suite1"));
        long start = System.currentTimeMillis();
        ModuleListParser p = new ModuleListParser(properties, ModuleType.SUITE, fakeproj);
        System.err.println("Scanned " + nball + " binaries in " + (System.currentTimeMillis() - start) + "msec");
        ModuleListParser.Entry e = p.findByCodeNameBase("org.netbeans.examples.modules.action");
        assertNotNull("found myself", e);
        assertEquals("org.netbeans.examples.modules.action", e.getCnb());
        assertEquals(file(nball, "apisupport/apisupport.ant/test/unit/data/example-external-projects/suite1/build/cluster/modules/org-netbeans-examples-modules-action.jar"), e.getJar());
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.netbeans.examples.modules.lib");
        assertNotNull("found sister project in suite", e);
        assertEquals("org.netbeans.examples.modules.lib", e.getCnb());
        assertEquals(file(nball, "apisupport/apisupport.ant/test/unit/data/example-external-projects/suite1/build/cluster/modules/org-netbeans-examples-modules-lib.jar"), e.getJar());
        File jar = file(nball, "nbbuild/netbeans/ide/modules/org-netbeans-libs-xerces.jar");
        assertTrue("Build all-libs/xerces first!", jar.isFile());
        e = p.findByCodeNameBase("org.netbeans.libs.xerces");
        assertNotNull("found netbeans.org module by its binary", e);
        assertEquals("org.netbeans.libs.xerces", e.getCnb());
        assertEquals(jar, e.getJar());
        assertEquals("correct CP extensions (using Class-Path header in manifest)",
                Collections.singletonList(file(nball, "nbbuild/netbeans/ide/modules/ext/xerces-2.8.0.jar")),
                Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.openide.loaders");
        assertNotNull(e);
        assertEquals("org.openide.loaders", e.getCnb());
        assertEquals(file(nball, "nbbuild/netbeans/platform/modules/org-openide-loaders.jar"), e.getJar());
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(e.getClassPathExtensions()));
        e = p.findByCodeNameBase("org.netbeans.bootstrap");
        assertNotNull(e);
        assertEquals("org.netbeans.bootstrap", e.getCnb());
        assertEquals(file(nball, "nbbuild/netbeans/platform/lib/boot.jar"), e.getJar());
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(e.getClassPathExtensions()));
        jar = file(nball, "nbbuild/netbeans/ide/modules/org-netbeans-modules-xml-tax.jar");
        assertTrue("Build all-xml/tax first!", jar.isFile());
        e = p.findByCodeNameBase("org.netbeans.modules.xml.tax");
        assertNotNull(e);
        assertEquals("org.netbeans.modules.xml.tax", e.getCnb());
        assertEquals(jar, e.getJar());
        assertEquals(Arrays.asList(new File[] {
            file(nball, "nbbuild/netbeans/ide/modules/ext/org-netbeans-tax.jar"),
        }), Arrays.asList(e.getClassPathExtensions()));
    }

    public void testScanBinariesForOSGi() throws Exception {
        doScanBinariesForOSGi("osgi", "netigso.test");
    }

    public void testScanBinariesForOSGiInModulesDir() throws Exception {
        doScanBinariesForOSGi("modules", "netigso.test_repackaged");
    }

    private void doScanBinariesForOSGi(String whereTo, String cnb) throws Exception {
        Project fakeproj = new Project();
        fakeproj.addBuildListener(new BuildListener() {
            public void messageLogged(BuildEvent buildEvent) {
                if (buildEvent.getPriority() <= Project.MSG_VERBOSE) {
                    System.err.println(buildEvent.getMessage());
                }
            }
            public void taskStarted(BuildEvent buildEvent) {}
            public void taskFinished(BuildEvent buildEvent) {}
            public void targetStarted(BuildEvent buildEvent) {}
            public void targetFinished(BuildEvent buildEvent) {}
            public void buildStarted(BuildEvent buildEvent) {}
            public void buildFinished(BuildEvent buildEvent) {}
        });

        File osgiRepo = new File(getWorkDir(), whereTo);
        osgiRepo.mkdirs();
        Manifest man = createManifest();
        man.getMainAttributes().putValue("Bundle-SymbolicName", cnb);
        String dashCnb = cnb.replace('.', '-');
        generateJar(new File(osgiRepo, dashCnb + ".jar"), new String[0], man);

        CreateModuleXML cmxml = new CreateModuleXML();
        cmxml.setProject(fakeproj);
        final File configDir = new File(new File(getWorkDir(), "config"), "Modules");
        configDir.mkdirs();
        cmxml.setXmldir(configDir);
        FileSet fs = new FileSet();
        fs.setDir(getWorkDir());
        fs.setIncludes("**/*.jar");
        cmxml.addAutoload(fs);
        cmxml.execute();

        String[] arr = configDir.list();
        assertEquals("One file generated", 1, arr.length);
        assertEquals(dashCnb + ".xml", arr[0]);

        Hashtable<String,Object> properties = new Hashtable<>();
        properties.put("cluster.path.final", filePath(nball, "nbbuild/netbeans/platform")
                + File.pathSeparator + getWorkDir());
        properties.put("basedir", filePath(nball, "apisupport/apisupport.ant/test/unit/data/example-external-projects/suite1/action-project"));
        properties.put("suite.dir", filePath(nball, "apisupport/apisupport.ant/test/unit/data/example-external-projects/suite1"));
        long start = System.currentTimeMillis();
        ModuleListParser p = new ModuleListParser(properties, ModuleType.SUITE, fakeproj);
        System.err.println("Scanned " + nball + " binaries in " + (System.currentTimeMillis() - start) + "msec");
        ModuleListParser.Entry e = p.findByCodeNameBase(cnb);
        assertNotNull("found netigso module", e);
    }

//    Disabled test - referenced project files were not donated to apache
//    public void testScanSourcesAndBinariesForExternalStandaloneModule() throws Exception {
//        Hashtable<String,Object> properties = new Hashtable<String,Object>();
//        properties.put("cluster.path.final", filePath(nball, "apisupport.ant/test/unit/data/example-external-projects/suite3/nbplatform/platform5") +
//                File.pathSeparator + filePath(nball, "apisupport.ant/test/unit/data/example-external-projects/suite3/nbplatform/random"));
//        properties.put("basedir", filePath(nball, "apisupport.ant/test/unit/data/example-external-projects/suite3/dummy-project"));
//        properties.put("project", filePath(nball, "apisupport.ant/test/unit/data/example-external-projects/suite3/dummy-project"));
//        ModuleListParser p = new ModuleListParser(properties, ModuleType.STANDALONE, null);
//        ModuleListParser.Entry e = p.findByCodeNameBase("org.netbeans.examples.modules.dummy");
//        assertNotNull("found myself", e);
//        assertEquals("org.netbeans.examples.modules.dummy", e.getCnb());
//        assertEquals(file(nball, "apisupport.ant/test/unit/data/example-external-projects/suite3/dummy-project/build/cluster/modules/org-netbeans-examples-modules-dummy.jar"), e.getJar());
//        assertEquals(Collections.EMPTY_LIST, Arrays.asList(e.getClassPathExtensions()));
//        e = p.findByCodeNameBase("org.netbeans.modules.classfile");
//        assertNotNull("found (fake) netbeans.org module by its binary", e);
//        assertEquals("org.netbeans.modules.classfile", e.getCnb());
//    }

    private File generateJar (File f, String[] content, Manifest manifest) throws IOException {
        try (JarOutputStream os = new JarOutputStream (new FileOutputStream (f), manifest)) {
            for (int i = 0; i < content.length; i++) {
                os.putNextEntry(new JarEntry (content[i]));
                os.closeEntry();
            }
            os.closeEntry ();
        }

        return f;
    }
    
}
