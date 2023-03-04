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

package org.netbeans.junit;


import java.net.URISyntaxException;
import test.pkg.not.in.junit.NbModuleSuiteIns;
import test.pkg.not.in.junit.NbModuleSuiteT;
import test.pkg.not.in.junit.NbModuleSuiteS;
import java.io.File;
import java.net.URL;
import java.util.Properties;
import java.util.Set;
import junit.framework.Test;
import org.openide.util.Utilities;
import test.pkg.not.in.junit.NbModuleSuiteClusters;
import test.pkg.not.in.junit.NbModuleSuiteTUserDir;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class NbModuleSuiteTest extends NbTestCase {

    public NbModuleSuiteTest(String testName) {
        super(testName);
    }

    public void testUserDir() {
        Test instance = NbModuleSuite.createConfiguration(NbModuleSuiteTUserDir.class).gui(false).suite();
        junit.textui.TestRunner.run(instance);

        assertEquals("Doesn't exist", System.getProperty("t.userdir"));

        instance = NbModuleSuite.createConfiguration(NbModuleSuiteTUserDir.class).gui(false).reuseUserDir(true).suite();
        junit.textui.TestRunner.run(instance);

        assertEquals("Exists", System.getProperty("t.userdir"));

        instance = NbModuleSuite.createConfiguration(NbModuleSuiteTUserDir.class).gui(false).reuseUserDir(false).suite();
        junit.textui.TestRunner.run(instance);

        assertEquals("Doesn't exist", System.getProperty("t.userdir"));
        assertProperty("netbeans.full.hack", "true");
    }
    
    public void testPreparePatches() throws URISyntaxException {
        Properties p = new Properties();

        String prop = File.separator + "x" + File.separator + "c:org-openide-util.jar" + File.pathSeparator +
            File.separator + "x" + File.separator + "org-openide-nodes.jar" + File.pathSeparator +
            File.separator + "x" + File.separator + "org-openide-util" + File.separator  + "tests.jar" + File.pathSeparator +
            File.separator + "x" + File.separator + "org-openide-filesystems.jar";
        Class<?>[] classes = {
            this.getClass(),
            this.getClass()
        };
        NbModuleSuite.S.preparePatches(prop, p, classes);
        assertNull(
            p.getProperty("netbeans.patches.org.openide.util")
        );
        assertEquals(
                File.separator + "x" + File.separator + "org-openide-util" + File.separator + "tests.jar"
                + File.pathSeparator + new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getPath(),
                p.getProperty("netbeans.systemclassloader.patches"));
    }

    public void testAccessToInsaneAndFS() {
        System.setProperty("ins.one", "no");
        System.setProperty("ins.fs", "no");

        Test instance = NbModuleSuite.createConfiguration(NbModuleSuiteIns.class).gui(false).enableModules(".*").suite();
        junit.textui.TestRunner.run(instance);

        assertProperty("ins.one", "OK");
        assertProperty("ins.fs", "OK");
    }

    public void testAccessToInsaneAndFSWithAllModules() {
        System.setProperty("ins.one", "no");
        System.setProperty("ins.fs", "no");

        Test instance = NbModuleSuite.createConfiguration(NbModuleSuiteIns.class).
                gui(false).clusters(".*").enableModules(".*").suite();
        junit.textui.TestRunner.run(instance);

        assertProperty("ins.one", "OK");
        assertProperty("ins.fs", "OK");
    }

    public void testAccessToInsaneAndFSWithAllModulesEnumerated() {
        System.setProperty("ins.one", "no");
        System.setProperty("ins.fs", "no");

        Test instance = NbModuleSuite.createConfiguration(NbModuleSuiteIns.class).
                gui(false).clusters(".*").enableModules(".*").addTest("testFS").suite();
        junit.textui.TestRunner.run(instance);

        assertProperty("ins.one", "no");
        assertProperty("ins.fs", "OK");
    }

    public void testOneCanEnumerateMethodsFromTheSuite() {
        System.setProperty("ins.one", "No");
        System.setProperty("ins.two", "No");
        System.setProperty("ins.three", "No");




        Test instance =
            NbModuleSuite.createConfiguration(NbModuleSuiteIns.class).addTest("testOne").
            addTest("testThree").gui(false)
            .suite();
        junit.textui.TestRunner.run(instance);

        assertProperty("ins.one", "OK");
        assertProperty("ins.two", "No");
        assertProperty("ins.three", "OK");
    }

    public void testOneCanEnumerateMethodsFromTheSuiteWithANewMethod() {
        System.setProperty("ins.one", "No");
        System.setProperty("ins.two", "No");
        System.setProperty("ins.three", "No");




        Test instance = NbModuleSuite.createConfiguration(NbModuleSuiteIns.class).gui(false).addTest("testOne", "testThree").suite();
        junit.textui.TestRunner.run(instance);

        assertProperty("ins.one", "OK");
        assertProperty("ins.two", "No");
        assertProperty("ins.three", "OK");
    }

    /* Cannot meaningfully rewrite while passing gui(false):
    public void testEmptyArrayMeansAll() {
        System.setProperty("ins.one", "No");
        System.setProperty("ins.two", "No");
        System.setProperty("ins.three", "No");

        Test instance = NbModuleSuite.create(NbModuleSuiteIns.class, null, null, new String[0]);
        junit.textui.TestRunner.run(instance);

        assertProperty("ins.one", "OK");
        assertProperty("ins.two", "OK");
        assertProperty("ins.three", "OK");
    }
     */

    static void assertProperty(String name, String value) {
        String v = System.getProperty(name);
        assertEquals("Property " + name, value, v);
    }

    public void testClustersCanBeCumulated() throws Exception {
        if (!isExtIDE()) {
            // skip
            return;
        }
        System.setProperty("clusters", "No");

        Test instance =
            NbModuleSuite.emptyConfiguration().
            gui(false).
            clusters("ide").
            clusters("extide").
            addTest(NbModuleSuiteClusters.class)
        .suite();
        junit.textui.TestRunner.run(instance);

        assertProperty("clusters", "ide:extide");
    }

    public void testClustersCanBeCumulatedInReverseOrder() throws Exception {
        if (!isExtIDE()) {
            // skip
            return;
        }
        System.setProperty("clusters", "No");

        Test instance =
            NbModuleSuite.emptyConfiguration().
            gui(false).
            clusters("extide").
            clusters("ide").
            addTest(NbModuleSuiteClusters.class)
        .suite();
        junit.textui.TestRunner.run(instance);

        assertProperty("clusters", "extide:ide");
    }

    /*
    public void testAccessClassPathDefinedAutoload() {

        NbModuleSuite.Configuration config = NbModuleSuite.Configuration.create(En.class);
        String manifest =
"Manifest-Version: 1.0\n" +
"OpenIDE-Module-Module-Dependencies: org.openide.util.enumerations>1.5\n" +
"OpenIDE-Module: org.netbeans.modules.test.nbjunit\n" +
"OpenIDE-Module-Specification-Version: 1.0\n";

        ClassLoader loader = new ManifestClassLoader(config.parentClassLoader, manifest);
        NbModuleSuite.Configuration load = config.classLoader(loader);
        Test instance = NbModuleSuite.create(load);
        junit.textui.TestRunner.run(instance);

        assertEquals("OK", System.getProperty("en.one"));
    }
     */

    public void testModulesForCL() throws Exception {
        Set<String> s = NbModuleSuite.S.findEnabledModules(ClassLoader.getSystemClassLoader());
        s.remove("org.netbeans.modules.nbjunit");
        assertEquals("Four modules left: " + s, 5, s.size());

        assertTrue("Util: " + s, s.contains("org.openide.util.ui"));
        assertTrue("Util: " + s, s.contains("org.openide.util"));
        assertTrue("Lookup: " + s, s.contains("org.openide.util.lookup"));
        assertTrue("junit: " + s, s.contains("org.netbeans.libs.junit4"));
        assertTrue("insane: " + s, s.contains("org.netbeans.insane"));
    }

    public void testModulesForMe() throws Exception {
        Set<String> s = NbModuleSuite.S.findEnabledModules(getClass().getClassLoader());
        s.remove("org.netbeans.modules.nbjunit");
        assertEquals("Four modules left: " + s, 5, s.size());

        assertTrue("Util: " + s, s.contains("org.openide.util.ui"));
        assertTrue("Util: " + s, s.contains("org.openide.util"));
        assertTrue("Lookup: " + s, s.contains("org.openide.util.lookup"));
        assertTrue("JUnit: " + s, s.contains("org.netbeans.libs.junit4"));
        assertTrue("insane: " + s, s.contains("org.netbeans.insane"));
    }

    public void testAddSuite() throws Exception{
        System.setProperty("t.one", "No");
        NbModuleSuite.Configuration conf = NbModuleSuite.emptyConfiguration();
        conf = conf.addTest(TS.class).gui(false);
        junit.textui.TestRunner.run(conf.suite());
        assertProperty("t.one", "OK");
    }

    private static boolean isExtIDE() throws URISyntaxException {
        return isCluster("extide");
    }

    static boolean isCluster(String name) throws URISyntaxException {
        URL where = NbModuleSuite.class.getProtectionDomain().getCodeSource().getLocation();
        File nbjunitJAR = Utilities.toFile(where.toURI());
        assertTrue(nbjunitJAR.exists());
        File harness = nbjunitJAR.getParentFile().getParentFile();
        assertEquals("harness", harness.getName());
        File root = harness.getParentFile();
        return new File(root, "extide").isDirectory();
    }

    public static class TS extends NbTestSuite{

        public TS() {
            super(NbModuleSuiteT.class);
        }
    }

    public void testRunSuiteNoSimpleTests() throws Exception{
        System.setProperty("s.one", "No");
        System.setProperty("s.two", "No");
        System.setProperty("nosuit", "OK");
        NbModuleSuite.Configuration conf = NbModuleSuite.emptyConfiguration().gui(false);
        junit.textui.TestRunner.run(conf.addTest(NbModuleSuiteS.class).suite());
        assertProperty("s.one", "OK");
        assertProperty("s.two", "OK");
        assertProperty("nosuit", "OK");
    }

    public void testRunEmptyConfiguration() throws Exception{
        junit.textui.TestRunner.run(NbModuleSuite.emptyConfiguration().gui(false).suite());
    }

    public void testAddTestCase()throws Exception{
        System.setProperty("t.one", "No");
        Test instance =
            NbModuleSuite.emptyConfiguration().addTest(NbModuleSuiteT.class).gui(false)
                .suite();
        junit.textui.TestRunner.run(instance);

        assertProperty("t.one", "OK");
    }
    
    public void testAddStartupArgument()throws Exception{
        System.setProperty("t.arg", "No");

        Test instance =
            NbModuleSuite.createConfiguration(NbModuleSuiteT.class)
                .gui(false)
                .addStartupArgument("--branding", "sample")
                .suite();

        junit.textui.TestRunner.run(instance);

        assertProperty("t.arg", "OK");
    }
}
