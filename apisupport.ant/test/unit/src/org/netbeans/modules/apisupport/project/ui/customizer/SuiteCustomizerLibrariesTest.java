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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.EventQueue;
import org.netbeans.modules.apisupport.project.universe.ClusterUtils;
import java.io.File;
import java.util.*;
import java.util.jar.Manifest;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.apisupport.project.ApisupportAntUtils;
import org.netbeans.modules.apisupport.project.api.EditableManifest;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.explorer.ExplorerManager;
import org.openide.modules.Dependency;
import org.openide.modules.SpecificationVersion;
import org.openide.nodes.Children;

/**
 * Tests module dependencies in a suite.
 * @author Jesse Glick
 */
public class SuiteCustomizerLibrariesTest extends TestBase {
    
    public SuiteCustomizerLibrariesTest(String name) {
        super(name);
    }

    private NbPlatform platform;
    private SuiteProject suite;
    private File install;

    protected void setUp() throws Exception {
        noDataDir = true;   // self-contained test; prevents name clash with 'custom' platform in data dir
        super.setUp();
        // PLATFORM SETUP
        install = new File(getWorkDir(), "install");
        TestBase.makePlatform(install);
        // MODULE foo
        Manifest mani = new Manifest();
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE, "foo/1");
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE_SPECIFICATION_VERSION, "1.0");
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE_IMPLEMENTATION_VERSION, "foo-1");
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE_LOCALIZING_BUNDLE, "foo/Bundle.properties");
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE_PROVIDES, "tok1, tok1a");
        Map<String,String> contents = new HashMap<String,String>();
        contents.put("foo/Bundle.properties", "OpenIDE-Module-Name=Foo Module");
        TestBase.createJar(new File(new File(new File(install, "somecluster"), "modules"), "foo.jar"), contents, mani);
        // MODULE bar
        mani = new Manifest();
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE, "bar");
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE_REQUIRES, "tok1");
        TestBase.createJar(new File(new File(new File(install, "somecluster"), "modules"), "bar.jar"), Collections.<String,String>emptyMap(), mani);
        // MODULE foo2
        mani = new Manifest();
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE, "foo2");
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE_PROVIDES, "tok1b");
        contents = new HashMap<String,String>();
        TestBase.createJar(new File(new File(new File(install, "somecluster"), "modules"), "foo2.jar"), contents, mani);
        // MODULE bar2
        mani = new Manifest();
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE, "bar2");
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE_NEEDS, "tok1b");
        TestBase.createJar(new File(new File(new File(install, "somecluster"), "modules"), "bar2.jar"), Collections.<String,String>emptyMap(), mani);
        // MODULE baz
        mani = new Manifest();
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE, "baz");
        mani.getMainAttributes().putValue("OpenIDE-Module-Module-Dependencies", "foo/1 > 1.0");
        mani.getMainAttributes().putValue("OpenIDE-Module-Requires", "org.openide.modules.ModuleFormat1, org.openide.modules.os.Windows");
        TestBase.createJar(new File(new File(new File(install, "anothercluster"), "modules"), "baz.jar"), Collections.<String,String>emptyMap(), mani);
        // MODULE bunnel
        mani = new Manifest();
        mani.getMainAttributes().putValue(ManifestManager.BUNDLE_SYMBOLIC_NAME, "bunnel");
        mani.getMainAttributes().putValue(ManifestManager.BUNDLE_EXPORT_PACKAGE, "bunnel.api,bunnel.spi");
        mani.getMainAttributes().putValue(ManifestManager.BUNDLE_IMPORT_PACKAGE, "javax.crypto,javax.crypto.spec,javax.crypto.interfaces,org.ietf.jgss,org.osgi.service.event;version=\"1.0.0\";resolution:=optional");
        TestBase.createJar(new File(new File(new File(install, "somecluster"), "modules"), "bunnel.jar"), Collections.<String,String>emptyMap(), mani);
        platform = NbPlatform.addPlatform("custom", install, "custom");
        // SUITE setup
        suite = TestBase.generateSuite(getWorkDir(), "suite", "custom");
        // MODULE org.example.module1
        NbModuleProject module = TestBase.generateSuiteComponent(suite, "module1");
        EditableManifest em = Util.loadManifest(module.getManifestFile());
        em.setAttribute(ManifestManager.OPENIDE_MODULE, "org.example.module1/2", null);
        em.setAttribute(ManifestManager.OPENIDE_MODULE_SPECIFICATION_VERSION, "2.0", null);
        em.setAttribute(ManifestManager.OPENIDE_MODULE_PROVIDES, "tok2", null);
        Util.storeManifest(module.getManifestFile(), em);
        LocalizedBundleInfo lbinfo = module.getLookup().lookup(LocalizedBundleInfo.Provider.class).getLocalizedBundleInfo();
        lbinfo.setDisplayName("Module One");
        lbinfo.store();
        // MODULE org.example.module2
        module = TestBase.generateSuiteComponent(suite, "module2");
        em = Util.loadManifest(module.getManifestFile());
        em.removeAttribute(ManifestManager.OPENIDE_MODULE_SPECIFICATION_VERSION, null);
        em.setAttribute(ManifestManager.OPENIDE_MODULE_REQUIRES, "tok2", null);
        Util.storeManifest(module.getManifestFile(), em);
        lbinfo = module.getLookup().lookup(LocalizedBundleInfo.Provider.class).getLocalizedBundleInfo();
        lbinfo.setDisplayName("Module Two");
        lbinfo.store();
        // MODULE org.example.module3
        module = TestBase.generateSuiteComponent(suite, "module3");
        ApisupportAntUtils.addDependency(module, "org.example.module2", null, null, true, null);
        ApisupportAntUtils.addDependency(module, "bar", null, null, true, null);
        lbinfo = module.getLookup().lookup(LocalizedBundleInfo.Provider.class).getLocalizedBundleInfo();
        lbinfo.setDisplayName("Module Three");
        lbinfo.store();
        ProjectManager.getDefault().saveAllProjects();
    }
    
    public void testUniverseModules() throws Exception { // #65924
        Map<String,SuiteCustomizerLibraries.UniverseModule> modulesByName = new HashMap<String,SuiteCustomizerLibraries.UniverseModule>();
        Set<SuiteCustomizerLibraries.UniverseModule> modules = SuiteCustomizerLibraries.loadUniverseModules(platform.getSortedModules(),
                SuiteUtils.getSubProjects(suite), Collections.<ModuleEntry>emptySet());
        for (SuiteCustomizerLibraries.UniverseModule m : modules) {
            modulesByName.put(m.getCodeNameBase(), m);
        }
        assertEquals(modules.size(), modulesByName.size());
        SuiteCustomizerLibraries.UniverseModule m = modulesByName.get("core");
        assertNotNull(m);
        // core.jar is just a dummy JAR, nothing interesting to test
        m = modulesByName.get("foo");
        assertNotNull(m);
        assertEquals(new File(install, "somecluster"), m.getCluster());
        assertEquals("Foo Module", m.getDisplayName());
        assertEquals(1, m.getReleaseVersion());
        assertEquals(new SpecificationVersion("1.0"), m.getSpecificationVersion());
        assertEquals("foo-1", m.getImplementationVersion());
        assertEquals(new TreeSet<String>(Arrays.asList("tok1", "tok1a")), assertProvidedTokens(m));
        assertEquals(Collections.EMPTY_SET, m.getRequiredTokens());
        assertEquals(Collections.EMPTY_SET, m.getModuleDependencies());
        m = modulesByName.get("bar");
        assertNotNull(m);
        assertEquals(Collections.EMPTY_SET, assertProvidedTokens(m));
        assertEquals(Collections.singleton("tok1"), m.getRequiredTokens());
        m = modulesByName.get("baz");
        assertNotNull(m);
        assertEquals(Dependency.create(Dependency.TYPE_MODULE, "foo/1 > 1.0"), m.getModuleDependencies());
        m = modulesByName.get("org.example.module1");
        assertNotNull(m);
        assertEquals(m.getCluster(), ClusterUtils.getClusterDirectory(suite));
        assertEquals("Module One", m.getDisplayName());
        assertEquals(2, m.getReleaseVersion());
        assertEquals(new SpecificationVersion("2.0"), m.getSpecificationVersion());
        assertNull(m.getImplementationVersion());
        assertEquals(Collections.singleton("tok2"), assertProvidedTokens(m));
        assertEquals(Collections.EMPTY_SET, m.getRequiredTokens());
        assertEquals(Collections.EMPTY_SET, m.getModuleDependencies());
        m = modulesByName.get("org.example.module2");
        assertNotNull(m);
        assertEquals(-1, m.getReleaseVersion());
        assertNull(m.getSpecificationVersion());
        assertNull(m.getImplementationVersion());
        assertEquals(Collections.EMPTY_SET, assertProvidedTokens(m));
        assertEquals(Collections.singleton("tok2"), m.getRequiredTokens());
        m = modulesByName.get("org.example.module3");
        assertNotNull(m);
        assertEquals(Dependency.create(Dependency.TYPE_MODULE, "org.example.module2, bar"), m.getModuleDependencies());
        m = modulesByName.get("bunnel");
        assertNotNull(m);
        assertEquals("[bunnel, bunnel.api, bunnel.spi]", assertProvidedTokens(m).toString());
        assertEquals("[]", m.getRequiredTokens().toString());
    }

    @RandomlyFails // NB-Core-Build #4183: expected:<[ERR_excluded_dep, Module Three, suite, bar, somecluster]> but was:<null>
    public void testDependencyWarnings() throws Exception { // #65924
        final SuiteProperties suiteProps = new SuiteProperties(suite, suite.getHelper(), suite.getEvaluator(), Collections.<NbModuleProject>emptySet());
        final Category cat = Category.create("dummy", "dummy", null);
        final SuiteCustomizerLibraries[] ref = new SuiteCustomizerLibraries[1];
        Runnable r = new Runnable() {
            public void run() {
                // OutlineView in SCL must be initialized in EQ
                ref[0] = new SuiteCustomizerLibraries(suiteProps, cat);
            }
        };
        EventQueue.invokeAndWait(r);
        SuiteCustomizerLibraries scl = ref[0];
        Set<SuiteCustomizerLibraries.UniverseModule> modules = SuiteCustomizerLibraries.loadUniverseModules(platform.getSortedModules(), SuiteUtils.getSubProjects(suite), Collections.<ModuleEntry>emptySet());
        Set<File> allClusters = new HashSet<File>(Arrays.asList(
                new File(install, "somecluster"), new File(install, "anothercluster"), ClusterUtils.getClusterDirectory(suite)));
        assertEquals(null, join(scl.findWarning(modules, allClusters, Collections.<String>emptySet()).warning));
        /* XXX failing, investigate:
        assertEquals("[ERR_excluded_dep, baz, anothercluster, Foo Module, somecluster]",
                join(scl.findWarning(modules, Collections.singleton(new File(install, "anothercluster")), Collections.<String>emptySet()).warning));
         */
        assertNull(join(scl.findWarning(modules, Collections.singleton(new File(install, "somecluster")), Collections.<String>emptySet()).warning));
        assertEquals("[ERR_excluded_dep, Module Three, suite, bar, somecluster]",
                join(scl.findWarning(modules, allClusters, Collections.singleton("bar")).warning));
        /* XXX failing, investigate:
        assertEquals("[ERR_only_excluded_providers, tok1, bar, somecluster, Foo Module, somecluster]",
                join(scl.findWarning(modules, Collections.singleton(ClusterUtils.getClusterDirectory(suite)), Collections.<String>emptySet()).warning));
         */
        assertEquals("[ERR_only_excluded_providers, tok1, bar, somecluster, Foo Module, somecluster]",
                join(scl.findWarning(modules, allClusters, Collections.singleton("foo")).warning));
        assertEquals("[ERR_only_excluded_providers, tok1b, bar2, somecluster, foo2, somecluster]",
                join(scl.findWarning(modules, allClusters, Collections.singleton("foo2")).warning));
        // XXX much more could be tested; check coverage results
    }
    
    public void testClusterAndModuleNodesEnablement() throws Exception {    // #70714
        final SuiteProperties suiteProps = new SuiteProperties(suite, suite.getHelper(), suite.getEvaluator(), Collections.<NbModuleProject>emptySet());
        final Category cat = Category.create("dummy", "dummy", null);
        final SuiteCustomizerLibraries[] ref = new SuiteCustomizerLibraries[1];
        Runnable r = new Runnable() {
            public void run() {
                // OutlineView in SCL must be initialized in EQ
                ref[0] = new SuiteCustomizerLibraries(suiteProps, cat);
            }
        };
        EventQueue.invokeAndWait(r);
        SuiteCustomizerLibraries scl = ref[0];
        SuiteCustomizerLibraries.TEST = true;
        scl.refresh();
        
        ExplorerManager mgr = scl.getExplorerManager();
        assertNotNull(mgr);
        Children clusters = mgr.getRootContext().getChildren();

        /* XXX reenable once SCL uses Children.create w/ asynch factory:
        // disable 'somecluster', all its modules should be disabled
        Enabled sc = (Enabled) clusters.findChild("somecluster");
        sc.setEnabled(false);
        assertFalse("Cluster \"somecluster\" is enabled!", sc.isEnabled());
        for (Node ch : sc.getChildren().getNodes()) {
            if (ch instanceof Enabled) {
                Enabled en = (Enabled) ch;
                assertFalse("Module node under disabled cluster is enabled!", en.isEnabled());
            }
        }

        // enable one of its modules, it should enable the cluster (and keep the others disabled)
        Enabled bar = (Enabled) sc.getChildren().findChild("bar");
        bar.setEnabled(true);
        assertTrue("Cluster is disabled.", sc.isEnabled());
        for (Node ch : sc.getChildren().getNodes()) {
            if (ch instanceof Enabled) {
                Enabled en = (Enabled) ch;
                if ("bar".equals(en.getName())) {
                    assertTrue("Module \"bar\" is disabled!", en.isEnabled());
                } else {
                    assertFalse("Module \"" + en.getName() + "\" is enabled!", en.isEnabled());
                }
            }
        }
        
        // disabling the only module of "anothercluster" should disable it as well
        Enabled ac = (Enabled) clusters.findChild("anothercluster");
        assertTrue("Cluster \"anothercluster\" is disabled!", ac.isEnabled());
        Enabled baz = (Enabled) ac.getChildren().findChild("baz");
        baz.setEnabled(false);
        assertFalse("Module \"baz\" is enabled!", baz.isEnabled());
        assertFalse("Cluster \"anothercluster\" is enabled!", ac.isEnabled());

        // check stored EnabledClusters and DisabledModules
        scl.store();
        SuiteProperties props = scl.getProperties();
        String[] ec = props.getEnabledClusters();
        Arrays.sort(ec);
        // "anothercluster" should be disabled
        assertTrue("Wrong clusters disabled!", Arrays.deepEquals(ec, new String[] { "harness", "platform", "somecluster" }));
        String[] dm = props.getDisabledModules();
        Arrays.sort(dm);
        // although "baz" has been disabled, it shouldn't appear here, as its cluster is disabled 
        assertTrue("Wrong modules disabled!", Arrays.deepEquals(dm, new String[] { "bar2", "foo", "foo2" }));
         */
 }
    
    private static String join(String[] elements) {
        if (elements != null) {
            return Arrays.asList(elements).toString();
        } else {
            return null;
        }
    }
    private static Set<String> assertProvidedTokens(SuiteCustomizerLibraries.UniverseModule mm) {
        Set<String> arr = new TreeSet<String>(mm.getProvidedTokens());
        if (!arr.remove("cnb." + mm.getCodeNameBase())) {
            fail("There should be cnb." + mm.getCodeNameBase() + " in " + arr);
        }
        return arr;
    }
    
}
