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
package org.netbeans.core.validation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.Util;
import org.netbeans.core.startup.AutomaticDependencies;
import org.netbeans.core.startup.ConsistencyVerifier;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Dependency;

public class ValidateModulesTest extends NbTestCase {
    protected boolean ignoreEager;
    
    static {
        System.setProperty("java.awt.headless", "true");
    }
    
    public ValidateModulesTest(String n) {
        super(n);
    }

    @Override
    protected int timeOut() {
        return 60000;
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new ValidateModulesTest("clusterVersions"));
        suite.addTest(NbModuleSuite.createConfiguration(ValidateModulesTest.class).addTest("deprecatedModulesAreDisabled").
                clusters("(?!extra$).*").enableModules(".*").honorAutoloadEager(true).gui(false).enableClasspathModules(false).suite());
        suite.addTest(NbModuleSuite.createConfiguration(ValidateModulesTest.class).
                clusters(".*").enableModules(".*").honorAutoloadEager(true).gui(false).enableClasspathModules(false).suite());
        suite.addTest(NbModuleSuite.createConfiguration(ValidateModulesTest.NoStrictEager.class).
                clusters("platform|harness|ide|webcommon|websvccommon|java|profiler|nb|extide").enableModules(".*").
                honorAutoloadEager(true).gui(false).enableClasspathModules(false).suite());
        suite.addTest(NbModuleSuite.createConfiguration(ValidateModulesTest.NoStrictEager.class).
                clusters("platform|harness|ide|extide").enableModules(".*").honorAutoloadEager(true).gui(false).enableClasspathModules(false).suite());
        return suite;
    }
    
    public void testInvisibleModules() throws Exception {
        Set<Manifest> manifests = loadManifests();
        Set<String> requiredBySomeone = new HashSet<String>();
        for (Manifest m : manifests) {
            String deps = m.getMainAttributes().getValue("OpenIDE-Module-Module-Dependencies");
            if (deps != null) {
                String identifier = "[\\p{javaJavaIdentifierStart}][\\p{javaJavaIdentifierPart}]*";
                Matcher match = Pattern.compile(identifier + "(\\." + identifier + ")*").matcher(deps);
                while (match.find()) {
                    requiredBySomeone.add(match.group());
                }
            }
        }
        StringBuilder auVisibilityProblems = new StringBuilder();
        String[] markers = {"autoload", "eager", "AutoUpdate-Show-In-Client", "AutoUpdate-Essential-Module"};
        MODULE: for (Manifest m : manifests) {
            String cnb = findCNB(m);
            if (requiredBySomeone.contains(cnb)) {
                continue;
            }
            Attributes attr = m.getMainAttributes();
            for (String marker : markers) {
                if ("true".equals(attr.getValue(marker))) {
                    continue MODULE;
                }
            }
            auVisibilityProblems.append("\n").append(cnb);
        }
        if (auVisibilityProblems.length() > 0) {
            fail("Some regular modules (that no one depends on) neither AutoUpdate-Show-In-Client=true nor AutoUpdate-Essential-Module=true (thus unreachable through Plugin Manager)" + auVisibilityProblems);
        }
    }
    
    public void testPluginDisplay() throws Exception {
        StringBuilder problems = new StringBuilder();
        for (Module mod : Main.getModuleSystem().getManager().getModules()) {
            if ("false".equals(mod.getAttribute("AutoUpdate-Show-In-Client"))) {
                continue;
            }
            if (mod.getAttribute("Bundle-SymbolicName") != null &&
                mod.getAttribute("AutoUpdate-Show-In-Client") == null
            ) {
                continue;
            }
            if (mod.getLocalizedAttribute("OpenIDE-Module-Display-Category") == null) {
                problems.append('\n').append(mod.getCodeNameBase());
            }
        }
        if (problems.length() > 0) {
            fail("Some modules are AutoUpdate-Show-In-Client=true but have no specified OpenIDE-Module-Display-Category" + problems);
        }
    }
    
    private static Map<String, String> SPECIAL_MODULES = new HashMap<>();
    
    static {
        // delivered through AutoUpdate
        SPECIAL_MODULES.put("org.netbeans.modules.java.source.nbjavac", "org.netbeans.modules.nbjavac");
        SPECIAL_MODULES.put("org.netbeans.libs.nashorn", "com.oracle.js.parser.implementation");
        
        // has dependency from nb to webcommon cluster
        SPECIAL_MODULES.put("org.netbeans.modules.ko4j.debugging", "org.netbeans.modules.web.browser.api.PageInspector");
    }

    public void testConsistency() throws Exception {
        LogHandler h = new LogHandler();
        Util.err.addHandler(h);
        
        Set<Manifest> manifests = loadManifests();
        SortedMap<String,SortedSet<String>> problems = ConsistencyVerifier.findInconsistencies(manifests, null);
        if (!problems.isEmpty()) {
            StringBuilder message = new StringBuilder();

            // 1st pass: start with modules, which are excluded from the check
            Set<String> excludedModules = new HashSet<>();
            boolean excludesChanged = false;
            
            for (Map.Entry<String, SortedSet<String>> entry : problems.entrySet()) {
                String affectedModule = entry.getKey();
                String msg = entry.getValue().first();
                String reason = SPECIAL_MODULES.get(affectedModule);
                if (reason != null && msg.contains(reason)) {
                    excludedModules.add(affectedModule);
                    excludesChanged = true;
                }
            }
            
            while (excludesChanged) {
                problems.keySet().removeAll(excludedModules);
                excludesChanged = false;
                O: for (Map.Entry<String, SortedSet<String>> entry : problems.entrySet()) {
                    String affectedModule = entry.getKey();
                    String msg = entry.getValue().first();
                    
                    for (String m : excludedModules) {
                        int pos = msg.indexOf(m);
                        if (pos != -1) {
                            pos += m.length();
                            // check if it is not just a prefix
                            if (msg.length() <= pos ||
                                ((msg.charAt(pos) != '.' && !Character.isAlphabetic(msg.charAt(pos))))) {
                                excludedModules.add(affectedModule);
                                excludesChanged = true;
                                break O;
                            }
                        }
                    }
                }
            }
            
            // next passes: add modules, which are disabled because of already "excluded" modules
            for (Map.Entry<String, SortedSet<String>> entry : problems.entrySet()) {
                String affectedModule = entry.getKey();
                if (ignoreEager) {
                    // eager modules may depend on other cluster's modules, which are not part
                    // of the test.
                    Module m = Main.getModuleSystem().getManager().get(affectedModule);
                    if (m != null && m.isEager()) {
                        continue;
                    }
                }
                message.append("\nProblems found for module ").append(entry.getKey()).append(": ").append(entry.getValue());
            }
            if (!h.warnings.isEmpty()) {
                message.append("\nWarnings were logged: ");
                for (LogRecord lr : h.warnings) {
                    message.append("\n").append(new SimpleFormatter().format(lr));
                }
            }
            if (message.length() == 0) {
                return;
            }
            message.insert(0, "Problems found with autoloads");
            fail(message.toString());
        }
    }

    public void clusterVersions() throws Exception {
        for (String clusterLocation : System.getProperty("cluster.path.final").split(File.pathSeparator)) {
            File cluster = new File(clusterLocation);
            if (cluster.getName().matches("ergonomics|harness|extra")) {
                // Not used for module dependencies, so exempted.
                continue;
            }
            if (cluster.isDirectory()) {
                assertTrue("found a VERSION.txt in " + cluster, new File(cluster, "VERSION.txt").isFile());
            }
        }
    }

    public void testAutomaticDependenciesUnused() throws Exception {
        List<URL> urls = new ArrayList<URL>();
        for (FileObject kid : FileUtil.getConfigFile("ModuleAutoDeps").getChildren()) {
            urls.add(kid.toURL());
        }
        StringBuilder problems = new StringBuilder();
        AutomaticDependencies ad = AutomaticDependencies.parse(urls.toArray(new URL[0]));
        for (Manifest m : loadManifests()) {
            String cnb = findCNB(m);
            AutomaticDependencies.Report r = ad.refineDependenciesAndReport(cnb,
                    Dependency.create(Dependency.TYPE_MODULE, m.getMainAttributes().getValue("OpenIDE-Module-Module-Dependencies")));
            if (r.isModified()) {
                problems.append('\n').append(r);
            }
        }
        if (problems.length() > 0) {
            fail("Some modules need to upgrade their dependencies" + problems);
        }
    }

    public void deprecatedModulesAreDisabled() {
        Set<String> cnbs = new TreeSet<String>();
        StringBuilder problems = new StringBuilder();
        for (Module m : Main.getModuleSystem().getManager().getModules()) {
            if ("true".equals(m.getAttribute("OpenIDE-Module-Deprecated"))) {
                String cnb = m.getCodeNameBase();
                if (cnb.equals("org.jdesktop.layout") || cnb.equals("org.netbeans.modules.editor.deprecated.pre65formatting") ||
                    cnb.equals("org.netbeans.modules.java.hints.legacy.spi") || cnb.equals("org.netbeans.modules.editor.structure")) {
                    // Will take a while to fix, don't report as error now.
                    continue;
                }
                cnbs.add(cnb);
                if (m.isEnabled()) {
                    problems.append('\n').append(cnb).append(" is deprecated and should not be enabled");
                }
            }
        }
        if (problems.length() > 0) {
            fail("Some deprecated modules are in use" + problems);
        } else {
            System.out.println("Deprecated modules all correctly disabled: " + cnbs);
        }
    }

    private static Set<Manifest> loadManifests() throws Exception {
        ModuleManager mgr = Main.getModuleSystem().getManager();
        Set<Manifest> manifests = new HashSet<Manifest>();
        boolean foundJUnit = false;
        for (Module m : mgr.getModules()) {
            Manifest manifest = new Manifest(m.getManifest());
            if (m.isAutoload()) {
                manifest.getMainAttributes().putValue("autoload", "true");
            } else if (m.isEager()) {
                manifest.getMainAttributes().putValue("eager", "true");
            }
            manifests.add(manifest);
            if ("org.netbeans.libs.junit4".equals(manifest.getMainAttributes().getValue("OpenIDE-Module"))) {
                foundJUnit = true;
            }
        }
        if (!foundJUnit) { // hack - pretend that this module is still in the platform cluster
            manifests.add(new Manifest(new ByteArrayInputStream("OpenIDE-Module: org.netbeans.libs.junit4\nOpenIDE-Module-Specification-Version: 1.14\n\n".getBytes())));
        }
        return manifests;
    }

    private static String findCNB(Manifest m) {
        String name = m.getMainAttributes().getValue("OpenIDE-Module");
        if (name == null) {
            name = m.getMainAttributes().getValue("Bundle-SymbolicName");
            if (name == null) {
                throw new IllegalArgumentException();
            }
        }
        return name.replaceFirst("/\\d+$", "");
    }
    
    public static class NoStrictEager extends ValidateModulesTest {
        public NoStrictEager(String n) {
            super(n);
            ignoreEager = true;
        }
    }

    class LogHandler extends Handler {
        List<LogRecord> warnings = new ArrayList<>();
        
        @Override
        public void publish(LogRecord record) {
            if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                warnings.add(record);
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
        
    }
}
