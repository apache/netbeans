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

package org.netbeans.modules.autoupdate.services;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.Util;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Dependency;
import org.openide.modules.SpecificationVersion;

class DependencyChecker extends Object {

    private static final Logger err = Logger.getLogger(DependencyChecker.class.getName ());
    
    public static Set<Dependency> findBrokenDependencies (Set<Dependency> deps, Collection<ModuleInfo> modules) {
        Set<Dependency> res = new HashSet<Dependency> ();
        for (Dependency dep : deps) {
            err.log(Level.FINE, "Dependency[" + dep.getType () + "]: " + dep);
            switch (dep.getType ()) {
                case (Dependency.TYPE_REQUIRES) :
                case (Dependency.TYPE_NEEDS) :
                    if (findModuleMatchesDependencyRequires (dep, modules).isEmpty ()) {
                        // bad, report missing module
                        res.add (dep);
                    } else {
                        // ok
                    }
                    break;
                case (Dependency.TYPE_RECOMMENDS) :
                    break;
                case (Dependency.TYPE_MODULE) :
                    if (matchDependencyModule (dep, modules) != null) {
                        // ok
                    } else {
                        // bad, report missing module
                        res.add (dep);
                    }
                    break;
                case (Dependency.TYPE_JAVA) :
                    if (! matchDependencyJava (dep)) {
                        err.log(Level.FINE, "The Java platform version " + dep +
                                " or higher was requested but only " + Dependency.JAVA_SPEC + " is running.");
                        res.add (dep);
                    }
                    break;
                case (Dependency.TYPE_PACKAGE) :
                    if (! matchPackageDependency (dep)) {
                        err.log(Level.FINE, "The package " + dep +
                                " was requested but it is not in current ClassPath.");
                        res.add (dep);
                    }
                    break;
                default:
                    //assert false : "Unknown type of Dependency, was " + dep.getType ();
                    err.log(Level.FINE, "Uncovered Dependency " + dep);                    
            }
        }
        return res;
    }
    
    public static Set<Dependency> findBrokenDependenciesTransitive (ModuleInfo info, Collection<ModuleInfo> modules, Set<ModuleInfo> seen) {
        if (seen.contains (info)) {
            return Collections.emptySet ();
        }
        seen.add (info);
        Set<Dependency> res = new HashSet<Dependency> ();
        for (Dependency dep : filterTypeRecommends (info.getDependencies ())) {
            err.log(Level.FINE, "Dependency[" + dep.getType () + "]: " + dep);
            Collection<ModuleInfo> providers = null;
            switch (dep.getType ()) {
                case (Dependency.TYPE_REQUIRES) :
                case (Dependency.TYPE_NEEDS) :
                case (Dependency.TYPE_RECOMMENDS) :
                    providers = findModuleMatchesDependencyRequires (dep, modules);
                    if (providers.size () > 0) {
                        for (ModuleInfo m : providers) {
                            res.addAll (findBrokenDependenciesTransitive (m, modules, seen));
                        }
                    } else {
                        // bad, report missing module
                        res.add (dep);
                    }
                    break;
                case (Dependency.TYPE_MODULE) :
                    ModuleInfo m = matchDependencyModule (dep, modules);
                    if (m != null) {
                        res.addAll (findBrokenDependenciesTransitive (m, modules, seen));
                    } else {
                        // bad, report missing module
                        res.add (dep);
                    }
                    break;
                case (Dependency.TYPE_JAVA) :
                    if (! matchDependencyJava (dep)) {
                        err.log(Level.FINE, "The Java platform version " + dep +
                                " or higher was requested but only " + Dependency.JAVA_SPEC + " is running.");
                        res.add (dep);
                    }
                    break;
                case (Dependency.TYPE_PACKAGE) :
                    if (! matchPackageDependency (dep)) {
                        err.log(Level.FINE, "The package " + dep +
                                " was requested but it is not in current ClassPath.");
                        res.add (dep);
                    }
                    break;
                default:
                    //assert false : "Unknown type of Dependency, was " + dep.getType ();
                    err.log(Level.FINE, "Uncovered Dependency " + dep);                    
            }
        }
        return res;
    }
    
    private static Set<Dependency> filterTypeRecommends (Collection<Dependency> deps) {
        Set<Dependency> res = new HashSet<Dependency> ();
        for (Dependency dep : deps) {
            if (Dependency.TYPE_RECOMMENDS != dep.getType ()) {
                res.add (dep);
            }
        }
        return res;
    }
    
    static Collection<ModuleInfo> findModuleMatchesDependencyRequires (Dependency dep, Collection<ModuleInfo> modules) {
        UpdateManagerImpl mgr = UpdateManagerImpl.getInstance ();
        Set<ModuleInfo> providers = new HashSet<ModuleInfo> ();
        providers.addAll (mgr.getAvailableProviders (dep.getName ()));
        providers.addAll (mgr.getInstalledProviders (dep.getName ()));
        Set<ModuleInfo> res = new HashSet<ModuleInfo> (providers);
        for (ModuleInfo mi : providers) {
            for (ModuleInfo input : modules) {
                if (mi.getCodeName ().equals (input.getCodeName ())) {
                    res.add (mi);
                }
            }
        }
        return res;
    }
    
    private static ModuleInfo matchDependencyModule (Dependency dep, Collection<ModuleInfo> modules) {
        for (ModuleInfo module : modules) {
            if (checkDependencyModule (dep, module)) {
                return module;
            }
        }
        
        return null;
    }
    
    public static boolean matchDependencyJava (Dependency dep) {
        if (dep.getName ().equals (Dependency.JAVA_NAME) && Dependency.COMPARE_SPEC == dep.getComparison ()) {
            return Dependency.JAVA_SPEC.compareTo (new SpecificationVersion (dep.getVersion ())) >= 0;
        }
        // All other usages unlikely
        return true;
    }
    
    public static boolean matchPackageDependency (Dependency dep) {
        if (dep.getName().equals("javafx.application[Application]")) {
            File javaHome = new File(System.getProperty("java.home"));
            return 
                new File(new File(javaHome, "lib"), "jfxrt.jar").exists() || 
                new File(new File(new File(javaHome, "lib"), "ext"), "jfxrt.jar").exists();
        } else {
            return Util.checkPackageDependency (dep, Util.class.getClassLoader());
        }        
    }
    
    static boolean checkDependencyModuleAllowEqual (Dependency dep, ModuleInfo module) {
        return checkDependencyModule (dep, module, true);
    }
    
    static boolean checkDependencyModule (Dependency dep, ModuleInfo module) {
        return checkDependencyModule (dep, module, false);
    }
    
    private static boolean checkDependencyModule (Dependency dep, ModuleInfo module, boolean allowEqual) {

        boolean ok = false;
        
        if (dep.getName ().equals (module.getCodeNameBase ()) || dep.getName ().equals (module.getCodeName ())) {
            if (dep.getComparison () == Dependency.COMPARE_ANY) {
                ok = true;
            } else if (dep.getComparison () == Dependency.COMPARE_SPEC) {
                    if (module.getSpecificationVersion () == null) {
                        ok = false;
                    } else if (new SpecificationVersion (dep.getVersion ()).compareTo (module.getSpecificationVersion ()) > 0) {
                        ok = false;
                    } else if (allowEqual && new SpecificationVersion (dep.getVersion ()).compareTo (module.getSpecificationVersion ()) == 0) {
                        ok = true;
                    } else {
                        ok = true;
                    }
            } else {
                // COMPARE_IMPL
                if (module.getImplementationVersion () == null) {
                    ok = false;
                } else if (! module.getImplementationVersion ().equals (dep.getVersion ())) {
                    ok = false;
                } else if (dep.getName ().indexOf ('/') == -1 || module.getCodeName().indexOf('/') !=-1) { // NOI18N
                    //COMPARE_IMPL with implicit release version specified - see Issue #177737
                    if(dep.getName ().equals (module.getCodeName ())) {
                        //release version specified in both dependency and module codename, and the same - since different release versions are handled separately
                        ok  = true;
                    } else {
                        ok = false;
                    }
                } else {
                    ok = true;
                }
            }
            
        } else {

            int dash = dep.getName ().indexOf ('-'); // NOI18N
            if (dash != -1) {
                // Ranged major release version, cf. #19714.
                int slash = dep.getName ().indexOf ('/'); // NOI18N
                String cnb = dep.getName ().substring (0, slash);
                int relMin = Integer.parseInt (dep.getName ().substring (slash + 1, dash));
                int relMax = Integer.parseInt (dep.getName ().substring (dash + 1));
                if (cnb.equals (module.getCodeNameBase ()) &&
                        relMin <= module.getCodeNameRelease () &&
                        relMax >= module.getCodeNameRelease ()) {
                    if (dep.getComparison () == Dependency.COMPARE_ANY) {
                        ok = true;
                    } else {
                        // COMPARE_SPEC; COMPARE_IMPL not allowed here
                        if (module.getCodeNameRelease () > relMin) {
                            // Great, skip the spec version.
                            ok = true;
                        } else {
                            // As usual.
                            if (module.getSpecificationVersion () == null) {
                                ok = false;
                            } else if (new SpecificationVersion (dep.getVersion ()).compareTo (module.getSpecificationVersion ()) > 0) {
                                ok = false;
                            } else if (allowEqual && new SpecificationVersion (dep.getVersion ()).compareTo (module.getSpecificationVersion ()) > 0) {
                                ok = true;
                            } else {
                                ok = true;
                            }
                        }
                    }
                }
            } else if (dep.getName ().indexOf ('/') != -1) { // NOI18N
                // MAJOR RELEASE
                String cnb = dep.getName ().substring (0, dep.getName ().indexOf ('/')); // NOI18N
                if (cnb.equals (module.getCodeNameBase ())) {
                    err.log (Level.FINE, "Unmatched major versions. Dependency " + dep + " doesn't match with module " + module);
                    ok = false;
                }
            }
        }

        return ok;
    }

}
