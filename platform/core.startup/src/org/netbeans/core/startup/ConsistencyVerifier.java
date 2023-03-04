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

package org.netbeans.core.startup;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.Events;
import org.netbeans.InvalidException;
import org.netbeans.Module;
import org.netbeans.ModuleInstaller;
import org.netbeans.ModuleManager;
import org.openide.modules.Dependency;

/**
 * Utility class permitting you to verify that a set of modules could be enabled together.
 * Currently used from <code>org.netbeans.core.validation.ValidateUpdateCenterTest</code>.
 * @author Jesse Glick
 */
public class ConsistencyVerifier {

    private ConsistencyVerifier() {}

    /**
     * Find all expected installation problems for a set of modules.
     * Standard OS and module format tokens are provided, but all other dependencies
     * must be accessible from the set of modules supplied.
     * @param modules a set of module manifests to test together
     * @return a map from module code name bases, to sets of problems, expressed in an unspecified but readable format
     * @throws IllegalArgumentException if the set of modules is illegal (e.g. contains duplicates)
     */
    public static SortedMap<String,SortedSet<String>> findInconsistencies(Set<Manifest> modules) throws IllegalArgumentException {
        return findInconsistencies(modules, null);
    }
    /**
     * Find all expected installation problems for a set of modules.
     * Standard OS and module format tokens are provided, but all other dependencies
     * must be accessible from the set of modules supplied.
     * <p>The manifests may contain the pseudoattributes <code>autoload</code> and <code>eager</code>
     * which if set to <code>true</code> will mark the corresponding modules autoload or eager (resp.).
     * It is considered an error if some nondeprecated autoload modules would not be enabled unless explicitly whitelisted.
     * @param modules a set of module manifests to test together
     * @param permittedDisabledAutoloads if null, do not check autoload enablement;
     *                                   otherwise (not null but possibly empty) permit the listed modules (as CNBs) to be disabled
     * @return a map from module code name bases, to sets of problems, expressed in an unspecified but readable format
     * @throws IllegalArgumentException if the set of modules is illegal (e.g. contains duplicates)
     */
    public static SortedMap<String,SortedSet<String>> findInconsistencies(
            Set<Manifest> modules, Set<String> permittedDisabledAutoloads) throws IllegalArgumentException {
        return findInconsistencies(modules, permittedDisabledAutoloads, true);
    }
    /* accessible to test */ static SortedMap<String,SortedSet<String>> findInconsistencies(
            Set<Manifest> modules, Set<String> permittedDisabledAutoloads, boolean formatted) throws IllegalArgumentException {
        ModuleManager mgr = new ModuleManager(new DummyInstaller(), new DummyEvents());
        mgr.mutexPrivileged().enterWriteAccess();
        Manifest dummy = new Manifest();
        dummy.getMainAttributes().putValue("OpenIDE-Module", "__dummy__"); // NOI18N
        dummy.getMainAttributes().putValue("OpenIDE-Module-Provides",
                "org.openide.modules.ModuleFormat1, " + // NOI18N
                "org.openide.modules.ModuleFormat2, " + // NOI18N
                "org.openide.modules.jre.JavaFX, " + // NOI18N
                "org.openide.modules.os.Unix, " + // NOI18N
                "org.openide.modules.os.PlainUnix, " + // NOI18N
                "org.openide.modules.os.Windows, " + // NOI18N
                "org.openide.modules.os.MacOSX, " + // NOI18N
                "org.openide.modules.os.Linux, " + // NOI18N
                "org.openide.modules.os.Solaris, " + // NOI18N
                "org.openide.modules.os.OS2"); // NOI18N
        dummy.getMainAttributes().putValue("OpenIDE-Module-Public-Packages", "-"); // NOI18N
        try {
            mgr.createFixed(dummy, null, ClassLoader.getSystemClassLoader());
        } catch (Exception x) {
            throw new AssertionError(x);
        }
        Set<Module> mods = new HashSet<Module>();
        Set<Module> regularMods = new HashSet<Module>();
        for (Manifest m : modules) {
            final Attributes man = m.getMainAttributes();
            try {
                man.putValue("OpenIDE-Module-Public-Packages", "-"); // NOI18N
                man.remove(new Attributes.Name("OpenIDE-Module-Friends")); // NOI18N
                man.remove(new Attributes.Name("OpenIDE-Module-Localizing-Bundle")); // NOI18N
                String bsn = man.getValue("Bundle-SymbolicName"); // NOI18N
                String ver = man.getValue("Bundle-Version"); // NOI18N
                if (bsn != null && ver != null) {
                    bsn = bsn.replaceAll(";.*", "");
                    if (man.getValue("OpenIDE-Module") == null) { // NOI18N
                        man.putValue("OpenIDE-Module", bsn); // NOI18N
                    }
                    if (man.getValue("OpenIDE-Module-Specification-Version") == null) { // NOI18N
                        Matcher match = Pattern.compile("[0-9]*(\\.[0-9]*)?(\\.[0-9]*)?").matcher(ver);
                        if (match.find()) {
                            ver = match.group();
                        }
                        man.putValue("OpenIDE-Module-Specification-Version", ver.replace("(.*)", "")); // NOI18N
                    }
                }
                boolean autoload = "true".equals(man.getValue("autoload"));
                boolean eager = "true".equals(man.getValue("eager"));
                if (autoload) {
                    // discard dependency on JDK: will allow other modules, dependent on these autoloads, to enable
                    man.remove(new Attributes.Name("OpenIDE-Module-Java-Dependencies"));
                }
                Module mod = mgr.createFixed(m, null, ClassLoader.getSystemClassLoader(), autoload, eager);
                mods.add(mod);
                if (!autoload && !eager) {
                    regularMods.add(mod);
                }
            } catch (Exception x) {
                throw new IllegalArgumentException("Error parsing " + man.entrySet() + ": " + x, x);
            }
        }
        SortedMap<String,SortedSet<String>> problems = new TreeMap<String,SortedSet<String>>();
        List<Module> regularModsEnabled = mgr.simulateEnable(regularMods);
        for (Module m : mods) {
            String cnb = m.getCodeNameBase();
            Set<Object> probs = m.getProblems();
            if (probs.isEmpty()) {
                if (permittedDisabledAutoloads != null && !permittedDisabledAutoloads.contains(cnb) && m.isAutoload() &&
                        !regularModsEnabled.contains(m) && !"true".equals(m.getAttribute("OpenIDE-Module-Deprecated"))) {
                    problems.put(cnb, new TreeSet<String>(Collections.singleton("module is autoload but would not be enabled")));
                }
                continue;
            }
            SortedSet<String> probnames = new TreeSet<String>();
            for (Object prob : probs) {
                if (prob instanceof Dependency) {
                    Dependency d = (Dependency) prob;
                    if (Dependency.TYPE_JAVA == d.getType() && (m.isEager() || m.isAutoload())) {
                        continue;
                    }
                }
                String description;
                if (formatted) {
                    description = NbProblemDisplayer.messageForProblem(m, prob, false);
                } else {
                    description = prob.toString();
                }
                probnames.add(description);
            }
            if (!probnames.isEmpty()) {
                problems.put(cnb, probnames);
            }
        }
        return problems;
    }

    private static final class DummyInstaller extends ModuleInstaller {
        public void prepare(Module m) throws InvalidException {
            throw new AssertionError();
        }
        public void dispose(Module m) {
            throw new AssertionError();
        }
        public void load(List<Module> modules) {
            throw new AssertionError();
        }
        public void unload(List<Module> modules) {
            throw new AssertionError();
        }
        public boolean closing(List<Module> modules) {
            throw new AssertionError();
        }
        public void close(List<Module> modules) {
            throw new AssertionError();
        }
    }

    private static final class DummyEvents extends Events {
        protected void logged(String message, Object[] args) {}
    }

}
