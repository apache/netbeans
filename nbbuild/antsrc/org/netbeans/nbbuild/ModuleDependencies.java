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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.FileSet;

/** This task implements the module dependencies verification proposal
 * that is described at
 * http://openide.netbeans.org/proposals/arch/clusters.html#verify-solution
 */
public class ModuleDependencies extends Task {
    private List<Input> inputs = new ArrayList<>();
    private List<Output> outputs = new ArrayList<>();
    private Set<ModuleInfo> modules;
    private Pattern regexp;
    
    public ModuleDependencies () {
    }
    
    public void setGenerate(String regexpList) {
        regexp = Pattern.compile(regexpList);
    }
    
    public Input createInput() throws BuildException {
        Input input = new Input ();
        inputs.add (input);
        return input;
    }

    public void addConfiguredInputPattern(InputPattern pattern) throws BuildException {
        inputs.addAll(pattern.inputs());
    }
    
    public Output createOutput() throws BuildException {
        Output output = new Output ();
        outputs.add (output);
        return output;
    }

    public @Override void execute() throws BuildException {
        if (outputs.size () == 0) throw new BuildException ("At least one <output> tag has to be specified");

        try {
            readModuleInfo ();

            for (Output o : outputs) {
                if (o.type == null) throw new BuildException ("<output> needs attribute type");
                if (o.file == null) throw new BuildException ("<output> needs attribute file");
                
                if ("public-packages".equals (o.type.getValue ())) {
                    generatePublicPackages (o.file, true, false);
                } else if ("friend-packages".equals (o.type.getValue ())) {
                    generatePublicPackages (o.file, false, false);
                } else if ("shared-packages".equals (o.type.getValue ())) {
                    generateSharedPackages (o.file);
                } else if ("modules".equals (o.type.getValue ())) {
                    generateListOfModules (o.file);
                } else if ("disabled-autoloads".equals(o.type.getValue())) {
                    generateListOfDisabledAutoloads(o.file);
                } else if ("dependencies".equals (o.type.getValue ())) {
                    generateDependencies (o.file, false);                    
                } else if ("implementation-dependencies".equals (o.type.getValue ())) {
                    generateDependencies (o.file, true);                    
                } else if ("group-dependencies".equals (o.type.getValue ())) {
                    generateGroupDependencies (o.file, false);                    
                } else if ("group-implementation-dependencies".equals (o.type.getValue ())) {
                    generateGroupDependencies (o.file, true);                    
                } else if ("group-friend-packages".equals (o.type.getValue ())) {
                    generatePublicPackages(o.file, false, true);                    
                } else if ("kits".equals(o.type.getValue())) {
                    generateKits(o.file);
                } else if ("kit-dependencies".equals(o.type.getValue())) {
                    generateKitDependencies(o.file);
                } else if ("plugins".equals(o.type.getValue())) {
                    generatePlugins(o.file);
                } else if ("reverse-dependencies".equals(o.type.getValue())) {
                    generateReverseDependencies(o.file);
                } else {
                    assert false : o.type;
                }
                getProject().log(o.file + ": generating " + o.type);
            }
        
        } catch (IOException ex) {
            throw new BuildException (ex);
        }
    }
    
    private void readModuleInfo () throws IOException {
        modules = new TreeSet<>();
        
        if (inputs.isEmpty()) {
            throw new BuildException ("At least one <input> tag is needed");
        }
        for (Input input : inputs) {
            if (input.jars == null) throw new BuildException ("<input> needs a subelement <jars>");
            if (input.name == null) throw new BuildException ("<input> needs attribute name");
            
            Project p = getProject();
            DirectoryScanner scan = input.jars.getDirectoryScanner(p);
            for (String incl : scan.getIncludedFiles()) {
                File f = new File(scan.getBasedir(), incl);
                getProject().log("Processing " + f, Project.MSG_VERBOSE);
                JarFile file = new JarFile (f);
                
                Manifest manifest = file.getManifest();
                if (manifest == null) {
                    // process only manifest files
                    continue;
                }

                final boolean[] osgi = new boolean[1];
                String module = JarWithModuleAttributes.extractCodeName(manifest.getMainAttributes(), osgi);

                if (module == null) {
                    // skip this one
                    continue;
                }


                ModuleInfo m;
                {
                    String codebasename;
                    int majorVersion;
                    // base name
                    int slash = module.indexOf ('/');
                    if (slash == -1) {
                        codebasename = module;
                        majorVersion = -1;
                    } else {
                        codebasename = module.substring (0, slash);
                        majorVersion = Integer.parseInt(module.substring(slash + 1));
                    }
                    m = new ModuleInfo (input.name, f, codebasename);
                    m.majorVersion = majorVersion;
                }

                String lb = file.getManifest().getMainAttributes().getValue("OpenIDE-Module-Localizing-Bundle");
                if (lb != null) {
                    Properties props = new Properties();
                    try (InputStream is = file.getInputStream(file.getEntry(lb))) {
                        props.load(is);
                    }
                    m.displayName = props.getProperty("OpenIDE-Module-Name");
                    m.displayCategory = props.getProperty("OpenIDE-Module-Display-Category");
                }

                // XXX if osgi[0], instead load Export-Package, Require-Bundle, Bundle-Version... ought to be some utility class to interconvert NB & OSGi manifests!

                m.publicPackages = file.getManifest ().getMainAttributes ().getValue ("OpenIDE-Module-Public-Packages");

                {
                    m.specificationVersion = file.getManifest ().getMainAttributes ().getValue ("OpenIDE-Module-Specification-Version");
                }

                m.implementationVersion = file.getManifest ().getMainAttributes ().getValue ("OpenIDE-Module-Implementation-Version");

                TreeSet<Dependency> depends = new TreeSet<>();
                TreeSet<Dependency> provides = new TreeSet<>();
                addDependencies (depends, file.getManifest (), Dependency.Type.REQUIRES, "OpenIDE-Module-Requires");
                addDependencies (depends, file.getManifest (), Dependency.Type.REQUIRES, "OpenIDE-Module-Needs");
                addDependencies (depends, file.getManifest (), Dependency.Type.RECOMMENDS, "OpenIDE-Module-Recommends");
                addDependencies (provides, file.getManifest (), /*irrelevant*/Dependency.Type.REQUIRES, "OpenIDE-Module-Provides");
                {
                    String ideDeps = file.getManifest ().getMainAttributes ().getValue ("OpenIDE-Module-IDE-Dependencies"); // IDE/1 > 4.25
                    if (ideDeps != null) {
                        throw new BuildException("OpenIDE-Module-IDE-Dependencies is obsolete in " + f);
                    }
                }
                addDependencies (depends, file.getManifest (), Dependency.Type.DIRECT, "OpenIDE-Module-Module-Dependencies");
                /* org.netbeans.api.java/1,org.netbeans.modules.queries/0,
                 org.netbeans.modules.javacore/1,org.netbeans.jmi.javamodel/1 > 1.11,org.netbeans.api.mdr/1,
                 org.netbeans.modules.mdr/1= 1.0.0,org.netbeans.modules.
                 jmiutils/1 = 1.0.0,javax.jmi.reflect/1,
                 org.openide.loaders,org.openide.src > 1.0
                 */
                m.depends = depends;
                m.provides = new HashSet<>();
                for (Dependency d : provides) {
                    m.provides.add(d.getName());
                }
                {
                    String friends = file.getManifest ().getMainAttributes ().getValue ("OpenIDE-Module-Friends"); 
                    if (friends != null) {
			TreeSet<String> set = new TreeSet<>();
                        StringTokenizer tok = new StringTokenizer(friends, ", ");
			while (tok.hasMoreElements()) {
			    set.add(tok.nextToken());
			}
			m.friends = set;
                    }
                }
                String essential = file.getManifest ().getMainAttributes ().getValue ("AutoUpdate-Essential-Module");
                m.isEssential = essential == null ? 
                    false : 
                    Boolean.parseBoolean(file.getManifest().getMainAttributes().getValue("AutoUpdate-Essential-Module"));
                m.isAutoload = determineParameter(f, "autoload");
                m.isEager = determineParameter(f, "eager");
                String showInAutoUpdate = file.getManifest().getMainAttributes().getValue("AutoUpdate-Show-In-Client");
                if (showInAutoUpdate == null) {
                    m.showInAutoupdate = !m.isAutoload && !m.isEager;
                } else {
                    m.showInAutoupdate = Boolean.parseBoolean(showInAutoUpdate);
                }
                modules.add (m);
            }
        }
    }
    
    private boolean determineParameter(File moduleFile, String parameter) throws IOException {
        String name = moduleFile.getName();
        name = name.substring(0, name.length() - 3) + "xml";
        File configFile = new File(moduleFile.getParentFile().getParentFile(), "config/Modules/" + name);
        log ("config " + configFile, Project.MSG_DEBUG);
        if (!configFile.exists())
            return true; // probably a classpath module, treat like autoload
        final String fragment = "<param name=\"" + parameter + "\">true</param>";
        try (BufferedReader br = new BufferedReader (new FileReader (configFile))) {
            String line;
            while ((line = br.readLine ()) != null) {
                if (line.indexOf (fragment) != -1) {
                    log ("autoload module: " + moduleFile, Project.MSG_DEBUG);
                    return true;
                }
            }
        }
        return false;
    }
    

    private void generatePublicPackages(File output, boolean justPublic, boolean justInterCluster) throws BuildException, IOException {
        TreeSet<String> packages = new TreeSet<>();
        TreeMap<ModuleInfo,TreeSet<String>> friendExports = new TreeMap<>();
        
        {
            for (ModuleInfo m : modules) {
                if (justPublic) {
                    if (m.friends != null) {
                        continue;
                    }
                }
                if (regexp != null && !regexp.matcher(m.group).matches()) {
                    continue;
                }

                String s = m.publicPackages;
                Map<String,Boolean> pkgs = null;
                if (s != null) {
                    pkgs = new HashMap<>();
                    StringTokenizer tok = new StringTokenizer(s, ",");
                    while (tok.hasMoreElements()) {
                        String p = tok.nextToken().trim();
                        if (p.equals("-")) {
                            continue;
                        }

                        if (p.endsWith(".*")) {
                            pkgs.put(p.substring(0, p.length() - 2).replace('.', '/'), Boolean.FALSE);
                            continue;
                        }
                        if (p.endsWith(".**")) {
                            pkgs.put(p.substring(0, p.length() - 3).replace('.', '/'), Boolean.TRUE);
                            continue;
                        }
                        throw new BuildException("Unknown package format: " + p + " in " + m.file);
                    }
                }

                if (justPublic) {
                    iterateThruPackages(m.file, pkgs, packages);
                    if (pkgs != null && packages.size() < pkgs.size()) {
                        throw new BuildException("Not enough packages found. The declared packages are: " + s + " but only " + packages + " were found in " + m.file);
                    }
                } else {
                    TreeSet<String> modulePkgs = new TreeSet<>();
                    iterateThruPackages(m.file, pkgs, modulePkgs);
                    friendExports.put(m, modulePkgs);
                }

            }
        }
        
        try (PrintWriter w = new PrintWriter(new FileWriter(output))) {
            if (justPublic) {
                for (String out : packages) {
                    w.println(out.replace('/', '.'));
                }
            } else {
                int maxFriends = Integer.MAX_VALUE;
                if (justInterCluster) {
                    String maxFriendsString = this.getProject().getProperty("deps.max.friends");
                    if (maxFriendsString != null) {
                        maxFriends = Integer.parseInt(maxFriendsString);
                    }
                }

                for (Map.Entry<ModuleInfo,TreeSet<String>> entry : friendExports.entrySet()) {
                    ModuleInfo info = entry.getKey();
                    if (info.friends == null) {
                        continue;
                    }
                    log("Friends for " + info.getName(false), Project.MSG_DEBUG);
                    int cntFriends = 0;
                    boolean printed = false;
                    for (String n : info.friends) {
                        ModuleInfo friend = findModuleInfo(n);
                        if (justInterCluster && friend != null && friend.group.equals(info.group)) {
                            continue;
                        }

                        if (!printed) {
                            w.print("MODULE ");
                            w.println(info.getName(false));
                            printed = true;
                        }

                        if (friend != null) {
                            w.print("  FRIEND ");
                            w.println(friend.getName(false));
                        } else {
                            w.print("  EXTERNAL ");
                            w.println(n);
                        }
                        cntFriends++;
                    }
                    if (cntFriends > maxFriends) {
                        w.println("  WARNING: excessive number of intercluster friends (" + cntFriends + ")");
                    }

                    if (cntFriends > 0) {
                        for (String out : entry.getValue()) {
                            w.print("  PACKAGE ");
                            w.println(out.replace('/', '.'));
                        }
                    }
                }
            }
        }
    }
    
    private void iterateThruPackages(File f, Map<String,Boolean> pkgs, TreeSet<String> packages) throws IOException {
        try (JarFile file = new JarFile(f)) {
            Enumeration<JarEntry> en = file.entries ();
            LOOP: while (en.hasMoreElements ()) {
                JarEntry e = en.nextElement ();
                if (e.getName().endsWith(".class")) {
                    int last = e.getName().lastIndexOf ('/');
                    if (last == -1) {
                        // skip default pkg
                        continue;
                    }
                    String p = e.getName().substring (0, last);

                    if (pkgs == null) {
                        packages.add (p);
                        continue;
                    }

                    Boolean b = pkgs.get(p);
                    if (b != null) {
                        packages.add (p);
                        continue;
                    }

                    String parent = p;
                    while (parent.length() > 0) {
                        int prev = parent.lastIndexOf ('/');
                        if (prev == -1) {
                            parent = "";
                        } else {
                            parent = parent.substring (0, prev);
                        }

                        b = pkgs.get(parent);
                        if (Boolean.TRUE.equals (b)) {
                            packages.add (p);
                            continue LOOP;
                        }
                    }
                }
            }

            java.util.jar.Manifest m = file.getManifest ();
            if (m != null) {
                String value = m.getMainAttributes ().getValue ("Class-Path");
                if (value != null) {
                    StringTokenizer tok = new StringTokenizer (value, " ");
                    while (tok.hasMoreElements ()) {
                        File sub = new File (f.getParentFile (), tok.nextToken ());
                        if (sub.isFile ()) {
                            iterateThruPackages (sub, pkgs, packages);
                        }
                    }
                }
            }
        }
    }

    private void generateListOfModules (File output) throws BuildException, IOException {
        try (PrintWriter w = new PrintWriter(new FileWriter(output))) {
            for (ModuleInfo m : modules) {
                if (regexp != null && !regexp.matcher(m.group).matches()) {
                    continue;
                }
                w.print("MODULE ");
                w.print(m.getName(true));
                w.println();
            }
        }
    }

    private void generateListOfDisabledAutoloads(File output) throws BuildException, IOException {
        Map<String,Set<String>> depsAll = new TreeMap<>();
        Map<String,ModuleInfo> considered = new TreeMap<>();
        Set<String> regular = new HashSet<>();
        for (ModuleInfo m : modules) {
            if (regexp != null && !regexp.matcher(m.group).matches()) {
                continue;
            }
            if (m.isAutoload) {
                considered.put(m.codebasename, m);
            } else if (!m.isEager) {
                regular.add(m.codebasename);
            }
            Set<String> deps = new TreeSet<>();
            depsAll.put(m.codebasename, deps);
            for (Dependency d : m.depends) {
                for (ModuleInfo m2 : findModuleInfo(d, m)) {
                    deps.add(m2.codebasename);
                }
            }
        }
        transitiveClosure(depsAll);
        Map<String,Set<ModuleInfo>> disabled = new TreeMap<>();
        for (Map.Entry<String, Set<String>> entry : depsAll.entrySet()) {
            if (!regular.contains(entry.getKey())) {
                continue;
            }

            considered.keySet().removeAll(entry.getValue());
        }
        for (ModuleInfo m : considered.values()) {
            Set<ModuleInfo> group = disabled.get(m.group);
            if (group == null) {
                group = new TreeSet<>();
                disabled.put(m.group, group);
            }
            group.add(m);
        }
        try (PrintWriter w = new PrintWriter(new FileWriter(output))) {
            for (Set<ModuleInfo> group : disabled.values()) {
                for (ModuleInfo m : group) {
                    w.print("MODULE ");
                    w.print(m.getName(false));
                    w.println();
                }
            }
        }
    }

    private void generateKits(File output) throws BuildException, IOException {
        // calculate transitive closure of kits
        try (PrintWriter w = new PrintWriter(new FileWriter(output))) {
            // calculate transitive closure of kits
            TreeMap<String, TreeSet<String>> allKitDeps = transitiveClosureOfKits();
            // calculate transitive closure of modules
            TreeMap<String, TreeSet<String>> allModuleDeps = transitiveClosureOfModules();
            // create a map of <module, kits that depend on it>
            TreeMap<ModuleInfo, Set<String>> dependingKits = new TreeMap<>();
            for (ModuleInfo m : modules) {
                if (regexp != null && !regexp.matcher(m.group).matches()) {
                    continue;
                }
                if (m.showInAutoupdate) {
                    // this is a kit
                    Set<String> dep = allModuleDeps.get(m.codebasename);
                    for (String ds : dep) {
                        if (regexp != null && !regexp.matcher(m.group).matches()) {
                            continue;
                        }
                        //log ("ds " + dep);
                        ModuleInfo theModuleOneIsDependingOn = findModuleInfo(ds);
                        if (!theModuleOneIsDependingOn.showInAutoupdate &&
                                !theModuleOneIsDependingOn.isAutoload &&
                                !theModuleOneIsDependingOn.isEager &&
                                !theModuleOneIsDependingOn.isEssential) {
                            // regular module, not a kit
                            Set<String> kits = dependingKits.get(theModuleOneIsDependingOn);
                            if (kits == null) {
                                kits = new TreeSet<>();
                                dependingKits.put(theModuleOneIsDependingOn, kits);
                            }
                            kits.add(m.getName(false));
//                            w.print("  REQUIRES " + theModuleOneIsDependingOn.getName());
//                            w.println();
                        }
                    }
                }
            }
            // now check that there is one canonical kit that "contains" the module
            // at the same time create a map of <kit, set of <module>>
            TreeMap<String, TreeSet<String>> allKits = new TreeMap<>();
            for (Map.Entry<ModuleInfo, Set<String>> it : dependingKits.entrySet()) {
                Set<String> kits = it.getValue();
                ModuleInfo module = it.getKey();

                // candidate for the lowest kit
                String lowestKitCandidate = null;
                for (String kit : kits) {
                    if (lowestKitCandidate == null) {
                        lowestKitCandidate = kit;
                        log ("  initial lowest kit candidate for " + module.getName(false) + " : " +
                                lowestKitCandidate, Project.MSG_DEBUG);
                    }
                    else {
                        if (dependsOnTransitively(lowestKitCandidate, kit, allKitDeps)) {
                            lowestKitCandidate = kit;
                            log ("  new lowest kit candidate for " + module.getName(false) + " : " +
                                    lowestKitCandidate, Project.MSG_DEBUG);
                        }
                    }
                }
                // check that all kits depend on the lowest kit candidate
                boolean passed = true;
                for (String kit : kits) {
                    if (!kit.equals(lowestKitCandidate) &&
                            !dependsOnTransitively(kit, lowestKitCandidate, allKitDeps)) {
                        log ("lowest kit not found for " + module.getName(false) + " : " +
                                lowestKitCandidate + ", " + kit + " do not have a dependency", Project.MSG_VERBOSE);
                        passed = false;
                        break;
                    }
                }
                if (passed) {
                    dependingKits.put(module, Collections.singleton(lowestKitCandidate));
                    registerModuleInKit(module, lowestKitCandidate, allKits);
                } else {
                    w.print("Warning: ambiguous module ownership - module ");
                    w.print(module.getName(false));
                    w.print(" is contained in kits ");
                    w.println();
                    for (String kit : kits) {
                        registerModuleInKit(module, kit, allKits);
                        w.print("  " + kit);
                        w.println();
                    }
                    w.println("No dependency between ");
                    for (String kit : kits) {
                        if (!kit.equals(lowestKitCandidate) &&
                                !dependsOnTransitively(kit, lowestKitCandidate, allKitDeps)) {
                            w.println ("  " + lowestKitCandidate + ", " + kit);
                        }
                    }
                }
            }
            // now actually print out the kit contents
            for (Map.Entry<String, TreeSet<String>> it : allKits.entrySet()) {
                w.print("KIT ");
                w.print(it.getKey());
                w.println();
                for (String m : it.getValue()) {
                    w.print("  CONTAINS " + m);
                    w.println();
                }
            }
        }
    }

    private boolean dependsOnTransitively(String kit1, String kit2, 
        TreeMap<String, TreeSet<String>> dependingKits) {
        TreeSet<String> kits = dependingKits.get(kit1);
        if (kits == null) {
            return false;
        }
        return kits.contains(kit2);
    }

    private static void registerModuleInKit(ModuleInfo module, String kit, TreeMap<String, TreeSet<String>> allKits) {
        TreeSet<String> modules = allKits.get(kit);
        if (modules == null) {
            modules = new TreeSet<>();
            allKits.put(kit, modules);
        }
        modules.add(module.getName(false));
    }

    private TreeMap<String, TreeSet<String>> transitiveClosureOfModules() {
        TreeMap<String, TreeSet<String>> moduleDepsAll = new TreeMap<>();
        // populate with modules first
        for (ModuleInfo m : modules) {
            TreeSet<String> deps = new TreeSet<>();
            moduleDepsAll.put(m.codebasename, deps);
            for (Dependency d : m.depends) {
                for (ModuleInfo theModuleOneIsDependingOn : findModuleInfo(d, m)) {
                    deps.add(theModuleOneIsDependingOn.codebasename);
                }
            }
        }
        transitiveClosure(moduleDepsAll);
        return moduleDepsAll;
    }
    
    
    private TreeMap<String, TreeSet<String>> transitiveClosureOfKits() {
        TreeMap<String, TreeSet<String>> kitDepsAll = new TreeMap<>();
        // populate with kits first
        for (ModuleInfo m : modules) {
            if (m.showInAutoupdate) {
                TreeSet<String> deps = new TreeSet<>();
                kitDepsAll.put(m.getName(false), deps);
                for (Dependency d : m.depends) {
                    for (ModuleInfo theModuleOneIsDependingOn : findModuleInfo(d, m)) {
                        if (theModuleOneIsDependingOn.showInAutoupdate) {
                            deps.add(theModuleOneIsDependingOn.getName(false));
                        }
                    }
                }
            }
        }
        transitiveClosure(kitDepsAll);
        return kitDepsAll;
    }
    
    /** Computes the transitive closure of the dependency map passed as a parameter.
     * 
     * @param deps the dependency map, will contain the transitive closure when the method exits
     */
    private <T> void transitiveClosure(Map<T,? extends Set<T>> allDeps) {
        boolean needAnotherIteration = true;
        while (needAnotherIteration) {
            needAnotherIteration = false;
            for (Map.Entry<T,? extends Set<T>> entry : allDeps.entrySet()) {
                Set<T> deps = entry.getValue();
                for (T d : new TreeSet<>(deps)) {
                    for (T d2: allDeps.get(d)) {
                        if (deps.add(d2)) {
                            log("transitive closure: need to add " + d2 + " to " + entry.getKey(), Project.MSG_DEBUG);
                            needAnotherIteration = true;
                        }
                    }
                }
            }
        }       
    }

    private void generateKitDependencies(File output) throws BuildException, IOException {
        try (PrintWriter w = new PrintWriter(new FileWriter(output))) {
            for (ModuleInfo m : modules) {
                if (regexp != null && !regexp.matcher(m.group).matches()) {
                    continue;
                }
                if (m.showInAutoupdate) {
                    w.print("KIT ");
                    w.print(m.getName(false));
                    w.println();
                    for (Dependency d : m.depends) {
                        if (regexp != null && !regexp.matcher(m.group).matches()) {
                            continue;
                        }
                        for (ModuleInfo theModuleOneIsDependingOn : findModuleInfo(d, m)) {
                            if (theModuleOneIsDependingOn.showInAutoupdate) {
                                w.print("  REQUIRES " + theModuleOneIsDependingOn.getName(false));
                                w.println();
                            }
                        }
                    }
                }
            }
        }
    }

    private void generatePlugins(File output) throws BuildException, IOException {
        Set<String> standardClusters = new HashSet<>();
        String standardClustersS = getProject().getProperty("clusters.config.full.list");
        if (standardClustersS != null) {
            for (String clusterProp : standardClustersS.split(",")) {
                String dir = getProject().getProperty(clusterProp + ".dir");
                if (dir != null) {
                    standardClusters.add(dir.replaceFirst("[0-9.]+$", ""));
                }
            }
        }
        try (FileWriter fw = new FileWriter(output)) {
            PrintWriter w = new PrintWriter(fw);
            SortedMap<String,String> lines = new TreeMap<>(Collator.getInstance());
            lines.put("A", "||Code Name Base||Display Name||Display Category||Standard Cluster");
            lines.put("C", "");
            lines.put("D", "||Code Name Base||Display Name||Display Category||Extra Cluster");
            for (ModuleInfo m : modules) {
                if (regexp != null && !regexp.matcher(m.group).matches()) {
                    continue;
                }
                if (m.showInAutoupdate) {
                    lines.put((standardClusters.contains(m.group) ? "B" : "E") + m.displayCategory + " " + m.displayName,
                            "|" + m.codebasename + "|" + m.displayName + "|" + m.displayCategory + "|" + m.group);
                }
            }
            for (String line : lines.values()) {
                w.println(line);
            }
            w.flush();
        }
    }

    private void generateReverseDependencies(File output) throws BuildException, IOException {
        try (FileWriter fw = new FileWriter(output)) {
            PrintWriter w = new PrintWriter(fw);
            for (ModuleInfo m : modules) {
                if (m.group.equals("extra")) {
                    continue;
                }
                String clusterDeps = getProject().getProperty("nb.cluster." + m.group + ".depends");
                if (clusterDeps == null) {
                    throw new BuildException("no property ${nb.cluster." + m.group + ".depends} defined");
                }
                Set<String> allowed = new HashSet<>();
                allowed.add(m.group);
                for (String piece : clusterDeps.split(",")) {
                    allowed.add(piece.replaceFirst("^nb[.]cluster[.]", ""));
                }
                for (Dependency d : m.depends) {
                    if (d.type == Dependency.Type.RECOMMENDS) {
                        continue;
                    }
                    for (ModuleInfo o : findModuleInfo(d, m)) {
                        if (o.codebasename.equals("org.netbeans.libs.junit4")) {
                            continue; // special case
                        }
                        if (!allowed.contains(o.group)) {
                            w.println(m.getName(false) + " -> " + o.getName(false));
                        }
                    }
                }
            }
            w.flush();
        }
    }

    private void generateSharedPackages (File output) throws BuildException, IOException {
        TreeMap<String,List<ModuleInfo>> packages = new TreeMap<>();

        for (ModuleInfo m : modules) {
            HashSet<String> pkgs = new HashSet<>();
            iterateSharedPackages(m.file, pkgs);
            for (String s : pkgs) {
                List<ModuleInfo> l = packages.get(s);
                if (l == null) {
                    l = new ArrayList<>();
                    packages.put(s, l);
                }
                l.add(m);
            }
        }

        try (PrintWriter w = new PrintWriter (new FileWriter (output))) {
            for (Map.Entry<String,List<ModuleInfo>> entry : packages.entrySet()) {
                String pkg = entry.getKey().replace('/', '.');
                if (pkg.equals("")) {
                    continue; // ignore default package
                }
                List<ModuleInfo> cnt = entry.getValue();
                if (cnt.size() > 1) {
                    SortedSet<String> cnbs = new TreeSet<>();
                    for (ModuleInfo m : cnt) {
                        if (regexp == null || regexp.matcher(m.group).matches()) {
                            cnbs.add(m.codebasename);
                        }
                    }
                    if (cnbs.size() > 1) {
                        w.println("PACKAGE " + pkg);
                        for (String cnb : cnbs) {
                            w.println("  MODULE " + cnb);
                        }
                    }
                }
            }
        }
    }

    private void iterateSharedPackages (File f, Set<String> myPkgs) throws IOException {
        try (JarFile file = new JarFile (f)) {
            Enumeration<JarEntry> en = file.entries ();
            LOOP: while(en.hasMoreElements()) {
                JarEntry e = en.nextElement();
                if (e.getName().endsWith ("/")) {
                    continue;
                }
                if (e.getName().startsWith ("META-INF/")) {
                    continue;
                }

                int last = e.getName().lastIndexOf('/');
                String pkg = last == -1 ? "" : e.getName().substring (0, last);
                myPkgs.add (pkg);
                log("Found package " + pkg + " in " + f, Project.MSG_DEBUG);
            }

            Manifest m = file.getManifest();
            if (m != null) {
                String value = m.getMainAttributes().getValue("Class-Path");
                if (value != null) {
                    StringTokenizer tok = new StringTokenizer (value, " ");
                    while (tok.hasMoreElements ()) {
                        File sub = new File(f.getParentFile(), tok.nextToken());
                        if (sub.isFile()) {
                            iterateSharedPackages (sub, myPkgs);
                        }
                    }
                }
            }
        }
    }
    
    private void generateDependencies (File output, boolean implementationOnly) throws BuildException, IOException {
        try (PrintWriter w = new PrintWriter(new FileWriter(output))) {
            for (ModuleInfo m : modules) {
                boolean first = true;
                Set<ModuleInfo> written = new HashSet<>(); // XXX needed for other uses of findModuleInfo too
                for (Dependency d : m.depends) {
                    if (d.getName().startsWith("org.openide.modules.ModuleFormat")) {
                        continue; // just clutter
                    }
                    String print = d.type == Dependency.Type.RECOMMENDS ? "  RECOMMENDS " : "  REQUIRES ";
                    if (d.exact && d.compare != null) {
                        // ok, impl deps
                    } else {
                        if (implementationOnly) {
                            continue;
                        }
                    }
                    if (regexp != null && !regexp.matcher(m.group).matches()) {
                        continue;
                    }

                    if (first) {
                        w.print ("MODULE ");
                        w.println(m.getName (false));
                        first = false;
                    }
                    if (d.isSpecial ()) {
                        w.print(print);
                        w.println(d.getName ());
                    } else {
                        for (ModuleInfo theModuleOneIsDependingOn : findModuleInfo(d, m)) {
                            if (written.add(theModuleOneIsDependingOn)) {
                                w.print(print);
                                w.println(theModuleOneIsDependingOn.getName(false));
                            }
                        }
                    }
                }
            }
        }
    }

    private void generateGroupDependencies (File output, boolean implementationOnly) throws BuildException, IOException {
        try (PrintWriter w = new PrintWriter (new FileWriter (output))) {
            Map<Dependency,Set<ModuleInfo>> referrers = new HashMap<>();

            TreeMap<String, Set<Dependency>> groups = new TreeMap<>();
            for (ModuleInfo m : modules) {
                if (regexp != null && !regexp.matcher(m.group).matches()) {
                    continue;
                }
                Set<Dependency> l = groups.get(m.group);
                if (l == null) {
                    l = new TreeSet<>();
                    groups.put(m.group, l);
                }
                Set<Dependency> deps = new HashSet<>();
                for (Dependency d : m.depends) {
                    if (implementationOnly && (!d.exact || d.compare == null)) {
                        continue;
                    }
                    // special dependencies are ignored
                    if (d.isSpecial ()) {
                        continue;
                    }
                    deps.add(d);
                }
                l.addAll(deps);
                for (Dependency d : deps) {
                    Set<ModuleInfo> r = referrers.get(d);
                    if (r == null) {
                        r = new HashSet<>();
                        referrers.put(d, r);
                    }
                    r.add(m);
                }
            }

            for (Map.Entry<String,Set<Dependency>> e : groups.entrySet()) {
                String groupName = e.getKey();
                Set<Dependency> depends = e.getValue();

                boolean first = true;
                for (Dependency d : depends) {
                    String print = d.type == Dependency.Type.RECOMMENDS ? "  RECOMMENDS ": "  REQUIRES ";
                    // dependencies within one group are not important
                    Set<ModuleInfo> r = referrers.get(d);
                    for (ModuleInfo ref : findModuleInfo(d, r.size() == 1 ? r.iterator().next() : null)) {
                        if (groupName.equals (ref.group)) {
                            continue;
                        }
                        if (first) {
                            w.print ("GROUP ");
                            w.print (groupName);
                            w.println ();
                            first = false;
                        }
                        w.print (print);
                        w.print (ref.getName (false));
                        w.println ();
                    }
                }
            }
        }
    }
    
    /** For a given dependency finds the module(s) that this dependency refers to.
     */
    private Set<ModuleInfo> findModuleInfo(Dependency dep, ModuleInfo referrer) throws BuildException {
        if (dep.isSpecial()) {
            return Collections.emptySet();
        }
        Set<ModuleInfo> result = new LinkedHashSet<>();
        for (ModuleInfo info : modules) {
            if (dep.isDependingOn (info)) {
                result.add(info);
            }
        }
        if (dep.type != Dependency.Type.RECOMMENDS && result.isEmpty()) {
            throw new BuildException ("Cannot find module that satisfies dependency: " + dep + (referrer != null ? " from: " + referrer : ""));
        }
        return result;
    }
    /** For a given codebasename finds module that we depend on
     */
    private ModuleInfo findModuleInfo (String cnb) throws BuildException {
        for (ModuleInfo info : modules) {
            if (info.codebasename.equals(cnb)) {
                return info;
            }
        }
        
        return null;
    }
    
    private static void addDependencies (TreeSet<Dependency> addTo, java.util.jar.Manifest man, Dependency.Type dependencyType, String attrName) throws BuildException {
        String value = man.getMainAttributes ().getValue (attrName);
        if (value == null) {
            return;
        }
        
        StringTokenizer tok = new StringTokenizer (value, ",");
        while (tok.hasMoreElements ()) {
            String nextDep = tok.nextToken ();
            StringTokenizer dep = new StringTokenizer (nextDep, "=>", true);
            if (dep.countTokens () == 1) {
                addTo.add (new Dependency (dep.nextToken ().trim (), dependencyType, false, null));
                continue;
            }

            if (dep.countTokens () == 3) {
                String name = dep.nextToken ().trim ();
                String equal = dep.nextToken ().trim ();
                String comp = dep.nextToken ().trim ();
                addTo.add (new Dependency (name, dependencyType, equal.equals ("="), comp));
                continue;
            }
            
            throw new BuildException ("Cannot parse dependency: " + value);
        }
    }
    
    public static final class Input extends Object {
        public FileSet jars;
        public String name;
        
        public FileSet createJars() {
            if (jars != null) throw new BuildException ();
            jars = new FileSet();
            return jars;
        }
        
        public void setName (String name) {
            this.name = name;
        }
    }

    public static class InputPattern {
        private File dir;
        public void setDir(File dir) {
            this.dir = dir;
        }
        Collection<Input> inputs() {
            List<Input> inputs = new ArrayList<>();
            for (File cluster : dir.listFiles()) {
                if (!new File(cluster, "update_tracking").isDirectory()) {
                    continue;
                }
                Input i = new Input();
                i.name = cluster.getName().replaceFirst("[0-9.]+$", "");
                i.jars = new FileSet();
                i.jars.setDir(cluster);
                i.jars.createInclude().setName("modules/*.jar");
                i.jars.createInclude().setName("lib/*.jar");
                i.jars.createInclude().setName("core/*.jar");
                inputs.add(i);
            }
            return inputs;
        }
    }
    
    public static final class Output extends Object {
        public OutputType type;
        public File file;
        
        public void setType (OutputType type) {
            this.type = type;
        }
        
        public void setFile (File file) {
            this.file = file;
        }
    }
    
    public static final class OutputType extends EnumeratedAttribute {
        public String[] getValues () {
            return new String[] { 
                "public-packages",
                "friend-packages",
                "shared-packages",
                "modules",
                "disabled-autoloads",
                "dependencies",
                "implementation-dependencies",
                "group-dependencies",
                "group-implementation-dependencies",
                "group-friend-packages",
                "external-libraries",
                "kits",
                "kit-dependencies",
                "plugins",
                "reverse-dependencies",
            };
        }
    }
    
    private static final class ModuleInfo extends Object implements Comparable<ModuleInfo> {
        public final String group;
        public final File file;
        public final String codebasename;
        public String publicPackages;
	public Set<String> friends;
        public int majorVersion;
        public String specificationVersion;
        public String implementationVersion;
        public Set<Dependency> depends;
        public Set<String> provides;
        public boolean showInAutoupdate;
        public boolean isEssential;
        public boolean isAutoload;
        public boolean isEager;
        public String displayName;
        public String displayCategory;
        
        public ModuleInfo (String g, File f, String a) {
            this.group = g;
            this.file = f;
            this.codebasename = a;
        }

        public int compareTo(ModuleInfo m) {
            return codebasename.compareTo (m.codebasename);
        }

        public @Override boolean equals(Object obj) {
            if (obj instanceof ModuleInfo) {
                return codebasename.equals(((ModuleInfo) obj).codebasename);
            }
            return false;
        }

        public @Override int hashCode() {
            return codebasename.hashCode ();
        }
        
        public String getName(boolean includeMajorVersion) {
            if (!includeMajorVersion || majorVersion == -1) {
                return codebasename + " (" + group + ")";
            } else {
                return codebasename + "/" + majorVersion + " (" + group + ")";
            }
        }

        public @Override String toString() {
            return "ModuleInfo[" + getName (false) + "]";
        }
    } // end of ModuleInfo
    
    private static final class Dependency extends Object implements Comparable<Dependency> {
        enum Type {DIRECT, REQUIRES, RECOMMENDS}
        
        public final String token;
        public final int majorVersionFrom;
        public final int majorVersionTo;
        public final Type type;
        public final boolean exact;
        public final String compare;
        
        
        public Dependency (String token, Type type, boolean exact, String compare) {
            // base name
            int slash = token.indexOf ('/');
            if (slash == -1) {
                this.token = token;
                this.majorVersionFrom = -1;
                this.majorVersionTo = -1;
            } else {
                this.token = token.substring (0, slash);
                
                String major = token.substring (slash + 1);
                int range = major.indexOf ('-');
                if (range == -1) {
                    this.majorVersionFrom = Integer.parseInt(major);
                    this.majorVersionTo = majorVersionFrom;
                } else {
                    this.majorVersionFrom = Integer.parseInt(major.substring(0, range));
                    this.majorVersionTo = Integer.parseInt(major.substring(range + 1));
                }
            }
            this.type = type;
            this.exact = exact;
            this.compare = compare;
        }
        public int compareTo(Dependency m) {
            return token.compareTo (m.token);
        }

        public @Override boolean equals(Object obj) {
            if (obj instanceof Dependency) {
                return token.equals(((Dependency) obj).token);
            }
            return false;
        }

        public @Override int hashCode() {
            return token.hashCode ();
        }
        
        /** These dependencies do not represent deps on real modules or
         * tokens provided by real modules.
         */
        public boolean isSpecial () {
            return token.startsWith ("org.openide.modules.os") ||
                   token.startsWith ("org.openide.modules.ModuleFormat") ||
                   token.equals("org.openide.modules.jre.JavaFX");
        }
        
        public boolean isDependingOn (ModuleInfo info) {
            switch (type) {
            case DIRECT:
                if (info.codebasename.equals (token)) {
                    return (majorVersionFrom == -1 || majorVersionFrom <= info.majorVersion) &&
                            (majorVersionTo == -1 || info.majorVersion <= majorVersionTo);
                } 
                break;
            default:
                for (String d : info.provides) {
                    if (d.equals(token)) {
                        return true;
                    }
                }
            }
            return false;
        }
        
        public String getName () {
            return token;
        }
        
        public @Override String toString() {
            return "Dependency[" + type + " " + getName () + "]";
        }

    } // end of Dependency
}
