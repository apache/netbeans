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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
                    if (Dependency.TYPE_JAVA == d.getType() && m.isAutoload()) {
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
