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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.InvalidException;
import org.netbeans.Module;
import org.openide.modules.Dependency;
import org.openide.modules.SpecificationVersion;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.TopologicalSortException;

// XXX public for reflection from contrib/modulemanager; should be changed to friend dep!

/**
 * Utility class to provide localized messages explaining problems
 * that modules had during attempted installation.
 * Used by both {@link org.netbeans.core.startup.NbEvents} and autoupdate's ModuleBean.
 * @author Jesse Glick
 * @see "#16636"
 */
public final class NbProblemDisplayer {
    
    private NbProblemDisplayer() {}
    
    /**
     * Provide a localized explanation of some installation problem.
     * Problem may be either an InvalidException or a Dependency.
     * Structure of message can assume that the module failing its
     * dependencies is already being displayed, and concentrate
     * on the problem.
     * @param m the module which cannot be installed
     * @param problem either an {@link InvalidException} or {@link Dependency} as returned from {@link Module#getProblems}
     * @return an explanation of the problem in the most human-friendly format available
     */
    // XXX only exists for reflective calls
    public static String messageForProblem(Module m, Object problem) {
        return messageForProblem(m, problem, true);
    }
    /**
     * @param localized true to use display names, false to use code name bases
     */
    static String messageForProblem(Module m, Object problem, boolean localized) {
        if (problem instanceof InvalidException) {
            String loc = Exceptions.findLocalizedMessage((InvalidException) problem);
            return loc != null ? loc : problem.toString();
        } else {
            Dependency dep = (Dependency)problem;
            switch (dep.getType()) {
            case Dependency.TYPE_MODULE:
                String polite = (String)m.getLocalizedAttribute("OpenIDE-Module-Module-Dependency-Message"); // NOI18N
                if (polite != null) {
                    return polite;
                } else {
                    String name = dep.getName();
                    // Find code name base:
                    int idx = name.lastIndexOf('/');
                    if (idx != -1) {
                        name = name.substring(0, idx);
                    }
                    Module other = m.getManager().get(name);
                    if (other != null && other.getCodeName().equals(dep.getName())) {
                        switch (dep.getComparison()) {
                        case Dependency.COMPARE_ANY:
                            // Just disabled (probably had its own problems).
                            return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_other_disabled", label(other, localized));
                        case Dependency.COMPARE_IMPL:
                            String requestedI = dep.getVersion();
                            String actualI = (other.getImplementationVersion() != null) ?
                                other.getImplementationVersion() :
                                NbBundle.getMessage(NbProblemDisplayer.class, "LBL_no_impl_version");
                            if (requestedI.equals(actualI)) {
                                // Just disabled (probably had its own problems).
                                return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_other_disabled", label(other, localized));
                            } else {
                                // Wrong version.
                                return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_other_wrong_version", label(other, localized), requestedI, actualI);
                            }
                        case Dependency.COMPARE_SPEC:
                            SpecificationVersion requestedS = new SpecificationVersion(dep.getVersion());
                            SpecificationVersion actualS = (other.getSpecificationVersion() != null) ?
                                other.getSpecificationVersion() :
                                new SpecificationVersion("0"); // NOI18N
                            if (actualS.compareTo(requestedS) >= 0) {
                                // Just disabled (probably had its own problems).
                                return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_other_disabled", label(other, localized));
                            } else {
                                // Too old.
                                return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_other_too_old", label(other, localized), requestedS, actualS);
                            }
                        default:
                            throw new IllegalStateException();
                        }
                    } else {
                        // Keep the release version info in this case.
                        // XXX would be nice to have a special message for mismatched major release
                        // version - i.e. other != null
                        return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_other_needed_not_found", dep.getName());
                    }
                }
            case Dependency.TYPE_REQUIRES:
            case Dependency.TYPE_NEEDS:
                polite = (String)m.getLocalizedAttribute("OpenIDE-Module-Requires-Message"); // NOI18N
                if (polite != null) {
                    return polite;
                } else {
                    for (Module other : m.getManager().getModules()) {
                        if (other.provides(dep.getName())) {
                            return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_require_disabled", dep.getName());
                        }
                    }
                    return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_require_not_found", dep.getName());
                }
            case Dependency.TYPE_PACKAGE:
                polite = (String)m.getLocalizedAttribute("OpenIDE-Module-Package-Dependency-Message"); // NOI18N
                if (polite != null) {
                    return polite;
                } else {
                    String name = dep.getName();
                    // Find package name or qualified name of probe class:
                    int idx = name.lastIndexOf('[');
                    if (idx == 0) {
                        // Probed class. [javax.television.Antenna]
                        return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_class_not_loaded", name.substring(1, name.length() - 1));
                    } else if (idx != -1) {
                        // Package plus sample class. javax.television[Antenna]
                        return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_package_not_loaded_or_old", name.substring(0, idx));
                    } else {
                        return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_package_not_loaded_or_old", name);
                    }
                }
            case Dependency.TYPE_JAVA:
                // XXX would OpenIDE-Module-Java-Dependency-Message be useful?
                if (dep.getName().equals(Dependency.JAVA_NAME) && dep.getComparison() == Dependency.COMPARE_SPEC) {
                    return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_java_too_old", dep.getVersion(), Dependency.JAVA_SPEC);
                } else {
                    // All other usages unlikely, don't bother making pretty.
                    return dep.toString();
                }
            default:
                throw new IllegalArgumentException(dep.toString());
            }
        }
    }

    private static String label(Module m, boolean localized) {
        if (localized) {
            return m.getDisplayName();
        } else {
            return m.getCodeNameBase();
        }
    }

    static void problemMessagesForModules(final Appendable writeTo, Collection<? extends Module> modules, final boolean justRootCause) {
        try {
            HashSet<String> names = new HashSet<String>();
            for (Module m : modules) {
                names.add(m.getCodeName());
            }
            HashSet<String> dependentModules = new HashSet<String>();
            class Report {
                final Module m;
                final List<Object> problems = new ArrayList<Object>();
                Report(Module m) {
                    this.m = m;
                }
                void write() throws IOException {
                    for (Object problem : problems) {
                        writeTo.append("\n\t").append(label(m, justRootCause) + " - " + NbProblemDisplayer.messageForProblem(m, problem, justRootCause));
                    }
                }
            }
            Map<Module,Report> reports = new HashMap<Module,Report>();
            Map<Module,Collection<Module>> edges = new HashMap<Module,Collection<Module>>();
            for (Module m : modules) {
                Set<Object> problems = m.getProblems();
                if (problems.isEmpty()) {
                    throw new IllegalStateException("Module " + m + " could not be installed but had no problems"); // NOI18N
                }
                Report r = new Report(m);
                for (Object problem : problems) {
                    if (problem instanceof Dependency && justRootCause) {
                        Dependency d = (Dependency) problem;
                        if (d.getType() == Dependency.TYPE_MODULE && names.contains(d.getName())) {
                            dependentModules.add(m.getCodeName());
                            continue;
                        }
                    }
                    r.problems.add(problem);
                }
                reports.put(m, r);
                edges.put(m, m.getManager().getModuleInterdependencies(m, true, false, false));
            }
            try {
                for (Module m : BaseUtilities.topologicalSort(edges.keySet(), edges)) {
                    reports.get(m).write();
                }
            } catch (TopologicalSortException x) {
                for (Report r : reports.values()) {
                    r.write();
                }
            }
            if (!dependentModules.isEmpty()) {
                writeTo.append("\n\t").append(NbBundle.getMessage(NbProblemDisplayer.class, "MSG_also_dep_modules", dependentModules.size()));
            }
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
}
